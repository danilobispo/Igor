package com.example.hal_9000.igor.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.example.hal_9000.igor.model.Aventura
import com.example.hal_9000.igor.model.Usuario

class MainViewModel : ViewModel() {
    private var user: MutableLiveData<Usuario> = MutableLiveData()
    private var username: MutableLiveData<String> = MutableLiveData()
    private var adventure: MutableLiveData<Aventura> = MutableLiveData()
    private var sessionId: MutableLiveData<String> = MutableLiveData()
    private var isMaster: MutableLiveData<Boolean> = MutableLiveData()

    init {
        isMaster.value = false
    }

    fun setUser(user: Usuario) {
        this.user.value = user
    }

    fun setUsername(username: String) {
        this.username.value = username
    }

    fun setAdventure(adventure: Aventura) {
        this.adventure.value = adventure
    }

    fun setSessionId(id: String) {
        this.sessionId.value = id
    }

    fun setIsMaster(isMaster: Boolean) {
        this.isMaster.value = isMaster
    }

    fun getUser(): Usuario? {
        return user.value
    }

    fun getUsername(): String? {
        return username.value
    }

    fun getAdventure(): Aventura? {
        return adventure.value
    }

    fun getSessionId(): String? {
        return sessionId.value
    }

    fun getIsMaster(): Boolean? {
        return isMaster.value
    }

    fun clearUsername() {
        username.value = null
    }

    fun clearUser() {
        user.value = null
    }

    fun clearAdventure() {
        adventure.value = null
    }

    fun clearSessionId() {
        adventure.value = null
    }

    fun clearIsMaster() {
        isMaster.value = false
    }
}