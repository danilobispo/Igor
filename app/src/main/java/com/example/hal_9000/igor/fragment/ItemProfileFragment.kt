package com.example.hal_9000.igor.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.example.hal_9000.igor.viewmodel.MainViewModel
import com.example.hal_9000.igor.NavGraphDirections
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.StatsListAdapter
import com.example.hal_9000.igor.model.Atributo
import com.example.hal_9000.igor.model.Evento
import com.example.hal_9000.igor.model.Item
import com.example.hal_9000.igor.model.Personagem
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore


class ItemProfileFragment : Fragment() {

    private val TAG = "ItemProfileFragment"

    private lateinit var ivImagem: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvType: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvSeeMore: TextView
    private lateinit var tvOwner: TextView
    private lateinit var tvState: TextView
    private lateinit var rvStats: RecyclerView
    private lateinit var buttons: LinearLayout
    private lateinit var btnAction: Button
    private lateinit var btnGive: Button
    private lateinit var btnDiscard: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var item: Item
    private lateinit var owner: Personagem
    private var readOnly = false

    private lateinit var itemRef: DocumentReference
    private lateinit var eventsRef: CollectionReference

    private lateinit var adapter: StatsListAdapter

    private lateinit var db: FirebaseFirestore

    private lateinit var model: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_item_profile, container, false)

        ivImagem = view.findViewById(R.id.iv_imagem)
        tvName = view.findViewById(R.id.tv_name)
        tvType = view.findViewById(R.id.tv_type)
        tvDescription = view.findViewById(R.id.tv_description)
        tvSeeMore = view.findViewById(R.id.tv_see_more)
        tvOwner = view.findViewById(R.id.tv_owner)
        tvState = view.findViewById(R.id.tv_state)
        rvStats = view.findViewById(R.id.rv_stats)
        buttons = view.findViewById(R.id.buttons)
        btnAction = view.findViewById(R.id.btn_action)
        btnGive = view.findViewById(R.id.btn_give)
        btnDiscard = view.findViewById(R.id.btn_discard)
        progressBar = view.findViewById(R.id.progressBar)

        setHasOptionsMenu(true)

        model = activity!!.run {
            ViewModelProviders.of(this).get(MainViewModel::class.java)
        }

        db = FirebaseFirestore.getInstance()

        item = ItemProfileFragmentArgs.fromBundle(arguments).item
        owner = ItemProfileFragmentArgs.fromBundle(arguments).owner
        readOnly = ItemProfileFragmentArgs.fromBundle(arguments).readOnly

        tvName.text = item.name
        tvType.text = item.type
        tvOwner.text = item.owner

        if (item.type == "Equipamento")
            if (item.equipped)
                tvState.text = "Equipado"
            else
                tvState.text = "Não Equipado"
        else
            tvState.text = ""

        if (item.description.isEmpty())
            tvDescription.text = "Item sem descrição"
        else
            tvDescription.text = item.description

        tvDescription.post {
            if (tvDescription.lineCount > 6) {

                tvSeeMore.visibility = View.VISIBLE

                tvSeeMore.setOnClickListener {
                    if (tvDescription.maxLines == 6) {
                        tvDescription.maxLines = 99
                        tvSeeMore.text = "ver menos ▲"
                    } else {
                        tvDescription.maxLines = 6
                        tvSeeMore.text = "ver mais ▼"
                    }
                }
            }
        }

        if (item.image_url.isNotEmpty())
            Glide.with(view)
                    .load(item.image_url)
                    .into(ivImagem)
        else
            Glide.with(view)
                    .load(R.drawable.ic_items)
                    .into(ivImagem)

        val arrayStats = ArrayList<Atributo>()
        for (stat in item.stats)
            arrayStats.add(stat)

        adapter = StatsListAdapter(arrayStats)
        rvStats.layoutManager = LinearLayoutManager(context)
        rvStats.setHasFixedSize(true)
        rvStats.adapter = adapter

        when {
            item.equipped -> btnAction.text = "Desequipar"
            item.type == "Equipamento" -> btnAction.text = "Equipar"
            item.type == "Consumível" -> btnAction.text = "Usar"
            else -> btnAction.text = "Ação"
        }

        if (readOnly) return view

        if (model.getUsername()!! == model.getAdventure()!!.creator
                || model.getUsername()!! == item.owner) {
            buttons.visibility = View.VISIBLE

            btnAction.setOnClickListener { use() }
            btnGive.setOnClickListener { transferItem() }
            btnDiscard.setOnClickListener { discard() }
        }

        itemRef = db.collection("adventures")
                .document(model.getAdventure()!!.id)
                .collection("items")
                .document(item.id)

        eventsRef = db.collection("adventures")
                .document(model.getAdventure()!!.id)
                .collection("sessions")
                .document(model.getSessionId()!!)
                .collection("events")

        return view
    }

    private fun discard() {
        progressBar.visibility = View.VISIBLE

        item.owner = model.getAdventure()!!.creator

        if (item.equipped)
            use(false)

        if (item.owner == model.getAdventure()!!.creator) {
            itemRef.delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "Document ${item.id} deleted successfully")
                        Toast.makeText(context, "Item deletado com sucesso!", Toast.LENGTH_SHORT).show()
                        NavHostFragment.findNavController(this).popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error deleting document", e)
                        Toast.makeText(context, "Erro ao deletar item", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.INVISIBLE
                    }
            return
        }

        val event = Evento(type = "item", date = System.currentTimeMillis())
        event.event = "${owner.nome} descartou ${item.name}"

        val batch = db.batch()
        batch.set(itemRef, item)
        batch.set(eventsRef.document(event.date.toString()), event)

        batch.commit()
                .addOnSuccessListener {
                    Log.d(TAG, "Document ${item.id} created successfully")
                    Toast.makeText(context, "Item descartado com sucesso!", Toast.LENGTH_SHORT).show()
                    NavHostFragment.findNavController(this).popBackStack()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error creating document", e)
                    Toast.makeText(context, "Erro ao criar item", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE
                }
    }


    private fun transferItem() {
        fun transfer() {
            progressBar.visibility = View.VISIBLE

            val event = Evento(type = "item", date = System.currentTimeMillis())

            if (owner.nome == model.getAdventure()!!.creator)
                event.event = "${item.owner} recebeu ${item.name}"
            else
                event.event = "${owner.nome} transferiu ${item.name} para ${item.owner}"

            val batch = db.batch()
            batch.set(itemRef, item)
            batch.set(eventsRef.document(event.date.toString()), event)

            batch.commit()
                    .addOnSuccessListener {
                        Log.d(TAG, "Document ${item.id} created successfully")
                        Toast.makeText(context, "Item transferido com sucesso!", Toast.LENGTH_SHORT).show()
                        NavHostFragment.findNavController(this).popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error creating document", e)
                        Toast.makeText(context, "Erro ao transferir item", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.INVISIBLE
                    }
        }

        fun selectUser() {
            progressBar.visibility = View.VISIBLE

            val query = if (model.getIsMaster()!!)
                db.collection("characters")
                        .whereEqualTo("aventura_id", model.getAdventure()!!.id)
            else
                db.collection("characters")
                        .whereEqualTo("aventura_id", model.getAdventure()!!.id)
                        .whereEqualTo("hidden", false)

            query.get()
                    .addOnSuccessListener {
                        Log.d(TAG, "Documents queried successfully")
                        progressBar.visibility = View.GONE

                        val charTextsArray: ArrayList<String> = arrayListOf()
                        val charNamesArray: ArrayList<String> = arrayListOf()

                        for (document in it.documents) {
                            val char = document.toObject(Personagem::class.java)!!
                            charTextsArray.add("${char.nome} (${char.classe})")
                            charNamesArray.add(char.nome)
                        }

                        val builder = AlertDialog.Builder(context!!)
                        builder.setTitle("Para quem deseja transferir?")
                        builder.setItems(charTextsArray.toTypedArray()) { _, which ->
                            item.owner = charNamesArray[which]
                            transfer()
                        }
                        builder.show()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error querying documents", e)
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "Erro ao buscar jogadores", Toast.LENGTH_SHORT).show()
                    }
        }

        selectUser()
    }

    private fun use(exitFragment: Boolean = true) {
        val health = resources.getStringArray(R.array.heal_names)
        val healthMax = resources.getStringArray(R.array.maxhealth_names)

        fun equip() {
            for (stat in item.stats) {
                if (stat.valor.toIntOrNull() != null) {
                    if (stat.valor.toInt() > 0) {
                        when {
                            health.contains(stat.nome.toLowerCase()) -> owner.heal(stat.valor.toInt())
                            healthMax.contains(stat.nome.toLowerCase()) -> owner.maxHealthUp(stat.valor.toInt())
                            else -> owner.statUp(stat.nome, stat.valor.toInt())
                        }
                    } else {
                        when {
                            health.contains(stat.nome.toLowerCase()) -> owner.hit(stat.valor.toInt())
                            healthMax.contains(stat.nome.toLowerCase()) -> owner.maxHealthDown(stat.valor.toInt())
                            else -> owner.statDown(stat.nome, stat.valor.toInt())
                        }
                    }
                } else {
                    owner.statChange(stat.nome, stat.valor)
                }
            }
        }

        fun unequip() {
            for (stat in item.stats) {
                if (stat.valor.toIntOrNull() != null) {
                    if (stat.valor.toInt() > 0) {
                        when {
                            health.contains(stat.nome.toLowerCase()) -> owner.hit(stat.valor.toInt())
                            healthMax.contains(stat.nome.toLowerCase()) -> owner.maxHealthDown(stat.valor.toInt())
                            else -> owner.statDown(stat.nome, stat.valor.toInt())
                        }
                    } else {
                        when {
                            health.contains(stat.nome.toLowerCase()) -> owner.heal(stat.valor.toInt())
                            healthMax.contains(stat.nome.toLowerCase()) -> owner.maxHealthUp(stat.valor.toInt())
                            else -> owner.statUp(stat.nome, stat.valor.toInt())
                        }
                    }
                } else {
                    owner.statRemove(stat.nome)
                }
            }
        }

        progressBar.visibility = View.VISIBLE

        val event = Evento(type = "item", date = System.currentTimeMillis())

        if (item.type == "Equipamento") {
            if (item.equipped) {
                unequip()
                event.event = "${owner.nome} desequipou ${item.name}"
            } else {
                equip()
                event.event = "${owner.nome} equipou ${item.name}"
            }
            item.equipped = !item.equipped
        } else if (item.type == "Consumível") {
            equip()
            item.owner = model.getAdventure()!!.creator
            event.event = "${owner.nome} consumiu ${item.name}"
        }

        val batch = db.batch()
        batch.set(itemRef, item)
        batch.set(db.collection("characters").document(owner.id), owner)
        batch.set(eventsRef.document(event.date.toString()), event)

        batch.commit()
                .addOnSuccessListener {
                    Log.d(TAG, "Documents updated successfully.")

                    if (item.type == "Equipamento") {
                        if (item.equipped)
                            Toast.makeText(context, "Item equipado com sucesso!", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(context, "Item desequipado com sucesso!", Toast.LENGTH_SHORT).show()
                    } else if (item.type == "Consumível")
                        Toast.makeText(context, "Item consumido com sucesso!", Toast.LENGTH_SHORT).show()
                    if (exitFragment)
                        NavHostFragment.findNavController(this).popBackStack()
                    progressBar.visibility = View.INVISIBLE
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating documents.", e)
                    Toast.makeText(context, "Erro ao criar aventura", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE
                }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (!model.getIsMaster()!!)
            return super.onOptionsItemSelected(menuItem)

        when (menuItem.itemId) {
            R.id.menu_editar -> {
                val action = NavGraphDirections.ActionGlobalNewItemFragment(item.owner)
                action.setItem(item)
                action.setNewOwner(item.owner)
                NavHostFragment.findNavController(this).navigate(action)
            }
            R.id.menu_ordenar -> {
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }
}
