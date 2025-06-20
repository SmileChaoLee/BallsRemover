package com.smile.ballsremover.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameProp(
    var isShowingScoreMessage : Boolean = false,
    var undoEnable : Boolean = false,
    var currentScore : Int = 0,
    var undoScore : Int = 0,
    var lastGotScore : Int = 0,
    var hasSound : Boolean = true) : Parcelable {

    fun initialize() {
        isShowingScoreMessage = false
        undoEnable = false
        currentScore = 0
        undoScore = 0
        lastGotScore = 0
        hasSound = true
    }
}