package com.j4.diabetestracker

import android.content.Context
import android.util.Log
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

object CustomReminderStorage {
    private const val TAG = "CustomReminderStorage"
    private const val PREFS_NAME = "custom_reminders"
    private const val KEY_REMINDERS = "reminders_list"
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }
    
    fun saveReminders(context: Context, reminders: List<CustomReminder>) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonString = json.encodeToString(reminders)
            prefs.edit().putString(KEY_REMINDERS, jsonString).apply()
            Log.d(TAG, "saveReminders: saved ${reminders.size} reminders")
        } catch (e: Exception) {
            Log.e(TAG, "saveReminders: failed", e)
        }
    }
    
    fun loadReminders(context: Context): List<CustomReminder> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonString = prefs.getString(KEY_REMINDERS, null)
            if (jsonString == null) {
                Log.d(TAG, "loadReminders: no saved data, creating and saving defaults")
                val defaults = getDefaultReminders(context)
                saveReminders(context, defaults)
                return defaults
            }
            
            Log.d(TAG, "loadReminders: raw JSON length=${jsonString.length}")
            Log.d(TAG, "loadReminders: raw JSON=${jsonString.take(500)}")
            
            // First: migrate JSON to fix Int→Float for intervalHours
            val migrated = migrateJsonString(jsonString)
            if (migrated != jsonString) {
                Log.d(TAG, "loadReminders: migration changed data")
                Log.d(TAG, "loadReminders: migrated JSON=${migrated.take(500)}")
            }
            
            val result = json.decodeFromString<List<CustomReminder>>(migrated)
            Log.d(TAG, "loadReminders: decoded ${result.size} reminders OK")
            result.forEach { r ->
                Log.d(TAG, "  reminder: id=${r.id.take(8)}, name=${r.name}, intervalHours=${r.intervalHours}, intervalStartMinute=${r.intervalStartMinute}")
            }
            
            // Re-save if migration changed the data
            if (migrated != jsonString) {
                saveReminders(context, result)
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "loadReminders: FAILED, returning defaults", e)
            val defaults = getDefaultReminders(context)
            saveReminders(context, defaults)
            defaults
        }
    }
    
    private fun migrateJsonString(jsonString: String): String {
        return try {
            val element = Json.parseToJsonElement(jsonString)
            if (element !is JsonArray) return jsonString
            
            val migrated = JsonArray(element.map { item ->
                if (item !is JsonObject) return@map item
                val mutable = item.toMutableMap()
                
                // Fix intervalHours: ensure it's a float (has decimal point)
                val intervalHours = mutable["intervalHours"]
                if (intervalHours is JsonPrimitive && intervalHours.isString.not()) {
                    val content = intervalHours.content
                    if (!content.contains(".")) {
                        mutable["intervalHours"] = JsonPrimitive(content.toDoubleOrNull() ?: 0.0)
                    }
                }
                
                JsonObject(mutable)
            })
            migrated.toString()
        } catch (e: Exception) {
            Log.w(TAG, "migrateJsonString: JSON element migration failed, trying regex", e)
            // Fallback: regex-based migration
            jsonString.replace(
                Regex("\"intervalHours\"\\s*:\\s*(\\d+)(?!\\.)"),
                "\"intervalHours\":\$1.0"
            )
        }
    }
    
    fun addReminder(context: Context, reminder: CustomReminder) {
        try {
            val reminders = loadReminders(context).toMutableList()
            val newReminder = reminder.copy(
                id = java.util.UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis()
            )
            reminders.add(newReminder)
            saveReminders(context, reminders)
            Log.d(TAG, "addReminder: added ${newReminder.name}")
        } catch (e: Exception) {
            Log.e(TAG, "addReminder: failed", e)
        }
    }
    
    fun updateReminder(context: Context, reminder: CustomReminder) {
        try {
            Log.d(TAG, "updateReminder: id=${reminder.id}, intervalHours=${reminder.intervalHours}")
            val reminders = loadReminders(context).toMutableList()
            val index = reminders.indexOfFirst { it.id == reminder.id }
            if (index != -1) {
                reminders[index] = reminder
                saveReminders(context, reminders)
                Log.d(TAG, "updateReminder: saved successfully")
            } else {
                Log.w(TAG, "updateReminder: reminder not found with id=${reminder.id}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateReminder: failed", e)
        }
    }
    
    fun deleteReminder(context: Context, reminderId: String) {
        try {
            val reminders = loadReminders(context).toMutableList()
            reminders.removeAll { it.id == reminderId }
            saveReminders(context, reminders)
        } catch (e: Exception) {
            Log.e(TAG, "deleteReminder: failed", e)
        }
    }
    
    fun getDefaultReminders(context: Context): List<CustomReminder> {
        val language = try {
            val settings = SettingsManager.loadSettings(context)
            settings.language
        } catch (e: Exception) {
            "English" // Fallback to English if settings can't be loaded
        }
        
        val timestamp = System.currentTimeMillis()
        return when (language) {
            "German" -> listOf(
                CustomReminder(
                    id = java.util.UUID.randomUUID().toString(),
                    emoji = "💧",
                    name = "Wasser trinken",
                    times = listOf(
                        ReminderTime(8, 0),
                        ReminderTime(12, 0),
                        ReminderTime(16, 0),
                        ReminderTime(20, 0)
                    ),
                    isEnabled = false,
                    createdAt = timestamp
                ),
                CustomReminder(
                    id = java.util.UUID.randomUUID().toString(),
                    emoji = "💊",
                    name = "Medikament nehmen",
                    times = listOf(
                        ReminderTime(9, 0),
                        ReminderTime(21, 0)
                    ),
                    isEnabled = false,
                    createdAt = timestamp
                )
            )
            "Spanish" -> listOf(
                CustomReminder(
                    id = java.util.UUID.randomUUID().toString(),
                    emoji = "💧",
                    name = "Beber agua",
                    times = listOf(
                        ReminderTime(8, 0),
                        ReminderTime(12, 0),
                        ReminderTime(16, 0),
                        ReminderTime(20, 0)
                    ),
                    isEnabled = false,
                    createdAt = timestamp
                ),
                CustomReminder(
                    id = java.util.UUID.randomUUID().toString(),
                    emoji = "💊",
                    name = "Tomar medicamento",
                    times = listOf(
                        ReminderTime(9, 0),
                        ReminderTime(21, 0)
                    ),
                    isEnabled = false,
                    createdAt = timestamp
                )
            )
            else -> listOf(
                CustomReminder(
                    id = java.util.UUID.randomUUID().toString(),
                    emoji = "💧",
                    name = "Drink water",
                    times = listOf(
                        ReminderTime(8, 0),
                        ReminderTime(12, 0),
                        ReminderTime(16, 0),
                        ReminderTime(20, 0)
                    ),
                    isEnabled = false,
                    createdAt = timestamp
                ),
                CustomReminder(
                    id = java.util.UUID.randomUUID().toString(),
                    emoji = "💊",
                    name = "Take medication",
                    times = listOf(
                        ReminderTime(9, 0),
                        ReminderTime(21, 0)
                    ),
                    isEnabled = false,
                    createdAt = timestamp
                )
            )
        }
    }
}
