package com.worldclock.app_themes.ads.utils

import android.content.Context
import android.util.TypedValue
import android.widget.Toast
import androidx.lifecycle.MutableLiveData

object Utils {

    var splashAdLoaded: MutableLiveData<Boolean> = MutableLiveData(false)

    fun showMessage(context: Context, string: String) {
        if (GetFirebase.toastForAds){
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show()

        }
    }

    var isPremium = false

    fun Context.getSspTextSizeInPx(sspValue: Int): Float {
        val textSizeResId = resources.getIdentifier("_${sspValue}ssp", "dimen", packageName)
        return if (textSizeResId != 0) {
            resources.getDimension(textSizeResId)
        } else {
            // Fallback if dimension is not found
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sspValue.toFloat(),
                resources.displayMetrics
            )
        }
    }


//for other views

    fun Context.getSdpHeightInPx(sdpValue: Int): Int {
        val heightResId = resources.getIdentifier("_${sdpValue}sdp", "dimen", packageName)
        return if (heightResId != 0) {
            resources.getDimensionPixelSize(heightResId)
        } else {
            // Fallback if dimension is not found
            (sdpValue * resources.displayMetrics.density).toInt()
        }
    }

}