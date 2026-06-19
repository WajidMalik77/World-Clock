package com.worldclock.app_themes.ads.di

import com.worldclock.app_themes.ads.helpers.ui.AdsManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface AdsManagerEntryPoint {
    fun adsManager(): AdsManager
}