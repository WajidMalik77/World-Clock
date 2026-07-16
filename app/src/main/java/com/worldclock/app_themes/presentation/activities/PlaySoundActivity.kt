package com.worldclock.app_themes.presentation.activities

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.addCallback
import com.bumptech.glide.Glide
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.ActivityPlaySoundBinding
import java.util.Locale
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import androidx.lifecycle.lifecycleScope
import com.worldclock.app_themes.ads.preload.AdLoadMode
import com.worldclock.app_themes.ads.preload.BannerPreload
import com.worldclock.app_themes.ads.preload.InterstitialAdManager
import com.worldclock.app_themes.ads.preload.InterstitialScreen
import com.worldclock.app_themes.ads.preload.NativePreload
import com.worldclock.app_themes.ads.preload.PreloadController
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.Utils
import com.worldclock.app_themes.core.analytics.AppEventLogger
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlaySoundActivity : BaseActivity() {
    @Inject
    lateinit var bannerAdOrchestrator: com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator


    private val binding by lazy { ActivityPlaySoundBinding.inflate(layoutInflater) }

    @Inject
    lateinit var nativeAdOrchestrator: NativeAdOrchestrator
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    private var elapsedSeconds = 0

    // Timer runnable
    private val timerRunnable = object : Runnable {
        override fun run() {
            elapsedSeconds++
            binding.tvTimer.text = formatTime(elapsedSeconds)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "PlaySoundScreen", "activity_lifecycle")
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)


        val soundName = intent.getStringExtra("sound_name") ?: ""
        val previewUrl = intent.getStringExtra("preview_url") ?: ""

        val thumbnailRes = intent.getIntExtra("thumbnail_res", R.drawable.ic_sleep_placeholder)
        binding.ivFullThumbnail.setImageResource(thumbnailRes)

        binding.toolbar.title.text = soundName
        binding.toolbar.back.setOnClickListener {
            AppEventLogger.trackButtonClick("PlaySoundScreen", "back", "navigate_back", "sleep_sound_flow")



            PreloadController.loadAdInBannerPosition(
                GetFirebase.banner_ad_sleepsoundactivity_top, "top", this@PlaySoundActivity,
                GetFirebase.adIdSleepSound_bannerTop, GetFirebase.adIdSleepSound_nativeTop
            )

            PreloadController.loadAdInBannerPosition(
                GetFirebase.banner_ad_sleepsoundactivity_bottom, "bottom", this@PlaySoundActivity,
                GetFirebase.adIdSleepSound_bannerBottom, GetFirebase.adIdSleepSound_nativeBottom
            )


            InterstitialAdManager.showIfReady(
                this@PlaySoundActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_PlaySoundBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@PlaySoundActivity, SleepSoundActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@PlaySoundActivity, SleepSoundActivity::class.java))
                    finish()
                })

        }

        onBackPressedDispatcher.addCallback(this){


            PreloadController.loadAdInBannerPosition(
                GetFirebase.banner_ad_sleepsoundactivity_top, "top", this@PlaySoundActivity,
                GetFirebase.adIdSleepSound_bannerTop, GetFirebase.adIdSleepSound_nativeTop
            )

            PreloadController.loadAdInBannerPosition(
                GetFirebase.banner_ad_sleepsoundactivity_bottom, "bottom", this@PlaySoundActivity,
                GetFirebase.adIdSleepSound_bannerBottom, GetFirebase.adIdSleepSound_nativeBottom
            )


            InterstitialAdManager.showIfReady(
                this@PlaySoundActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_PlaySoundBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@PlaySoundActivity, SleepSoundActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@PlaySoundActivity, SleepSoundActivity::class.java))
                    finish()
                })
        }

        startPlayback(previewUrl)

        PreloadController.observeBanner(this,
            BannerPreload.adBannerTopLiveData,
            binding.bannerContainer.bannerTopContainer,binding.bannerContainer.adVeiwTop,binding.bannerContainer.adTextAdvertisementTop,
            GetFirebase.banner_ad_playsoundactivity_top,"top",
            window,
            NativePreload.adNativeTopLiveData){

        }

        PreloadController.observeBanner(this,
            BannerPreload.adBannerBottomLiveData,
            binding.adsContainer.bannerBottomContainer,binding.adsContainer.adVeiwBottom,binding.adsContainer.adTextAdvertisementBottom,
            GetFirebase.banner_ad_playsoundactivity_bottom,"bottom",
            window,
            NativePreload.adNativeBottomLiveData){

        }

        binding.btnPlayPause.setOnClickListener {
            AppEventLogger.trackButtonClick(
                "PlaySoundScreen",
                if (isPlaying) "pause" else "play",
                "toggle_playback",
                "sleep_sound_player"
            )
            if (isPlaying) pausePlayback() else resumePlayback()
        }
    }

    private fun startPlayback(url: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener {
                start()
                this@PlaySoundActivity.isPlaying = true
                updatePlayPauseUI()
                handler.post(timerRunnable)
            }
            setOnCompletionListener {
                this@PlaySoundActivity.isPlaying = false
                elapsedSeconds = 0
                updatePlayPauseUI()
                handler.removeCallbacks(timerRunnable)
            }
        }
    }

    private fun pausePlayback() {
        mediaPlayer?.pause()
        isPlaying = false
        updatePlayPauseUI()
        handler.removeCallbacks(timerRunnable)
    }

    private fun resumePlayback() {
        mediaPlayer?.start()
        isPlaying = true
        updatePlayPauseUI()
        handler.post(timerRunnable)
    }

    private fun updatePlayPauseUI() {
        if (isPlaying) {
            binding.ivPlayPauseIcon.setImageResource(R.drawable.ic_pause)
            binding.tvPlayPauseLabel.text = "Pause"
        } else {
            binding.ivPlayPauseIcon.setImageResource(R.drawable.ic_play)
            binding.tvPlayPauseLabel.text = "Play"
        }
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }

    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "PlaySoundScreen")
        handler.removeCallbacks(timerRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
