package com.cs360.weighttracker.models

/**
 * User's daily weight.
 *
 * @param weight         The user weight.
 * @param dateTimeMillis Date-Time in milliseconds.
 * @param id             The unique Identifier for the daily weight.
 */
data class DailyWeight(
    val weight: Float,
    val dateTimeMillis: Long,
    val id: Long = -1,
)