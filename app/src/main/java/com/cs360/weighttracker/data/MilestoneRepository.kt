package com.cs360.weighttracker.data

import android.util.Log
import com.cs360.weighttracker.data.local.MilestonesDatabase
import com.cs360.weighttracker.data.local.MilestonesSharedPref
import com.cs360.weighttracker.data.local.models.DailyWeightEntity
import com.cs360.weighttracker.data.local.models.UserEntity
import com.cs360.weighttracker.domain.status.LoginStatus
import com.cs360.weighttracker.domain.status.RegisterStatus
import com.cs360.weighttracker.domain.IMilestoneRepository
import com.cs360.weighttracker.domain.models.DailyWeight
import com.cs360.weighttracker.domain.models.User
import com.cs360.weighttracker.domain.toDailyWeight
import com.cs360.weighttracker.domain.toUser
import com.cs360.weighttracker.presentation.utils.LogCategory
import com.cs360.weighttracker.presentation.utils.PasswordHasher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map


class MilestoneRepository(
    private val database: MilestonesDatabase,
    private val sharedPref: MilestonesSharedPref
) : IMilestoneRepository {

    ///------------------------
    ///       USER AUTH
    ///------------------------

    /**
     * Logs in the user with the given username and password.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return LoginStatus indicating whether the login was successful or not.
     */
    override suspend fun loginUser(username: String, password: String): LoginStatus {
        val hashedPassword = PasswordHasher.hash(password)
        val user = UserEntity(username, hashedPassword)
        try {
            // Try to validate the user to know if they exist in the database.
            var loggedInUser = database.userDao.validateUser(username, hashedPassword)
            if (loggedInUser != null) {
                // Store the user's current session
                sharedPref.currentUserId = loggedInUser.id
                return LoginStatus.SUCCESS
            } else {
                // Try to check whether the user exists but has an incorrect password
                loggedInUser = database.userDao.getUser(user.userName)
                return if (loggedInUser != null) {
                    // User exists but password is incorrect
                    LoginStatus.PASSWORD_ERROR
                } else {
                    // The user doesn't exist.
                    LoginStatus.USERNAME_ERROR
                }
            }
        } catch (e: Exception) {
            Log.e(LogCategory.REPOSITORY, "There was error logging the user in!\n" + e.message)
        }
        return LoginStatus.UNKNOWN_FAILURE
    }


    /**
     * Registers user with the given username and password.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return RegisterStatus indicating whether the registration was successful or not.
     */
    override suspend fun registerUser(username: String, password: String): RegisterStatus {
        val hashedPassword = PasswordHasher.hash(password)
        try {
            // Check whether the user exists in the database.
            val existingUser = database.userDao.getUser(username)
            if (existingUser != null) {
                return RegisterStatus.USER_EXISTS
            }
            // Register the user and save the user session to shared pref
            val newUser = UserEntity(username, hashedPassword)
            val userId = database.userDao.upsert(newUser)

            // If the insert was a success login in the user
            if (userId >= 0) {
                sharedPref.currentUserId = userId
                return RegisterStatus.SUCCESS
            } else { // Return error status indicating login error
                return RegisterStatus.UNKNOWN_FAILURE
            }
        } catch (e: Exception) {
            Log.e(LogCategory.REPOSITORY, "There was error registering the user!\n" + e.message)
        }

        return RegisterStatus.UNKNOWN_FAILURE
    }


    /**
     * Logs out the current user.
     */
    override fun logoutUser() {
        // Delete the user's session
        sharedPref.deleteCurrentUserId()
        // And clear user's preference as well
        sharedPref.deleteSMSSetting()
    }

    /**
     * Update the user's full name with the give name.
     *
     * @param fullName The name to update to.
     * @return Boolean indicating whether the update was successful or not.
     */
    override suspend fun updateCurrentUserFullName(fullName: String): Boolean {
        try {
            // Get the user logged-in user's id
            val currentUserId = sharedPref.currentUserId
            check(currentUserId >= 0) { "User not logged in!" }

            val user = database.userDao.getUser(currentUserId)

            // Retrieve the user object from database
            checkNotNull(user) { "User not found!" }

            // Update the user if user is logged in.
            return database.userDao.upsert(user.copy(fullName = fullName)) >= 0
        } catch (e: Exception) {
            Log.e(LogCategory.REPOSITORY, "There was an updating the user!\n" + e.message)
        }
        return false
    }


    /***
     * Retrieves the current logged-in user
     * @return The logged-in user if one exists else null.
     */
    override suspend fun getCurrentUser(): User? {
        try {
            // Get the user logged-in user's id
            val currentUserId = sharedPref.currentUserId
            if (currentUserId < 0)  // There is no current user
                return null
            return database.userDao.getUser(currentUserId)?.toUser()
        } catch (e: Exception) {
            Log.e(LogCategory.REPOSITORY, "There was an getting the user!\n" + e.message)
        }
        return null
    }


    ///------------------------
    ///      USER WEIGHT
    ///------------------------
    /**
     * Logs the user's daily weight.
     *
     * @param weight Today's weight to log.
     * @return A boolean whether the weight logging was successful.
     */
    override suspend fun logDailyWeight(weight: Float): Boolean {
        try {
            // Get the user logged-in user's id
            val currentUserId = sharedPref.currentUserId
            if (currentUserId < 0)  // There is no current user
                return false

            // Create a daily weight object
            val dailyWeight = DailyWeightEntity(weight, System.currentTimeMillis(), currentUserId)
            return database.dailyWeightDao.upsert(dailyWeight) >= 0
        } catch (e: Exception) {
            Log.e(LogCategory.REPOSITORY, "There was error logging the user weight!\n" + e.message)
        }
        return false
    }


    /**
     * Deletes the daily weight with the given id.
     *
     * @param id The id of the daily weight to delete.
     * @return A boolean indicating whether the weight was deleted.
     */
    override suspend fun deleteDailyWeight(id: Long): Boolean {
        try {
            return database.dailyWeightDao.delete(id) > 0
        } catch (e: Exception) {
            Log.e(LogCategory.REPOSITORY, "There was error logging the user weight!\n" + e.message)
        }
        return false
    }


    /**
     * Updates or inserts the user's current and goal weight.
     *
     * @param currentWeight The user's current weight.
     * @param goalWeight    The user's goal weight.
     * @return A boolean indicating whether the goal weight was upserted.
     */
    override suspend fun upsertGoalWeight(currentWeight: Float, goalWeight: Float): Boolean {
        try {
            // NOTE: Upsert logic is handled by the database so there is no need to check it here
            // Get the user logged-in user's id
            val currentUserId = sharedPref.currentUserId
            if (currentUserId < 0)  // There is no current user
                return false
            val user = database.userDao.getUser(currentUserId)
            checkNotNull(user) { "User not logged in!" }

            return database.userDao.upsert(
                user.copy(
                    currentWeight = currentWeight,
                    goalWeight = goalWeight
                )
            ) >= 0
        } catch (e: Exception) {
            Log.e(LogCategory.REPOSITORY, "There was error logging the user weight!\n" + e.message)
        }
        return false
    }


    /**
     * All the logged daily weights for the current user.
     *
     * @return A reactive flow of daily weights(List).
     */
    override val dailyWeights: Flow<List<DailyWeight>>
        get() {
            try {
                // Get the user logged-in user's id
                val currentUserId = sharedPref.currentUserId
                if (currentUserId < 0)  // There is no current user
                    return flow { listOf<DailyWeightEntity>() }
                return database.dailyWeightDao.getAllUserWeights(currentUserId)
                    .map { weightEntities -> weightEntities.map { weightEntity -> weightEntity.toDailyWeight() } }
            } catch (e: Exception) {
                Log.e(
                    LogCategory.REPOSITORY,
                    "There was an error retrieving user's daily weights.\n" + e.message
                )
            }
            return flow { listOf<DailyWeightEntity>() }
        }


    /**
     * Gets whether the user has a goal weight set.
     *
     * @param userId The current user's id.
     * @return A boolean whether the user has a goal weight.
     */
    override suspend fun userHasGoalSet(userId: Long): Boolean {
        try {
            val user = database.userDao.getUser(userId);
            return if (user == null) false else user.goalWeight >= 0.0f
        } catch (e: Exception) {
            Log.e(
                LogCategory.REPOSITORY,
                "There was error retrieving the user's goal weight!\n" + e.message
            )
        }
        return false
    }


    /**
     * Get the current user's latest logged weight.
     *
     * @return The current user's latest logged weight or null if the user has none.
     */
    override fun getLatestLoggedWeight(userId: Long): Flow<DailyWeight?> {
        try {
            // Get the user logged-in user's id
            val currentUserId = sharedPref.currentUserId
            if (currentUserId < 0)  // There is no current user
                return flow { }
            return database.dailyWeightDao.getLatestUserWeight(currentUserId)
                .map { it?.toDailyWeight() }
        } catch (e: Exception) {
            Log.e(
                LogCategory.REPOSITORY,
                "There was an error retrieving user's daily weight.\n" + e.message
            )
        }
        return flow { }

    }


    /**
     * Update the user's phone number.
     *
     * @return A boolean indicating whether the operation was successful.
     */
    override suspend fun setPhoneNumber(phoneNumber: String): Boolean {
        try {
            // Unsetting a phone number is not possible
            if (phoneNumber.isEmpty()) return false
            // Get the user logged-in user's id
            val currentUserId = sharedPref.currentUserId
            if (currentUserId < 0)  // There is no current user
                return false
            val currentUser = database.userDao.getUser(currentUserId) ?: return false
            return database.userDao.upsert(currentUser.copy(phoneNumber = phoneNumber)) > -1
        } catch (e: Exception) {
            Log.e(
                LogCategory.REPOSITORY,
                "There was an error retrieving user's daily weight.\n" + e.message
            )
        }
        return false
    }


    /**
     * The user's SMS notification setting.
     */
    override var userNotificationSetting: Boolean
        get() = sharedPref.smsSetting ?: false
        set(value: Boolean) {
            sharedPref.smsSetting = value
        }
}