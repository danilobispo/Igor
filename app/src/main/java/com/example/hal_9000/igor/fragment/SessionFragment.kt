package com.example.hal_9000.igor.fragment


import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.example.hal_9000.igor.LoginActivity
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.PlayerDices
import com.example.hal_9000.igor.model.Session
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore


class SessionFragment : Fragment() {

    private val TAG = "SessionFragment"
    private lateinit var db: FirebaseFirestore
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_session, container, false)

        session = SessionFragmentArgs.fromBundle(arguments).session
        sessionId = session.created_at.toString()

        val fragmentContainer = view.findViewById<View>(R.id.nav_host_session)
        navController = Navigation.findNavController(fragmentContainer)

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val adventureTitle = view.findViewById<TextView>(R.id.tv_adventure_title)
        val sessionTitle = view.findViewById<TextView>(R.id.tv_session_title)

        bottomNavigationView.setupWithNavController(navController)

        adventureTitle.text = AdventureFragment.aventura.title
        sessionTitle.text = session.title

        val ivTheme = view.findViewById<ImageView>(R.id.iv_theme)
        val layout = view.findViewById<ConstraintLayout>(R.id.contraint_layout)
        when (AdventureFragment.aventura.theme) {
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

        db = FirebaseFirestore.getInstance()

        val docRef = db.collection("adventures")
                .document(AdventureFragment.aventura.id)
                .collection("sessions")
                .document(sessionId)
                .collection("dices")
                .document(LoginActivity.username)

        docRef.addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@EventListener
            }

            if (snapshot != null && snapshot.exists()) {
                val playerDices = snapshot.toObject(PlayerDices::class.java)!!
                if (!playerDices.rolled && navController.currentDestination?.label != "DicesFragment") {
                    Log.d(TAG, "Dices roll requested: " + snapshot.data!!)
                    rollDices()
                }
            } else {
                Log.d(TAG, "Dices roll requested: null")
            }
        })

        return view
    }

    private fun rollDices() {
        Log.d(TAG, "User must roll dices")

        if (context == null) return

        AlertDialog.Builder(context!!)
                .setTitle("Rolar dados")
                .setMessage("Você precisa rolar dados. Deseja ir para a tela de rolagem de dados?")
                .setPositiveButton("Ir") { _, _ ->
                    navController.navigate(R.id.dicesFragment)
                }
                .setNegativeButton("Cancelar") { _, _ ->
                }
                .show()
    }

    companion object {
        lateinit var session: Session
        lateinit var sessionId: String
    }
}
