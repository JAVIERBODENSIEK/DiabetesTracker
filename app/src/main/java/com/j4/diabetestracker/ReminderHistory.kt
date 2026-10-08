package com.j4.diabetestracker

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Data class representing a sent reminder
 */
@Serializable
data class ReminderHistoryEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val reminderType: String, // "MEAL_REMINDER", "BLOOD_SUGAR_CHECK", "PATTERN_BASED"
    val title: String,
    val message: String,
    val scheduledTime: String, // "HH:mm" format
    val wasDelivered: Boolean = true,
    val wasDismissed: Boolean = false,
    val wasSnoozed: Boolean = false,
    val snoozeCount: Int = 0,
    val userAction: String = "NONE" // "NONE", "DISMISSED", "SNOOZED", "OPENED"
)

/**
 * Manager for reminder history tracking
 */
object ReminderHistoryManager {
    private const val PREFS_NAME = "reminder_history"
    private const val KEY_HISTORY = "history_entries"
    private const val MAX_HISTORY_SIZE = 100 // Keep last 100 reminders
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Log a sent reminder
     */
    fun logReminder(
        context: Context,
        reminderType: ReminderType,
        title: String,
        message: String,
        scheduledTime: String
    ): String {
        val entry = ReminderHistoryEntry(
            reminderType = reminderType.name,
            title = title,
            message = message,
            scheduledTime = scheduledTime
        )
        
        val history = getHistory(context).toMutableList()
        history.add(0, entry) // Add to beginning (most recent first)
        
        // Trim to max size
        if (history.size > MAX_HISTORY_SIZE) {
            history.subList(MAX_HISTORY_SIZE, history.size).clear()
        }
        
        saveHistory(context, history)
        return entry.id
    }
    
    /**
     * Update reminder action (dismissed, snoozed, opened)
     */
    fun updateReminderAction(
        context: Context,
        reminderId: String,
        action: String,
        wasSnoozed: Boolean = false,
        snoozeCount: Int = 0
    ) {
        val history = getHistory(context).toMutableList()
        val index = history.indexOfFirst { it.id == reminderId }
        
        if (index != -1) {
            val updated = history[index].copy(
                userAction = action,
                wasDismissed = action == "DISMISSED",
                wasSnoozed = wasSnoozed,
                snoozeCount = snoozeCount
            )
            history[index] = updated
            saveHistory(context, history)
        }
    }
    
    /**
     * Get all reminder history
     */
    fun getHistory(context: Context): List<ReminderHistoryEntry> {
        val prefs = getPreferences(context)
        val jsonString = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        
        return try {
            json.decodeFromString<List<ReminderHistoryEntry>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get history filtered by date range
     */
    fun getHistoryByDateRange(
        context: Context,
        startTimestamp: Long,
        endTimestamp: Long
    ): List<ReminderHistoryEntry> {
        return getHistory(context).filter { entry ->
            entry.timestamp in startTimestamp..endTimestamp
        }
    }
    
    /**
     * Get history filtered by reminder type
     */
    fun getHistoryByType(
        context: Context,
        reminderType: ReminderType
    ): List<ReminderHistoryEntry> {
        return getHistory(context).filter { it.reminderType == reminderType.name }
    }
    
    /**
     * Get statistics for reminder effectiveness
     */
    fun getStatistics(context: Context): ReminderStatistics {
        val history = getHistory(context)
        
        return ReminderStatistics(
            totalSent = history.size,
            totalDismissed = history.count { it.wasDismissed },
            totalSnoozed = history.count { it.wasSnoozed },
            totalOpened = history.count { it.userAction == "OPENED" },
            mealReminders = history.count { it.reminderType == "MEAL_REMINDER" },
            bloodSugarReminders = history.count { it.reminderType == "BLOOD_SUGAR_CHECK" },
            patternReminders = history.count { it.reminderType == "PATTERN_BASED" },
            averageSnoozeCount = if (history.isNotEmpty()) {
                history.sumOf { it.snoozeCount }.toFloat() / history.size
            } else 0f
        )
    }
    
    /**
     * Clear all history
     */
    fun clearHistory(context: Context) {
        getPreferences(context).edit().remove(KEY_HISTORY).apply()
    }
    
    /**
     * Clear history older than specified days
     */
    fun clearOldHistory(context: Context, daysToKeep: Int) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        val history = getHistory(context).filter { it.timestamp >= cutoffTime }
        saveHistory(context, history)
    }
    
    private fun saveHistory(context: Context, history: List<ReminderHistoryEntry>) {
        val jsonString = json.encodeToString(history)
        getPreferences(context).edit().putString(KEY_HISTORY, jsonString).apply()
    }
}

/**
 * Statistics about reminder effectiveness
 */
data class ReminderStatistics(
    val totalSent: Int,
    val totalDismissed: Int,
    val totalSnoozed: Int,
    val totalOpened: Int,
    val mealReminders: Int,
    val bloodSugarReminders: Int,
    val patternReminders: Int,
    val averageSnoozeCount: Float
)
