package com.example.hal_9000.igor.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Atributo(
        val nome: String = "",
        var valor: String = "") : Parcelable