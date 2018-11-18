package com.example.hal_9000.igor.fragment

import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.StatsListAdapter
import com.example.hal_9000.igor.model.Atributo
import com.example.hal_9000.igor.model.Personagem


class CharacterProfileFragment : Fragment() {

    private lateinit var ivImagem: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvClass: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvSeeMore: TextView
    private lateinit var tvHpText: TextView
    private lateinit var progressBarHealth: ProgressBar
    private lateinit var lvStats: ListView

    private lateinit var hpBarRemaining: View

    private lateinit var character: Personagem

    private lateinit var adapter: StatsListAdapter

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
        lvStats = view.findViewById(R.id.lv_stats)

        character = CharacterProfileFragmentArgs.fromBundle(arguments).character

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

        val arrayOfStats = ArrayList<Atributo>()
        adapter = StatsListAdapter(context!!, arrayOfStats)
        lvStats.adapter = adapter

        for (atributo in character.atributos)
            adapter.add(atributo)

        return view
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
}
