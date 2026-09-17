package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ActivityDao
import com.example.data.dao.BreakDao
import com.example.data.dao.SessionDao
import com.example.data.dao.SettingsDao
import com.example.data.model.ActivityEntity
import com.example.data.model.BreakEntity
import com.example.data.model.SessionEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ActivityEntity::class,
        SessionEntity::class,
        BreakEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AscendDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun sessionDao(): SessionDao
    abstract fun breakDao(): BreakDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AscendDatabase? = null

        val DEFAULT_ACTIVITIES = listOf(
            ActivityEntity(
                id = "meditation",
                name = "Meditation",
                description = "Stillness, breath awareness & mindfulness",
                dailyGoalMinutes = 120, // 2 hours default
                isCore = true,
                contributesToMainSystem = true,
                colorHex = "#10B981", // Emerald
                iconName = "self_improvement",
                sortOrder = 0
            ),
            ActivityEntity(
                id = "study",
                name = "Study",
                description = "Deep intellectual immersion & skill mastery",
                dailyGoalMinutes = 180, // 3 hours default
                isCore = true,
                contributesToMainSystem = true,
                colorHex = "#38BDF8", // Cyan / Intellectual Blue
                iconName = "auto_stories",
                sortOrder = 1
            ),
            ActivityEntity(
                id = "workout",
                name = "Workout",
                description = "Physical discipline, strength & endurance",
                dailyGoalMinutes = 60, // 1 hour default
                isCore = true,
                contributesToMainSystem = true,
                colorHex = "#F97316", // Flame Orange
                iconName = "fitness_center",
                sortOrder = 2
            ),
            ActivityEntity(
                id = "sleep",
                name = "Sleep",
                description = "Circadian restoration & biological recovery",
                dailyGoalMinutes = 480, // 8 hours default
                isCore = true,
                contributesToMainSystem = true,
                colorHex = "#A855F7", // Moonlit Indigo / Purple
                iconName = "bedtime",
                sortOrder = 3
            )
        )

        fun getDatabase(context: Context, scope: CoroutineScope): AscendDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AscendDatabase::class.java,
                    "ascend_mastery.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AscendDatabase) {
            val activityDao = database.activityDao()
            val existing = activityDao.getAllActivities()
            if (existing.isEmpty()) {
                activityDao.insertActivities(DEFAULT_ACTIVITIES)
            }

            val settingsDao = database.settingsDao()
            val existingSettings = settingsDao.getSettings()
            if (existingSettings == null) {
                settingsDao.insertSettings(
                    UserSettingsEntity(
                        id = 1,
                        userName = "Kotresh",
                        isDarkMode = true,
                        bedtimeWindowStartMinutes = 22 * 60, // 10:00 PM
                        bedtimeWindowEndMinutes = 23 * 60 + 30, // 11:30 PM
                        wakeWindowStartMinutes = 5 * 60 + 30, // 5:30 AM
                        wakeWindowEndMinutes = 7 * 60 + 30, // 7:30 AM
                        notificationsEnabled = true
                    )
                )
            }
        }
    }
}
