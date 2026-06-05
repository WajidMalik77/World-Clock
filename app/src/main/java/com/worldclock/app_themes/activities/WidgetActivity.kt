package com.worldclock.app_themes.activities

import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.worldclock.app_themes.R
import com.worldclock.app_themes.adapter.ClockWidgetAdapter
import com.worldclock.app_themes.databinding.ActivityWidgetBinding
import com.worldclock.app_themes.utils.ClockWidget

class WidgetActivity : BaseActivity() {
    private val binding by lazy { ActivityWidgetBinding.inflate(layoutInflater) }
    private val clockThemes: List<ClockWidget> = listOf(
        ClockWidget.Analog(
            R.drawable.face_01_orange_ring,
            R.drawable.needle_hour_01,
            R.drawable.needle_min_01,
            R.drawable.needle_sec_01
        ),
        ClockWidget.Analog(
            R.drawable.face_02_orange_ring,
            R.drawable.needle_hour_02,
            R.drawable.needle_min_02,
            R.drawable.needle_sec_01
        ),
        ClockWidget.Analog(
            R.drawable.face_03_galaxy,
            R.drawable.needle_hour_01,
            R.drawable.needle_min_01,
            R.drawable.needle_sec_01
        ),
        ClockWidget.Digital(R.drawable.face_04_digital_dark, Color.WHITE),
        ClockWidget.Analog(
            R.drawable.face_05_flat_blue,
            R.drawable.needle_hour_05,
            R.drawable.needle_min_05,
            R.drawable.needle_sec_05
        ),
        ClockWidget.Analog(
            R.drawable.face_06_white_pink,
            R.drawable.needle_hour_06,
            R.drawable.needle_min_06,
            R.drawable.needle_sec_06
        ),
        ClockWidget.Analog(
            R.drawable.face_07_blue_glass,
            R.drawable.needle_hour_01,
            R.drawable.needle_min_01,
            R.drawable.needle_sec_06
        ),
        ClockWidget.Analog(
            R.drawable.face_08_purple_pink,
            R.drawable.needle_hour_08,
            R.drawable.needle_min_08,
            R.drawable.needle_sec_06
        ),
        ClockWidget.Analog(
            R.drawable.face_09_neon_tach,
            R.drawable.needle_hour_09,
            R.drawable.needle_min_09,
            R.drawable.needle_sec_09
        ),
        ClockWidget.Analog(
            R.drawable.face_10_slate_blue,
            R.drawable.needle_hour_10,
            R.drawable.needle_min_10,
            R.drawable.needle_sec_10
        ),
        ClockWidget.Analog(
            R.drawable.face_11_black_red_ring,
            R.drawable.needle_hour_01,
            R.drawable.needle_min_01,
            R.drawable.needle_sec_01
        ),
        ClockWidget.Analog(
            R.drawable.face_12_crimson_card,
            R.drawable.needle_hour_12,
            R.drawable.needle_min_12,
            R.drawable.needle_sec_12
        ),
        ClockWidget.Analog(
            R.drawable.face_13_amber_card,
            R.drawable.needle_hour_13,
            R.drawable.needle_min_13,
            R.drawable.needle_sec_12
        ),
        ClockWidget.Analog(
            R.drawable.face_14_teal_card,
            R.drawable.needle_hour_10,
            R.drawable.needle_min_10,
            R.drawable.needle_sec_14
        ),
        ClockWidget.Analog(
            R.drawable.face_15_sky_card,
            R.drawable.needle_hour_15,
            R.drawable.needle_min_15,
            R.drawable.needle_sec_15
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)

        binding.toolbar.title.text = getString(R.string.widget_clock)
        binding.toolbar.back.setOnClickListener { finish() }

        binding.rvClockThemes.apply {
            layoutManager = GridLayoutManager(this@WidgetActivity, 3)
            adapter = ClockWidgetAdapter(clockThemes)
            setHasFixedSize(true)
        }
    }
}