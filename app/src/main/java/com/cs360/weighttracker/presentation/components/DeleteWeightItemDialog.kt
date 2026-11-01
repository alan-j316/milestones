package com.cs360.weighttracker.presentation.components

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.cs360.weighttracker.R

class DeleteWeightItemDialog(var clickListener: DialogClickListener) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Build an alert dialog with a title, dismiss, and positive actions
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.delete_weight_recording)
            .setPositiveButton(
                R.string.delete
            ) { _: DialogInterface?, _: Int -> this.clickListener.onPositiveAction() }
            .setNegativeButton(
                R.string.cancel
            ) { _: DialogInterface?, _: Int -> dismiss() }
        return builder.create()
    }
}