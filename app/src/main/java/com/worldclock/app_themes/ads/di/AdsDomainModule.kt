package com.worldclock.app_themes.ads.di

import com.worldclock.app_themes.ads.AppPrefsManager
import com.worldclock.app_themes.ads.config.AdControlConfigManager
import com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator
import com.worldclock.app_themes.ads.helpers.ui.AdsManager
import com.worldclock.app_themes.ads.helpers.ui.InterstitialAdOrchestrator
import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.usecases.AdConfigRepository
import com.worldclock.app_themes.ads.helpers.usecases.AdConfigRepositoryImpl
import com.worldclock.app_themes.ads.helpers.usecases.BannerAdRepository
import com.worldclock.app_themes.ads.helpers.usecases.BannerAdRepositoryImpl
import com.worldclock.app_themes.ads.helpers.usecases.CheckAdEligibilityUseCase
import com.worldclock.app_themes.ads.helpers.usecases.CheckAdEligibilityUseCaseImpl
import com.worldclock.app_themes.ads.helpers.usecases.ConnectivityChecker
import com.worldclock.app_themes.ads.helpers.usecases.ConnectivityCheckerImpl
import com.worldclock.app_themes.ads.helpers.usecases.NativeAdRepository
import com.worldclock.app_themes.ads.helpers.usecases.NativeAdRepositoryImpl
import com.worldclock.app_themes.ads.helpers.usecases.ShouldShowInterstitialUseCase
import com.worldclock.app_themes.ads.helpers.usecases.ShouldShowInterstitialUseCaseImpl
import com.worldclock.app_themes.ads.managers.AdmobNativeManager
import com.worldclock.app_themes.ads.managers.BannerAdsManager
import com.worldclock.app_themes.ads.utils.AdsPref
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdsDomainModule {

    @Binds
    @Singleton
    abstract fun bindCheckAdEligibilityUseCase(
        impl: CheckAdEligibilityUseCaseImpl
    ): CheckAdEligibilityUseCase

    @Binds
    @Singleton
    abstract fun bindShouldShowInterstitialUseCase(
        impl: ShouldShowInterstitialUseCaseImpl
    ): ShouldShowInterstitialUseCase

    @Binds
    @Singleton
    abstract fun bindConnectivityChecker(
        impl: ConnectivityCheckerImpl
    ): ConnectivityChecker
}

// Data Module
@Module
@InstallIn(SingletonComponent::class)
abstract class AdsDataModule {

    @Binds
    @Singleton
    abstract fun bindAdConfigRepository(
        impl: AdConfigRepositoryImpl
    ): AdConfigRepository

    @Binds
    @Singleton
    abstract fun bindBannerAdRepository(
        impl: BannerAdRepositoryImpl
    ): BannerAdRepository

    @Binds
    @Singleton
    abstract fun bindNativeAdRepository(
        impl: NativeAdRepositoryImpl
    ): NativeAdRepository
}

// Presentation Module
@Module
@InstallIn(ActivityComponent::class)
object AdsPresentationModule {

    @Provides
    @ActivityScoped
    fun provideAdsManager(
        adConfigRepository: AdConfigRepository,
        bannerAdOrchestrator: BannerAdOrchestrator,
        nativeAdOrchestrator: NativeAdOrchestrator,
        interstitialAdOrchestrator: InterstitialAdOrchestrator,
        checkEligibility: CheckAdEligibilityUseCase
    ): AdsManager {
        return AdsManager(
            adConfigRepository,
            bannerAdOrchestrator,
            nativeAdOrchestrator,
            interstitialAdOrchestrator,
            checkEligibility
        )
    }

    @Provides
    @ActivityScoped
    fun provideBannerAdOrchestrator(
        bannerAdRepository: BannerAdRepository,
        adConfigRepository: AdConfigRepository,
        @ActivityBanner bannerAdsManager: BannerAdsManager,
        checkEligibility: CheckAdEligibilityUseCase
    ): BannerAdOrchestrator {
        return BannerAdOrchestrator(
            bannerAdRepository,
            adConfigRepository,
            bannerAdsManager,
            checkEligibility
        )
    }

    @Provides
    @ActivityScoped
    fun provideNativeAdOrchestrator(
        nativeAdRepository: NativeAdRepository,
        adConfigRepository: AdConfigRepository,
        @ActivityNative admobNativeManager: AdmobNativeManager,
        checkEligibility: CheckAdEligibilityUseCase
    ): NativeAdOrchestrator {
        return NativeAdOrchestrator(
            nativeAdRepository,
            adConfigRepository,
            admobNativeManager,
            checkEligibility
        )
    }

    @Provides
    @ActivityScoped
    fun provideInterstitialAdOrchestrator(
        adsPref: AdsPref,
        prefsManager: AppPrefsManager,
        adConfigRepository: AdConfigRepository,
        shouldShowInterstitial: ShouldShowInterstitialUseCase,
        checkEligibility: CheckAdEligibilityUseCase
    ): InterstitialAdOrchestrator {
        return InterstitialAdOrchestrator(
            adsPref,
            prefsManager,
            adConfigRepository,
            shouldShowInterstitial,
            checkEligibility
        )
    }
}
