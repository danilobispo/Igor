package com.example.hal_9000.igor.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Dice

class DicesListAdapter(private val myDataset: ArrayList<Dice>, private val itemClickListener: (Int) -> Unit) : RecyclerView.Adapter<DicesListAdapter.DicesViewHolder>() {
    private val TAG = "DicesListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DicesListAdapter.DicesViewHolder {
        return DicesListAdapter.DicesViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.dice_item, parent, false))
    }

    override fun getItemCount() = myDataset.size

    override fun onBindViewHolder(holder: DicesViewHolder, position: Int) {
        holder.setDiceNumber(position + 1)
        holder.setDiceValue(myDataset[position].value)
        holder.setDiceName(myDataset[position].dice)
        holder.setClickListener(position, itemClickListener)
    }

    class DicesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setDiceNumber(number: Int) {
            val tvNumber: TextView = itemView.findViewById(R.id.tv_number)
            tvNumber.text = number.toString()
        }

        fun setDiceValue(value: Int) {
            val tvValue: TextView = itemView.findViewById(R.id.tv_value)
            tvValue.text = value.toString()
        }


        fun setDiceName(name: String) {
            val tvName: TextView = itemView.findViewById(R.id.tv_name)
            tvName.text = name
        }

        fun setClickListener(position: Int, itemClickListener: (Int) -> Unit) {
            itemView.setOnClickListener { itemClickListener(position) }
        }
    }
}