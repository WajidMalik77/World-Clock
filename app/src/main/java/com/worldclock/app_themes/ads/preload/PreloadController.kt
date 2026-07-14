package com.worldclock.app_themes.ads.preload

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.Utils
import com.worldclock.app_themes.core.analytics.AppEventLogger
import com.worldclock.app_themes.presentation.activities.AddAlarmActivity
import com.worldclock.app_themes.presentation.activities.AddAllRemindersActivity
import com.worldclock.app_themes.presentation.activities.AddClockActivity
import com.worldclock.app_themes.presentation.activities.AddReminderActiviity
import com.worldclock.app_themes.presentation.activities.AddWidgetActivity
import com.worldclock.app_themes.presentation.activities.AlarmActivity
import com.worldclock.app_themes.presentation.activities.AllRemindersActivity
import com.worldclock.app_themes.presentation.activities.ClockActivity
import com.worldclock.app_themes.presentation.activities.CompassActivity
import com.worldclock.app_themes.presentation.activities.ExitActivity
import com.worldclock.app_themes.presentation.activities.LanguagesActivity
import com.worldclock.app_themes.presentation.activities.MainActivity
import com.worldclock.app_themes.presentation.activities.MenuActivity
import com.worldclock.app_themes.presentation.activities.OnBoardingActivity
import com.worldclock.app_themes.presentation.activities.PlaySoundActivity
import com.worldclock.app_themes.presentation.activities.SleepSoundActivity
import com.worldclock.app_themes.presentation.activities.Splash
import com.worldclock.app_themes.presentation.activities.StopWatchActivity
import com.worldclock.app_themes.presentation.activities.TimerActivity
import com.worldclock.app_themes.presentation.activities.WidgetActivity

object PreloadController {


    fun heightForBanner(context: Context): Int =
        context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._50sdp)

    fun heightForNoMediaNative(context: Context): Int =
        context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._80sdp)

    fun heightForMediaNative(context: Context): Int =
        context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._220sdp)

    fun marginForNativeAds(layout: RelativeLayout, context: Context) {
        val margin = context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._6sdp)
        (layout.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            setMargins(margin, margin, margin, margin)
            layout.layoutParams = this
        }
    }

    fun loadAdInBannerPosition(
        flag: Int, position: String,
        context: Context, adIDBanner: String, adIDNative: String
    ) {
        BannerPreload.adBannerBottomLiveData.value = false
        BannerPreload.adBannerTopLiveData.value = false

        NativePreload.adNativeBottomLiveData.value = false
        NativePreload.adNativeTopLiveData.value = false
        NativePreload.adNativeNormalLiveData.value = false

        if (Utils.isPremium){
            return
        }


        if (position == "top") {
            when (flag) {
                1 -> {

                    Log.d("PRLO","load called")

                    //banner
                    BannerPreload.preLoadBannerAdTop(context, adIDBanner)
                }

                2 -> {
                    //collapsible top

                }

                3 -> {
                    //collapsible bottom

                }

                4 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }

                5 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }

                6 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }

                7 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }

                8 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }

                9 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }
                10 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }
                11 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }

                else -> {


                }
            }
        }
        else if (position == "bottom") {
            when (flag) {
                1 -> {
                    //banner
                    Log.d("BANNER_CALLED","called")
                    BannerPreload.preLoadBannerAdBottom(context, adIDBanner)
                }

                2 -> {
                    //collapsible top

                }

                3 -> {
                    //collapsible bottom

                }

                4 -> {
                    NativePreload.loadBottomNative(context, adIDNative)
                }

                5 -> {
                    NativePreload.loadBottomNative(context, adIDNative)
                }

                6 -> {
                    NativePreload.loadBottomNative(context, adIDNative)
                }

                7 -> {
                    NativePreload.loadBottomNative(context, adIDNative)
                }

                8 -> {
                    NativePreload.loadBottomNative(context, adIDNative)
                }

                9 -> {
                    NativePreload.loadBottomNative(context, adIDNative)
                }

                else -> {


                }
            }
        }
    }

    fun loadAdInNativePosition(flag: Int, context: Context, adIDNative: String) {

        BannerPreload.adBannerBottomLiveData.value = false
        BannerPreload.adBannerTopLiveData.value = false

        NativePreload.adNativeBottomLiveData.value = false
        NativePreload.adNativeTopLiveData.value = false
        NativePreload.adNativeNormalLiveData.value = false


        if (Utils.isPremium){
            return
        }

        when(flag){
            1 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }

            2 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }

            3 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }

            4 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }

            5 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }

            6 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }
            7 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }
            8 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }
            else -> {


            }
        }
    }

    fun observeBanner(
        context: Context,
        liveData: MutableLiveData<Boolean>,
        adParent: RelativeLayout,
        adLayout: FrameLayout,
        textView: TextView,
        flag: Int,
        adPosition: String,
        window: Window,
        liveDataNative: MutableLiveData<Boolean>,
        adIDCollapsible: String = "",
        callback: () -> Unit
    )
    {


        if (Utils.isPremium){
            adParent.visibility = View.GONE
            return
        }


        if (adPosition == "top") {
            when (flag) {
                1 -> {
                    //banner
                    val owner = context as? LifecycleOwner ?: return
                    liveData.observe(owner) {
                        if (it == true) {
                            textView.visibility = View.GONE
                            BannerPreload.showPreloadedBannerTop(
                                adLayout,
                                adParent,
                                textView,
                                context
                            )
                        }
                    }
                }

                2 -> {
                    //collapsible top
                    loadCollapsibleBanner(context as AppCompatActivity,adLayout, adIDCollapsible, textView, "top")
                }

                3 -> {
                    //collapsible bottom
                    loadCollapsibleBanner(context as AppCompatActivity,adLayout, adIDCollapsible, textView, "bottom")

                }

                4 -> {
                    adLayout.minimumHeight =
                        heightForMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,1,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )

                }

                5 -> {
                    adLayout.minimumHeight =
                        heightForMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,2,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )
                }

                6 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,3,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )
                }

                7 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,4,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )
                }

                8 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,5,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )
                }

                9 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,6,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )
                }

                10 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,6,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )
                }

                11 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,6,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )
                }

                else -> {
                    adParent.visibility = View.GONE
                }
            }
        }
        else if (adPosition == "bottom") {
            when (flag) {
                1 -> {
                    //banner
                    val owner = context as? LifecycleOwner ?: return
                    liveData.observe(owner) {
                        if (it == true) {
                            textView.visibility = View.GONE
                            BannerPreload.showPreloadedBannerBottom(
                                adLayout,
                                adParent,
                                textView,
                                context
                            )
                        }
                    }
                }

                2 -> {
                    //collapsible top
                    loadCollapsibleBanner(context as AppCompatActivity,adLayout, adIDCollapsible, textView, "top")
                }

                3 -> {
                    //collapsible bottom
                    loadCollapsibleBanner(context as AppCompatActivity,adLayout, adIDCollapsible, textView, "bottom")

                }

                4 -> {
                    adLayout.minimumHeight =
                        heightForMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,1,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )

                }

                5 -> {
                    adLayout.minimumHeight =
                        heightForMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,2,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )
                }

                6 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,3,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )
                }

                7 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,4,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )
                }

                8 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,5,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )
                }

                9 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,6,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )
                }

                10 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,6,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )
                }

                11 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,6,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )
                }

                else -> {
                    adParent.visibility = View.GONE
                }
            }
        }


    }

    fun observeNative(
        context: Context,
        adParent: RelativeLayout,
        adLayout: FrameLayout,
        textView: TextView,
        flag: Int,
        window: Window,
        liveDataNative: MutableLiveData<Boolean>
    )
    {


        if (Utils.isPremium){
            adParent.visibility = View.GONE
            return
        }


        when (flag) {
            1 -> {
                adLayout.minimumHeight =
                    heightForMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,1,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )

            }

            2 -> {
                adLayout.minimumHeight =
                    heightForMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,2,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )
            }

            3 -> {
                adLayout.minimumHeight =
                    heightForNoMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,3,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )
            }

            4 -> {
                adLayout.minimumHeight =
                    heightForNoMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,4,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )
            }

            5 -> {
                adLayout.minimumHeight =
                    heightForNoMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,5,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )
            }

            6 -> {
                adLayout.minimumHeight =
                    heightForNoMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,6,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )
            }

            7 -> {
                adLayout.minimumHeight =
                    heightForNoMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,6,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )
            }

            8 -> {
                adLayout.minimumHeight =
                    heightForNoMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,6,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )
            }

            else -> {
                adParent.visibility = View.GONE
            }
        }

    }

    private fun getCollapsibleAdId(context: AppCompatActivity, isTop: Boolean): String {
        return when (context) {
            is Splash -> if (isTop) GetFirebase.adIdSplash_collapsibleTop else GetFirebase.adIdSplash_collapsibleBottom
            is AddAlarmActivity -> if (isTop) GetFirebase.adIdAddAlarm_collapsibleTop else GetFirebase.adIdAddAlarm_collapsibleBottom
            is AddAllRemindersActivity -> {
                if (isTop) GetFirebase.adIdAddAllReminders_collapsibleTop else GetFirebase.adIdAddAllReminders_collapsibleBottom
            }
            is AddClockActivity -> if (isTop) GetFirebase.adIdAddClock_collapsibleTop else GetFirebase.adIdAddClock_collapsibleBottom
            is AddReminderActiviity -> if (isTop) GetFirebase.adIdAddReminder_collapsibleTop else GetFirebase.adIdAddReminder_collapsibleBottom
            is AddWidgetActivity -> if (isTop) GetFirebase.adIdAddWidget_collapsibleTop else GetFirebase.adIdAddWidget_collapsibleBottom
            is AlarmActivity -> if (isTop) GetFirebase.adIdAlarm_collapsibleTop else GetFirebase.adIdAlarm_collapsibleBottom
            is AllRemindersActivity -> if (isTop) GetFirebase.adIdAllReminders_collapsibleTop else GetFirebase.adIdAllReminders_collapsibleBottom
            is ClockActivity -> if (isTop) GetFirebase.adIdClock_collapsibleTop else GetFirebase.adIdClock_collapsibleBottom
            is CompassActivity -> if (isTop) GetFirebase.adIdCompass_collapsibleTop else GetFirebase.adIdCompass_collapsibleBottom
            is ExitActivity -> if (isTop) GetFirebase.adIdExit_collapsibleTop else GetFirebase.adIdExit_collapsibleBottom
            is LanguagesActivity -> if (isTop) GetFirebase.adIdLanguagesActivity_collapsibleTop else GetFirebase.adIdLanguagesActivity_collapsibleBottom
            is MainActivity -> if (isTop) GetFirebase.adIdMainActivity_collapsibleTop else GetFirebase.adIdMainActivity_collapsibleBottom
            is MenuActivity -> if (isTop) GetFirebase.adIdMenu_collapsibleTop else GetFirebase.adIdMenu_collapsibleBottom
            is OnBoardingActivity -> if (isTop) GetFirebase.adIdOnboarding_collapsibleTop else GetFirebase.adIdOnboarding_collapsibleBottom
            is PlaySoundActivity -> if (isTop) GetFirebase.adIdPlaySound_collapsibleTop else GetFirebase.adIdPlaySound_collapsibleBottom
            is SleepSoundActivity -> if (isTop) GetFirebase.adIdSleepSound_collapsibleTop else GetFirebase.adIdSleepSound_collapsibleBottom
            is StopWatchActivity -> if (isTop) GetFirebase.adIdStopWatch_collapsibleTop else GetFirebase.adIdStopWatch_collapsibleBottom
            is TimerActivity -> if (isTop) GetFirebase.adIdTimer_collapsibleTop else GetFirebase.adIdTimer_collapsibleBottom
            is WidgetActivity -> if (isTop) GetFirebase.adIdWidget_collapsibleTop else GetFirebase.adIdWidget_collapsibleBottom
            else -> if (isTop) GetFirebase.adIdMainActivity_collapsibleTop else GetFirebase.adIdMainActivity_collapsibleBottom
        }
    }

    private fun loadCollapsibleBanner(
        context: AppCompatActivity,
        adLayout: FrameLayout,
        adID: String,
        textView: TextView,
        position: String? = null
    ) {
        try {

            val isTop = position == "top"
            val adIDCollapsible = getCollapsibleAdId(context, isTop)

            val adView = AdView(context).apply {
                setAdSize(getAdSize(context))
                this.adUnitId = adIDCollapsible

            }

            val adRequestBuilder = AdRequest.Builder()

            // Add collapsible extras only if applicable
            if (position != null) {
                val extras = Bundle().apply { putString("collapsible", position) }
                adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            }

            adLayout.addView(adView)
            adView.loadAd(adRequestBuilder.build())

            adView.setOnPaidEventListener { adValue ->
                // Then pass it
                AppEventLogger.logCustomImpressions(
                    context, // 'this' in Activity
                    adValue = adValue,
                    adUnitId = adView?.adUnitId.toString(),
                    adFormat = "native"
                )
            }

            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {

                    if (GetFirebase.toastForAds) {
                        Utils.showMessage(context, "banner ad loaded")
                    }


                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.d("TAG_LOADED", error.message)

                    if (GetFirebase.toastForAds) {
                        Utils.showMessage(context, error.message)
                    }

                }

                override fun onAdClicked() {

                }
            }
        } catch (e: Exception) {
            // Optional: log or print the error
        }
    }

    private fun getAdSize(context: Context): AdSize {
        val outMetrics = DisplayMetrics()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay.getMetrics(outMetrics)
        val adWidth = (outMetrics.widthPixels / outMetrics.density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

}