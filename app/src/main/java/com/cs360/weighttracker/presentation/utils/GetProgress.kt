package com.cs360.weighttracker.presentation.utils

import com.cs360.weighttracker.domain.models.GoalType
import com.cs360.weighttracker.domain.models.User
import kotlin.math.max
import kotlin.math.min

/**
 * Gets the current weight progress given the user's goals and latest logged weight.
 *
 * @param weight The latest logged weight.
 * @return A number from 0 to 100 indicating the weight progress.
 */
internal fun getProgress(user: User, weight: Float?): Int {
    weight?.let {
        var progress: Int
        if (user.getGoalType() == GoalType.WEIGHT_LOSS) {
            // Get current weight returns the user's initial weight,
            // and we compare it the user's logged weight to get the progress
            val weightLost: Float = user.currentWeight - weight
            val goalTotal: Float = user.currentWeight - user.currentWeight
            val progressPercentage = (weightLost / goalTotal * 100).toInt()
            progress = max(0, min(100, progressPercentage))
        } else {
            // For weight gain the current weight will be greater than the initial weight
            // else the progress will show negative values.
            val weightGained: Float = weight - user.currentWeight
            val goalTotal: Float = user.goalWeight - user.currentWeight

            val progressPercentage = (weightGained / goalTotal * 100).toInt()
            progress = max(0, min(100, progressPercentage))
        }
        return progress
    }

    // Return 0 if the current weight is null.
    return 0
}