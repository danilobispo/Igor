package com.example.hal_9000.igor.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.JogadoresListAdapter
import com.example.hal_9000.igor.model.Personagem
import com.example.hal_9000.igor.model.Session
import com.example.hal_9000.igor.viewmodel.MainViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class JogadoresFragment : Fragment() {
    private val TAG = "JogadoresFragment"

    private lateinit var adapter: JogadoresListAdapter
    private lateinit var mJogadoresList: RecyclerView
    private lateinit var db: FirebaseFirestore

    private lateinit var model: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_jogadores, container, false)

        mJogadoresList = view.findViewById(R.id.jogadores_rv)

        model = activity!!.run {
            ViewModelProviders.of(this).get(MainViewModel::class.java)
        }

        mJogadoresList.layoutManager = LinearLayoutManager(context)
        mJogadoresList.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()

        val aventura = model.getAdventure()!!
        val query = db.collection("characters")
                .whereEqualTo("aventura_id", aventura.id)
                .whereEqualTo("isnpc", false)

        val options = FirestoreRecyclerOptions.Builder<Personagem>()
                .setQuery(query, Personagem::class.java)
                .build()

        adapter = JogadoresListAdapter(options, { personagem: Personagem -> personagemItemClicked(personagem) }, { personagem: Personagem, itemView: View -> personagemItemLongClicked(personagem, itemView) })
        mJogadoresList.adapter = adapter

        return view
    }

    private fun personagemItemClicked(personagem: Personagem) {
        val action = AdventureFragmentDirections.ActionAdventureFragmentToCharacterProfileFragment(personagem)
        action.setCharacter(personagem)
        action.setReadOnly(true)
        NavHostFragment.findNavController(this).navigate(action)
    }

    private fun personagemItemLongClicked(personagem: Personagem, itemView: View) {
        if (!model.getIsMaster()!!) return

        val popup = PopupMenu(context!!, itemView)
        popup.inflate(R.menu.popup_menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    val action = AdventureFragmentDirections.ActionAdventureFragmentToNewCharacterFragment()
                    action.setPersonagem(personagem)
                    NavHostFragment.findNavController(this).navigate(action)
                }
                R.id.menu_delete -> {
                    if (!personagem.ismaster)
                        deleteCharacter(personagem)
                    else
                        Toast.makeText(context, "Não é possível deletar o mestre da aventura", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        popup.gravity = Gravity.END
        popup.show()
    }

    private fun deleteCharacter(personagem: Personagem) {
        val batch = db.batch()
        var completionCounter = 2

        fun batchCommit() {
            completionCounter -= 1
            if (completionCounter != 0) return
            batch.commit()
                    .addOnSuccessListener {
                        Log.d(TAG, "Document deleted successfully")
                        Toast.makeText(context, "Sessão deletada com sucesso", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error deleting document", e)
                        Toast.makeText(context, "Erro ao deletar sessão", Toast.LENGTH_SHORT).show()
                    }
        }

        fun delete() {
            batch.delete(db
                    .collection("characters")
                    .document(personagem.id))

            batch.update(db
                    .collection("adventures")
                    .document(model.getAdventure()!!.id), "players.${personagem.nome}", FieldValue.delete())

            Log.d(TAG, "Querying items")
            db.collection("adventures")
                    .document(model.getAdventure()!!.id)
                    .collection("items")
                    .whereEqualTo("owner", personagem.nome)
                    .get()
                    .addOnSuccessListener { items ->
                        Log.d(TAG, "Queried ${items.size()} item documents")
                        for (item in items.documents)
                            batch.update(item.reference, "owner", personagem.creator)
                        batchCommit()
                    }

            Log.d(TAG, "Querying sessions")
            db.collection("adventures")
                    .document(model.getAdventure()!!.id)
                    .collection("sessions")
                    .get()
                    .addOnSuccessListener { sessions ->
                        Log.d(TAG, "Queried ${sessions.size()} session documents")
                        for (document in sessions.documents) {
                            val session = document.toObject(Session::class.java)!!
                            batch.delete(db
                                    .collection("adventures")
                                    .document(model.getAdventure()!!.id)
                                    .collection("sessions")
                                    .document(session.created_at.toString())
                                    .collection("dices")
                                    .document(personagem.nome))
                        }
                        batchCommit()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error querying documents", e)
                        Toast.makeText(context, "Erro ao deletar personagem", Toast.LENGTH_SHORT).show()
                    }
        }

        fun showDeleteConfirmationDialog() {
            AlertDialog.Builder(context!!)
                    .setTitle("Deletar personagem")
                    .setMessage("Tem certeza que deseja deletar o personagem?")
                    .setPositiveButton("Deletar") { _, _ ->
                        delete()
                    }
                    .setNegativeButton("Cancelar") { _, _ ->
                        Toast.makeText(context, "Acão cancelada", Toast.LENGTH_SHORT).show()
                    }
                    .show()
        }
        showDeleteConfirmationDialog()
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
