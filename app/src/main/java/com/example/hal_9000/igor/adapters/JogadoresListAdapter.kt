package com.example.hal_9000.igor.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Jogador

class JogadoresListAdapter(jogadoresList: ArrayList<Jogador>) : RecyclerView.Adapter<JogadoresViewHolder>() {

    private val jogadoresList: ArrayList<Jogador> = jogadoresList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JogadoresViewHolder {
        return JogadoresViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.jogador_item, parent, false))
    }

    override fun onBindViewHolder(holder: JogadoresViewHolder, position: Int) {
        holder.roleJogador.text = "D4RK4V3NG3R"
        holder.nomeJogador.text = "Bruno"
    }

    override fun getItemCount(): Int {
        return jogadoresList.size
    }

}