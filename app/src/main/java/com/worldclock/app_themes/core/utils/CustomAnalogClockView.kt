package com.worldclock.app_themes.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.worldclock.app_themes.R
import java.util.Calendar

class CustomAnalogClockView @JvmOverloads constructor(
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

    // How long each hand should be as fraction of clock radius
    // Tune these per clock theme if needed
    var hourLengthFraction: Float = 0.45f   // hour tip = 45% of radius
    var minuteLengthFraction: Float = 0.60f   // minute tip = 60% of radius

    // Correction for needles exported pre-rotated from Figma
    // Set to the NEGATIVE of the Figma rotation value shown on the needle layer
    // e.g. Figma shows -60° → set baseAngle = 90f  (trial & error in 90° steps)
    var hourBaseAngle: Float = 90f
    var minuteBaseAngle: Float = 90f

    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            invalidate(); handler.postDelayed(this, 1_000L)
        }
    }

    init {
        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.CustomAnalogClockView) {
                faceDrawable = getDrawable(R.styleable.CustomAnalogClockView_acv_face)
                hourDrawable = getDrawable(R.styleable.CustomAnalogClockView_acv_hourHand)
                minuteDrawable = getDrawable(R.styleable.CustomAnalogClockView_acv_minuteHand)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow(); handler.post(ticker)
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(ticker); super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = minOf(width, height)
        val cx = width / 2f
        val cy = height / 2f
        val radius = size / 2f
        if (size <= 0) return

        // 1. Face — fills full view
        faceDrawable?.apply { setBounds(0, 0, width, height); draw(canvas) }

        // 2. Time angles
        val cal = Calendar.getInstance()
        val second = cal.get(Calendar.SECOND)
        val minute = cal.get(Calendar.MINUTE)
        val hour = cal.get(Calendar.HOUR)

        val hourAngle = hour * 30f + minute * 0.5f
        val minuteAngle = minute * 6f + second * 0.1f

        // 3. Draw hands
        hourDrawable?.let {
            drawNeedle(canvas, it, hourAngle + hourBaseAngle, cx, cy, radius, hourLengthFraction)
        }
        minuteDrawable?.let {
            drawNeedle(
                canvas,
                it,
                minuteAngle + minuteBaseAngle,
                cx,
                cy,
                radius,
                minuteLengthFraction
            )
        }
    }

    /**
     * Draws a needle correctly regardless of how the asset was exported.
     *
     * - Scales the needle so its LONGER side = [lengthFraction] × radius × 2
     * - Preserves aspect ratio — no stretching
     * - Rotates around the clock centre (cx, cy)
     * - Works for both portrait needles (pointing up) AND
     *   landscape needles (pre-rotated from Figma)
     */
    private fun drawNeedle(
        canvas: Canvas,
        drawable: Drawable,
        angleDeg: Float,
        cx: Float, cy: Float,
        radius: Float,
        lengthFraction: Float
    ) {
        // Intrinsic size of the needle asset (preserves designer's proportions)
        val iW = drawable.intrinsicWidth.takeIf { it > 0 } ?: 10
        val iH = drawable.intrinsicHeight.takeIf { it > 0 } ?: 50

        // Scale so the longer dimension = lengthFraction × diameter
        val targetSize = (lengthFraction * radius * 2f)
        val scale = targetSize / maxOf(iW, iH).toFloat()
        val scaledW = (iW * scale).toInt().coerceAtLeast(1)
        val scaledH = (iH * scale).toInt().coerceAtLeast(1)

        // Render the drawable at scaled size
        val bitmap = drawable.toBitmap(scaledW, scaledH)

        // Rotate around clock centre
        val matrix = Matrix()
        // 1. Translate so bitmap centre sits on (cx, cy)
        matrix.postTranslate(cx - scaledW / 2f, cy - scaledH / 2f)
        // 2. Rotate around (cx, cy)
        matrix.postRotate(angleDeg, cx, cy)

        canvas.drawBitmap(bitmap, matrix, null)
    }

    private fun Drawable.toBitmap(w: Int, h: Int): Bitmap {
        if (this is BitmapDrawable && bitmap != null)
            return Bitmap.createScaledBitmap(bitmap, w, h, true)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        setBounds(0, 0, w, h)
        draw(c)
        return bmp
    }
}