package com.cs360.weighttracker.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    /**
     * Formats time in millis to `Month Day, Year` format.
     * 
     * @param millis Time in milliseconds
     * @return A string representation of the target datetime.
     */
    @JvmStatic
    fun fromMillis(millis: Long): String {
        val date = Date(millis)
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(date)
    }
}