package com.j4.diabetestracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomReminderDialog(
    reminder: CustomReminder?,
    selectedLanguage: String,
    onDismiss: () -> Unit,
    onSave: (CustomReminder) -> Unit,
    openSmartAlertsOnStart: Boolean = false
) {
    var emoji by remember { mutableStateOf(reminder?.emoji ?: "⏰") }
    var name by remember { mutableStateOf(reminder?.name ?: "") }
    var hasSound by remember { mutableStateOf(reminder?.hasSound ?: true) }
    var hasVibration by remember { mutableStateOf(reminder?.hasVibration ?: true) }
    var selectedDays by remember { mutableStateOf(reminder?.daysOfWeek ?: DayOfWeek.entries.toSet()) }
    var times by remember { mutableStateOf(reminder?.times ?: emptyList()) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    var editingTimeIndex by remember { mutableStateOf(-1) }
    var useInterval by remember { mutableStateOf(reminder != null && reminder.intervalHours > 0f) }
    var intervalHours by remember { mutableStateOf(reminder?.intervalHours?.takeIf { it > 0f } ?: 2f) }
    var intervalStartHour by remember { mutableStateOf(reminder?.intervalStartHour ?: 8) }
    var intervalStartMinute by remember { mutableStateOf(reminder?.intervalStartMinute ?: 0) }
    var editingIntervalHours by remember { mutableStateOf(false) }
    var intervalHoursText by remember { mutableStateOf("") }
    var showIntervalTimePicker by remember { mutableStateOf(false) }
    var timeToDelete by remember { mutableStateOf<ReminderTime?>(null) }
    var requiresConfirmation by remember { mutableStateOf(reminder?.requiresConfirmation ?: false) }
    var confirmationAlertSettings by remember { mutableStateOf(reminder?.confirmationAlertSettings ?: ConfirmationAlertSettings()) }
    var showAlertSettingsDialog by remember(openSmartAlertsOnStart) { mutableStateOf(openSmartAlertsOnStart) }
    var showSystemSettingsWarning by remember { mutableStateOf(false) }
    var warningMessage by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // === TOP: Emoji circle + Name field ===
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji circle
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.12f))
                            .clickable { showEmojiPicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 28.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Name text field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                when (selectedLanguage) {
                                    "German" -> "Beschreibung eingeben..."
                                    "Spanish" -> "Ingrese descripción..."
                                    else -> "Enter description..."
                                },
                                color = onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // === SCROLLABLE CONTENT ===
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // === DAYS SECTION ===
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Tage"
                            "Spanish" -> "Días"
                            else -> "Days"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DayOfWeek.entries.forEach { day ->
                            val isSelected = selectedDays.contains(day)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) primaryColor.copy(alpha = 0.15f)
                                        else surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(
                                            1.5.dp,
                                            primaryColor.copy(alpha = 0.4f),
                                            RoundedCornerShape(10.dp)
                                        ) else Modifier
                                    )
                                    .clickable {
                                        selectedDays = if (isSelected) {
                                            selectedDays - day
                                        } else {
                                            selectedDays + day
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toShortString(),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) primaryColor else onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // === TIMES SECTION ===
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Zeiten"
                            "Spanish" -> "Horarios"
                            else -> "Times"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Mode toggle: Manual vs Interval
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val manualSelected = !useInterval
                        Surface(
                            onClick = { useInterval = false },
                            shape = RoundedCornerShape(10.dp),
                            color = if (manualSelected) primaryColor.copy(alpha = 0.15f)
                                else surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Manuell"
                                    "Spanish" -> "Manual"
                                    else -> "Manual"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (manualSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (manualSelected) primaryColor else onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                        Surface(
                            onClick = { useInterval = true },
                            shape = RoundedCornerShape(10.dp),
                            color = if (useInterval) primaryColor.copy(alpha = 0.15f)
                                else surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Alle X Std."
                                    "Spanish" -> "Cada X hrs"
                                    else -> "Every X hrs"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (useInterval) FontWeight.Bold else FontWeight.Normal,
                                color = if (useInterval) primaryColor else onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (useInterval) {
                        // === INTERVAL MODE ===
                        // Interval hours selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Alle"
                                    "Spanish" -> "Cada"
                                    else -> "Every"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        editingIntervalHours = false
                                        if (intervalHours > 0.5f) {
                                            intervalHours = ((intervalHours - 0.5f) * 10).toInt() / 10f
                                            if (intervalHours < 0.5f) intervalHours = 0.5f
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Decrease",
                                        tint = primaryColor
                                    )
                                }
                                
                                val hoursFocusRequester = remember { FocusRequester() }
                                
                                Box(
                                    modifier = Modifier
                                        .widthIn(min = 56.dp)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(primaryColor.copy(alpha = 0.1f))
                                        .clickable {
                                            editingIntervalHours = true
                                            intervalHoursText = if (intervalHours == intervalHours.toInt().toFloat())
                                                intervalHours.toInt().toString()
                                            else
                                                intervalHours.toString()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (editingIntervalHours) {
                                        BasicTextField(
                                            value = intervalHoursText,
                                            onValueChange = { newVal ->
                                                if (newVal.length <= 4 && newVal.matches(Regex("^\\d{0,2}\\.?\\d{0,1}$"))) {
                                                    intervalHoursText = newVal
                                                    newVal.toFloatOrNull()?.let { parsed ->
                                                        if (parsed in 0.5f..24f) intervalHours = parsed
                                                    }
                                                }
                                            },
                                            textStyle = TextStyle(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = primaryColor,
                                                textAlign = TextAlign.Center
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            modifier = Modifier
                                                .width(52.dp)
                                                .focusRequester(hoursFocusRequester)
                                        )
                                        LaunchedEffect(Unit) { hoursFocusRequester.requestFocus() }
                                    } else {
                                        Text(
                                            text = if (intervalHours == intervalHours.toInt().toFloat())
                                                "${intervalHours.toInt()}"
                                            else
                                                "$intervalHours",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor
                                        )
                                    }
                                }
                                
                                IconButton(
                                    onClick = {
                                        editingIntervalHours = false
                                        if (intervalHours < 24f) {
                                            intervalHours = ((intervalHours + 0.5f) * 10).toInt() / 10f
                                            if (intervalHours > 24f) intervalHours = 24f
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Increase",
                                        tint = primaryColor
                                    )
                                }
                            }
                            
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Stunden"
                                    "Spanish" -> "horas"
                                    else -> "hours"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Start hour selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Ab"
                                    "Spanish" -> "Desde"
                                    else -> "From"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val totalMin = intervalStartHour * 60 + intervalStartMinute - 30
                                        val wrapped = if (totalMin < 0) totalMin + 1440 else totalMin
                                        intervalStartHour = (wrapped / 60) % 24
                                        intervalStartMinute = wrapped % 60
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Decrease",
                                        tint = primaryColor
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(56.dp, 36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(primaryColor.copy(alpha = 0.1f))
                                        .clickable {
                                            selectedHour = intervalStartHour
                                            selectedMinute = intervalStartMinute
                                            showIntervalTimePicker = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = String.format("%02d:%02d", intervalStartHour, intervalStartMinute),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                }
                                
                                IconButton(
                                    onClick = {
                                        val totalMin = intervalStartHour * 60 + intervalStartMinute + 30
                                        intervalStartHour = (totalMin / 60) % 24
                                        intervalStartMinute = totalMin % 60
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Increase",
                                        tint = primaryColor
                                    )
                                }
                            }
                            
                            Text(
                                text = when (selectedLanguage) {
                                    "German" -> "Stunden"
                                    "Spanish" -> "horas"
                                    else -> "hours"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Transparent
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Preview generated times
                        val previewTimes = generateIntervalTimes(intervalHours, intervalStartHour, intervalStartMinute)
                        Text(
                            text = previewTimes.joinToString("  ·  ") { it.toString() },
                            fontSize = 12.sp,
                            color = onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(surfaceVariant.copy(alpha = 0.3f))
                                .padding(8.dp)
                        )
                    } else {
                        // === MANUAL MODE ===
                        // Add time button — full width, centered
                        Surface(
                            onClick = {
                                val calendar = Calendar.getInstance()
                                selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
                                selectedMinute = calendar.get(Calendar.MINUTE)
                                editingTimeIndex = -1
                                showTimePicker = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = primaryColor.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = primaryColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Zeit hinzufügen"
                                        "Spanish" -> "Agregar hora"
                                        else -> "Add time"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = primaryColor
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        if (times.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(surfaceVariant.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Tippe +, um Zeiten hinzuzufügen"
                                        "Spanish" -> "Toca + para agregar horarios"
                                        else -> "Tap + to add times"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            // Time chips in a wrapping flow layout
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                times.forEachIndexed { index, time ->
                                    Surface(
                                        onClick = {
                                            selectedHour = time.hour
                                            selectedMinute = time.minute
                                            editingTimeIndex = index
                                            showTimePicker = true
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = primaryColor.copy(alpha = 0.08f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Rounded.AccessTime,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = primaryColor
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = time.toString(),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            IconButton(
                                                onClick = { timeToDelete = time },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    modifier = Modifier.size(14.dp),
                                                    tint = onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // === NOTIFICATION TYPE SECTION ===
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Benachrichtigung"
                            "Spanish" -> "Notificación"
                            else -> "Notification"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isSilent = !hasSound && !hasVibration
                        
                        // Silent button
                        Surface(
                            onClick = {
                                hasSound = false
                                hasVibration = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSilent) primaryColor.copy(alpha = 0.15f)
                                else surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Rounded.NotificationsOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isSilent) primaryColor else onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Lautlos"
                                        "Spanish" -> "Silencio"
                                        else -> "Silent"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = if (isSilent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSilent) primaryColor else onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                        
                        // Sound button
                        Surface(
                            onClick = {
                                val newSoundState = !hasSound
                                
                                // Check if user is trying to enable sound
                                if (newSoundState) {
                                    val (soundEnabled, vibrationEnabled) = NotificationHelper.getChannelSettings(context)
                                    if (!soundEnabled) {
                                        // System has sound disabled - show warning
                                        warningMessage = when (selectedLanguage) {
                                            "German" -> "Ton ist in den Android-Einstellungen deaktiviert. Bitte aktivieren Sie:\n\n1. App-Benachrichtigungen (falls deaktiviert)\n2. Ton für 'Zeitbasierte Erinnerungen'\n\nMöchten Sie die Einstellungen öffnen?"
                                            "Spanish" -> "El sonido está desactivado en la configuración de Android. Por favor, active:\n\n1. Notificaciones de la aplicación (si están desactivadas)\n2. Sonido para 'Recordatorios basados en tiempo'\n\n¿Desea abrir la configuración?"
                                            else -> "Sound is disabled in Android settings. Please enable:\n\n1. App notifications (if disabled)\n2. Sound for 'Time-Based Reminders'\n\nWould you like to open settings?"
                                        }
                                        showSystemSettingsWarning = true
                                        return@Surface
                                    }
                                }
                                
                                hasSound = newSoundState
                                if (hasSound || hasVibration) { /* at least one active */ }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (hasSound) primaryColor.copy(alpha = 0.15f)
                                else surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (hasSound) primaryColor else onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Ton"
                                        "Spanish" -> "Sonido"
                                        else -> "Sound"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = if (hasSound) FontWeight.Bold else FontWeight.Normal,
                                    color = if (hasSound) primaryColor else onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                        
                        // Vibration button
                        Surface(
                            onClick = {
                                val newVibrationState = !hasVibration
                                
                                // Check if user is trying to enable vibration
                                if (newVibrationState) {
                                    val (soundEnabled, vibrationEnabled) = NotificationHelper.getChannelSettings(context)
                                    if (!vibrationEnabled) {
                                        // System has vibration disabled - show warning
                                        warningMessage = when (selectedLanguage) {
                                            "German" -> "Vibration ist in den Android-Einstellungen deaktiviert. Bitte aktivieren Sie:\n\n1. App-Benachrichtigungen (falls deaktiviert)\n2. Vibration für 'Zeitbasierte Erinnerungen'\n\nMöchten Sie die Einstellungen öffnen?"
                                            "Spanish" -> "La vibración está desactivada en la configuración de Android. Por favor, active:\n\n1. Notificaciones de la aplicación (si están desactivadas)\n2. Vibración para 'Recordatorios basados en tiempo'\n\n¿Desea abrir la configuración?"
                                            else -> "Vibration is disabled in Android settings. Please enable:\n\n1. App notifications (if disabled)\n2. Vibration for 'Time-Based Reminders'\n\nWould you like to open settings?"
                                        }
                                        showSystemSettingsWarning = true
                                        return@Surface
                                    }
                                }
                                
                                hasVibration = newVibrationState
                                if (hasSound || hasVibration) { /* at least one active */ }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (hasVibration) primaryColor.copy(alpha = 0.15f)
                                else surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Rounded.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (hasVibration) primaryColor else onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Vibration"
                                        "Spanish" -> "Vibración"
                                        else -> "Vibration"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = if (hasVibration) FontWeight.Bold else FontWeight.Normal,
                                    color = if (hasVibration) primaryColor else onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    
                    if (hasSound) {
                        val context = LocalContext.current
                        Surface(
                            onClick = {
                                val intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    android.content.Intent(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                                        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        putExtra(android.provider.Settings.EXTRA_CHANNEL_ID, "time_pattern_reminders")
                                    }
                                } else {
                                    android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra("app_package", context.packageName)
                                        putExtra("app_uid", context.applicationInfo.uid)
                                    }
                                }
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = primaryColor.copy(alpha = 0.08f),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = primaryColor.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Ton anpassen…"
                                        "Spanish" -> "Personalizar sonido…"
                                        else -> "Customize sound…"
                                    },
                                    fontSize = 11.sp,
                                    color = primaryColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // === CONFIRMATION TRACKING SECTION ===
                Surface(
                    onClick = { requiresConfirmation = !requiresConfirmation },
                    shape = RoundedCornerShape(12.dp),
                    color = if (requiresConfirmation) primaryColor.copy(alpha = 0.12f)
                        else surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (requiresConfirmation) primaryColor else onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Bestätigung erforderlich"
                                        "Spanish" -> "Requiere confirmación"
                                        else -> "Requires confirmation"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (requiresConfirmation) primaryColor
                                        else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Aufgabe als erledigt/nicht erledigt markieren"
                                        "Spanish" -> "Marcar tarea como hecha o no"
                                        else -> "Track if task was done or not"
                                    },
                                    fontSize = 10.sp,
                                    color = onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            Switch(
                                checked = requiresConfirmation,
                                onCheckedChange = { requiresConfirmation = it },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        // Smart Alerts integrated sub-row
                        if (requiresConfirmation) {
                            val hasActiveRules = confirmationAlertSettings.alertOnNoStreak ||
                                    confirmationAlertSettings.alertOnYesStreak ||
                                    confirmationAlertSettings.alertOnMissed
                            HorizontalDivider(
                                color = if (hasActiveRules) primaryColor.copy(alpha = 0.15f)
                                    else onSurfaceVariant.copy(alpha = 0.12f),
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                            Surface(
                                onClick = { showAlertSettingsDialog = true },
                                color = Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        start = 44.dp, end = 14.dp,
                                        top = 8.dp, bottom = 10.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.NotificationsActive,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (hasActiveRules) primaryColor
                                            else onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (selectedLanguage) {
                                            "German" -> "Intelligente Warnungen"
                                            "Spanish" -> "Alertas inteligentes"
                                            else -> "Smart Alerts"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (hasActiveRules) primaryColor
                                            else onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (hasActiveRules) {
                                        val count = listOf(
                                            confirmationAlertSettings.alertOnNoStreak,
                                            confirmationAlertSettings.alertOnYesStreak,
                                            confirmationAlertSettings.alertOnMissed
                                        ).count { it }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = primaryColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "$count",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = primaryColor,
                                                modifier = Modifier.padding(
                                                    horizontal = 5.dp, vertical = 1.dp
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = "›",
                                        fontSize = 14.sp,
                                        color = onSurfaceVariant.copy(alpha = 0.35f)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // === BOTTOM ACTION BUTTONS ===
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
                            val finalTimes = if (useInterval) {
                                generateIntervalTimes(intervalHours, intervalStartHour, intervalStartMinute)
                            } else {
                                times.sortedBy { it.hour * 60 + it.minute }
                            }
                            if (name.isNotBlank() && finalTimes.isNotEmpty() && selectedDays.isNotEmpty()) {
                                val derivedType = when {
                                    hasSound && hasVibration -> NotificationType.SOUND_VIBRATION
                                    hasSound || hasVibration -> NotificationType.DEFAULT
                                    else -> NotificationType.SILENT
                                }
                                val newReminder = CustomReminder(
                                    id = reminder?.id ?: java.util.UUID.randomUUID().toString(),
                                    emoji = emoji,
                                    name = name,
                                    notificationType = derivedType,
                                    hasSound = hasSound,
                                    hasVibration = hasVibration,
                                    daysOfWeek = selectedDays,
                                    times = finalTimes,
                                    intervalHours = if (useInterval) intervalHours else 0f,
                                    intervalStartHour = if (useInterval) intervalStartHour else 8,
                                    intervalStartMinute = if (useInterval) intervalStartMinute else 0,
                                    isEnabled = reminder?.isEnabled ?: true,
                                    requiresConfirmation = requiresConfirmation,
                                    confirmationAlertSettings = confirmationAlertSettings,
                                    createdAt = reminder?.createdAt ?: System.currentTimeMillis()
                                )
                                onSave(newReminder)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        enabled = name.isNotBlank() && selectedDays.isNotEmpty() && (useInterval || times.isNotEmpty())
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
    
    // Delete time confirmation dialog
    timeToDelete?.let { time ->
        Dialog(onDismissRequest = { timeToDelete = null }) {
            Card(
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "${time} entfernen?"
                            "Spanish" -> "¿Eliminar ${time}?"
                            else -> "Remove ${time}?"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { timeToDelete = null },
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
                                times = times.filter { it != time }
                                timeToDelete = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                when (selectedLanguage) {
                                    "German" -> "Entfernen"
                                    "Spanish" -> "Eliminar"
                                    else -> "Remove"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Smart Alerts settings dialog
    if (showAlertSettingsDialog) {
        ConfirmationAlertSettingsDialog(
            settings = confirmationAlertSettings,
            selectedLanguage = selectedLanguage,
            onSave = { newSettings ->
                confirmationAlertSettings = newSettings
                showAlertSettingsDialog = false
            },
            onDismiss = { showAlertSettingsDialog = false }
        )
    }
    
    // System settings warning dialog
    if (showSystemSettingsWarning) {
        AlertDialog(
            onDismissRequest = { showSystemSettingsWarning = false },
            title = {
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Systemeinstellungen erforderlich"
                        "Spanish" -> "Configuración del sistema requerida"
                        else -> "System Settings Required"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(warningMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSystemSettingsWarning = false
                        // Open Android notification settings
                        val intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            android.content.Intent(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                putExtra(android.provider.Settings.EXTRA_CHANNEL_ID, "time_pattern_reminders")
                            }
                        } else {
                            android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.parse("package:${context.packageName}")
                            }
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        when (selectedLanguage) {
                            "German" -> "Einstellungen öffnen"
                            "Spanish" -> "Abrir configuración"
                            else -> "Open Settings"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSystemSettingsWarning = false }) {
                    Text(
                        when (selectedLanguage) {
                            "German" -> "Abbrechen"
                            "Spanish" -> "Cancelar"
                            else -> "Cancel"
                        }
                    )
                }
            }
        )
    }

    // Emoji picker dialog
    if (showEmojiPicker) {
        EmojiPickerDialog(
            selectedLanguage = selectedLanguage,
            onDismiss = { showEmojiPicker = false },
            onEmojiSelected = { selectedEmoji ->
                emoji = selectedEmoji
                showEmojiPicker = false
            }
        )
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = if (editingTimeIndex >= 0) {
                            when (selectedLanguage) {
                                "German" -> "Zeit bearbeiten"
                                "Spanish" -> "Editar hora"
                                else -> "Edit Time"
                            }
                        } else {
                            when (selectedLanguage) {
                                "German" -> "Zeit auswählen"
                                "Spanish" -> "Seleccionar hora"
                                else -> "Select Time"
                            }
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Hour and Minute Pickers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour Picker
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            var editingHour by remember { mutableStateOf(false) }
                            var hourText by remember { mutableStateOf("") }
                            val hourFocusRequester = remember { FocusRequester() }

                            IconButton(onClick = { 
                                selectedHour = (selectedHour + 1) % 24
                            }) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Increase hour",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (editingHour) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    )
                                    .then(
                                        if (!editingHour) Modifier.clickable {
                                            hourText = String.format("%02d", selectedHour)
                                            editingHour = true
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (editingHour) {
                                    LaunchedEffect(Unit) { hourFocusRequester.requestFocus() }
                                    BasicTextField(
                                        value = hourText,
                                        onValueChange = { v ->
                                            val filtered = v.filter { it.isDigit() }.take(2)
                                            hourText = filtered
                                            filtered.toIntOrNull()?.let { n ->
                                                if (n in 0..23) selectedHour = n
                                            }
                                        },
                                        modifier = Modifier
                                            .focusRequester(hourFocusRequester)
                                            .width(56.dp),
                                        textStyle = TextStyle(
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                    DisposableEffect(Unit) {
                                        onDispose {
                                            val n = hourText.toIntOrNull()
                                            if (n != null && n in 0..23) selectedHour = n
                                            editingHour = false
                                        }
                                    }
                                } else {
                                    Text(
                                        text = String.format("%02d", selectedHour),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            
                            IconButton(onClick = { 
                                selectedHour = if (selectedHour == 0) 23 else selectedHour - 1
                            }) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Decrease hour",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Text(
                            text = ":",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        
                        // Minute Picker
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            var editingMinute by remember { mutableStateOf(false) }
                            var minuteText by remember { mutableStateOf("") }
                            val minuteFocusRequester = remember { FocusRequester() }

                            IconButton(onClick = { 
                                selectedMinute = (selectedMinute + 1) % 60
                            }) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Increase minute",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (editingMinute) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    )
                                    .then(
                                        if (!editingMinute) Modifier.clickable {
                                            minuteText = String.format("%02d", selectedMinute)
                                            editingMinute = true
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (editingMinute) {
                                    LaunchedEffect(Unit) { minuteFocusRequester.requestFocus() }
                                    BasicTextField(
                                        value = minuteText,
                                        onValueChange = { v ->
                                            val filtered = v.filter { it.isDigit() }.take(2)
                                            minuteText = filtered
                                            filtered.toIntOrNull()?.let { n ->
                                                if (n in 0..59) selectedMinute = n
                                            }
                                        },
                                        modifier = Modifier
                                            .focusRequester(minuteFocusRequester)
                                            .width(56.dp),
                                        textStyle = TextStyle(
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                    DisposableEffect(Unit) {
                                        onDispose {
                                            val n = minuteText.toIntOrNull()
                                            if (n != null && n in 0..59) selectedMinute = n
                                            editingMinute = false
                                        }
                                    }
                                } else {
                                    Text(
                                        text = String.format("%02d", selectedMinute),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            
                            IconButton(onClick = { 
                                selectedMinute = if (selectedMinute == 0) 59 else selectedMinute - 1
                            }) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Decrease minute",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showTimePicker = false },
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
                                val newTime = ReminderTime(selectedHour, selectedMinute)
                                if (editingTimeIndex >= 0) {
                                    val mutableTimes = times.toMutableList()
                                    mutableTimes[editingTimeIndex] = newTime
                                    times = mutableTimes.distinctBy { it.hour * 60 + it.minute }
                                        .sortedBy { it.hour * 60 + it.minute }
                                } else {
                                    if (!times.contains(newTime)) {
                                        times = (times + newTime).sortedBy { it.hour * 60 + it.minute }
                                    }
                                }
                                editingTimeIndex = -1
                                showTimePicker = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
    
    // Interval Start Time Picker Dialog
    if (showIntervalTimePicker) {
        Dialog(onDismissRequest = { showIntervalTimePicker = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (selectedLanguage) {
                            "German" -> "Startzeit"
                            "Spanish" -> "Hora de inicio"
                            else -> "Start Time"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            var editingHour2 by remember { mutableStateOf(false) }
                            var hourText2 by remember { mutableStateOf("") }
                            val hourFocusReq2 = remember { FocusRequester() }

                            IconButton(onClick = { selectedHour = (selectedHour + 1) % 24 }) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase hour", tint = MaterialTheme.colorScheme.primary)
                            }
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (editingHour2) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    )
                                    .then(
                                        if (!editingHour2) Modifier.clickable {
                                            hourText2 = String.format("%02d", selectedHour)
                                            editingHour2 = true
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (editingHour2) {
                                    LaunchedEffect(Unit) { hourFocusReq2.requestFocus() }
                                    BasicTextField(
                                        value = hourText2,
                                        onValueChange = { v ->
                                            val filtered = v.filter { it.isDigit() }.take(2)
                                            hourText2 = filtered
                                            filtered.toIntOrNull()?.let { n -> if (n in 0..23) selectedHour = n }
                                        },
                                        modifier = Modifier.focusRequester(hourFocusReq2).width(56.dp),
                                        textStyle = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                    DisposableEffect(Unit) {
                                        onDispose {
                                            val n = hourText2.toIntOrNull()
                                            if (n != null && n in 0..23) selectedHour = n
                                            editingHour2 = false
                                        }
                                    }
                                } else {
                                    Text(String.format("%02d", selectedHour), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            IconButton(onClick = { selectedHour = if (selectedHour == 0) 23 else selectedHour - 1 }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease hour", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        
                        Text(":", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            var editingMinute2 by remember { mutableStateOf(false) }
                            var minuteText2 by remember { mutableStateOf("") }
                            val minuteFocusReq2 = remember { FocusRequester() }

                            IconButton(onClick = { selectedMinute = (selectedMinute + 1) % 60 }) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase minute", tint = MaterialTheme.colorScheme.primary)
                            }
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (editingMinute2) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    )
                                    .then(
                                        if (!editingMinute2) Modifier.clickable {
                                            minuteText2 = String.format("%02d", selectedMinute)
                                            editingMinute2 = true
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (editingMinute2) {
                                    LaunchedEffect(Unit) { minuteFocusReq2.requestFocus() }
                                    BasicTextField(
                                        value = minuteText2,
                                        onValueChange = { v ->
                                            val filtered = v.filter { it.isDigit() }.take(2)
                                            minuteText2 = filtered
                                            filtered.toIntOrNull()?.let { n -> if (n in 0..59) selectedMinute = n }
                                        },
                                        modifier = Modifier.focusRequester(minuteFocusReq2).width(56.dp),
                                        textStyle = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                    DisposableEffect(Unit) {
                                        onDispose {
                                            val n = minuteText2.toIntOrNull()
                                            if (n != null && n in 0..59) selectedMinute = n
                                            editingMinute2 = false
                                        }
                                    }
                                } else {
                                    Text(String.format("%02d", selectedMinute), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            IconButton(onClick = { selectedMinute = if (selectedMinute == 0) 59 else selectedMinute - 1 }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease minute", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showIntervalTimePicker = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(when (selectedLanguage) { "German" -> "Abbrechen"; "Spanish" -> "Cancelar"; else -> "Cancel" })
                        }
                        Button(
                            onClick = {
                                intervalStartHour = selectedHour
                                intervalStartMinute = selectedMinute
                                showIntervalTimePicker = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmojiPickerDialog(
    selectedLanguage: String,
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    val emojis = listOf(
        "⏰", "💧", "💊", "🏃", "🩸", "🍎", "🥗", "🧘", "😴", "📝",
        "🚶", "🏋️", "🧴", "🦷", "👁️", "🫀", "🧠", "💉", "🌡️", "⚕️",
        "🍵", "☕", "🥤", "🍊", "🥑", "🥦", "🥕", "🫐", "🍇", "🍌",
        "📊", "📈", "📉", "✅", "❌", "⚠️", "🔔", "🔕", "📱", "⌚"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = when (selectedLanguage) {
                        "German" -> "Emoji auswählen"
                        "Spanish" -> "Seleccionar emoji"
                        else -> "Select Emoji"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(emojis) { emoji ->
                        TextButton(
                            onClick = { onEmojiSelected(emoji) },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Text(
                                text = emoji,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun generateIntervalTimes(intervalHours: Float, startHour: Int, startMinute: Int = 0): List<ReminderTime> {
    if (intervalHours <= 0f) return emptyList()
    val intervalMinutes = (intervalHours * 60).toInt()
    if (intervalMinutes <= 0) return emptyList()
    val result = mutableListOf<ReminderTime>()
    val startTotal = startHour * 60 + startMinute
    var currentTotal = startTotal
    while (true) {
        val h = (currentTotal % 1440 + 1440) % 1440 / 60
        val m = (currentTotal % 1440 + 1440) % 1440 % 60
        result.add(ReminderTime(h, m))
        currentTotal += intervalMinutes
        if ((currentTotal % 1440 + 1440) % 1440 == startTotal || result.size >= 48) break
    }
    return result.sortedBy { it.hour * 60 + it.minute }
}
