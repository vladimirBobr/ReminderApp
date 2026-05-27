package com.example.reminderapp

import android.app.Application
import com.example.reminderapp.core.di.AppContainer

/**
 * Application class — initializes the dependency injection container.
 * Declared in AndroidManifest.xml as android:name=".ReminderApp".
 */
class ReminderApp : Application() {

    /** Application-wide DI container. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
