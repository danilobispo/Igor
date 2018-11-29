package com.example.hal_9000.igor.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.hal_9000.igor.NavGraphDirections
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.CharactersCombatListAdapter
import com.example.hal_9000.igor.model.*
import com.example.hal_9000.igor.viewmodel.MainViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlin.random.Random

class CombatFragment : Fragment() {
    private val TAG = "CombatFragment"

    private lateinit var fabAction: FloatingActionButton

    private lateinit var adapterPlayers: CharactersCombatListAdapter
    private lateinit var mPlayersList: RecyclerView

    private lateinit var adapterEnemies: CharactersCombatListAdapter
    private lateinit var mEnemiesList: RecyclerView

    private var characterSelected: Personagem? = null

    private var db: FirebaseFirestore? = null

    private lateinit var model: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_combat, container, false)

        fabAction = view.findViewById(R.id.fab_action)

        model = activity!!.run {
            ViewModelProviders.of(this).get(MainViewModel::class.java)
        }

        mPlayersList = view.findViewById(R.id.rv_players)
        mPlayersList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mPlayersList.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()

        val aventura = model.getAdventure()!!

        val queryPlayers = db!!.collection("characters")
                .whereEqualTo("aventura_id", aventura.id)
                .whereEqualTo("isnpc", false)
                .whereEqualTo("ismaster", false)
                .whereEqualTo("hidden", false)

        val optionsPlayers = FirestoreRecyclerOptions.Builder<Personagem>()
                .setQuery(queryPlayers, Personagem::class.java)
                .build()

        adapterPlayers = CharactersCombatListAdapter(optionsPlayers, { personagem: Personagem -> personagemItemClicked(personagem) }, { mode: Boolean -> selectionModeChanged("adapterPlayers", mode) })
        mPlayersList.adapter = adapterPlayers

        mEnemiesList = view.findViewById(R.id.rv_npcs)
        mEnemiesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mEnemiesList.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()

        val queryEnemies = db!!.collection("characters")
                .whereEqualTo("aventura_id", aventura.id)
                .whereEqualTo("isnpc", true)
                .whereEqualTo("ismaster", false)
                .whereEqualTo("hidden", false)

        val optionsEnemies = FirestoreRecyclerOptions.Builder<Personagem>()
                .setQuery(queryEnemies, Personagem::class.java)
                .build()

        adapterEnemies = CharactersCombatListAdapter(optionsEnemies, { personagem: Personagem -> enemyItemClicked(personagem) }, { mode: Boolean -> selectionModeChanged("adapterEnemies", mode) })
        mEnemiesList.adapter = adapterEnemies

        if (!model.getIsMaster()!!)
            return view

        fabAction.setOnClickListener { showActionsDialog() }

        return view
    }

    private fun showActionsDialog() {
        //TODO: Criar layout para o DialogAlert
        val actions = if (adapterPlayers.selectionModeOwn || adapterEnemies.selectionModeOwn)
            arrayOf("Rolar dados", "Dar dano", "Curar", "Aumentar Atributo", "Diminuir Atributo", "Criar ou Alterar Atributo")
        else
            arrayOf("Perfil", "Rolar dados", "Dar dano", "Curar", "Aumentar Atributo", "Diminuir Atributo", "Criar ou Alterar Atributo")

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Escolha uma ação")
        builder.setItems(actions) { _, which ->
            when (actions[which]) {
                "Perfil" -> {
                    showProfile()
                }
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
                "Criar ou Alterar Atributo" -> {
                    showStatChooserDialog("change")
                }
            }
        }
        builder.show()
    }

    private fun showProfile() {
        val action = NavGraphDirections.actionGlobalCharacterProfileFragment(characterSelected!!)
        Navigation.findNavController(activity!!, R.id.nav_host).navigate(action)
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

    private fun showStatChooserDialog(action: String) {

        fun showStatUpDialog(stat: String, statIdx: Int) {
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_NUMBER

            AlertDialog.Builder(view!!.context)
                    .setTitle("Aumentar atributo")
                    .setMessage("Digite o quanto você deseja aumentar o atributo $stat")
                    .setView(input)
                    .setPositiveButton("OK") { _, _ ->
                        if (input.text != null)
                            alterarAtributoPersonagem("up", stat, statIdx, input.text.toString())
                    }
                    .show()
        }

        fun showStatDownDialog(stat: String, statIdx: Int) {
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_NUMBER

            AlertDialog.Builder(view!!.context)
                    .setTitle("Diminuir atributo")
                    .setMessage("Digite o quanto você deseja diminuir o atributo $stat")
                    .setView(input)
                    .setPositiveButton("OK") { _, _ ->
                        if (input.text != null)
                            alterarAtributoPersonagem("down", stat, statIdx, input.text.toString())
                    }
                    .show()
        }

        fun showStatChangeDialog(stat: String, statIdx: Int) {
            fun showStatValueSetDialog(stat: String) {
                val input = EditText(context)
                input.inputType = InputType.TYPE_CLASS_NUMBER

                AlertDialog.Builder(view!!.context)
                        .setTitle("Criar ou Alterar Atributo")
                        .setMessage("Digite o novo valor do atributo $stat")
                        .setView(input)
                        .setPositiveButton("OK") { _, _ ->
                            if (input.text != null)
                                alterarAtributoPersonagem("change", stat, statIdx, input.text.toString())
                        }
                        .show()
            }

            fun showStatNameSetDialog() {
                val input = EditText(context)
                input.inputType = InputType.TYPE_CLASS_TEXT

                AlertDialog.Builder(view!!.context)
                        .setTitle("Curar atributo")
                        .setMessage("Digite nome do novo atributo")
                        .setView(input)
                        .setPositiveButton("OK") { _, _ ->
                            if (input.text != null) {
                                showStatValueSetDialog(input.text.toString())
                            }
                        }
                        .show()
            }

            if (stat == "Novo atributo")
                showStatNameSetDialog()
            else
                showStatValueSetDialog(stat)
        }

        val stats: ArrayList<Atributo> = when {
            characterSelected != null -> characterSelected!!.atributos
            adapterPlayers.selectedIds.size > 0 -> adapterPlayers.getItem(adapterPlayers.selectedIds[0]).atributos
            adapterEnemies.selectedIds.size > 0 -> adapterEnemies.getItem(adapterEnemies.selectedIds[0]).atributos
            else -> arrayListOf()
        }

        if (stats.size == 0) {
            Toast.makeText(context, "Personagem sem atributos", Toast.LENGTH_SHORT).show()
            return
        }

        val statsNames: ArrayList<String?> = arrayListOf()
        for (stat in stats)
            statsNames.add(stat.nome)

        if (action == "change")
            statsNames.add("Novo atributo")

        val array = arrayOfNulls<String>(statsNames.size)
        statsNames.toArray(array)

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Escolha um atributo")
        builder.setItems(array) { _, which ->
            when (action) {
                "up" -> showStatUpDialog(statsNames[which]!!, which)
                "down" -> showStatDownDialog(statsNames[which]!!, which)
                "change" -> showStatChangeDialog(statsNames[which]!!, which)
            }
        }
        builder.show()
    }

    private fun alterarVidaPersonagem(action: String, value: Int) {
        fun alterarVida(batch: WriteBatch, char: Personagem, action: String, value: Int) {
            val oldValue = char.health

            when (action) {
                "damage" -> char.hit(value)
                "heal" -> char.heal(value)
                else -> return
            }
            batch.set(db!!.collection("characters").document(char.id), char)
            if (action == "damage")
                logEvent(batch, "stat", "${char.nome} sofreu $value de dano ($oldValue➡${char.health})")
            else
                logEvent(batch, "stat", "${char.nome} recebeu $value de cura ($oldValue➡${char.health})")
        }

        val batch = db!!.batch()

        if (characterSelected != null) {
            alterarVida(batch, characterSelected!!, action, value)
            characterSelected = null
        }

        for (idx in adapterPlayers.selectedIds) {
            val char = adapterPlayers.getItem(idx)
            alterarVida(batch, char, action, value)
        }

        for (idx in adapterEnemies.selectedIds) {
            val char = adapterEnemies.getItem(idx)
            alterarVida(batch, char, action, value)
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

    private fun alterarAtributoPersonagem(action: String, stat: String, statIdx: Int, value: String) {
        fun alterarAtributo(batch: WriteBatch, char: Personagem, stat: String, statIdx: Int, value: String) {
            var oldValue = ""
            if (statIdx < char.atributos.size)
                oldValue = char.atributos[statIdx].valor

            when (action) {
                "up" -> char.statUp(stat, value.toInt())
                "down" -> char.statDown(stat, value.toInt())
                "change" -> char.statChange(stat, value)
                else -> return
            }
            batch.set(db!!.collection("characters").document(char.id), char)
            when (action) {
                "up" -> logEvent(batch, "stat", "${char.nome} aumentou seu $stat em $value ($oldValue➡${char.atributos[statIdx].valor})")
                "down" -> logEvent(batch, "stat", "${char.nome} diminuiu seu $stat em $value ($oldValue➡${char.atributos[statIdx].valor})")
                "change" -> logEvent(batch, "stat", "${char.nome} alterou seu atributo $stat para ${char.atributos[statIdx].valor}")
            }
        }

        val batch = db!!.batch()

        if (characterSelected != null) {
            alterarAtributo(batch, characterSelected!!, stat, statIdx, value)
            characterSelected = null
        }

        for (idx in adapterPlayers.selectedIds) {
            val char = adapterPlayers.getItem(idx)
            alterarAtributo(batch, char, stat, statIdx, value)
        }

        for (idx in adapterEnemies.selectedIds) {
            val char = adapterEnemies.getItem(idx)
            alterarAtributo(batch, char, stat, statIdx, value)
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
        fun ask(batch: WriteBatch, playerDices: PlayerDices, charName: String) {
            batch.set(db!!.collection("adventures")
                    .document(model.getAdventure()!!.id)
                    .collection("sessions")
                    .document(model.getSessionId()!!)
                    .collection("dices")
                    .document(charName)
                    , playerDices)
        }

        val playerDices = PlayerDices(rolled = false)
        for (i in 1..quantity) playerDices.dices.add(Dice(diceName))

        val batch = db!!.batch()

        if (characterSelected != null) {
            playerDices.character = characterSelected!!.nome
            if (characterSelected!!.isnpc)
                ask(batch, playerDices, model.getUsername()!!)
            else
                ask(batch, playerDices, characterSelected!!.nome)
            characterSelected = null
        }

        for (idx in adapterPlayers.selectedIds) {
            val char = adapterPlayers.getItem(idx)
            playerDices.character = char.nome
            ask(batch, playerDices, char.nome)
        }

        for (idx in adapterEnemies.selectedIds) {
            val char = adapterEnemies.getItem(idx)
            playerDices.character = char.nome
            ask(batch, playerDices, model.getUsername()!!)
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

    private fun logEvent(batch: WriteBatch, type: String, message: String) {
        val event = Evento()
        event.date = System.currentTimeMillis()
        event.type = type
        event.event = message

        val eventLogReference = db!!
                .collection("adventures")
                .document(model.getAdventure()!!.id)
                .collection("sessions")
                .document(model.getSessionId()!!)
                .collection("events")
                .document(event.date.toString())

        batch.set(eventLogReference, event)
    }

    private fun enemyItemClicked(personagem: Personagem) {
        Log.d(TAG, "Clicked enemy ${personagem.nome}")
        if (model.getIsMaster()!!) {
            characterSelected = personagem
            showActionsDialog()
        } else {
            val action = NavGraphDirections.actionGlobalCharacterProfileFragment(personagem)
            Navigation.findNavController(activity!!, R.id.nav_host).navigate(action)
        }
    }

    private fun personagemItemClicked(personagem: Personagem) {
        Log.d(TAG, "Clicked player ${personagem.nome}")
        if (model.getIsMaster()!!) {
            characterSelected = personagem
            showActionsDialog()
        } else {
            val action = NavGraphDirections.actionGlobalCharacterProfileFragment(personagem)
            Navigation.findNavController(activity!!, R.id.nav_host).navigate(action)
        }
    }

    private fun selectionModeChanged(adapter: String, mode: Boolean) {
        if (adapter == "adapterPlayers")
            adapterEnemies.selectionModeOther = mode
        else
            adapterPlayers.selectionModeOther = mode

        if (!model.getIsMaster()!!) return

        if (adapterPlayers.selectionModeOwn || adapterEnemies.selectionModeOwn)
            fabAction.show()
        else
            fabAction.hide()
    }

    override fun onStart() {
        super.onStart()
        adapterPlayers.startListening()
        adapterEnemies.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterPlayers.stopListening()
        adapterEnemies.stopListening()
    }
}
