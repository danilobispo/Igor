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
import com.example.hal_9000.igor.model.Personagem
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestoreException

class AccountCharactersListAdapter(options: FirestoreRecyclerOptions<Personagem>, private val itemClickListener: (Personagem) -> Unit) : FirestoreRecyclerAdapter<Personagem, AccountCharactersListAdapter.CharactersViewHolder>(options) {

    private val TAG = "CharactersListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharactersViewHolder {
        return CharactersViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.account_character_item, parent, false))
    }

    override fun onBindViewHolder(holder: CharactersViewHolder, position: Int, model: Personagem) {
        holder.setCharacterClass(model.classe)
        holder.setCharacterDescription(model.descricao)
        holder.setCharacterAdventure(model.aventura)
        holder.setCharacterImage(model.image_url, model.isnpc)
        holder.setClickListener(model, itemClickListener)
    }

    override fun onError(e: FirebaseFirestoreException) {
        Log.e(TAG, "Error: $e.message")
    }

    class CharactersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setCharacterClass(charClass: String) {
            val tvClass: TextView = itemView.findViewById(R.id.tv_class)
            tvClass.text = charClass
        }

        fun setCharacterDescription(description: String) {
            val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
            tvDescription.text = description
        }

        fun setCharacterAdventure(adventure: String) {
            val tvAdventure: TextView = itemView.findViewById(R.id.tv_adventure)
            tvAdventure.text = adventure
        }

        fun setCharacterImage(imageUrl: String, isNPC: Boolean) {
            val ivImage: ImageView = itemView.findViewById(R.id.iv_image)

            when {
                imageUrl.isNotEmpty() -> Glide.with(itemView)
                        .load(imageUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivImage)
                isNPC -> Glide.with(itemView)
                        .load(R.drawable.ic_monster)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivImage)
                else -> Glide.with(itemView)
                        .load(R.drawable.ic_person)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivImage)
            }
        }

        fun setClickListener(character: Personagem, clickListener: (Personagem) -> Unit) {
            itemView.setOnClickListener { clickListener(character) }
        }
    }
}