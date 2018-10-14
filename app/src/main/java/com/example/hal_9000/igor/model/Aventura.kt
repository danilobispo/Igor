package com.example.hal_9000.igor.model

import android.os.Parcel
import android.os.Parcelable

class Aventura(
        var title: String = "",
        var next_session: String = "",
        var theme: String = "",
        var creator: String = "",
        val sessions: ArrayList<Session> = arrayListOf(),
        var deleted: Boolean = false) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.createTypedArrayList(Session.CREATOR),
            parcel.readByte() != 0.toByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(next_session)
        parcel.writeString(theme)
        parcel.writeString(creator)
        parcel.writeByte(if (deleted) 1 else 0)
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

