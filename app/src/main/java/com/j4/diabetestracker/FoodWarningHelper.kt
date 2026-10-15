package com.j4.diabetestracker

import android.content.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
        if (isFoodContradicted(foodName, context, patterns)) {
            return null
        }
        
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
        if (isFoodContradicted(foodName, context, patterns)) {
            return emptyList()
        }

        return patterns.filter { pattern ->
            pattern.patternType == PatternType.FOOD_MARKER_CORRELATION &&
            pattern.correlatedItem.equals(foodName, ignoreCase = true)
        }
    }

    fun isFoodContradicted(
        foodName: String,
        context: Context,
        patterns: List<DetectedPattern> = PatternStorageManager.getActivePatterns(context)
    ): Boolean {
        val foodKey = normalizeFoodWarningKey(foodName)
        if (foodKey.isBlank()) return false

        val foodPatterns = patterns.filter { pattern ->
            pattern.patternType == PatternType.FOOD_MARKER_CORRELATION &&
                normalizeFoodWarningKey(pattern.correlatedItem) == foodKey
        }
        if (foodPatterns.isEmpty()) return false

        val entries = DataManager.getEntries(context)
        val entrySnapshots = entries.mapNotNull { entry ->
            val date = parseFoodWarningDate(entry.date) ?: return@mapNotNull null
            val consumedFoodKeys = entry.foodEntriesByColumn.values
                .flatten()
                .map { normalizeFoodWarningKey(it.name) }
                .filter { it.isNotBlank() }
                .toSet()
            val hasFeelBadMarker = entry.markers.any { marker ->
                marker.type.equals("feel_bad", ignoreCase = true) ||
                    marker.type.equals("i feel bad", ignoreCase = true)
            }
            FoodWarningEntrySnapshot(
                date = date,
                consumedFoodKeys = consumedFoodKeys,
                hasFeelBadMarker = hasFeelBadMarker,
            )
        }

        val supportDates = foodPatterns
            .flatMap { pattern -> pattern.details }
            .mapNotNull { detail -> parseFoodWarningDate(detail.date) }
            .distinct()
            .ifEmpty {
                entrySnapshots
                    .filter { snapshot -> foodKey in snapshot.consumedFoodKeys }
                    .map { snapshot -> snapshot.date }
                    .distinct()
            }

        val earliestSupportDate = supportDates.minOrNull() ?: return false
        return entrySnapshots.any { snapshot ->
            snapshot.hasFeelBadMarker &&
                !snapshot.date.isBefore(earliestSupportDate) &&
                foodKey !in snapshot.consumedFoodKeys
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

    private data class FoodWarningEntrySnapshot(
        val date: LocalDate,
        val consumedFoodKeys: Set<String>,
        val hasFeelBadMarker: Boolean,
    )

    private fun normalizeFoodWarningKey(raw: String): String = raw.trim().lowercase(Locale.ROOT)

    private fun parseFoodWarningDate(rawDate: String): LocalDate? {
        val formats = listOf("dd.MM.yyyy", "dd-MM-yyyy", "yyyy-MM-dd")
        formats.forEach { pattern ->
            runCatching {
                return LocalDate.parse(rawDate, DateTimeFormatter.ofPattern(pattern))
            }
        }
        return null
    }
}
