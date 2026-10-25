package com.cs360.weighttracker.models


/**
 * Encapsulation of user's goal and target weight.
 *
 * @param currentWeight The user's current weight.
 * @param goalWeight    The user's goal weight.
 * @param id            The unique identifier for the goal.
 */
data class GoalWeight(
    val currentWeight: Float,
    val goalWeight: Float,
    val id: Long = -1
) {

    /**
     * Returns user goals given the current goal and target weights.
     */
    fun getGoalType(): GoalType {
        return if (goalWeight > currentWeight) {
            GoalType.WEIGHT_GAIN
        } else if (goalWeight < currentWeight) {
            GoalType.WEIGHT_LOSS
        } else {
            GoalType.WEIGHT_MAINTAIN
        }
    }
}