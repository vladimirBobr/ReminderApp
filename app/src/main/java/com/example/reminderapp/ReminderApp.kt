package com.example.reminderapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
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
        createNotificationChannel()
        container = AppContainer(this)
    }

    /**
     * Creates the notification channel for event reminders.
     * Must be called before any notification is posted (Android 8.0+).
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания о событиях",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления о запланированных событиях"
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            )
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "event_reminder"
    }
}
