package com.worldclock.app_themes.ads.config.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BannerAds(
    val enabled: Int = 1,
    val placements: Map<String, Map<String, Int>> = emptyMap()
)