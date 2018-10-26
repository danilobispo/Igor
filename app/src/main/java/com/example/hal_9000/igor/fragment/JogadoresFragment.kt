package com.example.hal_9000.igor.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.JogadoresListAdapter
import com.example.hal_9000.igor.model.Jogador
import kotlinx.android.synthetic.main.fragment_jogadores.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class JogadoresFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val viewManager = LinearLayoutManager(context)
        val dataset = ArrayList<Jogador>()
        dataset.add(Jogador("D4RK 4VENGER", "BRUNO"))
        dataset.add(Jogador("D4RK 4VENGER", "BRUNO"))
        dataset.add(Jogador("D4RK 4VENGER", "BRUNO"))

        jogadores_rv.setHasFixedSize(true)
        // use a linear layout manager
        jogadores_rv.layoutManager = viewManager
        // specify an viewAdapter (see also next example)
        jogadores_rv.adapter = JogadoresListAdapter(dataset)

        return inflater.inflate(R.layout.fragment_jogadores, container, false)
    }


}
