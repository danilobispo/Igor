package com.example.hal_9000.igor.fragment

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
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
import android.widget.TextView
import android.widget.Toast
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.EventsListAdapter
import com.example.hal_9000.igor.model.Aventura
import com.example.hal_9000.igor.model.Evento
import com.example.hal_9000.igor.model.Session
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore

class CombatFragment : Fragment() {

    private val TAG = "CombatFragment"

    private lateinit var aventura: Aventura
    private lateinit var session: Session

    private lateinit var adapter: EventsListAdapter
    private lateinit var mEventosList: RecyclerView
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_combat, container, false)

        aventura = NewSessionArgs.fromBundle(arguments).aventura
        session = NewSessionArgs.fromBundle(arguments).session

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val adventureTitle = view.findViewById<TextView>(R.id.tv_adventure_title)
        val sessionTitle = view.findViewById<TextView>(R.id.tv_session_title)

        adventureTitle.text = aventura.title
        sessionTitle.text = session.title

        mEventosList = view.findViewById(R.id.eventos_rv)
        mEventosList.layoutManager = LinearLayoutManager(context)
        mEventosList.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()

        val aventura = AdventureFragment.aventura
        val query = db
                .collection("adventures")
                .document("${aventura.creator}_${aventura.title}")
                .collection("sessions")
                .document(session.created.toString())
                .collection("events")

        val options = FirestoreRecyclerOptions.Builder<Evento>()
                .setQuery(query, Evento::class.java)
                .build()

        adapter = EventsListAdapter(options) { evento: Evento -> eventoItemClicked(evento) }
        mEventosList.adapter = adapter

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                mEventosList.scrollToPosition(adapter.itemCount - 1)
            }
        })

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_npc -> {
                    item.isChecked = false
                    Log.d(TAG, "action_npc clicked")
                    bottomNavigationView.selectedItemId = 0
                }
                R.id.action_combat -> {
                    item.isChecked = false
                    Log.d(TAG, "action_combat clicked")
                    bottomNavigationView.selectedItemId = 0

                }
                R.id.action_notes -> {
                    item.isChecked = false
                    Log.d(TAG, "action_notes clicked")
                    bottomNavigationView.selectedItemId = 0

                    enterNote()
                }
            }
            true
        }

        return view
    }

    private fun enterNote() {
        //TODO: Criar layout para o DialogAlert

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT

        val event = Evento()
        event.date = System.currentTimeMillis() / 1000
        event.type = "custom"

        AlertDialog.Builder(view!!.context)
                .setTitle("Criar evento")
                .setView(input)
                .setPositiveButton("Criar") { _, _ ->
                    event.event = input.text.toString()

                    db
                            .collection("adventures")
                            .document("${aventura.creator}_${aventura.title}")
                            .collection("sessions")
                            .document(session.created.toString())
                            .collection("events")
                            .document(event.date.toString())
                            .set(event)
                            .addOnSuccessListener {
                                Log.d(TAG, "Document added successfully")
                                Toast.makeText(context, "Evento adicionado com sucesso", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding document", e)
                                Toast.makeText(context, "Erro ao adicionar evento", Toast.LENGTH_SHORT).show()
                            }
                }
                .setNegativeButton("Cancelar") { _, _ ->
                    Log.d(TAG, "Event creation canceled")
                }
                .show()
    }

    private fun eventoItemClicked(evento: Evento) {
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT

        input.setText(evento.event)

        AlertDialog.Builder(view!!.context)
                .setTitle("Editar evento")
                .setView(input)
                .setPositiveButton("Editar") { _, _ ->
                    evento.event = input.text.toString()

                    db
                            .collection("adventures")
                            .document("${aventura.creator}_${aventura.title}")
                            .collection("sessions")
                            .document(session.created.toString())
                            .collection("events")
                            .document(evento.date.toString())
                            .set(evento)
                            .addOnSuccessListener {
                                Log.d(TAG, "Document added successfully")
                                Toast.makeText(context, "Evento editado com sucesso", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding document", e)
                                Toast.makeText(context, "Erro ao editar evento", Toast.LENGTH_SHORT).show()
                            }
                }
                .setNegativeButton("Cancelar") { _, _ ->
                    Log.d(TAG, "note creation canceled")
                }
                .setNeutralButton("Deletar") { _, _ ->
                    db
                            .collection("adventures")
                            .document("${aventura.creator}_${aventura.title}")
                            .collection("sessions")
                            .document(session.created.toString())
                            .collection("events")
                            .document(evento.date.toString())
                            .delete()
                            .addOnSuccessListener {
                                Log.d(TAG, "Document removed successfully")
                                Toast.makeText(context, "Evento removido", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error removing document", e)
                                Toast.makeText(context, "Erro ao remover evento", Toast.LENGTH_SHORT).show()
                            }

                }
                .show()
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
        fun newInstance() = CombatFragment()
    }
}
