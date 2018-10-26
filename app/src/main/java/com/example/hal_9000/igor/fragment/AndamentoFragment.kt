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
import androidx.navigation.fragment.NavHostFragment

import com.example.hal_9000.igor.R
import kotlinx.android.synthetic.main.fragment_andamento.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class AndamentoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        
        val aventura = AdventureFragmentArgs.fromBundle(arguments).aventura

        val listView = view?.findViewById(R.id.lv_next_sessions) as ListView
        val values: ArrayList<String> = arrayListOf()

        for (session in aventura!!.sessions)
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



        if (aventura!!.description.isEmpty())
            tv_description.text = "Aventura sem descrição"
        else
            tv_description.text = aventura!!.description

        tv_description.post {
            if (tv_description.lineCount > 6) {

                tv_see_more.visibility = View.VISIBLE

                tv_see_more.setOnClickListener {
                    if (tv_description.maxLines == 6) {
                        tv_description.maxLines = 99
                        tv_see_more.text = "ver menos ▲"
                    } else {
                        tv_description.maxLines = 6
                        tv_see_more.text = "ver mais ▼"
                    }
                }
            }
        }

        return inflater.inflate(R.layout.fragment_andamento, container, false)
    }


}
