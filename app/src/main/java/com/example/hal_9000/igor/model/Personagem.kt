package com.example.hal_9000.igor.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Personagem(
        var id: String = "",
        var nome: String = "",
        var classe: String = "",
        var descricao: String = "",
        var health: Int = -1,
        var healthMax: Int = -1,
        var created: Long = 0,
        var creator: String = "",
        var isNpc: Boolean = false,
        var isMaster: Boolean = false,
        var aventuraId: String = "",
        var atributos: ArrayList<Atributo> = arrayListOf()) : Parcelable {

    fun hit(value: Int) {
        health -= value
        if (health < 0)
            health = 0
    }

    fun heal(value: Int) {
        health += value
        if (health > healthMax)
            health = healthMax
    }

    fun statUp(stat: String, value: Int) {
        atributos.forEachIndexed { index, atributo ->
            if (atributo.nome == stat) {
                val atributoValor = atributos[index].valor.toIntOrNull()
                if (atributoValor != null)
                    atributos[index].valor = (atributoValor + value).toString()
                return
            }
        }
    }

    fun statDown(stat: String, value: Int) {
        atributos.forEachIndexed { index, atributo ->
            if (atributo.nome == stat) {
                val atributoValor = atributos[index].valor.toIntOrNull()
                if (atributoValor != null)
                    atributos[index].valor = (atributoValor - value).toString()
                return
            }
        }
    }
}