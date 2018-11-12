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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_new_adventure.*


class NewAdventure : Fragment() {

    private val TAG = "NewAdventure"

    private var editMode: Boolean? = null

    private var adventure: Aventura? = null

    private var rb1: RadioButton? = null
    private var rb2: RadioButton? = null
    private var rb3: RadioButton? = null
    private var rb4: RadioButton? = null
    private var rb5: RadioButton? = null

    private var etTitle: EditText? = null
    private var etDescription: EditText? = null

    private var mProgressBar: ProgressBar? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_new_adventure, container, false)

        adventure = AdventureFragmentArgs.fromBundle(arguments).aventura

        rb1 = view.findViewById(R.id.rb_1)
        rb2 = view.findViewById(R.id.rb_2)
        rb3 = view.findViewById(R.id.rb_3)
        rb4 = view.findViewById(R.id.rb_4)
        rb5 = view.findViewById(R.id.rb_5)

        mProgressBar = view.findViewById(R.id.progressBar)

        rb1?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                clearOthersRadioButtons(1)
        }
        rb2?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                clearOthersRadioButtons(2)
        }
        rb3?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                clearOthersRadioButtons(3)
        }
        rb4?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                clearOthersRadioButtons(4)
        }
        rb5?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                clearOthersRadioButtons(5)
        }

        if (!adventure!!.creator.isEmpty()) {
            editMode = true
            val tvHeaderTitle = view.findViewById<TextView>(R.id.tv_header_title)
            tvHeaderTitle.text = "Editar Aventura"

            etTitle = view.findViewById(R.id.et_title)
            etDescription = view.findViewById(R.id.et_descricao)
            completeFields()
        } else {
            editMode = false
        }

        val buttonFinish = view.findViewById<Button>(R.id.btn_finish)
        buttonFinish.setOnClickListener {
            saveAdventure()
        }

        val imageClose = view.findViewById<ImageView>(R.id.iv_close)
        imageClose.setOnClickListener {
            exitFragment()
        }

        val fabEditMode = view.findViewById<FloatingActionButton>(R.id.fab_edit_mode)
        fabEditMode.setOnClickListener {
            exitFragment()
        }

        return view
    }

    private fun completeFields() {
        if (adventure!!.title.isNotEmpty())
            etTitle!!.setText(adventure!!.title)

        if (adventure!!.description.isNotEmpty())
            etDescription!!.setText(adventure!!.description)

        if (adventure!!.theme.isNotEmpty())
            when (adventure!!.theme) {
                "krevast" -> rb2!!.isChecked = true
                "corvali" -> rb3!!.isChecked = true
                "heartlands" -> rb4!!.isChecked = true
                "coast" -> rb5!!.isChecked = true
                "default" -> rb1!!.isChecked = true
            }
    }

    private fun saveAdventure() {
        if (et_title.text.isEmpty()) {
            Toast.makeText(context, "Escolha um título", Toast.LENGTH_SHORT).show()
            return
        }

        mProgressBar!!.visibility = View.VISIBLE

        val theme: String = when {
            rb2!!.isChecked -> "krevast"
            rb3!!.isChecked -> "corvali"
            rb4!!.isChecked -> "heartlands"
            rb5!!.isChecked -> "coast"
            else -> "default"
        }

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val mAuth = FirebaseAuth.getInstance()

        if (!editMode!!) {
            adventure = Aventura()
        }

        val originalTitle = adventure!!.title

        adventure!!.title = et_title.text.toString()
        adventure!!.description = et_descricao.text.toString()
        adventure!!.theme = theme
        adventure!!.creator = mAuth.currentUser!!.uid
        adventure!!.deleted = false

        adventure!!.players[LoginActivity.username] = true

        if (editMode!!) {
            db.collection("adventures")
                    .whereEqualTo("title", originalTitle)
                    .whereEqualTo("creator", this.adventure!!.creator)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { it ->
                        it.documents[0].reference
                                .set(this.adventure!!)
                                .addOnSuccessListener {
                                    Log.d(TAG, "Document updated successfully")
                                    Toast.makeText(context, "Aventura modificada com sucesso!", Toast.LENGTH_SHORT).show()
                                    exitFragment()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error updating document", e)
                                    mProgressBar!!.visibility = View.INVISIBLE
                                }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error querying document", e)
                        Toast.makeText(context, "Erro ao criar aventura", Toast.LENGTH_SHORT).show()
                    }
        } else {
            db.collection("adventures").document("${adventure!!.creator}_${adventure!!.title}")
                    .set(adventure!!)
                    .addOnSuccessListener {
                        Log.d(TAG, "Document created successfully")
                        Toast.makeText(context, "Aventura criada com sucesso!", Toast.LENGTH_SHORT).show()

                        val master = Personagem()
                        master.nome = LoginActivity.username
                        master.classe = "Mestre"
                        master.created = System.currentTimeMillis() / 1000
                        master.aventuraId = "${adventure?.creator}_${adventure?.title}"

                        db.collection("characters").add(master).addOnSuccessListener { documentReference ->
                            db.collection("characters").document(documentReference.id).update("id", documentReference.id)
                        }

                        exitFragment()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error creating document", e)
                        mProgressBar!!.visibility = View.INVISIBLE
                    }
        }
    }

    private fun exitFragment() {
        if (editMode!!)
            NavHostFragment.findNavController(this).popBackStack(R.id.adventureFragment, false)
        else
            NavHostFragment.findNavController(this).navigate(R.id.action_newAdventure_to_homeFragment)
    }

    private fun clearOthersRadioButtons(rb: Int) {
        if (rb != 1) rb1?.isChecked = false
        if (rb != 2) rb2?.isChecked = false
        if (rb != 3) rb3?.isChecked = false
        if (rb != 4) rb4?.isChecked = false
        if (rb != 5) rb5?.isChecked = false
    }

    companion object {
        @JvmStatic
        fun newInstance() = NewAdventure()
    }
}
