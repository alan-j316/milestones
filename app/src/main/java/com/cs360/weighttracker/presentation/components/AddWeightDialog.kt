package com.cs360.weighttracker.presentation.components

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.cs360.weighttracker.R
import com.cs360.weighttracker.presentation.home.HomeActions
import com.cs360.weighttracker.presentation.home.HomeViewModel
import com.cs360.weighttracker.presentation.validators.WeightValidator.validate
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class AddWeightDialog : DialogFragment() {
    private lateinit var weightEditText: EditText
    private lateinit var cancelButton: Button
    private lateinit var logWeightButton: Button
    private lateinit var weightErrorTextView: TextView

    val viewModel by activityViewModel<HomeViewModel>()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        // Get the layout inflater.
        val inflater = requireActivity().layoutInflater

        // Inflate and set the layout for the dialog.
        // Since we need the view to get the elements from dialog
        // we need a reference to the inflated view
        val dialogView = inflater.inflate(R.layout.dialog_add_weight, null)
        builder.setView(dialogView)

        setupUI(dialogView)
        setupEvents()

        return builder.create()
    }

    private fun setupUI(view: View) {
        weightEditText = view.findViewById(R.id.etAddWeightDialogAddWeight)
        cancelButton = view.findViewById(R.id.btnAddWeightDialogCancel)
        logWeightButton = view.findViewById(R.id.btnAddWeightDialogLog)
        weightErrorTextView = view.findViewById(R.id.tvAddWeightDialogWeightError)
    }


    /**
     * Sets up the event listeners necessary for all the views.
     */
    private fun setupEvents() {
        logWeightButton.setOnClickListener { _: View? ->
            val weightStr = weightEditText.text.toString()
            val weight = weightStr.toFloatOrNull()
//            Log.e("Weight", "Str: $weightStr Float: $weight")
            if (weight == null || !validate(weight)) {

                weightErrorTextView.text = requireContext().getString(R.string.weight_error)
                weightErrorTextView.visibility = View.VISIBLE
                return@setOnClickListener
            }

            viewModel.onAction(HomeActions.TrackWeight(weight))
            // Dismiss the dialog
            dismiss()
        }

        // If cancel button is pressed, then close the dialog
        cancelButton.setOnClickListener { _: View ->
            checkNotNull(this@AddWeightDialog.dialog)
            this@AddWeightDialog.dialog!!.cancel()
        }


        // Reset the error state when the user updates the text.
        weightEditText.doOnTextChanged { _, _, _, _ ->
            weightErrorTextView.visibility = View.GONE
        }
    }
}