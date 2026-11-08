package com.cs360.weighttracker.data.local.models

import androidx.room.Embedded
import androidx.room.Relation
import com.cs360.weighttracker.domain.models.DailyWeight


data class UserWithDailyWeights(
    @Embedded
    val user: UserEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val dailyWeights: List<DailyWeight>
)