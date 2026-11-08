package com.cs360.weighttracker.domain.status


/**
 * Indicates different login status and errors from repository layer.
 */
enum class LoginStatus {
    USERNAME_ERROR, PASSWORD_ERROR, SUCCESS, UNKNOWN_FAILURE
}
