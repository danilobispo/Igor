package com.example.hal_9000.igor.fragment


import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.example.hal_9000.igor.R


class AventuraFragment : Fragment() {

    private val TAG = "AventuraFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_aventura, container, false)

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_nova_aventura)
        fab.setOnClickListener {
            findNavController(this).navigate(R.id.action_aventuraFragment_to_newAdventure)
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = AventuraFragment()
    }
}
