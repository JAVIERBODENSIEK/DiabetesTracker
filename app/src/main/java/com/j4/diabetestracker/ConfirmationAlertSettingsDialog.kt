package com.j4.diabetestracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ConfirmationAlertSettingsDialog(
    settings: ConfirmationAlertSettings,
    selectedLanguage: String,
    onSave: (ConfirmationAlertSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var alertOnNoStreak by remember { mutableStateOf(settings.alertOnNoStreak) }
    var noStreakThreshold by remember { mutableStateOf(settings.noStreakThreshold) }
    var alertOnYesStreak by remember { mutableStateOf(settings.alertOnYesStreak) }
    var yesStreakThreshold by remember { mutableStateOf(settings.yesStreakThreshold) }
    var alertOnMissed by remember { mutableStateOf(settings.alertOnMissed) }
    var missedThreshold by remember { mutableStateOf(settings.missedThreshold) }
    var alertWithSound by remember { mutableStateOf(settings.alertWithSound) }
    var alertWithVibration by remember { mutableStateOf(settings.alertWithVibration) }
    var showSmartAlertsInfoDialog by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Erweiterte Benachrichtigungen"
                                "Spanish" -> "Alertas avanzadas"
                                else -> "Smart Alerts"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        onClick = { showSmartAlertsInfoDialog = true },
                        shape = CircleShape,
                        color = primaryColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = when (selectedLanguage) {
                                    "German" -> "Infos zu Smart Alerts"
                                    "Spanish" -> "Información sobre alertas inteligentes"
                                    else -> "Smart Alerts info"
                                },
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Erhalte Warnungen basierend auf deinen Bestätigungsmustern"
                        "Spanish" -> "Recibe alertas según tus patrones de confirmación"
                        else -> "Get warnings based on your confirmation patterns"
                    },
                    fontSize = 11.sp,
                    color = onSurfaceVariant.copy(alpha = 0.7f)
                )

                Divider(color = onSurfaceVariant.copy(alpha = 0.12f))

                // === NO Streak Alert ===
                AlertRuleCard(
                    icon = { Icon(Icons.Rounded.Warning, null, tint = errorColor, modifier = Modifier.size(18.dp)) },
                    title = when (selectedLanguage) {
                        "German" -> "\"Nein\"-Serie Warnung"
                        "Spanish" -> "Alerta de racha \"No\""
                        else -> "\"No\" Streak Warning"
                    },
                    description = when (selectedLanguage) {
                        "German" -> "Warnung nach X aufeinanderfolgenden \"Nein\""
                        "Spanish" -> "Alerta después de X \"No\" consecutivos"
                        else -> "Alert after X consecutive \"No\" responses"
                    },
                    enabled = alertOnNoStreak,
                    onEnabledChange = { alertOnNoStreak = it },
                    threshold = noStreakThreshold,
                    onThresholdChange = { noStreakThreshold = it },
                    thresholdRange = 2..10,
                    selectedLanguage = selectedLanguage,
                    accentColor = errorColor
                )

                // === YES Streak Alert (positive) ===
                AlertRuleCard(
                    icon = { Icon(Icons.Rounded.EmojiEvents, null, tint = primaryColor, modifier = Modifier.size(18.dp)) },
                    title = when (selectedLanguage) {
                        "German" -> "\"Ja\"-Serie Belohnung"
                        "Spanish" -> "Recompensa de racha \"Sí\""
                        else -> "\"Yes\" Streak Reward"
                    },
                    description = when (selectedLanguage) {
                        "German" -> "Positive Nachricht nach X aufeinanderfolgenden \"Ja\""
                        "Spanish" -> "Mensaje positivo después de X \"Sí\" consecutivos"
                        else -> "Positive message after X consecutive \"Yes\""
                    },
                    enabled = alertOnYesStreak,
                    onEnabledChange = { alertOnYesStreak = it },
                    threshold = yesStreakThreshold,
                    onThresholdChange = { yesStreakThreshold = it },
                    thresholdRange = 3..30,
                    selectedLanguage = selectedLanguage,
                    accentColor = primaryColor
                )

                // === Missed Alert ===
                AlertRuleCard(
                    icon = { Icon(Icons.Rounded.Schedule, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp)) },
                    title = when (selectedLanguage) {
                        "German" -> "Verpasste Bestätigungen"
                        "Spanish" -> "Confirmaciones perdidas"
                        else -> "Missed Confirmations"
                    },
                    description = when (selectedLanguage) {
                        "German" -> "Warnung nach X unbeantworteten Erinnerungen"
                        "Spanish" -> "Alerta después de X recordatorios sin responder"
                        else -> "Alert after X unanswered reminders"
                    },
                    enabled = alertOnMissed,
                    onEnabledChange = { alertOnMissed = it },
                    threshold = missedThreshold,
                    onThresholdChange = { missedThreshold = it },
                    thresholdRange = 1..10,
                    selectedLanguage = selectedLanguage,
                    accentColor = MaterialTheme.colorScheme.tertiary
                )

                Divider(color = onSurfaceVariant.copy(alpha = 0.12f))

                // === Notification Style ===
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Benachrichtigungsstil"
                        "Spanish" -> "Estilo de notificación"
                        else -> "Alert Notification Style"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sound toggle
                    Surface(
                        onClick = { alertWithSound = !alertWithSound },
                        shape = RoundedCornerShape(10.dp),
                        color = if (alertWithSound) primaryColor.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Rounded.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (alertWithSound) primaryColor else onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Ton"
                                    "Spanish" -> "Sonido"
                                    else -> "Sound"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (alertWithSound) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (alertWithSound) primaryColor else onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Vibration toggle
                    Surface(
                        onClick = { alertWithVibration = !alertWithVibration },
                        shape = RoundedCornerShape(10.dp),
                        color = if (alertWithVibration) primaryColor.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Rounded.Vibration,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (alertWithVibration) primaryColor else onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Vibration"
                                    "Spanish" -> "Vibración"
                                    else -> "Vibration"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (alertWithVibration) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (alertWithVibration) primaryColor else onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Ohne Ton/Vibration erscheinen Warnungen nur in der Glocke"
                        "Spanish" -> "Sin sonido/vibración, las alertas solo aparecen en la campana"
                        else -> "Without sound/vibration, alerts only appear in the bell"
                    },
                    fontSize = 10.sp,
                    color = onSurfaceVariant.copy(alpha = 0.5f)
                )

                // === Action Buttons ===
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
                        onClick = {
                            onSave(ConfirmationAlertSettings(
                                alertOnNoStreak = alertOnNoStreak,
                                noStreakThreshold = noStreakThreshold,
                                alertOnYesStreak = alertOnYesStreak,
                                yesStreakThreshold = yesStreakThreshold,
                                alertOnMissed = alertOnMissed,
                                missedThreshold = missedThreshold,
                                alertWithSound = alertWithSound,
                                alertWithVibration = alertWithVibration
                            ))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
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

    if (showSmartAlertsInfoDialog) {
        AlertDialog(
            onDismissRequest = { showSmartAlertsInfoDialog = false },
            title = {
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Was ist dieser Bereich?"
                        "Spanish" -> "¿Para qué sirve esta área?"
                        else -> "What is this area for?"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Dieser Bereich hilft dir, wichtige Muster bei Erinnerungs-Bestätigungen früh zu erkennen.\n\n" +
                                "• Du kannst Warnungen für wiederholtes \"Nein\", verpasste Bestätigungen und lange \"Ja\"-Serien einstellen.\n" +
                                "• Mit den Schwellwerten legst du fest, nach wie vielen Ereignissen eine Warnung erscheint.\n" +
                                "• Ton und Vibration bestimmen, wie deutlich die Warnung ausfällt.\n\n" +
                                "Kurz gesagt: Smart Alerts unterstützen dich dabei, Gewohnheiten schneller zu bemerken und deine Erinnerungseinstellungen passend anzupassen."
                        "Spanish" -> "Esta área te ayuda a detectar a tiempo patrones importantes en tus confirmaciones de recordatorios.\n\n" +
                                "• Puedes configurar alertas para rachas repetidas de \"No\", confirmaciones perdidas y rachas largas de \"Sí\".\n" +
                                "• Con los umbrales decides después de cuántos eventos debe aparecer una alerta.\n" +
                                "• Sonido y vibración definen qué tan visible será la alerta.\n\n" +
                                "En resumen: Smart Alerts te ayuda a identificar hábitos más rápido y ajustar tus recordatorios de forma más precisa."
                        else -> "This area helps you notice important confirmation patterns early.\n\n" +
                                "• You can enable alerts for repeated \"No\" streaks, missed confirmations, and long \"Yes\" streaks.\n" +
                                "• Thresholds let you choose after how many events an alert should appear.\n" +
                                "• Sound and vibration control how prominent the alert feels.\n\n" +
                                "In short: Smart Alerts helps you spot habits sooner and fine-tune your reminder setup with confidence."
                    },
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(onClick = { showSmartAlertsInfoDialog = false }) {
                    Text(
                        when (selectedLanguage) {
                            "German" -> "Verstanden"
                            "Spanish" -> "Entendido"
                            else -> "Got it"
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun AlertRuleCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    threshold: Int,
    onThresholdChange: (Int) -> Unit,
    thresholdRange: IntRange,
    selectedLanguage: String,
    accentColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) accentColor.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) accentColor else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    modifier = Modifier.height(22.dp)
                )
            }

            // Threshold slider — only visible when enabled
            if (enabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Nach"
                            "Spanish" -> "Después de"
                            else -> "After"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    // Decrease button
                    Surface(
                        onClick = {
                            if (threshold > thresholdRange.first) onThresholdChange(threshold - 1)
                        },
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("−", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        }
                    }

                    // Number display
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$threshold",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }

                    // Increase button
                    Surface(
                        onClick = {
                            if (threshold < thresholdRange.last) onThresholdChange(threshold + 1)
                        },
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Mal in Folge"
                            "Spanish" -> "veces seguidas"
                            else -> "times in a row"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
