package com.example.hal_9000.igor.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Personagem(
        var id: String = "",
        var nome: String = "",
        var classe: String = "",
        var descricao: String = "",
        var health: Int = -1,
        var created: Long = 0,
        var creator: String = "",
        var isNpc: Boolean = false,
        var aventuraId: String = "",
        var atributos: ArrayList<Atributo> = arrayListOf()) : Parcelable