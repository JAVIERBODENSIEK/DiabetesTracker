package com.j4.diabetestracker

import android.content.Context
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class PatternInsight(
    val type: InsightType,
    val title: String,
    val description: String,
    val confidence: Float, // 0.0 to 1.0
    val occurrences: Int,
    val details: List<String> = emptyList()
)

enum class InsightType {
    FOOD_CORRELATION,
    BLOOD_SUGAR_PATTERN,
    TIME_PATTERN,
    FREQUENCY_INSIGHT,
    TRIGGER_WARNING
}

object PatternAnalyzer {
    
    fun analyzePatterns(
        entries: List<DiabetesEntry>,
        customMarkers: List<CustomMarkerType>,
        language: String
    ): List<PatternInsight> {
        val insights = mutableListOf<PatternInsight>()
        
        // Collect all marker instances
        val markerInstances = entries.flatMap { entry ->
            entry.markers.map { marker ->
                MarkerWithContext(
                    marker = marker,
                    entry = entry,
                    markerTypeName = when {
                        marker.type == "I feel bad" -> when (language) {
                            "German" -> "Ich fühle mich schlecht"
                            "Spanish" -> "Me siento mal"
                            else -> "I feel bad"
                        }
                        marker.customMarkerTypeId != null -> {
                            customMarkers.find { it.id == marker.customMarkerTypeId }?.name ?: marker.type
                        }
                        else -> marker.type
                    }
                )
            }
        }
        
        if (markerInstances.isEmpty()) {
            return emptyList()
        }
        
        // Analyze food correlations
        insights.addAll(analyzeFoodCorrelations(markerInstances, language))
        
        // Analyze blood sugar patterns
        insights.addAll(analyzeBloodSugarPatterns(markerInstances, language))
        
        // Analyze time-of-day patterns
        insights.addAll(analyzeTimePatterns(markerInstances, language))
        
        // Analyze frequency and common triggers
        insights.addAll(analyzeFrequencyPatterns(markerInstances, language))
        
        // Sort by confidence (highest first)
        return insights.sortedByDescending { it.confidence }
    }
    
    private fun analyzeFoodCorrelations(
        markerInstances: List<MarkerWithContext>,
        language: String
    ): List<PatternInsight> {
        val insights = mutableListOf<PatternInsight>()
        
        // Group markers by type
        val markersByType = markerInstances.groupBy { it.markerTypeName }
        
        for ((markerType, instances) in markersByType) {
            if (instances.size < 3) continue // Need at least 3 occurrences for pattern
            
            // Collect all foods eaten within 2 hours before each marker
            val foodsBeforeMarkers = mutableListOf<String>()
            
            for (instance in instances) {
                val cellFoods = instance.entry.foodEntriesByColumn[instance.marker.cellId] ?: emptyList()
                foodsBeforeMarkers.addAll(cellFoods.map { it.name })
            }
            
            // Count food occurrences
            val foodCounts = foodsBeforeMarkers.groupingBy { it }.eachCount()
            
            // Find foods that appear frequently (>40% of the time)
            for ((food, count) in foodCounts) {
                val percentage = count.toFloat() / instances.size
                if (percentage >= 0.4f) {
                    insights.add(
                        PatternInsight(
                            type = InsightType.FOOD_CORRELATION,
                            title = when (language) {
                                "German" -> "🍽️ Nahrungsmittel-Korrelation"
                                "Spanish" -> "🍽️ Correlación de Alimentos"
                                else -> "🍽️ Food Correlation"
                            },
                            description = when (language) {
                                "German" -> "'$food' erscheint in ${(percentage * 100).roundToInt()}% der Fälle vor '$markerType'"
                                "Spanish" -> "'$food' aparece en ${(percentage * 100).roundToInt()}% de los casos antes de '$markerType'"
                                else -> "'$food' appears in ${(percentage * 100).roundToInt()}% of cases before '$markerType'"
                            },
                            confidence = percentage,
                            occurrences = count,
                            details = listOf(
                                when (language) {
                                    "German" -> "$count von ${instances.size} Vorkommen"
                                    "Spanish" -> "$count de ${instances.size} ocurrencias"
                                    else -> "$count out of ${instances.size} occurrences"
                                }
                            )
                        )
                    )
                }
            }
        }
        
        return insights
    }
    
    private fun analyzeBloodSugarPatterns(
        markerInstances: List<MarkerWithContext>,
        language: String
    ): List<PatternInsight> {
        val insights = mutableListOf<PatternInsight>()
        
        // Group markers by type
        val markersByType = markerInstances.groupBy { it.markerTypeName }
        
        for ((markerType, instances) in markersByType) {
            if (instances.size < 3) continue
            
            // Collect blood sugar values when marker occurred
            val bloodSugarValues = instances.mapNotNull { instance ->
                val value = when (instance.marker.cellId) {
                    "morning" -> instance.entry.morningBloodSugarLevel
                    "afternoon" -> instance.entry.afternoonBloodSugarLevel
                    "evening" -> instance.entry.eveningBloodSugarLevel
                    "night" -> instance.entry.nightBloodSugarLevel
                    else -> ""
                }
                value.toIntOrNull()
            }
            
            if (bloodSugarValues.isEmpty()) continue
            
            val avgBloodSugar = bloodSugarValues.average().roundToInt()
            val highCount = bloodSugarValues.count { it > 180 }
            val lowCount = bloodSugarValues.count { it < 70 }
            val normalCount = bloodSugarValues.count { it in 70..180 }
            
            // High blood sugar pattern
            if (highCount.toFloat() / bloodSugarValues.size >= 0.6f) {
                insights.add(
                    PatternInsight(
                        type = InsightType.BLOOD_SUGAR_PATTERN,
                        title = when (language) {
                            "German" -> "📈 Hoher Blutzucker-Muster"
                            "Spanish" -> "📈 Patrón de Azúcar Alta"
                            else -> "📈 High Blood Sugar Pattern"
                        },
                        description = when (language) {
                            "German" -> "'$markerType' tritt häufig bei hohem Blutzucker auf (Ø $avgBloodSugar mg/dL)"
                            "Spanish" -> "'$markerType' ocurre frecuentemente con azúcar alta (Ø $avgBloodSugar mg/dL)"
                            else -> "'$markerType' frequently occurs with high blood sugar (avg $avgBloodSugar mg/dL)"
                        },
                        confidence = highCount.toFloat() / bloodSugarValues.size,
                        occurrences = highCount,
                        details = listOf(
                            when (language) {
                                "German" -> "$highCount von ${bloodSugarValues.size} Fällen über 180 mg/dL"
                                "Spanish" -> "$highCount de ${bloodSugarValues.size} casos por encima de 180 mg/dL"
                                else -> "$highCount out of ${bloodSugarValues.size} cases above 180 mg/dL"
                            }
                        )
                    )
                )
            }
            
            // Low blood sugar pattern
            if (lowCount.toFloat() / bloodSugarValues.size >= 0.6f) {
                insights.add(
                    PatternInsight(
                        type = InsightType.BLOOD_SUGAR_PATTERN,
                        title = when (language) {
                            "German" -> "📉 Niedriger Blutzucker-Muster"
                            "Spanish" -> "📉 Patrón de Azúcar Baja"
                            else -> "📉 Low Blood Sugar Pattern"
                        },
                        description = when (language) {
                            "German" -> "'$markerType' tritt häufig bei niedrigem Blutzucker auf (Ø $avgBloodSugar mg/dL)"
                            "Spanish" -> "'$markerType' ocurre frecuentemente con azúcar baja (Ø $avgBloodSugar mg/dL)"
                            else -> "'$markerType' frequently occurs with low blood sugar (avg $avgBloodSugar mg/dL)"
                        },
                        confidence = lowCount.toFloat() / bloodSugarValues.size,
                        occurrences = lowCount,
                        details = listOf(
                            when (language) {
                                "German" -> "$lowCount von ${bloodSugarValues.size} Fällen unter 70 mg/dL"
                                "Spanish" -> "$lowCount de ${bloodSugarValues.size} casos por debajo de 70 mg/dL"
                                else -> "$lowCount out of ${bloodSugarValues.size} cases below 70 mg/dL"
                            }
                        )
                    )
                )
            }
        }
        
        return insights
    }
    
    private fun analyzeTimePatterns(
        markerInstances: List<MarkerWithContext>,
        language: String
    ): List<PatternInsight> {
        val insights = mutableListOf<PatternInsight>()
        
        // Group markers by type
        val markersByType = markerInstances.groupBy { it.markerTypeName }
        
        for ((markerType, instances) in markersByType) {
            if (instances.size < 3) continue
            
            // Categorize by time of day
            val timeCategories = instances.groupBy { instance ->
                val time = try {
                    LocalTime.parse(instance.marker.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                } catch (e: Exception) {
                    null
                }
                
                when {
                    time == null -> "Unknown"
                    time.hour in 5..11 -> "Morning"
                    time.hour in 12..16 -> "Afternoon"
                    time.hour in 17..21 -> "Evening"
                    else -> "Night"
                }
            }
            
            // Find dominant time category (>50% of occurrences)
            for ((timeCategory, categoryInstances) in timeCategories) {
                val percentage = categoryInstances.size.toFloat() / instances.size
                if (percentage >= 0.5f && timeCategory != "Unknown") {
                    val timeName = when (language) {
                        "German" -> when (timeCategory) {
                            "Morning" -> "Morgen"
                            "Afternoon" -> "Nachmittag"
                            "Evening" -> "Abend"
                            "Night" -> "Nacht"
                            else -> timeCategory
                        }
                        "Spanish" -> when (timeCategory) {
                            "Morning" -> "Mañana"
                            "Afternoon" -> "Tarde"
                            "Evening" -> "Noche"
                            "Night" -> "Madrugada"
                            else -> timeCategory
                        }
                        else -> timeCategory
                    }
                    
                    insights.add(
                        PatternInsight(
                            type = InsightType.TIME_PATTERN,
                            title = when (language) {
                                "German" -> "⏰ Tageszeit-Muster"
                                "Spanish" -> "⏰ Patrón de Hora del Día"
                                else -> "⏰ Time-of-Day Pattern"
                            },
                            description = when (language) {
                                "German" -> "'$markerType' tritt hauptsächlich am $timeName auf (${(percentage * 100).roundToInt()}%)"
                                "Spanish" -> "'$markerType' ocurre principalmente por la $timeName (${(percentage * 100).roundToInt()}%)"
                                else -> "'$markerType' occurs primarily in the $timeName (${(percentage * 100).roundToInt()}%)"
                            },
                            confidence = percentage,
                            occurrences = categoryInstances.size,
                            details = listOf(
                                when (language) {
                                    "German" -> "${categoryInstances.size} von ${instances.size} Vorkommen"
                                    "Spanish" -> "${categoryInstances.size} de ${instances.size} ocurrencias"
                                    else -> "${categoryInstances.size} out of ${instances.size} occurrences"
                                }
                            )
                        )
                    )
                }
            }
        }
        
        return insights
    }
    
    private fun analyzeFrequencyPatterns(
        markerInstances: List<MarkerWithContext>,
        language: String
    ): List<PatternInsight> {
        val insights = mutableListOf<PatternInsight>()
        
        // Group by marker type and count
        val markerCounts = markerInstances.groupingBy { it.markerTypeName }.eachCount()
        
        // Find most frequent marker
        val mostFrequent = markerCounts.maxByOrNull { it.value }
        if (mostFrequent != null && mostFrequent.value >= 5) {
            insights.add(
                PatternInsight(
                    type = InsightType.FREQUENCY_INSIGHT,
                    title = when (language) {
                        "German" -> "🔄 Häufigster Marker"
                        "Spanish" -> "🔄 Marcador Más Frecuente"
                        else -> "🔄 Most Frequent Marker"
                    },
                    description = when (language) {
                        "German" -> "'${mostFrequent.key}' ist Ihr häufigster Marker mit ${mostFrequent.value} Vorkommen"
                        "Spanish" -> "'${mostFrequent.key}' es su marcador más frecuente con ${mostFrequent.value} ocurrencias"
                        else -> "'${mostFrequent.key}' is your most frequent marker with ${mostFrequent.value} occurrences"
                    },
                    confidence = 1.0f,
                    occurrences = mostFrequent.value
                )
            )
        }
        
        // Analyze common reasons across all markers
        val allReasons = markerInstances.flatMap { it.marker.reasons }
        val reasonCounts = allReasons.groupingBy { it }.eachCount()
        val topReason = reasonCounts.maxByOrNull { it.value }
        
        if (topReason != null && topReason.value >= 3) {
            insights.add(
                PatternInsight(
                    type = InsightType.TRIGGER_WARNING,
                    title = when (language) {
                        "German" -> "⚠️ Häufigster Auslöser"
                        "Spanish" -> "⚠️ Desencadenante Más Común"
                        else -> "⚠️ Most Common Trigger"
                    },
                    description = when (language) {
                        "German" -> "'${topReason.key}' ist Ihr häufigster Auslöser (${topReason.value} mal)"
                        "Spanish" -> "'${topReason.key}' es su desencadenante más común (${topReason.value} veces)"
                        else -> "'${topReason.key}' is your most common trigger (${topReason.value} times)"
                    },
                    confidence = 0.8f,
                    occurrences = topReason.value
                )
            )
        }
        
        return insights
    }
    
    private data class MarkerWithContext(
        val marker: Marker,
        val entry: DiabetesEntry,
        val markerTypeName: String
    )
}
