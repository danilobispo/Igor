package com.example.hal_9000.igor.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Session
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestoreException
import java.text.SimpleDateFormat
import java.util.*

class SessionsListAdapter(options: FirestoreRecyclerOptions<Session>, private val itemClickListener: (Session) -> Unit) : FirestoreRecyclerAdapter<Session, SessionsListAdapter.SessionsViewHolder>(options) {

    private val TAG = "SessionsListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionsViewHolder {
        return SessionsViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.session_item, parent, false))
    }

    override fun onBindViewHolder(holder: SessionsViewHolder, position: Int, model: Session) {
        holder.setSessionData(model.date)
        holder.setSessionTexto(model.title)
        holder.setClickListener(model, itemClickListener)
    }

    override fun onError(e: FirebaseFirestoreException) {
        Log.e(TAG, "Error: $e.message")
    }

    class SessionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setSessionData(dataSession: Long) {
            val tvNome: TextView = itemView.findViewById(R.id.tv_date)

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = dataSession
            tvNome.text = SimpleDateFormat("dd/MM").format(calendar.time)
        }

        fun setSessionTexto(textSession: String) {
            val tvText: TextView = itemView.findViewById(R.id.tv_text)
            tvText.text = textSession
        }

        fun setClickListener(Session: Session, clickListener: (Session) -> Unit) {
            itemView.setOnClickListener { clickListener(Session) }
        }
    }
}