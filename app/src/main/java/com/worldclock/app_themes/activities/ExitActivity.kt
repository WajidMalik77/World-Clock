package com.worldclock.app_themes.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.GridLayoutManager
import com.worldclock.app_themes.R
import com.worldclock.app_themes.adapter.HomeAdapter
import com.worldclock.app_themes.databinding.ActivityExitBinding
import com.worldclock.app_themes.utils.ClockCanvasView
import com.worldclock.app_themes.utils.getHomeData
import java.util.Calendar

class ExitActivity : BaseActivity() {

    private val binding by lazy { ActivityExitBinding.inflate(layoutInflater) }
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clockRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)

        binding.toolbar.back.setOnClickListener { navigateToMain() }

        clockRunnable = object : Runnable {
            override fun run() {
                val cal = Calendar.getInstance()
                val sec = cal.get(Calendar.SECOND)
                val min = cal.get(Calendar.MINUTE)
                val hour = cal.get(Calendar.HOUR)


                binding.clockContainer.removeAllViews()
                binding.clockContainer.addView(
                    ClockCanvasView(
                        context = this@ExitActivity,
                        startDegHr = hour * 30f + min * 0.5f,
                        startDegMin = min * 6f + sec * 0.1f,
                        startDegSec = 0f,
                        clockImage = R.drawable.ic_exit_clock,
                        hourImage = R.drawable.ic_hour_hand,
                        minImage = R.drawable.ic_minute_hand
                    )
                )
                handler.postDelayed(this, 1_000L)
            }
        }
        handler.post(clockRunnable)

        val exitItems = ArrayList(getHomeData().take(3))

        binding.recycler.layoutManager = GridLayoutManager(this, 3)
        binding.recycler.adapter = HomeAdapter(exitItems) { pos ->
            when (pos) {
                0 -> startActivity(Intent(this, ClockActivity::class.java))
                1 -> startActivity(Intent(this, AlarmActivity::class.java))
                2 -> startActivity(Intent(this, StopWatchActivity::class.java))
            }
        }

        binding.tapToExit.setOnClickListener { finishAffinity() }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        navigateToMain()
    }

    private fun navigateToMain() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
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