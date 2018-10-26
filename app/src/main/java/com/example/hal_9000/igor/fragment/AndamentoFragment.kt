package com.example.hal_9000.igor.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.example.hal_9000.igor.R
import kotlinx.android.synthetic.main.fragment_andamento.*

class AndamentoFragment : Fragment() {

    private val TAG = "AndamentoFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_andamento, container, false)

        Log.d(TAG, "onCreateView: Started")

        val aventura = AdventureFragmentArgs.fromBundle(arguments).aventura

        val listView = view?.findViewById(R.id.lv_next_sessions) as ListView
        val values: ArrayList<String> = arrayListOf()

        for (session in aventura.sessions)
            values.add("${session.date} ${session.title}")

        val adapter = ArrayAdapter<String>(context, R.layout.next_sessions_item, R.id.tv_session, values)
        listView.adapter = adapter

//        listView.setOnItemClickListener { parent, _, position, _ ->
//            Log.d("", "${parent.getItemAtPosition(position)}")
//            if (editMode) {
//                val action = AdventureFragmentDirections.ActionAdventureFragmentToNewSession(aventura!!)
//                action.setAventura(aventura!!)
//                action.setSession(position)
//                NavHostFragment.findNavController(this).navigate(action)
//            }
//        }

        val tvDescription = view.findViewById<TextView>(R.id.tv_description)

        if (aventura.description.isEmpty())
            tvDescription.text = "Aventura sem descrição"
        else
            tvDescription.text = aventura.description

        tvDescription.post {
            if (tvDescription.lineCount > 6) {

                tv_see_more.visibility = View.VISIBLE

                tv_see_more.setOnClickListener {
                    if (tvDescription.maxLines == 6) {
                        tvDescription.maxLines = 99
                        tv_see_more.text = "ver menos ▲"
                    } else {
                        tvDescription.maxLines = 6
                        tv_see_more.text = "ver mais ▼"
                    }
                }
            }
        }

        return view
    }
}
