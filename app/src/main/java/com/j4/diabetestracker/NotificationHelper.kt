package com.j4.diabetestracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    private const val CHANNEL_ID_FOOD_WARNINGS = "food_warnings"
    private const val CHANNEL_ID_BLOOD_SUGAR = "blood_sugar_alerts"
    private const val CHANNEL_ID_TIME_PATTERNS = "time_pattern_reminders"
    
    private const val NOTIFICATION_ID_FOOD_WARNING = 1001
    private const val NOTIFICATION_ID_BLOOD_SUGAR = 1002
    const val NOTIFICATION_ID_TIME_PATTERN = 1003
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Food Warning Channel
            val foodWarningChannel = NotificationChannel(
                CHANNEL_ID_FOOD_WARNINGS,
                "Food Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when adding risky foods"
                enableVibration(true)
            }
            
            // Blood Sugar Alert Channel
            val bloodSugarChannel = NotificationChannel(
                CHANNEL_ID_BLOOD_SUGAR,
                "Blood Sugar Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for blood sugar spikes and patterns"
                enableVibration(true)
            }
            
            // Time Pattern Reminder Channel
            val timePatternChannel = NotificationChannel(
                CHANNEL_ID_TIME_PATTERNS,
                "Time-Based Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders based on time-of-day patterns"
                enableVibration(true) // Allow vibration at channel level
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
            }
            
            notificationManager.createNotificationChannel(foodWarningChannel)
            notificationManager.createNotificationChannel(bloodSugarChannel)
            notificationManager.createNotificationChannel(timePatternChannel)
        }
    }
    
    /**
     * Checks if the notification channel has vibration or sound disabled at the system level.
     * Checks BOTH app-level and channel-level settings.
     * Returns a pair of (soundEnabled, vibrationEnabled)
     */
    fun getChannelSettings(context: Context): Pair<Boolean, Boolean> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // First check if notifications are enabled at all for the app
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                return Pair(false, false)
            }
            
            // Then check the specific channel settings
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID_TIME_PATTERNS)
            
            if (channel != null) {
                // Check if channel is blocked
                if (channel.importance == NotificationManager.IMPORTANCE_NONE) {
                    return Pair(false, false)
                }
                
                val soundEnabled = channel.sound != null
                val vibrationEnabled = channel.shouldVibrate()
                return Pair(soundEnabled, vibrationEnabled)
            }
        }
        // For older Android versions or if channel doesn't exist, assume both are enabled
        return Pair(true, true)
    }
    
    fun showFoodWarningNotification(
        context: Context,
        foodName: String,
        markerName: String,
        confidence: Float,
        occurrences: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val confidencePercent = (confidence * 100).toInt()
        val title = "⚠️ Food Warning: $foodName"
        val message = "Linked to $markerName $confidencePercent% of the time ($occurrences occurrences)"
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_FOOD_WARNINGS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_FOOD_WARNING, notification)
    }
    
    fun showBloodSugarAlertNotification(
        context: Context,
        alertMessage: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_BLOOD_SUGAR)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Blood Sugar Alert")
            .setContentText(alertMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alertMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BLOOD_SUGAR, notification)
    }
    
    fun showTimePatternReminderNotification(
        context: Context,
        reminderMessage: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TIME_PATTERNS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Pattern Reminder")
            .setContentText(reminderMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminderMessage))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TIME_PATTERN, notification)
    }
    
    fun showTimeReminderNotification(
        context: Context,
        title: String,
        message: String,
        reminderId: String = "",
        reminderType: String = "",
        requestCode: Int = 0,
        hasSound: Boolean = true,
        hasVibration: Boolean = true,
        requiresConfirmation: Boolean = false
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add extra to indicate this notification requires confirmation
            if (requiresConfirmation) {
                putExtra("OPEN_PENDING_CONFIRMATIONS", true)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Create snooze action
        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = "ACTION_SNOOZE_REMINDER"
            putExtra("REMINDER_ID", reminderId)
            putExtra("REMINDER_TYPE", reminderType)
            putExtra("REMINDER_MESSAGE", message)
            putExtra("REQUEST_CODE", requestCode)
            putExtra("TITLE", title)
        }
        
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode + 10000, // Offset to avoid conflicts
            snoozeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_TIME_PATTERNS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_recent_history,
                "Snooze 15min",
                snoozePendingIntent
            )
        
        // Apply sound/vibration based on flags
        when {
            !hasSound && !hasVibration -> {
                // Silent: notification appears but no sound or vibration
                builder.setSilent(true)
                builder.setPriority(NotificationCompat.PRIORITY_LOW)
            }
            hasSound && hasVibration -> {
                // Both sound and vibration
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
                builder.setPriority(NotificationCompat.PRIORITY_HIGH)
            }
            hasSound -> {
                // Sound only, no vibration
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
                builder.setVibrate(null)
                builder.setPriority(NotificationCompat.PRIORITY_HIGH)
            }
            hasVibration -> {
                // Vibration only, no sound
                builder.setSound(null)
                builder.setVibrate(longArrayOf(0, 300, 200, 300))
                builder.setPriority(NotificationCompat.PRIORITY_HIGH)
            }
        }
        
        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_TIME_PATTERN + requestCode,
            builder.build()
        )
    }
    
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            true // Permission not required for older versions
        }
    }
    
    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}
