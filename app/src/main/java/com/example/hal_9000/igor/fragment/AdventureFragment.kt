package com.example.hal_9000.igor.fragment

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
import android.widget.*
import androidx.navigation.fragment.NavHostFragment
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Aventura
import kotlinx.android.synthetic.main.fragment_adventure.*


class AdventureFragment : Fragment() {

    private val TAG = "AdventureFragment"
    private var editMode = false
    var aventura: Aventura? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_adventure, container, false)
        fragmentManager!!.beginTransaction().add(R.id.frameAventura, AdventureFragment()).addToBackStack(null).commit()

        Log.d(TAG, "onCreateView: Started")

        setHasOptionsMenu(true)

        aventura = AdventureFragmentArgs.fromBundle(arguments).aventura

        val tvTitle = view.findViewById<TextView>(R.id.tv_adventure_title)
        tvTitle.text = aventura!!.title

        val ivTheme = view.findViewById<ImageView>(R.id.iv_theme)
        val layout = view.findViewById<ConstraintLayout>(R.id.contraint_layout)
        when (aventura!!.theme) {
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

        val ivTabLeft = view.findViewById<ImageView>(R.id.iv_tab_left)
        val ivTabRight = view.findViewById<ImageView>(R.id.iv_tab_right)

        ivTabLeft.setOnClickListener {
            Log.d(TAG, "left")
            ivTabLeft.setImageResource(R.drawable.tab_l1)
            ivTabRight.setImageResource(R.drawable.tab_r2)
            fragmentManager!!.beginTransaction().replace(R.id.frameAventura, AndamentoFragment()).addToBackStack(null).commit()
        }

        ivTabRight.setOnClickListener {
            Log.d(TAG, "right")
            ivTabLeft.setImageResource(R.drawable.tab_l2)
            ivTabRight.setImageResource(R.drawable.tab_r1)
            fragmentManager!!.beginTransaction().replace(R.id.frameAventura, JogadoresFragment()).addToBackStack(null).commit()
        }

        val fabEditMode = view.findViewById<FloatingActionButton>(R.id.fab_edit_mode)
        fabEditMode.setOnClickListener {
            setEditModeOff()
        }

        val fabEditSave = view.findViewById<FloatingActionButton>(R.id.fab_save_edit)
        fabEditSave.setOnClickListener {
            setEditModeOff()
        }

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_new_session)
        fab.setOnClickListener {
            val action = AdventureFragmentDirections.ActionAdventureFragmentToNewSession(aventura!!)
            action.setAventura(aventura!!)
            NavHostFragment.findNavController(this).navigate(action)
        }

        return view
    }

    private fun setEditModeOn() {
        Log.d(TAG, "Edit mode on")
        editMode = true

        fab_new_session.hide()
        fab_save_edit.show()
        fab_edit_mode.show()

        tv_adventure_title.setOnClickListener {
            val action = AdventureFragmentDirections.ActionAdventureFragmentToNewAdventure(aventura!!)
            action.setAventura(aventura!!)
            NavHostFragment.findNavController(this).navigate(action)
        }

//        tv_description.setOnClickListener {
//            val action = AdventureFragmentDirections.ActionAdventureFragmentToNewAdventure(aventura!!)
//            action.setAventura(aventura!!)
//            NavHostFragment.findNavController(this).navigate(action)
//        }
    }

    private fun setEditModeOff() {
        Log.d(TAG, "Edit mode off")
        editMode = false

        fab_new_session.show()
        fab_save_edit.hide()
        fab_edit_mode.hide()

        tv_adventure_title.setOnClickListener(null)
//        tv_description.setOnClickListener(null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_editar -> {
                Toast.makeText(context, "Editar", Toast.LENGTH_SHORT).show()
                setEditModeOn()
            }
            R.id.menu_ordenar -> {
                Toast.makeText(context, "Ordenar", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        @JvmStatic
        fun newInstance() = AdventureFragment()
    }
}
