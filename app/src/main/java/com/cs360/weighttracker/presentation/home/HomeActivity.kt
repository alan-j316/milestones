package com.cs360.weighttracker.presentation.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.cs360.weighttracker.R
import com.cs360.weighttracker.domain.models.DailyWeight
import com.cs360.weighttracker.domain.models.GoalType
import com.cs360.weighttracker.presentation.Constants
import com.cs360.weighttracker.presentation.ProfileActivity
import com.cs360.weighttracker.presentation.components.WeightItemAdapter
import com.cs360.weighttracker.presentation.utils.TimeUtils
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.jvm.java


class HomeActivity : AppCompatActivity() {
    private lateinit var fullNameTextView: TextView
    private lateinit var greetingTextView: TextView
    private lateinit var startWeightTextView: TextView
    private lateinit var currentWeightTextView: TextView
    private lateinit var goalWeightTextView: TextView
    private lateinit var trackWeightButton: android.widget.Button
    private lateinit var profileImage: android.widget.ImageView
    private lateinit var progressHistoryRecyclerView: RecyclerView
    private lateinit var weightProgressBar: ProgressBar
    private lateinit var adapter: WeightItemAdapter
    private lateinit var noWeightsLayout: LinearLayout

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        setupUI()
        setupEvents()


        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        HomeUIState.IsLoading -> {
                            // TODO: Add loading spinner
                        }

                        is HomeUIState.GoalWeightReached -> {
                            // TODO: Impl
                        }

                        is HomeUIState.InitialDataLoaded -> {
                            val user = state.user
                            fullNameTextView.text = user.fullName
                            greetingTextView.setText(TimeUtils.greetingResId)
                            startWeightTextView.text = getString(
                                R.string.weight_format,
                                user.currentWeight
                            )
                            goalWeightTextView.text = getString(
                                R.string.weight_format,
                                user.goalWeight
                            )
                        }
                    }
                }
            }

            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.latestWeight.collect { weight ->
                    // If the user has no goal weight set, then set the UserGoal's start weight as the current weight
                    val currentWeight = weight?.weight ?: viewModel.currentUser.currentWeight
                    currentWeightTextView.text = getString(
                        R.string.weight_format,
                        currentWeight
                    )
                    val progress = getProgress(weight.weight)
                    weightProgressBar.progress = progress
                }
            }

            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dailyWeights.collect { weights ->
                    // TODO: Impl
                }
            }
        }
    }


    /**
     * Query and attach each view instance from XML layout.
     */
    private fun setupUI() {
        trackWeightButton = findViewById(R.id.btnHomeTrackWeight)
        fullNameTextView = findViewById(R.id.tvHomeFullName)
        greetingTextView = findViewById(R.id.tvHomeGreeting)
        currentWeightTextView = findViewById(R.id.tvHomeCurrentWeight)
        goalWeightTextView = findViewById(R.id.tvHomeGoalWeight)
        startWeightTextView = findViewById(R.id.tvHomeStartingWeight)
        progressHistoryRecyclerView = findViewById(R.id.rvHomeHistory)
        profileImage = findViewById(R.id.imgHomeProfile)
        weightProgressBar = findViewById(R.id.pbHomeWeightChange)
        noWeightsLayout = findViewById(R.id.layoutHomeNoHistory)

        setupRecyclerView()
    }



    /**
     * Gets the current weight progress given the user's goals and latest logged weight.
     * 
     * @param currentWeight The latest logged weight.
     * @return A number from 0 to 100 indicating the weight progress.
     */
    private fun getProgress(currentWeight: Float): Int {
        var progress = 0
        if (userGoal.getGoalType() === GoalType.WEIGHT_LOSS) {
            // Get current weight returns the user's initial weight
            // and we compare it the user's logged weight to get the progress
            val weightLost: Float = userGoal.getCurrentWeight() - currentWeight
            val goalTotal: Float = userGoal.getCurrentWeight() - userGoal.getGoalWeight()
            val progressPercentage = (weightLost / goalTotal * 100).toInt()
            progress = kotlin.math.max(0, kotlin.math.min(100, progressPercentage))
        } else {
            // For weight gain the current weight will be greater than the initial weight
            // else the progress will show negative values.
            val weightGained: Float = currentWeight - userGoal.getCurrentWeight()
            val goalTotal: Float = userGoal.getGoalWeight() - userGoal.getCurrentWeight()

            val progressPercentage = (weightGained / goalTotal * 100).toInt()
            progress = kotlin.math.max(0, kotlin.math.min(100, progressPercentage))
        }
        return progress
    }


    private fun setupRecyclerView() {
        val weightList: MutableList<DailyWeight?> = repository.getDailyWeights()
        adapter = WeightItemAdapter(weightList, WeightItemRemoveListener { itemId: Long ->
            // For deleting the weight, to provide the user with a chance to rethink their
            // show a dialog with a confirmation. On confirmation, delete the weight.
            val dialog: DeleteWeightItemDialog = DeleteWeightItemDialog(DialogClickListener {
                val weightDeleted: Boolean = repository.deleteDailyWeight(itemId)
                if (weightDeleted) {
                    refreshHistory()
                    latestWeight = repository.getUserLatestLoggedWeight()
                    prefillUI()
                    Toast.makeText(this, "Weight deleted successfully.", Toast.LENGTH_SHORT).show()
                }
            })
            dialog.show(
                supportFragmentManager,
                com.cs360.weighttracker.presentation.Constants.DELETE_WEIGHT_ITEM_DIALOG
            )
        })
        progressHistoryRecyclerView.setAdapter(adapter)
        refreshHistory() // Must refresh on initial load
    }


    /**
     * Refresh the weight progress data with new data from the repository.
     */
    private fun refreshHistory() {
        val newData: MutableList<DailyWeight?> = repository.getDailyWeights()
        // Show the history view if there is data
        // else show the placeholder rather than a blank screen
        if (newData.isEmpty()) {
            progressHistoryRecyclerView.visibility = android.view.View.GONE
            noWeightsLayout.visibility = android.view.View.VISIBLE
        } else {
            noWeightsLayout.visibility = android.view.View.GONE
            progressHistoryRecyclerView.visibility = android.view.View.VISIBLE
            adapter.updateData(newData)
        }
    }


    /**
     * Sets up the event listeners necessary for all the views.
     */
    private fun setupEvents() {
        // Finish will pop off this activity from the backstack leaving only with the Login activity
        // from the navigation will take place
        trackWeightButton!!.setOnClickListener(android.view.View.OnClickListener { view: android.view.View? ->
            val dialog: AddWeightDialog = AddWeightDialog()
            dialog.show(
                supportFragmentManager,
                com.cs360.weighttracker.presentation.Constants.ADD_NEW_WEIGHT_DIALOG
            )
        })

        // Add event listener for the dialog
        supportFragmentManager.setFragmentResultListener(
            Constants.ADD_NEW_WEIGHT_DIALOG,
            this,
            (FragmentResultListener { _: String, result: Bundle ->
                val weight: Float =
                    result.getFloat(Constants.NEW_WEIGHT_BUNDLE_KEY)
                if (repository.logDailyWeight(weight)) {
                    refreshHistory()
                    latestWeight = repository.getUserLatestLoggedWeight()
                    prefillUI()
                    // Send a sms if the user surpassed or reached the goal weight and have the permission and phone number set
                    if (hasReachGoalWeight(weight)) {
                        val shouldSendNotification: Boolean =
                            repository.getUserNotificationSetting()
                        if (shouldSendNotification) {
                            // Check if the user has granted permission
                            // Note: We are not handling the permission here since if hte user has their preference as off
                            // or has manually turned off the permission, we can assume that they don't need the feature
                            // but we can show them an error message
                            val hasPermission = ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.SEND_SMS
                            ) == PackageManager.PERMISSION_GRANTED
                            val phoneNumber = currentUser!!.phoneNumber
                            if (hasPermission && !phoneNumber.trim { it <= ' ' }.isEmpty()) {
                                try {
                                    val userGoal: GoalWeight = repository.getUserGoalWeight()
                                    val smsManager =
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            this.getSystemService(SmsManager::class.java)
                                        } else {
                                            // Fallback for older Android versions like Android 7
                                            SmsManager.getDefault()
                                        }
                                    // We are using hte latest weight as it can surpass the user's current weight!
                                    val message = getString(
                                        R.string.congratulations_sms,
                                        currentUser!!.fullName,
                                        userGoal.getCurrentWeight(),
                                        weight
                                    )
                                    if (smsManager != null) {
                                        smsManager.sendTextMessage(
                                            phoneNumber,
                                            null,
                                            message,
                                            null,
                                            null
                                        ) //
                                        Toast.makeText(
                                            this,
                                            R.string.sms_sent_successfully,
                                            Toast.LENGTH_SHORT
                                        ).show() //
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        this,
                                        R.string.failed_to_send_sms,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        Toast.makeText(
                            this,
                            this.getString(R.string.congratulations),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            this.getString(R.string.weight_added_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
        )

        profileImage.setOnClickListener { _: android.view.View? -> navigateToProfile() }
    }

    /**
     * Returns a boolean indicating whether the user has reached a goal weight depending on the type of goal(loss/gain)
     * 
     * @param newWeight The latest logged weight.
     */
    private fun hasReachGoalWeight(newWeight: Float): Boolean {
        val weightGoal: GoalWeight = repository.getUserGoalWeight()
        // In case of weight gain we need to check if current logged weight is greater or equal to
        // the goal weight and weight loss if the logged weight is less than or equal
        if (weightGoal.getGoalType() === GoalType.WEIGHT_GAIN) {
            return newWeight >= weightGoal.getGoalWeight()
        } else {
            return newWeight <= weightGoal.getGoalWeight()
        }
    }


    private fun navigateToProfile() {
        val intent: Intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }
}