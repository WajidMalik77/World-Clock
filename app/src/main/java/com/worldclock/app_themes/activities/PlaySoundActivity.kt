package com.worldclock.app_themes.activities

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.bumptech.glide.Glide
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.ActivityPlaySoundBinding
import java.util.Locale

class PlaySoundActivity : BaseActivity() {

    private val binding by lazy { ActivityPlaySoundBinding.inflate(layoutInflater) }
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
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)

        val soundName = intent.getStringExtra("sound_name") ?: ""
        val previewUrl = intent.getStringExtra("preview_url") ?: ""

        val thumbnailRes = intent.getIntExtra("thumbnail_res", R.drawable.ic_sleep_placeholder)
        binding.ivFullThumbnail.setImageResource(thumbnailRes)

        binding.tvTitle.text = soundName
        binding.ivBack.setOnClickListener { finish() }

        startPlayback(previewUrl)

        binding.btnPlayPause.setOnClickListener {
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
        handler.removeCallbacks(timerRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}