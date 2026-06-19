package com.worldclock.app_themes.ads.di

import android.app.Activity
import android.content.Context
import com.worldclock.app_themes.ads.managers.AdmobNativeManager
import com.worldclock.app_themes.ads.managers.BannerAdsManager
import com.worldclock.app_themes.ads.managers.NativeAdCache
import com.worldclock.app_themes.ads.utils.AdsPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FragmentBanner

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ActivityBanner

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FragmentNative

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ActivityNative

@Module
@InstallIn(FragmentComponent::class)
object AdModule {

    @Provides
    @FragmentBanner
    fun provideBannerAdsManager(
        @ActivityContext context: Context,
        adsPref: AdsPref
    ): BannerAdsManager = BannerAdsManager(context as Activity, adsPref)

    @Provides
    @FragmentNative
    fun provideAdmobNativeManager(
        @ActivityContext context: Context,
        adsPref: AdsPref,
        nativeAdCache: NativeAdCache
    ): AdmobNativeManager = AdmobNativeManager(context as Activity, adsPref, nativeAdCache)
}

@Module
@InstallIn(ActivityComponent::class)
object ActivityAdModule {

    @Provides
    @ActivityScoped
    @ActivityBanner
    fun provideActivityBannerAdsManager(
        @ActivityContext context: Context,
        adsPref: AdsPref
    ): BannerAdsManager = BannerAdsManager(context as Activity, adsPref)

    @Provides
    @ActivityScoped
    @ActivityNative
    fun provideActivityAdmobNativeManager(
        @ActivityContext context: Context,
        adsPref: AdsPref,
        nativeAdCache: NativeAdCache
    ): AdmobNativeManager = AdmobNativeManager(context as Activity, adsPref, nativeAdCache)
}