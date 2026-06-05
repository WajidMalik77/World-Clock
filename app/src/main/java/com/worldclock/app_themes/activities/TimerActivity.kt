package com.worldclock.app_themes.activities

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
import com.worldclock.app_themes.utils.GradientTextHelper

class TimerActivity : BaseActivity() {

    lateinit var binding: ActivityTimerBinding
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
        mCountDownTimer.cancel()
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
        this.addTimerDialog!!.window?.setBackgroundDrawable(ColorDrawable(0))


        this.addTimerDialog!!.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        this.tvDialogSet = dialog.findViewById(R.id.layout_add_timer_tv_set)
        this.npMin = dialog.findViewById(R.id.layout_add_timer_np_min)
        this.npSec = dialog.findViewById(R.id.layout_add_timer_np_sec)


        this.npMin!!.maxValue = 60
        this.npMin!!.minValue = 0
        this.npSec!!.minValue = 0
        this.npSec!!.maxValue = 59

        // Cancel button
        dialog.findViewById<TextView>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        this.addTimerDialog!!.setCancelable(true)
        this.addTimerDialog!!.show()

        this.tvDialogSet!!.setOnClickListener {
            val min: Int = this@TimerActivity.npMin!!.value
            val sec: Int = this@TimerActivity.npSec!!.value

            START_TIME_IN_MILLIS = ((min * 60 + sec) * 1000).toLong()

            if (START_TIME_IN_MILLIS != 0L) {
                binding.startBtn.visibility = View.VISIBLE
                binding.resetBtn.visibility = View.VISIBLE
                binding.progressBar.progress = 0F
                mTimeLeftInMillis = START_TIME_IN_MILLIS
                updateCountDownText()
                this.addTimerDialog!!.dismiss()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Please Set Timer at least 1 Sec",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}