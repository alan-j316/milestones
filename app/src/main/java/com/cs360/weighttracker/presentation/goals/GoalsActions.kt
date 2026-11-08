package com.cs360.weighttracker.presentation.goals

sealed interface GoalsActions {
    data class Save(
        val fullName: String,
        val currentWeight: Float,
        val goalWeight: Float
    ) : GoalsActions
}