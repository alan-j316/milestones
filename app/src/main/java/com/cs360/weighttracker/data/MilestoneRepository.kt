package com.cs360.weighttracker.data

import android.content.Context
import android.util.Log
import com.cs360.weighttracker.data.local.MilestonesDatabase
import com.cs360.weighttracker.database.MilestoneDatabase
import com.cs360.weighttracker.database.MilestonePrefs
import com.cs360.weighttracker.database.MilestoneRepository
import com.cs360.weighttracker.database.status.LoginStatus
import com.cs360.weighttracker.database.status.RegisterStatus
import com.cs360.weighttracker.domain.models.DailyWeight
import com.cs360.weighttracker.domain.models.User
import com.cs360.weighttracker.utils.LogCategory
import com.cs360.weighttracker.utils.PasswordHasher
//
//
//class MilestoneRepository(
//    private val database: MilestonesDatabase,
//    private val sharedPref: Milestone
//) {
//    private val sharedPref: MilestonePrefs
//
//    init {
//        this.database = MilestoneDatabase(context)
//        this.sharedPref = MilestonePrefs(context)
//    }
//
//    /**////////////////////////
//    * USER AUTH       //
//    * / */
//    /**
//     * Logs in the user with the given username and password.
//     *
//     * @param username The username of the user.
//     * @param password The password of the user.
//     * @return LoginStatus indicating whether the login was successful or not.
//     */
//    fun loginUser(username: String, password: String): LoginStatus {
//        val hashedPassword = PasswordHasher.hash(password)
//        val user = User(username, hashedPassword)
//        try {
//            // Try to validate the user to know if they exist in the database.
//            var loggedInUser = database.validateUser(user)
//            if (loggedInUser != null && loggedInUser.userId != -1L) {
//                // Store the user's current session
//                sharedPref.setCurrentUserId(loggedInUser.userId)
//                return LoginStatus.SUCCESS
//            } else {
//                // Try to check whether the user exists but has an incorrect password
//                loggedInUser = database.getUser(user.userName)
//                if (loggedInUser != null) { // Indicates user exists but password is incorrect
//                    return LoginStatus.PASSWORD_ERROR
//                } else { // Else the user doesn't exist.
//                    return LoginStatus.USERNAME_ERROR
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(LogCategory.REPOSITORY, "There was error logging the user in!\n" + e.message)
//        }
//        return LoginStatus.UNKNOWN_FAILURE
//    }
//
//
//    /**
//     * Registers user with the given username and password.
//     *
//     * @param username The username of the user.
//     * @param password The password of the user.
//     * @return RegisterStatus indicating whether the registration was successful or not.
//     */
//    fun registerUser(username: String, password: String): RegisterStatus {
//        val hashedPassword = PasswordHasher.hash(password)
//        try {
//            // Check whether the user exists in the database.
//            val existingUser = database.getUser(username)
//            if (existingUser != null) {
//                return RegisterStatus.USER_EXISTS
//            }
//            // Register the user and save the user session to shared pref
//            val newUser = User(username, hashedPassword)
//            val userId = database.insertUser(newUser)
//
//            // If the insert was a success login in the user
//            if (userId != -1L) {
//                sharedPref.setCurrentUserId(userId)
//                return RegisterStatus.SUCCESS
//            } else { // Return error status indicating login error
//                return RegisterStatus.UNKNOWN_FAILURE
//            }
//        } catch (e: Exception) {
//            Log.e(LogCategory.REPOSITORY, "There was error registering the user!\n" + e.message)
//        }
//
//        return RegisterStatus.UNKNOWN_FAILURE
//    }
//
//    /**
//     * Logs out the current user.
//     */
//    fun logoutUser() {
//        // Delete the user's session
//        sharedPref.deleteCurrentUserId()
//        // And clear user's preference as well
//        sharedPref.deleteSMSSetting()
//    }
//
//    /**
//     * Update the user's full name with the give name.
//     *
//     * @param fullName The name to update to.
//     * @return Boolean indicating whether the update was successful or not.
//     */
//    fun updateCurrentUserFullName(fullName: String): Boolean {
//        try {
//            // Get the user logged-in user's id
//            val currentUserId = sharedPref.getCurrentUserId()
//            check(currentUserId != -1L) { "User not logged in!" }
//
//            val user = database.getUser(currentUserId)
//
//            // Retrieve the user object from database
//            checkNotNull(user) { "User not found!" }
//
//            // Update the user if user is logged in.
//            user.fullName = fullName
//            return database.updateUser(user)
//        } catch (e: Exception) {
//            Log.e(LogCategory.REPOSITORY, "There was an updating the user!\n" + e.message)
//        }
//        return false
//    }
//
//    val currentUser: User?
//        /***
//         * Retrieves the current logged-in user
//         * @return The logged-in user if one exists else null.
//         */
//        get() {
//            try {
//                // Get the user logged-in user's id
//                val currentUserId = sharedPref.getCurrentUserId()
//                if (currentUserId == -1L)  // There is no current user
//                    return null
//
//                return database.getUser(currentUserId)
//            } catch (e: Exception) {
//                Log.e(LogCategory.REPOSITORY, "There was an getting the user!\n" + e.message)
//            }
//            return null
//        }
//
//
//    /** ////////////////////////
//     * USER WEIGHT    ///
//     * /////////////////////// */
//    /**
//     * Logs the user's daily weight.
//     *
//     * @param weight Today's weight to log.
//     * @return A boolean whether the weight logging was successful.
//     */
//    fun logDailyWeight(weight: Float): Boolean {
//        try {
//            // Get the user logged-in user's id
//            val currentUserId = sharedPref.getCurrentUserId()
//            if (currentUserId == -1L)  // There is no current user
//                return false
//
//            // Create a daily weight object
//            val dailyWeight = DailyWeight(weight, System.currentTimeMillis())
//            return database.insertDailyWeight(currentUserId, dailyWeight) > 0
//        } catch (e: Exception) {
//            Log.e(LogCategory.REPOSITORY, "There was error logging the user weight!\n" + e.message)
//        }
//        return false
//    }
//
//
//    /**
//     * Deletes the daily weight with the given id.
//     *
//     * @param id The id of the daily weight to delete.
//     * @return A boolean indicating whether the weight was deleted.
//     */
//    fun deleteDailyWeight(id: Long): Boolean {
//        try {
//            return database.deleteDailyWeight(id)
//        } catch (e: Exception) {
//            Log.e(LogCategory.REPOSITORY, "There was error logging the user weight!\n" + e.message)
//        }
//        return false
//    }
//
//
//    /**
//     * Updates or inserts the user's current and goal weight.
//     *
//     * @param currentWeight The user's current weight.
//     * @param goalWeight    The user's goal weight.
//     * @return A boolean indicating whether the goal weight was upserted.
//     */
//    fun upsertGoalWeight(currentWeight: Float, goalWeight: Float): Boolean {
//        try {
//            // NOTE: Upsert logic is handled by the database so there is no need to check it here
//            // Get the user logged-in user's id
//            val currentUserId = sharedPref.getCurrentUserId()
//            if (currentUserId == -1L)  // There is no current user
//                return false
//            val gWeight: GoalWeight = GoalWeight(currentWeight, goalWeight)
//            return database.upsertGoalWeight(currentUserId, gWeight)
//        } catch (e: Exception) {
//            Log.e(LogCategory.REPOSITORY, "There was error logging the user weight!\n" + e.message)
//        }
//        return false
//    }
//
//
//    val dailyWeights: MutableList<DailyWeight?>?
//        /**
//         * Get all the logged daily weights for the currently logged-in user.
//         *
//         * @return List of daily weights or an empty list if an error occurs of if the user has no daily weight logged.
//         */
//        get() {
//            try {
//                // Get the user logged-in user's id
//                val currentUserId = sharedPref.getCurrentUserId()
//                if (currentUserId == -1L)  // There is no current user
//                    return mutableListOf<DailyWeight?>()
//                return database.getDailyWeights(currentUserId)
//            } catch (e: Exception) {
//                Log.e(
//                    LogCategory.REPOSITORY,
//                    "There was an error retrieving user's daily weights.\n" + e.message
//                )
//            }
//            return mutableListOf<DailyWeight?>()
//        }
//
//
//    /**
//     * Gets whether the user has a goal weight set.
//     *
//     * @param userId The current user's id.
//     * @return A boolean whether the user has a goal weight.
//     */
//    fun userHasGoalSet(userId: Long): Boolean {
//        try {
//            return database.getGoalWeight(userId) != null
//        } catch (e: Exception) {
//            Log.e(
//                LogCategory.REPOSITORY,
//                "There was error retrieving the user's goal weight!\n" + e.message
//            )
//        }
//        return false
//    }
//
//
//    val userGoalWeight: GoalWeight?
//        /**
//         * Get the current user's goal weight.
//         *
//         * @return The current user's goal weight or null if the user has none.
//         */
//        get() {
//            try {
//                // Get the user logged-in user's id
//                val currentUserId = sharedPref.getCurrentUserId()
//                if (currentUserId == -1L)  // There is no current user
//                    return null
//                return database.getGoalWeight(currentUserId)
//            } catch (e: Exception) {
//                Log.e(
//                    LogCategory.REPOSITORY,
//                    "There was an error retrieving user's goal weight.\n" + e.message
//                )
//            }
//            return null
//        }
//
//
//    val userLatestLoggedWeight: DailyWeight?
//        /**
//         * Get the current user's latest logged weight.
//         *
//         * @return The current user's latest logged weight or null if the user has none.
//         */
//        get() {
//            try {
//                // Get the user logged-in user's id
//                val currentUserId = sharedPref.getCurrentUserId()
//                if (currentUserId == -1L)  // There is no current user
//                    return null
//                return database.getLatestDailyWeight(currentUserId)
//            } catch (e: Exception) {
//                Log.e(
//                    LogCategory.REPOSITORY,
//                    "There was an error retrieving user's daily weight.\n" + e.message
//                )
//            }
//            return null
//        }
//
//
//    /**
//     * Update the user's phone number.
//     *
//     * @return A boolean indicating whether the operation was successful.
//     */
//    fun setPhoneNumber(phoneNumber: String): Boolean {
//        try {
//            // Unsetting a phone number is not possible
//            if (phoneNumber.isEmpty()) return false
//            // Get the user logged-in user's id
//            val currentUserId = sharedPref.getCurrentUserId()
//            if (currentUserId == -1L)  // There is no current user
//                return false
//            val currentUser = database.getUser(currentUserId)
//            currentUser.phoneNumber = phoneNumber
//            return database.updateUser(currentUser)
//        } catch (e: Exception) {
//            Log.e(
//                LogCategory.REPOSITORY,
//                "There was an error retrieving user's daily weight.\n" + e.message
//            )
//        }
//        return false
//    }
//
//
//    val userNotificationSetting: Boolean
//        /**
//         * Return the user's current sms setting.
//         */
//        get() {
//            try {
//                return sharedPref.getSMSSetting()
//            } catch (e: Exception) {
//                Log.e(
//                    LogCategory.REPOSITORY,
//                    "There was an error getting notification preference.\n" + e.message
//                )
//            }
//            // Always return false if the setting has not been applied.
//            return false
//        }
//
//
//    /**
//     * Set the user's current notification setting
//     *
//     * @param setting The notification setting flag to set.
//     */
//    fun setUserNotificationSetting(setting: Boolean): Boolean {
//        try {
//            sharedPref.setSMSSetting(setting)
//            return setting // Return the setting if it has been applied.
//        } catch (e: Exception) {
//            Log.e(
//                LogCategory.REPOSITORY,
//                "There was an error setting notification preference.\n" + e.message
//            )
//        }
//        // Always return false if the setting has not been applied.
//        return false
//    }
//
//
//    companion object {
//        private var instance: MilestoneRepository? = null
//
//        /**
//         * Get a singleton instance of the repository.
//         *
//         * @param context The android context.
//         * @return A repository instance.
//         */
//        @Synchronized
//        fun getInstance(context: Context): MilestoneRepository {
//            if (MilestoneRepository.Companion.instance == null) {
//                MilestoneRepository.Companion.instance = MilestoneRepository(context)
//            }
//            return MilestoneRepository.Companion.instance
//        }
//    }
//}