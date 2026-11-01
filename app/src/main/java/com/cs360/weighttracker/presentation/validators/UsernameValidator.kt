package com.cs360.weighttracker.presentation.validators

object UsernameValidator {
    /**
     * Checks whether a username conforms to the required pattern.
     * A username is valid only if it doesn't contain any spaces.
     * 
     * @param username The username to validate.
     * @return Boolean indicating whether the username is valid.
     */
    @JvmStatic
    fun validate(username: String?): Boolean {
        if (username == null || username.trim { it <= ' ' }.isEmpty()) {
            return false
        }
        return !username.contains(" ")
    }
}
