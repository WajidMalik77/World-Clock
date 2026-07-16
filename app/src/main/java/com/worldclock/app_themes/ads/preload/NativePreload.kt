package com.worldclock.app_themes.ads.preload

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.worldclock.app_themes.R
import com.worldclock.app_themes.ads.preload.BannerPreload.bannerAdBottomLoaded
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.Utils
import com.worldclock.app_themes.ads.utils.Utils.getSdpHeightInPx
import com.worldclock.app_themes.ads.utils.Utils.getSspTextSizeInPx
import com.worldclock.app_themes.core.analytics.AppEventLogger
import com.worldclock.app_themes.databinding.NativeAdWithoutMediaOneBinding
import com.worldclock.app_themes.databinding.NativeAdWithoutMediaThreeBinding
import com.worldclock.app_themes.databinding.NativeAdWithoutMediaTwoBinding
import com.worldclock.app_themes.databinding.NativeWithMediaFourBinding
import com.worldclock.app_themes.databinding.NativeWithMediaOneBinding
import com.worldclock.app_themes.databinding.NativeWithMediaThreeBinding
import com.worldclock.app_themes.databinding.NativeWithMediaTwoBinding
import com.worldclock.app_themes.databinding.NativeWithoutMediaFourBinding
import com.worldclock.app_themes.presentation.activities.AddAlarmActivity
import com.worldclock.app_themes.presentation.activities.LanguagesActivity
import com.worldclock.app_themes.presentation.activities.OnBoardingActivity
import com.worldclock.app_themes.presentation.activities.Splash
import com.worldclock.app_themes.presentation.activities.AddAllRemindersActivity
import com.worldclock.app_themes.presentation.activities.AddClockActivity
import com.worldclock.app_themes.presentation.activities.AddReminderActiviity
import com.worldclock.app_themes.presentation.activities.AddWidgetActivity
import com.worldclock.app_themes.presentation.activities.AlarmActivity
import com.worldclock.app_themes.presentation.activities.AllRemindersActivity
import com.worldclock.app_themes.presentation.activities.ClockActivity
import com.worldclock.app_themes.presentation.activities.CompassActivity
import com.worldclock.app_themes.presentation.activities.ExitActivity
import com.worldclock.app_themes.presentation.activities.MainActivity
import com.worldclock.app_themes.presentation.activities.MenuActivity
import com.worldclock.app_themes.presentation.activities.PlaySoundActivity
import com.worldclock.app_themes.presentation.activities.SleepSoundActivity
import com.worldclock.app_themes.presentation.activities.StopWatchActivity
import com.worldclock.app_themes.presentation.activities.TimerActivity
import com.worldclock.app_themes.presentation.activities.WidgetActivity

object NativePreload {

    var nativeAdTop: NativeAd? = null
    var nativeAdBottom: NativeAd? = null
    var nativeAdNormal: NativeAd? = null

    var adNativeTopLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    var adNativeBottomLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    var adNativeNormalLiveData: MutableLiveData<Boolean> = MutableLiveData(false)


    fun loadTopNative(context: Context, adID: String) {
        if (!GetFirebase.enable_banner_native_ads){
            return
        }
        nativeAdTop = null

        val builder = AdLoader.Builder(context, adID)

        val videoOptions =
            VideoOptions.Builder().build()

        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()

        builder.withNativeAdOptions(adOptions)

        builder.forNativeAd { nativeAdNew ->
            nativeAdTop = nativeAdNew

        }

        val adLoader =
            builder
                .withAdListener(
                    object : AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {


                            if (GetFirebase.toastForAds)
                                Utils.showMessage(
                                    context,
                                    "Failed to load native AdMob with error"
                                )

                        }

                        override fun onAdLoaded() {
                            super.onAdLoaded()

                            adNativeTopLiveData.value = true

                            if (GetFirebase.toastForAds) {
                                Utils.showMessage(context, "native ad loaded")
                            }

                        }

                        override fun onAdClicked() {
                            super.onAdClicked()


                        }


                    }
                )
                .build()

        adLoader.loadAd(AdRequest.Builder().build())

    }

    fun loadBottomNative(context: Context, adID: String) {
        if (!GetFirebase.enable_banner_native_ads){
            return
        }
        nativeAdBottom = null

        val builder = AdLoader.Builder(
            context,adID

        )

        val videoOptions =
            VideoOptions.Builder().build()

        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()

        builder.withNativeAdOptions(adOptions)

        builder.forNativeAd { nativeAdNew ->
            nativeAdBottom = nativeAdNew

        }

        val adLoader =
            builder
                .withAdListener(
                    object : AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {


                            if (GetFirebase.toastForAds)
                                Utils.showMessage(
                                    context,
                                    "Failed to load native AdMob with error"
                                )

                        }

                        override fun onAdLoaded() {
                            super.onAdLoaded()

                            adNativeBottomLiveData.value = true

                            if (GetFirebase.toastForAds) {
                                Utils.showMessage(context, "native ad loaded")
                            }

                        }

                        override fun onAdClicked() {
                            super.onAdClicked()


                        }


                    }
                )
                .build()

        adLoader.loadAd(AdRequest.Builder().build())

    }

    fun loadNormalNative(context: Context, adId: String) {
        if (!GetFirebase.enable_banner_native_ads){
            return
        }
        nativeAdNormal = null

        val builder = AdLoader.Builder(context, adId)

        val videoOptions =
            VideoOptions.Builder().build()

        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()

        builder.withNativeAdOptions(adOptions)

        builder.forNativeAd { nativeAdNew ->
            nativeAdNormal = nativeAdNew

        }

        val adLoader =
            builder
                .withAdListener(
                    object : AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {


                            if (GetFirebase.toastForAds)
                                Utils.showMessage(
                                    context,
                                    "Failed to load native AdMob with error"
                                )

                        }

                        override fun onAdLoaded() {
                            super.onAdLoaded()

                            adNativeNormalLiveData.value = true
                            if (GetFirebase.toastForAds) {
                                Utils.showMessage(context, "native ad loaded")
                            }

                        }

                        override fun onAdClicked() {
                            super.onAdClicked()

                        }


                    }
                )
                .build()

        adLoader.loadAd(AdRequest.Builder().build())

    }

    private fun getNativeAdId(context: AppCompatActivity, isTop: Boolean): String {
        return when (context) {
            is Splash -> if (isTop) GetFirebase.adIdSplash_nativeTop else GetFirebase.adIdSplash_nativeBottom
            is AddAlarmActivity -> if (isTop) GetFirebase.adIdAddAlarm_nativeTop else GetFirebase.adIdAddAlarm_nativeBottom
            is AddAllRemindersActivity -> if (isTop) GetFirebase.adIdAddAllReminders_nativeTop else GetFirebase.adIdAddAllReminders_nativeBottom
            is AddClockActivity -> if (isTop) GetFirebase.adIdAddClock_nativeTop else GetFirebase.adIdAddClock_nativeBottom
            is AddReminderActiviity -> if (isTop) GetFirebase.adIdAddReminder_nativeTop else GetFirebase.adIdAddReminder_nativeBottom
            is AlarmActivity -> if (isTop) GetFirebase.adIdAlarm_nativeTop else GetFirebase.adIdAlarm_nativeBottom
            is AllRemindersActivity -> if (isTop) GetFirebase.adIdAllReminders_nativeTop else GetFirebase.adIdAllReminders_nativeBottom
            is ClockActivity -> if (isTop) GetFirebase.adIdClock_nativeTop else GetFirebase.adIdClock_nativeBottom
            is CompassActivity -> if (isTop) GetFirebase.adIdCompass_nativeTop else GetFirebase.adIdCompass_nativeBottom
            is ExitActivity -> if (isTop) GetFirebase.adIdExit_nativeTop else GetFirebase.adIdExit_nativeBottom
            is LanguagesActivity -> if (isTop) GetFirebase.adIdLanguagesActivity_nativeTop else GetFirebase.adIdLanguagesActivity_nativeBottom
            is MainActivity -> if (isTop) GetFirebase.adIdMainActivity_nativeTop else GetFirebase.adIdMainActivity_nativeBottom
            is MenuActivity -> if (isTop) GetFirebase.adIdMenu_nativeTop else GetFirebase.adIdMenu_nativeBottom
            is OnBoardingActivity -> if (isTop) GetFirebase.adIdOnboarding_nativeTop else GetFirebase.adIdOnboarding_nativeBottom
            is PlaySoundActivity -> if (isTop) GetFirebase.adIdPlaySound_nativeTop else GetFirebase.adIdPlaySound_nativeBottom
            is SleepSoundActivity -> if (isTop) GetFirebase.adIdSleepSound_nativeTop else GetFirebase.adIdSleepSound_nativeBottom
            is StopWatchActivity -> if (isTop) GetFirebase.adIdStopWatch_nativeTop else GetFirebase.adIdStopWatch_nativeBottom
            is TimerActivity -> if (isTop) GetFirebase.adIdTimer_nativeTop else GetFirebase.adIdTimer_nativeBottom
            is WidgetActivity -> if (isTop) GetFirebase.adIdWidget_nativeTop else GetFirebase.adIdWidget_nativeBottom
            else -> if (isTop) GetFirebase.adIdMainActivity_nativeTop else GetFirebase.adIdMainActivity_nativeBottom
        }
    }

    fun showNativeAdForPreload(
        layoutParent: RelativeLayout,
        layoutAdView: FrameLayout,
        activity: AppCompatActivity,
        windowActivity: Window,
        i: Int,
        ad: NativeAd?,
        textView: TextView,
        liveDataNative: MutableLiveData<Boolean>, position: String
    ) {
        val isTop = position.equals("top", ignoreCase = true)
        val owner = activity as? LifecycleOwner ?: return
        liveDataNative.observe(owner) {
            if (it == true) {
                var native_ad: NativeAd? = null
                if (position == "top") {
                    native_ad = NativePreload.nativeAdTop
                } else {
                    native_ad = NativePreload.nativeAdBottom
                }

                native_ad?.setOnPaidEventListener { adValue ->
                    // Then pass it
                    AppEventLogger.logCustomImpressions(
                        activity, // 'this' in Activity
                        adValue = adValue,
                        adUnitId = getNativeAdId(activity,isTop),
                        adFormat = "native"
                    )
                }

                textView.visibility = View.GONE
                when (i) {
                    1 -> {
                        val admobBinding =
                            NativeWithMediaOneBinding.inflate(windowActivity.layoutInflater)

                        if (activity is Splash) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_splash * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_splash))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_splash.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_splash))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_splash))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_splash.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_splash.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_splash_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_splash,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is OnBoardingActivity) {


                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_onboardingscreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_onboardingscreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_onboardingscreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_onboardingscreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_onboardingscreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_onboarding.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_onboarding.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_onboarding_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_onboardingscreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is LanguagesActivity) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_languagescreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_languagescreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_languagescreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_languagescreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_languagescreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_language_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_languagescreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_otherscreens * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_otherscreens))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_otherscreens.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_otherscreens))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_otherscreens))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_other_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_otherscreens,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }





                        showNativeAdView(native_ad!!, admobBinding)
                        layoutAdView.removeAllViews()
                        layoutAdView.addView(admobBinding.root)
                    }

                    2 -> {
                        val admobBinding =
                            NativeWithMediaTwoBinding.inflate(windowActivity.layoutInflater)
                        if (activity is Splash) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_splash * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_splash))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_splash.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_splash))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_splash))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_splash.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_splash.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_splash_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_splash,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is OnBoardingActivity) {


                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_onboardingscreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_onboardingscreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_onboardingscreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_onboardingscreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_onboardingscreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_onboarding.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_onboarding.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_onboarding_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_onboardingscreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is LanguagesActivity) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_languagescreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_languagescreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_languagescreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_languagescreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_languagescreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_language_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_languagescreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_otherscreens * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_otherscreens))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_otherscreens.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_otherscreens))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_otherscreens))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_other_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_otherscreens,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }




                        showNativeAdView(native_ad!!, admobBinding)
                        layoutAdView.removeAllViews()
                        layoutAdView.addView(admobBinding.root)
                    }

                    3 -> {
                        val admobBinding =
                            NativeWithMediaThreeBinding.inflate(windowActivity.layoutInflater)

                        if (activity is Splash) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_splash * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_splash))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_splash.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_splash))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_splash))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_splash.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_splash.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_splash_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_splash,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is OnBoardingActivity) {


                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_onboardingscreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_onboardingscreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_onboardingscreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_onboardingscreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_onboardingscreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_onboarding.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_onboarding.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_onboarding_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_onboardingscreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is LanguagesActivity) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_languagescreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_languagescreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_languagescreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_languagescreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_languagescreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_language_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_languagescreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_otherscreens * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_otherscreens))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_otherscreens.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_otherscreens))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_otherscreens))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_other_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_otherscreens,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }





                        showNativeAdView(native_ad!!, admobBinding)
                        layoutAdView.removeAllViews()
                        layoutAdView.addView(admobBinding.root)
                    }

                    4 -> {
                        val admobBinding =
                            NativeWithMediaFourBinding.inflate(windowActivity.layoutInflater)

                        if (activity is Splash) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_splash * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_splash))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_splash.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_splash))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_splash))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_splash.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_splash.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_splash_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_splash,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is OnBoardingActivity) {


                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_onboardingscreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_onboardingscreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_onboardingscreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_onboardingscreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_onboardingscreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_onboarding.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_onboarding.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_onboarding_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_onboardingscreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is LanguagesActivity) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_languagescreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_languagescreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_languagescreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_languagescreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_languagescreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_language_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_languagescreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_otherscreens * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_otherscreens))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_otherscreens.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_otherscreens))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_otherscreens))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_other_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_otherscreens,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }




                        showNativeAdView(native_ad!!, admobBinding)
                        layoutAdView.removeAllViews()
                        layoutAdView.addView(admobBinding.root)
                    }

                    5 -> {
                        val admobBinding =
                            NativeAdWithoutMediaOneBinding.inflate(windowActivity.layoutInflater)
                        if (activity is Splash) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_splash * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_splash))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_splash.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_splash))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_splash))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_splash.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_splash.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_splash_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_splash,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is OnBoardingActivity) {


                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_onboardingscreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_onboardingscreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_onboardingscreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_onboardingscreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_onboardingscreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_onboarding.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_onboarding.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_onboarding_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_onboardingscreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is LanguagesActivity) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_languagescreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_languagescreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_languagescreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_languagescreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_languagescreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_language_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_languagescreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_otherscreens * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_otherscreens))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_otherscreens.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_otherscreens))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_otherscreens))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_other_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_otherscreens,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }



                        showNativeAdView(native_ad!!, admobBinding)
                        layoutAdView.removeAllViews()
                        layoutAdView.addView(admobBinding.root)
                    }

                    6 -> {
                        val admobBinding =
                            NativeAdWithoutMediaTwoBinding.inflate(windowActivity.layoutInflater)

                        if (activity is Splash) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_splash * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_splash))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_splash.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_splash))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_splash))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_splash.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_splash.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_splash_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_splash,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is OnBoardingActivity) {


                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_onboardingscreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_onboardingscreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_onboardingscreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_onboardingscreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_onboardingscreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_onboarding.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_onboarding.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_onboarding_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_onboardingscreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is LanguagesActivity) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_languagescreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_languagescreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_languagescreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_languagescreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_languagescreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_language_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_languagescreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_otherscreens * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_otherscreens))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_otherscreens.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_otherscreens))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_otherscreens))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_other_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_otherscreens,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }


                        showNativeAdView(native_ad!!, admobBinding)
                        layoutAdView.removeAllViews()
                        layoutAdView.addView(admobBinding.root)
                    }

                    7 -> {
                        val admobBinding =
                            NativeAdWithoutMediaThreeBinding.inflate(windowActivity.layoutInflater)
                        if (activity is Splash) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_splash * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_splash))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_splash.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_splash))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_splash))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_splash.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_splash.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_splash_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_splash,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is OnBoardingActivity) {


                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_onboardingscreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_onboardingscreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_onboardingscreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_onboardingscreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_onboardingscreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_onboarding.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_onboarding.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_onboarding_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_onboardingscreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is LanguagesActivity) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_languagescreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_languagescreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_languagescreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_languagescreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_languagescreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_language_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_languagescreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_otherscreens * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_otherscreens))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_otherscreens.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_otherscreens))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_otherscreens))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_other_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_otherscreens,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }



                        showNativeAdView(native_ad!!, admobBinding)
                        layoutAdView.removeAllViews()
                        layoutAdView.addView(admobBinding.root)
                    }

                    8 -> {
                        val admobBinding =
                            NativeWithoutMediaFourBinding.inflate(windowActivity.layoutInflater)

                        if (activity is Splash) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_splash * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_splash))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_splash.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_splash))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_splash))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_splash.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_splash.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_splash_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_splash,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is OnBoardingActivity) {


                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_onboardingscreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_onboardingscreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_onboardingscreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_onboardingscreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_onboardingscreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_onboarding.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_onboarding.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_onboarding_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_onboardingscreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else if (activity is LanguagesActivity) {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_languagescreen * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_languagescreen))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_languagescreen.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_languagescreen))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_languagescreen))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_languagescreen.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_language_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_languagescreen,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }
                        else {

                            val density = 1

// Calculate height in pixels from dp
                            val buttonHeightPx =
                                (GetFirebase.native_ad_buttonheight_for_otherscreens * density).toInt()

                            with(admobBinding.adCallToAction) {
                                layoutParams = layoutParams.apply {
                                    height = activity.getSdpHeightInPx(buttonHeightPx)
                                }
                                setTextColor(Color.parseColor(GetFirebase.native_ad_buttontextcolor_for_otherscreens))
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                        GetFirebase.native_ad_buttontextheight_for_otherscreens.toInt()
                                    ).toFloat()
                                )
                            }

                            admobBinding.adHeadline.setTextColor(Color.parseColor(GetFirebase.native_ad_headlinecolor_for_otherscreens))
                            admobBinding.adBody.setTextColor(Color.parseColor(GetFirebase.native_ad_othertextcolor_for_otherscreens))
                            admobBinding.adHeadline.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_headlinetextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )
                            admobBinding.adBody.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, activity.getSspTextSizeInPx(
                                    GetFirebase.native_ad_bodytextheight_for_otherscreens.toInt()
                                ).toFloat()
                            )

                            val sizeInDp = GetFirebase.native_ad_other_iconsize

                            val sizeInPx =
                                (sizeInDp * activity.resources.displayMetrics.density).toInt()

                            val params = admobBinding.adAppIcon.layoutParams

                            params.width = sizeInPx
                            params.height = sizeInPx

                            admobBinding.adAppIcon.layoutParams = params

                            setButtonColor(
                                GetFirebase.native_ad_buttoncolor_for_otherscreens,
                                activity,
                                admobBinding.adCallToAction
                            )
                        }



                        showNativeAdView(native_ad!!, admobBinding)
                        layoutAdView.removeAllViews()
                        layoutAdView.addView(admobBinding.root)
                    }

                }

            }
        }


        if (activity is Splash) {
            changeBackgroundForNativeAds(
                GetFirebase.native_ad_backgroundcolor_for_splash,
                layoutParent,
                activity
            )
        } else if (activity is OnBoardingActivity) {
            changeBackgroundForNativeAds(
                GetFirebase.native_ad_backgroundcolor_for_onboardingscreen,
                layoutParent,
                activity
            )

        } else if (activity is LanguagesActivity) {
            changeBackgroundForNativeAds(
                GetFirebase.native_ad_backgroundcolor_for_languagescreen,
                layoutParent,
                activity
            )

        } else {
            changeBackgroundForNativeAds(
                GetFirebase.native_ad_backgroundcolor_for_otherscreens,
                layoutParent,
                activity
            )
        }


    }

    fun changeBackgroundForNativeAds(
        bgColor: String,
        layoutParent: RelativeLayout,
        activity: AppCompatActivity
    ) {
        layoutParent.setBackgroundResource(R.drawable.bg_native_ad)
        val drawable = ContextCompat.getDrawable(activity, R.drawable.bg_native_ad)
        drawable?.let {
            it.setTint(/* tintColor = */ Color.parseColor(bgColor))
            layoutParent.background = it
        }

    }

    fun setButtonColor(
        bgColor: String,
        activity: AppCompatActivity,
        layoutParent: AppCompatButton

    ) {
        layoutParent.setBackgroundResource(R.drawable.bg_native_ad)
        val drawable = ContextCompat.getDrawable(activity, R.drawable.bg_native_button)
        drawable?.let {
            it.setTint(/* tintColor = */ Color.parseColor(bgColor))
            layoutParent.background = it
        }

    }



    fun showNativeAdView(
        ad: NativeAd,
        binding: NativeAdWithoutMediaOneBinding
    ) {
        val nativeAdView = binding.nativeAdView


        // Set other ad assets.
        nativeAdView.headlineView = binding.adHeadline
        nativeAdView.bodyView = binding.adBody
        nativeAdView.callToActionView = binding.adCallToAction
        nativeAdView.iconView = binding.adAppIcon
        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        binding.adHeadline.text = ad.headline

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (ad.body == null) {
            binding.adBody.visibility = View.GONE
        } else {
            binding.adBody.visibility = View.VISIBLE
            binding.adBody.text = ad.body
        }

        if (ad.callToAction == null) {
            binding.adCallToAction.visibility = View.GONE
        } else {
            binding.adCallToAction.visibility = View.VISIBLE
            binding.adCallToAction.text = ad.callToAction
        }

        if (ad.icon == null) {
            binding.adAppIcon.visibility = View.GONE
        } else {
            binding.adAppIcon.setImageDrawable(ad.icon?.drawable)
            binding.adAppIcon.visibility = View.VISIBLE
        }

        nativeAdView.setNativeAd(ad)


    }

    fun showNativeAdView(
        ad: NativeAd,
        binding: NativeAdWithoutMediaTwoBinding
    ) {
        val nativeAdView = binding.nativeAdView

        // Set other ad assets.
        nativeAdView.headlineView = binding.adHeadline
        nativeAdView.bodyView = binding.adBody
        nativeAdView.callToActionView = binding.adCallToAction
        nativeAdView.iconView = binding.adAppIcon
        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        binding.adHeadline.text = ad.headline

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (ad.body == null) {
            binding.adBody.visibility = View.GONE
        } else {
            binding.adBody.visibility = View.VISIBLE
            binding.adBody.text = ad.body
        }

        if (ad.callToAction == null) {
            binding.adCallToAction.visibility = View.GONE
        } else {
            binding.adCallToAction.visibility = View.VISIBLE
            binding.adCallToAction.text = ad.callToAction
        }

        if (ad.icon == null) {
            binding.adAppIcon.visibility = View.GONE
        } else {
            binding.adAppIcon.setImageDrawable(ad.icon?.drawable)
            binding.adAppIcon.visibility = View.VISIBLE
        }

        nativeAdView.setNativeAd(ad)


    }

    fun showNativeAdView(
        ad: NativeAd,
        binding: NativeAdWithoutMediaThreeBinding
    ) {
        val nativeAdView = binding.nativeAdView

        // Set other ad assets.
        nativeAdView.headlineView = binding.adHeadline
        nativeAdView.bodyView = binding.adBody
        nativeAdView.callToActionView = binding.adCallToAction
        nativeAdView.iconView = binding.adAppIcon
        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        binding.adHeadline.text = ad.headline

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (ad.body == null) {
            binding.adBody.visibility = View.GONE
        } else {
            binding.adBody.visibility = View.VISIBLE
            binding.adBody.text = ad.body
        }

        if (ad.callToAction == null) {
            binding.adCallToAction.visibility = View.GONE
        } else {
            binding.adCallToAction.visibility = View.VISIBLE
            binding.adCallToAction.text = ad.callToAction
        }

        if (ad.icon == null) {
            binding.adAppIcon.visibility = View.GONE
        } else {
            binding.adAppIcon.setImageDrawable(ad.icon?.drawable)
            binding.adAppIcon.visibility = View.VISIBLE
        }

        nativeAdView.setNativeAd(ad)


    }

    fun showNativeAdView(
        ad: NativeAd,
        binding: NativeWithoutMediaFourBinding
    ) {
        val nativeAdView = binding.nativeAdView

        // Set other ad assets.
        nativeAdView.headlineView = binding.adHeadline
        nativeAdView.bodyView = binding.adBody
        nativeAdView.callToActionView = binding.adCallToAction
        nativeAdView.iconView = binding.adAppIcon
        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        binding.adHeadline.text = ad.headline

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (ad.body == null) {
            binding.adBody.visibility = View.GONE
        } else {
            binding.adBody.visibility = View.VISIBLE
            binding.adBody.text = ad.body
        }

        if (ad.callToAction == null) {
            binding.adCallToAction.visibility = View.GONE
        } else {
            binding.adCallToAction.visibility = View.VISIBLE
            binding.adCallToAction.text = ad.callToAction
        }

        if (ad.icon == null) {
            binding.adAppIcon.visibility = View.GONE
        } else {
            binding.adAppIcon.setImageDrawable(ad.icon?.drawable)
            binding.adAppIcon.visibility = View.VISIBLE
        }

        nativeAdView.setNativeAd(ad)


    }



    fun showNativeAdView(
        ad: NativeAd,
        binding: NativeWithMediaOneBinding
    ) {
        val nativeAdView = binding.nativeAdView

        // Set the media view.
        nativeAdView.mediaView = binding.adMedia

        // Set other ad assets.
        nativeAdView.headlineView = binding.adHeadline
        nativeAdView.bodyView = binding.adBody
        nativeAdView.callToActionView = binding.adCallToAction
        nativeAdView.iconView = binding.adAppIcon
        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        binding.adHeadline.text = ad.headline
        ad.mediaContent?.let { binding.adMedia.setMediaContent(it) }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (ad.body == null) {
            binding.adBody.visibility = View.GONE
        } else {
            binding.adBody.visibility = View.VISIBLE
            binding.adBody.text = ad.body
        }

        if (ad.callToAction == null) {
            binding.adCallToAction.visibility = View.GONE
        } else {
            binding.adCallToAction.visibility = View.VISIBLE
            binding.adCallToAction.text = ad.callToAction
        }

        if (ad.icon == null) {
            binding.adAppIcon.visibility = View.GONE
        } else {
            binding.adAppIcon.setImageDrawable(ad.icon?.drawable)
            binding.adAppIcon.visibility = View.VISIBLE
        }

        nativeAdView.setNativeAd(ad)


        val mediaContent = ad.mediaContent
        val vc = mediaContent?.videoController


        if (vc != null && mediaContent.hasVideoContent()) {

            vc.videoLifecycleCallbacks =
                object : VideoController.VideoLifecycleCallbacks() {
                    override fun onVideoEnd() {


                        super.onVideoEnd()
                    }
                }
        } else {

        }


    }

    fun showNativeAdView(
        ad: NativeAd,
        binding: NativeWithMediaTwoBinding
    ) {
        val nativeAdView = binding.nativeAdView

        // Set the media view.
        nativeAdView.mediaView = binding.adMedia

        // Set other ad assets.
        nativeAdView.headlineView = binding.adHeadline
        nativeAdView.bodyView = binding.adBody
        nativeAdView.callToActionView = binding.adCallToAction
        nativeAdView.iconView = binding.adAppIcon
        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        binding.adHeadline.text = ad.headline
        ad.mediaContent?.let { binding.adMedia.setMediaContent(it) }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (ad.body == null) {
            binding.adBody.visibility = View.GONE
        } else {
            binding.adBody.visibility = View.VISIBLE
            binding.adBody.text = ad.body
        }

        if (ad.callToAction == null) {
            binding.adCallToAction.visibility = View.GONE
        } else {
            binding.adCallToAction.visibility = View.VISIBLE
            binding.adCallToAction.text = ad.callToAction
        }

        if (ad.icon == null) {
            binding.adAppIcon.visibility = View.GONE
        } else {
            binding.adAppIcon.setImageDrawable(ad.icon?.drawable)
            binding.adAppIcon.visibility = View.VISIBLE
        }

        nativeAdView.setNativeAd(ad)


        val mediaContent = ad.mediaContent
        val vc = mediaContent?.videoController


        if (vc != null && mediaContent.hasVideoContent()) {

            vc.videoLifecycleCallbacks =
                object : VideoController.VideoLifecycleCallbacks() {
                    override fun onVideoEnd() {


                        super.onVideoEnd()
                    }
                }
        } else {

        }


    }

    fun showNativeAdView(
        ad: NativeAd,
        binding: NativeWithMediaThreeBinding
    ) {
        val nativeAdView = binding.nativeAdView

        // Set the media view.
        nativeAdView.mediaView = binding.adMedia

        // Set other ad assets.
        nativeAdView.headlineView = binding.adHeadline
        nativeAdView.bodyView = binding.adBody
        nativeAdView.callToActionView = binding.adCallToAction
        nativeAdView.iconView = binding.adAppIcon
        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        binding.adHeadline.text = ad.headline
        ad.mediaContent?.let { binding.adMedia.setMediaContent(it) }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (ad.body == null) {
            binding.adBody.visibility = View.GONE
        } else {
            binding.adBody.visibility = View.VISIBLE
            binding.adBody.text = ad.body
        }

        if (ad.callToAction == null) {
            binding.adCallToAction.visibility = View.GONE
        } else {
            binding.adCallToAction.visibility = View.VISIBLE
            binding.adCallToAction.text = ad.callToAction
        }

        if (ad.icon == null) {
            binding.adAppIcon.visibility = View.GONE
        } else {
            binding.adAppIcon.setImageDrawable(ad.icon?.drawable)
            binding.adAppIcon.visibility = View.VISIBLE
        }

        nativeAdView.setNativeAd(ad)


        val mediaContent = ad.mediaContent
        val vc = mediaContent?.videoController


        if (vc != null && mediaContent.hasVideoContent()) {

            vc.videoLifecycleCallbacks =
                object : VideoController.VideoLifecycleCallbacks() {
                    override fun onVideoEnd() {


                        super.onVideoEnd()
                    }
                }
        } else {

        }


    }

    fun showNativeAdView(
        ad: NativeAd,
        binding: NativeWithMediaFourBinding
    ) {
        val nativeAdView = binding.nativeAdView

        // Set the media view.
        nativeAdView.mediaView = binding.adMedia

        // Set other ad assets.
        nativeAdView.headlineView = binding.adHeadline
        nativeAdView.bodyView = binding.adBody
        nativeAdView.callToActionView = binding.adCallToAction
        nativeAdView.iconView = binding.adAppIcon
        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        binding.adHeadline.text = ad.headline
        ad.mediaContent?.let { binding.adMedia.setMediaContent(it) }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (ad.body == null) {
            binding.adBody.visibility = View.GONE
        } else {
            binding.adBody.visibility = View.VISIBLE
            binding.adBody.text = ad.body
        }

        if (ad.callToAction == null) {
            binding.adCallToAction.visibility = View.GONE
        } else {
            binding.adCallToAction.visibility = View.VISIBLE
            binding.adCallToAction.text = ad.callToAction
        }

        if (ad.icon == null) {
            binding.adAppIcon.visibility = View.GONE
        } else {
            binding.adAppIcon.setImageDrawable(ad.icon?.drawable)
            binding.adAppIcon.visibility = View.VISIBLE
        }

        nativeAdView.setNativeAd(ad)


        val mediaContent = ad.mediaContent
        val vc = mediaContent?.videoController


        if (vc != null && mediaContent.hasVideoContent()) {

            vc.videoLifecycleCallbacks =
                object : VideoController.VideoLifecycleCallbacks() {
                    override fun onVideoEnd() {


                        super.onVideoEnd()
                    }
                }
        } else {

        }


    }


}