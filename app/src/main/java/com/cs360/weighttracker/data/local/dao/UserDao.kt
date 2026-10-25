package com.cs360.weighttracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.cs360.weighttracker.data.local.models.UserEntity


@Dao
interface UserDao {

    @Insert(onConflict= OnConflictStrategy.IGNORE)
    suspend fun upsert(user: UserEntity)

    @Query("SELECT * FROM UserEntity WHERE id = :userId")
    suspend fun getUser(userId: Long)

    @Delete
    suspend fun delete(user: UserEntity)
}