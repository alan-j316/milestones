package com.cs360.weighttracker.data.local.models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class GoalWeightEntity(
    val currentWeight: Float,
    val goalWeight: Float,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)