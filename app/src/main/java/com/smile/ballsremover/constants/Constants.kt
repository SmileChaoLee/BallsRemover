package com.smile.ballsremover.constants

object Constants {
    const val GAME_ID = "5"
    const val HAS_SOUND = "HasSound"
    const val GAME_LEVEL = "GameLevel"
    const val FILL_COLUMN = "FillColumn"
    const val NUM_BALLS_USED_EASY = 5
    const val NUM_BALLS_USED_DIFF = 6
    const val EASY_LEVEL = 0
    const val DIFFICULT_LEVEL = 1
    const val IS_LOCAL_TOP10 = "IsLocalTop10"
    // 10->RED, 20->GREEN, 30->BLUE, 40->MAGENTA, 50->YELLOW, 60->Cyan
    const val COLOR_RED = 10
    const val COLOR_GREEN = 20
    const val COLOR_BLUE = 30
    const val COLOR_MAGENTA = 40
    const val COLOR_YELLOW = 50
    const val COLOR_CYAN = 60
    const val BALL_NUM_COMPLETED = 2
    const val ROW_COUNTS = 12
    const val COLUMN_COUNTS = 10
    @JvmField
    val BallColor =
        intArrayOf(COLOR_RED, COLOR_GREEN, COLOR_BLUE, COLOR_MAGENTA, COLOR_YELLOW, COLOR_CYAN)
    const val GAME_PROP_TAG = "GameProp"
    const val GRID_DATA_TAG = "GridData"
    const val IS_CREATING_GAME = 1
    const val IS_QUITING_GAME = 0
    const val SHOW_ADS_AFTER_TIMES = 3
}