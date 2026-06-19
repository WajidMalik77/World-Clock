package com.worldclock.app_themes.ads.managers.facebook

import android.app.Activity
import android.content.Context
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.worldclock.app_themes.ads.utils.AdShowCallback
import com.worldclock.app_themes.ads.utils.AdStateManager
import com.worldclock.app_themes.ads.utils.AdUnitIdSanitizer
import com.worldclock.app_themes.ads.utils.AdsPref
import com.worldclock.app_themes.ads.utils.DebugToaster
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Facebook Audience Network has no dedicated "app open" ad format, so a fullscreen
 * [InterstitialAd] is used as a surrogate, mirroring [com.worldclock.app_themes.ads.managers.AppOpenManager]'s
 * public surface so [com.worldclock.app_themes.ads.app.AppOpenAdLifecycleManager] can treat both
 * networks interchangeably.
 */
class FbAppOpenManager private constructor(private val adsPref: AdsPref) {
    private var interstitialAd: InterstitialAd? = null
    private val isAdLoadingFlag = AtomicBoolean(false)
    private val isShowing = AtomicBoolean(false)
    private val loadTimeRef = AtomicLong(0L)
    private val lastLoadAttemptAtRef = AtomicLong(0L)
    private val lastTooFrequentAtRef = AtomicLong(0L)
    private var showCallback: AdShowCallback? = null
    private var lastPlacementId: String? = null

    companion object {
        @Volatile
        private var INSTANCE: FbAppOpenManager? = null
        private const val MIN_LOAD_INTERVAL_MS = 30_000L
        private const val TOO_FREQUENT_BACKOFF_MS = 120_000L

        fun getInstance(adsPref: AdsPref): FbAppOpenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FbAppOpenManager(adsPref).also { INSTANCE = it }
            }
        }
    }

    fun loadAppOpenAd(
        context: Context,
        placementId: String,
        onAdLoaded: () -> Unit = {},
        onAdFailed: (String) -> Unit = {}
    ) {
        val resolvedId = AdUnitIdSanitizer.sanitizeFbAppOpen(placementId)
        if (resolvedId.isBlank()) {
            onAdFailed("Facebook placement id is blank")
            return
        }
        if (hasUsableAd(resolvedId)) {
            onAdLoaded()
            return
        }
        if (interstitialAd != null && lastPlacementId != resolvedId) {
            interstitialAd?.destroy()
            interstitialAd = null
        }

        val now = System.currentTimeMillis()
        val tooFrequentElapsed = now - lastTooFrequentAtRef.get()
        if (tooFrequentElapsed in 0 until TOO_FREQUENT_BACKOFF_MS) {
            onAdFailed("Facebook load throttled after too-frequent error")
            return
        }

        val lastAttemptAt = lastLoadAttemptAtRef.get()
        val loadElapsed = now - lastAttemptAt
        if (lastPlacementId == resolvedId && loadElapsed in 0 until MIN_LOAD_INTERVAL_MS) {
            onAdFailed("Facebook load throttled: last attempt ${loadElapsed}ms ago")
            return
        }

        if (!isAdLoadingFlag.compareAndSet(false, true)) {
            onAdFailed("Load already in progress")
            return
        }

        lastPlacementId = resolvedId
        lastLoadAttemptAtRef.set(now)
        interstitialAd?.destroy()
        val ad = InterstitialAd(context, resolvedId)
        interstitialAd = ad

        val loadAdConfig = ad.buildLoadAdConfig()
            .withAdListener(object : InterstitialAdListener {
                override fun onError(adObj: Ad?, adError: AdError) {
                    isAdLoadingFlag.set(false)
                    Timber.e("FB app-open ad error: ${adError.errorMessage}")
                    if (adError.errorMessage.contains("re-loaded too frequently", ignoreCase = true) ||
                        adError.errorMessage.contains("too frequent", ignoreCase = true)
                    ) {
                        lastTooFrequentAtRef.set(System.currentTimeMillis())
                    }
                    val callback = showCallback
                    if (callback != null) {
                        DebugToaster.showAdDebugCard(context, adsPref.isDebugToastAppOpenEnabled(), "FB AppOpen: Failed to Show")
                        cleanUpAdState()
                        callback.onAdFailedToShow("Ad failed: ${adError.errorMessage}")
                    } else {
                        DebugToaster.showAdDebugCard(context, adsPref.isDebugToastAppOpenEnabled(), "FB AppOpen: Failed to Load")
                        onAdFailed(adError.errorMessage)
                    }
                }

                override fun onAdLoaded(adObj: Ad?) {
                    isAdLoadingFlag.set(false)
                    loadTimeRef.set(System.currentTimeMillis())
                    DebugToaster.showAdDebugCard(context, adsPref.isDebugToastAppOpenEnabled(), "FB AppOpen: Loaded")
                    onAdLoaded()
                }

                override fun onAdClicked(adObj: Ad?) {}

                override fun onLoggingImpression(adObj: Ad?) {
                    DebugToaster.showAdDebugCard(context, adsPref.isDebugToastAppOpenEnabled(), "FB AppOpen: Shown")
                    isShowing.set(true)
                    AdStateManager.isAppOpenAdShowing = true
                    showCallback?.onAdShown()
                }

                override fun onInterstitialDisplayed(adObj: Ad?) {}

                override fun onInterstitialDismissed(adObj: Ad?) {
                    val callback = showCallback
                    cleanUpAdState()
                    callback?.onAdDismissed()
                }
            })
            .build()

        ad.loadAd(loadAdConfig)
    }

    fun isAdLoading(): Boolean = isAdLoadingFlag.get()

    fun hasUsableAd(): Boolean {
        val ad = interstitialAd ?: return false
        return ad.isAdLoaded && !isShowing.get() && wasLoadTimeLessThanNHoursAgo()
    }

    fun hasUsableAd(placementId: String): Boolean {
        val resolvedId = AdUnitIdSanitizer.sanitizeFbAppOpen(placementId)
        return hasUsableAd() && lastPlacementId == resolvedId
    }

    fun isShowingOrShowingSoon(): Boolean = isShowing.get()

    private fun wasLoadTimeLessThanNHoursAgo(): Boolean {
        val dateDifference = System.currentTimeMillis() - loadTimeRef.get()
        val numMilliSecondsPerHour = 3600000L
        return dateDifference < numMilliSecondsPerHour * 4
    }

    fun showAppOpenAdIfAvailable(activity: Activity, callback: AdShowCallback) {
        val ad = interstitialAd
        if (ad == null || !ad.isAdLoaded || isShowing.get()) {
            callback.onAdFailedToShow("Ad not available")
            return
        }

        showCallback = callback

        if (!ad.show()) {
            showCallback = null
            callback.onAdFailedToShow("show() returned false")
        }
    }

    private fun cleanUpAdState() {
        interstitialAd?.destroy()
        interstitialAd = null
        isShowing.set(false)
        AdStateManager.isAppOpenAdShowing = false
        showCallback = null
    }

    fun destroy() {
        interstitialAd?.destroy()
        interstitialAd = null
        isShowing.set(false)
    }
}
