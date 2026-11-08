package com.cs360.weighttracker.presentation.utils

import androidx.annotation.StringRes
import com.cs360.weighttracker.R
import java.util.Calendar

object TimeUtils {
    @JvmStatic
    @get:StringRes
    val greetingResId: Int
        /**
         * Determines the appropriate greeting based on the device's current local time.
         * 
         * @return The String resource ID for the time-based greeting.
         */
        get() {
            // HOUR_OF_DAY returns the hour in 24-hour format (0-23)
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

            return if (hour < 12) {
                R.string.greeting_morning // 12:00 AM to 11:59 AM
            } else if (hour < 17) {
                R.string.greeting_afternoon // 12:00 PM to 4:59 PM
            } else {
                R.string.greeting_evening // 5:00 PM to 11:59 PM
            }
        }
}
