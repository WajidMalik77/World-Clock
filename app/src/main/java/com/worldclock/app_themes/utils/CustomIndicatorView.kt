package com.worldclock.app_themes.utils


import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.worldclock.app_themes.R
import kotlin.ranges.until

class CustomIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val indicatorSize = 5 // size in dp
    private val indicatorMargin = 10 // margin in dp
    private val activeIndicatorWidth = 30 // elongated indicator width in dp
    private val inactiveIndicatorWidth = 30

    init {
        orientation = HORIZONTAL
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    fun setupIndicators(count: Int) {
        removeAllViews()
        for (i in 0 until count) {
            val view = View(context)
            val params = LayoutParams(dpToPx(inactiveIndicatorWidth), dpToPx(indicatorSize))
            params.setMargins(dpToPx(indicatorMargin), 0, dpToPx(indicatorMargin), 0)
            view.layoutParams = params
            view.setBackgroundResource(R.drawable.inactive_indicator)
            addView(view)
        }
    }

    fun selectIndicator(position: Int) {
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            val params = view.layoutParams as LayoutParams
            if (i == position) {
                animateIndicator(view, params, activeIndicatorWidth)
                view.setBackgroundResource(R.drawable.active_indicator)
            } else {
                animateIndicator(view, params, inactiveIndicatorWidth)
                view.setBackgroundResource(R.drawable.inactive_indicator)
            }
        }
    }

    private fun animateIndicator(view: View, params: LayoutParams, targetWidth: Int) {
        val startWidth = params.width
        val endWidth = dpToPx(targetWidth)

        val animator = ValueAnimator.ofInt(startWidth, endWidth)
        animator.addUpdateListener { animation ->
            params.width = animation.animatedValue as Int
            view.layoutParams = params
        }
        animator.duration = 300 // Duration of the animation in milliseconds
        animator.start()
    }


}

