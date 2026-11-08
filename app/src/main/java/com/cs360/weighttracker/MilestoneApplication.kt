package com.cs360.weighttracker

import android.app.Application
import com.cs360.weighttracker.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MilestoneApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Start Koin (Dependency Injection)
        startKoin {
            androidContext(this@MilestoneApplication)
            modules(appModule)
        }
    }
}