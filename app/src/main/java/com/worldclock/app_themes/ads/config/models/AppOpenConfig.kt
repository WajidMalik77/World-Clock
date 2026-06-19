package com.worldclock.app_themes.ads.config.models

import kotlinx.serialization.Serializable

@Serializable
data class AppOpenConfig(
    val enabled: Int = 1,
    val splash: Int = 1,
    val resume: Int = 1,
    @kotlinx.serialization.SerialName("resume_min_background_seconds")
    val resumeMinBackgroundSeconds: Int = 0
)
