package com.worldclock.app_themes.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldclock.app_themes.R
import com.worldclock.app_themes.adapter.LapTimeAdaptor
import com.worldclock.app_themes.databinding.ActivityPurchaseBinding
import com.worldclock.app_themes.databinding.ActivityStopWatchBinding
import com.worldclock.app_themes.utils.GradientTextHelper
import com.worldclock.app_themes.utils.TimeFormatUtil
import java.util.Timer
import java.util.TimerTask

class StopWatchActivity : BaseActivity() {

    lateinit var binding: ActivityStopWatchBinding
    private var timer: Timer? = null
    private var currentTime = 0
    private var lapTime = 0
    private var lapCounter = 0
    private val mId = 1
    private var lapViewExists = false
    private var isButtonStartPressed = false
    private var isStarted = false
    lateinit var lapTimeAdaptor: LapTimeAdaptor
    lateinit var counterList: ArrayList<String>
    lateinit var lapTimeList: ArrayList<String>
    var stopWatchProgress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStopWatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        binding.toolbar.back.setOnClickListener { finish() }
        binding.toolbar.title.text = getString(R.string.stop_watch)


        GradientTextHelper.apply(
            binding.stopWatchTime,
            Color.parseColor("#7441D0"),
            Color.parseColor("#EF4A9A"),
            direction = GradientTextHelper.Direction.LEFT_RIGHT
        )

        counterList = arrayListOf<String>()
        lapTimeList = arrayListOf<String>()
        binding.progressBar.isEnabled = false
        binding.lapTimeRv.layoutManager = GridLayoutManager(this, 2)

        binding.startBtn.setOnClickListener {

            if (isButtonStartPressed) {
                onSWatchStop()
            } else {


                isButtonStartPressed = true
                isStarted = true
                binding.startBtn.setImageResource(R.drawable.pause_btn)
                binding.lapsBtn.visibility = View.VISIBLE
                binding.resetBtn.visibility = View.INVISIBLE
                timer = Timer()
                timer!!.schedule(object : TimerTask() {
                    override fun run() {
                        runOnUiThread(Runnable {
                            currentTime += 1
                            lapTime += 1
                            stopWatchProgress += 1
                            if (stopWatchProgress > 5999) {
                                stopWatchProgress = 0
                            } else {
                                binding.progressBar.progress = stopWatchProgress.toFloat()
                            }
                            binding.stopWatchTime.text = TimeFormatUtil.toDisplayString(currentTime)
                        })
                    }
                }, 0, 10)
            }

        }

        binding.resetBtn.setOnClickListener {
            if (!isButtonStartPressed) {
                onSWatchReset()
            }
        }

        binding.lapsBtn.setOnClickListener {
            if (isButtonStartPressed) {
                onSWatchLap()
            }
        }

    }

    fun onSWatchStop() {


        binding.startBtn.setImageResource(R.drawable.play_btn)
        binding.resetBtn.visibility = View.VISIBLE
        binding.lapsBtn.visibility = View.INVISIBLE
        isButtonStartPressed = false
        timer!!.cancel()
    }

    fun onSWatchReset() {
        if (isStarted) {
            timer!!.cancel()

            currentTime = 0
            lapTime = 0
            stopWatchProgress = 0

            binding.stopWatchTime.text = TimeFormatUtil.toDisplayString(currentTime)
            binding.startBtn.setImageResource(R.drawable.play_btn)
            binding.lapsBtn.visibility = View.INVISIBLE
            binding.resetBtn.visibility = View.INVISIBLE
            binding.progressBar.progress = 0F
        }
    }

    fun onSWatchLap() {


        lapViewExists = true
        lapCounter++

        counterList.add(0, lapCounter.toString())
        lapTimeList.add(0, TimeFormatUtil.toDisplayString(lapTime))

        lapTimeAdaptor = LapTimeAdaptor(counterList, lapTimeList)
        binding.lapTimeRv.adapter = lapTimeAdaptor
        lapTimeAdaptor.notifyDataSetChanged()

    }


}