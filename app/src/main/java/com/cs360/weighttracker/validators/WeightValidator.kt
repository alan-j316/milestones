package com.cs360.weighttracker.validators

object WeightValidator {
    const val MIN_WEIGHT: Float = 1.0f
    const val MAX_WEIGHT: Float = 700.0f

    /**
     * Validates whether given weight is in bounds of MIN_WEIGHT and MAX_WEIGHT.
     * 
     * @param weight The weight to validate.
     * @return Boolean indicating whether the weight is valid.
     */
    @JvmStatic
    fun validate(weight: Float): Boolean {
        return weight >= MIN_WEIGHT && weight <= MAX_WEIGHT
    }
}
