package com.cs360.weighttracker.presentation.validators

object PasswordValidator {
    /**
     * Checks whether a password conforms to the required pattern.
     * A password must be greater than 8 character.
     * A password must contain at least 1 digit.
     * A password must not contain any spaces.
     * 
     * @param password The password to validate.
     * @return Boolean indicating whether the password is valid.
     */
    @JvmStatic
    fun validate(password: String?): Boolean {
        if (password == null || password.trim { it <= ' ' }.isEmpty()) {
            return false
        }
        if (!password.contains(" ")) {
            return password.length >= 8 && password.matches(".*\\d+.*".toRegex())
        }
        return false
    }
}
