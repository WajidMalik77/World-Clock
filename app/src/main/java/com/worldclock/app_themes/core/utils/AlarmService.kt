package com.worldclock.app_themes.core.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.worldclock.app_themes.R

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "STOP_ALARM" -> stopAlarm()
            else -> {
                val soundUri = intent?.getStringExtra("sound_uri")
                startPlaying(soundUri)
            }
        }
        return START_NOT_STICKY
    }

    private fun startPlaying(soundUri: String?) {
        mediaPlayer?.release()
        mediaPlayer = if (!soundUri.isNullOrEmpty()) {
            MediaPlayer().apply {
                setDataSource(this@AlarmService, Uri.parse(soundUri))
                isLooping = true
                prepare()
                start()
            }
        } else {
            MediaPlayer.create(this, R.raw.alram)?.apply {
                isLooping = true
                start()
            }
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val stopPendingIntent = PendingIntent.getService(
            this, 0,
            Intent(this, AlarmService::class.java).apply { action = "STOP_ALARM" },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val channelId = "alarm_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(channelId, "Alarm", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("⏰ Alarm Ringing")
            .setContentText("Tap STOP to turn off alarm")
            .setSmallIcon(R.drawable.alarm_empty)
            .addAction(R.drawable.delete_list, "STOP", stopPendingIntent)
            .setOngoing(true)
            .build()
    }
}
