package com.cs360.weighttracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cs360.weighttracker.database.MilestoneRepository;
import com.cs360.weighttracker.database.status.LoginStatus;
import com.cs360.weighttracker.models.User;
import com.cs360.weighttracker.utils.PasswordVisibilityToggler;
import com.cs360.weighttracker.validators.PasswordValidator;
import com.cs360.weighttracker.validators.UsernameValidator;

public class LoginActivity extends AppCompatActivity {


    private TextView usernameErrorTextView;
    private TextView passwordErrorTextView;
    private EditText usernameEditText, passwordEditText;
    private Button loginButton, registerButton;
    private ImageButton passwordVisibilityToggleButton;

    MilestoneRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

        // Attach to application context to ensure the repository share the application's lifetime
        // not getting recreated for every shared activity.
        repository = MilestoneRepository.getInstance(getApplicationContext());
        // Repo must be initialized before navigation (else NPE can occur)
        navigateOnUserAuth();

        setupUI();
        setupEvents();

    }


    /**
     * Query and attach each view instance from XML layout.
     */
    private void setupUI() {
        usernameEditText = findViewById(R.id.etAuthUsername);
        passwordEditText = findViewById(R.id.etAuthPassword);
        loginButton = findViewById(R.id.btnAuthPrimary);
        registerButton = findViewById(R.id.btnAuthSecondary);
        usernameErrorTextView = findViewById(R.id.tvAuthUsernameError);
        passwordErrorTextView = findViewById(R.id.tvAuthPasswordError);
        passwordVisibilityToggleButton = findViewById(R.id.btnAuthPasswordVisibility);

        // Setup UI Text
        // Since we are using the auth layout for both login and register
        // we need to adjust the text accordingly
        TextView pageTitleTextView = findViewById(R.id.tvAuthTitle);
        pageTitleTextView.setText(getApplicationContext().getString(R.string.login_title));
        loginButton.setText(getApplicationContext().getString(R.string.login));
        registerButton.setText(getApplicationContext().getString(R.string.create_account));


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
        loginButton.setOnClickListener(view -> {
            login();
        });
        registerButton.setOnClickListener(view -> {
            navigateToRegister();
        });
        passwordVisibilityToggleButton.setOnClickListener(view -> {
            PasswordVisibilityToggler.togglePasswordState(passwordEditText, passwordVisibilityToggleButton);
        });

        // If the text changes after an edit, the error state must be reset
        passwordEditText.addTextChangedListener(new ResetErrorStateTextWatcher());
        usernameEditText.addTextChangedListener(new ResetErrorStateTextWatcher());
    }


    /**
     * Performs validation on the login credentials.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return A boolean indicating whether the validation was successful.
     */
    private boolean validateLogin(String username, String password) {

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

        return isValidLogin;
    }


    /**
     * Performs validation on the login credentials and logs the user into the application.
     */
    private void login() {
        // Store the state locally in the function rather than querying it
        // per function call to ensure that the validated username and password
        // is what gets passed down to the login function.
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        boolean isLoginValid = validateLogin(username, password);
        if (isLoginValid) {
            LoginStatus status = repository.loginUser(username, password);

            switch (status) {
                case USERNAME_ERROR:
                    usernameErrorTextView.setText(getApplicationContext().getString(R.string.no_user_match));
                    usernameErrorTextView.setVisibility(View.VISIBLE);
                    break;
                case PASSWORD_ERROR:
                    passwordErrorTextView.setText(getApplicationContext().getString(R.string.incorrect_password));
                    passwordErrorTextView.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    // We are not navigating to goals activity
                    // since we assume that the user has goal set
                    // indicated by the login rather than a register.
                    navigateToHome();
                    break;
                case UNKNOWN_FAILURE:
                    Toast.makeText(this, R.string.unknown_error_signin, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
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
        usernameErrorTextView.setVisibility(View.GONE);
        passwordErrorTextView.setVisibility(View.GONE);
    }


    /**
     * Handles navigation based on user session and whether user has a goal set.
     */
    private void navigateOnUserAuth() {
        User user = repository.getCurrentUser();
        if (user != null) {
            boolean userHasGoalSet = repository.userHasGoalSet(user.userId);
            if (userHasGoalSet)
                navigateToHome();
            else
                navigateToGoals();
        }
    }


    /**
     * Handles navigation to goals activity.
     *
     * @implNote The "navigation backstack" is cleared.
     */
    private void navigateToGoals() {
        Intent intent = new Intent(this, GoalsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    /**
     * Handles user to the main(home) activity.
     *
     * @implNote The "navigation backstack" is cleared.
     */
    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    /**
     * Handles navigation to the register activity.
     *
     * @implNote This will not clear the "navigation stack" i.e,
     * the register activity will be placed on top of the login screen.
     */
    private void navigateToRegister() {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

}