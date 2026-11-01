package com.cs360.weighttracker.presentation.utils

import android.text.InputType
import android.widget.EditText
import android.widget.ImageButton
import com.cs360.weighttracker.R

object PasswordVisibilityToggler {
    /**
     * Update an edittext's password visibility and its triggering button's icon.
     * 
     * @param passwordEditText The EditText text to toggle based on password status.
     * @param visibilityButton The ImageButton that toggle the visibility.
     * @implNote Uses the passed-in edittext's internal state to toggle visibility.
     */
    @JvmStatic
    fun togglePasswordState(passwordEditText: EditText, visibilityButton: ImageButton) {
        // Check if the password is visible
        val variation = passwordEditText.inputType and InputType.TYPE_MASK_VARIATION
        val isVisible = (variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

        // Cursor position is reset when we toggle edit text type, so we need to save it
        // and restore after transformation
        val cursorPosition = passwordEditText.selectionStart

        if (isVisible) {
            // It is currently visible, hide it
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            visibilityButton.setImageResource(R.drawable.visibility_on)
        } else {
            // It is currently hidden, show it
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            visibilityButton.setImageResource(R.drawable.visibility_off)
        }
        // Restore cursor position to the end
        passwordEditText.setSelection(cursorPosition)
    }
}
