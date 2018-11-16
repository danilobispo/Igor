package com.example.hal_9000.igor.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.SessionsListAdapter
import com.example.hal_9000.igor.model.Aventura
import com.example.hal_9000.igor.model.Session
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_andamento.*

class AndamentoFragment : Fragment() {

    private val TAG = "AndamentoFragment"
    private lateinit var aventura: Aventura

    private lateinit var adapter: SessionsListAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_andamento, container, false)

        Log.d(TAG, "onCreateView: Started")

        aventura = AdventureFragmentArgs.fromBundle(arguments).aventura

        mRecyclerView = view.findViewById(R.id.rv_next_sessions)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()

        val query = db
                .collection("adventures")
                .document("${aventura.creator}_${aventura.title}")
                .collection("sessions")

        val options = FirestoreRecyclerOptions.Builder<Session>()
                .setQuery(query, Session::class.java)
                .build()

        adapter = SessionsListAdapter(options) { session: Session -> sessionItemClicked(session) }
        mRecyclerView.adapter = adapter

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

    private fun sessionItemClicked(session: Session) {
        Log.d(TAG, "Clicked ${session.title}")

        if (AdventureFragment.editMode) {
            val action = AdventureFragmentDirections.ActionAdventureFragmentToNewSession(aventura, session)
            action.setAventura(aventura)
            action.setSession(session)
            NavHostFragment.findNavController(this).navigate(action)
        } else {
            val action = AdventureFragmentDirections.ActionAdventureFragmentToSessionFragment(aventura, session)
            action.setAventura(aventura)
            action.setSession(session)
            NavHostFragment.findNavController(this).navigate(action)
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}
