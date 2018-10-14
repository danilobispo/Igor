package com.example.hal_9000.igor.fragment

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Aventura


class AdventureFragment : Fragment() {

    val TAG = "AdventureFragment"
    var aventura: Aventura? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_adventure, container, false)

        aventura = AdventureFragmentArgs.fromBundle(arguments).aventura

        val tvTitle = view.findViewById<TextView>(R.id.tv_adventure_title)
        tvTitle.text = aventura!!.title

        val ivTheme = view.findViewById<ImageView>(R.id.iv_theme)
        val layout = view.findViewById<ConstraintLayout>(R.id.contraint_layout)
        when (aventura!!.theme) {
            "krevast" -> {
                ivTheme.setImageResource(R.drawable.miniatura_krevast)
                layout.setBackgroundColor(ContextCompat.getColor(view.context, R.color.krevast_background))
            }
            "corvali" -> {
                ivTheme.setImageResource(R.drawable.miniatura_corvali)
                layout.setBackgroundColor(ContextCompat.getColor(view.context, R.color.corvali_background))
            }
            "heartlands" -> {
                ivTheme.setImageResource(R.drawable.miniatura_heartlands)
                layout.setBackgroundColor(ContextCompat.getColor(view.context, R.color.heartlands_background))
            }
            "coast" -> {
                ivTheme.setImageResource(R.drawable.miniatura_coast)
                layout.setBackgroundColor(ContextCompat.getColor(view.context, R.color.coast_background))
            }
            else -> {
                ivTheme.setImageResource(R.drawable.miniatura_imagem_automatica)
                layout.setBackgroundColor(ContextCompat.getColor(view.context, R.color.default_background))
            }
        }

        val listView = view.findViewById(R.id.lv_next_sessions) as ListView
        val values = arrayOf("17/10 Sessão sem título", "18/10 Sessão sem título", "19/10 Sessão sem título", "20/10 Sessão sem título")

        val adapter = ArrayAdapter<String>(view.context, R.layout.next_sessions_item, R.id.tv_session, values)
        listView.adapter = adapter

        listView.setOnItemClickListener { parent, _, position, _ ->
            Log.d(TAG, "${parent.getItemAtPosition(position)}")
        }

        val tvAdventureText = view.findViewById<TextView>(R.id.tv_adventure_text)
        tvAdventureText.text = "Aventura sem descrição"

        tvAdventureText.post {
            if (tvAdventureText.lineCount > 6) {
                val tvSeeMore = view.findViewById<TextView>(R.id.tv_see_more)
                tvSeeMore.visibility = View.VISIBLE

                tvSeeMore.setOnClickListener {
                    if (tvAdventureText.maxLines == 6) {
                        tvAdventureText.maxLines = 99
                        tvSeeMore.text = "ver menos ▲"
                    } else {
                        tvAdventureText.maxLines = 6
                        tvSeeMore.text = "ver mais ▼"
                    }
                }
            }
        }

        val ivTabLeft = view.findViewById<ImageView>(R.id.iv_tab_left)
        val ivTabRight = view.findViewById<ImageView>(R.id.iv_tab_right)

        ivTabLeft.setOnClickListener {
            Log.d(TAG, "left")
            ivTabLeft.setImageResource(R.drawable.tab_l1)
            ivTabRight.setImageResource(R.drawable.tab_r2)
        }

        ivTabRight.setOnClickListener {
            Log.d(TAG, "right")
            ivTabLeft.setImageResource(R.drawable.tab_l2)
            ivTabRight.setImageResource(R.drawable.tab_r1)
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = AdventureFragment()
    }
}
