package com.smile.ballscollapse.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameProp(
    var isShowingLoadingMessage : Boolean = false,
    var isShowingScoreMessage : Boolean = false,
    var isShowNextBallsAfterBlinking : Boolean = false,
    var isProcessingJob : Boolean = false,
    var isShowingNewGameDialog : Boolean = false,
    var isShowingQuitGameDialog : Boolean = false,
    var isShowingSureSaveDialog : Boolean = false,
    var isShowingSureLoadDialog : Boolean = false,
    var isShowingGameOverDialog : Boolean = false,
    var bouncyBallIndexI : Int = -1,
    var bouncyBallIndexJ : Int = -1,
    var isBallBouncing : Boolean = false,
    var isBallMoving : Boolean = false,
    var undoEnable : Boolean = false,
    var currentScore : Int = 0,
    var undoScore : Int = 0,
    var lastGotScore : Int = 0,
    var isEasyLevel : Boolean = true,
    var hasSound : Boolean = true,
    var hasNextBall : Boolean = true) : Parcelable {

    fun initialize() {
        isShowingLoadingMessage = false
        isShowingScoreMessage = false
        isShowNextBallsAfterBlinking = false
        isProcessingJob = false
        isShowingNewGameDialog = false
        isShowingQuitGameDialog = false
        isShowingSureSaveDialog = false
        isShowingSureLoadDialog = false
        isShowingGameOverDialog = false
        bouncyBallIndexI = -1
        bouncyBallIndexJ = -1
        isBallBouncing = false
        isBallMoving = false
        undoEnable = false
        currentScore = 0
        undoScore = 0
        lastGotScore = 0
        isEasyLevel = true
        hasSound = true
        hasNextBall = true
    }
}