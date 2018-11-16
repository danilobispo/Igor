package com.example.hal_9000.igor.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Session(
        var title: String = "",
        var summary: String = "",
        var created: Long = 0L,
        var date: String = "") : Parcelable

