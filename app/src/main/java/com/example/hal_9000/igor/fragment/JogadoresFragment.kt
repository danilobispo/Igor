package com.example.hal_9000.igor.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.JogadoresListAdapter
import com.example.hal_9000.igor.model.Jogador


class JogadoresFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_jogadores, container, false)

        val viewManager = LinearLayoutManager(context)
        val dataset = ArrayList<Jogador>()
        dataset.add(Jogador("D4RK 4VENGER", "BRUNO"))
        dataset.add(Jogador("D4RK 4VENGER", "BRUNO"))
        dataset.add(Jogador("D4RK 4VENGER", "BRUNO"))

        val jogadoresRv = view.findViewById<RecyclerView>(R.id.jogadores_rv)
        jogadoresRv.setHasFixedSize(true)
        // use a linear layout manager
        jogadoresRv.layoutManager = viewManager
        // specify an viewAdapter (see also next example)
        jogadoresRv.adapter = JogadoresListAdapter(dataset)

        return view
    }
}
