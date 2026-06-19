package com.worldclock.app_themes.ads.app

import android.content.Context
import com.worldclock.app_themes.core.utils.InAppPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.worldclock.app_themes.ads.helpers.models.PremiumState
import com.worldclock.app_themes.ads.utils.AdsPref
import com.worldclock.app_themes.core.utils.PrefUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumRepository @Inject constructor(
    private val adsPref: AdsPref,
    @ApplicationContext private val appContext: Context
) {
    private val _premiumState = MutableStateFlow(PremiumState.UNKNOWN)
    val premiumState: StateFlow<PremiumState> = _premiumState.asStateFlow()

    fun updatePremiumState(isPremium: Boolean) {
        _premiumState.value = if (isPremium) PremiumState.PREMIUM else PremiumState.FREE
    }

    fun isPremiumUser(): Boolean {
        if (adsPref.getIsPremiumStatus()) return true
        if (PrefUtil(appContext).getBool("is_premium", false)) return true
        if (PrefUtil.isPremium(appContext)) return true
        return InAppPrefs.getInstance(appContext).premium
    }
}
