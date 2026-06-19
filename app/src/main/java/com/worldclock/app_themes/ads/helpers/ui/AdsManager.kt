package com.worldclock.app_themes.ads.helpers.ui

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.worldclock.app_themes.ads.helpers.models.BannerConfig
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import com.worldclock.app_themes.ads.helpers.models.NativeAdEvent
import com.worldclock.app_themes.ads.helpers.usecases.AdConfigRepository
import com.worldclock.app_themes.ads.helpers.usecases.CheckAdEligibilityUseCase
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class AdsManager @Inject constructor(
    private val adConfigRepository: AdConfigRepository,
    private val bannerAdOrchestrator: BannerAdOrchestrator,
    private val nativeAdOrchestrator: NativeAdOrchestrator,
    private val interstitialAdOrchestrator: InterstitialAdOrchestrator,
    private val checkEligibility: CheckAdEligibilityUseCase
) : DefaultLifecycleObserver {

    private var onConfigReady: (() -> Unit)? = null
    private var onConfigFailed: (() -> Unit)? = null
    private var keepNativeOnNextPause = false

    /**
     * Initialize ads configuration
     */
    fun initializeAds(
        activity: Activity?,
        screen: String,
        trigger: String
    ) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            Timber.w("Activity invalid — cannot initialize ads")
            onConfigFailed?.invoke()
            return
        }

        if (activity is LifecycleOwner) {
            activity.lifecycle.addObserver(this)
        }

        // Initialize managers
        interstitialAdOrchestrator.initializeManager()

        // Load configuration
        adConfigRepository.initialize(
            onReady = {
                onConfigReady?.invoke()
            },
            onFailed = {
                Timber.w("Ad configuration failed")
                onConfigFailed?.invoke()
            }
        )
    }

    /**
     * Initialize without trigger (simplified flow)
     */
    fun initializeAdsSimple(
        activity: Activity,
        onLoaded: () -> Unit = {},
        onFailed: () -> Unit = {}
    ) {
        if (activity is LifecycleOwner) {
            activity.lifecycle.addObserver(this)
        }

        interstitialAdOrchestrator.initializeManager()

        adConfigRepository.initialize(
            onReady = {
                onConfigReady?.invoke()
                lifecycleScope(activity).launch {
                    loadInterstitialOnly(activity, "SplashScreen", onLoaded, onFailed)
                }
            },
            onFailed = {
                Timber.w("Config not loaded")
                onConfigFailed?.invoke()
                onFailed()
            }
        )
    }

    /**
     * Set configuration callbacks
     */
    fun setConfigCallbacks(
        onReady: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        this.onConfigReady = onReady
        this.onConfigFailed = onFailed
    }

    suspend fun loadBannerAd(
        context: Context,
        screen: String,
        position: String,
        container: FrameLayout,
        shimmer: View,
        adId: String? = null
    ) {
        bannerAdOrchestrator.loadBannerAd(
            context, screen, position, container, shimmer, adId
        )
    }

    suspend fun loadBannerAds(
        context: Context,
        screen: String,
        configs: List<BannerConfig>
    ) {
        configs.forEach { config ->
            bannerAdOrchestrator.loadBannerAd(
                context, screen, config.position, config.container, config.shimmer, config.adId
            )
        }
    }

    suspend fun loadNativeAds(
        context: Context,
        screen: String,
        configs: List<NativeAdConfig>,
        onEvent: ((NativeAdEvent) -> Unit)? = null
    ) {
        nativeAdOrchestrator.loadNativeAds(context, screen, configs, onEvent)
    }

    suspend fun preloadNativeForNextContainer(
        context: Context,
        screen: String,
        position: String
    ) {
        nativeAdOrchestrator.preloadNativeForNextContainer(context, screen, position)
    }

    suspend fun showInterstitialAd(
        activity: Activity,
        screen: String,
        trigger: String,
        noCounterNeeded: Boolean = false,
        onAdClosed: (() -> Unit)? = null,
        onAdNotShown: (() -> Unit)? = null
    ) {
        interstitialAdOrchestrator.showInterstitialAd(
            activity, screen, trigger, noCounterNeeded, onAdClosed, onAdNotShown
        )
    }

    fun showInterstitialMust(
        activity: Activity,
        onAdNotShown: (() -> Unit)? = null,
        onAdClosed: (() -> Unit)? = null
    ) {
        interstitialAdOrchestrator.showMustShowAd(activity, onAdNotShown, onAdClosed)
    }

    suspend fun shouldLoadAds(context: Context): Boolean {
        return checkEligibility(context).canShowAds
    }

    fun clearAllAds() {
        bannerAdOrchestrator.destroyAllBanners()
        Timber.d("All ads cleared")
    }

    fun keepCurrentNativeOnNextPause() {
        keepNativeOnNextPause = true
    }

    fun keepCurrentNativeForNextContainer() {
        nativeAdOrchestrator.keepCurrentNativeForNextContainer()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Timber.d("AdsManager: onDestroy called by LifecycleOwner")
        destroy()
        owner.lifecycle.removeObserver(this)
        super.onDestroy(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
        if (keepNativeOnNextPause) {
            keepNativeOnNextPause = false
        } else {
            nativeAdOrchestrator.releaseUnimpressedNative()
        }
        super.onPause(owner)
    }

    fun destroy() {
        clearAllAds()
        interstitialAdOrchestrator.destroy()
        nativeAdOrchestrator.destroy()
        onConfigReady = null
        onConfigFailed = null
    }

    private suspend fun loadInterstitialOnly(
        activity: Activity,
        screen: String,
        onLoaded: () -> Unit,
        onFailed: () -> Unit
    ) {
        interstitialAdOrchestrator.loadInterstitialAd(
            activity, screen, "splash", onLoaded, onAdFailedToLoad = { onFailed }
        )
    }

    private fun lifecycleScope(activity: Activity): CoroutineScope {
        return (activity as? ComponentActivity)?.lifecycleScope
            ?: CoroutineScope(Dispatchers.Main + SupervisorJob())
    }
}
