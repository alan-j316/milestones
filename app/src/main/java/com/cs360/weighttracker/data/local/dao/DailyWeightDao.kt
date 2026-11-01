package com.cs360.weighttracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cs360.weighttracker.data.local.models.DailyWeightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyWeightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(weight: DailyWeightEntity): Long

    @Query("DELETE FROM dailyWeights WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("SELECT * FROM dailyWeights WHERE userId = :userId")
    fun getAllUserWeights(userId: Long): Flow<List<DailyWeightEntity>>

    @Query("SELECT * FROM dailyWeights WHERE userId = :userId ORDER BY dateTimeMillis DESC LIMIT 1")
    fun getLatestUserWeight(userId: Long): Flow<DailyWeightEntity?>
}