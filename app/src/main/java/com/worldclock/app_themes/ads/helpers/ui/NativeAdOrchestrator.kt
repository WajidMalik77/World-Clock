package com.worldclock.app_themes.ads.helpers.ui

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.ads.LoadAdError
import dagger.hilt.android.scopes.ActivityScoped
import com.worldclock.app_themes.R
import com.worldclock.app_themes.ads.config.models.NativeAdColorConfig
import com.worldclock.app_themes.ads.di.ActivityNative
import com.worldclock.app_themes.ads.helpers.models.AdNetwork
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import com.worldclock.app_themes.ads.helpers.models.NativeAdEvent
import com.worldclock.app_themes.ads.helpers.usecases.AdConfigRepository
import com.worldclock.app_themes.ads.helpers.usecases.CheckAdEligibilityUseCase
import com.worldclock.app_themes.ads.helpers.usecases.NativeAdRepository
import com.worldclock.app_themes.ads.managers.AdmobNativeManager
import com.worldclock.app_themes.ads.managers.facebook.FbAdInitializer
import com.worldclock.app_themes.ads.managers.facebook.FbNativeAdManager
import timber.log.Timber
import javax.inject.Inject
import kotlin.collections.all

@ActivityScoped
class NativeAdOrchestrator @Inject constructor(
    private val nativeAdRepository: NativeAdRepository,
    private val adConfigRepository: AdConfigRepository,
    @param:ActivityNative private val admobNativeManager: AdmobNativeManager,
    @param:ActivityNative private val fbNativeAdManager: FbNativeAdManager,
    private val checkEligibility: CheckAdEligibilityUseCase
) {

    private val impressedPositions = mutableSetOf<String>()

    private fun networkFromValue(value: Int): AdNetwork = when (value) {
        2 -> AdNetwork.FACEBOOK
        else -> AdNetwork.ADMOB
    }

    private fun otherNetwork(network: AdNetwork): AdNetwork =
        if (network == AdNetwork.FACEBOOK) AdNetwork.ADMOB else AdNetwork.FACEBOOK

    suspend fun loadNativeAds(
        context: Context,
        screen: String,
        nativeConfigs: List<NativeAdConfig>,
        forceRefresh: Boolean = false,
        onEvent: ((NativeAdEvent) -> Unit)? = null
    ) {
        // Check eligibility
        val eligibility = checkEligibility(context)
        if (!eligibility.canShowAds) {
            Timber.w("[$screen] ${eligibility.reason} — skipping ads")
            nativeConfigs.forEach { hideNativeSlot(it) }
            onEvent?.invoke(NativeAdEvent.AllOffFromConfig)
            return
        }

        // Check if any ads are visible
        val allAdsInvisible = nativeConfigs.all {
            !nativeAdRepository.getNativeVisibility(screen, it.position)
        }

        if (allAdsInvisible) {
            Timber.w("[$screen] No visible native ads for this screen")
            nativeConfigs.forEach { hideNativeSlot(it) }
            onEvent?.invoke(NativeAdEvent.AllOffFromConfig)
            return
        }

        // Load each native ad
        nativeConfigs.forEach { config ->
            val isImpressed = impressedPositions.contains(config.position) || admobNativeManager.isImpressed()
            val shouldRefresh = forceRefresh || isImpressed
            if (shouldRefresh) {
                admobNativeManager.clearCurrentAd()
                impressedPositions.remove(config.position)
            }

            val wrappedEvent: (NativeAdEvent) -> Unit = { event ->
                if (event is NativeAdEvent.Impression) {
                    impressedPositions.add(event.position)
                }
                onEvent?.invoke(event)
            }

            loadSingleNativeAd(screen, config, wrappedEvent)
        }
    }

    suspend fun preloadNativeForNextContainer(
        context: Context,
        screen: String,
        position: String
    ) {
        if (position.equals("top", ignoreCase = true) && !screen.equals("SplashScreen", ignoreCase = true)) {
            Timber.d("[$screen][$position] Top native preload disabled")
            return
        }
        val eligibility = checkEligibility(context)
        if (!eligibility.canShowAds) {
            Timber.w("[$screen][$position] ${eligibility.reason} — skipping native preload")
            return
        }
        if (!nativeAdRepository.getNativeVisibility(screen, position)) {
            Timber.d("[$screen][$position] Native preload off from config")
            return
        }

        val size = nativeAdRepository.getNativeAdSize(screen, position)
        val adUnitId = adConfigRepository.getNativeAdUnitId(screen, position)
        admobNativeManager.preloadNativeForNextContainer(
            adUnitId = adUnitId,
            layoutRes = layoutResForSize(screen, position, size),
            onLoaded = {
                Timber.i("[$screen][$position] Native preloaded for next container")
            },
            onFailed = { error ->
                Timber.e("[$screen][$position] Native preload failed: ${error.message}")
            }
        )
    }

    private fun loadSingleNativeAd(
        screen: String,
        config: NativeAdConfig,
        onEvent: ((NativeAdEvent) -> Unit)?
    ) {
        val position = config.position
        if (position.equals("top", ignoreCase = true) && !screen.equals("SplashScreen", ignoreCase = true)) {
            Timber.d("[$screen][$position] Top native disabled")
            hideNativeSlot(config)
            onEvent?.invoke(NativeAdEvent.Off(position))
            return
        }
        val visible = nativeAdRepository.getNativeVisibility(screen, position)

        if (!visible) {
            Timber.d("[$screen][$position] Native ad off from config")
            hideNativeSlot(config)
            onEvent?.invoke(NativeAdEvent.Off(position))
            return
        }

        val size = nativeAdRepository.getNativeAdSize(screen, position)
        val rawTheme = nativeAdRepository.getNativeAdColorConfig(screen, position)
        val theme = rawTheme?.resolveForCurrentTheme(config.container.context)
        val shouldPreload = nativeAdRepository.shouldNativePreload(screen, position)

        val networkValue = adConfigRepository.getNativeAdNetwork(screen, position)
        if (networkValue == 0) {
            Timber.d("[$screen][$position] Native ad network off from config")
            hideNativeSlot(config)
            onEvent?.invoke(NativeAdEvent.Off(position))
            return
        }
        val primaryNetwork = networkFromValue(networkValue)
        val fallbackNetwork = if (adConfigRepository.isNativeWaterfallEnabled()) otherNetwork(primaryNetwork) else null
        if (primaryNetwork == AdNetwork.FACEBOOK || fallbackNetwork == AdNetwork.FACEBOOK) {
            FbAdInitializer.initialize(config.container.context.applicationContext)
        }
        val adUnitId = adConfigRepository.getNativeAdUnitId(screen, position, primaryNetwork)

        Timber.d("Should Native Preload: $screen : $position : $shouldPreload")
        loadNativeAdBySize(
            screen = screen,
            position = position,
            size = size,
            adUnitId = adUnitId,
            network = primaryNetwork,
            fallbackNetwork = fallbackNetwork,
            theme = theme,
            container = config.container,
            shimmer = config.shimmer,
            shouldPreload = shouldPreload,
            onLoaded = { onEvent?.invoke(NativeAdEvent.Loaded(position)) },
            onImpression = { onEvent?.invoke(NativeAdEvent.Impression(position)) },
            onFailed = { error -> onEvent?.invoke(NativeAdEvent.Failed(position, error)) }
        )
    }

    private fun loadNativeAdBySize(
        screen: String,
        position: String,
        size: Int,
        adUnitId: String,
        network: AdNetwork,
        fallbackNetwork: AdNetwork?,
        theme: NativeAdColorConfig?,
        container: FrameLayout,
        shimmer: FrameLayout,
        shouldPreload: Boolean = true,
        onLoaded: () -> Unit,
        onImpression: () -> Unit,
        onFailed: (LoadAdError) -> Unit
    ) {
        fun dispatch(
            net: AdNetwork,
            unitId: String,
            isFallback: Boolean,
            admobCall: (String, (LoadAdError) -> Unit) -> Unit,
            fbCall: (String, (String) -> Unit) -> Unit
        ) {
            val failHandler: (String) -> Unit = { message ->
                if (!isFallback && fallbackNetwork != null) {
                    Timber.d("[$screen][$position] Native network=$net failed ($message), trying fallback=$fallbackNetwork")
                    val fallbackId = adConfigRepository.getNativeAdUnitId(screen, position, fallbackNetwork)
                    dispatch(fallbackNetwork, fallbackId, true, admobCall, fbCall)
                } else {
                    onFailed(LoadAdError(0, message, "FbAdError", null, null))
                }
            }
            when (net) {
                AdNetwork.FACEBOOK -> fbCall(unitId, failHandler)
                else -> admobCall(unitId) { failHandler(it.message) }
            }
        }

        if (size == 1) {
            val isIntroFullScreen = (screen == "IntroScreen" ||
                    screen == "OnBoardingScreen" ||
                    screen == "IntroFullScreen") &&
                    (position.equals("fullScreen", ignoreCase = true) || position.startsWith("full_screen", ignoreCase = true))
            if (isIntroFullScreen) {
                dispatch(
                    net = network,
                    unitId = adUnitId,
                    isFallback = false,
                    admobCall = { unitId, onFail ->
                        admobNativeManager.loadNativeFullScreenIntroAd(
                            adContainer = container,
                            adUnitId = unitId,
                            shimmerContainer = shimmer,
                            colorConfig = theme,
                            shouldPreloadNext = shouldPreload,
                            onLoaded = {
                                Timber.i("Fullscreen intro native ad loaded")
                                onLoaded()
                            },
                            onImpression = onImpression,
                            onFailed = { error ->
                                Timber.e("Fullscreen intro native ad failed: ${error.message}")
                                onFail(error)
                            }
                        )
                    },
                    fbCall = { unitId, onFail ->
                        fbNativeAdManager.loadNativeFullScreenIntroAd(
                            adContainer = container,
                            adUnitId = unitId,
                            shimmerContainer = shimmer,
                            colorConfig = theme,
                            onLoaded = {
                                Timber.i("FB fullscreen intro native ad loaded")
                                onLoaded()
                            },
                            onImpression = onImpression,
                            onFailed = onFail
                        )
                    }
                )
                return
            }
            dispatch(
                net = network,
                unitId = adUnitId,
                isFallback = false,
                admobCall = { unitId, onFail ->
                    admobNativeManager.loadNativeSmallAd(
                        container, unitId, shimmer, R.layout.native_ad_shimmer_small, theme, shouldPreload,
                        onLoaded = {
                            Timber.i("Small native ad loaded")
                            onLoaded()
                        },
                        onImpression = onImpression,
                        onFailed = { error ->
                            Timber.e("Small native ad failed: ${error.message}")
                            onFail(error)
                        }
                    )
                },
                fbCall = { unitId, onFail ->
                    fbNativeAdManager.loadNativeSmallAd(
                        container, unitId, shimmer, R.layout.native_ad_shimmer_small, theme,
                        onLoaded = {
                            Timber.i("FB small native ad loaded")
                            onLoaded()
                        },
                        onImpression = onImpression,
                        onFailed = onFail
                    )
                }
            )
        } else {
            val layoutRes = layoutResForSize(screen, position, size)
            val shimmerRes = shimmerLayoutResForSize(size)

            dispatch(
                net = network,
                unitId = adUnitId,
                isFallback = false,
                admobCall = { unitId, onFail ->
                    admobNativeManager.loadNativeCustomAd(
                        adContainer = container,
                        adUnitId = unitId,
                        layoutRes = layoutRes,
                        shimmerContainer = shimmer,
                        shimmerLayout = shimmerRes,
                        shouldPreloadNext = shouldPreload,
                        colorConfig = theme,
                        onLoaded = {
                            Timber.i("Native ad loaded for size=$size (layout=$layoutRes)")
                            onLoaded()
                        },
                        onImpression = onImpression,
                        onFailed = { error ->
                            Timber.e("Native ad failed for size=$size: ${error.message}")
                            onFail(error)
                        }
                    )
                },
                fbCall = { unitId, onFail ->
                    fbNativeAdManager.loadNativeCustomAd(
                        adContainer = container,
                        adUnitId = unitId,
                        layoutRes = layoutRes,
                        shimmerContainer = shimmer,
                        shimmerLayout = shimmerRes,
                        colorConfig = theme,
                        onLoaded = {
                            Timber.i("FB native ad loaded for size=$size (layout=$layoutRes)")
                            onLoaded()
                        },
                        onImpression = onImpression,
                        onFailed = onFail
                    )
                }
            )
        }
    }

    private fun layoutResForSize(screen: String, position: String, size: Int): Int {
        val isIntroFullScreen = (screen == "IntroScreen" ||
                screen == "OnBoardingScreen" ||
                screen == "IntroFullScreen") &&
                (position.equals("fullScreen", ignoreCase = true) || position.startsWith("full_screen", ignoreCase = true))
        return when {
            size == 1 && isIntroFullScreen -> R.layout.native_ad_fullscreen_intro
            size == 1 -> R.layout.native_admob_small
            size == 2 -> R.layout.native_medium
            size == 3 -> R.layout.native_medium_top_cta
            size == 4 -> R.layout.native_medium_side_media
            size == 5 -> R.layout.native_medium_compact
            size == 6 -> R.layout.native_large_center
            size == 7 -> R.layout.native_full_media
            else -> R.layout.native_medium
        }
    }

    private fun shimmerLayoutResForSize(size: Int): Int {
        return when (size) {
            // placement_values:
            // 0=off, 1=small, 2=medium, 3=medium_top_cta,
            // 4=medium_side_media, 5=medium_compact, 6=large_center, 7=full_media
            2 -> R.layout.native_loading_medium
            3 -> R.layout.native_loading_medium_top_cta
            4 -> R.layout.native_loading_medium_side_media
            5 -> R.layout.native_loading_medium_compact
            6 -> R.layout.native_loading_large_center
            7 -> R.layout.native_loading_full_media
            else -> R.layout.native_loading_medium
        }
    }

    private fun hideNativeSlot(config: NativeAdConfig) {
        config.shimmer.visibility = View.GONE
        config.container.removeAllViews()
        config.container.visibility = View.GONE
    }

    private fun NativeAdColorConfig.resolveForCurrentTheme(context: Context): NativeAdColorConfig {
        val isNightMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

        val resolvedBackground = if (isNightMode) {
            darkBackgroundColorHex ?: darkBackgroundColorHexSnake ?: backgroundColorHex ?: lightBackgroundColorHex ?: lightBackgroundColorHexSnake
        } else {
            lightBackgroundColorHex ?: lightBackgroundColorHexSnake ?: backgroundColorHex ?: darkBackgroundColorHex ?: darkBackgroundColorHexSnake
        }
        val resolvedStroke = if (isNightMode) {
            darkStrokeColorHex ?: darkStrokeColorHexSnake ?: strokeColorHex ?: lightStrokeColorHex ?: lightStrokeColorHexSnake
        } else {
            lightStrokeColorHex ?: lightStrokeColorHexSnake ?: strokeColorHex ?: darkStrokeColorHex ?: darkStrokeColorHexSnake
        }
        val resolvedHeadlineRaw = if (isNightMode) {
            darkHeadlineColorHex ?: darkHeadlineColorHexSnake ?: headlineColorHex ?: lightHeadlineColorHex ?: lightHeadlineColorHexSnake
        } else {
            lightHeadlineColorHex ?: lightHeadlineColorHexSnake ?: headlineColorHex ?: darkHeadlineColorHex ?: darkHeadlineColorHexSnake
        }
        val resolvedBodyRaw = if (isNightMode) {
            darkBodyColorHex ?: darkBodyColorHexSnake ?: bodyTextColorHex ?: lightBodyColorHex ?: lightBodyColorHexSnake
        } else {
            lightBodyColorHex ?: lightBodyColorHexSnake ?: bodyTextColorHex ?: darkBodyColorHex ?: darkBodyColorHexSnake
        }
        val resolvedCtaBackground = if (isNightMode) {
            darkCtaBackgroundColorHex ?: darkCtaBackgroundColorHexSnake ?: ctaBackgroundColorHex ?: lightCtaBackgroundColorHex ?: lightCtaBackgroundColorHexSnake
        } else {
            lightCtaBackgroundColorHex ?: lightCtaBackgroundColorHexSnake ?: ctaBackgroundColorHex ?: darkCtaBackgroundColorHex ?: darkCtaBackgroundColorHexSnake
        }
        val resolvedCtaText = if (isNightMode) {
            darkCtaTextColorHex ?: darkCtaTextColorHexSnake ?: ctaTextColorHex ?: lightCtaTextColorHex ?: lightCtaTextColorHexSnake
        } else {
            lightCtaTextColorHex ?: lightCtaTextColorHexSnake ?: ctaTextColorHex ?: darkCtaTextColorHex ?: darkCtaTextColorHexSnake
        }

        val resolvedHeadline = if (!isNightMode && isEffectivelyWhite(resolvedHeadlineRaw)) {
            "#111111"
        } else {
            resolvedHeadlineRaw
        }
        val resolvedBody = if (!isNightMode && isEffectivelyWhite(resolvedBodyRaw)) {
            "#333333"
        } else {
            resolvedBodyRaw
        }

        return copy(
            backgroundColorHex = resolvedBackground,
            strokeColorHex = resolvedStroke,
            headlineColorHex = resolvedHeadline,
            bodyTextColorHex = resolvedBody,
            ctaBackgroundColorHex = resolvedCtaBackground,
            ctaTextColorHex = resolvedCtaText
        )
    }

    private fun isEffectivelyWhite(colorHex: String?): Boolean {
        if (colorHex.isNullOrBlank()) return false
        return when (colorHex.trim().lowercase()) {
            "#fff", "#ffff", "#ffffff", "#ffffffff" -> true
            else -> false
        }
    }

    fun releaseUnimpressedNative() {
        admobNativeManager.releaseUnimpressedForReuse()
    }

    fun keepCurrentNativeForNextContainer() {
        admobNativeManager.keepCurrentNativeForNextContainer()
    }

    fun destroy() {
        admobNativeManager.cleanup()
        fbNativeAdManager.cleanup()
    }
}
