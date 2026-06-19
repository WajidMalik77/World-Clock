package com.worldclock.app_themes.ads.config.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class LaunchScreens(
    val splash: Int = 1,
    val languages: Int? = null,
    val intro: Int? = null,
    val premium: Int? = null
)
