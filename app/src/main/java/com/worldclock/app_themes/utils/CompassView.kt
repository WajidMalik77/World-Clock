package com.worldclock.app_themes.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class CompassView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val dialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 48f
        textAlign = Paint.Align.CENTER
    }
    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var azimuthDegrees: Float = 0f    // 0..360 (0 = North)
    private var needlePath = Path()

    // Colors (you can expose these via attrs if you want)
    init {
        dialPaint.color = Color.LTGRAY
        tickPaint.color = Color.DKGRAY
        textPaint.color = Color.BLACK
        needlePaint.color = Color.RED
    }

    // Public setter; call from UI thread
    fun setAzimuth(deg: Float) {
        azimuthDegrees = (deg % 360 + 360) % 360
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f
        val radius = min(cx, cy) * 0.85f

        // Draw dial
        canvas.drawCircle(cx, cy, radius, dialPaint)

        // Draw ticks and labels (N, E, S, W)
        for (i in 0 until 360 step 10) {
            val angleRad = Math.toRadians(i.toDouble())
            val inner = radius - (if (i % 90 == 0) 30f else if (i % 30 == 0) 18f else 10f)
            val outer = radius
            val sx = (cx + inner * Math.sin(angleRad)).toFloat()
            val sy = (cy - inner * Math.cos(angleRad)).toFloat()
            val ex = (cx + outer * Math.sin(angleRad)).toFloat()
            val ey = (cy - outer * Math.cos(angleRad)).toFloat()
            canvas.drawLine(sx, sy, ex, ey, tickPaint)
        }

        // Cardinal labels
        val labelOffset = radius - 60f
        canvas.drawText("N", cx, cy - labelOffset, textPaint)
        canvas.drawText("S", cx, cy + labelOffset + textPaint.textSize/2, textPaint)
        canvas.drawText("E", cx + labelOffset, cy + textPaint.textSize/2, textPaint)
        canvas.drawText("W", cx - labelOffset, cy + textPaint.textSize/2, textPaint)

        // Save, rotate canvas so needle points to azimuth (0 degree = north/up)
        canvas.save()
        canvas.rotate(-azimuthDegrees, cx, cy) // negative because we want world->screen

        // Draw needle: simple triangle pointing up (north)
        needlePath.reset()
        val needleLength = radius * 0.9f
        val baseWidth = radius * 0.14f
        needlePath.moveTo(cx, cy - needleLength)                  // tip (north)
        needlePath.lineTo(cx - baseWidth, cy + 20f)               // bottom-left
        needlePath.lineTo(cx + baseWidth, cy + 20f)               // bottom-right
        needlePath.close()
        canvas.drawPath(needlePath, needlePaint)

        // Optional center circle
        canvas.drawCircle(cx, cy, 10f, tickPaint)

        canvas.restore()

        // Show numeric azimuth (optional)
        canvas.drawText("${azimuthDegrees.toInt()}°", cx, cy + radius + 60f, textPaint)
    }
}
