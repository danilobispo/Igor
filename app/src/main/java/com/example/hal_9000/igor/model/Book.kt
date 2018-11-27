package com.example.hal_9000.igor.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Book(
        var title: String = "",
        var url: String = "",
        var uploader: String = "",
        var uploaded_at: Long = 0L) : Parcelable