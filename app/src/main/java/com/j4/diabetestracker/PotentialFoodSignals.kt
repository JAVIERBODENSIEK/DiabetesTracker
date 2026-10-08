package com.j4.diabetestracker

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class PotentialFoodStatus {
    POTENTIAL,
    NOT_POTENTIAL,
    IGNORED,
}

private enum class PotentialFoodViewMode {
    LIST,
    CALENDAR,
}

private data class PotentialFoodSignal(
    val foodName: String,
    val emoji: String,
    val maxConfidence: Float,
    val patternCount: Int,
    val supportDates: List<LocalDate>,
    val contradictionDates: List<LocalDate>,
    val status: PotentialFoodStatus,
)

private data class FoodSignalEntrySnapshot(
    val date: LocalDate,
    val foodsByNormalizedName: Map<String, FoodPreset>,
    val hasFeelBadMarker: Boolean,
)

private data class PotentialFoodCalendarDay(
    val date: LocalDate,
    val emojis: List<String>,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PotentialFoodSignalsSection(
    entries: List<DiabetesEntry>,
    patterns: List<DetectedPattern>,
    activeDateRange: ClosedRange<LocalDate>?,
    context: Context,
    selectedLanguage: String,
    onNavigateToDate: (String) -> Unit,
    onFilterFood: (String) -> Unit = {},
) {
    var viewMode by remember { mutableStateOf(PotentialFoodViewMode.LIST) }
    val foodSignals = remember(entries, patterns, activeDateRange, context) {
        buildPotentialFoodSignals(
            entries = entries,
            patterns = patterns,
            activeDateRange = activeDateRange,
            context = context,
        )
    }

    val potentialCount = foodSignals.count { it.status == PotentialFoodStatus.POTENTIAL }
    val notPotentialCount = foodSignals.count { it.status == PotentialFoodStatus.NOT_POTENTIAL }
    val hasValidationFixtures = remember(entries) {
        entries
            .filter { it.isTestData }
            .flatMap { snapshot -> snapshot.foodEntriesByColumn.values.flatten() }
            .map { food -> normalizeFoodKey(food.name) }
            .toSet()
            .let { names -> "donut" in names || "blueberries" in names }
    }
    val donutStatus = remember(foodSignals) {
        foodSignals.firstOrNull { normalizeFoodKey(it.foodName) == "donut" }?.status
    }
    val blueberriesStatus = remember(foodSignals) {
        foodSignals.firstOrNull { normalizeFoodKey(it.foodName) == "blueberries" }?.status
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = potentialFoodTitle(selectedLanguage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = potentialFoodSummary(
                    selectedLanguage = selectedLanguage,
                    potentialCount = potentialCount,
                    notPotentialCount = notPotentialCount,
                    total = foodSignals.size,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = viewMode == PotentialFoodViewMode.LIST,
                    onClick = { viewMode = PotentialFoodViewMode.LIST },
                    label = { Text(listLabel(selectedLanguage)) },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = viewMode == PotentialFoodViewMode.CALENDAR,
                    onClick = { viewMode = PotentialFoodViewMode.CALENDAR },
                    label = { Text(calendarLabel(selectedLanguage)) },
                    modifier = Modifier.weight(1f),
                )
            }

            if (foodSignals.isEmpty()) {
                Text(
                    text = potentialFoodEmptyLabel(selectedLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                if (viewMode == PotentialFoodViewMode.LIST) {
                    PotentialFoodSignalsList(
                        foodSignals = foodSignals,
                        selectedLanguage = selectedLanguage,
                        onNavigateToDate = onNavigateToDate,
                        onFilterFood = onFilterFood,
                    )
                } else {
                    PotentialFoodSignalsCalendar(
                        foodSignals = foodSignals,
                        activeDateRange = activeDateRange,
                        selectedLanguage = selectedLanguage,
                        onNavigateToDate = onNavigateToDate,
                    )
                }
            }

            Text(
                text = guidanceText(selectedLanguage, potentialCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )

            if (hasValidationFixtures) {
                PotentialFoodTestExpectationsHint(
                    selectedLanguage = selectedLanguage,
                    donutStatus = donutStatus,
                    blueberriesStatus = blueberriesStatus,
                )
            }
        }
    }
}

@Composable
private fun PotentialFoodTestExpectationsHint(
    selectedLanguage: String,
    donutStatus: PotentialFoodStatus?,
    blueberriesStatus: PotentialFoodStatus?,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = expectedResultsTitle(selectedLanguage),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = expectedDonutResultLine(selectedLanguage, donutStatus),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = expectedBlueberriesResultLine(selectedLanguage, blueberriesStatus),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PotentialFoodSignalsList(
    foodSignals: List<PotentialFoodSignal>,
    selectedLanguage: String,
    onNavigateToDate: (String) -> Unit,
    onFilterFood: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        foodSignals.forEach { signal ->
            PotentialFoodSignalCard(
                signal = signal,
                selectedLanguage = selectedLanguage,
                onNavigateToDate = onNavigateToDate,
                onFilterFood = onFilterFood,
            )
        }
    }
}

@Composable
private fun PotentialFoodSignalCard(
    signal: PotentialFoodSignal,
    selectedLanguage: String,
    onNavigateToDate: (String) -> Unit,
    onFilterFood: (String) -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${signal.emoji} ${signal.foodName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                PotentialFoodStatusPill(
                    status = signal.status,
                    selectedLanguage = selectedLanguage,
                )
            }

            Text(
                text = potentialFoodConfidenceLabel(
                    selectedLanguage = selectedLanguage,
                    confidence = signal.maxConfidence,
                    patternCount = signal.patternCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (signal.supportDates.isNotEmpty()) {
                DateChipRow(
                    title = supportDatesLabel(selectedLanguage),
                    dates = signal.supportDates,
                    onNavigateToDate = onNavigateToDate,
                )
            }

            if (signal.contradictionDates.isNotEmpty()) {
                DateChipRow(
                    title = contradictionDatesLabel(selectedLanguage),
                    dates = signal.contradictionDates,
                    onNavigateToDate = onNavigateToDate,
                )
            }

            TextButton(
                onClick = { onFilterFood(signal.foodName) },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(focusInPatternsLabel(selectedLanguage))
            }
        }
    }
}

@Composable
private fun PotentialFoodSignalsCalendar(
    foodSignals: List<PotentialFoodSignal>,
    activeDateRange: ClosedRange<LocalDate>?,
    selectedLanguage: String,
    onNavigateToDate: (String) -> Unit,
) {
    val dayMap = remember(foodSignals) { buildPotentialFoodCalendarDays(foodSignals) }
    if (dayMap.isEmpty()) {
        Text(
            text = calendarEmptyLabel(selectedLanguage),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    val minAllowedMonth = remember(activeDateRange, dayMap) {
        activeDateRange?.start?.let { YearMonth.from(it) }
            ?: dayMap.keys.minOrNull()?.let { YearMonth.from(it) }
            ?: YearMonth.now()
    }
    val maxAllowedMonth = remember(activeDateRange, dayMap) {
        activeDateRange?.endInclusive?.let { YearMonth.from(it) }
            ?: dayMap.keys.maxOrNull()?.let { YearMonth.from(it) }
            ?: YearMonth.now()
    }

    var displayedMonth by remember(maxAllowedMonth) { mutableStateOf(maxAllowedMonth) }
    val monthFormatter = remember(selectedLanguage) {
        val locale = when (selectedLanguage) {
            "German" -> Locale.GERMAN
            "Spanish" -> Locale("es")
            else -> Locale.ENGLISH
        }
        DateTimeFormatter.ofPattern("MMMM yyyy", locale)
    }

    val canGoPrevious = displayedMonth.isAfter(minAllowedMonth)
    val canGoNext = displayedMonth.isBefore(maxAllowedMonth)
    val cells = remember(displayedMonth, dayMap) { buildMonthCells(displayedMonth, dayMap) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                onClick = { if (canGoPrevious) displayedMonth = displayedMonth.minusMonths(1) },
                enabled = canGoPrevious,
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
            }
            Text(
                text = displayedMonth.format(monthFormatter),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(
                onClick = { if (canGoNext) displayedMonth = displayedMonth.plusMonths(1) },
                enabled = canGoNext,
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            weekdayLabels(selectedLanguage).forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                week.forEach { (date, dayInfo) ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clickable(enabled = date != null && dayInfo != null) {
                                if (date != null && dayInfo != null) {
                                    onNavigateToDate("${date}|all_day")
                                }
                            },
                        shape = MaterialTheme.shapes.small,
                        color = when {
                            dayInfo == null -> MaterialTheme.colorScheme.surface
                            else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        },
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = if (dayInfo == null) 0.1f else 0.35f),
                        ),
                    ) {
                        if (date == null) {
                            Spacer(modifier = Modifier.fillMaxWidth())
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 3.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                                Text(
                                    text = dayInfo?.emojis?.take(2)?.joinToString(separator = " ").orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = calendarHintLabel(selectedLanguage),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DateChipRow(
    title: String,
    dates: List<LocalDate>,
    onNavigateToDate: (String) -> Unit,
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            dates.take(4).forEach { date ->
                AssistChip(
                    onClick = { onNavigateToDate("${date}|all_day") },
                    label = { Text(date.format(formatter)) },
                )
            }
        }
    }
}

@Composable
private fun PotentialFoodStatusPill(
    status: PotentialFoodStatus,
    selectedLanguage: String,
) {
    val (label, color) = when (status) {
        PotentialFoodStatus.POTENTIAL -> potentialStatusLabel(selectedLanguage) to Color(0xFFD32F2F)
        PotentialFoodStatus.NOT_POTENTIAL -> notPotentialStatusLabel(selectedLanguage) to Color(0xFF2E7D32)
        PotentialFoodStatus.IGNORED -> ignoredStatusLabel(selectedLanguage) to Color(0xFF757575)
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.16f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun buildPotentialFoodCalendarDays(
    foodSignals: List<PotentialFoodSignal>,
): Map<LocalDate, PotentialFoodCalendarDay> {
    val dayEmojiMap = mutableMapOf<LocalDate, MutableSet<String>>()
    foodSignals
        .filter { it.status == PotentialFoodStatus.POTENTIAL }
        .forEach { signal ->
            signal.supportDates.forEach { date ->
                dayEmojiMap.getOrPut(date) { mutableSetOf() }.add(signal.emoji)
            }
        }

    return dayEmojiMap.mapValues { (date, emojis) ->
        PotentialFoodCalendarDay(
            date = date,
            emojis = emojis.toList().sorted(),
        )
    }
}

private fun buildMonthCells(
    month: YearMonth,
    dayMap: Map<LocalDate, PotentialFoodCalendarDay>,
): List<Pair<LocalDate?, PotentialFoodCalendarDay?>> {
    val firstDay = month.atDay(1)
    val leadingEmptyCells = firstDay.dayOfWeek.value - 1
    val days = (1..month.lengthOfMonth()).map { day ->
        val date = month.atDay(day)
        date to dayMap[date]
    }

    val cells = List(leadingEmptyCells) { null to null } + days
    val trailingEmptyCells = if (cells.size % 7 == 0) 0 else 7 - (cells.size % 7)
    return cells + List(trailingEmptyCells) { null to null }
}

private fun listLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Liste"
        "Spanish" -> "Lista"
        else -> "List"
    }
}

private fun calendarLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Kalender"
        "Spanish" -> "Calendario"
        else -> "Calendar"
    }
}

private fun calendarEmptyLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Keine aktiven Trigger-Tage im aktuellen Bereich."
        "Spanish" -> "No hay días activos de desencadenantes en este rango."
        else -> "No active trigger days in this range."
    }
}

private fun calendarHintLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Tippen Sie auf einen Tag, um direkt zum Tagebucheintrag zu springen."
        "Spanish" -> "Toca un día para abrir directamente la entrada del diario."
        else -> "Tap a day to jump directly to that diary entry."
    }
}

private fun weekdayLabels(selectedLanguage: String): List<String> {
    return when (selectedLanguage) {
        "German" -> listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
        "Spanish" -> listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do")
        else -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }
}

private fun buildPotentialFoodSignals(
    entries: List<DiabetesEntry>,
    patterns: List<DetectedPattern>,
    activeDateRange: ClosedRange<LocalDate>?,
    context: Context,
): List<PotentialFoodSignal> {
    val foodPatterns = patterns.filter {
        it.patternType == PatternType.FOOD_MARKER_CORRELATION &&
            it.correlatedItem.isNotBlank()
    }
    if (foodPatterns.isEmpty()) return emptyList()

    val entrySnapshots = entries
        .mapNotNull { entry ->
            val parsedDate = parsePotentialFoodDate(entry.date) ?: return@mapNotNull null
            if (activeDateRange != null && (parsedDate.isBefore(activeDateRange.start) || parsedDate.isAfter(activeDateRange.endInclusive))) {
                return@mapNotNull null
            }

            val normalizedFoods = entry.foodEntriesByColumn.values
                .flatten()
                .filter { it.name.isNotBlank() }
                .associateBy { normalizeFoodKey(it.name) }

            val hasFeelBadMarker = entry.markers.any { marker ->
                marker.type.equals("feel_bad", ignoreCase = true) ||
                    marker.type.equals("i feel bad", ignoreCase = true)
            }

            FoodSignalEntrySnapshot(
                date = parsedDate,
                foodsByNormalizedName = normalizedFoods,
                hasFeelBadMarker = hasFeelBadMarker,
            )
        }

    val feelBadDates = entrySnapshots
        .filter { it.hasFeelBadMarker }
        .map { it.date }
        .toSet()

    val groupedByFood = foodPatterns.groupBy { normalizeFoodKey(it.correlatedItem) }
    return groupedByFood
        .mapNotNull { (foodKey, groupedPatterns) ->
            if (foodKey.isBlank()) return@mapNotNull null

            val displayName = groupedPatterns
                .map { it.correlatedItem.trim() }
                .filter { it.isNotBlank() }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key
                ?: return@mapNotNull null

            val consumedDates = entrySnapshots
                .filter { snapshot -> snapshot.foodsByNormalizedName.containsKey(foodKey) }
                .map { it.date }
                .distinct()
                .sortedDescending()

            val supportDates = groupedPatterns
                .flatMap { pattern -> pattern.details }
                .mapNotNull { detail -> parsePotentialFoodDate(detail.date) }
                .filter { date ->
                    activeDateRange == null ||
                        (!date.isBefore(activeDateRange.start) && !date.isAfter(activeDateRange.endInclusive))
                }
                .distinct()
                .sortedDescending()
                .ifEmpty { consumedDates }

            val earliestSupportDate = supportDates.minOrNull()
            val contradictionDates = if (earliestSupportDate == null) {
                emptyList()
            } else {
                feelBadDates
                    .filter { date ->
                        !date.isBefore(earliestSupportDate) &&
                            consumedDates.none { consumedDate -> consumedDate == date }
                    }
                    .distinct()
                    .sortedDescending()
            }

            val emoji = entrySnapshots
                .firstNotNullOfOrNull { snapshot ->
                    snapshot.foodsByNormalizedName[foodKey]?.emoji?.takeIf { it.isNotBlank() }
                }
                ?: "🍽️"

            val status = when {
                IgnoredFoodManager.isFoodIgnored(context, displayName) -> PotentialFoodStatus.IGNORED
                supportDates.isEmpty() -> PotentialFoodStatus.NOT_POTENTIAL
                contradictionDates.isNotEmpty() -> PotentialFoodStatus.NOT_POTENTIAL
                else -> PotentialFoodStatus.POTENTIAL
            }

            PotentialFoodSignal(
                foodName = displayName,
                emoji = emoji,
                maxConfidence = groupedPatterns.maxOfOrNull { it.confidenceScore } ?: 0f,
                patternCount = groupedPatterns.size,
                supportDates = supportDates,
                contradictionDates = contradictionDates,
                status = status,
            )
        }
        .sortedWith(
            compareBy<PotentialFoodSignal> { signal ->
                when (signal.status) {
                    PotentialFoodStatus.POTENTIAL -> 0
                    PotentialFoodStatus.NOT_POTENTIAL -> 1
                    PotentialFoodStatus.IGNORED -> 2
                }
            }.thenByDescending { it.maxConfidence }
                .thenBy { it.foodName.lowercase(Locale.ROOT) },
        )
}

private fun normalizeFoodKey(raw: String): String = raw.trim().lowercase(Locale.ROOT)

private fun parsePotentialFoodDate(rawDate: String): LocalDate? {
    val formats = listOf("dd.MM.yyyy", "dd-MM-yyyy", "yyyy-MM-dd")
    formats.forEach { pattern ->
        runCatching {
            return LocalDate.parse(rawDate, DateTimeFormatter.ofPattern(pattern))
        }
    }
    return null
}

private fun potentialFoodTitle(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Mögliche Trigger-Lebensmittel"
        "Spanish" -> "Posibles alimentos desencadenantes"
        else -> "Possible Trigger Foods"
    }
}

private fun potentialFoodSummary(
    selectedLanguage: String,
    potentialCount: Int,
    notPotentialCount: Int,
    total: Int,
): String {
    return when (selectedLanguage) {
        "German" -> "$potentialCount aktiv, $notPotentialCount nicht mehr aktiv · Gesamt: $total"
        "Spanish" -> "$potentialCount activos, $notPotentialCount ya no activos · Total: $total"
        else -> "$potentialCount active, $notPotentialCount no longer active · Total: $total"
    }
}

private fun potentialFoodEmptyLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Derzeit wurden keine verdächtigen Lebensmittelmuster erkannt."
        "Spanish" -> "No se detectaron patrones sospechosos de alimentos por ahora."
        else -> "No suspicious food patterns detected right now."
    }
}

private fun potentialFoodConfidenceLabel(selectedLanguage: String, confidence: Float, patternCount: Int): String {
    val confidencePercent = (confidence * 100).toInt()
    return when (selectedLanguage) {
        "German" -> "Konfidenz $confidencePercent% · Muster: $patternCount"
        "Spanish" -> "Confianza $confidencePercent% · Patrones: $patternCount"
        else -> "Confidence $confidencePercent% · Patterns: $patternCount"
    }
}

private fun supportDatesLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Hinweis-Tage"
        "Spanish" -> "Días de señal"
        else -> "Supporting days"
    }
}

private fun contradictionDatesLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Gegenbeispiel-Tage (schlecht gefühlt ohne dieses Essen)"
        "Spanish" -> "Días de contradicción (malestar sin este alimento)"
        else -> "Contradiction days (felt bad without this food)"
    }
}

private fun focusInPatternsLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Im Musterbereich öffnen"
        "Spanish" -> "Abrir en detalles de patrones"
        else -> "Open in pattern details"
    }
}

private fun potentialStatusLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Potenziell"
        "Spanish" -> "Potencial"
        else -> "Potential"
    }
}

private fun notPotentialStatusLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Nicht mehr potenziell"
        "Spanish" -> "Ya no potencial"
        else -> "No longer potential"
    }
}

private fun ignoredStatusLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Ignoriert"
        "Spanish" -> "Ignorado"
        else -> "Ignored"
    }
}

private fun expectedResultsTitle(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Schnellcheck (Testdaten)"
        "Spanish" -> "Verificación rápida (datos de prueba)"
        else -> "Quick validation (test data)"
    }
}

private fun expectedDonutResultLine(selectedLanguage: String, status: PotentialFoodStatus?): String {
    val currentStatus = status?.let { potentialFoodStatusLabel(it, selectedLanguage) }
        ?: notDetectedYetLabel(selectedLanguage)
    return when (selectedLanguage) {
        "German" -> "🍩 Donut: erwartet ${notPotentialStatusLabel(selectedLanguage)} · aktuell: $currentStatus"
        "Spanish" -> "🍩 Donut: esperado ${notPotentialStatusLabel(selectedLanguage)} · actual: $currentStatus"
        else -> "🍩 Donut: expected ${notPotentialStatusLabel(selectedLanguage)} · current: $currentStatus"
    }
}

private fun expectedBlueberriesResultLine(selectedLanguage: String, status: PotentialFoodStatus?): String {
    val currentStatus = status?.let { potentialFoodStatusLabel(it, selectedLanguage) }
        ?: notDetectedYetLabel(selectedLanguage)
    return when (selectedLanguage) {
        "German" -> "🫐 Blueberries: erwartet ${potentialStatusLabel(selectedLanguage)} · aktuell: $currentStatus"
        "Spanish" -> "🫐 Blueberries: esperado ${potentialStatusLabel(selectedLanguage)} · actual: $currentStatus"
        else -> "🫐 Blueberries: expected ${potentialStatusLabel(selectedLanguage)} · current: $currentStatus"
    }
}

private fun potentialFoodStatusLabel(status: PotentialFoodStatus, selectedLanguage: String): String {
    return when (status) {
        PotentialFoodStatus.POTENTIAL -> potentialStatusLabel(selectedLanguage)
        PotentialFoodStatus.NOT_POTENTIAL -> notPotentialStatusLabel(selectedLanguage)
        PotentialFoodStatus.IGNORED -> ignoredStatusLabel(selectedLanguage)
    }
}

private fun notDetectedYetLabel(selectedLanguage: String): String {
    return when (selectedLanguage) {
        "German" -> "Noch nicht erkannt"
        "Spanish" -> "Aún no detectado"
        else -> "Not detected yet"
    }
}

private fun guidanceText(selectedLanguage: String, potentialCount: Int): String {
    return when {
        potentialCount <= 0 -> when (selectedLanguage) {
            "German" -> "Aktuell sind keine aktiven Verdachtslebensmittel übrig."
            "Spanish" -> "Actualmente no quedan alimentos sospechosos activos."
            else -> "There are currently no active suspected foods left."
        }

        selectedLanguage == "German" -> "Nächster Schritt: vermeiden Sie testweise eines der aktiven Lebensmittel und markieren Sie weiter, wie Sie sich fühlen."
        selectedLanguage == "Spanish" -> "Siguiente paso: evita uno de los alimentos activos y sigue marcando cómo te sientes."
        else -> "Next step: try avoiding one active food and continue logging how you feel."
    }
}
