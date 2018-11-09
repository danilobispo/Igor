package com.example.hal_9000.igor.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListAdapter
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Atributo
import kotlinx.android.synthetic.main.atributos_item.view.*

public class AtributosListAdapter(val atributos: ArrayList<Atributo>): BaseAdapter() {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.atributos_item, parent, false)
        val atr: Atributo = atributos[position]
        view.atributoNome.text = atr.nome
        view.atributoValor.text = atr.valor

        return view
    }

    override fun getItem(position: Int): Atributo {
        return atributos[position]
    }

    override fun getItemId(position: Int): Long {
        return 0;
    }

    override fun getCount(): Int {
        return atributos.size
    }

}