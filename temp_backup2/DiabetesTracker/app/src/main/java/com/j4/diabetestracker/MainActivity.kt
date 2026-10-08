package com.j4.diabetestracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.provider.DocumentsContract
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.WindowManager
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.positionInWindow
import android.widget.Toast
import com.j4.diabetestracker.ui.theme.DiabetesTrackerTheme
import java.io.File
import java.io.FileOutputStream
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
// import java.time.format.TextStyle - using fully qualified name instead
import java.util.Locale
import java.util.UUID
import kotlinx.serialization.encodeToString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.text.input.TextFieldValue

@Serializable
data class ColorPreset(
    val name: String,
    @Serializable(with = ColorSerializer::class)
    val color: Color,
    val id: String = UUID.randomUUID().toString()
)

@Serializable
data class FoodPreset(
    val name: String,
    val emoji: String,
    val id: String = UUID.randomUUID().toString()
)

@Serializable
data class FoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String = ""
)

@Serializable
data class ColorScheme(
    @Serializable(with = ColorSerializer::class)
    val fontColor: Color,
    @Serializable(with = ColorSerializer::class)
    val backgroundColor: Color,
    @Serializable(with = ColorSerializer::class)
    val gridColor: Color,
    val name: String,
    val isDarkMode: Boolean,
    val id: String = UUID.randomUUID().toString()
)

object ColorSerializer : KSerializer<Color> {
    override val descriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: Color) {
        val colorString = "rgba(${(value.red * 255).toInt()}, ${(value.green * 255).toInt()}, ${(value.blue * 255).toInt()}, ${value.alpha})"
        encoder.encodeString(colorString)
    }
    
    override fun deserialize(decoder: Decoder): Color {
        val colorString = decoder.decodeString()
        val regex = """rgba\((\d+),\s*(\d+),\s*(\d+),\s*([\d.]+)\)""".toRegex()
        val match = regex.find(colorString) ?: return Color.Black
        val (r, g, b, a) = match.destructured
        return Color(
            red = r.toInt() / 255f,
            green = g.toInt() / 255f,
            blue = b.toInt() / 255f,
            alpha = a.toFloat()
        )
    }
}

@Serializable
data class ViewPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val scale: Float,
    val widthScale: Float
)

@Serializable
data class AppSettings(
    @Serializable(with = ColorSerializer::class)
    val fontColor: Color = Color.Black,
    @Serializable(with = ColorSerializer::class)
    val backgroundColor: Color = Color.White,
    @Serializable(with = ColorSerializer::class)
    val gridColor: Color = Color.Gray,
    @Serializable(with = ColorSerializer::class)
    val insulinColumnColor: Color = Color(0xFF3F51B5).copy(alpha = 0.3f), // Default insulin column color (Material Indigo with transparency)
    val isDarkMode: Boolean = false,
    val language: String = "English",
    val colorsModifiedByUser: Boolean = false,
    val scale: Float = 1f, // Current scale
    val widthScale: Float = 2.2f, // Current width scale
    val zoomPercentage: Int = 100, // Current zoom percentage
    val useSymbolsInCharts: Boolean = true,
    val savedUserScale: Float? = null, // User's saved scale
    val savedUserWidthScale: Float? = null, // User's saved width scale
    val showArchivedEntries: Boolean = true // Archive/hide view state persistence
)

@Serializable
enum class BackupType {
    SETTINGS_ONLY,
    DATA_ONLY,
    ALL
}

@Serializable
data class BackupData(
    val appVersion: String = "1.0.0",
    val exportDate: String,
    val exportType: BackupType,
    val settings: AppSettings? = null,
    val entries: List<DiabetesEntry>? = null,
    val foodPresets: List<FoodPreset>? = null,
    val colorSchemes: List<ColorScheme>? = null,
    val viewPresets: List<ViewPreset>? = null,
    val securedEntryIds: Set<String>? = null
)

@Serializable
data class BackupMetadata(
    val settingsCount: Int = 0,
    val entriesCount: Int = 0,
    val foodPresetsCount: Int = 0,
    val colorSchemesCount: Int = 0,
    val viewPresetsCount: Int = 0,
    val hasSecuredEntries: Boolean = false
)

object SettingsManager {
    private const val SETTINGS_NAME = "app_settings"
    private const val SETTINGS_KEY = "settings"
    private const val COLOR_SCHEMES_KEY = "color_schemes"
    private const val FOOD_PRESETS_KEY = "food_presets"
    private const val VIEW_PRESETS_KEY = "view_presets"
    private const val SECURED_ENTRY_IDS_KEY = "secured_entry_ids"
    
    // Cache values in memory to avoid frequent disk reads
    private var cachedSettings: AppSettings? = null
    private var cachedColorSchemes: List<ColorScheme>? = null
    private var cachedViewPresets: List<ViewPreset>? = null
    private var cachedFoodPresets: List<FoodPreset>? = null

    fun getDefaultColors(isDarkMode: Boolean): Quartet<Color, Color, Color, Color> {
        return if (isDarkMode) {
            Quartet(Color.White, Color.DarkGray, Color.Black, Color(0xFF7986CB).copy(alpha = 0.3f)) // Lighter indigo for dark mode
        } else {
            Quartet(Color.Black, Color.LightGray, Color.Black, Color(0xFF3F51B5).copy(alpha = 0.3f)) // Standard indigo for light mode
        }
    }
    
    // Helper class for returning 4 values
    data class Quartet<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    fun loadSettings(context: Context): AppSettings {
        // Return cached settings if available
        cachedSettings?.let { return it }
        
        val sharedPrefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        val json = sharedPrefs.getString(SETTINGS_KEY, null)
        val settings = if (json != null) {
            try {
                Json.decodeFromString<AppSettings>(json)
            } catch (e: Exception) {
                createDefaultSettings(context)
            }
        } else {
            createDefaultSettings(context)
        }
        
        // Cache the result
        cachedSettings = settings
        return settings
    }
    
    private fun createDefaultSettings(context: Context): AppSettings {
        val isDarkMode = context.resources.configuration.uiMode and 
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val (fontColor, backgroundColor, gridColor) = getDefaultColors(isDarkMode)
        return AppSettings(
            fontColor = fontColor,
            backgroundColor = backgroundColor,
            gridColor = gridColor,
            isDarkMode = isDarkMode
        )
    }

    fun saveSettings(context: Context, settings: AppSettings) {
        // Update cache immediately
        cachedSettings = settings
        
        // Save to disk in background
        CoroutineScope(Dispatchers.IO).launch {
            val sharedPrefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
            val json = Json.encodeToString(settings)
            sharedPrefs.edit().putString(SETTINGS_KEY, json).apply()
        }
    }

    fun saveColorScheme(context: Context, scheme: ColorScheme) {
        // Update cache first
        val currentSchemes = cachedColorSchemes ?: getColorSchemes(context)
        val updatedSchemes = currentSchemes + scheme
        cachedColorSchemes = updatedSchemes
        
        // Save to disk in background
        CoroutineScope(Dispatchers.IO).launch {
            val sharedPrefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
            sharedPrefs.edit().putString(COLOR_SCHEMES_KEY, Json.encodeToString(updatedSchemes)).apply()
        }
    }

    fun getColorSchemes(context: Context): List<ColorScheme> {
        // Return cached schemes if available
        cachedColorSchemes?.let { return it }
        
        val sharedPrefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        val json = sharedPrefs.getString(COLOR_SCHEMES_KEY, "[]") ?: "[]"
        return try {
            Json.decodeFromString<List<ColorScheme>>(json).also { cachedColorSchemes = it }
        } catch (e: Exception) {
            emptyList<ColorScheme>().also { cachedColorSchemes = it }
        }
    }

    fun deleteColorScheme(context: Context, schemeId: String) {
        // Update cache first
        val currentSchemes = cachedColorSchemes ?: getColorSchemes(context)
        val updatedSchemes = currentSchemes.filter { it.id != schemeId }
        cachedColorSchemes = updatedSchemes
        
        // Save to disk in background
        CoroutineScope(Dispatchers.IO).launch {
            val sharedPrefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
            sharedPrefs.edit().putString(COLOR_SCHEMES_KEY, Json.encodeToString(updatedSchemes)).apply()
        }
    }

    fun clearColorSchemes(context: Context) {
        // Clear cache
        cachedColorSchemes = emptyList()
        
        // Clear from disk
        CoroutineScope(Dispatchers.IO).launch {
            val sharedPrefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
            sharedPrefs.edit().putString(COLOR_SCHEMES_KEY, "[]").apply()
        }
    }

    // ---- VIEW PRESET FUNCTIONS ----
    fun getViewPresets(context: Context): List<ViewPreset> {
        // Return cached presets if available
        cachedViewPresets?.let { return it }
        
        val sharedPrefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        val json = sharedPrefs.getString(VIEW_PRESETS_KEY, "[]") ?: "[]"
        return try { 
            Json.decodeFromString<List<ViewPreset>>(json).also { cachedViewPresets = it } 
        } catch (e: Exception) { 
            emptyList<ViewPreset>().also { cachedViewPresets = it } 
        }
    }

    fun saveViewPresets(context: Context, presets: List<ViewPreset>) {
        // Update cache first
        cachedViewPresets = presets
        
        // Save to disk in background
        CoroutineScope(Dispatchers.IO).launch {
            val sharedPrefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
            sharedPrefs.edit().putString(VIEW_PRESETS_KEY, Json.encodeToString(presets)).apply()
        }
    }

    fun saveFoodPreset(context: Context, preset: FoodPreset) {
        val sharedPrefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        val existingJson = sharedPrefs.getString(FOOD_PRESETS_KEY, "[]") ?: "[]"
        val presets = Json.decodeFromString<List<FoodPreset>>(existingJson)
        val updatedPresets = presets + preset
        sharedPrefs.edit().putString(FOOD_PRESETS_KEY, Json.encodeToString(updatedPresets)).apply()
    }

    fun getFoodPresets(context: Context): List<FoodPreset> {
        val sharedPrefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        val json = sharedPrefs.getString(FOOD_PRESETS_KEY, "[]") ?: "[]"
        return Json.decodeFromString(json)
    }

    fun deleteFoodPreset(context: Context, presetId: String) {
        val sharedPrefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        val existingJson = sharedPrefs.getString(FOOD_PRESETS_KEY, "[]") ?: "[]"
        val presets = Json.decodeFromString<List<FoodPreset>>(existingJson)
        val updatedPresets = presets.filter { it.id != presetId }
        sharedPrefs.edit().putString(FOOD_PRESETS_KEY, Json.encodeToString(updatedPresets)).apply()
    }

    // Secure mode persistence methods
    fun saveSecuredEntryIds(context: Context, securedIds: Set<String>) {
        val sharedPrefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        val idsJson = Json.encodeToString(securedIds.toList())
        sharedPrefs.edit().putString(SECURED_ENTRY_IDS_KEY, idsJson).apply()
    }

    fun loadSecuredEntryIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString("secured_entry_ids", null) ?: return emptySet()
        return try {
            Json.decodeFromString<Set<String>>(json)
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    // Filter persistence methods
    fun saveLastDateFilter(context: Context, dateFilter: DateFilter, customStartDate: LocalDate? = null, customEndDate: LocalDate? = null, showEmptyDates: Boolean = true, flexibleNumber: Int = 1, flexibleUnit: TimeUnit = TimeUnit.DAY) {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("last_date_filter", dateFilter.name)
            .putString("custom_start_date", customStartDate?.toString())
            .putString("custom_end_date", customEndDate?.toString())
            .putBoolean("show_empty_dates_in_filter", showEmptyDates)
            .putInt("flexible_number", flexibleNumber)
            .putString("flexible_unit", flexibleUnit.name)
            .apply()
    }
    
    fun loadLastDateFilter(context: Context): Tuple5<DateFilter, Pair<LocalDate?, LocalDate?>, Boolean, Int, TimeUnit> {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        val filterName = prefs.getString("last_date_filter", DateFilter.ALL.name) ?: DateFilter.ALL.name
        val startDateStr = prefs.getString("custom_start_date", null)
        val endDateStr = prefs.getString("custom_end_date", null)
        val showEmptyDates = prefs.getBoolean("show_empty_dates_in_filter", true)
        val flexibleNumber = prefs.getInt("flexible_number", 1)
        val flexibleUnitStr = prefs.getString("flexible_unit", TimeUnit.DAY.name) ?: TimeUnit.DAY.name
        
        val dateFilter = try {
            DateFilter.valueOf(filterName)
        } catch (e: Exception) {
            DateFilter.ALL
        }
        
        val startDate = try {
            startDateStr?.let { LocalDate.parse(it) }
        } catch (e: Exception) {
            null
        }
        
        val endDate = try {
            endDateStr?.let { LocalDate.parse(it) }
        } catch (e: Exception) {
            null
        }
        
        val flexibleUnit = try {
            TimeUnit.valueOf(flexibleUnitStr)
        } catch (e: Exception) {
            TimeUnit.DAY
        }
        
        return Tuple5(dateFilter, Pair(startDate, endDate), showEmptyDates, flexibleNumber, flexibleUnit)
    }
    
    // Auto-save on exit setting methods
    fun saveAutoSaveOnExit(context: Context, autoSave: Boolean) {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("auto_save_on_exit", autoSave).apply()
    }
    
    fun loadAutoSaveOnExit(context: Context): Boolean {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("auto_save_on_exit", true) // Default to true (enabled)
    }
}

object BackupManager {
    
    /**
     * Creates a backup based on the specified type
     */
    fun createBackup(context: Context, backupType: BackupType): BackupData {
        val currentDate = java.time.LocalDateTime.now().toString()
        
        return when (backupType) {
            BackupType.SETTINGS_ONLY -> BackupData(
                exportDate = currentDate,
                exportType = backupType,
                settings = SettingsManager.loadSettings(context),
                colorSchemes = SettingsManager.getColorSchemes(context),
                viewPresets = SettingsManager.getViewPresets(context)
            )
            
            BackupType.DATA_ONLY -> BackupData(
                exportDate = currentDate,
                exportType = backupType,
                entries = DataManager.getEntries(context),
                foodPresets = PresetManager.getFoodPresets(context),
                securedEntryIds = SettingsManager.loadSecuredEntryIds(context)
            )
            
            BackupType.ALL -> BackupData(
                exportDate = currentDate,
                exportType = backupType,
                settings = SettingsManager.loadSettings(context),
                entries = DataManager.getEntries(context),
                foodPresets = PresetManager.getFoodPresets(context),
                colorSchemes = SettingsManager.getColorSchemes(context),
                viewPresets = SettingsManager.getViewPresets(context),
                securedEntryIds = SettingsManager.loadSecuredEntryIds(context)
            )
        }
    }
    
    /**
     * Exports backup data to a file URI
     */
    fun exportBackupToFile(context: Context, backupData: BackupData, fileUri: Uri) {
        try {
            context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                outputStream.bufferedWriter().use { writer ->
                    val backupJson = Json.encodeToString(backupData)
                    writer.write(backupJson)
                }
            }
            
            val typeText = when (backupData.exportType) {
                BackupType.SETTINGS_ONLY -> "Settings"
                BackupType.DATA_ONLY -> "Data"
                BackupType.ALL -> "Complete"
            }
            
            Toast.makeText(context, "$typeText backup exported successfully", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export backup: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Reads and validates backup data from a file URI
     */
    fun readBackupFromFile(context: Context, fileUri: Uri): BackupData? {
        return try {
            context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                inputStream.bufferedReader().use { reader ->
                    val backupJson = reader.readText()
                    Json.decodeFromString<BackupData>(backupJson)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to read backup file: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }
    
    /**
     * Gets metadata about backup contents for preview
     */
    fun getBackupMetadata(backupData: BackupData): BackupMetadata {
        return BackupMetadata(
            settingsCount = if (backupData.settings != null) 1 else 0,
            entriesCount = backupData.entries?.size ?: 0,
            foodPresetsCount = backupData.foodPresets?.size ?: 0,
            colorSchemesCount = backupData.colorSchemes?.size ?: 0,
            viewPresetsCount = backupData.viewPresets?.size ?: 0,
            hasSecuredEntries = !backupData.securedEntryIds.isNullOrEmpty()
        )
    }
    
    /**
     * Imports backup data with merge/replace options
     */
    fun importBackup(context: Context, backupData: BackupData, mergeWithExisting: Boolean = true) {
        try {
            // Import settings
            backupData.settings?.let { settings ->
                SettingsManager.saveSettings(context, settings)
            }
            
            // Import entries
            backupData.entries?.let { entries ->
                if (mergeWithExisting) {
                    val existingEntries = DataManager.getEntries(context)
                    val mergedEntries = mergeEntries(existingEntries, entries)
                    DataManager.saveEntries(context, mergedEntries)
                } else {
                    DataManager.saveEntries(context, entries)
                }
            }
            
            // Import food presets
            backupData.foodPresets?.let { presets ->
                if (mergeWithExisting) {
                    val existingPresets = PresetManager.getFoodPresets(context)
                    val mergedPresets = mergeFoodPresets(existingPresets, presets)
                    PresetManager.saveFoodPresets(context, mergedPresets)
                } else {
                    PresetManager.saveFoodPresets(context, presets)
                }
            }
            
            // Import color schemes
            backupData.colorSchemes?.let { schemes ->
                if (mergeWithExisting) {
                    schemes.forEach { scheme ->
                        SettingsManager.saveColorScheme(context, scheme)
                    }
                } else {
                    SettingsManager.clearColorSchemes(context)
                    schemes.forEach { scheme ->
                        SettingsManager.saveColorScheme(context, scheme)
                    }
                }
            }
            
            // Import view presets
            backupData.viewPresets?.let { presets ->
                if (mergeWithExisting) {
                    val existingPresets = SettingsManager.getViewPresets(context)
                    val mergedPresets = mergeViewPresets(existingPresets, presets)
                    SettingsManager.saveViewPresets(context, mergedPresets)
                } else {
                    SettingsManager.saveViewPresets(context, presets)
                }
            }
            
            // Import secured entry IDs
            backupData.securedEntryIds?.let { securedIds ->
                if (mergeWithExisting) {
                    val existingIds = SettingsManager.loadSecuredEntryIds(context)
                    SettingsManager.saveSecuredEntryIds(context, existingIds + securedIds)
                } else {
                    SettingsManager.saveSecuredEntryIds(context, securedIds)
                }
            }
            
            val actionText = if (mergeWithExisting) "merged" else "restored"
            Toast.makeText(context, "Backup $actionText successfully", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to import backup: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Merges existing entries with backup entries, avoiding duplicates
     */
    private fun mergeEntries(existing: List<DiabetesEntry>, backup: List<DiabetesEntry>): List<DiabetesEntry> {
        val existingIds = existing.map { it.id }.toSet()
        val newEntries = backup.filter { it.id !in existingIds }
        return existing + newEntries
    }
    
    /**
     * Merges existing food presets with backup presets, avoiding duplicates
     */
    private fun mergeFoodPresets(existing: List<FoodPreset>, backup: List<FoodPreset>): List<FoodPreset> {
        val existingNames = existing.map { "${it.name}_${it.emoji}" }.toSet()
        val newPresets = backup.filter { "${it.name}_${it.emoji}" !in existingNames }
        return existing + newPresets
    }
    
    /**
     * Merges existing view presets with backup presets, avoiding duplicates
     */
    private fun mergeViewPresets(existing: List<ViewPreset>, backup: List<ViewPreset>): List<ViewPreset> {
        val existingNames = existing.map { it.name }.toSet()
        val newPresets = backup.filter { it.name !in existingNames }
        return existing + newPresets
    }
}

object PresetManager {
    private const val PREFS_NAME = "color_presets"
    private const val KEY_PRESETS = "saved_presets"
    private const val FOOD_PRESETS_KEY = "food_presets"
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun savePreset(context: Context, preset: ColorPreset) {
        val prefs = getPreferences(context)
        val existingPresets = getPresets(context).toMutableList()
        existingPresets.add(preset)
        val presetsJson = Json.encodeToString(existingPresets)
        prefs.edit().putString(KEY_PRESETS, presetsJson).apply()
    }
    
    fun getPresets(context: Context): List<ColorPreset> {
        val prefs = getPreferences(context)
        val presetsJson = prefs.getString(KEY_PRESETS, "[]")
        return try {
            Json.decodeFromString(presetsJson ?: "[]")
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun deletePreset(context: Context, presetId: String) {
        val prefs = getPreferences(context)
        val existingPresets = getPresets(context).toMutableList()
        existingPresets.removeAll { it.id == presetId }
        val presetsJson = Json.encodeToString(existingPresets)
        prefs.edit().putString(KEY_PRESETS, presetsJson).apply()
    }

    fun getFoodPresets(context: Context): List<FoodPreset> {
        val sharedPreferences = context.getSharedPreferences("presets", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(FOOD_PRESETS_KEY, "[]") ?: "[]"
        return try {
            Json.decodeFromString<List<FoodPreset>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveFoodPresets(context: Context, presets: List<FoodPreset>) {
        val sharedPreferences = context.getSharedPreferences("presets", Context.MODE_PRIVATE)
        val json = Json.encodeToString(presets)
        sharedPreferences.edit().putString(FOOD_PRESETS_KEY, json).apply()
    }

    fun addFoodPreset(context: Context, preset: FoodPreset) {
        val presets = getFoodPresets(context).toMutableList()
        presets.add(preset)
        saveFoodPresets(context, presets)
    }

    fun removeFoodPreset(context: Context, presetId: String) {
        val presets = getFoodPresets(context).toMutableList()
        presets.removeAll { it.id == presetId }
        saveFoodPresets(context, presets)
    }
}

object DataManager {
    private const val PREFS_NAME = "diabetes_data"
    private const val KEY_ENTRIES = "entries"
    
    // Cache entries in memory to avoid frequent disk reads
    private var cachedEntries: List<DiabetesEntry>? = null
    
    // Add mutex for thread safety when accessing cached entries
    private val mutex = Mutex()

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveEntries(context: Context, entries: List<DiabetesEntry>) {
        // Update cache immediately
        CoroutineScope(Dispatchers.Default).launch {
            mutex.withLock {
                cachedEntries = entries
            }
        }
        
        // Save to disk in background
        CoroutineScope(Dispatchers.IO).launch {
            val json = Json.encodeToString(entries)
            getPreferences(context).edit().putString(KEY_ENTRIES, json).apply()
        }
    }

    fun getEntries(context: Context): List<DiabetesEntry> {
        // Return cached entries if available
        cachedEntries?.let { return it }
        
        val prefs = getPreferences(context)
        val json = prefs.getString(KEY_ENTRIES, null)
        val result = if (json != null) {
            try {
                Json.decodeFromString<List<DiabetesEntry>>(json)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        
        // Cache the result
        CoroutineScope(Dispatchers.Default).launch {
            mutex.withLock {
                cachedEntries = result
            }
        }
        
        return result
    }
}

// Date filter options for entry filtering
enum class DateFilter(val displayName: String) {
    ALL("All Entries"),
    FLEXIBLE("Flexible Range"),
    CUSTOM("Custom Range"),
    SPECIFIC_DATE("Specific Date")
}

// Time units for flexible date filtering
enum class TimeUnit(val displayName: String) {
    DAY("day"),
    WEEK("week"),
    MONTH("month"),
    YEAR("year")
}

// Data class for returning multiple values
data class Tuple5<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)

// Helper functions for date-based filtering
object DateFilterHelper {
    /**
     * Filters entries based on the selected date filter
     */
    fun filterEntriesByDate(
        entries: List<DiabetesEntry>,
        dateFilter: DateFilter,
        customStartDate: LocalDate? = null,
        customEndDate: LocalDate? = null,
        specificDate: LocalDate? = null,
        flexibleNumber: Int = 1,
        flexibleUnit: TimeUnit = TimeUnit.DAY
    ): List<DiabetesEntry> {
        if (dateFilter == DateFilter.ALL) return entries
        
        val today = LocalDate.now()
        val startDate = when (dateFilter) {
            DateFilter.FLEXIBLE -> {
                when (flexibleUnit) {
                    TimeUnit.DAY -> today.minusDays((flexibleNumber - 1).toLong())
                    TimeUnit.WEEK -> today.minusWeeks(flexibleNumber.toLong()).plusDays(1)
                    TimeUnit.MONTH -> today.minusMonths(flexibleNumber.toLong()).plusDays(1)
                    TimeUnit.YEAR -> today.minusYears(flexibleNumber.toLong()).plusDays(1)
                }
            }
            DateFilter.CUSTOM -> customStartDate ?: return entries
            DateFilter.SPECIFIC_DATE -> specificDate ?: return entries
            DateFilter.ALL -> return entries
        }
        
        val endDate = when (dateFilter) {
            DateFilter.FLEXIBLE -> today
            DateFilter.CUSTOM -> customEndDate ?: today
            DateFilter.SPECIFIC_DATE -> specificDate ?: today
            DateFilter.ALL -> today
        }
        
        // Debug logging
        println("DEBUG: Filtering with dateFilter=$dateFilter, flexibleNumber=$flexibleNumber, flexibleUnit=$flexibleUnit")
        println("DEBUG: today=$today, startDate=$startDate, endDate=$endDate")
        println("DEBUG: Total entries to filter: ${entries.size}")
        
        return entries.filter { entry ->
            try {
                if (entry.date.isBlank()) {
                    println("DEBUG: Entry has blank date, excluding from filter")
                    return@filter false
                }
                
                // Parse entry date - handle DD.MM.YYYY format
                val entryDate = if (entry.date.contains(".")) {
                    // Parse DD.MM.YYYY format
                    val parts = entry.date.split(".")
                    if (parts.size == 3) {
                        val day = parts[0].toIntOrNull() ?: return@filter false
                        val month = parts[1].toIntOrNull() ?: return@filter false
                        val year = parts[2].toIntOrNull() ?: return@filter false
                        LocalDate.of(year, month, day)
                    } else {
                        println("DEBUG: Invalid DD.MM.YYYY format '${entry.date}', excluding")
                        return@filter false
                    }
                } else if (entry.date.contains("-")) {
                    // Parse ISO format YYYY-MM-DD
                    LocalDate.parse(entry.date)
                } else {
                    println("DEBUG: Unknown date format '${entry.date}', excluding")
                    return@filter false
                }
                
                // For debugging: show what we're comparing
                println("DEBUG: Comparing entry date=$entryDate with range [$startDate to $endDate]")
                
                // Check if entry date is within the range (inclusive)
                val isWithinRange = !entryDate.isBefore(startDate) && !entryDate.isAfter(endDate)
                
                println("DEBUG: Entry '${entry.date}' -> $entryDate, isWithinRange=$isWithinRange")
                
                isWithinRange
            } catch (e: Exception) {
                // If date parsing fails, exclude the entry to ensure proper filtering
                println("DEBUG: Failed to parse entry date '${entry.date}': ${e.message}, excluding")
                false
            }
        }
    }
    
    /**
     * Gets a user-friendly description of the current filter
     */
    fun getFilterDescription(
        dateFilter: DateFilter,
        customStartDate: LocalDate? = null,
        customEndDate: LocalDate? = null,
        specificDate: LocalDate? = null,
        flexibleNumber: Int = 1,
        flexibleUnit: TimeUnit = TimeUnit.DAY
    ): String {
        return when (dateFilter) {
            DateFilter.ALL -> "Showing all entries"
            DateFilter.FLEXIBLE -> {
                val unitText = when (flexibleUnit) {
                    TimeUnit.DAY -> if (flexibleNumber == 1) "day" else "days"
                    TimeUnit.WEEK -> if (flexibleNumber == 1) "week" else "weeks"
                    TimeUnit.MONTH -> if (flexibleNumber == 1) "month" else "months"
                    TimeUnit.YEAR -> if (flexibleNumber == 1) "year" else "years"
                }
                "Showing entries from the last $flexibleNumber $unitText"
            }
            DateFilter.CUSTOM -> {
                if (customStartDate != null && customEndDate != null) {
                    "Custom range: ${customStartDate} to ${customEndDate}"
                } else {
                    "Custom range (not set)"
                }
            }
            DateFilter.SPECIFIC_DATE -> {
                if (specificDate != null) {
                    "Specific date: ${specificDate}"
                } else {
                    "Specific date (not set)"
                }
            }
        }
    }
}

// Helper functions for automatic locking and smart archiving
object EntryLockingHelper {
    /**
     * Checks if an entry has any filled blood sugar control fields
     */
    fun hasFilledBloodSugarFields(entry: DiabetesEntry): Boolean {
        return entry.morningBloodSugarLevel.isNotBlank() ||
               entry.afternoonBloodSugarLevel.isNotBlank() ||
               entry.eveningBloodSugarLevel.isNotBlank() ||
               entry.nightBloodSugarLevel.isNotBlank()
    }
    
    /**
     * Checks if an entry has any filled insulin fields
     */
    fun hasFilledInsulinFields(entry: DiabetesEntry): Boolean {
        return entry.morningInsulin.isNotBlank() ||
               entry.afternoonInsulin.isNotBlank() ||
               entry.eveningInsulin.isNotBlank()
    }
    
    /**
     * Checks if an entry should be automatically locked
     * (has data in blood sugar control OR insulin sections)
     */
    fun shouldBeLocked(entry: DiabetesEntry): Boolean {
        return hasFilledBloodSugarFields(entry) || hasFilledInsulinFields(entry)
    }
    
    /**
     * Checks if an entry is complete in both blood sugar control AND insulin sections
     * This determines if the entry should be hidden when archive is active
     */
    fun isCompleteForArchiving(entry: DiabetesEntry): Boolean {
        val hasBloodSugarData = hasFilledBloodSugarFields(entry)
        val hasInsulinData = hasFilledInsulinFields(entry)
        
        // Only hide entries that have data in BOTH sections
        // This ensures incomplete entries (being worked on) always remain visible
        return hasBloodSugarData && hasInsulinData
    }
    
    /**
     * Gets all entry IDs that should be automatically locked
     */
    fun getAutoLockableEntryIds(entries: List<DiabetesEntry>): Set<String> {
        return entries.filter { shouldBeLocked(it) }.map { it.id }.toSet()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiabetesTrackerApp() {
    val context = LocalContext.current
    
    // Load settings once and derive individual values from it
    val settings = remember { SettingsManager.loadSettings(context) }
    
    // Mutable state variables
    var scale by remember { mutableStateOf(settings.scale) }
    var widthScale by remember { mutableStateOf(settings.widthScale) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoomPercentage by remember { mutableStateOf(settings.zoomPercentage) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetName by remember { mutableStateOf("") }
    var showPresetManagerDialog by remember { mutableStateOf(false) }
    var activeDeletePreset by remember { mutableStateOf<ViewPreset?>(null) }
    var selectedLanguage by remember { mutableStateOf(settings.language) }
    var showZoomControls by remember { mutableStateOf(false) }
    var showColorSchemeDialog by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(settings.isDarkMode) }
    var fontColor by remember { mutableStateOf(settings.fontColor) }
    var backgroundColor by remember { mutableStateOf(settings.backgroundColor) }
    var gridColor by remember { mutableStateOf(settings.gridColor) }
    var insulinColumnColor by remember { mutableStateOf(settings.insulinColumnColor) }
    var showChart by remember { mutableStateOf(false) }
    var showAdvancedSettings by remember { mutableStateOf(false) }
    
    // Secure mode state for data protection
    var securedEntryIds by remember { mutableStateOf(setOf<String>()) }
    var showEditConfirmDialog by remember { mutableStateOf(false) }
    var pendingEditAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    // Track cells that are currently being edited after confirmation (to prevent multiple dialogs)
    var confirmedEditingSessions by remember { mutableStateOf(setOf<String>()) }
    // Track unsaved changes and exit confirmation
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var autoSaveOnExit by remember { mutableStateOf(SettingsManager.loadAutoSaveOnExit(context)) }
    var showExitSnackbar by remember { mutableStateOf(false) }
    var backPressedOnce by remember { mutableStateOf(false) }
    
    // Date-based filtering feature state - load saved filter preferences
    val savedFilterData = remember { SettingsManager.loadLastDateFilter(context) }
    var selectedDateFilter by remember { mutableStateOf(savedFilterData.first) }
    var showDateFilterDialog by remember { mutableStateOf(false) }
    var customStartDate by remember { mutableStateOf(savedFilterData.second.first) }
    var customEndDate by remember { mutableStateOf(savedFilterData.second.second) }
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    var showEmptyDatesInFilter by remember { mutableStateOf(savedFilterData.third) } // User option to show/hide empty-date entries during filtering
    var specificDate by remember { mutableStateOf<LocalDate?>(null) } // For specific date filter
    var showSpecificDatePicker by remember { mutableStateOf(false) } // For specific date picker dialog
    
    // Flexible filter state variables - load saved values
    var flexibleNumber by remember { mutableStateOf(savedFilterData.fourth) } // 1-31 days
    var flexibleUnit by remember { mutableStateOf(savedFilterData.fifth) } // day, week, month, year
    
    // Backup/Restore feature state
    var showBackupExportDialog by remember { mutableStateOf(false) }
    var showImportPreviewDialog by remember { mutableStateOf(false) }
    var pendingBackupData by remember { mutableStateOf<BackupData?>(null) }
    var selectedBackupData by remember { mutableStateOf<BackupData?>(null) }
    var selectedBackupType by remember { mutableStateOf(BackupType.ALL) }
    var mergeWithExisting by remember { mutableStateOf(true) }
    
    // Use LaunchedEffect to load data asynchronously
    var entries by remember { mutableStateOf(emptyList<DiabetesEntry>()) }
    
    // Handle back button press - enhanced exit logic with auto-save and double-tap
    BackHandler(enabled = true) {
        if (autoSaveOnExit) {
            // Auto-save enabled: always show snackbar for double-tap confirmation (regardless of unsaved changes)
            if (backPressedOnce) {
                // Second press: save (if needed) and exit
                if (hasUnsavedChanges) {
                    DataManager.saveEntries(context, entries)
                    SettingsManager.saveSettings(context, AppSettings(
                        fontColor = fontColor,
                        backgroundColor = backgroundColor,
                        gridColor = gridColor,
                        insulinColumnColor = insulinColumnColor,
                        isDarkMode = isDarkMode,
                        language = selectedLanguage,
                        colorsModifiedByUser = true,
                        scale = scale,
                        widthScale = widthScale,
                        zoomPercentage = zoomPercentage
                    ))
                    
                    // Mark entries with data as secured
                    val entriesWithData = entries.filter { entry ->
                        entry.date.isNotBlank() && (
                            entry.morningBloodSugarLevel.isNotBlank() ||
                            entry.afternoonBloodSugarLevel.isNotBlank() ||
                            entry.eveningBloodSugarLevel.isNotBlank() ||
                            entry.nightBloodSugarLevel.isNotBlank() ||
                            entry.morningInsulin.isNotBlank() ||
                            entry.afternoonInsulin.isNotBlank() ||
                            entry.eveningInsulin.isNotBlank() ||
                            entry.remarks.isNotBlank() ||
                            entry.foodEntriesByColumn.values.any { it.isNotEmpty() }
                        )
                    }
                    securedEntryIds = entriesWithData.map { it.id }.toSet()
                    SettingsManager.saveSecuredEntryIds(context, securedEntryIds)
                    
                    hasUnsavedChanges = false
                    confirmedEditingSessions = emptySet()
                }
                (context as? Activity)?.finish()
            } else {
                // First press: show snackbar (regardless of unsaved changes)
                backPressedOnce = true
                showExitSnackbar = true
                
                // Reset after 2 seconds
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(2000)
                    backPressedOnce = false
                    showExitSnackbar = false
                }
            }
        } else {
            // Auto-save disabled: check for unsaved changes
            if (hasUnsavedChanges) {
                // Show exit confirmation dialog
                showExitConfirmDialog = true
            } else {
                // No unsaved changes: exit immediately
                (context as? Activity)?.finish()
            }
        }
    }
    
    // File picker for PDF export (more reliable than folder picker)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Generate PDF to selected file location
                generatePDFToFile(context, entries, selectedLanguage, uri)
            }
        }
    }
    
    // File picker for TXT export
    val txtFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Generate TXT to selected file location
                generateTXTToFile(context, entries, selectedLanguage, uri)
            }
        }
    }
    
    // File picker for backup export
    val backupExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Export backup to selected file location
                pendingBackupData?.let { backupData ->
                    BackupManager.exportBackupToFile(context, backupData, uri)
                    pendingBackupData = null
                }
            }
        }
    }
    
    // File picker for backup import
    val backupImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Read and preview backup file
                BackupManager.readBackupFromFile(context, uri)?.let { backupData ->
                    selectedBackupData = backupData
                    showImportPreviewDialog = true
                }
            }
        }
    }
    
    LaunchedEffect(Unit) {
        entries = DataManager.getEntries(context)
        // Load persisted secure mode state
        securedEntryIds = SettingsManager.loadSecuredEntryIds(context)
    }
    
    // Automatic locking effect - locks filled cells when entries change
    LaunchedEffect(entries) {
        if (entries.isNotEmpty()) {
            val autoLockableIds = EntryLockingHelper.getAutoLockableEntryIds(entries)
            // Merge persisted secured entries with auto-lockable entries instead of replacing
            val mergedSecuredIds = securedEntryIds + autoLockableIds
            if (mergedSecuredIds != securedEntryIds) {
                securedEntryIds = mergedSecuredIds
                SettingsManager.saveSecuredEntryIds(context, securedEntryIds)
            }
        }
    }
    
    // Handle automatic locking when app goes to background
    DisposableEffect(Unit) {
        onDispose {
            // Automatically lock filled cells when leaving the app
            val autoLockableIds = EntryLockingHelper.getAutoLockableEntryIds(entries)
            if (autoLockableIds.isNotEmpty()) {
                SettingsManager.saveSecuredEntryIds(context, autoLockableIds)
            }
        }
    }
    
    // Use remember with keys to prevent unnecessary recalculations
    var viewPresets by remember { mutableStateOf(SettingsManager.getViewPresets(context)) }
    var colorSchemes by remember { mutableStateOf(SettingsManager.getColorSchemes(context)) }
    
    val scope = rememberCoroutineScope()

    fun saveUserView() {
        val currentSettings = SettingsManager.loadSettings(context)
        val newSettings = currentSettings.copy(
            savedUserScale = scale,
            savedUserWidthScale = widthScale
        )
        SettingsManager.saveSettings(context, newSettings)
        Toast.makeText(context, "Current view saved", Toast.LENGTH_SHORT).show()
    }

    fun loadUserView() {
        val settings = SettingsManager.loadSettings(context)
        settings.savedUserScale?.let { savedScale ->
            settings.savedUserWidthScale?.let { savedWidthScale ->
                scale = savedScale
                widthScale = savedWidthScale
                zoomPercentage = (savedScale * 100).toInt()

                val updatedSettings = settings.copy(
                    scale = savedScale,
                    widthScale = savedWidthScale,
                    zoomPercentage = (savedScale * 100).toInt()
                )
                SettingsManager.saveSettings(context, updatedSettings)
                Toast.makeText(context, "Saved view loaded", Toast.LENGTH_SHORT).show()
                return
            }
        }
        Toast.makeText(context, "No saved view found", Toast.LENGTH_SHORT).show()
    }

    fun saveCurrentViewAsPreset(name: String) {
        val newPreset = ViewPreset(name = name, scale = scale, widthScale = widthScale)
        val updated = viewPresets + newPreset
        SettingsManager.saveViewPresets(context, updated)
        viewPresets = updated
        Toast.makeText(context, "Preset saved", Toast.LENGTH_SHORT).show()
    }

    fun loadPreset(preset: ViewPreset) {
        scale = preset.scale
        widthScale = preset.widthScale
        zoomPercentage = (preset.scale * 100).toInt()
        val existing = SettingsManager.loadSettings(context)
        SettingsManager.saveSettings(context, existing.copy(
            scale = scale,
            widthScale = widthScale,
            zoomPercentage = zoomPercentage
        ))
        Toast.makeText(context, "Preset loaded", Toast.LENGTH_SHORT).show()
    }

    fun deletePreset(preset: ViewPreset) {
        val updated = viewPresets.filter { it.id != preset.id }
        SettingsManager.saveViewPresets(context, updated)
        viewPresets = updated
        Toast.makeText(context, "Preset deleted", Toast.LENGTH_SHORT).show()
    }


    // Preset save dialog
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (presetName.isNotBlank()) {
                        saveCurrentViewAsPreset(presetName.trim())
                        presetName = ""
                        showSavePresetDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showSavePresetDialog = false }) { Text("Cancel") } },
            title = { Text("Preset Name") },
            text = {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }

    // Preset Manager Dialog
    if (showPresetManagerDialog) {
        AlertDialog(
            onDismissRequest = { showPresetManagerDialog = false },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showPresetManagerDialog = false }) { Text("Close") } },
            title = { Text("View Settings & Presets") },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    // Zoom / width info
                    Text("Zoom: $zoomPercentage%", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { showSavePresetDialog = true }) {
                        Text("Save Current View")
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Saved Presets", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    viewPresets.forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    loadPreset(preset)
                                    showPresetManagerDialog = false
                                }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(preset.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            IconButton(onClick = { activeDeletePreset = preset }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete Preset")
                            }
                        }
                    }
                }
            }
        )
    }

        // ---- Delete Confirmation Dialog ----
    activeDeletePreset?.let { presetToDelete ->
        AlertDialog(
            onDismissRequest = { activeDeletePreset = null },
            confirmButton = {
                TextButton(onClick = {
                    deletePreset(presetToDelete)
                    activeDeletePreset = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { activeDeletePreset = null }) { Text("Cancel") } },
            title = { Text("Delete Preset") },
            text = { Text("Are you sure you want to delete \"${presetToDelete.name}\"?") }
        )
    }

    // FOCUS MANAGEMENT STATE
    var focusedCell by remember { mutableStateOf<Pair<Int, String>?>(null) }
    val focusRequesters = remember { mutableStateMapOf<Pair<Int, String>, FocusRequester>() }
    val cellTextFieldValues = remember { mutableStateMapOf<Pair<Int, String>, TextFieldValue>() }

    // FOOD PRESETS STATE
    var foodPresets by remember { mutableStateOf(PresetManager.getFoodPresets(context)) }
    var setFoodPresets by remember { mutableStateOf<(List<FoodPreset>) -> Unit>({ foodPresets = it }) }

    // Add helper function (move syncTextFieldValue above usages)
    fun syncTextFieldValue(tfv: TextFieldValue?, newText: String): TextFieldValue {
        return if (tfv == null || tfv.text != newText) TextFieldValue(newText) else tfv
    }

    // Use derivedStateOf to memoize cell text field values based on entries
    val memoizedCellValues = remember(entries) {
        derivedStateOf {
            val newValues = mutableMapOf<Pair<Int, String>, TextFieldValue>()
            entries.forEachIndexed { index, entry ->
                newValues[index to "morningBloodSugarLevel"] = syncTextFieldValue(cellTextFieldValues[index to "morningBloodSugarLevel"], entry.morningBloodSugarLevel)
                newValues[index to "afternoonBloodSugarLevel"] = syncTextFieldValue(cellTextFieldValues[index to "afternoonBloodSugarLevel"], entry.afternoonBloodSugarLevel)
                newValues[index to "eveningBloodSugarLevel"] = syncTextFieldValue(cellTextFieldValues[index to "eveningBloodSugarLevel"], entry.eveningBloodSugarLevel)
                newValues[index to "nightBloodSugarLevel"] = syncTextFieldValue(cellTextFieldValues[index to "nightBloodSugarLevel"], entry.nightBloodSugarLevel)
                newValues[index to "morningInsulin"] = syncTextFieldValue(cellTextFieldValues[index to "morningInsulin"], entry.morningInsulin)
                newValues[index to "afternoonInsulin"] = syncTextFieldValue(cellTextFieldValues[index to "afternoonInsulin"], entry.afternoonInsulin)
                newValues[index to "eveningInsulin"] = syncTextFieldValue(cellTextFieldValues[index to "eveningInsulin"], entry.eveningInsulin)
            }
            newValues
        }
    }
    
    // Update cell text field values when entries change
    LaunchedEffect(memoizedCellValues.value) {
        memoizedCellValues.value.forEach { (key, value) ->
            cellTextFieldValues[key] = value
        }
    }
    
    // We've moved saving entries to the onEntryChanged and onDeleteEntry handlers
    // to avoid unnecessary disk writes and UI blocking

    LaunchedEffect(isDarkMode) {
        val settings = SettingsManager.loadSettings(context)
        if (!settings.colorsModifiedByUser) {
            val (newFontColor, newBackgroundColor, newGridColor) = SettingsManager.getDefaultColors(isDarkMode)
            fontColor = newFontColor
            backgroundColor = newBackgroundColor
            gridColor = newGridColor
            SettingsManager.saveSettings(context, settings.copy(
                fontColor = newFontColor,
                backgroundColor = newBackgroundColor,
                gridColor = newGridColor,
                isDarkMode = isDarkMode
            ))
        } else {
            SettingsManager.saveSettings(context, settings.copy(
                isDarkMode = isDarkMode
            ))
        }
    }

    if (showColorSchemeDialog) {
        Dialog(onDismissRequest = { showColorSchemeDialog = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Color Schemes", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))

                    // Dark Mode Schemes
                    Text(
                        "Dark Mode Schemes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    val darkSchemes = colorSchemes.filter { it.isDarkMode }
                    if (darkSchemes.isEmpty()) {
                        Text(
                            "No dark mode schemes saved",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        darkSchemes.forEach { scheme ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(scheme.name)
                                Row {
                                    IconButton(onClick = {
                                        fontColor = scheme.fontColor
                                        backgroundColor = scheme.backgroundColor
                                        gridColor = scheme.gridColor
                                        isDarkMode = true
                                        SettingsManager.saveSettings(context, AppSettings(
                                            fontColor = scheme.fontColor,
                                            backgroundColor = scheme.backgroundColor,
                                            gridColor = scheme.gridColor,
                                            insulinColumnColor = insulinColumnColor,
                                            isDarkMode = true,
                                            language = selectedLanguage,
                                            colorsModifiedByUser = true
                                        ))
                                        showColorSchemeDialog = false
                                    }) {
                                        Icon(Icons.Filled.Check, "Apply Scheme")
                                    }
                                    IconButton(onClick = {
                                        SettingsManager.deleteColorScheme(context, scheme.id)
                                        colorSchemes = SettingsManager.getColorSchemes(context)
                                    }) {
                                        Icon(Icons.Filled.Delete, "Delete Scheme")
                                    }
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Light Mode Schemes
                    Text(
                        "Light Mode Schemes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    val lightSchemes = colorSchemes.filter { !it.isDarkMode }
                    if (lightSchemes.isEmpty()) {
                        Text(
                            "No light mode schemes saved",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        lightSchemes.forEach { scheme ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(scheme.name)
                                Row {
                                    IconButton(onClick = {
                                        // Update the color state variables
                                        fontColor = scheme.fontColor
                                        backgroundColor = scheme.backgroundColor
                                        gridColor = scheme.gridColor
                                        isDarkMode = false
                                        
                                        // Save the settings
                                        SettingsManager.saveSettings(context, AppSettings(
                                            fontColor = scheme.fontColor,
                                            backgroundColor = scheme.backgroundColor,
                                            gridColor = scheme.gridColor,
                                            insulinColumnColor = insulinColumnColor,
                                            isDarkMode = false,
                                            language = selectedLanguage,
                                            colorsModifiedByUser = true
                                        ))
                                        
                                        // Close the dialog
                                        showColorSchemeDialog = false
                                    }) {
                                        Icon(Icons.Filled.Check, "Apply Scheme")
                                    }
                                    IconButton(onClick = {
                                        SettingsManager.deleteColorScheme(context, scheme.id)
                                        colorSchemes = SettingsManager.getColorSchemes(context)
                                    }) {
                                        Icon(Icons.Filled.Delete, "Delete Scheme")
                                    }
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Button(
                        onClick = { 
                            showColorSchemeDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text("Save Current Colors as Scheme")
                    }
                }
            }
        }
    }

    if (showChart) {
        TimeChartDialog(onDismissRequest = { showChart = false }, entries = entries.filter { it.date.isNotBlank() })
    }

    if (showAdvancedSettings) {
        AdvancedSettingsDialog(
            onDismissRequest = { 
                showAdvancedSettings = false
                // Refresh auto-save setting when dialog closes
                autoSaveOnExit = SettingsManager.loadAutoSaveOnExit(context)
            },
            settings = SettingsManager.loadSettings(context),
            onSettingsChanged = { newSettings ->
                SettingsManager.saveSettings(context, newSettings)
                scale = newSettings.scale
                widthScale = newSettings.widthScale
                zoomPercentage = newSettings.zoomPercentage
                // Refresh auto-save setting when settings change
                autoSaveOnExit = SettingsManager.loadAutoSaveOnExit(context)
            }
        )
    }

    LaunchedEffect(Unit) {
        SettingsManager.saveSettings(context, SettingsManager.loadSettings(context))
    }

    LaunchedEffect(isDarkMode, fontColor, backgroundColor, gridColor, selectedLanguage, scale, widthScale, zoomPercentage) {
        val existing = SettingsManager.loadSettings(context)
        val merged = existing.copy(
            fontColor = fontColor,
            backgroundColor = backgroundColor,
            gridColor = gridColor,
            isDarkMode = isDarkMode,
            language = selectedLanguage,
            colorsModifiedByUser = true,
            scale = scale,
            widthScale = widthScale,
            zoomPercentage = zoomPercentage
        )
        SettingsManager.saveSettings(context, merged)
    }

    DiabetesTrackerTheme(
        darkTheme = isDarkMode
    ) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val drawerListState = rememberLazyListState()
        val viewSettingsRequester = remember { BringIntoViewRequester() }
        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(16.dp)
                    ) {
                        item {
                            Text(
                                "Settings",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Save Button
                            // Note: Save All Changes button removed - locking is now automatic
                            
                            // Export PDF Button
                            Button(
                                onClick = {
                                    // Launch file picker to save PDF
                                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_TITLE, "diabetes_data_${System.currentTimeMillis()}.pdf")
                                    }
                                    filePickerLauncher.launch(intent)
                                    scope.launch {
                                        drawerState.close()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Text("Export as PDF")
                            }
                            
                            // Export TXT Button
                            Button(
                                onClick = {
                                    // Launch file picker to save TXT
                                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TITLE, "diabetes_data_${System.currentTimeMillis()}.txt")
                                    }
                                    txtFilePickerLauncher.launch(intent)
                                    scope.launch {
                                        drawerState.close()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Text("Export as TXT")
                            }

                            // Language Settings
                            Text(
                                "Language",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("English", "German", "Spanish").forEach { lang ->
                                    TextButton(
                                        onClick = { 
                                            selectedLanguage = lang
                                            scope.launch { drawerState.close() }
                                        }
                                    ) {
                                        Text(
                                            when (lang) {
                                                "German" -> "🇩🇪"
                                                "Spanish" -> "🇪🇸"
                                                else -> "🇬🇧"
                                            }
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Theme Settings
                            Text(
                                "Theme",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Dark Mode")
                                Switch(
                                    checked = isDarkMode,
                                    onCheckedChange = { isDarkMode = it }
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Color Settings
                            Text(
                                "Colors",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            // Color management buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = {
                                        val (newFontColor, newBackgroundColor, newGridColor, newInsulinColumnColor) = SettingsManager.getDefaultColors(isDarkMode)
                                        fontColor = newFontColor
                                        backgroundColor = newBackgroundColor
                                        gridColor = newGridColor
                                        insulinColumnColor = newInsulinColumnColor

                                        SettingsManager.saveSettings(context, AppSettings(
                                            fontColor = newFontColor,
                                            backgroundColor = newBackgroundColor,
                                            gridColor = newGridColor,
                                            insulinColumnColor = newInsulinColumnColor,
                                            isDarkMode = isDarkMode,
                                            language = selectedLanguage,
                                            colorsModifiedByUser = false,
                                            scale = scale, // Preserve current zoom
                                            widthScale = widthScale, // Preserve current width scale
                                            zoomPercentage = zoomPercentage // Preserve current zoom percentage
                                        ))
                                    },
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                ) {
                                    Text("Reset to Defaults")
                                }

                                Button(
                                    onClick = { showColorSchemeDialog = true },
                                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                                ) {
                                    Text("Load Color Schemes")
                                }
                            }

                            Button(
                                onClick = { showColorSchemeDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text("Save Current Colors as Scheme")
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            // Color pickers
                            ColorPicker(
                                title = "Font Color",
                                color = fontColor,
                                onColorChange = { newColor ->
                                    fontColor = newColor
                                    val currentSettings = SettingsManager.loadSettings(context)
                                    SettingsManager.saveSettings(context, currentSettings.copy(
                                        fontColor = newColor,
                                        colorsModifiedByUser = true
                                    ))
                                }
                            )

                            ColorPicker(
                                title = "Background Color",
                                color = backgroundColor,
                                onColorChange = { newColor ->
                                    backgroundColor = newColor
                                    val currentSettings = SettingsManager.loadSettings(context)
                                    SettingsManager.saveSettings(context, currentSettings.copy(
                                        backgroundColor = newColor,
                                        colorsModifiedByUser = true
                                    ))
                                }
                            )

                            ColorPicker(
                                title = "Grid Color",
                                color = gridColor,
                                onColorChange = { newColor ->
                                    gridColor = newColor
                                    val currentSettings = SettingsManager.loadSettings(context)
                                    SettingsManager.saveSettings(context, currentSettings.copy(
                                        gridColor = newColor,
                                        colorsModifiedByUser = true
                                    ))
                                }
                            )
                            
                            ColorPicker(
                                title = "Insulin Column Color",
                                color = insulinColumnColor,
                                onColorChange = { newColor ->
                                    insulinColumnColor = newColor
                                    val currentSettings = SettingsManager.loadSettings(context)
                                    SettingsManager.saveSettings(context, currentSettings.copy(
                                        insulinColumnColor = newColor,
                                        colorsModifiedByUser = true
                                    ))
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Backup & Restore Section
                            Text(
                                "Backup & Restore",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            // Export Backup Button
                            Button(
                                onClick = { showBackupExportDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(
                                    Icons.Default.Upload,
                                    contentDescription = "Export Backup",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export Backup")
                            }
                            
                            // Import Backup Button
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "*/*"
                                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/plain"))
                                    }
                                    backupImportLauncher.launch(intent)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = "Import Backup",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Import Backup")
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Button(
                                onClick = { showAdvancedSettings = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text("Advanced Settings")
                            }
                        } // End of the first big item

                        item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

                        // View Settings and Saved Presets sections removed from sidebar
                    } // End of LazyColumn
                } // End of ModalDrawerSheet
            },
            drawerState = drawerState
        ) {
            Scaffold(
                topBar = {
                    // Custom fixed-height top bar to prevent jumping/stretching
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp), // Fixed height for stability
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Menu button
                            IconButton(
                                onClick = { 
                                    scope.launch {
                                        drawerState.open()
                                        viewSettingsRequester.bringIntoView()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            
                            // Right-side actions
                            Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                IconButton(
                                    onClick = { showZoomControls = !showZoomControls }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ZoomIn,
                                        contentDescription = "Zoom Controls",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                DropdownMenu(
                                    expanded = showZoomControls,
                                    onDismissRequest = { showZoomControls = false },
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            IconButton(
                                                onClick = {
                                                  showPresetManagerDialog = true
                                                  showZoomControls = false
                                                }
                                            ) {
                                                Icon(Icons.Filled.Bookmark, "Preset Management")
                                            }
                                            
                                            IconButton(
                                                onClick = {
                                                    scale = 1.0f
                                                    zoomPercentage = 100
                                                    widthScale = 2.2f
                                                },
                                                modifier = Modifier.size(36.dp) // Smaller IconButton
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Refresh, 
                                                    contentDescription = "Reset Zoom and Width",
                                                    modifier = Modifier.size(20.dp) // Smaller Icon
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        // Reset button moved to row with bookmark icon
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Zoom: $zoomPercentage%",
                                            modifier = Modifier.align(Alignment.CenterHorizontally).clickable {
                                                scale = 1.0f
                                                zoomPercentage = 100
                                            }
                                        )
                                        Row(
                                            modifier = Modifier.align(Alignment.CenterHorizontally),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    val newScale = (scale - 0.1f).coerceIn(0.5f, 3f)
                                                    if (newScale != scale) {
                                                        scale = newScale
                                                        zoomPercentage = (scale * 100).toInt()
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Filled.Remove, "Zoom Out")
                                            }
                                            IconButton(
                                                onClick = {
                                                    val newScale = (scale + 0.1f).coerceIn(0.5f, 3f)
                                                    if (newScale != scale) {
                                                        scale = newScale
                                                        zoomPercentage = (scale * 100).toInt()
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Filled.Add, "Zoom In")
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Width: ${(widthScale * 100).toInt()}%",
                                            modifier = Modifier.align(Alignment.CenterHorizontally).clickable {
                                                widthScale = 2.2f
                                            }
                                        )
                                        Row(
                                            modifier = Modifier.align(Alignment.CenterHorizontally),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    widthScale = (widthScale - 0.1f).coerceIn(1f, 3f)
                                                }
                                            ) {
                                                Icon(Icons.Filled.Remove, "Decrease Width")
                                            }
                                            IconButton(
                                                onClick = {
                                                    widthScale = (widthScale + 0.1f).coerceIn(1f, 3f)
                                                }
                                            ) {
                                                Icon(Icons.Filled.Add, "Increase Width")
                                            }
                                        }
                                    }
                                }
                            }
                            IconButton(onClick = { showChart = true }) {
                                Icon(
                                    Icons.Default.Timeline, 
                                    contentDescription = "Show Chart",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    val configuration = LocalConfiguration.current
                    val baseWidth = configuration.screenWidthDp.dp
                    val scrollState = rememberScrollState()
                    
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(scrollState)
                    ) {
                        Column(
                            modifier = Modifier
                                .width(baseWidth * widthScale * scale) // Adjust width for zoom
                                .background(backgroundColor)
                                
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    // Adjust transform origin to top-left to prevent content shifting
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                                }
                        ) {
                            val labels = when (selectedLanguage) {
                                "German" -> mapOf(
                                    "date" to "Datum",
                                    "bloodSugarControl" to "Blutzuckerkontrolle",
                                    "morning" to "Morgen",
                                    "afternoon" to "Mittag",
                                    "evening" to "Abend",
                                    "night" to "Nacht",
                                    "insulin" to "Insulin",
                                    "remarks" to "Bemerkungen"
                                )
                                "Spanish" -> mapOf(
                                    "date" to "Fecha",
                                    "bloodSugarControl" to "Control de Azúcar",
                                    "morning" to "Mañana",
                                    "afternoon" to "Mediodía",
                                    "evening" to "Tarde",
                                    "night" to "Noche",
                                    "insulin" to "Insulina",
                                    "remarks" to "Notas"
                                )
                                else -> mapOf(
                                    "date" to "Date",
                                    "bloodSugarControl" to "Blood Sugar Control",
                                    "morning" to "Morning",
                                    "afternoon" to "Afternoon",
                                    "evening" to "Evening",
                                    "night" to "Night",
                                    "insulin" to "Insulin",
                                    "remarks" to "Remarks"
                                )
                            }
                            
                            TableHeader(labels, fontColor, gridColor, insulinColumnColor)
                            // Use a stable key for LazyColumn items and remember the filtered entries
                            val entriesWithIndex = remember(entries, selectedDateFilter, customStartDate, customEndDate, securedEntryIds, showEmptyDatesInFilter, specificDate, flexibleNumber, flexibleUnit) {
                                println("\n=== FILTERING DEBUG START ===")
                                println("DEBUG TABLE: selectedDateFilter = $selectedDateFilter")
                                println("DEBUG TABLE: entries.size = ${entries.size}")
                                println("DEBUG TABLE: customStartDate = $customStartDate")
                                println("DEBUG TABLE: customEndDate = $customEndDate")
                                
                                // Show first few original entries
                                println("DEBUG TABLE: First 3 original entries:")
                                entries.take(3).forEach { entry ->
                                    println("  - Original entry: ${entry.date}")
                                }
                                
                                // PROPER DATE FILTERING: Parse DD.MM.YYYY format and use real date comparison
                                val filteredEntriesWithOriginalIndex = if (selectedDateFilter != DateFilter.ALL) {
                                    println("=== FILTERING DEBUG START ===")
                                    println("DEBUG FILTER: selectedDateFilter = $selectedDateFilter")
                                    println("DEBUG FILTER: customStartDate = $customStartDate, customEndDate = $customEndDate")
                                    println("DEBUG FILTER: specificDate = $specificDate")
                                    println("DEBUG FILTER: showEmptyDatesInFilter = $showEmptyDatesInFilter")
                                    println("DEBUG FILTER: Total entries before filtering = ${entries.size}")
                                    
                                    // Use DateFilterHelper for consistent filtering logic
                                    val filteredEntries = DateFilterHelper.filterEntriesByDate(
                                        entries = entries,
                                        dateFilter = selectedDateFilter,
                                        customStartDate = customStartDate,
                                        customEndDate = customEndDate,
                                        specificDate = specificDate,
                                        flexibleNumber = flexibleNumber,
                                        flexibleUnit = flexibleUnit
                                    )
                                    
                                    // Filter entries but keep track of original indices and handle empty dates
                                    val result = entries.mapIndexedNotNull { originalIndex, entry ->
                                        if (entry.date.isBlank()) {
                                            val includeEmpty = showEmptyDatesInFilter
                                            println("DEBUG FILTER: Empty date, showEmptyDatesInFilter=$showEmptyDatesInFilter, including=$includeEmpty")
                                            return@mapIndexedNotNull if (includeEmpty) originalIndex to entry else null
                                        }
                                        
                                        // Check if this entry is in the filtered results
                                        val isIncluded = filteredEntries.any { it.id == entry.id }
                                        println("DEBUG FILTER: Entry date='${entry.date}', included=$isIncluded")
                                        
                                        if (isIncluded) {
                                            originalIndex to entry
                                        } else {
                                            null
                                        }
                                    }
                                    println("DEBUG TABLE: Filter applied, result size = ${result.size}")
                                    result
                                } else {
                                    println("DEBUG TABLE: No filter applied (showing ALL)")
                                    entries.mapIndexed { index, entry -> index to entry } // Show all with original indices
                                }
                                
                                println("DEBUG TABLE: Final filteredEntries.size = ${filteredEntriesWithOriginalIndex.size}")
                                println("DEBUG TABLE: Final filtered entries:")
                                filteredEntriesWithOriginalIndex.forEach { (originalIndex, entry) ->
                                    println("  - Filtered entry: ${entry.date} (originalIndex=$originalIndex)")
                                }
                                println("=== FILTERING DEBUG END ===\n")
                                
                                // Return entries with their original indices preserved
                                filteredEntriesWithOriginalIndex
                            }
                            
                            LazyColumn {
                                items(
                                    items = entriesWithIndex,
                                    // Use the original entry index as the stable key
                                    key = { (originalIndex, _) -> originalIndex }
                                ) { (originalIndex, entry) ->
                                    TableRow(
                                        entry = entry,
                                        entryIndex = originalIndex, // Use original index, not filtered index
                                        labels = labels,
                                        isSecured = securedEntryIds.contains(entry.id),
                                        confirmedEditingSessions = confirmedEditingSessions,
                                        onShowEditConfirmDialog = { cellKey, action ->
                                            pendingEditAction = {
                                                action()
                                                confirmedEditingSessions = confirmedEditingSessions + cellKey
                                            }
                                            showEditConfirmDialog = true
                                        },
                                        onEntryChanged = { originalIndex, newEntry -> 
                            // Use immutable list operations to update entries
                            // This is more efficient than creating a new mutable list each time
                            entries = entries.mapIndexed { i, entry ->
                                if (i == originalIndex) newEntry else entry
                            }
                            // Mark as having unsaved changes
                            hasUnsavedChanges = true
                            // Auto-save immediately on every change
                            DataManager.saveEntries(context, entries)
                        },
                                        onDeleteEntry = { originalIndex ->
                            // Use immutable list operations to remove entries
                            entries = entries.filterIndexed { i, _ -> i != originalIndex }
                            
                            // Clean up cached cell values for the deleted entry
                            val keysToRemove = cellTextFieldValues.keys.filter { it.first == originalIndex }
                            keysToRemove.forEach { cellTextFieldValues.remove(it) }
                            
                            // Clean up focus requesters for the deleted entry
                            val focusKeysToRemove = focusRequesters.keys.filter { it.first == originalIndex }
                            focusKeysToRemove.forEach { focusRequesters.remove(it) }
                            
                            // Update indices for remaining entries (shift down)
                            val updatedCellValues = mutableMapOf<Pair<Int, String>, TextFieldValue>()
                            val updatedFocusRequesters = mutableMapOf<Pair<Int, String>, FocusRequester>()
                            
                            cellTextFieldValues.forEach { (key, value) ->
                                val (index, field) = key
                                if (index > originalIndex) {
                                    updatedCellValues[index - 1 to field] = value
                                } else if (index < originalIndex) {
                                    updatedCellValues[key] = value
                                }
                            }
                            
                            focusRequesters.forEach { (key, value) ->
                                val (index, field) = key
                                if (index > originalIndex) {
                                    updatedFocusRequesters[index - 1 to field] = value
                                } else if (index < originalIndex) {
                                    updatedFocusRequesters[key] = value
                                }
                            }
                            
                            cellTextFieldValues.clear()
                            cellTextFieldValues.putAll(updatedCellValues)
                            focusRequesters.clear()
                            focusRequesters.putAll(updatedFocusRequesters)
                            
                            // Clear focused cell if it was in the deleted entry
                            if (focusedCell?.first == originalIndex) {
                                focusedCell = null
                            } else if (focusedCell?.first != null && focusedCell!!.first > originalIndex) {
                                // Adjust focused cell index if it's after the deleted entry
                                focusedCell = (focusedCell!!.first - 1) to focusedCell!!.second
                            }
                            
                            // Mark as having unsaved changes
                            hasUnsavedChanges = true
                            // Auto-save immediately on every change
                            DataManager.saveEntries(context, entries)
                        },
                                        fontColor = fontColor,
                                        backgroundColor = backgroundColor,
                                        gridColor = gridColor,
                                        insulinColumnColor = insulinColumnColor,
                                        focusedCell = focusedCell,
                                        focusRequesters = focusRequesters,
                                        onRequestFocus = { focusedCell = it },
                                        cellTextFieldValues = cellTextFieldValues,
                                        foodPresets = foodPresets,
                                        setFoodPresets = setFoodPresets
                                    )
                                }
                            }
                        }
                    }

                    // Reset position button (new)
                    if (scale != 1f || offset != Offset.Zero) {
                        IconButton(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                                zoomPercentage = 100
                            },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Filled.Remove, "Reset View")
                        }
                    }

                    // Date Filter Button
                    FloatingActionButton(
                        onClick = {
                            showDateFilterDialog = true
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 88.dp, bottom = 16.dp), // Position to the left of Add button
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Filter entries by date",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Add Entry Button
                    FloatingActionButton(
                        onClick = {
                            // Create new entry with current date
                            val today = java.time.LocalDate.now()
                            val formattedDate = "${today.dayOfMonth.toString().padStart(2, '0')}.${today.monthValue.toString().padStart(2, '0')}.${today.year}"
                            entries = entries + DiabetesEntry(date = formattedDate)
                            hasUnsavedChanges = true
                            // Auto-save immediately when adding new entry
                            DataManager.saveEntries(context, entries)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Entry"
                        )
                    }
                }
            }
        }
        
        // Confirmation dialog for editing secured data
        if (showEditConfirmDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {
                    showEditConfirmDialog = false
                    pendingEditAction = null
                },
                title = {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Gespeicherte Daten bearbeiten?"
                            "Spanish" -> "¿Editar datos guardados?"
                            else -> "Edit Saved Data?"
                        }
                    )
                },
                text = {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Diese Daten wurden bereits gespeichert. Möchten Sie sie wirklich bearbeiten?"
                            "Spanish" -> "Estos datos ya han sido guardados. ¿Realmente desea editarlos?"
                            else -> "This data has already been saved. Do you really want to edit it?"
                        }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pendingEditAction?.invoke()
                            showEditConfirmDialog = false
                            pendingEditAction = null
                        }
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Ja, bearbeiten"
                                "Spanish" -> "Sí, editar"
                                else -> "Yes, Edit"
                            }
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showEditConfirmDialog = false
                            pendingEditAction = null
                        }
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Abbrechen"
                                "Spanish" -> "Cancelar"
                                else -> "Cancel"
                            }
                        )
                    }
                }
            )
        }
        
        // Exit confirmation dialog when user has unsaved changes
        if (showExitConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showExitConfirmDialog = false },
                title = {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Ungespeicherte Änderungen"
                            "Spanish" -> "Cambios no guardados"
                            else -> "Unsaved Changes"
                        }
                    )
                },
                text = {
                    Column {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Sie haben ungespeicherte Änderungen. Möchten Sie alle Änderungen speichern?"
                                "Spanish" -> "Tienes cambios sin guardar. ¿Quieres guardar todos los cambios?"
                                else -> "You have unsaved changes. Do you want to save all changes?"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "💡 Tipp: In den erweiterten Einstellungen können Sie 'Automatisch speichern beim Beenden' aktivieren, um diese Abfrage zu überspringen."
                                "Spanish" -> "💡 Consejo: En configuración avanzada puedes activar 'Guardar automáticamente al salir' para omitir esta pregunta."
                                else -> "💡 Tip: In Advanced Settings you can enable 'Auto-save on exit' to skip this dialog."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                confirmButton = {
                    // Custom three-button layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Close without saving button
                        TextButton(
                            onClick = {
                                hasUnsavedChanges = false
                                confirmedEditingSessions = emptySet()
                                showExitConfirmDialog = false
                                (context as? Activity)?.finish()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Ohne Speichern schließen"
                                    "Spanish" -> "Cerrar sin guardar"
                                    else -> "Close without saving"
                                },
                                fontSize = 12.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Cancel button
                        TextButton(
                            onClick = { showExitConfirmDialog = false }
                        ) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Abbrechen"
                                    "Spanish" -> "Cancelar"
                                    else -> "Cancel"
                                }
                            )
                        }
                        
                        // Save All Changes button
                        TextButton(
                            onClick = {
                                // Save all changes
                                DataManager.saveEntries(context, entries)
                                SettingsManager.saveSettings(context, AppSettings(
                                    fontColor = fontColor,
                                    backgroundColor = backgroundColor,
                                    gridColor = gridColor,
                                    insulinColumnColor = insulinColumnColor,
                                    isDarkMode = isDarkMode,
                                    language = selectedLanguage,
                                    colorsModifiedByUser = true,
                                    scale = scale,
                                    widthScale = widthScale,
                                    zoomPercentage = zoomPercentage
                                ))
                                
                                // Mark entries with data as secured
                                val entriesWithData = entries.filter { entry ->
                                    entry.date.isNotBlank() && (
                                        entry.morningBloodSugarLevel.isNotBlank() ||
                                        entry.afternoonBloodSugarLevel.isNotBlank() ||
                                        entry.eveningBloodSugarLevel.isNotBlank() ||
                                        entry.nightBloodSugarLevel.isNotBlank() ||
                                        entry.morningInsulin.isNotBlank() ||
                                        entry.afternoonInsulin.isNotBlank() ||
                                        entry.eveningInsulin.isNotBlank() ||
                                        entry.remarks.isNotBlank() ||
                                        entry.foodEntriesByColumn.values.any { it.isNotEmpty() }
                                    )
                                }
                                securedEntryIds = entriesWithData.map { it.id }.toSet()
                                SettingsManager.saveSecuredEntryIds(context, securedEntryIds)
                                
                                hasUnsavedChanges = false
                                confirmedEditingSessions = emptySet()
                                showExitConfirmDialog = false
                                
                                Toast.makeText(context, "All changes saved!", Toast.LENGTH_SHORT).show()
                                
                                // Close the app after saving
                                (context as? Activity)?.finish()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Speichern & Schließen"
                                    "Spanish" -> "Guardar y cerrar"
                                    else -> "Save & Close"
                                }
                            )
                        }
                    }
                },
                dismissButton = {}
            )
        }
        
        // Exit snackbar for double-tap confirmation when auto-save is enabled
        if (showExitSnackbar) {
            LaunchedEffect(showExitSnackbar) {
                // Position snackbar at bottom of screen
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Nochmals zurück drücken zum Speichern und Beenden"
                            "Spanish" -> "Presiona atrás nuevamente para guardar y salir"
                            else -> "Press back again to save and exit"
                        },
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Backup Export Dialog
        if (showBackupExportDialog) {
            AlertDialog(
                onDismissRequest = { showBackupExportDialog = false },
                title = {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Backup exportieren"
                            "Spanish" -> "Exportar copia de seguridad"
                            else -> "Export Backup"
                        }
                    )
                },
                text = {
                    Column {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Was möchten Sie exportieren?"
                                "Spanish" -> "¿Qué desea exportar?"
                                else -> "What would you like to export?"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Backup type selection
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedBackupType = BackupType.ALL }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedBackupType == BackupType.ALL,
                                    onClick = { selectedBackupType = BackupType.ALL }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = when (selectedLanguage) {
                                            "German" -> "Alles (Empfohlen)"
                                            "Spanish" -> "Todo (Recomendado)"
                                            else -> "Everything (Recommended)"
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = when (selectedLanguage) {
                                            "German" -> "Einstellungen, Daten & Presets"
                                            "Spanish" -> "Configuración, datos y presets"
                                            else -> "Settings, data & presets"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedBackupType = BackupType.SETTINGS_ONLY }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedBackupType == BackupType.SETTINGS_ONLY,
                                    onClick = { selectedBackupType = BackupType.SETTINGS_ONLY }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = when (selectedLanguage) {
                                            "German" -> "Nur Einstellungen"
                                            "Spanish" -> "Solo configuración"
                                            else -> "Settings Only"
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = when (selectedLanguage) {
                                            "German" -> "Farben, Zoom, Sprache, etc."
                                            "Spanish" -> "Colores, zoom, idioma, etc."
                                            else -> "Colors, zoom, language, etc."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedBackupType = BackupType.DATA_ONLY }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedBackupType == BackupType.DATA_ONLY,
                                    onClick = { selectedBackupType = BackupType.DATA_ONLY }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = when (selectedLanguage) {
                                            "German" -> "Nur Daten"
                                            "Spanish" -> "Solo datos"
                                            else -> "Data Only"
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = when (selectedLanguage) {
                                            "German" -> "Diabetes-Einträge & Lebensmittel"
                                            "Spanish" -> "Entradas de diabetes y alimentos"
                                            else -> "Diabetes entries & food items"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Create backup and launch file picker
                            pendingBackupData = BackupManager.createBackup(context, selectedBackupType)
                            showBackupExportDialog = false
                            
                            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/json"
                                val fileName = when (selectedBackupType) {
                                    BackupType.SETTINGS_ONLY -> "DiabetesTracker_Settings_Backup.diabackup"
                                    BackupType.DATA_ONLY -> "DiabetesTracker_Data_Backup.diabackup"
                                    BackupType.ALL -> "DiabetesTracker_Complete_Backup.diabackup"
                                }
                                putExtra(Intent.EXTRA_TITLE, fileName)
                            }
                            backupExportLauncher.launch(intent)
                        }
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Exportieren"
                                "Spanish" -> "Exportar"
                                else -> "Export"
                            }
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showBackupExportDialog = false }
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Abbrechen"
                                "Spanish" -> "Cancelar"
                                else -> "Cancel"
                            }
                        )
                    }
                }
            )
        }
        
        // Backup Import Preview Dialog
        if (showImportPreviewDialog && selectedBackupData != null) {
            val backupData = selectedBackupData!!
            val metadata = BackupManager.getBackupMetadata(backupData)
            
            AlertDialog(
                onDismissRequest = { 
                    showImportPreviewDialog = false
                    selectedBackupData = null
                },
                title = {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Backup importieren"
                            "Spanish" -> "Importar copia de seguridad"
                            else -> "Import Backup"
                        }
                    )
                },
                text = {
                    Column {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Backup-Inhalt:"
                                "Spanish" -> "Contenido del backup:"
                                else -> "Backup contains:"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Show backup contents
                        if (metadata.settingsCount > 0) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "✅ App-Einstellungen"
                                    "Spanish" -> "✅ Configuración de la app"
                                    else -> "✅ App Settings"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        
                        if (metadata.entriesCount > 0) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "✅ ${metadata.entriesCount} Diabetes-Einträge"
                                    "Spanish" -> "✅ ${metadata.entriesCount} entradas de diabetes"
                                    else -> "✅ ${metadata.entriesCount} diabetes entries"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        
                        if (metadata.foodPresetsCount > 0) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "✅ ${metadata.foodPresetsCount} Lebensmittel-Presets"
                                    "Spanish" -> "✅ ${metadata.foodPresetsCount} presets de alimentos"
                                    else -> "✅ ${metadata.foodPresetsCount} food presets"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        
                        if (metadata.colorSchemesCount > 0) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "✅ ${metadata.colorSchemesCount} Farbschemata"
                                    "Spanish" -> "✅ ${metadata.colorSchemesCount} esquemas de color"
                                    else -> "✅ ${metadata.colorSchemesCount} color schemes"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        
                        if (metadata.viewPresetsCount > 0) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "✅ ${metadata.viewPresetsCount} Ansichts-Presets"
                                    "Spanish" -> "✅ ${metadata.viewPresetsCount} presets de vista"
                                    else -> "✅ ${metadata.viewPresetsCount} view presets"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Import options
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mergeWithExisting = !mergeWithExisting }
                        ) {
                            Checkbox(
                                checked = mergeWithExisting,
                                onCheckedChange = { mergeWithExisting = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Mit vorhandenen Daten zusammenführen"
                                        "Spanish" -> "Combinar con datos existentes"
                                        else -> "Merge with existing data"
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Vorhandene Daten bleiben erhalten"
                                        "Spanish" -> "Los datos existentes se conservan"
                                        else -> "Existing data will be preserved"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            BackupManager.importBackup(context, backupData, mergeWithExisting)
                            showImportPreviewDialog = false
                            selectedBackupData = null
                            
                            // Refresh UI state after import
                            entries = DataManager.getEntries(context)
                            securedEntryIds = SettingsManager.loadSecuredEntryIds(context)
                            val newSettings = SettingsManager.loadSettings(context)
                            fontColor = newSettings.fontColor
                            backgroundColor = newSettings.backgroundColor
                            gridColor = newSettings.gridColor
                            isDarkMode = newSettings.isDarkMode
                            selectedLanguage = newSettings.language
                            
                            // Refresh presets after import
                            colorSchemes = SettingsManager.getColorSchemes(context)
                        }
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Importieren"
                                "Spanish" -> "Importar"
                                else -> "Import"
                            }
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showImportPreviewDialog = false
                            selectedBackupData = null
                        }
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Abbrechen"
                                "Spanish" -> "Cancelar"
                                else -> "Cancel"
                            }
                        )
                    }
                }
            )
        }
        
        // Date Filter Dialog
        if (showDateFilterDialog) {
            AlertDialog(
                onDismissRequest = { showDateFilterDialog = false },
                title = {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Einträge filtern"
                            "Spanish" -> "Filtrar entradas"
                            else -> "Filter Entries"
                        }
                    )
                },
                text = {
                    Column {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Wählen Sie einen Zeitraum:"
                                "Spanish" -> "Selecciona un período de tiempo:"
                                else -> "Select a time period:"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Current filter description
                        Text(
                            text = DateFilterHelper.getFilterDescription(selectedDateFilter, customStartDate, customEndDate, specificDate, flexibleNumber, flexibleUnit),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Filter options with new flexible selector
                        Column {
                            // All Entries
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedDateFilter = DateFilter.ALL
                                        SettingsManager.saveLastDateFilter(
                                            context = context,
                                            dateFilter = selectedDateFilter,
                                            customStartDate = customStartDate,
                                            customEndDate = customEndDate,
                                            showEmptyDates = showEmptyDatesInFilter,
                                            flexibleNumber = flexibleNumber,
                                            flexibleUnit = flexibleUnit
                                        )
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDateFilter == DateFilter.ALL,
                                    onClick = {
                                        selectedDateFilter = DateFilter.ALL
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Alle Einträge"
                                        "Spanish" -> "Todas las entradas"
                                        else -> "All Entries"
                                    }
                                )
                            }
                            
                            // Flexible Range
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedDateFilter = DateFilter.FLEXIBLE
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDateFilter == DateFilter.FLEXIBLE,
                                    onClick = {
                                        selectedDateFilter = DateFilter.FLEXIBLE
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Letzte"
                                        "Spanish" -> "Últimos"
                                        else -> "Last"
                                    }
                                )
                            }
                            
                            // Flexible Range Selectors
                            if (selectedDateFilter == DateFilter.FLEXIBLE) {
                                // Create stable states outside the UI to prevent recreation
                                val numberListState = rememberLazyListState(
                                    initialFirstVisibleItemIndex = (flexibleNumber - 1).coerceIn(0, 30)
                                )
                                val unitListState = rememberLazyListState(
                                    initialFirstVisibleItemIndex = TimeUnit.values().indexOf(flexibleUnit).coerceIn(0, TimeUnit.values().size - 1)
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 32.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    
                                    // Number selector (1-31) with perfect centering
                                    Box(
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(120.dp)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                                    ) {
                                        var isSnapping by remember { mutableStateOf(false) }
                                        
                                        // Active snap detection - monitors when selection frame is between values
                                        LaunchedEffect(numberListState.isScrollInProgress) {
                                            if (!numberListState.isScrollInProgress && !isSnapping) {
                                                // Wait for scroll to fully settle
                                                kotlinx.coroutines.delay(150)
                                                val layout = numberListState.layoutInfo
                                                if (layout.visibleItemsInfo.isNotEmpty()) {
                                                    val frameTop = layout.viewportStartOffset + 40 // Account for top padding
                                                    val frameBottom = frameTop + 40 // Frame height
                                                    val frameCenter = (frameTop + frameBottom) / 2
                                                    
                                                    // Find item that should be centered in frame
                                                    val targetItem = layout.visibleItemsInfo.minByOrNull { item ->
                                                        kotlin.math.abs((item.offset + item.size / 2) - frameCenter)
                                                    }
                                                    
                                                    if (targetItem != null) {
                                                        val itemCenter = targetItem.offset + targetItem.size / 2
                                                        val distanceFromCenter = kotlin.math.abs(itemCenter - frameCenter)
                                                        
                                                        // If item is not properly centered (threshold of 5px), snap it
                                                        if (distanceFromCenter > 5) {
                                                            isSnapping = true
                                                            try {
                                                                // Calculate exact scroll needed to center this item
                                                                val targetOffset = targetItem.offset - frameTop
                                                                numberListState.animateScrollToItem(
                                                                    index = targetItem.index,
                                                                    scrollOffset = targetOffset
                                                                )
                                                                flexibleNumber = (targetItem.index + 1).coerceIn(1, 31)
                                                            } finally {
                                                                isSnapping = false
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        
                                        // Selection update while scrolling using closest-to-center item
                                        LaunchedEffect(numberListState.firstVisibleItemIndex, numberListState.firstVisibleItemScrollOffset) {
                                            if (!isSnapping) {
                                                val layout = numberListState.layoutInfo
                                                val center = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
                                                val closest = layout.visibleItemsInfo.minByOrNull { info ->
                                                    kotlin.math.abs((info.offset + info.size / 2) - center)
                                                }
                                                val centerIndex = closest?.index ?: numberListState.firstVisibleItemIndex
                                                val newNumber = (centerIndex + 1).coerceIn(1, 31)
                                                if (newNumber != flexibleNumber) flexibleNumber = newNumber
                                            }
                                        }
                                        
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            state = numberListState,
                                            contentPadding = PaddingValues(vertical = 40.dp) // Reduced padding for better centering
                                        ) {
                                            items(31) { index ->
                                                val number = index + 1
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(40.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = number.toString(),
                                                        textAlign = TextAlign.Center,
                                                        color = if (number == flexibleNumber) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                        style = if (number == flexibleNumber) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                        }
                                        
                                        // Clean selection frame overlay - NO horizontal lines
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(40.dp)
                                                .align(Alignment.Center)
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    // Unit selector (day/week/month/year) with perfect centering
                                    Box(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(120.dp)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                                    ) {
                                        var isUnitSnapping by remember { mutableStateOf(false) }
                                        
                                        // Active snap detection for units - monitors when selection frame is between values
                                        LaunchedEffect(unitListState.isScrollInProgress) {
                                            if (!unitListState.isScrollInProgress && !isUnitSnapping) {
                                                // Wait for scroll to fully settle
                                                kotlinx.coroutines.delay(150)
                                                val layout = unitListState.layoutInfo
                                                if (layout.visibleItemsInfo.isNotEmpty()) {
                                                    val frameTop = layout.viewportStartOffset + 45 // Account for top padding
                                                    val frameBottom = frameTop + 30 // Frame height for units
                                                    val frameCenter = (frameTop + frameBottom) / 2
                                                    
                                                    // Find item that should be centered in frame
                                                    val targetItem = layout.visibleItemsInfo.minByOrNull { item ->
                                                        kotlin.math.abs((item.offset + item.size / 2) - frameCenter)
                                                    }
                                                    
                                                    if (targetItem != null) {
                                                        val itemCenter = targetItem.offset + targetItem.size / 2
                                                        val distanceFromCenter = kotlin.math.abs(itemCenter - frameCenter)
                                                        
                                                        // If item is not properly centered (threshold of 5px), snap it
                                                        if (distanceFromCenter > 5) {
                                                            isUnitSnapping = true
                                                            try {
                                                                // Calculate exact scroll needed to center this item
                                                                val targetOffset = targetItem.offset - frameTop
                                                                unitListState.animateScrollToItem(
                                                                    index = targetItem.index,
                                                                    scrollOffset = targetOffset
                                                                )
                                                                flexibleUnit = TimeUnit.values().getOrNull(targetItem.index) ?: TimeUnit.DAY
                                                            } finally {
                                                                isUnitSnapping = false
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        
                                        // Selection update while scrolling using closest-to-center item
                                        LaunchedEffect(unitListState.firstVisibleItemIndex, unitListState.firstVisibleItemScrollOffset) {
                                            if (!isUnitSnapping) {
                                                val layout = unitListState.layoutInfo
                                                val center = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
                                                val closest = layout.visibleItemsInfo.minByOrNull { info ->
                                                    kotlin.math.abs((info.offset + info.size / 2) - center)
                                                }
                                                val centerIndex = closest?.index ?: unitListState.firstVisibleItemIndex
                                                val newUnit = TimeUnit.values().getOrNull(centerIndex) ?: TimeUnit.DAY
                                                if (newUnit != flexibleUnit) flexibleUnit = newUnit
                                            }
                                        }
                                        
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            state = unitListState,
                                            contentPadding = PaddingValues(vertical = 45.dp) // Adjusted for better centering
                                        ) {
                                            items(TimeUnit.values()) { unit ->
                                                val unitText = when (selectedLanguage) {
                                                    "German" -> when (unit) {
                                                        TimeUnit.DAY -> if (flexibleNumber == 1) "Tag" else "Tage"
                                                        TimeUnit.WEEK -> if (flexibleNumber == 1) "Woche" else "Wochen"
                                                        TimeUnit.MONTH -> if (flexibleNumber == 1) "Monat" else "Monate"
                                                        TimeUnit.YEAR -> if (flexibleNumber == 1) "Jahr" else "Jahre"
                                                    }
                                                    "Spanish" -> when (unit) {
                                                        TimeUnit.DAY -> if (flexibleNumber == 1) "día" else "días"
                                                        TimeUnit.WEEK -> if (flexibleNumber == 1) "semana" else "semanas"
                                                        TimeUnit.MONTH -> if (flexibleNumber == 1) "mes" else "meses"
                                                        TimeUnit.YEAR -> if (flexibleNumber == 1) "año" else "años"
                                                    }
                                                    else -> when (unit) {
                                                        TimeUnit.DAY -> if (flexibleNumber == 1) "day" else "days"
                                                        TimeUnit.WEEK -> if (flexibleNumber == 1) "week" else "weeks"
                                                        TimeUnit.MONTH -> if (flexibleNumber == 1) "month" else "months"
                                                        TimeUnit.YEAR -> if (flexibleNumber == 1) "year" else "years"
                                                    }
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(30.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = unitText,
                                                        textAlign = TextAlign.Center,
                                                        color = if (unit == flexibleUnit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                        fontSize = 12.sp,
                                                        style = if (unit == flexibleUnit) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }
                                        
                                        // Clean selection frame overlay - NO horizontal lines
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(30.dp)
                                                .align(Alignment.Center)
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                        )
                                    }
                                }
                            }

                            
                            // Custom Range
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedDateFilter = DateFilter.CUSTOM
                                        showCustomDatePicker = true
                                        isSelectingStartDate = true
                                        println("DEBUG: Selected CUSTOM filter")
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDateFilter == DateFilter.CUSTOM,
                                    onClick = {
                                        selectedDateFilter = DateFilter.CUSTOM
                                        showCustomDatePicker = true
                                        isSelectingStartDate = true
                                        println("DEBUG: RadioButton CUSTOM clicked")
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Benutzerdefinierter Bereich"
                                        "Spanish" -> "Rango personalizado"
                                        else -> "Custom Range"
                                    }
                                )
                            }
                            
                            // Specific Date
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedDateFilter = DateFilter.SPECIFIC_DATE
                                        showSpecificDatePicker = true
                                        println("DEBUG: Selected SPECIFIC_DATE filter")
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDateFilter == DateFilter.SPECIFIC_DATE,
                                    onClick = {
                                        selectedDateFilter = DateFilter.SPECIFIC_DATE
                                        showSpecificDatePicker = true
                                        println("DEBUG: RadioButton SPECIFIC_DATE clicked")
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Spezifisches Datum"
                                        "Spanish" -> "Fecha específica"
                                        else -> "Specific Date"
                                    }
                                )
                            }
                        }
                        
                        // Show empty dates option (only when filtering is active)
                        if (selectedDateFilter != DateFilter.ALL) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showEmptyDatesInFilter = !showEmptyDatesInFilter
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = showEmptyDatesInFilter,
                                    onCheckedChange = { 
                                        showEmptyDatesInFilter = it
                                        // Save filter state immediately
                                        SettingsManager.saveLastDateFilter(
                                            context = context,
                                            dateFilter = selectedDateFilter,
                                            customStartDate = customStartDate,
                                            customEndDate = customEndDate,
                                            showEmptyDates = showEmptyDatesInFilter,
                                            flexibleNumber = flexibleNumber,
                                            flexibleUnit = flexibleUnit
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = when (selectedLanguage) {
                                            "German" -> "Leere Datumszeilen anzeigen"
                                            "Spanish" -> "Mostrar filas con fecha vacía"
                                            else -> "Show empty date rows"
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Save the current filter state for persistence
                            SettingsManager.saveLastDateFilter(
                                context = context,
                                dateFilter = selectedDateFilter,
                                customStartDate = customStartDate,
                                customEndDate = customEndDate,
                                showEmptyDates = showEmptyDatesInFilter,
                                flexibleNumber = flexibleNumber,
                                flexibleUnit = flexibleUnit
                            )
                            showDateFilterDialog = false
                        }
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Anwenden"
                                "Spanish" -> "Aplicar"
                                else -> "Apply"
                            }
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDateFilterDialog = false
                        }
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Abbrechen"
                                "Spanish" -> "Cancelar"
                                else -> "Cancel"
                            }
                        )
                    }
                }
            )
        }
        
        // Custom Date Picker Dialog
        if (showCustomDatePicker) {
            var selectedMonth by remember { mutableStateOf(LocalDate.now().monthValue) }
            var selectedYear by remember { mutableStateOf(LocalDate.now().year) }
            
            AlertDialog(
                onDismissRequest = { showCustomDatePicker = false },
                title = {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> if (isSelectingStartDate) "Startmonat wählen" else "Endmonat wählen"
                            "Spanish" -> if (isSelectingStartDate) "Seleccionar mes de inicio" else "Seleccionar mes de fin"
                            else -> if (isSelectingStartDate) "Select Start Month" else "Select End Month"
                        }
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Wählen Sie Monat und Jahr:"
                                "Spanish" -> "Selecciona mes y año:"
                                else -> "Select month and year:"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Current selection display
                        Text(
                            text = "${Month.of(selectedMonth).name} $selectedYear",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Month selector
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Monat"
                                        "Spanish" -> "Mes"
                                        else -> "Month"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                LazyColumn(
                                    modifier = Modifier.height(150.dp).width(100.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    items(12) { monthIndex ->
                                        val month = monthIndex + 1
                                        val monthName = when (selectedLanguage) {
                                            "German" -> when (month) {
                                                1 -> "Jan"; 2 -> "Feb"; 3 -> "Mär"; 4 -> "Apr"
                                                5 -> "Mai"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
                                                9 -> "Sep"; 10 -> "Okt"; 11 -> "Nov"; 12 -> "Dez"
                                                else -> "$month"
                                            }
                                            "Spanish" -> when (month) {
                                                1 -> "Ene"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Abr"
                                                5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Ago"
                                                9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dic"
                                                else -> "$month"
                                            }
                                            else -> Month.of(month).name.take(3)
                                        }
                                        
                                        TextButton(
                                            onClick = { selectedMonth = month },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = if (selectedMonth == month) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Text(
                                                text = monthName,
                                                fontWeight = if (selectedMonth == month) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Year selector
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Jahr"
                                        "Spanish" -> "Año"
                                        else -> "Year"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                LazyColumn(
                                    modifier = Modifier.height(150.dp).width(80.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    items((2020..2030).toList()) { year ->
                                        TextButton(
                                            onClick = { selectedYear = year },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = if (selectedYear == year) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Text(
                                                text = "$year",
                                                fontWeight = if (selectedYear == year) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            try {
                                val selectedDate = if (isSelectingStartDate) {
                                    // For start date, use first day of month
                                    LocalDate.of(selectedYear, selectedMonth, 1)
                                } else {
                                    // For end date, use last day of month
                                    val lastDay = YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
                                    LocalDate.of(selectedYear, selectedMonth, lastDay)
                                }
                                
                                if (isSelectingStartDate) {
                                    customStartDate = selectedDate
                                    if (customEndDate == null) {
                                        isSelectingStartDate = false
                                        // Reset to current month/year for end date selection
                                        selectedMonth = LocalDate.now().monthValue
                                        selectedYear = LocalDate.now().year
                                    } else {
                                        showCustomDatePicker = false
                                    }
                                } else {
                                    customEndDate = selectedDate
                                    showCustomDatePicker = false
                                }
                            } catch (e: Exception) {
                                // Should not happen with month/year selection
                            }
                        }
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "OK"
                                "Spanish" -> "OK"
                                else -> "OK"
                            }
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showCustomDatePicker = false
                            if (customStartDate == null && customEndDate == null) {
                                selectedDateFilter = DateFilter.ALL
                            }
                        }
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Abbrechen"
                                "Spanish" -> "Cancelar"
                                else -> "Cancel"
                            }
                        )
                    }
                }
            )
        }
        
        // Specific Date Picker Dialog
        if (showSpecificDatePicker) {
            var selectedDate by remember { mutableStateOf(LocalDate.now()) }
            
            AlertDialog(
                onDismissRequest = { showSpecificDatePicker = false },
                title = {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Spezifisches Datum wählen"
                            "Spanish" -> "Seleccionar fecha específica"
                            else -> "Select Specific Date"
                        }
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Wählen Sie ein Datum:"
                                "Spanish" -> "Selecciona una fecha:"
                                else -> "Select a date:"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Current selection display
                        Text(
                            text = selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Day selector
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Tag"
                                        "Spanish" -> "Día"
                                        else -> "Day"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                LazyColumn(
                                    modifier = Modifier.height(120.dp).width(60.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    items((1..31).toList()) { day ->
                                        TextButton(
                                            onClick = { 
                                                try {
                                                    selectedDate = LocalDate.of(selectedDate.year, selectedDate.month, day)
                                                } catch (e: Exception) {
                                                    // Handle invalid dates (e.g., Feb 31)
                                                }
                                            },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = if (selectedDate.dayOfMonth == day) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Text(
                                                text = "$day",
                                                fontWeight = if (selectedDate.dayOfMonth == day) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Month selector
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Monat"
                                        "Spanish" -> "Mes"
                                        else -> "Month"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                LazyColumn(
                                    modifier = Modifier.height(120.dp).width(80.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    items(12) { monthIndex ->
                                        val month = monthIndex + 1
                                        val monthName = when (selectedLanguage) {
                                            "German" -> when (month) {
                                                1 -> "Jan"; 2 -> "Feb"; 3 -> "Mär"; 4 -> "Apr"
                                                5 -> "Mai"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
                                                9 -> "Sep"; 10 -> "Okt"; 11 -> "Nov"; 12 -> "Dez"
                                                else -> "$month"
                                            }
                                            "Spanish" -> when (month) {
                                                1 -> "Ene"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Abr"
                                                5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Ago"
                                                9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dic"
                                                else -> "$month"
                                            }
                                            else -> Month.of(month).name.take(3)
                                        }
                                        
                                        TextButton(
                                            onClick = { 
                                                try {
                                                    selectedDate = LocalDate.of(selectedDate.year, month, selectedDate.dayOfMonth.coerceAtMost(Month.of(month).length(selectedDate.isLeapYear)))
                                                } catch (e: Exception) {
                                                    selectedDate = LocalDate.of(selectedDate.year, month, 1)
                                                }
                                            },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = if (selectedDate.monthValue == month) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Text(
                                                text = monthName,
                                                fontWeight = if (selectedDate.monthValue == month) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Year selector
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Jahr"
                                        "Spanish" -> "Año"
                                        else -> "Year"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                LazyColumn(
                                    modifier = Modifier.height(120.dp).width(70.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    items((2020..2030).toList()) { year ->
                                        TextButton(
                                            onClick = { 
                                                try {
                                                    val isLeapYear = java.time.Year.of(year).isLeap
                                                    selectedDate = LocalDate.of(year, selectedDate.month, selectedDate.dayOfMonth.coerceAtMost(selectedDate.month.length(isLeapYear)))
                                                } catch (e: Exception) {
                                                    selectedDate = LocalDate.of(year, selectedDate.month, 1)
                                                }
                                            },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = if (selectedDate.year == year) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Text(
                                                text = "$year",
                                                fontWeight = if (selectedDate.year == year) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            specificDate = selectedDate
                            showSpecificDatePicker = false
                            // Save filter state immediately
                            SettingsManager.saveLastDateFilter(
                                context = context,
                                dateFilter = selectedDateFilter,
                                customStartDate = customStartDate,
                                customEndDate = customEndDate,
                                showEmptyDates = showEmptyDatesInFilter,
                                flexibleNumber = flexibleNumber,
                                flexibleUnit = flexibleUnit
                            )
                        }
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Auswählen"
                                "Spanish" -> "Seleccionar"
                                else -> "Select"
                            }
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showSpecificDatePicker = false
                            if (specificDate == null) {
                                selectedDateFilter = DateFilter.ALL
                            }
                        }
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Abbrechen"
                                "Spanish" -> "Cancelar"
                                else -> "Cancel"
                            }
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun EditableTableCell(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
    isDateCell: Boolean = false,
    isFoodCell: Boolean = false,
    isInsulinCell: Boolean = false,
    isSecured: Boolean = false,
    cellKey: String = "",
    confirmedEditingSessions: Set<String> = emptySet(),
    onShowEditConfirmDialog: ((String, (() -> Unit)) -> Unit)? = null,
    fontColor: Color,
    gridColor: Color,
    onDelete: (() -> Unit)? = null,
    isFocused: Boolean,
    focusRequester: FocusRequester,
    onRequestFocus: () -> Unit,
    onDateClick: (() -> Unit)? = null,
    foodPresets: List<FoodPreset>,
    setFoodPresets: (List<FoodPreset>) -> Unit,
    currentCellFoodItems: List<FoodPreset>,
    onCurrentCellFoodItemsChange: (List<FoodPreset>) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var showFoodList by remember { mutableStateOf(false) }
    var newFoodText by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("") }
    var tooltipPreset by remember { mutableStateOf<FoodPreset?>(null) }
    var showDeletePresetDialog by remember { mutableStateOf<FoodPreset?>(null) }
    var showEditPresetDialog by remember { mutableStateOf<FoodPreset?>(null) }
    var activeDeletePreset by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    
    // Common food symbols
    val commonFoods = listOf(
        "🍎" to "Apple",
        "🍌" to "Banana",
        "🥖" to "Bread",
        "🥩" to "Meat",
        "🥗" to "Salad",
        "🥛" to "Milk",
        "🍚" to "Rice",
        "🥚" to "Egg",
        "🐟" to "Fish",
        "🥔" to "Potato",
        "🥕" to "Carrot",
        "🍝" to "Pasta"
    )

    // Food emojis for picker
    val foodEmojis = listOf(
        // Fruits
        "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍈",
        "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🥑", "🥦", "🥬",
        
        // Vegetables
        "🥒", "🌶️", "🫑", "🥕", "🧅", "🧄", "🥔", "🍠", "🫚", "🥗",
        
        // Bread & Dairy
        "🥐", "🥯", "🍞", "🥖", "🥨", "🧀", "🥚", "🍳", "🧈", "🥞",
        "🧇", "🥓", "🧉", "🥛", "🧋", "🫙", "🧃", "🥤", "🧊", "🫖",
        
        // Meat & Fish
        "🥩", "🍗", "🍖", "🦴", "🌭", "🍔", "🍟", "🍕", "🥪", "🥙",
        "🐟", "🦞", "🦐", "🦑", "🦀", "🦪", "🐙", "🦈", "🐠", "🐡",
        
        // Prepared Foods
        "🧆", "🌮", "🌯", "🫔", "🥘", "🫕", "🥫", "🍝", "🍜", "🍲",
        "🍛", "🍣", "🍱", "🥟", "🍤", "🍙", "🍚", "🍘", "🍥", "🥠",
        
        // Desserts & Sweets
        "🥮", "🍡", "🍧", "🍨", "🍦", "🥧", "🧁", "🍰", "🎂", "🍮",
        "🍭", "🍬", "🍫", "🍿", "🍩", "🍪", "🌰", "🥜", "🧂", "🍯",
        
        // Drinks
        "🍵", "☕", "🍶", "🍾", "🍷", "🍸", "🍹", "🍺", "🍻", "🥂",
        "🥃", "🫗", "🧉", "🥛", "🧋", "🫙", "🧃", "🥤", "🧊", "🫖",
        
        // Additional Foods
        "🥄", "🍴", "🍽️", "🥢", "🧆", "🥣", "🫒", "🧈", "🧀", "🥞"
    )

    Surface(
        modifier = modifier.padding(horizontal = 2.dp),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        border = BorderStroke(0.5.dp, gridColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text field gets more space
            Box(
                modifier = Modifier
                    .weight(2.2f)
                    .padding(end = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!isDateCell) {
                    BasicTextField(
                        value = value,
                        onValueChange = { newValue ->
                            // Allow normal editing - confirmation dialog is now handled on focus
                            onValueChange(newValue)
                        },
                        textStyle = TextStyle(
                            color = fontColor,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { state ->
                                if (state.isFocused) {
                                    onRequestFocus()
                                    
                                    // Show confirmation dialog immediately when focusing on a filled (secured) cell
                                    // Only for cells with actual data in Blood Sugar Control or Insulin sections
                                    val hasData = value.text.isNotBlank()
                                    val isBloodSugarOrInsulinCell = !isDateCell // All cells except date cells (blood sugar and insulin cells both have isFoodCell=true)
                                    
                                    if (isSecured && hasData && isBloodSugarOrInsulinCell && !confirmedEditingSessions.contains(cellKey)) {
                                        // Show confirmation dialog before allowing edit (only once per session)
                                        onShowEditConfirmDialog?.invoke(cellKey) {
                                            // Focus is already gained, just mark as confirmed
                                        }
                                    }
                                }
                                isEditing = state.isFocused
                                if (state.isFocused) keyboardController?.show()
                            }
                            .testTag("EditableTextField")
                    )

                    if (value.text.isEmpty() && !isEditing) {
                        Text(
                            text = hint,
                            color = fontColor.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = value.text.ifEmpty { hint },
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = if (value.text.isEmpty()) fontColor.copy(alpha = 0.5f) else fontColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp, end = 2.dp)
                                .clickable {
                                    if (value.text.isEmpty()) {
                                        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                        onValueChange(TextFieldValue(today))
                                    }
                                    showContextMenu = true
                                }
                        )
                        IconButton(
                            onClick = { showContextMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Date options",
                                tint = fontColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showContextMenu,
                        onDismissRequest = { showContextMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Today") },
                            onClick = {
                                val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                onValueChange(TextFieldValue(today))
                                showContextMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear") },
                            onClick = {
                                onValueChange(TextFieldValue(""))
                                showContextMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Pick Date...") },
                            onClick = {
                                showContextMenu = false
                                showDatePicker = true
                            }
                        )
                        if (onDelete != null) {
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Delete Entry", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showContextMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            // Only show food chips/add button in the dedicated food column
            if (isFoodCell) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.weight(1f, false)
                ) {
                    if (currentCellFoodItems.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .height(20.dp)
                                .clickable { showFoodList = true }
                                .testTag("FoodCountChip")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            ) {
                                Text(
                                    // Force use of the current size to ensure recomposition
                                    text = "${currentCellFoodItems.size}",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 12.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = "Food entries",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                    // Only show add food button for non-insulin cells
                    if (!isInsulinCell) {
                        IconButton(
                            onClick = { showAddFoodDialog = true },
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("AddFoodButton")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add food",
                                tint = fontColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showContextMenu,
                        onDismissRequest = { showContextMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear") },
                            onClick = {
                                onValueChange(TextFieldValue(""))
                                showContextMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Pick Date...") },
                            onClick = {
                                showContextMenu = false
                                showDatePicker = true
                            }
                        )
                    }
                    if (onDelete != null) {
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Delete Entry", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showContextMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }

        if (showFoodList && currentCellFoodItems.isNotEmpty()) {
            Dialog(onDismissRequest = { showFoodList = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Food Entries (${currentCellFoodItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Create a snapshot of the current list to ensure proper rendering
                            val foodItems = currentCellFoodItems.toList()
                            foodItems.forEachIndexed { index, food ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = food.emoji,
                                            fontSize = 18.sp,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                        Text(
                                            text = food.name,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontSize = 14.sp
                                        )
                                        IconButton(
                                            onClick = {
                                                // Remove only the specific instance at this index
                                                val updatedList = currentCellFoodItems.toMutableList()
                                                updatedList.removeAt(index)
                                                onCurrentCellFoodItemsChange(updatedList)
                                                if (updatedList.isEmpty()) {
                                                    showFoodList = false
                                                }
                                            },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove food",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                showFoodList = false
                                showAddFoodDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 1.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add food",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Add Food",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        if (showAddFoodDialog) {
            var activeDeletePreset by remember { mutableStateOf<String?>(null) }
            var showEmojiPicker by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = {
                    showAddFoodDialog = false
                    newFoodText = ""
                    selectedEmoji = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { activeDeletePreset = null },
                title = { Text("Add Food") },
                text = {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                        // No header needed as AlertDialog has a title
                        
                        // Food name input and emoji selector in a row (emoji left, same height as text field)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            val foodFieldHeight = 56.dp // Default OutlinedTextField height
                            Surface(
                                modifier = Modifier
                                    .size(foodFieldHeight)
                                    .clickable { showEmojiPicker = true },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = selectedEmoji.ifEmpty { "🍽️" },
                                        fontSize = 28.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = newFoodText,
                                onValueChange = { newFoodText = it },
                                label = { Text("Food name") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = foodFieldHeight)
                                    .padding(vertical = 4.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newFoodText.isNotEmpty()) {
                                                // Add to presets if not already present
                                                val newPreset = FoodPreset(name = newFoodText, emoji = selectedEmoji)
                                                if (foodPresets.none { it.name == newFoodText && it.emoji == selectedEmoji }) {
                                                    PresetManager.addFoodPreset(context, newPreset)
                                                    setFoodPresets(PresetManager.getFoodPresets(context))
                                                }
                                                onCurrentCellFoodItemsChange(currentCellFoodItems + newPreset)
                                                newFoodText = ""
                                                selectedEmoji = ""
                                                // Keep the dialog open after adding food
                                            }                                  }
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Saved presets section
                        if (foodPresets.isNotEmpty()) {
                            Text(
                                text = "Your Presets",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            // Box as a container for the FlowRow
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activeDeletePreset = null }
                            ) {
                                // Main FlowRow for food presets
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    foodPresets.forEach { preset ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .width(100.dp)
                                                .padding(4.dp)
                                        ) {
                                            // Main preset button
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                                modifier = Modifier
                                                    .size(90.dp)
                                                    .clickable {
                                                        if (activeDeletePreset != preset.id) {
                                                            // Create a mutable list from the current items
                                                            val updatedItems = currentCellFoodItems.toMutableList()
                                                            // Add the new preset
                                                            updatedItems.add(preset)
                                                            
                                                            // Update the cell's food items list with the new list
                                                            onCurrentCellFoodItemsChange(updatedItems)
                                                            
                                                            // Show tooltip
                                                            tooltipPreset = preset
                                                            CoroutineScope(Dispatchers.Main).launch {
                                                                delay(1500)
                                                                tooltipPreset = null
                                                            }
                                                        }
                                                    }
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    // Emoji and name in center
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(4.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        // Emoji
                                                        Text(
                                                            text = preset.emoji,
                                                            fontSize = 32.sp,
                                                            textAlign = TextAlign.Center
                                                        )
                                                        
                                                        // Name
                                                        Text(
                                                            text = preset.name,
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            textAlign = TextAlign.Center,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    
                                                    // Three dots menu button
                                                    IconButton(
                                                        onClick = { activeDeletePreset = preset.id },
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .align(Alignment.TopEnd)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.MoreVert,
                                                            contentDescription = "More options",
                                                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    
                                                    // Show tooltip directly within the food item when it's tapped
                                                    if (tooltipPreset?.id == preset.id) {
                                                        Box(
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            FoodTagTooltip(
                                                                text = "Added",
                                                                visible = true,
                                                                modifier = Modifier.padding(4.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            // Menu options when three-dots is clicked
                                            if (activeDeletePreset == preset.id) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    modifier = Modifier.padding(top = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButton(
                                                        onClick = { 
                                                            showEditPresetDialog = preset
                                                            activeDeletePreset = null
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Edit preset",
                                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { 
                                                            showDeletePresetDialog = preset
                                                            activeDeletePreset = null
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Delete preset",
                                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Tooltips are now displayed directly within each food item
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Common foods section
                        Text(
                            text = "Common Foods",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        // Box as a container for the FlowRow
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Main FlowRow for common foods
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                commonFoods.forEach { (emoji, name) ->
                                    // Check if this common food is the one that was just added
                                    val isAdded = tooltipPreset?.let { it.name == name && it.emoji == emoji } == true
                                    
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .clickable {
                                                // Create the food preset
                                                val newFoodPreset = FoodPreset(name = name, emoji = emoji)
                                                
                                                // Create a mutable list from the current items
                                                val updatedItems = currentCellFoodItems.toMutableList()
                                                // Add the new preset
                                                updatedItems.add(newFoodPreset)
                                                
                                                // Update the state with the new food items list
                                                onCurrentCellFoodItemsChange(updatedItems)
                                                
                                                // Show tooltip
                                                tooltipPreset = newFoodPreset
                                                
                                                // Auto-hide tooltip after 1.5 seconds
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    delay(1500)
                                                    tooltipPreset = null
                                                }
                                                // Keep the dialog open after adding food
                                            }
                                    ) {
                                        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                            // Normal content
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = emoji,
                                                    fontSize = 20.sp
                                                )
                                                Text(
                                                    text = name,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                            
                                            // Show tooltip directly within the food item when it's tapped
                                            if (isAdded) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    FoodTagTooltip(
                                                        text = "Added",
                                                        visible = true,
                                                        modifier = Modifier.padding(4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newFoodText.isNotEmpty()) {
                                val newPresetToAdd = FoodPreset(name = newFoodText, emoji = selectedEmoji)
                                PresetManager.addFoodPreset(context, newPresetToAdd) // Save globally
                                setFoodPresets(PresetManager.getFoodPresets(context)) // Update global list state

                                // Create a mutable list from the current items
                                val updatedItems = currentCellFoodItems.toMutableList()
                                // Add the new preset
                                updatedItems.add(newPresetToAdd)
                                
                                // Update the cell's food items list with the new list
                                onCurrentCellFoodItemsChange(updatedItems)
                                
                                // Show tooltip for feedback
                                tooltipPreset = newPresetToAdd
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(1500)
                                    tooltipPreset = null
                                }

                                // Keep dialog open after saving preset
                                newFoodText = ""
                                selectedEmoji = ""
                                // Do not close dialog to allow multiple selections
                            }
                        }
                    ) {
                        Text("Add Food")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showAddFoodDialog = false  // Close dialog on done
                            newFoodText = ""
                            selectedEmoji = ""
                        }
                    ) {
                        Text("Done")
                    }
                }
            )

            // Emoji picker dialog
            if (showEmojiPicker) {
                Dialog(onDismissRequest = { showEmojiPicker = false }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Select Emoji",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 36.dp), // Bigger icons
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 200.dp, max = 420.dp)
                            ) {
                                items(foodEmojis) { emoji ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clickable {
                                                selectedEmoji = emoji
                                                showEmojiPicker = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = emoji,
                                            fontSize = 20.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDeletePresetDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeletePresetDialog = null },
                title = { Text("Delete Food Preset") },
                text = { 
                    Text(
                        text = "Are you sure you want to delete '${showDeletePresetDialog!!.emoji} ${showDeletePresetDialog!!.name}'?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            PresetManager.removeFoodPreset(context, showDeletePresetDialog!!.id)
                            setFoodPresets(PresetManager.getFoodPresets(context))
                            showDeletePresetDialog = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeletePresetDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showEditPresetDialog != null) {
            var editedName by remember { mutableStateOf(showEditPresetDialog!!.name) }
            var editedEmoji by remember { mutableStateOf(showEditPresetDialog!!.emoji) }
            var showEmojiPicker by remember { mutableStateOf(false) }
            var showError by remember { mutableStateOf(false) }

            Dialog(onDismissRequest = { showEditPresetDialog = null }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Edit Food Preset",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        // Show emoji and food name in a row, with moderate emoji size
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = editedEmoji,
                                fontSize = 32.sp,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            OutlinedTextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                label = { Text("Food name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Emoji picker
                        Text(
                            text = "Change Emoji",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 48.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 280.dp)
                        ) {
                            items(foodEmojis) { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable {
                                            editedEmoji = emoji
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emoji,
                                        fontSize = 28.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) {
                            TextButton(onClick = { showEditPresetDialog = null }) {
                                Text("Cancel")
                            }
                            TextButton(
                                onClick = {
                                    if (editedName.isNotBlank()) {
                                        val updatedPreset = showEditPresetDialog!!.copy(
                                            name = editedName,
                                            emoji = editedEmoji
                                        )
                                        setFoodPresets(foodPresets.map { 
                                            if (it.id == updatedPreset.id) updatedPreset else it 
                                        })
                                        showEditPresetDialog = null
                                        PresetManager.saveFoodPresets(context, foodPresets)
                                    }
                                }
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }

        if (showDatePicker) {
            val currentDate = LocalDate.now()
            var selectedYear by remember { mutableStateOf(currentDate.year) }
            var selectedMonth by remember { mutableStateOf(currentDate.monthValue) }
            
            AlertDialog(
                onDismissRequest = { showDatePicker = false },
                title = { Text("Select Date") },
                text = {
                    Column(Modifier.fillMaxWidth()) {
                        // Year and Month selection
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            // Year dropdown
                            Column(Modifier.weight(1f)) {
                                Text("Year", style = MaterialTheme.typography.bodyMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { selectedYear-- }) {
                                        Icon(Icons.Filled.KeyboardArrowLeft, "Previous Year")
                                    }
                                    Text(
                                        "$selectedYear",
                                        Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                    IconButton(onClick = { selectedYear++ }) {
                                        Icon(Icons.Filled.KeyboardArrowRight, "Next Year")
                                    }
                                }
                            }
                            
                            Spacer(Modifier.width(8.dp))
                            
                            // Month dropdown
                            Column(Modifier.weight(1f)) {
                                Text("Month", style = MaterialTheme.typography.bodyMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { 
                                        if (selectedMonth > 1) selectedMonth-- 
                                        else {
                                            selectedMonth = 12
                                            selectedYear--
                                        }
                                    }) {
                                        Icon(Icons.Filled.KeyboardArrowLeft, "Previous Month")
                                    }
                                    Text(
                                        Month.of(selectedMonth).getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, Locale.getDefault()),
                                        Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    IconButton(onClick = { 
                                        if (selectedMonth < 12) selectedMonth++ 
                                        else {
                                            selectedMonth = 1
                                            selectedYear++
                                        }
                                    }) {
                                        Icon(Icons.Filled.KeyboardArrowRight, "Next Month")
                                    }
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        Divider()
                        Spacer(Modifier.height(8.dp))
                        
                        // Days grid
                        LazyColumn(Modifier.height(200.dp)) {
                            val yearMonth = YearMonth.of(selectedYear, selectedMonth)
                            val daysInMonth = yearMonth.lengthOfMonth()
                            val days = (1..daysInMonth).map { LocalDate.of(selectedYear, selectedMonth, it) }
                            
                            items(days) { date ->
                                val isToday = date.equals(LocalDate.now())
                                Text(
                                    text = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onValueChange(TextFieldValue(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))))
                                            showDatePicker = false
                                        }
                                        .padding(8.dp)
                                        .background(
                                            if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                            else Color.Transparent,
                                            shape = RoundedCornerShape(4.dp)
                                        ),
                                    textAlign = TextAlign.Center,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            onValueChange(TextFieldValue(""))
                            showDatePicker = false 
                        }
                    ) {
                        Text("Clear Date")
                    }
                }
            )
        }
    }
}

@Composable
fun CommentCell(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    fontColor: Color,
    gridColor: Color
) {
    Surface(
        modifier = modifier.padding(horizontal = 2.dp),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        border = BorderStroke(0.5.dp, gridColor.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = fontColor,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (value.isEmpty()) {
                Text(
                    text = "Remarks",
                    color = fontColor.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ColorPicker(
    title: String,
    color: Color,
    onColorChange: (Color) -> Unit,
    context: Context = LocalContext.current
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    var presets by remember { mutableStateOf(PresetManager.getPresets(context)) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // Title
            item {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Color presets section
            if (presets.isNotEmpty()) {
                item {
                    Text(
                        text = "Saved Presets",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        items(
                            items = presets,
                            key = { it.id }
                        ) { preset ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(50.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            color = preset.color,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .border(
                                            width = 2.dp,
                                            color = if (color == preset.color) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clickable { onColorChange(preset.color) }
                                ) {
                                    var showDeleteConfirmation by remember { mutableStateOf(false) }
                                    
                                    if (showDeleteConfirmation) {
                                        AlertDialog(
                                            onDismissRequest = { showDeleteConfirmation = false },
                                            title = { Text("Delete Color Preset") },
                                            text = { Text("Are you sure you want to delete this color preset?") },
                                            confirmButton = {
                                                TextButton(
                                                    onClick = {
                                                        PresetManager.deletePreset(context, preset.id)
                                                        presets = PresetManager.getPresets(context)
                                                        showDeleteConfirmation = false
                                                    }
                                                ) {
                                                    Text("Delete")
                                                }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { showDeleteConfirmation = false }) {
                                                    Text("Cancel")
                                                }
                                            }
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = { showDeleteConfirmation = true },
                                        modifier = Modifier
                                            .size(16.dp)
                                            .align(Alignment.TopEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Remove,
                                            contentDescription = "Delete preset",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Standard color palette
            item {
                val colors = listOf(
                    // Reds
                    Color(0xFFF44336), Color(0xFFE91E63), Color(0xFFFF1744), Color(0xFFFF5252),
                    // Purples
                    Color(0xFF9C27B0), Color(0xFF673AB7), Color(0xFF7C4DFF), Color(0xFFD500F9),
                    // Blues
                    Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4), Color(0xFF448AFF),
                    // Cyans and Teals
                    Color(0xFF00BCD4), Color(0xFF009688), Color(0xFF64FFDA), Color(0xFF1DE9B6),
                    // Greens
                    Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFF69F0AE), Color(0xFFB2FF59),
                    // Yellows and Oranges
                    Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF6D00),
                    // Browns and Greys
                    Color(0xFF795548), Color(0xFF9E9E9E), Color(0xFF607D8B), Color(0xFF757575),
                    // Black, White and Transparent
                    Color.Black, Color.White, Color.Transparent, Color.DarkGray
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 400.dp)
                ) {
                    items(
                        count = colors.size
                    ) { index ->
                        val colorOption = colors[index]
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = colorOption,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (color == colorOption) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { 
                                    onColorChange(colorOption)
                                }
                        ) {
                            IconButton(
                                onClick = {
                                    PresetManager.savePreset(context, ColorPreset("Custom", colorOption))
                                    presets = PresetManager.getPresets(context)
                                },
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Save preset",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Color Dialog
    if (showCustomDialog) {
        var red by remember { mutableFloatStateOf(color.red) }
        var green by remember { mutableFloatStateOf(color.green) }
        var blue by remember { mutableFloatStateOf(color.blue) }
        var alpha by remember { mutableFloatStateOf(color.alpha) }

        Dialog(
            onDismissRequest = { showCustomDialog = false }
        ) {
            Surface(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Custom Color",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    ColorSlider("Red", red) { red = it }
                    ColorSlider("Green", green) { green = it }
                    ColorSlider("Blue", blue) { blue = it }
                    ColorSlider("Alpha", alpha) { alpha = it }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .padding(vertical = 8.dp)
                            .background(
                                Color(red, green, blue, alpha),
                                RoundedCornerShape(4.dp)
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showCustomDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val newColor = Color(red, green, blue, alpha)
                                onColorChange(newColor)
                                showCustomDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$label: ${(value * 255).toInt()}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FoodTagTooltip(
    text: String,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (visible) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFF4CAF50).copy(alpha = 0.9f), // Bright green color for better visibility
            shadowElevation = 4.dp
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

enum class BloodSugarTimeFilter {
    ALL_AVERAGE,
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT
}

enum class InsulinTimeFilter {
    ALL_AVERAGE,
    MORNING,
    AFTERNOON,
    EVENING
}

enum class ChartType {
    BLOOD_SUGAR,
    INSULIN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTimeChart(data: List<DiabetesEntry>) {
    var selectedFilter by remember { mutableStateOf(BloodSugarTimeFilter.ALL_AVERAGE) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // Filter options
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            BloodSugarTimeFilter.values().forEach { filter ->
                val text = when (filter) {
                    BloodSugarTimeFilter.ALL_AVERAGE -> "Average"
                    BloodSugarTimeFilter.MORNING -> "Morning"
                    BloodSugarTimeFilter.AFTERNOON -> "Afternoon"
                    BloodSugarTimeFilter.EVENING -> "Evening"
                    BloodSugarTimeFilter.NIGHT -> "Night"
                }
                SegmentedButton(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    shape = SegmentedButtonDefaults.itemShape(index = filter.ordinal, count = BloodSugarTimeFilter.values().size)
                ) {
                    TimeFilterText(text, SettingsManager.loadSettings(LocalContext.current).useSymbolsInCharts)
                }
            }
        }

        // Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 24.dp
                )
        ) {
            val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
            val displayFormatter = remember { DateTimeFormatter.ofPattern("dd/MM") }
            
            val sortedData = remember(data) {
                data.mapNotNull { entry ->
                    try {
                        val date = LocalDate.parse(entry.date, dateFormatter)
                        println("Successfully parsed date: ${entry.date} -> $date")
                        entry to date
                    } catch (e: Exception) {
                        println("Failed to parse date: ${entry.date}, error: ${e.message}")
                        null
                    }
                }.sortedBy { it.second }
            }

            println("Sorted data size: ${sortedData.size}")

            val entries = remember(sortedData, selectedFilter) {
                println("Processing entries for filter: $selectedFilter")
                sortedData.mapIndexedNotNull { index, (entry, _) ->
                    val value = when (selectedFilter) {
                        BloodSugarTimeFilter.ALL_AVERAGE -> {
                            val values = listOfNotNull(
                                entry.morningBloodSugarLevel.takeIf { it.isNotBlank() }?.toFloatOrNull(),
                                entry.afternoonBloodSugarLevel.takeIf { it.isNotBlank() }?.toFloatOrNull(),
                                entry.eveningBloodSugarLevel.takeIf { it.isNotBlank() }?.toFloatOrNull(),
                                entry.nightBloodSugarLevel.takeIf { it.isNotBlank() }?.toFloatOrNull()
                            )
                            println("Values for average: $values")
                            if (values.isEmpty()) null else values.average().toFloat()
                        }
                        BloodSugarTimeFilter.MORNING -> entry.morningBloodSugarLevel.takeIf { it.isNotBlank() }?.toFloatOrNull()
                        BloodSugarTimeFilter.AFTERNOON -> entry.afternoonBloodSugarLevel.takeIf { it.isNotBlank() }?.toFloatOrNull()
                        BloodSugarTimeFilter.EVENING -> entry.eveningBloodSugarLevel.takeIf { it.isNotBlank() }?.toFloatOrNull()
                        BloodSugarTimeFilter.NIGHT -> entry.nightBloodSugarLevel.takeIf { it.isNotBlank() }?.toFloatOrNull()
                    }
                    value?.let { FloatEntry(index.toFloat(), it) }
                }
            }

            println("Final entries size: ${entries.size}")

            if (entries.isNotEmpty()) {
                Chart(
                    chart = lineChart(),
                    model = entryModelOf(entries),
                    startAxis = rememberStartAxis(
                        title = "Blood Sugar",
                        valueFormatter = { value, _ -> "${value.toInt()}" }
                    ),
                    bottomAxis = rememberBottomAxis(
                        title = "Date",
                        valueFormatter = { value, _ ->
                            val index = value.toInt()
                            if (index >= 0 && index < sortedData.size) {
                                sortedData[index].second.format(displayFormatter)
                            } else {
                                ""
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available for the selected time period",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsulinTimeChart(data: List<DiabetesEntry>) {
    var selectedFilter by remember { mutableStateOf(InsulinTimeFilter.ALL_AVERAGE) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // Filter options
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            InsulinTimeFilter.values().forEach { filter ->
                val text = when (filter) {
                    InsulinTimeFilter.ALL_AVERAGE -> "Average"
                    InsulinTimeFilter.MORNING -> "Morning"
                    InsulinTimeFilter.AFTERNOON -> "Afternoon"
                    InsulinTimeFilter.EVENING -> "Evening"
                }
                SegmentedButton(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    shape = SegmentedButtonDefaults.itemShape(index = filter.ordinal, count = InsulinTimeFilter.values().size)
                ) {
                    TimeFilterText(text, SettingsManager.loadSettings(LocalContext.current).useSymbolsInCharts)
                }
            }
        }

        // Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 24.dp
                )
        ) {
            val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
            val displayFormatter = remember { DateTimeFormatter.ofPattern("dd/MM") }
            
            // Use simple remember without derivedStateOf for better compatibility
            val sortedData = remember(data) {
                data.mapNotNull { entry ->
                    try {
                        val date = LocalDate.parse(entry.date, dateFormatter)
                        entry to date
                    } catch (e: Exception) {
                        println("Failed to parse date: ${entry.date}, error: ${e.message}")
                        null
                    }
                }.sortedBy { it.second }
            }

            // Use simple remember for chart entries
            val entries = remember(sortedData, selectedFilter) {
                sortedData.mapIndexedNotNull { index, (entry, _) ->
                    val value = when (selectedFilter) {
                        InsulinTimeFilter.ALL_AVERAGE -> {
                            val values = listOfNotNull(
                                entry.morningInsulin.takeIf { it.isNotBlank() }?.toFloatOrNull(),
                                entry.afternoonInsulin.takeIf { it.isNotBlank() }?.toFloatOrNull(),
                                entry.eveningInsulin.takeIf { it.isNotBlank() }?.toFloatOrNull()
                            )
                            if (values.isEmpty()) null else values.average().toFloat()
                        }
                        InsulinTimeFilter.MORNING -> entry.morningInsulin.takeIf { it.isNotBlank() }?.toFloatOrNull()
                        InsulinTimeFilter.AFTERNOON -> entry.afternoonInsulin.takeIf { it.isNotBlank() }?.toFloatOrNull()
                        InsulinTimeFilter.EVENING -> entry.eveningInsulin.takeIf { it.isNotBlank() }?.toFloatOrNull()
                    }
                    value?.let { FloatEntry(index.toFloat(), it) }
                }
            }

            println("Final entries size: ${entries.size}")

            if (entries.isNotEmpty()) {
                Chart(
                    chart = lineChart(),
                    model = entryModelOf(entries.toList()),
                    startAxis = rememberStartAxis(
                        title = "Insulin Units",
                        valueFormatter = { value, _ -> "${value.toInt()}" }
                    ),
                    bottomAxis = rememberBottomAxis(
                        title = "Date",
                        valueFormatter = { value, _ ->
                            val index = value.toInt()
                            if (index >= 0 && index < sortedData.size) {
                                sortedData[index].second.format(displayFormatter)
                            } else {
                                ""
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available for the selected time period",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}



@Composable
fun TimeFilterText(text: String, useSymbols: Boolean) {
    val symbol = when (text) {
        "Morning" -> "🌅"
        "Afternoon" -> "☀️"
        "Evening" -> "🌆"
        "Night" -> "🌙"
        else -> text
    }
    Text(if (useSymbols && symbol != text) symbol else text, maxLines = 1)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeChartDialog(
    onDismissRequest: () -> Unit,
    entries: List<DiabetesEntry>
) {
    var selectedChartType by remember { mutableStateOf(ChartType.BLOOD_SUGAR) }
    
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedChartType == ChartType.BLOOD_SUGAR) "Blood Sugar Over Time" else "Insulin Over Time",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                // Chart Type Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TabRow(
                        selectedTabIndex = selectedChartType.ordinal,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = selectedChartType == ChartType.BLOOD_SUGAR,
                            onClick = { selectedChartType = ChartType.BLOOD_SUGAR },
                            text = { Text("Blood Sugar") }
                        )
                        Tab(
                            selected = selectedChartType == ChartType.INSULIN,
                            onClick = { selectedChartType = ChartType.INSULIN },
                            text = { Text("Insulin") }
                        )
                    }
                }

                when (selectedChartType) {
                    ChartType.BLOOD_SUGAR -> SimpleTimeChart(data = entries.filter { it.date.isNotBlank() })
                    ChartType.INSULIN -> InsulinTimeChart(data = entries.filter { it.date.isNotBlank() })
                }
            }
        }
    }
}

@Composable
fun TableHeader(labels: Map<String, String>, fontColor: Color, gridColor: Color, insulinColumnColor: Color) {
    val settings = SettingsManager.loadSettings(LocalContext.current)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Date Column
            HeaderCell(
                title = labels["date"] ?: "Date",
                modifier = Modifier.weight(1f),
                fontColor = fontColor
            )
            
            // Blood Sugar Control Column
            Column(
                modifier = Modifier
                    .weight(3f)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = labels["bloodSugarControl"] ?: "Blood Sugar Control",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = fontColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        labels["morning"] ?: "Morning",
                        labels["afternoon"] ?: "Afternoon",
                        labels["evening"] ?: "Evening",
                        labels["night"] ?: "Night"
                    ).forEach { time ->
                        SubHeaderCell(
                            text = time,
                            useSymbols = settings.useSymbolsInCharts,
                            modifier = Modifier.weight(1f),
                            fontColor = fontColor,
                            gridColor = gridColor
                        )
                    }
                }
            }
            
            // Insulin Column
            Column(
                modifier = Modifier
                    .weight(2.2f)
                    .padding(horizontal = 4.dp)
                    .background(
                        color = insulinColumnColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = labels["insulin"] ?: "Insulin",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = fontColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        labels["morning"] ?: "Morning",
                        labels["afternoon"] ?: "Afternoon",
                        labels["evening"] ?: "Evening"
                    ).forEach { time ->
                        SubHeaderCell(
                            text = time,
                            useSymbols = settings.useSymbolsInCharts,
                            modifier = Modifier.weight(1f),
                            fontColor = fontColor,
                            gridColor = gridColor
                        )
                    }
                }
            }
            
            // Remarks Column
            HeaderCell(
                title = labels["remarks"] ?: "Remarks",
                modifier = Modifier.weight(1.5f),
                fontColor = fontColor
            )
        }
    }
}

@Composable
private fun HeaderCell(
    title: String,
    modifier: Modifier = Modifier,
    fontColor: Color
) {
    Surface(
        modifier = modifier.padding(horizontal = 2.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minHeight = 64.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = fontColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SubHeaderCell(
    text: String,
    useSymbols: Boolean,
    modifier: Modifier = Modifier,
    fontColor: Color,
    gridColor: Color
) {
    Surface(
        modifier = modifier.padding(horizontal = 2.dp),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, gridColor.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            TimeFilterText(text, useSymbols)
        }
    }
}

@Composable
fun TableRow(
    entry: DiabetesEntry,
    entryIndex: Int,
    labels: Map<String, String>,
    isSecured: Boolean = false,
    confirmedEditingSessions: Set<String> = emptySet(),
    onShowEditConfirmDialog: ((String, (() -> Unit)) -> Unit)? = null,
    onEntryChanged: (Int, DiabetesEntry) -> Unit,
    onDeleteEntry: (Int) -> Unit,
    fontColor: Color,
    backgroundColor: Color,
    gridColor: Color,
    insulinColumnColor: Color,
    focusedCell: Pair<Int, String>?,
    focusRequesters: MutableMap<Pair<Int, String>, FocusRequester>,
    onRequestFocus: (Pair<Int, String>) -> Unit,
    cellTextFieldValues: MutableMap<Pair<Int, String>, TextFieldValue>,
    foodPresets: List<FoodPreset>,
    setFoodPresets: (List<FoodPreset>) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            // Date Column with delete functionality
            Box(
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { showDeleteDialog = true }
                        )
                    }
            ) {
                val cellKey = entryIndex to "date"
                val focusRequester = focusRequesters.getOrPut(cellKey) { FocusRequester() }
                val cellValue = cellTextFieldValues.getOrPut(cellKey) { TextFieldValue(entry.date) }
                EditableTableCell(
                    value = cellValue,
                    onValueChange = { 
                        cellTextFieldValues[cellKey] = it
                        onEntryChanged(entryIndex, entry.copy(date = it.text))
                    },
                    hint = labels["date"] ?: "Date",
                    isDateCell = true,
                    isFoodCell = false,
                    isSecured = isSecured,
                    cellKey = "${entryIndex}_date",
                    confirmedEditingSessions = confirmedEditingSessions,
                    onShowEditConfirmDialog = onShowEditConfirmDialog,
                    fontColor = fontColor,
                    gridColor = gridColor,
                    onDelete = { onDeleteEntry(entryIndex) },
                    isFocused = (focusedCell == cellKey),
                    focusRequester = focusRequester,
                    onRequestFocus = { onRequestFocus(cellKey) },
                    onDateClick = null,
                    foodPresets = foodPresets,
                    setFoodPresets = setFoodPresets,
                    currentCellFoodItems = emptyList(), // Date cell is not a food cell
                    onCurrentCellFoodItemsChange = { /* No-op for non-food cell */ }
                )
            }
            
            // Blood Sugar Values
            Row(
                modifier = Modifier.weight(3f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val cellKey = entryIndex to "morningBloodSugarLevel"
                val focusRequester = focusRequesters.getOrPut(cellKey) { FocusRequester() }
                val cellValue = cellTextFieldValues.getOrPut(cellKey) { TextFieldValue(entry.morningBloodSugarLevel) }
                EditableTableCell(
                    value = cellValue,
                    onValueChange = { 
                        cellTextFieldValues[cellKey] = it
                        onEntryChanged(entryIndex, entry.copy(morningBloodSugarLevel = it.text))
                    },
                    modifier = Modifier.weight(1f),
                    hint = "0",
                    isDateCell = false,
                    isFoodCell = true,
                    isSecured = isSecured,
                    cellKey = "${entryIndex}_morningBloodSugar",
                    confirmedEditingSessions = confirmedEditingSessions,
                    onShowEditConfirmDialog = onShowEditConfirmDialog,
                    fontColor = fontColor,
                    gridColor = gridColor,
                    isFocused = (focusedCell == cellKey),
                    focusRequester = focusRequester,
                    onRequestFocus = { onRequestFocus(cellKey) },
                    foodPresets = foodPresets,
                    setFoodPresets = setFoodPresets,
                    currentCellFoodItems = entry.foodEntriesByColumn["morningBloodSugarLevel"] ?: emptyList(),
                    onCurrentCellFoodItemsChange = { newFoodItemsForCell -> 
                        val updatedFoodMap = entry.foodEntriesByColumn + ("morningBloodSugarLevel" to newFoodItemsForCell)
                        onEntryChanged(entryIndex, entry.copy(foodEntriesByColumn = updatedFoodMap))
                    }
                )
                val cellKey2 = entryIndex to "afternoonBloodSugarLevel"
                val focusRequester2 = focusRequesters.getOrPut(cellKey2) { FocusRequester() }
                val cellValue2 = cellTextFieldValues.getOrPut(cellKey2) { TextFieldValue(entry.afternoonBloodSugarLevel) }
                EditableTableCell(
                    value = cellValue2,
                    onValueChange = { 
                        cellTextFieldValues[cellKey2] = it
                        onEntryChanged(entryIndex, entry.copy(afternoonBloodSugarLevel = it.text))
                    },
                    modifier = Modifier.weight(1f),
                    hint = "0",
                    isDateCell = false,
                    isFoodCell = true,
                    isSecured = isSecured,
                    cellKey = "${entryIndex}_afternoonBloodSugar",
                    confirmedEditingSessions = confirmedEditingSessions,
                    onShowEditConfirmDialog = onShowEditConfirmDialog,
                    fontColor = fontColor,
                    gridColor = gridColor,
                    isFocused = (focusedCell == cellKey2),
                    focusRequester = focusRequester2,
                    onRequestFocus = { onRequestFocus(cellKey2) },
                    foodPresets = foodPresets,
                    setFoodPresets = setFoodPresets,
                    currentCellFoodItems = entry.foodEntriesByColumn["afternoonBloodSugarLevel"] ?: emptyList(),
                    onCurrentCellFoodItemsChange = { newFoodItemsForCell -> 
                        val updatedFoodMap = entry.foodEntriesByColumn + ("afternoonBloodSugarLevel" to newFoodItemsForCell)
                        onEntryChanged(entryIndex, entry.copy(foodEntriesByColumn = updatedFoodMap))
                    }
                )
                val cellKey3 = entryIndex to "eveningBloodSugarLevel"
                val focusRequester3 = focusRequesters.getOrPut(cellKey3) { FocusRequester() }
                val cellValue3 = cellTextFieldValues.getOrPut(cellKey3) { TextFieldValue(entry.eveningBloodSugarLevel) }
                EditableTableCell(
                    value = cellValue3,
                    onValueChange = { 
                        cellTextFieldValues[cellKey3] = it
                        onEntryChanged(entryIndex, entry.copy(eveningBloodSugarLevel = it.text))
                    },
                    modifier = Modifier.weight(1f),
                    hint = "0",
                    isDateCell = false,
                    isFoodCell = true,
                    isSecured = isSecured,
                    cellKey = "${entryIndex}_eveningBloodSugar",
                    confirmedEditingSessions = confirmedEditingSessions,
                    onShowEditConfirmDialog = onShowEditConfirmDialog,
                    fontColor = fontColor,
                    gridColor = gridColor,
                    isFocused = (focusedCell == cellKey3),
                    focusRequester = focusRequester3,
                    onRequestFocus = { onRequestFocus(cellKey3) },
                    foodPresets = foodPresets,
                    setFoodPresets = setFoodPresets,
                    currentCellFoodItems = entry.foodEntriesByColumn["eveningBloodSugarLevel"] ?: emptyList(),
                    onCurrentCellFoodItemsChange = { newFoodItemsForCell -> 
                        val updatedFoodMap = entry.foodEntriesByColumn + ("eveningBloodSugarLevel" to newFoodItemsForCell)
                        onEntryChanged(entryIndex, entry.copy(foodEntriesByColumn = updatedFoodMap))
                    }
                )
                val cellKey4 = entryIndex to "nightBloodSugarLevel"
                val focusRequester4 = focusRequesters.getOrPut(cellKey4) { FocusRequester() }
                val cellValue4 = cellTextFieldValues.getOrPut(cellKey4) { TextFieldValue(entry.nightBloodSugarLevel) }
                EditableTableCell(
                    value = cellValue4,
                    onValueChange = { 
                        cellTextFieldValues[cellKey4] = it
                        onEntryChanged(entryIndex, entry.copy(nightBloodSugarLevel = it.text))
                    },
                    modifier = Modifier.weight(1f),
                    hint = "0",
                    isDateCell = false,
                    isFoodCell = true,
                    isSecured = isSecured,
                    cellKey = "${entryIndex}_nightBloodSugar",
                    confirmedEditingSessions = confirmedEditingSessions,
                    onShowEditConfirmDialog = onShowEditConfirmDialog,
                    fontColor = fontColor,
                    gridColor = gridColor,
                    isFocused = (focusedCell == cellKey4),
                    focusRequester = focusRequester4,
                    onRequestFocus = { onRequestFocus(cellKey4) },
                    foodPresets = foodPresets,
                    setFoodPresets = setFoodPresets,
                    currentCellFoodItems = entry.foodEntriesByColumn["nightBloodSugarLevel"] ?: emptyList(),
                    onCurrentCellFoodItemsChange = { newFoodItemsForCell -> 
                        val updatedFoodMap = entry.foodEntriesByColumn + ("nightBloodSugarLevel" to newFoodItemsForCell)
                        onEntryChanged(entryIndex, entry.copy(foodEntriesByColumn = updatedFoodMap))
                    }
                )
            }
            
            // Insulin Values
            Row(
                modifier = Modifier
                    .weight(2.2f)
                    .background(
                        color = insulinColumnColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val cellKey = entryIndex to "morningInsulin"
                val focusRequester = focusRequesters.getOrPut(cellKey) { FocusRequester() }
                val cellValue = cellTextFieldValues.getOrPut(cellKey) { TextFieldValue(entry.morningInsulin) }
                EditableTableCell(
                    value = cellValue,
                    onValueChange = { 
                        cellTextFieldValues[cellKey] = it
                        onEntryChanged(entryIndex, entry.copy(morningInsulin = it.text))
                    },
                    modifier = Modifier.weight(1f),
                    hint = "0",
                    isDateCell = false,
                    isFoodCell = true,
                    isInsulinCell = true,
                    isSecured = isSecured,
                    cellKey = "${entryIndex}_morningInsulin",
                    confirmedEditingSessions = confirmedEditingSessions,
                    onShowEditConfirmDialog = onShowEditConfirmDialog,
                    fontColor = fontColor,
                    gridColor = gridColor,
                    isFocused = (focusedCell == cellKey),
                    focusRequester = focusRequester,
                    onRequestFocus = { onRequestFocus(cellKey) },
                    foodPresets = foodPresets,
                    setFoodPresets = setFoodPresets,
                    currentCellFoodItems = entry.foodEntriesByColumn["morningInsulin"] ?: emptyList(),
                    onCurrentCellFoodItemsChange = { newFoodItemsForCell -> 
                        val updatedFoodMap = entry.foodEntriesByColumn + ("morningInsulin" to newFoodItemsForCell)
                        onEntryChanged(entryIndex, entry.copy(foodEntriesByColumn = updatedFoodMap))
                    }
                )
                val cellKey2 = entryIndex to "afternoonInsulin"
                val focusRequester2 = focusRequesters.getOrPut(cellKey2) { FocusRequester() }
                val cellValue2 = cellTextFieldValues.getOrPut(cellKey2) { TextFieldValue(entry.afternoonInsulin) }
                EditableTableCell(
                    value = cellValue2,
                    onValueChange = { 
                        cellTextFieldValues[cellKey2] = it
                        onEntryChanged(entryIndex, entry.copy(afternoonInsulin = it.text))
                    },
                    modifier = Modifier.weight(1f),
                    hint = "0",
                    isDateCell = false,
                    isFoodCell = true,
                    isInsulinCell = true,
                    isSecured = isSecured,
                    cellKey = "${entryIndex}_afternoonInsulin",
                    confirmedEditingSessions = confirmedEditingSessions,
                    onShowEditConfirmDialog = onShowEditConfirmDialog,
                    fontColor = fontColor,
                    gridColor = gridColor,
                    isFocused = (focusedCell == cellKey2),
                    focusRequester = focusRequester2,
                    onRequestFocus = { onRequestFocus(cellKey2) },
                    foodPresets = foodPresets,
                    setFoodPresets = setFoodPresets,
                    currentCellFoodItems = entry.foodEntriesByColumn["afternoonInsulin"] ?: emptyList(),
                    onCurrentCellFoodItemsChange = { newFoodItemsForCell -> 
                        val updatedFoodMap = entry.foodEntriesByColumn + ("afternoonInsulin" to newFoodItemsForCell)
                        onEntryChanged(entryIndex, entry.copy(foodEntriesByColumn = updatedFoodMap))
                    }
                )
                val cellKey3 = entryIndex to "eveningInsulin"
                val focusRequester3 = focusRequesters.getOrPut(cellKey3) { FocusRequester() }
                val cellValue3 = cellTextFieldValues.getOrPut(cellKey3) { TextFieldValue(entry.eveningInsulin) }
                EditableTableCell(
                    value = cellValue3,
                    onValueChange = { 
                        cellTextFieldValues[cellKey3] = it
                        onEntryChanged(entryIndex, entry.copy(eveningInsulin = it.text))
                    },
                    modifier = Modifier.weight(1f),
                    hint = "0",
                    isDateCell = false,
                    isFoodCell = true,
                    isInsulinCell = true,
                    isSecured = isSecured,
                    cellKey = "${entryIndex}_eveningInsulin",
                    confirmedEditingSessions = confirmedEditingSessions,
                    onShowEditConfirmDialog = onShowEditConfirmDialog,
                    fontColor = fontColor,
                    gridColor = gridColor,
                    isFocused = (focusedCell == cellKey3),
                    focusRequester = focusRequester3,
                    onRequestFocus = { onRequestFocus(cellKey3) },
                    foodPresets = foodPresets,
                    setFoodPresets = setFoodPresets,
                    currentCellFoodItems = entry.foodEntriesByColumn["eveningInsulin"] ?: emptyList(),
                    onCurrentCellFoodItemsChange = { newFoodItemsForCell -> 
                        val updatedFoodMap = entry.foodEntriesByColumn + ("eveningInsulin" to newFoodItemsForCell)
                        onEntryChanged(entryIndex, entry.copy(foodEntriesByColumn = updatedFoodMap))
                    }
                )
            }
            
            // Remarks Column
            val cellKey = entryIndex to "remarks"
            CommentCell(
                value = entry.remarks,
                onValueChange = { onEntryChanged(entryIndex, entry.copy(remarks = it)) },
                modifier = Modifier.weight(1.5f),
                fontColor = fontColor,
                gridColor = gridColor
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete this entry?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteEntry(entryIndex)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helper function to create PDF document
fun createPDFDocument(validEntries: List<DiabetesEntry>, language: String): PdfDocument {
    val pdfDocument = PdfDocument()
    val pageWidth = 595 // A4 width in points
    val pageHeight = 842 // A4 height in points
    
    // Get labels based on language (using shorter headers for better fit)
    val labels = when (language) {
        "German" -> mapOf(
            "title" to "Diabetes Tracker - Datenexport",
            "date" to "Datum",
            "morning" to "Morgen",
            "afternoon" to "Mittag",
            "evening" to "Abend",
            "night" to "Nacht",
            "morningInsulin" to "Ins. Morgen",
            "afternoonInsulin" to "Ins. Mittag",
            "eveningInsulin" to "Ins. Abend",
            "remarks" to "Bemerkungen"
        )
        "Spanish" -> mapOf(
            "title" to "Diabetes Tracker - Exportación de Datos",
            "date" to "Fecha",
            "morning" to "Mañana",
            "afternoon" to "Mediodía",
            "evening" to "Tarde",
            "night" to "Noche",
            "morningInsulin" to "Ins. Mañana",
            "afternoonInsulin" to "Ins. Mediodía",
            "eveningInsulin" to "Ins. Tarde",
            "remarks" to "Notas"
        )
        else -> mapOf(
            "title" to "Diabetes Tracker - Data Export",
            "date" to "Date",
            "morning" to "Morning",
            "afternoon" to "Afternoon",
            "evening" to "Evening",
            "night" to "Night",
            "morningInsulin" to "Morn. Ins.",
            "afternoonInsulin" to "Aftn. Ins.",
            "eveningInsulin" to "Even. Ins.",
            "remarks" to "Remarks"
        )
    }
    
    // Calculate pages needed (entries per page)
    val entriesPerPage = 25
    val totalPages = (validEntries.size + entriesPerPage - 1) / entriesPerPage
    
    for (pageNum in 0 until totalPages) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum + 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        // Paint objects
        val titlePaint = Paint().apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.BLACK
        }
        
        val headerPaint = Paint().apply {
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.BLACK
        }
        
        val dataPaint = Paint().apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = android.graphics.Color.BLACK
        }
        
        val linePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            strokeWidth = 1f
        }
        
        // Draw title
        canvas.drawText(labels["title"] ?: "Diabetes Tracker", 50f, 50f, titlePaint)
        
        // Draw table headers
        val startY = 100f
        val rowHeight = 25f
        // Adjusted column positions for better spacing and readability
        val colPositions = floatArrayOf(40f, 110f, 170f, 230f, 290f, 350f, 420f, 490f, 560f)
        
        // Draw header row
        canvas.drawText(labels["date"] ?: "Date", colPositions[0], startY, headerPaint)
        canvas.drawText(labels["morning"] ?: "Morning", colPositions[1], startY, headerPaint)
        canvas.drawText(labels["afternoon"] ?: "Afternoon", colPositions[2], startY, headerPaint)
        canvas.drawText(labels["evening"] ?: "Evening", colPositions[3], startY, headerPaint)
        canvas.drawText(labels["night"] ?: "Night", colPositions[4], startY, headerPaint)
        canvas.drawText(labels["morningInsulin"] ?: "M.Ins", colPositions[5], startY, headerPaint)
        canvas.drawText(labels["afternoonInsulin"] ?: "A.Ins", colPositions[6], startY, headerPaint)
        canvas.drawText(labels["eveningInsulin"] ?: "E.Ins", colPositions[7], startY, headerPaint)
        canvas.drawText(labels["remarks"] ?: "Remarks", colPositions[8], startY, headerPaint)
        
        // Draw horizontal line under header
        canvas.drawLine(40f, startY + 10f, 620f, startY + 10f, linePaint)
        
        // Draw data rows
        val startIndex = pageNum * entriesPerPage
        val endIndex = minOf(startIndex + entriesPerPage, validEntries.size)
        
        for (i in startIndex until endIndex) {
            val entry = validEntries[i]
            val rowY = startY + ((i - startIndex + 1) * rowHeight) + 15f
            
            // Format date
            val formattedDate = if (entry.date.isNotBlank()) {
                try {
                    val date = LocalDate.parse(entry.date)
                    date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yy"))
                } catch (e: Exception) {
                    entry.date
                }
            } else ""
            
            canvas.drawText(formattedDate, colPositions[0], rowY, dataPaint)
            canvas.drawText(entry.morningBloodSugarLevel, colPositions[1], rowY, dataPaint)
            canvas.drawText(entry.afternoonBloodSugarLevel, colPositions[2], rowY, dataPaint)
            canvas.drawText(entry.eveningBloodSugarLevel, colPositions[3], rowY, dataPaint)
            canvas.drawText(entry.nightBloodSugarLevel, colPositions[4], rowY, dataPaint)
            canvas.drawText(entry.morningInsulin, colPositions[5], rowY, dataPaint)
            canvas.drawText(entry.afternoonInsulin, colPositions[6], rowY, dataPaint)
            canvas.drawText(entry.eveningInsulin, colPositions[7], rowY, dataPaint)
            
            // Truncate remarks if too long
            val remarks = if (entry.remarks.length > 15) {
                entry.remarks.take(12) + "..."
            } else entry.remarks
            canvas.drawText(remarks, colPositions[8], rowY, dataPaint)
        }
        
        // Draw page number
        canvas.drawText("Page ${pageNum + 1} of $totalPages", 500f, pageHeight - 30f, dataPaint)
        
        pdfDocument.finishPage(page)
    }
    
    return pdfDocument
}

// PDF Generation Function for user-selected file (more reliable)
fun generatePDFToFile(context: Context, entries: List<DiabetesEntry>, language: String, fileUri: Uri) {
    try {
        // Filter entries with valid dates and data
        val validEntries = entries.filter { entry ->
            entry.date.isNotBlank() && (
                entry.morningBloodSugarLevel.isNotBlank() ||
                entry.afternoonBloodSugarLevel.isNotBlank() ||
                entry.eveningBloodSugarLevel.isNotBlank() ||
                entry.nightBloodSugarLevel.isNotBlank() ||
                entry.morningInsulin.isNotBlank() ||
                entry.afternoonInsulin.isNotBlank() ||
                entry.eveningInsulin.isNotBlank() ||
                entry.remarks.isNotBlank()
            )
        }
        
        if (validEntries.isEmpty()) {
            Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create and write PDF directly to the selected file
        context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
            val pdfDocument = createPDFDocument(validEntries, language)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
        }
        
        Toast.makeText(context, "PDF exported successfully", Toast.LENGTH_LONG).show()
        
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// PDF Generation Function for user-selected folder (legacy)
fun generatePDFToFolder(context: Context, entries: List<DiabetesEntry>, language: String, folderUri: Uri) {
    try {
        // Create PDF document
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 width in points
        val pageHeight = 842 // A4 height in points
        
        // Get labels based on language
        val labels = when (language) {
            "German" -> mapOf(
                "title" to "Diabetes Tracker - Datenexport",
                "date" to "Datum",
                "morning" to "Morgen",
                "afternoon" to "Mittag",
                "evening" to "Abend",
                "night" to "Nacht",
                "morningInsulin" to "Insulin Morgen",
                "afternoonInsulin" to "Insulin Mittag",
                "eveningInsulin" to "Insulin Abend",
                "remarks" to "Bemerkungen"
            )
            "Spanish" -> mapOf(
                "title" to "Diabetes Tracker - Exportación de Datos",
                "date" to "Fecha",
                "morning" to "Mañana",
                "afternoon" to "Mediodía",
                "evening" to "Tarde",
                "night" to "Noche",
                "morningInsulin" to "Insulina Mañana",
                "afternoonInsulin" to "Insulina Mediodía",
                "eveningInsulin" to "Insulina Tarde",
                "remarks" to "Notas"
            )
            else -> mapOf(
                "title" to "Diabetes Tracker - Data Export",
                "date" to "Date",
                "morning" to "Morning",
                "afternoon" to "Afternoon",
                "evening" to "Evening",
                "night" to "Night",
                "morningInsulin" to "Morning Insulin",
                "afternoonInsulin" to "Afternoon Insulin",
                "eveningInsulin" to "Evening Insulin",
                "remarks" to "Remarks"
            )
        }
        
        // Filter entries with valid dates and data
        val validEntries = entries.filter { entry ->
            entry.date.isNotBlank() && (
                entry.morningBloodSugarLevel.isNotBlank() ||
                entry.afternoonBloodSugarLevel.isNotBlank() ||
                entry.eveningBloodSugarLevel.isNotBlank() ||
                entry.nightBloodSugarLevel.isNotBlank() ||
                entry.morningInsulin.isNotBlank() ||
                entry.afternoonInsulin.isNotBlank() ||
                entry.eveningInsulin.isNotBlank() ||
                entry.remarks.isNotBlank()
            )
        }
        
        if (validEntries.isEmpty()) {
            Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Calculate pages needed (entries per page)
        val entriesPerPage = 25
        val totalPages = (validEntries.size + entriesPerPage - 1) / entriesPerPage
        
        for (pageNum in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            // Paint objects
            val titlePaint = Paint().apply {
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = android.graphics.Color.BLACK
            }
            
            val headerPaint = Paint().apply {
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = android.graphics.Color.BLACK
            }
            
            val dataPaint = Paint().apply {
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                color = android.graphics.Color.BLACK
            }
            
            val linePaint = Paint().apply {
                color = android.graphics.Color.BLACK
                strokeWidth = 1f
            }
            
            // Draw title
            canvas.drawText(labels["title"] ?: "Diabetes Tracker", 50f, 50f, titlePaint)
            
            // Draw table headers
            val startY = 100f
            val rowHeight = 25f
            val colWidths = floatArrayOf(80f, 60f, 60f, 60f, 60f, 60f, 60f, 60f, 100f)
            val colPositions = floatArrayOf(50f, 130f, 190f, 250f, 310f, 370f, 430f, 490f, 550f)
            
            // Draw header row
            canvas.drawText(labels["date"] ?: "Date", colPositions[0], startY, headerPaint)
            canvas.drawText(labels["morning"] ?: "Morning", colPositions[1], startY, headerPaint)
            canvas.drawText(labels["afternoon"] ?: "Afternoon", colPositions[2], startY, headerPaint)
            canvas.drawText(labels["evening"] ?: "Evening", colPositions[3], startY, headerPaint)
            canvas.drawText(labels["night"] ?: "Night", colPositions[4], startY, headerPaint)
            canvas.drawText(labels["morningInsulin"] ?: "M.Ins", colPositions[5], startY, headerPaint)
            canvas.drawText(labels["afternoonInsulin"] ?: "A.Ins", colPositions[6], startY, headerPaint)
            canvas.drawText(labels["eveningInsulin"] ?: "E.Ins", colPositions[7], startY, headerPaint)
            canvas.drawText(labels["remarks"] ?: "Remarks", colPositions[8], startY, headerPaint)
            
            // Draw horizontal line under header
            canvas.drawLine(50f, startY + 10f, 650f, startY + 10f, linePaint)
            
            // Draw data rows
            val startIndex = pageNum * entriesPerPage
            val endIndex = minOf(startIndex + entriesPerPage, validEntries.size)
            
            for (i in startIndex until endIndex) {
                val entry = validEntries[i]
                val rowY = startY + ((i - startIndex + 1) * rowHeight) + 15f
                
                // Format date
                val formattedDate = if (entry.date.isNotBlank()) {
                    try {
                        val date = LocalDate.parse(entry.date)
                        date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yy"))
                    } catch (e: Exception) {
                        entry.date
                    }
                } else ""
                
                canvas.drawText(formattedDate, colPositions[0], rowY, dataPaint)
                canvas.drawText(entry.morningBloodSugarLevel, colPositions[1], rowY, dataPaint)
                canvas.drawText(entry.afternoonBloodSugarLevel, colPositions[2], rowY, dataPaint)
                canvas.drawText(entry.eveningBloodSugarLevel, colPositions[3], rowY, dataPaint)
                canvas.drawText(entry.nightBloodSugarLevel, colPositions[4], rowY, dataPaint)
                canvas.drawText(entry.morningInsulin, colPositions[5], rowY, dataPaint)
                canvas.drawText(entry.afternoonInsulin, colPositions[6], rowY, dataPaint)
                canvas.drawText(entry.eveningInsulin, colPositions[7], rowY, dataPaint)
                
                // Truncate remarks if too long
                val remarks = if (entry.remarks.length > 15) {
                    entry.remarks.take(12) + "..."
                } else entry.remarks
                canvas.drawText(remarks, colPositions[8], rowY, dataPaint)
            }
            
            // Draw page number
            canvas.drawText("Page ${pageNum + 1} of $totalPages", 500f, pageHeight - 30f, dataPaint)
            
            pdfDocument.finishPage(page)
        }
        
        // Create file in selected folder using DocumentsContract
        val fileName = "diabetes_data_${System.currentTimeMillis()}.pdf"
        
        try {
            // Log the folder URI for debugging
            android.util.Log.d("PDFExport", "Folder URI: $folderUri")
            android.util.Log.d("PDFExport", "File name: $fileName")
            
            val fileUri = DocumentsContract.createDocument(
                context.contentResolver,
                folderUri,
                "application/pdf",
                fileName
            )
            
            android.util.Log.d("PDFExport", "Created file URI: $fileUri")
            
            if (fileUri != null) {
                context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                Toast.makeText(context, "PDF exported successfully to selected folder", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to create PDF file in selected folder", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: try to save to app's external files directory
            try {
                val fallbackFile = File(context.getExternalFilesDir(null), fileName)
                pdfDocument.writeTo(FileOutputStream(fallbackFile))
                Toast.makeText(context, "PDF exported to app folder: ${fallbackFile.absolutePath}", Toast.LENGTH_LONG).show()
            } catch (fallbackException: Exception) {
                fallbackException.printStackTrace()
                Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        pdfDocument.close()
        
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// PDF Generation Function (fallback for app's external files directory)
fun generatePDF(context: Context, entries: List<DiabetesEntry>, language: String) {
    try {
        // Create PDF document
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 width in points
        val pageHeight = 842 // A4 height in points
        
        // Get labels based on language
        val labels = when (language) {
            "German" -> mapOf(
                "title" to "Diabetes Tracker - Datenexport",
                "date" to "Datum",
                "morning" to "Morgen",
                "afternoon" to "Mittag",
                "evening" to "Abend",
                "night" to "Nacht",
                "morningInsulin" to "Insulin Morgen",
                "afternoonInsulin" to "Insulin Mittag",
                "eveningInsulin" to "Insulin Abend",
                "remarks" to "Bemerkungen"
            )
            "Spanish" -> mapOf(
                "title" to "Diabetes Tracker - Exportación de Datos",
                "date" to "Fecha",
                "morning" to "Mañana",
                "afternoon" to "Mediodía",
                "evening" to "Tarde",
                "night" to "Noche",
                "morningInsulin" to "Insulina Mañana",
                "afternoonInsulin" to "Insulina Mediodía",
                "eveningInsulin" to "Insulina Tarde",
                "remarks" to "Notas"
            )
            else -> mapOf(
                "title" to "Diabetes Tracker - Data Export",
                "date" to "Date",
                "morning" to "Morning",
                "afternoon" to "Afternoon",
                "evening" to "Evening",
                "night" to "Night",
                "morningInsulin" to "Morning Insulin",
                "afternoonInsulin" to "Afternoon Insulin",
                "eveningInsulin" to "Evening Insulin",
                "remarks" to "Remarks"
            )
        }
        
        // Filter entries with valid dates and data
        val validEntries = entries.filter { entry ->
            entry.date.isNotBlank() && (
                entry.morningBloodSugarLevel.isNotBlank() ||
                entry.afternoonBloodSugarLevel.isNotBlank() ||
                entry.eveningBloodSugarLevel.isNotBlank() ||
                entry.nightBloodSugarLevel.isNotBlank() ||
                entry.morningInsulin.isNotBlank() ||
                entry.afternoonInsulin.isNotBlank() ||
                entry.eveningInsulin.isNotBlank() ||
                entry.remarks.isNotBlank()
            )
        }
        
        if (validEntries.isEmpty()) {
            Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Calculate pages needed (entries per page)
        val entriesPerPage = 25
        val totalPages = (validEntries.size + entriesPerPage - 1) / entriesPerPage
        
        for (pageNum in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            // Paint objects
            val titlePaint = Paint().apply {
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = android.graphics.Color.BLACK
            }
            
            val headerPaint = Paint().apply {
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = android.graphics.Color.BLACK
            }
            
            val dataPaint = Paint().apply {
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                color = android.graphics.Color.BLACK
            }
            
            val linePaint = Paint().apply {
                color = android.graphics.Color.BLACK
                strokeWidth = 1f
            }
            
            // Draw title
            canvas.drawText(labels["title"] ?: "Diabetes Tracker", 50f, 50f, titlePaint)
            
            // Draw table headers
            val startY = 100f
            val rowHeight = 25f
            val colWidths = floatArrayOf(80f, 60f, 60f, 60f, 60f, 60f, 60f, 60f, 100f)
            val colPositions = floatArrayOf(50f, 130f, 190f, 250f, 310f, 370f, 430f, 490f, 550f)
            
            // Draw header row
            canvas.drawText(labels["date"] ?: "Date", colPositions[0], startY, headerPaint)
            canvas.drawText(labels["morning"] ?: "Morning", colPositions[1], startY, headerPaint)
            canvas.drawText(labels["afternoon"] ?: "Afternoon", colPositions[2], startY, headerPaint)
            canvas.drawText(labels["evening"] ?: "Evening", colPositions[3], startY, headerPaint)
            canvas.drawText(labels["night"] ?: "Night", colPositions[4], startY, headerPaint)
            canvas.drawText(labels["morningInsulin"] ?: "M.Ins", colPositions[5], startY, headerPaint)
            canvas.drawText(labels["afternoonInsulin"] ?: "A.Ins", colPositions[6], startY, headerPaint)
            canvas.drawText(labels["eveningInsulin"] ?: "E.Ins", colPositions[7], startY, headerPaint)
            canvas.drawText(labels["remarks"] ?: "Remarks", colPositions[8], startY, headerPaint)
            
            // Draw horizontal line under header
            canvas.drawLine(50f, startY + 10f, 650f, startY + 10f, linePaint)
            
            // Draw data rows
            val startIndex = pageNum * entriesPerPage
            val endIndex = minOf(startIndex + entriesPerPage, validEntries.size)
            
            for (i in startIndex until endIndex) {
                val entry = validEntries[i]
                val rowY = startY + ((i - startIndex + 1) * rowHeight) + 15f
                
                // Format date
                val formattedDate = if (entry.date.isNotBlank()) {
                    try {
                        val date = LocalDate.parse(entry.date)
                        date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yy"))
                    } catch (e: Exception) {
                        entry.date
                    }
                } else ""
                
                canvas.drawText(formattedDate, colPositions[0], rowY, dataPaint)
                canvas.drawText(entry.morningBloodSugarLevel, colPositions[1], rowY, dataPaint)
                canvas.drawText(entry.afternoonBloodSugarLevel, colPositions[2], rowY, dataPaint)
                canvas.drawText(entry.eveningBloodSugarLevel, colPositions[3], rowY, dataPaint)
                canvas.drawText(entry.nightBloodSugarLevel, colPositions[4], rowY, dataPaint)
                canvas.drawText(entry.morningInsulin, colPositions[5], rowY, dataPaint)
                canvas.drawText(entry.afternoonInsulin, colPositions[6], rowY, dataPaint)
                canvas.drawText(entry.eveningInsulin, colPositions[7], rowY, dataPaint)
                
                // Truncate remarks if too long
                val remarks = if (entry.remarks.length > 15) {
                    entry.remarks.take(12) + "..."
                } else entry.remarks
                canvas.drawText(remarks, colPositions[8], rowY, dataPaint)
            }
            
            // Draw page number
            canvas.drawText("Page ${pageNum + 1} of $totalPages", 500f, pageHeight - 30f, dataPaint)
            
            pdfDocument.finishPage(page)
        }
        
        // Save PDF to app's external files directory
        val fileName = "diabetes_data_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        
        Toast.makeText(context, "PDF exported to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// TXT Generation Function for user-selected file
fun generateTXTToFile(context: Context, entries: List<DiabetesEntry>, language: String, fileUri: Uri) {
    try {
        // Filter entries that have at least a date and some data
        val validEntries = entries.filter { entry ->
            entry.date.isNotBlank() && (
                entry.morningBloodSugarLevel.isNotBlank() ||
                entry.afternoonBloodSugarLevel.isNotBlank() ||
                entry.eveningBloodSugarLevel.isNotBlank() ||
                entry.nightBloodSugarLevel.isNotBlank() ||
                entry.morningInsulin.isNotBlank() ||
                entry.afternoonInsulin.isNotBlank() ||
                entry.eveningInsulin.isNotBlank() ||
                entry.remarks.isNotBlank() ||
                entry.foodEntriesByColumn.values.any { it.isNotEmpty() }
            )
        }
        
        if (validEntries.isEmpty()) {
            Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create simplified localized headers
        val headers = when(language) {
            "German" -> listOf(
                "Datum", "Morgens", "Mittags", "Abends", "Nachts",
                "Morg.Ins", "Mitt.Ins", "Aben.Ins", "Bemerkungen"
            )
            "Spanish" -> listOf(
                "Fecha", "Mañana", "Tarde", "Noche", "Madrugada",
                "Mañ.Ins", "Tar.Ins", "Noc.Ins", "Observaciones"
            )
            else -> listOf(
                "Date", "Morning", "Afternoon", "Evening", "Night",
                "Morn.Ins", "Aftn.Ins", "Even.Ins", "Remarks"
            )
        }
        
        context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
            outputStream.bufferedWriter().use { writer ->
                // Write title and separator
                writer.write("DIABETES TRACKER DATA EXPORT")
                writer.newLine()
                writer.write("=".repeat(50))
                writer.newLine()
                writer.newLine()
                
                // Calculate dynamic column widths based on header lengths and content
                val allDataRows = validEntries.map { entry ->
                    listOf(
                        entry.date,
                        entry.morningBloodSugarLevel,
                        entry.afternoonBloodSugarLevel,
                        entry.eveningBloodSugarLevel,
                        entry.nightBloodSugarLevel,
                        entry.morningInsulin,
                        entry.afternoonInsulin,
                        entry.eveningInsulin,
                        entry.remarks
                    )
                }
                
                // Calculate optimal column widths (minimum header length + 2 spaces padding)
                val columnWidths = headers.mapIndexed { index, header ->
                    val headerLength = header.length
                    val maxDataLength = allDataRows.maxOfOrNull { it[index].length } ?: 0
                    maxOf(headerLength, maxDataLength) + 2 // Add 2 spaces padding
                }
                
                // Write header row with dynamic width formatting
                val headerLine = headers.mapIndexed { index, header ->
                    header.padEnd(columnWidths[index])
                }.joinToString("")
                writer.write(headerLine)
                writer.newLine()
                writer.write("-".repeat(headerLine.length))
                writer.newLine()
                
                // Write data rows with dynamic width formatting
                validEntries.forEach { entry ->
                    val row = listOf(
                        entry.date,
                        entry.morningBloodSugarLevel,
                        entry.afternoonBloodSugarLevel,
                        entry.eveningBloodSugarLevel,
                        entry.nightBloodSugarLevel,
                        entry.morningInsulin,
                        entry.afternoonInsulin,
                        entry.eveningInsulin,
                        entry.remarks
                    )
                    val dataLine = row.mapIndexed { index, value ->
                        value.padEnd(columnWidths[index])
                    }.joinToString("")
                    writer.write(dataLine)
                    writer.newLine()
                }
                
                // Add separator before CSV section
                writer.newLine()
                writer.write("=".repeat(50))
                writer.newLine()
                writer.write("CSV FORMAT (for spreadsheet import):")
                writer.newLine()
                writer.write("-".repeat(50))
                writer.newLine()
                
                // Also include CSV format for easy spreadsheet import
                writer.write(headers.joinToString(","))
                writer.newLine()
                
                validEntries.forEach { entry ->
                    val csvRow = listOf(
                        "\"${entry.date}\"",
                        "\"${entry.morningBloodSugarLevel}\"",
                        "\"${entry.afternoonBloodSugarLevel}\"",
                        "\"${entry.eveningBloodSugarLevel}\"",
                        "\"${entry.nightBloodSugarLevel}\"",
                        "\"${entry.morningInsulin}\"",
                        "\"${entry.afternoonInsulin}\"",
                        "\"${entry.eveningInsulin}\"",
                        "\"${entry.remarks}\""
                    )
                    writer.write(csvRow.joinToString(","))
                    writer.newLine()
                }
                
                // Add summary information
                writer.newLine()
                writer.write("\n// Export Summary")
                writer.newLine()
                writer.write("// Total entries: ${validEntries.size}")
                writer.newLine()
                writer.write("// Export date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
                writer.newLine()
                writer.write("// Language: $language")
            }
        }
        
        Toast.makeText(context, 
            when(language) {
                "German" -> "TXT erfolgreich exportiert"
                "Spanish" -> "TXT exportado exitosamente"
                else -> "TXT exported successfully"
            }, 
            Toast.LENGTH_LONG
        ).show()
        
    } catch (e: Exception) {
        Toast.makeText(context, 
            when(language) {
                "German" -> "Fehler beim TXT-Export: ${e.message}"
                "Spanish" -> "Error al exportar TXT: ${e.message}"
                else -> "Failed to export TXT: ${e.message}"
            }, 
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun AdvancedSettingsDialog(
    onDismissRequest: () -> Unit,
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit
) {
    val context = LocalContext.current
    var autoSaveOnExit by remember { mutableStateOf(SettingsManager.loadAutoSaveOnExit(context)) }
    val selectedLanguage = settings.language
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = when (selectedLanguage) {
                    "German" -> "Erweiterte Einstellungen"
                    "Spanish" -> "Configuración avanzada"
                    else -> "Advanced Settings"
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Auto-save on exit toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            autoSaveOnExit = !autoSaveOnExit
                            SettingsManager.saveAutoSaveOnExit(context, autoSaveOnExit)
                        }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Automatisch speichern beim Beenden"
                                "Spanish" -> "Guardar automáticamente al salir"
                                else -> "Auto-save on exit"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Speichert alle Änderungen automatisch und beendet die App mit doppeltem Zurück-Drücken"
                                "Spanish" -> "Guarda automáticamente todos los cambios y cierra la app con doble toque atrás"
                                else -> "Automatically saves all changes and exits app with double back press"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = autoSaveOnExit,
                        onCheckedChange = { 
                            autoSaveOnExit = it
                            SettingsManager.saveAutoSaveOnExit(context, autoSaveOnExit)
                        }
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Zoom settings section
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Zoom-Einstellungen"
                        "Spanish" -> "Configuración de zoom"
                        else -> "Zoom Settings"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Scale slider
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Skalierung: ${(settings.scale * 100).toInt()}%"
                        "Spanish" -> "Escala: ${(settings.scale * 100).toInt()}%"
                        else -> "Scale: ${(settings.scale * 100).toInt()}%"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Slider(
                    value = settings.scale,
                    onValueChange = { newScale ->
                        val newSettings = settings.copy(
                            scale = newScale,
                            zoomPercentage = (newScale * 100).toInt()
                        )
                        onSettingsChanged(newSettings)
                    },
                    valueRange = 0.5f..2.0f,
                    steps = 15,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                // Width scale slider
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Breitenskalierung: ${String.format("%.1f", settings.widthScale)}x"
                        "Spanish" -> "Escala de ancho: ${String.format("%.1f", settings.widthScale)}x"
                        else -> "Width Scale: ${String.format("%.1f", settings.widthScale)}x"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Slider(
                    value = settings.widthScale,
                    onValueChange = { newWidthScale ->
                        val newSettings = settings.copy(widthScale = newWidthScale)
                        onSettingsChanged(newSettings)
                    },
                    valueRange = 1.0f..4.0f,
                    steps = 30,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Fertig"
                        "Spanish" -> "Listo"
                        else -> "Done"
                    }
                )
            }
        }
    )
}

@Serializable
data class DiabetesEntry(
    val id: String = UUID.randomUUID().toString(),
    var date: String = "",
    var morningBloodSugarLevel: String = "",
    var afternoonBloodSugarLevel: String = "",
    var eveningBloodSugarLevel: String = "",
    var nightBloodSugarLevel: String = "",
    var morningInsulin: String = "",
    var afternoonInsulin: String = "",
    var eveningInsulin: String = "",
    var remarks: String = "",
    var foodEntriesByColumn: Map<String, List<FoodPreset>> = emptyMap()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide both status bar and navigation bar
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE or
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
            android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
        
        // Set flags to keep system bars hidden even when user interacts
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        
        // Prevent keyboard from affecting layout
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        
        setContent {
            DiabetesTrackerTheme {
                DiabetesTrackerApp()
            }
        }
    }
}