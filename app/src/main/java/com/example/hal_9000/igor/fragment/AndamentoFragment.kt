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
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.SessionsListAdapter
import com.example.hal_9000.igor.model.Session
import com.example.hal_9000.igor.viewmodel.MainViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_andamento.*

class AndamentoFragment : Fragment() {
    private val TAG = "AndamentoFragment"

    private lateinit var adapter: SessionsListAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore

    private lateinit var model: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_andamento, container, false)

        mRecyclerView = view.findViewById(R.id.rv_next_sessions)

        model = activity!!.run {
            ViewModelProviders.of(this).get(MainViewModel::class.java)
        }

        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()

        val query = db
                .collection("adventures")
                .document(model.getAdventure()!!.id)
                .collection("sessions")
                .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<Session>()
                .setQuery(query, Session::class.java)
                .build()

        adapter = SessionsListAdapter(options, { session: Session -> sessionItemClicked(session) }, { session: Session, itemView: View -> sessionItemLongClicked(session, itemView) })
        mRecyclerView.adapter = adapter

        val tvDescription = view.findViewById<TextView>(R.id.tv_description)

        if (model.getAdventure()!!.description.isEmpty())
            tvDescription.text = "Aventura sem descrição"
        else
            tvDescription.text = model.getAdventure()!!.description

        tvDescription.post {
            if (tvDescription.lineCount > 6) {

                tv_see_more.visibility = View.VISIBLE

                tv_see_more.setOnClickListener {
                    if (tvDescription.maxLines == 6) {
                        tvDescription.maxLines = 99
                        tv_see_more.text = "ver menos ▲"
                    } else {
                        tvDescription.maxLines = 6
                        tv_see_more.text = "ver mais ▼"
                    }
                }
            }
        }

        return view
    }

    private fun sessionItemClicked(session: Session) {
        Log.d(TAG, "Clicked ${session.title}")

        if (AdventureFragment.editMode && model.getIsMaster()!!) {
            val action = AdventureFragmentDirections.ActionAdventureFragmentToNewSession()
            action.setSession(session)
            NavHostFragment.findNavController(this).navigate(action)
        } else {
            val action = AdventureFragmentDirections.ActionAdventureFragmentToSessionFragment(session)
            action.setSession(session)
            NavHostFragment.findNavController(this).navigate(action)
        }
    }

    private fun sessionItemLongClicked(session: Session, itemView: View) {
        if (!model.getIsMaster()!!) return

        val popup = PopupMenu(context!!, itemView)
        popup.inflate(R.menu.popup_menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    val action = AdventureFragmentDirections.ActionAdventureFragmentToNewSession()
                    action.setSession(session)
                    NavHostFragment.findNavController(this).navigate(action)
                }
                R.id.menu_delete -> {
                    deleteSession(session)
                }
            }
            true
        }
        popup.gravity = Gravity.END
        popup.show()
    }

    private fun deleteSession(session: Session) {
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
            Log.d(TAG, "Querying dices")
            db.collection("adventures")
                    .document(model.getAdventure()!!.id)
                    .collection("sessions")
                    .document(session.created_at.toString())
                    .collection("dices")
                    .get()
                    .addOnSuccessListener {
                        Log.d(TAG, "Queried ${it.size()} dice documents")
                        for (document in it.documents)
                            batch.delete(document.reference)
                        batchCommit()
                    }

            Log.d(TAG, "Querying events")
            db.collection("adventures")
                    .document(model.getAdventure()!!.id)
                    .collection("sessions")
                    .document(session.created_at.toString())
                    .collection("events")
                    .get()
                    .addOnSuccessListener {
                        Log.d(TAG, "Queried ${it.size()} event documents")
                        for (document in it.documents)
                            batch.delete(document.reference)
                        batchCommit()
                    }

            batch.delete(db
                    .collection("adventures")
                    .document(model.getAdventure()!!.id)
                    .collection("sessions")
                    .document(session.created_at.toString()))
        }

        fun showDeleteConfirmationDialog() {
            AlertDialog.Builder(context!!)
                    .setTitle("Deletar sessão")
                    .setMessage("Tem certeza que deseja deletar a sessão ${session.title}?")
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
