package com.worldclock.app_themes.ads.config.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InterstitialAds(
    val enabled: Int = 1,
    @SerialName("show_after")
    val showAfter: Int = 1,
    @SerialName("click_interval")
    val clickInterval: Int = 1,
    @SerialName("isInterFirstCount")
    val isInterFirstCount: Int = 1,
    @SerialName("cooldown_seconds")
    val cooldownSeconds: Int = 0,
    val screens: Map<String, Map<String, Int>> = emptyMap()
)
