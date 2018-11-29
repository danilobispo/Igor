package com.example.hal_9000.igor.fragment


import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.example.hal_9000.igor.viewmodel.MainViewModel
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.AdventureRecyclerViewAdapter
import com.example.hal_9000.igor.model.Aventura
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class HomeFragment : Fragment() {

    private val TAG = "HomeFragment"

    private lateinit var fabNovaAventura: FloatingActionButton
    private lateinit var fabSaveEdit: FloatingActionButton

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

        adapter = AdventureRecyclerViewAdapter(options, model.getUsername()!!, { aventura: Aventura -> aventuraItemClicked(aventura) }, { aventura: Aventura -> aventuraDeleteItemClicked(aventura) })
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

    private fun deleteAdventure(aventura: Aventura) {
        Log.d(TAG, "Deleting adventure ${aventura.title}")
        db.collection("adventures")
                .document(aventura.id).delete()
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully deleted!")
                    Toast.makeText(context, "Aventura deletada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error deleting document", e)
                    Toast.makeText(context, "Erro ao deletar aventura", Toast.LENGTH_SHORT).show()
                }
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

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
