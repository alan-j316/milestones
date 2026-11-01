package com.cs360.weighttracker.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cs360.weighttracker.R;
import com.cs360.weighttracker.database.MilestoneRepository;
import com.cs360.weighttracker.domain.models.User;
import com.cs360.weighttracker.presentation.validators.WeightValidator;

public class GoalsActivity extends AppCompatActivity {


    private EditText fullNameEditText, currentWeightEditText, goalWeightEditText;
    private Button getStartedButton;
    private TextView fullNameErrorTextview, currentWeightErrorTextView, goalWeightErrorTextView, headingTextView;

    MilestoneRepository repository;
    boolean isEditMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_goals);

        // Attach to application context to ensure the repository share the application's lifetime
        // not getting recreated for every shared activity.
        repository = MilestoneRepository.getInstance(getApplicationContext());

        setupUI();
        setupEvents();
    }


    /**
     * Query and attach each view instance from XML layout.
     */
    private void setupUI() {
        fullNameEditText = findViewById(R.id.etGoalsFullName);
        currentWeightEditText = findViewById(R.id.etAddWeightDialogAddWeight);
        goalWeightEditText = findViewById(R.id.etGoalsGoalWeight);

        fullNameErrorTextview = findViewById(R.id.tvGoalsFullNameError);
        currentWeightErrorTextView = findViewById(R.id.tvGoalsCurrentWeightError);
        goalWeightErrorTextView = findViewById(R.id.tvGoalsGoalWeightError);
        headingTextView = findViewById(R.id.tvGoalsHeading);

        getStartedButton = findViewById(R.id.btnGoalsGetStarted);

        prefillData();

        // Update action name according to starting activity
        // as recognizable from intent's passed-in boolean flag.
        Intent intent = getIntent();
        if (intent != null) {
            boolean isEdit = intent.getBooleanExtra(Constants.GOAL_ACTIVITY_EDIT_FLAG, false);
            if (isEdit) {
                getStartedButton.setText(getString(R.string.update_profile));
                headingTextView.setText(getString(R.string.edit_your_profile));
                isEditMode = true; // Flag indicating whether we are in edit mode.
            }
        }

        // Since we have turned on edge to edge we need to add the system inset padding
        // and IME(keyboard) padding to ensure the keyboard popup scroll the ui up
        ScrollView goalsScrollView = findViewById(R.id.goalsScrollView);
        ViewCompat.setOnApplyWindowInsetsListener(goalsScrollView, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());

            // If keyboard is open, push the scroll view bounds up by the keyboard height
            int bottomPadding = (imeInsets.bottom > 0) ? imeInsets.bottom : systemBars.bottom;

            // Apply to the scrollview container
            view.setPadding(
                    systemBars.left,
                    systemBars.top,  // Protects your status bar / top toolbar
                    systemBars.right,
                    bottomPadding    // Dynamically adjusts when keyboard shows up
            );

            // The padding is getting applied, but we need to find the focus element
            // and manually scroll to ensure that the edit text is not behind the soft keyboard.
            view.postDelayed(() -> {
                View focusedView = view.findFocus();
                if (focusedView != null) {
                    // Get the exact bounds of the focused EditText
                    android.graphics.Rect rect = new android.graphics.Rect();
                    focusedView.getDrawingRect(rect);

                    // 2. Map those nested coordinates up to the ScrollView's coordinate space
                    goalsScrollView.offsetDescendantRectToMyCoords(focusedView, rect);

                    // 3. Smoothly scroll to that exact translated Y position
                    // We subtract a little bit (e.g., 50 pixels) so the field isn't flush against the top
                    goalsScrollView.smoothScrollTo(0, rect.top - 50);
                }
            }, 100);

            return WindowInsetsCompat.CONSUMED;
        });
    }


    /**
     * If the user has already data with respect to their name and goal
     * then that data will be populated in the UI.
     */
    private void prefillData() {
        User currentUser = repository.getCurrentUser();
        if (currentUser == null)
            return; // This will not hit with our current flow but left here for safety from NPE
        GoalWeight weight = repository.getUserGoalWeight();

        if (!currentUser.fullName.isEmpty())
            fullNameEditText.setText(currentUser.fullName);

        if (weight != null) {
            currentWeightEditText.setText(String.valueOf(weight.getCurrentWeight()));
            goalWeightEditText.setText(String.valueOf(weight.getGoalWeight()));
        }
    }


    /**
     * Sets up the event listeners necessary for all the views.
     */
    private void setupEvents() {
        getStartedButton.setOnClickListener(view -> {
            getStarted();
        });

        // If the text changes after an edit, the error state must be reset
        fullNameEditText.addTextChangedListener(new ResetErrorStateTextWatcher());
        currentWeightEditText.addTextChangedListener(new ResetErrorStateTextWatcher());
        goalWeightEditText.addTextChangedListener(new ResetErrorStateTextWatcher());
    }


    /**
     * Handles event propagation from Get Started Button.
     */
    private void getStarted() {
        String fullName = fullNameEditText.getText().toString();
        String currentWeightStr = currentWeightEditText.getText().toString();
        String goalWeightStr = goalWeightEditText.getText().toString();
        try {
            float currentWeight = Float.parseFloat(currentWeightStr);
            float goalWeight = Float.parseFloat(goalWeightStr);

            boolean isInputValid = validateInputs(fullName, currentWeight, goalWeight);
            if (isInputValid) {
                if (saveUserData(fullName, currentWeight, goalWeight))
                    if (isEditMode) {
                        finish(); // If we are in edit mode, just pop the back stack to the previous view.
                    } else {
                        navigateToHome();
                    }
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.invalid_weights, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Saves the user data to the repository.
     *
     * @param fullName      The user's full name.
     * @param currentWeight The user's current weight.
     * @param goalWeight    The user's goal weight.
     * @return A boolean indicating whether the user data was updated.
     */
    private boolean saveUserData(String fullName, float currentWeight, float goalWeight) {
        // Update the name first and check for process success
        boolean nameUpdated = repository.updateCurrentUserFullName(fullName);
        if (!nameUpdated) {
            Toast.makeText(this, R.string.name_update_error, Toast.LENGTH_LONG).show();
            return false;
        }
        // Update then update the goals and check for process success
        boolean goalUpdated = repository.upsertGoalWeight(currentWeight, goalWeight);
        if (!goalUpdated) {
            Toast.makeText(this, R.string.goal_update_error, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    /**
     * Validates the user input.
     *
     * @param fullName   The user's full name.
     * @param curWeight  The user's current weight.
     * @param goalWeight The user's goal weight.
     * @return Boolean whether all the fields are valid.
     */
    private boolean validateInputs(String fullName, float curWeight, float goalWeight) {
        boolean isInputValid = true;
        if (fullName.isEmpty()) {
            fullNameErrorTextview.setText(getApplicationContext().getString(R.string.full_name_empty_error));
            fullNameErrorTextview.setVisibility(View.VISIBLE);
            isInputValid = false;
        }

        if (!WeightValidator.validate(curWeight)) {
            currentWeightErrorTextView.setText(getApplicationContext().getString(R.string.current_weight_error));
            currentWeightErrorTextView.setVisibility(View.VISIBLE);
            isInputValid = false;
        }

        if (!WeightValidator.validate(goalWeight)) {
            goalWeightErrorTextView.setText(getApplicationContext().getString(R.string.goal_weight_error));
            goalWeightErrorTextView.setVisibility(View.VISIBLE);
            isInputValid = false;
        }
        return isInputValid;
    }


    /**
     * TextWatcher for clearing error states when username or password updates.
     */
    private class ResetErrorStateTextWatcher implements TextWatcher {

        @Override
        public void afterTextChanged(Editable editable) {
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            resetErrorState();
        }
    }


    /**
     * Reset the error state of the text views.
     */
    private void resetErrorState() {
        fullNameErrorTextview.setVisibility(View.GONE);
        currentWeightErrorTextView.setVisibility(View.GONE);
        goalWeightErrorTextView.setVisibility(View.GONE);
    }


    /**
     * Handles navigation to home activity.
     */
    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}