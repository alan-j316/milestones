package com.cs360.weighttracker.data.local.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DailyWeightEntity(
    val weight: Float,
    val dateTimeMillis: Long,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)