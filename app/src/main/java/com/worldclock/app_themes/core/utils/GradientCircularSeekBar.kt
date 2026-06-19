package com.worldclock.app_themes.core.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class GradientCircularSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val startColor = Color.parseColor("#7441D0")
    private val endColor = Color.parseColor("#EF4A9A")

    var max = 6000f
    var progress = 0f
        set(value) {
            field = value.coerceIn(0f, max)
            invalidate()
        }

    var onProgressChanged: ((Float) -> Unit)? = null

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 30f
        color = Color.parseColor("#F0F0F0")
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 30f
        strokeCap = Paint.Cap.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#EF4A9A")
        maskFilter = BlurMaskFilter(24f, BlurMaskFilter.Blur.NORMAL)
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#EF4A9A")
    }

    private val arcRect = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val strokeWidth = trackPaint.strokeWidth
        val padding = strokeWidth / 2f + 20f
        arcRect.set(padding, padding, w - padding, h - padding)

        val cx = w / 2f
        val cy = h / 2f
        val shader = SweepGradient(
            cx, cy,
            intArrayOf(startColor, endColor),
            floatArrayOf(0f, 1f)
        )
        val matrix = Matrix()
        matrix.postRotate(-90f, cx, cy)
        shader.setLocalMatrix(matrix)
        progressPaint.shader = shader
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Grey track
        canvas.drawArc(arcRect, -90f, 360f, false, trackPaint)

        // 2. Gradient progress arc
        if (progress > 0) {
            val sweepAngle = (progress / max) * 360f
            canvas.drawArc(arcRect, -90f, sweepAngle, false, progressPaint)

            // 3. Glowing dot at arc end
            val angleRad = Math.toRadians((-90.0 + sweepAngle))
            val cx = arcRect.centerX()
            val cy = arcRect.centerY()
            val radius = arcRect.width() / 2f
            val dotX = (cx + radius * cos(angleRad)).toFloat()
            val dotY = (cy + radius * sin(angleRad)).toFloat()

            canvas.drawCircle(dotX, dotY, 22f, glowPaint)
            canvas.drawCircle(dotX, dotY, 10f, dotPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                val cx = width / 2f
                val cy = height / 2f
                val angle = Math.toDegrees(
                    atan2((event.y - cy).toDouble(), (event.x - cx).toDouble())
                ).toFloat() + 90f
                val normalizedAngle = (angle + 360f) % 360f
                progress = (normalizedAngle / 360f) * max
                onProgressChanged?.invoke(progress)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}