package com.worldclock.app_themes.presentation.activities

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.bumptech.glide.Glide
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.ActivityPlaySoundBinding
import java.util.Locale
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import androidx.lifecycle.lifecycleScope
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

        lifecycleScope.launch {
            bannerAdOrchestrator.loadBannerAd(
                context = this@PlaySoundActivity,
                screen = "PlaySoundScreen",
                position = "top",
                container = binding.bannerContainer.admobBanner,
                shimmer = binding.bannerContainer.bannerAdShimmer
            )
        }



        lifecycleScope.launch {
            nativeAdOrchestrator.loadNativeAds(
                context = this@PlaySoundActivity,
                screen = "PlaySoundScreen",
                nativeConfigs = listOf(
                    NativeAdConfig(
                        position = "bottom",
                        container = binding.adsContainer.admobNative,
                        shimmer = binding.adsContainer.nativeAdShimmer
                    )
                )
            )
        }

        val soundName = intent.getStringExtra("sound_name") ?: ""
        val previewUrl = intent.getStringExtra("preview_url") ?: ""

        val thumbnailRes = intent.getIntExtra("thumbnail_res", R.drawable.ic_sleep_placeholder)
        binding.ivFullThumbnail.setImageResource(thumbnailRes)

        binding.tvTitle.text = soundName
        binding.ivBack.setOnClickListener {
            AppEventLogger.trackButtonClick("PlaySoundScreen", "back", "navigate_back", "sleep_sound_flow")
            finish()
        }

        startPlayback(previewUrl)

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
