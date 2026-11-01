package com.cs360.weighttracker.presentation.home

import com.cs360.weighttracker.domain.models.User

sealed interface HomeUIState {
    object IsLoading : HomeUIState
    data class InitialDataLoaded(
        val user: User
    ) : HomeUIState

    data class GoalWeightReached(val fullName: String, val goalWeight: Float) : HomeUIState
    data class TrackWeight(val weight: Float)
}