package com.cs360.weighttracker.data.local.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntity(
    val userName: String,
    val passwordHash: String,
    val goalWeight: Float = 0.0f,
    val currentWeight: Float = 0.0f,
    val fullName: String = "",
    val phoneNumber: String = "",
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)