package com.example.hal_9000.igor.fragment

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.CharactersListAdapter
import com.example.hal_9000.igor.model.Personagem
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore

class CharactersFragment : Fragment() {

    private val TAG = "EventsFragment"

    private lateinit var adapter: CharactersListAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_characters, container, false)

        val fabNewCharacter = view.findViewById<FloatingActionButton>(R.id.fab_new_character)

        mRecyclerView = view.findViewById(R.id.eventos_rv)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()

        val query = if (AdventureFragment.isMaster)
            db.collection("characters")
                    .whereEqualTo("aventura_id", AdventureFragment.aventura.id)
        else
            db.collection("characters")
                    .whereEqualTo("aventura_id", AdventureFragment.aventura.id)
                    .whereEqualTo("hidden", false)

        val options = FirestoreRecyclerOptions.Builder<Personagem>()
                .setQuery(query, Personagem::class.java)
                .build()

        adapter = CharactersListAdapter(options) { character: Personagem -> characterItemClicked(character) }
        mRecyclerView.adapter = adapter

        if (!AdventureFragment.isMaster) {
            fabNewCharacter.hide()
            return view
        }

        fabNewCharacter.setOnClickListener {
            val action = SessionFragmentDirections.ActionSessionFragmentToNewCharacterFragment(Personagem())
            action.setPersonagem(Personagem())
            action.setIsNpc(true)
            Navigation.findNavController(activity!!, R.id.nav_host).navigate(action)
        }

        return view
    }

    private fun characterItemClicked(character: Personagem) {
        val action = SessionFragmentDirections.ActionSessionFragmentToCharacterProfileFragment(character)
        action.setCharacter(character)
        action.setReadOnly(false)
        Navigation.findNavController(activity!!, R.id.nav_host).navigate(action)
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
