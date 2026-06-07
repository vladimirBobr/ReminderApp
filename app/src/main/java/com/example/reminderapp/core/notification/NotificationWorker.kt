package com.example.reminderapp.core.notification

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.reminderapp.MainActivity
import com.example.reminderapp.ReminderApp

/**
 * [CoroutineWorker] that shows a system notification with sound
 * at the time scheduled by [NotificationScheduler].
 *
 * Input data (from WorkManager):
 * - "eventId" (String) — unique event identifier, used as notification ID
 * - "eventTitle" (String) — notification title
 * - "eventDescription" (String) — notification body text
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val eventId = inputData.getString("eventId") ?: return Result.failure()
        val eventTitle = inputData.getString("eventTitle") ?: "Событие"
        val eventDescription = inputData.getString("eventDescription") ?: ""

        showNotification(eventId, eventTitle, eventDescription)
        return Result.success()
    }

    /**
     * Builds and displays the notification via [NotificationManager].
     */
    private fun showNotification(eventId: String, title: String, description: String) {
        // Skip if notification permission is not granted (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        // Intent that opens MainActivity when notification is tapped
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, ReminderApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(description.ifEmpty { "Без описания" })
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        // Use eventId.hashCode() as a stable notification ID for this event
        manager.notify(eventId.hashCode(), notification)
    }
}
