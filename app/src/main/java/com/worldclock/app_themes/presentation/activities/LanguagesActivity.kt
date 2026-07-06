package com.worldclock.app_themes.presentation.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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

        lifecycleScope.launch {
            bannerAdOrchestrator.loadBannerAd(
                context = this@LanguagesActivity,
                screen = "LanguagesScreen",
                position = "top",
                container = binding.bannerContainer.admobBanner,
                shimmer = binding.bannerContainer.bannerAdShimmer
            )
        }

        lifecycleScope.launch {
            withTimeoutOrNull(5_000L) {
                nativeAdOrchestrator.loadNativeAds(
                    context = this@LanguagesActivity,
                    screen = "LanguagesScreen",
                    nativeConfigs = listOf(
                        NativeAdConfig(
                            position = "bottom",
                            container = binding.adsContainer.admobNative,
                            shimmer = binding.adsContainer.nativeAdShimmer
                        )
                    ),
                    onEvent = { event ->
                        when (event) {
                            is NativeAdEvent.Loaded,
                            is NativeAdEvent.Failed,
                            is NativeAdEvent.Off,
                            is NativeAdEvent.AllOffFromConfig -> showDoneIcon()
                            else -> Unit
                        }
                    }
                )
            } ?: showDoneIcon()
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
        binding.back.setOnClickListener {
            AppEventLogger.trackButtonClick("LanguagesScreen", "back", "navigate_back", "languages_flow")
            finish()
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
            Log.d("TAG", "onCreate: ${languages[selectedPos].locale}")

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

                safeShowInterstitialAction(
                    screenName = "LanguagesScreen",
                    trigger = triggerKey,
                    noCounterNeeded = false,
                    afterAd = { onCompleted() }
                )
            }
        }
    }
    private fun goNext() {
        Log.d("lang123", "goNext: $isSplash")
        if (isSplash) {
            val cfg = EntryPointAccessors.fromActivity(
                this,
                AdConfigEntryPoint::class.java
            ).adControlConfigManager()
            val intent = cfg.getNextScreenIntent(this, "languages")
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
        binding.doneProgress.visibility = View.GONE
        binding.done.visibility = View.VISIBLE
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
