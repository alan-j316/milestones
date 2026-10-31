package com.cs360.weighttracker.data.local.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "dailyWeights", foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DailyWeightEntity(
    val weight: Float,
    val dateTimeMillis: Long,
    val userId: Long,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)