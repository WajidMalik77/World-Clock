package com.worldclock.app_themes.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WorldClockItem::class,
        AlarmEntity::class,
        WidgetClockItem::class,
        ReminderEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WorldClockDatabase : RoomDatabase() {
    abstract fun worldClockDao(): WorldClockDao
    abstract fun alarmDao(): AlarmDao
    abstract fun widgetClockDao(): WidgetClockDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: WorldClockDatabase? = null

        fun getDatabase(context: Context): WorldClockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorldClockDatabase::class.java,
                    "world_clock_db1"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // ← add this migration
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS reminders (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL DEFAULT '',
                name TEXT NOT NULL DEFAULT '',
                startDate TEXT NOT NULL DEFAULT '',
                endDate TEXT NOT NULL DEFAULT '',
                hour INTEGER NOT NULL DEFAULT 12,
                minute INTEGER NOT NULL DEFAULT 0,
                isAm INTEGER NOT NULL DEFAULT 1,
                sound TEXT NOT NULL DEFAULT 'Digital Alarm',
                soundUri TEXT NOT NULL DEFAULT '',
                vibration TEXT NOT NULL DEFAULT 'None',
                snooze TEXT NOT NULL DEFAULT '5min',
                repeatDays TEXT NOT NULL DEFAULT '',
                isEnabled INTEGER NOT NULL DEFAULT 1,
                categoryId INTEGER NOT NULL DEFAULT -1,
                categoryTitle TEXT NOT NULL DEFAULT ''
            )
        """.trimIndent()
                )
            }
        }
    }
}
