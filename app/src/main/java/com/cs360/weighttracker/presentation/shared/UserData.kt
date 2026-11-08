package com.cs360.weighttracker.presentation.shared

import com.cs360.weighttracker.domain.models.DailyWeight
import com.cs360.weighttracker.domain.models.User

data class UserData(
    val user: User? = null,
    val latestWeight: DailyWeight? = null
)