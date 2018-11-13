package com.example.hal_9000.igor.fragment


import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment

import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Aventura
import com.example.hal_9000.igor.model.Session
import android.R.attr.fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.hal_9000.igor.model.Personagem


class SessionFragment : Fragment() {

    private val TAG = "SessionFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_session, container, false)

        Log.d(TAG, "onCreateView")

        aventura = SessionFragmentArgs.fromBundle(arguments).aventura
        session = SessionFragmentArgs.fromBundle(arguments).session

        val args = Bundle()
        args.putParcelable("aventura", aventura)
        args.putParcelable("session", session)

        val fragmentContainer = view.findViewById<View>(R.id.nav_host_session)
        val navController = Navigation.findNavController(fragmentContainer)
        navController.setGraph(R.navigation.sub_nav_graph, args)

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val adventureTitle = view.findViewById<TextView>(R.id.tv_adventure_title)
        val sessionTitle = view.findViewById<TextView>(R.id.tv_session_title)

        bottomNavigationView.setupWithNavController(navController)

        adventureTitle.text = aventura.title
        sessionTitle.text = session.title

        return view
    }

    companion object {
        lateinit var aventura: Aventura
        lateinit var session: Session

//        fun navigateToNewCharacter() {
//            val action = SessionFragmentDirections.ActionSessionFragmentToNewCharacterFragment(aventura, Personagem())
//            action.setAventura(aventura)
//            action.setPersonagem(Personagem())
//            action.setIsNpc(true)
//            NavHostFragment.findNavController(getAc).navigate(action)
//        }
    }
}
