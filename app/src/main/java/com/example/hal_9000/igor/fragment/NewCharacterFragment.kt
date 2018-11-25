package com.example.hal_9000.igor.fragment

import android.app.Activity.RESULT_OK
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.example.hal_9000.igor.viewmodel.MainViewModel
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.StatsListAdapter
import com.example.hal_9000.igor.model.Atributo
import com.example.hal_9000.igor.model.Personagem
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_new_character.*
import kotlinx.android.synthetic.main.fragment_new_character.view.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class NewCharacterFragment : Fragment() {

    private val TAG = "NewCharacterFragment"

    private lateinit var personagemOld: Personagem
    private var isNPC: Boolean = false

    private lateinit var etNome: EditText
    private lateinit var etClasse: EditText
    private lateinit var etDescricao: EditText
    private lateinit var etHealth: EditText
    private lateinit var rvStats: RecyclerView
    private lateinit var ivPhoto: ImageView
    private lateinit var switchHidden: Switch
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: StatsListAdapter
    private val arrayStats = ArrayList<Atributo>()

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var downloadUrl: String = ""

    private lateinit var model: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_new_character, container, false)

        personagemOld = NewCharacterFragmentArgs.fromBundle(arguments).personagem
        isNPC = NewCharacterFragmentArgs.fromBundle(arguments).isNpc

        etNome = view.findViewById(R.id.et_nome)
        etClasse = view.findViewById(R.id.et_classe)
        etDescricao = view.findViewById(R.id.et_descricao)
        etHealth = view.findViewById(R.id.et_hp)
        rvStats = view.findViewById(R.id.rv_stats)
        ivPhoto = view.findViewById(R.id.iv_photo)
        switchHidden = view.findViewById(R.id.swith_hidden)
        progressBar = view.findViewById(R.id.progressBar)

        model = activity!!.run {
            ViewModelProviders.of(this).get(MainViewModel::class.java)
        }

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        adapter = StatsListAdapter(arrayStats)
        rvStats.layoutManager = LinearLayoutManager(context)
        rvStats.setHasFixedSize(true)
        rvStats.adapter = adapter

        if (isNPC)
            etNome.hint = "Nome"

        if (personagemOld.id.isNotEmpty())
            completeFields()

        view.btn_add_stat.setOnClickListener { newStat() }

        view.btn_concluir.setOnClickListener { concluirCriacao() }

        view.iv_photo.setOnClickListener { chooseImage() }

        return view
    }

    private fun newStat() {
        fun showStatValueDialog(stat: Atributo) {
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT

            AlertDialog.Builder(view!!.context)
                    .setTitle("Novo atributo")
                    .setMessage("Digite o valor do atributo")
                    .setView(input)
                    .setPositiveButton("OK") { _, _ ->
                        stat.valor = input.text.toString()
                        arrayStats.add(stat)
                        adapter.notifyItemInserted(arrayStats.size - 1)
                    }
                    .show()
        }

        fun showStatNameDialog(stat: Atributo) {
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT

            AlertDialog.Builder(view!!.context)
                    .setTitle("Novo atributo")
                    .setMessage("Digite o nome do atributo")
                    .setView(input)
                    .setPositiveButton("OK") { _, _ ->
                        stat.nome = input.text.toString()
                        showStatValueDialog(stat)
                    }
                    .show()
        }

        val stat = Atributo()
        showStatNameDialog(stat)
    }

    private fun completeFields() {
        etNome.setText(personagemOld.nome)
        etClasse.setText(personagemOld.classe)
        etDescricao.setText(personagemOld.descricao)
        switchHidden.isChecked = personagemOld.hidden

        if (personagemOld.health_max != -1)
            etHealth.setText(personagemOld.health_max.toString(), TextView.BufferType.EDITABLE)

        for (atributo in personagemOld.atributos) {
            arrayStats.add(atributo)
            adapter.notifyItemInserted(arrayStats.size - 1)
        }

        if (personagemOld.image_url.isNotEmpty()) {
            Glide.with(this)
                    .load(personagemOld.image_url)
                    .into(ivPhoto)

            downloadUrl = personagemOld.image_url
        }
    }

    private fun concluirCriacao() {
        progressBar.visibility = View.VISIBLE

        val personagem = Personagem()
        personagem.nome = etNome.text.toString()
        personagem.classe = etClasse.text.toString()
        personagem.descricao = etDescricao.text.toString()
        personagem.hidden = switchHidden.isChecked

        if (etHealth.text.isNotEmpty())
            personagem.health_max = Integer.valueOf(etHealth.text.toString())

        personagem.creator = model.getUsername()!!
        personagem.image_url = downloadUrl
        personagem.isnpc = isNPC
        personagem.atributos = arrayStats
        personagem.aventura_id = model.getAdventure()!!.id

        Log.d(TAG, "${personagem.nome}, ${personagem.classe}, ${personagem.descricao}, ${personagem.health_max}, ${personagem.atributos}")

        if (personagemOld.id.isEmpty()) {
            personagem.created_at = System.currentTimeMillis()
            personagem.id = "${personagem.creator}_${personagem.created_at}"
            personagem.ismaster = false
            personagem.health = personagem.health_max
            createCharacter(personagem, false)

        } else {
            personagem.ismaster = personagemOld.ismaster
            personagem.id = personagemOld.id
            personagem.created_at = personagemOld.created_at
            personagem.health = personagemOld.health
            createCharacter(personagem, true)
        }
    }

    private fun createCharacter(personagem: Personagem, edit: Boolean) {

        val batch: WriteBatch = db.batch()

        batch.set(db.collection("characters").document(personagem.id), personagem)

        batch.update(db.collection("adventures").document(model.getAdventure()!!.id), "players." + personagem.nome, true)

        // Remove old name
        if (edit && !isNPC && personagemOld.nome.isNotEmpty() && personagemOld.nome != personagem.nome)
            batch.update(db.collection("adventures").document(model.getAdventure()!!.id), "players." + personagemOld.nome, FieldValue.delete())

        batch.commit()
                .addOnSuccessListener {
                    Log.d(TAG, "Document ${personagem.id} created_at successfully. Edit: $edit")
                    if (edit)
                        Toast.makeText(context, "Personagem atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(context, "Personagem criado com sucesso!", Toast.LENGTH_SHORT).show()
                    NavHostFragment.findNavController(this).popBackStack()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error creating document. Edit: $edit", e)
                    if (edit)
                        Toast.makeText(context, "Erro ao editar personagem", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(context, "Erro ao criar personagem", Toast.LENGTH_SHORT).show()

                    progressBar.visibility = View.INVISIBLE
                }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Selecione um imagem"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.data != null) {
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, filePath)
                iv_photo.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            uploadImage()
        }
    }

    private fun uploadImage() {
        if (filePath == null) return

        btn_concluir.isEnabled = false

        progressBar.visibility = View.VISIBLE

        val ref = storageReference.child("images/" + UUID.randomUUID().toString())
        ref.putFile(filePath!!)
                .addOnSuccessListener { taskSnapshot ->
                    Log.d(TAG, "Image upload successfully")
                    ref.downloadUrl.addOnSuccessListener {
                        Log.d(TAG, "DownloadUrl: $taskSnapshot")
                        downloadUrl = it.toString()
                        progressBar.visibility = View.INVISIBLE
                        btn_concluir.isEnabled = true
                    }
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.INVISIBLE
                    Log.e(TAG, "Error: $e.message")
                    Toast.makeText(context, "Erro ao enviar imagem " + e.message, Toast.LENGTH_SHORT).show()
                    btn_concluir.isEnabled = true
                }
    }
}
