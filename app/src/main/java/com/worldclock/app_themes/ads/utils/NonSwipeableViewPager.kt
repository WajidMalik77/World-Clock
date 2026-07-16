package com.worldclock.app_themes.ads.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NonSwipeableViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    var swipeEnabled = true

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return swipeEnabled && super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return swipeEnabled && super.onInterceptTouchEvent(event)
    }
}