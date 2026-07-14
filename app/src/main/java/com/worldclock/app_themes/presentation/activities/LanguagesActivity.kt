package com.worldclock.app_themes.presentation.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldclock.app_themes.R
import com.worldclock.app_themes.presentation.adapter.LangAdapter
import com.worldclock.app_themes.databinding.ActivityLanguagesBinding
import com.worldclock.app_themes.core.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.core.utils.AdsConstants.PrefsName
import com.worldclock.app_themes.core.utils.AdsConstants.isFirstTime
import com.worldclock.app_themes.core.utils.PrefUtil
import com.worldclock.app_themes.core.utils.getLangData
import com.worldclock.app_themes.core.analytics.AppEventLogger

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import com.worldclock.app_themes.ads.helpers.models.NativeAdEvent

import com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator
import com.worldclock.app_themes.ads.helpers.safeShowInterstitialAction
import com.zeugmasolutions.localehelper.LocaleHelper
import dagger.hilt.android.EntryPointAccessors
import com.worldclock.app_themes.ads.di.AdConfigEntryPoint
import com.worldclock.app_themes.ads.preload.AdLoadMode
import com.worldclock.app_themes.ads.preload.BannerPreload
import com.worldclock.app_themes.ads.preload.InterstitialAdManager
import com.worldclock.app_themes.ads.preload.InterstitialScreen
import com.worldclock.app_themes.ads.preload.NativePreload
import com.worldclock.app_themes.ads.preload.PreloadController
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.Utils
import com.worldclock.app_themes.core.utils.AdsConstants

@AndroidEntryPoint
class LanguagesActivity : BaseActivity() {
    private val binding by lazy {
        ActivityLanguagesBinding.inflate(layoutInflater)
    }
    var pos = -1
    private var isSplash = false
    private var toastJob: Job? = null
    private var doneConsumed = false

    @Inject
    lateinit var nativeAdOrchestrator: NativeAdOrchestrator

    @Inject
    lateinit var bannerAdOrchestrator: BannerAdOrchestrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "LanguagesScreen", "activity_lifecycle")

        if (GetFirebase.enable_on_demand_interstitial == 1){
            InterstitialAdManager.loadLanguage(this, GetFirebase.adIdLanguage_interstitial)
        }

        isSplash = intent.getBooleanExtra("isSplash", false)
        val languages = getLangData()
        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val hasSavedLang = sharedPreferences.contains("lang")
        val initialPos = if (hasSavedLang && !isSplash) {
            sharedPreferences.getInt("lang", 0).coerceIn(0, languages.lastIndex)
        } else {
            0
        }
        pos = initialPos

        if (!isSplash)
            binding.back.visibility = View.VISIBLE
        else binding.back.visibility = View.INVISIBLE


        PreloadController.observeBanner(this,
            BannerPreload.adBannerTopLiveData,
            binding.bannerContainer.bannerTopContainer,binding.bannerContainer.adVeiwTop,binding.bannerContainer.adTextAdvertisementTop,
            GetFirebase.banner_ad_languagesactivity_top,"top",
            window,
            NativePreload.adNativeTopLiveData){

        }

        PreloadController.observeBanner(this,
            BannerPreload.adBannerBottomLiveData,
            binding.adsContainer.bannerBottomContainer,binding.adsContainer.adVeiwBottom,binding.adsContainer.adTextAdvertisementBottom,
            GetFirebase.banner_ad_languagesactivity_bottom,"bottom",
            window,
            NativePreload.adNativeBottomLiveData){

        }

        binding.back.setOnClickListener {
            AppEventLogger.trackButtonClick("LanguagesScreen", "back", "navigate_back", "languages_flow")

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_menuactivity_top,"top",this@LanguagesActivity,
                GetFirebase.adIdMenu_bannerTop, GetFirebase.adIdMenu_nativeTop)

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_menuactivity_bottom,"bottom",this@LanguagesActivity,
                GetFirebase.adIdMenu_bannerBottom, GetFirebase.adIdMenu_nativeBottom)


            InterstitialAdManager.showIfReady(
                this@LanguagesActivity,
                InterstitialScreen.LANGUAGE,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_LanguagesBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@LanguagesActivity, MenuActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@LanguagesActivity, MenuActivity::class.java))
                    finish()
                })


        }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        val adapter = LangAdapter(languages) { it, it1 ->
            pos = it1

        }
        binding.recycler.adapter = adapter
        adapter.setPos(initialPos)

        binding.done.setOnClickListener {
            if (doneConsumed) return@setOnClickListener
            AppEventLogger.trackButtonClick("LanguagesScreen", "done", "confirm", "languages_flow")
            val selectedPos = adapter.getSelectedPos()
            if (selectedPos == -1) {
                showCustomToast(getString(R.string.please_select_a_language))
                return@setOnClickListener
            }
            doneConsumed = true
            binding.done.isEnabled = false
            pos = selectedPos
            getSharedPreferences("MySharedPref", MODE_PRIVATE).edit { putInt("lang", selectedPos) }

            val isPremium = PrefUtil(this).getBool("is_premium", false)
                || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)

            val onCompleted = {
                if (selectedPos != initialPos) {
                    LocaleHelper.setLocale(this@LanguagesActivity, languages[selectedPos].locale)
                }
                goNext()
            }

            if (isPremium) {
                onCompleted()
            } else {
                val isFirstLaunch = !getSharedPreferences(PrefsName, Context.MODE_PRIVATE)
                    .getBoolean(isFirstTime, false)
                val triggerKey = if (isFirstLaunch) "language_first_done" else "language_second_done"

//                safeShowInterstitialAction(
//                    screenName = "LanguagesScreen",
//                    trigger = triggerKey,
//                    noCounterNeeded = false,
//                    afterAd = { onCompleted() }
//                )

                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_onboardingactivity_top,"top",this@LanguagesActivity,
                    GetFirebase.adIdOnboarding_bannerTop, GetFirebase.adIdOnboarding_nativeTop)

                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_onboardingactivity_bottom,"bottom",this@LanguagesActivity,
                    GetFirebase.adIdOnboarding_bannerBottom, GetFirebase.adIdOnboarding_nativeBottom)


                InterstitialAdManager.showIfReady(
                    this,
                    InterstitialScreen.LANGUAGE,
                    GetFirebase.adIdOther_interstitial,
                    if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                    GetFirebase.transition_LanguageForward,
                    GetFirebase.counter_interval,
                    Utils.isPremium,
                    GetFirebase.enable_interstitial_ads,
                    {
                        onCompleted()

                    },
                    {
                        onCompleted()

                    })

            }
        }


        onBackPressedDispatcher.addCallback(this@LanguagesActivity){
            if (!isSplash){
                InterstitialAdManager.showIfReady(
                    this@LanguagesActivity,
                    InterstitialScreen.LANGUAGE,
                    GetFirebase.adIdOther_interstitial,
                    if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                    GetFirebase.transition_LanguagesBack,
                    GetFirebase.counter_interval,
                    Utils.isPremium,
                    GetFirebase.enable_interstitial_ads,
                    {
                        startActivity(Intent(this@LanguagesActivity, MenuActivity::class.java))
                        finish()
                    },
                    {
                        startActivity(Intent(this@LanguagesActivity, MenuActivity::class.java))
                        finish()
                    })
            }


        }

        showDoneIcon()

    }
    private fun goNext() {
        Log.d("lang123", "goNext: $isSplash")
        if (isSplash) {

            val intent = getNextScreenIntent(this, "languages")
            startActivity(intent)
            finish()
        } else {
            startActivity(
                Intent(
                    this@LanguagesActivity,
                    MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            finish()
        }
    }

    fun getNextScreenIntent(context: Context, currentScreen: String): Intent {
        val isFirstLaunch = !context.getSharedPreferences(AdsConstants.PrefsName, Context.MODE_PRIVATE)
            .getBoolean(AdsConstants.isFirstTime, false)

        if (Utils.isPremium) {
            return Intent(context, MainActivity::class.java)
        }

        when (currentScreen) {
            "splash" -> {
                return Intent(context, OnBoardingActivity::class.java).putExtra("isSplash", true)

            }
            "languages" -> {
                return Intent(context, OnBoardingActivity::class.java).putExtra("isSplash", true)

            }
            "intro" -> {
                return Intent(context, OnBoardingActivity::class.java).putExtra("isSplash", true)

            }
            "premium" -> {
                return Intent(context, OnBoardingActivity::class.java).putExtra("isSplash", true)

            }
            else -> {
                return Intent(context, OnBoardingActivity::class.java).putExtra("isSplash", true)

            }
        }

    }


    /* private fun goNext() {
         Log.d("lang123", "goNext: $isSplash")
         if (isSplash) {
             if (!getSharedPreferences(
                     PrefsName,
                     Context.MODE_PRIVATE
                 ).getBoolean(
                     isFirstTime,
                     false
                 )
             ) {

 //                preLoadShowInterstitial(Islang_inter_ad_key, lang_inter_ad_key) {

 //                    loadInterstitial(Ispurchase_inter_ad_key, purchase_inter_ad_key) {}
                 startActivity(Intent(this, OnBoardingActivity::class.java))
                 finish()
             }

         } else finish()
     }*/

    private fun showDoneIcon() {

        Handler().postDelayed(object : Runnable{
            override fun run() {
                binding.doneProgress.visibility = View.GONE
                binding.done.visibility = View.VISIBLE
            }

        },3000)


    }

    private fun showCustomToast(message: String) {
        toastJob?.cancel()
        binding.customToastText.text = message
        binding.customToast.alpha = 0f
        binding.customToast.visibility = View.VISIBLE
        binding.customToast.animate()
            .alpha(1f)
            .setDuration(300)
            .setListener(null)
            .start()

        toastJob = lifecycleScope.launch {
            delay(2000)
            binding.customToast.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        binding.customToast.visibility = View.GONE
                    }
                })
                .start()
        }
    }

    override fun onDestroy() {
        toastJob?.cancel()
        AppEventLogger.trackScreenDestroy(this, "LanguagesScreen")
        super.onDestroy()
    }
}
