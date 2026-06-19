package com.worldclock.app_themes.ads.helpers.ui

import android.app.Activity
import timber.log.Timber
import com.worldclock.app_themes.ads.AppPrefsManager
import com.worldclock.app_themes.ads.helpers.usecases.AdConfigRepository
import com.worldclock.app_themes.ads.helpers.usecases.CheckAdEligibilityUseCase
import com.worldclock.app_themes.ads.helpers.usecases.ShouldShowInterstitialUseCase
import com.worldclock.app_themes.ads.managers.InterstitialAdsManager
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

    fun initializeManager() {
        if (interstitialAdsManager == null) {
            interstitialAdsManager = InterstitialAdsManager.getInstance(adsPref)
        }
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

        val interstitialId = adConfigRepository.getInterstitialAdUnitId(screen, trigger)
        Timber.tag(TAG_INTER).d("load using adUnitId=$interstitialId")
        interstitialAdsManager?.loadInterstitialAd(
            activity, interstitialId, onAdLoaded, onAdFailedToLoad
        )
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

        if (interstitialAdsManager?.hasLoadedInterstitialAd() == true) {
            Timber.tag(TAG_INTER).d("show using already-loaded singleton interstitial")
            showLoadedInterstitial(activity, onAdClosed, onAdNotShown)
            return
        }

        showInterstitialWithLoading(screen, trigger, activity, onAdClosed, onAdNotShown)
    }

    private fun showLoadedInterstitial(
        activity: Activity,
        onAdClosed: (() -> Unit)?,
        onAdNotShown: (() -> Unit)?
    ) {
        interstitialAdsManager?.showInterstitialAd(
            activity,
            onAdFailedToShow = { onAdNotShown?.invoke() },
            onAdDismissed = {
                prefsManager.resetInterstitialCounter()
                prefsManager.setLastInterstitialShownNow()
                onAdClosed?.invoke()
            }
        ) ?: onAdNotShown?.invoke()
    }

    private fun showInterstitialWithLoading(
        screen: String,
        trigger: String,
        activity: Activity,
        onAdClosed: (() -> Unit)?,
        onAdNotShown: (() -> Unit)?
    ) {
        Timber.tag(TAG_INTER).d("showInterstitialWithLoading screen=$screen")
        val loadingHandler = InterstitialLoadingHandler(activity)
        val interstitialId = adConfigRepository.getInterstitialAdUnitId(screen, trigger)
        Timber.tag(TAG_INTER).d("loading handler using adUnitId=$interstitialId")

        loadingHandler.showWithLoading(
            adUnitId = interstitialId,
            onLoadAd = { adId, onLoaded, onFailed ->
                interstitialAdsManager?.loadInterstitialAd(
                    activity, adId, onLoaded, onFailed
                )
            },
            onShowAd = {
                interstitialAdsManager?.showInterstitialAd(
                    activity,
                    onAdFailedToShow = { onAdNotShown?.invoke() },
                    onAdDismissed = {
                        prefsManager.resetInterstitialCounter()
                        prefsManager.setLastInterstitialShownNow()
                        onAdClosed?.invoke()
                    }
                )
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
    }
}
