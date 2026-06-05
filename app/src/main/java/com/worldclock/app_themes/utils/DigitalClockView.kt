package com.worldclock.app_themes.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import com.worldclock.app_themes.R
import java.util.Calendar
import androidx.core.content.withStyledAttributes

class DigitalClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var faceDrawable: Drawable? = null
        set(value) {
            field = value; invalidate()
        }

    var timeColor: Int = Color.WHITE
        set(value) {
            field = value; timePaint.color = value; invalidate()
        }

    var label: String? = null
        set(value) {
            field = value; invalidate()
        }

    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = timeColor
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 255, 255, 255)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            invalidate()
            handler.postDelayed(this, 1_000L)
        }
    }

    init {
        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.DigitalClockView) {
                faceDrawable = getDrawable(R.styleable.DigitalClockView_dcv_background)
                timeColor = getColor(
                    R.styleable.DigitalClockView_dcv_timeColor,
                    Color.WHITE
                )
                label = getString(R.styleable.DigitalClockView_dcv_label)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(ticker)
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(ticker)
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f


        faceDrawable?.apply {
            setBounds(0, 0, width, height)
            draw(canvas)
        }


        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val timeStr = "%02d:%02d".format(hour, minute)


        timePaint.textSize = h * 0.30f
        labelPaint.textSize = h * 0.12f


        val timeY = cy + timePaint.textSize * 0.35f
        canvas.drawText(timeStr, cx, timeY, timePaint)

        label?.let {
            val labelY = timeY + timePaint.textSize * 0.70f
            canvas.drawText(it, cx, labelY, labelPaint)
        }
    }
}