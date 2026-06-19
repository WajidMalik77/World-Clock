package com.worldclock.app_themes.ads.helpers.ui

import android.app.Activity
import android.os.Handler
import android.os.Looper
import timber.log.Timber
import com.worldclock.app_themes.ads.AppPrefsManager
import com.worldclock.app_themes.ads.helpers.models.AdNetwork
import com.worldclock.app_themes.ads.helpers.models.AdWaterfallPlan
import com.worldclock.app_themes.ads.helpers.usecases.AdConfigRepository
import com.worldclock.app_themes.ads.helpers.usecases.CheckAdEligibilityUseCase
import com.worldclock.app_themes.ads.helpers.usecases.ShouldShowInterstitialUseCase
import com.worldclock.app_themes.ads.managers.InterstitialAdsManager
import com.worldclock.app_themes.ads.managers.facebook.FbAdInitializer
import com.worldclock.app_themes.ads.managers.facebook.FbInterstitialAdManager
import com.worldclock.app_themes.ads.utils.AdStateManager
import com.worldclock.app_themes.ads.utils.AdsPref
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class InterstitialAdOrchestrator @Inject constructor(
    private val adsPref: AdsPref,
    private val prefsManager: AppPrefsManager,
    private val adConfigRepository: AdConfigRepository,
    private val shouldShowInterstitial: ShouldShowInterstitialUseCase,
    private val checkEligibility: CheckAdEligibilityUseCase
) {
    companion object {
        private const val TAG_INTER = "InterstitialTrace"
    }

    private var interstitialAdsManager: InterstitialAdsManager? = null
    private var fbInterstitialAdManager: FbInterstitialAdManager? = null

    fun initializeManager() {
        if (interstitialAdsManager == null) {
            interstitialAdsManager = InterstitialAdsManager.getInstance(adsPref)
        }
        if (fbInterstitialAdManager == null) {
            fbInterstitialAdManager = FbInterstitialAdManager.getInstance(adsPref)
        }
    }

    private fun defaultPlan(): AdWaterfallPlan = AdWaterfallPlan(primary = AdNetwork.ADMOB, fallback = null)

    private fun adUnitIdFor(network: AdNetwork, screen: String, trigger: String): String {
        return adConfigRepository.getInterstitialAdUnitId(screen, trigger, network)
    }

    private fun managerFor(network: AdNetwork) = when (network) {
        AdNetwork.FACEBOOK -> fbInterstitialAdManager
        else -> interstitialAdsManager
    }

    suspend fun loadInterstitialAd(
        activity: Activity,
        screen: String,
        trigger: String = "default",
        onAdLoaded: () -> Unit = {},
        onAdFailedToLoad: (String) -> Unit = {}
    ) {
        Timber.tag(TAG_INTER).d("loadInterstitialAd screen=$screen")
        val eligibility = checkEligibility(activity)
        if (!eligibility.canShowAds) {
            Timber.tag(TAG_INTER).d("load blocked eligibility=${eligibility.reason}")
            onAdFailedToLoad(eligibility.reason ?: "Cannot show ads")
            return
        }

        if (!adConfigRepository.isConfigLoaded()) {
            Timber.tag(TAG_INTER).d("load blocked config not loaded")
            onAdFailedToLoad("Config not loaded")
            return
        }

        initializeManager()
        val plan = adConfigRepository.getInterstitialWaterfallPlan(screen, trigger) ?: defaultPlan()
        if (plan.fallback == AdNetwork.FACEBOOK || plan.primary == AdNetwork.FACEBOOK) {
            FbAdInitializer.initialize(activity.applicationContext)
        }
        loadForNetwork(activity, screen, trigger, plan.primary, plan.fallback, onAdLoaded, onAdFailedToLoad)
    }

    private fun loadForNetwork(
        activity: Activity,
        screen: String,
        trigger: String,
        network: AdNetwork,
        fallback: AdNetwork?,
        onAdLoaded: () -> Unit,
        onAdFailedToLoad: (String) -> Unit
    ) {
        val adUnitId = adUnitIdFor(network, screen, trigger)
        Timber.tag(TAG_INTER).d("load using network=$network adUnitId=$adUnitId")
        val onFailed: (String) -> Unit = { message ->
            if (fallback != null) {
                Timber.tag(TAG_INTER).d("network=$network load failed ($message), trying fallback=$fallback")
                loadForNetwork(activity, screen, trigger, fallback, null, onAdLoaded, onAdFailedToLoad)
            } else {
                onAdFailedToLoad(message)
            }
        }
        loadOn(network, activity, adUnitId, onAdLoaded, onFailed)
    }

    suspend fun showInterstitialAd(
        activity: Activity,
        screen: String,
        trigger: String,
        noCounterNeeded: Boolean = false,
        onAdClosed: (() -> Unit)? = null,
        onAdNotShown: (() -> Unit)? = null
    ) {
        Timber.tag(TAG_INTER).d("showInterstitialAd screen=$screen trigger=$trigger noCounterNeeded=$noCounterNeeded")
        // Most fragments call show directly without initializeAds(), so make this path self-sufficient.
        initializeManager()

        // Check eligibility
        val eligibility = checkEligibility(activity)
        if (!eligibility.canShowAds) {
            Timber.tag(TAG_INTER).d("show blocked eligibility=${eligibility.reason}")
            onAdNotShown?.invoke()
            return
        }

        if (!shouldShowInterstitial(screen, trigger, noCounterNeeded)) {
            Timber.tag(TAG_INTER).d("show blocked by shouldShowInterstitial=false")
            onAdNotShown?.invoke()
            return
        }

        // Validate activity
        if (activity.isFinishing || activity.isDestroyed) {
            Timber.tag(TAG_INTER).d("show blocked activity finishing/destroyed")
            onAdNotShown?.invoke()
            return
        }

        val plan = adConfigRepository.getInterstitialWaterfallPlan(screen, trigger) ?: defaultPlan()
        if (plan.fallback == AdNetwork.FACEBOOK || plan.primary == AdNetwork.FACEBOOK) {
            FbAdInitializer.initialize(activity.applicationContext)
        }

        val readyNetwork = plan.networksInOrder().firstOrNull { managerFor(it)?.hasLoadedAd() == true }
        if (readyNetwork != null) {
            Timber.tag(TAG_INTER).d("show using already-loaded interstitial network=$readyNetwork")
            showLoadedInterstitial(readyNetwork, activity, onAdClosed, onAdNotShown)
            return
        }

        showInterstitialWithLoading(screen, trigger, plan, activity, onAdClosed, onAdNotShown)
    }

    private fun Any?.hasLoadedAd(): Boolean = when (this) {
        is InterstitialAdsManager -> hasLoadedInterstitialAd()
        is FbInterstitialAdManager -> hasLoadedInterstitialAd()
        else -> false
    }

    private fun loadOn(
        network: AdNetwork,
        activity: Activity,
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        when (val manager = managerFor(network)) {
            is InterstitialAdsManager -> manager.loadInterstitialAd(activity, adUnitId, onLoaded, onFailed)
            is FbInterstitialAdManager -> manager.loadInterstitialAd(activity, adUnitId, onLoaded, onFailed)
            else -> onFailed("No manager available for $network")
        }
    }

    private fun onAdDismissedCommon(onAdClosed: (() -> Unit)?) {
        prefsManager.resetInterstitialCounter()
        prefsManager.setLastInterstitialShownNow()
        // Re-set the flag synchronously so ProcessLifecycleOwner.onStart
        // (which fires on the next looper tick) sees an ad still "showing"
        // and blocks the App Open Ad from firing before navigation commits.
        AdStateManager.isInterstitialAdShowing = true
        Handler(Looper.getMainLooper()).post {
            try {
                onAdClosed?.invoke()
            } finally {
                AdStateManager.isInterstitialAdShowing = false
            }
        }
    }

    private fun showLoadedInterstitial(
        network: AdNetwork,
        activity: Activity,
        onAdClosed: (() -> Unit)?,
        onAdNotShown: (() -> Unit)?
    ) {
        when (val manager = managerFor(network)) {
            is InterstitialAdsManager -> manager.showInterstitialAd(
                activity,
                onAdFailedToShow = { onAdNotShown?.invoke() },
                onAdDismissed = { onAdDismissedCommon(onAdClosed) }
            )
            is FbInterstitialAdManager -> manager.showInterstitialAd(
                activity,
                onAdFailedToShow = { onAdNotShown?.invoke() },
                onAdDismissed = { onAdDismissedCommon(onAdClosed) }
            )
            else -> onAdNotShown?.invoke()
        }
    }

    private fun showInterstitialWithLoading(
        screen: String,
        trigger: String,
        plan: AdWaterfallPlan,
        activity: Activity,
        onAdClosed: (() -> Unit)?,
        onAdNotShown: (() -> Unit)?
    ) {
        Timber.tag(TAG_INTER).d("showInterstitialWithLoading screen=$screen plan=$plan")
        val loadingHandler = InterstitialLoadingHandler(activity)
        val interstitialId = adUnitIdFor(plan.primary, screen, trigger)
        Timber.tag(TAG_INTER).d("loading handler using network=${plan.primary} adUnitId=$interstitialId")

        loadingHandler.showWithLoading(
            adUnitId = interstitialId,
            onLoadAd = { _, onLoaded, onFailed ->
                val loadFailed: (String) -> Unit = { message ->
                    if (plan.fallback != null) {
                        Timber.tag(TAG_INTER).d("primary=${plan.primary} load failed ($message), trying fallback=${plan.fallback}")
                        val fallbackId = adUnitIdFor(plan.fallback, screen, trigger)
                        loadOn(plan.fallback, activity, fallbackId, onLoaded, onFailed)
                    } else {
                        onFailed(message)
                    }
                }
                loadOn(plan.primary, activity, interstitialId, onLoaded, loadFailed)
            },
            onShowAd = {
                val readyNetwork = plan.networksInOrder().firstOrNull { managerFor(it)?.hasLoadedAd() == true }
                    ?: plan.primary
                showLoadedInterstitial(readyNetwork, activity, onAdClosed, onAdNotShown)
            },
            onFailed = { onAdNotShown?.invoke() }
        )
    }

    fun showMustShowAd(
        activity: Activity,
        onAdNotShown: (() -> Unit)?,
        onAdClosed: (() -> Unit)?
    ) {
        initializeManager()

        if (activity.isFinishing || activity.isDestroyed) {
            Timber.w("Activity is finishing or destroyed — aborting ad show")
            onAdNotShown?.invoke()
            return
        }

        if (interstitialAdsManager == null) {
            Timber.w("interstitialAdsManager is NULL — cannot show ad")
            onAdNotShown?.invoke()
            return
        }

        interstitialAdsManager?.showInterstitialAd(
            activity,
            onAdFailedToShow = {
                Timber.w("Failed to show interstitial ad")
                onAdNotShown?.invoke()
            },
            onAdDismissed = {
                Timber.d("Interstitial ad dismissed successfully")
                onAdClosed?.invoke()
            }
        )
    }

    fun destroy() {
        interstitialAdsManager = null
        fbInterstitialAdManager = null
    }
}
