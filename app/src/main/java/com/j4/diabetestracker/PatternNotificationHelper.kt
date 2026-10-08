package com.j4.diabetestracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Helper for pattern summary notifications
 */
object PatternNotificationHelper {
    
    private const val CHANNEL_ID = "pattern_summary_channel"
    private const val CHANNEL_NAME = "Pattern Summaries"
    private const val NOTIFICATION_ID = 1001
    
    /**
     * Create notification channel (required for Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Weekly and monthly pattern analysis summaries"
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Show pattern summary notification
     */
    fun showPatternSummaryNotification(
        context: Context,
        patterns: List<DetectedPattern>,
        periodType: String = "Weekly" // "Weekly" or "Monthly"
    ) {
        createNotificationChannel(context)
        
        if (patterns.isEmpty()) {
            return // Don't notify if no patterns
        }
        
        val highRiskCount = patterns.count { it.confidenceScore >= 0.8f }
        val newPatternsCount = patterns.size
        
        // Build notification title and text
        val title = when {
            highRiskCount > 0 -> "⚠️ $periodType Pattern Alert"
            else -> "📊 $periodType Pattern Summary"
        }
        
        val text = when {
            highRiskCount > 0 -> "$highRiskCount high-risk pattern${if (highRiskCount != 1) "s" else ""} detected! Tap to review."
            newPatternsCount > 0 -> "$newPatternsCount pattern${if (newPatternsCount != 1) "s" else ""} detected. Tap to view details."
            else -> "Your health patterns are being monitored."
        }
        
        // Create intent to open app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_analysis", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(if (highRiskCount > 0) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Add big text style for more details
        if (patterns.isNotEmpty()) {
            val topPatterns = patterns.sortedByDescending { it.confidenceScore }.take(3)
            val bigText = buildString {
                append("Top Patterns:\n")
                topPatterns.forEach { pattern ->
                    append("• ${pattern.correlatedItem} → ${pattern.markerName}\n")
                }
            }
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
        }
        
        // Show notification
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted, silently fail
        }
    }
    
    /**
     * Generate weekly summary
     */
    fun generateWeeklySummary(
        context: Context,
        entries: List<DiabetesEntry>,
        customMarkers: List<CustomMarkerType>
    ) {
        // Detect patterns for last 7 days
        val patterns = PatternDetectionEngine.detectPatterns(
            entries = entries,
            customMarkers = customMarkers,
            daysToAnalyze = 7,
            context = context
        )
        
        // Show notification if patterns found
        if (patterns.isNotEmpty()) {
            showPatternSummaryNotification(context, patterns, "Weekly")
        }
    }
    
    /**
     * Generate monthly summary
     */
    fun generateMonthlySummary(
        context: Context,
        entries: List<DiabetesEntry>,
        customMarkers: List<CustomMarkerType>
    ) {
        // Detect patterns for last 30 days
        val patterns = PatternDetectionEngine.detectPatterns(
            entries = entries,
            customMarkers = customMarkers,
            daysToAnalyze = 30,
            context = context
        )
        
        // Show notification if patterns found
        if (patterns.isNotEmpty()) {
            showPatternSummaryNotification(context, patterns, "Monthly")
        }
    }
    
    /**
     * Check if it's time for weekly summary (every Monday)
     */
    fun shouldShowWeeklySummary(context: Context): Boolean {
        val prefs = context.getSharedPreferences("pattern_notifications", Context.MODE_PRIVATE)
        val lastWeeklySummary = prefs.getLong("last_weekly_summary", 0)
        val currentTime = System.currentTimeMillis()
        val oneWeek = 7 * 24 * 60 * 60 * 1000L
        
        return (currentTime - lastWeeklySummary) >= oneWeek
    }
    
    /**
     * Check if it's time for monthly summary (1st of each month)
     */
    fun shouldShowMonthlySummary(context: Context): Boolean {
        val prefs = context.getSharedPreferences("pattern_notifications", Context.MODE_PRIVATE)
        val lastMonthlySummary = prefs.getLong("last_monthly_summary", 0)
        val currentTime = System.currentTimeMillis()
        val oneMonth = 30 * 24 * 60 * 60 * 1000L
        
        return (currentTime - lastMonthlySummary) >= oneMonth
    }
    
    /**
     * Mark weekly summary as shown
     */
    fun markWeeklySummaryShown(context: Context) {
        val prefs = context.getSharedPreferences("pattern_notifications", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_weekly_summary", System.currentTimeMillis()).apply()
    }
    
    /**
     * Mark monthly summary as shown
     */
    fun markMonthlySummaryShown(context: Context) {
        val prefs = context.getSharedPreferences("pattern_notifications", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_monthly_summary", System.currentTimeMillis()).apply()
    }
}
