package com.smile.ballscollapse.presenters

import com.smile.ballscollapse.interfaces.MainPresentView
import com.smile.smilelibraries.utilities.SoundPoolUtil

class MainPresenter(private val presentView: MainPresentView) {
    val medalImageIds = presentView.getMedalImageIds()
    val loadingStr = presentView.getLoadingStr()
    val savingGameStr = presentView.geSavingGameStr()
    val loadingGameStr =  presentView.getLoadingGameStr()
    val sureToSaveGameStr = presentView.getSureToSaveGameStr()
    val sureToLoadGameStr = presentView.getSureToLoadGameStr()
    val gameOverStr = presentView.getGameOverStr()
    val saveScoreStr = presentView. getSaveScoreStr()
    val soundPool: SoundPoolUtil = presentView.soundPool()
    val highestScore = presentView.getHighestScore()
    fun fileInputStream(filename: String) = presentView.fileInputStream(filename)
    fun fileOutputStream(filename: String) = presentView.fileOutputStream(filename)
    fun addScoreInLocalTop10(playerName: String, score: Int) =
        presentView.addScoreInLocalTop10(playerName, score)
}