package com.cs360.weighttracker.presentation.validators

object PhoneNumberValidator {
    /**
     * Validates a phone number as not empty and having less than 20 character.
     * 
     * @param phoneNumber The phone number to validate.
     * @return A boolean indicating whether the phone number is valid.
     */
    @JvmStatic
    fun validate(phoneNumber: String): Boolean {
        return !phoneNumber.trim { it <= ' ' }.isEmpty() && phoneNumber.length <= 20
    }
}
