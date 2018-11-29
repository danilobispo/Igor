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

class JogadoresListAdapter(options: FirestoreRecyclerOptions<Personagem>, private val itemClickListener: (Personagem) -> Unit) : FirestoreRecyclerAdapter<Personagem, JogadoresListAdapter.JogadoresViewHolder>(options) {
    private val TAG = "JogadoresListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JogadoresViewHolder {
        return JogadoresViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.jogador_item, parent, false))
    }

    override fun onBindViewHolder(holder: JogadoresViewHolder, position: Int, model: Personagem) {
        holder.setJogadorNome(model.nome)
        holder.setJogadorRole(model.classe)
        holder.setJogadorDescricao(model.descricao)
        holder.setJogadorImagem(model.image_url, model.isnpc)
        holder.setClickListener(model, itemClickListener)
    }

    override fun onError(e: FirebaseFirestoreException) {
        Log.e(TAG, "Error: $e.message")
    }

    class JogadoresViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setJogadorNome(nomeJogador: String) {
            val tvNome: TextView = itemView.findViewById(R.id.tv_name)
            tvNome.text = nomeJogador
        }

        fun setJogadorRole(roleJogador: String) {
            val tvClasse: TextView = itemView.findViewById(R.id.tv_class)
            tvClasse.text = roleJogador
        }

        fun setJogadorDescricao(descricaoJogador: String) {
            val tvDescricao: TextView = itemView.findViewById(R.id.descricaoJogadorItem)
            tvDescricao.text = descricaoJogador
        }

        fun setJogadorImagem(imagemJogador: String, isNPC: Boolean) {
            val ivImagem: ImageView = itemView.findViewById(R.id.iv_imagem)

            when {
                imagemJogador.isNotEmpty() -> Glide.with(itemView)
                        .load(imagemJogador)
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