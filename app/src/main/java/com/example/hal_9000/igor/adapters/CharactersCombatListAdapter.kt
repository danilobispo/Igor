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
import com.example.hal_9000.igor.`interface`.ViewHolderClickListerner
import com.example.hal_9000.igor.model.Personagem
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.android.synthetic.main.character_item.view.*

class CharactersCombatListAdapter(options: FirestoreRecyclerOptions<Personagem>, private val itemClickListener: (Personagem) -> Unit) : FirestoreRecyclerAdapter<Personagem, CharactersCombatListAdapter.CharactersViewHolder>(options), ViewHolderClickListerner {

    private val TAG = "CharsCombatListAdapter"

    val selectedIds: MutableList<Int> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharactersViewHolder {
        return CharactersViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.character_item, parent, false), this)
    }

    override fun onBindViewHolder(holder: CharactersViewHolder, position: Int, model: Personagem) {
        holder.setCharacterRole(model.classe)
        holder.setCharacterNome(model.nome)
        holder.setCharacterHP(model.health, model.healthMax)
        holder.setCharacterImagem(model.imageUrl, model.isNpc)
        holder.setClickListener(model, itemClickListener)

        if (selectedIds.contains(position))
            holder.itemView.selected_overlay.visibility = View.VISIBLE
        else
            holder.itemView.selected_overlay.visibility = View.INVISIBLE
    }

    override fun onError(e: FirebaseFirestoreException) {
        Log.e(TAG, "Error: $e.message")
    }

    private fun addIDIntoSelectedIds(index: Int) {
        if (selectedIds.contains(index))
            selectedIds.remove(index)
        else
            selectedIds.add(index)

        notifyItemChanged(index)
    }

    override fun onTap(index: Int) {
        addIDIntoSelectedIds(index)
    }

    override fun onLongTap(index: Int) {
        addIDIntoSelectedIds(index)
    }

    class CharactersViewHolder(itemView: View, private val r_tap: ViewHolderClickListerner) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        fun setCharacterNome(nomeCharacter: String) {
            val tvNome: TextView = itemView.findViewById(R.id.tv_name)
            tvNome.text = nomeCharacter
        }

        fun setCharacterRole(roleCharacter: String) {
            val tvClasse: TextView = itemView.findViewById(R.id.tv_class)
            tvClasse.text = roleCharacter
        }

        fun setCharacterHP(health: Int, healthMax: Int) {
            val tvHp: TextView = itemView.findViewById(R.id.tv_hp)
            tvHp.text = "$health/$healthMax"
        }

        fun setCharacterImagem(imagemCharacter: String, isNPC: Boolean) {
            val ivImagem: ImageView = itemView.findViewById(R.id.iv_imagem)

            if (imagemCharacter.isEmpty()) {
                if (isNPC)
                    Glide.with(itemView)
                            .load(R.drawable.ic_monster)
                            .apply(RequestOptions.circleCropTransform())
                            .into(ivImagem)
                else
                    Glide.with(itemView)
                            .load(R.drawable.ic_person)
                            .apply(RequestOptions.circleCropTransform())
                            .into(ivImagem)
            } else
                Glide.with(itemView)
                        .load(imagemCharacter)
                            .apply(RequestOptions.circleCropTransform())
                        .into(ivImagem)
        }

        fun setClickListener(personagem: Personagem, clickListener: (Personagem) -> Unit) {
            //itemView.setOnClickListener { clickListener(personagem) }
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            r_tap.onTap(adapterPosition)
        }
    }
}