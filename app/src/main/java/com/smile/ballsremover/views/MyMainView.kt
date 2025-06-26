package com.smile.ballsremover.views

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.smile.ballsremover.BuildConfig
import com.smile.ballsremover.Composables
import com.smile.ballsremover.R
import com.smile.ballsremover.SmileApp
import com.smile.ballsremover.constants.Constants
import com.smile.ballsremover.interfaces.MainPresentView
import com.smile.ballsremover.presenters.MainPresenter
import com.smile.ballsremover.viewmodels.MainViewModel
import com.smile.smilelibraries.AdMobBanner
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.core.graphics.scale
import androidx.core.graphics.drawable.toDrawable

abstract class MyMainView: ComponentActivity(), MainPresentView {

    protected val viewModel: MainViewModel by viewModels()
    protected var textFontSize = 0f
    protected var boxImage: Bitmap? = null
    protected val colorBallMap: HashMap<Int, Bitmap> = HashMap()
    protected val colorOvalBallMap: HashMap<Int, Bitmap> = HashMap()

    private var interstitialAd: ShowInterstitial? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!BuildConfig.DEBUG) {
            requestedOrientation = if (ScreenUtil.isTablet(this@MyMainView)) {
                // Table then change orientation to Landscape
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                // phone then change orientation to Portrait
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
        super.onCreate(savedInstanceState)

        Log.d(TAG, "$TAG.onCreate")
        Log.d(TAG, "onCreate.textFontSize")
        textFontSize = ScreenUtil.suitableFontSize(
            this, ScreenUtil.getDefaultTextSizeFromTheme(this,
                ScreenUtil.FontSize_Pixel_Type, null),
            ScreenUtil.FontSize_Pixel_Type,
            0.0f)
        Composables.mFontSize = ScreenUtil.pixelToDp(textFontSize).sp

        Log.d(TAG, "onCreate.interstitialAd")

        (application as SmileApp).let {
            interstitialAd = ShowInterstitial(this, null,
                it.googleInterstitialAd)
        }

        Log.d(TAG, "onCreate.instantiate MainPresenter")
        viewModel.setPresenter(MainPresenter(this@MyMainView))
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        viewModel.release()
        interstitialAd?.releaseInterstitial()
    }

    protected fun showInterstitialAd() {
        Log.d(TAG, "showInterstitialAd = $interstitialAd")
        interstitialAd?.ShowAdThread()?.startShowAd(0) // AdMob first
    }

    protected fun bitmapDrawableResources(sizePx: Float) {
        Log.w(TAG, "bitmapDrawableResources.sizePx = $sizePx")
        val ballWidth = sizePx.toInt()
        val ballHeight = sizePx.toInt()
        val ovalBallWidth = (sizePx * 0.9f).toInt()
        val ovalBallHeight = (sizePx * 0.7f).toInt()

        BitmapFactory.decodeResource(resources, R.drawable.box_image).let { bm ->
            // boxImage = createScaledBitmap(bm, ballWidth, ballHeight, true)
            boxImage = bm.scale(ballWidth, ballHeight)
        }
        Log.d(TAG, "bitmapDrawableResources.boxImage.height = ${boxImage?.height}")
        Log.d(TAG, "bitmapDrawableResources.boxImage.height.toDp " +
                "= ${ScreenUtil.pixelToDp(boxImage?.height!!.toFloat())}")

        BitmapFactory.decodeResource(resources, R.drawable.redball)?.let { bm ->
            colorBallMap[Constants.COLOR_RED] =
                bm.scale(ballWidth, ballHeight)
            colorOvalBallMap[Constants.COLOR_RED] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.greenball)?.let { bm ->
            colorBallMap[Constants.COLOR_GREEN] =
                bm.scale(ballWidth, ballHeight)
            colorOvalBallMap[Constants.COLOR_GREEN] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.blueball)?.let { bm ->
            colorBallMap[Constants.COLOR_BLUE] =
                bm.scale(ballWidth, ballHeight)
            colorOvalBallMap[Constants.COLOR_BLUE] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.magentaball)?.let { bm ->
            colorBallMap[Constants.COLOR_MAGENTA] =
                bm.scale(ballWidth, ballHeight)
            colorOvalBallMap[Constants.COLOR_MAGENTA] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.yellowball)?.let { bm ->
            colorBallMap[Constants.COLOR_YELLOW] =
                bm.scale(ballWidth, ballHeight)
            colorOvalBallMap[Constants.COLOR_YELLOW] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.cyanball)?.let { bm ->
            colorBallMap[Constants.COLOR_CYAN] =
                bm.scale(ballWidth, ballHeight)
            colorOvalBallMap[Constants.COLOR_CYAN] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }
    }

    private fun exitApplication() {
        val handlerClose = Handler(Looper.getMainLooper())
        val timeDelay = 200
        // exit application
        handlerClose.postDelayed({ this.finish() }, timeDelay.toLong())
    }

    private fun quitOrNewGame() {
        if (viewModel.mGameAction == Constants.IS_QUITING_GAME) {
            //  END PROGRAM
            exitApplication()
        } else if (viewModel.mGameAction == Constants.IS_CREATING_GAME) {
            //  NEW GAME
            viewModel.initGame(null)
        }
    }

    @Composable
    fun CreateNewGameDialog() {
        Log.d(TAG, "CreateNewGameDialog")
        val dialogText = viewModel.getCreateNewGameText()
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListener {
                override fun buttonOkClick() {
                    viewModel.setCreateNewGameText("")
                    quitOrNewGame()
                }
                override fun buttonCancelClick() {
                    viewModel.setCreateNewGameText("")
                }
            }
            Composables.DialogWithText(
                this@MyMainView,
                buttonListener, "", dialogText
            )
        }
    }

    @Composable
    fun SaveGameDialog() {
        Log.d(TAG, "SaveGameDialog")
        val dialogText = viewModel.getSaveGameText()
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListener {
                override fun buttonOkClick() {
                    val msg = if (viewModel.startSavingGame()) {
                            getString(R.string.succeededSaveGameStr)
                        } else {
                            getString(R.string.failedSaveGameStr)
                        }
                    ScreenUtil.showToast(this@MyMainView, msg, textFontSize,
                        ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
                    viewModel.setSaveGameText("")
                    showInterstitialAd()
                }
                override fun buttonCancelClick() {
                    viewModel.setSaveGameText("")
                }
            }
            Composables.DialogWithText(
                this@MyMainView,
                buttonListener, "", dialogText
            )
        }
    }

    @Composable
    fun LoadGameDialog() {
        Log.d(TAG, "LoadGameDialog")
        val dialogText = viewModel.getLoadGameText()
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListener {
                override fun buttonOkClick() {
                    val msg = if (viewModel.startLoadingGame()) {
                        getString(R.string.succeededLoadGameStr)
                    } else {
                        getString(R.string.failedLoadGameStr)
                    }
                    ScreenUtil.showToast(this@MyMainView, msg, textFontSize,
                        ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
                    viewModel.setLoadGameText("")
                    showInterstitialAd()
                }
                override fun buttonCancelClick() {
                    viewModel.setLoadGameText("")
                }
            }
            Composables.DialogWithText(
                this@MyMainView,
                buttonListener, "", dialogText
            )
        }
    }

    @Composable
    fun SaveScoreDialog() {
        Log.d(TAG, "SaveScoreDialog")
        val dialogTitle = viewModel.getSaveScoreTitle()
        if (dialogTitle.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListenerString {
                override fun buttonOkClick(value: String?) {
                    Log.d(TAG, "SaveScoreDialog.buttonOkClick.value = $value")
                    viewModel.saveScore(value ?: "No Name")
                    quitOrNewGame()
                    viewModel.setSaveScoreTitle("")
                }
                override fun buttonCancelClick(value: String?) {
                    Log.d(TAG, "SaveScoreDialog.buttonCancelClick.value = $value")
                    quitOrNewGame()
                    // set SaveScoreDialog() invisible
                    viewModel.setSaveScoreTitle("")
                }
            }
            val hitStr = getString(R.string.nameStr)
            Composables.DialogWithTextField(
                this@MyMainView,
                buttonListener, dialogTitle, hitStr
            )
        } else {
            if (viewModel.timesPlayed >= Constants.SHOW_ADS_AFTER_TIMES) {
                Log.d(TAG, "SaveScoreDialog.showInterstitialAd")
                showInterstitialAd()
                viewModel.timesPlayed = 0
            }
        }
    }

    @Composable
    fun ShowAdmobBanner(modifier: Modifier = Modifier,
                                adId: String, width: Int = 0) {
        Log.d(TAG, "ShowAdmobBanner.adId = $adId")
        AndroidView(
            modifier = modifier,
            factory = { context ->
                AdView(context)
            },
            update = { adView ->
                AdMobBanner(adView, adId, width)
            }
        )
    }

    @Composable
    fun ShowAdmobNormalBanner(modifier: Modifier = Modifier) {
        val adId = SmileApp.googleAdMobBannerID
        Log.d(TAG, "ShowAdmobNormalBanner.adId = $adId")
        ShowAdmobBanner(modifier = modifier, adId = adId)
    }

    @Composable
    fun MyNativeAdView(
        modifier: Modifier = Modifier,
        ad: NativeAd,
        adContent: @Composable (ad: NativeAd, view: View) -> Unit,
    ) {
        Log.d(TAG, "MyNativeAdView")
        val contentViewId by rememberSaveable { mutableIntStateOf(View.generateViewId()) }
        val adViewId by rememberSaveable { mutableIntStateOf(View.generateViewId()) }
        Log.d(TAG, "MyNativeAdView.AndroidView")
        AndroidView(
            modifier = modifier,
            factory = { context ->
                Log.d(TAG, "MyNativeAdView.AndroidView.factory")
                val contentView = ComposeView(context).apply {
                    id = contentViewId
                }
                NativeAdView(context).apply {
                    id = adViewId
                    addView(contentView)
                }
            },
            update = { nativeAdView ->
                Log.d(TAG, "MyNativeAdView.AndroidView.update")
                val adView = nativeAdView.findViewById<NativeAdView>(adViewId)
                val contentView = nativeAdView.findViewById<ComposeView>(contentViewId)
                Log.d(TAG, "MyNativeAdView.AndroidView.update.setNativeAd()")
                adView.setNativeAd(ad)
                adView.background = (-0x1).toDrawable() // white color
                adView.callToActionView = contentView
                contentView.setContent { adContent(ad, contentView) }
            }
        )
    }

    // implementing MainPresentView
    override fun getMedalImageIds(): List<Int> {
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
        return medalImageIds
    }

    override fun getCreateNewGameStr() = getString(R.string.createNewGameStr)

    override fun getLoadingStr() = getString(R.string.loadingStr)

    override fun geSavingGameStr() = getString(R.string.savingGameStr)

    override fun getLoadingGameStr() = getString(R.string.loadingGameStr)

    override fun getSureToSaveGameStr() = getString(R.string.sureToSaveGameStr)

    override fun getSureToLoadGameStr() = getString(R.string.sureToLoadGameStr)

    override fun getSaveScoreStr() = getString(R.string.saveScoreStr)

    override fun soundPool(): SoundPoolUtil {
        return SoundPoolUtil(this, R.raw.uhoh)
    }

    override fun getHighestScore() : Int {
        Log.d(TAG, "getHighestScore")
        val scoreSQLiteDB = ScoreSQLite(this)
        val score = scoreSQLiteDB.readHighestScore()
        Log.d(TAG, "getHighestScore.score = $score")
        scoreSQLiteDB.close()
        return score
    }

    override fun addScoreInLocalTop10(playerName : String, score : Int) {
        Log.d(TAG, "addScoreInLocalTop10")
        val scoreSQLiteDB = ScoreSQLite(this)
        if (scoreSQLiteDB.isInTop10(score)) {
            // inside top 10, then record the current score
            scoreSQLiteDB.addScore(playerName, score)
            scoreSQLiteDB.deleteAllAfterTop10() // only keep the top 10
        }
        scoreSQLiteDB.close()
    }

    override fun fileInputStream(fileName : String): FileInputStream {
        return FileInputStream(File(filesDir, fileName))
    }

    override fun fileOutputStream(fileName : String): FileOutputStream {
        return FileOutputStream(File(filesDir, fileName))
    }
    // end of implementing


    companion object {
        private const val TAG = "MyMainView"
    }
}