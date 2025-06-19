package com.smile.ballsremover.views

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import com.smile.ballsremover.Composables
import com.smile.ballsremover.R
import com.smile.ballsremover.constants.Constants
import com.smile.ballsremover.models.TopPlayer
import com.smile.ballsremover.ui.theme.BallsRemoverTheme
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Top10Activity : ComponentActivity() {

    private val top10Players = mutableStateOf(listOf<TopPlayer>())
    private var top10TitleName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        var isLocal = false // default is Global top10
        intent.extras?.let {
            isLocal = it.getBoolean(Constants.IS_LOCAL_TOP10, false)
        }
        top10TitleName =
            if (isLocal) getString(R.string.localTop10Score) else
                getString(R.string.globalTop10Str)
        val medalImageIds = listOf(
            R.drawable.gold_medal,
            R.drawable.silver_medal,
            R.drawable.bronze_medal,
            R.drawable.copper_medal,
            R.drawable.olympics_image,
            R.drawable.olympics_image,
            R.drawable.olympics_image,
            R.drawable.olympics_image,
            R.drawable.olympics_image,
            R.drawable.olympics_image
        )

        setContent {
            Log.d(TAG, "onCreate.setContent.Composables.Top10Compose")
            BallsRemoverTheme {
                LaunchedEffect(Unit) {
                    Log.d(TAG, "onCreate.setContent.LaunchedEffect")
                    launch(Dispatchers.IO) {
                        val players = if (isLocal) {
                            PlayerRecordRest.GetLocalTop10(ScoreSQLite(
                                this@Top10Activity))
                        } else {
                            PlayerRecordRest.GetGlobalTop10(Constants.GAME_ID)
                        }
                        val top10 = ArrayList<TopPlayer>()
                        for (i in 0 until players.size) {
                            players[i].playerName?.let { name ->
                                if (name.trim().isEmpty()) players[i].playerName = "No Name"
                            } ?: run {
                                Log.d(TAG, "onCreate.players[i].playerName = null")
                                players[i].playerName = "No Name"
                            }
                            top10.add(TopPlayer(players[i], medalImageIds[i]))
                        }
                        top10Players.value = top10
                    }
                }
                Log.d(TAG, "onCreate.setContent.Composables.Top10Compose")
                Composables.Top10Composable(
                    title = top10TitleName,
                    topPlayers = top10Players.value, buttonListener =
                    object : Composables.ButtonClickListener {
                        override fun buttonOkClick() {
                            Log.d(TAG, "Composables.OkButtonListener.buttonOkClick")
                            setResult(RESULT_OK)
                            finish()
                        }
                    },
                    getString(R.string.okStr)
                )
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                setResult(RESULT_OK)
                finish()
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

    companion object {
        private const val TAG = "Top10ComposeActivity"
    }
}