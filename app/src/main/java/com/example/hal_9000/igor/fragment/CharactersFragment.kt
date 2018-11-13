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
import androidx.navigation.NavHost
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.CharactersListAdapter
import com.example.hal_9000.igor.model.Aventura
import com.example.hal_9000.igor.model.Personagem
import com.example.hal_9000.igor.model.Session
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore

class CharactersFragment : Fragment() {

    private val TAG = "CombatFragment"

    private lateinit var aventura: Aventura
    private lateinit var session: Session

    private lateinit var adapter: CharactersListAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_characters, container, false)

//        aventura = NewSessionArgs.fromBundle(arguments).aventura
//        session = NewSessionArgs.fromBundle(arguments).session

        aventura = SessionFragment.aventura
        session = SessionFragment.session

        mRecyclerView = view.findViewById(R.id.eventos_rv)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()

        val aventura = AdventureFragment.aventura
        val query = db.collection("characters")
                .whereEqualTo("aventuraId", "${aventura.creator}_${aventura.title}")
                .orderBy("created")

        val options = FirestoreRecyclerOptions.Builder<Personagem>()
                .setQuery(query, Personagem::class.java)
                .build()

        adapter = CharactersListAdapter(options) { character: Personagem -> characterItemClicked(character) }
        mRecyclerView.adapter = adapter

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val action = SessionFragmentDirections.ActionSessionFragmentToNewCharacterFragment(SessionFragment.aventura, Personagem())
            action.setAventura(SessionFragment.aventura)
            action.setPersonagem(Personagem())
            action.setIsNpc(true)
            Navigation.findNavController(activity!!, R.id.nav_host).navigate(action)
        }

        return view
    }

    private fun characterItemClicked(character: Personagem) {
        Log.d(TAG, "Clicked ${character.nome}")
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    companion object {
        @JvmStatic
        fun newInstance() = CharactersFragment()
    }
}
