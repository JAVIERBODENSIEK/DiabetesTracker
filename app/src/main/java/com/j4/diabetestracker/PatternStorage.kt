package com.j4.diabetestracker

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Manager for storing and retrieving detected patterns
 */
object PatternStorageManager {
    private const val PREFS_NAME = "pattern_storage"
    private const val KEY_PATTERNS = "detected_patterns"
    private const val KEY_LAST_ANALYSIS = "last_analysis_timestamp"
    private const val KEY_DISMISSED_PATTERNS = "dismissed_patterns"
    
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    
    /**
     * Save detected patterns to storage
     */
    fun savePatterns(context: Context, patterns: List<DetectedPattern>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val patternsJson = json.encodeToString(patterns)
        prefs.edit().putString(KEY_PATTERNS, patternsJson).apply()
        prefs.edit().putLong(KEY_LAST_ANALYSIS, System.currentTimeMillis()).apply()
    }
    
    /**
     * Load detected patterns from storage
     */
    fun loadPatterns(context: Context): List<DetectedPattern> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val patternsJson = prefs.getString(KEY_PATTERNS, null) ?: return emptyList()
        
        return try {
            json.decodeFromString<List<DetectedPattern>>(patternsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get timestamp of last pattern analysis
     */
    fun getLastAnalysisTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_ANALYSIS, 0L)
    }
    
    /**
     * Check if patterns need to be re-analyzed (once per day)
     */
    fun shouldReanalyze(context: Context): Boolean {
        val lastAnalysis = getLastAnalysisTime(context)
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        return lastAnalysis < oneDayAgo
    }
    
    /**
     * Dismiss a pattern (user doesn't want to see it anymore)
     */
    fun dismissPattern(context: Context, patternId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dismissed = getDismissedPatterns(context).toMutableSet()
        dismissed.add(patternId)
        prefs.edit().putStringSet(KEY_DISMISSED_PATTERNS, dismissed).apply()
    }
    
    /**
     * Get list of dismissed pattern IDs
     */
    fun getDismissedPatterns(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_DISMISSED_PATTERNS, emptySet()) ?: emptySet()
    }
    
    /**
     * Get active patterns (not dismissed)
     */
    fun getActivePatterns(context: Context): List<DetectedPattern> {
        val allPatterns = loadPatterns(context)
        val dismissed = getDismissedPatterns(context)
        return allPatterns.filter { it.id !in dismissed }
    }
    
    /**
     * Get high-priority patterns (confidence >= 0.7)
     */
    fun getHighPriorityPatterns(context: Context): List<DetectedPattern> {
        return getActivePatterns(context).filter { it.confidenceScore >= 0.7f }
    }
    
    /**
     * Clear all patterns
     */
    fun clearPatterns(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_PATTERNS).apply()
        prefs.edit().remove(KEY_LAST_ANALYSIS).apply()
    }
    
    /**
     * Clear dismissed patterns list
     */
    fun clearDismissedPatterns(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_DISMISSED_PATTERNS).apply()
    }
}
