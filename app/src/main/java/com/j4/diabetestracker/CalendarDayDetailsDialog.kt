package com.j4.diabetestracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal data class CalendarDayDetails(
    val date: LocalDate,
    val markerBreakdown: List<Pair<String, Int>>,
    val triggerBreakdown: List<Pair<String, Int>>,
    val patternBreakdown: List<Pair<String, Int>>,
    val helperSummaryLines: List<String>
)

@Composable
internal fun CalendarDayDetailsDialog(
    dayDetails: CalendarDayDetails,
    locale: Locale,
    onDismiss: () -> Unit,
    onNavigateToTableDay: () -> Unit
) {
    val dialogDateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", locale)
    var showHelperSummary by remember(dayDetails.date) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        R.string.calendar_day_details_title,
                        dayDetails.date.format(dialogDateFormatter)
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { showHelperSummary = !showHelperSummary }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.calendar_day_details_info)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showHelperSummary) {
                    CalendarDayDetailHelperSummary(dayDetails = dayDetails)
                }
                CalendarDayDetailSection(
                    title = stringResource(R.string.calendar_day_details_markers),
                    entries = dayDetails.markerBreakdown
                )
                CalendarDayDetailSection(
                    title = stringResource(R.string.calendar_day_details_triggers),
                    entries = dayDetails.triggerBreakdown
                )
                CalendarDayDetailSection(
                    title = stringResource(R.string.calendar_day_details_patterns),
                    entries = dayDetails.patternBreakdown
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onNavigateToTableDay) {
                Text(stringResource(R.string.calendar_view_table_day))
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
private fun CalendarDayDetailHelperSummary(
    dayDetails: CalendarDayDetails
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.calendar_day_details_helper_title),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (dayDetails.helperSummaryLines.isEmpty()) {
                Text(
                    text = stringResource(R.string.calendar_day_details_helper_empty),
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                dayDetails.helperSummaryLines.forEach { helperLine ->
                    Text(
                        text = "• $helperLine",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayDetailSection(
    title: String,
    entries: List<Pair<String, Int>>
) {
    if (entries.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        entries.forEach { (label, count) ->
            Text(
                text = "• $label ($count)",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
    }
}
