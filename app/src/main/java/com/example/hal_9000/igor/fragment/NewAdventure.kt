package com.example.hal_9000.igor.fragment

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.NavHostFragment
import com.example.hal_9000.igor.LoginActivity
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Aventura
import com.example.hal_9000.igor.model.Personagem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlinx.android.synthetic.main.fragment_new_adventure.*

class NewAdventure : Fragment() {

    private val TAG = "NewAdventure"

    private lateinit var adventureOld: Aventura
    private var editMode: Boolean = false

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var rb1: RadioButton
    private lateinit var rb2: RadioButton
    private lateinit var rb3: RadioButton
    private lateinit var rb4: RadioButton
    private lateinit var rb5: RadioButton
    private lateinit var mProgressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_new_adventure, container, false)

        rb1 = view.findViewById(R.id.rb_1)
        rb2 = view.findViewById(R.id.rb_2)
        rb3 = view.findViewById(R.id.rb_3)
        rb4 = view.findViewById(R.id.rb_4)
        rb5 = view.findViewById(R.id.rb_5)

        mProgressBar = view.findViewById(R.id.progressBar)

        val buttonFinish = view.findViewById<Button>(R.id.btn_finish)
        val imageClose = view.findViewById<ImageView>(R.id.iv_close)
        val fabEditMode = view.findViewById<FloatingActionButton>(R.id.fab_edit_mode)

        rb1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                clearOthersRadioButtons(1)
        }
        rb2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                clearOthersRadioButtons(2)
        }
        rb3.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                clearOthersRadioButtons(3)
        }
        rb4.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                clearOthersRadioButtons(4)
        }
        rb5.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                clearOthersRadioButtons(5)
        }

        adventureOld = AdventureFragmentArgs.fromBundle(arguments).aventura

        if (adventureOld.id.isEmpty()) {
            editMode = false
        } else {
            editMode = true
            val tvHeaderTitle = view.findViewById<TextView>(R.id.tv_header_title)
            tvHeaderTitle.text = "Editar Aventura"
            etTitle = view.findViewById(R.id.et_title)
            etDescription = view.findViewById(R.id.et_descricao)
            completeFields()
        }

        buttonFinish.setOnClickListener { saveAdventure() }
        imageClose.setOnClickListener { exitFragment() }
        fabEditMode.setOnClickListener { exitFragment() }

        return view
    }

    private fun completeFields() {
        etTitle.setText(adventureOld.title)
        etDescription.setText(adventureOld.description)

        when (adventureOld.theme) {
            "krevast" -> rb2.isChecked = true
            "corvali" -> rb3.isChecked = true
            "heartlands" -> rb4.isChecked = true
            "coast" -> rb5.isChecked = true
            "default" -> rb1.isChecked = true
        }
    }

    private fun saveAdventure() {
        if (et_title.text.isEmpty()) {
            Toast.makeText(context, "Escolha um título", Toast.LENGTH_SHORT).show()
            return
        }

        mProgressBar.visibility = View.VISIBLE

        val adventure = Aventura()

        if (editMode) {
            adventure.creator = adventureOld.creator
            adventure.created_at = adventureOld.created_at
            adventure.id = adventureOld.id
        } else {
            adventure.creator = LoginActivity.username
            adventure.created_at = System.currentTimeMillis()
            adventure.id = "${adventure.creator}_${adventure.created_at}"
        }

        adventure.title = et_title.text.toString()
        adventure.description = et_descricao.text.toString()
        adventure.players[adventure.creator] = true

        adventure.theme = when {
            rb2.isChecked -> "krevast"
            rb3.isChecked -> "corvali"
            rb4.isChecked -> "heartlands"
            rb5.isChecked -> "coast"
            else -> "default"
        }

        val master = Personagem(
                id = "System_${adventure.created_at}",
                nome = adventure.creator,
                classe = "Mestre",
                created_at = adventure.created_at,
                creator = "System",
                ismaster = true,
                isnpc = false,
                aventura_id = adventure.id)

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val batch: WriteBatch = FirebaseFirestore.getInstance().batch()

        batch.set(db.collection("adventures").document(adventure.id), adventure)
        batch.set(db.collection("characters").document(master.id), master)
        batch.commit()
                .addOnSuccessListener {
                    Log.d(TAG, "Document created_at successfully. Edit: $editMode")
                    if (editMode)
                        Toast.makeText(context, "Aventura atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(context, "Aventura criada com sucesso!", Toast.LENGTH_SHORT).show()
                    exitFragment()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error creating document. Edit: $editMode", e)
                    Toast.makeText(context, "Erro ao criar aventura", Toast.LENGTH_SHORT).show()
                    mProgressBar.visibility = View.INVISIBLE
                }
    }

    private fun exitFragment() {
        if (editMode)
            NavHostFragment.findNavController(this).popBackStack(R.id.adventureFragment, false)
        else
            NavHostFragment.findNavController(this).navigate(R.id.action_newAdventure_to_homeFragment)
    }

    private fun clearOthersRadioButtons(rb: Int) {
        if (rb != 1) rb1.isChecked = false
        if (rb != 2) rb2.isChecked = false
        if (rb != 3) rb3.isChecked = false
        if (rb != 4) rb4.isChecked = false
        if (rb != 5) rb5.isChecked = false
    }

    companion object {
        @JvmStatic
        fun newInstance() = NewAdventure()
    }
}
