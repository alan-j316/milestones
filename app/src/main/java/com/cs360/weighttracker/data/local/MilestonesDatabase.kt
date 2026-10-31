package com.cs360.weighttracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cs360.weighttracker.data.local.dao.DailyWeightDao
import com.cs360.weighttracker.data.local.dao.UserDao
import com.cs360.weighttracker.data.local.models.DailyWeightEntity
import com.cs360.weighttracker.data.local.models.UserEntity

@Database(
    entities = [DailyWeightEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MilestonesDatabase : RoomDatabase() {

    abstract val userDao: UserDao
    abstract val dailyWeightDao: DailyWeightDao

    companion object {
        @Volatile
        private var INSTANCE: MilestonesDatabase? = null;

        /// Get the current Room Database instance.
        fun getInstance(ctx: Context): RoomDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    ctx,
                    klass = MilestonesDatabase::class.java,
                    name = "milestones.db",
                ).build().also { INSTANCE = it; }
            }
        }
    }
}