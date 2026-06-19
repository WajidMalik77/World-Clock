package com.worldclock.app_themes.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.View
import androidx.core.content.ContextCompat

class ClockCanvasView(
    context: Context,
    private val startDegHr: Float,
    private val startDegMin: Float,
    private val startDegSec: Float = 0f,
    private val clockImage: Int,
    private val hourImage: Int,
    private val minImage: Int,
    private val secImage: Int? = null
) : View(context) {

    private fun getPercentageWidth(percentage: Int, total: Int): Int =
        (percentage * total) / 100

    private fun decodeFace(resId: Int, size: Int): Bitmap? {
        return try {
            val drawable = ContextCompat.getDrawable(context, resId) ?: return null
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            drawable.setBounds(0, 0, size, size)
            drawable.draw(Canvas(bmp))
            bmp
        } catch (e: Exception) { null }
    }

    private fun decodeNeedle(resId: Int, maxSize: Int): Bitmap? {
        return try {
            val drawable = ContextCompat.getDrawable(context, resId) ?: return null
            val iW = drawable.intrinsicWidth.takeIf  { it > 0 } ?: maxSize
            val iH = drawable.intrinsicHeight.takeIf { it > 0 } ?: maxSize
            // Scale so longer side = maxSize, preserve aspect ratio
            val scale = maxSize.toFloat() / maxOf(iW, iH)
            val w = (iW * scale).toInt().coerceAtLeast(1)
            val h = (iH * scale).toInt().coerceAtLeast(1)
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            drawable.setBounds(0, 0, w, h)
            drawable.draw(Canvas(bmp))
            bmp
        } catch (e: Exception) { null }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (canvas.width <= 0 || canvas.height <= 0) return

        val size = getPercentageWidth(100, canvas.width)

        // 1. Clock face — forced square, fills whole cell
        val face = decodeFace(clockImage, size) ?: return
        canvas.drawBitmap(face,
            ((canvas.width  - face.width)  / 2).toFloat(),
            ((canvas.height - face.height) / 2).toFloat(),
            null)

        // 2. Hour hand
        drawNeedle(canvas, hourImage, startDegHr, size)

        // 3. Minute hand
        drawNeedle(canvas, minImage, startDegMin, size)

        // 4. Second hand (optional)
        secImage?.let { drawNeedle(canvas, it, startDegSec, size) }

        // 5. Centre dot — always on top
        drawCentreDot(canvas, size)
    }

    /**
     * Draws needle at natural proportional size, rotated around clock centre.
     * Works correctly for ANY needle shape — square, landscape, portrait, diagonal.
     */
    private fun drawNeedle(canvas: Canvas, imageRes: Int, angleDeg: Float, size: Int) {
        var bmp = decodeNeedle(imageRes, size) ?: return

        // Rotate around the bitmap's own centre
        val matrix = Matrix()
        matrix.postRotate(angleDeg, bmp.width / 2f, bmp.height / 2f)
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)

        // Draw centred on the clock centre
        canvas.drawBitmap(bmp,
            ((canvas.width  - bmp.width)  / 2).toFloat(),
            ((canvas.height - bmp.height) / 2).toFloat(),
            null)
    }

    private fun drawCentreDot(canvas: Canvas, clockSize: Int) {
        val cx = canvas.width  / 2f
        val cy = canvas.height / 2f
        canvas.drawCircle(cx, cy, clockSize * 0.018f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#888888")
        })
        canvas.drawCircle(cx, cy, clockSize * 0.012f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF3B00")
        })
    }
}