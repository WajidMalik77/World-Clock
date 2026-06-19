package com.worldclock.app_themes.ads.helpers.usecases

interface ShouldShowInterstitialUseCase {
    operator fun invoke(
        screen: String,
        trigger: String,
        noCounterNeeded: Boolean
    ): Boolean
}