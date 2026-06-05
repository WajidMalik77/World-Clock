package com.worldclock.app_themes.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.ActivityAddAllRemindersBinding
import com.worldclock.app_themes.databinding.ActivityAllRemindersBinding
import com.worldclock.app_themes.utils.GradientTextHelper
import androidx.core.graphics.toColorInt
import com.worldclock.app_themes.adapter.ReminderAdapter
import com.worldclock.app_themes.database.ReminderDao
import com.worldclock.app_themes.database.WorldClockDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddAllRemindersActivity : BaseActivity() {

    private val binding by lazy { ActivityAddAllRemindersBinding.inflate(layoutInflater) }
    private lateinit var dao: ReminderDao
    private lateinit var adapter: ReminderAdapter
    private var categoryId = -1
    private var categoryTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)

        categoryId = intent.getIntExtra("category_id", -1)
        categoryTitle = intent.getStringExtra("category_title") ?: ""

        dao = WorldClockDatabase.getDatabase(this).reminderDao()

        binding.toolbar.back.setOnClickListener { finish() }
        binding.toolbar.title.text = getString(R.string.all_reminders)
        binding.textView1.text = getString(R.string.create_reminder_for, categoryTitle)

        GradientTextHelper.apply(
            binding.textView,
            "#7441D0".toColorInt(),
            "#EF4A9A".toColorInt(),
            direction = GradientTextHelper.Direction.LEFT_RIGHT
        )

        setupRecycler()

        binding.addNewReminder.setOnClickListener {
            startActivity(
                Intent(this, AddReminderActiviity::class.java)
                    .putExtra("category_id", categoryId)
                    .putExtra("category_title", categoryTitle)
            )
        }
    }

    private fun setupRecycler() {
        adapter = ReminderAdapter(
            onToggle = { reminder, enabled ->
                CoroutineScope(Dispatchers.IO).launch {
                    dao.setEnabled(reminder.id, enabled)
                }
            },
            onEdit = { reminder ->
                startActivity(
                    Intent(this, AddReminderActiviity::class.java)
                        .putExtra("reminder_id", reminder.id)
                        .putExtra("category_id", categoryId)
                        .putExtra("category_title", categoryTitle)
                )
            },
            onDelete = { reminder ->
                CoroutineScope(Dispatchers.IO).launch {
                    dao.deleteReminder(reminder)
                }
            }
        )

        binding.recyclerAddReminder.adapter = adapter

        // Observe reminders for this category
        dao.getRemindersByCategory(categoryId).observe(this) { reminders ->
            adapter.submitList(reminders)
            // Show empty state if no reminders
            binding.empty.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerAddReminder.visibility = if (reminders.isEmpty()) View.GONE else View.VISIBLE
        }
    }
}