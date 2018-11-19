package com.example.hal_9000.igor.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Atributo


class StatsListAdapter(private val myDataset: ArrayList<Atributo>) : RecyclerView.Adapter<StatsListAdapter.StatsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        return StatsViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.stat_item, parent, false))
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        holder.setStatName(myDataset[position].nome)
        holder.setStatValue(myDataset[position].valor)
    }

    override fun getItemCount() = myDataset.size

    class StatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setStatName(name: String) {
            val tvName: TextView = itemView.findViewById(R.id.tv_stat_name) as TextView
            tvName.text = name
        }

        fun setStatValue(value: String) {
            val tvValue: TextView = itemView.findViewById(R.id.tv_stat_value) as TextView
            tvValue.text = value
        }
    }
}