package com.example.hal_9000.igor.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

import com.example.hal_9000.igor.model.Categoria


class MenuAdapter(context: Context?, resource: Int, arrayCategorias: ArrayList<Categoria>) : ArrayAdapter<Categoria>(context, resource) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Todo: criar layout do item

        return super.getView(position, convertView, parent)
    }
}