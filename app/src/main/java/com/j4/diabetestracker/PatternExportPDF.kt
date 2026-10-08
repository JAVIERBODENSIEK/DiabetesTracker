package com.j4.diabetestracker

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

/**
 * Export pattern analysis to PDF file
 */
fun exportPatternAnalysisToPDF(
    context: Context,
    patterns: List<DetectedPattern>,
    fileUri: Uri,
    language: String = "English"
) {
    try {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 portrait
        val pageHeight = 842
        val margin = 40f
        val contentWidth = pageWidth - (2 * margin)
        
        var pageNumber = 1
        var yPosition = margin
        
        // Create first page
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        
        // Paint objects
        val titlePaint = Paint().apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        val headerPaint = Paint().apply {
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        val subheaderPaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        val bodyPaint = Paint().apply {
            textSize = 11f
            isAntiAlias = true
        }
        
        val smallPaint = Paint().apply {
            textSize = 9f
            isAntiAlias = true
        }
        
        val linePaint = Paint().apply {
            strokeWidth = 1f
        }
        
        // Helper function to check if we need a new page
        fun checkNewPage(requiredSpace: Float): Boolean {
            if (yPosition + requiredSpace > pageHeight - margin) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = margin
                return true
            }
            return false
        }
        
        // Title
        canvas.drawText(context.getString(R.string.pdf_report_title), margin, yPosition, titlePaint)
        yPosition += 30f
        
        // Date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        canvas.drawText("${context.getString(R.string.pdf_generated)} ${dateFormat.format(Date())}", margin, yPosition, bodyPaint)
        yPosition += 20f
        canvas.drawText("${context.getString(R.string.pdf_total_patterns)} ${patterns.size}", margin, yPosition, bodyPaint)
        yPosition += 30f
        
        // Line separator
        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint)
        yPosition += 20f
        
        // SUMMARY SECTION
        checkNewPage(150f)
        canvas.drawText(context.getString(R.string.pdf_summary), margin, yPosition, headerPaint)
        yPosition += 25f
        
        // Risk distribution
        val highRisk = patterns.count { it.confidenceScore >= 0.8f }
        val mediumRisk = patterns.count { it.confidenceScore >= 0.6f && it.confidenceScore < 0.8f }
        val lowRisk = patterns.count { it.confidenceScore < 0.6f }
        
        canvas.drawText(context.getString(R.string.pdf_risk_distribution), margin, yPosition, subheaderPaint)
        yPosition += 20f
        canvas.drawText("• ${context.getString(R.string.pdf_high_risk)} $highRisk ${context.getString(R.string.pdf_pattern_s)}", margin + 20f, yPosition, bodyPaint)
        yPosition += 18f
        canvas.drawText("• ${context.getString(R.string.pdf_medium_risk)} $mediumRisk ${context.getString(R.string.pdf_pattern_s)}", margin + 20f, yPosition, bodyPaint)
        yPosition += 18f
        canvas.drawText("• ${context.getString(R.string.pdf_low_risk)} $lowRisk ${context.getString(R.string.pdf_pattern_s)}", margin + 20f, yPosition, bodyPaint)
        yPosition += 25f
        
        // Pattern types
        val markerPatterns = patterns.count { it.patternType == PatternType.FOOD_MARKER_CORRELATION }
        val spikePatterns = patterns.count { it.patternType == PatternType.BLOOD_SUGAR_SPIKE }
        val timePatterns = patterns.count { it.patternType == PatternType.TIME_MARKER_CORRELATION }
        
        canvas.drawText(context.getString(R.string.pdf_pattern_types), margin, yPosition, subheaderPaint)
        yPosition += 20f
        if (markerPatterns > 0) {
            canvas.drawText("• ${context.getString(R.string.pdf_food_marker_correlations)} $markerPatterns", margin + 20f, yPosition, bodyPaint)
            yPosition += 18f
        }
        if (spikePatterns > 0) {
            canvas.drawText("• ${context.getString(R.string.pdf_blood_sugar_spikes)} $spikePatterns", margin + 20f, yPosition, bodyPaint)
            yPosition += 18f
        }
        if (timePatterns > 0) {
            canvas.drawText("• ${context.getString(R.string.pdf_time_based_patterns)} $timePatterns", margin + 20f, yPosition, bodyPaint)
            yPosition += 18f
        }
        yPosition += 15f
        
        // Top risky foods
        val topRiskyFoods = patterns.sortedByDescending { it.confidenceScore }.take(5)
        if (topRiskyFoods.isNotEmpty()) {
            checkNewPage(150f)
            canvas.drawText(context.getString(R.string.pdf_top_risky_foods), margin, yPosition, subheaderPaint)
            yPosition += 20f
            topRiskyFoods.forEachIndexed { index, pattern ->
                checkNewPage(40f)
                canvas.drawText("${index + 1}. ${pattern.correlatedItem} - ${(pattern.confidenceScore * 100).toInt()}% ${context.getString(R.string.pdf_confidence)}", 
                    margin + 20f, yPosition, bodyPaint)
                yPosition += 18f
                canvas.drawText("   → ${pattern.markerName} (${pattern.occurrences} ${context.getString(R.string.times)})", 
                    margin + 20f, yPosition, smallPaint)
                yPosition += 18f
            }
            yPosition += 15f
        }
        
        // Line separator
        checkNewPage(30f)
        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint)
        yPosition += 20f
        
        // DETAILED PATTERNS
        checkNewPage(50f)
        canvas.drawText(context.getString(R.string.pdf_detailed_analysis), margin, yPosition, headerPaint)
        yPosition += 30f
        
        patterns.sortedByDescending { it.confidenceScore }.forEachIndexed { index, pattern ->
            // Check if we need a new page for this pattern
            checkNewPage(200f)
            
            // Pattern number
            canvas.drawText(context.getString(R.string.pdf_pattern_number, index + 1), margin, yPosition, subheaderPaint)
            yPosition += 25f
            
            // Pattern type
            val typeLabel = when (pattern.patternType) {
                PatternType.FOOD_MARKER_CORRELATION -> context.getString(R.string.food_marker_correlations)
                PatternType.BLOOD_SUGAR_SPIKE -> context.getString(R.string.blood_sugar_spikes)
                PatternType.TIME_MARKER_CORRELATION -> context.getString(R.string.time_based_patterns)
                else -> context.getString(R.string.food_marker_correlations)
            }
            canvas.drawText("${context.getString(R.string.pdf_type)} $typeLabel", margin + 10f, yPosition, bodyPaint)
            yPosition += 18f
            
            // Main info
            canvas.drawText("${context.getString(R.string.pdf_food_item)} ${pattern.correlatedItem}", margin + 10f, yPosition, bodyPaint)
            yPosition += 18f
            canvas.drawText("${context.getString(R.string.pdf_effect)} ${pattern.markerName}", margin + 10f, yPosition, bodyPaint)
            yPosition += 18f
            canvas.drawText("${context.getString(R.string.pdf_occurrences)} ${pattern.occurrences}", margin + 10f, yPosition, bodyPaint)
            yPosition += 18f
            canvas.drawText("${context.getString(R.string.pdf_confidence_label)} ${(pattern.confidenceScore * 100).toInt()}%", margin + 10f, yPosition, bodyPaint)
            yPosition += 18f
            canvas.drawText("${context.getString(R.string.pdf_time_range)} ${pattern.timeRange}", margin + 10f, yPosition, bodyPaint)
            yPosition += 20f
            
            // Risk level
            val riskLevel = when {
                pattern.confidenceScore >= 0.8f -> context.getString(R.string.pdf_high_risk_label)
                pattern.confidenceScore >= 0.6f -> context.getString(R.string.pdf_medium_risk_label)
                else -> context.getString(R.string.pdf_low_risk_label)
            }
            canvas.drawText("${context.getString(R.string.pdf_risk_level)} $riskLevel", margin + 10f, yPosition, bodyPaint)
            yPosition += 25f
            
            // Suggestion (wrap text if needed)
            canvas.drawText("${context.getString(R.string.pdf_suggestion)}", margin + 10f, yPosition, bodyPaint)
            yPosition += 18f
            val suggestionWords = pattern.suggestion.split(" ")
            var currentLine = ""
            suggestionWords.forEach { word ->
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val textWidth = bodyPaint.measureText(testLine)
                if (textWidth > contentWidth - 30f) {
                    checkNewPage(20f)
                    canvas.drawText(currentLine, margin + 20f, yPosition, smallPaint)
                    yPosition += 16f
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                checkNewPage(20f)
                canvas.drawText(currentLine, margin + 20f, yPosition, smallPaint)
                yPosition += 20f
            }
            
            // Occurrence details (show first 3)
            if (pattern.details.isNotEmpty()) {
                checkNewPage(80f)
                canvas.drawText("${context.getString(R.string.pdf_recent_occurrences)}", margin + 10f, yPosition, bodyPaint)
                yPosition += 18f
                pattern.details.take(3).forEachIndexed { detailIndex, occurrence ->
                    checkNewPage(60f)
                    canvas.drawText("${detailIndex + 1}. ${occurrence.date} (${occurrence.timeOfDay})", 
                        margin + 20f, yPosition, smallPaint)
                    yPosition += 15f
                    canvas.drawText("   ${occurrence.context}", margin + 25f, yPosition, smallPaint)
                    yPosition += 15f
                    if (occurrence.bloodSugar.isNotBlank()) {
                        canvas.drawText("   ${context.getString(R.string.pdf_blood_sugar)} ${occurrence.bloodSugar}", margin + 25f, yPosition, smallPaint)
                        yPosition += 15f
                    }
                }
                if (pattern.details.size > 3) {
                    canvas.drawText("   ${context.getString(R.string.pdf_more_occurrences, pattern.details.size - 3)}", 
                        margin + 20f, yPosition, smallPaint)
                    yPosition += 15f
                }
            }
            
            yPosition += 20f
            
            // Separator line between patterns
            checkNewPage(20f)
            canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint)
            yPosition += 20f
        }
        
        // Footer/Disclaimer
        checkNewPage(100f)
        yPosition += 10f
        canvas.drawText(context.getString(R.string.pdf_disclaimer), margin, yPosition, subheaderPaint)
        yPosition += 20f
        
        // Wrap disclaimer text
        val disclaimerText = context.getString(R.string.pdf_disclaimer_text)
        val disclaimerWords = disclaimerText.split(" ")
        var disclaimerLine = ""
        disclaimerWords.forEach { word ->
            val testLine = if (disclaimerLine.isEmpty()) word else "$disclaimerLine $word"
            val textWidth = smallPaint.measureText(testLine)
            if (textWidth > contentWidth) {
                canvas.drawText(disclaimerLine, margin, yPosition, smallPaint)
                yPosition += 14f
                disclaimerLine = word
            } else {
                disclaimerLine = testLine
            }
        }
        if (disclaimerLine.isNotEmpty()) {
            canvas.drawText(disclaimerLine, margin, yPosition, smallPaint)
        }
        
        // Finish last page
        pdfDocument.finishPage(page)
        
        // Write PDF to file
        context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        
        pdfDocument.close()
        
        Toast.makeText(context, context.getString(R.string.pattern_analysis_exported_pdf), Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, context.getString(R.string.export_failed), Toast.LENGTH_SHORT).show()
    }
}
