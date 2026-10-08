package com.j4.diabetestracker

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun NotificationSettingsDialog(
    selectedLanguage: String,
    context: Context,
    pendingConfirmations: List<ReminderConfirmation>,
    confirmationHistory: List<ReminderConfirmation>,
    activeAlerts: List<ReminderConfirmationStorage.ConfirmationAlert>,
    focusSmartAlertsReminderId: String? = null,
    onConfirmationsChanged: () -> Unit,
    onOpenHistory: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val dialogScrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Benachrichtigungen"
                            "Spanish" -> "Notificaciones"
                            else -> "Notifications"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                // Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(dialogScrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Global Master Toggle
                    var globalNotificationsEnabled by remember { mutableStateOf(NotificationPreferences.isTimeRemindersEnabled(context)) }
                    
                    GlobalNotificationToggle(
                        selectedLanguage = selectedLanguage,
                        context = context,
                        isEnabled = globalNotificationsEnabled,
                        onToggle = { enabled ->
                            globalNotificationsEnabled = enabled
                            NotificationPreferences.setTimeRemindersEnabled(context, enabled)
                        }
                    )

                    // Meal Reminders section (independent)
                    MealRemindersCard(
                        selectedLanguage = selectedLanguage,
                        context = context,
                        globalEnabled = globalNotificationsEnabled
                    )

                    // Blood Sugar Reminders section (independent)
                    BloodSugarRemindersCard(
                        selectedLanguage = selectedLanguage,
                        context = context,
                        globalEnabled = globalNotificationsEnabled
                    )

                    // Custom Reminders section
                    CustomRemindersSection(
                        selectedLanguage = selectedLanguage,
                        context = context,
                        pendingConfirmations = pendingConfirmations,
                        confirmationHistory = confirmationHistory,
                        activeAlerts = activeAlerts,
                        focusSmartAlertsReminderId = focusSmartAlertsReminderId,
                        onConfirmationsChanged = onConfirmationsChanged,
                        onOpenHistory = onOpenHistory,
                        globalEnabled = globalNotificationsEnabled
                    )

                    // Quiet Hours section
                    QuietHoursSection(
                        selectedLanguage = selectedLanguage,
                        context = context
                    )

                    // Test Notification button
                    TestNotificationButton(
                        selectedLanguage = selectedLanguage,
                        context = context
                    )
                }

                ScrollableDialogHint(
                    selectedLanguage = selectedLanguage,
                    scrollState = dialogScrollState,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun GlobalNotificationToggle(
    selectedLanguage: String,
    context: Context,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = if (isEnabled) 2.dp else 1.dp,
            color = if (isEnabled)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Alle Benachrichtigungen"
                            "Spanish" -> "Todas las notificaciones"
                            else -> "All Notifications"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Globaler Ein-/Ausschalter"
                            "Spanish" -> "Interruptor global"
                            else -> "Global master switch"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle
                )
            }
        }
    }
}

@Composable
private fun MealRemindersCard(
    selectedLanguage: String,
    context: Context,
    globalEnabled: Boolean = true
) {
    var mealRemindersEnabled by remember { mutableStateOf(NotificationPreferences.isMealRemindersEnabled(context)) }
    var mealBreakfastTime by remember { mutableStateOf(NotificationPreferences.getMealBreakfastTime(context)) }
    var mealLunchTime by remember { mutableStateOf(NotificationPreferences.getMealLunchTime(context)) }
    var mealDinnerTime by remember { mutableStateOf(NotificationPreferences.getMealDinnerTime(context)) }
    var showMealBreakfastPicker by remember { mutableStateOf(false) }
    var showMealLunchPicker by remember { mutableStateOf(false) }
    var showMealDinnerPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (globalEnabled)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (globalEnabled) 0.5f else 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !globalEnabled) {
                    if (!globalEnabled) {
                        android.widget.Toast.makeText(
                            context,
                            when (selectedLanguage) {
                                "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                "Spanish" -> "Por favor, active primero las notificaciones globales"
                                else -> "Please enable global notifications first"
                            },
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .padding(14.dp)
                .alpha(if (globalEnabled) 1f else 0.4f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Mahlzeiterinnerungen"
                        "Spanish" -> "Recordatorios de comidas"
                        else -> "Meal Reminders"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Switch(
                    checked = mealRemindersEnabled,
                    enabled = globalEnabled,
                    onCheckedChange = {
                        if (globalEnabled) {
                            mealRemindersEnabled = it
                            NotificationPreferences.setMealRemindersEnabled(context, it)
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                when (selectedLanguage) {
                                    "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                    "Spanish" -> "Por favor, active primero las notificaciones globales"
                                    else -> "Please enable global notifications first"
                                },
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }

            if (mealRemindersEnabled) {
                Spacer(modifier = Modifier.height(8.dp))

                TimeRow(
                    label = when (selectedLanguage) { "German" -> "Frühstück:"; "Spanish" -> "Desayuno:"; else -> "Breakfast:" },
                    time = mealBreakfastTime,
                    enabled = globalEnabled,
                    onClick = { 
                        if (globalEnabled) {
                            showMealBreakfastPicker = true
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                when (selectedLanguage) {
                                    "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                    "Spanish" -> "Por favor, active primero las notificaciones globales"
                                    else -> "Please enable global notifications first"
                                },
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
                TimeRow(
                    label = when (selectedLanguage) { "German" -> "Mittagessen:"; "Spanish" -> "Almuerzo:"; else -> "Lunch:" },
                    time = mealLunchTime,
                    enabled = globalEnabled,
                    onClick = { 
                        if (globalEnabled) {
                            showMealLunchPicker = true
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                when (selectedLanguage) {
                                    "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                    "Spanish" -> "Por favor, active primero las notificaciones globales"
                                    else -> "Please enable global notifications first"
                                },
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
                TimeRow(
                    label = when (selectedLanguage) { "German" -> "Abendessen:"; "Spanish" -> "Cena:"; else -> "Dinner:" },
                    time = mealDinnerTime,
                    enabled = globalEnabled,
                    onClick = { 
                        if (globalEnabled) {
                            showMealDinnerPicker = true
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                when (selectedLanguage) {
                                    "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                    "Spanish" -> "Por favor, active primero las notificaciones globales"
                                    else -> "Please enable global notifications first"
                                },
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
    }

    // Time pickers
    if (showMealBreakfastPicker) {
        TimePickerDialog(
            title = when (selectedLanguage) { "German" -> "Frühstückszeit"; "Spanish" -> "Hora de desayuno"; else -> "Breakfast Time" },
            currentTime = mealBreakfastTime,
            onDismiss = { showMealBreakfastPicker = false },
            onTimeSelected = { h, m ->
                mealBreakfastTime = Pair(h, m)
                NotificationPreferences.setMealBreakfastTime(context, h, m)
                showMealBreakfastPicker = false
            }
        )
    }
    if (showMealLunchPicker) {
        TimePickerDialog(
            title = when (selectedLanguage) { "German" -> "Mittagszeit"; "Spanish" -> "Hora de almuerzo"; else -> "Lunch Time" },
            currentTime = mealLunchTime,
            onDismiss = { showMealLunchPicker = false },
            onTimeSelected = { h, m ->
                mealLunchTime = Pair(h, m)
                NotificationPreferences.setMealLunchTime(context, h, m)
                showMealLunchPicker = false
            }
        )
    }
    if (showMealDinnerPicker) {
        TimePickerDialog(
            title = when (selectedLanguage) { "German" -> "Abendessenzeit"; "Spanish" -> "Hora de cena"; else -> "Dinner Time" },
            currentTime = mealDinnerTime,
            onDismiss = { showMealDinnerPicker = false },
            onTimeSelected = { h, m ->
                mealDinnerTime = Pair(h, m)
                NotificationPreferences.setMealDinnerTime(context, h, m)
                showMealDinnerPicker = false
            }
        )
    }
}

@Composable
private fun BloodSugarRemindersCard(
    selectedLanguage: String,
    context: Context,
    globalEnabled: Boolean = true
) {
    var bloodSugarCheckEnabled by remember { mutableStateOf(NotificationPreferences.isBloodSugarCheckRemindersEnabled(context)) }
    var bsMorningTime by remember { mutableStateOf(NotificationPreferences.getBloodSugarMorningTime(context)) }
    var bsAfternoonTime by remember { mutableStateOf(NotificationPreferences.getBloodSugarAfternoonTime(context)) }
    var bsEveningTime by remember { mutableStateOf(NotificationPreferences.getBloodSugarEveningTime(context)) }
    var showBsMorningPicker by remember { mutableStateOf(false) }
    var showBsAfternoonPicker by remember { mutableStateOf(false) }
    var showBsEveningPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (globalEnabled)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (globalEnabled) 0.5f else 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !globalEnabled) {
                    if (!globalEnabled) {
                        android.widget.Toast.makeText(
                            context,
                            when (selectedLanguage) {
                                "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                "Spanish" -> "Por favor, active primero las notificaciones globales"
                                else -> "Please enable global notifications first"
                            },
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .padding(14.dp)
                .alpha(if (globalEnabled) 1f else 0.4f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Blutzucker-Check"
                        "Spanish" -> "Chequeo de azúcar"
                        else -> "Blood Sugar Check"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Switch(
                    checked = bloodSugarCheckEnabled,
                    enabled = globalEnabled,
                    onCheckedChange = {
                        if (globalEnabled) {
                            bloodSugarCheckEnabled = it
                            NotificationPreferences.setBloodSugarCheckRemindersEnabled(context, it)
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                when (selectedLanguage) {
                                    "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                    "Spanish" -> "Por favor, active primero las notificaciones globales"
                                    else -> "Please enable global notifications first"
                                },
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }

            if (bloodSugarCheckEnabled) {
                Spacer(modifier = Modifier.height(8.dp))

                TimeRow(
                    label = when (selectedLanguage) { "German" -> "Morgen:"; "Spanish" -> "Mañana:"; else -> "Morning:" },
                    time = bsMorningTime,
                    enabled = globalEnabled,
                    onClick = { 
                        if (globalEnabled) {
                            showBsMorningPicker = true
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                when (selectedLanguage) {
                                    "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                    "Spanish" -> "Por favor, active primero las notificaciones globales"
                                    else -> "Please enable global notifications first"
                                },
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
                TimeRow(
                    label = when (selectedLanguage) { "German" -> "Nachmittag:"; "Spanish" -> "Tarde:"; else -> "Afternoon:" },
                    time = bsAfternoonTime,
                    enabled = globalEnabled,
                    onClick = { 
                        if (globalEnabled) {
                            showBsAfternoonPicker = true
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                when (selectedLanguage) {
                                    "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                    "Spanish" -> "Por favor, active primero las notificaciones globales"
                                    else -> "Please enable global notifications first"
                                },
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
                TimeRow(
                    label = when (selectedLanguage) { "German" -> "Abend:"; "Spanish" -> "Noche:"; else -> "Evening:" },
                    time = bsEveningTime,
                    enabled = globalEnabled,
                    onClick = { 
                        if (globalEnabled) {
                            showBsEveningPicker = true
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                when (selectedLanguage) {
                                    "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                    "Spanish" -> "Por favor, active primero las notificaciones globales"
                                    else -> "Please enable global notifications first"
                                },
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
    }

    // Time pickers
    if (showBsMorningPicker) {
        TimePickerDialog(
            title = when (selectedLanguage) { "German" -> "Morgen-Check-Zeit"; "Spanish" -> "Hora de chequeo matutino"; else -> "Morning Check Time" },
            currentTime = bsMorningTime,
            onDismiss = { showBsMorningPicker = false },
            onTimeSelected = { h, m ->
                bsMorningTime = Pair(h, m)
                NotificationPreferences.setBloodSugarMorningTime(context, h, m)
                showBsMorningPicker = false
            }
        )
    }
    if (showBsAfternoonPicker) {
        TimePickerDialog(
            title = when (selectedLanguage) { "German" -> "Nachmittags-Check-Zeit"; "Spanish" -> "Hora de chequeo vespertino"; else -> "Afternoon Check Time" },
            currentTime = bsAfternoonTime,
            onDismiss = { showBsAfternoonPicker = false },
            onTimeSelected = { h, m ->
                bsAfternoonTime = Pair(h, m)
                NotificationPreferences.setBloodSugarAfternoonTime(context, h, m)
                showBsAfternoonPicker = false
            }
        )
    }
    if (showBsEveningPicker) {
        TimePickerDialog(
            title = when (selectedLanguage) { "German" -> "Abend-Check-Zeit"; "Spanish" -> "Hora de chequeo nocturno"; else -> "Evening Check Time" },
            currentTime = bsEveningTime,
            onDismiss = { showBsEveningPicker = false },
            onTimeSelected = { h, m ->
                bsEveningTime = Pair(h, m)
                NotificationPreferences.setBloodSugarEveningTime(context, h, m)
                showBsEveningPicker = false
            }
        )
    }
}

@Composable
private fun TimeRow(
    label: String,
    time: Pair<Int, Int>,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        TextButton(
            onClick = onClick,
            enabled = enabled
        ) {
            Text("${String.format("%02d", time.first)}:${String.format("%02d", time.second)}")
        }
    }
}

@Composable
private fun CustomRemindersSection(
    selectedLanguage: String,
    context: Context,
    pendingConfirmations: List<ReminderConfirmation>,
    confirmationHistory: List<ReminderConfirmation>,
    activeAlerts: List<ReminderConfirmationStorage.ConfirmationAlert>,
    focusSmartAlertsReminderId: String? = null,
    onConfirmationsChanged: () -> Unit,
    onOpenHistory: (String) -> Unit,
    globalEnabled: Boolean = true
) {
    var customReminders by remember { mutableStateOf(CustomReminderStorage.loadReminders(context)) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<CustomReminder?>(null) }
    var smartAlertsFocusHandled by remember(focusSmartAlertsReminderId) { mutableStateOf(false) }

    LaunchedEffect(focusSmartAlertsReminderId, customReminders, globalEnabled, smartAlertsFocusHandled) {
        if (!smartAlertsFocusHandled && !focusSmartAlertsReminderId.isNullOrBlank()) {
            val targetReminder = customReminders.find { it.id == focusSmartAlertsReminderId }
            if (targetReminder != null && globalEnabled) {
                editingReminder = targetReminder
                showAddReminderDialog = true
            }
            smartAlertsFocusHandled = true
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (globalEnabled)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (globalEnabled) 0.5f else 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !globalEnabled) {
                    if (!globalEnabled) {
                        android.widget.Toast.makeText(
                            context,
                            when (selectedLanguage) {
                                "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                "Spanish" -> "Por favor, active primero las notificaciones globales"
                                else -> "Please enable global notifications first"
                            },
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .padding(14.dp)
                .alpha(if (globalEnabled) 1f else 0.4f)
        ) {
            Text(
                text = when (selectedLanguage) {
                    "German" -> "Benutzerdefinierte Erinnerungen"
                    "Spanish" -> "Recordatorios personalizados"
                    else -> "Custom Reminders"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Collapsible pending confirmations section
            val sectionPending = pendingConfirmations.filter { pc ->
                customReminders.any { it.id == pc.reminderId }
            }
            if (sectionPending.isNotEmpty()) {
                var pendingExpanded by remember { mutableStateOf(false) }
                
                // Pending header with counter badge
                Surface(
                    onClick = { pendingExpanded = !pendingExpanded },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
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
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Red counter badge
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = "${sectionPending.size}",
                                        fontSize = 11.sp,
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
                    Spacer(modifier = Modifier.height(8.dp))
                    sectionPending.forEach { confirmation ->
                        PendingConfirmationCard(
                            confirmation = confirmation,
                            selectedLanguage = selectedLanguage,
                            onRespond = { status, comment ->
                                ReminderConfirmationStorage.respondToConfirmation(context, confirmation.id, status, comment)
                                val alertReminder = customReminders.find { it.id == confirmation.reminderId }
                                if (alertReminder != null && alertReminder.requiresConfirmation) {
                                    ReminderConfirmationStorage.checkAndTriggerAlerts(context, confirmation.reminderId, alertReminder.confirmationAlertSettings)
                                }
                                onConfirmationsChanged()
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // List of custom reminders
            if (customReminders.isEmpty()) {
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Keine benutzerdefinierten Erinnerungen. Tippen Sie unten, um eine hinzuzufügen."
                        "Spanish" -> "No hay recordatorios personalizados. Toca abajo para agregar uno."
                        else -> "No custom reminders. Tap below to add one."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                customReminders.forEach { reminder ->
                    CustomReminderItem(
                        reminder = reminder,
                        selectedLanguage = selectedLanguage,
                        context = context,
                        globalEnabled = globalEnabled,
                        onToggle = { enabled ->
                            if (globalEnabled) {
                                try {
                                    val updated = reminder.copy(isEnabled = enabled)
                                    CustomReminderStorage.updateReminder(context, updated)
                                    customReminders = CustomReminderStorage.loadReminders(context)
                                    val entries = DataManager.getEntries(context)
                                    ReminderScheduler.rescheduleAllReminders(context, entries, emptyList())
                                } catch (e: Exception) {
                                    android.util.Log.e("CustomReminder", "Error toggling reminder", e)
                                }
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    when (selectedLanguage) {
                                        "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                        "Spanish" -> "Por favor, active primero las notificaciones globales"
                                        else -> "Please enable global notifications first"
                                    },
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onEdit = {
                            if (globalEnabled) {
                                editingReminder = reminder
                                showAddReminderDialog = true
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    when (selectedLanguage) {
                                        "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                        "Spanish" -> "Por favor, active primero las notificaciones globales"
                                        else -> "Please enable global notifications first"
                                    },
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onDelete = {
                            if (globalEnabled) {
                                CustomReminderStorage.deleteReminder(context, reminder.id)
                                customReminders = CustomReminderStorage.loadReminders(context)
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    when (selectedLanguage) {
                                        "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                        "Spanish" -> "Por favor, active primero las notificaciones globales"
                                        else -> "Please enable global notifications first"
                                    },
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onHistory = { 
                            if (globalEnabled) {
                                onOpenHistory(reminder.id)
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    when (selectedLanguage) {
                                        "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                        "Spanish" -> "Por favor, active primero las notificaciones globales"
                                        else -> "Please enable global notifications first"
                                    },
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (globalEnabled) {
                        editingReminder = null
                        showAddReminderDialog = true
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            when (selectedLanguage) {
                                "German" -> "Bitte aktivieren Sie zuerst die globalen Benachrichtigungen"
                                "Spanish" -> "Por favor, active primero las notificaciones globales"
                                else -> "Please enable global notifications first"
                            },
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = globalEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    when (selectedLanguage) {
                        "German" -> "Erinnerung hinzufügen"
                        "Spanish" -> "Agregar recordatorio"
                        else -> "Add Custom Reminder"
                    }
                )
            }

            if (showAddReminderDialog) {
                CustomReminderDialog(
                    reminder = editingReminder,
                    selectedLanguage = selectedLanguage,
                    openSmartAlertsOnStart =
                        focusSmartAlertsReminderId != null &&
                        editingReminder?.id == focusSmartAlertsReminderId,
                    onDismiss = {
                        showAddReminderDialog = false
                        editingReminder = null
                    },
                    onSave = { reminder ->
                        try {
                            if (editingReminder != null) {
                                CustomReminderStorage.updateReminder(context, reminder)
                            } else {
                                CustomReminderStorage.addReminder(context, reminder)
                            }
                            customReminders = CustomReminderStorage.loadReminders(context)
                            val entries = DataManager.getEntries(context)
                            val customMarkers = CustomMarkerManager.getCustomMarkers(context)
                            ReminderScheduler.rescheduleAllReminders(context, entries, customMarkers)
                        } catch (e: Exception) {
                            android.util.Log.e("CustomReminder", "Error saving reminder", e)
                        }
                        showAddReminderDialog = false
                        editingReminder = null
                    }
                )
            }
        }
    }
}

@Composable
private fun CustomReminderItem(
    reminder: CustomReminder,
    selectedLanguage: String,
    context: Context,
    globalEnabled: Boolean = true,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isEnabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${reminder.emoji} ${reminder.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Switch(
                    checked = reminder.isEnabled,
                    enabled = globalEnabled,
                    onCheckedChange = onToggle
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val daysText = if (reminder.daysOfWeek.size == 7) {
                when (selectedLanguage) {
                    "German" -> "Jeden Tag"
                    "Spanish" -> "Todos los días"
                    else -> "Every day"
                }
            } else {
                reminder.daysOfWeek.sortedBy { it.ordinal }.joinToString(", ") { it.toShortString() }
            }

            val timesText = if (reminder.intervalHours > 0f) {
                val hrsStr = if (reminder.intervalHours == reminder.intervalHours.toInt().toFloat())
                    "${reminder.intervalHours.toInt()}" else "${reminder.intervalHours}"
                val startTime = String.format("%02d:%02d", reminder.intervalStartHour, reminder.intervalStartMinute)
                when (selectedLanguage) {
                    "German" -> "Alle $hrsStr Std. ab $startTime"
                    "Spanish" -> "Cada $hrsStr hrs desde $startTime"
                    else -> "Every $hrsStr hrs from $startTime"
                }
            } else {
                reminder.times.joinToString(", ") { it.toString() }
            }

            Text(
                text = "$daysText: $timesText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (reminder.requiresConfirmation) {
                    TextButton(
                        onClick = onHistory,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            when (selectedLanguage) {
                                "German" -> "📊 Verlauf"
                                "Spanish" -> "📊 Historial"
                                else -> "📊 History"
                            },
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
                TextButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        when (selectedLanguage) {
                            "German" -> "Bearbeiten"
                            "Spanish" -> "Editar"
                            else -> "Edit"
                        },
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        when (selectedLanguage) {
                            "German" -> "Löschen"
                            "Spanish" -> "Eliminar"
                            else -> "Delete"
                        },
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun QuietHoursSection(
    selectedLanguage: String,
    context: Context
) {
    var quietHoursEnabled by remember { mutableStateOf(NotificationPreferences.isQuietHoursEnabled(context)) }
    var quietStartHour by remember { mutableStateOf(NotificationPreferences.getQuietStartHour(context)) }
    var quietEndHour by remember { mutableStateOf(NotificationPreferences.getQuietEndHour(context)) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Ruhezeiten"
                        "Spanish" -> "Horas de silencio"
                        else -> "Quiet Hours"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Switch(
                    checked = quietHoursEnabled,
                    onCheckedChange = {
                        quietHoursEnabled = it
                        NotificationPreferences.setQuietHoursEnabled(context, it)
                    }
                )
            }

            if (quietHoursEnabled) {
                Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (selectedLanguage) { "German" -> "Start:"; "Spanish" -> "Inicio:"; else -> "Start:" },
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(onClick = { showStartTimePicker = true }) {
                            Text("${String.format("%02d", quietStartHour)}:00")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (selectedLanguage) { "German" -> "Ende:"; "Spanish" -> "Fin:"; else -> "End:" },
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(onClick = { showEndTimePicker = true }) {
                            Text("${String.format("%02d", quietEndHour)}:00")
                        }
                    }
                }
            }
        }
    }

    if (showStartTimePicker) {
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = {
                Text(
                    when (selectedLanguage) {
                        "German" -> "Startzeit wählen"
                        "Spanish" -> "Seleccionar hora de inicio"
                        else -> "Select Start Time"
                    }
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    (0..23).forEach { hour ->
                        TextButton(
                            onClick = {
                                quietStartHour = hour
                                NotificationPreferences.setQuietStartHour(context, hour)
                                showStartTimePicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${String.format("%02d", hour)}:00")
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showEndTimePicker) {
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = {
                Text(
                    when (selectedLanguage) {
                        "German" -> "Endzeit wählen"
                        "Spanish" -> "Seleccionar hora de fin"
                        else -> "Select End Time"
                    }
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    (0..23).forEach { hour ->
                        TextButton(
                            onClick = {
                                quietEndHour = hour
                                NotificationPreferences.setQuietEndHour(context, hour)
                                showEndTimePicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${String.format("%02d", hour)}:00")
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun TestNotificationButton(
    selectedLanguage: String,
    context: Context
) {
    val timeRemindersEnabled = NotificationPreferences.isTimeRemindersEnabled(context)
    val mealRemindersEnabled = NotificationPreferences.isMealRemindersEnabled(context)
    val bloodSugarCheckEnabled = NotificationPreferences.isBloodSugarCheckRemindersEnabled(context)

    Button(
        onClick = {
            if (!NotificationHelper.hasNotificationPermission(context)) {
                android.widget.Toast.makeText(
                    context,
                    when (selectedLanguage) {
                        "German" -> "Benachrichtigungsberechtigung erforderlich"
                        "Spanish" -> "Se requiere permiso de notificación"
                        else -> "Notification permission required"
                    },
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@Button
            }

            if (!timeRemindersEnabled) {
                android.widget.Toast.makeText(
                    context,
                    when (selectedLanguage) {
                        "German" -> "Zeiterinnerungen sind deaktiviert"
                        "Spanish" -> "Los recordatorios de tiempo están desactivados"
                        else -> "Time Reminders are disabled"
                    },
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@Button
            }

            val (title, message) = when {
                mealRemindersEnabled -> {
                    val t = when (selectedLanguage) {
                        "German" -> "Mahlzeiterinnerung"
                        "Spanish" -> "Recordatorio de comida"
                        else -> "Meal Reminder"
                    }
                    val m = when (selectedLanguage) {
                        "German" -> "Zeit zum Mittagessen! Du isst normalerweise um diese Zeit."
                        "Spanish" -> "¡Hora de almorzar! Normalmente comes a esta hora."
                        else -> "Time for lunch! You usually eat around this time."
                    }
                    t to m
                }
                bloodSugarCheckEnabled -> {
                    val t = when (selectedLanguage) {
                        "German" -> "Blutzucker-Check"
                        "Spanish" -> "Chequeo de azúcar"
                        else -> "Blood Sugar Check"
                    }
                    val m = when (selectedLanguage) {
                        "German" -> "Zeit, deinen Blutzucker zu überprüfen!"
                        "Spanish" -> "¡Hora de revisar tu azúcar en sangre!"
                        else -> "Time to check your blood sugar!"
                    }
                    t to m
                }
                else -> {
                    android.widget.Toast.makeText(
                        context,
                        when (selectedLanguage) {
                            "German" -> "Bitte aktiviere mindestens eine Erinnerungsoption"
                            "Spanish" -> "Por favor activa al menos una opción de recordatorio"
                            else -> "Please enable at least one reminder option"
                        },
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }
            }

            NotificationHelper.showTimeReminderNotification(
                context = context,
                title = title,
                message = message
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            when (selectedLanguage) {
                "German" -> "Testbenachrichtigung"
                "Spanish" -> "Notificación de prueba"
                else -> "Test Notification"
            }
        )
    }
}
