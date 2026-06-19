package com.worldclock.app_themes.ads.helpers.usecases

import com.worldclock.app_themes.ads.config.AdControlConfigManager
import javax.inject.Inject

class BannerAdRepositoryImpl @Inject constructor(
    private val adControlConfigManager: AdControlConfigManager
) : BannerAdRepository {

    override fun getBannerVisibility(screen: String, position: String): Boolean {
        return adControlConfigManager.isBannerVisible(screen, position)
    }

    override fun getBannerType(screen: String, position: String): String {
        return adControlConfigManager.getBannerType(screen, position)
    }
}