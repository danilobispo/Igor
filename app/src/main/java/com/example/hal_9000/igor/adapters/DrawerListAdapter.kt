package com.example.hal_9000.igor.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.DrawerItem

class DrawerListAdapter(context: Context, private val dataSource: ArrayList<DrawerItem>) : BaseAdapter() {
    private val TAG = "DrawerListAdapter"

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private val selectedColor = ContextCompat.getColor(context, R.color.nav_item_selected)
    private val unselectedColor = ContextCompat.getColor(context, R.color.nav_item)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.drawer_item, parent, false)

            holder = ViewHolder()
            holder.name = view.findViewById(R.id.tv_name) as TextView
            holder.selectedMarker = view.findViewById(R.id.selected_marker) as LinearLayout
            holder.icon = view.findViewById(R.id.iv_icon) as ImageView

            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val name = holder.name
        val selectedMarker = holder.selectedMarker
        val icon = holder.icon

        val drawerItem = getItem(position) as DrawerItem

        if (drawerItem.notification) {
            when (position) {
                0 -> icon.setImageResource(R.drawable.nav_adventure)
                1 -> icon.setImageResource(R.drawable.nav_books)
                2 -> icon.setImageResource(R.drawable.nav_account)
                3 -> icon.setImageResource(R.drawable.nav_notifications)
                4 -> icon.setImageResource(R.drawable.nav_configurations)
                5 -> icon.setImageResource(R.drawable.ic_exit)
            }
        } else {
            when (position) {
                0 -> icon.setImageResource(R.drawable.ic_map)
                1 -> icon.setImageResource(R.drawable.ic_book)
                2 -> icon.setImageResource(R.drawable.ic_person2)
                3 -> icon.setImageResource(R.drawable.ic_notifications)
                4 -> icon.setImageResource(R.drawable.ic_settings)
                5 -> icon.setImageResource(R.drawable.ic_exit)
            }
        }

        name.text = drawerItem.name

        if (drawerItem.selected) {
            selectedMarker.visibility = View.VISIBLE
            name.setTextColor(selectedColor)
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(selectedColor))
        } else {
            selectedMarker.visibility = View.INVISIBLE
            name.setTextColor(unselectedColor)
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(unselectedColor))
        }

        return view
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

    private class ViewHolder {
        lateinit var name: TextView
        lateinit var selectedMarker: LinearLayout
        lateinit var icon: ImageView
    }
}
