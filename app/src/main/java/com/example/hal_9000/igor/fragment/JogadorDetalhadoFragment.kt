package com.example.hal_9000.igor.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.hal_9000.igor.R
import kotlinx.android.synthetic.main.fragment_jogador_detalhado.*
import kotlinx.android.synthetic.main.fragment_jogador_detalhado.view.*
import android.animation.ObjectAnimator
import android.view.ViewTreeObserver
import android.support.v4.content.ContextCompat
import com.example.hal_9000.igor.adapters.AtributosListAdapter
import com.example.hal_9000.igor.model.Atributo
import kotlin.math.exp


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
        var expand: Boolean = false

        view.descricaoJogador.viewTreeObserver.addOnGlobalLayoutListener {
            if (expand) {
                expand = false
                if (view.descricaoJogador.lineCount > 4) {
                    expand = true
                    textoVerMais.visibility = View.VISIBLE
                    flechaVerMais.visibility = View.VISIBLE
                    val animation = ObjectAnimator.ofInt(view.descricaoJogador, "maxLines", 4)
                    animation.setDuration(0).start()
                }
            }
        }

        // Parte do código responsável pela expansão do texto de descrição, não foi testado ainda
        view.setOnClickListener { v ->
            when(v!!.id) {
                R.id.flechaVerMais -> {
                    if (!expand) {
                        expand = true
                        val animation = ObjectAnimator.ofInt(view.descricaoJogador, "maxLines", 40)
                        animation.setDuration(100).start()
                        view.flechaVerMais.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.abc_ic_arrow_drop_right_black_24dp))
                    } else {
                        expand = false
                        val animation = ObjectAnimator.ofInt(view.descricaoJogador, "maxLines", 4)
                        animation.setDuration(100).start()
                        view.flechaVerMais.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.abc_ic_arrow_drop_right_black_24dp))
                    }
                }

                R.id.textoVerMais -> {/* do your code */}
            }
        }

        var atributosList: ArrayList<Atributo> = ArrayList<Atributo>()
        atributosList.add(Atributo("Força", "10"))
        atributosList.add(Atributo("Inteligência", "20"))
        atributosList.add(Atributo("Destreza", "9"))

        view.listaDeAtributos.adapter = AtributosListAdapter(atributosList)
        return view
    }

}
