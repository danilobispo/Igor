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
import com.example.hal_9000.igor.model.Session
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_andamento.*

class AndamentoFragment : Fragment() {

    private val TAG = "AndamentoFragment"
    private var sessions: ArrayList<Session> = arrayListOf()

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_andamento, container, false)

        Log.d(TAG, "onCreateView: Started")

        val aventura = AdventureFragmentArgs.fromBundle(arguments).aventura

        val listView = view?.findViewById(R.id.lv_next_sessions) as ListView

        db = FirebaseFirestore.getInstance()
        db
                .collection("adventures")
                .document("${aventura.creator}_${aventura.title}")
                .collection("sessions")
                .get()
                .addOnSuccessListener { it ->
                    val values: ArrayList<String> = arrayListOf()

                    for (session in it.documents) {
                        values.add("${session["date"].toString()} ${session["title"].toString()}")
                        sessions.add(session.toObject(Session::class.java)!!)
                    }

                    if (context != null) {
                        val adapter = ArrayAdapter<String>(context!!, R.layout.next_sessions_item, R.id.tv_session, values)
                        listView.adapter = adapter
                    }
                }

        listView.setOnItemClickListener { parent, _, position, _ ->

            Log.d(TAG, "${parent.getItemAtPosition(position)}")

            val session = sessions[position]

            if (AdventureFragment.editMode) {
                val action = AdventureFragmentDirections.ActionAdventureFragmentToNewSession(aventura, session)
                action.setAventura(aventura)
                action.setSession(session)
                NavHostFragment.findNavController(this).navigate(action)
            }
        }

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
