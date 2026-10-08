package com.j4.diabetestracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun friendlyTimeAgo(
    date: String,
    time: String,
    selectedLanguage: String
): String {
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val entryDate = sdf.parse("$date $time") ?: return "$time · $date"
        val now = System.currentTimeMillis()
        val diffMs = now - entryDate.time
        val diffMin = (diffMs / 60000).toInt()
        val diffHours = diffMin / 60
        val diffDays = diffHours / 24

        // Today / Yesterday check
        val cal = Calendar.getInstance()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

        return when {
            diffMin < 1 -> when (selectedLanguage) {
                "German" -> "Gerade eben"
                "Spanish" -> "Justo ahora"
                else -> "Just now"
            }
            diffMin < 60 -> when (selectedLanguage) {
                "German" -> if (diffMin == 1) "Vor 1 Minute" else "Vor $diffMin Minuten"
                "Spanish" -> if (diffMin == 1) "Hace 1 minuto" else "Hace $diffMin minutos"
                else -> if (diffMin == 1) "1 minute ago" else "$diffMin minutes ago"
            }
            diffHours < 24 && date == todayStr -> when (selectedLanguage) {
                "German" -> if (diffHours == 1) "Vor 1 Stunde" else "Vor $diffHours Stunden"
                "Spanish" -> if (diffHours == 1) "Hace 1 hora" else "Hace $diffHours horas"
                else -> if (diffHours == 1) "1 hour ago" else "$diffHours hours ago"
            }
            date == todayStr -> when (selectedLanguage) {
                "German" -> "Heute um $time"
                "Spanish" -> "Hoy a las $time"
                else -> "Today at $time"
            }
            date == yesterdayStr -> when (selectedLanguage) {
                "German" -> "Gestern um $time"
                "Spanish" -> "Ayer a las $time"
                else -> "Yesterday at $time"
            }
            diffDays < 7 -> when (selectedLanguage) {
                "German" -> "Vor $diffDays Tagen"
                "Spanish" -> "Hace $diffDays días"
                else -> "$diffDays days ago"
            }
            else -> "$time · $date"
        }
    } catch (_: Exception) {
        return "$time · $date"
    }
}

private fun friendlyTimestampAgo(
    timestamp: Long,
    selectedLanguage: String
): String {
    val diffMs = System.currentTimeMillis() - timestamp
    val diffMin = (diffMs / 60000).toInt()
    val diffHours = diffMin / 60
    val diffDays = diffHours / 24

    return when {
        diffMin < 1 -> when (selectedLanguage) {
            "German" -> "Gerade eben"
            "Spanish" -> "Justo ahora"
            else -> "Just now"
        }
        diffMin < 60 -> when (selectedLanguage) {
            "German" -> if (diffMin == 1) "Vor 1 Minute" else "Vor $diffMin Minuten"
            "Spanish" -> if (diffMin == 1) "Hace 1 minuto" else "Hace $diffMin minutos"
            else -> if (diffMin == 1) "1 minute ago" else "$diffMin minutes ago"
        }
        diffHours < 24 -> when (selectedLanguage) {
            "German" -> if (diffHours == 1) "Vor 1 Stunde" else "Vor $diffHours Stunden"
            "Spanish" -> if (diffHours == 1) "Hace 1 hora" else "Hace $diffHours horas"
            else -> if (diffHours == 1) "1 hour ago" else "$diffHours hours ago"
        }
        diffDays < 7 -> when (selectedLanguage) {
            "German" -> if (diffDays == 1) "Vor 1 Tag" else "Vor $diffDays Tagen"
            "Spanish" -> if (diffDays == 1) "Hace 1 día" else "Hace $diffDays días"
            else -> if (diffDays == 1) "1 day ago" else "$diffDays days ago"
        }
        else -> {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

@Composable
fun PendingConfirmationCard(
    confirmation: ReminderConfirmation,
    selectedLanguage: String,
    onRespond: (ConfirmationStatus, String) -> Unit
) {
    var pendingStatus by remember { mutableStateOf<ConfirmationStatus?>(null) }
    var showCommentPrompt by remember { mutableStateOf(false) }
    var showCommentDialog by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val friendlyTime = remember(confirmation.date, confirmation.scheduledTime) {
        friendlyTimeAgo(confirmation.date, confirmation.scheduledTime, selectedLanguage)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = primaryColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.42f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = confirmation.reminderEmoji,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = confirmation.reminderName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = friendlyTime,
                        fontSize = 10.sp,
                        color = onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (!showCommentPrompt) {
                PendingDecisionButtons(
                    selectedLanguage = selectedLanguage,
                    onSelectYes = {
                        pendingStatus = ConfirmationStatus.YES
                        showCommentPrompt = true
                    },
                    onSelectNo = {
                        pendingStatus = ConfirmationStatus.NO
                        showCommentPrompt = true
                    }
                )
            } else {
                AddCommentPromptCard(
                    selectedLanguage = selectedLanguage,
                    onBack = {
                        showCommentPrompt = false
                        pendingStatus = null
                    },
                    onNoComment = {
                        pendingStatus?.let { status -> onRespond(status, "") }
                    },
                    onYesComment = { showCommentDialog = true }
                )
            }
        }
    }

    if (showCommentDialog) {
        CommentDialog(
            selectedLanguage = selectedLanguage,
            reminderName = "${confirmation.reminderEmoji} ${confirmation.reminderName}",
            onDismiss = {
                showCommentDialog = false
                // Dismiss comment = submit without comment
                onRespond(pendingStatus!!, "")
            },
            onSubmit = { comment ->
                onRespond(pendingStatus!!, comment)
                showCommentDialog = false
            }
        )
    }
}

@Composable
private fun PendingDecisionButtons(
    selectedLanguage: String,
    onSelectYes: () -> Unit,
    onSelectNo: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            onClick = onSelectYes,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Ja"
                        "Spanish" -> "Sí"
                        else -> "Yes"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Surface(
            onClick = onSelectNo,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.18f),
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.9f)),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "No",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddCommentPromptCard(
    selectedLanguage: String,
    onBack: () -> Unit,
    onNoComment: () -> Unit,
    onYesComment: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.75f))
                ) {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Zurück"
                            "Spanish" -> "Atrás"
                            else -> "Back"
                        },
                        fontSize = 11.sp,
                        color = onSurfaceVariant
                    )
                }

                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Möchtest du einen Kommentar hinzufügen?"
                        "Spanish" -> "¿Quieres añadir un comentario?"
                        else -> "Would you like to add a comment?"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onNoComment,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Nein"
                            "Spanish" -> "No"
                            else -> "No"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onYesComment,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Ja"
                            "Spanish" -> "Sí"
                            else -> "Yes"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

}

@Composable
fun CommentDialog(
    selectedLanguage: String,
    reminderName: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var comment by remember { mutableStateOf("") }
    val primaryColor = MaterialTheme.colorScheme.primary

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = reminderName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp)
                            .padding(12.dp),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        decorationBox = { innerTextField ->
                            Box {
                                if (comment.isEmpty()) {
                                    Text(
                                        text = when (selectedLanguage) {
                                            "German" -> "Kommentar hinzufügen…"
                                            "Spanish" -> "Añadir comentario…"
                                            else -> "Add a comment…"
                                        },
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            when (selectedLanguage) {
                                "German" -> "Abbrechen"
                                "Spanish" -> "Cancelar"
                                else -> "Cancel"
                            }
                        )
                    }
                    Button(
                        onClick = { onSubmit(comment) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        enabled = comment.isNotBlank()
                    ) {
                        Text(
                            when (selectedLanguage) {
                                "German" -> "Speichern"
                                "Spanish" -> "Guardar"
                                else -> "Save"
                            }
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun ConfirmationHistorySection(
    history: List<ReminderConfirmation>,
    selectedLanguage: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val displayList = if (expanded) history else history.take(5)
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (selectedLanguage) {
                    "German" -> "Verlauf"
                    "Spanish" -> "Historial"
                    else -> "History"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (history.size > 5) {
                Text(
                    text = "${history.size}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        displayList.forEach { entry ->
            HistoryEntryRow(entry, dateFormat, selectedLanguage)
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (history.isEmpty()) {
            Text(
                text = when (selectedLanguage) {
                    "German" -> "Noch keine Einträge"
                    "Spanish" -> "Sin entradas aún"
                    else -> "No entries yet"
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun HistoryEntryRow(
    entry: ReminderConfirmation,
    dateFormat: SimpleDateFormat,
    selectedLanguage: String
) {
    val statusColor = when (entry.status) {
        ConfirmationStatus.YES -> MaterialTheme.colorScheme.primary
        ConfirmationStatus.NO -> MaterialTheme.colorScheme.error
        ConfirmationStatus.COMMENTED -> MaterialTheme.colorScheme.primary
        ConfirmationStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusText = when (entry.status) {
        ConfirmationStatus.YES -> "✓"
        ConfirmationStatus.NO -> "✗"
        ConfirmationStatus.COMMENTED -> "✓"
        ConfirmationStatus.PENDING -> "…"
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = statusColor.copy(alpha = 0.06f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = statusText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(
                        text = "${entry.reminderEmoji} ${entry.reminderName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entry.scheduledTime,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                if (entry.comment.isNotBlank()) {
                    Text(
                        text = entry.comment,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = entry.date,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun TappableHistorySection(
    history: List<ReminderConfirmation>,
    selectedLanguage: String,
    onItemClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayList = if (expanded) history else history.take(5)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (selectedLanguage) {
                    "German" -> "Letzte Aktivität"
                    "Spanish" -> "Actividad reciente"
                    else -> "Recent activity"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (history.size > 5) {
                Text(
                    text = "${history.size}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        displayList.forEach { entry ->
            val statusColor = when (entry.status) {
                ConfirmationStatus.YES -> MaterialTheme.colorScheme.primary
                ConfirmationStatus.NO -> MaterialTheme.colorScheme.error
                ConfirmationStatus.COMMENTED -> MaterialTheme.colorScheme.primary
                ConfirmationStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            val statusText = when (entry.status) {
                ConfirmationStatus.YES -> "✓"
                ConfirmationStatus.NO -> "✗"
                ConfirmationStatus.COMMENTED -> "✓"
                ConfirmationStatus.PENDING -> "…"
            }
            val entryFriendlyTime = friendlyTimeAgo(entry.date, entry.scheduledTime, selectedLanguage)

            Surface(
                onClick = { onItemClick(entry.reminderId) },
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.06f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${entry.reminderEmoji} ${entry.reminderName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (entry.comment.isNotBlank()) {
                            Text(
                                text = entry.comment,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entryFriendlyTime,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "›",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
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
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun BellPopupContent(
    pendingConfirmations: List<ReminderConfirmation>,
    history: List<ReminderConfirmation>,
    alerts: List<ReminderConfirmationStorage.ConfirmationAlert> = emptyList(),
    selectedLanguage: String,
    preferredSection: String? = null,
    focusReminderId: String? = null,
    onRespond: (String, ConfirmationStatus, String) -> Unit,
    onDismissAlert: (String) -> Unit = {},
    onOpenSmartAlertsSettings: (String) -> Unit = {},
    onHistoryItemClick: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val totalNotifications = pendingConfirmations.size + alerts.size
    val prioritizedAlerts = remember(alerts, focusReminderId) {
        if (focusReminderId.isNullOrBlank()) alerts
        else alerts.sortedByDescending { it.reminderId == focusReminderId }
    }
    val prioritizedPendingConfirmations = remember(pendingConfirmations, focusReminderId) {
        if (focusReminderId.isNullOrBlank()) pendingConfirmations
        else pendingConfirmations.sortedByDescending { it.reminderId == focusReminderId }
    }
    val shouldExpandAlertsByDefault = preferredSection == "alerts" && alerts.isNotEmpty()
    val shouldExpandPendingByDefault = when (preferredSection) {
        "pending" -> pendingConfirmations.isNotEmpty()
        "alerts" -> alerts.isEmpty() && pendingConfirmations.isNotEmpty()
        else -> false
    }

    var alertsExpanded by remember(preferredSection, alerts) {
        mutableStateOf(shouldExpandAlertsByDefault)
    }
    var pendingExpanded by remember(preferredSection, pendingConfirmations, alerts) {
        mutableStateOf(shouldExpandPendingByDefault)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .widthIn(max = 340.dp)
            .heightIn(max = 480.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header — Global Notifications
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Benachrichtigungen"
                        "Spanish" -> "Notificaciones"
                        else -> "Notifications"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (totalNotifications > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = "$totalNotifications",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Collapsible Smart Alerts section (show first — most urgent)
            if (alerts.isNotEmpty()) {
                // Alerts header with counter badge
                Surface(
                    onClick = { alertsExpanded = !alertsExpanded },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f),
                    border = BorderStroke(
                        1.2.dp,
                        if (alertsExpanded) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Warnungen"
                                    "Spanish" -> "Alertas"
                                    else -> "Alerts"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Red counter badge
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onError.copy(alpha = 0.75f))
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = "${alerts.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = if (alertsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Expandable alerts
                if (alertsExpanded) {
                    Spacer(modifier = Modifier.height(6.dp))
                    prioritizedAlerts.forEach { alert ->
                        AlertNotificationCard(
                            alert = alert,
                            selectedLanguage = selectedLanguage,
                            onDismiss = { onDismissAlert(alert.id) },
                            onOpenSmartAlertsSettings = { onOpenSmartAlertsSettings(alert.reminderId) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Collapsible Pending confirmations
            if (pendingConfirmations.isNotEmpty()) {
                // Pending header with counter badge
                Surface(
                    onClick = { pendingExpanded = !pendingExpanded },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f),
                    border = BorderStroke(
                        1.2.dp,
                        if (pendingExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Ausstehend"
                                    "Spanish" -> "Pendientes"
                                    else -> "Pending"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Red counter badge
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onError.copy(alpha = 0.75f))
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = "${pendingConfirmations.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = if (pendingExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Expandable pending items
                if (pendingExpanded) {
                    Spacer(modifier = Modifier.height(6.dp))
                    prioritizedPendingConfirmations.forEach { confirmation ->
                        PendingConfirmationCard(
                            confirmation = confirmation,
                            selectedLanguage = selectedLanguage,
                            onRespond = { status, comment ->
                                onRespond(confirmation.id, status, comment)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Empty state
            if (pendingConfirmations.isEmpty() && alerts.isEmpty() && history.isEmpty()) {
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Keine Benachrichtigungen"
                        "Spanish" -> "Sin notificaciones"
                        else -> "No notifications"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            // Recent activity section (tappable history items)
            if (history.isNotEmpty()) {
                if (pendingConfirmations.isNotEmpty() || alerts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                TappableHistorySection(
                    history = history,
                    selectedLanguage = selectedLanguage,
                    onItemClick = onHistoryItemClick
                )
            }
        }
    }
}

@Composable
fun AlertNotificationCard(
    alert: ReminderConfirmationStorage.ConfirmationAlert,
    selectedLanguage: String,
    onDismiss: () -> Unit,
    onOpenSmartAlertsSettings: (String) -> Unit = {}
) {
    var showInfoDialog by remember { mutableStateOf(false) }
    val alertColor = when (alert.alertType) {
        "no_streak" -> MaterialTheme.colorScheme.error
        "yes_streak" -> MaterialTheme.colorScheme.primary
        "missed" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val alertIcon = when (alert.alertType) {
        "no_streak" -> "⚠️"
        "yes_streak" -> "🏆"
        "missed" -> "⏰"
        else -> "🔔"
    }
    val n = alert.streakCount
    val alertTitle = when (alert.alertType) {
        "no_streak" -> when (selectedLanguage) {
            "German" -> if (n == 1) "Du hast 1× nicht bestätigt"
                else "Du hast ${n}× in Folge nicht bestätigt"
            "Spanish" -> if (n == 1) "No has confirmado 1 vez"
                else "No has confirmado $n veces seguidas"
            else -> if (n == 1) "You haven't confirmed 1 time"
                else "You haven't confirmed $n times in a row"
        }
        "yes_streak" -> when (selectedLanguage) {
            "German" -> if (n == 1) "1× bestätigt — weiter so!"
                else "Großartig! ${n}× in Folge bestätigt!"
            "Spanish" -> if (n == 1) "1× confirmado — ¡sigue así!"
                else "¡Genial! $n veces seguidas confirmado!"
            else -> if (n == 1) "1× confirmed — keep going!"
                else "Amazing! $n times confirmed in a row!"
        }
        "missed" -> when (selectedLanguage) {
            "German" -> if (n == 1) "1 Erinnerung ohne Antwort"
                else "$n Erinnerungen ohne Antwort"
            "Spanish" -> if (n == 1) "1 recordatorio sin respuesta"
                else "$n recordatorios sin respuesta"
            else -> if (n == 1) "1 reminder left unanswered"
                else "$n reminders left unanswered"
        }
        else -> ""
    }
    val timeText = friendlyTimestampAgo(alert.timestamp, selectedLanguage)
    val infoTitle = when (selectedLanguage) {
        "German" -> "Warum sehe ich diese Warnung?"
        "Spanish" -> "¿Por qué veo esta alerta?"
        else -> "Why am I seeing this alert?"
    }
    val alertReasonText = when (alert.alertType) {
        "no_streak" -> when (selectedLanguage) {
            "German" -> "Du hast $n× in Folge mit \"Nein\" geantwortet."
            "Spanish" -> "Has respondido \"No\" $n veces seguidas."
            else -> "You responded \"No\" $n times in a row."
        }
        "yes_streak" -> when (selectedLanguage) {
            "German" -> "Du hast $n× in Folge mit \"Ja\" geantwortet."
            "Spanish" -> "Has respondido \"Sí\" $n veces seguidas."
            else -> "You responded \"Yes\" $n times in a row."
        }
        "missed" -> when (selectedLanguage) {
            "German" -> "Es gibt $n unbeantwortete Erinnerungen."
            "Spanish" -> "Hay $n recordatorios sin responder."
            else -> "There are $n unanswered reminders."
        }
        else -> ""
    }
    val smartAlertEnabledText = when (selectedLanguage) {
        "German" -> "Diese Meldung erscheint, weil \"Smart Alerts\" für diese Erinnerung aktiviert ist."
        "Spanish" -> "Este mensaje aparece porque \"Alertas inteligentes\" está activado para este recordatorio."
        else -> "You see this message because Smart Alerts is enabled for this reminder."
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = alertColor.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, alertColor.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = alertIcon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${alert.reminderEmoji} ${alert.reminderName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = alertTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = alertColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeText,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                onClick = { showInfoDialog = true },
                shape = RoundedCornerShape(6.dp),
                color = alertColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, alertColor.copy(alpha = 0.75f)),
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = alertColor.copy(alpha = 0.85f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(6.dp),
                color = alertColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, alertColor.copy(alpha = 0.75f)),
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("✕", fontSize = 12.sp, color = alertColor.copy(alpha = 0.7f))
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = infoTitle,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = alertReasonText, fontSize = 13.sp)
                    Text(
                        text = smartAlertEnabledText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInfoDialog = false
                        onOpenSmartAlertsSettings(alert.reminderId)
                    }
                ) {
                    Text(
                        when (selectedLanguage) {
                            "German" -> "Smart Alerts öffnen"
                            "Spanish" -> "Abrir Alertas inteligentes"
                            else -> "Open Smart Alerts"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(
                        when (selectedLanguage) {
                            "German" -> "Schließen"
                            "Spanish" -> "Cerrar"
                            else -> "Close"
                        }
                    )
                }
            }
        )
    }
}
