package com.cs360.weighttracker.presentation.components;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.cs360.weighttracker.presentation.Constants;
import com.cs360.weighttracker.R;
import com.cs360.weighttracker.presentation.validators.WeightValidator;

public class AddWeightDialog extends DialogFragment {

    EditText weightEditText;
    Button cancelButton, logWeightButton;
    TextView weightErrorTextView;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater.
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog.
        // Since we need the view to get the elements from dialog
        // we need a reference to the inflated view
        View dialogView = inflater.inflate(R.layout.dialog_add_weight, null);
        builder.setView(dialogView);

        setupUI(dialogView);
        setupEvents();

        return builder.create();
    }

    private void setupUI(View view) {
        weightEditText = view.findViewById(R.id.etAddWeightDialogAddWeight);
        cancelButton = view.findViewById(R.id.btnAddWeightDialogCancel);
        logWeightButton = view.findViewById(R.id.btnAddWeightDialogLog);
        weightErrorTextView = view.findViewById(R.id.tvAddWeightDialogWeightError);
    }


    /**
     * Sets up the event listeners necessary for all the views.
     */
    private void setupEvents() {
        // Finish will pop off this activity from the backstack leaving only with the Login activity
        // from the navigation will take place
        // Since the events listeners only trigger for one textbox,
        // I've decided to keep it simple rather than split it into functions like the other
        // activities
        logWeightButton.setOnClickListener(view -> {
            String weightStr = weightEditText.getText().toString();
            try {
                float weight = Float.parseFloat(weightStr);
                if (!WeightValidator.validate(weight))
                    throw new Exception("Illegal weight value");
                // Save the result in a bundle so we can share it to the parent view (Home Activity)
                Bundle result = new Bundle();
                result.putFloat(Constants.NEW_WEIGHT_BUNDLE_KEY, weight);
                getParentFragmentManager().setFragmentResult(Constants.ADD_NEW_WEIGHT_DIALOG, result);

                // Dismiss the dialog
                dismiss();
            } catch (Exception e) {
                weightErrorTextView.setText(requireContext().getString(R.string.weight_error));
                weightErrorTextView.setVisibility(View.VISIBLE);
            }
        });

        // If cancel button is pressed, then close the dialog
        cancelButton.setOnClickListener(view -> {
            assert AddWeightDialog.this.getDialog() != null;
            AddWeightDialog.this.getDialog().cancel();
        });


        weightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Remove the error text when we update the weight dialog's edit text
                weightErrorTextView.setVisibility(View.GONE);
            }
        });

    }

}