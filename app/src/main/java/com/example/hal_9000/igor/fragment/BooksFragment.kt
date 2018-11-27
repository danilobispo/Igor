package com.example.hal_9000.igor.fragment


import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.BooksListAdapter
import com.example.hal_9000.igor.model.Book
import com.example.hal_9000.igor.viewmodel.MainViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class BooksFragment : Fragment() {

    private val TAG = "BooksFragment"
    private val PICK_DOCUMENT_REQUEST = 72

    private lateinit var fabUpload: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: BooksListAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    private lateinit var model: MainViewModel

    private var bookTitle = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_books, container, false)

        setHasOptionsMenu(false)

        model = activity!!.run {
            ViewModelProviders.of(this).get(MainViewModel::class.java)
        }

        fabUpload = view.findViewById(R.id.fab_upload_document)
        progressBar = view.findViewById(R.id.progress_bar)
        recyclerView = view.findViewById(R.id.rv_documents)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        val query = db.collection("books")

        val options = FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book::class.java)
                .build()

        adapter = BooksListAdapter(options) { book: Book -> bookItemClicked(book) }
        recyclerView.adapter = adapter

        fabUpload.setOnClickListener { chooseDocument() }

        return view
    }

    private fun bookItemClicked(book: Book) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(book.url), "application/pdf")
        startActivity(intent)
    }

    private fun chooseDocument() {
        fun selectDocument() {
            val intent = Intent()
            intent.type = "application/pdf"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Selecione um documento PDF"), PICK_DOCUMENT_REQUEST)
        }

        fun showBookTitleChooserDialog() {
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT

            AlertDialog.Builder(view!!.context)
                    .setTitle("Enviar livro")
                    .setMessage("Digite o título do livro")
                    .setView(input)
                    .setPositiveButton("OK") { _, _ ->
                        if (input.text.isNotEmpty()) {
                            bookTitle = input.text.toString()
                            selectDocument()
                        }
                    }
                    .show()
        }

        showBookTitleChooserDialog()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_DOCUMENT_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.data != null) {
            uploadDocument(data.data!!)
        }
    }

    private fun uploadDocument(filePath: Uri) {
        progressBar.visibility = View.VISIBLE
        Toast.makeText(context, "Enviando livro...", Toast.LENGTH_SHORT).show()

        val ref = storageReference.child("documents/" + UUID.randomUUID().toString() + ".pdf")
        ref.putFile(filePath)
                .addOnProgressListener {
                    Log.d(TAG, "Progress: ${(100 * it.bytesTransferred / it.totalByteCount).toInt()}")
                    progressBar.progress = (100 * it.bytesTransferred / it.totalByteCount).toInt()
                }
                .addOnSuccessListener { taskSnapshot ->
                    Log.d(TAG, "Document upload successfully")
                    ref.downloadUrl.addOnSuccessListener {
                        Log.d(TAG, "DownloadUrl: $taskSnapshot")
                        insertDatabase(it.toString())
                    }
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.INVISIBLE
                    Log.e(TAG, "Error: $e.message")
                    Toast.makeText(context, "Erro ao enviar documento" + e.message, Toast.LENGTH_SHORT).show()
                }
    }

    private fun insertDatabase(url: String) {
        val book = Book(
                title = bookTitle,
                url = url,
                uploaded_at = System.currentTimeMillis(),
                uploader = model.getUsername()!!)

        db.collection("books").add(book)
                .addOnSuccessListener {
                    progressBar.visibility = View.INVISIBLE
                    Log.d(TAG, "Document created successfully")
                    Toast.makeText(context, "Livro enviado com sucesso!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.INVISIBLE
                    Log.w(TAG, "Error creating document", e)
                    Toast.makeText(context, "Erro ao enviar livro", Toast.LENGTH_SHORT).show()
                }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}
