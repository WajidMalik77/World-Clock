package com.worldclock.app_themes.ads.managers.facebook

import android.app.Activity
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.worldclock.app_themes.ads.utils.AdStateManager
import com.worldclock.app_themes.ads.utils.AdUnitIdSanitizer
import com.worldclock.app_themes.ads.utils.AdsPref
import com.worldclock.app_themes.ads.utils.DebugToaster
import timber.log.Timber

class FbInterstitialAdManager private constructor(private val adsPref: AdsPref) {
    private data class PendingLoadCallback(
        val onLoaded: () -> Unit?,
        val onFailed: (String) -> Unit?
    )

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isDestroyed = false
    private var isInterstitialAdShowing = false
    private val pendingLoadCallbacks = mutableListOf<PendingLoadCallback>()

    private var onShowFailed: (() -> Unit)? = null
    private var onShowDismissed: (() -> Unit)? = null

    companion object {
        @Volatile
        private var INSTANCE: FbInterstitialAdManager? = null

        fun getInstance(adsPref: AdsPref): FbInterstitialAdManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FbInterstitialAdManager(adsPref).also { INSTANCE = it }
            }
        }
    }

    fun loadInterstitialAd(
        activity: Activity,
        placementId: String,
        onAdLoaded: () -> Unit? = {},
        onAdFailedToLoad: (message: String) -> Unit? = {}
    ) {
        val resolvedPlacementId = AdUnitIdSanitizer.sanitizeFbInterstitial(placementId)
        if (resolvedPlacementId.isBlank()) {
            onAdFailedToLoad("Facebook placement id is blank")
            return
        }

        synchronized(this) {
            if (isDestroyed) {
                onAdFailedToLoad("Ad load aborted: Manager destroyed.")
                return
            }
            if (isLoading) {
                pendingLoadCallbacks += PendingLoadCallback(onAdLoaded, onAdFailedToLoad)
                return
            }
            if (interstitialAd?.isAdLoaded == true) {
                onAdLoaded()
                return
            }
            isLoading = true
        }

        interstitialAd?.destroy()
        val ad = InterstitialAd(activity, resolvedPlacementId)
        interstitialAd = ad

        val loadAdConfig = ad.buildLoadAdConfig()
            .withAdListener(object : InterstitialAdListener {
                override fun onError(adObj: Ad?, adError: AdError) {
                    isLoading = false
                    AdStateManager.isInterstitialAdShowing = false
                    Timber.e("FB interstitial ad error: ${adError.errorMessage}")
                    val showFailedCallback = onShowFailed
                    if (showFailedCallback != null) {
                        DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastInterstitialEnabled(), "FB Interstitial: Failed to Show")
                        cleanUpAdState()
                        showFailedCallback.invoke()
                    } else {
                        DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastInterstitialEnabled(), "FB Interstitial: Failed to Load")
                        onAdFailedToLoad(adError.errorMessage)
                        notifyPendingLoadFailed(adError.errorMessage)
                    }
                }

                override fun onAdLoaded(adObj: Ad?) {
                    isLoading = false
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastInterstitialEnabled(), "FB Interstitial: Loaded")
                    onAdLoaded()
                    notifyPendingLoadLoaded()
                }

                override fun onAdClicked(adObj: Ad?) {}
                override fun onLoggingImpression(adObj: Ad?) {
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastInterstitialEnabled(), "FB Interstitial: Shown")
                    AdStateManager.isInterstitialAdShowing = true
                    isInterstitialAdShowing = true
                }

                override fun onInterstitialDisplayed(adObj: Ad?) {}

                override fun onInterstitialDismissed(adObj: Ad?) {
                    Timber.i("FB interstitial ad dismissed by user.")
                    val callback = onShowDismissed
                    cleanUpAdState()
                    callback?.invoke()
                }
            })
            .build()

        ad.loadAd(loadAdConfig)
    }

    fun hasLoadedInterstitialAd(): Boolean {
        return synchronized(this) {
            interstitialAd?.isAdLoaded == true && !isDestroyed && !isInterstitialAdShowing
        }
    }

    fun showInterstitialAd(
        activity: Activity,
        onAdFailedToShow: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        if (isInterstitialAdShowing) {
            Timber.w("FB Ad is already showing. Skipping.")
            return
        }

        val ad = interstitialAd
        if (ad == null || isDestroyed || !ad.isAdLoaded) {
            Timber.w("FB Interstitial ad is not ready.")
            onAdFailedToShow()
            return
        }

        onShowFailed = onAdFailedToShow
        onShowDismissed = onAdDismissed

        if (!ad.show()) {
            onShowFailed = null
            onShowDismissed = null
            onAdFailedToShow()
        }
    }

    private fun cleanUpAdState() {
        interstitialAd?.destroy()
        interstitialAd = null
        isInterstitialAdShowing = false
        AdStateManager.isInterstitialAdShowing = false
        onShowFailed = null
        onShowDismissed = null
    }

    private fun notifyPendingLoadLoaded() {
        val callbacks = synchronized(this) {
            pendingLoadCallbacks.toList().also { pendingLoadCallbacks.clear() }
        }
        callbacks.forEach { it.onLoaded() }
    }

    private fun notifyPendingLoadFailed(message: String) {
        val callbacks = synchronized(this) {
            pendingLoadCallbacks.toList().also { pendingLoadCallbacks.clear() }
        }
        callbacks.forEach { it.onFailed(message) }
    }
}
