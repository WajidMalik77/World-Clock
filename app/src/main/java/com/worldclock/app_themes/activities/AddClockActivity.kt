package com.worldclock.app_themes.activities

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.worldclock.app_themes.R
import com.worldclock.app_themes.activities.ClockActivity
import com.worldclock.app_themes.adapter.AddClockAdapter
import com.worldclock.app_themes.adapter.WorldClockAdapter
import com.worldclock.app_themes.database.WorldClockDao
import com.worldclock.app_themes.database.WorldClockDatabase
import com.worldclock.app_themes.databinding.ActivityAddClockBinding
import com.worldclock.app_themes.databinding.ActivityClockBinding
import com.worldclock.app_themes.utils.getAllWorldClocksUsingZoneTabWithRelation
import com.worldclock.app_themes.utils.updateTimes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddClockActivity : BaseActivity() {
    private val binding by lazy {
        ActivityAddClockBinding.inflate(layoutInflater)
    }
        private lateinit var dao: WorldClockDao

    private lateinit var adapter: AddClockAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
               dao = WorldClockDatabase.getDatabase(this).worldClockDao()

        CoroutineScope(Dispatchers.IO).launch {
            val existingCount = dao.getCount()  // 👈 check existing data

            if (existingCount == 0) {
                // generate and insert only if database is empty
                val clocks = getAllWorldClocksUsingZoneTabWithRelation()
                dao.insertAll(clocks)
                Log.d("DB", "Inserted ${clocks.size} clocks")
            } else {
                Log.d("DB", "Database already has data, skipping insert.")
            }

            withContext(Dispatchers.Main) {
                adapter = AddClockAdapter{ clock ->
                    lifecycleScope.launch {
                        dao.updateClock(clock)
                    }
                }
                binding.toolbar.title.text = getString(R.string.add_city)
                binding.toolbar.back.setOnClickListener {
                    finish()
                }
                loadClocks()

                binding.addNewClock.setOnClickListener {
                    val selectedItems = adapter.getSelectedItems()
                    if (selectedItems.isEmpty()) return@setOnClickListener

                    lifecycleScope.launch {
                        selectedItems.forEach { dao.updateClock(it) }
                        finish()
                    }
                }


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
        }

    }
    private fun loadClocks() {
        CoroutineScope(Dispatchers.IO).launch {
            val clocks =
                WorldClockDatabase.getDatabase(this@AddClockActivity).worldClockDao().getAllClocks()
            val updated = updateTimes(clocks)
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