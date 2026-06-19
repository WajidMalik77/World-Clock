package com.worldclock.app_themes.core.utils

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.widget.NumberPicker
import android.widget.TextView

object GradientTextHelper {

    fun apply(
        textView: TextView,
        vararg colors: Int,
        direction: Direction = Direction.LEFT_RIGHT
    ) {
        val paint = textView.paint
        val width = paint.measureText(textView.text.toString())
        val height = textView.textSize

        val (x0, y0, x1, y1) = when (direction) {
            Direction.LEFT_RIGHT -> listOf(0f, 0f, width, 0f)
            Direction.TOP_BOTTOM -> listOf(0f, 0f, 0f, height)
            Direction.DIAGONAL -> listOf(0f, 0f, width, height)
        }

        val shader = LinearGradient(x0, y0, x1, y1, colors, null, Shader.TileMode.CLAMP)
        textView.paint.shader = shader
        textView.invalidate()
    }

    fun applyToNumberPicker(
        numberPicker: NumberPicker,
        vararg colors: Int,
        direction: Direction = Direction.LEFT_RIGHT
    ) {
        numberPicker.post {
            try {
                val field = NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
                field.isAccessible = true
                val paint = field.get(numberPicker) as Paint

                val textWidth = paint.measureText("00")
                val height = paint.textSize
                val viewWidth = numberPicker.width.toFloat()
                val startX = (viewWidth - textWidth) / 2f
                val endX = startX + textWidth

                val (x0, y0, x1, y1) = when (direction) {
                    Direction.LEFT_RIGHT -> listOf(startX, 0f, endX, 0f)
                    Direction.TOP_BOTTOM -> listOf(startX, 0f, startX, height)
                    Direction.DIAGONAL -> listOf(startX, 0f, endX, height)
                }

                paint.shader = LinearGradient(
                    x0, y0, x1, y1,
                    colors,
                    null,
                    Shader.TileMode.CLAMP
                )
                numberPicker.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    enum class Direction { LEFT_RIGHT, TOP_BOTTOM, DIAGONAL }
}