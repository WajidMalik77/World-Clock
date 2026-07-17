package com.worldclock.app_themes.ads.preload

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.GetFirebase.MIN_INTERVAL_MS
import com.worldclock.app_themes.ads.utils.Utils
import com.worldclock.app_themes.core.analytics.AppEventLogger
import com.worldclock.app_themes.databinding.DialogTimerBinding
import com.worldclock.app_themes.databinding.LoadingAdDialogBinding

enum class AdLoadMode {
    PRELOADED,
    ON_DEMAND
}

enum class InterstitialScreen {
    SPLASH,
    LANGUAGE,
    ONBOARDING,
    PREMIUM,
    OTHER
}

object InterstitialAdManager {

    // Per-screen ad slots
    private var adSplash: InterstitialAd? = null
    private var adLanguage: InterstitialAd? = null
    private var adOnboarding: InterstitialAd? = null
    private var adPremium: InterstitialAd? = null
    private var adOther: InterstitialAd? = null
    private var onDemandAd: InterstitialAd? = null

    // Per-screen ad unit IDs

    // Per-screen click counters
    private var counterSplash: Int = 0
    private var counterLanguage: Int = 0
    private var counterOnboarding: Int = 0
    private var counterPremium: Int = 0
    private var counterOther: Int = 0

    // Shared state
    var isAdVisible = false
    var lastShownTime: Long = 0
    var isOnDemandLoading = false


    // ══════════════════════════════════════
    // HELPERS — get/set per screen
    // ══════════════════════════════════════

    private fun getAd(screen: InterstitialScreen): InterstitialAd? {
        return when (screen) {
            InterstitialScreen.SPLASH -> adSplash
            InterstitialScreen.LANGUAGE -> adLanguage
            InterstitialScreen.ONBOARDING -> adOnboarding
            InterstitialScreen.PREMIUM -> adPremium
            InterstitialScreen.OTHER -> adOther
        }
    }

    private fun setAd(screen: InterstitialScreen, ad: InterstitialAd?) {
        when (screen) {
            InterstitialScreen.SPLASH -> adSplash = ad
            InterstitialScreen.LANGUAGE -> adLanguage = ad
            InterstitialScreen.ONBOARDING -> adOnboarding = ad
            InterstitialScreen.PREMIUM -> adPremium = ad
            InterstitialScreen.OTHER -> adOther = ad
        }
    }

    private fun getAdId(screen: InterstitialScreen): String {
        return when (screen) {
            InterstitialScreen.SPLASH -> GetFirebase.adIdSplash_interstitial
            InterstitialScreen.LANGUAGE -> GetFirebase.adIdLanguage_interstitial
            InterstitialScreen.ONBOARDING -> GetFirebase.adIdOnboarding_interstitial
            InterstitialScreen.PREMIUM -> GetFirebase.adIdPremium_interstitial
            InterstitialScreen.OTHER -> GetFirebase.adIdOther_interstitial
        }
    }

    private fun setAdId(screen: InterstitialScreen, id: String) {
        when (screen) {
            InterstitialScreen.SPLASH -> GetFirebase.adIdSplash_interstitial = id
            InterstitialScreen.LANGUAGE -> GetFirebase.adIdLanguage_interstitial = id
            InterstitialScreen.ONBOARDING -> GetFirebase.adIdOnboarding_interstitial = id
            InterstitialScreen.PREMIUM -> GetFirebase.adIdPremium_interstitial = id
            InterstitialScreen.OTHER -> GetFirebase.adIdOther_interstitial = id
        }
    }

    private fun getCounter(screen: InterstitialScreen): Int {
        return when (screen) {
            InterstitialScreen.SPLASH -> counterSplash
            InterstitialScreen.LANGUAGE -> counterLanguage
            InterstitialScreen.ONBOARDING -> counterOnboarding
            InterstitialScreen.PREMIUM -> counterPremium
            InterstitialScreen.OTHER -> counterOther
        }
    }

    private fun incrementCounter(screen: InterstitialScreen) {
        when (screen) {
            InterstitialScreen.SPLASH -> counterSplash++
            InterstitialScreen.LANGUAGE -> counterLanguage++
            InterstitialScreen.ONBOARDING -> counterOnboarding++
            InterstitialScreen.PREMIUM -> counterPremium++
            InterstitialScreen.OTHER -> counterOther++
        }
    }

    private fun resetCounter(screen: InterstitialScreen) {
        when (screen) {
            InterstitialScreen.SPLASH -> counterSplash = 0
            InterstitialScreen.LANGUAGE -> counterLanguage = 0
            InterstitialScreen.ONBOARDING -> counterOnboarding = 0
            InterstitialScreen.PREMIUM -> counterPremium = 0
            InterstitialScreen.OTHER -> counterOther = 0
        }
    }

    // ══════════════════════════════════════
    // PRELOADING
    // ══════════════════════════════════════

    fun load(context: Context, adId: String, screen: InterstitialScreen = InterstitialScreen.OTHER) {


        if (!GetFirebase.enable_interstitial_ads){
            return
        }


        if (getAd(screen) != null) return
        setAdId(screen, adId)

        InterstitialAd.load(
            context,
            adId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Utils.showMessage(context, "interstitial loaded")

                    setAd(screen, ad)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.d("INTER_FAILED", error.message.toString())
                    setAd(screen, null)
                }
            }
        )
    }

    // Convenience loaders
    fun loadSplash(context: Context, adId: String) = load(context, adId, InterstitialScreen.SPLASH)
    fun loadLanguage(context: Context, adId: String) = load(context, adId, InterstitialScreen.LANGUAGE)
    fun loadOnboarding(context: Context, adId: String) = load(context, adId, InterstitialScreen.ONBOARDING)
    fun loadPremium(context: Context, adId: String) = load(context, adId, InterstitialScreen.PREMIUM)
    fun loadOther(context: Context, adId: String) = load(context, adId, InterstitialScreen.OTHER)

    // ══════════════════════════════════════
    // CONDITION CHECKS
    // ══════════════════════════════════════

    private fun canShow(
        screen: InterstitialScreen,
        isEnabledOnScreen: Int,
        requiredClicks: Int,
        isPremium: Boolean,
        adsEnabled: Boolean
    ): Boolean {
        if (isPremium || !adsEnabled) return false
        if (isAdVisible) return false

        if (isEnabledOnScreen == -1) return true


        if (GetFirebase.use_counter){

            if (screen == InterstitialScreen.SPLASH ||
                screen == InterstitialScreen.ONBOARDING ||
                screen == InterstitialScreen.LANGUAGE){
                val timerReached = lastShownTime == 0L ||
                        (System.currentTimeMillis() - lastShownTime) >= MIN_INTERVAL_MS
                return timerReached
            }
            else{
                if (requiredClicks == -1){
                    val timerReached = lastShownTime == 0L ||
                            (System.currentTimeMillis() - lastShownTime) >= MIN_INTERVAL_MS
                    return timerReached
                }
                else{
                    val counterReached = getCounter(screen) >= requiredClicks
                    val timerReached = lastShownTime == 0L ||
                            (System.currentTimeMillis() - lastShownTime) >= MIN_INTERVAL_MS
                    return counterReached || timerReached
                }

            }
        }
        else{
            if (isEnabledOnScreen == 0) return false

            val timerReached = lastShownTime == 0L ||
                    (System.currentTimeMillis() - lastShownTime) >= MIN_INTERVAL_MS
            return timerReached
        }







    }

    private fun canShowNoCounter(
        isEnabledOnScreen: Int,
        isPremium: Boolean,
        adsEnabled: Boolean
    ): Boolean {
        if (isPremium || !adsEnabled) return false
        if (isAdVisible) return false
        if (isEnabledOnScreen == 0) return false

        if (isEnabledOnScreen == -1) return true

        val timerReached = lastShownTime == 0L ||
                (System.currentTimeMillis() - lastShownTime) >= MIN_INTERVAL_MS

        return timerReached
    }

    // ══════════════════════════════════════
    // SHOW WITH COUNTER
    // ══════════════════════════════════════

    fun showIfReady(
        activity: Activity,
        screen: InterstitialScreen = InterstitialScreen.OTHER,
        adId: String = getAdId(screen),
        mode: AdLoadMode = AdLoadMode.PRELOADED,
        isEnabledOnScreen: Int = 0,
        requiredClicks: Int = 1,
        isPremium: Boolean = false,
        adsEnabled: Boolean = true,
        onDismiss: () -> Unit,
        onFailed: () -> Unit
    ) {

        if (!GetFirebase.enable_interstitial_ads){
            onDismiss()
            return
        }

        if (screen == InterstitialScreen.LANGUAGE ||
            screen == InterstitialScreen.ONBOARDING ||
            screen == InterstitialScreen.PREMIUM){
            showWithoutCounter(activity,screen,adId,mode,isEnabledOnScreen,
                isPremium,adsEnabled,{
                    onDismiss()
                }, {
                    onFailed()
                })
        }
        else{
            incrementCounter(screen)

            if (!canShow(screen, isEnabledOnScreen, requiredClicks, isPremium, adsEnabled)) {
                onFailed()
                return
            }

            when (mode) {

                AdLoadMode.PRELOADED -> {
                    if (isEnabledOnScreen == 1){
                        val ad = getAd(screen) ?: run {
                            onFailed()
                            return
                        }
                        showPreloadedWithDialog(ad, activity, screen, onDismiss, onFailed)
                    }
                    else{
                        onFailed()
                        return
                    }

                }

                AdLoadMode.ON_DEMAND -> {
                    if (isEnabledOnScreen == 1){
                        loadAndShowWithDialog(activity, adId, screen, onDismiss, onFailed)
                    }
                    else{
                        onFailed()
                        return
                    }
                }
            }
        }


    }

    // ══════════════════════════════════════
    // SHOW WITHOUT COUNTER
    // ══════════════════════════════════════

    fun showWithoutCounter(
        activity: Activity,
        screen: InterstitialScreen = InterstitialScreen.OTHER,
        adId: String = getAdId(screen),
        mode: AdLoadMode = AdLoadMode.PRELOADED,
        isEnabledOnScreen: Int = -1,
        isPremium: Boolean = false,
        adsEnabled: Boolean = true,
        onDismiss: () -> Unit,
        onFailed: () -> Unit
    ) {
        if (!GetFirebase.enable_interstitial_ads){
            onDismiss()
            return
        }

        if (!canShowNoCounter(isEnabledOnScreen, isPremium, adsEnabled)) {
            onFailed()
            return
        }

        when (mode) {
            AdLoadMode.PRELOADED -> {
                if (isEnabledOnScreen == 1){
                    val ad = getAd(screen) ?: run {
                        onFailed()
                        return
                    }
                    showPreloadedWithDialog(ad, activity, screen, onDismiss, onFailed)
                }
                else{
                    onFailed()
                    return
                }


            }

            AdLoadMode.ON_DEMAND -> {
                if (isEnabledOnScreen == 1){
                    loadAndShowWithDialog(activity, adId, screen, onDismiss, onFailed)
                }
                else{
                    onFailed()
                    return
                }

            }
        }
    }

    // ══════════════════════════════════════
    // PRELOADED WITH DIALOG
    // ══════════════════════════════════════

    private fun showPreloadedWithDialog(
        ad: InterstitialAd,
        activity: Activity,
        screen: InterstitialScreen,
        onDismiss: () -> Unit,
        onFailed: () -> Unit
    ) {
        if (GetFirebase.time_delay_for_preloaded_interstitial.toInt() == 0) {
            showAd(ad, activity, screen, onDismiss, onFailed, isPreloaded = true)
        } else {
            val dialog = createLoadingDialog(activity) ?: run {
                showAd(ad, activity, screen, onDismiss, onFailed, isPreloaded = true)
                return
            }

            Handler(Looper.getMainLooper()).postDelayed({
                dismissDialog(dialog, activity)
                showAd(ad, activity, screen, onDismiss, onFailed, isPreloaded = true)
            }, GetFirebase.time_delay_for_preloaded_interstitial)
        }
    }

    // ══════════════════════════════════════
    // ON DEMAND WITH TIMEOUT DIALOG
    // ══════════════════════════════════════

    private fun loadAndShowWithDialog(
        activity: Activity,
        adId: String,
        screen: InterstitialScreen,
        onDismiss: () -> Unit,
        onFailed: () -> Unit
    ) {
        if (isOnDemandLoading) {
            onFailed()
            return
        }

        if (GetFirebase.time_delay_for_ondemand_interstitial.toInt() == 0) {
            isOnDemandLoading = true
            var hasResponded = false

            InterstitialAd.load(
                activity,
                adId,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        if (hasResponded) return
                        hasResponded = true
                        isOnDemandLoading = false
                        onDemandAd = ad

                        Utils.showMessage(activity, "interstitial loaded")


                        if (activity.isFinishing) {
                            onFailed()
                            return
                        }

                        showAd(ad, activity, screen, onDismiss, onFailed, isPreloaded = false)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.d("INTER_FAILED", error.message.toString())

                        if (hasResponded) return
                        hasResponded = true
                        isOnDemandLoading = false
                        onDemandAd = null
                        onFailed()
                    }
                }
            )
        } else {

            if (onDemandAd != null){
                showAd(onDemandAd!!, activity, screen, onDismiss, onFailed, isPreloaded = false)

            }
            else{
                isOnDemandLoading = true
                var hasResponded = false
                val handler = Handler(Looper.getMainLooper())

                val dialog = createLoadingDialog(activity) ?: run {
                    onFailed()
                    return
                }

                val timeoutRunnable = Runnable {
                    if (!hasResponded) {
                        hasResponded = true
                        isOnDemandLoading = false
                        onDemandAd = null
                        dismissDialog(dialog, activity)
                        onFailed()
                    }
                }

                handler.postDelayed(timeoutRunnable, GetFirebase.time_delay_for_ondemand_interstitial)

                InterstitialAd.load(
                    activity,
                    adId,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            if (hasResponded) return
                            hasResponded = true
                            handler.removeCallbacks(timeoutRunnable)
                            isOnDemandLoading = false
                            onDemandAd = ad
                            Utils.showMessage(activity, "interstitial loaded")

                            if (activity.isFinishing) {
                                dismissDialog(dialog, activity)
                                onFailed()
                                return
                            }

                            dismissDialog(dialog, activity)
                            showAd(ad, activity, screen, onDismiss, onFailed, isPreloaded = false)
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.d("INTER_FAILED", error.message.toString())

                            if (hasResponded) return
                            hasResponded = true
                            handler.removeCallbacks(timeoutRunnable)
                            isOnDemandLoading = false
                            onDemandAd = null
                            dismissDialog(dialog, activity)
                            onFailed()
                        }
                    }
                )
            }


        }
    }

    // ══════════════════════════════════════
    // INTERNAL SHOW
    // ══════════════════════════════════════

    private fun showAd(
        ad: InterstitialAd,
        activity: Activity,
        screen: InterstitialScreen,
        onDismiss: () -> Unit,
        onFailed: () -> Unit,
        isPreloaded: Boolean
    ) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Utils.showMessage(activity, "interstitial showed")

                isAdVisible = true
            }

            override fun onAdDismissedFullScreenContent() {
                isAdVisible = false
                lastShownTime = System.currentTimeMillis()
                resetCounter(screen)

                if (isPreloaded) {
                    setAd(screen, null)
                    load(activity, ad.adUnitId, screen)
                } else {
                    onDemandAd = null
                }
                Utils.showMessage(activity, "interstitial dismissed")


                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                isAdVisible = false

                if (isPreloaded) {
                    setAd(screen, null)
                } else {
                    onDemandAd = null
                }

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
                adUnitId = ad?.adUnitId.toString(),
                adFormat = "native"
            )
        }

        ad.show(activity)
    }

    // ══════════════════════════════════════
    // DIALOG
    // ══════════════════════════════════════

    private fun createLoadingDialog(activity: Activity): AlertDialog? {
        if (activity.isFinishing) return null

        val binding = LoadingAdDialogBinding.inflate(LayoutInflater.from(activity))

        val dialog = AlertDialog.Builder(activity)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        dialog.show()

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }

        return dialog
    }

    private fun dismissDialog(dialog: AlertDialog, activity: Activity) {
        try {
            if (!activity.isFinishing && dialog.isShowing) {
                dialog.dismiss()
            }
        } catch (_: Exception) {
        }
    }

    // ══════════════════════════════════════
    // RESET
    // ══════════════════════════════════════

    fun reset() {
        adSplash = null
        adLanguage = null
        adOnboarding = null
        adPremium = null
        adOther = null
        onDemandAd = null
        isAdVisible = false
        isOnDemandLoading = false
        counterSplash = 0
        counterLanguage = 0
        counterOnboarding = 0
        counterPremium = 0
        counterOther = 0
    }

    fun resetScreen(screen: InterstitialScreen) {
        setAd(screen, null)
        resetCounter(screen)
    }
}