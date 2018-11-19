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
import com.example.hal_9000.igor.model.Item
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.android.synthetic.main.character_item.view.*

class ItemsListAdapter(options: FirestoreRecyclerOptions<Item>, private val itemClickListener: (Item) -> Unit) : FirestoreRecyclerAdapter<Item, ItemsListAdapter.CharactersViewHolder>(options), ViewHolderClickListerner {

    private val TAG = "CharsCombatListAdapter"

    val selectedIds: MutableList<Int> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharactersViewHolder {
        return CharactersViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_item, parent, false), this)
    }

    override fun onBindViewHolder(holder: CharactersViewHolder, position: Int, model: Item) {
        holder.setItemName(model.name)
        holder.setItemType(model.type)
        holder.setItemImage(model.image_url)
        holder.setClickListener(model, itemClickListener)

        if (selectedIds.contains(position))
            holder.itemView.selected_overlay.visibility = View.VISIBLE
        else
            holder.itemView.selected_overlay.visibility = View.INVISIBLE
    }

    override fun onError(e: FirebaseFirestoreException) {
        Log.e(TAG, "Error: $e.message")
    }

    private fun toggleSelected(index: Int) {
        if (selectedIds.contains(index))
            selectedIds.remove(index)
        else
            selectedIds.add(index)
        notifyItemChanged(index)
    }

    private fun setSelected(index: Int) {
        if (selectedIds.contains(index))
            return
        selectedIds.add(index)
        notifyItemChanged(index)
    }

    override fun onTap(index: Int) {
        if (selectedIds.size > 0)
            toggleSelected(index)
        else
            itemClickListener(getItem(index))
    }

    override fun onLongTap(index: Int) {
        setSelected(index)
    }

    class CharactersViewHolder(itemView: View, private val clickListener: ViewHolderClickListerner) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

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
//            itemView.setOnClickListener { clickListener(item) }
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