package com.cs360.weighttracker.domain

import com.cs360.weighttracker.data.local.models.DailyWeightEntity
import com.cs360.weighttracker.data.local.models.UserEntity
import com.cs360.weighttracker.domain.models.DailyWeight
import com.cs360.weighttracker.domain.models.User


/**
 * Map a daily weight entity to a domain level object.
 */
fun DailyWeightEntity.toDailyWeight(): DailyWeight {
    return DailyWeight(
        weight = weight,
        dateTimeMillis = dateTimeMillis,
        id = id,
    )
}


/**
 * Map a UserEntity(Data Object) to a Domain Level object.
 */
fun UserEntity.toUser(): User {
    return User(
        userName = userName,
        passwordHash = passwordHash,
        fullName = fullName,
        goalWeight = goalWeight,
        currentWeight = currentWeight,
        phoneNumber = phoneNumber,
        userId = id
    )
}