package com.example.hal_9000.igor.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Atributo


class StatsListAdapter(context: Context, stats: ArrayList<Atributo>) : ArrayAdapter<Atributo>(context, 0, stats) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val stat = getItem(position)

        val viewHolder: ViewHolder // view lookup cache stored in tag

        if (view == null) {
            viewHolder = ViewHolder()
            view = LayoutInflater.from(context).inflate(R.layout.stat_item, parent, false)
            viewHolder.name = view?.findViewById(R.id.tv_stat_name) as TextView
            viewHolder.value = view.findViewById(R.id.tv_stat_value) as TextView
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.name.text = stat?.nome
        viewHolder.value.text = stat?.valor

        return view
    }

    private class ViewHolder {
        lateinit var name: TextView
        lateinit var value: TextView
    }
}