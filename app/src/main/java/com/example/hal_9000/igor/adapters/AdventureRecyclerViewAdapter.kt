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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException


class AdventureRecyclerViewAdapter(options: FirestoreRecyclerOptions<Aventura>, private val editMode: Boolean, private val itemClickListener: (Aventura) -> Unit, private val deleteClickListener: (Aventura) -> Unit) : FirestoreRecyclerAdapter<Aventura, AdventureRecyclerViewAdapter.AdventureViewHolder>(options) {

    private val TAG = "AdventureRecyclrAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdventureViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.aventura_item_row, parent, false)
        return AdventureViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdventureViewHolder, position: Int, model: Aventura) {
        holder.setAdventureTitle(model.title)
        holder.setAdventureNextSession(model.next_session)
        holder.setAdventureTheme(model.theme)

        if (editMode)
            holder.setDeleteClickListener(model, deleteClickListener)
        else
            holder.setClickListener(model, itemClickListener)
    }

    override fun onError(e: FirebaseFirestoreException) {
        Log.e(TAG, "Error: $e.message")
    }

    class AdventureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setAdventureTitle(adventureTitle: String) {
            val title: TextView = itemView.findViewById(R.id.tv_title)
            title.text = adventureTitle
        }

        fun setAdventureNextSession(adventureNextSession: String) {
            val nextSession: TextView = itemView.findViewById(R.id.tv_next_session)
            val nextSessionText = "próxima sessão $adventureNextSession"
            nextSession.text = nextSessionText
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

        fun setClickListener(aventura: Aventura, clickListener: (Aventura) -> Unit) {
            itemView.setOnClickListener { clickListener(aventura) }
        }

        fun setDeleteClickListener(aventura: Aventura, clickListener: (Aventura) -> Unit) {

            val mAuth = FirebaseAuth.getInstance()
            if (aventura.creator != mAuth.uid) {
                val overlay: View = itemView.findViewById(R.id.overlay_image)
                overlay.visibility = View.VISIBLE
                return
            }

            val image: ImageView = itemView.findViewById(R.id.iv_delete)
            image.visibility = View.VISIBLE
            image.setOnClickListener { clickListener(aventura) }
        }
    }
}