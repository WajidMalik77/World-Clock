package com.worldclock.app_themes.activities

import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.worldclock.app_themes.R
import com.worldclock.app_themes.adapter.WorldClockAdapter
import com.worldclock.app_themes.databinding.ActivityClockBinding
import com.worldclock.app_themes.databinding.ActivityMainBinding
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.worldclock.app_themes.activities.WidgetActivity
import com.worldclock.app_themes.database.WidgetClockDao
import com.worldclock.app_themes.database.WidgetClockItem
import com.worldclock.app_themes.database.WorldClockDao
import com.worldclock.app_themes.database.WorldClockDatabase
import com.worldclock.app_themes.database.WorldClockItem
import com.worldclock.app_themes.utils.getAllWorldClocksUsingZoneTabWithRelation
import com.worldclock.app_themes.utils.getCurrentWorldClock
import com.worldclock.app_themes.utils.updateTimes
import com.worldclock.app_themes.widgets.WorldClockWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClockActivity : BaseActivity() {
    private val binding by lazy {
        ActivityClockBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: WorldClockAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        binding.toolbar.back.setOnClickListener {
            finish()
        }
        binding.countryName.text = getCurrentWorldClock().countryName
        binding.countryImg.text = getCurrentWorldClock().flag
        binding.timeTv.text = getCurrentWorldClock().time
        binding.dateTv.text = getCurrentWorldClock().date
        adapter = WorldClockAdapter(callbacks = {}, onLongClick = {
            showDeleteDialog(
                it,
                WorldClockDatabase.getDatabase(this@ClockActivity).worldClockDao()
            )
        })
        WorldClockDatabase.getDatabase(this@ClockActivity).worldClockDao().getSelectedClocks()
            .observe(this@ClockActivity) {
                val updated = updateTimes(it)
                adapter.updateList(updated)
                binding.recycler.adapter = adapter
                startClockUpdates()

            }
        binding.addClock.setOnClickListener {
            startActivity(Intent(this, AddClockActivity::class.java))
        }

    }

    private fun showDeleteDialog(alarm: WorldClockItem, dao: WorldClockDao) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_clock))
            .setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_clock))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                alarm.isSelected = false
                lifecycleScope.launch(Dispatchers.IO) {
                    dao.updateClock(alarm)

                }

            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateRunnable: Runnable
    private fun startClockUpdates() {
        updateRunnable = object : Runnable {
            override fun run() {
                adapter.updateTime()
                binding.timeTv.text = getCurrentWorldClock().time

                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }

    private fun loadClocks() {
        CoroutineScope(Dispatchers.IO).launch {
            WorldClockDatabase.getDatabase(this@ClockActivity).worldClockDao().getSelectedClocks()
                .observe(this@ClockActivity) {
                    val updated = updateTimes(it)

                }
            withContext(Dispatchers.Main) {

            }
        }
    }
}