package com.j4.diabetestracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ReminderScheduler {
    
    /**
     * Schedule a daily reminder at a specific time
     */
    fun scheduleDailyReminder(
        context: Context,
        requestCode: Int,
        hour: Int,
        minute: Int,
        reminderType: ReminderType,
        message: String,
        hasSound: Boolean = true,
        hasVibration: Boolean = true,
        customReminderId: String = "",
        reminderEmoji: String = "",
        requiresConfirmation: Boolean = false
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Create intent for the broadcast receiver
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_TYPE", reminderType.name)
            putExtra("REMINDER_MESSAGE", message)
            putExtra("REQUEST_CODE", requestCode)
            putExtra("HOUR", hour)
            putExtra("MINUTE", minute)
            putExtra("HAS_SOUND", hasSound)
            putExtra("HAS_VIBRATION", hasVibration)
            putExtra("CUSTOM_REMINDER_ID", customReminderId)
            putExtra("REMINDER_EMOJI", reminderEmoji)
            putExtra("REQUIRES_CONFIRMATION", requiresConfirmation)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Set the alarm to start at the specified time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            
            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        // Schedule repeating alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
    
    /**
     * Cancel a scheduled reminder
     */
    fun cancelReminder(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Only cancel if the PendingIntent exists
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
    
    /**
     * Schedule meal reminders based on mode (manual/pattern/both)
     */
    fun scheduleMealReminders(context: Context, entries: List<DiabetesEntry>) {
        if (!NotificationPreferences.isMealRemindersEnabled(context)) {
            return
        }
        
        val mode = NotificationPreferences.getMealReminderMode(context)
        val patterns = mutableListOf<TimePattern>()
        
        when (mode) {
            ReminderMode.MANUAL -> {
                // Use manual times only
                val breakfast = NotificationPreferences.getMealBreakfastTime(context)
                val lunch = NotificationPreferences.getMealLunchTime(context)
                val dinner = NotificationPreferences.getMealDinnerTime(context)
                
                patterns.add(TimePattern(breakfast.first, breakfast.second, "Time for breakfast!"))
                patterns.add(TimePattern(lunch.first, lunch.second, "Time for lunch!"))
                patterns.add(TimePattern(dinner.first, dinner.second, "Time for dinner!"))
            }
            ReminderMode.PATTERN -> {
                // Use pattern detection only
                patterns.addAll(analyzeMealPatterns(entries))
            }
            ReminderMode.BOTH -> {
                // Try pattern detection first
                val detectedPatterns = analyzeMealPatterns(entries)
                
                if (detectedPatterns.isNotEmpty()) {
                    patterns.addAll(detectedPatterns)
                } else {
                    // Fallback to manual times if no patterns detected
                    val breakfast = NotificationPreferences.getMealBreakfastTime(context)
                    val lunch = NotificationPreferences.getMealLunchTime(context)
                    val dinner = NotificationPreferences.getMealDinnerTime(context)
                    
                    patterns.add(TimePattern(breakfast.first, breakfast.second, "Time for breakfast!"))
                    patterns.add(TimePattern(lunch.first, lunch.second, "Time for lunch!"))
                    patterns.add(TimePattern(dinner.first, dinner.second, "Time for dinner!"))
                }
            }
        }
        
        // Schedule reminders for each time
        patterns.forEachIndexed { index, pattern ->
            val requestCode = 1000 + index
            scheduleDailyReminder(
                context = context,
                requestCode = requestCode,
                hour = pattern.hour,
                minute = pattern.minute,
                reminderType = ReminderType.MEAL_REMINDER,
                message = pattern.message
            )
        }
    }
    
    /**
     * Schedule blood sugar check reminders based on mode (manual/pattern/both)
     */
    fun scheduleBloodSugarCheckReminders(context: Context, entries: List<DiabetesEntry>) {
        if (!NotificationPreferences.isBloodSugarCheckRemindersEnabled(context)) {
            return
        }
        
        val mode = NotificationPreferences.getBloodSugarReminderMode(context)
        val patterns = mutableListOf<TimePattern>()
        
        when (mode) {
            ReminderMode.MANUAL -> {
                // Use manual times only
                val morning = NotificationPreferences.getBloodSugarMorningTime(context)
                val afternoon = NotificationPreferences.getBloodSugarAfternoonTime(context)
                val evening = NotificationPreferences.getBloodSugarEveningTime(context)
                
                patterns.add(TimePattern(morning.first, morning.second, "Time to check your morning blood sugar!"))
                patterns.add(TimePattern(afternoon.first, afternoon.second, "Time to check your afternoon blood sugar!"))
                patterns.add(TimePattern(evening.first, evening.second, "Time to check your evening blood sugar!"))
            }
            ReminderMode.PATTERN -> {
                // Use pattern detection only
                patterns.addAll(analyzeBloodSugarCheckPatterns(entries))
            }
            ReminderMode.BOTH -> {
                // Try pattern detection first
                val detectedPatterns = analyzeBloodSugarCheckPatterns(entries)
                
                if (detectedPatterns.isNotEmpty()) {
                    patterns.addAll(detectedPatterns)
                } else {
                    // Fallback to manual times if no patterns detected
                    val morning = NotificationPreferences.getBloodSugarMorningTime(context)
                    val afternoon = NotificationPreferences.getBloodSugarAfternoonTime(context)
                    val evening = NotificationPreferences.getBloodSugarEveningTime(context)
                    
                    patterns.add(TimePattern(morning.first, morning.second, "Time to check your morning blood sugar!"))
                    patterns.add(TimePattern(afternoon.first, afternoon.second, "Time to check your afternoon blood sugar!"))
                    patterns.add(TimePattern(evening.first, evening.second, "Time to check your evening blood sugar!"))
                }
            }
        }
        
        // Schedule reminders for each time
        patterns.forEachIndexed { index, pattern ->
            val requestCode = 2000 + index
            scheduleDailyReminder(
                context = context,
                requestCode = requestCode,
                hour = pattern.hour,
                minute = pattern.minute,
                reminderType = ReminderType.BLOOD_SUGAR_CHECK,
                message = pattern.message
            )
        }
    }
    
    /**
     * Schedule custom user-defined reminders
     */
    fun scheduleCustomReminders(context: Context) {
        val customReminders = CustomReminderStorage.loadReminders(context)
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
            else -> DayOfWeek.MONDAY
        }
        
        customReminders.filter { it.isEnabled }.forEachIndexed { reminderIndex, reminder ->
            // Only schedule if today is one of the selected days
            if (reminder.daysOfWeek.contains(currentDayOfWeek)) {
                reminder.times.forEachIndexed { timeIndex, time ->
                    val requestCode = 3000 + (reminderIndex * 10) + timeIndex
                    
                    scheduleDailyReminder(
                        context = context,
                        requestCode = requestCode,
                        hour = time.hour,
                        minute = time.minute,
                        reminderType = ReminderType.CUSTOM,
                        message = "${reminder.emoji} ${reminder.name}",
                        hasSound = reminder.hasSound,
                        hasVibration = reminder.hasVibration,
                        customReminderId = reminder.id,
                        reminderEmoji = reminder.emoji,
                        requiresConfirmation = reminder.requiresConfirmation
                    )
                }
            }
        }
    }
    
    /**
     * Analyze meal patterns from user entries by detecting which time-of-day periods have food
     */
    private fun analyzeMealPatterns(entries: List<DiabetesEntry>): List<TimePattern> {
        val patterns = mutableListOf<TimePattern>()
        
        // Count food entries by time of day (morning, afternoon, evening, night)
        var morningMeals = 0
        var afternoonMeals = 0
        var eveningMeals = 0
        var nightMeals = 0
        
        entries.forEach { entry ->
            entry.foodEntriesByColumn.forEach { (columnId, foods) ->
                if (foods.isNotEmpty()) {
                    when {
                        columnId.contains("morning", ignoreCase = true) -> morningMeals++
                        columnId.contains("afternoon", ignoreCase = true) -> afternoonMeals++
                        columnId.contains("evening", ignoreCase = true) -> eveningMeals++
                        columnId.contains("night", ignoreCase = true) -> nightMeals++
                    }
                }
            }
        }
        
        // Schedule reminders for time periods with significant meal activity (≥10 occurrences)
        if (morningMeals >= 10) {
            patterns.add(TimePattern(7, 30, "Time for breakfast! You usually eat around this time."))
        }
        
        if (afternoonMeals >= 10) {
            patterns.add(TimePattern(12, 0, "Time for lunch! You usually eat around this time."))
        }
        
        if (eveningMeals >= 10) {
            patterns.add(TimePattern(18, 0, "Time for dinner! You usually eat around this time."))
        }
        
        if (nightMeals >= 10) {
            patterns.add(TimePattern(21, 0, "Time for evening snack! You usually eat around this time."))
        }
        
        return patterns
    }
    
    /**
     * Analyze blood sugar check patterns
     */
    private fun analyzeBloodSugarCheckPatterns(entries: List<DiabetesEntry>): List<TimePattern> {
        val patterns = mutableListOf<TimePattern>()
        
        // Count entries with blood sugar data by time of day
        val morningChecks = entries.count { it.morningBloodSugarLevel.isNotBlank() }
        val afternoonChecks = entries.count { it.afternoonBloodSugarLevel.isNotBlank() }
        val eveningChecks = entries.count { it.eveningBloodSugarLevel.isNotBlank() }
        
        // Suggest check times based on user habits
        if (morningChecks >= 5) {
            patterns.add(TimePattern(7, 30, "Time to check your morning blood sugar!"))
        }
        
        if (afternoonChecks >= 5) {
            patterns.add(TimePattern(14, 0, "Time to check your afternoon blood sugar!"))
        }
        
        if (eveningChecks >= 5) {
            patterns.add(TimePattern(20, 0, "Time to check your evening blood sugar!"))
        }
        
        return patterns
    }
    
    /**
     * Calculate average time from pattern occurrences
     */
    private fun calculateAverageTimeFromOccurrences(occurrences: List<PatternOccurrence>): Pair<Int, Int> {
        // Map time of day to approximate hours
        val timeMap = mapOf(
            "morning" to 8,
            "afternoon" to 14,
            "evening" to 19,
            "night" to 22
        )
        
        val avgHour = occurrences.mapNotNull { timeMap[it.timeOfDay.lowercase()] }.average().toInt()
        return Pair(avgHour, 0)
    }
    
    /**
     * Reschedule all reminders based on current user data
     */
    fun rescheduleAllReminders(context: Context, entries: List<DiabetesEntry>, customMarkers: List<CustomMarkerType>) {
        // Cancel all existing reminders
        for (i in 1000..3100) {
            cancelReminder(context, i)
        }
        
        // Reschedule based on current preferences
        if (NotificationPreferences.isTimeRemindersEnabled(context)) {
            scheduleMealReminders(context, entries)
            scheduleBloodSugarCheckReminders(context, entries)
            scheduleCustomReminders(context)
        }
    }
}

/**
 * Data class for time-based patterns
 */
data class TimePattern(
    val hour: Int,
    val minute: Int,
    val message: String
)
