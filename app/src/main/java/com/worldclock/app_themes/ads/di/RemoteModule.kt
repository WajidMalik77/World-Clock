package com.worldclock.app_themes.ads.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.worldclock.app_themes.ads.config.AdControlConfigManager
import com.worldclock.app_themes.ads.config.NativeAdConfigManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return FirebaseRemoteConfig.getInstance()
    }

    @Provides
    @Singleton
    fun provideAdControlConfigManager(remoteConfig: FirebaseRemoteConfig): AdControlConfigManager {
        return AdControlConfigManager(remoteConfig)
    }

    @Provides
    @Singleton
    fun provideNativeAdConfigManager(remoteConfig: FirebaseRemoteConfig): NativeAdConfigManager {
        return NativeAdConfigManager(remoteConfig)
    }
}
