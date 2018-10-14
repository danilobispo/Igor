package com.example.hal_9000.igor.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Aventura
import com.example.hal_9000.igor.model.Session
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_new_session.*
import java.util.*
import kotlin.collections.HashMap


class NewSession : Fragment() {

    private val TAG = "NewSession"
    private var aventura: Aventura? = null

    private var editMode = false
    private var editPosition: Int? = null

    private var mProgressBar: ProgressBar? = null

    private var etTitle: EditText? = null
    private var etSummary: EditText? = null
    private var btnDate: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_new_session, container, false)

        etTitle = view.findViewById(R.id.et_title)
        etSummary = view.findViewById(R.id.et_summary)
        btnDate = view.findViewById(R.id.btn_date)

        aventura = NewSessionArgs.fromBundle(arguments).aventura
        editPosition = NewSessionArgs.fromBundle(arguments).session

        if (editPosition!! > -1) {
            editMode = true
            val tvHeaderTitle = view.findViewById<TextView>(R.id.tv_header_title)
            tvHeaderTitle.text = "Editar Aventura"
            completeFields()
        }

        mProgressBar = view.findViewById(R.id.progressBar)

        btnDate!!.setOnClickListener {

            val c = Calendar.getInstance()

            val mDay: Int
            val mMonth: Int
            val mYear = c.get(Calendar.YEAR)

            if (editMode) {
                mDay = aventura!!.sessions[editPosition!!].date.split("/")[0].toInt()
                mMonth = aventura!!.sessions[editPosition!!].date.split("/")[1].toInt()
            } else {
                mDay = c.get(Calendar.DAY_OF_MONTH)
                mMonth = c.get(Calendar.MONTH)
            }

            val datePickerDialog = DatePickerDialog(view.context,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        val dateText = dayOfMonth.toString() + "/" + (monthOfYear + 1).toString()
                        btnDate!!.text = dateText
                    }, mYear, mMonth, mDay)
            datePickerDialog.show()
        }

        val buttonFinish = view.findViewById<Button>(R.id.btn_finish)
        buttonFinish.setOnClickListener {
            saveSession()
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
        if (aventura!!.sessions[editPosition!!].title.isNotEmpty())
            etTitle!!.setText(aventura!!.sessions[editPosition!!].title)

        if (aventura!!.sessions[editPosition!!].summary.isNotEmpty())
            etSummary!!.setText(aventura!!.sessions[editPosition!!].summary)

        if (aventura!!.sessions[editPosition!!].date.isNotEmpty())
            btnDate!!.text = aventura!!.sessions[editPosition!!].date
    }

    private fun saveSession() {
        if (et_title.text.isEmpty()) {
            Toast.makeText(context, "Escolha um título", Toast.LENGTH_SHORT).show()
            return
        }

        if (et_summary.text.isEmpty()) {
            Toast.makeText(context, "Escreva um resumo", Toast.LENGTH_SHORT).show()
            return
        }

        if (btn_date.text == "DATA") {
            Toast.makeText(context, "Escolha uma data", Toast.LENGTH_SHORT).show()
            return
        }

        mProgressBar!!.visibility = View.VISIBLE

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        val session = Session()
        session.title = et_title.text.toString()
        session.summary = et_summary.text.toString()
        session.date = btn_date.text.toString()

        val sessionHash = HashMap<String, String>()
        sessionHash["title"] = session.title
        sessionHash["summary"] = session.summary
        sessionHash["date"] = session.date

        if (editMode!!) {

            val oldSessionHash = HashMap<String, String>()
            oldSessionHash["title"] = aventura!!.sessions[editPosition!!].title
            oldSessionHash["summary"] = aventura!!.sessions[editPosition!!].summary
            oldSessionHash["date"] = aventura!!.sessions[editPosition!!].date

            // TODO: Join calls
            db.collection("adventures")
                    .whereEqualTo("title", aventura!!.title)
                    .whereEqualTo("creator", aventura!!.creator)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { it ->
                        it.documents[0].reference
                                .update("sessions", FieldValue.arrayUnion(sessionHash))
                                .addOnSuccessListener { _ ->
                                    Log.d(TAG, "Document updated successfully")
                                    Toast.makeText(context, "Sessão criada com sucesso!", Toast.LENGTH_SHORT).show()
                                    it.documents[0].reference.update("sessions", FieldValue.arrayRemove(oldSessionHash))
                                    aventura!!.sessions.removeAt(editPosition!!)
                                    aventura!!.sessions.add(session)
                                    exitFragment()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error creating document", e)
                                    mProgressBar!!.visibility = View.INVISIBLE
                                }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error querying document", e)
                        Toast.makeText(context, "Erro ao criar sessão", Toast.LENGTH_SHORT).show()
                    }
        } else {
            db.collection("adventures")
                    .whereEqualTo("title", aventura!!.title)
                    .whereEqualTo("creator", aventura!!.creator)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { it ->
                        it.documents[0].reference
                                .update("sessions", FieldValue.arrayUnion(sessionHash))
                                .addOnSuccessListener {
                                    Log.d(TAG, "Document updated successfully")
                                    Toast.makeText(context, "Sessão criada com sucesso!", Toast.LENGTH_SHORT).show()
                                    aventura!!.sessions.add(session)
                                    exitFragment()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error creating document", e)
                                    mProgressBar!!.visibility = View.INVISIBLE
                                }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error querying document", e)
                        Toast.makeText(context, "Erro ao criar sessão", Toast.LENGTH_SHORT).show()
                    }
        }
    }

    private fun exitFragment() {

        val action = NewSessionDirections.ActionNewSessionToAdventureFragment(aventura!!)
        action.setAventura(aventura!!)

        val navBuilder = NavOptions.Builder()
        val navOptions = navBuilder.setPopUpTo(R.id.adventureFragment, true).build()

        NavHostFragment.findNavController(this).navigate(action, navOptions)
    }

    companion object {
        @JvmStatic
        fun newInstance() = NewSession()
    }
}
