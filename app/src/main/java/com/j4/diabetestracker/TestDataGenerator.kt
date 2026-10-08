package com.j4.diabetestracker

import android.content.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Generate test data with pre-configured patterns for demonstration
 */
object TestDataGenerator {

    private val testDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    
    /**
     * Generate test entries with clear patterns for demonstration
     */
    fun generateTestEntriesWithPatterns(): List<DiabetesEntry> {
        val entries = mutableListOf<DiabetesEntry>()
        val today = LocalDate.now()
        
        // Pattern 1: Pizza → Feel Bad (5 times over 20 days)
        // This should create a HIGH confidence pattern (≥80%)
        val pizzaDates = listOf(-18, -15, -12, -8, -3)
        pizzaDates.forEach { daysAgo ->
            val date = today.plusDays(daysAgo.toLong()).format(testDateFormatter)
            entries.add(
                DiabetesEntry(
                    id = UUID.randomUUID().toString(),
                    date = date,
                    morningBloodSugarLevel = "110",
                    afternoonBloodSugarLevel = "145",
                    eveningBloodSugarLevel = "120",
                    nightBloodSugarLevel = "105",
                    morningInsulin = "10",
                    afternoonInsulin = "8",
                    eveningInsulin = "12",
                    remarks = "Had pizza for lunch",
                    foodEntriesByColumn = mapOf(
                        "afternoonBloodSugarLevel" to listOf(
                            FoodPreset(name = "Pizza", emoji = "🍕")
                        )
                    ),
                    markers = listOf(
                        Marker(
                            id = UUID.randomUUID().toString(),
                            type = "feel_bad",
                            startTime = "afternoon",
                            reasons = listOf("nausea", "headache"),
                            customNote = "Felt bad after lunch",
                            timestamp = System.currentTimeMillis()
                        )
                    ),
                    isTestData = true
                )
            )
        }
        
        // Pattern 2: Pasta → Blood Sugar Spike (4 times over 15 days)
        // This should create a MEDIUM-HIGH confidence pattern
        val pastaDates = listOf(-14, -10, -6, -2)
        pastaDates.forEach { daysAgo ->
            val date = today.plusDays(daysAgo.toLong()).format(testDateFormatter)
            entries.add(
                DiabetesEntry(
                    id = UUID.randomUUID().toString(),
                    date = date,
                    morningBloodSugarLevel = "105",
                    afternoonBloodSugarLevel = "180", // High spike
                    eveningBloodSugarLevel = "125",
                    nightBloodSugarLevel = "110",
                    morningInsulin = "10",
                    afternoonInsulin = "10",
                    eveningInsulin = "12",
                    remarks = "Pasta for lunch",
                    foodEntriesByColumn = mapOf(
                        "morningBloodSugarLevel" to listOf(
                            FoodPreset(name = "Pasta", emoji = "🍝")
                        )
                    ),
                    markers = emptyList(),
                    isTestData = true
                )
            )
        }
        
        // Pattern 3: Banana → Fatigue (3 times over 12 days)
        // This should create a MEDIUM confidence pattern (≥60%)
        val bananaDates = listOf(-11, -7, -4)
        bananaDates.forEach { daysAgo ->
            val date = today.plusDays(daysAgo.toLong()).format(testDateFormatter)
            entries.add(
                DiabetesEntry(
                    id = UUID.randomUUID().toString(),
                    date = date,
                    morningBloodSugarLevel = "100",
                    afternoonBloodSugarLevel = "115",
                    eveningBloodSugarLevel = "110",
                    nightBloodSugarLevel = "105",
                    morningInsulin = "10",
                    afternoonInsulin = "8",
                    eveningInsulin = "10",
                    remarks = "Banana for breakfast",
                    foodEntriesByColumn = mapOf(
                        "morningBloodSugarLevel" to listOf(
                            FoodPreset(name = "Banana", emoji = "🍌")
                        )
                    ),
                    markers = listOf(
                        Marker(
                            id = UUID.randomUUID().toString(),
                            type = "feel_bad",
                            startTime = "morning",
                            reasons = listOf("low_energy"),
                            customNote = "Feeling tired",
                            timestamp = System.currentTimeMillis()
                        )
                    ),
                    isTestData = true
                )
            )
        }
        
        // Pattern 4: Chocolate → Headache (4 times over 16 days)
        val chocolateDates = listOf(-16, -13, -9, -5)
        chocolateDates.forEach { daysAgo ->
            val date = today.plusDays(daysAgo.toLong()).format(testDateFormatter)
            entries.add(
                DiabetesEntry(
                    id = UUID.randomUUID().toString(),
                    date = date,
                    morningBloodSugarLevel = "108",
                    afternoonBloodSugarLevel = "130",
                    eveningBloodSugarLevel = "165",
                    nightBloodSugarLevel = "120",
                    morningInsulin = "10",
                    afternoonInsulin = "8",
                    eveningInsulin = "12",
                    remarks = "Chocolate dessert",
                    foodEntriesByColumn = mapOf(
                        "eveningBloodSugarLevel" to listOf(
                            FoodPreset(name = "Chocolate", emoji = "🍫")
                        )
                    ),
                    markers = listOf(
                        Marker(
                            id = UUID.randomUUID().toString(),
                            type = "feel_bad",
                            startTime = "evening",
                            reasons = listOf("headache"),
                            customNote = "Headache after dinner",
                            timestamp = System.currentTimeMillis()
                        )
                    ),
                    isTestData = true
                )
            )
        }
        
        // Pattern 5: Bread → Blood Sugar Spike (3 times)
        val breadDates = listOf(-17, -12, -6)
        breadDates.forEach { daysAgo ->
            val date = today.plusDays(daysAgo.toLong()).format(testDateFormatter)
            entries.add(
                DiabetesEntry(
                    id = UUID.randomUUID().toString(),
                    date = date,
                    morningBloodSugarLevel = "95",
                    afternoonBloodSugarLevel = "155", // Spike
                    eveningBloodSugarLevel = "120",
                    nightBloodSugarLevel = "105",
                    morningInsulin = "10",
                    afternoonInsulin = "8",
                    eveningInsulin = "10",
                    remarks = "Bread for breakfast",
                    foodEntriesByColumn = mapOf(
                        "morningBloodSugarLevel" to listOf(
                            FoodPreset(name = "Bread", emoji = "🍞")
                        )
                    ),
                    markers = emptyList(),
                    isTestData = true
                )
            )
        }
        
        // Pattern 6: Coffee → Headache (DELAYED - morning coffee, afternoon headache)
        val coffeeDates = listOf(-16, -11, -7, -3)
        coffeeDates.forEach { daysAgo ->
            val date = today.plusDays(daysAgo.toLong()).format(testDateFormatter)
            entries.add(
                DiabetesEntry(
                    id = UUID.randomUUID().toString(),
                    date = date,
                    morningBloodSugarLevel = "100",
                    afternoonBloodSugarLevel = "120",
                    eveningBloodSugarLevel = "115",
                    nightBloodSugarLevel = "105",
                    morningInsulin = "10",
                    afternoonInsulin = "8",
                    eveningInsulin = "10",
                    remarks = "Coffee in morning, headache later",
                    foodEntriesByColumn = mapOf(
                        "morningBloodSugarLevel" to listOf(
                            FoodPreset(name = "Coffee", emoji = "☕")
                        )
                    ),
                    markers = listOf(
                        Marker(
                            id = UUID.randomUUID().toString(),
                            type = "feel_bad",
                            startTime = "afternoon", // Delayed reaction!
                            reasons = listOf("headache"),
                            customNote = "Headache appeared later",
                            timestamp = System.currentTimeMillis()
                        )
                    ),
                    isTestData = true
                )
            )
        }
        
        // Add some normal days without patterns (filler data)
        val normalDates = listOf(-19, -13, -10, -7, -4, -1)
        normalDates.forEach { daysAgo ->
            val date = today.plusDays(daysAgo.toLong()).format(testDateFormatter)
            entries.add(
                DiabetesEntry(
                    id = UUID.randomUUID().toString(),
                    date = date,
                    morningBloodSugarLevel = "105",
                    afternoonBloodSugarLevel = "115",
                    eveningBloodSugarLevel = "110",
                    nightBloodSugarLevel = "100",
                    morningInsulin = "10",
                    afternoonInsulin = "8",
                    eveningInsulin = "10",
                    remarks = "Normal day",
                    foodEntriesByColumn = mapOf(
                        "morningBloodSugarLevel" to listOf(
                            FoodPreset(name = "Salad", emoji = "🥗")
                        ),
                        "eveningBloodSugarLevel" to listOf(
                            FoodPreset(name = "Chicken", emoji = "🍗")
                        )
                    ),
                    markers = emptyList(),
                    isTestData = true
                )
            )
        }
        
        return entries.sortedBy { it.date }
    }

    private fun generatePotentialFoodValidationEntries(): List<DiabetesEntry> {
        val today = LocalDate.now()

        return listOf(
            createPotentialFoodTestEntry(
                date = today.minusDays(24),
                food = FoodPreset(name = "Donut", emoji = "🍩"),
                hasFeelBad = true,
                note = "Donut support day 1"
            ),
            createPotentialFoodTestEntry(
                date = today.minusDays(22),
                food = FoodPreset(name = "Donut", emoji = "🍩"),
                hasFeelBad = true,
                note = "Donut support day 2"
            ),
            createPotentialFoodTestEntry(
                date = today.minusDays(21),
                food = null,
                hasFeelBad = true,
                note = "Felt bad without donut (contradiction day)"
            ),
            createPotentialFoodTestEntry(
                date = today.minusDays(1),
                food = FoodPreset(name = "Blueberries", emoji = "🫐"),
                hasFeelBad = true,
                note = "Blueberries support day 1"
            ),
            createPotentialFoodTestEntry(
                date = today,
                food = FoodPreset(name = "Blueberries", emoji = "🫐"),
                hasFeelBad = true,
                note = "Blueberries support day 2"
            ),
        )
    }

    private fun createPotentialFoodTestEntry(
        date: LocalDate,
        food: FoodPreset?,
        hasFeelBad: Boolean,
        note: String,
    ): DiabetesEntry {
        val foodEntries = if (food == null) {
            emptyMap()
        } else {
            mapOf("afternoonBloodSugarLevel" to listOf(food))
        }

        val markers = if (hasFeelBad) {
            listOf(
                Marker(
                    id = UUID.randomUUID().toString(),
                    type = "feel_bad",
                    startTime = "afternoon",
                    reasons = listOf("nausea"),
                    customNote = note,
                    timestamp = System.currentTimeMillis(),
                ),
            )
        } else {
            emptyList()
        }

        return DiabetesEntry(
            id = UUID.randomUUID().toString(),
            date = date.format(testDateFormatter),
            morningBloodSugarLevel = "104",
            afternoonBloodSugarLevel = "132",
            eveningBloodSugarLevel = "116",
            nightBloodSugarLevel = "102",
            morningInsulin = "10",
            afternoonInsulin = "8",
            eveningInsulin = "10",
            remarks = note,
            foodEntriesByColumn = foodEntries,
            markers = markers,
            isTestData = true,
        )
    }
    
    /**
     * Load test data into the app
     */
    fun loadTestData(context: Context) {
        val testEntries = generateTestEntriesWithPatterns() +
            generatePotentialFoodValidationEntries()
        
        // Get existing entries and merge with test data
        val existingEntries = DataManager.getEntries(context)
        val allEntries = existingEntries + testEntries
        
        // Save combined entries
        DataManager.saveEntries(context, allEntries)
        
        // Trigger pattern detection on all entries
        val customMarkers = CustomMarkerManager.getCustomMarkers(context)
        val patterns = PatternDetectionEngine.detectPatterns(
            entries = allEntries,
            customMarkers = customMarkers,
            daysToAnalyze = 30,
            context = context
        )
        
        // Save detected patterns
        PatternStorageManager.savePatterns(context, patterns)
    }
    
    /**
     * Remove test data from the app
     */
    fun removeTestData(context: Context) {
        val allEntries = DataManager.getEntries(context)
        
        // Filter out entries that have isTestData flag set to true
        val realEntries = allEntries.filter { entry ->
            entry.isTestData != true
        }
        
        // Save filtered entries
        DataManager.saveEntries(context, realEntries)
        
        // Re-run pattern detection on remaining entries
        val customMarkers = CustomMarkerManager.getCustomMarkers(context)
        val patterns = PatternDetectionEngine.detectPatterns(
            entries = realEntries,
            customMarkers = customMarkers,
            daysToAnalyze = 30,
            context = context
        )
        
        // Save updated patterns
        PatternStorageManager.savePatterns(context, patterns)
    }
    
    /**
     * Diagnose why patterns might not be detected
     */
    fun diagnosePatternDetection(
        entries: List<DiabetesEntry>,
        customMarkers: List<CustomMarkerType>
    ): String {
        val report = StringBuilder()
        report.appendLine("=== PATTERN DETECTION DIAGNOSTIC ===\n")
        
        // Check total entries
        report.appendLine("Total entries: ${entries.size}")
        if (entries.isEmpty()) {
            report.appendLine("❌ No entries found! Add some diary entries first.\n")
            return report.toString()
        }
        
        // Check entries with foods
        val entriesWithFoods = entries.filter { 
            it.foodEntriesByColumn.values.any { foods -> foods.isNotEmpty() }
        }
        report.appendLine("Entries with foods: ${entriesWithFoods.size}")
        
        // Check entries with markers
        val entriesWithMarkers = entries.filter { it.markers.isNotEmpty() }
        report.appendLine("Entries with markers: ${entriesWithMarkers.size}\n")
        
        if (entriesWithFoods.isEmpty()) {
            report.appendLine("❌ No foods found! Add foods to your entries.\n")
        }
        
        if (entriesWithMarkers.isEmpty()) {
            report.appendLine("❌ No markers found! Add 'Feel Bad' markers to your entries.\n")
        }
        
        // Analyze food-marker combinations
        report.appendLine("--- Food-Marker Analysis ---")
        val foodMarkerCombos = mutableMapOf<Pair<String, String>, MutableList<String>>()
        
        entries.forEach { entry ->
            if (entry.markers.isEmpty()) return@forEach
            
            listOf("morning", "afternoon", "evening", "night").forEach { timeOfDay ->
                val foodsForTime = entry.foodEntriesByColumn
                    .filter { (columnId, _) -> columnId.contains(timeOfDay, ignoreCase = true) }
                    .flatMap { (_, foods) -> foods }
                
                val markersForTime = entry.markers.filter { it.startTime == timeOfDay }
                
                if (foodsForTime.isNotEmpty() && markersForTime.isNotEmpty()) {
                    foodsForTime.forEach { food ->
                        markersForTime.forEach { marker ->
                            val markerName = if (marker.type == "feel_bad") {
                                "Feel Bad (${marker.reasons.joinToString(", ")})"
                            } else {
                                customMarkers.find { it.id == marker.customMarkerTypeId }?.name ?: "Unknown"
                            }
                            val key = Pair(food.name, markerName)
                            foodMarkerCombos.getOrPut(key) { mutableListOf() }.add(entry.date)
                        }
                    }
                }
            }
        }
        
        if (foodMarkerCombos.isEmpty()) {
            report.appendLine("❌ No food-marker combinations found!")
            report.appendLine("\nPossible reasons:")
            report.appendLine("1. Foods and markers are in different time periods")
            report.appendLine("   - Food in 'morning' column but marker is 'afternoon'")
            report.appendLine("2. Markers don't have a specific time (might be 'all_day' or 'cell_specific')")
            report.appendLine("3. Foods are in wrong columns (e.g., not in blood sugar columns)\n")
        } else {
            report.appendLine("Found ${foodMarkerCombos.size} food-marker combination(s):\n")
            foodMarkerCombos.forEach { (combo, dates) ->
                val (food, marker) = combo
                report.appendLine("• $food → $marker")
                report.appendLine("  Occurrences: ${dates.size}")
                report.appendLine("  Dates: ${dates.joinToString(", ")}")
                if (dates.size >= 2) {
                    report.appendLine("  ✅ Should create a pattern (≥2 occurrences)")
                } else {
                    report.appendLine("  ❌ Not enough occurrences (need ≥2)")
                }
                report.appendLine()
            }
        }
        
        // Check for blood sugar spikes
        report.appendLine("--- Blood Sugar Spike Analysis ---")
        var spikeCount = 0
        entries.forEach { entry ->
            val timePeriods = listOf(
                Triple("morningBloodSugarLevel", "afternoonBloodSugarLevel", "morning"),
                Triple("afternoonBloodSugarLevel", "eveningBloodSugarLevel", "afternoon"),
                Triple("eveningBloodSugarLevel", "nightBloodSugarLevel", "evening")
            )
            
            timePeriods.forEach { (beforeCol, afterCol, time) ->
                val before = when (beforeCol) {
                    "morningBloodSugarLevel" -> entry.morningBloodSugarLevel
                    "afternoonBloodSugarLevel" -> entry.afternoonBloodSugarLevel
                    "eveningBloodSugarLevel" -> entry.eveningBloodSugarLevel
                    else -> ""
                }.toIntOrNull() ?: 0
                
                val after = when (afterCol) {
                    "afternoonBloodSugarLevel" -> entry.afternoonBloodSugarLevel
                    "eveningBloodSugarLevel" -> entry.eveningBloodSugarLevel
                    "nightBloodSugarLevel" -> entry.nightBloodSugarLevel
                    else -> ""
                }.toIntOrNull() ?: 0
                
                if (before > 0 && after > 0) {
                    val spike = after - before
                    if (spike >= 30) {
                        spikeCount++
                        val foods = entry.foodEntriesByColumn[beforeCol] ?: emptyList()
                        if (foods.isNotEmpty()) {
                            report.appendLine("${entry.date} ($time): ${foods.joinToString { it.name }} → +$spike mg/dL")
                        }
                    }
                }
            }
        }
        
        if (spikeCount == 0) {
            report.appendLine("❌ No blood sugar spikes detected (≥30 mg/dL increase)")
        } else {
            report.appendLine("\nTotal spikes: $spikeCount")
        }
        
        return report.toString()
    }
    
    /**
     * Get summary of what patterns should be detected
     */
    fun getExpectedPatternsSummary(): String {
        return """
            Expected Patterns from Test Data:
            
            1. 🍕 Pizza → Feel Bad (IMMEDIATE)
               - Occurrences: 5 times
               - Confidence: HIGH (≥80%)
               - Time: Afternoon
               - Symptoms: Nausea, Headache
            
            2. 🍝 Pasta → Blood Sugar Spike
               - Occurrences: 4 times
               - Avg Spike: +75 mg/dL
               - Confidence: HIGH (≥80%)
               - Time: Morning to Afternoon
            
            3. 🍌 Banana → Fatigue (IMMEDIATE)
               - Occurrences: 3 times
               - Confidence: MEDIUM (≥60%)
               - Time: Morning
               - Symptoms: Low Energy
            
            4. 🍫 Chocolate → Headache (IMMEDIATE)
               - Occurrences: 4 times
               - Confidence: HIGH (≥80%)
               - Time: Evening
               - Symptoms: Headache
            
            5. 🍞 Bread → Blood Sugar Spike
               - Occurrences: 3 times
               - Avg Spike: +60 mg/dL
               - Confidence: MEDIUM (≥60%)
               - Time: Morning to Afternoon
            
            6. ☕ Coffee → Headache (DELAYED) ⭐ NEW!
               - Occurrences: 4 times
               - Confidence: MEDIUM-HIGH (≥65%)
               - Time: Morning → Afternoon
               - Symptoms: Headache (appears later)
               - Note: Demonstrates delayed reaction detection!
        """.trimIndent()
    }
}
