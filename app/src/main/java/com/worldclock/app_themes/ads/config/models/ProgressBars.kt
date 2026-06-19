package com.worldclock.app_themes.ads.config.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProgressBars(
    val interstitial: Int = 0,
    @SerialName("app_open")
    val appOpen: Int = 0,
    @SerialName("app_open_splash")
    val appOpenSplash: Int = 0
)