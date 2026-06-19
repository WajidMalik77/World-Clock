package com.worldclock.app_themes.ads.helpers.models

sealed class AdResult {
    object Shown : AdResult()
    object NotShown : AdResult()
    data class Failed(val error: String) : AdResult()
}