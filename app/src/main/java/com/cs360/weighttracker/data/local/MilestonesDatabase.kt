package com.cs360.weighttracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cs360.weighttracker.data.local.dao.DailyWeightDao
import com.cs360.weighttracker.data.local.dao.GoalWeightDao
import com.cs360.weighttracker.data.local.dao.UserDao
import com.cs360.weighttracker.data.local.models.DailyWeightEntity
import com.cs360.weighttracker.data.local.models.UserEntity

@Database(
    entities = [DailyWeightEntity::class, GoalWeightEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MilestonesDatabase : RoomDatabase() {

    abstract val userDao: UserDao
    abstract val dailyWeightDao: DailyWeightDao
    abstract val goalWeightDao: GoalWeightDao

    companion object {
        fun getInstance(): RoomDatabase {

        }
    }
}