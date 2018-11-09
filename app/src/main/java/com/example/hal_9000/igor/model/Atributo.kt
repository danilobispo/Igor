package com.example.hal_9000.igor.model

import android.os.Parcel
import android.os.Parcelable

public class Atributo(
        val nome: String = "",
        val valor: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nome)
        parcel.writeString(valor)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Atributo> {
        override fun createFromParcel(parcel: Parcel): Atributo {
            return Atributo(parcel)
        }

        override fun newArray(size: Int): Array<Atributo?> {
            return arrayOfNulls(size)
        }
    }
}