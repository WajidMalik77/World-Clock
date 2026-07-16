package com.worldclock.app_themes.presentation.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import com.worldclock.app_themes.R
import com.worldclock.app_themes.ads.config.AdControlConfigManager
import com.worldclock.app_themes.ads.config.NativeAdConfigManager
import com.worldclock.app_themes.ads.di.AdConfigEntryPoint
import com.worldclock.app_themes.ads.di.AppOpenEntryPoint
import com.worldclock.app_themes.ads.helpers.AdConfigInitializer
import com.worldclock.app_themes.ads.helpers.loadBottomNative
import com.worldclock.app_themes.ads.helpers.safeShowInterstitialAction
import com.worldclock.app_themes.ads.helpers.models.NativeAdEvent
import com.worldclock.app_themes.ads.managers.UmpConsentManager
import com.worldclock.app_themes.ads.app.AdsManager
import com.worldclock.app_themes.databinding.ActivitySplashBinding
import com.worldclock.app_themes.core.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.core.utils.AdsConstants.PrefsName
import com.worldclock.app_themes.core.utils.AdsConstants.isFirstTime
import com.worldclock.app_themes.core.utils.PrefUtil
import com.worldclock.app_themes.core.analytics.AppEventLogger
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.worldclock.app_themes.ads.preload.AdLoadMode
import com.worldclock.app_themes.ads.preload.AppOpenAdManager
import com.worldclock.app_themes.ads.preload.AppOpenScreen
import com.worldclock.app_themes.ads.preload.BannerPreload
import com.worldclock.app_themes.ads.preload.InterstitialAdManager
import com.worldclock.app_themes.ads.preload.InterstitialScreen
import com.worldclock.app_themes.ads.preload.NativePreload
import com.worldclock.app_themes.ads.preload.PreloadController
import com.worldclock.app_themes.ads.utils.FetchRemoteConfig
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.Utils
import com.worldclock.app_themes.ads.utils.Utils.isPremium
import com.worldclock.app_themes.core.utils.AdsConstants
import com.worldclock.app_themes.core.utils.requestNotificationPermissionIfNeeded
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class Splash : BaseActivity() {

    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }

    @Inject lateinit var umpConsentManager: UmpConsentManager
    @Inject lateinit var adsManager: AdsManager
    @Inject lateinit var adConfigInitializer: AdConfigInitializer

    private var isNext = false
    private var splashNativeLoaded = false
    private var splashNativeEnabled = false
    private var splashNativeExpected = false
    private var splashNativeExpectedPositions: Set<String> = emptySet()

    private var getStartedConsumed = false
    private var splashAppOpenWarmupStarted = false

    private lateinit var appUpdateManager: AppUpdateManager
    private var forceUpdateDialog: Dialog? = null
    private var forceUpdateEnabled = false

    private val appUpdateLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                startMainActivity()
            } else if (forceUpdateEnabled || DEBUG_FORCE_UPDATE) {
                showForceUpdateDialog()
            } else {
                startMainActivity()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "Splash", "activity_lifecycle")
        AppEventLogger.trackFunnelStep("app_startup", "splash_created", screenName = "Splash", source = "lifecycle")
        appUpdateManager = AppUpdateManagerFactory.create(this)

        binding.getStarted.visibility = View.GONE
        binding.action.visibility = View.GONE
        binding.getStartedActionSpacer.visibility = View.GONE
        binding.imgLoading.visibility = View.GONE

        requestNotificationPermissionIfNeeded()


        binding.getStarted.setOnClickListener {
            if (getStartedConsumed) return@setOnClickListener
            getStartedConsumed = true
            AppEventLogger.trackButtonClick("Splash", "get_started", "continue", "splash_flow")
            AppEventLogger.trackFunnelStep("app_startup", "get_started_clicked", screenName = "Splash", source = "button")
            showLoadingAfterGetStartedClick()
            continueAfterAds()
        }

        binding.adsContainerTop.adTextAdvertisementTop.visibility = View.INVISIBLE
        binding.adsContainerBottom.adTextAdvertisementBottom.visibility = View.INVISIBLE
        startProgressBar()
        startSplashFlow()
    }

    private fun startProgressBar() {
        val progressBar = binding.splashProgress
        val handler = Handler(Looper.getMainLooper())
        var progress = 0
        val runnable = object : Runnable {
            override fun run() {
                if (progress <= 100) {
                    progressBar.progress = progress
                    progress += 2
                    handler.postDelayed(this, 80)
                }
            }
        }
        handler.post(runnable)
    }

    private fun startSplashFlow() {
        lifecycleScope.launch {

            // Phase 1: Run App Update + UMP consent + config preloads in parallel
            val updateInfoAsync = async { checkAppUpdateSuspending() }
            val umpConsentAsync = async { umpConsentManager.gatherConsent(this@Splash) }
            val configAsync = async {
                adConfigInitializer.preloadConfigs(false)
                withTimeoutOrNull(15_000) {
                    suspendCancellableCoroutine<Unit> { cont ->
                        adConfigInitializer.setListener(
                            onReady = { if (cont.isActive) cont.resume(Unit) },
                            onFailed = { if (cont.isActive) cont.resume(Unit) }
                        )
                    }
                }
            }

            val updateInfo = updateInfoAsync.await()
            val canRequestAds = umpConsentAsync.await()
            configAsync.await()

            // Phase 1.5: Mandatory Update Check
//            val adControlConfigManager = EntryPointAccessors.fromActivity(
//                this@Splash,
//                AdConfigEntryPoint::class.java
//            ).adControlConfigManager()
            forceUpdateEnabled = false

            if (updateInfo != null && shouldShowImmediateUpdate(updateInfo)) {
                startImmediateUpdate(updateInfo)
                return@launch // Stop ad flow, wait for update UI
            }

            if (DEBUG_FORCE_UPDATE || forceUpdateEnabled) {
                showForceUpdateDialog()
                return@launch
            }

            // Phase 2: Initialize MobileAds if consent granted
            if (canRequestAds) {
                FetchRemoteConfig.fetchAndApply {
                    initializedAds()
                }

            }



        }
    }

    fun initializedAds() {
        MobileAds.initialize(this)

        if (GetFirebase.transition_splash_ad_type == 1){
            AppOpenAdManager().loadSplash(this, GetFirebase.adIdSplash_appopen)
        }
        else if (GetFirebase.transition_splash_ad_type == 2){
            InterstitialAdManager.loadSplash(this, GetFirebase.adIdSplash_interstitial)
        }


        if (GetFirebase.open_ad_from_background){
            AppOpenAdManager().loadBackground(this, GetFirebase.adIdBackground_appopen)
        }

        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_splashactivity_top,"top",this,
            GetFirebase.adIdSplash_bannerTop, GetFirebase.adIdSplash_nativeTop)

        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_splashactivity_bottom,"bottom",this,
            GetFirebase.adIdSplash_bannerBottom, GetFirebase.adIdSplash_nativeBottom)

        Log.d("bannerAdSplashTop", GetFirebase.banner_ad_splashactivity_top.toString())

        PreloadController.observeBanner(this,
            BannerPreload.adBannerTopLiveData,
            binding.adsContainerTop.bannerTopContainer,binding.adsContainerTop.adVeiwTop,binding.adsContainerTop.adTextAdvertisementTop,
            GetFirebase.banner_ad_splashactivity_top,"top",
            window,
            NativePreload.adNativeTopLiveData){

        }

        PreloadController.observeBanner(this,
            BannerPreload.adBannerBottomLiveData,
            binding.adsContainerBottom.bannerBottomContainer,binding.adsContainerBottom.adVeiwBottom,binding.adsContainerBottom.adTextAdvertisementBottom,
            GetFirebase.banner_ad_splashactivity_bottom,"bottom",
            window,
            NativePreload.adNativeBottomLiveData){

        }

        Handler().postDelayed(object : Runnable{
            override fun run() {
                showGetStartedCta()

            }

        },10000)



    }

    // ---------------------------------------------------------------------------
    // Ad helpers
    // ---------------------------------------------------------------------------

    private suspend fun preloadSplashAppOpenAdSuspending() = suspendCancellableCoroutine<Unit> { cont ->
        val appOpenManager = EntryPointAccessors.fromActivity(
            this@Splash,
            AppOpenEntryPoint::class.java
        ).appOpenAdLifecycleManager()
        appOpenManager.preloadSplashAd {
            if (cont.isActive) cont.resume(Unit)
        }
    }

    // ---------------------------------------------------------------------------
    // UI transitions
    // ---------------------------------------------------------------------------

    private fun showGetStartedCta() {
        binding.splashProgress.visibility = View.GONE
        binding.imgLoading.visibility     = View.GONE
        binding.action.visibility         = View.VISIBLE
        binding.getStartedActionSpacer.visibility = View.VISIBLE
        binding.getStarted.visibility     = View.VISIBLE
    }

    private fun startSplashAppOpenWarmupForCta() {
        if (splashAppOpenWarmupStarted) return
        splashAppOpenWarmupStarted = true

        lifecycleScope.launch {
            if (
                splashNativeEnabled &&
                !getStartedConsumed &&
                lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            ) {
                preloadSplashAppOpenAdSuspending()
            }
        }
    }

    private fun showLoadingAfterGetStartedClick() {
        binding.getStarted.isEnabled      = false
        binding.getStarted.visibility     = View.GONE
        binding.action.visibility         = View.GONE
        binding.getStartedActionSpacer.visibility = View.GONE
        binding.imgLoading.visibility     = View.VISIBLE
    }

    private fun continueAfterAds() {



        if (GetFirebase.transition_splash_ad_type == 1){
            preloadAds(this, "splash")
            AppOpenAdManager().showIfAvailable(this@Splash, AppOpenScreen.SPLASH,
                true, isPremium = isPremium, true, {
                    startMainActivity(showInterstitial = false)
                }, {
                    InterstitialAdManager.showWithoutCounter(this@Splash,
                        InterstitialScreen.SPLASH, GetFirebase.adIdSplash_interstitial,
                        AdLoadMode.ON_DEMAND,1,isPremium, true, {
                            startMainActivity(showInterstitial = false)
                        },{
                            startMainActivity(showInterstitial = false)
                        })
                })
        }
        else if (GetFirebase.transition_splash_ad_type == 2){
            preloadAds(this, "splash")
            InterstitialAdManager.showWithoutCounter(this@Splash,
                InterstitialScreen.SPLASH, GetFirebase.adIdSplash_interstitial,
                AdLoadMode.PRELOADED,1,isPremium, true, {
                    startMainActivity(showInterstitial = false)
                },{
                    startMainActivity(showInterstitial = false)
                })
        }
        else{
            preloadAds(this, "splash")
            startMainActivity(showInterstitial = false)
        }

    }

    private fun showSplashAppOpenThen(afterAd: (appOpenAdShown: Boolean) -> Unit) {
        val appOpenManager = EntryPointAccessors.fromActivity(
            this,
            AppOpenEntryPoint::class.java
        ).appOpenAdLifecycleManager()

        var appOpenAdShown = false

        appOpenManager.showSplashAppOpenIfAvailable(
            onAdShown = {
                appOpenAdShown = true
                Timber.tag(TAG_SPLASH_ADS).d("splash app-open shown")
            }
        ) {
            Timber.tag(TAG_SPLASH_ADS).d("splash app-open finished shown=$appOpenAdShown")
            afterAd(appOpenAdShown)
        }
    }

    // ---------------------------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------------------------

    private fun startMainActivity(showInterstitial: Boolean = false) {
        if (isNext) return
        isNext = true

        val isPremium = PrefUtil(this).getBool("is_premium", false)
            || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)

        val proceed = {
            startActivity(getNextScreenIntent(this, "splash"))
            finish()
        }

        if (isPremium) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        if (showInterstitial) {
            safeShowInterstitialAction(
                screenName = "SplashScreen",
                trigger = "splash",
                noCounterNeeded = true,
                afterAd = proceed
            )
        } else {
            proceed()
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
                if (isFirstLaunch){
                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_top,"top",this@Splash,
                        GetFirebase.adIdLanguagesActivity_bannerTop,
                        GetFirebase.adIdLanguagesActivity_nativeTop)

                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_bottom,"bottom",this@Splash,
                        GetFirebase.adIdLanguagesActivity_bannerBottom,
                        GetFirebase.adIdLanguagesActivity_nativeBottom)

                    return Intent(context, LanguagesActivity::class.java).putExtra("isSplash", true)
                }
                else{
                    if (GetFirebase.show_language_for_retained_user){

                        return Intent(context, LanguagesActivity::class.java).putExtra("isSplash", true)
                    }
                    else{
                        if (Utils.isPremium){
                            return Intent(context, MainActivity::class.java).putExtra("isSplash", true)
                        }
                        else{
                            return Intent(context, MainActivity::class.java).putExtra("isSplash", true)

                        }

                    }
                }
            }
            "languages" -> {
                if (isFirstLaunch){
                    return Intent(context, LanguagesActivity::class.java).putExtra("isSplash", true)
                }
                else{
                    if (GetFirebase.show_language_for_retained_user){
                        return Intent(context, LanguagesActivity::class.java).putExtra("isSplash", true)
                    }
                    else{
                        if (Utils.isPremium){
                            return Intent(context, MainActivity::class.java).putExtra("isSplash", true)
                        }
                        else{
                            return Intent(context, MainActivity::class.java).putExtra("isSplash", true)

                        }

                    }
                }
            }
            "intro" -> {
                if (isFirstLaunch){
                    return Intent(context, LanguagesActivity::class.java).putExtra("isSplash", true)
                }
                else{
                    if (GetFirebase.show_language_for_retained_user){
                        return Intent(context, LanguagesActivity::class.java).putExtra("isSplash", true)
                    }
                    else{
                        if (Utils.isPremium){
                            return Intent(context, MainActivity::class.java).putExtra("isSplash", true)
                        }
                        else{
                            return Intent(context, MainActivity::class.java).putExtra("isSplash", true)

                        }

                    }
                }
            }
            "premium" -> {
                if (isFirstLaunch){
                    return Intent(context, LanguagesActivity::class.java).putExtra("isSplash", true)
                }
                else{
                    if (GetFirebase.show_language_for_retained_user){
                        return Intent(context, LanguagesActivity::class.java).putExtra("isSplash", true)
                    }
                    else{
                        if (Utils.isPremium){
                            return Intent(context, MainActivity::class.java).putExtra("isSplash", true)
                        }
                        else{
                            return Intent(context, MainActivity::class.java).putExtra("isSplash", true)

                        }

                    }
                }
            }
            else -> {
                if (isFirstLaunch){
                    return Intent(context, LanguagesActivity::class.java).putExtra("isSplash", true)
                }
                else{
                    if (GetFirebase.show_language_for_retained_user){
                        return Intent(context, LanguagesActivity::class.java).putExtra("isSplash", true)
                    }
                    else{
                        if (Utils.isPremium){
                            return Intent(context, MainActivity::class.java).putExtra("isSplash", true)
                        }
                        else{
                            return Intent(context, MainActivity::class.java).putExtra("isSplash", true)

                        }

                    }
                }
            }
        }

    }

    fun preloadAds(context: Context, currentScreen: String) {
        val isFirstLaunch = !context.getSharedPreferences(AdsConstants.PrefsName, Context.MODE_PRIVATE)
            .getBoolean(AdsConstants.isFirstTime, false)


        when (currentScreen) {
            "splash" -> {
                if (isFirstLaunch){
                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_top,"top",this@Splash,
                        GetFirebase.adIdLanguagesActivity_bannerTop,
                        GetFirebase.adIdLanguagesActivity_nativeTop)

                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_bottom,"bottom",this@Splash,
                        GetFirebase.adIdLanguagesActivity_bannerBottom,
                        GetFirebase.adIdLanguagesActivity_nativeBottom)

                }
                else{
                    if (GetFirebase.show_language_for_retained_user){
                        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_top,"top",this@Splash,
                            GetFirebase.adIdLanguagesActivity_bannerTop,
                            GetFirebase.adIdLanguagesActivity_nativeTop)

                        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_bottom,"bottom",this@Splash,
                            GetFirebase.adIdLanguagesActivity_bannerBottom,
                            GetFirebase.adIdLanguagesActivity_nativeBottom)

                    }
                    else{
                        if (Utils.isPremium){
                        }
                        else{
                            if (GetFirebase.show_premium_for_retained_user){
                            }
                            else{
                                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@Splash,
                                    GetFirebase.adIdMainActivity_bannerTop,
                                    GetFirebase.adIdMainActivity_nativeTop)

                                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@Splash,
                                    GetFirebase.adIdMainActivity_bannerBottom,
                                    GetFirebase.adIdMainActivity_nativeBottom)

                            }
                        }

                    }
                }
            }
            "languages" -> {
                if (isFirstLaunch){
                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_top,"top",this@Splash,
                        GetFirebase.adIdLanguagesActivity_bannerTop,
                        GetFirebase.adIdLanguagesActivity_nativeTop)

                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_bottom,"bottom",this@Splash,
                        GetFirebase.adIdLanguagesActivity_bannerBottom,
                        GetFirebase.adIdLanguagesActivity_nativeBottom)

                }
                else{
                    if (GetFirebase.show_language_for_retained_user){
                        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_top,"top",this@Splash,
                            GetFirebase.adIdLanguagesActivity_bannerTop,
                            GetFirebase.adIdLanguagesActivity_nativeTop)

                        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_bottom,"bottom",this@Splash,
                            GetFirebase.adIdLanguagesActivity_bannerBottom,
                            GetFirebase.adIdLanguagesActivity_nativeBottom)

                    }
                    else{
                        if (Utils.isPremium){
                        }
                        else{
                            if (GetFirebase.show_premium_for_retained_user){
                            }
                            else{
                                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@Splash,
                                    GetFirebase.adIdMainActivity_bannerTop,
                                    GetFirebase.adIdMainActivity_nativeTop)

                                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@Splash,
                                    GetFirebase.adIdMainActivity_bannerBottom,
                                    GetFirebase.adIdMainActivity_nativeBottom)

                            }
                        }

                    }
                }
            }
            "intro" -> {
                if (isFirstLaunch){
                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_top,"top",this@Splash,
                        GetFirebase.adIdLanguagesActivity_bannerTop,
                        GetFirebase.adIdLanguagesActivity_nativeTop)

                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_bottom,"bottom",this@Splash,
                        GetFirebase.adIdLanguagesActivity_bannerBottom,
                        GetFirebase.adIdLanguagesActivity_nativeBottom)

                }
                else{
                    if (GetFirebase.show_language_for_retained_user){
                        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_top,"top",this@Splash,
                            GetFirebase.adIdLanguagesActivity_bannerTop,
                            GetFirebase.adIdLanguagesActivity_nativeTop)

                        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_bottom,"bottom",this@Splash,
                            GetFirebase.adIdLanguagesActivity_bannerBottom,
                            GetFirebase.adIdLanguagesActivity_nativeBottom)

                    }
                    else{
                        if (Utils.isPremium){
                        }
                        else{
                            if (GetFirebase.show_premium_for_retained_user){
                            }
                            else{
                                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@Splash,
                                    GetFirebase.adIdMainActivity_bannerTop,
                                    GetFirebase.adIdMainActivity_nativeTop)

                                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@Splash,
                                    GetFirebase.adIdMainActivity_bannerBottom,
                                    GetFirebase.adIdMainActivity_nativeBottom)

                            }
                        }

                    }
                }
            }
            "premium" -> {
                if (isFirstLaunch){
                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_top,"top",this@Splash,
                        GetFirebase.adIdLanguagesActivity_bannerTop,
                        GetFirebase.adIdLanguagesActivity_nativeTop)

                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_bottom,"bottom",this@Splash,
                        GetFirebase.adIdLanguagesActivity_bannerBottom,
                        GetFirebase.adIdLanguagesActivity_nativeBottom)

                }
                else{
                    if (GetFirebase.show_language_for_retained_user){
                        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_top,"top",this@Splash,
                            GetFirebase.adIdLanguagesActivity_bannerTop,
                            GetFirebase.adIdLanguagesActivity_nativeTop)

                        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_bottom,"bottom",this@Splash,
                            GetFirebase.adIdLanguagesActivity_bannerBottom,
                            GetFirebase.adIdLanguagesActivity_nativeBottom)

                    }
                    else{
                        if (Utils.isPremium){
                        }
                        else{
                            if (GetFirebase.show_premium_for_retained_user){
                            }
                            else{
                                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@Splash,
                                    GetFirebase.adIdMainActivity_bannerTop,
                                    GetFirebase.adIdMainActivity_nativeTop)

                                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@Splash,
                                    GetFirebase.adIdMainActivity_bannerBottom,
                                    GetFirebase.adIdMainActivity_nativeBottom)

                            }
                        }

                    }
                }
            }
            else -> {
                if (isFirstLaunch){
                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_top,"top",this@Splash,
                        GetFirebase.adIdLanguagesActivity_bannerTop,
                        GetFirebase.adIdLanguagesActivity_nativeTop)

                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_bottom,"bottom",this@Splash,
                        GetFirebase.adIdLanguagesActivity_bannerBottom,
                        GetFirebase.adIdLanguagesActivity_nativeBottom)

                }
                else{
                    if (GetFirebase.show_language_for_retained_user){
                        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_top,"top",this@Splash,
                            GetFirebase.adIdLanguagesActivity_bannerTop,
                            GetFirebase.adIdLanguagesActivity_nativeTop)

                        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_languagesactivity_bottom,"bottom",this@Splash,
                            GetFirebase.adIdLanguagesActivity_bannerBottom,
                            GetFirebase.adIdLanguagesActivity_nativeBottom)

                    }
                    else{
                        if (Utils.isPremium){
                        }
                        else{
                            if (GetFirebase.show_premium_for_retained_user){
                            }
                            else{
                                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@Splash,
                                    GetFirebase.adIdMainActivity_bannerTop,
                                    GetFirebase.adIdMainActivity_nativeTop)

                                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@Splash,
                                    GetFirebase.adIdMainActivity_bannerBottom,
                                    GetFirebase.adIdMainActivity_nativeBottom)

                            }
                        }

                    }
                }
            }
        }

    }




    private suspend fun checkAppUpdateSuspending(): AppUpdateInfo? = suspendCancellableCoroutine { cont ->
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                if (cont.isActive) cont.resume(info)
            }
            .addOnFailureListener {
                if (cont.isActive) cont.resume(null)
            }
    }

    private fun shouldShowImmediateUpdate(info: AppUpdateInfo): Boolean {
        return info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
    }

    private fun startImmediateUpdate(info: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                info,
                appUpdateLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
        } catch (e: Exception) {
            startMainActivity()
        }
    }

    override fun onDestroy() {
        forceUpdateDialog?.dismiss()
        AppEventLogger.trackScreenDestroy(this, "Splash")
        super.onDestroy()
    }

    private fun showForceUpdateDialog() {
        if (forceUpdateDialog?.isShowing == true) return
        val isDark = isDarkThemeEnabled()
        val dialog = Dialog(this, R.style.TransparentDialog)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        val view = layoutInflater.inflate(R.layout.dialog_force_update, null)

        val root = view.findViewById<android.widget.LinearLayout>(R.id.dialog_root)
        val title = view.findViewById<android.widget.TextView>(R.id.update_title)
        val message = view.findViewById<android.widget.TextView>(R.id.update_message)
        val button = view.findViewById<android.widget.TextView>(R.id.update_button)

        if (isDark) {
            val darkBg = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                setColor(Color.parseColor("#2D3137"))
                cornerRadius = 14f * resources.displayMetrics.density
            }
            root.background = darkBg
            title.setTextColor(Color.WHITE)
            message.setTextColor(Color.parseColor("#B5B3BD"))
        }

        button.setOnClickListener { openAppInPlayStore() }

        dialog.setContentView(view)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        forceUpdateDialog = dialog
        dialog.show()
    }

    private fun openAppInPlayStore() {
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            setPackage("com.android.vending")
        }
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )
        try {
            startActivity(marketIntent)
        } catch (_: ActivityNotFoundException) {
            runCatching { startActivity(webIntent) }
        }
    }

    private fun isDarkThemeEnabled(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    companion object {
        private const val TAG_SPLASH_ADS = "SplashAdSequence"
        private const val SPLASH_NATIVE_IMPRESSION_GRACE_MS = 1200L
        private const val SPLASH_AD_SEQUENCE_TIMEOUT_MS = 24_000L
        private const val DEBUG_FORCE_UPDATE = false
    }
}
