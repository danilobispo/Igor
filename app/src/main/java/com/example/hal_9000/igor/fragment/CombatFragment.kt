package com.example.hal_9000.igor.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.hal_9000.igor.LoginActivity
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.CharactersCombatListAdapter
import com.example.hal_9000.igor.model.Atributo
import com.example.hal_9000.igor.model.Dice
import com.example.hal_9000.igor.model.Personagem
import com.example.hal_9000.igor.model.PlayerDices
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random


class CombatFragment : Fragment() {

    private val TAG = "CombatFragment"

    private var adapterPlayers: CharactersCombatListAdapter? = null
    private var mPlayersList: RecyclerView? = null

    private var adapterEnemies: CharactersCombatListAdapter? = null
    private var mEnemiesList: RecyclerView? = null

    private var db: FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_combat, container, false)

        mPlayersList = view.findViewById(R.id.rv_chars1)
        mPlayersList?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mPlayersList?.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()

        val aventura = AdventureFragment.aventura

        val queryPlayers = db!!.collection("characters")
                .whereEqualTo("aventuraId", "${aventura.creator}_${aventura.title}")
                .whereEqualTo("npc", false)
                .whereEqualTo("master", false)
                .orderBy("created")

        val optionsPlayers = FirestoreRecyclerOptions.Builder<Personagem>()
                .setQuery(queryPlayers, Personagem::class.java)
                .build()

        adapterPlayers = CharactersCombatListAdapter(optionsPlayers) { personagem: Personagem -> personagemItemClicked(personagem) }
        mPlayersList?.adapter = adapterPlayers

        mEnemiesList = view.findViewById(R.id.rv_chars2)
        mEnemiesList?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mEnemiesList?.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()

        val queryEnemies = db!!.collection("characters")
                .whereEqualTo("aventuraId", "${aventura.creator}_${aventura.title}")
                .whereEqualTo("npc", true)
                .whereEqualTo("master", false)
                .orderBy("created")

        val optionsEnemies = FirestoreRecyclerOptions.Builder<Personagem>()
                .setQuery(queryEnemies, Personagem::class.java)
                .build()

        adapterEnemies = CharactersCombatListAdapter(optionsEnemies) { personagem: Personagem -> enemyItemClicked(personagem) }
        mEnemiesList?.adapter = adapterEnemies

        val btnAction = view.findViewById<Button>(R.id.btn_action)
        btnAction.setOnClickListener {
            val actions = arrayOf("Rolar dados", "Dar dano", "Curar", "Aumentar Atributo", "Diminuir Atributo")

            //TODO: Criar layout para o DialogAlert

            val builder = AlertDialog.Builder(context!!)
            builder.setTitle("Escolha uma ação")
            builder.setItems(actions) { _, which ->
                when (actions[which]) {
                    "Rolar dados" -> {
                        showDiceChooser()
                    }
                    "Dar dano" -> {
                        showDamageDialog()
                    }
                    "Curar" -> {
                        showHealDialog()
                    }
                    "Aumentar Atributo" -> {
                        showStatChooserDialog("up")
                    }
                    "Diminuir Atributo" -> {
                        showStatChooserDialog("down")
                    }
                }
            }
            builder.show()
        }

        return view
    }

    private fun showDamageDialog() {
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(view!!.context)
                .setTitle("Dar dano")
                .setMessage("Digite a quantidade de dano")
                .setView(input)
                .setPositiveButton("OK") { _, _ ->
                    if (input.text != null)
                        alterarVidaPersonagem("damage", input.text.toString().toInt())
                }
                .show()
    }

    private fun showHealDialog() {
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(view!!.context)
                .setTitle("Curar")
                .setMessage("Digite a quantidade da cura")
                .setView(input)
                .setPositiveButton("OK") { _, _ ->
                    if (input.text != null)
                        alterarVidaPersonagem("heal", input.text.toString().toInt())
                }
                .show()
    }

    private fun showStatUpDialog(stat: String) {
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(view!!.context)
                .setTitle("Aumentar atributo")
                .setMessage("Digite o quanto você deseja aumentar o atributo $stat")
                .setView(input)
                .setPositiveButton("OK") { _, _ ->
                    if (input.text != null)
                        alterarAtributoPersonagem("up", stat, input.text.toString().toInt())
                }
                .show()
    }

    private fun showStatDownDialog(stat: String) {
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(view!!.context)
                .setTitle("Diminuir atributo")
                .setMessage("Digite o quanto você deseja diminuir o atributo $stat")
                .setView(input)
                .setPositiveButton("OK") { _, _ ->
                    if (input.text != null)
                        alterarAtributoPersonagem("down", stat, input.text.toString().toInt())
                }
                .show()
    }

    private fun showStatChooserDialog(action: String) {

        val stats: ArrayList<Atributo> = when {
            adapterPlayers!!.selectedIds.size > 0 ->
                adapterPlayers!!.getItem(adapterPlayers!!.selectedIds[0]).atributos
            adapterEnemies!!.selectedIds.size > 0 -> {
                adapterEnemies!!.getItem(adapterEnemies!!.selectedIds[0]).atributos
            }
            else -> return
        }

        val statsNames: Array<String?> = arrayOfNulls(stats.size)
        for (stat in stats)
            statsNames[statsNames.lastIndex] = stat.nome

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Escolha um atributo")
        builder.setItems(statsNames) { dialog, which ->
            when (action) {
                "up" -> showStatUpDialog(statsNames[which]!!)
                "down" -> showStatDownDialog(statsNames[which]!!)
            }
        }
        builder.show()
    }

    private fun alterarVidaPersonagem(acao: String, valor: Int) {
        val players = adapterPlayers!!.selectedIds
        val enemies = adapterEnemies!!.selectedIds

        val batch = db!!.batch()

        for (idx in players) {
            val char = adapterPlayers!!.getItem(idx)
            when (acao) {
                "damage" -> char.hit(valor)
                "heal" -> char.heal(valor)
                else -> return
            }
            batch.set(db!!.collection("characters").document(char.id), char)
        }

        for (idx in enemies) {
            val char = adapterEnemies!!.getItem(idx)
            when (acao) {
                "damage" -> char.hit(valor)
                "heal" -> char.heal(valor)
                else -> return
            }
            batch.set(db!!.collection("characters").document(char.id), char)
        }

        batch.commit()
                .addOnSuccessListener {
                    Log.d(TAG, "Batch alterarVidaPersonagem success")
                    Toast.makeText(context, "Sucesso", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.d(TAG, "Batch alterarVidaPersonagem error")
                    Toast.makeText(context, "Erro ao realizar operação", Toast.LENGTH_SHORT).show()
                }
    }

    private fun alterarAtributoPersonagem(action: String, stat: String, value: Int) {
        val players = adapterPlayers!!.selectedIds
        val enemies = adapterEnemies!!.selectedIds

        val batch = db!!.batch()

        for (idx in players) {
            val char = adapterPlayers!!.getItem(idx)
            when (action) {
                "up" -> char.statUp(stat, value)
                "down" -> char.statDown(stat, value)
                else -> return
            }
            batch.set(db!!.collection("characters").document(char.id), char)
        }

        for (idx in enemies) {
            val char = adapterEnemies!!.getItem(idx)
            when (action) {
                "up" -> char.statUp(stat, value)
                "down" -> char.statDown(stat, value)
                else -> return
            }
            batch.set(db!!.collection("characters").document(char.id), char)
        }

        batch.commit()
                .addOnSuccessListener {
                    Log.d(TAG, "Batch alterarAtributoPersonagem success")
                    Toast.makeText(context, "Sucesso", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.d(TAG, "Batch alterarAtributoPersonagem error")
                    Toast.makeText(context, "Erro ao realizar operação", Toast.LENGTH_SHORT).show()
                }
    }

    private fun showDiceChooser() {
        val dices = arrayOf("D4", "D6", "D8", "D10", "D00", "D12", "D20")

        AlertDialog.Builder(context!!)
                .setTitle("Escolha o tipo do dado")
                .setItems(dices) { _, which ->
                    showDiceQuantityChooser(dices[which])
                }
                .show()
    }

    private fun showDiceQuantityChooser(dice: String) {
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(view!!.context)
                .setTitle("Solicitar rolagem de dados")
                .setMessage("Digite a quantidade de dados")
                .setView(input)
                .setPositiveButton("OK") { _, _ ->
                    if (input.text != null)
                        askRollDices(dice, input.text.toString().toInt())
                }
                .show()
    }

    private fun askRollDices(diceName: String, quantity: Int = Random.nextInt(1, 5)) {
        val players = adapterPlayers!!.selectedIds
        val enemies = adapterEnemies!!.selectedIds

        val playerDices = PlayerDices()
        playerDices.rolled = false
        for (i in 1..quantity)
            playerDices.dices.add(Dice(diceName))

        val batch = db!!.batch()

        for (idx in players) {
            val char = adapterPlayers!!.getItem(idx)
            playerDices.character = char.nome

            batch.set(db!!.collection("adventures")
                    .document(AdventureFragment.aventuraId)
                    .collection("sessions")
                    .document(SessionFragment.sessionId)
                    .collection("dices")
                    .document(char.nome)
                    , playerDices)
        }

        for (idx in enemies) {
            val char = adapterEnemies!!.getItem(idx)
            playerDices.character = char.nome

            batch.set(db!!.collection("adventures")
                    .document(AdventureFragment.aventuraId)
                    .collection("sessions")
                    .document(SessionFragment.sessionId)
                    .collection("dices")
                    .document(LoginActivity.username)
                    , playerDices)
        }

        batch.commit()
                .addOnSuccessListener {
                    Log.d(TAG, "Batch dices add success")
                    Toast.makeText(context, "Sucesso", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.d(TAG, "Batch dices add error")
                    Toast.makeText(context, "Erro ao realizar operação", Toast.LENGTH_SHORT).show()
                }
    }

    private fun enemyItemClicked(personagem: Personagem) {
        Log.d(TAG, "Clicked enemy ${personagem.nome}")
    }

    private fun personagemItemClicked(personagem: Personagem) {
        Log.d(TAG, "Clicked player ${personagem.nome}")
    }

    override fun onStart() {
        super.onStart()
        adapterPlayers!!.startListening()
        adapterEnemies!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterPlayers!!.stopListening()
        adapterEnemies!!.stopListening()
    }

}
