package com.j4.diabetestracker

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.j4.diabetestracker.Marker
import com.j4.diabetestracker.DiabetesEntry

// Helper function to generate PDF for marker history
fun generateMarkerHistoryPDF(
    context: Context,
    fileUri: Uri,
    markerInstances: List<Triple<Marker, DiabetesEntry, String>>,
    language: String
) {
    val pdfDocument = PdfDocument()
    val pageWidth = 595 // A4 width in points
    val pageHeight = 842 // A4 height in points
    val margin = 40f
    val contentWidth = pageWidth - (2 * margin)
    
    var pageNumber = 1
    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas
    var yPosition = margin + 20f
    
    // Paint objects
    val titlePaint = Paint().apply {
        textSize = 20f
        isFakeBoldText = true
        isAntiAlias = true
    }
    
    val headerPaint = Paint().apply {
        textSize = 16f
        isFakeBoldText = true
        isAntiAlias = true
    }
    
    val bodyPaint = Paint().apply {
        textSize = 12f
        isAntiAlias = true
    }
    
    val smallPaint = Paint().apply {
        textSize = 10f
        isAntiAlias = true
        color = android.graphics.Color.GRAY
    }
    
    // Title
    val title = when (language) {
        "German" -> "MARKER-VERLAUF EXPORT"
        "Spanish" -> "EXPORTACIÓN DE HISTORIAL DE MARCADORES"
        else -> "MARKER HISTORY EXPORT"
    }
    canvas.drawText(title, margin, yPosition, titlePaint)
    yPosition += 40f
    
    // Summary
    val summaryTitle = when (language) {
        "German" -> "Zusammenfassung"
        "Spanish" -> "Resumen"
        else -> "Summary"
    }
    canvas.drawText(summaryTitle, margin, yPosition, headerPaint)
    yPosition += 25f
    
    val totalText = when (language) {
        "German" -> "Gesamt: ${markerInstances.size} Marker"
        "Spanish" -> "Total: ${markerInstances.size} marcadores"
        else -> "Total: ${markerInstances.size} markers"
    }
    canvas.drawText(totalText, margin, yPosition, bodyPaint)
    yPosition += 30f
    
    // Category breakdown
    val markersByType = markerInstances.groupBy { it.third }
    for ((markerType, instances) in markersByType.entries.sortedByDescending { it.value.size }) {
        // Check if we need a new page
        if (yPosition > pageHeight - 100) {
            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPosition = margin + 20f
        }
        
        canvas.drawText("• $markerType: ${instances.size} ${when (language) {
            "German" -> "mal"
            "Spanish" -> "veces"
            else -> "times"
        }}", margin, yPosition, bodyPaint)
        yPosition += 20f
        
        // Unique days
        val uniqueDays = instances.map { it.second.date }.toSet().size
        canvas.drawText("  ${when (language) {
            "German" -> "An $uniqueDays verschiedenen Tagen"
            "Spanish" -> "En $uniqueDays días diferentes"
            else -> "On $uniqueDays different days"
        }}", margin + 10, yPosition, smallPaint)
        yPosition += 25f
    }
    
    yPosition += 20f
    
    // Detailed timeline
    if (yPosition > pageHeight - 100) {
        pdfDocument.finishPage(page)
        pageNumber++
        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        yPosition = margin + 20f
    }
    
    val timelineTitle = when (language) {
        "German" -> "Detaillierte Zeitleiste"
        "Spanish" -> "Cronología Detallada"
        else -> "Detailed Timeline"
    }
    canvas.drawText(timelineTitle, margin, yPosition, headerPaint)
    yPosition += 30f
    
    // Timeline entries
    for ((marker, entry, markerType) in markerInstances) {
        // Check if we need a new page
        if (yPosition > pageHeight - 150) {
            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPosition = margin + 20f
        }
        
        // Date and time
        canvas.drawText("${entry.date} - ${marker.startTime}", margin, yPosition, bodyPaint)
        yPosition += 18f
        
        // Type
        canvas.drawText("  ${when (language) {
            "German" -> "Typ:"
            "Spanish" -> "Tipo:"
            else -> "Type:"
        }} $markerType", margin + 10, yPosition, smallPaint)
        yPosition += 15f
        
        // Blood sugar
        val cellValue = when (marker.cellId) {
            "morning" -> entry.morningBloodSugarLevel
            "afternoon" -> entry.afternoonBloodSugarLevel
            "evening" -> entry.eveningBloodSugarLevel
            "night" -> entry.nightBloodSugarLevel
            else -> ""
        }
        if (cellValue.isNotEmpty()) {
            canvas.drawText("  ${when (language) {
                "German" -> "Blutzucker:"
                "Spanish" -> "Azúcar en sangre:"
                else -> "Blood Sugar:"
            }} $cellValue", margin + 10, yPosition, smallPaint)
            yPosition += 15f
        }
        
        // Insulin
        val insulinValue = when (marker.cellId) {
            "morning" -> entry.morningInsulin
            "afternoon" -> entry.afternoonInsulin
            "evening" -> entry.eveningInsulin
            else -> ""
        }
        if (insulinValue.isNotEmpty()) {
            canvas.drawText("  ${when (language) {
                "German" -> "Insulin:"
                "Spanish" -> "Insulina:"
                else -> "Insulin:"
            }} $insulinValue", margin + 10, yPosition, smallPaint)
            yPosition += 15f
        }
        
        // Reasons
        if (marker.reasons.isNotEmpty()) {
            canvas.drawText("  ${when (language) {
                "German" -> "Gründe:"
                "Spanish" -> "Razones:"
                else -> "Reasons:"
            }}", margin + 10, yPosition, smallPaint)
            yPosition += 15f
            for (reason in marker.reasons) {
                canvas.drawText("    - $reason", margin + 20, yPosition, smallPaint)
                yPosition += 13f
            }
        }
        
        // Custom note
        if (marker.customNote.isNotEmpty()) {
            canvas.drawText("  ${when (language) {
                "German" -> "Notiz:"
                "Spanish" -> "Nota:"
                else -> "Note:"
            }} ${marker.customNote}", margin + 10, yPosition, smallPaint)
            yPosition += 15f
        }
        
        yPosition += 10f
    }
    
    pdfDocument.finishPage(page)
    
    // Write to file
    context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
        pdfDocument.writeTo(outputStream)
    }
    pdfDocument.close()
}
