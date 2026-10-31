package com.cs360.weighttracker.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit


class MilestonesSharedPref(ctx: Context) {

    ///-----------------------
    ///   INTERNAL CONSTANTS
    ///-----------------------
    private companion object {
        const val PREF_NAME = "milestone_prefs"
        const val CURRENT_SESSION_USER_ID = "user-id"
        const val SMS_SETTING_KEY = "sms-settings"

    }


    ///-----------------------
    ///       MEMBERS
    ///-----------------------

    private val sharedPreferences: SharedPreferences by lazy {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * @brief A boolean flag indicating whether the user has SMS option turned ON or OFF
     * @remarks The values are directly read and written to the shared preferences.
     */
    var smsSetting: Boolean
        get() = sharedPreferences.getBoolean(SMS_SETTING_KEY, false)
        set(status) {
            sharedPreferences.edit {
                putBoolean(SMS_SETTING_KEY, status)
            }
        }

    /**
     * Get/Set the current logged-in user's id.
     * @remarks Reads are writes and directly propagated to the shared preference.
     */
    var currentUserId: Long
        get() = sharedPreferences.getLong(CURRENT_SESSION_USER_ID, -1)
        set(id) {
            sharedPreferences.edit {
                putLong(CURRENT_SESSION_USER_ID, id)
            }
        }


    /**
     * Delete the current user id from the shared preference.
     */
    fun deleteCurrentUserId() {
        sharedPreferences.edit {
            remove(CURRENT_SESSION_USER_ID)
        }
    }


    /**
     * Delete the current user's SMS setting.
     */
    fun deleteSMSSetting() {
        sharedPreferences.edit {
            remove(SMS_SETTING_KEY)
        }
    }
}