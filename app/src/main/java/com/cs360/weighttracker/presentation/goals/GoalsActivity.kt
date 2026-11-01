package com.cs360.weighttracker.presentation.goals

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import com.cs360.weighttracker.presentation.Constants
import com.cs360.weighttracker.presentation.HomeActivity
import com.cs360.weighttracker.presentation.validators.WeightValidator.validate
import kotlinx.coroutines.launch
import java.lang.String
import kotlin.Boolean
import kotlin.CharSequence
import kotlin.Exception
import kotlin.Float
import kotlin.Int
import kotlin.Unit

class GoalsActivity : AppCompatActivity() {
    private lateinit var fullNameEditText: EditText
    private lateinit var currentWeightEditText: EditText
    private lateinit var goalWeightEditText: EditText
    private lateinit var getStartedButton: Button
    private lateinit var fullNameErrorTextview: TextView
    private lateinit var currentWeightErrorTextView: TextView
    private lateinit var goalWeightErrorTextView: TextView
    private lateinit var headingTextView: TextView


    private val viewModel: GoalsViewModel by viewModels()
    var isEditMode: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_goals)

        setupUI()
        setupEvents()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        GoalsUIState.CurrentWeightError -> {
                            currentWeightErrorTextView.text =
                                applicationContext.getString(R.string.current_weight_error)
                            currentWeightErrorTextView.visibility = View.VISIBLE
                        }

                        GoalsUIState.FullNameError -> {
                            fullNameErrorTextview.text =
                                applicationContext.getString(R.string.full_name_empty_error)
                            fullNameErrorTextview.visibility = View.VISIBLE
                        }

                        GoalsUIState.GoalWeighError -> {
                            goalWeightErrorTextView.text =
                                applicationContext.getString(R.string.goal_weight_error)
                            goalWeightErrorTextView.visibility = View.VISIBLE
                        }

                        GoalsUIState.IsLoading -> {
                            // TODO: Add loading state
                        }

                        GoalsUIState.SaveSuccess -> {
                            // Navigate back
                            if (isEditMode)
                                finish()
                            else navigateToHome()

                        }

                        is GoalsUIState.InitialDataLoaded -> {
                            // Loaded the data in to UI
                            if (state.fullName.isNotBlank()) {
                                fullNameEditText.setText(state.fullName)
                            }
                            if (state.goalWeight > 0.0) {
                                goalWeightEditText.setText(String.valueOf(state.goalWeight))
                            }
                            if (state.currentWeight > 0.0) {
                                currentWeightEditText.setText(String.valueOf(state.currentWeight))
                            }
                        }

                        else -> {
                            Toast.makeText(this@GoalsActivity, R.string.goal_update_error, Toast.LENGTH_LONG).show()
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
        fullNameEditText = findViewById(R.id.etGoalsFullName)
        currentWeightEditText = findViewById(R.id.etAddWeightDialogAddWeight)
        goalWeightEditText = findViewById(R.id.etGoalsGoalWeight)

        fullNameErrorTextview = findViewById(R.id.tvGoalsFullNameError)
        currentWeightErrorTextView = findViewById(R.id.tvGoalsCurrentWeightError)
        goalWeightErrorTextView = findViewById(R.id.tvGoalsGoalWeightError)
        headingTextView = findViewById(R.id.tvGoalsHeading)

        getStartedButton = findViewById(R.id.btnGoalsGetStarted)

        // Update action name according to starting activity
        // as recognizable from intent's passed-in boolean flag.
        val intent = getIntent()
        if (intent != null) {
            val isEdit = intent.getBooleanExtra(Constants.GOAL_ACTIVITY_EDIT_FLAG, false)
            if (isEdit) {
                getStartedButton.text = getString(R.string.update_profile)
                headingTextView.text = getString(R.string.edit_your_profile)
                isEditMode = true // Flag indicating whether we are in edit mode.
            }
        }

        // Since we have turned on edge to edge we need to add the system inset padding
        // and IME(keyboard) padding to ensure the keyboard popup scroll the ui up
        val goalsScrollView = findViewById<ScrollView>(R.id.goalsScrollView)
        ViewCompat.setOnApplyWindowInsetsListener(
            goalsScrollView
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
                    goalsScrollView.offsetDescendantRectToMyCoords(focusedView, rect)

                    // 3. Smoothly scroll to that exact translated Y position
                    // We subtract a little bit (e.g., 50 pixels) so the field isn't flush against the top
                    goalsScrollView.smoothScrollTo(0, rect.top - 50)
                }
            }, 100)
            WindowInsetsCompat.CONSUMED
        }
    }


    /**
     * Sets up the event listeners necessary for all the views.
     */
    private fun setupEvents() {
        getStartedButton.setOnClickListener { _: View? ->
            saveData()
        }

        // If the text changes after an edit, the error state must be reset
        fullNameEditText.doOnTextChanged { _, _, _, _ -> resetErrorState() }
        currentWeightEditText.doOnTextChanged { _, _, _, _ -> resetErrorState() }
        goalWeightEditText.doOnTextChanged { _, _, _, _ -> resetErrorState() }
    }


    /**
     * Gather the input from the field and trigger the save action.
     */
    private fun saveData() {
        val fullName = fullNameEditText.text.toString()
        val currentWeightStr = currentWeightEditText.text.toString()
        val goalWeightStr = goalWeightEditText.text.toString()
        try {
            val currentWeight = currentWeightStr.toFloat()
            val goalWeight = goalWeightStr.toFloat()
            viewModel.onAction(GoalsActions.Save(fullName, currentWeight, goalWeight))
        } catch (_: Exception) {
            Toast.makeText(this, R.string.invalid_weights, Toast.LENGTH_LONG).show()
        }
    }


    /**
     * Reset the error state of the text views.
     */
    private fun resetErrorState() {
        fullNameErrorTextview.visibility = View.GONE
        currentWeightErrorTextView.visibility = View.GONE
        goalWeightErrorTextView.visibility = View.GONE
    }


    /**
     * Handles navigation to home activity.
     */
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}