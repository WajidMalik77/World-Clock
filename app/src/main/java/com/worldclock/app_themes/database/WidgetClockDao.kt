package com.worldclock.app_themes.database

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetClockDao {

    @Query("SELECT COUNT(*) FROM widget_clocks")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clocks: List<WidgetClockItem>)

    @Query("SELECT * FROM widget_clocks ORDER BY country ASC")
    suspend fun getAllClocks(): List<WidgetClockItem>

    @Query("DELETE FROM widget_clocks")
    suspend fun deleteAll()
    @Delete
    suspend fun deleteWidget(alarm: WidgetClockItem)
    @Update
    suspend fun updateClock(clock: WidgetClockItem)

    @Query("SELECT * FROM widget_clocks WHERE isSelected = 1 ORDER BY city ASC")
    fun getSelectedClocks(): LiveData<List<WidgetClockItem>>
    @Query("SELECT * FROM widget_clocks WHERE isSelected = 1 ORDER BY city ASC")
    fun getSelectedClocksSync(): List<WidgetClockItem>
}
