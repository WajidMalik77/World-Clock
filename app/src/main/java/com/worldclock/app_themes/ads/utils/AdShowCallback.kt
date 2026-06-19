package com.worldclock.app_themes.ads.utils

interface AdShowCallback {
    fun onAdShown()
    fun onAdFailedToShow(adError: String)
    fun onAdDismissed()
}