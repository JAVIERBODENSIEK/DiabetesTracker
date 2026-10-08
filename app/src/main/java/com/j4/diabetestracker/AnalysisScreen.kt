package com.j4.diabetestracker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import java.time.Instant
import java.time.Month
import java.time.YearMonth
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private enum class AnalysisDateRangeOption { ALL_TIME, ONE_YEAR, ONE_MONTH, CUSTOM }
private enum class PatternTypeVisibilityOption { DETECTED_ONLY, SHOW_ALL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    patterns: List<DetectedPattern>,
    entries: List<DiabetesEntry>,
    customMarkers: List<CustomMarkerType>,
    context: Context,
    selectedLanguage: String = "English",
    onDismiss: () -> Unit,
    onDismissPattern: (String) -> Unit,
    onIgnoreFood: (String, String, Int, ClosedRange<LocalDate>?) -> Unit, // foodName, patternId, daysToAnalyze, dateRange
    initialDateRange: ClosedRange<LocalDate>? = null,
    initialDaysToAnalyze: Int = 30,
    onDateRangeChanged: (Int, ClosedRange<LocalDate>?) -> Unit = { _, _ -> },
    onNavigateToDate: (String) -> Unit = {}, // Navigate to specific date in main table
    onExportTXT: () -> Unit = {},
    onExportPDF: () -> Unit = {}
) {
    // Note: selectedLanguage parameter kept for dynamic content translation (marker names, time-of-day, suggestions)
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("txt") } // "txt" or "pdf"
    
    // Filter state
    var filterRiskLevel by remember { mutableStateOf<String?>(null) } // "high", "medium", "low"
    var filterPatternType by remember { mutableStateOf<PatternType?>(null) }
    var filterFoodName by remember { mutableStateOf<String?>(null) }
    var showPatternWorkspace by remember { mutableStateOf(false) }
    var activeDateRange by remember { mutableStateOf(initialDateRange) }
    var activeAnalysisDays by remember { mutableStateOf(initialDaysToAnalyze) }

    LaunchedEffect(initialDateRange, initialDaysToAnalyze) {
        activeDateRange = initialDateRange
        activeAnalysisDays = initialDaysToAnalyze
    }
    
    // Apply filters
    val filteredPatterns = remember(patterns, filterRiskLevel, filterPatternType, filterFoodName) {
        var result = patterns
        
        // Filter by risk level
        if (filterRiskLevel != null) {
            result = result.filter { pattern ->
                when (filterRiskLevel) {
                    "high" -> pattern.confidenceScore >= 0.8f
                    "medium" -> pattern.confidenceScore >= 0.6f && pattern.confidenceScore < 0.8f
                    "low" -> pattern.confidenceScore < 0.6f
                    else -> true
                }
            }
        }
        
        // Filter by pattern type
        if (filterPatternType != null) {
            result = result.filter { it.patternType == filterPatternType }
        }
        
        // Filter by food name
        if (filterFoodName != null) {
            result = result.filter { it.correlatedItem == filterFoodName }
        }
        
        result
    }
    
    val hasActiveFilter = filterRiskLevel != null || filterPatternType != null || filterFoodName != null
    val highRiskCount = filteredPatterns.count { it.confidenceScore >= 0.8f }
    val mediumRiskCount = filteredPatterns.count { it.confidenceScore >= 0.6f && it.confidenceScore < 0.8f }
    val lowRiskCount = filteredPatterns.count { it.confidenceScore < 0.6f }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.pattern_analysis),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (hasActiveFilter) {
                                stringResource(R.string.showing_patterns, filteredPatterns.size, patterns.size)
                            } else {
                                stringResource(R.string.detected_patterns, patterns.size)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (patterns.isNotEmpty()) {
                        IconButton(onClick = { showExportDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.export_analysis),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (patterns.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        stringResource(R.string.no_patterns_detected),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        stringResource(R.string.keep_tracking_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (hasActiveFilter) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.88f)
                        } else {
                            MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                        }
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (hasActiveFilter) 6.dp else 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.pattern_overview),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasActiveFilter) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    tonalElevation = 2.dp
                                ) {
                                    Text(
                                        text = patterns.size.toString(),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                if (hasActiveFilter) {
                                    FilledTonalButton(
                                        onClick = {
                                            filterRiskLevel = null
                                            filterPatternType = null
                                            filterFoodName = null
                                        },
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.clear_filter),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            stringResource(R.string.clear_filter),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }

                        PatternDateRangeFilterSection(
                            selectedLanguage = selectedLanguage,
                            initialDateRange = initialDateRange,
                            initialDaysToAnalyze = initialDaysToAnalyze,
                            onRangeApplied = { days, range ->
                                activeAnalysisDays = days
                                activeDateRange = range
                                onDateRangeChanged(days, range)
                            }
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                stringResource(R.string.risk_distribution),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            RiskBar(
                                label = stringResource(R.string.high_risk),
                                count = highRiskCount,
                                total = filteredPatterns.size,
                                color = Color(0xFFD32F2F),
                                isActive = filterRiskLevel == "high",
                                onClick = { filterRiskLevel = if (filterRiskLevel == "high") null else "high" }
                            )
                            RiskBar(
                                label = stringResource(R.string.medium_risk),
                                count = mediumRiskCount,
                                total = filteredPatterns.size,
                                color = Color(0xFFF57C00),
                                isActive = filterRiskLevel == "medium",
                                onClick = { filterRiskLevel = if (filterRiskLevel == "medium") null else "medium" }
                            )
                            RiskBar(
                                label = stringResource(R.string.low_risk),
                                count = lowRiskCount,
                                total = filteredPatterns.size,
                                color = Color(0xFF757575),
                                isActive = filterRiskLevel == "low",
                                onClick = { filterRiskLevel = if (filterRiskLevel == "low") null else "low" }
                            )
                        }

                        if (hasActiveFilter) {
                            Text(
                                text = stringResource(R.string.showing_patterns, filteredPatterns.size, patterns.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.detected_patterns, patterns.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                }

                item {
                    PotentialFoodSignalsSection(
                        entries = entries,
                        patterns = patterns,
                        activeDateRange = activeDateRange,
                        context = context,
                        selectedLanguage = selectedLanguage,
                        onNavigateToDate = onNavigateToDate,
                        onFilterFood = { foodName ->
                            filterFoodName = foodName
                            filterPatternType = PatternType.FOOD_MARKER_CORRELATION
                            showPatternWorkspace = true
                        }
                    )
                }

                item {
                    MarkerCalendarOverviewCard(
                        entries = entries,
                        customMarkers = customMarkers,
                        filteredPatterns = filteredPatterns,
                        availablePatternTypes = patterns.map { it.patternType }.distinct(),
                        activePatternTypeFilter = filterPatternType,
                        selectedLanguage = selectedLanguage,
                        activeDateRange = activeDateRange,
                        onNavigateToDate = onNavigateToDate,
                        onPatternTypeFilterApplied = { filterPatternType = it },
                        onOpenPatternDetails = { showPatternWorkspace = true }
                    )
                }

                if (showPatternWorkspace) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showPatternWorkspace = false }) {
                                Text(stringResource(R.string.calendar_hide_pattern_workspace))
                            }
                        }
                    }

                    // Visualization section
                    item {
                        PatternVisualization(
                            patterns = patterns,
                            selectedLanguage = selectedLanguage,
                            onFilterType = { type -> filterPatternType = if (filterPatternType == type) null else type },
                            onFilterFood = { food -> filterFoodName = if (filterFoodName == food) null else food },
                            activeTypeFilter = filterPatternType,
                            activeFoodFilter = filterFoodName
                        )
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                        ) {
                            Text(
                                stringResource(R.string.pattern_details),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }

                    item {
                        PatternDetailsGroupedSection(
                            patterns = filteredPatterns,
                            selectedLanguage = selectedLanguage,
                            activeAnalysisDays = activeAnalysisDays,
                            activeDateRange = activeDateRange,
                            onDismissPattern = onDismissPattern,
                            onIgnoreFood = onIgnoreFood,
                            onNavigateToDate = onNavigateToDate
                        )
                    }
                }
                
                if (hasActiveFilter && filteredPatterns.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.no_patterns_match_filter),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
        
        // Export dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = {
                    Text(stringResource(R.string.export_analysis_title))
                },
                text = {
                    Column {
                        Text(
                            stringResource(R.string.export_analysis_description),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.export_will_include),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.export_includes_list),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Export format selection
                        Text(
                            stringResource(R.string.export_format),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // PDF option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { exportFormat = "pdf" }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = exportFormat == "pdf",
                                onClick = { exportFormat = "pdf" }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.pdf_document), style = MaterialTheme.typography.bodyMedium)
                        }
                        
                        // TXT option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { exportFormat = "txt" }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = exportFormat == "txt",
                                onClick = { exportFormat = "txt" }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.text_file), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExportDialog = false
                            if (exportFormat == "pdf") {
                                onExportPDF()
                            } else {
                                onExportTXT()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.export))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatternDateRangeFilterSection(
    selectedLanguage: String,
    initialDateRange: ClosedRange<LocalDate>?,
    initialDaysToAnalyze: Int,
    onRangeApplied: (Int, ClosedRange<LocalDate>?) -> Unit
) {
    val today = remember { LocalDate.now() }
    var selectedRange by remember { mutableStateOf(AnalysisDateRangeOption.ONE_MONTH) }
    var customStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var customEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(initialDateRange, initialDaysToAnalyze, today) {
        selectedRange = resolveInitialAnalysisRangeOption(initialDateRange, initialDaysToAnalyze, today)
        if (selectedRange == AnalysisDateRangeOption.CUSTOM) {
            customStartDate = initialDateRange?.start
            customEndDate = initialDateRange?.endInclusive
        } else {
            customStartDate = null
            customEndDate = null
        }
    }

    val selectedBounds = analysisRangeBounds(selectedRange, customStartDate, customEndDate, today)
    val hasInvalidCustomRange = selectedRange == AnalysisDateRangeOption.CUSTOM &&
        customStartDate != null && customEndDate != null && customStartDate!!.isAfter(customEndDate)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = analysisRangeHeaderLabel(selectedLanguage),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnalysisDateRangeOption.entries.forEach { option ->
                FilterChip(
                    selected = selectedRange == option,
                    onClick = {
                        selectedRange = option
                        if (option != AnalysisDateRangeOption.CUSTOM) {
                            val bounds = analysisRangeBounds(option, customStartDate, customEndDate, today)
                            onRangeApplied(analysisDaysForRange(option, bounds, initialDaysToAnalyze), bounds)
                        }
                    },
                    label = { Text(analysisRangeOptionLabel(option, selectedLanguage)) }
                )
            }
        }

        if (selectedRange == AnalysisDateRangeOption.CUSTOM) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(customStartDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: localizedStartDateLabel(selectedLanguage))
                }
                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(customEndDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: localizedEndDateLabel(selectedLanguage))
                }
            }

            if (hasInvalidCustomRange) {
                Text(
                    text = localizedInvalidDateRange(selectedLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        val entriesInScopeLabel = selectedBounds?.let {
            "${it.start.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} - ${it.endInclusive.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
        } ?: analysisAllTimeLabel(selectedLanguage)

        Text(
            text = entriesInScopeLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showStartDatePicker) {
        val startState = rememberDatePickerState(
            initialSelectedDateMillis = (customStartDate ?: today).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        customStartDate = startState.selectedDateMillis?.toLocalDateFromEpochMillis()
                        showStartDatePicker = false
                        val bounds = analysisRangeBounds(selectedRange, customStartDate, customEndDate, today)
                        if (bounds != null) {
                            onRangeApplied(analysisDaysForRange(selectedRange, bounds, initialDaysToAnalyze), bounds)
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = startState)
        }
    }

    if (showEndDatePicker) {
        val endState = rememberDatePickerState(
            initialSelectedDateMillis = (customEndDate ?: today).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        customEndDate = endState.selectedDateMillis?.toLocalDateFromEpochMillis()
                        showEndDatePicker = false
                        val bounds = analysisRangeBounds(selectedRange, customStartDate, customEndDate, today)
                        if (bounds != null) {
                            onRangeApplied(analysisDaysForRange(selectedRange, bounds, initialDaysToAnalyze), bounds)
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = endState)
        }
    }
}

private fun resolveInitialAnalysisRangeOption(
    initialDateRange: ClosedRange<LocalDate>?,
    initialDaysToAnalyze: Int,
    today: LocalDate
): AnalysisDateRangeOption {
    if (initialDateRange == null) {
        return when {
            initialDaysToAnalyze >= 36500 -> AnalysisDateRangeOption.ALL_TIME
            initialDaysToAnalyze >= 365 -> AnalysisDateRangeOption.ONE_YEAR
            else -> AnalysisDateRangeOption.ONE_MONTH
        }
    }

    return when {
        initialDateRange.start == today.minusYears(1) && initialDateRange.endInclusive == today -> AnalysisDateRangeOption.ONE_YEAR
        initialDateRange.start == today.minusMonths(1) && initialDateRange.endInclusive == today -> AnalysisDateRangeOption.ONE_MONTH
        else -> AnalysisDateRangeOption.CUSTOM
    }
}

private fun analysisRangeBounds(
    selectedRange: AnalysisDateRangeOption,
    customStartDate: LocalDate?,
    customEndDate: LocalDate?,
    today: LocalDate
): ClosedRange<LocalDate>? {
    return when (selectedRange) {
        AnalysisDateRangeOption.ALL_TIME -> null
        AnalysisDateRangeOption.ONE_YEAR -> today.minusYears(1)..today
        AnalysisDateRangeOption.ONE_MONTH -> today.minusMonths(1)..today
        AnalysisDateRangeOption.CUSTOM -> {
            if (customStartDate == null || customEndDate == null || customStartDate.isAfter(customEndDate)) null
            else customStartDate..customEndDate
        }
    }
}

private fun analysisDaysForRange(
    selectedRange: AnalysisDateRangeOption,
    bounds: ClosedRange<LocalDate>?,
    fallbackDays: Int
): Int {
    return when (selectedRange) {
        AnalysisDateRangeOption.ALL_TIME -> 36500
        AnalysisDateRangeOption.ONE_YEAR -> 365
        AnalysisDateRangeOption.ONE_MONTH -> 30
        AnalysisDateRangeOption.CUSTOM -> {
            if (bounds == null) fallbackDays
            else kotlin.math.max(1, ChronoUnit.DAYS.between(bounds.start, bounds.endInclusive).toInt() + 1)
        }
    }
}

private fun analysisRangeHeaderLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Analysezeitraum"
        "Spanish" -> "Rango de análisis"
        else -> "Analysis Range"
    }
}

private fun analysisRangeOptionLabel(option: AnalysisDateRangeOption, selectedLanguage: String): String {
    return when (option) {
        AnalysisDateRangeOption.ALL_TIME -> when (selectedLanguage) {
            "German" -> "Gesamt"
            "Spanish" -> "Todo"
            else -> "All time"
        }
        AnalysisDateRangeOption.ONE_YEAR -> when (selectedLanguage) {
            "German" -> "1 Jahr"
            "Spanish" -> "1 año"
            else -> "1 year"
        }
        AnalysisDateRangeOption.ONE_MONTH -> when (selectedLanguage) {
            "German" -> "1 Monat"
            "Spanish" -> "1 mes"
            else -> "1 month"
        }
        AnalysisDateRangeOption.CUSTOM -> when (selectedLanguage) {
            "German" -> "Benutzerdefiniert"
            "Spanish" -> "Personalizado"
            else -> "Custom"
        }
    }
}

private fun analysisAllTimeLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Gesamte Historie"
        "Spanish" -> "Historial completo"
        else -> "Entire history"
    }
}

private fun localizedStartDateLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Startdatum"
        "Spanish" -> "Fecha inicio"
        else -> "Start date"
    }
}

private fun localizedEndDateLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Enddatum"
        "Spanish" -> "Fecha fin"
        else -> "End date"
    }
}

private fun localizedInvalidDateRange(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Das Startdatum muss vor dem Enddatum liegen."
        "Spanish" -> "La fecha de inicio debe ser anterior a la fecha final."
        else -> "Start date must be before end date."
    }
}

private fun Long.toLocalDateFromEpochMillis(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}

private data class MarkerDaySummary(
    val markerName: String,
    val triggerLabel: String?,
    val markerCount: Int,
    val triggerCount: Int,
    val patternLabel: String?,
    val patternCount: Int
)

private enum class CalendarDetailMode { MARKER, TRIGGER, PATTERN }
private enum class CalendarPrimaryFilter { ALL_DAYS, MARKED_DAYS, TRIGGER_DAYS, PATTERN_DAYS }

@Composable
fun MarkerCalendarOverviewCard(
    entries: List<DiabetesEntry>,
    customMarkers: List<CustomMarkerType>,
    filteredPatterns: List<DetectedPattern>,
    availablePatternTypes: List<PatternType>,
    activePatternTypeFilter: PatternType?,
    selectedLanguage: String,
    activeDateRange: ClosedRange<LocalDate>?,
    onNavigateToDate: (String) -> Unit,
    onPatternTypeFilterApplied: (PatternType?) -> Unit,
    onOpenPatternDetails: () -> Unit
) {
    val currentMonth = remember { YearMonth.now() }
    var displayedMonth by remember { mutableStateOf(currentMonth) }
    var showMonthYearPicker by remember { mutableStateOf(false) }
    var selectedDayDetails by remember { mutableStateOf<CalendarDayDetails?>(null) }
    var primaryFilter by remember { mutableStateOf(CalendarPrimaryFilter.ALL_DAYS) }
    var showMarkedFilterDialog by remember { mutableStateOf(false) }
    var showTriggerFilterDialog by remember { mutableStateOf(false) }
    var showPatternFilterDialog by remember { mutableStateOf(false) }
    var selectedMarkerLabels by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedTriggerLabels by remember { mutableStateOf<Set<String>>(emptySet()) }

    val minAllowedMonth = remember(activeDateRange) { activeDateRange?.start?.let { YearMonth.from(it) } }
    val maxAllowedMonth = remember(activeDateRange) { activeDateRange?.endInclusive?.let { YearMonth.from(it) } }
    val canGoPrevious = minAllowedMonth == null || displayedMonth.isAfter(minAllowedMonth)
    val canGoNext = maxAllowedMonth == null || displayedMonth.isBefore(maxAllowedMonth)

    LaunchedEffect(activeDateRange) {
        if (activeDateRange != null) {
            val rangeStartMonth = YearMonth.from(activeDateRange.start)
            val rangeEndMonth = YearMonth.from(activeDateRange.endInclusive)
            if (displayedMonth.isBefore(rangeStartMonth) || displayedMonth.isAfter(rangeEndMonth)) {
                displayedMonth = rangeStartMonth
            }
        }
    }

    val locale = remember(selectedLanguage) {
        when (selectedLanguage) {
            "German" -> Locale.GERMAN
            "Spanish" -> Locale("es")
            else -> Locale.ENGLISH
        }
    }
    val monthFormatter = remember(locale) { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }
    val selectableYears = remember(entries, activeDateRange) {
        supportedCalendarYears(entries, activeDateRange)
    }
    val monthSummaries = remember(entries, customMarkers, filteredPatterns, selectedLanguage, displayedMonth, activeDateRange, selectedMarkerLabels, selectedTriggerLabels) {
        buildMarkerDaySummaries(
            entries = entries,
            customMarkers = customMarkers,
            filteredPatterns = filteredPatterns,
            selectedLanguage = selectedLanguage,
            month = displayedMonth,
            activeDateRange = activeDateRange,
            selectedMarkerLabels = selectedMarkerLabels,
            selectedTriggerLabels = selectedTriggerLabels
        )
    }
    val monthDayDetails = remember(entries, customMarkers, filteredPatterns, selectedLanguage, displayedMonth, activeDateRange, selectedMarkerLabels, selectedTriggerLabels, activePatternTypeFilter) {
        buildCalendarDayDetails(
            entries = entries,
            customMarkers = customMarkers,
            filteredPatterns = filteredPatterns,
            selectedLanguage = selectedLanguage,
            month = displayedMonth,
            activeDateRange = activeDateRange,
            selectedMarkerLabels = selectedMarkerLabels,
            selectedTriggerLabels = selectedTriggerLabels,
            activePatternTypeFilter = activePatternTypeFilter
        )
    }
    val unfilteredMonthDayDetails = remember(entries, customMarkers, filteredPatterns, selectedLanguage, displayedMonth, activeDateRange, activePatternTypeFilter) {
        buildCalendarDayDetails(
            entries = entries,
            customMarkers = customMarkers,
            filteredPatterns = filteredPatterns,
            selectedLanguage = selectedLanguage,
            month = displayedMonth,
            activeDateRange = activeDateRange,
            selectedMarkerLabels = emptySet(),
            selectedTriggerLabels = emptySet(),
            activePatternTypeFilter = activePatternTypeFilter
        )
    }
    val calendarCells = remember(displayedMonth, monthSummaries) {
        buildCalendarCells(displayedMonth, monthSummaries)
    }
    val daysWithMarkerData = remember(monthSummaries) {
        monthSummaries.values.count { it.markerCount > 0 }
    }
    val daysWithTriggerData = remember(monthSummaries) {
        monthSummaries.values.count { it.triggerCount > 0 }
    }
    val daysWithPatternData = remember(monthSummaries) {
        monthSummaries.values.count { it.patternCount > 0 }
    }
    val availableTriggers = remember(unfilteredMonthDayDetails) {
        unfilteredMonthDayDetails.values
            .flatMap { it.triggerBreakdown.map { pair -> pair.first } }
            .distinct()
            .sorted()
    }
    val availableMarkers = remember(unfilteredMonthDayDetails) {
        unfilteredMonthDayDetails.values
            .flatMap { it.markerBreakdown.map { pair -> pair.first } }
            .distinct()
            .sorted()
    }
    val activeDetailMode = remember(primaryFilter) {
        when (primaryFilter) {
            CalendarPrimaryFilter.TRIGGER_DAYS -> CalendarDetailMode.TRIGGER
            CalendarPrimaryFilter.PATTERN_DAYS -> CalendarDetailMode.PATTERN
            CalendarPrimaryFilter.ALL_DAYS,
            CalendarPrimaryFilter.MARKED_DAYS -> CalendarDetailMode.MARKER
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.marker_calendar_overview),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(
                    onClick = { displayedMonth = currentMonth },
                    enabled = displayedMonth != currentMonth
                ) {
                    Text(stringResource(R.string.calendar_today))
                }
            }

            CalendarMonthNavigationRow(
                displayedMonth = displayedMonth,
                monthFormatter = monthFormatter,
                canGoPrevious = canGoPrevious,
                canGoNext = canGoNext,
                onPreviousMonth = { displayedMonth = displayedMonth.minusMonths(1) },
                onNextMonth = { displayedMonth = displayedMonth.plusMonths(1) },
                onMonthLabelClick = { showMonthYearPicker = true }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = primaryFilter == CalendarPrimaryFilter.ALL_DAYS,
                    onClick = { primaryFilter = CalendarPrimaryFilter.ALL_DAYS },
                    label = { Text(stringResource(R.string.calendar_filter_all_days)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = primaryFilter == CalendarPrimaryFilter.MARKED_DAYS,
                    onClick = {
                        primaryFilter = CalendarPrimaryFilter.MARKED_DAYS
                        showMarkedFilterDialog = true
                    },
                    label = { Text("${stringResource(R.string.calendar_filter_marked_days)} ($daysWithMarkerData)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = primaryFilter == CalendarPrimaryFilter.TRIGGER_DAYS,
                    onClick = {
                        primaryFilter = CalendarPrimaryFilter.TRIGGER_DAYS
                        showTriggerFilterDialog = true
                    },
                    label = { Text("${stringResource(R.string.calendar_filter_trigger_days)} ($daysWithTriggerData)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = primaryFilter == CalendarPrimaryFilter.PATTERN_DAYS,
                    onClick = {
                        primaryFilter = CalendarPrimaryFilter.PATTERN_DAYS
                        showPatternFilterDialog = true
                    },
                    label = { Text("${stringResource(R.string.calendar_filter_pattern_days)} ($daysWithPatternData)") },
                    modifier = Modifier.weight(1f)
                )
            }

            WeekdayHeaderRow(locale = locale)

            calendarCells.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    week.forEach { (date, summary) ->
                        val filteredSummary = when (primaryFilter) {
                            CalendarPrimaryFilter.ALL_DAYS -> summary
                            CalendarPrimaryFilter.MARKED_DAYS -> summary?.takeIf { it.markerCount > 0 }
                            CalendarPrimaryFilter.TRIGGER_DAYS -> summary?.takeIf { it.triggerCount > 0 }
                            CalendarPrimaryFilter.PATTERN_DAYS -> summary?.takeIf { it.patternCount > 0 }
                        }
                        CalendarDayCell(
                            modifier = Modifier.weight(1f),
                            date = date,
                            summary = filteredSummary,
                            detailMode = activeDetailMode,
                            onClick = {
                                date?.let { selectedDate ->
                                    monthDayDetails[selectedDate]?.let { selectedDayDetails = it }
                                }
                            }
                        )
                    }
                }
            }

            if (showMonthYearPicker) {
                MonthYearPickerDialog(
                    selectedMonth = displayedMonth,
                    locale = locale,
                    selectableYears = selectableYears,
                    minAllowedMonth = minAllowedMonth,
                    maxAllowedMonth = maxAllowedMonth,
                    onDismiss = { showMonthYearPicker = false },
                    onConfirm = { selectedMonth ->
                        displayedMonth = selectedMonth
                        showMonthYearPicker = false
                    }
                )
            }

            if (showMarkedFilterDialog) {
                CalendarMarkedFilterDialog(
                    selectedLanguage = selectedLanguage,
                    markerOptions = availableMarkers,
                    selectedMarkers = selectedMarkerLabels,
                    onDismiss = { showMarkedFilterDialog = false },
                    onApply = { appliedMarkers ->
                        selectedMarkerLabels = appliedMarkers
                        showMarkedFilterDialog = false
                    }
                )
            }

            if (showTriggerFilterDialog) {
                CalendarTriggerFilterDialog(
                    selectedLanguage = selectedLanguage,
                    triggerOptions = availableTriggers,
                    selectedTriggers = selectedTriggerLabels,
                    onDismiss = { showTriggerFilterDialog = false },
                    onApply = { appliedTriggers ->
                        selectedTriggerLabels = appliedTriggers
                        showTriggerFilterDialog = false
                    }
                )
            }

            if (showPatternFilterDialog) {
                CalendarPatternFilterDialog(
                    selectedLanguage = selectedLanguage,
                    patternTypes = availablePatternTypes,
                    selectedPatternType = activePatternTypeFilter,
                    onDismiss = { showPatternFilterDialog = false },
                    onApply = { selectedType ->
                        onPatternTypeFilterApplied(selectedType)
                        showPatternFilterDialog = false
                    },
                    onOpenPatternDetails = {
                        onOpenPatternDetails()
                        showPatternFilterDialog = false
                    }
                )
            }

            val isAnyFilterActive = remember(monthSummaries) {
                monthSummaries.values.any { it.patternCount > 0 || it.markerCount > 0 || it.triggerCount > 0 }
            }
            if (!isAnyFilterActive) {
                Text(
                    text = stringResource(R.string.no_patterns_match_filter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            selectedDayDetails?.let { dayDetails ->
                CalendarDayDetailsDialog(
                    dayDetails = dayDetails,
                    locale = locale,
                    onDismiss = { selectedDayDetails = null },
                    onNavigateToTableDay = {
                        onNavigateToDate(dayDetails.date.format(dateFormatter))
                        selectedDayDetails = null
                    }
                )
            }
        }
    }
}

@Composable
private fun CalendarMonthNavigationRow(
    displayedMonth: YearMonth,
    monthFormatter: DateTimeFormatter,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthLabelClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth, enabled = canGoPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.calendar_previous_month)
            )
        }
        Text(
            text = displayedMonth.atDay(1).format(monthFormatter),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .clickable(onClick = onMonthLabelClick)
        )
        IconButton(onClick = onNextMonth, enabled = canGoNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.calendar_next_month)
            )
        }
    }
}

@Composable
private fun MonthYearPickerDialog(
    selectedMonth: YearMonth,
    locale: Locale,
    selectableYears: List<Int>,
    minAllowedMonth: YearMonth?,
    maxAllowedMonth: YearMonth?,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    val monthLabels = remember(locale) {
        (1..12).map { month -> Month.of(month).getDisplayName(TextStyle.FULL, locale) }
    }
    var selectedMonthIndex by remember(selectedMonth) { mutableStateOf(selectedMonth.monthValue - 1) }
    var selectedYear by remember(selectedMonth, selectableYears) {
        mutableStateOf(
            selectableYears.firstOrNull { it == selectedMonth.year }
                ?: selectableYears.firstOrNull()
                ?: selectedMonth.year
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.calendar_pick_month_year)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.calendar_pick_month),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                monthLabels.forEachIndexed { index, label ->
                    val candidate = YearMonth.of(selectedYear, index + 1)
                    val isAllowed = (minAllowedMonth == null || !candidate.isBefore(minAllowedMonth)) &&
                        (maxAllowedMonth == null || !candidate.isAfter(maxAllowedMonth))
                    FilterChip(
                        selected = selectedMonthIndex == index,
                        onClick = { if (isAllowed) selectedMonthIndex = index },
                        enabled = isAllowed,
                        label = { Text(label.replaceFirstChar { it.titlecase(locale) }) }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.calendar_pick_year),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectableYears.forEach { year ->
                        FilterChip(
                            selected = selectedYear == year,
                            onClick = { selectedYear = year },
                            label = { Text(year.toString()) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(YearMonth.of(selectedYear, selectedMonthIndex + 1)) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

}

@Composable
private fun CalendarFilterShortHelp(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}

private fun markedFilterHelpText(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Wähle Marker, die im Kalender unter 'Markiert' gezeigt werden sollen. Mehrfachauswahl ist möglich."
        "Spanish" -> "Selecciona los marcadores que quieres ver en 'Marcados'. Puedes elegir varios a la vez."
        else -> "Select which markers should appear in 'Marked'. You can choose multiple marker types."
    }
}

private fun triggerFilterHelpText(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Wähle Auslöser für die Trigger-Ansicht. Nur Tage mit diesen Auslösern bleiben sichtbar."
        "Spanish" -> "Elige desencadenantes para la vista de Trigger. Solo se mostrarán los días con esos desencadenantes."
        else -> "Choose triggers for the Trigger view. Only days containing those triggers will stay visible."
    }
}

private fun patternAllTypesButtonHelpText(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Button 'Alle Mustertypen': zeigt wieder alle Pattern-Typen gleichzeitig. Wenn du ihn wählst, wird kein einzelner Typ erzwungen."
        "Spanish" -> "Botón 'Todos los tipos': vuelve a mostrar todos los tipos de patrón juntos. Al elegirlo, no se fuerza un tipo único."
        else -> "Button 'All pattern types': returns to showing all pattern types together. Choosing it removes single-type restriction."
    }
}

private fun patternTypeButtonHelpText(
    patternType: PatternType,
    buttonLabel: String,
    selectedLanguage: String
): String {
    val whyVisible = when (selectedLanguage) {
        "German" -> "Du siehst '$buttonLabel', weil dafür aktuell Pattern-Einträge vorhanden sind."
        "Spanish" -> "Ves '$buttonLabel' porque actualmente existen entradas de patrón de este tipo."
        else -> "You see '$buttonLabel' because pattern entries of this type currently exist."
    }

    val actionMeaning = when (patternType) {
        PatternType.FOOD_MARKER_CORRELATION -> when (selectedLanguage) {
            "German" -> "Beim Auswählen werden nur Zusammenhänge zwischen Lebensmitteln und Markern gezeigt."
            "Spanish" -> "Al elegirlo, se muestran solo relaciones entre comida y marcadores."
            else -> "Choosing it shows only food-to-marker relationship patterns."
        }
        PatternType.TIME_MARKER_CORRELATION -> when (selectedLanguage) {
            "German" -> "Beim Auswählen werden nur zeitbasierte Marker-Muster (z. B. morgens/abends) gezeigt."
            "Spanish" -> "Al elegirlo, se muestran solo patrones de marcador basados en hora (p. ej., mañana/noche)."
            else -> "Choosing it shows only time-based marker patterns (e.g., morning/evening)."
        }
        PatternType.BLOOD_SUGAR_SPIKE -> when (selectedLanguage) {
            "German" -> "Beim Auswählen werden nur Muster zu Blutzucker-Spitzen angezeigt."
            "Spanish" -> "Al elegirlo, se muestran solo patrones de picos de glucosa."
            else -> "Choosing it shows only blood sugar spike patterns."
        }
        PatternType.BLOOD_SUGAR_DROP -> when (selectedLanguage) {
            "German" -> "Beim Auswählen werden nur Muster zu Blutzucker-Abfällen angezeigt."
            "Spanish" -> "Al elegirlo, se muestran solo patrones de bajadas de glucosa."
            else -> "Choosing it shows only blood sugar drop patterns."
        }
        PatternType.INSULIN_PATTERN -> when (selectedLanguage) {
            "German" -> "Beim Auswählen werden nur Insulin-bezogene Muster angezeigt."
            "Spanish" -> "Al elegirlo, se muestran solo patrones relacionados con insulina."
            else -> "Choosing it shows only insulin-related patterns."
        }
        PatternType.CUSTOM_COLUMN_PATTERN -> when (selectedLanguage) {
            "German" -> "Beim Auswählen werden nur Muster aus benutzerdefinierten Spalten angezeigt."
            "Spanish" -> "Al elegirlo, se muestran solo patrones de columnas personalizadas."
            else -> "Choosing it shows only custom-column patterns."
        }
    }

    return "$whyVisible $actionMeaning"
}

private fun supportedCalendarYears(
    entries: List<DiabetesEntry>,
    activeDateRange: ClosedRange<LocalDate>?
): List<Int> {
    val minYear = activeDateRange?.start?.year
        ?: entries.mapNotNull { parseAnalysisEntryDate(it.date)?.year }.minOrNull()
        ?: LocalDate.now().year - 1
    val maxYear = activeDateRange?.endInclusive?.year
        ?: entries.mapNotNull { parseAnalysisEntryDate(it.date)?.year }.maxOrNull()
        ?: LocalDate.now().year
    return (minYear..maxYear).toList().ifEmpty { listOf(LocalDate.now().year) }
}

@Composable
private fun WeekdayHeaderRow(locale: Locale) {
    val labels = remember(locale) {
        (1..7).map { index ->
            java.time.DayOfWeek.of(index).getDisplayName(TextStyle.SHORT, locale)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    modifier: Modifier,
    date: LocalDate?,
    summary: MarkerDaySummary?,
    detailMode: CalendarDetailMode,
    onClick: () -> Unit
) {
    if (date == null) {
        Spacer(modifier = modifier.height(86.dp))
        return
    }

    val hasMarkerData = summary != null &&
        (summary.markerCount > 0 || summary.triggerCount > 0 || summary.patternCount > 0)
    val primaryLabel = when (detailMode) {
        CalendarDetailMode.MARKER -> summary?.let { "${it.markerName} (${it.markerCount})" } ?: "-"
        CalendarDetailMode.TRIGGER -> summary?.triggerLabel?.let { "${it} (${summary.triggerCount})" } ?: "-"
        CalendarDetailMode.PATTERN -> summary?.patternLabel?.let { "${it} (${summary.patternCount})" } ?: "-"
    }
    val secondaryLabel = when (detailMode) {
        CalendarDetailMode.MARKER -> summary?.triggerLabel ?: ""
        CalendarDetailMode.TRIGGER -> summary?.markerName ?: ""
        CalendarDetailMode.PATTERN -> summary?.markerName ?: ""
    }

    Surface(
        modifier = modifier
            .height(86.dp)
            .clickable(enabled = hasMarkerData, onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (hasMarkerData) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (hasMarkerData) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = primaryLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = secondaryLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

private fun buildMarkerDaySummaries(
    entries: List<DiabetesEntry>,
    customMarkers: List<CustomMarkerType>,
    filteredPatterns: List<DetectedPattern>,
    selectedLanguage: String,
    month: YearMonth,
    activeDateRange: ClosedRange<LocalDate>?,
    selectedMarkerLabels: Set<String>,
    selectedTriggerLabels: Set<String>
): Map<LocalDate, MarkerDaySummary> {
    val markerCountsByDay = mutableMapOf<LocalDate, MutableMap<String, Int>>()
    val triggerCountsByDay = mutableMapOf<LocalDate, MutableMap<String, Int>>()
    val patternCountsByDay = mutableMapOf<LocalDate, MutableMap<String, Int>>()

    entries.forEach entryLoop@{ entry ->
        val entryDate = parseAnalysisEntryDate(entry.date) ?: return@entryLoop
        if (YearMonth.from(entryDate) != month) return@entryLoop
        if (activeDateRange != null && (entryDate.isBefore(activeDateRange.start) || entryDate.isAfter(activeDateRange.endInclusive))) {
            return@entryLoop
        }

        entry.markers.forEach { marker ->
            val markerName = resolveMarkerName(marker, customMarkers, selectedLanguage)
            if (selectedMarkerLabels.isNotEmpty() && !selectedMarkerLabels.contains(markerName)) {
                return@forEach
            }
            val markerCountMap = markerCountsByDay.getOrPut(entryDate) { mutableMapOf() }
            markerCountMap[markerName] = (markerCountMap[markerName] ?: 0) + 1

            val trigger = marker.reasons.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
                ?: marker.customNote.trim().takeIf { it.isNotEmpty() }?.take(16)
            if (trigger != null && (selectedTriggerLabels.isEmpty() || selectedTriggerLabels.contains(trigger))) {
                val triggerCountMap = triggerCountsByDay.getOrPut(entryDate) { mutableMapOf() }
                triggerCountMap[trigger] = (triggerCountMap[trigger] ?: 0) + 1
            }
        }
    }

    filteredPatterns.forEach { pattern ->
        pattern.details.forEach occurrenceLoop@{ occurrence ->
            val occurrenceDate = parseAnalysisEntryDate(occurrence.date) ?: return@occurrenceLoop
            if (YearMonth.from(occurrenceDate) != month) return@occurrenceLoop
            if (activeDateRange != null &&
                (occurrenceDate.isBefore(activeDateRange.start) || occurrenceDate.isAfter(activeDateRange.endInclusive))
            ) {
                return@occurrenceLoop
            }

            val patternLabel = pattern.correlatedItem.ifBlank { pattern.markerName }
            val patternCountMap = patternCountsByDay.getOrPut(occurrenceDate) { mutableMapOf() }
            patternCountMap[patternLabel] = (patternCountMap[patternLabel] ?: 0) + 1
        }
    }

    val allDates = markerCountsByDay.keys + triggerCountsByDay.keys + patternCountsByDay.keys
    return allDates.associateWith { date ->
        val markerCountMap = markerCountsByDay[date].orEmpty()
        val topMarker = markerCountMap.maxWithOrNull(
            compareBy<Map.Entry<String, Int>> { it.value }.thenBy { it.key }
        )
        val topTriggerEntry = triggerCountsByDay[date].orEmpty().maxWithOrNull(
            compareBy<Map.Entry<String, Int>> { it.value }.thenBy { it.key }
        )
        val topPattern = patternCountsByDay[date].orEmpty().maxWithOrNull(
            compareBy<Map.Entry<String, Int>> { it.value }.thenBy { it.key }
        )

        MarkerDaySummary(
            markerName = topMarker?.key ?: "-",
            triggerLabel = topTriggerEntry?.key,
            markerCount = topMarker?.value ?: 0,
            triggerCount = topTriggerEntry?.value ?: 0,
            patternLabel = topPattern?.key,
            patternCount = topPattern?.value ?: 0
        )
    }
}

private fun buildCalendarDayDetails(
    entries: List<DiabetesEntry>,
    customMarkers: List<CustomMarkerType>,
    filteredPatterns: List<DetectedPattern>,
    selectedLanguage: String,
    month: YearMonth,
    activeDateRange: ClosedRange<LocalDate>?,
    selectedMarkerLabels: Set<String>,
    selectedTriggerLabels: Set<String>,
    activePatternTypeFilter: PatternType?
): Map<LocalDate, CalendarDayDetails> {
    val markerCountsByDay = mutableMapOf<LocalDate, MutableMap<String, Int>>()
    val triggerCountsByDay = mutableMapOf<LocalDate, MutableMap<String, Int>>()
    val patternCountsByDay = mutableMapOf<LocalDate, MutableMap<String, Int>>()
    val helperSummaryByDay = mutableMapOf<LocalDate, MutableList<String>>()
    val patternTypesByDayLabel = mutableMapOf<LocalDate, MutableMap<String, MutableSet<PatternType>>>()
    val markerNamesByDayLabel = mutableMapOf<LocalDate, MutableMap<String, MutableSet<String>>>()

    entries.forEach entryLoop@{ entry ->
        val entryDate = parseAnalysisEntryDate(entry.date) ?: return@entryLoop
        if (YearMonth.from(entryDate) != month) return@entryLoop
        if (activeDateRange != null && (entryDate.isBefore(activeDateRange.start) || entryDate.isAfter(activeDateRange.endInclusive))) {
            return@entryLoop
        }

        entry.markers.forEach markerLoop@{ marker ->
            val markerName = resolveMarkerName(marker, customMarkers, selectedLanguage)
            if (selectedMarkerLabels.isNotEmpty() && !selectedMarkerLabels.contains(markerName)) {
                return@markerLoop
            }
            val markerCountMap = markerCountsByDay.getOrPut(entryDate) { mutableMapOf() }
            markerCountMap[markerName] = (markerCountMap[markerName] ?: 0) + 1

            val trigger = marker.reasons.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
                ?: marker.customNote.trim().takeIf { it.isNotEmpty() }?.take(16)
            val triggerMatchesFilter = trigger != null && (selectedTriggerLabels.isEmpty() || selectedTriggerLabels.contains(trigger))
            if (triggerMatchesFilter) {
                val triggerValue = trigger ?: return@markerLoop
                val triggerCountMap = triggerCountsByDay.getOrPut(entryDate) { mutableMapOf() }
                triggerCountMap[triggerValue] = (triggerCountMap[triggerValue] ?: 0) + 1
            }

            val helperLine = buildCalendarMarkerHelperLine(
                markerName = markerName,
                markerStartTime = marker.startTime,
                trigger = trigger?.takeIf { triggerMatchesFilter },
                selectedLanguage = selectedLanguage
            )
            helperSummaryByDay.getOrPut(entryDate) { mutableListOf() }.add(helperLine)
        }
    }

    filteredPatterns.forEach { pattern ->
        pattern.details.forEach occurrenceLoop@{ occurrence ->
            val occurrenceDate = parseAnalysisEntryDate(occurrence.date) ?: return@occurrenceLoop
            if (YearMonth.from(occurrenceDate) != month) return@occurrenceLoop
            if (activeDateRange != null &&
                (occurrenceDate.isBefore(activeDateRange.start) || occurrenceDate.isAfter(activeDateRange.endInclusive))
            ) {
                return@occurrenceLoop
            }

            val patternLabel = pattern.correlatedItem.ifBlank { pattern.markerName }
            val patternCountMap = patternCountsByDay.getOrPut(occurrenceDate) { mutableMapOf() }
            patternCountMap[patternLabel] = (patternCountMap[patternLabel] ?: 0) + 1

            val typeMapByLabel = patternTypesByDayLabel.getOrPut(occurrenceDate) { mutableMapOf() }
            typeMapByLabel.getOrPut(patternLabel) { mutableSetOf() }.add(pattern.patternType)

            val markerMapByLabel = markerNamesByDayLabel.getOrPut(occurrenceDate) { mutableMapOf() }
            markerMapByLabel.getOrPut(patternLabel) { mutableSetOf() }.add(pattern.markerName)
        }
    }

    val allDates = markerCountsByDay.keys + triggerCountsByDay.keys + patternCountsByDay.keys
    return allDates.associateWith { date ->
        val markerBreakdown = markerCountsByDay[date].orEmpty().entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { it.key to it.value }
        val triggerBreakdown = triggerCountsByDay[date].orEmpty().entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { it.key to it.value }
        val patternBreakdown = patternCountsByDay[date].orEmpty().entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { it.key to it.value }
        val helperLines = helperSummaryByDay[date].orEmpty().distinct().take(4).toMutableList()
        helperLines.addAll(
            buildCalendarPatternHelperLines(
                patternBreakdown = patternBreakdown,
                selectedLanguage = selectedLanguage,
                activePatternTypeFilter = activePatternTypeFilter,
                patternTypesByLabel = patternTypesByDayLabel[date].orEmpty(),
                markerNamesByLabel = markerNamesByDayLabel[date].orEmpty()
            )
        )

        CalendarDayDetails(
            date = date,
            markerBreakdown = markerBreakdown,
            triggerBreakdown = triggerBreakdown,
            patternBreakdown = patternBreakdown,
            helperSummaryLines = helperLines
        )
    }
}

private fun buildCalendarCells(
    month: YearMonth,
    summaries: Map<LocalDate, MarkerDaySummary>
): List<Pair<LocalDate?, MarkerDaySummary?>> {
    val firstDay = month.atDay(1)
    val leadingEmptyCells = firstDay.dayOfWeek.value - 1
    val days = (1..month.lengthOfMonth()).map { day ->
        val date = month.atDay(day)
        date to summaries[date]
    }

    val cells = List(leadingEmptyCells) { null to null } + days
    val trailingEmptyCells = if (cells.size % 7 == 0) 0 else 7 - (cells.size % 7)
    return cells + List(trailingEmptyCells) { null to null }
}

@Composable
private fun CalendarMarkedFilterDialog(
    selectedLanguage: String,
    markerOptions: List<String>,
    selectedMarkers: Set<String>,
    onDismiss: () -> Unit,
    onApply: (Set<String>) -> Unit
) {
    var draftSelection by remember(selectedMarkers) { mutableStateOf(selectedMarkers) }
    var showInfo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.calendar_marked_filter_title), modifier = Modifier.weight(1f))
                IconButton(onClick = { showInfo = !showInfo }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.calendar_filter_info_icon)
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (showInfo) {
                    CalendarFilterShortHelp(
                        text = markedFilterHelpText(selectedLanguage),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (markerOptions.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_patterns_match_filter),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        markerOptions.forEach { marker ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        draftSelection = if (draftSelection.contains(marker)) {
                                            draftSelection - marker
                                        } else {
                                            draftSelection + marker
                                        }
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = draftSelection.contains(marker),
                                    onCheckedChange = { isChecked ->
                                        draftSelection = if (isChecked) draftSelection + marker else draftSelection - marker
                                    }
                                )
                                Text(marker, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(draftSelection) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = { draftSelection = emptySet() }) {
                    Text(stringResource(R.string.clear))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )

}

@Composable
private fun CalendarTriggerFilterDialog(
    selectedLanguage: String,
    triggerOptions: List<String>,
    selectedTriggers: Set<String>,
    onDismiss: () -> Unit,
    onApply: (Set<String>) -> Unit
) {
    var draftSelection by remember(selectedTriggers) { mutableStateOf(selectedTriggers) }
    var showInfo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.calendar_trigger_filter_title), modifier = Modifier.weight(1f))
                IconButton(onClick = { showInfo = !showInfo }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.calendar_filter_info_icon)
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (showInfo) {
                    CalendarFilterShortHelp(
                        text = triggerFilterHelpText(selectedLanguage),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (triggerOptions.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_patterns_match_filter),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        triggerOptions.forEach { trigger ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        draftSelection = if (draftSelection.contains(trigger)) {
                                            draftSelection - trigger
                                        } else {
                                            draftSelection + trigger
                                        }
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = draftSelection.contains(trigger),
                                    onCheckedChange = { isChecked ->
                                        draftSelection = if (isChecked) draftSelection + trigger else draftSelection - trigger
                                    }
                                )
                                Text(trigger, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(draftSelection) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = { draftSelection = emptySet() }) {
                    Text(stringResource(R.string.clear))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )
}

@Composable
private fun CalendarPatternFilterDialog(
    selectedLanguage: String,
    patternTypes: List<PatternType>,
    selectedPatternType: PatternType?,
    onDismiss: () -> Unit,
    onApply: (PatternType?) -> Unit,
    onOpenPatternDetails: () -> Unit
) {
    var draftPatternType by remember(selectedPatternType) { mutableStateOf(selectedPatternType) }
    var activeInfoMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.calendar_pattern_filter_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = draftPatternType == null,
                        onClick = { draftPatternType = null },
                        label = { Text(stringResource(R.string.calendar_pattern_filter_all_types)) },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { activeInfoMessage = patternAllTypesButtonHelpText(selectedLanguage) }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.calendar_filter_info_icon)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 260.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    patternTypes.forEach { patternType ->
                        val buttonLabel = localizedPatternTypeLabel(patternType)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = draftPatternType == patternType,
                                onClick = {
                                    draftPatternType = if (draftPatternType == patternType) null else patternType
                                },
                                label = { Text(buttonLabel) },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    activeInfoMessage = patternTypeButtonHelpText(
                                        patternType = patternType,
                                        buttonLabel = buttonLabel,
                                        selectedLanguage = selectedLanguage
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = stringResource(R.string.calendar_filter_info_icon)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(draftPatternType) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onOpenPatternDetails) {
                    Text(stringResource(R.string.calendar_open_pattern_details))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )

    activeInfoMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { activeInfoMessage = null },
            title = { Text(stringResource(R.string.calendar_pattern_filter_title)) },
            text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { activeInfoMessage = null }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

private fun parseAnalysisEntryDate(rawDate: String): LocalDate? {
    val patterns = listOf("dd.MM.yyyy", "dd-MM-yyyy", "yyyy-MM-dd")
    patterns.forEach { pattern ->
        runCatching {
            return LocalDate.parse(rawDate, DateTimeFormatter.ofPattern(pattern))
        }
    }
    return null
}

private enum class ConfidenceGroupLevel { HIGH, MEDIUM, LOW }

private data class ConfidencePatternGroup(
    val level: ConfidenceGroupLevel,
    val titleRes: Int,
    val color: Color,
    val patterns: List<DetectedPattern>
)

@Composable
private fun PatternDetailsGroupedSection(
    patterns: List<DetectedPattern>,
    selectedLanguage: String,
    activeAnalysisDays: Int,
    activeDateRange: ClosedRange<LocalDate>?,
    onDismissPattern: (String) -> Unit,
    onIgnoreFood: (String, String, Int, ClosedRange<LocalDate>?) -> Unit,
    onNavigateToDate: (String) -> Unit
) {
    val confidenceGroups = remember(patterns) {
        listOf(
            ConfidencePatternGroup(
                level = ConfidenceGroupLevel.HIGH,
                titleRes = R.string.high_confidence,
                color = Color(0xFFD32F2F),
                patterns = patterns.filter { it.confidenceScore >= 0.8f }
            ),
            ConfidencePatternGroup(
                level = ConfidenceGroupLevel.MEDIUM,
                titleRes = R.string.medium_confidence,
                color = Color(0xFFF57C00),
                patterns = patterns.filter { it.confidenceScore >= 0.6f && it.confidenceScore < 0.8f }
            ),
            ConfidencePatternGroup(
                level = ConfidenceGroupLevel.LOW,
                titleRes = R.string.low_confidence,
                color = Color(0xFF757575),
                patterns = patterns.filter { it.confidenceScore < 0.6f }
            )
        ).filter { it.patterns.isNotEmpty() }
    }

    var expandedConfidenceGroup by remember(confidenceGroups) {
        mutableStateOf(confidenceGroups.firstOrNull()?.level)
    }
    var expandedMarkerGroupKey by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        confidenceGroups.forEach { confidenceGroup ->
            val isConfidenceExpanded = expandedConfidenceGroup == confidenceGroup.level

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedConfidenceGroup = if (isConfidenceExpanded) null else confidenceGroup.level
                                expandedMarkerGroupKey = null
                            }
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(confidenceGroup.color, RoundedCornerShape(5.dp))
                            )
                            Text(
                                text = stringResource(confidenceGroup.titleRes),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = confidenceGroup.color.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = confidenceGroup.patterns.size.toString(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = confidenceGroup.color,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = if (isConfidenceExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isConfidenceExpanded) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val markerGroups = confidenceGroup.patterns
                            .groupBy { translateMarkerName(it.markerName, selectedLanguage) }
                            .toList()
                            .sortedWith(compareByDescending<Pair<String, List<DetectedPattern>>> { it.second.size }.thenBy { it.first })

                        markerGroups.forEach { (markerType, markerPatterns) ->
                            val markerGroupKey = "${confidenceGroup.level}_$markerType"
                            val isMarkerExpanded = expandedMarkerGroupKey == markerGroupKey

                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedMarkerGroupKey = if (isMarkerExpanded) null else markerGroupKey
                                            }
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = markerType,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "${markerPatterns.size}",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                imageVector = if (isMarkerExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (isMarkerExpanded) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        markerPatterns.forEach { pattern ->
                                            PatternCard(
                                                pattern = pattern,
                                                selectedLanguage = selectedLanguage,
                                                onDismiss = { onDismissPattern(pattern.id) },
                                                onIgnoreFood = {
                                                    onIgnoreFood(
                                                        pattern.correlatedItem,
                                                        pattern.id,
                                                        activeAnalysisDays,
                                                        activeDateRange
                                                    )
                                                },
                                                onNavigateToDate = onNavigateToDate
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun resolveMarkerName(
    marker: Marker,
    customMarkers: List<CustomMarkerType>,
    selectedLanguage: String
): String {
    val customMarkerName = marker.customMarkerTypeId?.let { markerId ->
        customMarkers.find { it.id == markerId }?.name
    }

    return when {
        marker.type == "I feel bad" || marker.type == "feel_bad" -> when (selectedLanguage) {
            "German" -> "Ich fühle mich schlecht"
            "Spanish" -> "Me siento mal"
            else -> "I feel bad"
        }
        customMarkerName != null -> customMarkerName
        else -> marker.type
    }
}

private fun buildCalendarMarkerHelperLine(
    markerName: String,
    markerStartTime: String,
    trigger: String?,
    selectedLanguage: String
): String {
    val localizedTime = localizeCalendarMarkerTimeSlot(markerStartTime, selectedLanguage)
    val localizedTrigger = trigger?.let { localizeCalendarTriggerLabel(it, selectedLanguage) }

    return when (selectedLanguage) {
        "German" -> {
            if (localizedTrigger != null) {
                "$markerName wurde erfasst: Zeit $localizedTime, Auslöser \"$localizedTrigger\"."
            } else {
                "$markerName wurde erfasst: Zeit $localizedTime."
            }
        }
        "Spanish" -> {
            if (localizedTrigger != null) {
                "$markerName se registró: momento $localizedTime, desencadenante \"$localizedTrigger\"."
            } else {
                "$markerName se registró: momento $localizedTime."
            }
        }
        else -> {
            if (localizedTrigger != null) {
                "$markerName was logged during $localizedTime with trigger \"$localizedTrigger\"."
            } else {
                "$markerName was logged during $localizedTime."
            }
        }
    }
}

private fun buildCalendarPatternHelperLines(
    patternBreakdown: List<Pair<String, Int>>,
    selectedLanguage: String,
    activePatternTypeFilter: PatternType?,
    patternTypesByLabel: Map<String, Set<PatternType>>,
    markerNamesByLabel: Map<String, Set<String>>
): List<String> {
    if (patternBreakdown.isEmpty()) return emptyList()

    return patternBreakdown
        .take(2)
        .map { (label, count) ->
            val sourceType = activePatternTypeFilter ?: patternTypesByLabel[label]?.firstOrNull()
            val markerNames = markerNamesByLabel[label].orEmpty()
            buildCalendarPatternExplanationLine(
                label = label,
                count = count,
                sourceType = sourceType,
                markerNames = markerNames,
                selectedLanguage = selectedLanguage
            )
        }
}

private fun buildCalendarPatternExplanationLine(
    label: String,
    count: Int,
    sourceType: PatternType?,
    markerNames: Set<String>,
    selectedLanguage: String
): String {
    val markerHint = markerNames.firstOrNull()?.let {
        when (selectedLanguage) {
            "German" -> "Marker \"$it\""
            "Spanish" -> "marcador \"$it\""
            else -> "marker \"$it\""
        }
    }

    return when (sourceType) {
        PatternType.FOOD_MARKER_CORRELATION -> when (selectedLanguage) {
            "German" -> "\"$label\" erscheint, weil der Lebensmittel-Marker-Filter auf diesen Zusammenhang hinweist${markerHint?.let { " ($it)" } ?: ""}. Treffer heute: $count."
            "Spanish" -> "\"$label\" aparece porque el filtro de comida-marcador detectó esta relación${markerHint?.let { " ($it)" } ?: ""}. Coincidencias hoy: $count."
            else -> "\"$label\" appears because the food-marker filter detected this relation${markerHint?.let { " ($it)" } ?: ""}. Matches today: $count."
        }

        PatternType.TIME_MARKER_CORRELATION -> {
            val localizedTimeLabel = localizeCalendarPatternTimeLabel(label, selectedLanguage)
            when (selectedLanguage) {
                "German" -> "\"$localizedTimeLabel\" erscheint, weil der Zeitmuster-Filter Marker in diesem Zeitfenster erkannt hat${markerHint?.let { " ($it)" } ?: ""}. Treffer heute: $count."
                "Spanish" -> "\"$localizedTimeLabel\" aparece porque el filtro basado en tiempo detectó marcadores en esta franja${markerHint?.let { " ($it)" } ?: ""}. Coincidencias hoy: $count."
                else -> "\"$localizedTimeLabel\" appears because the time-based filter detected marker activity in this time window${markerHint?.let { " ($it)" } ?: ""}. Matches today: $count."
            }
        }

        PatternType.BLOOD_SUGAR_SPIKE -> when (selectedLanguage) {
            "German" -> "\"$label\" erscheint wegen des Blutzucker-Spitzen-Filters: dieses Lebensmittel war mit Spitzenwerten verknüpft${markerHint?.let { " und $it" } ?: ""}. Treffer heute: $count."
            "Spanish" -> "\"$label\" aparece por el filtro de picos de glucosa: este alimento se relacionó con subidas altas${markerHint?.let { " y $it" } ?: ""}. Coincidencias hoy: $count."
            else -> "\"$label\" appears due to the blood sugar spike filter: this food was linked to high spikes${markerHint?.let { " and $it" } ?: ""}. Matches today: $count."
        }

        PatternType.BLOOD_SUGAR_DROP -> when (selectedLanguage) {
            "German" -> "\"$label\" erscheint wegen des Blutzucker-Abfall-Filters: dieses Lebensmittel war mit Abfällen verknüpft${markerHint?.let { " und $it" } ?: ""}. Treffer heute: $count."
            "Spanish" -> "\"$label\" aparece por el filtro de bajadas de glucosa: este elemento se relacionó con descensos${markerHint?.let { " y $it" } ?: ""}. Coincidencias hoy: $count."
            else -> "\"$label\" appears due to the blood sugar drop filter: this entry was linked to glucose drops${markerHint?.let { " and $it" } ?: ""}. Matches today: $count."
        }

        PatternType.INSULIN_PATTERN -> when (selectedLanguage) {
            "German" -> "\"$label\" erscheint, weil der Insulin-Filter dazu ein wiederkehrendes Muster erkannt hat. Treffer heute: $count."
            "Spanish" -> "\"$label\" aparece porque el filtro de insulina detectó un patrón repetido aquí. Coincidencias hoy: $count."
            else -> "\"$label\" appears because the insulin filter detected a repeating pattern here. Matches today: $count."
        }

        PatternType.CUSTOM_COLUMN_PATTERN -> when (selectedLanguage) {
            "German" -> "\"$label\" erscheint aus einem Muster in benutzerdefinierten Spalten. Treffer heute: $count."
            "Spanish" -> "\"$label\" aparece por un patrón detectado en columnas personalizadas. Coincidencias hoy: $count."
            else -> "\"$label\" appears from a pattern detected in custom columns. Matches today: $count."
        }

        null -> when (selectedLanguage) {
            "German" -> "\"$label\" kommt aus den aktuell aktiven Musterfiltern. Treffer heute: $count."
            "Spanish" -> "\"$label\" viene de los filtros de patrones activos. Coincidencias hoy: $count."
            else -> "\"$label\" comes from the currently active pattern filters. Matches today: $count."
        }
    }
}

private fun localizeCalendarPatternTimeLabel(timeLabel: String, selectedLanguage: String): String {
    return when (timeLabel.trim().lowercase()) {
        "morning" -> when (selectedLanguage) {
            "German" -> "Morgen"
            "Spanish" -> "mañana"
            else -> "Morning"
        }
        "afternoon" -> when (selectedLanguage) {
            "German" -> "Nachmittag"
            "Spanish" -> "tarde"
            else -> "Afternoon"
        }
        "evening" -> when (selectedLanguage) {
            "German" -> "Abend"
            "Spanish" -> "noche"
            else -> "Evening"
        }
        "night" -> when (selectedLanguage) {
            "German" -> "Nacht"
            "Spanish" -> "noche"
            else -> "Night"
        }
        else -> timeLabel
    }
}

private fun localizeCalendarMarkerTimeSlot(startTime: String, selectedLanguage: String): String {
    return when (startTime.lowercase()) {
        "morning" -> when (selectedLanguage) {
            "German" -> "Morgen"
            "Spanish" -> "la mañana"
            else -> "the morning"
        }
        "afternoon" -> when (selectedLanguage) {
            "German" -> "Nachmittag"
            "Spanish" -> "la tarde"
            else -> "the afternoon"
        }
        "evening" -> when (selectedLanguage) {
            "German" -> "Abend"
            "Spanish" -> "la noche"
            else -> "the evening"
        }
        "night" -> when (selectedLanguage) {
            "German" -> "Nacht"
            "Spanish" -> "la madrugada"
            else -> "the night"
        }
        "all_day" -> when (selectedLanguage) {
            "German" -> "den ganzen Tag"
            "Spanish" -> "todo el día"
            else -> "the whole day"
        }
        else -> when (selectedLanguage) {
            "German" -> "diesem Eintrag"
            "Spanish" -> "esta entrada"
            else -> "this entry"
        }
    }
}

private fun localizeCalendarTriggerLabel(trigger: String, selectedLanguage: String): String {
    val normalizedTrigger = trigger.trim().lowercase()
    return when (normalizedTrigger) {
        "headache" -> when (selectedLanguage) {
            "German" -> "Kopfschmerzen"
            "Spanish" -> "dolor de cabeza"
            else -> "headache"
        }
        "nausea" -> when (selectedLanguage) {
            "German" -> "Übelkeit"
            "Spanish" -> "náuseas"
            else -> "nausea"
        }
        "high_blood_sugar" -> when (selectedLanguage) {
            "German" -> "hoher Blutzucker"
            "Spanish" -> "azúcar alta"
            else -> "high blood sugar"
        }
        "low_energy" -> when (selectedLanguage) {
            "German" -> "niedrige Energie"
            "Spanish" -> "baja energía"
            else -> "low energy"
        }
        else -> trigger.replace("_", " ")
    }
}

@Composable
private fun VisualizationMetricPill(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    color: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isActive) 1.5.dp else 1.dp, if (isActive) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isActive) color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PatternCard(
    pattern: DetectedPattern,
    selectedLanguage: String = "English",
    onDismiss: () -> Unit,
    onIgnoreFood: () -> Unit,
    onNavigateToDate: (String) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAllOccurrences by remember { mutableStateOf(false) }
    var showIgnoreDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                pattern.confidenceScore >= 0.8f -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                pattern.confidenceScore >= 0.6f -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        translateMarkerName(pattern.markerName, selectedLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        when (selectedLanguage) {
                            "German" -> "Nach: ${translateTimeOfDay(pattern.correlatedItem, selectedLanguage)}"
                            "Spanish" -> "Después de: ${translateTimeOfDay(pattern.correlatedItem, selectedLanguage)}"
                            else -> "After: ${translateTimeOfDay(pattern.correlatedItem, selectedLanguage)}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.dismiss),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConfidenceBadge(pattern.confidenceScore, selectedLanguage)
                
                Text(
                    when (selectedLanguage) {
                        "German" -> "${pattern.occurrences} Vorkommen"
                        "Spanish" -> "${pattern.occurrences} ocurrencia${if (pattern.occurrences != 1) "s" else ""}"
                        else -> "${pattern.occurrences} occurrence${if (pattern.occurrences != 1) "s" else ""}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                translateSuggestion(pattern.suggestion, selectedLanguage),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Show Ignore Food button for food-related patterns
            if (pattern.patternType == PatternType.FOOD_MARKER_CORRELATION || 
                pattern.patternType == PatternType.BLOOD_SUGAR_SPIKE) {
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { showIgnoreDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.ignore_this_food))
                }
            }
            
            if (pattern.details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isExpanded) 
                            stringResource(R.string.hide_details) 
                        else 
                            stringResource(R.string.show_details)
                    )
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.occurrences_label),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val displayedOccurrences = if (showAllOccurrences) pattern.details else pattern.details.take(5)
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = if (showAllOccurrences) 400.dp else 1000.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            displayedOccurrences.forEach { occurrence ->
                                OccurrenceItem(
                                    occurrence = occurrence,
                                    selectedLanguage = selectedLanguage,
                                    onClick = {
                                        onNavigateToDate("${occurrence.date}|${occurrence.timeOfDay}")
                                    }
                                )
                            }
                        }
                        
                        if (pattern.details.size > 5 && !showAllOccurrences) {
                            TextButton(
                                onClick = { showAllOccurrences = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    stringResource(R.string.show_all_occurrences, pattern.details.size),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        if (showAllOccurrences) {
                            TextButton(
                                onClick = { showAllOccurrences = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    stringResource(R.string.show_less),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Ignore Food Confirmation Dialog
    if (showIgnoreDialog) {
        AlertDialog(
            onDismissRequest = { showIgnoreDialog = false },
            title = { 
                Text(stringResource(R.string.ignore_food_title))
            },
            text = {
                Column {
                    Text(stringResource(R.string.ignore_food_message, pattern.correlatedItem))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.ignore_food_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onIgnoreFood()
                        showIgnoreDialog = false
                    }
                ) {
                    Text(stringResource(R.string.ignore_food_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showIgnoreDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun ConfidenceBadge(confidence: Float, selectedLanguage: String = "English") {
    val text = when {
        confidence >= 0.8f -> stringResource(R.string.high_confidence)
        confidence >= 0.6f -> stringResource(R.string.medium_confidence)
        else -> stringResource(R.string.low_confidence)
    }
    
    val color = when {
        confidence >= 0.8f -> Color(0xFFD32F2F)
        confidence >= 0.6f -> Color(0xFFF57C00)
        else -> Color(0xFF757575)
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun localizedPatternTypeLabel(patternType: PatternType): String {
    return when (patternType) {
        PatternType.FOOD_MARKER_CORRELATION -> stringResource(R.string.food_marker_correlations)
        PatternType.TIME_MARKER_CORRELATION -> stringResource(R.string.time_based_patterns)
        PatternType.BLOOD_SUGAR_SPIKE -> stringResource(R.string.blood_sugar_spikes)
        PatternType.BLOOD_SUGAR_DROP -> stringResource(R.string.blood_sugar_drops)
        PatternType.INSULIN_PATTERN -> stringResource(R.string.insulin_patterns)
        PatternType.CUSTOM_COLUMN_PATTERN -> stringResource(R.string.custom_column_patterns)
    }
}

private fun patternTypeIcon(patternType: PatternType): String {
    return when (patternType) {
        PatternType.FOOD_MARKER_CORRELATION -> "🍕"
        PatternType.TIME_MARKER_CORRELATION -> "🕐"
        PatternType.BLOOD_SUGAR_SPIKE -> "📈"
        PatternType.BLOOD_SUGAR_DROP -> "📉"
        PatternType.INSULIN_PATTERN -> "💉"
        PatternType.CUSTOM_COLUMN_PATTERN -> "🧩"
    }
}

@Composable
fun PatternVisualization(
    patterns: List<DetectedPattern>,
    selectedLanguage: String = "English",
    onFilterType: (PatternType) -> Unit = {},
    onFilterFood: (String) -> Unit = {},
    activeTypeFilter: PatternType? = null,
    activeFoodFilter: String? = null
) {
    var patternTypeVisibility by remember { mutableStateOf(PatternTypeVisibilityOption.DETECTED_ONLY) }
    val patternTypeCounts = remember(patterns) {
        patterns
            .groupingBy { it.patternType }
            .eachCount()
    }

    val patternTypeBreakdown = remember(patternTypeCounts, patternTypeVisibility) {
        val allTypes = enumValues<PatternType>().toList()
        val visibleTypes = if (patternTypeVisibility == PatternTypeVisibilityOption.SHOW_ALL) {
            allTypes
        } else {
            allTypes.filter { type -> (patternTypeCounts[type] ?: 0) > 0 }
        }

        visibleTypes.map { type ->
            type to (patternTypeCounts[type] ?: 0)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.pattern_types),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    FilterChip(
                        selected = patternTypeVisibility == PatternTypeVisibilityOption.SHOW_ALL,
                        onClick = {
                            patternTypeVisibility = if (patternTypeVisibility == PatternTypeVisibilityOption.SHOW_ALL) {
                                PatternTypeVisibilityOption.DETECTED_ONLY
                            } else {
                                PatternTypeVisibilityOption.SHOW_ALL
                            }
                        },
                        label = {
                            Text(
                                if (patternTypeVisibility == PatternTypeVisibilityOption.SHOW_ALL) {
                                    stringResource(R.string.pattern_types_show_all)
                                } else {
                                    stringResource(R.string.pattern_types_detected_only)
                                }
                            )
                        }
                    )
                }
                
                patternTypeBreakdown.forEach { (type, count) ->
                    val isEnabled = count > 0
                    PatternTypeRow(
                        icon = patternTypeIcon(type),
                        label = localizedPatternTypeLabel(type),
                        count = count,
                        isActive = activeTypeFilter == type,
                        enabled = isEnabled,
                        onClick = {
                            if (isEnabled) {
                                onFilterType(type)
                            }
                        }
                    )
                }
            }
            
            HorizontalDivider()
            
            // Top risky foods
            val topRiskyFoods = patterns
                .sortedByDescending { it.confidenceScore }
                .take(3)
            
            if (topRiskyFoods.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.top_risky_foods),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    topRiskyFoods.forEach { pattern ->
                        RiskyFoodItem(
                            foodName = pattern.correlatedItem,
                            markerName = pattern.markerName,
                            confidence = pattern.confidenceScore,
                            occurrences = pattern.occurrences,
                            selectedLanguage = selectedLanguage,
                            isActive = activeFoodFilter == pattern.correlatedItem,
                            onClick = { onFilterFood(pattern.correlatedItem) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RiskBar(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    isActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) color.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (isActive) 1.5.dp else 1.dp,
            if (isActive) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.width(86.dp)
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(22.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        RoundedCornerShape(6.dp)
                    )
            ) {
                val percentage = if (total > 0) count.toFloat() / total else 0f
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(percentage)
                        .background(color.copy(alpha = if (isActive) 0.95f else 0.75f), RoundedCornerShape(6.dp))
                )
                
                Text(
                    "$count",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PatternTypeRow(
    icon: String,
    label: String,
    count: Int,
    isActive: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val activeRow = isActive && enabled

    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            if (enabled) {
                onClick()
            }
        },
        shape = RoundedCornerShape(12.dp),
        color = when {
            !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            activeRow -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.44f)
            else -> MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            if (activeRow) 1.5.dp else 1.dp,
            if (activeRow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(icon, fontSize = 18.sp)
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (activeRow) FontWeight.Bold else FontWeight.Normal,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (activeRow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    "$count",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (activeRow) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun RiskyFoodItem(
    foodName: String,
    markerName: String,
    confidence: Float,
    occurrences: Int,
    selectedLanguage: String = "English",
    isActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val baseColor = when {
        confidence >= 0.8f -> MaterialTheme.colorScheme.errorContainer
        confidence >= 0.6f -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) baseColor.copy(alpha = 0.95f) else baseColor.copy(alpha = 0.72f),
        border = BorderStroke(
            if (isActive) 1.5.dp else 1.dp,
            if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        shadowElevation = if (isActive) 3.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    foodName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            Text(
                when (selectedLanguage) {
                    "German" -> "→ ${translateMarkerName(markerName, selectedLanguage)} ($occurrences Mal)"
                    "Spanish" -> "→ ${translateMarkerName(markerName, selectedLanguage)} ($occurrences veces)"
                    else -> "→ ${translateMarkerName(markerName, selectedLanguage)} ($occurrences times)"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
        
        Text(
            "${(confidence * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = when {
                confidence >= 0.8f -> Color(0xFFD32F2F)
                confidence >= 0.6f -> Color(0xFFF57C00)
                else -> Color(0xFF757575)
            }
        )
        }
    }
}

@Composable
fun OccurrenceItem(
    occurrence: PatternOccurrence, 
    selectedLanguage: String = "English",
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                formatDate(occurrence.date),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                translateOccurrenceContext(occurrence.context, selectedLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            if (occurrence.bloodSugar.isNotBlank()) {
                Text(
                    when (selectedLanguage) {
                        "German" -> "Blutzucker: ${occurrence.bloodSugar}"
                        "Spanish" -> "Azúcar en Sangre: ${occurrence.bloodSugar}"
                        else -> "Blood Sugar: ${occurrence.bloodSugar}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }
        
        Text(
            translateTimeOfDay(occurrence.timeOfDay.replaceFirstChar { it.uppercase() }, selectedLanguage),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// Translation helper functions
fun translateMarkerName(markerName: String, selectedLanguage: String): String {
    return when (markerName) {
        "Feel Bad" -> when (selectedLanguage) {
            "German" -> "Fühle mich schlecht"
            "Spanish" -> "Me siento mal"
            else -> markerName
        }
        "Blood Sugar Spike" -> when (selectedLanguage) {
            "German" -> "Blutzuckerspitze"
            "Spanish" -> "Pico de Azúcar"
            else -> markerName
        }
        else -> markerName // Custom markers keep their original name
    }
}

fun translateTimeOfDay(timeOfDay: String, selectedLanguage: String): String {
    val lowerTime = timeOfDay.lowercase()
    return when {
        lowerTime.contains("morning") -> when (selectedLanguage) {
            "German" -> timeOfDay.replace("morning", "Morgen", ignoreCase = true)
            "Spanish" -> timeOfDay.replace("morning", "Mañana", ignoreCase = true)
            else -> timeOfDay
        }
        lowerTime.contains("afternoon") -> when (selectedLanguage) {
            "German" -> timeOfDay.replace("afternoon", "Nachmittag", ignoreCase = true)
            "Spanish" -> timeOfDay.replace("afternoon", "Tarde", ignoreCase = true)
            else -> timeOfDay
        }
        lowerTime.contains("evening") -> when (selectedLanguage) {
            "German" -> timeOfDay.replace("evening", "Abend", ignoreCase = true)
            "Spanish" -> timeOfDay.replace("evening", "Noche", ignoreCase = true)
            else -> timeOfDay
        }
        lowerTime.contains("night") -> when (selectedLanguage) {
            "German" -> timeOfDay.replace("night", "Nacht", ignoreCase = true)
            "Spanish" -> timeOfDay.replace("night", "Noche", ignoreCase = true)
            else -> timeOfDay
        }
        else -> timeOfDay
    }
}

fun translateSuggestion(suggestion: String, selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> {
            suggestion
                .replace("You often experience", "Sie erleben oft")
                .replace("during", "während")
                .replace("Consider tracking what you do or eat before this time", "Überlegen Sie, was Sie vor dieser Zeit tun oder essen")
                .replace("Consider limiting this food", "Erwägen Sie, dieses Lebensmittel zu begrenzen")
                .replace("Strongly consider avoiding this food", "Erwägen Sie dringend, dieses Lebensmittel zu vermeiden")
                .replace("times", "Mal")
                .replace("ALERT", "WARNUNG")
                .replace("WARNING", "ACHTUNG")
                .replace("causes large blood sugar spikes", "verursacht große Blutzuckerspitzen")
                .replace("causes significant blood sugar spikes", "verursacht erhebliche Blutzuckerspitzen")
        }
        "Spanish" -> {
            suggestion
                .replace("You often experience", "A menudo experimentas")
                .replace("during", "durante")
                .replace("Consider tracking what you do or eat before this time", "Considera rastrear lo que haces o comes antes de este momento")
                .replace("Consider limiting this food", "Considera limitar esta comida")
                .replace("Strongly consider avoiding this food", "Considera seriamente evitar esta comida")
                .replace("times", "veces")
                .replace("ALERT", "ALERTA")
                .replace("WARNING", "ADVERTENCIA")
                .replace("causes large blood sugar spikes", "causa grandes picos de azúcar en sangre")
                .replace("causes significant blood sugar spikes", "causa picos significativos de azúcar en sangre")
        }
        else -> suggestion
    }
}

fun translateOccurrenceContext(context: String, selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> {
            context
                .replace("Ate", "Gegessen")
                .replace("same time", "gleiche Zeit")
                .replace("Immediate reaction", "Sofortige Reaktion")
                .replace("Delayed reaction", "Verzögerte Reaktion")
                .replace("Marker applied at", "Marker angewendet um")
                .replace("morning", "Morgen")
                .replace("afternoon", "Nachmittag")
                .replace("evening", "Abend")
                .replace("night", "Nacht")
        }
        "Spanish" -> {
            context
                .replace("Ate", "Comió")
                .replace("same time", "mismo tiempo")
                .replace("Immediate reaction", "Reacción inmediata")
                .replace("Delayed reaction", "Reacción retardada")
                .replace("Marker applied at", "Marcador aplicado en")
                .replace("morning", "mañana")
                .replace("afternoon", "tarde")
                .replace("evening", "noche")
                .replace("night", "noche")
        }
        else -> context
    }
}

fun formatDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    } catch (e: Exception) {
        dateString
    }
}
