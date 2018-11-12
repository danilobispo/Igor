package com.example.hal_9000.igor.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Evento(
        var date: Long = 0L,
        var type: String = "",
        var event: String = "") : Parcelable