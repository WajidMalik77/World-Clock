package com.worldclock.app_themes.ads.helpers.ui

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import dagger.hilt.android.scopes.ActivityScoped
import com.worldclock.app_themes.ads.di.ActivityBanner
import com.worldclock.app_themes.ads.helpers.models.AdNetwork
import com.worldclock.app_themes.ads.helpers.usecases.AdConfigRepository
import com.worldclock.app_themes.ads.helpers.usecases.BannerAdRepository
import com.worldclock.app_themes.ads.helpers.usecases.CheckAdEligibilityUseCase
import com.worldclock.app_themes.ads.managers.BannerAdsManager
import com.worldclock.app_themes.ads.managers.facebook.FbAdInitializer
import com.worldclock.app_themes.ads.managers.facebook.FbBannerAdsManager
import javax.inject.Inject

@ActivityScoped
class BannerAdOrchestrator @Inject constructor(
    private val bannerAdRepository: BannerAdRepository,
    private val adConfigRepository: AdConfigRepository,
    @param:ActivityBanner private val bannerAdsManager: BannerAdsManager,
    @param:ActivityBanner private val fbBannerAdsManager: FbBannerAdsManager,
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
        if (position.equals("bottom", ignoreCase = true)) {
            shimmer.visibility = View.GONE
            container.visibility = View.GONE
            return
        }

        // Check eligibility
        val eligibility = checkEligibility(context)
        if (!eligibility.canShowAds) {
            shimmer.visibility = View.GONE
            container.visibility = View.GONE
            return
        }

        // Get configuration
        val visible = bannerAdRepository.getBannerVisibility(screen, position)
        val type = bannerAdRepository.getBannerType(screen, position)

        // Handle visibility
        shimmer.visibility = if (visible) View.VISIBLE else View.GONE
        if (!visible) {
            container.visibility = View.GONE
            return
        }

        val plan = adConfigRepository.getBannerWaterfallPlan(screen, position)
        if (plan?.fallback == AdNetwork.FACEBOOK || plan?.primary == AdNetwork.FACEBOOK) {
            FbAdInitializer.initialize(context.applicationContext)
        }
        val primary = plan?.primary ?: AdNetwork.ADMOB
        val fallback = plan?.fallback

        val bannerId = adId ?: adConfigRepository.getBannerAdUnitId(screen, position, primary)

        loadBannerByType(type, position, container, bannerId, shimmer, primary) { failedMessage ->
            if (fallback != null) {
                val fallbackId = adId ?: adConfigRepository.getBannerAdUnitId(screen, position, fallback)
                loadBannerByType(type, position, container, fallbackId, shimmer, fallback) {
                    shimmer.visibility = View.GONE
                }
            } else {
                shimmer.visibility = View.GONE
            }
        }
    }

    private fun loadBannerByType(
        type: String,
        position: String,
        container: FrameLayout,
        bannerId: String,
        shimmer: View,
        network: AdNetwork,
        onFailed: (String) -> Unit
    ) {
        val onLoaded = { shimmer.visibility = View.GONE }

        when (type) {
            "a" -> if (network == AdNetwork.FACEBOOK) {
                fbBannerAdsManager.loadAdaptiveBanner(container, bannerId, View.VISIBLE, onLoaded, onAdFailed = onFailed)
            } else {
                bannerAdsManager.loadAdaptiveBanner(container, bannerId, View.VISIBLE, onLoaded, onAdFailed = { onFailed(it.message) })
            }

            "r" -> if (network == AdNetwork.FACEBOOK) {
                fbBannerAdsManager.loadRectangleAd(container, bannerId, View.VISIBLE, onLoaded, onAdFailed = onFailed)
            } else {
                bannerAdsManager.loadRectangleAd(container, bannerId, View.VISIBLE, onLoaded, onAdFailed = { onFailed(it.message) })
            }

            "c" -> loadCollapsibleBanner(position, container, bannerId, onLoaded, network, onFailed)
            else -> container.visibility = View.GONE
        }
    }

    private fun loadCollapsibleBanner(
        position: String,
        container: FrameLayout,
        bannerId: String,
        onLoaded: () -> Unit,
        network: AdNetwork,
        onFailed: (String) -> Unit
    ) {
        when (position) {
            "top" -> if (network == AdNetwork.FACEBOOK) {
                fbBannerAdsManager.loadCollapsibleTopBanner(container, bannerId, View.VISIBLE, onLoaded, onAdFailed = onFailed)
            } else {
                bannerAdsManager.loadCollapsibleTopBanner(container, bannerId, View.VISIBLE, onLoaded, onAdFailed = { onFailed(it.message) })
            }

            else -> container.visibility = View.GONE
        }
    }

    fun destroyAllBanners() {
        bannerAdsManager.destroyAllBanners()
        fbBannerAdsManager.destroyAllBanners()
    }
}
