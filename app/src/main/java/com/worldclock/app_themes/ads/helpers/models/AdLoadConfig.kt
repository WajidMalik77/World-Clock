package com.worldclock.app_themes.ads.helpers.models

data class AdLoadConfig(
    val screen: String,
    val trigger: String,
    val shouldShowProgressBar: Boolean = false
)