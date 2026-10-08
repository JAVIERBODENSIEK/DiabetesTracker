package com.j4.diabetestracker

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Data class for foods that user has marked as safe/ignored
 */
@Serializable
data class IgnoredFood(
    val id: String = java.util.UUID.randomUUID().toString(),
    val foodName: String,
    val reason: String,
    val ignoredDate: Long = System.currentTimeMillis(),
    val patternId: String? = null, // Link to original pattern that triggered this
    val notes: String = ""
)

/**
 * Manager for storing and retrieving ignored/whitelisted foods
 */
object IgnoredFoodManager {
    private const val PREFS_NAME = "ignored_foods"
    private const val KEY_IGNORED_FOODS = "ignored_foods_list"
    
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    
    /**
     * Add a food to the ignore list
     */
    fun ignoreFood(
        context: Context,
        foodName: String,
        reason: String,
        patternId: String? = null,
        notes: String = ""
    ) {
        val currentList = getIgnoredFoods(context).toMutableList()
        
        // Check if food is already ignored
        if (currentList.any { it.foodName.equals(foodName, ignoreCase = true) }) {
            return // Already ignored
        }
        
        val ignoredFood = IgnoredFood(
            foodName = foodName,
            reason = reason,
            patternId = patternId,
            notes = notes
        )
        
        currentList.add(ignoredFood)
        saveIgnoredFoods(context, currentList)
    }
    
    /**
     * Remove a food from the ignore list
     */
    fun unignoreFood(context: Context, foodId: String) {
        val currentList = getIgnoredFoods(context).toMutableList()
        currentList.removeAll { it.id == foodId }
        saveIgnoredFoods(context, currentList)
    }
    
    /**
     * Check if a food is ignored
     */
    fun isFoodIgnored(context: Context, foodName: String): Boolean {
        return getIgnoredFoods(context).any { 
            it.foodName.equals(foodName, ignoreCase = true) 
        }
    }
    
    /**
     * Get all ignored foods
     */
    fun getIgnoredFoods(context: Context): List<IgnoredFood> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_IGNORED_FOODS, null) ?: return emptyList()
        
        return try {
            this.json.decodeFromString<List<IgnoredFood>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Save ignored foods list
     */
    private fun saveIgnoredFoods(context: Context, foods: List<IgnoredFood>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = json.encodeToString(foods)
        prefs.edit().putString(KEY_IGNORED_FOODS, jsonString).apply()
    }
    
    /**
     * Clear all ignored foods
     */
    fun clearAllIgnoredFoods(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_IGNORED_FOODS).apply()
    }
    
    /**
     * Get ignored food by ID
     */
    fun getIgnoredFoodById(context: Context, foodId: String): IgnoredFood? {
        return getIgnoredFoods(context).firstOrNull { it.id == foodId }
    }
    
    /**
     * Get ignored food by name
     */
    fun getIgnoredFoodByName(context: Context, foodName: String): IgnoredFood? {
        return getIgnoredFoods(context).firstOrNull { 
            it.foodName.equals(foodName, ignoreCase = true) 
        }
    }
}
