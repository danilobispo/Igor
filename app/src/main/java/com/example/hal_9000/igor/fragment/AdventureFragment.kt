package com.example.hal_9000.igor.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Aventura
import com.example.hal_9000.igor.viewmodel.MainViewModel

class AdventureFragment : Fragment() {
    private val TAG = "AdventureFragment"

    private lateinit var openedTab: String
    private lateinit var ivTabLeft: ImageView
    private lateinit var ivTabRight: ImageView

    private lateinit var fabNewCharacter: FloatingActionButton
    private lateinit var fabNewSession: FloatingActionButton

    private lateinit var adventure: Aventura

    private lateinit var model: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openedTab = "andamento"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_adventure, container, false)

        setHasOptionsMenu(true)

        model = activity!!.run {
            ViewModelProviders.of(this).get(MainViewModel::class.java)
        }

        adventure = AdventureFragmentArgs.fromBundle(arguments).aventura
        val isMaster = adventure.creator == model.getUsername()

        model.setAdventure(adventure)
        model.setIsMaster(isMaster)

        fabNewCharacter = view.findViewById(R.id.fab_new_character)
        fabNewSession = view.findViewById(R.id.fab_new_session)
        ivTabLeft = view.findViewById(R.id.iv_tab_left)
        ivTabRight = view.findViewById(R.id.iv_tab_right)
        val fabNewSession = view.findViewById<FloatingActionButton>(R.id.fab_new_session)
        val fabNewCharacter = view.findViewById<FloatingActionButton>(R.id.fab_new_character)

        if (openedTab == "jogadores")
            switchTab("jogadores")
        else
            switchTab("andamento")

        val tvTitle = view.findViewById<TextView>(R.id.tv_adventure_title)
        tvTitle.text = adventure.title

        val ivTheme = view.findViewById<ImageView>(R.id.iv_theme)
        val layout = view.findViewById<ConstraintLayout>(R.id.contraint_layout)
        when (adventure.theme) {
            "krevast" -> {
                ivTheme.setImageResource(R.drawable.miniatura_krevast)
                layout.setBackgroundColor(ContextCompat.getColor(view.context, R.color.krevast_background))
            }
            "corvali" -> {
                ivTheme.setImageResource(R.drawable.miniatura_corvali)
                layout.setBackgroundColor(ContextCompat.getColor(view.context, R.color.corvali_background))
            }
            "heartlands" -> {
                ivTheme.setImageResource(R.drawable.miniatura_heartlands)
                layout.setBackgroundColor(ContextCompat.getColor(view.context, R.color.heartlands_background))
            }
            "coast" -> {
                ivTheme.setImageResource(R.drawable.miniatura_coast)
                layout.setBackgroundColor(ContextCompat.getColor(view.context, R.color.coast_background))
            }
            else -> {
                ivTheme.setImageResource(R.drawable.miniatura_imagem_automatica)
                layout.setBackgroundColor(ContextCompat.getColor(view.context, R.color.default_background))
            }
        }

        ivTabLeft.setOnClickListener {
            if (openedTab != "andamento")
                switchTab("andamento")
        }

        ivTabRight.setOnClickListener {
            if (openedTab != "jogadores")
                switchTab("jogadores")
        }

        if (!isMaster) {
            fabNewSession.hide()
            fabNewCharacter.hide()
            return view
        }

        fabNewSession.setOnClickListener {
            val action = AdventureFragmentDirections.ActionAdventureFragmentToNewSession()
            NavHostFragment.findNavController(this).navigate(action)
        }

        fabNewCharacter.setOnClickListener {
            val action = AdventureFragmentDirections.ActionAdventureFragmentToNewCharacterFragment()
            action.setIsNpc(false)
            NavHostFragment.findNavController(this).navigate(action)
        }

        return view
    }

    private fun switchTab(tab: String) {
        Log.d(TAG, "switchTab: $tab, OpenedTad: $openedTab")

        if (tab == "andamento") {
            openedTab = "andamento"
            ivTabLeft.setImageResource(R.drawable.tab_l1)
            ivTabRight.setImageResource(R.drawable.tab_r2)
            if (model.getIsMaster()!!) {
                fabNewCharacter.hide()
                fabNewSession.show()
            }
            fragmentManager!!.beginTransaction().replace(R.id.frameAventura, AndamentoFragment()).commit()

        } else if (tab == "jogadores") {
            openedTab = "jogadores"
            ivTabLeft.setImageResource(R.drawable.tab_l2)
            ivTabRight.setImageResource(R.drawable.tab_r1)
            if (model.getIsMaster()!!) {
                fabNewCharacter.show()
                fabNewSession.hide()
            }
            fragmentManager!!.beginTransaction().replace(R.id.frameAventura, JogadoresFragment()).commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!model.getIsMaster()!!)
            return super.onOptionsItemSelected(item)

        when (item.itemId) {
            R.id.menu_editar -> {
                val action = AdventureFragmentDirections.ActionAdventureFragmentToNewAdventure()
                action.setAventura(adventure)
                NavHostFragment.findNavController(this).navigate(action)
            }
            R.id.menu_ordenar -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        var editMode: Boolean = false
    }
}
