package com.j4.diabetestracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Reminder received")
        
        // Check if this is a snooze action
        if (intent.action == "ACTION_SNOOZE_REMINDER") {
            handleSnooze(context, intent)
            return
        }
        
        // Extract reminder data
        val reminderTypeString = intent.getStringExtra("REMINDER_TYPE") ?: return
        val message = intent.getStringExtra("REMINDER_MESSAGE") ?: return
        val requestCode = intent.getIntExtra("REQUEST_CODE", -1)
        
        // Parse reminder type
        val reminderType = try {
            ReminderType.valueOf(reminderTypeString)
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "Invalid reminder type: $reminderTypeString")
            return
        }
        
        // Check if we should send this reminder
        if (!NotificationPreferences.shouldSendReminder(context, reminderType)) {
            Log.d("ReminderReceiver", "Reminder blocked by preferences")
            return
        }
        
        // Get localized title based on reminder type
        val settings = SettingsManager.loadSettings(context)
        val language = settings.language
        val title = when (reminderType) {
            ReminderType.MEAL_REMINDER -> when (language) {
                "German" -> "Mahlzeiterinnerung"
                "Spanish" -> "Recordatorio de comida"
                else -> "Meal Reminder"
            }
            ReminderType.BLOOD_SUGAR_CHECK -> when (language) {
                "German" -> "Blutzucker-Check"
                "Spanish" -> "Chequeo de azúcar"
                else -> "Blood Sugar Check"
            }
            ReminderType.CUSTOM -> when (language) {
                "German" -> "Erinnerung"
                "Spanish" -> "Recordatorio"
                else -> "Reminder"
            }
        }
        
        // Get scheduled time for logging
        val hour = intent.getIntExtra("HOUR", 0)
        val minute = intent.getIntExtra("MINUTE", 0)
        val scheduledTime = String.format("%02d:%02d", hour, minute)
        
        // Get notification style flags
        val hasSound = intent.getBooleanExtra("HAS_SOUND", true)
        val hasVibration = intent.getBooleanExtra("HAS_VIBRATION", true)
        
        // Get confirmation tracking flags
        val customReminderId = intent.getStringExtra("CUSTOM_REMINDER_ID") ?: ""
        val reminderEmoji = intent.getStringExtra("REMINDER_EMOJI") ?: ""
        val requiresConfirmation = intent.getBooleanExtra("REQUIRES_CONFIRMATION", false)
        
        // Log reminder to history
        val reminderId = ReminderHistoryManager.logReminder(
            context = context,
            reminderType = reminderType,
            title = title,
            message = message,
            scheduledTime = scheduledTime
        )
        
        // Send notification with snooze action
        NotificationHelper.showTimeReminderNotification(
            context = context,
            title = title,
            message = message,
            reminderId = reminderId,
            reminderType = reminderType.name,
            requestCode = requestCode,
            hasSound = hasSound,
            hasVibration = hasVibration,
            requiresConfirmation = requiresConfirmation
        )
        
        // Create pending confirmation if required
        if (requiresConfirmation && customReminderId.isNotEmpty()) {
            val reminderName = message.replace(reminderEmoji, "").trim()
            ReminderConfirmationStorage.addPendingConfirmation(
                context = context,
                reminderId = customReminderId,
                reminderName = reminderName,
                reminderEmoji = reminderEmoji,
                scheduledTime = scheduledTime
            )
            Log.d("ReminderReceiver", "Created pending confirmation for: $message")
        }
        
        // Reschedule for next day (since we're using setExact, not setRepeating)
        rescheduleReminder(context, intent, requestCode, reminderType, message)
    }
    
    /**
     * Handle snooze action - schedule reminder 15 minutes later
     */
    private fun handleSnooze(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("REMINDER_ID") ?: return
        val reminderTypeString = intent.getStringExtra("REMINDER_TYPE") ?: return
        val message = intent.getStringExtra("REMINDER_MESSAGE") ?: return
        val requestCode = intent.getIntExtra("REQUEST_CODE", -1)
        val title = intent.getStringExtra("TITLE") ?: "Reminder"
        
        // Parse reminder type
        val reminderType = try {
            ReminderType.valueOf(reminderTypeString)
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "Invalid reminder type in snooze: $reminderTypeString")
            return
        }
        
        // Get current snooze count from history
        val history = ReminderHistoryManager.getHistory(context)
        val currentEntry = history.firstOrNull { it.id == reminderId }
        val snoozeCount = (currentEntry?.snoozeCount ?: 0) + 1
        
        // Update history with snooze action
        ReminderHistoryManager.updateReminderAction(
            context = context,
            reminderId = reminderId,
            action = "SNOOZED",
            wasSnoozed = true,
            snoozeCount = snoozeCount
        )
        
        // Cancel the current notification
        NotificationHelper.cancelNotification(context, NotificationHelper.NOTIFICATION_ID_TIME_PATTERN)
        
        // Schedule snoozed reminder for 15 minutes later
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_TYPE", reminderType.name)
            putExtra("REMINDER_MESSAGE", message)
            putExtra("REQUEST_CODE", requestCode)
            putExtra("HOUR", 0) // Not used for snoozed reminders
            putExtra("MINUTE", 0)
        }
        
        val snoozePendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            requestCode + 20000, // Different offset for snoozed reminders
            snoozeIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        // Schedule for 15 minutes from now
        val snoozeTime = System.currentTimeMillis() + (15 * 60 * 1000) // 15 minutes
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                snoozeTime,
                snoozePendingIntent
            )
        } else {
            alarmManager.setExact(
                android.app.AlarmManager.RTC_WAKEUP,
                snoozeTime,
                snoozePendingIntent
            )
        }
        
        Log.d("ReminderReceiver", "Reminder snoozed for 15 minutes (count: $snoozeCount)")
    }
    
    /**
     * Reschedule the reminder for the next day
     */
    private fun rescheduleReminder(
        context: Context,
        originalIntent: Intent,
        requestCode: Int,
        reminderType: ReminderType,
        message: String
    ) {
        // Extract hour and minute from the original schedule
        // This is a simplified approach - in production, you'd store these values
        val hour = when (requestCode) {
            in 1000..1099 -> when (requestCode - 1000) {
                0 -> 8  // Breakfast
                1 -> 12 // Lunch
                2 -> 18 // Dinner
                else -> 12
            }
            in 2000..2099 -> when (requestCode - 2000) {
                0 -> 7  // Morning check
                1 -> 14 // Afternoon check
                2 -> 20 // Evening check
                else -> 12
            }
            in 3000..3099 -> {
                // Pattern-based - extract from stored data
                12 // Default to noon
            }
            else -> 12
        }
        
        val minute = when (requestCode) {
            in 1000..1099 -> when (requestCode - 1000) {
                0 -> 0   // 8:00
                1 -> 30  // 12:30
                2 -> 30  // 18:30
                else -> 0
            }
            in 2000..2099 -> when (requestCode - 2000) {
                0 -> 30  // 7:30
                1 -> 0   // 14:00
                2 -> 0   // 20:00
                else -> 0
            }
            else -> 0
        }
        
        // Reschedule for tomorrow
        ReminderScheduler.scheduleDailyReminder(
            context = context,
            requestCode = requestCode,
            hour = hour,
            minute = minute,
            reminderType = reminderType,
            message = message
        )
    }
}
