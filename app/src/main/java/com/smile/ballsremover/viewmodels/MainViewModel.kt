package com.smile.ballsremover.viewmodels

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.BundleCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smile.ballsremover.SmileApp
import com.smile.ballsremover.constants.Constants
import com.smile.ballsremover.models.ColorBallInfo
import com.smile.ballsremover.models.GameProp
import com.smile.ballsremover.models.GridData
import com.smile.ballsremover.presenters.MainPresenter
import com.smile.ballsremover.constants.WhichBall
import com.smile.ballsremover.models.Settings
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.utilities.SoundPoolUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.nio.ByteBuffer

class MainViewModel: ViewModel() {

    private interface ShowScoreCallback {
        fun sCallback()
    }

    private lateinit var mPresenter: MainPresenter
    private val showingScoreHandler = Handler(Looper.getMainLooper())
    private var mGameProp = GameProp()
    private var mGridData = GridData()
    private val settings = Settings()

    private var loadingStr = ""
    private var savingGameStr = ""
    private var loadingGameStr = ""
    private var sureToSaveGameStr = ""
    private var sureToLoadGameStr = ""
    private var saveScoreStr = ""
    private lateinit var soundPool: SoundPoolUtil
    private lateinit var medalImageIds: List<Int>

    var mGameAction = Constants.IS_QUITING_GAME
    val currentScore = mutableIntStateOf(0)
    val highestScore = mutableIntStateOf(0)
    val screenMessage = mutableStateOf("")

    private val _saveGameText = mutableStateOf("")
    val saveGameText: MutableState<String>
        get() = _saveGameText
    fun setSaveGameText(text: String) {
        _saveGameText.value = text
    }

    private val _loadGameText = mutableStateOf("")
    val loadGameText: MutableState<String>
        get() = _loadGameText
    fun setLoadGameText(text: String) {
        _loadGameText.value = text
    }

    private val _saveScoreTitle = mutableStateOf("")
    val saveScoreTitle: MutableState<String>
        get() = _saveScoreTitle
    fun setSaveScoreTitle(title: String) {
        _saveScoreTitle.value = title
    }

    val gridDataArray = Array(Constants.ROW_COUNTS) {
        Array(Constants.COLUMN_COUNTS) {
            mutableStateOf(ColorBallInfo())
        }
    }

    init {
        Log.d(TAG, "MainViewModel.init")
    }

    fun setPresenter(presenter: MainPresenter) {
        mPresenter = presenter
        medalImageIds = mPresenter.medalImageIds
        loadingStr = mPresenter.loadingStr
        savingGameStr = mPresenter.savingGameStr
        loadingGameStr = mPresenter.loadingGameStr
        sureToSaveGameStr = mPresenter.sureToSaveGameStr
        sureToLoadGameStr = mPresenter.sureToLoadGameStr
        saveScoreStr = mPresenter.saveScoreStr
        soundPool = mPresenter.soundPool
    }

    fun cellClickListener(i: Int, j: Int) {
        Log.d(TAG, "cellClickListener.($i, $j)")
        if (SmileApp.isProcessingJob) return
        if (mGridData.checkMoreThanTwo(i, j)) {
            mGridData.backupCells()
            mGameProp.undoScore = mGameProp.currentScore
            mGameProp.undoEnable = true
            SmileApp.isProcessingJob = true
            val tempLine = HashSet(mGridData.getLightLine())
            Log.d(TAG, "cellClickListener.tempLine.size = ${tempLine.size}")
            mGameProp.lastGotScore = calculateScore(tempLine)
            mGameProp.currentScore += mGameProp.lastGotScore
            currentScore.intValue = mGameProp.currentScore
            val showScore = ShowScore(
                mGridData.getLightLine(), mGameProp.lastGotScore,
                object : ShowScoreCallback {
                    override fun sCallback() {
                        Log.d(TAG, "cellClickListener.sCallback")
                        viewModelScope.launch(Dispatchers.Default) {
                            // Refresh the game view
                            mGridData.refreshColorBalls(fillColumn())
                            delay(200)
                            displayGameGridView()
                            SmileApp.isProcessingJob = false
                            if (mGridData.isGameOver()) {
                                Log.d(TAG, "cellClickListener.sCallback.gameOver()")
                                gameOver()
                            }
                        }
                    }
                })
            showingScoreHandler.post(showScore)
        }
    }

    private fun setData(prop: GameProp, gData: GridData) {
        Log.d(TAG, "setData")
        mGameProp = prop
        mGridData = gData
    }

    private fun initData() {
        Log.d(TAG, "initData")
        mGameProp.initialize()
        mGridData.initialize()
    }

    fun initGame(state: Bundle?) {
        Log.d(TAG, "initGame.isNewGame = $state")
        val isNewGame = restoreState(state)
        highestScore.intValue = mPresenter.highestScore
        currentScore.intValue = mGameProp.currentScore
        if (isNewGame) {
            // generate
            mGridData.generateColorBalls()
        }
        displayGameGridView()
    }

    private fun restoreState(state: Bundle?): Boolean {
        var isNewGame: Boolean
        var gameProp: GameProp? = null
        var gridData: GridData? = null
        state?.let {
            Log.d(TAG,"restoreState.state not null then restore the state")
            gameProp =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    BundleCompat.getParcelable(it, Constants.GAME_PROP_TAG,
                        GameProp::class.java)
                else it.getParcelable(Constants.GAME_PROP_TAG)
            gridData =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    BundleCompat.getParcelable(it, Constants.GRID_DATA_TAG,
                        GridData::class.java)
                else it.getParcelable(Constants.GRID_DATA_TAG)
        }
        isNewGame = true
        if (gameProp != null && gridData != null) {
            Log.d(TAG, "restoreState.gridData!! = ${gridData!!}")
            gridData?.apply {
                for (i in 0 until Constants.ROW_COUNTS) {
                    for (j in 0 until Constants.COLUMN_COUNTS) {
                        if (getCellValue(i, j) != 0) {
                            // has value, so not a new game
                            isNewGame = false
                            break
                        }
                    }
                }
                if (isNewGame) {
                    Log.d(TAG, "restoreState.CellValues are all 0")
                }
            }
        }
        Log.d(TAG, "restoreState.isNewGame = $isNewGame")
        if (isNewGame) {
            initData()
        } else {
            setData(gameProp!!, gridData!!)
        }

        return isNewGame
    }

    fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState.mGridData = $mGridData")
        outState.putParcelable(Constants.GAME_PROP_TAG, mGameProp)
        outState.putParcelable(Constants.GRID_DATA_TAG, mGridData)
    }

    fun setHasSound(hasSound: Boolean) {
        settings.hasSound = hasSound
    }
    fun hasSound(): Boolean {
        return settings.hasSound
    }

    fun setGameLevel(gameLevel: Int) {
        settings.gameLevel = gameLevel
    }
    fun gameLevel(): Int {
        return settings.gameLevel
    }

    fun setFillColumn(fillColumn: Boolean) {
        settings.fillColumn = fillColumn
    }
    fun fillColumn(): Boolean {
        return settings.fillColumn
    }

    fun undoTheLast() {
        Log.d(TAG, "undoTheLast.undoEnable = ${mGameProp.undoEnable}")
        if (!mGameProp.undoEnable) {
            return
        }
        mGridData.undoTheLast()
        // restore the screen
        displayGameGridView()
        mGameProp.currentScore = mGameProp.undoScore
        currentScore.intValue = mGameProp.currentScore
        mGameProp.undoEnable = false
    }

    fun saveScore(playerName: String) {
        // use thread to add a record to remote database
        val restThread: Thread = object : Thread() {
            override fun run() {
                try {
                    // ASP.NET Core
                    val jsonObject = JSONObject()
                    jsonObject.put("PlayerName", playerName)
                    jsonObject.put("Score", mGameProp.currentScore)
                    jsonObject.put("GameId", Constants.GAME_ID)
                    PlayerRecordRest.addOneRecord(jsonObject)
                    Log.d(TAG, "saveScore.Succeeded to add one record to remote.")
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    Log.d(TAG, "saveScore.Failed to add one record to remote.")
                }
            }
        }
        restThread.start()

        // save to local storage
        mPresenter.addScoreInLocalTop10(playerName, mGameProp.currentScore)
    }

    fun newGame() {
        // creating a new game
        mGameAction = Constants.IS_CREATING_GAME
        setSaveScoreTitle(saveScoreStr)
    }

    fun quitGame() {
        // quiting the game
        mGameAction = Constants.IS_QUITING_GAME
        setSaveScoreTitle(saveScoreStr)
    }

    fun saveGame() {
        Log.d(TAG, "saveGame")
        setSaveGameText(sureToSaveGameStr)
    }

    fun loadGame() {
        Log.d(TAG, "loadGame")
        setLoadGameText(sureToLoadGameStr)
    }

    fun startSavingGame(): Boolean {
        Log.d(TAG, "startSavingGame")
        SmileApp.isProcessingJob = true
        screenMessage.value = savingGameStr
        var succeeded = true
        try {
            val foStream = mPresenter.fileOutputStream(SAVE_FILENAME)
            // save settings
            if (hasSound()) foStream.write(1) else foStream.write(0)
            foStream.write(gameLevel())
            if (fillColumn()) foStream.write(1) else foStream.write(0)
            // save values on game grid
            for (i in 0 until Constants.ROW_COUNTS) {
                for (j in 0 until Constants.COLUMN_COUNTS) {
                    foStream.write(mGridData.getCellValue(i, j))
                }
            }
            // save backupCells
            for (i in 0 until Constants.ROW_COUNTS) {
                for (j in 0 until Constants.COLUMN_COUNTS) {
                    foStream.write(mGridData.getBackupCells()[i][j])
                }
            }
            // save current score
            val scoreByte = ByteBuffer.allocate(4).putInt(mGameProp.currentScore).array()
            foStream.write(scoreByte)
            // save undo score
            val undoScoreByte = ByteBuffer.allocate(4).putInt(mGameProp.undoScore).array()
            foStream.write(undoScoreByte)
            // save undoEnable
            if (mGameProp.undoEnable) foStream.write(1) else foStream.write(0)
            foStream.close()
            // end of writing
            Log.d(TAG, "startSavingGame.Succeeded.")
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
            Log.d(TAG, "startSavingGame.Failed.")
        }
        SmileApp.isProcessingJob = false
        screenMessage.value = ""
        Log.d(TAG, "startSavingGame.Finished")
        return succeeded
    }

    fun startLoadingGame(): Boolean {
        Log.d(TAG, "startLoadingGame")
        SmileApp.isProcessingJob = true
        screenMessage.value = loadingGameStr
        var succeeded = true
        val hasSound: Boolean
        val gameLevel: Int
        val fillColumn: Boolean
        val gameCells = Array(Constants.ROW_COUNTS) {
            IntArray(Constants.COLUMN_COUNTS) }
        val cScore: Int
        val isUndoEnable: Boolean
        val backupCells = Array(Constants.ROW_COUNTS) {
            IntArray(Constants.COLUMN_COUNTS) }
        val unScore: Int
        try {
            val fiStream = mPresenter.fileInputStream(SAVE_FILENAME)
            Log.d(TAG, "startLoadingGame.available() = " + fiStream.available())
            Log.d(TAG, "startLoadingGame.getChannel().size() = " + fiStream.channel.size())
            // read game settings
            var bValue = fiStream.read()
            hasSound = bValue == 1
            gameLevel = fiStream.read()
            bValue = fiStream.read()
            fillColumn = bValue == 1
            settings.hasSound = hasSound
            settings.gameLevel = gameLevel
            settings.fillColumn = fillColumn
            // load values on game grid
            for (i in 0 until Constants.ROW_COUNTS) {
                for (j in 0 until Constants.COLUMN_COUNTS) {
                    gameCells[i][j] = fiStream.read()
                }
            }
            // reading backupCells
            for (i in 0 until Constants.ROW_COUNTS) {
                for (j in 0 until Constants.COLUMN_COUNTS) {
                    backupCells[i][j] = fiStream.read()
                }
            }
            // reading current score
            val scoreByte = ByteArray(4)
            fiStream.read(scoreByte)
            cScore = ByteBuffer.wrap(scoreByte).getInt()
            // reading undo score
            val undoScoreByte = ByteArray(4)
            fiStream.read(undoScoreByte)
            unScore = ByteBuffer.wrap(undoScoreByte).getInt()
            // reading undoEnable
            bValue = fiStream.read()
            isUndoEnable = bValue == 1
            fiStream.close()

            // refresh Main UI with loaded data
            mGridData.setCellValues(gameCells)
            mGridData.setBackupCells(backupCells)
            mGameProp.currentScore = cScore
            mGameProp.undoScore = unScore
            mGameProp.undoEnable = isUndoEnable
            // start update UI
            currentScore.intValue = mGameProp.currentScore
            displayGameGridView()
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
        }
        SmileApp.isProcessingJob = false
        screenMessage.value = ""
        return succeeded
    }

    fun release() {
        showingScoreHandler.removeCallbacksAndMessages(null)
        soundPool.release()
    }

    private fun gameOver() {
        Log.d(TAG, "gameOver")
        if (hasSound()) {
            soundPool.playSound()
        }
        newGame()
    }

    private fun calculateScore(linkedLine: HashSet<Point>?): Int {
        if (linkedLine == null) {
            return 0
        }
        // 5 points for each ball if only 2 balls
        // 6 points for each ball if it is 3 balls
        // 7 points for each ball if it is 4 balls
        // 8 points for each ball if it is 5 balls
        val minBalls = 2
        val minScoreEach = 5
        val numBalls = linkedLine.size
        return (minScoreEach + (numBalls - minBalls)) * numBalls
    }

    private fun drawBall(i: Int, j: Int, color: Int) {
        Log.d(TAG, "drawBall.($i, $j), color = $color")
        gridDataArray[i][j].value = ColorBallInfo(color, WhichBall.BALL)
    }

    private fun drawOval(i: Int, j: Int, color: Int) {
        Log.d(TAG, "drawOval.($i, $j), color = $color")
        gridDataArray[i][j].value = ColorBallInfo(color, WhichBall.OVAL_BALL)
    }

    private fun clearCell(i: Int, j: Int) {
        mGridData.setCellValue(i, j, 0)
    }

    private fun displayGameGridView() {
        Log.d(TAG, "displayGameGridView")
        try {
            for (i in 0 until Constants.ROW_COUNTS) {
                for (j in 0 until Constants.COLUMN_COUNTS) {
                    val color = mGridData.getCellValue(i, j)
                    drawBall(i, j, color)
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.d(TAG, "displayGameGridView.Exception: ")
            ex.printStackTrace()
        }
    }

    private inner class ShowScore(
        linkedPoint: HashSet<Point>,
        val lastGotScore: Int,
        val callback: ShowScoreCallback
    ): Runnable {
        private var pointSet: HashSet<Point>
        private var mCounter = 0
        init {
            Log.d(TAG, "ShowScore")
            pointSet = HashSet(linkedPoint)
        }

        @Synchronized
        private fun onProgressUpdate(status: Int) {
            when (status) {
                0 -> for (item in pointSet) {
                    drawBall(item.x, item.y, mGridData.getCellValue(item.x, item.y))
                }
                1 -> for (item in pointSet) {
                    drawOval(item.x, item.y, mGridData.getCellValue(item.x, item.y))
                }
                2 -> {}
                3 -> {
                    screenMessage.value = lastGotScore.toString()
                    for (item in pointSet) {
                        clearCell(item.x, item.y)
                        drawBall(item.x, item.y, mGridData.getCellValue(item.x, item.y))
                    }
                }
                4 -> {
                    Log.d(TAG, "ShowScore.onProgressUpdate.dismissShowMessageOnScreen.")
                    screenMessage.value = ""
                }
                else -> {}
            }
        }

        @Synchronized
        override fun run() {
            val twinkleCountDown = 5
            mCounter++
            Log.d(TAG, "ShowScore.run().mCounter = $mCounter")
            if (mCounter <= twinkleCountDown) {
                val md = mCounter % 2 // modulus
                onProgressUpdate(md)
                showingScoreHandler.postDelayed(this, 150)
            } else {
                if (mCounter == twinkleCountDown + 1) {
                    onProgressUpdate(3) // show score
                    showingScoreHandler.postDelayed(this, 500)
                } else {
                    showingScoreHandler.removeCallbacksAndMessages(null)
                    onProgressUpdate(4) // dismiss showing message
                    callback.sCallback()
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
        private const val SAVE_FILENAME = "SavedGame"
    }
}