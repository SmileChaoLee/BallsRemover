package com.smile.ballscollapse.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smile.ballscollapse.models.Settings

class SettingViewModel : ViewModel() {
    private val _settings = MutableLiveData<Settings>()
    val settings : LiveData<Settings>
        get() = _settings

    fun setSettings(set: Settings) {
        _settings.value = set
    }

    fun setHasSound(hasSound: Boolean) {
        _settings.value?.let {
            it.hasSound = hasSound
            // Have to trigger notifyPropertyChanged of Settings model
            // _settings.postValue(it)
            _settings.value = it
        }
    }

    fun setEasyLevel(easyLevel: Boolean) {
        _settings.value?.let {
            it.easyLevel = easyLevel
            // Have to trigger notifyPropertyChanged of Settings model
            // _settings.postValue(it)
            _settings.value = it
        }
    }

    fun setHasNextBall(hasNextBall: Boolean) {
        _settings.value?.let {
            it.hasNextBall = hasNextBall
            // Have to trigger notifyPropertyChanged of Settings model
            // _settings.postValue(it)
            _settings.value = it
        }
    }
}