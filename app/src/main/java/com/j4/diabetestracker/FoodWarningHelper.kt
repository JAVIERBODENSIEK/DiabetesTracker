package com.j4.diabetestracker

import android.content.Context

/**
 * Helper object for checking food patterns and generating warnings
 */
object FoodWarningHelper {
    
    /**
     * Check if a food has any existing patterns and return a warning message
     * @param foodName The name of the food to check
     * @param context Android context for accessing stored patterns
     * @return Warning message if pattern exists, null otherwise
     */
    fun checkFoodPattern(foodName: String, context: Context): String? {
        // Skip if food is ignored
        if (IgnoredFoodManager.isFoodIgnored(context, foodName)) {
            return null
        }
        
        // Get active patterns
        val patterns = PatternStorageManager.getActivePatterns(context)
        
        // Find patterns related to this food
        val foodPatterns = patterns.filter { pattern ->
            pattern.patternType == PatternType.FOOD_MARKER_CORRELATION &&
            pattern.correlatedItem.equals(foodName, ignoreCase = true)
        }
        
        if (foodPatterns.isEmpty()) {
            return null
        }
        
        // Get the highest confidence pattern for this food
        val topPattern = foodPatterns.maxByOrNull { it.confidenceScore }
        
        return if (topPattern != null) {
            when {
                topPattern.confidenceScore >= 0.8f -> 
                    "⚠️ WARNING: You've experienced '${topPattern.markerName}' after eating $foodName ${topPattern.occurrences} times. High risk!"
                topPattern.confidenceScore >= 0.6f -> 
                    "⚠️ CAUTION: You've experienced '${topPattern.markerName}' after eating $foodName ${topPattern.occurrences} times."
                else -> 
                    "ℹ️ Note: You've experienced '${topPattern.markerName}' after eating $foodName ${topPattern.occurrences} times."
            }
        } else {
            null
        }
    }
    
    /**
     * Get all patterns for a specific food
     */
    fun getFoodPatterns(foodName: String, context: Context): List<DetectedPattern> {
        if (IgnoredFoodManager.isFoodIgnored(context, foodName)) {
            return emptyList()
        }
        
        val patterns = PatternStorageManager.getActivePatterns(context)
        return patterns.filter { pattern ->
            pattern.patternType == PatternType.FOOD_MARKER_CORRELATION &&
            pattern.correlatedItem.equals(foodName, ignoreCase = true)
        }
    }
    
    /**
     * Get risk level for a food (0 = safe, 1 = low, 2 = medium, 3 = high)
     */
    fun getFoodRiskLevel(foodName: String, context: Context): Int {
        if (IgnoredFoodManager.isFoodIgnored(context, foodName)) {
            return 0 // Safe/ignored
        }
        
        val patterns = getFoodPatterns(foodName, context)
        if (patterns.isEmpty()) {
            return 0 // No patterns = safe
        }
        
        val maxConfidence = patterns.maxOfOrNull { it.confidenceScore } ?: 0f
        
        return when {
            maxConfidence >= 0.8f -> 3 // High risk
            maxConfidence >= 0.6f -> 2 // Medium risk
            maxConfidence >= 0.4f -> 1 // Low risk
            else -> 0 // Safe
        }
    }
    
    /**
     * Get color for food highlighting based on risk level
     */
    fun getFoodHighlightColor(foodName: String, context: Context): androidx.compose.ui.graphics.Color? {
        val riskLevel = getFoodRiskLevel(foodName, context)
        
        return when (riskLevel) {
            3 -> androidx.compose.ui.graphics.Color(0xFFFFCDD2) // Light red
            2 -> androidx.compose.ui.graphics.Color(0xFFFFE0B2) // Light orange
            1 -> androidx.compose.ui.graphics.Color(0xFFFFF9C4) // Light yellow
            else -> null // No highlighting
        }
    }
}
