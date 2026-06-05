package com.example.reminderapp.core.di

import android.content.Context
import com.example.reminderapp.core.notification.NotificationScheduler
import com.example.reminderapp.feature.events.EventsRepository
import java.io.File

/**
 * Simple manual dependency injection container.
 * Initialized once in [ReminderApp.onCreate].
 */
class AppContainer(context: Context) {

    private val storageDir: File = File(context.filesDir, "yamldb")

    val notificationScheduler: NotificationScheduler by lazy {
        NotificationScheduler(context)
    }

    val eventsRepository: EventsRepository by lazy {
        EventsRepository(storageDir, notificationScheduler)
    }
}
