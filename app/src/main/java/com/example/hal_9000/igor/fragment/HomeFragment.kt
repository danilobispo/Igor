package com.example.hal_9000.igor.fragment


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
import com.example.hal_9000.igor.LoginActivity
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.AdventureRecyclerViewAdapter
import com.example.hal_9000.igor.model.Aventura
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment() {

    private val TAG = "HomeFragment"

    private var adapter: AdventureRecyclerViewAdapter? = null
    private var mRecyclerView: RecyclerView? = null
    private var db: FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        setHasOptionsMenu(true)

        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        LoginActivity.username = mAuth.currentUser?.displayName.toString()

        mRecyclerView = view.findViewById(R.id.rv_adventures_list)
        mRecyclerView?.layoutManager = LinearLayoutManager(context)
        mRecyclerView?.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()
        val query = db!!.collection("adventures").whereEqualTo("deleted", false).whereEqualTo("players.${LoginActivity.username}", true)

        val options = FirestoreRecyclerOptions.Builder<Aventura>()
                .setQuery(query, Aventura::class.java)
                .build()

        adapter = AdventureRecyclerViewAdapter(options, false, { aventura: Aventura -> aventuraItemClicked(aventura) }, { aventura: Aventura -> aventuraDeleteItemClicked(aventura) })
        mRecyclerView?.adapter = adapter

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_nova_aventura)
        fab.setOnClickListener {

            val aventura = Aventura()
            val action = HomeFragmentDirections.ActionHomeFragmentToNewAdventure(aventura)
            action.setAventura(aventura)
            NavHostFragment.findNavController(this).navigate(action)
        }

        val fabEditMode = view.findViewById<FloatingActionButton>(R.id.fab_edit_mode)
        fabEditMode.setOnClickListener {
            setEditModeOff()
        }

        val fabEditSave = view.findViewById<FloatingActionButton>(R.id.fab_save_edit)
        fabEditSave.setOnClickListener {
            setEditModeOff()
        }

        return view
    }

    private fun aventuraItemClicked(aventura: Aventura) {
        Log.d(TAG, "Clicked: ${aventura.title}")

        val action = HomeFragmentDirections.ActionHomeFragmentToAdventureFragment(aventura)
        action.setAventura(aventura)

        findNavController(this).navigate(action)
    }

    private fun aventuraDeleteItemClicked(aventura: Aventura) {
        AlertDialog.Builder(view!!.context)
                .setTitle("Deletar aventura")
                .setMessage("Tem certeza que deseja deletar a aventura  ${aventura.title}?")
                .setPositiveButton("Deletar") { _, _ ->
//                    deleteAdventure(aventura)
                    hideAdventure(aventura)
                }
                .setNegativeButton("Cancelar") { _, _ ->
                    Toast.makeText(context, "Acão cancelada", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "aventuraDeleteItemClicked: aborted")
                }
                .show()
    }

    private fun deleteAdventure(aventura: Aventura) {
        Log.d(TAG, "Deleting adventure ${aventura.title}")

        db!!.collection("adventures")
                .whereEqualTo("title", aventura.title)
                .whereEqualTo("creator", aventura.creator)
                .limit(1)
                .get()
                .addOnSuccessListener { it ->
                    it.documents[0].reference
                            .delete()
                            .addOnSuccessListener { _ ->
                                Log.d(TAG, "DocumentSnapshot successfully deleted!")
                                Toast.makeText(context, "Aventura deletada", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error deleting document", e)
                                Toast.makeText(context, "Erro ao deletar aventura", Toast.LENGTH_SHORT).show()
                            }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error querying document", e)
                    Toast.makeText(context, "Erro ao deletar aventura", Toast.LENGTH_SHORT).show()
                }
    }

    private fun hideAdventure(aventura: Aventura) {
        Log.d(TAG, "Hiding adventure ${aventura.title}")

        db!!.collection("adventures")
                .whereEqualTo("title", aventura.title)
                .whereEqualTo("creator", aventura.creator)
                .limit(1)
                .get()
                .addOnSuccessListener { it ->
                    it.documents[0].reference
                            .update("deleted", true)
                            .addOnSuccessListener { _ ->
                                Log.d(TAG, "DocumentSnapshot successfully updated!")
                                Toast.makeText(context, "Aventura deletada", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error updating document", e)
                                Toast.makeText(context, "Erro ao deletar aventura", Toast.LENGTH_SHORT).show()
                            }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error querying document", e)
                    Toast.makeText(context, "Erro ao deletar aventura", Toast.LENGTH_SHORT).show()
                }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_editar -> {
                setEditModeOn()
            }
            R.id.menu_ordenar -> {
                Toast.makeText(context, "Ordenar", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setRecyclerView(editMode: Boolean) {
        val query = db!!.collection("adventures").whereEqualTo("deleted", false)

        val options = FirestoreRecyclerOptions.Builder<Aventura>()
                .setQuery(query, Aventura::class.java)
                .build()

        adapter!!.stopListening()
        adapter = AdventureRecyclerViewAdapter(options, editMode, { aventura: Aventura -> aventuraItemClicked(aventura) }, { aventura: Aventura -> aventuraDeleteItemClicked(aventura) })
        mRecyclerView?.adapter = adapter
        adapter!!.stopListening()
        adapter!!.startListening()
    }

    private fun setEditModeOn() {
        Log.d(TAG, "Edit mode on")
        fab_nova_aventura.hide()
        fab_save_edit.show()
        fab_edit_mode.show()
        setRecyclerView(true)
    }

    private fun setEditModeOff() {
        Log.d(TAG, "Edit mode off")
        fab_nova_aventura.show()
        fab_save_edit.hide()
        fab_edit_mode.hide()
        setRecyclerView(false)
    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter!!.stopListening()
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
