package com.worldclock.app_themes.ads.utils

import com.worldclock.app_themes.BuildConfig

object AdUnitIdSanitizer {
    private const val GOOGLE_TEST_PUBLISHER_PREFIX = "ca-app-pub-3940256099942544"

    fun sanitizeBanner(adUnitId: String): String {
        return sanitize(adUnitId, ADS.PROD_ADMOB_BANNER_AD_ID)
    }

    fun sanitizeNative(adUnitId: String): String {
        return sanitize(adUnitId, ADS.PROD_ADMOB_NATIVE_AD_ID)
    }

    fun sanitizeInterstitial(adUnitId: String): String {
        return sanitize(adUnitId, ADS.PROD_ADMOB_INTERSTITIAL_AD_ID)
    }

    fun sanitizeAppOpen(adUnitId: String, fallbackId: String = ADS.PROD_ADMOB_APP_OPEN_ID): String {
        return sanitize(adUnitId, fallbackId)
    }

    private fun sanitize(adUnitId: String, releaseFallback: String): String {
        if (adUnitId.isBlank()) return releaseFallback
        if (BuildConfig.DEBUG) return adUnitId
        return if (adUnitId.startsWith(GOOGLE_TEST_PUBLISHER_PREFIX)) {
            releaseFallback
        } else {
            adUnitId
        }
    }
}
