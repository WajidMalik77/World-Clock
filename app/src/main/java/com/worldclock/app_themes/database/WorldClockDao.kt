package com.worldclock.app_themes.database

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldClockDao {

    @Query("SELECT COUNT(*) FROM world_clocks")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clocks: List<WorldClockItem>)

    @Query("SELECT * FROM world_clocks ORDER BY country ASC")
    suspend fun getAllClocks(): List<WorldClockItem>

    @Query("DELETE FROM world_clocks")
    suspend fun deleteAll()
    @Delete
    suspend fun deleteClock(alarm: WorldClockItem)
    @Update
    suspend fun updateClock(clock: WorldClockItem)

    @Query("SELECT * FROM world_clocks WHERE isSelected = 1 ORDER BY city ASC")
    fun getSelectedClocks(): LiveData<List<WorldClockItem>>
}
