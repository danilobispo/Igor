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

class CharactersListAdapter(options: FirestoreRecyclerOptions<Personagem>, private val itemClickListener: (Personagem) -> Unit) : FirestoreRecyclerAdapter<Personagem, CharactersListAdapter.CharactersViewHolder>(options) {

    private val TAG = "CharactersListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharactersViewHolder {
        return CharactersViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.jogador_item, parent, false))
    }

    override fun onBindViewHolder(holder: CharactersViewHolder, position: Int, model: Personagem) {
        holder.setCharacterRole(model.classe)
        holder.setCharacterNome(model.nome)
        holder.setCharacterDescricao(model.descricao)
        holder.setCharacterImagem(model.image_url, model.isnpc)
        holder.setClickListener(model, itemClickListener)
    }

    override fun onError(e: FirebaseFirestoreException) {
        Log.e(TAG, "Error: $e.message")
    }

    class CharactersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setCharacterNome(nomeCharacter: String) {
            val tvNome: TextView = itemView.findViewById(R.id.tv_class)
            tvNome.text = nomeCharacter
        }

        fun setCharacterRole(roleCharacter: String) {
            val tvClasse: TextView = itemView.findViewById(R.id.tv_name)
            tvClasse.text = roleCharacter
        }

        fun setCharacterDescricao(descricaoCharacter: String) {
            val tvDescricao: TextView = itemView.findViewById(R.id.descricaoJogadorItem)
            tvDescricao.text = descricaoCharacter
        }

        fun setCharacterImagem(imagemCharacter: String, isNPC: Boolean) {
            val ivImagem: ImageView = itemView.findViewById(R.id.iv_imagem)

            when {
                imagemCharacter.isNotEmpty() -> Glide.with(itemView)
                        .load(imagemCharacter)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivImagem)
                isNPC -> Glide.with(itemView)
                        .load(R.drawable.ic_monster)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivImagem)
                else -> Glide.with(itemView)
                        .load(R.drawable.ic_person)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivImagem)
            }
        }

        fun setClickListener(personagem: Personagem, clickListener: (Personagem) -> Unit) {
            itemView.setOnClickListener { clickListener(personagem) }
        }
    }
}