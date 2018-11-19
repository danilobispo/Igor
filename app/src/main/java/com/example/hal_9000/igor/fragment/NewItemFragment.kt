package com.example.hal_9000.igor.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
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
import com.example.hal_9000.igor.LoginActivity
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.StatsListAdapter
import com.example.hal_9000.igor.model.Atributo
import com.example.hal_9000.igor.model.Evento
import com.example.hal_9000.igor.model.Item
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_new_character.*
import kotlinx.android.synthetic.main.fragment_new_character.view.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class NewItemFragment : Fragment() {

    private val TAG = "NewItemFragment"
    private lateinit var itemOld: Item
    private lateinit var owner: String

    private lateinit var etName: EditText
    private lateinit var spinnerType: Spinner
    private lateinit var etDescription: EditText
    private lateinit var rvStats: RecyclerView
    private lateinit var ivPhoto: ImageView
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: StatsListAdapter
    private val arrayStats = ArrayList<Atributo>()

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var downloadUrl: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_new_item, container, false)

        etName = view.findViewById(R.id.et_name)
        spinnerType = view.findViewById(R.id.spinner_type)
        etDescription = view.findViewById(R.id.et_description)
        rvStats = view.findViewById(R.id.rv_stats)
        ivPhoto = view.findViewById(R.id.iv_photo)
        progressBar = view.findViewById(R.id.progressBar)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        itemOld = NewItemFragmentArgs.fromBundle(arguments).item
        owner = NewItemFragmentArgs.fromBundle(arguments).newOwner

        adapter = StatsListAdapter(arrayStats)
        rvStats.layoutManager = LinearLayoutManager(context)
        rvStats.setHasFixedSize(true)
        rvStats.adapter = adapter

        if (itemOld.name.isNotEmpty())
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
        etName.setText(itemOld.name)
        if (itemOld.type == "Equipamento") spinnerType.setSelection(0)
        else if (itemOld.type == "Consumível") spinnerType.setSelection(1)
        etDescription.setText(itemOld.description)

        for (atributo in itemOld.stats) {
            arrayStats.add(atributo)
            adapter.notifyItemInserted(arrayStats.size - 1)
        }

        if (itemOld.image_url.isNotEmpty()) {
            Glide.with(this)
                    .load(itemOld.image_url)
                    .into(ivPhoto)
            downloadUrl = itemOld.image_url
        }
    }

    private fun concluirCriacao() {

        if (etName.text.isEmpty()) {
            Toast.makeText(context, "Preencha ao menos o nome do item", Toast.LENGTH_SHORT).show();
            return
        }

        progressBar.visibility = View.VISIBLE

        val item = Item()
        item.name = etName.text.toString()
        item.type = spinnerType.selectedItem.toString()
        item.description = etDescription.text.toString()
        item.image_url = downloadUrl

        Log.d(TAG, "${item.name}, ${item.type}, ${item.description}, ${item.owner}")

        item.stats = arrayStats

        if (owner.isEmpty())
            item.owner = LoginActivity.username
        else
            item.owner = owner

        if (itemOld.name.isEmpty()) {
            item.id = "${item.name}_${System.currentTimeMillis()}"
            item.equipped = false
            createItem(item, false)
        } else {
            item.id = itemOld.id
            item.equipped = itemOld.equipped
            createItem(item, true)
        }
    }

    private fun createItem(item: Item, edit: Boolean = false) {

        val batch = db.batch()

        val itemRef = db
                .collection("adventures")
                .document(AdventureFragment.aventura.id)
                .collection("items")
                .document(item.id)

        batch.set(itemRef, item)

        if (item.owner != AdventureFragment.aventura.creator) {
            val eventsRef =
                    db.collection("adventures")
                            .document(AdventureFragment.aventura.id)
                            .collection("sessions")
                            .document(SessionFragment.sessionId)
                            .collection("events")

            val event = Evento(type = "item", date = System.currentTimeMillis())
            event.event = "${item.owner} recebeu ${item.name}"
            batch.set(eventsRef.document(event.date.toString()), event)
        }

        batch.commit()
                .addOnSuccessListener {
                    Log.d(TAG, "Document ${item.id} created successfully")
                    if (edit)
                        Toast.makeText(context, "Item atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(context, "Item criado com sucesso!", Toast.LENGTH_SHORT).show()
                    NavHostFragment.findNavController(this).popBackStack()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error creating document", e)
                    if (edit)
                        Toast.makeText(context, "Erro ao atualizar item", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(context, "Erro ao criar item", Toast.LENGTH_SHORT).show()
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
        if (filePath == null) return

        btn_concluir.isEnabled = false

        progressBar.visibility = View.VISIBLE

        val ref = storageReference.child("images/" + UUID.randomUUID().toString())
        ref.putFile(filePath!!)
                .addOnSuccessListener { taskSnapshot ->
                    Log.d(TAG, "Image upload successfully")
                    ref.downloadUrl.addOnSuccessListener {
                        Log.d(TAG, "DownloadUrl: $taskSnapshot")
                        downloadUrl = taskSnapshot.toString()
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
