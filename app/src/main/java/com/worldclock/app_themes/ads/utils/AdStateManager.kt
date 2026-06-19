package com.worldclock.app_themes.ads.utils

object AdStateManager {
    var isInterstitialAdShowing: Boolean = false
    var isAppOpenAdShowing = false

    fun isAnyAdShowing() = isInterstitialAdShowing ||
            isAppOpenAdShowing
}
