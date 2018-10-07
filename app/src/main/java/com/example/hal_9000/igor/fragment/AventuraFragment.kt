package com.example.hal_9000.igor.fragment


import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.AdventureRecyclerViewAdapter
import com.example.hal_9000.igor.model.Aventura
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore


class AventuraFragment : Fragment() {

    private val TAG = "AventuraFragment"

    private var adapter: AdventureRecyclerViewAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_aventura, container, false)

        Log.d(TAG, "onCreateView: Started")


        val mRecyclerView: RecyclerView = view.findViewById(R.id.rv_adventures_list)
        mRecyclerView.layoutManager = LinearLayoutManager(context)

        val db = FirebaseFirestore.getInstance()
        val query = db.collection("adventures")

        val options = FirestoreRecyclerOptions.Builder<Aventura>()
                .setQuery(query, Aventura::class.java)
                .build()

        adapter = AdventureRecyclerViewAdapter(options)
        mRecyclerView.adapter = adapter

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_nova_aventura)
        fab.setOnClickListener {
            findNavController(this).navigate(R.id.action_aventuraFragment_to_newAdventure)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()

        if (adapter != null) {
            adapter!!.stopListening()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AventuraFragment()
    }
}
