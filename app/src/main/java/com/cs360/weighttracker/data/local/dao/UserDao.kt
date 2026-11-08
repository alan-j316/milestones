package com.cs360.weighttracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cs360.weighttracker.data.local.models.UserEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity): Int

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUser(userId: Long): UserEntity?

    // Get the latest instance of the user as a flow
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserLatest(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE userName = :userName AND passwordHash = :passwordHash LIMIT 1")
    suspend fun validateUser(userName: String, passwordHash: String): UserEntity?

    @Query("SELECT * FROM users WHERE userName = :userName LIMIT 1")
    suspend fun getUser(userName: String): UserEntity?

    @Delete
    suspend fun delete(user: UserEntity)
}