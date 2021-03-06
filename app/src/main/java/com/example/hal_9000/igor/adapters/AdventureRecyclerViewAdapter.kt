package com.example.hal_9000.igor.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Aventura
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestoreException
import java.text.SimpleDateFormat
import java.util.*

class AdventureRecyclerViewAdapter(options: FirestoreRecyclerOptions<Aventura>, private val username: String, private val itemClickListener: (Aventura) -> Unit, private val itemLongClickListener: (Aventura, View) -> Unit, private val deleteClickListener: (Aventura) -> Unit) : FirestoreRecyclerAdapter<Aventura, AdventureRecyclerViewAdapter.AdventureViewHolder>(options) {
    private val TAG = "AdventureRecyclrAdapter"

    var editMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdventureViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.aventura_item_row, parent, false)
        return AdventureViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdventureViewHolder, position: Int, model: Aventura) {
        holder.setAdventureTitle(model.title)
        holder.setAdventureNextSession(model.next_session)
        holder.setAdventureTheme(model.theme)
        holder.setClickListener(model, editMode, itemClickListener)
        holder.setLongClickListener(model, editMode, itemLongClickListener)
        holder.setEditMode(model, editMode, username, deleteClickListener)
    }

    override fun onError(e: FirebaseFirestoreException) {
        Log.e(TAG, "Error: $e.message")
    }

    class AdventureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setAdventureTitle(adventureTitle: String) {
            val title: TextView = itemView.findViewById(R.id.tv_title)
            title.text = adventureTitle
        }

        fun setAdventureNextSession(adventureNextSession: Long) {
            val nextSession: TextView = itemView.findViewById(R.id.tv_next_session)

            if (adventureNextSession != 0L) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = adventureNextSession
                nextSession.text = "próxima sessão ${SimpleDateFormat("dd/MM").format(calendar.time)}"
            } else {
                nextSession.text = "próxima sessão"
            }
        }

        fun setAdventureTheme(adventureTheme: String) {
            val image: ImageView = itemView.findViewById(R.id.iv_theme)

            when (adventureTheme) {
                "krevast" ->
                    Glide.with(itemView)
                            .load(R.drawable.miniatura_krevast)
                            .into(image)
                "corvali" ->
                    Glide.with(itemView)
                            .load(R.drawable.miniatura_corvali)
                            .into(image)
                "heartlands" ->
                    Glide.with(itemView)
                            .load(R.drawable.miniatura_heartlands)
                            .into(image)
                "coast" ->
                    Glide.with(itemView)
                            .load(R.drawable.miniatura_coast)
                            .into(image)
                else ->
                    Glide.with(itemView)
                            .load(R.drawable.miniatura_imagem_automatica)
                            .into(image)
            }
        }

        fun setClickListener(aventura: Aventura, editMode: Boolean, clickListener: (Aventura) -> Unit) {
            if (editMode)
                itemView.setOnClickListener {}
            else
                itemView.setOnClickListener { clickListener(aventura) }
        }

        fun setLongClickListener(aventura: Aventura, editMode: Boolean, longClickListener: (Aventura, View) -> Unit) {
            if (editMode)
                itemView.setOnLongClickListener { false }
            else
                itemView.setOnLongClickListener {
                    longClickListener(aventura, itemView)
                    true
                }
        }

        fun setEditMode(aventura: Aventura, editMode: Boolean, username: String, clickListener: (Aventura) -> Unit) {
            val overlay: View = itemView.findViewById(R.id.overlay_image)
            val deleteIcon: ImageView = itemView.findViewById(R.id.iv_delete)

            if (editMode) {
                if (aventura.creator == username) {
                    overlay.visibility = View.GONE
                    deleteIcon.visibility = View.VISIBLE
                    deleteIcon.setOnClickListener { clickListener(aventura) }
                } else {
                    overlay.visibility = View.VISIBLE
                    deleteIcon.visibility = View.GONE
                    deleteIcon.setOnClickListener {}
                }
            } else {
                overlay.visibility = View.GONE
                deleteIcon.visibility = View.GONE
                deleteIcon.setOnClickListener {}
            }
        }
    }
}