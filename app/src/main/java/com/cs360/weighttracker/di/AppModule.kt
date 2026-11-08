package com.cs360.weighttracker.di

import androidx.room.Room
import com.cs360.weighttracker.data.MilestoneRepository
import com.cs360.weighttracker.data.local.MilestonesDatabase
import com.cs360.weighttracker.data.local.MilestonesSharedPref
import com.cs360.weighttracker.domain.IMilestoneRepository
import com.cs360.weighttracker.presentation.goals.GoalsViewModel
import com.cs360.weighttracker.presentation.home.HomeViewModel
import com.cs360.weighttracker.presentation.login.LoginViewModel
import com.cs360.weighttracker.presentation.profile.ProfileViewModel
import com.cs360.weighttracker.presentation.register.RegisterViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel


val appModule = module {

    // Data Modules
    single<MilestonesSharedPref> {
        MilestonesSharedPref(androidContext())
    }

    single<MilestonesDatabase> {
        Room.databaseBuilder(
            androidContext(),
            klass = MilestonesDatabase::class.java,
            name = "milestones.db",
        ).build()
    }

    single<MilestoneRepository>() bind IMilestoneRepository::class

    // View Models
    viewModel<LoginViewModel>()
    viewModel<RegisterViewModel>()
    viewModel<GoalsViewModel>()
    viewModel<HomeViewModel>()
    viewModel<ProfileViewModel>()

}