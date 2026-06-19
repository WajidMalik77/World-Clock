package com.worldclock.app_themes.ads.helpers.usecases

import android.content.Context
import com.worldclock.app_themes.ads.config.AdControlConfigManager
import com.worldclock.app_themes.ads.helpers.models.AdEligibility
import com.worldclock.app_themes.ads.utils.AdsPref
import com.worldclock.app_themes.core.utils.InAppPrefs
import com.worldclock.app_themes.core.utils.PrefUtil
import timber.log.Timber
import javax.inject.Inject

class CheckAdEligibilityUseCaseImpl @Inject constructor(
    private val adsPref: AdsPref,
    private val connectivityChecker: ConnectivityChecker,
    private val adControlConfigManager: AdControlConfigManager
) : CheckAdEligibilityUseCase {

    override suspend operator fun invoke(context: Context): AdEligibility {
        val hasPremium = adsPref.getIsPremiumStatus() ||
            PrefUtil(context).getBool("is_premium", false) ||
            PrefUtil.isPremium(context) ||
            InAppPrefs.getInstance(context).premium

        return when {
            !adControlConfigManager.areAdsEnabled() ->
                AdEligibility(false, "Ads disabled by global config")
            hasPremium ->
                AdEligibility(false, "Premium user")
            !connectivityChecker.isConnected(context) ->
                AdEligibility(false, "No internet")
            else -> AdEligibility(true).also { Timber.d("Ad eligibility true") }
        }
    }
}
