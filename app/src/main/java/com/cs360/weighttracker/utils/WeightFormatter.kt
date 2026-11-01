package com.cs360.weighttracker.utils

import android.annotation.SuppressLint

object WeightFormatter {
    /**
     * Formats weight with 2 decimal precision and unit(uses KG).
     * 
     * @param weight The weight to format.
     * @return A string representing the formatted weight.
     */
    @JvmStatic
    @SuppressLint("DefaultLocale")
    fun format(weight: Float): String {
        val formatString = "%02.02f KG"
        return String.format(formatString, weight) // "05.35 KG"
    }
}
