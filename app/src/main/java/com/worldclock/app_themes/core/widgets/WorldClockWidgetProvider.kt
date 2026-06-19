package com.worldclock.app_themes.core.widgets


import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.worldclock.app_themes.R
import java.util.TimeZone


class WorldClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        Log.e("TAG", "onUpdate: ", )



        for (appWidgetId in appWidgetIds) {

                            updateAppWidget(context, appWidgetManager, appWidgetId)



        }

    }


    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        Log.e("TAG", "onReceive: ")
        val safeContext = context ?: return


        if (intent?.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(safeContext)
            val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)

            if (appWidgetIds != null) {
                onUpdate(safeContext, appWidgetManager, appWidgetIds)
            }
        }



    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)

        Log.e("TAG", "onDisabled: dsfgdf")


    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)

        Log.e("TAG", "onEnabled: dsfgdf")

    }


    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {


        val views = RemoteViews(context.packageName, R.layout.widget_clock_layout)
        views.setTextViewText(R.id.cityName, TimeZone.getDefault().id.substring(TimeZone.getDefault().id.lastIndexOf("/") + 1))

        views.setString(R.id.clock_time_tv, "setTimeZone", TimeZone.getDefault().id)
        val intent = Intent(context, ListWidgetService::class.java)
        views.setRemoteAdapter(R.id.widgetListView, intent)


        Log.e("TAG", "updateAppWidget: ")

        appWidgetManager.updateAppWidget(appWidgetId, views)

    }



}
