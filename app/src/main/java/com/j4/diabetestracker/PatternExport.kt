package com.j4.diabetestracker

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Export pattern analysis to TXT file
 */
fun exportPatternAnalysisToFile(
    context: Context,
    patterns: List<DetectedPattern>,
    fileUri: Uri
) {
    try {
        context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                // Header
                writer.write("═══════════════════════════════════════════════════\n")
                writer.write("        DIABETES PATTERN ANALYSIS REPORT\n")
                writer.write("═══════════════════════════════════════════════════\n\n")
                
                val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                writer.write("Generated: ${dateFormat.format(Date())}\n")
                writer.write("Total Patterns Detected: ${patterns.size}\n\n")
                
                // Summary statistics
                writer.write("───────────────────────────────────────────────────\n")
                writer.write("SUMMARY\n")
                writer.write("───────────────────────────────────────────────────\n\n")
                
                val highRisk = patterns.count { it.confidenceScore >= 0.8f }
                val mediumRisk = patterns.count { it.confidenceScore >= 0.6f && it.confidenceScore < 0.8f }
                val lowRisk = patterns.count { it.confidenceScore < 0.6f }
                
                writer.write("Risk Distribution:\n")
                writer.write("  • High Risk (≥80%):    $highRisk pattern(s)\n")
                writer.write("  • Medium Risk (60-79%): $mediumRisk pattern(s)\n")
                writer.write("  • Low Risk (<60%):      $lowRisk pattern(s)\n\n")
                
                val markerPatterns = patterns.count { it.patternType == PatternType.FOOD_MARKER_CORRELATION }
                val spikePatterns = patterns.count { it.patternType == PatternType.BLOOD_SUGAR_SPIKE }
                val timePatterns = patterns.count { it.patternType == PatternType.TIME_MARKER_CORRELATION }
                
                writer.write("Pattern Types:\n")
                if (markerPatterns > 0) writer.write("  • Food-Marker Correlations: $markerPatterns\n")
                if (spikePatterns > 0) writer.write("  • Blood Sugar Spikes:       $spikePatterns\n")
                if (timePatterns > 0) writer.write("  • Time-Based Patterns:      $timePatterns\n")
                writer.write("\n")
                
                // Top risky foods
                val topRiskyFoods = patterns.sortedByDescending { it.confidenceScore }.take(5)
                if (topRiskyFoods.isNotEmpty()) {
                    writer.write("Top Risky Foods:\n")
                    topRiskyFoods.forEachIndexed { index, pattern ->
                        writer.write("  ${index + 1}. ${pattern.correlatedItem} - ${(pattern.confidenceScore * 100).toInt()}% confidence\n")
                        writer.write("     → ${pattern.markerName} (${pattern.occurrences} times)\n")
                    }
                    writer.write("\n")
                }
                
                // Detailed patterns
                writer.write("═══════════════════════════════════════════════════\n")
                writer.write("DETAILED PATTERN ANALYSIS\n")
                writer.write("═══════════════════════════════════════════════════\n\n")
                
                patterns.sortedByDescending { it.confidenceScore }.forEachIndexed { index, pattern ->
                    writer.write("───────────────────────────────────────────────────\n")
                    writer.write("PATTERN #${index + 1}\n")
                    writer.write("───────────────────────────────────────────────────\n\n")
                    
                    // Pattern type
                    val typeLabel = when (pattern.patternType) {
                        PatternType.FOOD_MARKER_CORRELATION -> "Food-Marker Correlation"
                        PatternType.BLOOD_SUGAR_SPIKE -> "Blood Sugar Spike"
                        PatternType.TIME_MARKER_CORRELATION -> "Time-Based Pattern"
                        else -> "Other Pattern"
                    }
                    writer.write("Type: $typeLabel\n")
                    
                    // Main info
                    writer.write("Food/Item: ${pattern.correlatedItem}\n")
                    writer.write("Effect: ${pattern.markerName}\n")
                    writer.write("Occurrences: ${pattern.occurrences}\n")
                    writer.write("Confidence: ${(pattern.confidenceScore * 100).toInt()}%\n")
                    writer.write("Time Range: ${pattern.timeRange}\n\n")
                    
                    // Risk level
                    val riskLevel = when {
                        pattern.confidenceScore >= 0.8f -> "HIGH RISK ⚠️"
                        pattern.confidenceScore >= 0.6f -> "MEDIUM RISK ⚠"
                        else -> "LOW RISK"
                    }
                    writer.write("Risk Level: $riskLevel\n\n")
                    
                    // Suggestion
                    writer.write("Suggestion:\n")
                    writer.write("${pattern.suggestion}\n\n")
                    
                    // Occurrence details
                    if (pattern.details.isNotEmpty()) {
                        writer.write("Occurrence Details:\n")
                        pattern.details.forEachIndexed { detailIndex, occurrence ->
                            writer.write("  ${detailIndex + 1}. ${occurrence.date} (${occurrence.timeOfDay})\n")
                            writer.write("     Context: ${occurrence.context}\n")
                            if (occurrence.bloodSugar.isNotBlank()) {
                                writer.write("     Blood Sugar: ${occurrence.bloodSugar}\n")
                            }
                            if (occurrence.notes.isNotBlank()) {
                                writer.write("     Notes: ${occurrence.notes}\n")
                            }
                        }
                        writer.write("\n")
                    }
                }
                
                // Footer
                writer.write("═══════════════════════════════════════════════════\n")
                writer.write("END OF REPORT\n")
                writer.write("═══════════════════════════════════════════════════\n\n")
                
                writer.write("DISCLAIMER:\n")
                writer.write("This analysis is based on your tracked data and is\n")
                writer.write("intended for informational purposes only. Please\n")
                writer.write("consult with your healthcare provider before making\n")
                writer.write("any changes to your diet or treatment plan.\n")
                
                writer.flush()
            }
        }
        Toast.makeText(context, context.getString(R.string.pattern_analysis_exported_txt), Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, context.getString(R.string.export_failed), Toast.LENGTH_SHORT).show()
    }
}
