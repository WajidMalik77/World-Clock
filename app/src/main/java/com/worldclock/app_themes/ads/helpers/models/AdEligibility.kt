package com.worldclock.app_themes.ads.helpers.models

data class AdEligibility(
    val canShowAds: Boolean,
    val reason: String? = null
)