package com.cs360.weighttracker.presentation.components;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.cs360.weighttracker.R;

public class DeleteWeightItemDialog extends DialogFragment {

    DialogClickListener clickListener;

    public DeleteWeightItemDialog(DialogClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Build an alert dialog with a title, dismiss, and positive actions
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_weight_recording)
                .setPositiveButton(R.string.delete, (dialog, id) -> this.clickListener.onPositiveAction())
                .setNegativeButton(R.string.cancel, (dialog, id) -> dismiss());
        return builder.create();
    }
}