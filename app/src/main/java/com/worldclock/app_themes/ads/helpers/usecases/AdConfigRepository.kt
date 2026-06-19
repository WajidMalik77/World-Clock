package com.worldclock.app_themes.ads.helpers.usecases

interface AdConfigRepository {
    fun initialize(onReady: () -> Unit, onFailed: () -> Unit)
    fun isConfigLoaded(): Boolean
    fun getInterstitialAdUnitId(screen: String, trigger: String): String
    fun getBannerAdUnitId(screen: String, position: String): String
    fun getNativeAdUnitId(screen: String, position: String): String
 }
