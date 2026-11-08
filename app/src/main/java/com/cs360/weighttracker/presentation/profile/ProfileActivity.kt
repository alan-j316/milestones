package com.cs360.weighttracker.presentation.profile

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cs360.weighttracker.R
import com.cs360.weighttracker.domain.models.GoalType
import com.cs360.weighttracker.presentation.Constants
import com.cs360.weighttracker.presentation.goals.GoalsActivity
import com.cs360.weighttracker.presentation.login.LoginActivity
import com.cs360.weighttracker.presentation.validators.PhoneNumberValidator
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.max
import kotlin.math.min

class ProfileActivity : AppCompatActivity() {
    private lateinit var weightProgressTextView: TextView
    private lateinit var numberTextView: TextView
    private lateinit var weightChangeProgressBar: ProgressBar

    private lateinit var notificationSwitch: Switch
    private lateinit var updateProfileButton: Button
    private lateinit var logoutButton: Button
    private lateinit var updateNumberButton: Button
    private lateinit var navigateBackButton: ImageButton

    private val viewModel: ProfileViewModel by viewModel()


    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                handleUserNumberAndToggleSMSOn()
            } else {
                // Revert the switch visually because they denied it
                notificationSwitch.isChecked = false

                // If the user denied the permission and show rationale is false, it means they permanently blocked it.
                // in which case we display the dialog for opening setting and manually switching the permission
                if (!shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
                    showSettingsRedirectDialog()
                }
            }
        }


        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        when (state) {
                            ProfileUIState.IsLoading -> {
                                // TODO: Add loading state
                            }

                            ProfileUIState.LogoutError -> {
                                makeText(
                                    this@ProfileActivity, R.string.log_out_error, Toast.LENGTH_SHORT
                                ).show()
                            }

                            ProfileUIState.LogoutSuccess -> {
                                makeText(
                                    this@ProfileActivity,
                                    R.string.log_out_success,
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateToLogin()
                            }

                            ProfileUIState.UpdateNumberError -> {
                                makeText(
                                    this@ProfileActivity,
                                    R.string.phone_number_update_error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            ProfileUIState.UpdateNumberSuccess -> {
                                makeText(
                                    this@ProfileActivity,
                                    R.string.phone_number_update_success,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            ProfileUIState.UpdateSMSSettingError -> {
                                makeText(
                                    this@ProfileActivity,
                                    R.string.sms_pref_update_error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            ProfileUIState.UpdateSMSSettingSuccess -> {
                                makeText(
                                    this@ProfileActivity,
                                    R.string.sms_pref_update_success,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.userData.collect { (user, weight) ->
                        if (user == null) return@collect

                        val currentGoal = user.goalWeight
                        // If the user has not logged weight use the initial weight as the current weight
                        val currentWeight: Float = weight?.weight ?: currentGoal

                        if (user.getGoalType() === GoalType.WEIGHT_LOSS) {
                            // Get current weight returns the user's initial weight,
                            // and we compare it the user's logged weight to get the progress
                            val weightLost: Float = currentGoal - currentWeight
                            val goalTotal: Float = currentGoal - currentGoal
                            weightProgressTextView.text = getString(
                                R.string.weight_loss_progress, weightLost, goalTotal
                            )
                            val progressPercentage = (weightLost / goalTotal * 100).toInt()
                            val progress = max(0, min(100, progressPercentage))
                            weightChangeProgressBar.progress = progress
                        } else {
                            // For weight gain the current weight will be greater than the initial weight
                            // else the progress will show negative values.
                            val weightGained: Float = currentWeight - currentGoal
                            val goalTotal: Float = currentGoal - currentGoal
                            weightProgressTextView.text = getString(
                                R.string.weight_gain_progress, weightGained, goalTotal
                            )

                            val progressPercentage = (weightGained / goalTotal * 100).toInt()
                            val progress = max(0, min(100, progressPercentage))
                            weightChangeProgressBar.progress = progress
                        }

                        // Update user information
                        val fullNameTextView: TextView = findViewById(R.id.tvProfileFullName)
                        // If the user has no full name, then use their username(This is highly unlikely).
                        val fullName = user.fullName.ifEmpty { user.userName }
                        fullNameTextView.text = fullName

                        // If the user has a phone number set, the update the number the textview with their number
                        if (user.phoneNumber.isNotEmpty()) numberTextView.text = getString(
                            R.string.sms_number_text, user.phoneNumber
                        )
                    }
                }

                launch {
                    viewModel.smsPreference.collect { pref ->
                        // Set up the toggles for SMS preference
                        notificationSwitch.setOnCheckedChangeListener(null)
                        notificationSwitch.isChecked = pref
                    }
                }
            }

        }

        setupUI()
        setupEvents()
    }


    /**
     * Query and attach each view instance from XML layout.
     */
    private fun setupUI() {
        weightChangeProgressBar = findViewById(R.id.pbProfileWeightChange)
        weightProgressTextView = findViewById(R.id.tvProfileWeightChange)
        navigateBackButton = findViewById(R.id.btnProfileNavigateBack)
        logoutButton = findViewById(R.id.btnProfileLogout)
        updateProfileButton = findViewById(R.id.btnProfileUpdateProfile)
        notificationSwitch = findViewById(R.id.switchProfileNotificationSetting)
        numberTextView = findViewById(R.id.tvProfileNotificationNumber)
        updateNumberButton = findViewById(R.id.btnProfileUpdateNumber)
    }


    /**
     * Sets up the event listeners necessary for all the views.
     */
    private fun setupEvents() {
        logoutButton.setOnClickListener { _: View ->
            viewModel.onAction(ProfileActions.Logout)
        }

        // Navigate back by popping out the current activity
        navigateBackButton.setOnClickListener { _: View? ->
            finish()
        }
        updateProfileButton.setOnClickListener { _: View? ->
            navigateToGoals()
        }

        notificationSwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            // Only ask permission for toggling it off
            if (isChecked) {
                handleSmsPermission() // Only ask if they are turning it ON
            } else {
                // User turned it OFF. Just update the setting to false.
                viewModel.onAction(ProfileActions.UpdateSMSSetting(true))
            }
        }

        updateNumberButton.setOnClickListener { _: View? ->
            showAddPhoneDialog()
        }
    }


    /**
     * Handles the SMS permission.
     */
    private fun handleSmsPermission() {
        val permission = Manifest.permission.SEND_SMS

        if (ContextCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            handleUserNumberAndToggleSMSOn()
        } else if (shouldShowRequestPermissionRationale(permission)) {
            // The permission is denied once
            showRationaleDialog()
        } else {
            // Asking user for permission for the first time.
            requestPermissionLauncher.launch(permission)
        }
    }


    /**
     * Handles the user's SMS preference and toggles sms setting accordingly
     */
    private fun handleUserNumberAndToggleSMSOn() {
        val user = viewModel.userData.value.user ?: return
        // Check if user has a phone number
        // if they don't have, request for a phone number
        if (user.phoneNumber.isEmpty()) {
            showAddPhoneDialog() // Show the dialog
        } else { // If we have permission and user has phone number, turn it on!
            viewModel.onAction(ProfileActions.UpdateSMSSetting(true))
        }
    }


    /**
     * Shows a dialog to enable permission to the user
     * if the system allow the permission rationale(permission popup) to be shown.
     */
    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.sms_permission_dialog_title).setMessage(R.string.sms_permission_body)
            .setPositiveButton(
                R.string.ok
            ) { _: DialogInterface?, _: Int ->
                // Request the system popup again
                requestPermissionLauncher.launch(Manifest.permission.SEND_SMS)
            }.setNegativeButton(
                R.string.cancel
            ) { _: DialogInterface?, _: Int ->
                // Toggle the switch to off since the user denied permission
                notificationSwitch.isChecked = false
            }.show()
    }


    /**
     * Shows a phone number add/update dialog.
     * Due to the coupled nature of the dialog, this layout is kept as a standalone function
     * rather than refactoring out.
     */
    private fun showAddPhoneDialog() {
        val user = viewModel.userData.value.user ?: return

        val dialogView: View = layoutInflater.inflate(R.layout.dialog_add_phone_number, null)
        val phoneNumberEditText: EditText =
            dialogView.findViewById(R.id.etAddPhoneNumberDialogNumber)
        val phoneNumberErrorTextView: TextView =
            dialogView.findViewById(R.id.tvAddPhoneNumberDialogNumberError)

        // If the user already has a phone number update the edit text with it
        phoneNumberEditText.setText(user.phoneNumber)
        val positiveActionTextRes: Int =
            if (user.phoneNumber.isEmpty()) R.string.add_number else R.string.update_number

        AlertDialog.Builder(this).setTitle(R.string.add_phone_number).setView(dialogView)
            .setPositiveButton(
                positiveActionTextRes
            ) { _: DialogInterface?, _: Int ->
                val phoneNumber = phoneNumberEditText.text.toString()
                // Inline phone validation
                if (!PhoneNumberValidator.validate(phoneNumber)) {
                    phoneNumberErrorTextView.visibility = View.VISIBLE
                    phoneNumberErrorTextView.setText(R.string.phone_number_error)
                    // We must toggle the notification switch to off.
                    notificationSwitch.isChecked = false
                    makeText(this, R.string.phone_number_error, Toast.LENGTH_LONG).show()
                } else {
                    viewModel.onAction(ProfileActions.UpdatePhone(phoneNumber))
                    viewModel.onAction(ProfileActions.UpdateSMSSetting(true))
                }
            }.setNegativeButton(
                R.string.cancel
            ) { _: DialogInterface?, _: Int ->
                // Toggle back the button if there is no phone number
                if (user.phoneNumber.isEmpty()) {
                    notificationSwitch.isChecked = false
                    viewModel.onAction(ProfileActions.UpdateSMSSetting(false))
                }
            }.show()

        // Update the phone number text if the user updates the phone number
        phoneNumberEditText.doOnTextChanged { _, _, _, _ ->
            phoneNumberErrorTextView.visibility = View.GONE
        }

    }


    /**
     * Create a dialog to be displayed when the user permanently denies the permission.
     */
    private fun showSettingsRedirectDialog() {
        AlertDialog.Builder(this).setTitle(R.string.permission_denied_permanently_title)
            .setMessage(R.string.enable_permission_manually).setPositiveButton(
                R.string.go_to_settings
            ) { _: DialogInterface?, _: Int ->
                // Take them to the settings screen
                openAppSettings()
            }.setNegativeButton(
                R.string.cancel
            ) { _: DialogInterface?, _: Int ->
                // Toggle the switch to off since the user denied permission
                notificationSwitch.isChecked = false
            }.show()
    }


    /**
     * Open the application settings so that the user can turn on the permission manually
     */
    private fun openAppSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }


    /**
     * Handles navigation to login after logout.
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    /**
     * Handles navigation to goal activity.
     */
    private fun navigateToGoals() {
        val intent = Intent(this, GoalsActivity::class.java)
        intent.putExtra(
            Constants.GOAL_ACTIVITY_EDIT_FLAG, true
        )
        startActivity(intent)
    }
}