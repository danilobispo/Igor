package com.example.hal_9000.igor.model

import android.os.Parcel
import android.os.Parcelable

class Personagem(
        var nome: String = "",
        var classe: String = "",
        var descricao: String = "",
        var health: Int = -1,
        var aventuraId: String = "",
        var jogadorId: String = "",
        var atributos: ArrayList<Atributo> = arrayListOf()) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.createTypedArrayList(Atributo.CREATOR))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nome)
        parcel.writeString(classe)
        parcel.writeString(descricao)
        parcel.writeInt(health)
        parcel.writeString(aventuraId)
        parcel.writeString(jogadorId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Personagem> {
        override fun createFromParcel(parcel: Parcel): Personagem {
            return Personagem(parcel)
        }

        override fun newArray(size: Int): Array<Personagem?> {
            return arrayOfNulls(size)
        }
    }
}