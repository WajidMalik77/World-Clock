package com.worldclock.app_themes.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.worldclock.app_themes.R
import com.worldclock.app_themes.adapter.HomeAdapter
import com.worldclock.app_themes.databinding.ActivityMainBinding
import com.worldclock.app_themes.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.utils.ClockCanvasView
import com.worldclock.app_themes.utils.Constants
import com.worldclock.app_themes.utils.PrefUtil
import com.worldclock.app_themes.utils.SharePref
import com.worldclock.app_themes.utils.SharePref.Companion.getBoolean
import com.worldclock.app_themes.utils.getHomeData
import java.util.Calendar

class MainActivity : BaseActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clockRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        binding.toolbar.back.setImageResource(R.drawable.menu)
        binding.toolbar.back.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
        }

        clockRunnable = object : Runnable {
            override fun run() {
                val cal = Calendar.getInstance()
                val sec = cal.get(Calendar.SECOND)
                val min = cal.get(Calendar.MINUTE)
                val hour = cal.get(Calendar.HOUR)

                binding.timeTxt.text = String.format("%02d:%02d", hour, min)

                binding.clockContainer.removeAllViews()
                binding.clockContainer.addView(
                    ClockCanvasView(
                        context = this@MainActivity,
                        startDegHr = hour * 30f + min * 0.5f,
                        startDegMin = min * 6f + sec * 0.1f,
                        startDegSec = 0f,
                        clockImage = R.drawable.ic_clock_home,
                        hourImage = R.drawable.ic_hour_hand,
                        minImage = R.drawable.ic_minute_hand
                    )
                )
                handler.postDelayed(this, 1_000L)
            }
        }
        handler.post(clockRunnable)

        binding.premium.setOnClickListener {
            startActivity(Intent(this, PremiumActivity::class.java))
        }

        val layoutManager = GridLayoutManager(this, 6)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                if (position >= 6) 3 else 2
        }
        binding.recycler.layoutManager = layoutManager

        binding.recycler.adapter = HomeAdapter(getHomeData()) { pos ->
            when (pos) {
                0 -> startActivity(Intent(this, ClockActivity::class.java))
                1 -> startActivity(Intent(this, AlarmActivity::class.java))
                2 -> startActivity(Intent(this, StopWatchActivity::class.java))
                3 -> startActivity(Intent(this, TimerActivity::class.java))
                4 -> startActivity(Intent(this, CompassActivity::class.java))
                5 -> startActivity(Intent(this, WidgetActivity::class.java))
                6 -> startActivity(Intent(this, AllRemindersActivity::class.java))
                7 -> startActivity(Intent(this, SleepSoundActivity::class.java))
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(
            Intent(this, ExitActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        )
    }

    override fun onResume() {
        super.onResume()
        handler.post(clockRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(clockRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(clockRunnable)
    }
}