package com.worldclock.app_themes.utils

object TimeFormatUtil {
    fun toDisplayString(timeHundreds: Int): String {
        var timeHundreds = timeHundreds
        val formatterArrayMillis = arrayOfNulls<String>(2)
        val formattedSeconds: String
        val formattedMinutes: String
        val hundreds: Int = timeHundreds % 100
        val milliSecStr = Integer.toString(hundreds)
        formatterArrayMillis[0] = "0$milliSecStr"
        formatterArrayMillis[1] = milliSecStr
        val seconds: Int = 100.let { timeHundreds /= it; timeHundreds } % 60
        val minutes: Int = 60.let { timeHundreds /= it; timeHundreds } % 60

        //format output
        formattedSeconds = Integer.toString(seconds / 10) +
                Integer.toString(seconds % 10)
        formattedMinutes = Integer.toString(minutes / 10) +
                Integer.toString(minutes % 10)
        val millSecDigitsCnt = milliSecStr.length
        return formattedMinutes + ":" +
                formattedSeconds + "." +
                formatterArrayMillis[millSecDigitsCnt - 1]
    }
}