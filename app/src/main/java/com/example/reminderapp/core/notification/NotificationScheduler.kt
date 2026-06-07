package com.example.reminderapp.core.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.reminderapp.MainActivity
import com.example.reminderapp.ReminderApp
import com.example.reminderapp.core.model.Event
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Schedules and cancels notification work requests via [WorkManager].
 *
 * Each event gets a unique work identified by its [Event.id].
 * Timing logic:
 * - Event with time → notification at [Event.date] + [Event.time]
 * - Event without time → notification at [Event.date] 07:00
 * - Events already in the past are silently skipped.
 *
 * @param context Application or activity context (WorkManager is process-wide).
 */
class NotificationScheduler(private val context: Context) {

    private val workManager: WorkManager = WorkManager.getInstance(context)

    /**
     * Schedules (or replaces) a one-shot notification for [event].
     * If the calculated trigger time is in the past the call is ignored.
     */
    fun schedule(event: Event) {
        val triggerDateTime: LocalDateTime = if (event.time != null) {
            event.date.atTime(event.time)
        } else {
            event.date.atTime(LocalTime.of(7, 0))
        }

        // Do not schedule notifications for past events
        if (triggerDateTime.isBefore(LocalDateTime.now())) return

        val delayMs = triggerDateTime
            .atZone(ZONE)
            .toInstant()
            .toEpochMilli() - System.currentTimeMillis()

        // Guard against negative delays due to clock skew
        if (delayMs <= 0) return

        val inputData = workDataOf(
            "eventId" to event.id,
            "eventTitle" to event.title,
            "eventDescription" to event.description
        )

        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(WORK_TAG_PREFIX + event.id)
            .build()

        workManager.enqueueUniqueWork(
            event.id,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /**
     * Cancels a previously scheduled notification for the event with [eventId].
     */
    fun cancel(eventId: String) {
        workManager.cancelUniqueWork(eventId)
    }

    /**
     * Shows an immediate test notification directly via [NotificationManager].
     * Bypasses WorkManager for reliable debugging.
     */
    fun sendTestNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ReminderApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Тест")
            .setContentText("Тестовое уведомление — система работает!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify("test_notification".hashCode(), notification)
    }

    /**
     * Cancels all pending event-reminder work and re-schedules every event in [events].
     * Typically called once at app startup or after data import.
     */
    fun rescheduleAll(events: List<Event>) {
        // Cancel all work tagged with our prefix
        workManager.cancelAllWorkByTag(WORK_TAG_PREFIX)
        events.forEach { schedule(it) }
    }

    companion object {
        /** Tag prefix used to identify all event-reminder work requests. */
        const val WORK_TAG_PREFIX = "event_reminder_"

        /** Time zone used when converting [LocalDateTime] to epoch millis. */
        private val ZONE: ZoneId = ZoneId.of("Europe/Samara")
    }
}
