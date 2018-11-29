package com.example.hal_9000.igor.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Item
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestoreException

class ItemsListAdapter(options: FirestoreRecyclerOptions<Item>, private val itemClickListener: (Item) -> Unit) : FirestoreRecyclerAdapter<Item, ItemsListAdapter.CharactersViewHolder>(options) {
    private val TAG = "CharsCombatListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharactersViewHolder {
        return CharactersViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_item, parent, false))
    }

    override fun onBindViewHolder(holder: CharactersViewHolder, position: Int, model: Item) {
        holder.setItemName(model.name)
        holder.setItemType(model.type)
        holder.setItemImage(model.image_url)
        holder.setClickListener(model, itemClickListener)
    }

    override fun onError(e: FirebaseFirestoreException) {
        Log.e(TAG, "Error: $e.message")
    }

    class CharactersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setItemName(characterName: String) {
            val tvName: TextView = itemView.findViewById(R.id.tv_name)
            tvName.text = characterName
        }

        fun setItemType(itemType: String) {
            val tvType: TextView = itemView.findViewById(R.id.tv_type)
            tvType.text = itemType
        }

        fun setItemImage(itemImage: String) {
            val ivImagem: ImageView = itemView.findViewById(R.id.iv_image)

            if (itemImage.isNotEmpty())
                Glide.with(itemView)
                        .load(itemImage)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivImagem)
            else
                Glide.with(itemView)
                        .load(R.drawable.ic_items)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivImagem)
        }

        fun setClickListener(item: Item, clickListener: (Item) -> Unit) {
            itemView.setOnClickListener { clickListener(item) }
        }
    }
}