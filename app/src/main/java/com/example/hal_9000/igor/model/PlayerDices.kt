package com.example.hal_9000.igor.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlayerDices(
        var character: String = "",
        var rolled: Boolean = false,
        var dices: ArrayList<Dice> = arrayListOf()) : Parcelable
