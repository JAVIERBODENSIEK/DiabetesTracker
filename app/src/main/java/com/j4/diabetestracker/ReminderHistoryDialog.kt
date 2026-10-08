package com.j4.diabetestracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ReminderHistoryDialog(
    reminderId: String,
    history: List<ReminderConfirmation>,
    selectedLanguage: String,
    onUpdateConfirmation: (String, ConfirmationStatus, String) -> Unit,
    onNavigateToNotifications: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val reminderHistory = history.filter { it.reminderId == reminderId }
    val reminderName = reminderHistory.firstOrNull()?.let {
        "${it.reminderEmoji} ${it.reminderName}"
    } ?: ""

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (reminderHistory.isEmpty()) {
                    EmptyHistoryState(
                        selectedLanguage = selectedLanguage,
                        onDismiss = onDismiss
                    )
                } else {
                    // Header
                    DialogHeader(
                        reminderName = reminderName,
                        onDismiss = onDismiss
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Streak summary
                    StreakSummaryCard(
                        history = reminderHistory,
                        selectedLanguage = selectedLanguage,
                        onNavigateToNotifications = {
                            onNavigateToNotifications(reminderId)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // History entries
                    HistoryEntriesList(
                        history = reminderHistory,
                        selectedLanguage = selectedLanguage,
                        onUpdateConfirmation = onUpdateConfirmation
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(
    selectedLanguage: String,
    onDismiss: () -> Unit
) {
    val title = when (selectedLanguage) {
        "German" -> "Noch kein Verlauf"
        "Spanish" -> "Aún sin historial"
        else -> "No history yet"
    }
    val message = when (selectedLanguage) {
        "German" -> "Du hast noch keinen Bestätigungsverlauf für diese Erinnerung."
        "Spanish" -> "Aún no tienes historial de confirmación para este recordatorio."
        else -> "You still have no confirmation history for this reminder."
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.info_icon_20260321_212053),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = 2.35f,
                                scaleY = 2.35f
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DialogHeader(
    reminderName: String,
    onDismiss: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (reminderName.isNotBlank()) {
            Text(
                text = reminderName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StreakSummaryCard(
    history: List<ReminderConfirmation>,
    selectedLanguage: String,
    onNavigateToNotifications: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // Calculate current streak
    val currentStreakType = history.firstOrNull()?.status
    val currentStreak = if (currentStreakType != null && currentStreakType != ConfirmationStatus.PENDING) {
        history.takeWhile {
            it.status == currentStreakType ||
            (currentStreakType == ConfirmationStatus.YES && it.status == ConfirmationStatus.COMMENTED) ||
            (currentStreakType == ConfirmationStatus.COMMENTED && it.status == ConfirmationStatus.YES)
        }.size
    } else 0

    val isYesStreak = currentStreakType == ConfirmationStatus.YES ||
            currentStreakType == ConfirmationStatus.COMMENTED

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        // ── Current Streak Card ──
        if (currentStreak > 0 && currentStreakType != null) {
            val streakColor = if (isYesStreak) primaryColor else errorColor
            val streakBg = if (isYesStreak) primaryColor.copy(alpha = 0.12f)
                else errorColor.copy(alpha = 0.12f)

            val isActionable = !isYesStreak

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = streakBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isActionable) Modifier.clickable { onNavigateToNotifications() }
                        else Modifier
                    )
            ) {
                Column {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Big streak number
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(streakColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$currentStreak",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = streakColor
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isYesStreak) {
                                if (currentStreak == 1) {
                                    when (selectedLanguage) {
                                        "German" -> "Ja-Bestätigung!"
                                        "Spanish" -> "Confirmacion de Si!"
                                        else -> "Yes confirmation!"
                                    }
                                } else {
                                    when (selectedLanguage) {
                                        "German" -> "Ja-Serie in Folge!"
                                        "Spanish" -> "Racha de Si seguidos!"
                                        else -> "Yes streak in a row!"
                                    }
                                }
                            } else {
                                if (currentStreak == 1) {
                                    when (selectedLanguage) {
                                        "German" -> "Nein-Bestätigung"
                                        "Spanish" -> "Confirmacion de No"
                                        else -> "No confirmation"
                                    }
                                } else {
                                    when (selectedLanguage) {
                                        "German" -> "Nein-Serie in Folge"
                                        "Spanish" -> "Racha de No seguidos"
                                        else -> "No streak in a row"
                                    }
                                }
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = streakColor
                        )
                        Text(
                            text = if (isYesStreak) {
                                when (selectedLanguage) {
                                    "German" -> "Weiter so! Du bist auf dem richtigen Weg."
                                    "Spanish" -> "Sigue asi! Vas por buen camino."
                                    else -> "Keep it up! You're on the right track."
                                }
                            } else {
                                if (currentStreak == 1) {
                                    when (selectedLanguage) {
                                        "German" -> "Du hast 1× nicht bestätigt."
                                        "Spanish" -> "No has confirmado 1 vez."
                                        else -> "You haven't confirmed 1 time."
                                    }
                                } else {
                                    when (selectedLanguage) {
                                        "German" -> "Du hast ${currentStreak}× in Folge nicht bestätigt."
                                        "Spanish" -> "No has confirmado $currentStreak veces seguidas."
                                        else -> "You haven't confirmed for $currentStreak times in a row."
                                    }
                                }
                            },
                            fontSize = 10.sp,
                            color = onSurfaceVariant.copy(alpha = 0.85f),
                            lineHeight = 13.sp
                        )
                    }
                    Text(
                        text = if (isYesStreak) "🔥" else "⚠️",
                        fontSize = 24.sp
                    )
                }
                if (isActionable) {
                    HorizontalDivider(
                        color = streakColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Tippen, um jetzt zu bestätigen"
                                "Spanish" -> "Toca para confirmar ahora"
                                else -> "Tap to confirm now"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = streakColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "›",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = streakColor.copy(alpha = 0.8f)
                        )
                    }
                }
                }
            }
        }

        // ── Completion Overview Card ──
        CompletionOverviewCard(
            history = history,
            selectedLanguage = selectedLanguage,
            primaryColor = primaryColor,
            errorColor = errorColor,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant
        )
    }
}

@Composable
private fun CompletionOverviewCard(
    history: List<ReminderConfirmation>,
    selectedLanguage: String,
    primaryColor: androidx.compose.ui.graphics.Color,
    errorColor: androidx.compose.ui.graphics.Color,
    surfaceVariant: androidx.compose.ui.graphics.Color,
    onSurfaceVariant: androidx.compose.ui.graphics.Color,
    onSurface: androidx.compose.ui.graphics.Color = onSurfaceVariant
) {
    // Time range options
    val rangeOptions = listOf("7d", "30d", "3m", "all")
    var selectedRange by remember { mutableStateOf("all") }
    var showCustomPicker by remember { mutableStateOf(false) }
    var showDeleteBadge by remember { mutableStateOf(false) }
    var customAmount by remember { mutableStateOf("1") }
    var customUnit by remember { mutableStateOf("weeks") }
    var customLabel by remember { mutableStateOf("") }
    var customCutoffMs by remember { mutableStateOf(0L) }

    val unitLabels = mapOf(
        "days" to when (selectedLanguage) {
            "German" -> "Tage"; "Spanish" -> "días"; else -> "days"
        },
        "weeks" to when (selectedLanguage) {
            "German" -> "Wochen"; "Spanish" -> "semanas"; else -> "weeks"
        },
        "months" to when (selectedLanguage) {
            "German" -> "Monate"; "Spanish" -> "meses"; else -> "months"
        },
        "years" to when (selectedLanguage) {
            "German" -> "Jahre"; "Spanish" -> "años"; else -> "years"
        }
    )

    val rangeLabel = { key: String ->
        when (key) {
            "7d" -> when (selectedLanguage) {
                "German" -> "7 Tage"
                "Spanish" -> "7 días"
                else -> "7 days"
            }
            "30d" -> when (selectedLanguage) {
                "German" -> "30 Tage"
                "Spanish" -> "30 días"
                else -> "30 days"
            }
            "3m" -> when (selectedLanguage) {
                "German" -> "3 Monate"
                "Spanish" -> "3 meses"
                else -> "3 months"
            }
            "custom" -> customLabel
            else -> when (selectedLanguage) {
                "German" -> "Gesamt"
                "Spanish" -> "Todo"
                else -> "All time"
            }
        }
    }

    // Filter history by selected range
    val now = System.currentTimeMillis()
    val cutoffMs = when (selectedRange) {
        "7d" -> now - 7L * 24 * 60 * 60 * 1000
        "30d" -> now - 30L * 24 * 60 * 60 * 1000
        "3m" -> now - 90L * 24 * 60 * 60 * 1000
        "custom" -> customCutoffMs
        else -> 0L
    }
    val filtered = if (selectedRange == "all") history
        else history.filter { it.timestamp >= cutoffMs }

    // Calculate stats from filtered data
    val totalYes = filtered.count {
        it.status == ConfirmationStatus.YES || it.status == ConfirmationStatus.COMMENTED
    }
    val totalNo = filtered.count { it.status == ConfirmationStatus.NO }
    val total = totalYes + totalNo
    val yesPercent = if (total > 0) (totalYes * 100) / total else 0

    var bestYesStreak = 0
    var currentYesRun = 0
    for (entry in filtered.reversed()) {
        if (entry.status == ConfirmationStatus.YES || entry.status == ConfirmationStatus.COMMENTED) {
            currentYesRun++
            if (currentYesRun > bestYesStreak) bestYesStreak = currentYesRun
        } else {
            currentYesRun = 0
        }
    }

    val firstDate = filtered.lastOrNull()?.date ?: ""

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = surfaceVariant.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header row with title
            Text(
                text = when (selectedLanguage) {
                    "German" -> "Deine Übersicht"
                    "Spanish" -> "Tu resumen"
                    else -> "Your overview"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = onSurfaceVariant.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Time range filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val allChips = if (customLabel.isNotBlank())
                    rangeOptions + "custom" else rangeOptions
                allChips.forEach { key ->
                    val isSelected = selectedRange == key
                    if (key == "custom") {
                        // Custom chip with long-press delete
                        @OptIn(ExperimentalFoundationApi::class)
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) primaryColor.copy(alpha = 0.2f)
                                    else onSurfaceVariant.copy(alpha = 0.12f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(
                                    1.dp, primaryColor.copy(alpha = 0.5f)
                                ) else null,
                                modifier = Modifier.combinedClickable(
                                    onClick = {
                                        if (showDeleteBadge) showDeleteBadge = false
                                        else selectedRange = key
                                    },
                                    onLongClick = { showDeleteBadge = !showDeleteBadge }
                                )
                            ) {
                                Text(
                                    text = rangeLabel(key),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) primaryColor
                                        else onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            if (showDeleteBadge) {
                                Surface(
                                    onClick = {
                                        customLabel = ""
                                        customCutoffMs = 0L
                                        showDeleteBadge = false
                                        if (selectedRange == "custom") selectedRange = "all"
                                    },
                                    shape = CircleShape,
                                    color = errorColor,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = "✕",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                    Surface(
                        onClick = { selectedRange = key },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) primaryColor.copy(alpha = 0.2f)
                            else onSurfaceVariant.copy(alpha = 0.12f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(
                            1.dp, primaryColor.copy(alpha = 0.5f)
                        ) else null
                    ) {
                        Text(
                            text = rangeLabel(key),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) primaryColor
                                else onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    }
                }
                // "+" chip for custom range
                Surface(
                    onClick = { showCustomPicker = !showCustomPicker },
                    shape = RoundedCornerShape(8.dp),
                    color = if (showCustomPicker) primaryColor.copy(alpha = 0.2f)
                        else onSurfaceVariant.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "+",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (showCustomPicker) primaryColor
                            else onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Custom range picker
            AnimatedVisibility(visible = showCustomPicker) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = onSurfaceVariant.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Number input
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.width(52.dp)
                            ) {
                                BasicTextField(
                                    value = customAmount,
                                    onValueChange = { v ->
                                        if (v.length <= 3 && v.all { it.isDigit() }) customAmount = v
                                    },
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                        .fillMaxWidth()
                                )
                            }
                            // Unit selector chips
                            listOf("days", "weeks", "months", "years").forEach { unit ->
                                val isUnitSelected = customUnit == unit
                                Surface(
                                    onClick = { customUnit = unit },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isUnitSelected) primaryColor.copy(alpha = 0.2f)
                                        else onSurfaceVariant.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = unitLabels[unit] ?: unit,
                                        fontSize = 9.sp,
                                        fontWeight = if (isUnitSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isUnitSelected) primaryColor
                                            else onSurfaceVariant.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Apply button
                        Surface(
                            onClick = {
                                val amt = customAmount.toIntOrNull() ?: 1
                                val ms = when (customUnit) {
                                    "days" -> amt.toLong() * 24 * 60 * 60 * 1000
                                    "weeks" -> amt.toLong() * 7 * 24 * 60 * 60 * 1000
                                    "months" -> amt.toLong() * 30 * 24 * 60 * 60 * 1000
                                    "years" -> amt.toLong() * 365 * 24 * 60 * 60 * 1000
                                    else -> 0L
                                }
                                customCutoffMs = System.currentTimeMillis() - ms
                                customLabel = "$amt ${unitLabels[customUnit] ?: customUnit}"
                                selectedRange = "custom"
                                showCustomPicker = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = primaryColor.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Anwenden"
                                    "Spanish" -> "Aplicar"
                                    else -> "Apply"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (total == 0) {
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Keine Daten in diesem Zeitraum"
                        "Spanish" -> "Sin datos en este período"
                        else -> "No data in this period"
                    },
                    fontSize = 11.sp,
                    color = onSurfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {

            // Completion rate bar
            val rateColor = if (yesPercent >= 70) primaryColor
                else if (yesPercent >= 40) MaterialTheme.colorScheme.tertiary
                else errorColor
            val rateLabel = when {
                yesPercent >= 80 -> when (selectedLanguage) {
                    "German" -> "Ausgezeichnet"
                    "Spanish" -> "Excelente"
                    else -> "Excellent"
                }
                yesPercent >= 60 -> when (selectedLanguage) {
                    "German" -> "Gut"
                    "Spanish" -> "Bien"
                    else -> "Good"
                }
                yesPercent >= 40 -> when (selectedLanguage) {
                    "German" -> "Durchschnittlich"
                    "Spanish" -> "Regular"
                    else -> "Average"
                }
                else -> when (selectedLanguage) {
                    "German" -> "Verbesserungsbedarf"
                    "Spanish" -> "Necesita mejorar"
                    else -> "Needs improvement"
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Erfolgsquote"
                        "Spanish" -> "Tasa de éxito"
                        else -> "Completion rate"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$yesPercent%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = rateColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = rateLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = rateColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(onSurfaceVariant.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (yesPercent / 100f).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(rateColor)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Yes / No breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Yes card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = primaryColor.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "$totalYes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Erledigt"
                                    "Spanish" -> "Completado"
                                    else -> "Completed"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = primaryColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                // No card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = errorColor.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(errorColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✗", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = errorColor)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "$totalNo",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = errorColor
                            )
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Verpasst"
                                    "Spanish" -> "Perdido"
                                    else -> "Missed"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = errorColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = onSurfaceVariant.copy(alpha = 0.18f))
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom row: Total + Best streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Insgesamt"
                            "Spanish" -> "En total"
                            else -> "Total responses"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$total",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                // Best streak
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🏆",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Bestleistung"
                            "Spanish" -> "Mejor racha"
                            else -> "Personal best"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$bestYesStreak",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            // Tracking since
            if (firstDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Erfasst seit $firstDate"
                        "Spanish" -> "Registrado desde $firstDate"
                        else -> "Tracking since $firstDate"
                    },
                    fontSize = 9.sp,
                    color = onSurface.copy(alpha = 0.5f)
                )
            }

            } // end if total > 0
        }
    }
}

@Composable
private fun HistoryEntriesList(
    history: List<ReminderConfirmation>,
    selectedLanguage: String,
    onUpdateConfirmation: (String, ConfirmationStatus, String) -> Unit
) {
    // Group by date
    val grouped = history.groupBy { it.date }

    Text(
        text = when (selectedLanguage) {
            "German" -> "Einträge"
            "Spanish" -> "Entradas"
            else -> "Entries"
        },
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    )
    Spacer(modifier = Modifier.height(8.dp))

    grouped.forEach { (date, entries) ->
        // Date header
        Text(
            text = date,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(vertical = 4.dp)
        )

        entries.forEach { entry ->
            EditableHistoryEntry(
                entry = entry,
                selectedLanguage = selectedLanguage,
                onUpdate = { newStatus, newComment ->
                    onUpdateConfirmation(entry.id, newStatus, newComment)
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))
    }

    if (history.isEmpty()) {
        Text(
            text = when (selectedLanguage) {
                "German" -> "Noch keine Einträge"
                "Spanish" -> "Sin entradas aún"
                else -> "No entries yet"
            },
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}

@Composable
private fun EditableHistoryEntry(
    entry: ReminderConfirmation,
    selectedLanguage: String,
    onUpdate: (ConfirmationStatus, String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editStatus by remember(entry) { mutableStateOf(entry.status) }
    var editComment by remember(entry) { mutableStateOf(entry.comment) }

    val statusColor = when (entry.status) {
        ConfirmationStatus.YES -> MaterialTheme.colorScheme.primary
        ConfirmationStatus.NO -> MaterialTheme.colorScheme.error
        ConfirmationStatus.COMMENTED -> MaterialTheme.colorScheme.primary
        ConfirmationStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = statusColor.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (entry.status) {
                            ConfirmationStatus.YES -> "✓"
                            ConfirmationStatus.NO -> "✗"
                            ConfirmationStatus.COMMENTED -> "✓"
                            ConfirmationStatus.PENDING -> "…"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.scheduledTime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (entry.comment.isNotBlank() && !isEditing) {
                        Text(
                            text = entry.comment,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // Edit button
                Surface(
                    onClick = {
                        if (isEditing) {
                            // Save
                            onUpdate(editStatus, editComment)
                            isEditing = false
                        } else {
                            editStatus = entry.status
                            editComment = entry.comment
                                isEditing = true
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (isEditing) {
                            Text(
                                text = "✓",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }

            // Edit mode
            AnimatedVisibility(
                visible = isEditing,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    // Yes/No toggle pills
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val yesSelected = editStatus == ConfirmationStatus.YES ||
                                editStatus == ConfirmationStatus.COMMENTED
                        val noSelected = editStatus == ConfirmationStatus.NO

                        Surface(
                            onClick = { editStatus = ConfirmationStatus.YES },
                            shape = RoundedCornerShape(8.dp),
                            color = if (yesSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "✓ Ja"
                                    "Spanish" -> "✓ Sí"
                                    else -> "✓ Yes"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (yesSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (yesSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                            )
                        }
                        Surface(
                            onClick = { editStatus = ConfirmationStatus.NO },
                            shape = RoundedCornerShape(8.dp),
                            color = if (noSelected) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "✗ No",
                                fontSize = 12.sp,
                                fontWeight = if (noSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (noSelected) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Comment field
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BasicTextField(
                            value = editComment,
                            onValueChange = { editComment = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 36.dp)
                                .padding(8.dp),
                            textStyle = TextStyle(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (editComment.isEmpty()) {
                                        Text(
                                            text = when (selectedLanguage) {
                                                "German" -> "Kommentar…"
                                                "Spanish" -> "Comentario…"
                                                else -> "Comment…"
                                            },
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Cancel button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                editStatus = entry.status
                                editComment = entry.comment
                                isEditing = false
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Abbrechen"
                                    "Spanish" -> "Cancelar"
                                    else -> "Cancel"
                                },
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
