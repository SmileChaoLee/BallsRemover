package com.smile.ballsremover

import android.content.res.Resources
import android.util.Log
import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.MobileAds
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial

class SmileApp : MultiDexApplication() {
    @JvmField
    var googleInterstitialAd: AdMobInterstitial? = null
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        mResources = resources
        isProcessingJob = false
        val googleAdMobInterstitialID = "ca-app-pub-8354869049759576/6690798717"
        MobileAds.initialize(applicationContext) {
            Log.d(TAG, "Google Banner ads was initialized successfully")
        }
        googleInterstitialAd = AdMobInterstitial(applicationContext,
            googleAdMobInterstitialID)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(TAG, "onTrimMemory.level = $level")
    }

    companion object {
        private const val TAG = "SmileApp"
        var isProcessingJob = false
        const val googleAdMobBannerID = "ca-app-pub-8354869049759576/7152164841"
        const val googleAdMobNativeID = "ca-app-pub-8354869049759576/3429645905"
        lateinit var mResources: Resources
    }
}