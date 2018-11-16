package com.example.hal_9000.igor.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.hal_9000.igor.LoginActivity
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.DicesListAdapter
import com.example.hal_9000.igor.model.Evento
import com.example.hal_9000.igor.model.PlayerDices
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlin.random.Random


class DicesFragment : Fragment() {

    private val TAG = "DicesFragment"

    private lateinit var playerDices: PlayerDices
    private lateinit var adapter: DicesListAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var documentReference: DocumentReference
    private lateinit var batch: WriteBatch

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dices, container, false)

        mRecyclerView = view.findViewById(R.id.recyclerview)
        mRecyclerView.layoutManager = GridLayoutManager(context, 2)
        mRecyclerView.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()

        documentReference = db.collection("adventures")
                .document(AdventureFragment.aventuraId)
                .collection("sessions")
                .document(SessionFragment.sessionId)
                .collection("dices")
                .document(LoginActivity.username)

        documentReference.get()
                .addOnSuccessListener {
                    if (it == null || !it.exists()) return@addOnSuccessListener

                    playerDices = it.toObject(PlayerDices::class.java)!!
                    adapter = DicesListAdapter(playerDices.dices) { position: Int -> diceItemClicked(position) }

                    mRecyclerView.adapter = adapter
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error querying dices document", e)
                    Toast.makeText(context, "Erro ao receber dados", Toast.LENGTH_SHORT).show()
                }

        return view
    }

    private fun diceItemClicked(position: Int) {
        if (!playerDices.rolled && playerDices.dices[position].value == 0) {
            playerDices.dices[position].value =
                    when (playerDices.dices[position].dice) {
                        "D4" -> Random.nextInt(1, 4)
                        "D6" -> Random.nextInt(1, 6)
                        "D8" -> Random.nextInt(1, 8)
                        "D10" -> Random.nextInt(1, 10)
                        "D00" -> Random.nextInt(1, 10) * 10
                        "D12" -> Random.nextInt(1, 12)
                        "D20" -> Random.nextInt(1, 20)
                        else -> 0
                    }

            var dicesRolled = 0
            for (dice in playerDices.dices)
                if (dice.value != 0) dicesRolled++
            if (dicesRolled == playerDices.dices.size)
                playerDices.rolled = true

            batch = db.batch()

            batch.set(documentReference, playerDices)
            logEvent(position)

            batch.commit()
                    .addOnSuccessListener {
                        Log.d(TAG, "Batch dices update success")
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "Batch dices update error")
                        Toast.makeText(context, "Erro ao salvar dados", Toast.LENGTH_SHORT).show()
                    }
            adapter.notifyDataSetChanged()
        }
    }

    private fun logEvent(position: Int) {
        val event = Evento()
        event.date = System.currentTimeMillis()
        event.type = "dice"

        event.event = "${playerDices.character} tirou ${playerDices.dices[position].value} no dado ${playerDices.dices[position].dice}"

        val eventLogReference = db
                .collection("adventures")
                .document(AdventureFragment.aventuraId)
                .collection("sessions")
                .document(SessionFragment.sessionId)
                .collection("events")
                .document(event.date.toString())

        batch.set(eventLogReference, event)
    }

    companion object {
        @JvmStatic
        fun newInstance() = DicesFragment()
    }
}
