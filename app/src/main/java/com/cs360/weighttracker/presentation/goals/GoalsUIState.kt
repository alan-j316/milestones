package com.cs360.weighttracker.presentation.goals

sealed interface GoalsUIState {
    object IsLoading : GoalsUIState
    object SaveSuccess : GoalsUIState
    object SaveError : GoalsUIState
    object GoalWeighError : GoalsUIState
    object FullNameError : GoalsUIState
    object CurrentWeightError : GoalsUIState
    data class InitialDataLoaded(
        val fullName: String,
        val currentWeight: Float,
        val goalWeight: Float
    ) : GoalsUIState
}