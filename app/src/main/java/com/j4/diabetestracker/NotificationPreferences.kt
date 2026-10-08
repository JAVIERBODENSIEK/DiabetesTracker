package com.j4.diabetestracker

import android.content.Context
import android.content.SharedPreferences

object NotificationPreferences {
    private const val PREFS_NAME = "notification_preferences"
    
    private const val KEY_TIME_REMINDERS_ENABLED = "time_reminders_enabled"
    private const val KEY_REMINDER_CONFIDENCE_THRESHOLD = "reminder_confidence_threshold"
    private const val KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled"
    private const val KEY_QUIET_START_HOUR = "quiet_start_hour"
    private const val KEY_QUIET_END_HOUR = "quiet_end_hour"
    
    // Reminder types
    private const val KEY_MEAL_REMINDERS_ENABLED = "meal_reminders_enabled"
    private const val KEY_BLOOD_SUGAR_CHECK_REMINDERS_ENABLED = "blood_sugar_check_reminders_enabled"
    private const val KEY_PATTERN_BASED_REMINDERS_ENABLED = "pattern_based_reminders_enabled"
    
    // Reminder modes (manual, pattern, both)
    private const val KEY_MEAL_REMINDER_MODE = "meal_reminder_mode"
    private const val KEY_BLOOD_SUGAR_REMINDER_MODE = "blood_sugar_reminder_mode"
    
    // Manual times for meal reminders
    private const val KEY_MEAL_BREAKFAST_HOUR = "meal_breakfast_hour"
    private const val KEY_MEAL_BREAKFAST_MINUTE = "meal_breakfast_minute"
    private const val KEY_MEAL_LUNCH_HOUR = "meal_lunch_hour"
    private const val KEY_MEAL_LUNCH_MINUTE = "meal_lunch_minute"
    private const val KEY_MEAL_DINNER_HOUR = "meal_dinner_hour"
    private const val KEY_MEAL_DINNER_MINUTE = "meal_dinner_minute"
    
    // Manual times for blood sugar check reminders
    private const val KEY_BS_MORNING_HOUR = "bs_morning_hour"
    private const val KEY_BS_MORNING_MINUTE = "bs_morning_minute"
    private const val KEY_BS_AFTERNOON_HOUR = "bs_afternoon_hour"
    private const val KEY_BS_AFTERNOON_MINUTE = "bs_afternoon_minute"
    private const val KEY_BS_EVENING_HOUR = "bs_evening_hour"
    private const val KEY_BS_EVENING_MINUTE = "bs_evening_minute"
    
    // Fallback times for pattern-based reminders (stored as comma-separated "HH:MM" strings)
    private const val KEY_PATTERN_FALLBACK_TIMES = "pattern_fallback_times"
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Time-Based Reminders (Master Toggle)
    fun isTimeRemindersEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_TIME_REMINDERS_ENABLED, true)
    }
    
    fun setTimeRemindersEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_TIME_REMINDERS_ENABLED, enabled).apply()
    }
    
    // Meal Reminders
    fun isMealRemindersEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_MEAL_REMINDERS_ENABLED, true)
    }
    
    fun setMealRemindersEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_MEAL_REMINDERS_ENABLED, enabled).apply()
    }
    
    fun getMealReminderMode(context: Context): ReminderMode {
        val mode = getPreferences(context).getString(KEY_MEAL_REMINDER_MODE, "PATTERN")
        return try {
            ReminderMode.valueOf(mode ?: "PATTERN")
        } catch (e: Exception) {
            ReminderMode.PATTERN
        }
    }
    
    fun setMealReminderMode(context: Context, mode: ReminderMode) {
        getPreferences(context).edit().putString(KEY_MEAL_REMINDER_MODE, mode.name).apply()
    }
    
    // Meal manual times
    fun getMealBreakfastTime(context: Context): Pair<Int, Int> {
        val prefs = getPreferences(context)
        return Pair(prefs.getInt(KEY_MEAL_BREAKFAST_HOUR, 8), prefs.getInt(KEY_MEAL_BREAKFAST_MINUTE, 0))
    }
    
    fun setMealBreakfastTime(context: Context, hour: Int, minute: Int) {
        getPreferences(context).edit()
            .putInt(KEY_MEAL_BREAKFAST_HOUR, hour)
            .putInt(KEY_MEAL_BREAKFAST_MINUTE, minute)
            .apply()
    }
    
    fun getMealLunchTime(context: Context): Pair<Int, Int> {
        val prefs = getPreferences(context)
        return Pair(prefs.getInt(KEY_MEAL_LUNCH_HOUR, 12), prefs.getInt(KEY_MEAL_LUNCH_MINUTE, 30))
    }
    
    fun setMealLunchTime(context: Context, hour: Int, minute: Int) {
        getPreferences(context).edit()
            .putInt(KEY_MEAL_LUNCH_HOUR, hour)
            .putInt(KEY_MEAL_LUNCH_MINUTE, minute)
            .apply()
    }
    
    fun getMealDinnerTime(context: Context): Pair<Int, Int> {
        val prefs = getPreferences(context)
        return Pair(prefs.getInt(KEY_MEAL_DINNER_HOUR, 18), prefs.getInt(KEY_MEAL_DINNER_MINUTE, 30))
    }
    
    fun setMealDinnerTime(context: Context, hour: Int, minute: Int) {
        getPreferences(context).edit()
            .putInt(KEY_MEAL_DINNER_HOUR, hour)
            .putInt(KEY_MEAL_DINNER_MINUTE, minute)
            .apply()
    }
    
    // Blood Sugar Check Reminders
    fun isBloodSugarCheckRemindersEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_BLOOD_SUGAR_CHECK_REMINDERS_ENABLED, true)
    }
    
    fun setBloodSugarCheckRemindersEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_BLOOD_SUGAR_CHECK_REMINDERS_ENABLED, enabled).apply()
    }
    
    fun getBloodSugarReminderMode(context: Context): ReminderMode {
        val mode = getPreferences(context).getString(KEY_BLOOD_SUGAR_REMINDER_MODE, "PATTERN")
        return try {
            ReminderMode.valueOf(mode ?: "PATTERN")
        } catch (e: Exception) {
            ReminderMode.PATTERN
        }
    }
    
    fun setBloodSugarReminderMode(context: Context, mode: ReminderMode) {
        getPreferences(context).edit().putString(KEY_BLOOD_SUGAR_REMINDER_MODE, mode.name).apply()
    }
    
    // Blood sugar manual times
    fun getBloodSugarMorningTime(context: Context): Pair<Int, Int> {
        val prefs = getPreferences(context)
        return Pair(prefs.getInt(KEY_BS_MORNING_HOUR, 7), prefs.getInt(KEY_BS_MORNING_MINUTE, 30))
    }
    
    fun setBloodSugarMorningTime(context: Context, hour: Int, minute: Int) {
        getPreferences(context).edit()
            .putInt(KEY_BS_MORNING_HOUR, hour)
            .putInt(KEY_BS_MORNING_MINUTE, minute)
            .apply()
    }
    
    fun getBloodSugarAfternoonTime(context: Context): Pair<Int, Int> {
        val prefs = getPreferences(context)
        return Pair(prefs.getInt(KEY_BS_AFTERNOON_HOUR, 14), prefs.getInt(KEY_BS_AFTERNOON_MINUTE, 0))
    }
    
    fun setBloodSugarAfternoonTime(context: Context, hour: Int, minute: Int) {
        getPreferences(context).edit()
            .putInt(KEY_BS_AFTERNOON_HOUR, hour)
            .putInt(KEY_BS_AFTERNOON_MINUTE, minute)
            .apply()
    }
    
    fun getBloodSugarEveningTime(context: Context): Pair<Int, Int> {
        val prefs = getPreferences(context)
        return Pair(prefs.getInt(KEY_BS_EVENING_HOUR, 20), prefs.getInt(KEY_BS_EVENING_MINUTE, 0))
    }
    
    fun setBloodSugarEveningTime(context: Context, hour: Int, minute: Int) {
        getPreferences(context).edit()
            .putInt(KEY_BS_EVENING_HOUR, hour)
            .putInt(KEY_BS_EVENING_MINUTE, minute)
            .apply()
    }
    
    // Pattern-Based Reminders (e.g., "You usually have a spike at this time")
    fun isPatternBasedRemindersEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_PATTERN_BASED_REMINDERS_ENABLED, true)
    }
    
    fun setPatternBasedRemindersEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_PATTERN_BASED_REMINDERS_ENABLED, enabled).apply()
    }
    
    // Confidence Threshold for Pattern-Based Reminders
    fun getReminderConfidenceThreshold(context: Context): Float {
        return getPreferences(context).getFloat(KEY_REMINDER_CONFIDENCE_THRESHOLD, 0.7f)
    }
    
    fun setReminderConfidenceThreshold(context: Context, threshold: Float) {
        getPreferences(context).edit().putFloat(KEY_REMINDER_CONFIDENCE_THRESHOLD, threshold).apply()
    }
    
    // Pattern-based fallback times (when no patterns detected)
    fun getPatternFallbackTimes(context: Context): List<Pair<Int, Int>> {
        val timesString = getPreferences(context).getString(KEY_PATTERN_FALLBACK_TIMES, "10:00,15:00")
        return timesString?.split(",")?.mapNotNull { timeStr ->
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull()
                val minute = parts[1].toIntOrNull()
                if (hour != null && minute != null) Pair(hour, minute) else null
            } else null
        } ?: listOf(Pair(10, 0), Pair(15, 0))
    }
    
    fun setPatternFallbackTimes(context: Context, times: List<Pair<Int, Int>>) {
        val timesString = times.joinToString(",") { "${it.first}:${it.second}" }
        getPreferences(context).edit().putString(KEY_PATTERN_FALLBACK_TIMES, timesString).apply()
    }
    
    // Quiet Hours
    fun isQuietHoursEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_QUIET_HOURS_ENABLED, false)
    }
    
    fun setQuietHoursEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_QUIET_HOURS_ENABLED, enabled).apply()
    }
    
    fun getQuietStartHour(context: Context): Int {
        return getPreferences(context).getInt(KEY_QUIET_START_HOUR, 22) // 10 PM
    }
    
    fun setQuietStartHour(context: Context, hour: Int) {
        getPreferences(context).edit().putInt(KEY_QUIET_START_HOUR, hour).apply()
    }
    
    fun getQuietEndHour(context: Context): Int {
        return getPreferences(context).getInt(KEY_QUIET_END_HOUR, 7) // 7 AM
    }
    
    fun setQuietEndHour(context: Context, hour: Int) {
        getPreferences(context).edit().putInt(KEY_QUIET_END_HOUR, hour).apply()
    }
    
    // Check if current time is within quiet hours
    fun isInQuietHours(context: Context): Boolean {
        if (!isQuietHoursEnabled(context)) return false
        
        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val startHour = getQuietStartHour(context)
        val endHour = getQuietEndHour(context)
        
        return if (startHour < endHour) {
            // Normal range (e.g., 8 AM to 10 PM)
            currentHour in startHour until endHour
        } else {
            // Overnight range (e.g., 10 PM to 7 AM)
            currentHour >= startHour || currentHour < endHour
        }
    }
    
    // Check if reminder notification should be sent based on preferences
    fun shouldSendReminder(
        context: Context,
        reminderType: ReminderType,
        confidence: Float = 1.0f
    ): Boolean {
        // Check if in quiet hours
        if (isInQuietHours(context)) return false
        
        // Check global master toggle - if disabled, no notifications are sent
        if (!isTimeRemindersEnabled(context)) return false
        
        // Check if specific reminder type is enabled (each works independently when master is on)
        val isEnabled = when (reminderType) {
            ReminderType.MEAL_REMINDER -> isMealRemindersEnabled(context)
            ReminderType.BLOOD_SUGAR_CHECK -> isBloodSugarCheckRemindersEnabled(context)
            ReminderType.CUSTOM -> true // Custom reminders have their own enable/disable toggle per reminder
        }
        
        return isEnabled
    }
}

enum class ReminderType {
    MEAL_REMINDER,           // "Time to eat lunch"
    BLOOD_SUGAR_CHECK,       // "Time to check blood sugar"
    CUSTOM                   // User-defined custom reminders
}

enum class ReminderMode {
    MANUAL,      // Use manual times only
    PATTERN,     // Detect from user's data patterns
    BOTH         // Use pattern detection with manual fallback
}
