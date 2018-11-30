package com.example.hal_9000.igor.fragment

import android.app.DatePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Session
import com.example.hal_9000.igor.viewmodel.MainViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlinx.android.synthetic.main.fragment_new_session.*
import java.util.*

class NewSession : Fragment() {
    private val TAG = "NewSession"

    private lateinit var sessionOld: Session
    private var editMode = false

    private lateinit var etTitle: EditText
    private lateinit var etSummary: EditText
    private lateinit var btnDate: Button
    private lateinit var mProgressBar: ProgressBar

    private var date: Long = 0L

    private lateinit var model: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_new_session, container, false)

        etTitle = view.findViewById(R.id.et_title)
        etSummary = view.findViewById(R.id.et_summary)
        btnDate = view.findViewById(R.id.btn_date)
        val buttonFinish = view.findViewById<Button>(R.id.btn_finish)
        val imageClose = view.findViewById<ImageView>(R.id.iv_close)

        model = activity!!.run {
            ViewModelProviders.of(this).get(MainViewModel::class.java)
        }

        if (NewSessionArgs.fromBundle(arguments).session != null) {
            sessionOld = NewSessionArgs.fromBundle(arguments).session!!

            editMode = true
            val tvHeaderTitle = view.findViewById<TextView>(R.id.tv_header_title)
            tvHeaderTitle.text = "Editar Aventura"
            completeFields()
        }

        mProgressBar = view.findViewById(R.id.progressBar)

        btnDate.setOnClickListener {

            val calendar = Calendar.getInstance()

            if (editMode)
                calendar.timeInMillis = sessionOld.date

            val mDay = calendar.get(Calendar.DAY_OF_MONTH)
            val mMonth = calendar.get(Calendar.MONTH)
            val mYear = calendar.get(Calendar.YEAR)

            val datePickerDialog = DatePickerDialog(view.context,
                    DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                        val dateText = dayOfMonth.toString() + "/" + (monthOfYear + 1).toString()
                        btnDate.text = dateText
                        date = GregorianCalendar(year, monthOfYear, dayOfMonth).timeInMillis
                    }, mYear, mMonth, mDay)
            datePickerDialog.show()
        }

        imageClose.setOnClickListener { exitFragment() }
        buttonFinish.setOnClickListener { saveSession() }

        return view
    }

    private fun completeFields() {
        if (sessionOld.title.isNotEmpty())
            etTitle.setText(sessionOld.title)

        if (sessionOld.summary.isNotEmpty())
            etSummary.setText(sessionOld.summary)

        if (sessionOld.date != 0L) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = sessionOld.date
            val dateText = calendar.get(Calendar.DAY_OF_MONTH).toString() + "/" + (calendar.get(Calendar.MONTH) + 1).toString()
            btnDate.text = dateText
            date = sessionOld.date
        }
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

        mProgressBar.visibility = View.VISIBLE

        val session = Session()
        session.title = et_title.text.toString()
        session.summary = et_summary.text.toString()
        session.date = date

        if (editMode) {
            session.created_at = sessionOld.created_at
        } else {
            session.created_at = System.currentTimeMillis()
        }

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val batch: WriteBatch = db.batch()

        batch.set(db
                .collection("adventures")
                .document(model.getAdventure()!!.id)
                .collection("sessions")
                .document(session.created_at.toString()), session)

        if (model.getAdventure()!!.next_session == 0L || session.date < model.getAdventure()!!.next_session)
            batch.update(db
                    .collection("adventures")
                    .document(model.getAdventure()!!.id), "next_session", session.date)

        batch.commit()
                .addOnSuccessListener {
                    Log.d(TAG, "Document ${session.created_at} created_at successfully")
                    if (editMode)
                        Toast.makeText(context, "Sessão atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(context, "Sessão criada com sucesso!", Toast.LENGTH_SHORT).show()
                    exitFragment()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error creating document", e)
                    mProgressBar.visibility = View.INVISIBLE
                }
    }

    private fun exitFragment() {

        val action = NewSessionDirections.ActionNewSessionToAdventureFragment(model.getAdventure()!!)
        action.setAventura(model.getAdventure()!!)

        val navBuilder = NavOptions.Builder()
        val navOptions = navBuilder.setPopUpTo(R.id.adventureFragment, true).build()

        NavHostFragment.findNavController(this).navigate(action, navOptions)
    }
}
