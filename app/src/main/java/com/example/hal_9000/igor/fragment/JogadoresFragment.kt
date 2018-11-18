package com.example.hal_9000.igor.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.JogadoresListAdapter
import com.example.hal_9000.igor.model.Aventura
import com.example.hal_9000.igor.model.Personagem
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore


class JogadoresFragment : Fragment() {

    private val TAG = "JogadoresFragment"

    private var adapter: JogadoresListAdapter? = null
    private var mJogadoresList: RecyclerView? = null
    private var db: FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_jogadores, container, false)

        mJogadoresList = view.findViewById(R.id.jogadores_rv)
        mJogadoresList?.layoutManager = LinearLayoutManager(context)
        mJogadoresList?.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()

        val aventura = AdventureFragment.aventura
        val query = db!!.collection("characters")
                .whereEqualTo("aventura_id", aventura.id)
                .whereEqualTo("isnpc", false)

        val options = FirestoreRecyclerOptions.Builder<Personagem>()
                .setQuery(query, Personagem::class.java)
                .build()

        adapter = JogadoresListAdapter(options, { personagem: Personagem -> personagemItemClicked(personagem) }, { personagem: Personagem -> personagemLongItemClicked(personagem) })
        mJogadoresList?.adapter = adapter

        return view
    }

    private fun personagemItemClicked(personagem: Personagem) {
        val action = AdventureFragmentDirections.ActionAdventureFragmentToCharacterProfileFragment(personagem)
        action.setCharacter(personagem)
        NavHostFragment.findNavController(this).navigate(action)
    }

    private fun personagemLongItemClicked(personagem: Personagem) {
        val action = AdventureFragmentDirections.ActionAdventureFragmentToNewCharacterFragment(personagem)
        action.setPersonagem(personagem)
        action.setIsNpc(false)
        NavHostFragment.findNavController(this).navigate(action)
    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter!!.stopListening()
    }

    companion object {
        @JvmStatic
        fun newInstance() = JogadoresFragment()
    }
}
