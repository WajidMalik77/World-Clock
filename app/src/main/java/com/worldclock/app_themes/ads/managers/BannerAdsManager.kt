package com.worldclock.app_themes.ads.managers

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.worldclock.app_themes.ads.utils.AdUnitIdSanitizer
import com.worldclock.app_themes.ads.utils.AdsPref
import com.worldclock.app_themes.ads.utils.DebugToaster
import timber.log.Timber
import kotlin.also
import kotlin.apply
import kotlin.collections.forEach
import kotlin.jvm.java

class BannerAdsManager(
    private val activity: Activity,
    private val adsPref: AdsPref
) {
    private var adaptiveAdView: AdView? = null
    private var rectangleAdView: AdView? = null
    private var collapsibleTopBannerAdView: AdView? = null
    private var collapsibleBottomBannerAdView: AdView? = null
    private val activeBannerAds = mutableMapOf<FrameLayout, AdView>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val collapseRunnables = mutableMapOf<AdView, Runnable>()
    private val AUTO_COLLAPSE_DELAY = 3000L // 10 seconds
    private val DESTRUCTION_TIMEOUT = 2000L // 2 seconds timeout for destruction

    fun loadCollapsibleTopBanner(
        adContainer: FrameLayout,
        adUnitId: String,
        defaultVisibility: Int = View.VISIBLE,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((LoadAdError) -> Unit)? = null
    ) {
        if (defaultVisibility != View.VISIBLE) return
        val resolvedAdUnitId = AdUnitIdSanitizer.sanitizeBanner(adUnitId)

        // Clean up existing ad first
        collapsibleTopBannerAdView?.let { cleanupAdView(it, adContainer) }

        val adView = AdView(activity).apply {
            this.adUnitId = resolvedAdUnitId
            collapsibleTopBannerAdView = this
        }

        adContainer.post {
            if (adView.adSize == null) {
                val adSize = getAdSize(adContainer)
                adView.setAdSize(adSize)
            }

            (adView.parent as? ViewGroup)?.removeView(adView)
            adContainer.removeAllViews()
            adContainer.addView(adView)
            adContainer.visibility = View.GONE

            // Track this ad view
            activeBannerAds[adContainer] = adView

            val extras = Bundle().apply {
                putString("collapsible", "top")
                putString("collapsible_request_id", System.currentTimeMillis().toString())
            }

            val adRequest = AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()

            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    adContainer.visibility = View.VISIBLE
                    Timber.i("Top collapsible banner loaded: collapsible=${adView.isCollapsible}")
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastBannerEnabled(), "Banner (Top Col): Loaded")
                    // Setup auto-collapse
                    setupAutoCollapse(adView)

                    onAdLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    adContainer.visibility = View.GONE
                    Timber.d("Top collapsible banner ad failed: ${error.message}")
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastBannerEnabled(), "Banner (Top Col): Failed")
                    onAdFailed?.invoke(error)

                    // Retry with exponential backoff
                    adContainer.postDelayed({
                        if (!activity.isFinishing && !activity.isDestroyed) {
                            adView.loadAd(adRequest)
                        }
                    }, 5000)
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    cancelAutoCollapse(adView)
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    cancelAutoCollapse(adView)
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastBannerEnabled(), "Banner (Top Col): Shown")
                }
            }

            adView.loadAd(adRequest)
        }
    }

    fun loadCollapsibleBottomBanner(
        adContainer: FrameLayout,
        adUnitId: String,
        defaultVisibility: Int = View.VISIBLE,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((LoadAdError) -> Unit)? = null
    ) {
        if (defaultVisibility != View.VISIBLE) return
        val resolvedAdUnitId = AdUnitIdSanitizer.sanitizeBanner(adUnitId)

        // Clean up existing ad first
        collapsibleBottomBannerAdView?.let { cleanupAdView(it, adContainer) }

        val adView = AdView(activity).apply {
            this.adUnitId = resolvedAdUnitId
            collapsibleBottomBannerAdView = this
        }

        adContainer.post {
            if (adView.adSize == null) {
                val adSize = getAdSize(adContainer)
                adView.setAdSize(adSize)
            }

            (adView.parent as? ViewGroup)?.removeView(adView)
            adContainer.removeAllViews()
            adContainer.addView(adView)
            adContainer.visibility = View.GONE

            // Track this ad view
            activeBannerAds[adContainer] = adView

            val extras = Bundle().apply {
                putString("collapsible", "bottom")
                putString("collapsible_request_id", System.currentTimeMillis().toString())
            }

            val adRequest = AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()

            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    adContainer.visibility = View.VISIBLE
                    Timber.i("Bottom collapsible banner loaded: collapsible=${adView.isCollapsible}")
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastBannerEnabled(), "Banner (Bottom Col): Loaded")

                    // Setup auto-collapse
                    setupAutoCollapse(adView)

                    onAdLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    adContainer.visibility = View.GONE
                    Timber.d("Bottom collapsible banner ad failed: ${error.message}")
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastBannerEnabled(), "Banner (Bottom Col): Failed")
                    onAdFailed?.invoke(error)

                    // Retry with exponential backoff
                    adContainer.postDelayed({
                        if (!activity.isFinishing && !activity.isDestroyed) {
                            adView.loadAd(adRequest)
                        }
                    }, 5000)
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    cancelAutoCollapse(adView)
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    cancelAutoCollapse(adView)
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastBannerEnabled(), "Banner (Bottom Col): Shown")
                }
            }

            adView.loadAd(adRequest)
        }
    }

    fun collapseAllBanners(onComplete: () -> Unit = {}) {
        val banners = listOfNotNull(collapsibleTopBannerAdView, collapsibleBottomBannerAdView)
        if (banners.isEmpty()) {
            onComplete()
            return
        }

        var completedCount = 0

        banners.forEach { adView ->
            try {
                adView.pause()
                adView.resume()

                (adView.parent as? ViewGroup)?.removeView(adView)
                Timber.d("Banner view removed instantly")

                Handler(Looper.getMainLooper()).post {
                    try {
                        adView.destroy()
                        Timber.d("Banner destroyed on main thread")
                    } catch (e: Exception) {
                        Timber.e(e, "Error destroying banner")
                    } finally {
                        completedCount++
                        if (completedCount == banners.size) {
                            onComplete()
                        }
                    }
                }

            } catch (e: Exception) {
                Timber.e(e, "Error collapsing banner")
                completedCount++
                if (completedCount == banners.size) {
                    onComplete()
                }
            }
        }
    }

    fun loadAdaptiveBanner(
        container: FrameLayout,
        adUnitId: String,
        visibility: Int = View.VISIBLE,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((LoadAdError) -> Unit)? = null
    ) {
        if (visibility != View.VISIBLE) return
        val resolvedAdUnitId = AdUnitIdSanitizer.sanitizeBanner(adUnitId)

        // Clean up existing ad first
        adaptiveAdView?.let { cleanupAdView(it, container) }

        val adView = AdView(activity).apply {
            this.adUnitId = resolvedAdUnitId
            adaptiveAdView = this
        }

        container.post {
            if (adView.adSize == null) {
                adView.setAdSize(calculateAdSize(container))
            }

            (adView.parent as? ViewGroup)?.removeView(adView)
            container.removeAllViews()
            container.addView(adView)
            container.visibility = View.GONE

            // Track this ad view
            activeBannerAds[container] = adView

            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    container.visibility = visibility
                    Timber.i("Adaptive banner loaded")
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastBannerEnabled(), "Banner (Adaptive): Loaded")
                    onAdLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    container.visibility = View.GONE
                    Timber.d("Adaptive banner failed: ${error.message}")
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastBannerEnabled(), "Banner (Adaptive): Failed")
                    onAdFailed?.invoke(error)

                    container.postDelayed({
                        if (!activity.isFinishing && !activity.isDestroyed) {
                            adView.loadAd(buildAdRequest())
                        }
                    }, 5000)
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastBannerEnabled(), "Banner (Adaptive): Shown")
                }
            }

            adView.loadAd(buildAdRequest())
        }
    }

    fun loadRectangleAd(
        adContainer: FrameLayout,
        adUnitId: String,
        defaultVisibility: Int = View.VISIBLE,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((LoadAdError) -> Unit)? = null
    ) {
        if (defaultVisibility != View.VISIBLE) return
        val resolvedAdUnitId = AdUnitIdSanitizer.sanitizeBanner(adUnitId)

        // Clean up existing ad first
        rectangleAdView?.let { cleanupAdView(it, adContainer) }

        val adView = AdView(activity).apply {
            this.adUnitId = resolvedAdUnitId
            rectangleAdView = this
            setAdSize(AdSize.MEDIUM_RECTANGLE)
        }

        (adView.parent as? ViewGroup)?.removeView(adView)
        adContainer.removeAllViews()
        adContainer.addView(adView)
        adContainer.visibility = View.GONE

        // Track this ad view
        activeBannerAds[adContainer] = adView

        val adRequest = AdRequest.Builder().build()

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adContainer.visibility = defaultVisibility
                Timber.i("Rectangle banner loaded")
                DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastBannerEnabled(), "Banner (Rectangle): Loaded")
                onAdLoaded?.invoke()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                adContainer.visibility = View.GONE
                Timber.d("Rectangle banner ad failed: ${error.message}")
                DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastBannerEnabled(), "Banner (Rectangle): Failed")
                onAdFailed?.invoke(error)

                adContainer.postDelayed({
                    if (!activity.isFinishing && !activity.isDestroyed) {
                        adView.loadAd(adRequest)
                    }
                }, 5000)
            }

            override fun onAdOpened() {
                super.onAdOpened()
                DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastBannerEnabled(), "Banner (Rectangle): Shown")
            }
        }

        adView.loadAd(adRequest)
    }

    /**
     * Setup auto-collapse for collapsible banners
     */
    private fun setupAutoCollapse(adView: AdView) {
        if (!adView.isCollapsible) return

        // Cancel any existing auto-collapse for this ad
        cancelAutoCollapse(adView)

        val collapseRunnable = Runnable {
            try {
                if (!activity.isFinishing && !activity.isDestroyed) {
                    // Trigger collapse by simulating user interaction pause
                    adView.pause()
                    adView.resume()
                    Timber.d("Auto-collapsed banner ad after ${AUTO_COLLAPSE_DELAY}ms")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during auto-collapse")
            }
        }

        collapseRunnables[adView] = collapseRunnable
        mainHandler.postDelayed(collapseRunnable, AUTO_COLLAPSE_DELAY)
    }

    /**
     * Cancel auto-collapse for specific ad view
     */
    private fun cancelAutoCollapse(adView: AdView) {
        collapseRunnables[adView]?.let { runnable ->
            mainHandler.removeCallbacks(runnable)
            collapseRunnables.remove(adView)
        }
    }

    /**
     * Clean up specific ad view with timeout protection
     */
    private fun cleanupAdView(adView: AdView, container: FrameLayout) {
        try {
            // Cancel auto-collapse
            cancelAutoCollapse(adView)

            // Remove from tracking
            activeBannerAds.remove(container)

            // Fast destruction with timeout
            val destructionRunnable = Runnable {
                try {
                    (adView.parent as? ViewGroup)?.removeView(adView)
                    adView.destroy()
                } catch (e: Exception) {
                    Timber.e(e, "Error during ad destruction")
                }
            }

            // Execute destruction immediately, but with timeout protection
            mainHandler.post(destructionRunnable)

            // Fallback cleanup in case destruction hangs
            mainHandler.postDelayed({
                try {
                    if (adView.parent != null) {
                        (adView.parent as? ViewGroup)?.removeView(adView)
                        Timber.d("Forced ad view removal after timeout")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error during forced ad cleanup")
                }
            }, DESTRUCTION_TIMEOUT)

        } catch (e: Exception) {
            Timber.e(e, "Error during ad view cleanup")
        }
    }

    private fun getAdSize(container: ViewGroup): AdSize {
        val metrics = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics =
                activity.getSystemService(WindowManager::class.java).currentWindowMetrics
            val insets =
                windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            val bounds = windowMetrics.bounds

            DisplayMetrics().apply {
                widthPixels = bounds.width() - insets.left - insets.right
                density = activity.resources.displayMetrics.density
            }
        } else {
            @Suppress("DEPRECATION")
            DisplayMetrics().also {
                activity.windowManager.defaultDisplay.getMetrics(it)
            }
        }

        val adWidthPixels = container.width.toFloat()
        val adWidth =
            if (adWidthPixels > 0) adWidthPixels / metrics.density else metrics.widthPixels / metrics.density

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth.toInt())
    }

    private fun calculateAdSize(container: ViewGroup): AdSize {
        return getAdSize(container)
    }

    /**
     * Builds ad request with respect to NPA (non-personalized ads)
     */
    private fun buildAdRequest(): AdRequest {
        val builder = AdRequest.Builder()

        if (adsPref.isNpa()) {
            val npaBundle = Bundle().apply { putString("npa", "1") }
            builder.addNetworkExtrasBundle(AdMobAdapter::class.java, npaBundle)
            Timber.d("Building AdRequest with NPA=true")
        } else {
            Timber.d("Building AdRequest with NPA=false")
        }

        return builder.build()
    }

    /**
     * Destroy and remove a specific banner ad view
     */
    fun destroyBanner(adView: AdView?) {
        adView ?: return
        try {
            cancelAutoCollapse(adView)
            (adView.parent as? ViewGroup)?.removeView(adView)
            adView.destroy()
            Timber.d("Banner destroyed: $adView")
        } catch (e: Exception) {
            Timber.e(e, "Error destroying banner ad")
        }
    }

    /**
     * Destroy all currently active banner ads
     */
    fun destroyAllBanners() {
        val banners = listOf(adaptiveAdView, rectangleAdView, collapsibleTopBannerAdView, collapsibleBottomBannerAdView)
        banners.forEach { destroyBanner(it) }

        // Clear references
        adaptiveAdView = null
        rectangleAdView = null
        collapsibleTopBannerAdView = null
        collapsibleBottomBannerAdView = null
        activeBannerAds.clear()
        collapseRunnables.clear()

        Timber.d("All banner ads destroyed and cleared")
    }
}
