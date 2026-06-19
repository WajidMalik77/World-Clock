package com.worldclock.app_themes.ads.helpers.ui

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import dagger.hilt.android.scopes.ActivityScoped
import com.worldclock.app_themes.ads.di.ActivityBanner
import com.worldclock.app_themes.ads.helpers.usecases.AdConfigRepository
import com.worldclock.app_themes.ads.helpers.usecases.BannerAdRepository
import com.worldclock.app_themes.ads.helpers.usecases.CheckAdEligibilityUseCase
import com.worldclock.app_themes.ads.managers.BannerAdsManager
import javax.inject.Inject

@ActivityScoped
class BannerAdOrchestrator @Inject constructor(
    private val bannerAdRepository: BannerAdRepository,
    private val adConfigRepository: AdConfigRepository,
    @param:ActivityBanner private val bannerAdsManager: BannerAdsManager,
    private val checkEligibility: CheckAdEligibilityUseCase
) {

    suspend fun loadBannerAd(
        context: Context,
        screen: String,
        position: String,
        container: FrameLayout,
        shimmer: View,
        adId: String? = null
    ) {
        // Check eligibility
        val eligibility = checkEligibility(context)
        if (!eligibility.canShowAds) {
            shimmer.visibility = View.GONE
            (shimmer as? com.facebook.shimmer.ShimmerFrameLayout)?.stopShimmer()
            container.visibility = View.GONE
            return
        }

        // Get configuration
        val visible = bannerAdRepository.getBannerVisibility(screen, position)
        val type = bannerAdRepository.getBannerType(screen, position)
        val bannerId = adId ?: adConfigRepository.getBannerAdUnitId(screen, position)

        // Handle visibility
        if (visible) {
            shimmer.visibility = View.VISIBLE
            (shimmer as? com.facebook.shimmer.ShimmerFrameLayout)?.startShimmer()
        } else {
            shimmer.visibility = View.GONE
            (shimmer as? com.facebook.shimmer.ShimmerFrameLayout)?.stopShimmer()
            container.visibility = View.GONE
            return
        }

        // Load appropriate banner type
        loadBannerByType(type, position, container, bannerId, shimmer)
    }

    private fun loadBannerByType(
        type: String,
        position: String,
        container: FrameLayout,
        bannerId: String,
        shimmer: View
    ) {
        val onLoaded: () -> Unit = {
            shimmer.visibility = View.GONE
            (shimmer as? com.facebook.shimmer.ShimmerFrameLayout)?.stopShimmer()
        }
        val onFailed: () -> Unit = {
            shimmer.visibility = View.VISIBLE
            (shimmer as? com.facebook.shimmer.ShimmerFrameLayout)?.startShimmer()
        }

        when (type) {
            "a" -> bannerAdsManager.loadAdaptiveBanner(
                container, bannerId, View.VISIBLE, onLoaded, onAdFailed = { onFailed() }
            )

            "r" -> bannerAdsManager.loadRectangleAd(
                container, bannerId, View.VISIBLE, onLoaded, onAdFailed = { onFailed() }
            )

            "c" -> loadCollapsibleBanner(position, container, bannerId, onLoaded, onFailed)
            else -> {
                shimmer.visibility = View.GONE
                (shimmer as? com.facebook.shimmer.ShimmerFrameLayout)?.stopShimmer()
                container.visibility = View.GONE
            }
        }
    }

    private fun loadCollapsibleBanner(
        position: String,
        container: FrameLayout,
        bannerId: String,
        onLoaded: () -> Unit,
        onFailed: () -> Unit
    ) {
        when (position) {
            "top" -> bannerAdsManager.loadCollapsibleTopBanner(
                container, bannerId, View.VISIBLE, onLoaded, onAdFailed = { onFailed() }
            )

            "bottom" -> bannerAdsManager.loadCollapsibleBottomBanner(
                container, bannerId, View.VISIBLE, onLoaded, onAdFailed = { onFailed() }
            )

            else -> container.visibility = View.GONE
        }
    }

    fun destroyAllBanners() {
        bannerAdsManager.destroyAllBanners()
    }
}
