package com.example.hal_9000.igor.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Aventura(
        var title: String = "",
        var description: String = "",
        var theme: String = "",
        var creator: String = "",
        var next_session: String = "",
        var players: HashMap<String, Boolean> = hashMapOf(),
        val sessions: ArrayList<Session> = arrayListOf(),
        var deleted: Boolean = false) : Parcelable