package com.cs360.weighttracker.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cs360.weighttracker.domain.status.LoginStatus;
import com.cs360.weighttracker.domain.status.RegisterStatus;
import com.cs360.weighttracker.domain.models.DailyWeight;
import com.cs360.weighttracker.domain.models.User;
import com.cs360.weighttracker.presentation.utils.LogCategory;
import com.cs360.weighttracker.presentation.utils.PasswordHasher;

import java.util.Collections;
import java.util.List;

public class MilestoneRepository {

    private static MilestoneRepository instance;

    private final MilestoneDatabase database;
    private final MilestonePrefs sharedPref;

    private MilestoneRepository(Context context) {
        this.database = new MilestoneDatabase(context);
        this.sharedPref = new MilestonePrefs(context);
    }

    /**
     * Get a singleton instance of the repository.
     *
     * @param context The android context.
     * @return A repository instance.
     */
    public static synchronized MilestoneRepository getInstance(Context context) {
        if (instance == null) {
            instance = new MilestoneRepository(context);
        }
        return instance;
    }


    ///////////////////////////
    ///     USER AUTH       //
    /////////////////////////

    /**
     * Logs in the user with the given username and password.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return LoginStatus indicating whether the login was successful or not.
     */
    public LoginStatus loginUser(@NonNull String username, @NonNull String password) {
        String hashedPassword = PasswordHasher.hash(password);
        User user = new User(username, hashedPassword);
        try {
            // Try to validate the user to know if they exist in the database.
            User loggedInUser = database.validateUser(user);
            if (loggedInUser != null && loggedInUser.userId != -1) {
                // Store the user's current session
                sharedPref.setCurrentUserId(loggedInUser.userId);
                return LoginStatus.SUCCESS;
            } else {
                // Try to check whether the user exists but has an incorrect password
                loggedInUser = database.getUser(user.getUserName());
                if (loggedInUser != null) { // Indicates user exists but password is incorrect
                    return LoginStatus.PASSWORD_ERROR;
                } else { // Else the user doesn't exist.
                    return LoginStatus.USERNAME_ERROR;
                }
            }
        } catch (Exception e) {
            Log.e(LogCategory.REPOSITORY, "There was error logging the user in!\n" + e.getMessage());
        }
        return LoginStatus.UNKNOWN_FAILURE;
    }


    /**
     * Registers user with the given username and password.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return RegisterStatus indicating whether the registration was successful or not.
     */
    public RegisterStatus registerUser(@NonNull String username, @NonNull String password) {
        String hashedPassword = PasswordHasher.hash(password);
        try {
            // Check whether the user exists in the database.
            User existingUser = database.getUser(username);
            if (existingUser != null) {
                return RegisterStatus.USER_EXISTS;
            }
            // Register the user and save the user session to shared pref
            User newUser = new User(username, hashedPassword);
            long userId = database.insertUser(newUser);

            // If the insert was a success login in the user
            if (userId != -1) {
                sharedPref.setCurrentUserId(userId);
                return RegisterStatus.SUCCESS;
            } else { // Return error status indicating login error
                return RegisterStatus.UNKNOWN_FAILURE;
            }
        } catch (Exception e) {
            Log.e(LogCategory.REPOSITORY, "There was error registering the user!\n" + e.getMessage());
        }

        return RegisterStatus.UNKNOWN_FAILURE;
    }

    /**
     * Logs out the current user.
     */
    public void logoutUser() {
        // Delete the user's session
        sharedPref.deleteCurrentUserId();
        // And clear user's preference as well
        sharedPref.deleteSMSSetting();
    }

    /**
     * Update the user's full name with the give name.
     *
     * @param fullName The name to update to.
     * @return Boolean indicating whether the update was successful or not.
     */
    public boolean updateCurrentUserFullName(@NonNull String fullName) {
        try {
            // Get the user logged-in user's id
            long currentUserId = sharedPref.getCurrentUserId();
            if (currentUserId == -1) // There is no current user
                throw new IllegalStateException("User not logged in!");

            User user = database.getUser(currentUserId);

            // Retrieve the user object from database
            if (user == null)
                throw new IllegalStateException("User not found!");

            // Update the user if user is logged in.
            user.fullName = fullName;
            return database.updateUser(user);
        } catch (Exception e) {
            Log.e(LogCategory.REPOSITORY, "There was an updating the user!\n" + e.getMessage());
        }
        return false;
    }

    /***
     * Retrieves the current logged-in user
     * @return The logged-in user if one exists else null.
     */
    public User getCurrentUser() {
        try {
            // Get the user logged-in user's id
            long currentUserId = sharedPref.getCurrentUserId();
            if (currentUserId == -1) // There is no current user
                return null;

            return database.getUser(currentUserId);
        } catch (Exception e) {
            Log.e(LogCategory.REPOSITORY, "There was an getting the user!\n" + e.getMessage());
        }
        return null;
    }

    /// ////////////////////////
    ///     USER WEIGHT    ///
    /// ///////////////////////


    /**
     * Logs the user's daily weight.
     *
     * @param weight Today's weight to log.
     * @return A boolean whether the weight logging was successful.
     */
    public boolean logDailyWeight(float weight) {
        try {
            // Get the user logged-in user's id
            long currentUserId = sharedPref.getCurrentUserId();
            if (currentUserId == -1) // There is no current user
                return false;

            // Create a daily weight object
            DailyWeight dailyWeight = new DailyWeight(weight, System.currentTimeMillis());
            return database.insertDailyWeight(currentUserId, dailyWeight) > 0;
        } catch (Exception e) {
            Log.e(LogCategory.REPOSITORY, "There was error logging the user weight!\n" + e.getMessage());
        }
        return false;
    }


    /**
     * Deletes the daily weight with the given id.
     *
     * @param id The id of the daily weight to delete.
     * @return A boolean indicating whether the weight was deleted.
     */
    public boolean deleteDailyWeight(long id) {
        try {
            return database.deleteDailyWeight(id);
        } catch (Exception e) {
            Log.e(LogCategory.REPOSITORY, "There was error logging the user weight!\n" + e.getMessage());
        }
        return false;
    }


    /**
     * Updates or inserts the user's current and goal weight.
     *
     * @param currentWeight The user's current weight.
     * @param goalWeight    The user's goal weight.
     * @return A boolean indicating whether the goal weight was upserted.
     */
    public boolean upsertGoalWeight(float currentWeight, float goalWeight) {
        try {
            // NOTE: Upsert logic is handled by the database so there is no need to check it here
            // Get the user logged-in user's id
            long currentUserId = sharedPref.getCurrentUserId();
            if (currentUserId == -1) // There is no current user
                return false;
            GoalWeight gWeight = new GoalWeight(currentWeight, goalWeight);
            return database.upsertGoalWeight(currentUserId, gWeight);
        } catch (Exception e) {
            Log.e(LogCategory.REPOSITORY, "There was error logging the user weight!\n" + e.getMessage());
        }
        return false;
    }


    /**
     * Get all the logged daily weights for the currently logged-in user.
     *
     * @return List of daily weights or an empty list if an error occurs of if the user has no daily weight logged.
     */
    public List<DailyWeight> getDailyWeights() {
        try {
            // Get the user logged-in user's id
            long currentUserId = sharedPref.getCurrentUserId();
            if (currentUserId == -1) // There is no current user
                return Collections.emptyList();
            return database.getDailyWeights(currentUserId);
        } catch (Exception e) {
            Log.e(LogCategory.REPOSITORY, "There was an error retrieving user's daily weights.\n" + e.getMessage());
        }
        return Collections.emptyList();
    }


    /**
     * Gets whether the user has a goal weight set.
     *
     * @param userId The current user's id.
     * @return A boolean whether the user has a goal weight.
     */
    public boolean userHasGoalSet(long userId) {
        try {
            return database.getGoalWeight(userId) != null;
        } catch (Exception e) {
            Log.e(LogCategory.REPOSITORY, "There was error retrieving the user's goal weight!\n" + e.getMessage());
        }
        return false;
    }


    /**
     * Get the current user's goal weight.
     *
     * @return The current user's goal weight or null if the user has none.
     */
    public GoalWeight getUserGoalWeight() {
        try {
            // Get the user logged-in user's id
            long currentUserId = sharedPref.getCurrentUserId();
            if (currentUserId == -1) // There is no current user
                return null;
            return database.getGoalWeight(currentUserId);
        } catch (Exception e) {
            Log.e(LogCategory.REPOSITORY, "There was an error retrieving user's goal weight.\n" + e.getMessage());
        }
        return null;
    }


    /**
     * Get the current user's latest logged weight.
     *
     * @return The current user's latest logged weight or null if the user has none.
     */
    public DailyWeight getUserLatestLoggedWeight() {
        try {
            // Get the user logged-in user's id
            long currentUserId = sharedPref.getCurrentUserId();
            if (currentUserId == -1) // There is no current user
                return null;
            return database.getLatestDailyWeight(currentUserId);
        } catch (Exception e) {
            Log.e(LogCategory.REPOSITORY, "There was an error retrieving user's daily weight.\n" + e.getMessage());
        }
        return null;
    }


    /**
     * Update the user's phone number.
     *
     * @return A boolean indicating whether the operation was successful.
     */
    public boolean setPhoneNumber(String phoneNumber) {
        try {
            // Unsetting a phone number is not possible
            if (phoneNumber.isEmpty())
                return false;
            // Get the user logged-in user's id
            long currentUserId = sharedPref.getCurrentUserId();
            if (currentUserId == -1) // There is no current user
                return false;
            User currentUser = database.getUser(currentUserId);
            currentUser.phoneNumber = phoneNumber;
            return database.updateUser(currentUser);
        } catch (Exception e) {
            Log.e(LogCategory.REPOSITORY, "There was an error retrieving user's daily weight.\n" + e.getMessage());
        }
        return false;
    }


    /**
     * Return the user's current sms setting.
     */
    public boolean getUserNotificationSetting() {
        try {
            return sharedPref.getSMSSetting();
        } catch (Exception e) {
            Log.e(LogCategory.REPOSITORY, "There was an error getting notification preference.\n" + e.getMessage());
        }
        // Always return false if the setting has not been applied.
        return false;
    }


    /**
     * Set the user's current notification setting
     *
     * @param setting The notification setting flag to set.
     */
    public boolean setUserNotificationSetting(boolean setting) {
        try {
            sharedPref.setSMSSetting(setting);
            return setting; // Return the setting if it has been applied.
        } catch (Exception e) {
            Log.e(LogCategory.REPOSITORY, "There was an error setting notification preference.\n" + e.getMessage());
        }
        // Always return false if the setting has not been applied.
        return false;
    }


}
