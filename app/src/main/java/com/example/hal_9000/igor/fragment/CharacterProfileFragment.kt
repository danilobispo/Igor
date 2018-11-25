package com.example.hal_9000.igor.fragment

import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.example.hal_9000.igor.NavGraphDirections
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.ItemsListAdapter
import com.example.hal_9000.igor.adapters.StatsListAdapter
import com.example.hal_9000.igor.model.Atributo
import com.example.hal_9000.igor.model.Item
import com.example.hal_9000.igor.model.Personagem
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore


class CharacterProfileFragment : Fragment() {

    private val TAG = "CharacterProfileFragt"

    private lateinit var ivImagem: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvClass: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvSeeMore: TextView
    private lateinit var tvHpText: TextView
    private lateinit var progressBarHealth: ProgressBar
    private lateinit var rvStats: RecyclerView
    private lateinit var rvInventory: RecyclerView
    private lateinit var btnCreateItem: Button

    private lateinit var db: FirebaseFirestore

    private lateinit var character: Personagem
    private var readOnly = false

    private lateinit var adapterStats: StatsListAdapter
    private lateinit var adapterItems: ItemsListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_character_profile, container, false)

        ivImagem = view.findViewById(R.id.iv_imagem)
        tvName = view.findViewById(R.id.tv_name)
        tvClass = view.findViewById(R.id.tv_class)
        tvDescription = view.findViewById(R.id.tv_description)
        tvSeeMore = view.findViewById(R.id.tv_see_more)
        tvHpText = view.findViewById(R.id.tv_hp_text)
        progressBarHealth = view.findViewById(R.id.progress_bar_health)
        rvStats = view.findViewById(R.id.rv_stats)
        rvInventory = view.findViewById(R.id.rv_inventory)
        btnCreateItem = view.findViewById(R.id.btn_create_item)

        db = FirebaseFirestore.getInstance()

        setHasOptionsMenu(true)

        character = CharacterProfileFragmentArgs.fromBundle(arguments).character
        readOnly = CharacterProfileFragmentArgs.fromBundle(arguments).readOnly

        tvName.text = character.nome
        tvClass.text = character.classe

        setHealthBar()

        if (character.descricao.isEmpty())
            tvDescription.text = "Personagem sem descrição"
        else
            tvDescription.text = character.descricao

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

        when {
            character.image_url.isNotEmpty() -> Glide.with(view)
                    .load(character.image_url)
                    .into(ivImagem)
            character.isnpc -> Glide.with(view)
                    .load(R.drawable.ic_monster)
                    .into(ivImagem)
            else -> Glide.with(view)
                    .load(R.drawable.ic_person)
                    .into(ivImagem)
        }

        val arrayStats = ArrayList<Atributo>()
        for (atributo in character.atributos)
            arrayStats.add(atributo)

        adapterStats = StatsListAdapter(arrayStats)
        rvStats.layoutManager = LinearLayoutManager(context)
        rvStats.setHasFixedSize(true)
        rvStats.adapter = adapterStats

        val queryItems = db
                .collection("adventures")
                .document(AdventureFragment.aventura.id)
                .collection("items")
                .whereEqualTo("owner", character.nome)

        val options = FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(queryItems, Item::class.java)
                .build()

        adapterItems = ItemsListAdapter(options) { item: Item -> itemItemClicked(item) }
        rvInventory.layoutManager = GridLayoutManager(context, 4)
        rvInventory.setHasFixedSize(true)

        if (character.ismaster && LoginFragment.username != character.nome)
            return view

        rvInventory.adapter = adapterItems

        if (!readOnly && LoginFragment.username == AdventureFragment.aventura.creator) {
            btnCreateItem.visibility = View.VISIBLE

            btnCreateItem.setOnClickListener {
                val action = CharacterProfileFragmentDirections.ActionCharacterProfileFragmentToNewItemFragment(Item(), character.nome)
                action.setItem(Item())
                action.setNewOwner(character.nome)
                NavHostFragment.findNavController(this).navigate(action)
            }
        }

        return view
    }

    private fun itemItemClicked(item: Item) {
        val action = CharacterProfileFragmentDirections.ActionCharacterProfileFragmentToItemProfileFragment(item, character)
        action.setItem(item)
        action.setOwner(character)
        action.setReadOnly(readOnly)
        NavHostFragment.findNavController(this).navigate(action)
    }

    private fun setHealthBar() {
        tvHpText.text = "${character.health}/${character.health_max}"

        var healthPercentage = 100
        if (character.health_max != 0)
            healthPercentage = 100 * character.health / character.health_max
        progressBarHealth.progress = healthPercentage

        val color = when {
            healthPercentage >= 66 -> ContextCompat.getColor(context!!, R.color.hp_green)
            healthPercentage >= 33 -> ContextCompat.getColor(context!!, R.color.hp_yellow)
            else -> ContextCompat.getColor(context!!, R.color.hp_red)
        }

        val progressDrawable = progressBarHealth.progressDrawable.mutate() as LayerDrawable
        progressDrawable.getDrawable(1).setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        progressBarHealth.progressDrawable = progressDrawable
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (!AdventureFragment.isMaster)
            return super.onOptionsItemSelected(menuItem)

        when (menuItem.itemId) {
            R.id.menu_editar -> {
                val action = NavGraphDirections.ActionGlobalNewCharacterFragment(character)
                action.setPersonagem(character)
                action.setIsNpc(character.isnpc)
                NavHostFragment.findNavController(this).navigate(action)
            }
            R.id.menu_ordenar -> {
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    override fun onStart() {
        super.onStart()
        adapterItems.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterItems.stopListening()
    }
}
