package com.cs360.weighttracker.presentation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.cs360.weighttracker.R;
import com.cs360.weighttracker.database.MilestoneRepository;
import com.cs360.weighttracker.domain.models.DailyWeight;
import com.cs360.weighttracker.domain.models.GoalType;
import com.cs360.weighttracker.domain.models.User;
import com.cs360.weighttracker.presentation.goals.GoalsActivity;
import com.cs360.weighttracker.presentation.login.LoginActivity;
import com.cs360.weighttracker.presentation.validators.PhoneNumberValidator;

public class ProfileActivity extends AppCompatActivity {

    private TextView weightProgressTextView, numberTextView;
    private ProgressBar weightChangeProgressBar;

    private Switch notificationSwitch;
    private Button updateProfileButton, logoutButton, updateNumberButton;
    private ImageButton navigateBackButton;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    MilestoneRepository repository;
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Repository is set to the application lifetime.
        repository = MilestoneRepository.getInstance(this.getApplicationContext());
        currentUser = repository.getCurrentUser();
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        handleUserNumberAndToggleSMSOn();
                    } else {
                        // Revert the switch visually because they denied it
                        notificationSwitch.setChecked(false);

                        // If the user denied the permission and show rationale is false, it means they permanently blocked it.
                        // in which case we display the dialog for opening setting and manually switching the permission
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
                            showSettingsRedirectDialog();
                        }
                    }
                });

        setupUI();
        setupEvents();
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Prefill the ui here to refresh the data when the ui changes like when the
        // user updates their info and comes back
        currentUser = repository.getCurrentUser();
        prefillUI();
        updateProgress();
    }

    /**
     * Query and attach each view instance from XML layout.
     */
    private void setupUI() {
        weightChangeProgressBar = findViewById(R.id.pbProfileWeightChange);
        weightProgressTextView = findViewById(R.id.tvProfileWeightChange);
        navigateBackButton = findViewById(R.id.btnProfileNavigateBack);
        logoutButton = findViewById(R.id.btnProfileLogout);
        updateProfileButton = findViewById(R.id.btnProfileUpdateProfile);
        notificationSwitch = findViewById(R.id.switchProfileNotificationSetting);
        numberTextView = findViewById(R.id.tvProfileNotificationNumber);
        updateNumberButton = findViewById(R.id.btnProfileUpdateNumber);

        // Setup UI Text
        // Redundant as already done in onStart
//        prefillUI();

        // Update progress
        updateProgress();
    }

    private void prefillUI() {
        TextView fullNameTextView = findViewById(R.id.tvProfileFullName);
        // If the user has no full name, then use their username(This is highly unlikely).
        String fullName = currentUser.fullName;
        fullName = !fullName.isEmpty() ? fullName : currentUser.getUserName();
        fullNameTextView.setText(fullName);

        // If the user has a phone number set, the update the number the textview with their number
        if (!currentUser.phoneNumber.isEmpty())
            numberTextView.setText(getString(R.string.sms_number_text, currentUser.phoneNumber));
        // Update the notification setting
        notificationSwitch.setChecked(repository.getUserNotificationSetting());
    }


    private void updateProgress() {
        GoalWeight currentGoal = repository.getUserGoalWeight();
        DailyWeight latestWeight = repository.getUserLatestLoggedWeight();

        // If the user has not logged weight use the initial weight as the current weight
        float currentWeight = latestWeight == null ? currentGoal.getCurrentWeight() : latestWeight.userWeight;

        if (currentGoal.getGoalType() == GoalType.WEIGHT_LOSS) {
            // Get current weight returns the user's initial weight
            // and we compare it the user's logged weight to get the progress
            float weightLost = currentGoal.getCurrentWeight() - currentWeight;
            float goalTotal = currentGoal.getCurrentWeight() - currentGoal.getGoalWeight();
            weightProgressTextView.setText(this.getString(R.string.weight_loss_progress, weightLost, goalTotal));
            int progressPercentage = (int) (weightLost / goalTotal * 100);
            int progress = Math.max(0, Math.min(100, progressPercentage));
            weightChangeProgressBar.setProgress(progress);
        } else {
            // For weight gain the current weight will be greater than the initial weight
            // else the progress will show negative values.
            float weightGained = currentWeight - currentGoal.getCurrentWeight();
            float goalTotal = currentGoal.getGoalWeight() - currentGoal.getCurrentWeight();
            weightProgressTextView.setText(this.getString(R.string.weight_gain_progress, weightGained, goalTotal));

            int progressPercentage = (int) (weightGained / goalTotal * 100);
            int progress = Math.max(0, Math.min(100, progressPercentage));
            weightChangeProgressBar.setProgress(progress);
        }
    }


    /**
     * Sets up the event listeners necessary for all the views.
     */
    private void setupEvents() {
        logoutButton.setOnClickListener(view -> {
            repository.logoutUser();
            navigateToLogin();
        });
        // Navigate back by popping out the current activity
        navigateBackButton.setOnClickListener(view -> {
            finish();
        });
        updateProfileButton.setOnClickListener(view -> {
            navigateToGoals();
        });

        notificationSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            // Only ask permission for toggling it off
            if (isChecked) {
                handleSmsPermission(); // Only ask if they are turning it ON
            } else {
                // User turned it OFF. Just update the setting to false.
                // repository.setSmsEnabled(false);
                toggleSMSSetting(false);
            }
        });

        updateNumberButton.setOnClickListener(view -> {
            showAddPhoneDialog();
        });

    }


    /**
     * Handles the SMS permission.
     */
    private void handleSmsPermission() {
        String permission = Manifest.permission.SEND_SMS;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            handleUserNumberAndToggleSMSOn();
        } else if (shouldShowRequestPermissionRationale(permission)) {
            // The permission is denied once
            showRationaleDialog();
        } else {
            // Asking user for permission for the first time.
            requestPermissionLauncher.launch(permission);
        }
    }


    /**
     * Handles the user's SMS preference and toggles sms setting accordingly
     */
    private void handleUserNumberAndToggleSMSOn() {
        // Check if user has a phone number
        // if they don't have, request for a phone number
        if (currentUser.phoneNumber.isEmpty()) {
            showAddPhoneDialog(); // Show the dialog
        } else {// If we have permission and user has phone number, turn it on!
            toggleSMSSetting(true);
        }
    }


    /**
     * Shows a dialog to enable permission to the user
     * if the system allow the permission rationale(permission popup) to be shown.
     */
    private void showRationaleDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.sms_permission_dialog_title)
                .setMessage(R.string.sms_permission_body)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    // Request the system popup again
                    requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // Toggle the switch to off since the user denied permission
                    notificationSwitch.setChecked(false);
                    // Implicitly done
                })
                .show();
    }


    /**
     * Shows a phone number add/update dialog.
     * Due to the coupled nature of the dialog, this layout is kept as a standalone function
     * rather than refactoring out.
     */
    private void showAddPhoneDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_phone_number, null);
        EditText phoneNumberEditText = dialogView.findViewById(R.id.etAddPhoneNumberDialogNumber);
        TextView phoneNumberErrorTextView = dialogView.findViewById(R.id.tvAddPhoneNumberDialogNumberError);

        // If the user already has a phone number update the edit text with it
        phoneNumberEditText.setText(currentUser.phoneNumber);
        int positiveActionTextRes = currentUser.phoneNumber.isEmpty() ? R.string.add_number : R.string.update_number;

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_phone_number)
                .setView(dialogView)
                .setPositiveButton(positiveActionTextRes, (dialog, which) -> {
                    String phoneNumber = phoneNumberEditText.getText().toString();
                    // Inline phone validation
                    if (!PhoneNumberValidator.validate(phoneNumber)) {
                        phoneNumberErrorTextView.setVisibility(View.VISIBLE);
                        phoneNumberErrorTextView.setText(R.string.phone_number_error);
                        // We must toggle the notification switch to off.
                        notificationSwitch.setChecked(false);
                        Toast.makeText(this, R.string.phone_number_error, Toast.LENGTH_LONG).show();

                    } else {
                        // If validation passes, set the user's phone number
                        if (repository.setPhoneNumber(phoneNumber)) {
                            // Update the current user's phone number
                            currentUser = repository.getCurrentUser();
                            toggleSMSSetting(true);
                            // Update the UI manually since our state is not reactive
                            prefillUI();
                            Toast.makeText(this, R.string.phone_number_added_successfully, Toast.LENGTH_LONG).show();

                        }
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // Toggle back the button if there is no phone number
                    if (currentUser.phoneNumber.isEmpty()) {
                        notificationSwitch.setChecked(false);
                        toggleSMSSetting(false);
                    }
                })
                .show();

        // Update the phone number text if the user updates the phone number
        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Remove the error text when we update the weight dialog's edit text
                phoneNumberErrorTextView.setVisibility(View.GONE);
            }
        });
    }


    /**
     * Create a dialog to be displayed when the user permanently denies the permission.
     */
    private void showSettingsRedirectDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_denied_permanently_title)
                .setMessage(R.string.enable_permission_manually)
                .setPositiveButton(R.string.go_to_settings, (dialog, which) -> {
                    // Take them to the settings screen
                    openAppSettings();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // Toggle the switch to off since the user denied permission
                    notificationSwitch.setChecked(false);
                    // Dialog is implicitly closed
                })
                .show();
    }


    /**
     * Open the application settings so that the user can turn on the permission manually
     */
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void toggleSMSSetting(boolean setting) {
        repository.setUserNotificationSetting(setting);
    }


    /**
     * Handles navigation to login after logout.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    /**
     * Handles navigation to goal activity.
     */
    private void navigateToGoals() {
        Intent intent = new Intent(this, GoalsActivity.class);
        intent.putExtra(Constants.GOAL_ACTIVITY_EDIT_FLAG, true);
        startActivity(intent);
    }

}