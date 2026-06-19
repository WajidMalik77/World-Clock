package com.worldclock.app_themes.ads.managers

import android.app.Activity
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.LruCache
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.graphics.toColorInt
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.worldclock.app_themes.R
import com.worldclock.app_themes.ads.config.models.NativeAdColorConfig
import com.worldclock.app_themes.ads.helpers.models.AdLoadParams
import com.worldclock.app_themes.ads.utils.AdUnitIdSanitizer
import com.worldclock.app_themes.ads.utils.AdsPref
import com.worldclock.app_themes.ads.utils.DebugToaster
import timber.log.Timber
import java.lang.ref.WeakReference

class AdmobNativeManager(
    activity: Activity,
    private val adsPref: AdsPref,
    private val nativeAdCache: NativeAdCache,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
) {
    private val activityRef = WeakReference(activity)
    private val displayMetrics by lazy { Resources.getSystem().displayMetrics.density }
    private var nativeAd: NativeAd? = null
    private var adLoader: AdLoader? = null
    @Volatile
    private var isLoading = false
    @Volatile
    private var isPreloading = false
    @Volatile
    private var isDestroyed = false
    private val colorCache = LruCache<String, Int>(20)
    private val layoutInflater by lazy { LayoutInflater.from(activity) }
    private var lastLoadParams: AdLoadParams? = null

    fun loadNativeSmallAd(
        adContainer: FrameLayout,
        adUnitId: String,
        shimmerContainer: FrameLayout? = null,
        shimmerLayout: Int = R.layout.native_ad_shimmer_small,
        colorConfig: NativeAdColorConfig? = null,
        shouldPreloadNext: Boolean = true,
        onLoaded: (() -> Unit)? = null,
        onImpression: (() -> Unit)? = null,
        onFailed: ((LoadAdError) -> Unit)? = null
    ) {
        showDebugToast("Native Small Ad: Load Called")
        loadNativeAd(
            AdLoadParams(
                adContainer = adContainer,
                shimmerContainer = shimmerContainer,
                adUnitId = adUnitId,
                layoutRes = R.layout.native_admob_small,
                shimmerLayoutRes = shimmerLayout,
                colorConfig = colorConfig,
                shouldPreloadNext = shouldPreloadNext,
                onLoaded = onLoaded,
                onImpression = onImpression,
                onFailed = onFailed
            )
        )
    }

    fun loadNativeMediumAd(
        adContainer: FrameLayout,
        adUnitId: String,
        shimmerContainer: FrameLayout? = null,
        shimmerLayout: Int = R.layout.native_ad_shimmer_medium,
        shouldPreloadNext: Boolean = true,
        colorConfig: NativeAdColorConfig? = null,
        onLoaded: (() -> Unit)? = null,
        onImpression: (() -> Unit)? = null,
        onFailed: ((LoadAdError) -> Unit)? = null
    ) {
        showDebugToast("Native Medium Ad: Load Called")
        loadNativeAd(
            AdLoadParams(
                adContainer = adContainer,
                shimmerContainer = shimmerContainer,
                adUnitId = adUnitId,
                layoutRes = R.layout.native_admob,
                shimmerLayoutRes = shimmerLayout,
                colorConfig = colorConfig,
                shouldPreloadNext = shouldPreloadNext,
                onLoaded = onLoaded,
                onImpression = onImpression,
                onFailed = onFailed
            )
        )
    }

    fun loadNativeCustomAd(
        adContainer: FrameLayout,
        adUnitId: String,
        layoutRes: Int,
        shimmerContainer: FrameLayout? = null,
        shimmerLayout: Int = R.layout.native_loading_medium,
        shouldPreloadNext: Boolean = true,
        colorConfig: NativeAdColorConfig? = null,
        onLoaded: (() -> Unit)? = null,
        onImpression: (() -> Unit)? = null,
        onFailed: ((LoadAdError) -> Unit)? = null
    ) {
        showDebugToast("Native Custom Ad: Load Called")
        loadNativeAd(
            AdLoadParams(
                adContainer = adContainer,
                shimmerContainer = shimmerContainer,
                adUnitId = adUnitId,
                layoutRes = layoutRes,
                shimmerLayoutRes = shimmerLayout,
                colorConfig = colorConfig,
                shouldPreloadNext = shouldPreloadNext,
                onLoaded = onLoaded,
                onImpression = onImpression,
                onFailed = onFailed
            )
        )
    }

    fun loadNativeFullScreenIntroAd(
        adContainer: FrameLayout,
        adUnitId: String,
        shimmerContainer: FrameLayout? = null,
        colorConfig: NativeAdColorConfig? = null,
        shouldPreloadNext: Boolean = true,
        onLoaded: (() -> Unit)? = null,
        onImpression: (() -> Unit)? = null,
        onFailed: ((LoadAdError) -> Unit)? = null
    ) {
        showDebugToast("Native Fullscreen Ad: Load Called")
        loadNativeAd(
            AdLoadParams(
                adContainer = adContainer,
                shimmerContainer = shimmerContainer,
                adUnitId = adUnitId,
                layoutRes = R.layout.native_ad_fullscreen_intro,
                shimmerLayoutRes = R.layout.native_ad_fullscreen_intro_shimmer,
                colorConfig = colorConfig,
                shouldPreloadNext = shouldPreloadNext,
                onLoaded = onLoaded,
                onImpression = onImpression,
                onFailed = onFailed
            )
        )
    }

    fun cleanup() {
        if (isDestroyed) return
        isDestroyed = true
        isLoading = false
        isPreloading = false

        scope.launch(Dispatchers.Main.immediate) {
            adLoader = null
            val keepForNextContainer = nativeAdCache.releaseIfUnimpressed(nativeAd)
            if (!keepForNextContainer) {
                nativeAd?.destroy()
            }
            nativeAd = null
            lastLoadParams = null
            colorCache.evictAll()
        }
    }

    fun releaseUnimpressedForReuse() {
        if (isDestroyed) return
        nativeAdCache.releaseIfUnimpressed(nativeAd)
    }

    fun keepCurrentNativeForNextContainer() {
        if (isDestroyed) return
        val currentNative = nativeAd ?: return
        val params = lastLoadParams ?: return
        if (!nativeAdCache.isTrackedUnimpressed(currentNative)) return
        nativeAdCache.rememberMatched(
            ad = currentNative,
            adUnitId = params.adUnitId,
            layoutRes = params.layoutRes,
            available = true,
            onImpression = params.onImpression
        )
    }

    fun preloadNativeForNextContainer(
        adUnitId: String,
        layoutRes: Int,
        onLoaded: (() -> Unit)? = null,
        onFailed: ((LoadAdError) -> Unit)? = null
    ) {
        val activity = activityRef.get() ?: return
        val sanitizedAdUnitId = AdUnitIdSanitizer.sanitizeNative(adUnitId)
        if (nativeAdCache.hasTrackedAd()) {
            Timber.d("Native preload skipped: singleton native already tracked")
            onLoaded?.invoke()
            return
        }
        if (isLoading || isPreloading || isDestroyed || activity.isFinishing || activity.isDestroyed) return

        isPreloading = true
        val loader = AdLoader.Builder(activity.applicationContext, sanitizedAdUnitId)
            .forNativeAd { ad ->
                isPreloading = false
                nativeAdCache.rememberMatched(ad, sanitizedAdUnitId, layoutRes, available = true)
                showDebugToast("Native Ad: Singleton Preload Success")
                onLoaded?.invoke()
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isPreloading = false
                    showDebugToast("Native Ad: Singleton Preload Failed - ${error.message}")
                    onFailed?.invoke(error)
                }
            })
            .withNativeAdOptions(createNativeAdOptions())
            .build()

        loader.loadAd(buildAdRequest())
    }

    private fun loadNativeAd(params: AdLoadParams) {
        val activity = activityRef.get() ?: return
        val sanitizedParams = params.copy(adUnitId = AdUnitIdSanitizer.sanitizeNative(params.adUnitId))

        // Already-displayed guard: this exact container already shows our loaded ad with a
        // matching layout. Re-entry (config change, re-navigation, repeated load calls) must
        // NOT fire a fresh request — that would discard an already-matched ad (hurting show
        // rate) and waste an impression-less request (hurting match rate).
        if (!isDestroyed && nativeAd != null
            && sanitizedParams.adContainer.childCount > 0
            && lastLoadParams?.layoutRes == sanitizedParams.layoutRes
            && lastLoadParams?.adContainer === sanitizedParams.adContainer
        ) {
            lastLoadParams = sanitizedParams
            sanitizedParams.onLoaded?.invoke()
            return
        }

        // Fast path: an ad is already loaded for this activity (e.g. fragment was popped
        // and recreated). Re-bind it to the new empty container instead of running a
        // fresh load + shimmer cycle.
        // Layout must match — re-binding a NativeAd to a different NativeAdView size
        // unregisters it from the previous view, which corrupts the other native render.
        if (!isDestroyed && !activity.isFinishing && !activity.isDestroyed
            && nativeAd != null && sanitizedParams.adContainer.childCount == 0
            && lastLoadParams?.layoutRes == sanitizedParams.layoutRes
        ) {
            lastLoadParams = sanitizedParams
            nativeAdCache.setImpressionCallback(nativeAd, sanitizedParams.onImpression)
            scope.launch(Dispatchers.Main.immediate) {
                try {
                    val adView = inflateAndPopulateAdView(nativeAd!!, sanitizedParams)
                    displayAd(adView, sanitizedParams)
                    sanitizedParams.onLoaded?.invoke()
                } catch (e: Exception) {
                    Timber.e(e, "Native ad reuse failed; falling back to fresh load")
                    if (canLoadAd(activity)) {
                        isLoading = true
                        prepareContainers(sanitizedParams)
                        startAdRequest(activity, sanitizedParams, isPreload = false)
                    }
                }
            }
            return
        }

        // If a previous screen has already released this matched native for reuse, the next
        // container gets it even when the next placement wants a different layout or ad unit.
        if (!isDestroyed && !activity.isFinishing && !activity.isDestroyed
            && nativeAd != null && sanitizedParams.adContainer.childCount == 0
            && nativeAdCache.isAvailable(nativeAd)
        ) {
            val reusableAd = nativeAd!!
            lastLoadParams = sanitizedParams
            nativeAdCache.setImpressionCallback(reusableAd, sanitizedParams.onImpression)
            scope.launch(Dispatchers.Main.immediate) {
                try {
                    val adView = inflateAndPopulateAdView(reusableAd, sanitizedParams)
                    displayAd(adView, sanitizedParams)
                    sanitizedParams.onLoaded?.invoke()
                } catch (e: Exception) {
                    Timber.e(e, "Available native ad reuse failed; falling back to fresh load")
                    nativeAdCache.forget(reusableAd)
                    reusableAd.destroy()
                    nativeAd = null
                    if (canLoadAd(activity)) {
                        isLoading = true
                        prepareContainers(sanitizedParams)
                        startAdRequest(activity, sanitizedParams, isPreload = false)
                    }
                }
            }
            return
        }

        // Singleton reuse: before requesting a new native, first consume any matched native that
        // left a previous screen before AdMob recorded an impression. This is intentionally not
        // tied to the next placement's ad unit id; the already-matched ad gets the next container.
        if (!isDestroyed && !activity.isFinishing && !activity.isDestroyed
            && nativeAd == null && sanitizedParams.adContainer.childCount == 0
        ) {
            val cached = nativeAdCache.consumeAny()
            if (cached != null) {
                nativeAd = cached
                lastLoadParams = sanitizedParams
                nativeAdCache.setImpressionCallback(cached, sanitizedParams.onImpression)
                scope.launch(Dispatchers.Main.immediate) {
                    try {
                        val adView = inflateAndPopulateAdView(cached, sanitizedParams)
                        displayAd(adView, sanitizedParams)
                        sanitizedParams.onLoaded?.invoke()
                    } catch (e: Exception) {
                        Timber.e(e, "Cached native ad reuse failed; falling back to fresh load")
                        nativeAdCache.forget(cached)
                        cached.destroy()
                        nativeAd = null
                        if (canLoadAd(activity)) {
                            isLoading = true
                            prepareContainers(sanitizedParams)
                            startAdRequest(activity, sanitizedParams, isPreload = false)
                        }
                    }
                }
                return
            }
        }

        if (!canLoadAd(activity)) return

        // Store params for rendering context
        lastLoadParams = sanitizedParams

        isLoading = true

        scope.launch(Dispatchers.Main.immediate) {
            prepareContainers(sanitizedParams)
            startAdRequest(activity, sanitizedParams, isPreload = false)
        }
    }

    private fun canLoadAd(activity: Activity): Boolean =
        !isLoading && !isDestroyed && !activity.isFinishing && !activity.isDestroyed

    private suspend fun prepareContainers(params: AdLoadParams) {
        withContext(Dispatchers.Main.immediate) {
            params.adContainer.apply {
                removeAllViews()
                visibility = View.GONE
            }

            params.shimmerContainer?.let { shimmer ->
                shimmer.removeAllViews()
                layoutInflater.inflate(params.shimmerLayoutRes, shimmer, true)
                (shimmer as? ShimmerFrameLayout)?.startShimmer()
                shimmer.visibility = View.VISIBLE
            }
        }
    }

    private fun startAdRequest(activity: Activity, params: AdLoadParams, isPreload: Boolean) {
        var loadedAdForListener: NativeAd? = null
        // Use application context for loading so a cached NativeAd never retains a destroyed
        // Activity. Rendering still uses the Activity's LayoutInflater.
        val loader = AdLoader.Builder(activity.applicationContext, params.adUnitId)
            .forNativeAd { ad ->
                loadedAdForListener = ad
                if (isPreload) {
                    handlePreloadedAdReceived(ad)
                } else {
                    handleAdReceived(ad, params, isPreloaded = false)
                }
            }
            .withAdListener(createAdListener(params, isPreload) { loadedAdForListener })
            .withNativeAdOptions(createNativeAdOptions())
            .build()

        if (!isPreload) {
            adLoader = loader
        }

        loader.loadAd(buildAdRequest())
    }

    private fun handlePreloadedAdReceived(ad: NativeAd) {
        isPreloading = false
        ad.destroy()
        showDebugToast("Native Ad: Preload Success")
    }

    private fun handleAdReceived(ad: NativeAd, params: AdLoadParams, isPreloaded: Boolean) {
        isLoading = false

        // Don't destroy current ad if this is from preload
        if (!isPreloaded) {
            val keepPrevious = nativeAdCache.releaseIfUnimpressed(nativeAd)
            if (!keepPrevious) {
                nativeAd?.destroy()
            }
        }
        nativeAd = ad

        // Track this matched ad until AdMob reports an impression. If this Activity disappears
        // first, cleanup() releases it so the next native container consumes it before requesting.
        nativeAdCache.rememberMatched(
            ad = ad,
            adUnitId = params.adUnitId,
            layoutRes = params.layoutRes,
            available = false,
            onImpression = params.onImpression
        )

        scope.launch(Dispatchers.Main.immediate) {
            try {
                val activity = activityRef.get()
                if (isDestroyed || activity == null || activity.isFinishing || activity.isDestroyed) {
                    nativeAdCache.releaseIfUnimpressed(ad)
                    params.onLoaded?.invoke()
                    return@launch
                }
                val adView = inflateAndPopulateAdView(ad, params)
                displayAd(adView, params)

                showDebugToast("Native Ad: Load Success")
                params.onLoaded?.invoke()
            } catch (e: Exception) {
                Timber.e(e, "Ad render failed")
                showDebugToast("Native Ad: Render Failed - ${e.message}")
                nativeAdCache.forget(ad)
                ad.destroy()
                params.onFailed?.invoke(createRenderError())
            }
        }
    }

    private fun createAdListener(
        params: AdLoadParams,
        isPreload: Boolean,
        loadedAd: () -> NativeAd?
    ) = object : AdListener() {
        override fun onAdFailedToLoad(error: LoadAdError) {
            if (isPreload) {
                isPreloading = false
                showDebugToast("Native Ad: Preload Failed - ${error.message}")
            } else {
                isLoading = false
                showDebugToast("Native Ad: Load Failed - ${error.message}")
                Timber.e("Ad load failed: ${error.message}")
                params.onFailed?.invoke(error)
            }
        }

        override fun onAdImpression() {
            nativeAdCache.markImpressed(loadedAd() ?: nativeAd)
        }

        override fun onAdClicked() {}

        override fun onAdOpened() {}

        override fun onAdClosed() {}
    }

    private fun inflateAndPopulateAdView(
        ad: NativeAd,
        params: AdLoadParams
    ): NativeAdView {
        val adView = layoutInflater.inflate(params.layoutRes, null) as NativeAdView

        // Bind all views in one pass
        bindAdViews(adView)

        // Apply styling and content
        val config = normalizeConfig(params.colorConfig ?: NativeAdColorConfig())
        applyAdStyling(adView, config)
        populateAdContent(adView, ad, config)

        adView.setNativeAd(ad)
        return adView
    }

    private fun bindAdViews(adView: NativeAdView) {
        adView.apply {
            mediaView = findViewById(R.id.ad_media)
            headlineView = findViewById(R.id.ad_headline)
            bodyView = findViewById(R.id.ad_body)
            callToActionView = findViewById(R.id.ad_call_to_action)
            iconView = findViewById(R.id.ad_app_icon)
            advertiserView = findViewById(R.id.ad_advertiser)
            starRatingView = findViewById(R.id.ad_star_rating)
            adChoicesView = findViewById(R.id.ad_choices_view)
        }
    }

    private fun applyAdStyling(adView: NativeAdView, config: NativeAdColorConfig) {
        val isTransparent = config.mode == 1

        adView.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = config.cornerRadiusDp.toDp()

            val bgColor = if (isTransparent) Color.TRANSPARENT
            else getCachedColor(config.backgroundColorHex, Color.WHITE)
            setColor(bgColor)

            val strokeColor = if (isTransparent) Color.TRANSPARENT
            else getCachedColor(config.strokeColorHex, Color.TRANSPARENT)
            setStroke(config.strokeWidthDp.toDp().toInt(), strokeColor)
        }
    }

    private fun populateAdContent(adView: NativeAdView, ad: NativeAd, config: NativeAdColorConfig) {
        // Headline
        (adView.headlineView as? TextView)?.run {
            text = ad.headline
            visibility = if (ad.headline.isNullOrBlank()) View.GONE else View.VISIBLE
            alpha = 1f
            setTextColor(resolveTextColor(config, config.headlineColorHex, Color.BLACK))
        }

        // Body
        (adView.bodyView as? TextView)?.run {
            text = ad.body
            visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE
            alpha = 1f
            setTextColor(resolveTextColor(config, config.bodyTextColorHex, Color.DKGRAY))
        }

        // CTA Button
        (adView.callToActionView as? TextView)?.run {
            text = ad.callToAction
            visibility = if (ad.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE
            alpha = 1f
            setTextColor(getCachedColor(config.ctaTextColorHex, Color.WHITE))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, config.ctaTextSizeSp?.toFloat() ?: 14f)
            background = getCachedCtaBackground(config)
            // Some AppCompat/Material button styles re-tint backgrounds to theme defaults.
            // Clear tint so remote-config CTA color is shown exactly.
            ViewCompat.setBackgroundTintList(this, null)
            ViewCompat.setBackgroundTintMode(this, null)
            backgroundTintList = null
            backgroundTintMode = null
        }

        // Icon
        (adView.iconView as? ImageView)?.run {
            ad.icon?.let {
                setImageDrawable(it.drawable)
                visibility = View.VISIBLE
            } ?: run { visibility = View.GONE }
        }

        // Metadata (price, store, rating)
        populateMetadata(adView, ad, config)
    }

    private fun populateMetadata(adView: NativeAdView, ad: NativeAd, config: NativeAdColorConfig) {
        val textColor = resolveTextColor(config, config.bodyTextColorHex, Color.DKGRAY)

        // Price
        (adView.priceView as? TextView)?.run {
            text = ad.price
            visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
            setTextColor(textColor)
        }

        // Store
        (adView.storeView as? TextView)?.run {
            text = ad.store
            visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
            setTextColor(textColor)
        }

        // Rating
        (adView.starRatingView as? RatingBar)?.run {
            ad.starRating?.takeIf { it > 0 }?.let {
                rating = it.toFloat()
                visibility = View.VISIBLE
            } ?: run { visibility = View.GONE }
        }
    }

    private fun displayAd(adView: NativeAdView, params: AdLoadParams) {
        params.shimmerContainer?.let {
            (it as? ShimmerFrameLayout)?.stopShimmer()
            it.visibility = View.GONE
        }

        params.adContainer.apply {
            removeAllViews()
            addView(adView)
            visibility = View.VISIBLE
        }
    }

    private fun getCachedColor(hex: String?, default: Int): Int {
        if (hex.isNullOrBlank()) return default

        return colorCache.get(hex) ?: run {
            val color = try {
                hex.toColorInt()
            } catch (e: Exception) {
                default
            }
            colorCache.put(hex, color)
            color
        }
    }

    private fun getCachedCtaBackground(config: NativeAdColorConfig): Drawable {
        val colorString =
            config.ctaBackgroundColorHex ?: config.backgroundColorHex
            ?: return createSolidBackground(Color.parseColor("#09265A"), config)
        return createCtaBackground(colorString, config)
    }

    private fun createCtaBackground(colorString: String, config: NativeAdColorConfig): Drawable {
        val colors = colorString.split(",").mapNotNull {
            try {
                it.trim().toColorInt()
            } catch (e: Exception) {
                null
            }
        }

        return when {
            colors.size >= 2 -> GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                colors.toIntArray()
            ).apply { cornerRadius = config.ctaCornerRadiusDp.toDp() }

            colors.size == 1 -> createSolidBackground(colors[0], config)

            else -> createSolidBackground(Color.parseColor("#09265A"), config)
        }
    }

    private fun createSolidBackground(color: Int, config: NativeAdColorConfig): Drawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = config.ctaCornerRadiusDp.toDp()
        }

    private fun resolveTextColor(config: NativeAdColorConfig, hex: String?, default: Int): Int {
        // Always honor explicit color from config first.
        if (!hex.isNullOrBlank()) return getCachedColor(hex, default)
        // Transparent mode fallback: avoid white text on clear bg.
        if (config.mode == 1 && default == Color.WHITE) return Color.BLACK
        return default
    }

    private fun normalizeConfig(config: NativeAdColorConfig): NativeAdColorConfig {
        return config.copy(
            backgroundColorHex = config.backgroundColorHex ?: "#2009265A",
            headlineColorHex = config.headlineColorHex ?: "#FFFFFF",
            bodyTextColorHex = config.bodyTextColorHex ?: "#FFFFFF",
            ctaBackgroundColorHex = config.ctaBackgroundColorHex ?: "#FF0048,#FA702C",
            ctaTextColorHex = config.ctaTextColorHex ?: "#FFFFFF",
            ctaCornerRadiusDp = config.ctaCornerRadiusDp ?: 48,
            ctaTextSizeSp = config.ctaTextSizeSp ?: 18,
            cornerRadiusDp = config.cornerRadiusDp ?: 12,
            strokeWidthDp = config.strokeWidthDp ?: 1,
            strokeColorHex = config.strokeColorHex ?: "#00000000"
        )
    }

    private fun buildAdRequest(): AdRequest =
        AdRequest.Builder().apply {
            if (adsPref.isNpa()) {
                addNetworkExtrasBundle(
                    AdMobAdapter::class.java,
                    Bundle(1).apply { putString("npa", "1") }
                )
            }
        }.build()

    private fun createNativeAdOptions(): NativeAdOptions =
        NativeAdOptions.Builder()
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
            .setRequestMultipleImages(false)
            .build()

    private fun Int?.toDp(): Float = (this ?: 0) * displayMetrics

    private fun createRenderError(): LoadAdError =
        LoadAdError(0, "Render error", "errorDomain", null, null)

    private fun showDebugToast(message: String) {
        val act = activityRef.get()
        if (act != null && !act.isFinishing && !act.isDestroyed) {
            DebugToaster.showAdDebugCard(act, adsPref.isDebugToastNativeEnabled(), message)
        }
    }
}
