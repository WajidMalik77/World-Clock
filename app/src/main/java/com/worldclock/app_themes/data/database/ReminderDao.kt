package com.worldclock.app_themes.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReminderDao {
    @Insert
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE categoryId = :categoryId ORDER BY id DESC")
    fun getRemindersByCategory(categoryId: Int): LiveData<List<ReminderEntity>>

    @Query("SELECT COUNT(*) FROM reminders WHERE categoryId = :categoryId")
    suspend fun getCountByCategoryId(categoryId: Int): Int


    @Query("UPDATE reminders SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Int, enabled: Boolean)

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Int): ReminderEntity?
    @Query("SELECT * FROM reminders")
    suspend fun getAllRemindersSync(): List<ReminderEntity>

}