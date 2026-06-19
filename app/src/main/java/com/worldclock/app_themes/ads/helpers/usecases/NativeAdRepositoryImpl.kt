package com.worldclock.app_themes.ads.helpers.usecases

import com.worldclock.app_themes.ads.config.AdControlConfigManager
import com.worldclock.app_themes.ads.config.NativeAdConfigManager
import com.worldclock.app_themes.ads.config.models.NativeAdColorConfig
import javax.inject.Inject

class NativeAdRepositoryImpl @Inject constructor(
    private val adControlConfigManager: AdControlConfigManager,
    private val nativeAdConfigManager: NativeAdConfigManager
) : NativeAdRepository {

    override fun getNativeVisibility(screen: String, position: String): Boolean {
        if (!adControlConfigManager.areAdsEnabled()) return false
        if (adControlConfigManager.isRemoteAdsOverrideEnabled()) return true
        return nativeAdConfigManager.isNativeVisible(screen, position)
    }

    override fun shouldNativePreload(
        screen: String,
        position: String
    ): Boolean {
        if (!adControlConfigManager.areAdsEnabled()) return false
        if (adControlConfigManager.isRemoteAdsOverrideEnabled()) return false
        return nativeAdConfigManager.shouldNativePreload(screen, position)
    }

    override fun getNativeAdSize(screen: String, position: String): Int {
        return nativeAdConfigManager.getNativeAdSize(screen, position)
    }

    override fun getNativeAdColorConfig(
        screen: String,
        position: String
    ): NativeAdColorConfig? {
        return nativeAdConfigManager.getNativeAdColorConfig(screen, position)
    }
}
