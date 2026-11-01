package com.cs360.weighttracker.domain.models

import com.cs360.weighttracker.data.local.models.DailyWeightEntity
import com.cs360.weighttracker.data.local.models.UserEntity
import com.cs360.weighttracker.data.local.status.LoginStatus
import com.cs360.weighttracker.data.local.status.RegisterStatus
import kotlinx.coroutines.flow.Flow

interface IMilestoneRepository {
    /**
     * All the logged daily weights for the current user.
     *
     * @return A reactive flow of daily weights(List).
     */
    val dailyWeights: Flow<List<DailyWeightEntity>>

    /**
     * The user's SMS notification setting.
     */
    var userNotificationSetting: Boolean

    /**
     * Logs in the user with the given username and password.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return LoginStatus indicating whether the login was successful or not.
     */
    suspend fun loginUser(username: String, password: String): LoginStatus

    /**
     * Registers user with the given username and password.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return RegisterStatus indicating whether the registration was successful or not.
     */
    suspend fun registerUser(username: String, password: String): RegisterStatus

    /**
     * Logs out the current user.
     */
    fun logoutUser()

    /**
     * Update the user's full name with the give name.
     *
     * @param fullName The name to update to.
     * @return Boolean indicating whether the update was successful or not.
     */
    suspend fun updateCurrentUserFullName(fullName: String): Boolean

    /***
     * Retrieves the current logged-in user
     * @return The logged-in user if one exists else null.
     */
    suspend fun getCurrentUser(): UserEntity?

    /**
     * Logs the user's daily weight.
     *
     * @param weight Today's weight to log.
     * @return A boolean whether the weight logging was successful.
     */
    suspend fun logDailyWeight(weight: Float): Boolean

    /**
     * Deletes the daily weight with the given id.
     *
     * @param id The id of the daily weight to delete.
     * @return A boolean indicating whether the weight was deleted.
     */
    suspend fun deleteDailyWeight(id: Long): Boolean

    /**
     * Updates or inserts the user's current and goal weight.
     *
     * @param currentWeight The user's current weight.
     * @param goalWeight    The user's goal weight.
     * @return A boolean indicating whether the goal weight was upserted.
     */
    suspend fun upsertGoalWeight(currentWeight: Float, goalWeight: Float): Boolean

    /**
     * Gets whether the user has a goal weight set.
     *
     * @param userId The current user's id.
     * @return A boolean whether the user has a goal weight.
     */
    suspend fun userHasGoalSet(userId: Long): Boolean

    /**
     * Get the current user's latest logged weight.
     *
     * @return The current user's latest logged weight or null if the user has none.
     */
    suspend fun getLatestLoggedWeight(userId: Long): DailyWeightEntity?

    /**
     * Update the user's phone number.
     *
     * @return A boolean indicating whether the operation was successful.
     */
    suspend fun setPhoneNumber(phoneNumber: String): Boolean
}