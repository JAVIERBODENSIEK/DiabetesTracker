package com.j4.diabetestracker

import android.content.Context
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Data class representing a detected pattern in the user's health data
 */
@Serializable
data class DetectedPattern(
    val id: String = java.util.UUID.randomUUID().toString(),
    val patternType: PatternType,
    val markerName: String, // Name of the marker (e.g., "Feel Bad", "Headache")
    val correlatedItem: String, // Food name or other context
    val occurrences: Int, // Number of times this pattern occurred
    val confidenceScore: Float, // 0.0 to 1.0
    val timeRange: String, // e.g., "Last 30 days", "This month"
    val firstDetected: Long = System.currentTimeMillis(),
    val lastOccurrence: Long = System.currentTimeMillis(),
    val details: List<PatternOccurrence> = emptyList(),
    val suggestion: String = "" // AI-generated suggestion
)

@Serializable
enum class PatternType {
    FOOD_MARKER_CORRELATION, // Food causes marker (e.g., pizza -> feel bad)
    TIME_MARKER_CORRELATION, // Time of day causes marker (e.g., evening -> headache)
    BLOOD_SUGAR_SPIKE, // Unusual blood sugar spike after food
    BLOOD_SUGAR_DROP, // Unusual blood sugar drop
    INSULIN_PATTERN, // Unusual insulin patterns
    CUSTOM_COLUMN_PATTERN // Pattern in custom columns
}

@Serializable
data class PatternOccurrence(
    val date: String,
    val timeOfDay: String, // "morning", "afternoon", "evening", "night"
    val context: String, // What happened (e.g., "Ate pizza")
    val markerApplied: String, // Which marker was applied
    val bloodSugar: String = "", // Blood sugar value if relevant
    val notes: String = ""
)

/**
 * Pattern detection engine that analyzes diary entries to find correlations
 */
object PatternDetectionEngine {
    
    // Date formatter for parsing dd-MM-yyyy format
    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val dottedDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    
    /**
     * Analyze entries and detect patterns
     */
    fun detectPatterns(
        entries: List<DiabetesEntry>,
        customMarkers: List<CustomMarkerType>,
        daysToAnalyze: Int = 30,
        context: Context? = null,
        dateRange: ClosedRange<LocalDate>? = null
    ): List<DetectedPattern> {
        val patterns = mutableListOf<DetectedPattern>()

        val analysisDays = if (dateRange != null) {
            kotlin.math.max(1, ChronoUnit.DAYS.between(dateRange.start, dateRange.endInclusive).toInt() + 1)
        } else {
            daysToAnalyze
        }

        // Filter entries to the requested time range
        val recentEntries = entries.filter { entry ->
            val entryDate = parseEntryDate(entry.date) ?: return@filter false
            if (dateRange != null) {
                !entryDate.isBefore(dateRange.start) && !entryDate.isAfter(dateRange.endInclusive)
            } else {
                val cutoffDate = LocalDate.now().minusDays(daysToAnalyze.toLong())
                !entryDate.isBefore(cutoffDate)
            }
        }
        
        // Detect food-marker correlations
        patterns.addAll(detectFoodMarkerCorrelations(recentEntries, customMarkers, analysisDays, context))
        
        // Detect time-based patterns
        patterns.addAll(detectTimeBasedPatterns(recentEntries, customMarkers, analysisDays))
        
        // Detect blood sugar patterns
        patterns.addAll(detectBloodSugarPatterns(recentEntries, analysisDays, context))
        
        // Sort by confidence score (highest first)
        return patterns.sortedByDescending { it.confidenceScore }
    }
    
    /**
     * Detect correlations between foods and markers (e.g., "pizza -> feel bad")
     */
    /**
     * Get the next time period for delayed reaction detection
     */
    private fun getNextTimePeriod(currentPeriod: String): String? {
        return when (currentPeriod) {
            "morning" -> "afternoon"
            "afternoon" -> "evening"
            "evening" -> "night"
            else -> null
        }
    }
    
    private fun detectFoodMarkerCorrelations(
        entries: List<DiabetesEntry>,
        customMarkers: List<CustomMarkerType>,
        daysToAnalyze: Int,
        context: Context? = null
    ): List<DetectedPattern> {
        val patterns = mutableListOf<DetectedPattern>()
        val foodMarkerMap = mutableMapOf<Pair<String, String>, MutableList<PatternOccurrence>>()
        
        // Build correlation map
        for (entry in entries) {
            val markers = entry.markers
            if (markers.isEmpty()) continue
            
            // Check each time period
            listOf("morning", "afternoon", "evening", "night").forEach { timeOfDay ->
                // Get all foods for this time period from all columns
                val foodsForTime = entry.foodEntriesByColumn
                    .filter { (columnId, _) -> columnId.contains(timeOfDay, ignoreCase = true) }
                    .flatMap { (_, foods) -> foods }
                
                if (foodsForTime.isEmpty()) return@forEach
                
                // Check for IMMEDIATE reactions (same time period)
                val markersForSameTime = markers.filter { it.startTime == timeOfDay }
                
                for (food in foodsForTime) {
                    // Skip ignored foods
                    if (context != null && IgnoredFoodManager.isFoodIgnored(context, food.name)) {
                        continue
                    }
                    
                    // Immediate reactions (same time period)
                    for (marker in markersForSameTime) {
                        val markerName = getMarkerName(marker, customMarkers)
                        val key = Pair(food.name, "$markerName (immediate)")
                        
                        val occurrence = PatternOccurrence(
                            date = entry.date,
                            timeOfDay = timeOfDay,
                            context = "Ate ${food.name} → ${markerName} (same time)",
                            markerApplied = markerName,
                            bloodSugar = getBloodSugarForTime(entry, timeOfDay),
                            notes = "Immediate reaction"
                        )
                        
                        foodMarkerMap.getOrPut(key) { mutableListOf() }.add(occurrence)
                    }
                    
                    // DELAYED reactions (next time period)
                    val nextPeriod = getNextTimePeriod(timeOfDay)
                    if (nextPeriod != null) {
                        val markersForNextTime = markers.filter { it.startTime == nextPeriod }
                        
                        for (marker in markersForNextTime) {
                            val markerName = getMarkerName(marker, customMarkers)
                            val key = Pair(food.name, "$markerName (delayed)")
                            
                            val occurrence = PatternOccurrence(
                                date = entry.date,
                                timeOfDay = "$timeOfDay → $nextPeriod",
                                context = "Ate ${food.name} ($timeOfDay) → ${markerName} ($nextPeriod)",
                                markerApplied = markerName,
                                bloodSugar = getBloodSugarForTime(entry, timeOfDay),
                                notes = "Delayed reaction (${timeOfDay} → ${nextPeriod})"
                            )
                            
                            foodMarkerMap.getOrPut(key) { mutableListOf() }.add(occurrence)
                        }
                    }
                }
            }
        }
        
        // Analyze correlations and create patterns
        for ((key, occurrences) in foodMarkerMap) {
            val (foodName, markerName) = key
            val occurrenceCount = occurrences.size
            
            // Only report patterns with 2+ occurrences
            if (occurrenceCount >= 2) {
                var confidence = calculateConfidence(occurrenceCount, daysToAnalyze)
                
                // Reduce confidence slightly for delayed reactions (less certain)
                val isDelayed = markerName.contains("(delayed)")
                if (isDelayed) {
                    confidence *= 0.85f // 15% reduction for delayed patterns
                }
                
                // Clean marker name for display
                val cleanMarkerName = markerName.replace(" (immediate)", "").replace(" (delayed)", "")
                val reactionType = when {
                    markerName.contains("(immediate)") -> "immediate"
                    markerName.contains("(delayed)") -> "delayed"
                    else -> "immediate"
                }
                
                val suggestion = generateSuggestion(foodName, cleanMarkerName, occurrenceCount, reactionType)
                
                val pattern = DetectedPattern(
                    patternType = PatternType.FOOD_MARKER_CORRELATION,
                    markerName = cleanMarkerName,
                    correlatedItem = foodName,
                    occurrences = occurrenceCount,
                    confidenceScore = confidence,
                    timeRange = "Last $daysToAnalyze days",
                    firstDetected = occurrences.minOf { parseDate(it.date) },
                    lastOccurrence = occurrences.maxOf { parseDate(it.date) },
                    details = occurrences.sortedByDescending { it.date },
                    suggestion = suggestion
                )
                
                patterns.add(pattern)
            }
        }
        
        return patterns
    }
    
    /**
     * Detect time-based patterns (e.g., "always feel bad in the evening")
     */
    private fun detectTimeBasedPatterns(
        entries: List<DiabetesEntry>,
        customMarkers: List<CustomMarkerType>,
        daysToAnalyze: Int
    ): List<DetectedPattern> {
        val patterns = mutableListOf<DetectedPattern>()
        val timeMarkerMap = mutableMapOf<Pair<String, String>, MutableList<PatternOccurrence>>()
        
        for (entry in entries) {
            val markers = entry.markers
            if (markers.isEmpty()) continue
            
            for (marker in markers) {
                val markerName = getMarkerName(marker, customMarkers)
                val timeOfDay = marker.startTime
                val key = Pair(timeOfDay, markerName)
                
                val occurrence = PatternOccurrence(
                    date = entry.date,
                    timeOfDay = timeOfDay,
                    context = "Marker applied at $timeOfDay",
                    markerApplied = markerName,
                    bloodSugar = getBloodSugarForTime(entry, timeOfDay)
                )
                
                timeMarkerMap.getOrPut(key) { mutableListOf() }.add(occurrence)
            }
        }
        
        // Analyze time-based patterns
        for ((key, occurrences) in timeMarkerMap) {
            val (timeOfDay, markerName) = key
            val occurrenceCount = occurrences.size
            
            // Only report if it happens frequently (3+ times)
            if (occurrenceCount >= 3) {
                val confidence = calculateConfidence(occurrenceCount, daysToAnalyze)
                
                val suggestion = "You often experience '$markerName' during $timeOfDay. Consider tracking what you do or eat before this time."
                
                val pattern = DetectedPattern(
                    patternType = PatternType.TIME_MARKER_CORRELATION,
                    markerName = markerName,
                    correlatedItem = timeOfDay.capitalize(),
                    occurrences = occurrenceCount,
                    confidenceScore = confidence,
                    timeRange = "Last $daysToAnalyze days",
                    firstDetected = occurrences.minOf { parseDate(it.date) },
                    lastOccurrence = occurrences.maxOf { parseDate(it.date) },
                    details = occurrences.sortedByDescending { it.date },
                    suggestion = suggestion
                )
                
                patterns.add(pattern)
            }
        }
        
        return patterns
    }
    
    /**
     * Detect blood sugar spike/drop patterns
     */
    private fun detectBloodSugarPatterns(
        entries: List<DiabetesEntry>,
        daysToAnalyze: Int,
        context: Context? = null
    ): List<DetectedPattern> {
        val patterns = mutableListOf<DetectedPattern>()
        
        // Track food -> blood sugar spike correlations
        val foodSpikeMap = mutableMapOf<String, MutableList<BloodSugarChange>>()
        
        // Analyze each time period
        val timePeriods = listOf(
            Triple("morningBloodSugarLevel", "afternoonBloodSugarLevel", "morning"),
            Triple("afternoonBloodSugarLevel", "eveningBloodSugarLevel", "afternoon"),
            Triple("eveningBloodSugarLevel", "nightBloodSugarLevel", "evening")
        )
        
        entries.forEach { entry ->
            timePeriods.forEach { (beforeColumn, afterColumn, timeOfDay) ->
                val beforeBS = getBloodSugarValue(entry, beforeColumn)
                val afterBS = getBloodSugarValue(entry, afterColumn)
                
                if (beforeBS > 0 && afterBS > 0) {
                    val spike = afterBS - beforeBS
                    
                    // Consider it a spike if increase is >= 30 mg/dL
                    if (spike >= 30) {
                        // Get foods eaten during this time period
                        val foods = entry.foodEntriesByColumn[beforeColumn] ?: emptyList()
                        
                        foods.forEach { food ->
                            // Skip ignored foods
                            if (context != null && IgnoredFoodManager.isFoodIgnored(context, food.name)) {
                                return@forEach
                            }
                            
                            foodSpikeMap.getOrPut(food.name) { mutableListOf() }.add(
                                BloodSugarChange(
                                    date = entry.date,
                                    timeOfDay = timeOfDay,
                                    beforeValue = beforeBS,
                                    afterValue = afterBS,
                                    change = spike,
                                    foodName = food.name
                                )
                            )
                        }
                    }
                }
            }
        }
        
        // Create patterns for foods that consistently cause spikes
        foodSpikeMap.forEach { (foodName, spikes) ->
            if (spikes.size >= 2) { // At least 2 occurrences
                val avgSpike = spikes.map { it.change }.average().toInt()
                val occurrences = spikes.size
                
                val details = spikes.map { spike ->
                    PatternOccurrence(
                        date = spike.date,
                        timeOfDay = spike.timeOfDay,
                        context = "Ate $foodName",
                        markerApplied = "Blood Sugar Spike",
                        bloodSugar = "${spike.beforeValue} → ${spike.afterValue} (+${spike.change} mg/dL)",
                        notes = "Spike of ${spike.change} mg/dL"
                    )
                }
                
                val confidence = calculateSpikeConfidence(occurrences, avgSpike, daysToAnalyze)
                
                patterns.add(
                    DetectedPattern(
                        patternType = PatternType.BLOOD_SUGAR_SPIKE,
                        markerName = "Blood Sugar Spike",
                        correlatedItem = foodName,
                        occurrences = occurrences,
                        confidenceScore = confidence,
                        timeRange = "Last $daysToAnalyze days",
                        details = details,
                        suggestion = generateSpikeSuggestion(foodName, avgSpike, occurrences)
                    )
                )
            }
        }
        
        return patterns
    }
    
    /**
     * Helper data class for tracking blood sugar changes
     */
    private data class BloodSugarChange(
        val date: String,
        val timeOfDay: String,
        val beforeValue: Int,
        val afterValue: Int,
        val change: Int,
        val foodName: String
    )
    
    /**
     * Get blood sugar value from entry
     */
    private fun getBloodSugarValue(entry: DiabetesEntry, columnId: String): Int {
        val value = when (columnId) {
            "morningBloodSugarLevel" -> entry.morningBloodSugarLevel
            "afternoonBloodSugarLevel" -> entry.afternoonBloodSugarLevel
            "eveningBloodSugarLevel" -> entry.eveningBloodSugarLevel
            "nightBloodSugarLevel" -> entry.nightBloodSugarLevel
            else -> ""
        }
        return value.toIntOrNull() ?: 0
    }
    
    /**
     * Calculate confidence for blood sugar spike patterns
     */
    private fun calculateSpikeConfidence(occurrences: Int, avgSpike: Int, daysAnalyzed: Int): Float {
        // Base confidence on occurrences
        val occurrenceScore = when {
            occurrences >= 5 -> 0.9f
            occurrences >= 4 -> 0.8f
            occurrences >= 3 -> 0.7f
            occurrences >= 2 -> 0.6f
            else -> 0.5f
        }
        
        // Boost confidence for larger spikes
        val spikeBoost = when {
            avgSpike >= 60 -> 0.1f
            avgSpike >= 45 -> 0.05f
            else -> 0f
        }
        
        return (occurrenceScore + spikeBoost).coerceIn(0f, 1f)
    }
    
    /**
     * Generate suggestion for blood sugar spike patterns
     */
    private fun generateSpikeSuggestion(foodName: String, avgSpike: Int, occurrences: Int): String {
        return when {
            avgSpike >= 60 -> "⚠️ ALERT: $foodName causes large blood sugar spikes (avg +$avgSpike mg/dL, $occurrences times). Strongly consider avoiding this food."
            avgSpike >= 45 -> "⚠️ WARNING: $foodName causes significant blood sugar spikes (avg +$avgSpike mg/dL, $occurrences times). Consider limiting this food."
            else -> "ℹ️ NOTICE: $foodName causes moderate blood sugar spikes (avg +$avgSpike mg/dL, $occurrences times). Monitor your response to this food."
        }
    }
    
    /**
     * Calculate confidence score based on occurrences and time range
     */
    private fun calculateConfidence(occurrences: Int, daysAnalyzed: Int): Float {
        // Base confidence on frequency
        val frequency = occurrences.toFloat() / daysAnalyzed
        
        // More occurrences = higher confidence
        val occurrenceScore = when {
            occurrences >= 5 -> 0.9f
            occurrences >= 4 -> 0.8f
            occurrences >= 3 -> 0.7f
            occurrences >= 2 -> 0.6f
            else -> 0.5f
        }
        
        // Combine frequency and occurrence count
        return (frequency * 10 + occurrenceScore) / 2f
    }
    
    /**
     * Generate a helpful suggestion based on the pattern
     */
    private fun generateSuggestion(foodName: String, markerName: String, occurrences: Int, reactionType: String = "immediate"): String {
        val timingNote = when (reactionType) {
            "delayed" -> " (delayed reaction - symptoms appear later)"
            else -> ""
        }
        
        return when {
            occurrences >= 4 -> "⚠️ Strong pattern detected: You experienced '$markerName' after eating $foodName $occurrences times$timingNote. Consider avoiding this food."
            occurrences >= 3 -> "⚠️ Pattern detected: You experienced '$markerName' after eating $foodName $occurrences times$timingNote. You may want to limit this food."
            else -> "ℹ️ Possible pattern: You experienced '$markerName' after eating $foodName $occurrences times$timingNote. Keep monitoring."
        }
    }
    
    /**
     * Get marker name from marker object
     */
    private fun getMarkerName(marker: Marker, customMarkers: List<CustomMarkerType>): String {
        return when (marker.type) {
            "feel_bad" -> "Feel Bad"
            "custom" -> {
                customMarkers.firstOrNull { it.id == marker.customMarkerTypeId }?.name ?: "Unknown"
            }
            else -> "Unknown"
        }
    }
    
    /**
     * Get blood sugar value for a specific time of day
     */
    private fun getBloodSugarForTime(entry: DiabetesEntry, timeOfDay: String): String {
        return when (timeOfDay) {
            "morning" -> entry.morningBloodSugarLevel
            "afternoon" -> entry.afternoonBloodSugarLevel
            "evening" -> entry.eveningBloodSugarLevel
            "night" -> entry.nightBloodSugarLevel
            else -> ""
        }
    }
    
    /**
     * Parse date string to timestamp
     */
    private fun parseDate(dateString: String): Long {
        return try {
            val date = parseEntryDate(dateString) ?: return System.currentTimeMillis()
            date.toEpochDay() * 24 * 60 * 60 * 1000
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun parseEntryDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, dateFormatter)
        } catch (_: Exception) {
            try {
                LocalDate.parse(dateString, dottedDateFormatter)
            } catch (_: Exception) {
                null
            }
        }
    }
}

/**
 * Extension function to capitalize first letter
 */
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
