package com.worldclock.app_themes.ads.managers.facebook

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.LruCache
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.MediaView
import com.facebook.ads.NativeAd
import com.facebook.ads.NativeAdLayout
import com.facebook.ads.NativeAdListener
import com.facebook.shimmer.ShimmerFrameLayout
import com.worldclock.app_themes.R
import com.worldclock.app_themes.ads.config.models.NativeAdColorConfig
import com.worldclock.app_themes.ads.utils.AdUnitIdSanitizer
import com.worldclock.app_themes.ads.utils.AdsPref
import com.worldclock.app_themes.ads.utils.DebugToaster
import timber.log.Timber
import java.lang.ref.WeakReference

class FbNativeAdManager(
    activity: Activity,
    private val adsPref: AdsPref
) {
    private val activityRef = WeakReference(activity)
    private val layoutInflater by lazy { LayoutInflater.from(activity) }
    private val displayMetrics by lazy { activity.resources.displayMetrics.density }
    private val colorCache = LruCache<String, Int>(20)
    private var nativeAd: NativeAd? = null

    @Volatile
    private var isDestroyed = false

    fun loadNativeSmallAd(
        adContainer: FrameLayout,
        adUnitId: String,
        shimmerContainer: FrameLayout? = null,
        shimmerLayout: Int = R.layout.native_ad_shimmer_small,
        colorConfig: NativeAdColorConfig? = null,
        onLoaded: (() -> Unit)? = null,
        onImpression: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) = loadNativeAd(adContainer, adUnitId, R.layout.native_admob_small, shimmerContainer, shimmerLayout, colorConfig, compactMedia = true, onLoaded, onImpression, onFailed)

    fun loadNativeMediumAd(
        adContainer: FrameLayout,
        adUnitId: String,
        shimmerContainer: FrameLayout? = null,
        shimmerLayout: Int = R.layout.native_ad_shimmer_medium,
        colorConfig: NativeAdColorConfig? = null,
        onLoaded: (() -> Unit)? = null,
        onImpression: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) = loadNativeAd(adContainer, adUnitId, R.layout.native_admob, shimmerContainer, shimmerLayout, colorConfig, compactMedia = false, onLoaded, onImpression, onFailed)

    fun loadNativeCustomAd(
        adContainer: FrameLayout,
        adUnitId: String,
        layoutRes: Int,
        shimmerContainer: FrameLayout? = null,
        shimmerLayout: Int = R.layout.native_loading_medium,
        colorConfig: NativeAdColorConfig? = null,
        onLoaded: (() -> Unit)? = null,
        onImpression: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) = loadNativeAd(adContainer, adUnitId, layoutRes, shimmerContainer, shimmerLayout, colorConfig, compactMedia = false, onLoaded, onImpression, onFailed)

    fun loadNativeFullScreenIntroAd(
        adContainer: FrameLayout,
        adUnitId: String,
        shimmerContainer: FrameLayout? = null,
        colorConfig: NativeAdColorConfig? = null,
        onLoaded: (() -> Unit)? = null,
        onImpression: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) = loadNativeAd(adContainer, adUnitId, R.layout.native_ad_fullscreen_intro, shimmerContainer, R.layout.native_ad_fullscreen_intro, colorConfig, compactMedia = false, onLoaded, onImpression, onFailed)

    private fun loadNativeAd(
        adContainer: FrameLayout,
        adUnitId: String,
        layoutRes: Int,
        shimmerContainer: FrameLayout?,
        shimmerLayout: Int,
        colorConfig: NativeAdColorConfig?,
        compactMedia: Boolean,
        onLoaded: (() -> Unit)?,
        onImpression: (() -> Unit)?,
        onFailed: ((String) -> Unit)?
    ) {
        val activity = activityRef.get() ?: return
        if (isDestroyed || activity.isFinishing || activity.isDestroyed) return

        val resolvedId = AdUnitIdSanitizer.sanitizeFbNative(adUnitId)
        if (resolvedId.isBlank()) {
            onFailed?.invoke("Facebook native placement id is blank")
            return
        }

        shimmerContainer?.let { shimmer ->
            shimmer.removeAllViews()
            layoutInflater.inflate(shimmerLayout, shimmer, true)
            (shimmer as? ShimmerFrameLayout)?.startShimmer()
            shimmer.visibility = View.VISIBLE
        }
        adContainer.removeAllViews()
        adContainer.visibility = View.GONE

        nativeAd?.destroy()
        val ad = NativeAd(activity, resolvedId)
        nativeAd = ad

        val loadAdConfig = ad.buildLoadAdConfig()
            .withAdListener(object : NativeAdListener {
                override fun onError(adObj: Ad?, adError: AdError) {
                    stopShimmer(shimmerContainer)
                    Timber.e("FB native ad failed: ${adError.errorMessage}")
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastNativeEnabled(), "FB Native: Failed")
                    onFailed?.invoke(adError.errorMessage)
                }

                override fun onMediaDownloaded(adObj: Ad?) {}

                override fun onAdLoaded(adObj: Ad?) {
                    if (isDestroyed || adObj == null || adObj !== ad) return
                    try {
                        val view = inflateAndPopulate(ad, layoutRes, colorConfig, compactMedia)
                        stopShimmer(shimmerContainer)
                        adContainer.removeAllViews()
                        adContainer.addView(view)
                        adContainer.visibility = View.VISIBLE
                        DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastNativeEnabled(), "FB Native: Loaded")
                        onLoaded?.invoke()
                    } catch (e: Exception) {
                        Timber.e(e, "FB native render failed")
                        stopShimmer(shimmerContainer)
                        onFailed?.invoke(e.message ?: "Render error")
                    }
                }

                override fun onAdClicked(adObj: Ad?) {}
                override fun onLoggingImpression(adObj: Ad?) {
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastNativeEnabled(), "FB Native: Shown")
                    onImpression?.invoke()
                }
            })
            .build()

        ad.loadAd(loadAdConfig)
    }

    private fun stopShimmer(shimmerContainer: FrameLayout?) {
        shimmerContainer?.let {
            (it as? ShimmerFrameLayout)?.stopShimmer()
            it.visibility = View.GONE
        }
    }

    private fun inflateAndPopulate(ad: NativeAd, layoutRes: Int, colorConfig: NativeAdColorConfig?, compactMedia: Boolean = false): View {
        val content = layoutInflater.inflate(layoutRes, null) as ViewGroup
        val nativeAdLayout = NativeAdLayout(content.context)
        nativeAdLayout.addView(
            content,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )

        val headlineView = content.findViewById<TextView?>(R.id.ad_headline)
        val bodyView = content.findViewById<TextView?>(R.id.ad_body)
        val ctaView = content.findViewById<TextView?>(R.id.ad_call_to_action)
        val iconView = content.findViewById<ImageView?>(R.id.ad_app_icon)
        val mediaContainer = content.findViewById<ViewGroup?>(R.id.ad_media)
        val adChoicesContainer = content.findViewById<ViewGroup?>(R.id.ad_choices_view)

        val config = normalizeConfig(colorConfig ?: NativeAdColorConfig())
        applyAdStyling(content, config)

        headlineView?.apply {
            text = ad.adHeadline
            visibility = if (ad.adHeadline.isNullOrBlank()) View.GONE else View.VISIBLE
            setTextColor(resolveTextColor(config, config.headlineColorHex, Color.BLACK))
        }
        bodyView?.apply {
            text = ad.adBodyText
            visibility = if (ad.adBodyText.isNullOrBlank()) View.GONE else View.VISIBLE
            setTextColor(resolveTextColor(config, config.bodyTextColorHex, Color.DKGRAY))
        }
        ctaView?.apply {
            text = ad.adCallToAction
            visibility = if (ad.adCallToAction.isNullOrBlank()) View.GONE else View.VISIBLE
            setTextColor(getCachedColor(config.ctaTextColorHex, Color.WHITE))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, config.ctaTextSizeSp?.toFloat() ?: 14f)
            background = getCachedCtaBackground(config)
            ViewCompat.setBackgroundTintList(this, null)
            ViewCompat.setBackgroundTintMode(this, null)
            backgroundTintList = null
            backgroundTintMode = null
        }

        val mediaView = MediaView(content.context)
        if (mediaContainer != null) {
            mediaContainer.removeAllViews()
            mediaContainer.addView(mediaView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            if (compactMedia) {
                // Facebook requires the registered MediaView to remain laid out/visible (not
                // GONE) for impression + media rendering, but a non-zero height draws a
                // visible placeholder bar. Zero height keeps it measured/laid out while
                // drawing nothing.
                mediaContainer.layoutParams = mediaContainer.layoutParams.apply { height = 0 }
                mediaContainer.visibility = View.VISIBLE
            }
        }

        val iconMediaView: MediaView? = iconView?.let { icon ->
            val parent = icon.parent as? ViewGroup
            val iconMedia = MediaView(content.context)
            iconMedia.id = icon.id
            val params = icon.layoutParams
            if (parent != null) {
                val index = parent.indexOfChild(icon)
                parent.removeView(icon)
                parent.addView(iconMedia, index, params)
            }
            iconMedia
        }

        adChoicesContainer?.removeAllViews()

        val clickableViews = listOfNotNull(headlineView, bodyView, ctaView, mediaView)
        ad.registerViewForInteraction(content, mediaView, iconMediaView, clickableViews)

        return nativeAdLayout
    }

    private fun applyAdStyling(content: ViewGroup, config: NativeAdColorConfig) {
        val isTransparent = config.mode == 1

        content.background = GradientDrawable().apply {
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

    private fun resolveTextColor(config: NativeAdColorConfig, hex: String?, default: Int): Int {
        if (!hex.isNullOrBlank()) return getCachedColor(hex, default)
        if (config.mode == 1 && default == Color.WHITE) return Color.BLACK
        return default
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

    private fun getCachedCtaBackground(config: NativeAdColorConfig): android.graphics.drawable.Drawable {
        val colorString =
            config.ctaBackgroundColorHex ?: config.backgroundColorHex
            ?: return createSolidBackground(Color.parseColor("#09265A"), config)
        return createCtaBackground(colorString, config)
    }

    private fun createCtaBackground(colorString: String, config: NativeAdColorConfig): android.graphics.drawable.Drawable {
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

    private fun createSolidBackground(color: Int, config: NativeAdColorConfig): android.graphics.drawable.Drawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = config.ctaCornerRadiusDp.toDp()
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

    private fun Int?.toDp(): Float = (this ?: 0) * displayMetrics

    fun cleanup() {
        isDestroyed = true
        nativeAd?.destroy()
        nativeAd = null
    }
}
