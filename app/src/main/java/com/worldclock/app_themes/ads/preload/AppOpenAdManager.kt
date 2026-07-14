package com.worldclock.app_themes.ads.preload

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.Utils
import com.worldclock.app_themes.core.analytics.AppEventLogger
import java.util.Date

enum class AppOpenScreen {
    SPLASH,
    BACKGROUND
}

class AppOpenAdManager {

    companion object{
        var adSplash: AppOpenAd? = null
        var adBackground: AppOpenAd? = null
    }


    private var isLoadingSplash = false
    private var isLoadingBackground = false
    var isShowingAd = false
        private set

    private var loadTimeSplash: Long = -1L
    private var loadTimeBackground: Long = -1L

    private var lastShownTime: Long = 0



    private val AD_EXPIRY_HOURS = 4L

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    private fun getAd(screen: AppOpenScreen): AppOpenAd? {
        return when (screen) {
            AppOpenScreen.SPLASH -> adSplash
            AppOpenScreen.BACKGROUND -> adBackground
        }
    }

    private fun setAd(screen: AppOpenScreen, ad: AppOpenAd?) {
        when (screen) {
            AppOpenScreen.SPLASH -> adSplash = ad
            AppOpenScreen.BACKGROUND -> adBackground = ad
        }
    }

    private fun isLoading(screen: AppOpenScreen): Boolean {
        return when (screen) {
            AppOpenScreen.SPLASH -> isLoadingSplash
            AppOpenScreen.BACKGROUND -> isLoadingBackground
        }
    }

    private fun setLoading(screen: AppOpenScreen, loading: Boolean) {
        when (screen) {
            AppOpenScreen.SPLASH -> isLoadingSplash = loading
            AppOpenScreen.BACKGROUND -> isLoadingBackground = loading
        }
    }

    private fun getLoadTime(screen: AppOpenScreen): Long {
        return when (screen) {
            AppOpenScreen.SPLASH -> loadTimeSplash
            AppOpenScreen.BACKGROUND -> loadTimeBackground
        }
    }

    private fun setLoadTime(screen: AppOpenScreen, time: Long) {
        when (screen) {
            AppOpenScreen.SPLASH -> loadTimeSplash = time
            AppOpenScreen.BACKGROUND -> loadTimeBackground = time
        }
    }

    private fun getAdId(screen: AppOpenScreen): String {
        return when (screen) {
            AppOpenScreen.SPLASH -> GetFirebase.adIdSplash_appopen
            AppOpenScreen.BACKGROUND -> GetFirebase.adIdBackground_appopen
        }
    }

    private fun setAdId(screen: AppOpenScreen, id: String) {
        when (screen) {
            AppOpenScreen.SPLASH -> GetFirebase.adIdSplash_appopen
            AppOpenScreen.BACKGROUND -> GetFirebase.adIdBackground_appopen
        }
    }


    private fun isAdFresh(screen: AppOpenScreen): Boolean {
        val loadTime = getLoadTime(screen)
        if (loadTime == -1L) return false // never loaded

        val elapsed = Date().time - loadTime

        return elapsed < AD_EXPIRY_HOURS * 60 * 60 * 1000
    }

    private fun isAdAvailable(screen: AppOpenScreen): Boolean {
        return getAd(screen) != null
    }

    // ══════════════════════════════════════
    // LOADING
    // ══════════════════════════════════════

    fun load(context: Context, adId: String, screen: AppOpenScreen) {

        if (!GetFirebase.enable_appopen_ads){
            return
        }

        if (isLoading(screen) || isAdAvailable(screen)) return
        setAdId(screen, adId)
        setLoading(screen, true)

        AppOpenAd.load(
            context,
            adId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Utils.showMessage(context, "App open loaded")
                    setAd(screen, ad)
                    setLoading(screen, false)
                    setLoadTime(screen, Date().time)

                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Utils.showMessage(context, "App open failed to load")

                    setAd(screen, null)
                    setLoading(screen, false)

                }
            }
        )
    }

    fun loadSplash(context: Context, adId: String) = load(context, adId, AppOpenScreen.SPLASH)
    fun loadBackground(context: Context, adId: String) = load(context, adId, AppOpenScreen.BACKGROUND)

    // ══════════════════════════════════════
    // CONDITION CHECK
    // ══════════════════════════════════════

    private fun canShow(
        activity: Activity,
        screen: AppOpenScreen,
        isEnabled: Boolean,
        isPremium: Boolean,
        adsEnabled: Boolean
    ): Boolean {

        if (isPremium || !adsEnabled) return false

        if (screen == AppOpenScreen.BACKGROUND){
            load(activity, GetFirebase.adIdBackground_appopen, AppOpenScreen.BACKGROUND)

        }
        else{
            load(activity, GetFirebase.adIdSplash_appopen, AppOpenScreen.SPLASH)

        }

        if (!isEnabled) return false
        if (isShowingAd) return false
        if (!isAdAvailable(screen)) return false

        return true
    }

    // ══════════════════════════════════════
    // SHOWING
    // ══════════════════════════════════════

    fun showIfAvailable(
        activity: Activity,
        screen: AppOpenScreen,
        isEnabled: Boolean = true,
        isPremium: Boolean = false,
        adsEnabled: Boolean = true,
        onDismiss: () -> Unit,
        onFailed: () -> Unit
    ) {



        if (!canShow(activity,screen, isEnabled, isPremium, adsEnabled)) {
            onFailed()
            return
        }

        val ad = getAd(screen) ?: run {
            if (screen == AppOpenScreen.BACKGROUND){
                load(activity, GetFirebase.adIdBackground_appopen, AppOpenScreen.BACKGROUND)

            }
            else{
                load(activity, GetFirebase.adIdSplash_appopen, AppOpenScreen.SPLASH)

            }
            onFailed()
            return
        }

        val adId = getAdId(screen)

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Utils.showMessage(activity, "App open showed")

                isShowingAd = true
            }

            override fun onAdDismissedFullScreenContent() {
                isShowingAd = false
                lastShownTime = System.currentTimeMillis()
                setAd(screen, null)
                load(activity, adId, screen)
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Utils.showMessage(activity, "App open failed to")

                isShowingAd = false
                setAd(screen, null)
                load(activity, adId, screen)

                onFailed()
            }

            override fun onAdClicked() {
                super.onAdClicked()
            }
        }

        ad.setOnPaidEventListener { adValue ->
            // Then pass it
            AppEventLogger.logCustomImpressions(
                activity, // 'this' in Activity
                adValue = adValue,
                adUnitId = ad.adUnitId,
                adFormat = "app open"
            )
        }

        isShowingAd = true
        ad.show(activity)
    }

    // ══════════════════════════════════════
    // RESET
    // ══════════════════════════════════════

    fun reset() {
        adSplash = null
        adBackground = null
        isLoadingSplash = false
        isLoadingBackground = false
        isShowingAd = false
    }

    fun resetScreen(screen: AppOpenScreen) {
        setAd(screen, null)
        setLoading(screen, false)
    }
}