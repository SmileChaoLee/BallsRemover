package com.smile.ballsremover.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.graphics.Color
import com.smile.ballsremover.Composables
import com.smile.ballsremover.R
import com.smile.ballsremover.constants.Constants
import com.smile.ballsremover.models.Settings
import com.smile.ballsremover.ui.theme.BallsRemoverTheme
import com.smile.ballsremover.viewmodels.SettingViewModel

class SettingActivity : ComponentActivity() {

    private val settingViewModel : SettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        if (savedInstanceState == null) {
            // new creation of this activity
            Log.d(TAG, "onCreate.savedInstanceState is null")
            val setting = Settings()
            intent.extras?.let {
                setting.hasSound = it.getBoolean(Constants.HAS_SOUND, true)
                setting.gameLevel = it.getInt(Constants.GAME_LEVEL, Constants.EASY_LEVEL)
                setting.fillColumn = it.getBoolean(Constants.FILL_COLUMN, true)
            }
            settingViewModel.setSettings(setting)
        } else {
            // re-creation of this activity
            Log.d(TAG, "onCreate.savedInstanceState not null")
            if (settingViewModel.settings.value == null) {
                settingViewModel.setSettings(Settings())
            }
        }

        val textClick = object : Composables.SettingClickListener {
            override fun hasSoundClick(hasSound: Boolean) {
                Log.d(TAG, "textClick.hasSoundClick.hasSound = $hasSound")
                settingViewModel.setHasSound(hasSound)
            }
            override fun gameLevelClick(gameLevel: Int) {
                Log.d(TAG, "textClick.gameLevelClick.gameLevel = $gameLevel")
                settingViewModel.setGameLevel(gameLevel)
            }
            override fun isFillColumnClick(fillColumn: Boolean) {
                Log.d(TAG, "textClick.isFillColumnClick.hasNext = $fillColumn")
                settingViewModel.setFillColumn(fillColumn)
            }
        }

        val buttonClick = object : Composables.ButtonClickListener {
            override fun buttonOkClick() {
                returnToPrevious(confirmed = true)
            }
            override fun buttonCancelClick() {
                returnToPrevious(confirmed = false)
            }
        }

        setContent {
            Log.d(TAG, "onCreate.setContent")
            BallsRemoverTheme {
                settingViewModel.settings.value?.let {
                    Composables.SettingCompose(
                        this@SettingActivity,
                        buttonClick, textClick,
                        getString(R.string.settingStr),
                        backgroundColor = Color(0xbb0000ff), it
                    )
                }
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                returnToPrevious(confirmed = false)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause()")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState()")
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
    }

    private fun returnToPrevious(confirmed: Boolean) {
        Intent().let { intent ->
            Bundle().let { bundle ->
                settingViewModel.settings.value?.also {
                    bundle.putBoolean(Constants.HAS_SOUND, it.hasSound)
                    bundle.putInt(Constants.GAME_LEVEL, it.gameLevel)
                    bundle.putBoolean(Constants.FILL_COLUMN, it.fillColumn)
                    intent.putExtras(bundle)
                }
            }
            setResult(if (confirmed) RESULT_OK else RESULT_CANCELED,
                intent) // can bundle some data to previous activity
        }
        finish()
    }

    companion object {
        private const val TAG = "SettingComposeActivity"
    }
}