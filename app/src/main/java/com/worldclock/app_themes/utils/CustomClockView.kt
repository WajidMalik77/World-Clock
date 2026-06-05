package com.worldclock.app_themes.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import com.worldclock.app_themes.R
import java.util.Calendar
import androidx.core.content.withStyledAttributes

class CustomClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    var faceDrawable: Drawable? = null
        set(value) {
            field = value; invalidate()
        }

    var hourDrawable: Drawable? = null
        set(value) {
            field = value; invalidate()
        }

    var minuteDrawable: Drawable? = null
        set(value) {
            field = value; invalidate()
        }

    var secondDrawable: Drawable? = null
        set(value) {
            field = value; invalidate()
        }

    private val handler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        override fun run() {
            invalidate()
            handler.postDelayed(this, 1_000L)
        }
    }

    init {

        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.CustomClockView) {
                faceDrawable = getDrawable(R.styleable.CustomClockView_clockFace)
                hourDrawable = getDrawable(R.styleable.CustomClockView_hourHand)
                minuteDrawable = getDrawable(R.styleable.CustomClockView_minuteHand)
                secondDrawable = getDrawable(R.styleable.CustomClockView_secondHand)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(tickRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(tickRunnable)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f


        faceDrawable?.let { d ->
            d.setBounds(0, 0, width, height)
            d.draw(canvas)
        }


        val cal = Calendar.getInstance()
        val second = cal.get(Calendar.SECOND)
        val minute = cal.get(Calendar.MINUTE)
        val hour = cal.get(Calendar.HOUR)

        val secondAngle = second * 6f
        val minuteAngle = minute * 6f + second * 0.1f
        val hourAngle = hour * 30f + minute * 0.5f

        drawHand(canvas, hourDrawable, hourAngle, cx, cy, w, h, scale = 0.55f)
        drawHand(canvas, minuteDrawable, minuteAngle, cx, cy, w, h, scale = 0.75f)
        drawHand(canvas, secondDrawable, secondAngle, cx, cy, w, h, scale = 0.80f)
    }

    private fun drawHand(
        canvas: Canvas,
        drawable: Drawable?,
        angleDeg: Float,
        cx: Float, cy: Float,
        viewW: Float, viewH: Float,
        scale: Float
    ) {
        drawable ?: return

        val halfW = (viewW * scale / 2f).toInt()
        val halfH = (viewH * scale / 2f).toInt()

        canvas.save()
        canvas.rotate(angleDeg, cx, cy)


        drawable.setBounds(
            (cx - halfW).toInt(),
            (cy - halfH).toInt(),
            (cx + halfW).toInt(),
            (cy + halfH).toInt()
        )
        drawable.draw(canvas)

        canvas.restore()
    }
}