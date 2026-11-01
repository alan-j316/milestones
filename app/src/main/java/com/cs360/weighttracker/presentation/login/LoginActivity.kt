package com.cs360.weighttracker.presentation.login

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cs360.weighttracker.R
import com.cs360.weighttracker.presentation.goals.GoalsActivity
import com.cs360.weighttracker.presentation.HomeActivity
import com.cs360.weighttracker.presentation.register.RegisterActivity
import com.cs360.weighttracker.presentation.utils.PasswordVisibilityToggler
import kotlinx.coroutines.launch
import kotlin.getValue

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameErrorTextView: TextView
    private lateinit var passwordErrorTextView: TextView
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var passwordVisibilityToggleButton: ImageButton


    private val viewModel: LoginViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_auth)

        setupUI()
        setupEvents()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        LoginUIState.GoalNotSet -> {
                            // Navigate to goal screen
                            navigateToGoals()
                        }

                        LoginUIState.IsLoading -> {
                            // TODO: Show loader
                        }

                        LoginUIState.LogInSuccess -> {
                            // Navigate to home screen
                            navigateToHome()
                        }

                        LoginUIState.UsernameError -> {
                            // Show username error
                            usernameErrorTextView.text =
                                applicationContext.getString(R.string.no_user_match)
                            usernameErrorTextView.visibility = View.VISIBLE
                        }

                        LoginUIState.PasswordError -> {
                            // Show password error
                            passwordErrorTextView.text =
                                applicationContext.getString(R.string.incorrect_password)
                            passwordErrorTextView.visibility = View.VISIBLE
                        }

                        LoginUIState.UnknownError -> {
                            // Show unknown error
                            Toast.makeText(
                                this@LoginActivity,
                                R.string.unknown_error_signin,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }


    /**
     * Query and attach each view instance from XML layout.
     */
    private fun setupUI() {
        usernameEditText = findViewById(R.id.etAuthUsername)
        passwordEditText = findViewById(R.id.etAuthPassword)
        loginButton = findViewById(R.id.btnAuthPrimary)
        registerButton = findViewById(R.id.btnAuthSecondary)
        usernameErrorTextView = findViewById(R.id.tvAuthUsernameError)
        passwordErrorTextView = findViewById(R.id.tvAuthPasswordError)
        passwordVisibilityToggleButton = findViewById(R.id.btnAuthPasswordVisibility)

        // Setup UI Text
        // Since we are using the auth layout for both login and register
        // we need to adjust the text accordingly
        val pageTitleTextView = findViewById<TextView>(R.id.tvAuthTitle)
        pageTitleTextView.text = applicationContext.getString(R.string.login_title)
        loginButton.text = applicationContext.getString(R.string.login)
        registerButton.text = applicationContext.getString(R.string.create_account)


        // Since we have turned on edge to edge we need to add the system inset padding
        // and IME(keyboard) padding to ensure the keyboard popup scroll the ui up
        val authScrollView = findViewById<ScrollView>(R.id.authScrollView)
        ViewCompat.setOnApplyWindowInsetsListener(
            authScrollView
        ) { view: View?, windowInsets: WindowInsetsCompat? ->
            val systemBars = windowInsets!!.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            // If keyboard is open, push the scroll view bounds up by the keyboard height
            val bottomPadding =
                if (imeInsets.bottom > 0) imeInsets.bottom else systemBars.bottom

            // Apply to the scrollview container
            view!!.setPadding(
                systemBars.left,
                systemBars.top,  // Protects your status bar / top toolbar
                systemBars.right,
                bottomPadding // Dynamically adjusts when keyboard shows up
            )

            // The padding is getting applied, but we need to find the focus element
            // and manually scroll to ensure that the edit text is not behind the soft keyboard.
            view.postDelayed({
                val focusedView = view.findFocus()
                if (focusedView != null) {
                    // Get the exact bounds of the focused EditText
                    val rect = Rect()
                    focusedView.getDrawingRect(rect)

                    // 2. Map those nested coordinates up to the ScrollView's coordinate space
                    authScrollView.offsetDescendantRectToMyCoords(focusedView, rect)

                    // 3. Smoothly scroll to that exact translated Y position
                    // We subtract a little bit (e.g., 50 pixels) so the field isn't flush against the top
                    authScrollView.smoothScrollTo(0, rect.top - 50)
                }
            }, 100)
            WindowInsetsCompat.CONSUMED
        }
    }


    /**
     * Sets up the event listeners necessary for all the views.
     */
    private fun setupEvents() {
        loginButton.setOnClickListener { _: View ->
            login()
        }
        registerButton.setOnClickListener { _: View ->
            navigateToRegister()
        }
        passwordVisibilityToggleButton.setOnClickListener { _: View ->
            PasswordVisibilityToggler.togglePasswordState(
                passwordEditText,
                passwordVisibilityToggleButton
            )
        }

        // If the text changes after an edit, the error state must be reset
        usernameEditText.doOnTextChanged { _, _, _, _ -> resetErrorState() }
        passwordEditText.doOnTextChanged { _, _, _, _ -> resetErrorState() }
    }


    /**
     * Performs validation on the login credentials and logs the user into the application.
     */
    private fun login() {
        // Store the state locally in the function rather than querying it
        // per function call to ensure that the validated username and password
        // is what gets passed down to the login function.
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        viewModel.onAction(LoginActions.Login(username, password))

    }


    /**
     * Reset the error state of the text views.
     */
    private fun resetErrorState() {
        usernameErrorTextView.visibility = View.GONE
        passwordErrorTextView.visibility = View.GONE
    }


    /**
     * Handles navigation to goals activity.
     * 
     * @implNote The "navigation backstack" is cleared.
     */
    private fun navigateToGoals() {
        val intent = Intent(this, GoalsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    /**
     * Handles user to the main(home) activity.
     * 
     * @implNote The "navigation backstack" is cleared.
     */
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    /**
     * Handles navigation to the register activity.
     * 
     * @implNote This will not clear the "navigation stack" i.e,
     * the register activity will be placed on top of the login screen.
     */
    private fun navigateToRegister() {
        val i = Intent(this, RegisterActivity::class.java)
        startActivity(i)
    }
}