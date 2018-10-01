package com.example.hal_9000.igor.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.widget.ImageViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Categoria

class ListDrawerAdapter(context: Context,
                        private val dataSource: ArrayList<Categoria>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private val selectedColor = ContextCompat.getColor(context, R.color.nav_item_selected)
    private val unselectedColor = ContextCompat.getColor(context, R.color.nav_item)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.listview_item_row, parent, false)

        val categoria = getItem(position) as Categoria

        val categoriaTextView = rowView.findViewById(R.id.tv_name) as TextView
        val marker = rowView.findViewById(R.id.selected_marker) as LinearLayout
        val iconImageView = rowView.findViewById(R.id.iv_icon) as ImageView

        categoriaTextView.text = categoria.nome

        if (!categoria.notification!!) {
            when (position) {
                0 -> {
                    iconImageView.setImageDrawable(ResourcesCompat.getDrawable(parent!!.context!!.resources, R.drawable.aventuras_icone, null))
                }
                1 -> {
                    iconImageView.setImageDrawable(ResourcesCompat.getDrawable(parent!!.context!!.resources, R.drawable.livros_icone, null))
                }
                2 -> {
                    iconImageView.setImageDrawable(ResourcesCompat.getDrawable(parent!!.context!!.resources, R.drawable.conta_icone, null))
                }
                3 -> {
                    iconImageView.setImageDrawable(ResourcesCompat.getDrawable(parent!!.context!!.resources, R.drawable.notificacoes_icone, null))
                }
                4 -> {
                    iconImageView.setImageDrawable(ResourcesCompat.getDrawable(parent!!.context!!.resources, R.drawable.configuracoes_icone, null))
                }
                5 -> {
                    iconImageView.setImageDrawable(ResourcesCompat.getDrawable(parent!!.context!!.resources, R.drawable.ic_exit, null))
                }
            }
        } else {
            when (position) {
                0 -> {
                    iconImageView.setImageDrawable(ResourcesCompat.getDrawable(parent!!.context!!.resources, R.drawable.nav_aventura, null))
                }
                1 -> {
                    iconImageView.setImageDrawable(ResourcesCompat.getDrawable(parent!!.context!!.resources, R.drawable.nav_livros, null))
                }
                2 -> {
                    iconImageView.setImageDrawable(ResourcesCompat.getDrawable(parent!!.context!!.resources, R.drawable.nav_conta, null))
                }
                3 -> {
                    iconImageView.setImageDrawable(ResourcesCompat.getDrawable(parent!!.context!!.resources, R.drawable.nav_notificacoes, null))
                }
                4 -> {
                    iconImageView.setImageDrawable(ResourcesCompat.getDrawable(parent!!.context!!.resources, R.drawable.nav_configuracoes, null))
                }
                5 -> {
                    iconImageView.setImageDrawable(ResourcesCompat.getDrawable(parent!!.context!!.resources, R.drawable.ic_exit, null))
                }
            }
        }

        if (categoria.selected!!) {
            marker.visibility = View.VISIBLE
            categoriaTextView.setTextColor(selectedColor)
            ImageViewCompat.setImageTintList(iconImageView, ColorStateList.valueOf(selectedColor))
        } else {
            marker.visibility = View.INVISIBLE
            categoriaTextView.setTextColor(unselectedColor)
        }

        return rowView
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return dataSource.size
    }
}
