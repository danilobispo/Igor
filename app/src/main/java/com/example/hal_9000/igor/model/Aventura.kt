package com.example.hal_9000.igor.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Aventura(
        var id: String = "",
        var title: String = "",
        var description: String = "",
        var theme: String = "",
        var creator: String = "",
        var created_at: Long = 0L,
        var next_session: Long = 0L,
        var players: HashMap<String, Boolean> = hashMapOf(),
        var deleted: Boolean = false) : Parcelable