package com.example.hal_9000.igor.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.example.hal_9000.igor.model.Aventura

class MainViewModel : ViewModel() {
    private var adventure: MutableLiveData<Aventura> = MutableLiveData()
    private var sessionId: MutableLiveData<String> = MutableLiveData()
    private var username: MutableLiveData<String> = MutableLiveData()
    private var isMaster: MutableLiveData<Boolean> = MutableLiveData()

    fun setAdventure(adventure: Aventura) {
        this.adventure.value = adventure
    }

    fun setSessionId(id: String) {
        this.sessionId.value = id
    }

    fun setUsername(username: String) {
        this.username.value = username
    }

    fun setIsMaster(isMaster: Boolean) {
        this.isMaster.value = isMaster
    }

    fun getAdventure(): Aventura? {
        return adventure.value
    }

    fun getSessionId(): String? {
        return sessionId.value
    }

    fun getUsername(): String? {
        return username.value
    }

    fun getIsMaster(): Boolean? {
        return isMaster.value
    }
}