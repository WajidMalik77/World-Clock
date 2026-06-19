package com.worldclock.app_themes.presentation.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.ActivityTimerBinding
import java.util.Locale
import androidx.core.view.isVisible
import com.worldclock.app_themes.core.utils.GradientTextHelper
import com.worldclock.app_themes.core.analytics.AppEventLogger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerActivity : BaseActivity() {
    @Inject
    lateinit var bannerAdOrchestrator: com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator


    lateinit var binding: ActivityTimerBinding

    @Inject
    lateinit var nativeAdOrchestrator: NativeAdOrchestrator
    lateinit var mCountDownTimer: CountDownTimer
    private var mTimerRunning = false
    private var mAddTimer = true
    private var START_TIME_IN_MILLIS: Long = 0
    private var mTimeLeftInMillis: Long = START_TIME_IN_MILLIS
    var addTimerDialog: Dialog? = null
    var npMin: NumberPicker? = null
    var npSec: NumberPicker? = null
    private var tvDialogSet: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "TimerScreen", "activity_lifecycle")

        lifecycleScope.launch {
            bannerAdOrchestrator.loadBannerAd(
                context = this@TimerActivity,
                screen = "TimerScreen",
                position = "top",
                container = binding.bannerContainer.admobBanner,
                shimmer = binding.bannerContainer.bannerAdShimmer
            )
        }



        lifecycleScope.launch {
            nativeAdOrchestrator.loadNativeAds(
                context = this@TimerActivity,
                screen = "TimerScreen",
                nativeConfigs = listOf(
                    NativeAdConfig(
                        position = "bottom",
                        container = binding.adsContainer.admobNative,
                        shimmer = binding.adsContainer.nativeAdShimmer
                    )
                )
            )
        }

        GradientTextHelper.apply(
            binding.stopWatchTime,
            Color.parseColor("#7441D0"),
            Color.parseColor("#EF4A9A"),
            direction = GradientTextHelper.Direction.LEFT_RIGHT
        )

        binding.toolbar.title.text = getString(R.string.timer)
        binding.toolbar.back.setOnClickListener {
            finish()
        }
        binding.progressBar.isEnabled = false
//        addTimer()

        binding.startBtn.setOnClickListener(View.OnClickListener {
            if (mTimerRunning) {
                pauseTimer()
            } else {
                if (START_TIME_IN_MILLIS != 0L) {
                    startTimer()
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.please_add_timer_to_start), Toast.LENGTH_SHORT
                    ).show()
                }

            }
        })

        binding.addTimerBtn.setOnClickListener(View.OnClickListener {

            if (mAddTimer) {
                addTimer()
            }
//            else {
//                resetTimer()
//            }

        })

        binding.resetBtn.setOnClickListener {
            resetTimer()
        }

        updateCountDownText()

    }

    private fun startTimer() {


        binding.progressBar.max = (START_TIME_IN_MILLIS / 1000).toFloat()
//        Log.e("TAG", "startTimer: sldjkfghdsjf  ${binding.progressBar.max}", )
        mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, 10) {
            override fun onTick(millisUntilFinished: Long) {
                mTimeLeftInMillis = millisUntilFinished
                updateCountDownText()
                val progress =
                    (START_TIME_IN_MILLIS / 1000).toInt() - (millisUntilFinished / 1000).toInt()
//                Log.e("TAG", "onTick: sldkghdlghd $progress", )

                binding.progressBar.progress = progress.toFloat()
            }

            override fun onFinish() {
                mTimerRunning = false
                mAddTimer = true
                binding.startBtn.setImageResource(R.drawable.play_btn)
                binding.addTimerBtn.visibility = View.VISIBLE
                if (this@TimerActivity::mCountDownTimer.isInitialized) {
                    mCountDownTimer.cancel()
                }
                START_TIME_IN_MILLIS = 0
                mTimeLeftInMillis = START_TIME_IN_MILLIS
            }
        }.start()
        mTimerRunning = true
        mAddTimer = false

        if (binding.addTimerBtn.isVisible) {
            binding.addTimerBtn.visibility = View.INVISIBLE
        }
        binding.startBtn.setImageResource(R.drawable.pause_btn)
    }

    private fun pauseTimer() {


        mTimerRunning = false
        mAddTimer = false
        if (this::mCountDownTimer.isInitialized) {
            mCountDownTimer.cancel()
        }
        binding.startBtn.setImageResource(R.drawable.play_btn)
    }

    fun addTimer() {


        openDialog()
    }

    private fun resetTimer() {


        mTimerRunning = false
        mAddTimer = true
        if (this::mCountDownTimer.isInitialized) {
            mCountDownTimer.cancel()
        }
        START_TIME_IN_MILLIS = 0
        mTimeLeftInMillis = START_TIME_IN_MILLIS
        updateCountDownText()
        binding.startBtn.setImageResource(R.drawable.play_btn)
        binding.addTimerBtn.visibility = View.VISIBLE
        binding.progressBar.progress = 0F
    }

    private fun updateCountDownText() {
        val minutes = (mTimeLeftInMillis / 1000) / 60
        val seconds = (mTimeLeftInMillis / 1000) % 60

        val timeLeftFormatted: String =
            java.lang.String.format(Locale.getDefault(), "00:%02d:%02d", minutes, seconds)
        binding.stopWatchTime.text = timeLeftFormatted
//        binding.timerTvSmall.text = timeLeftFormatted
    }

    private fun openDialog() {
        val dialog = Dialog(this@TimerActivity)
        this.addTimerDialog = dialog
        dialog.setContentView(R.layout.dialog_timer)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))


        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val dialogSet = dialog.findViewById<TextView>(R.id.layout_add_timer_tv_set) ?: return
        val minPicker = dialog.findViewById<NumberPicker>(R.id.layout_add_timer_np_min) ?: return
        val secPicker = dialog.findViewById<NumberPicker>(R.id.layout_add_timer_np_sec) ?: return
        this.tvDialogSet = dialogSet
        this.npMin = minPicker
        this.npSec = secPicker


        minPicker.maxValue = 60
        minPicker.minValue = 0
        secPicker.minValue = 0
        secPicker.maxValue = 59

        // Cancel button
        dialog.findViewById<TextView>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()

        dialogSet.setOnClickListener {
            val min: Int = minPicker.value
            val sec: Int = secPicker.value

            START_TIME_IN_MILLIS = ((min * 60 + sec) * 1000).toLong()

            if (START_TIME_IN_MILLIS != 0L) {
                binding.startBtn.visibility = View.VISIBLE
                binding.resetBtn.visibility = View.VISIBLE
                binding.progressBar.progress = 0F
                mTimeLeftInMillis = START_TIME_IN_MILLIS
                updateCountDownText()
                dialog.dismiss()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Please Set Timer at least 1 Sec",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "TimerScreen")
        super.onDestroy()
        if (::mCountDownTimer.isInitialized) {
            mCountDownTimer.cancel()
        }
        addTimerDialog?.dismiss()
        addTimerDialog = null
    }
}
