package com.worldclock.app_themes.presentation.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.worldclock.app_themes.R


import android.animation.ValueAnimator
import android.content.Context
import android.hardware.*
import android.view.View
import com.worldclock.app_themes.core.utils.CompassView
import com.worldclock.app_themes.core.analytics.AppEventLogger
import kotlin.math.*

// Make sure activity uses sensor permission? No runtime permission needed for these sensors.


import android.animation.ObjectAnimator
import android.hardware.*
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.worldclock.app_themes.databinding.ActivityCompassBinding
import com.worldclock.app_themes.databinding.ActivityPurchaseBinding
import kotlin.math.abs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CompassActivity : BaseActivity(), SensorEventListener {
    private val binding by lazy {
        ActivityCompassBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var bannerAdOrchestrator: com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator

    @Inject
    lateinit var nativeAdOrchestrator: NativeAdOrchestrator
    private var sensorManager: SensorManager? = null
    private var rotationVector: Sensor? = null
    private var accel: Sensor? = null
    private var mag: Sensor? = null


    private var lastAzimuth = 0f
    private var currentAzimuth = 0f

    private val accelValues = FloatArray(3)
    private val magValues = FloatArray(3)
    private var hasAccel = false
    private var hasMag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "CompassScreen", "activity_lifecycle")

        lifecycleScope.launch {
            bannerAdOrchestrator.loadBannerAd(
                context = this@CompassActivity,
                screen = "CompassScreen",
                position = "top",
                container = binding.bannerContainer.admobBanner,
                shimmer = binding.bannerContainer.bannerAdShimmer
            )
        }

        lifecycleScope.launch {
            nativeAdOrchestrator.loadNativeAds(
                context = this@CompassActivity,
                screen = "CompassScreen",
                nativeConfigs = listOf(
                    NativeAdConfig(
                        position = "bottom",
                        container = binding.adsContainer.admobNative,
                        shimmer = binding.adsContainer.nativeAdShimmer
                    )
                )
            )
        }
        binding.toolbar.title.text = getString(R.string.compass)
        binding.toolbar.back.setOnClickListener {
            finish()
        }


        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val manager = sensorManager
        if (manager == null) {
            binding.sensorStatusTv.text = "Sensor service unavailable"
            return
        }
        rotationVector = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        accel = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mag = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val available = buildString {
            append("RotationVector: ${rotationVector != null}\n")
            append("Accelerometer: ${accel != null}\n")
            append("Magnetometer: ${mag != null}\n")
        }
        binding.sensorStatusTv.text = available
    }

    override fun onResume() {
        super.onResume()
        val manager = sensorManager ?: return
        rotationVector?.let {
            manager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        } ?: run {
            accel?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
            mag?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                updateNeedle(azimuth)
            }

            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelValues, 0, 3)
                hasAccel = true
                if (hasMag) updateFromAccelMag()
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magValues, 0, 3)
                hasMag = true
                if (hasAccel) updateFromAccelMag()
            }
        }
    }

    private fun updateFromAccelMag() {
        val rotationMatrix = FloatArray(9)
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelValues, magValues)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            updateNeedle(azimuth)
        }
    }

    private fun updateNeedle(azimuthDeg: Float) {
        val az = (azimuthDeg + 360) % 360
        val delta = abs(az - currentAzimuth)
        if (delta < 0.5f) return // too small to matter

        lastAzimuth = currentAzimuth
        currentAzimuth = az

        runOnUiThread {
            binding.degreeText.text = "${az.toInt()}°"
            binding.degreeDirection.text = getDirectionFromDegree(az.roundToInt())

            val animator = ObjectAnimator.ofFloat(
                binding.needleImage,
                "rotation",
                -lastAzimuth,
                -currentAzimuth
            )
            animator.duration = 300
            animator.interpolator = LinearInterpolator()
            animator.start()
        }
    }

    private fun getDirectionFromDegree(degree: Int): String {
        return when (degree) {
            in 350..360, in 0..10 -> "N"
            in 11..33 -> "NE"
            in 34..56 -> "ENE"
            in 57..78 -> "E"
            in 79..101 -> "ESE"
            in 102..124 -> "SE"
            in 125..146 -> "SSE"
            in 147..168 -> "S"
            in 169..191 -> "SSW"
            in 192..213 -> "SW"
            in 214..236 -> "WSW"
            in 237..258 -> "W"
            in 259..281 -> "WNW"
            in 282..303 -> "NW"
            in 304..326 -> "NNW"
            in 327..349 -> "N"
            else -> ""
        }
    }

    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "CompassScreen")
        super.onDestroy()
    }
}
