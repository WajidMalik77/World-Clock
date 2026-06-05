package com.worldclock.app_themes.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.worldclock.app_themes.activities.MainActivity
import com.worldclock.app_themes.R
import com.worldclock.app_themes.database.WorldClockDatabase
import com.worldclock.app_themes.databinding.ActivitySplashBinding
import com.worldclock.app_themes.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.utils.AdsConstants.PrefsName
import com.worldclock.app_themes.utils.AdsConstants.isFirstTime
import com.worldclock.app_themes.utils.Constants
import com.worldclock.app_themes.utils.PrefUtil
import com.worldclock.app_themes.utils.RemoteConfig.Companion.getRCValues
import com.worldclock.app_themes.utils.SharePref
import com.worldclock.app_themes.utils.getAllWorldClocksUsingZoneTabWithRelation
import com.worldclock.app_themes.utils.loadGifFromRaw
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Splash : BaseActivity() {
    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        binding.splashImage.setImageResource(R.drawable.ic_splash)

        startProgressBar()

        initAds()

    }

    private fun startProgressBar() {
        val progressBar = binding.splashProgress
        val handler = Handler(Looper.getMainLooper())
        var progress = 0

        val runnable = object : Runnable {
            override fun run() {
                if (progress <= 100) {
                    progressBar.setProgress(progress, false)
                    progress += 2
                    handler.postDelayed(this, 80)
                }
            }
        }
        handler.post(runnable)
    }

    private fun initAds() {
        getRCValues(this) {
            Handler(Looper.getMainLooper()).postDelayed({
                startMainActivity("")
            }, 3000)
        }
    }


    private var isNext = false

    private fun startMainActivity(s: String) {
        if (!isNext) {
            isNext = true
            if (!getSharedPreferences(
                    PrefsName,
                    Context.MODE_PRIVATE
                ).getBoolean(
                    isFirstTime,
                    false
                )
            ) {

                startActivity(
                    Intent(this, LanguagesActivity::class.java)
                        .putExtra("isSplash", true)
                )
            } else if (PrefUtil(this).getBool("is_premium", false)
                || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)
            ) {

                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    )
                )
            } else {
                startActivity(
                    Intent(this, ActivityPurchase::class.java)
                        .putExtra("isSplash", true)
                )

            }

            Log.d("TAG", "goNextActivity: $s")

            finish()
        }
    }
}