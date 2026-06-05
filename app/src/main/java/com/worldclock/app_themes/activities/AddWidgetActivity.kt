package com.worldclock.app_themes.activities

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.worldclock.app_themes.R
import com.worldclock.app_themes.activities.AddClockActivity
import com.worldclock.app_themes.adapter.AddClockAdapter
import com.worldclock.app_themes.adapter.AddWidgetsClockAdapter
import com.worldclock.app_themes.database.WidgetClockDao
import com.worldclock.app_themes.database.WorldClockDao
import com.worldclock.app_themes.database.WorldClockDatabase
import com.worldclock.app_themes.databinding.ActivityAddClockBinding
import com.worldclock.app_themes.databinding.ActivityAddWidgetBinding
import com.worldclock.app_themes.utils.getAllWidgetClocksUsingZoneTabWithRelation
import com.worldclock.app_themes.utils.getAllWorldClocksUsingZoneTabWithRelation
import com.worldclock.app_themes.utils.updateTimes
import com.worldclock.app_themes.utils.updateWidgetTimes
import com.worldclock.app_themes.widgets.WorldClockWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddWidgetActivity : BaseActivity() {
    private val binding by lazy {
        ActivityAddWidgetBinding.inflate(layoutInflater)
    }
    private lateinit var dao: WidgetClockDao

    private lateinit var adapter: AddWidgetsClockAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        dao = WorldClockDatabase.getDatabase(this).widgetClockDao()

        CoroutineScope(Dispatchers.IO).launch {
            val existingCount = dao.getCount()

            if (existingCount == 0) {
                val clocks = getAllWidgetClocksUsingZoneTabWithRelation()
                dao.insertAll(clocks)
                Log.d("DB", "Inserted ${clocks.size} clocks")
            } else {
                Log.d("DB", "Database already has data, skipping insert.")
            }

            withContext(Dispatchers.Main) {
                adapter = AddWidgetsClockAdapter { clock ->
                    lifecycleScope.launch {
                        dao.updateClock(clock)
                        val appWidgetManager = AppWidgetManager.getInstance(this@AddWidgetActivity)
                        val componentName = ComponentName(
                            this@AddWidgetActivity,
                            WorldClockWidgetProvider::class.java
                        )
                        appWidgetManager.notifyAppWidgetViewDataChanged(
                            appWidgetManager.getAppWidgetIds(componentName),
                            R.id.widgetListView // the id of your ListView in the widget layout
                        )

                    }
                }
                binding.toolbar.title.text = getString(R.string.add_city)
                binding.toolbar.back.setOnClickListener {
                    finish()
                }
                loadClocks()

                binding.searchIcon.setOnClickListener {
                    if (binding.searchBar.isGone) {
                        // Expand with animation
                        binding.searchBar.visibility = View.VISIBLE
                        binding.searchIcon.setImageResource(R.drawable.close)

                        binding.searchBar.alpha = 0f
                        binding.searchBar.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .withEndAction {
                                binding.searchBar.requestFocus()
                                val imm =
                                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.showSoftInput(
                                    binding.searchBar,
                                    InputMethodManager.SHOW_IMPLICIT
                                )
                            }
                            .start()
                    } else {
                        binding.searchIcon.setImageResource(R.drawable.search)

                        // Collapse with animation
                        binding.searchBar.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction {
                                binding.searchBar.visibility = View.GONE
                                binding.searchBar.text?.clear()
                            }
                            .start()
                    }
                }
            }
        }

        adapter = AddWidgetsClockAdapter { clock ->
            lifecycleScope.launch {
                dao.updateClock(clock)
            }
        }
        binding.toolbar.title.text = getString(R.string.add_city)
        binding.toolbar.back.setOnClickListener {
            finish()
        }
        loadClocks()

        binding.searchIcon.setOnClickListener {
            if (binding.searchBar.isGone) {
                // Expand with animation
                binding.searchBar.visibility = View.VISIBLE
                binding.searchIcon.setImageResource(R.drawable.close)

                binding.searchBar.alpha = 0f
                binding.searchBar.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .withEndAction {
                        binding.searchBar.requestFocus()
                        val imm =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(binding.searchBar, InputMethodManager.SHOW_IMPLICIT)
                    }
                    .start()
            } else {
                binding.searchIcon.setImageResource(R.drawable.search)

                // Collapse with animation
                binding.searchBar.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        binding.searchBar.visibility = View.GONE
                        binding.searchBar.text?.clear()
                    }
                    .start()
            }
        }
    }

    private fun loadClocks() {
        CoroutineScope(Dispatchers.IO).launch {
            val clocks =
                WorldClockDatabase.getDatabase(this@AddWidgetActivity).widgetClockDao()
                    .getAllClocks()
            val updated = updateWidgetTimes(clocks)
            withContext(Dispatchers.Main) {
                adapter.updateList(updated)
                binding.recycler.adapter = adapter
                binding.recycler.adapter = adapter
                binding.searchBar.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        val query = s.toString().trim()
                        val filtered = if (query.isEmpty()) {
                            updated
                        } else {
                            updated.filter {
                                it.city.contains(query, ignoreCase = true) ||
                                        it.country.contains(query, ignoreCase = true)
                            }
                        }
                        adapter.updateList(filtered)
                    }
                })

            }
        }
    }

}