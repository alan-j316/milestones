package com.cs360.weighttracker.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.cs360.weighttracker.domain.status.RegisterStatus;
import com.cs360.weighttracker.presentation.utils.PasswordVisibilityToggler;
import com.cs360.weighttracker.presentation.validators.PasswordValidator;
import com.cs360.weighttracker.presentation.validators.UsernameValidator;

public class RegisterActivity extends AppCompatActivity {

    TextView pageTitleTextView, usernameErrorTextView, passwordErrorTextView, confirmPasswordErrorTextView;
    EditText usernameEditText, passwordEditText, confirmPasswordEditText;
    Button loginButton, registerButton;
    ImageButton passwordVisibilityToggleButton, confirmPasswordVisibilityToggleButton;

    MilestoneRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

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
        usernameEditText = findViewById(R.id.etAuthUsername);
        passwordEditText = findViewById(R.id.etAuthPassword);
        confirmPasswordEditText = findViewById(R.id.etAuthConfirmPassword);
        // We have swapped the button labels to ensure that primary button leads to the primary action(create account)
        registerButton = findViewById(R.id.btnAuthPrimary);
        loginButton = findViewById(R.id.btnAuthSecondary);
        pageTitleTextView = findViewById(R.id.tvAuthTitle);
        usernameErrorTextView = findViewById(R.id.tvAuthUsernameError);
        passwordErrorTextView = findViewById(R.id.tvAuthPasswordError);
        confirmPasswordErrorTextView = findViewById(R.id.tvAuthConfirmPasswordError);
        passwordVisibilityToggleButton = findViewById(R.id.btnAuthPasswordVisibility);
        confirmPasswordVisibilityToggleButton = findViewById(R.id.btnAuthConfirmPasswordVisibility);

        // Since we are reusing the layout for login and register, we need to unhide the confirm password field
        // Since the layout is not further manipulated or queried, local variable is sufficient here
        LinearLayout confirmPasswordLayout = findViewById(R.id.layoutAuthConfirmPassword);
        confirmPasswordLayout.setVisibility(View.VISIBLE);

        // Setup UI Text
        // Since we are using the auth layout for both login and register
        // we need to adjust the text accordingly
        pageTitleTextView.setText(getApplicationContext().getString(R.string.register_title));
        registerButton.setText(getApplicationContext().getString(R.string.create_account));
        loginButton.setText(getApplicationContext().getString(R.string.login));


        // Since we have turned on edge to edge we need to add the system inset padding
        // and IME(keyboard) padding to ensure the keyboard popup scroll the ui up
        ScrollView authScrollView = findViewById(R.id.authScrollView);
        ViewCompat.setOnApplyWindowInsetsListener(authScrollView, (view, windowInsets) -> {
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
                    authScrollView.offsetDescendantRectToMyCoords(focusedView, rect);

                    // 3. Smoothly scroll to that exact translated Y position
                    // We subtract a little bit (e.g., 50 pixels) so the field isn't flush against the top
                    authScrollView.smoothScrollTo(0, rect.top - 50);
                }
            }, 100);

            return WindowInsetsCompat.CONSUMED;
        });
    }


    /**
     * Sets up the event listeners necessary for all the views.
     */
    private void setupEvents() {
        // Finish will pop off this activity from the backstack leaving only with the Login activity
        // from the navigation will take place
        loginButton.setOnClickListener(view -> finish());
        registerButton.setOnClickListener(view -> register());

        passwordVisibilityToggleButton.setOnClickListener(view -> {
            PasswordVisibilityToggler.togglePasswordState(passwordEditText, passwordVisibilityToggleButton);
        });
        confirmPasswordVisibilityToggleButton.setOnClickListener(view -> {
            PasswordVisibilityToggler.togglePasswordState(confirmPasswordEditText, confirmPasswordVisibilityToggleButton);
        });


        usernameEditText.addTextChangedListener(new ResetErrorStateTextWatcher());
        passwordEditText.addTextChangedListener(new ResetErrorStateTextWatcher());
        confirmPasswordEditText.addTextChangedListener(new ResetErrorStateTextWatcher());
    }


    private void register() {
        // Store the state locally in the function rather than querying it
        // per function call to ensure that the validated username and password
        // is what gets passed down to the login function.
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        boolean isLoginValid = validateLogin(username, password, confirmPassword);
        if (isLoginValid) {
            RegisterStatus status = repository.registerUser(username, password);

            switch (status) {
                case USER_EXISTS:
                    usernameErrorTextView.setText(getApplicationContext().getString(R.string.no_user_match));
                    usernameErrorTextView.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    // We are not navigating to goals activity
                    // since we assume that the user has goal set
                    // indicated by the login rather than a register.
                    navigateToGoal();
                    break;
                case UNKNOWN_FAILURE:
                    Toast.makeText(this, R.string.unknown_error_signup, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    }

    private void navigateToGoal() {
        Intent intent = new Intent(this, GoalsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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

    private boolean validateLogin(String username, String password, String confirmPassword) {

        boolean isValidLogin = true;
        if (!UsernameValidator.validate(username)) {
            usernameErrorTextView.setText(getApplicationContext().getString(R.string.invalid_username));
            usernameErrorTextView.setVisibility(View.VISIBLE);
            isValidLogin = false;
        }

        if (!PasswordValidator.validate(password)) {
            passwordErrorTextView.setText(getApplicationContext().getString(R.string.invalid_password));
            passwordErrorTextView.setVisibility(View.VISIBLE);
            isValidLogin = false;
        }

        if (!PasswordValidator.validate(confirmPassword) || !password.equals(confirmPassword)) {
            confirmPasswordErrorTextView.setText(getApplicationContext().getString(R.string.mismatched_passwords));
            confirmPasswordErrorTextView.setVisibility(View.VISIBLE);
            isValidLogin = false;
        }

        return isValidLogin;
    }


    /**
     * Reset the error state of the text views.
     */
    private void resetErrorState() {
        usernameErrorTextView.setVisibility(View.GONE);
        passwordErrorTextView.setVisibility(View.GONE);
        confirmPasswordErrorTextView.setVisibility(View.GONE);
    }

}