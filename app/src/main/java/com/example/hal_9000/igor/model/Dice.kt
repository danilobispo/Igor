package com.example.hal_9000.igor.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Dice(
        var dice: String = "",
        var value: Int = 0) : Parcelable