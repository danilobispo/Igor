package com.example.hal_9000.igor.model

import android.os.Parcel
import android.os.Parcelable

class Aventura(
        var title: String = "",
        var next_session: String = "",
        var theme: String = "",
        var creator: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(next_session)
        parcel.writeString(theme)
        parcel.writeString(creator)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Aventura> {
        override fun createFromParcel(parcel: Parcel): Aventura {
            return Aventura(parcel)
        }

        override fun newArray(size: Int): Array<Aventura?> {
            return arrayOfNulls(size)
        }
    }
}