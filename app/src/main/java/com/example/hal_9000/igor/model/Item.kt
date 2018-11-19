package com.example.hal_9000.igor.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Item(
        var id: String = "",
        var name: String = "",
        var description: String = "",
        var type: String = "",
        var equipped: Boolean = false,
        var owner: String = "",
        var image_url: String = "",
        var stats: ArrayList<Atributo> = arrayListOf()) : Parcelable
