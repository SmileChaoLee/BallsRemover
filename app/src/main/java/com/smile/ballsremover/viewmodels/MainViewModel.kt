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
    private val bouncyBallHandler = Handler(Looper.getMainLooper())
    private val movingBallHandler = Handler(Looper.getMainLooper())
    private val showingScoreHandler = Handler(Looper.getMainLooper())
    private var mGameProp = GameProp()
    private var mGridData = GridData()

    private var loadingStr = ""
    private var savingGameStr = ""
    private var loadingGameStr = ""
    private var sureToSaveGameStr = ""
    private var sureToLoadGameStr = ""
    private var gameOverStr = ""
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

    private val _gameOverText = mutableStateOf("")
    val gameOverText: MutableState<String>
        get() = _gameOverText
    fun setGameOverText(text: String) {
        _gameOverText.value = text
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
        gameOverStr = mPresenter.gameOverStr
        saveScoreStr = mPresenter.saveScoreStr
        soundPool = mPresenter.soundPool
    }

    fun cellClickListener(i: Int, j: Int) {
        Log.d(TAG, "cellClickListener.($i, $j)")
        if (mGameProp.isShowingScoreMessage) return
        val hasMoreTwo = mGridData.checkMoreThanTwo(i, j)
        if (hasMoreTwo) {
            val tempLine = HashSet(mGridData.getLightLine())
            Log.d(TAG, "cellClickListener.tempLine.size = ${tempLine.size}")
            mGameProp.lastGotScore = calculateScore(tempLine)
            mGameProp.undoScore = mGameProp.currentScore
            mGameProp.currentScore += mGameProp.lastGotScore
            currentScore.intValue = mGameProp.currentScore
            mGameProp.isShowingScoreMessage = true
            val showScore = ShowScore(
                mGridData.getLightLine(), mGameProp.lastGotScore,
                object : ShowScoreCallback {
                    override fun sCallback() {
                        Log.d(TAG, "cellClickListener.sCallback")
                        viewModelScope.launch(Dispatchers.Default) {
                            // Refresh the game view
                            mGridData.refreshColorBalls()
                            delay(200)
                            displayGameGridView()
                            mGameProp.isShowingScoreMessage = false
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

    private fun lastPartOfInitialGame() {
        if (mGameProp.isShowingNewGameDialog) {
            Log.d(TAG, "lastPartOfInitialGame.newGame()")
            newGame()
        }
        if (mGameProp.isShowingQuitGameDialog) {
            Log.d(TAG, "lastPartOfInitialGame.show quitGame()")
            quitGame()
        }
        if (mGameProp.isShowingSureSaveDialog) {
            Log.d(TAG, "lastPartOfInitialGame.saveGame()")
            saveGame()
        }
        if (mGameProp.isShowingSureLoadDialog) {
            Log.d(TAG, "lastPartOfInitialGame.loadGame()")
            loadGame()
        }
        if (mGameProp.isShowingGameOverDialog) {
            Log.d(TAG, "lastPartOfInitialGame.gameOver()")
            gameOver()
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        mGameProp.isShowingLoadingMessage = SmileApp.isShowingLoadingMessage
        mGameProp.isProcessingJob = SmileApp.isProcessingJob
        Log.d(TAG, "onSaveInstanceState.mGridData = $mGridData")
        outState.putParcelable(Constants.GAME_PROP_TAG, mGameProp)
        outState.putParcelable(Constants.GRID_DATA_TAG, mGridData)
    }

    fun hasSound(): Boolean {
        println("Presenter.hasSound.mGameProp = $mGameProp")
        println("Presenter.hasSound.hasSound = ${mGameProp.hasSound}")
        return mGameProp.hasSound
    }

    fun setHasSound(hasSound: Boolean) {
        println("Presenter.setHasSound.mGameProp = $mGameProp")
        println("Presenter.setHasSound.hasSound = $hasSound")
        mGameProp.hasSound = hasSound
    }

    private fun setShowingNewGameDialog(showingNewGameDialog: Boolean) {
        mGameProp.isShowingNewGameDialog = showingNewGameDialog
    }

    private fun setShowingQuitGameDialog(showingQuitGameDialog: Boolean) {
        mGameProp.isShowingQuitGameDialog = showingQuitGameDialog
    }

    fun undoTheLast() {
        if (!mGameProp.undoEnable) {
            return
        }
        SmileApp.isProcessingJob = true // started undoing
        mGridData.undoTheLast()
        stopBouncyAnimation()
        mGameProp.bouncyBallIndexI = -1
        mGameProp.bouncyBallIndexJ = -1
        // restore the screen
        displayGameGridView()
        mGameProp.currentScore = mGameProp.undoScore
        currentScore.intValue = mGameProp.currentScore
        // completedPath = true;
        mGameProp.undoEnable = false
        SmileApp.isProcessingJob = false // finished
    }

    fun setSaveScoreAlertDialogState(state: Boolean) {
        SmileApp.isProcessingJob = state
        if (mGameAction == Constants.IS_CREATING_GAME) {
            // new game
            setShowingNewGameDialog(state)
        } else {
            // quit game
            setShowingQuitGameDialog(state)
        }
    }

    fun setShowingSureSaveDialog(isShowingSureSaveDialog: Boolean) {
        mGameProp.isShowingSureSaveDialog = isShowingSureSaveDialog
    }

    fun setShowingSureLoadDialog(isShowingSureLoadDialog: Boolean) {
        mGameProp.isShowingSureLoadDialog = isShowingSureLoadDialog
    }

    fun setShowingGameOverDialog(isShowingGameOverDialog: Boolean) {
        mGameProp.isShowingGameOverDialog = isShowingGameOverDialog
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
        stopBouncyAnimation()
        mGameAction = Constants.IS_CREATING_GAME
        setSaveScoreTitle(saveScoreStr)
    }

    fun quitGame() {
        // quiting the game
        stopBouncyAnimation()
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

    fun readNumberOfSaved(): Int {
        Log.d(TAG, "readNumberOfSaved")
        var numOfSaved = 0
        try {
            val fiStream = mPresenter.fileInputStream(NUM_SAVE_FILENAME)
            numOfSaved = fiStream.read()
            fiStream.close()
        } catch (ex: IOException) {
            Log.d(TAG, "readNumberOfSaved.IOException")
            ex.printStackTrace()
        }
        return numOfSaved
    }

    fun startSavingGame(num: Int): Boolean {
        Log.d(TAG, "startSavingGame")
        SmileApp.isProcessingJob = true
        screenMessage.value = savingGameStr

        var numOfSaved = num
        var succeeded = true
        try {
            var foStream = mPresenter.fileOutputStream(SAVE_FILENAME)
            // save settings
            Log.d(TAG, "startSavingGame.hasSound = " + mGameProp.hasSound)
            if (mGameProp.hasSound) foStream.write(1) else foStream.write(0)
            Log.d(TAG, "startSavingGame.isEasyLevel = " + mGameProp.isEasyLevel)
            if (mGameProp.isEasyLevel) foStream.write(1) else foStream.write(0)
            Log.d(TAG, "startSavingGame.hasNextBall = " + mGameProp.hasNextBall)
            if (mGameProp.hasNextBall) foStream.write(1) else foStream.write(0)
            // save next balls
            // foStream.write(gridData.ballNumOneTime);
            Log.d(TAG, "startSavingGame.ballNumOneTime = " + Constants.BALL_NUM_ONE_TIME)
            foStream.write(Constants.BALL_NUM_ONE_TIME)
            // save values on 9x9 grid
            for (i in 0 until Constants.ROW_COUNTS) {
                for (j in 0 until Constants.COLUMN_COUNTS) {
                    Log.d(
                        TAG,"startSavingGame.gridData.getCellValue(i, j) = "
                            + mGridData.getCellValue(i, j))
                    foStream.write(mGridData.getCellValue(i, j))
                }
            }
            // save current score
            val scoreByte = ByteBuffer.allocate(4).putInt(mGameProp.currentScore).array()
            Log.d(TAG, "startSavingGame.scoreByte = $scoreByte")
            foStream.write(scoreByte)
            // save undoEnable
            Log.d(TAG, "startSavingGame.isUndoEnable = " + mGameProp.undoEnable)
            // can undo or no undo
            if (mGameProp.undoEnable) foStream.write(1) else foStream.write(0)
            Log.d(TAG, "startSavingGame.ballNumOneTime = " + Constants.BALL_NUM_ONE_TIME)
            foStream.write(Constants.BALL_NUM_ONE_TIME)
            // save backupCells
            for (i in 0 until Constants.ROW_COUNTS) {
                for (j in 0 until Constants.COLUMN_COUNTS) {
                    Log.d(
                        TAG,"startSavingGame.gridData.getBackupCells()[i][j] = "
                            + mGridData.getBackupCells()[i][j])
                    foStream.write(mGridData.getBackupCells()[i][j])
                }
            }
            val undoScoreByte = ByteBuffer.allocate(4).putInt(mGameProp.undoScore).array()
            Log.d(TAG, "startSavingGame.undoScoreByte = $undoScoreByte")
            foStream.write(undoScoreByte)
            foStream.close()
            // end of writing
            numOfSaved++
            // save numOfSaved back to file (ColorBallsApp.NumOfSavedGameFileName)
            Log.d(TAG, "startSavingGame.creating fileOutputStream.")
            foStream = mPresenter.fileOutputStream(NUM_SAVE_FILENAME)
            foStream.write(numOfSaved)
            foStream.close()
            Log.d(TAG, "startSavingGame.Succeeded.")
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
            Log.d(TAG, "startSavingGame.Failed.")
        }

        SmileApp.isProcessingJob = false
        // presentView.dismissShowMessageOnScreen()
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
        val isEasyLevel: Boolean
        val hasNextBall: Boolean
        var ballNumOneTime: Int
        val nextBalls = IntArray(Constants.NUM_DIFFICULT)
        val gameCells = Array(Constants.ROW_COUNTS) {
            IntArray(Constants.COLUMN_COUNTS) }
        val cScore: Int
        val isUndoEnable: Boolean
        val undoNextBalls = IntArray(Constants.NUM_DIFFICULT)
        val backupCells = Array(Constants.ROW_COUNTS) {
            IntArray(Constants.COLUMN_COUNTS) }
        var unScore = mGameProp.undoScore
        try {
            // clear nextCellIndices and undoNextCellIndices
            Log.d(TAG, "startLoadingGame.Creating inputFile")
            // File inputFile = new File(mContext.getFilesDir(), savedGameFileName);
            // long fileSizeInByte = inputFile.length();
            // Log.d(TAG, "startLoadingGame.File size = " + fileSizeInByte);
            // FileInputStream fiStream = new FileInputStream(inputFile);
            val fiStream = mPresenter.fileInputStream(SAVE_FILENAME)
            Log.d(TAG, "startLoadingGame.available() = " + fiStream.available())
            Log.d(TAG, "startLoadingGame.getChannel().size() = " + fiStream.channel.size())
            // game sound
            var bValue = fiStream.read()
            hasSound = bValue == 1
            Log.d(TAG, "startLoadingGame.hasSound = $hasSound")
            // game level
            bValue = fiStream.read()
            isEasyLevel = bValue == 1
            Log.d(TAG, "startLoadingGame.isEasyLevel = $isEasyLevel")
            // next balls
            bValue = fiStream.read()
            hasNextBall = bValue == 1
            Log.d(TAG, "startLoadingGame.hasNextBall = $hasNextBall")
            ballNumOneTime = fiStream.read()
            Log.i(TAG, "startLoadingGame.ballNumOneTime = $ballNumOneTime")
            for (i in 0 until Constants.NUM_DIFFICULT) {
                nextBalls[i] = fiStream.read()
                Log.d(TAG, "startLoadingGame.nextCellIndices.cell.getColor() = " + nextBalls[i])
            }
            val nextCellIndicesSize = fiStream.read()
            Log.d(TAG, "startLoadingGame.getNextCellIndices.size() = $nextCellIndicesSize")
            for (i in 0 until nextCellIndicesSize) {
                val x = fiStream.read()
                val y = fiStream.read()
                Log.d(TAG, "startLoadingGame.nextCellIndices.getKey().x = $x")
                Log.d(TAG, "startLoadingGame.nextCellIndices.getKey().y = $y")
            }
            val undoNextCellIndicesSize = fiStream.read()
            Log.d(
                TAG,"startLoadingGame.getUndoNextCellIndices.size() = " +
                    "$undoNextCellIndicesSize")
            for (i in 0 until undoNextCellIndicesSize) {
                val x = fiStream.read()
                val y = fiStream.read()
                Log.d(TAG, "startLoadingGame.undoNextCellIndices.getKey().x = $x")
                Log.d(TAG, "startLoadingGame.undoNextCellIndices.geyKey().y = $y")
            }
            // load values on 9x9 grid
            for (i in 0 until Constants.ROW_COUNTS) {
                for (j in 0 until Constants.COLUMN_COUNTS) {
                    gameCells[i][j] = fiStream.read()
                    Log.d(TAG, "startLoadingGame.gridData.getCellValue(i, j) = " + gameCells[i][j])
                }
            }
            // reading current score
            val scoreByte = ByteArray(4)
            fiStream.read(scoreByte)
            Log.d(TAG, "startLoadingGame.scoreByte = $scoreByte")
            cScore = ByteBuffer.wrap(scoreByte).getInt()
            // reading undoEnable
            bValue = fiStream.read()
            isUndoEnable = bValue == 1
            Log.d(TAG, "startLoadingGame.isUndoEnable = $isUndoEnable")
            if (isUndoEnable) {
                ballNumOneTime = fiStream.read()
                Log.d(TAG, "startLoadingGame.ballNumOneTime = $ballNumOneTime")
                for (i in 0 until Constants.NUM_DIFFICULT) {
                    undoNextBalls[i] = fiStream.read()
                    Log.d(
                        TAG,"startLoadingGame.undoNextCellIndices.getValue() = "
                            + undoNextBalls[i])
                }
                // save backupCells
                for (i in 0 until Constants.ROW_COUNTS) {
                    for (j in 0 until Constants.COLUMN_COUNTS) {
                        backupCells[i][j] = fiStream.read()
                        Log.d(
                            TAG,"startLoadingGame.gridData.getBackupCells()[i][j] = "
                                + backupCells[i][j])
                    }
                }
                val undoScoreByte = ByteArray(4)
                fiStream.read(undoScoreByte)
                Log.d(TAG, "startLoadingGame.undoScoreByte = $undoScoreByte")
                unScore = ByteBuffer.wrap(undoScoreByte).getInt()
            }
            fiStream.close()
            // refresh Main UI with loaded data
            setHasSound(hasSound)
            mGridData.setCellValues(gameCells)
            mGameProp.currentScore = cScore
            mGameProp.undoEnable = isUndoEnable
            mGridData.setBackupCells(backupCells)
            mGameProp.undoScore = unScore
            // start update UI
            currentScore.intValue = mGameProp.currentScore
            displayGameGridView()
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
        }
        SmileApp.isProcessingJob = false
        // presentView.dismissShowMessageOnScreen()
        screenMessage.value = ""

        return succeeded
    }

    fun release() {
        stopBouncyAnimation()
        showingScoreHandler.removeCallbacksAndMessages(null)
        movingBallHandler.removeCallbacksAndMessages(null)
        soundPool.release()
    }

    private fun gameOver() {
        // setGameOverText(gameOverStr)
        Log.d(TAG, "gameOver")
        soundPool.playSound()
        newGame()
    }

    private fun calculateScore(linkedLine: HashSet<Point>?): Int {
        if (linkedLine == null) {
            return 0
        }
        val numBalls = intArrayOf(0, 0, 0, 0, 0, 0)
        for (point in linkedLine) {
            when (mGridData.getCellValue(point.x, point.y)) {
                Constants.COLOR_RED -> numBalls[0]++
                Constants.COLOR_GREEN -> numBalls[1]++
                Constants.COLOR_BLUE -> numBalls[2]++
                Constants.COLOR_MAGENTA -> numBalls[3]++
                Constants.COLOR_YELLOW -> numBalls[4]++
                Constants.COLOR_CYAN -> numBalls[5]++
                else -> {}
            }
        }
        // 5 balls --> 5
        // 6 balls --> 5 + (6-5)*2
        // 7 balls --> 5 + (6-5)*2 + (7-5)*2
        // 8 balls --> 5 + (6-5)*2 + (7-5)*2 + (8-5)*2
        // n balls --> 5 + (6-5)*2 + (7-5)*2 + ... + (n-5)*2
        val minScore = 5
        var totalScore = 0
        for (numBall in numBalls) {
            if (numBall >= 5) {
                var score = minScore
                val extraBalls = numBall - minScore
                if (extraBalls > 0) {
                    // greater than 5 balls
                    val rate = 2
                    for (i in 1..extraBalls) {
                        // rate = 2;   // added on 2018-10-02
                        score += i * rate
                    }
                }
                totalScore += score
            }
        }
        if (!mGameProp.isEasyLevel) {
            // difficult level
            totalScore *= 2 // double of easy level
        }

        return totalScore
    }

    private fun drawBouncyBall(i: Int, j: Int) {
        val color = mGridData.getCellValue(i, j)
        Log.d(TAG, "drawBouncyBall.($i, $j), color = $color")
        var whichBall= WhichBall.BALL
        object : Runnable {
            override fun run() {
                if (whichBall == WhichBall.BALL) drawBall(i, j, color)
                else drawOval(i, j, color)
                whichBall = if (whichBall == WhichBall.BALL) WhichBall.OVAL_BALL
                else WhichBall.BALL
                bouncyBallHandler.postDelayed(this, 200)
            }
        }.also { bouncyBallHandler.post(it) }
    }

    private fun stopBouncyAnimation() {
        bouncyBallHandler.removeCallbacksAndMessages(null)
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
        private const val NUM_SAVE_FILENAME = "NumSavedGame"
        private const val SAVE_FILENAME = "SavedGame"
    }
}