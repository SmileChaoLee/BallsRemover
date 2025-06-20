package com.smile.ballsremover

import android.content.res.Resources
import android.util.Log
import androidx.multidex.MultiDexApplication
import com.smile.smilelibraries.facebook_ads_util.FacebookInterstitial
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial

class SmileApp : MultiDexApplication() {
    @JvmField
    var facebookAds: FacebookInterstitial? = null
    @JvmField
    var googleInterstitialAd: AdMobInterstitial? = null
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        mResources = resources
        isShowingLoadingMessage = false
        val googleAdMobInterstitialID = ""
        var facebookInterstitialID = ""
        facebookBannerID = ""
        facebookBannerID2 = ""
        // Google AdMob
        googleAdMobBannerID = ""
        googleAdMobBannerID2 = ""
        googleAdMobNativeID = ""
        Log.d(TAG, "onCreate.!AudienceNetworkAds.isInitialized")
        /*
        if (!AudienceNetworkAds.isInitialized(this)) {
            if (BuildConfig.DEBUG) {
                AdSettings.turnOnSDKDebugger(this)
            }
            AudienceNetworkAds
                .buildInitSettings(this)
                .withInitListener { initResult: InitResult ->
                    Log.d(TAG, initResult.message)
                }
                .initialize()
        }
        */
        // AudienceNetworkAds.initialize(this);
        var testString = ""
        // for debug mode
        if (BuildConfig.DEBUG) {
            testString = "IMG_16_9_APP_INSTALL#"
        }
        facebookInterstitialID = testString + facebookInterstitialID
        Log.d(TAG, "onCreate.facebookInterstitialID")
        //
        /*
        facebookAds = FacebookInterstitial(applicationContext, facebookInterstitialID)
        MobileAds.initialize(applicationContext) {
            Log.d(TAG, "Google Banner ads was initialized successfully")
        }
        googleInterstitialAd = AdMobInterstitial(applicationContext, googleAdMobInterstitialID)
        */
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(TAG, "onTrimMemory.level = $level")
    }

    companion object {
        private const val TAG = "SmileApp"
        @JvmField
        var isShowingLoadingMessage = false
        @JvmField
        var facebookBannerID = ""
        @JvmField
        var facebookBannerID2 = ""
        @JvmField
        var googleAdMobBannerID = ""
        @JvmField
        var googleAdMobBannerID2 = ""
        @JvmField
        var googleAdMobNativeID = ""
        lateinit var mResources: Resources
    }
}