package com.smile.ballsremover.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smile.ballsremover.models.Settings

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

    fun setGameLevel(gameLevel: Int) {
        _settings.value?.let {
            it.gameLevel = gameLevel
            // Have to trigger notifyPropertyChanged of Settings model
            // _settings.postValue(it)
            _settings.value = it
        }
    }

    fun setFillColumn(fillColumn: Boolean) {
        _settings.value?.let {
            it.fillColumn = fillColumn
            _settings.value = it
        }
    }
}