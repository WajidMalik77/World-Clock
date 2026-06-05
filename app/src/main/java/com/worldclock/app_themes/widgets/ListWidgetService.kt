package com.worldclock.app_themes.widgets

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.lifecycle.LifecycleOwner
import com.worldclock.app_themes.R
import com.worldclock.app_themes.database.WidgetClockItem
import com.worldclock.app_themes.database.WorldClockDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ListWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ListRemoteViewsFactory(applicationContext)
    }
}

class ListRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var widgetTimeZoneList: List<WidgetClockItem> = emptyList()

    override fun onCreate() {
        // initial load (optional)
        loadData()
    }

    override fun onDataSetChanged() {
        // called whenever widget needs to refresh
        loadData()
    }

    private fun loadData() {
        runBlocking(Dispatchers.IO) {
            val db = WorldClockDatabase.getDatabase(context)
            widgetTimeZoneList = db.widgetClockDao().getSelectedClocksSync()
        }
    }


    override fun onDestroy() {
        widgetTimeZoneList = emptyList()
    }

    override fun getCount(): Int = widgetTimeZoneList.size

    override fun getViewAt(position: Int): RemoteViews {
        val item = widgetTimeZoneList[position]

        val views = RemoteViews(context.packageName, R.layout.widget_list_item)
        views.setTextViewText(
            R.id.cityName,
            item.city
        )

        views.setTextViewText(R.id.countryName, item.country)
        views.setTextViewText(R.id.countryFlag, item.flag)
        val zone = ZoneId.of(item.timeZoneId)
        val currentTime = ZonedDateTime.now(zone)
        val formattedTime = currentTime.format(DateTimeFormatter.ofPattern("hh:mm a"))

        views.setTextViewText(R.id.current_time_tv, formattedTime)
        views.setTextViewText(R.id.current_time_tv1, item.relation)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}
