package com.worldclock.app_themes.ads.preload

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.Utils
import com.worldclock.app_themes.ads.utils.Utils.getSdpHeightInPx
import com.worldclock.app_themes.core.analytics.AppEventLogger

object BannerPreload {

    var bannerAdTopLoaded: AdView? = null
    var bannerAdBottomLoaded: AdView? = null


    //ad live data
    var adBannerTopLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    var adBannerBottomLiveData: MutableLiveData<Boolean> = MutableLiveData(false)


    private fun getAdSize(context: Context): AdSize {
        val outMetrics = DisplayMetrics()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay.getMetrics(outMetrics)
        val adWidth = (outMetrics.widthPixels / outMetrics.density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

    fun preLoadBannerAdTop(context: Context, adId: String) {

        bannerAdTopLoaded = null
        val adView = AdView(context)

        adView.apply {
            adUnitId = adId

            setAdSize(
                getAdSize(
                    context
                )
            )
            loadAd(AdRequest.Builder().build())
        }

        bannerAdTopLoaded = adView

        bannerAdTopLoaded?.adListener = object : com.google.android.gms.ads.AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()

                adBannerTopLiveData.value = true
                if (GetFirebase.toastForAds) {
                    Utils.showMessage(context, "banner loaded")
                }

            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)

                adBannerTopLiveData.value = false

                Log.d("PRELOOO", "failed loaded")

                if (GetFirebase.toastForAds) {
                    Utils.showMessage(context, "banner failed to load")
                }

            }

            override fun onAdClicked() {
                super.onAdClicked()



            }

        }

    }

    fun preLoadBannerAdBottom(context: Context, adID: String) {

        bannerAdBottomLoaded = null

        val adView = AdView(context)

        adView.apply {
            adUnitId = adID
            setAdSize(
                getAdSize(
                    context
                )
            )
            loadAd(AdRequest.Builder().build())
        }

        bannerAdBottomLoaded = adView

        bannerAdBottomLoaded?.adListener = object : com.google.android.gms.ads.AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()

                adBannerBottomLiveData.value = true
                if (GetFirebase.toastForAds) {
                    Utils.showMessage(context, "banner loaded")
                }

            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)

                adBannerBottomLiveData.value = false

                if (GetFirebase.toastForAds) {
                    Utils.showMessage(context, "banner failed to load")
                }

            }

            override fun onAdClicked() {
                super.onAdClicked()


            }

        }

    }


    fun showPreloadedBannerTop(
        layoutAd: FrameLayout,
        adViewContainer: RelativeLayout,
        textLayout: TextView,
        contextAd: Context
    ) {
        try {

            val topPadding =  contextAd.getSdpHeightInPx(2)

            adViewContainer.setPadding(
                adViewContainer.paddingLeft,
                topPadding,
                adViewContainer.paddingRight,
                adViewContainer.paddingBottom
            )

            textLayout.visibility = View.GONE

            layoutAd.removeAllViews()

            layoutAd.post {
                layoutAd.addView(bannerAdTopLoaded)
            }

            bannerAdTopLoaded?.setOnPaidEventListener { adValue ->
                // Then pass it
                AppEventLogger.logCustomImpressions(
                    contextAd, // 'this' in Activity
                    adValue = adValue,
                    adUnitId = bannerAdTopLoaded?.adUnitId.toString(),
                    adFormat = "banner"
                )
            }

        } catch (e: Exception) {

        }
    }

    fun showPreloadedBannerBottom(
        layoutAd: FrameLayout,
        adViewContainer: RelativeLayout,
        textLayout: TextView,
        contextAd: Context
    ) {
        try {

            val topPadding = contextAd.getSdpHeightInPx(2)

            adViewContainer.setPadding(
                adViewContainer.paddingLeft,
                topPadding,
                adViewContainer.paddingRight,
                adViewContainer.paddingBottom
            )

            textLayout.visibility = View.GONE

            layoutAd.removeAllViews()

            layoutAd.post {
                layoutAd.addView(bannerAdBottomLoaded)
            }

            bannerAdBottomLoaded?.setOnPaidEventListener { adValue ->
                // Then pass it
                AppEventLogger.logCustomImpressions(
                    contextAd, // 'this' in Activity
                    adValue = adValue,
                    adUnitId = bannerAdBottomLoaded?.adUnitId.toString(),
                    adFormat = "banner"
                )
            }

        } catch (e: Exception) {

        }
    }

}