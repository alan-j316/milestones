package com.cs360.weighttracker.data.local.status

/**
 * Indicates different registration status and errors from repository layer.
 * NO_STATUS is a placeholder that must only be used for defining initial/undefined states.
 */
enum class RegisterStatus {
    USER_EXISTS, SUCCESS, UNKNOWN_FAILURE
}
