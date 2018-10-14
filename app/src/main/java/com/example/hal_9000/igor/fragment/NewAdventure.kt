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
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Aventura
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_new_adventure.*


class NewAdventure : Fragment() {

    private val TAG = "NewAdventure"

    private var rb1: RadioButton? = null
    private var rb2: RadioButton? = null
    private var rb3: RadioButton? = null
    private var rb4: RadioButton? = null
    private var rb5: RadioButton? = null

    private var mProgressBar: ProgressBar? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_new_adventure, container, false)

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

        val adventure = Aventura()
        adventure.title = et_title.text.toString()
        adventure.next_session = ""
        adventure.theme = theme
        adventure.deleted = false
        adventure.creator = mAuth.currentUser!!.uid

        db.collection("adventures")
                .add(adventure)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "Document ${documentReference.id} created successfully")
                    Toast.makeText(context, "Aventura criada com sucesso!", Toast.LENGTH_SHORT).show()
                    exitFragment()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error creating document", e)
                    mProgressBar!!.visibility = View.INVISIBLE
                }
    }

    private fun exitFragment() {
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
