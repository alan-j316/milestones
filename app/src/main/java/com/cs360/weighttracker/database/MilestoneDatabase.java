package com.cs360.weighttracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cs360.weighttracker.domain.models.DailyWeight;
import com.cs360.weighttracker.domain.models.User;
import com.cs360.weighttracker.utils.LogCategory;

import java.util.ArrayList;
import java.util.List;

public class MilestoneDatabase extends SQLiteOpenHelper {

    public MilestoneDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    private static final String DATABASE_NAME = "milestones.db";
    private static final int VERSION = 1;

    private static final class UserTable {
        private static final String TABLE = "users";
        private static final String COL_ID = "_id";

        private static final String COL_USERNAME = "username";
        private static final String COL_PASSWORD_HASH = "password_hash";
        private static final String COL_FULL_NAME = "full_name";
        private static final String COL_PHONE_NUMBER = "phone_number";
    }

    private static final class DailyWeightTable {
        private static final String TABLE = "daily_weight";
        private static final String COL_ID = "_id";

        private static final String USER_FK = "user_id";

        private static final String COL_WEIGHT = "weight";
        private static final String COL_DATE = "date";

    }


    private static final class GoalWeightTable {
        private static final String TABLE = "goal_weight";
        private static final String COL_ID = "_id";

        private static final String USER_FK = "user_id";
        private static final String COL_CUR_WEIGHT = "current_weight";
        private static final String COL_GOAL_WEIGHT = "goal_weight";

    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable foreign constraint which are disabled by default for backwards compatibility
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create all the databases
        String userTableQuery = "CREATE TABLE " +
                UserTable.TABLE + " (" +
                UserTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserTable.COL_USERNAME + " TEXT, " +
                UserTable.COL_FULL_NAME + " TEXT, " +
                UserTable.COL_PHONE_NUMBER + " TEXT, " +
                UserTable.COL_PASSWORD_HASH + " TEXT)";

        String dailyWeightTableQuery = "CREATE TABLE " +
                DailyWeightTable.TABLE + " (" +
                DailyWeightTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DailyWeightTable.COL_WEIGHT + " REAL, " +
                DailyWeightTable.COL_DATE + " INTEGER, " +
                DailyWeightTable.USER_FK + " INTEGER, " +
                "FOREIGN KEY (" + DailyWeightTable.USER_FK +
                ") REFERENCES " + UserTable.TABLE + "(" + UserTable.COL_ID + "));";

        String goalWeightTableQuery = "CREATE TABLE " +
                GoalWeightTable.TABLE + " (" +
                GoalWeightTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GoalWeightTable.COL_GOAL_WEIGHT + " REAL, " +
                GoalWeightTable.COL_CUR_WEIGHT + " REAL," +
                DailyWeightTable.USER_FK + " INTEGER, " +
                "FOREIGN KEY (" + DailyWeightTable.USER_FK +
                ") REFERENCES " + UserTable.TABLE + "(" + UserTable.COL_ID + "));";

        sqLiteDatabase.execSQL(userTableQuery);
        sqLiteDatabase.execSQL(dailyWeightTableQuery);
        sqLiteDatabase.execSQL(goalWeightTableQuery);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop all the databases
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + UserTable.TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DailyWeightTable.TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GoalWeightTable.TABLE);

        // Create the database tables
        onCreate(sqLiteDatabase);
    }


    //////////////////////////////
    ///                         //
    ///        USER CRUD        //
    ///                         //
    //////////////////////////////

    /**
     * Inserts a user into the database.
     *
     * @param user The user to be inserted.
     * @return The id of the inserted user.
     * @apiNote The user's userid must be unique, so if try to insert a user with an existing id will return the user.
     * It WILL NOT update due to security reasons (anyone could change the password if we were to update the user).
     */
    public long insertUser(@NonNull User user) {
        SQLiteDatabase db = getWritableDatabase();

        // Get the user if there exists a user with same id
        User retrievedUser = getUser(user.getUserName());
        if (retrievedUser != null)
            return user.userId;

        // Create values for insertion
        ContentValues values = new ContentValues();
        values.put(UserTable.COL_USERNAME, user.getUserName());
        values.put(UserTable.COL_FULL_NAME, user.fullName);
        values.put(UserTable.COL_PASSWORD_HASH, user.hashedPassword);
        values.put(UserTable.COL_PHONE_NUMBER, user.phoneNumber);

        // Insert the value into database
        return db.insert(UserTable.TABLE, null, values);
    }


    /**
     * Get a user from the database with the given username.
     *
     * @param username The username to match against.
     * @return A user object, if the user exist and null otherwise.
     */
    public User getUser(@NonNull String username) {
        SQLiteDatabase db = getReadableDatabase();

        // Execute the query
        String query = "SELECT * FROM " + UserTable.TABLE + " WHERE " + UserTable.COL_USERNAME + " = ? LIMIT 1";
        try (Cursor cursor = db.rawQuery(query, new String[]{username})) {
            if (cursor.moveToFirst()) {
                long id = cursor.getLong(0);
                String fullName = cursor.getString(2);
                String password = cursor.getString(3);
                String phoneNumber = cursor.getString(4);
                return new User(id, username, password, fullName, phoneNumber);
            }
        } catch (Exception e) {
            Log.e(LogCategory.DATABASE, "There was an error getting the user.\n" + e.getMessage());
        }
        return null;
    }

    /**
     * Get a user from the database with the given user ID.
     *
     * @param userId The user's user id.
     * @return A user object, if the user exist and null otherwise.
     */
    public User getUser(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        // Execute the query
        String query = "SELECT * FROM " + UserTable.TABLE + " WHERE " + UserTable.COL_ID + " = ? LIMIT 1";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                long id = cursor.getLong(0);
                String username = cursor.getString(1);
                String fullName = cursor.getString(2);
                String phoneNumber = cursor.getString(3);
                String password = cursor.getString(4);
                return new User(id, username, password, fullName, phoneNumber);
            }
        } catch (Exception e) {
            Log.e(LogCategory.DATABASE, "There was an error getting the user.\n" + e.getMessage());
        }
        return null;
    }

    /**
     * Validates whether the given user credentials are valid.
     *
     * @param user The user to validate.
     * @return The user object if a record exists else null.
     * @apiNote The method doesn't return whether the user exists, only if the credentials are valid.
     * To know if a user exists, user `getUser` with username.
     */
    public User validateUser(@NonNull User user) {
        SQLiteDatabase db = getReadableDatabase();

        // Execute the query
        String query = "SELECT * FROM " + UserTable.TABLE + " WHERE " + UserTable.COL_USERNAME + " = ? AND " + UserTable.COL_PASSWORD_HASH + " = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{user.getUserName(), user.hashedPassword})) {
            // Get the user if they exist in the database
            if (cursor.moveToFirst()) {
                long id = cursor.getLong(0);
                String username = cursor.getString(1);
                String fullName = cursor.getString(2);
                String phoneNumber = cursor.getString(3);
                String password = cursor.getString(4);
                return new User(id, username, password, fullName, phoneNumber);
            }
        } catch (Exception e) {
            Log.e(LogCategory.DATABASE, "There was an validating the user.\n" + e.getMessage());
        }

        // Else return null
        return null;
    }

    /**
     * Update a user information.
     *
     * @param user The user to update with.
     * @return A boolean indicating whether the update was a success.
     * @apiNote The user is updated based on the userId field of the user.
     */
    public boolean updateUser(@NonNull User user) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserTable.COL_USERNAME, user.getUserName());
        values.put(UserTable.COL_FULL_NAME, user.fullName);
        values.put(UserTable.COL_PASSWORD_HASH, user.hashedPassword);
        values.put(UserTable.COL_PHONE_NUMBER, user.phoneNumber);

        int rowsUpdated = db.update(UserTable.TABLE, values, UserTable.COL_ID + " = ?", new String[]{String.valueOf(user.userId)});

        return rowsUpdated > 0;
    }


    /**
     * Delete the user with the provided user id.
     *
     * @param userId The user id associated with the user to delete.
     * @return A boolean indicating whether the delete operation was a success.
     */
    public boolean deleteUser(long userId) {
        SQLiteDatabase db = getWritableDatabase();

        int rowsDeleted = db.delete(UserTable.TABLE, UserTable.COL_ID + " = ?",
                new String[]{Long.toString(userId)});
        return rowsDeleted > 0;
    }


    //////////////////////////////
    ///                         //
    ///    USER Goal Weight     //
    ///                         //
    //////////////////////////////

    /**
     * Inserts a user's goal weight into the database.
     *
     * @param userId     The id of the user associated with the goal weight.
     * @param goalWeight The goal weight of the user.
     * @return The id of the inserted goal weight, -1 if the goal weight already exists.
     */
    public long insertGoalWeight(long userId, @NonNull GoalWeight goalWeight) {
        SQLiteDatabase db = getWritableDatabase();

        if (goalWeight.getId() > -1)
            return -1; // If the goal weight already exists then don't update.

        // Create the values for insertion
        ContentValues values = new ContentValues();
        values.put(GoalWeightTable.COL_CUR_WEIGHT, goalWeight.getCurrentWeight());
        values.put(GoalWeightTable.COL_GOAL_WEIGHT, goalWeight.getGoalWeight());
        values.put(GoalWeightTable.USER_FK, userId);

        return db.insert(GoalWeightTable.TABLE, null, values);
    }


    /**
     * Get the goal weight of a user.
     *
     * @param userId The id of the user associated with the goal weight.
     * @return A goal weight object, or null if it does not exist.
     */
    public GoalWeight getGoalWeight(long userId) {
        SQLiteDatabase db = getReadableDatabase();

        // Execute the query
        String query = "SELECT * FROM " + GoalWeightTable.TABLE + " WHERE " + GoalWeightTable.USER_FK + " = ? LIMIT 1";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                long id = cursor.getLong(0);
                float goalWeight = cursor.getFloat(1);
                float currentWeight = cursor.getFloat(2);
                return new GoalWeight(id, currentWeight, goalWeight);
            }
        } catch (Exception e) {
            Log.e(LogCategory.DATABASE, "There was an error getting the user's goal weight!\n" + e.getMessage());
        }
        return null;
    }


    /**
     * Update the goal weight associated with a user.
     *
     * @param userId     The id of the user associated with the goal weight.
     * @param goalWeight The goal weight to update with.
     * @return A boolean indicating whether the update was a success.
     * @apiNote The goal weight must have an id associated with it, else it will not get
     * updated.
     */
    public boolean updateGoalWeight(long userId, @NonNull GoalWeight goalWeight) {
        SQLiteDatabase db = getWritableDatabase();

        if (goalWeight.getId() < 0)
            return false; // If the goal weight has no id, then return, since its an invalid weight.

        // If the user goal weight exists, update it
        ContentValues values = new ContentValues();
        values.put(GoalWeightTable.COL_CUR_WEIGHT, goalWeight.getCurrentWeight());
        values.put(GoalWeightTable.COL_GOAL_WEIGHT, goalWeight.getGoalWeight());

        int rowsUpdated = db.update(GoalWeightTable.TABLE, values, GoalWeightTable.COL_ID + " = ?", new String[]{String.valueOf(goalWeight.getId())});

        return rowsUpdated > 0;
    }


    /**
     * Updates/Inserts the goal weight associated with a user as appropriate.
     *
     * @param userId     The id of the user associated with the goal weight.
     * @param goalWeight The goal weight to upsert.
     * @return A boolean indicating whether the upsert was a success.
     * @apiNote If the given user has no goal weight associated with them,
     * then the api inserted the current goal weight.
     */
    public boolean upsertGoalWeight(long userId, @NonNull GoalWeight goalWeight) {
        SQLiteDatabase db = getWritableDatabase();
        // The goal weight association is made based on user's goal weight
        // Retrieve the goal weight to ensure that the data exists.
        GoalWeight queriedGoalWeight = getGoalWeight(userId);
        // Upsertion: Inserts the goal weight if it doesn't exist.
        if (queriedGoalWeight == null) {
            return insertGoalWeight(userId, goalWeight) > 0;
        }
        // Since the user may pass in a goal weight without an id(from UI for e.g)
        // we must always update with the matching id
        long id = goalWeight.getId();
        if (id < 0)
            id = queriedGoalWeight.getId();

        // If the user goal weight exists, update it
        ContentValues values = new ContentValues();
        values.put(GoalWeightTable.COL_CUR_WEIGHT, goalWeight.getCurrentWeight());
        values.put(GoalWeightTable.COL_GOAL_WEIGHT, goalWeight.getGoalWeight());
        values.put(GoalWeightTable.COL_ID, id);

        int rowsUpdated = db.update(GoalWeightTable.TABLE, values, GoalWeightTable.COL_ID + " = ?", new String[]{String.valueOf(id)});

        return rowsUpdated > 0;
    }


    /**
     * Delete the goal weight associated with the user.
     *
     * @param userId The user id associated with the goal weight to delete.
     * @return A boolean indicating whether the delete operation was a success.
     */
    public boolean deleteGoalWeight(long userId) {
        SQLiteDatabase db = getWritableDatabase();
        // Retrieve the id
        GoalWeight goalWeight = getGoalWeight(userId);

        int rowsDeleted = db.delete(GoalWeightTable.TABLE, GoalWeightTable.COL_ID + " = ?",
                new String[]{Long.toString(goalWeight.getId())});
        return rowsDeleted > 0;
    }


    //////////////////////////////
    ///                         //
    ///    USER Daily Weight    //
    ///                         //
    //////////////////////////////

    /**
     * Inserts a user's daily weight into the database.
     *
     * @param userId      The id of the user associated with the goal weight.
     * @param dailyWeight The tracked weight of the user.
     * @return The id of the inserted daily weight.
     * @apiNote The API constrains one goal weight per user, upserting the data if a duplicate
     * goal weight is inserted.
     */
    public long insertDailyWeight(long userId, @NonNull DailyWeight dailyWeight) {
        SQLiteDatabase db = getWritableDatabase();

        // Create the values for insertion
        ContentValues values = new ContentValues();
        values.put(DailyWeightTable.COL_WEIGHT, dailyWeight.userWeight);
        values.put(DailyWeightTable.COL_DATE, dailyWeight.getDateTimeMillis());
        values.put(DailyWeightTable.USER_FK, userId);

        return db.insert(DailyWeightTable.TABLE, null, values);
    }


    /**
     * Get all daily weight of the user with the given @p userId.
     *
     * @param userId The id of the user associated with the daily weights.
     * @return A list of daily weights associated with the user, or any empty list if none exists.
     */
    public List<DailyWeight> getDailyWeights(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        List<DailyWeight> dailyWeights = new ArrayList<>();

        String query = "SELECT * FROM " + DailyWeightTable.TABLE + " WHERE " +
                DailyWeightTable.USER_FK + " = ? ORDER BY " + DailyWeightTable.COL_DATE + " DESC";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                // Traverse through the database and add each weight to the list.
                do {
                    long id = cursor.getLong(0);
                    float dailyWeight = cursor.getFloat(1);
                    long dateMillis = cursor.getLong(2);
                    dailyWeights.add(new DailyWeight(id, dailyWeight, dateMillis));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LogCategory.DATABASE, "There was an error getting user's daily weights!\n" + e.getMessage());
        }
        return dailyWeights;
    }

    /**
     * Get the latest weight of the user with @p userId.
     *
     * @param userId The id of the user associated with the goal weight.
     * @return A daily weight object if there is a logged weight, null otherwise.
     */
    public DailyWeight getLatestDailyWeight(long userId) {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + DailyWeightTable.TABLE + " WHERE " +
                DailyWeightTable.USER_FK + " = ? ORDER BY " + DailyWeightTable.COL_DATE + " DESC LIMIT 1";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                long id = cursor.getLong(0);
                float dailyWeight = cursor.getFloat(1);
                long dateMillis = cursor.getLong(2);
                return new DailyWeight(id, dailyWeight, dateMillis);
            }
        } catch (Exception e) {
            Log.e(LogCategory.DATABASE, "There was an error getting user's daily weights!\n" + e.getMessage());
        }
        return null;
    }


    /**
     * Update the daily weight.
     *
     * @param dailyWeight The daily weight to update.
     * @return A boolean indicating whether the update was a success.
     */
    public boolean updateDailyWeight(@NonNull DailyWeight dailyWeight) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DailyWeightTable.COL_WEIGHT, dailyWeight.userWeight);
        values.put(DailyWeightTable.COL_DATE, dailyWeight.getDateTimeMillis());

        int rowsUpdated = db.update(DailyWeightTable.TABLE, values, DailyWeightTable.COL_ID + " = ?", new String[]{String.valueOf(dailyWeight.id)});

        return rowsUpdated > 0;
    }


    /**
     * Delete a daily weight.
     *
     * @param id The id of the goal weight to delete.
     * @return A boolean indicating whether the delete operation was a success.
     */
    public boolean deleteDailyWeight(long id) {
        SQLiteDatabase db = getWritableDatabase();

        int rowsDeleted = db.delete(DailyWeightTable.TABLE, DailyWeightTable.COL_ID + " = ?",
                new String[]{Long.toString(id)});
        return rowsDeleted > 0;
    }

}
