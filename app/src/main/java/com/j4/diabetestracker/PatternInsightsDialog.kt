package com.j4.diabetestracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.roundToInt

private enum class PatternInsightRangeOption { ALL_TIME, ONE_YEAR, ONE_MONTH, CUSTOM }

@Composable
fun PatternInsightsDialog(
    entries: List<DiabetesEntry>,
    customMarkers: List<CustomMarkerType>,
    selectedLanguage: String,
    onDismiss: () -> Unit,
    onOpenDetailedAnalysis: (Int, ClosedRange<LocalDate>?) -> Unit
) {
    val dialogScrollState = rememberScrollState()
    var selectedRange by remember { mutableStateOf(PatternInsightRangeOption.ALL_TIME) }
    var customStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var customEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val today = remember { LocalDate.now() }
    val selectedRangeBounds = selectedBounds(selectedRange, customStartDate, customEndDate, today)
    val hasInvalidCustomRange = selectedRange == PatternInsightRangeOption.CUSTOM &&
        customStartDate != null && customEndDate != null && customStartDate!!.isAfter(customEndDate)

    val filteredEntries = remember(entries, selectedRange, customStartDate, customEndDate, today) {
        entries.filter { entry ->
            val entryDate = parsePatternEntryDate(entry.date) ?: return@filter false
            when (selectedRange) {
                PatternInsightRangeOption.ALL_TIME -> true
                else -> selectedRangeBounds?.let { !entryDate.isBefore(it.start) && !entryDate.isAfter(it.endInclusive) } ?: false
            }
        }
    }

    val insights = remember(filteredEntries, customMarkers, selectedLanguage) {
        PatternAnalyzer.analyzePatterns(filteredEntries, customMarkers, selectedLanguage)
    }

    val analysisDays = selectedAnalysisDays(selectedRange, selectedRangeBounds)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizedTitle(selectedLanguage)) },
        text = {
            Column {
                PatternInsightsDialogContent(
                    selectedLanguage = selectedLanguage,
                    selectedRange = selectedRange,
                    onRangeSelected = { selectedRange = it },
                    customStartDate = customStartDate,
                    customEndDate = customEndDate,
                    hasInvalidCustomRange = hasInvalidCustomRange,
                    onStartDateClick = { showStartDatePicker = true },
                    onEndDateClick = { showEndDatePicker = true },
                    filteredEntriesCount = filteredEntries.size,
                    rangeBounds = selectedRangeBounds,
                    insights = insights,
                    onInsightClick = { onOpenDetailedAnalysis(analysisDays, selectedRangeBounds) },
                    scrollState = dialogScrollState
                )
                ScrollableDialogHint(
                    selectedLanguage = selectedLanguage,
                    scrollState = dialogScrollState,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(localizedClose(selectedLanguage)) }
        }
    )

    if (showStartDatePicker) {
        LocalDatePickerDialog(
            initialDate = customStartDate ?: today,
            onDismiss = { showStartDatePicker = false },
            onConfirm = {
                customStartDate = it
                showStartDatePicker = false
            }
        )
    }

    if (showEndDatePicker) {
        LocalDatePickerDialog(
            initialDate = customEndDate ?: today,
            onDismiss = { showEndDatePicker = false },
            onConfirm = {
                customEndDate = it
                showEndDatePicker = false
            }
        )
    }
}

@Composable
private fun PatternInsightsDialogContent(
    selectedLanguage: String,
    selectedRange: PatternInsightRangeOption,
    onRangeSelected: (PatternInsightRangeOption) -> Unit,
    customStartDate: LocalDate?,
    customEndDate: LocalDate?,
    hasInvalidCustomRange: Boolean,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    filteredEntriesCount: Int,
    rangeBounds: ClosedRange<LocalDate>?,
    insights: List<PatternInsight>,
    onInsightClick: () -> Unit,
    scrollState: ScrollState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 540.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = localizedRangeHeader(selectedLanguage),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PatternInsightRangeOption.entries.forEach { option ->
                FilterChip(
                    selected = selectedRange == option,
                    onClick = { onRangeSelected(option) },
                    label = { Text(rangeOptionLabel(option, selectedLanguage)) }
                )
            }
        }

        if (selectedRange == PatternInsightRangeOption.CUSTOM) {
            CustomRangePickerRow(
                selectedLanguage = selectedLanguage,
                customStartDate = customStartDate,
                customEndDate = customEndDate,
                hasInvalidCustomRange = hasInvalidCustomRange,
                onStartDateClick = onStartDateClick,
                onEndDateClick = onEndDateClick
            )
        }

        Text(
            text = localizedEntriesInRange(selectedLanguage, filteredEntriesCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (selectedRange == PatternInsightRangeOption.CUSTOM && rangeBounds == null) {
            Text(
                text = localizedSelectBothDates(selectedLanguage),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            InsightsResultSection(
                selectedLanguage = selectedLanguage,
                insights = insights,
                onInsightClick = onInsightClick
            )
        }
    }
}

@Composable
private fun CustomRangePickerRow(
    selectedLanguage: String,
    customStartDate: LocalDate?,
    customEndDate: LocalDate?,
    hasInvalidCustomRange: Boolean,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(onClick = onStartDateClick, modifier = Modifier.weight(1f)) {
            Text(customStartDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: localizedStartDateLabel(selectedLanguage))
        }
        OutlinedButton(onClick = onEndDateClick, modifier = Modifier.weight(1f)) {
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

@Composable
private fun InsightsResultSection(
    selectedLanguage: String,
    insights: List<PatternInsight>,
    onInsightClick: () -> Unit
) {
    if (insights.isEmpty()) {
        PatternNoDataState(selectedLanguage)
        return
    }

    Text(
        text = localizedDiscoveredPatterns(selectedLanguage, insights.size),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    insights.forEach { insight ->
        InsightCardItem(insight = insight, selectedLanguage = selectedLanguage, onClick = onInsightClick)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = localizedTapForDetails(selectedLanguage),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun InsightCardItem(insight: PatternInsight, selectedLanguage: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (insight.type) {
                InsightType.FOOD_CORRELATION -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                InsightType.BLOOD_SUGAR_PATTERN -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                InsightType.TIME_PATTERN -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                InsightType.FREQUENCY_INSIGHT -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                InsightType.TRIGGER_WARNING -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(insight.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(insight.description, style = MaterialTheme.typography.bodyMedium)

                if (insight.confidence > 0f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(localizedConfidenceLabel(selectedLanguage), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { insight.confidence },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when {
                                insight.confidence >= 0.7f -> Color(0xFF4CAF50)
                                insight.confidence >= 0.5f -> Color(0xFFFFC107)
                                else -> Color(0xFFFF9800)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(insight.confidence * 100).roundToInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun PatternNoDataState(selectedLanguage: String) {
    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🔍", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = localizedNoDataTitle(selectedLanguage),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = localizedNoDataHint(selectedLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LocalDatePickerDialog(initialDate: LocalDate, onDismiss: () -> Unit, onConfirm: (LocalDate?) -> Unit) {
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onConfirm(dateState.selectedDateMillis?.toLocalDateFromEpochMillis()) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = dateState)
    }
}

private fun selectedBounds(
    selectedRange: PatternInsightRangeOption,
    customStartDate: LocalDate?,
    customEndDate: LocalDate?,
    today: LocalDate
): ClosedRange<LocalDate>? {
    return when (selectedRange) {
        PatternInsightRangeOption.ALL_TIME -> null
        PatternInsightRangeOption.ONE_YEAR -> today.minusYears(1)..today
        PatternInsightRangeOption.ONE_MONTH -> today.minusMonths(1)..today
        PatternInsightRangeOption.CUSTOM -> {
            if (customStartDate == null || customEndDate == null || customStartDate.isAfter(customEndDate)) null
            else customStartDate..customEndDate
        }
    }
}

private fun selectedAnalysisDays(selectedRange: PatternInsightRangeOption, bounds: ClosedRange<LocalDate>?): Int {
    return when (selectedRange) {
        PatternInsightRangeOption.ONE_MONTH -> 30
        PatternInsightRangeOption.ONE_YEAR -> 365
        PatternInsightRangeOption.ALL_TIME -> 36500
        PatternInsightRangeOption.CUSTOM -> {
            if (bounds == null) 30
            else max(1, ChronoUnit.DAYS.between(bounds.start, bounds.endInclusive).toInt() + 1)
        }
    }
}

private fun parsePatternEntryDate(value: String): LocalDate? {
    val normalized = value.trim()
    if (normalized.isEmpty()) return null
    val formats = listOf(DateTimeFormatter.ofPattern("dd.MM.yyyy"), DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    for (formatter in formats) {
        try {
            return LocalDate.parse(normalized, formatter)
        } catch (_: Exception) {
            // continue
        }
    }
    return null
}

private fun Long.toLocalDateFromEpochMillis(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}

private fun localizedTitle(language: String): String = when (language) {
    "German" -> "📊 Muster-Analyse"
    "Spanish" -> "📊 Análisis de Patrones"
    else -> "📊 Pattern Analysis"
}

private fun localizedClose(language: String): String = when (language) {
    "German" -> "Schließen"
    "Spanish" -> "Cerrar"
    else -> "Close"
}

private fun localizedRangeHeader(language: String): String = when (language) {
    "German" -> "Zeitraum"
    "Spanish" -> "Rango de tiempo"
    else -> "Time range"
}

private fun rangeOptionLabel(option: PatternInsightRangeOption, language: String): String = when (option) {
    PatternInsightRangeOption.ALL_TIME -> if (language == "German") "Gesamt" else if (language == "Spanish") "Todo" else "All time"
    PatternInsightRangeOption.ONE_YEAR -> if (language == "German") "1 Jahr" else if (language == "Spanish") "1 año" else "1 year"
    PatternInsightRangeOption.ONE_MONTH -> if (language == "German") "1 Monat" else if (language == "Spanish") "1 mes" else "1 month"
    PatternInsightRangeOption.CUSTOM -> if (language == "German") "Eigene Daten" else if (language == "Spanish") "Fechas específicas" else "Custom dates"
}

private fun localizedStartDateLabel(language: String): String = when (language) {
    "German" -> "Start wählen"
    "Spanish" -> "Elegir inicio"
    else -> "Choose start"
}

private fun localizedEndDateLabel(language: String): String = when (language) {
    "German" -> "Ende wählen"
    "Spanish" -> "Elegir fin"
    else -> "Choose end"
}

private fun localizedInvalidDateRange(language: String): String = when (language) {
    "German" -> "Startdatum muss vor dem Enddatum liegen."
    "Spanish" -> "La fecha de inicio debe ser anterior a la fecha final."
    else -> "Start date must be before end date."
}

private fun localizedEntriesInRange(language: String, count: Int): String = when (language) {
    "German" -> "Einträge im Bereich: $count"
    "Spanish" -> "Entradas en rango: $count"
    else -> "Entries in range: $count"
}

private fun localizedSelectBothDates(language: String): String = when (language) {
    "German" -> "Bitte Start- und Enddatum auswählen, um Muster zu berechnen."
    "Spanish" -> "Seleccione fecha de inicio y fin para calcular patrones."
    else -> "Select both start and end dates to calculate patterns."
}

private fun localizedDiscoveredPatterns(language: String, count: Int): String = when (language) {
    "German" -> "Entdeckte Muster ($count)"
    "Spanish" -> "Patrones Descubiertos ($count)"
    else -> "Discovered Patterns ($count)"
}

private fun localizedTapForDetails(language: String): String = when (language) {
    "German" -> "💡 Tippen Sie auf ein Muster, um detaillierte Analysen zu sehen"
    "Spanish" -> "💡 Toque un patrón para ver análisis detallados"
    else -> "💡 Tap any pattern to view detailed analysis"
}

private fun localizedNoDataTitle(language: String): String = when (language) {
    "German" -> "Nicht genug Daten für Muster-Analyse"
    "Spanish" -> "No hay suficientes datos para análisis de patrones"
    else -> "Not enough data for pattern analysis"
}

private fun localizedNoDataHint(language: String): String = when (language) {
    "German" -> "Versuchen Sie einen größeren Zeitraum oder fügen Sie mehr Marker hinzu"
    "Spanish" -> "Pruebe un rango más amplio o agregue más marcadores"
    else -> "Try a larger range or add more markers"
}

private fun localizedConfidenceLabel(language: String): String = when (language) {
    "German" -> "Konfidenz:"
    "Spanish" -> "Confianza:"
    else -> "Confidence:"
}
