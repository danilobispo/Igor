package com.example.hal_9000.igor.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Evento
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestoreException
import java.text.SimpleDateFormat
import java.util.*

class EventsListAdapter(options: FirestoreRecyclerOptions<Evento>, private val itemClickListener: (Evento) -> Unit) : FirestoreRecyclerAdapter<Evento, EventsListAdapter.EventosViewHolder>(options) {

    private val TAG = "EventsListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventosViewHolder {
        return EventosViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.evento_item, parent, false))
    }

    override fun onBindViewHolder(holder: EventosViewHolder, position: Int, model: Evento) {
        holder.setEventoData(model.date)
        holder.setEventoTexto(model.event)
        holder.setClickListener(model, itemClickListener)
    }

    override fun onError(e: FirebaseFirestoreException) {
        Log.e(TAG, "Error: $e.message")
    }

    class EventosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setEventoData(dataEvento: Long) {
            val tvNome: TextView = itemView.findViewById(R.id.tv_date)

            val sdf = SimpleDateFormat("dd/MM HH:mm:ss")
            tvNome.text = sdf.format(Date(dataEvento))
        }

        fun setEventoTexto(textEvento: String) {
            val tvClasse: TextView = itemView.findViewById(R.id.tv_text)
            tvClasse.text = textEvento
        }

        fun setClickListener(evento: Evento, clickListener: (Evento) -> Unit) {
            itemView.setOnClickListener { clickListener(evento) }
        }
    }
}