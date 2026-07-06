package com.worldclock.app_themes.ads.managers.facebook

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.worldclock.app_themes.ads.utils.AdUnitIdSanitizer
import com.worldclock.app_themes.ads.utils.AdsPref
import com.worldclock.app_themes.ads.utils.DebugToaster
import timber.log.Timber

class FbBannerAdsManager(
    private val activity: Activity,
    private val adsPref: AdsPref
) {
    private val activeBannerAds = mutableMapOf<FrameLayout, AdView>()
    private val impressedContainers = mutableSetOf<FrameLayout>()

    fun loadAdaptiveBanner(
        container: FrameLayout,
        placementId: String,
        visibility: Int = View.VISIBLE,
        forceRefresh: Boolean = false,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null
    ) {
        loadBanner(container, placementId, AdSize.BANNER_HEIGHT_50, visibility, "Adaptive", forceRefresh, onAdLoaded, onAdFailed)
    }

    fun loadCollapsibleTopBanner(
        adContainer: FrameLayout,
        placementId: String,
        defaultVisibility: Int = View.VISIBLE,
        forceRefresh: Boolean = false,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null
    ) {
        loadBanner(adContainer, placementId, AdSize.BANNER_HEIGHT_50, defaultVisibility, "Top Collapsible", forceRefresh, onAdLoaded, onAdFailed)
    }

    fun loadCollapsibleBottomBanner(
        adContainer: FrameLayout,
        placementId: String,
        defaultVisibility: Int = View.VISIBLE,
        forceRefresh: Boolean = false,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null
    ) {
        loadBanner(adContainer, placementId, AdSize.BANNER_HEIGHT_50, defaultVisibility, "Bottom Collapsible", forceRefresh, onAdLoaded, onAdFailed)
    }

    fun loadRectangleAd(
        adContainer: FrameLayout,
        placementId: String,
        defaultVisibility: Int = View.VISIBLE,
        forceRefresh: Boolean = false,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null
    ) {
        loadBanner(adContainer, placementId, AdSize.RECTANGLE_HEIGHT_250, defaultVisibility, "Rectangle", forceRefresh, onAdLoaded, onAdFailed)
    }

    private fun loadBanner(
        container: FrameLayout,
        placementId: String,
        adSize: AdSize,
        visibility: Int,
        label: String,
        forceRefresh: Boolean,
        onAdLoaded: (() -> Unit)?,
        onAdFailed: ((String) -> Unit)?
    ) {
        if (visibility != View.VISIBLE) return
        val resolvedPlacementId = AdUnitIdSanitizer.sanitizeFbBanner(placementId)
        if (resolvedPlacementId.isBlank()) {
            container.visibility = View.GONE
            onAdFailed?.invoke("Facebook placement id is blank")
            return
        }

        val existingAdView = activeBannerAds[container]
        if (!forceRefresh && existingAdView != null && existingAdView.placementId == resolvedPlacementId) {
            Timber.d("FB banner already loaded in this container for placement=$resolvedPlacementId")
            container.visibility = visibility
            onAdLoaded?.invoke()
            return
        }

        activeBannerAds[container]?.let { destroyBanner(it, container) }
        impressedContainers.remove(container)

        val adView = AdView(activity, resolvedPlacementId, adSize)
        activeBannerAds[container] = adView

        (adView.parent as? ViewGroup)?.removeView(adView)
        container.removeAllViews()
        container.addView(adView)
        container.visibility = View.GONE

        val loadAdConfig = adView.buildLoadAdConfig()
            .withAdListener(object : AdListener {
                override fun onError(adObj: Ad?, adError: AdError) {
                    container.visibility = View.GONE
                    Timber.d("FB $label banner failed: ${adError.errorMessage}")
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastBannerEnabled(), "FB Banner ($label): Failed")
                    onAdFailed?.invoke(adError.errorMessage)
                }

                override fun onAdLoaded(adObj: Ad?) {
                    container.visibility = visibility
                    Timber.i("FB $label banner loaded")
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastBannerEnabled(), "FB Banner ($label): Loaded")
                    onAdLoaded?.invoke()
                }

                override fun onAdClicked(adObj: Ad?) {}
                override fun onLoggingImpression(adObj: Ad?) {
                    impressedContainers.add(container)
                    DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastBannerEnabled(), "FB Banner ($label): Shown")
                }
            })
            .build()

        adView.loadAd(loadAdConfig)
    }

    private fun destroyBanner(adView: AdView, container: FrameLayout) {
        try {
            (adView.parent as? ViewGroup)?.removeView(adView)
            adView.destroy()
        } catch (e: Exception) {
            Timber.e(e, "Error destroying FB banner")
        } finally {
            activeBannerAds.remove(container)
            impressedContainers.remove(container)
        }
    }

    fun isImpressed(container: FrameLayout): Boolean {
        return impressedContainers.contains(container)
    }

    fun destroyAllBanners() {
        activeBannerAds.toMap().forEach { (container, adView) -> destroyBanner(adView, container) }
        activeBannerAds.clear()
    }
}
