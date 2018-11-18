package com.example.hal_9000.igor.fragment

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
import android.widget.Toast
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.EventsListAdapter
import com.example.hal_9000.igor.model.Evento
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore

class EventsFragment : Fragment() {

    private val TAG = "EventsFragment"

    private lateinit var adapter: EventsListAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_events, container, false)

        mRecyclerView = view.findViewById(R.id.eventos_rv)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()

        val aventura = AdventureFragment.aventura
        val query = db
                .collection("adventures")
                .document(aventura.id)
                .collection("sessions")
                .document(SessionFragment.sessionId)
                .collection("events")

        val options = FirestoreRecyclerOptions.Builder<Evento>()
                .setQuery(query, Evento::class.java)
                .build()

        adapter = EventsListAdapter(options) { evento: Evento -> eventoItemClicked(evento) }
        mRecyclerView.adapter = adapter

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                mRecyclerView.scrollToPosition(adapter.itemCount - 1)
            }
        })

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            enterNote()
        }

        return view
    }

    private fun enterNote() {
        //TODO: Criar layout para o DialogAlert

        val event = Evento(System.currentTimeMillis(), "custom")

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT

        AlertDialog.Builder(view!!.context)
                .setTitle("Criar evento")
                .setView(input)
                .setPositiveButton("Criar") { _, _ ->
                    event.event = input.text.toString()

                    db
                            .collection("adventures")
                            .document(AdventureFragment.aventura.id)
                            .collection("sessions")
                            .document(SessionFragment.sessionId)
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
                            .document(AdventureFragment.aventura.id)
                            .collection("sessions")
                            .document(SessionFragment.sessionId)
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
                            .document(AdventureFragment.aventura.id)
                            .collection("sessions")
                            .document(SessionFragment.sessionId)
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
        fun newInstance() = EventsFragment()
    }
}
