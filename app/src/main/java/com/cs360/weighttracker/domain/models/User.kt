package com.cs360.weighttracker.domain.models

/**
 * Model representing a user.
 * 
 * @apiNote Password is stored as a hash and is immutable after creation.
 */
data class User(
    val userName: String,
    val passwordHash: String,
    val fullName: String = "",
    val goalWeight: Float = 0.0f,
    val currentWeight: Float = 0.0f,
    val phoneNumber: String = "",
    var userId: Long,
) {

    /**
     * Returns whether this user has a full name.
     */
    fun hasFullName(): Boolean = fullName.isNotBlank();

    /**
     * Returns whether this user has a phone number.
     */
    fun hasPhoneNumber(): Boolean = phoneNumber.isNotBlank();

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