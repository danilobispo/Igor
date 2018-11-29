package com.example.hal_9000.igor.adapters

import android.graphics.drawable.LayerDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
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

class CharactersCombatListAdapter(options: FirestoreRecyclerOptions<Personagem>, private val itemClickListener: (Personagem) -> Unit, private val syncSelectionListener: (Boolean) -> Unit) : FirestoreRecyclerAdapter<Personagem, CharactersCombatListAdapter.CharactersViewHolder>(options), ViewHolderClickListerner {
    private val TAG = "CharsCombatListAdapter"

    val selectedIds: MutableList<Int> = arrayListOf()
    var selectionModeOwn = false
    var selectionModeOther = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharactersViewHolder {
        return CharactersViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.character_item, parent, false), this)
    }

    override fun onBindViewHolder(holder: CharactersViewHolder, position: Int, model: Personagem) {
        holder.setCharacterRole(model.classe)
        holder.setCharacterNome(model.nome)
        holder.setCharacterHP(model.health, model.health_max)
        holder.setCharacterImagem(model.image_url, model.isnpc)
        holder.setClickListener(model, itemClickListener)

        if (selectedIds.contains(position))
            holder.itemView.selected_overlay.visibility = View.VISIBLE
        else
            holder.itemView.selected_overlay.visibility = View.INVISIBLE
    }

    override fun onError(e: FirebaseFirestoreException) {
        Log.e(TAG, "Error: $e.message")
    }

    private fun setSelectionMode(mode: Boolean) {
        if (selectionModeOwn != mode) {
            selectionModeOwn = mode
            syncSelectionListener(mode)
        }
    }

    private fun toggleSelected(index: Int) {
        if (selectedIds.contains(index))
            selectedIds.remove(index)
        else
            selectedIds.add(index)
        setSelectionMode(selectedIds.count() > 0)
        notifyItemChanged(index)
    }

    private fun setSelected(index: Int) {
        if (selectedIds.contains(index))
            return
        selectedIds.add(index)
        setSelectionMode(true)
        notifyItemChanged(index)
    }

    override fun onTap(index: Int) {
        if (selectionModeOwn || selectionModeOther)
            toggleSelected(index)
        else
            itemClickListener(getItem(index))
    }

    override fun onLongTap(index: Int) {
        setSelected(index)
    }

    class CharactersViewHolder(itemView: View, private val clickListener: ViewHolderClickListerner) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

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

            val progressBarHealth: ProgressBar = itemView.findViewById(R.id.progress_bar_health)

            val healthPercentage = 100 * health / healthMax
            progressBarHealth.progress = healthPercentage

//            val progressAnimator = ObjectAnimator.ofInt(progressBarHealth, "progress", 0, healthPercentage)
//            progressAnimator.duration = 3000
//            progressAnimator.start()

            val color = when {
                healthPercentage >= 66 -> ContextCompat.getColor(progressBarHealth.context, R.color.hp_green)
                healthPercentage >= 33 -> ContextCompat.getColor(progressBarHealth.context, R.color.hp_yellow)
                else -> ContextCompat.getColor(progressBarHealth.context, R.color.hp_red)
            }

            val progressDrawable = progressBarHealth.progressDrawable.mutate() as LayerDrawable
            progressDrawable.getDrawable(1).setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            progressBarHealth.progressDrawable = progressDrawable

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
            //itemView.setOnClickListener { clickListener(personagem) }
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener.onTap(adapterPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            clickListener.onLongTap(adapterPosition)
            return true
        }
    }
}