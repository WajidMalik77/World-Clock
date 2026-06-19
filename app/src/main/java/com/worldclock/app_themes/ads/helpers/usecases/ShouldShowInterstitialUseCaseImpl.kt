package com.worldclock.app_themes.ads.helpers.usecases

import timber.log.Timber
import com.worldclock.app_themes.ads.AppPrefsManager
import com.worldclock.app_themes.ads.config.AdControlConfigManager
import javax.inject.Inject

class ShouldShowInterstitialUseCaseImpl @Inject constructor(
    private val adControlConfigManager: AdControlConfigManager,
    private val prefsManager: AppPrefsManager
) : ShouldShowInterstitialUseCase {
    companion object {
        private const val TAG_INTER = "InterstitialTrace"
    }

    override operator fun invoke(
        screen: String,
        trigger: String,
        noCounterNeeded: Boolean
    ): Boolean {
        Timber.tag(TAG_INTER).d("shouldShow start screen=$screen trigger=$trigger noCounterNeeded=$noCounterNeeded")
        if (!adControlConfigManager.isInterstitialEnabledForTrigger(screen, trigger)) {
            Timber.tag(TAG_INTER).d("blocked: interstitial disabled for trigger")
            return false
        }

        // Pre-home screens must bypass counter logic by requirement.
        if (noCounterNeeded || adControlConfigManager.isPreHomeScreen(screen)) {
            Timber.tag(TAG_INTER).d("allowed: bypass counter logic")
            return true
        }

        val cooldownSeconds = adControlConfigManager.getInterstitialCooldownSeconds()
        if (cooldownSeconds > 0) {
            val lastShownAt = prefsManager.getLastInterstitialShownAtMillis()
            if (lastShownAt > 0L) {
                val elapsedSeconds = (System.currentTimeMillis() - lastShownAt) / 1000
                if (elapsedSeconds < cooldownSeconds) {
                    Timber.tag(TAG_INTER).d("blocked: cooldown active elapsed=$elapsedSeconds cooldown=$cooldownSeconds")
                    return false
                }
            }
        }

        // Home first-click behavior gate.
        if (screen == "HomeFragmentScreen" || screen == "DashboardFragmentScreen" || screen == "MainScreen") {
            if (prefsManager.isFirstHomeInterstitialClickPending()) {
                prefsManager.markFirstHomeInterstitialClickConsumed()
                val allowed = adControlConfigManager.isInterFirstCountEnabledForHome()
                Timber.tag(TAG_INTER).d("home first-click gate allowed=$allowed")
                return allowed
            }
        }

        val currentCounter = prefsManager.getAdCounter()
        val nextCounter = currentCounter + 1

        prefsManager.incrementAdCounter()
        val allowedByThreshold = adControlConfigManager.isInterstitialThresholdReached(nextCounter)
        Timber.tag(TAG_INTER).d("counter gate current=$currentCounter next=$nextCounter allowed=$allowedByThreshold")
        return allowedByThreshold
    }
}
