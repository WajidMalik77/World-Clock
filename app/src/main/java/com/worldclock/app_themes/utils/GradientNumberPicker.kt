package com.worldclock.app_themes.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.widget.EditText
import android.widget.NumberPicker

class GradientNumberPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : NumberPicker(context, attrs) {

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(com.intuit.ssp.R.dimen._48ssp)
    }

    private val greyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(com.intuit.ssp.R.dimen._38ssp)
        color = Color.parseColor("#B1B1B1")
    }

    private val dividerPaint = Paint().apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 2f
    }

    init {
        descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        hideInternalEditText()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        hideInternalEditText()
    }

    private fun hideInternalEditText() {
        try {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child is EditText) {
                    child.setTextColor(Color.TRANSPARENT)
                    child.background = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val centerY = height / 2f
        val centerX = width / 2f
        val itemHeight = height / 3f

        val displayedValue = getDisplayedValue() ?: return
        val prevValue = getPrevValue()
        val nextValue = getNextValue()

        val gradientTextOffset = (gradientPaint.descent() + gradientPaint.ascent()) / 2f
        val greyTextOffset = (greyPaint.descent() + greyPaint.ascent()) / 2f

        // Top grey item
        if (prevValue != null) {
            canvas.drawText(prevValue, centerX, centerY - itemHeight - greyTextOffset, greyPaint)
        }

        // Selected gradient item
        val textWidth = gradientPaint.measureText(displayedValue)
        val startX = centerX - textWidth / 2f
        val endX = centerX + textWidth / 2f

        gradientPaint.shader = LinearGradient(
            startX, 0f, endX, 0f,
            intArrayOf(Color.parseColor("#7441D0"), Color.parseColor("#EF4A9A")),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawText(displayedValue, centerX, centerY - gradientTextOffset, gradientPaint)

        // Bottom grey item
        if (nextValue != null) {
            canvas.drawText(nextValue, centerX, centerY + itemHeight - greyTextOffset, greyPaint)
        }

        // Divider lines
        canvas.drawLine(
            0f,
            centerY - itemHeight / 2f,
            width.toFloat(),
            centerY - itemHeight / 2f,
            dividerPaint
        )
        canvas.drawLine(
            0f,
            centerY + itemHeight / 2f,
            width.toFloat(),
            centerY + itemHeight / 2f,
            dividerPaint
        )
    }

    private fun getSelectorIndices(): IntArray? {
        return try {
            val field = NumberPicker::class.java.getDeclaredField("mSelectorIndices")
            field.isAccessible = true
            field.get(this) as IntArray
        } catch (e: Exception) {
            null
        }
    }

    private fun getDisplayedValue(): String? {
        val indices = getSelectorIndices() ?: return null
        return String.format("%02d", indices[indices.size / 2])
    }

    private fun getPrevValue(): String? {
        val indices = getSelectorIndices() ?: return null
        val prev = indices[indices.size / 2 - 1]
        return if (prev < minValue) null else String.format("%02d", prev)
    }

    private fun getNextValue(): String? {
        val indices = getSelectorIndices() ?: return null
        val next = indices[indices.size / 2 + 1]
        return if (next > maxValue) null else String.format("%02d", next)
    }
}