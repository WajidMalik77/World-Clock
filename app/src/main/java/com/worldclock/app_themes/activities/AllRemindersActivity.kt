package com.worldclock.app_themes.activities

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.worldclock.app_themes.R
import com.worldclock.app_themes.adapter.AlarmCategoryAdapter
import com.worldclock.app_themes.database.WorldClockDatabase
import com.worldclock.app_themes.databinding.ActivityAllRemindersBinding
import com.worldclock.app_themes.utils.getAlarmCategories
import kotlinx.coroutines.launch

class AllRemindersActivity : BaseActivity() {

    private val binding by lazy { ActivityAllRemindersBinding.inflate(layoutInflater) }
    private lateinit var categoryAdapter: AlarmCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        binding.toolbar.back.setOnClickListener { finish() }
        binding.toolbar.title.text = getString(R.string.all_reminders)

        categoryAdapter = AlarmCategoryAdapter(getAlarmCategories()) { category ->
            startActivity(
                Intent(this, AddAllRemindersActivity::class.java)
                    .putExtra("category_id", category.id)
                    .putExtra("category_title", category.title)
            )
        }

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(this@AllRemindersActivity, 2)
            adapter = categoryAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        loadReminderCounts()
    }

    private fun loadReminderCounts() {
        val dao = WorldClockDatabase.getDatabase(this).reminderDao()
        lifecycleScope.launch {
            val updatedCategories = getAlarmCategories().map { category ->
                val count = dao.getCountByCategoryId(category.id)
                category.copy(reminderCount = count)
            }
            categoryAdapter.updateItems(updatedCategories)
        }
    }
}