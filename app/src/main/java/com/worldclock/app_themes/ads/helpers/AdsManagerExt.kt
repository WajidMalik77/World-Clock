package com.worldclock.app_themes.ads.helpers


import android.app.Activity
import timber.log.Timber
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStateAtLeast
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.worldclock.app_themes.R
import com.worldclock.app_themes.ads.di.AdsManagerEntryPoint
import com.worldclock.app_themes.ads.helpers.models.BannerConfig
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import com.worldclock.app_themes.ads.helpers.models.NativeAdEvent
import com.worldclock.app_themes.ads.helpers.ui.AdsManager

/**
 * Get AdsManager instance for Fragment
 * Cached to avoid repeated EntryPoint lookups
 */
import java.util.WeakHashMap

private val adsManagerCache = WeakHashMap<Activity, AdsManager>()
private const val TAG_INTER = "InterstitialTrace"
private val PRE_HOME_COUNTER_BYPASS_SCREENS = setOf(
    "SplashScreen",
    "LanguagesScreen",
    "IntroScreen",
    "OnBoardingScreen",
    "PremiumScreen"
)

private fun Activity.resolveAdsManager(): AdsManager? {
    val cachedManager = adsManagerCache[this]
    if (cachedManager != null) {
        (this as? ComponentActivity)?.lifecycle?.addObserver(cachedManager)
        return cachedManager
    }

    val manager = try {
        val entryPoint = EntryPointAccessors.fromActivity(
            this,
            AdsManagerEntryPoint::class.java
        )
        entryPoint.adsManager()
    } catch (e: Exception) {
        Timber.e(e, "Failed to get AdsManager for activity=${this::class.java.name}")
        null
    } ?: return null

    adsManagerCache[this] = manager
    (this as? ComponentActivity)?.lifecycle?.addObserver(manager)
    return manager
}

private val Fragment.cachedAdsManager: AdsManager?
    get() {
        if (!isAdded) return null
        val act = activity ?: return null

        val cachedManager = adsManagerCache[act]
        if (cachedManager != null) {
            (act as? ComponentActivity)?.lifecycle?.addObserver(cachedManager)
            return cachedManager
        }

        val manager = try {
            val entryPoint = EntryPointAccessors.fromActivity(
                act,
                AdsManagerEntryPoint::class.java
            )
            entryPoint.adsManager()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get AdsManager for activity=${act::class.java.name}")
            null
        } ?: return null

        adsManagerCache[act] = manager
        (act as? ComponentActivity)?.lifecycle?.addObserver(manager)
        return manager
    }

/**
 * Initialize ads in Fragment
 */
fun Fragment.initializeAds(screen: String, trigger: String) {
    val activity = activity ?: return
    cachedAdsManager?.initializeAds(activity, screen, trigger)
}

/**
 * Initialize ads without trigger
 */
fun Fragment.initializeAdsSimple(
    onLoaded: () -> Unit = {},
    onFailed: () -> Unit = {}
) {
    val activity = activity ?: return
    cachedAdsManager?.initializeAdsSimple(activity, onLoaded, onFailed)
}

/**
 * Load banner ads with simplified configuration
 */
fun Fragment.loadBannerAds(
    screen: String,
    topContainer: FrameLayout? = null,
    topShimmer: View? = null,
    bottomContainer: FrameLayout? = null,
    bottomShimmer: View? = null
) {
    val adsManager = cachedAdsManager ?: run {
        Timber.e("[$screen] AdsManager unavailable, skipping banner load")
        return
    }
    val context = context ?: return

    viewLifecycleOwner.lifecycleScope.launch {
        val configs = buildList {
            if (topContainer != null && topShimmer != null) {
                add(BannerConfig("top", topContainer, topShimmer))
            }
        }

        if (configs.isNotEmpty()) {
            adsManager.loadBannerAds(context, screen, configs)
        }
    }
}

/**
 * Load native ads with simplified configuration
 */
fun Fragment.loadNativeAds(
    screen: String,
    topContainer: FrameLayout? = null,
    topShimmer: FrameLayout? = null,
    centerContainer: FrameLayout? = null,
    centerShimmer: FrameLayout? = null,
    bottomContainer: FrameLayout? = null,
    bottomShimmer: FrameLayout? = null,
    onEvent: ((NativeAdEvent) -> Unit)? = null
) {
    val adsManager = cachedAdsManager ?: run {
        Timber.e("[$screen] AdsManager is null")
        return
    }

    val context = context ?: return

    viewLifecycleOwner.lifecycleScope.launch {
        val configs = buildList {
            if (centerContainer != null && centerShimmer != null) {
                add(NativeAdConfig("center", centerContainer, centerShimmer))
            }
            if (bottomContainer != null && bottomShimmer != null) {
                add(NativeAdConfig("bottom", bottomContainer, bottomShimmer))
            }
        }

        if (configs.isEmpty()) {
            Timber.w("[$screen] No native ad configs")
            return@launch
        }

        adsManager.loadNativeAds(context, screen, configs, onEvent)
    }
}

/**
 * Show interstitial with navigation
 */
fun Fragment.showInterstitialAndNavigate(
    screen: String,
    trigger: String,
    noCounterNeeded: Boolean = false,
    onAdResult: (wasAdShown: Boolean) -> Unit = {}
) {
    val effectiveNoCounter = noCounterNeeded || PRE_HOME_COUNTER_BYPASS_SCREENS.contains(screen)
    Timber.tag(TAG_INTER).d("showInterstitialAndNavigate screen=$screen trigger=$trigger noCounterNeeded=$noCounterNeeded effectiveNoCounter=$effectiveNoCounter")
    val activity = activity ?: run {
        Timber.tag(TAG_INTER).d("showInterstitialAndNavigate blocked: activity null")
        onAdResult(false)
        return
    }

    if (!isAdded || activity.isFinishing) {
        Timber.tag(TAG_INTER).d("showInterstitialAndNavigate blocked: fragment not added or activity finishing")
        onAdResult(false)
        return
    }

    viewLifecycleOwner.lifecycleScope.launch {
        cachedAdsManager?.showInterstitialAd(
            activity,
            screen,
            trigger,
            effectiveNoCounter,
            onAdClosed = { onAdResult(true) },
            onAdNotShown = { onAdResult(false) }
        ) ?: onAdResult(false)
    }
}

/**
 * Show interstitial (must show)
 */
fun Fragment.showInterstitialMust(
    onAdResult: (wasAdShown: Boolean) -> Unit = {}
) {
    val activity = activity ?: run {
        onAdResult(false)
        return
    }

    if (!isAdded || view == null || activity.isFinishing) {
        Timber.w("Fragment not ready for interstitial")
        onAdResult(false)
        return
    }

    viewLifecycleOwner.lifecycleScope.launch {
        cachedAdsManager?.showInterstitialMust(
            activity,
            onAdNotShown = { onAdResult(false) },
            onAdClosed = { onAdResult(true) }
        ) ?: onAdResult(false)
    }
}



fun Fragment.safeShowInterstitialNavigate(
    screenName: String,
    direction: String,
    noCounterNeeded: Boolean = false,
    afterAd: (() -> Unit)? = null
) {
    Timber.tag(TAG_INTER).d("safeShowInterstitialNavigate screen=$screenName trigger=$direction noCounterNeeded=$noCounterNeeded")
    // Abort if fragment is not attached
    if (!isAdded) {
        Timber.tag(TAG_INTER).d("safeShowInterstitialNavigate blocked: fragment not added")
        afterAd?.invoke() // fallback
        return
    }

    // Use activity context for ad
    val activityContext = activity
    if (activityContext == null || activityContext.isFinishing) {
        Timber.tag(TAG_INTER).d("safeShowInterstitialNavigate blocked: activity null/finishing")
        afterAd?.invoke()
        return
    }

    try {
        // Show the interstitial ad
        showInterstitialAndNavigate(screenName, direction, noCounterNeeded) { adShown ->
            Timber.tag(TAG_INTER).d("safeShowInterstitialNavigate result adShown=$adShown screen=$screenName trigger=$direction")
            // Wait for the fragment to resume before navigating — the interstitial
            // dismissal callback fires while the fragment is still in the background,
            // so navigating immediately causes NavController to silently drop the call.
            lifecycleScope.launch {
                lifecycle.withStateAtLeast(Lifecycle.State.RESUMED) {
                    if (!isAdded) return@withStateAtLeast
                    try {
                        afterAd?.invoke()
                    } catch (e: Exception) {
                        Timber.e(e, "🚫 Error executing after-ad action: ${e.message}")
                    }
                }
            }
        }
    } catch (e: Exception) {
        Timber.e(e, "🚫 Failed to show interstitial: ${e.message}")
        afterAd?.invoke()
    }
}

fun Activity.safeShowInterstitialAction(
    screenName: String,
    trigger: String,
    noCounterNeeded: Boolean = false,
    afterAd: (() -> Unit)? = null
) {
    if (isFinishing || isDestroyed) {
        afterAd?.invoke()
        return
    }
    val adsManager = resolveAdsManager()
    if (adsManager == null) {
        afterAd?.invoke()
        return
    }
    val effectiveNoCounter = noCounterNeeded || PRE_HOME_COUNTER_BYPASS_SCREENS.contains(screenName)
    val launch = {
        (this as? ComponentActivity)?.lifecycleScope?.launch {
            adsManager.showInterstitialAd(
                activity = this@safeShowInterstitialAction,
                screen = screenName,
                trigger = trigger,
                noCounterNeeded = effectiveNoCounter,
                onAdClosed = { afterAd?.invoke() },
                onAdNotShown = { afterAd?.invoke() }
            )
        } ?: afterAd?.invoke()
    }
    launch()
}

fun Activity.keepCurrentNativeOnNextPause() {
    resolveAdsManager()?.keepCurrentNativeOnNextPause()
}

fun Activity.keepCurrentNativeForNextContainer() {
    resolveAdsManager()?.keepCurrentNativeForNextContainer()
}

fun Activity.loadBottomNative(
    screen: String,
    nativeScreen: String? = null,
    topBannerLayout: ViewGroup? = null,
    topNativeLayout: ViewGroup? = null,
    bottomLayout: ViewGroup? = null,
    loadBannerAds: Boolean = true,
    onEvent: ((NativeAdEvent) -> Unit)? = null
) {
    val adsManager = resolveAdsManager() ?: return
    val topBannerView = topBannerLayout
    val topNativeView = topNativeLayout
    val bottomView = bottomLayout ?: return
    val topBannerContainer = topBannerView?.findViewById<FrameLayout>(R.id.admob_banner)
    val topBannerShimmer = topBannerView?.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
    val topNativeContainer = topNativeView?.findViewById<FrameLayout>(R.id.admob_native)
    val topNativeShimmer = topNativeView?.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
    val nativeContainer = bottomView.findViewById<FrameLayout>(R.id.admob_native)
    val nativeShimmer = bottomView.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
    val nativeTargetScreen = nativeScreen ?: screen

    bottomView.post {
        if (isFinishing || isDestroyed) return@post
        (this as? ComponentActivity)?.lifecycleScope?.launch {
            if (loadBannerAds) {
                if (topBannerContainer != null && topBannerShimmer != null) {
                    adsManager.loadBannerAd(
                        this@loadBottomNative,
                        screen,
                        "top",
                        topBannerContainer,
                        topBannerShimmer
                    )
                }
            }

            val nativeConfigs = buildList {
                if (topNativeContainer != null && topNativeShimmer != null) {
                    add(NativeAdConfig("top", topNativeContainer, topNativeShimmer))
                }
                if (nativeContainer != null && nativeShimmer != null) {
                    add(NativeAdConfig("bottom", nativeContainer, nativeShimmer))
                }
            }

            if (nativeConfigs.isNotEmpty()) {
                if (nativeConfigs.all { it.container.childCount > 0 }) {
                    nativeConfigs.forEach { onEvent?.invoke(NativeAdEvent.Loaded(it.position)) }
                    return@launch
                }
                adsManager.loadNativeAds(
                    this@loadBottomNative,
                    nativeTargetScreen,
                    nativeConfigs,
                    onEvent
                )
            }
        }
    }
}

/**
 * Check if ads should be loaded
 */
suspend fun Fragment.shouldLoadAds(): Boolean {
    if (!isAdded || view == null || activity == null) {
        Timber.w("Fragment not attached")
        return false
    }

    val context = requireContext()
    val adsManager = cachedAdsManager ?: return false

    return try {
        adsManager.shouldLoadAds(context)
    } catch (e: Exception) {
        Timber.w(e, "Error checking shouldLoadAds")
        Firebase.analytics.logEvent("ads_check_error") {
            param("fragment", this@shouldLoadAds::class.simpleName ?: "Unknown")
            param("error", e.localizedMessage ?: "unknown")
        }
        false
    }
}

/**
 * Load standard screen ads (all types)
 */
fun Fragment.loadStandardScreenAds(
    screen: String,
    nativeScreen: String? = null,
    bannerTopLayout: ViewGroup? = null,
    bannerBottomLayout: ViewGroup? = null,
    nativeTopLayout: ViewGroup? = null,
    nativeCenterLayout: ViewGroup? = null,
    nativeBottomLayout: ViewGroup? = null,
    onEvent: ((NativeAdEvent) -> Unit)? = null
) {
    // Post all operations to the view queue to ensure layout is complete
    val fragmentView = view ?: return
    fragmentView.post {
        if (!isAdded) return@post

        // Extract banner views
        val bannerTopContainer = bannerTopLayout?.findViewById<FrameLayout>(R.id.admob_banner)
        val bannerTopShimmer = bannerTopLayout?.findViewById<ShimmerFrameLayout>(R.id.banner_ad_shimmer)
        loadBannerAds(
            screen,
            bannerTopContainer,
            bannerTopShimmer,
            null,
            null
        )

        // Extract native views
        val nativeTopContainer = nativeTopLayout?.findViewById<FrameLayout>(R.id.admob_native)
        val nativeTopShimmer = nativeTopLayout?.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        val nativeCenterContainer = nativeCenterLayout?.findViewById<FrameLayout>(R.id.admob_native)
        val nativeCenterShimmer = nativeCenterLayout?.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)
        val nativeBottomContainer = nativeBottomLayout?.findViewById<FrameLayout>(R.id.admob_native)
        val nativeBottomShimmer = nativeBottomLayout?.findViewById<ShimmerFrameLayout>(R.id.native_ad_shimmer)

        // Load natives
        loadNativeAds(
            nativeScreen ?: screen,
            nativeTopContainer,
            nativeTopShimmer,
            nativeCenterContainer,
            nativeCenterShimmer,
            nativeBottomContainer,
            nativeBottomShimmer,
            onEvent
        )
    }
}
