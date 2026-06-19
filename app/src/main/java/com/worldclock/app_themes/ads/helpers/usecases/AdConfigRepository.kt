package com.worldclock.app_themes.ads.helpers.usecases

import com.worldclock.app_themes.ads.helpers.models.AdNetwork
import com.worldclock.app_themes.ads.helpers.models.AdWaterfallPlan

interface AdConfigRepository {
    fun initialize(onReady: () -> Unit, onFailed: () -> Unit)
    fun isConfigLoaded(): Boolean
    fun getInterstitialAdUnitId(screen: String, trigger: String): String
    fun getBannerAdUnitId(screen: String, position: String): String
    fun getNativeAdUnitId(screen: String, position: String): String

    fun getInterstitialAdUnitId(screen: String, trigger: String, network: AdNetwork): String
    fun getBannerAdUnitId(screen: String, position: String, network: AdNetwork): String
    fun getNativeAdUnitId(screen: String, position: String, network: AdNetwork): String
    fun getAppOpenAdUnitId(type: String, network: AdNetwork): String

    fun getInterstitialWaterfallPlan(screen: String, trigger: String): AdWaterfallPlan?
    fun getBannerWaterfallPlan(screen: String, position: String): AdWaterfallPlan?
    fun getAppOpenWaterfallPlan(type: String): AdWaterfallPlan?
    fun getNativeAdNetwork(screen: String, position: String): Int
    fun isNativeWaterfallEnabled(): Boolean
 }
