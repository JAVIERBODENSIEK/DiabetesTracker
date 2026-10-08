package com.j4.diabetestracker

import android.content.Context
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ReminderConfirmationStorage {
    private const val TAG = "ConfirmationStorage"
    private const val PREFS_NAME = "reminder_confirmations"
    private const val KEY_CONFIRMATIONS = "confirmations_list"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

    fun addPendingConfirmation(
        context: Context,
        reminderId: String,
        reminderName: String,
        reminderEmoji: String,
        scheduledTime: String
    ) {
        try {
            val confirmations = loadConfirmations(context).toMutableList()
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val confirmation = ReminderConfirmation(
                id = java.util.UUID.randomUUID().toString(),
                reminderId = reminderId,
                reminderName = reminderName,
                reminderEmoji = reminderEmoji,
                scheduledTime = scheduledTime,
                date = dateFormat.format(java.util.Date()),
                timestamp = System.currentTimeMillis(),
                status = ConfirmationStatus.PENDING
            )
            confirmations.add(confirmation)
            saveConfirmations(context, confirmations)
            Log.d(TAG, "Added pending: ${reminderEmoji} ${reminderName} at $scheduledTime")
        } catch (e: Exception) {
            Log.e(TAG, "addPendingConfirmation failed", e)
        }
    }

    fun respondToConfirmation(
        context: Context,
        confirmationId: String,
        status: ConfirmationStatus,
        comment: String = ""
    ) {
        try {
            val confirmations = loadConfirmations(context).toMutableList()
            val index = confirmations.indexOfFirst { it.id == confirmationId }
            if (index != -1) {
                confirmations[index] = confirmations[index].copy(
                    status = status,
                    comment = comment,
                    respondedAt = System.currentTimeMillis()
                )
                saveConfirmations(context, confirmations)
                Log.d(TAG, "Responded to $confirmationId: $status")
            }
        } catch (e: Exception) {
            Log.e(TAG, "respondToConfirmation failed", e)
        }
    }

    fun getPendingConfirmations(context: Context): List<ReminderConfirmation> {
        return loadConfirmations(context).filter { it.status == ConfirmationStatus.PENDING }
    }

    fun getHistory(context: Context): List<ReminderConfirmation> {
        return loadConfirmations(context)
            .filter { it.status != ConfirmationStatus.PENDING }
            .sortedByDescending { it.respondedAt }
    }

    fun updateConfirmation(
        context: Context,
        confirmationId: String,
        newStatus: ConfirmationStatus,
        newComment: String
    ) {
        try {
            val confirmations = loadConfirmations(context).toMutableList()
            val index = confirmations.indexOfFirst { it.id == confirmationId }
            if (index != -1) {
                confirmations[index] = confirmations[index].copy(
                    status = newStatus,
                    comment = newComment
                )
                saveConfirmations(context, confirmations)
                Log.d(TAG, "Updated confirmation $confirmationId: $newStatus")
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateConfirmation failed", e)
        }
    }

    fun getHistoryForReminder(context: Context, reminderId: String): List<ReminderConfirmation> {
        return loadConfirmations(context)
            .filter { it.reminderId == reminderId && it.status != ConfirmationStatus.PENDING }
            .sortedByDescending { it.respondedAt }
    }

    fun loadConfirmations(context: Context): List<ReminderConfirmation> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonString = prefs.getString(KEY_CONFIRMATIONS, null) ?: return emptyList()
            json.decodeFromString<List<ReminderConfirmation>>(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "loadConfirmations failed", e)
            emptyList()
        }
    }

    private fun saveConfirmations(context: Context, confirmations: List<ReminderConfirmation>) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonString = json.encodeToString(confirmations)
            prefs.edit().putString(KEY_CONFIRMATIONS, jsonString).apply()
        } catch (e: Exception) {
            Log.e(TAG, "saveConfirmations failed", e)
        }
    }

    // === Alert System ===
    private const val KEY_ALERTS = "confirmation_alerts"

    @kotlinx.serialization.Serializable
    data class ConfirmationAlert(
        val id: String = "",
        val reminderId: String = "",
        val reminderName: String = "",
        val reminderEmoji: String = "",
        val alertType: String = "", // "no_streak", "yes_streak", "missed"
        val streakCount: Int = 0,
        val timestamp: Long = 0L,
        val dismissed: Boolean = false
    )

    fun checkAndTriggerAlerts(
        context: Context,
        reminderId: String,
        alertSettings: ConfirmationAlertSettings
    ) {
        try {
            val history = getHistoryForReminder(context, reminderId)
            if (history.isEmpty()) return

            val reminder = history.firstOrNull() ?: return
            val alerts = loadAlerts(context).toMutableList()

            // Check consecutive NO streak
            if (alertSettings.alertOnNoStreak) {
                val noStreak = history.takeWhile {
                    it.status == ConfirmationStatus.NO
                }.size
                if (noStreak >= alertSettings.noStreakThreshold) {
                    val existing = alerts.any {
                        it.reminderId == reminderId && it.alertType == "no_streak" && !it.dismissed
                    }
                    if (!existing) {
                        alerts.add(ConfirmationAlert(
                            id = java.util.UUID.randomUUID().toString(),
                            reminderId = reminderId,
                            reminderName = reminder.reminderName,
                            reminderEmoji = reminder.reminderEmoji,
                            alertType = "no_streak",
                            streakCount = noStreak,
                            timestamp = System.currentTimeMillis()
                        ))
                    }
                }
            }

            // Check consecutive YES streak (positive reinforcement)
            if (alertSettings.alertOnYesStreak) {
                val yesStreak = history.takeWhile {
                    it.status == ConfirmationStatus.YES || it.status == ConfirmationStatus.COMMENTED
                }.size
                if (yesStreak >= alertSettings.yesStreakThreshold) {
                    val existing = alerts.any {
                        it.reminderId == reminderId && it.alertType == "yes_streak" && !it.dismissed
                    }
                    if (!existing) {
                        alerts.add(ConfirmationAlert(
                            id = java.util.UUID.randomUUID().toString(),
                            reminderId = reminderId,
                            reminderName = reminder.reminderName,
                            reminderEmoji = reminder.reminderEmoji,
                            alertType = "yes_streak",
                            streakCount = yesStreak,
                            timestamp = System.currentTimeMillis()
                        ))
                    }
                }
            }

            // Check missed (PENDING) streak
            if (alertSettings.alertOnMissed) {
                val allConfirmations = loadConfirmations(context)
                    .filter { it.reminderId == reminderId }
                    .sortedByDescending { it.timestamp }
                val missedStreak = allConfirmations.takeWhile {
                    it.status == ConfirmationStatus.PENDING
                }.size
                if (missedStreak >= alertSettings.missedThreshold) {
                    val existing = alerts.any {
                        it.reminderId == reminderId && it.alertType == "missed" && !it.dismissed
                    }
                    if (!existing) {
                        alerts.add(ConfirmationAlert(
                            id = java.util.UUID.randomUUID().toString(),
                            reminderId = reminderId,
                            reminderName = reminder.reminderName,
                            reminderEmoji = reminder.reminderEmoji,
                            alertType = "missed",
                            streakCount = missedStreak,
                            timestamp = System.currentTimeMillis()
                        ))
                    }
                }
            }

            saveAlerts(context, alerts)
        } catch (e: Exception) {
            Log.e(TAG, "checkAndTriggerAlerts failed", e)
        }
    }

    fun getActiveAlerts(context: Context): List<ConfirmationAlert> {
        return loadAlerts(context).filter { !it.dismissed }.sortedByDescending { it.timestamp }
    }

    fun dismissAlert(context: Context, alertId: String) {
        try {
            val alerts = loadAlerts(context).toMutableList()
            val index = alerts.indexOfFirst { it.id == alertId }
            if (index != -1) {
                alerts[index] = alerts[index].copy(dismissed = true)
                saveAlerts(context, alerts)
            }
        } catch (e: Exception) {
            Log.e(TAG, "dismissAlert failed", e)
        }
    }

    private fun loadAlerts(context: Context): List<ConfirmationAlert> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonString = prefs.getString(KEY_ALERTS, null) ?: return emptyList()
            json.decodeFromString<List<ConfirmationAlert>>(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "loadAlerts failed", e)
            emptyList()
        }
    }

    private fun saveAlerts(context: Context, alerts: List<ConfirmationAlert>) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonString = json.encodeToString(alerts)
            prefs.edit().putString(KEY_ALERTS, jsonString).apply()
        } catch (e: Exception) {
            Log.e(TAG, "saveAlerts failed", e)
        }
    }

    fun cleanupOldEntries(context: Context, keepDays: Int = 90) {
        try {
            val cutoff = System.currentTimeMillis() - (keepDays.toLong() * 24 * 60 * 60 * 1000)
            val confirmations = loadConfirmations(context)
            val filtered = confirmations.filter {
                it.status == ConfirmationStatus.PENDING || it.timestamp > cutoff
            }
            if (filtered.size < confirmations.size) {
                saveConfirmations(context, filtered)
                Log.d(TAG, "Cleaned up ${confirmations.size - filtered.size} old entries")
            }
        } catch (e: Exception) {
            Log.e(TAG, "cleanupOldEntries failed", e)
        }
    }
}
