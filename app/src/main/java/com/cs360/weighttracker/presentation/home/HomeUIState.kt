package com.cs360.weighttracker.presentation.home

sealed interface HomeUIState {
    object IsLoading : HomeUIState
    object WeightDeleted : HomeUIState

    // Error states
    object WeightTrackingError : HomeUIState
    object WeightDeleteError : HomeUIState

    // GoalWeightReached and WeightTracked events as mutually exclusive states.
    // They should NOT be triggered sequentially, as WeightTracked indicates
    // the user has finished tracking their weights and hence should only save the weight.
    // GoalWeightReached is also triggered after the same events(user saving the weight)
    // but requires the newly tracked weight to at least be equal to their goal weight.
    data class GoalWeightReached(val weight: Float, val sendSms: Boolean) : HomeUIState
    data class WeightTracked(val weight: Float) : HomeUIState
}