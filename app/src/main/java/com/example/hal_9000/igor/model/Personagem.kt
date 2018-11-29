package com.example.hal_9000.igor.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Personagem(
        var id: String = "",
        var nome: String = "",
        var classe: String = "",
        var descricao: String = "",
        var health: Int = 0,
        var health_max: Int = 0,
        var created_at: Long = 0,
        var creator: String = "",
        var image_url: String = "",
        var hidden: Boolean = false,
        var isnpc: Boolean = false,
        var ismaster: Boolean = false,
        var aventura: String = "",
        var aventura_id: String = "",
        var atributos: ArrayList<Atributo> = arrayListOf()) : Parcelable {

    fun hit(value: Int) {
        health -= value
        if (health < 0)
            health = 0
    }

    fun heal(value: Int) {
        health += value
        if (health > health_max)
            health = health_max
    }

    private fun statCreate(statName: String, value: String) {
        atributos.add(Atributo(statName, value))
    }

    fun statRemove(statName: String) {
        atributos.forEachIndexed { index, atributo ->
            if (atributo.nome == statName) {
                atributos.removeAt(index)
                return
            }
        }
    }

    fun statChange(statName: String, value: String) {
        atributos.forEachIndexed { index, atributo ->
            if (atributo.nome == statName) {
                atributos[index].valor = value
                return
            }
        }
        statCreate(statName, value)
    }

    fun statUp(statName: String, value: Int) {
        atributos.forEachIndexed { index, atributo ->
            if (atributo.nome == statName) {
                val atributoValor = atributos[index].valor.toIntOrNull()
                if (atributoValor != null)
                    atributos[index].valor = (atributoValor + value).toString()
                return
            }
        }
        statCreate(statName, value.toString())
    }

    fun statDown(statName: String, value: Int) {
        atributos.forEachIndexed { index, atributo ->
            if (atributo.nome == statName) {
                val atributoValor = atributos[index].valor.toIntOrNull()
                if (atributoValor != null)
                    atributos[index].valor = (atributoValor - value).toString()
                return
            }
        }
        statCreate(statName, (0 - value).toString())
    }

    fun maxHealthUp(value: Int) {
        health_max += value
    }

    fun maxHealthDown(value: Int) {
        health_max -= value
    }
}