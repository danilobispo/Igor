package com.example.hal_9000.igor.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.example.hal_9000.igor.viewmodel.MainViewModel
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.JogadoresListAdapter
import com.example.hal_9000.igor.model.Personagem
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore

class JogadoresFragment : Fragment() {
    private val TAG = "JogadoresFragment"

    private lateinit var adapter: JogadoresListAdapter
    private lateinit var mJogadoresList: RecyclerView
    private lateinit var db: FirebaseFirestore

    private lateinit var model: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_jogadores, container, false)

        mJogadoresList = view.findViewById(R.id.jogadores_rv)

        model = activity!!.run {
            ViewModelProviders.of(this).get(MainViewModel::class.java)
        }

        mJogadoresList.layoutManager = LinearLayoutManager(context)
        mJogadoresList.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()

        val aventura = model.getAdventure()!!
        val query = db.collection("characters")
                .whereEqualTo("aventura_id", aventura.id)
                .whereEqualTo("isnpc", false)

        val options = FirestoreRecyclerOptions.Builder<Personagem>()
                .setQuery(query, Personagem::class.java)
                .build()

        adapter = JogadoresListAdapter(options) { personagem: Personagem -> personagemItemClicked(personagem) }
        mJogadoresList.adapter = adapter

        return view
    }

    private fun personagemItemClicked(personagem: Personagem) {
        val action = AdventureFragmentDirections.ActionAdventureFragmentToCharacterProfileFragment(personagem)
        action.setCharacter(personagem)
        action.setReadOnly(true)
        NavHostFragment.findNavController(this).navigate(action)
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
