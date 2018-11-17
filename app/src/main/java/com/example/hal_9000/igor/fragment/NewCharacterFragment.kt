package com.example.hal_9000.igor.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.example.hal_9000.igor.LoginActivity
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.StatsListAdapter
import com.example.hal_9000.igor.model.Atributo
import com.example.hal_9000.igor.model.Aventura
import com.example.hal_9000.igor.model.Personagem
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_new_character.*
import kotlinx.android.synthetic.main.fragment_new_character.view.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class NewCharacterFragment : Fragment() {

    private val TAG = "NewCharacterFragment"
    private var aventura: Aventura? = null
    private lateinit var personagemOld: Personagem
    private var isNPC: Boolean = false

    private lateinit var etNome: EditText
    private lateinit var etClasse: EditText
    private lateinit var etDescricao: EditText
    private lateinit var etHealth: EditText
    private lateinit var lvStats: ListView
    private lateinit var ivPhoto: ImageView
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: StatsListAdapter

    private lateinit var db: FirebaseFirestore
    private lateinit var aventuraId: String

    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private val PICK_IMAGE_REQUEST = 71

    private var filePath: Uri? = null
    private var downloadUrl: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_new_character, container, false)

        aventura = NewCharacterFragmentArgs.fromBundle(arguments).aventura
        personagemOld = NewCharacterFragmentArgs.fromBundle(arguments).personagem
        isNPC = NewCharacterFragmentArgs.fromBundle(arguments).isNpc

        etNome = view.findViewById(R.id.et_nome)
        etClasse = view.findViewById(R.id.et_classe)
        etDescricao = view.findViewById(R.id.et_descricao)
        etHealth = view.findViewById(R.id.et_hp)
        lvStats = view.findViewById(R.id.lv_stats)
        ivPhoto = view.findViewById(R.id.iv_photo)
        progressBar = view.findViewById(R.id.progressBar)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        aventuraId = "${aventura?.creator}_${aventura?.title}"

        val arrayOfStats = ArrayList<Atributo>()
        adapter = StatsListAdapter(context!!, arrayOfStats)
        lvStats.adapter = adapter

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
                        adapter.add(stat)
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

        if (personagemOld.healthMax != -1)
            etHealth.setText(personagemOld.healthMax.toString(), TextView.BufferType.EDITABLE)

        for (atributo in personagemOld.atributos) {
            adapter.add(atributo)
        }

        if (personagemOld.imageUrl.isNotEmpty()) {
            Glide.with(this)
                    .load(personagemOld.imageUrl)
                    .into(ivPhoto)

            downloadUrl = personagemOld.imageUrl
        }
    }

    private fun concluirCriacao() {
        progressBar.visibility = View.VISIBLE

        uploadImage()

        val personagem = Personagem()
        personagem.nome = etNome.text.toString()
        personagem.classe = etClasse.text.toString()
        personagem.descricao = etDescricao.text.toString()

        if (etHealth.text.isNotEmpty())
            personagem.healthMax = Integer.valueOf(etHealth.text.toString())

        personagem.creator = LoginActivity.username
        personagem.imageUrl = downloadUrl
        personagem.isNpc = isNPC
        personagem.isMaster = false

        Log.d(TAG, "${personagem.nome}, ${personagem.classe}, ${personagem.descricao}, ${personagem.healthMax}")

        for (i in 0 until adapter.count)
            personagem.atributos.add(adapter.getItem(i)!!)

        personagem.aventuraId = aventuraId

        if (personagemOld.id.isEmpty()) {
            personagem.created = System.currentTimeMillis() / 1000
            personagem.health = personagem.healthMax
            createCharacter(personagem)

        } else {
            personagem.id = personagemOld.id
            personagem.created = personagemOld.created
            personagem.health = personagemOld.health
            editCharacter(personagem)
        }
    }

    private fun createCharacter(personagem: Personagem) {
        db.collection("characters")
                .add(personagem)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "Document ${documentReference.id} created successfully")
                    Toast.makeText(context, "Personagem criado com sucesso!", Toast.LENGTH_SHORT).show()
                    db.collection("characters").document(documentReference.id).update("id", documentReference.id)
                    db.collection("adventures").document(aventuraId).update("players." + personagem.nome, true)
                    NavHostFragment.findNavController(this).popBackStack()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error creating document", e)
                    Toast.makeText(context, "Erro ao criar personagem", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE
                }
    }

    private fun editCharacter(personagem: Personagem) {
        db.collection("characters")
                .document(personagem.id)
                .set(personagem)
                .addOnSuccessListener {
                    Log.d(TAG, "Document updated successfully")
                    Toast.makeText(context, "Personagem atualizado com sucesso!", Toast.LENGTH_SHORT).show()

                    if (!isNPC && personagemOld.nome != personagem.nome) {
                        if (personagem.nome.isNotEmpty())
                            db.collection("adventures").document(aventuraId).update("players." + personagem.nome, true)
                        if (personagemOld.nome.isNotEmpty())
                            db.collection("adventures").document(aventuraId).update("players." + personagemOld.nome, FieldValue.delete())
                    }

                    NavHostFragment.findNavController(this).popBackStack()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating document", e)
                    Toast.makeText(context, "Erro ao atualizar personagem", Toast.LENGTH_SHORT).show()
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
        if (requestCode === PICK_IMAGE_REQUEST && resultCode === RESULT_OK
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

        btn_concluir.isEnabled = false

        if (filePath != null) {
            progressBar.visibility = View.VISIBLE

            val ref = storageReference.child("images/" + UUID.randomUUID().toString())
            ref.putFile(filePath!!)
                    .addOnSuccessListener { it ->
                        Log.d(TAG, "Image upload successfully")
                        ref.downloadUrl.addOnSuccessListener {
                            Log.d(TAG, "DownloadUrl: $it")
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
}
