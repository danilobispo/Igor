package com.example.hal_9000.igor.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.AdventureRecyclerViewAdapter
import com.example.hal_9000.igor.model.Aventura
import com.example.hal_9000.igor.model.Session
import com.example.hal_9000.igor.viewmodel.MainViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"

    private lateinit var fabNovaAventura: FloatingActionButton
    private lateinit var fabSaveEdit: FloatingActionButton
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: AdventureRecyclerViewAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore

    private var editMode = false

    private lateinit var model: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        setHasOptionsMenu(true)

        fabNovaAventura = view.findViewById(R.id.fab_nova_aventura)
        fabSaveEdit = view.findViewById(R.id.fab_save_edit)
        progressBar = view.findViewById(R.id.progress_bar)
        mRecyclerView = view.findViewById(R.id.rv_adventures_list)
        db = FirebaseFirestore.getInstance()

        model = activity!!.run {
            ViewModelProviders.of(this).get(MainViewModel::class.java)
        }

        model.setUsername(FirebaseAuth.getInstance().currentUser?.displayName.toString())

        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.setHasFixedSize(true)

        val query = db
                .collection("adventures")
                .whereEqualTo("players.${model.getUsername()!!}", true)

        val options = FirestoreRecyclerOptions.Builder<Aventura>()
                .setQuery(query, Aventura::class.java)
                .build()

        adapter = AdventureRecyclerViewAdapter(options, model.getUsername()!!, { aventura: Aventura -> aventuraItemClicked(aventura) }, { aventura: Aventura, itemView: View -> aventuraItemLongClicked(aventura, itemView) }, { aventura: Aventura -> aventuraDeleteItemClicked(aventura) })
        mRecyclerView.adapter = adapter

        fabNovaAventura.setOnClickListener {
            val action = HomeFragmentDirections.ActionHomeFragmentToNewAdventure()
            NavHostFragment.findNavController(this).navigate(action)
        }

        fabSaveEdit.setOnClickListener { setEditModeOff() }

        return view
    }

    private fun aventuraItemClicked(aventura: Aventura) {
        val action = HomeFragmentDirections.ActionHomeFragmentToAdventureFragment(aventura)
        action.setAventura(aventura)
        findNavController(this).navigate(action)
    }

    private fun aventuraItemLongClicked(aventura: Aventura, itemView: View) {
        if (aventura.creator != model.getUsername()) return

        val popup = PopupMenu(context!!, itemView)
        popup.inflate(R.menu.popup_menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    val action = HomeFragmentDirections.ActionHomeFragmentToNewAdventure()
                    action.setAventura(aventura)
                    NavHostFragment.findNavController(this).navigate(action)
                }
                R.id.menu_delete -> {
                    aventuraDeleteItemClicked(aventura)
                }
            }
            true
        }
        popup.gravity = Gravity.END
        popup.show()
    }

    private fun aventuraDeleteItemClicked(aventura: Aventura) {
        AlertDialog.Builder(context!!)
                .setTitle("Deletar aventura")
                .setMessage("Tem certeza que deseja deletar a aventura  ${aventura.title}?")
                .setPositiveButton("Deletar") { _, _ ->
                    deleteAdventure(aventura)
                }
                .setNegativeButton("Cancelar") { _, _ ->
                    Toast.makeText(context, "Acão cancelada", Toast.LENGTH_SHORT).show()
                }
                .show()
    }

    private fun deleteAdventure(adventure: Aventura) {
        val batch = db.batch()
        var completionCounter = 5

        fun batchCommit() {
            completionCounter -= 1
            Log.d(TAG, "batchCommit: $completionCounter")
            if (completionCounter != 0) return

            batch.commit()
                    .addOnSuccessListener {
                        Log.d(TAG, "Batch delete success")
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "Aventura deletada com sucesso", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Batch delete error", e)
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "Erro ao deletar aventura", Toast.LENGTH_SHORT).show()
                    }
        }

        Log.d(TAG, "Deleting adventure ${adventure.title}")
        progressBar.visibility = View.VISIBLE

        Log.d(TAG, "Querying sessions")
        db.collection("adventures")
                .document(adventure.id)
                .collection("sessions")
                .get()
                .addOnSuccessListener { sessions ->
                    Log.d(TAG, "Queried ${sessions.size()} session documents")
                    for (document in sessions.documents) {
                        val session = document.toObject(Session::class.java)!!

                        Log.d(TAG, "Querying dices")

                        db.collection("adventures")
                                .document(adventure.id)
                                .collection("sessions")
                                .document(session.created_at.toString())
                                .collection("dices")
                                .get()
                                .addOnSuccessListener { dices ->
                                    Log.d(TAG, "Queried ${dices.size()} dice documents")
                                    for (dice in dices.documents)
                                        batch.delete(dice.reference)
                                    batchCommit()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error querying dice documents", e)
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(context, "Erro ao deletar aventura", Toast.LENGTH_SHORT).show()
                                }

                        Log.d(TAG, "Querying events")
                        db.collection("adventures")
                                .document(adventure.id)
                                .collection("sessions")
                                .document(session.created_at.toString())
                                .collection("events")
                                .get()
                                .addOnSuccessListener { events ->
                                    Log.d(TAG, "Queried ${events.size()} event documents")
                                    for (event in events.documents)
                                        batch.delete(event.reference)
                                    batchCommit()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error querying event documents", e)
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(context, "Erro ao deletar aventura", Toast.LENGTH_SHORT).show()
                                }

                        batch.delete(db
                                .collection("adventures")
                                .document(adventure.id)
                                .collection("sessions")
                                .document(session.created_at.toString()))
                    }
                    if (sessions.size() == 0) {
                        completionCounter -= 2
                        batchCommit()
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error querying documents", e)
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Erro ao deletar aventura", Toast.LENGTH_SHORT).show()
                }

        Log.d(TAG, "Querying items")
        db.collection("adventures")
                .document(adventure.id)
                .collection("items")
                .get()
                .addOnSuccessListener { items ->
                    Log.d(TAG, "Queried ${items.size()} item documents")
                    for (item in items.documents)
                        batch.delete(item.reference)
                    batchCommit()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error querying item documents", e)
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Erro ao deletar aventura", Toast.LENGTH_SHORT).show()
                }

        Log.d(TAG, "Querying characters")
        db.collection("characters")
                .whereEqualTo("aventura_id", adventure.id)
                .get()
                .addOnSuccessListener {
                    Log.d(TAG, "Queried ${it.size()} character documents")
                    for (document in it.documents) {
                        batch.delete(document.reference)
                    }
                    batchCommit()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error querying character documents", e)
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Erro ao deletar aventura", Toast.LENGTH_SHORT).show()
                }

        batch.delete(db.collection("adventures").document(adventure.id))
        batchCommit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_editar -> setEditModeOn()
            R.id.menu_ordenar -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setEditModeOn() {
        Log.d(TAG, "Edit mode on")
        editMode = true
        fabNovaAventura.hide()
        fabSaveEdit.show()
        adapter.editMode = true
        adapter.notifyDataSetChanged()
    }

    private fun setEditModeOff() {
        Log.d(TAG, "Edit mode off")
        editMode = false
        fabNovaAventura.show()
        fabSaveEdit.hide()
        adapter.editMode = false
        adapter.notifyDataSetChanged()
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
