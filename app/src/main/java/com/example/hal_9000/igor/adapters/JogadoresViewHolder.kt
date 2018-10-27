package com.example.hal_9000.igor.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.example.hal_9000.igor.R

class JogadoresViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var imagemJogador: ImageView
    var nomeJogador: TextView
    var roleJogador: TextView
    var descricaoJogador: TextView

    init {
        imagemJogador = itemView.findViewById<View>(R.id.imagemJogadorItem) as ImageView
        nomeJogador = itemView.findViewById<View>(R.id.nomeJogadorItem) as TextView
        roleJogador = itemView.findViewById<View>(R.id.roleJogadorItem) as TextView
        descricaoJogador = itemView.findViewById<View>(R.id.descricaoJogadorItem) as TextView
    }
}