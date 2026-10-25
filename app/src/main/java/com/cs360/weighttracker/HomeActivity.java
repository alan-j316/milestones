package com.cs360.weighttracker;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cs360.weighttracker.components.AddWeightDialog;
import com.cs360.weighttracker.components.DeleteWeightItemDialog;
import com.cs360.weighttracker.components.WeightItemAdapter;
import com.cs360.weighttracker.database.MilestoneRepository;
import com.cs360.weighttracker.domain.models.DailyWeight;
import com.cs360.weighttracker.domain.models.GoalType;
import com.cs360.weighttracker.domain.models.User;
import com.cs360.weighttracker.utils.TimeUtils;

import java.util.List;

public class HomeActivity extends AppCompatActivity {


    TextView fullNameTextView, greetingTextView, startWeightTextView, currentWeightTextView, goalWeightTextView;
    Button trackWeightButton;
    ImageView profileImage;
    RecyclerView progressHistoryRecyclerView;
    ProgressBar weightProgressBar;


    MilestoneRepository repository;
    User currentUser;
    GoalWeight userGoal;
    DailyWeight latestWeight;

    WeightItemAdapter adapter;

    LinearLayout noWeightsLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Attach to application context to ensure the repository share the application's lifetime
        // not getting recreated for every shared activity.
        repository = MilestoneRepository.getInstance(getApplicationContext());
        currentUser = repository.getCurrentUser();

        setupUI();
        setupEvents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // We must update the current user to ensure that the navigation from say EditProfile updates the ui
        // since the UI is not reactive right now.
        // Note: This will be better off performed on a background thread with a reactive model like livedata.
        currentUser = repository.getCurrentUser();
        userGoal = repository.getUserGoalWeight();
        latestWeight = repository.getUserLatestLoggedWeight();
        prefillUI();
    }


    /**
     * Query and attach each view instance from XML layout.
     */
    private void setupUI() {
        trackWeightButton = findViewById(R.id.btnHomeTrackWeight);
        fullNameTextView = findViewById(R.id.tvHomeFullName);
        greetingTextView = findViewById(R.id.tvHomeGreeting);
        currentWeightTextView = findViewById(R.id.tvHomeCurrentWeight);
        goalWeightTextView = findViewById(R.id.tvHomeGoalWeight);
        startWeightTextView = findViewById(R.id.tvHomeStartingWeight);
        progressHistoryRecyclerView = findViewById(R.id.rvHomeHistory);
        profileImage = findViewById(R.id.imgHomeProfile);
        weightProgressBar = findViewById(R.id.pbHomeWeightChange);
        noWeightsLayout = findViewById(R.id.layoutHomeNoHistory);

        setupRecyclerView();
    }


    /**
     * Fills the UI with appropriate data.
     */
    private void prefillUI() {
        // Setup UI Text
        String fullName = currentUser.fullName;
        fullNameTextView.setText(fullName);
        // Set the greeting based on time of day
        greetingTextView.setText(TimeUtils.getGreetingResId());

        // If the user goal is not set then we are calling the activity before query the repo
        // so don't populate with anything
        if (userGoal == null) return;
        startWeightTextView.setText(getString(R.string.weight_format, userGoal.getCurrentWeight()));
        goalWeightTextView.setText(getString(R.string.weight_format, userGoal.getGoalWeight()));

        // If the user has no goal weight set, then set the UserGoal's start weight as the current weight
        float currentWeight = latestWeight == null ? userGoal.getCurrentWeight() : latestWeight.userWeight;
        currentWeightTextView.setText(getString(R.string.weight_format, currentWeight));
        int progress = getProgress(currentWeight);
        weightProgressBar.setProgress(progress);
    }


    /**
     * Gets the current weight progress given the user's goals and latest logged weight.
     *
     * @param currentWeight The latest logged weight.
     * @return A number from 0 to 100 indicating the weight progress.
     */
    private int getProgress(float currentWeight) {
        int progress = 0;
        if (userGoal.getGoalType() == GoalType.WEIGHT_LOSS) {
            // Get current weight returns the user's initial weight
            // and we compare it the user's logged weight to get the progress
            float weightLost = userGoal.getCurrentWeight() - currentWeight;
            float goalTotal = userGoal.getCurrentWeight() - userGoal.getGoalWeight();
            int progressPercentage = (int) (weightLost / goalTotal * 100);
            progress = Math.max(0, Math.min(100, progressPercentage));
        } else {
            // For weight gain the current weight will be greater than the initial weight
            // else the progress will show negative values.
            float weightGained = currentWeight - userGoal.getCurrentWeight();
            float goalTotal = userGoal.getGoalWeight() - userGoal.getCurrentWeight();

            int progressPercentage = (int) (weightGained / goalTotal * 100);
            progress = Math.max(0, Math.min(100, progressPercentage));
        }
        return progress;
    }


    private void setupRecyclerView() {
        List<DailyWeight> weightList = repository.getDailyWeights();
        adapter = new WeightItemAdapter(weightList, itemId -> {
            // For deleting the weight, to provide the user with a chance to rethink their
            // show a dialog with a confirmation. On confirmation, delete the weight.
            DeleteWeightItemDialog dialog = new DeleteWeightItemDialog(() -> {
                boolean weightDeleted = repository.deleteDailyWeight(itemId);
                if (weightDeleted) {
                    refreshHistory();
                    latestWeight = repository.getUserLatestLoggedWeight();
                    prefillUI();
                    Toast.makeText(this, "Weight deleted successfully.", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show(getSupportFragmentManager(), Constants.DELETE_WEIGHT_ITEM_DIALOG);
        });
        progressHistoryRecyclerView.setAdapter(adapter);
        refreshHistory(); // Must refresh on initial load
    }


    /**
     * Refresh the weight progress data with new data from the repository.
     */
    private void refreshHistory() {
        List<DailyWeight> newData = repository.getDailyWeights();
        // Show the history view if there is data
        // else show the placeholder rather than a blank screen
        if (newData.isEmpty()) {
            progressHistoryRecyclerView.setVisibility(View.GONE);
            noWeightsLayout.setVisibility(View.VISIBLE);
        } else {
            noWeightsLayout.setVisibility(View.GONE);
            progressHistoryRecyclerView.setVisibility(View.VISIBLE);
            adapter.updateData(newData);
        }
    }


    /**
     * Sets up the event listeners necessary for all the views.
     */
    private void setupEvents() {
        // Finish will pop off this activity from the backstack leaving only with the Login activity
        // from the navigation will take place
        trackWeightButton.setOnClickListener(view -> {
            AddWeightDialog dialog = new AddWeightDialog();
            dialog.show(getSupportFragmentManager(), Constants.ADD_NEW_WEIGHT_DIALOG);
        });

        // Add event listener for the dialog
        getSupportFragmentManager().setFragmentResultListener(Constants.ADD_NEW_WEIGHT_DIALOG, this, ((requestKey, result) -> {
            float weight = result.getFloat(Constants.NEW_WEIGHT_BUNDLE_KEY);

            if (repository.logDailyWeight(weight)) {
                refreshHistory();
                latestWeight = repository.getUserLatestLoggedWeight();
                prefillUI();
                // Send a sms if the user surpassed or reached the goal weight and have the permission and phone number set
                if (hasReachGoalWeight(weight)) {
                    boolean shouldSendNotification = repository.getUserNotificationSetting();
                    if (shouldSendNotification) {
                        // Check if the user has granted permission
                        // Note: We are not handling the permission here since if hte user has their preference as off
                        // or has manually turned off the permission, we can assume that they don't need the feature
                        // but we can show them an error message
                        boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
                        String phoneNumber = currentUser.phoneNumber;
                        if (hasPermission && !phoneNumber.trim().isEmpty()) {
                            try {
                                GoalWeight userGoal = repository.getUserGoalWeight();
                                SmsManager smsManager;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    smsManager = this.getSystemService(SmsManager.class);
                                } else {
                                    // Fallback for older Android versions like Android 7
                                    smsManager = SmsManager.getDefault();
                                }
                                // We are using hte latest weight as it can surpass the user's current weight!
                                String message = getString(R.string.congratulations_sms, currentUser.fullName, userGoal.getCurrentWeight(), weight);
                                if (smsManager != null) {
                                    smsManager.sendTextMessage(phoneNumber, null, message, null, null); //
                                    Toast.makeText(this, R.string.sms_sent_successfully, Toast.LENGTH_SHORT).show(); //
                                }
                            } catch (Exception e) {
                                Toast.makeText(this, R.string.failed_to_send_sms, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    Toast.makeText(this, this.getString(R.string.congratulations), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, this.getString(R.string.weight_added_successfully), Toast.LENGTH_SHORT).show();
                }
            }
        }));

        profileImage.setOnClickListener(view -> navigateToProfile());
    }

    /**
     * Returns a boolean indicating whether the user has reached a goal weight depending on the type of goal(loss/gain)
     *
     * @param newWeight The latest logged weight.
     */
    private boolean hasReachGoalWeight(float newWeight) {
        GoalWeight weightGoal = repository.getUserGoalWeight();
        // In case of weight gain we need to check if current logged weight is greater or equal to
        // the goal weight and weight loss if the logged weight is less than or equal
        if (weightGoal.getGoalType() == GoalType.WEIGHT_GAIN) {
            return newWeight >= weightGoal.getGoalWeight();
        } else {
            return newWeight <= weightGoal.getGoalWeight();
        }
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
}