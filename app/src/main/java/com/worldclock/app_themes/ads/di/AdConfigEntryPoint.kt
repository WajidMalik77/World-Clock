package com.worldclock.app_themes.ads.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import com.worldclock.app_themes.ads.AppPrefsManager
import com.worldclock.app_themes.ads.config.AdControlConfigManager
import com.worldclock.app_themes.ads.config.NativeAdConfigManager

@EntryPoint
@InstallIn(ActivityComponent::class)
interface AdConfigEntryPoint {
    fun adControlConfigManager(): AdControlConfigManager
    fun nativeAdConfigManager(): NativeAdConfigManager
    fun appPrefsManager(): AppPrefsManager
}
