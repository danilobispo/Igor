package com.example.hal_9000.igor.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.hal_9000.igor.R
import kotlinx.android.synthetic.main.fragment_jogador_detalhado.*
import kotlinx.android.synthetic.main.fragment_jogador_detalhado.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class JogadorDetalhadoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_jogador_detalhado, container, false)

        if(view.descricaoJogador.text.length > 50) {
            view.textoVerMais.visibility = View.VISIBLE
            view.flechaVerMais.visibility = View.VISIBLE
        }

        return view
    }

}
