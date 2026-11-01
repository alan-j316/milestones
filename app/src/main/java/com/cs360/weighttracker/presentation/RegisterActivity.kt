package com.cs360.weighttracker.presentation

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
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
import com.cs360.weighttracker.presentation.actions.AuthActions
import com.cs360.weighttracker.presentation.states.AuthUiState
import com.cs360.weighttracker.presentation.states.RegisterUIState
import com.cs360.weighttracker.presentation.utils.PasswordVisibilityToggler
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var pageTitleTextView: TextView
    private lateinit var usernameErrorTextView: TextView
    private lateinit var passwordErrorTextView: TextView
    private lateinit var confirmPasswordErrorTextView: TextView
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var passwordVisibilityToggleButton: ImageButton
    private lateinit var confirmPasswordVisibilityToggleButton: ImageButton

    private val viewModel: MilestonesViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_auth)

        setupUI()
        setupEvents()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerUIState.collect { state ->
                    when (state) {
                        is RegisterUIState.IsLoading -> {
                            // TODO: Show loader
                        }
                        is RegisterUIState.RegistrationSuccess -> {
                            // Navigate to goal activity
                            navigateToGoal()
                        }
                        is RegisterUIState.UsernameError -> {
                            // Show username error
                            usernameErrorTextView.text =
                                applicationContext.getString(R.string.invalid_username)
                            usernameErrorTextView.visibility = View.VISIBLE
                        }
                        is RegisterUIState.PasswordError -> {
                            // Show password error
                            passwordErrorTextView.text =
                                applicationContext.getString(R.string.invalid_password)
                            passwordErrorTextView.visibility = View.VISIBLE
                        }
                        is RegisterUIState.PasswordMismatchError -> {
                            // Show password mismatch error
                            confirmPasswordErrorTextView.text =
                                applicationContext.getString(R.string.mismatched_passwords)
                            confirmPasswordErrorTextView.visibility = View.VISIBLE
                        }
                        RegisterUIState.UserExistsError -> {
                            Toast.makeText(
                                this@RegisterActivity,
                                R.string.user_already_exists,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        RegisterUIState.UnknownError -> {
                            // Show error message
                            Toast.makeText(
                                this@RegisterActivity,
                                R.string.unknown_error_signup,
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
        confirmPasswordEditText = findViewById(R.id.etAuthConfirmPassword)
        // We have swapped the button labels to ensure that primary button leads to the primary action(create account)
        registerButton = findViewById(R.id.btnAuthPrimary)
        loginButton = findViewById(R.id.btnAuthSecondary)
        pageTitleTextView = findViewById(R.id.tvAuthTitle)
        usernameErrorTextView = findViewById(R.id.tvAuthUsernameError)
        passwordErrorTextView = findViewById(R.id.tvAuthPasswordError)
        confirmPasswordErrorTextView = findViewById(R.id.tvAuthConfirmPasswordError)
        passwordVisibilityToggleButton = findViewById(R.id.btnAuthPasswordVisibility)
        confirmPasswordVisibilityToggleButton =
            findViewById(R.id.btnAuthConfirmPasswordVisibility)

        // Since we are reusing the layout for login and register, we need to unhide the confirm password field
        // Since the layout is not further manipulated or queried, local variable is sufficient here
        val confirmPasswordLayout = findViewById<LinearLayout>(R.id.layoutAuthConfirmPassword)
        confirmPasswordLayout.visibility = View.VISIBLE

        // Setup UI Text
        // Since we are using the auth layout for both login and register
        // we need to adjust the text accordingly
        pageTitleTextView.text = applicationContext.getString(R.string.register_title)
        registerButton.text = applicationContext.getString(R.string.create_account)
        loginButton.text = applicationContext.getString(R.string.login)


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
        // Finish will pop off this activity from the backstack leaving only with the Login activity
        // from the navigation will take place
        loginButton.setOnClickListener { _: View? -> finish() }
        registerButton.setOnClickListener { _: View? -> register() }

        passwordVisibilityToggleButton.setOnClickListener { _: View? ->
            PasswordVisibilityToggler.togglePasswordState(
                passwordEditText,
                passwordVisibilityToggleButton
            )
        }
        confirmPasswordVisibilityToggleButton.setOnClickListener { _: View? ->
            PasswordVisibilityToggler.togglePasswordState(
                confirmPasswordEditText,
                confirmPasswordVisibilityToggleButton
            )
        }


        usernameEditText.doOnTextChanged { _, _, _, _ ->
            resetErrorState()
        }
        passwordEditText.doOnTextChanged { _, _, _, _ ->
            resetErrorState()
        }
        confirmPasswordEditText.doOnTextChanged { _, _, _, _ ->
            resetErrorState()
        }
    }


    private fun register() {
        // Store the state locally in the function rather than querying it
        // per function call to ensure that the validated username and password
        // is what gets passed down to the login function.
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()
        viewModel.onAction(AuthActions.Register(username, password, confirmPassword))
    }

    private fun navigateToGoal() {
        val intent = Intent(this, GoalsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    /**
     * Reset the error state of the text views.
     */
    private fun resetErrorState() {
        usernameErrorTextView.visibility = View.GONE
        passwordErrorTextView.visibility = View.GONE
        confirmPasswordErrorTextView.visibility = View.GONE
    }
}