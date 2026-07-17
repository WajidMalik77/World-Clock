package com.worldclock.app_themes.ads.preload

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.Utils
import com.worldclock.app_themes.core.analytics.AppEventLogger
import com.worldclock.app_themes.databinding.LoadingAdDialogBinding
import java.util.Date

enum class AppOpenScreen {
    SPLASH,
    BACKGROUND
}

class AppOpenAdManager {

    companion object {
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

    private fun getAd(screen: AppOpenScreen): AppOpenAd? = when (screen) {
        AppOpenScreen.SPLASH -> adSplash
        AppOpenScreen.BACKGROUND -> adBackground
    }

    private fun setAd(screen: AppOpenScreen, ad: AppOpenAd?) = when (screen) {
        AppOpenScreen.SPLASH -> adSplash = ad
        AppOpenScreen.BACKGROUND -> adBackground = ad
    }

    private fun isLoading(screen: AppOpenScreen): Boolean = when (screen) {
        AppOpenScreen.SPLASH -> isLoadingSplash
        AppOpenScreen.BACKGROUND -> isLoadingBackground
    }

    private fun setLoading(screen: AppOpenScreen, loading: Boolean) = when (screen) {
        AppOpenScreen.SPLASH -> isLoadingSplash = loading
        AppOpenScreen.BACKGROUND -> isLoadingBackground = loading
    }

    private fun getLoadTime(screen: AppOpenScreen): Long = when (screen) {
        AppOpenScreen.SPLASH -> loadTimeSplash
        AppOpenScreen.BACKGROUND -> loadTimeBackground
    }

    private fun setLoadTime(screen: AppOpenScreen, time: Long) = when (screen) {
        AppOpenScreen.SPLASH -> loadTimeSplash = time
        AppOpenScreen.BACKGROUND -> loadTimeBackground = time
    }

    private fun getAdId(screen: AppOpenScreen): String = when (screen) {
        AppOpenScreen.SPLASH -> GetFirebase.adIdSplash_appopen
        AppOpenScreen.BACKGROUND -> GetFirebase.adIdBackground_appopen
    }

    private fun setAdId(screen: AppOpenScreen, id: String) = when (screen) {
        AppOpenScreen.SPLASH -> GetFirebase.adIdSplash_appopen = id
        AppOpenScreen.BACKGROUND -> GetFirebase.adIdBackground_appopen = id
    }

    private fun isAdFresh(screen: AppOpenScreen): Boolean {
        val loadTime = getLoadTime(screen)
        if (loadTime == -1L) return false
        val elapsed = Date().time - loadTime
        return elapsed < AD_EXPIRY_HOURS * 60 * 60 * 1000
    }

    private fun isAdAvailable(screen: AppOpenScreen): Boolean = getAd(screen) != null

    // ══════════════════════════════════════
    // LOADING (preload)
    // ══════════════════════════════════════

    fun load(context: Context, adId: String, screen: AppOpenScreen) {
        if (!GetFirebase.enable_appopen_ads) return
        if (isLoading(screen) || isAdAvailable(screen)) return
        setAdId(screen, adId)
        setLoading(screen, true)

        AppOpenAd.load(
            context, adId, AdRequest.Builder().build(),
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
        screen: AppOpenScreen,
        isEnabled: Boolean,
        isPremium: Boolean,
        adsEnabled: Boolean
    ): Boolean {
        if (isPremium || !adsEnabled) return false
        if (!isEnabled) return false
        if (isShowingAd) return false
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
        onDemand: Boolean = false,
        onDismiss: () -> Unit,
        onFailed: () -> Unit
    ) {
        if (!GetFirebase.enable_appopen_ads) {
            onFailed()
            return
        }

        if (!canShow(screen, isEnabled, isPremium, adsEnabled)) {
            onFailed()
            return
        }

        if (onDemand) {
            if (isShowingAd) return
            loadAndShow(activity, screen, onDismiss, onFailed)
        } else {
            if (isShowingAd) return
            val ad = getAd(screen) ?: run {
                load(activity, getAdId(screen), screen)
                onFailed()
                return
            }
            showAd(ad, activity, screen, onDismiss, onFailed)
        }
    }

    // ══════════════════════════════════════
    // ON DEMAND — LOAD & SHOW
    // ══════════════════════════════════════

    private fun loadAndShow(
        activity: Activity,
        screen: AppOpenScreen,
        onDismiss: () -> Unit,
        onFailed: () -> Unit
    ) {
        if (isLoading(screen)) {
            onFailed()
            return
        }

        val adId = getAdId(screen)
        setLoading(screen, true)
        var hasResponded = false
        val handler = Handler(Looper.getMainLooper())

        val dialog = createLoadingDialog(activity)

        val timeoutRunnable = Runnable {
            if (!hasResponded) {
                hasResponded = true
                setLoading(screen, false)
                setAd(screen, null)
                dismissDialog(dialog, activity)
                onFailed()
            }
        }

        handler.postDelayed(timeoutRunnable, GetFirebase.time_delay_for_ondemand_appopen.toLong())

        AppOpenAd.load(
            activity, adId, AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    if (hasResponded) return
                    hasResponded = true
                    handler.removeCallbacks(timeoutRunnable)
                    setLoading(screen, false)
                    setAd(screen, ad)
                    setLoadTime(screen, Date().time)

                    if (activity.isFinishing) {
                        dismissDialog(dialog, activity)
                        onFailed()
                        return
                    }

                    dismissDialog(dialog, activity)
                    showAd(ad, activity, screen, onDismiss, onFailed)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (hasResponded) return
                    hasResponded = true
                    handler.removeCallbacks(timeoutRunnable)
                    setLoading(screen, false)
                    setAd(screen, null)
                    dismissDialog(dialog, activity)
                    onFailed()
                }
            }
        )
    }

    private fun createLoadingDialog(activity: Activity): AlertDialog? {
        if (activity.isFinishing) return null
        return try {
            val binding = LoadingAdDialogBinding.inflate(LayoutInflater.from(activity))
            val dialog = AlertDialog.Builder(activity)
                .setView(binding.root)
                .setCancelable(false)
                .create()
            dialog.show()
            dialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
            }
            dialog
        } catch (e: Exception) {
            null
        }
    }

    private fun dismissDialog(dialog: AlertDialog?, activity: Activity) {
        try {
            if (dialog != null && !activity.isFinishing && dialog.isShowing) {
                dialog.dismiss()
            }
        } catch (_: Exception) {}
    }

    // ══════════════════════════════════════
    // INTERNAL SHOW
    // ══════════════════════════════════════

    private fun showAd(
        ad: AppOpenAd,
        activity: Activity,
        screen: AppOpenScreen,
        onDismiss: () -> Unit,
        onFailed: () -> Unit
    ) {
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
                if (!GetFirebase.isAppOpenOnDemand && screen != AppOpenScreen.SPLASH){
                    load(activity, adId, screen)
                }

                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Utils.showMessage(activity, "App open failed to show")
                isShowingAd = false
                setAd(screen, null)
                if (!GetFirebase.isAppOpenOnDemand){
                    load(activity, adId, screen)
                }
                onFailed()
            }

            override fun onAdClicked() {
                super.onAdClicked()
            }
        }

        ad.setOnPaidEventListener { adValue ->
            AppEventLogger.logCustomImpressions(
                activity,
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