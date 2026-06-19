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
    version = 4,
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                normalizeAlarmsTable(database)
                normalizeRemindersTable(database)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                normalizeAlarmsTable(database)
                normalizeRemindersTable(database)
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                normalizeAlarmsTable(database)
                normalizeRemindersTable(database)
            }
        }

        private fun normalizeAlarmsTable(database: SupportSQLiteDatabase) {
            val columns = getColumnNames(database, "alarms")
            database.execSQL("DROP TABLE IF EXISTS alarms_new")
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS alarms_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    label TEXT NOT NULL,
                    hour INTEGER NOT NULL,
                    minute INTEGER NOT NULL,
                    isEnabled INTEGER NOT NULL,
                    repeatDays TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    vibrate INTEGER NOT NULL,
                    alarmSound TEXT NOT NULL,
                    snoozeMinutes INTEGER NOT NULL
                )
                """.trimIndent()
            )

            if (columns.isNotEmpty()) {
                database.execSQL(
                    """
                    INSERT INTO alarms_new (
                        id,
                        label,
                        hour,
                        minute,
                        isEnabled,
                        repeatDays,
                        createdAt,
                        vibrate,
                        alarmSound,
                        snoozeMinutes
                    )
                    SELECT
                        ${columnOrNull(columns, "id")},
                        ${textColumnOrDefault(columns, "label", "")},
                        ${intColumnOrDefault(columns, "hour", 12)},
                        ${intColumnOrDefault(columns, "minute", 0)},
                        ${intColumnOrDefault(columns, "isEnabled", 1)},
                        ${textColumnOrDefault(columns, "repeatDays", "")},
                        ${intColumnOrDefault(columns, "createdAt", "CAST(strftime('%s','now') AS INTEGER) * 1000")},
                        ${intColumnOrDefault(columns, "vibrate", 1)},
                        ${textColumnOrDefault(columns, "alarmSound", "Radar (Default)")},
                        ${intColumnOrDefault(columns, "snoozeMinutes", 5)}
                    FROM alarms
                    """.trimIndent()
                )
            }

            database.execSQL("DROP TABLE IF EXISTS alarms")
            database.execSQL("ALTER TABLE alarms_new RENAME TO alarms")
        }

        private fun normalizeRemindersTable(database: SupportSQLiteDatabase) {
            val columns = getColumnNames(database, "reminders")
            database.execSQL("DROP TABLE IF EXISTS reminders_new")
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS reminders_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    name TEXT NOT NULL,
                    startDate TEXT NOT NULL,
                    endDate TEXT NOT NULL,
                    hour INTEGER NOT NULL,
                    minute INTEGER NOT NULL,
                    isAm INTEGER NOT NULL,
                    sound TEXT NOT NULL,
                    soundUri TEXT NOT NULL,
                    vibration TEXT NOT NULL,
                    snooze TEXT NOT NULL,
                    repeatDays TEXT NOT NULL,
                    isEnabled INTEGER NOT NULL,
                    categoryId INTEGER NOT NULL,
                    categoryTitle TEXT NOT NULL
                )
                """.trimIndent()
            )

            if (columns.isNotEmpty()) {
                database.execSQL(
                    """
                    INSERT INTO reminders_new (
                        id,
                        title,
                        name,
                        startDate,
                        endDate,
                        hour,
                        minute,
                        isAm,
                        sound,
                        soundUri,
                        vibration,
                        snooze,
                        repeatDays,
                        isEnabled,
                        categoryId,
                        categoryTitle
                    )
                    SELECT
                        ${columnOrNull(columns, "id")},
                        ${textColumnOrDefault(columns, "title", "")},
                        ${textColumnOrDefault(columns, "name", "")},
                        ${textColumnOrDefault(columns, "startDate", "")},
                        ${textColumnOrDefault(columns, "endDate", "")},
                        ${intColumnOrDefault(columns, "hour", 12)},
                        ${intColumnOrDefault(columns, "minute", 0)},
                        ${intColumnOrDefault(columns, "isAm", 1)},
                        ${textColumnOrDefault(columns, "sound", "Default")},
                        ${textColumnOrDefault(columns, "soundUri", "")},
                        ${textColumnOrDefault(columns, "vibration", "None")},
                        ${textColumnOrDefault(columns, "snooze", "5min")},
                        ${textColumnOrDefault(columns, "repeatDays", "")},
                        ${intColumnOrDefault(columns, "isEnabled", 1)},
                        ${intColumnOrDefault(columns, "categoryId", -1)},
                        ${textColumnOrDefault(columns, "categoryTitle", "")}
                    FROM reminders
                    """.trimIndent()
                )
            }

            database.execSQL("DROP TABLE IF EXISTS reminders")
            database.execSQL("ALTER TABLE reminders_new RENAME TO reminders")
        }

        private fun getColumnNames(
            database: SupportSQLiteDatabase,
            tableName: String
        ): Set<String> {
            val columns = mutableSetOf<String>()
            database.query("PRAGMA table_info(`$tableName`)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    columns += cursor.getString(nameIndex)
                }
            }
            return columns
        }

        private fun columnOrNull(columns: Set<String>, name: String): String {
            return if (name in columns) name else "NULL"
        }

        private fun textColumnOrDefault(
            columns: Set<String>,
            name: String,
            defaultValue: String
        ): String {
            return if (name in columns) {
                "COALESCE($name, ${sqlString(defaultValue)})"
            } else {
                sqlString(defaultValue)
            }
        }

        private fun intColumnOrDefault(
            columns: Set<String>,
            name: String,
            defaultValue: Int
        ): String {
            return intColumnOrDefault(columns, name, defaultValue.toString())
        }

        private fun intColumnOrDefault(
            columns: Set<String>,
            name: String,
            defaultExpression: String
        ): String {
            return if (name in columns) "COALESCE($name, $defaultExpression)" else defaultExpression
        }

        private fun sqlString(value: String): String {
            return "'${value.replace("'", "''")}'"
        }
    }
}
