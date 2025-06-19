package com.smile.ballsremover.views

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.nativead.NativeAd
import com.smile.ballsremover.BuildConfig
import com.smile.ballsremover.Composables
import com.smile.ballsremover.R
import com.smile.ballsremover.SmileApp
import com.smile.ballsremover.constants.Constants
import com.smile.ballsremover.interfaces.MainPresentView
import com.smile.ballsremover.presenters.MainPresenter
import com.smile.ballsremover.viewmodels.MainViewModel
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

abstract class MyMainView: ComponentActivity(), MainPresentView {

    protected val viewModel: MainViewModel by viewModels()
    protected var textFontSize = 0f
    protected var boxImage: Bitmap? = null
    protected val colorBallMap: HashMap<Int, Bitmap> = HashMap()
    protected val colorOvalBallMap: HashMap<Int, Bitmap> = HashMap()
    protected val colorNextBallMap: HashMap<Int, Bitmap> = HashMap()

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

        /*
        (application as SmileApp).let {
            interstitialAd = ShowInterstitial(this, it.facebookAds,
                it.googleInterstitialAd)
        }
        */

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
        val nextBallWidth = (sizePx * 0.5f).toInt()
        val nextBallHeight = (sizePx * 0.5f).toInt()
        val ovalBallWidth = (sizePx * 0.9f).toInt()
        val ovalBallHeight = (sizePx * 0.7f).toInt()

        BitmapFactory.decodeResource(resources, R.drawable.box_image).let { bm ->
            boxImage = createScaledBitmap(bm, ballWidth, ballHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.redball)?.let { bm ->
            colorBallMap[Constants.COLOR_RED] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_RED] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_RED] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.greenball)?.let { bm ->
            colorBallMap[Constants.COLOR_GREEN] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_GREEN] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_GREEN] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.blueball)?.let { bm ->
            colorBallMap[Constants.COLOR_BLUE] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_BLUE] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_BLUE] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.magentaball)?.let { bm ->
            colorBallMap[Constants.COLOR_MAGENTA] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_MAGENTA] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_MAGENTA] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.yellowball)?.let { bm ->
            colorBallMap[Constants.COLOR_YELLOW] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_YELLOW] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_YELLOW] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.cyanball)?.let { bm ->
            colorBallMap[Constants.COLOR_CYAN] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_CYAN] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_CYAN] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
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
        viewModel.setSaveScoreAlertDialogState(false)
        SmileApp.isProcessingJob = false
    }

    @Composable
    fun SaveGameDialog() {
        Log.d(TAG, "SaveGameDialog")
        val dialogText = viewModel.saveGameText.value
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListener {
                override fun buttonOkClick() {
                    val numOfSaved: Int = viewModel.readNumberOfSaved()
                    val msg = if (viewModel.startSavingGame(numOfSaved)) {
                            getString(R.string.succeededSaveGameStr)
                        } else {
                            getString(R.string.failedSaveGameStr)
                        }
                    ScreenUtil.showToast(this@MyMainView, msg, textFontSize,
                        ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
                    viewModel.setShowingSureSaveDialog(false)
                    viewModel.setSaveGameText("")
                    showInterstitialAd()
                }
                override fun buttonCancelClick() {
                    viewModel.setShowingSureSaveDialog(false)
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
        val dialogText = viewModel.loadGameText.value
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
                    viewModel.setShowingSureLoadDialog(false)
                    viewModel.setLoadGameText("")
                    showInterstitialAd()
                }
                override fun buttonCancelClick() {
                    viewModel.setShowingSureLoadDialog(false)
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
    fun GameOverDialog() {
        Log.d(TAG, "GameOverDialog")
        val dialogText = viewModel.gameOverText.value
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListener {
                override fun buttonOkClick() {
                    viewModel.newGame()
                    viewModel.setShowingGameOverDialog(false)
                    viewModel.setGameOverText("")
                }
                override fun buttonCancelClick() {
                    viewModel.quitGame()
                    viewModel.setShowingGameOverDialog(false)
                    viewModel.setGameOverText("")
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
        val dialogTitle = viewModel.saveScoreTitle.value
        if (dialogTitle.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListenerString {
                override fun buttonOkClick(value: String?) {
                    Log.d(TAG, "SaveScoreDialog.buttonOkClick.value = $value")
                    viewModel.saveScore(value ?: "No Name")
                    quitOrNewGame()
                    // set SaveScoreDialog() invisible
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
        }
    }

    /*
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
    fun ShowAdmobAdaptiveBanner(modifier: Modifier = Modifier, width: Int) {
        val adId = SmileApp.googleAdMobBannerID2
        Log.d(TAG, "ShowAdmobAdaptiveBanner.adId = $adId")
        ShowAdmobBanner(modifier = modifier, adId = adId, width = width)
    }
    */

    @Composable
    fun MyNativeAdView(
        modifier: Modifier = Modifier,
        ad: NativeAd,
        adContent: @Composable (ad: NativeAd, view: View) -> Unit,) {
        Log.d(TAG, "MyNativeAdView")
        /*
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
                adView.background = ColorDrawable(-0x1)
                adView.callToActionView = contentView
                contentView.setContent { adContent(ad, contentView) }
            }
        )
        */
    }

    /*
    @Composable
    fun ShowFacebookBanner(modifier: Modifier = Modifier, adId: String) {
        // val adId = ColorBallsApp.facebookBannerID
        val pId = if (BuildConfig.DEBUG) "IMG_16_9_APP_INSTALL#$adId" else adId
        Log.d(TAG, "ShowFacebookBanner.adId = $adId")
        AndroidView(
            modifier = modifier,
            factory = { context ->
                com.facebook.ads.AdView(context, pId,
                    com.facebook.ads.AdSize.BANNER_HEIGHT_50)
            },
            update = { faceAdView ->
                FacebookBanner(faceAdView)
            }
        )
    }
    */

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

    override fun getLoadingStr() = getString(R.string.loadingStr)

    override fun geSavingGameStr() = getString(R.string.savingGameStr)

    override fun getLoadingGameStr() = getString(R.string.loadingGameStr)

    override fun getSureToSaveGameStr() = getString(R.string.sureToSaveGameStr)

    override fun getSureToLoadGameStr() = getString(R.string.sureToLoadGameStr)

    override fun getGameOverStr() = getString(R.string.gameOverStr)

    override fun getSaveScoreStr() = getString(R.string.saveScoreStr)

    override fun soundPool(): SoundPoolUtil {
        return SoundPoolUtil(this, R.raw.uhoh)
    }

    override fun getHighestScore() : Int {
        Log.d(TAG, "getHighestScore")
        val scoreSQLiteDB = ScoreSQLite(this)
        val score = scoreSQLiteDB.readHighestScore()
        scoreSQLiteDB.close()
        return score
    }

    override fun addScoreInLocalTop10(playerName : String, score : Int) {
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