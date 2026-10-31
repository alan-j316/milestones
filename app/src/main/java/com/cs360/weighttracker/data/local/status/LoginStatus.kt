package com.cs360.weighttracker.data.local.status


/**
 * Indicates different login status and errors from repository layer.
 * NO_STATUS is a placeholder that must only be used for defining initial/undefined states.
 */
enum class LoginStatus {
    USERNAME_ERROR, PASSWORD_ERROR, SUCCESS, UNKNOWN_FAILURE, NO_STATUS
}
