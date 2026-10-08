# Enhance auto-backup with "Every Exit" frequency and custom/infinity count options

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Enhancing backup frequency and count options..."

# Step 1: Update frequency chips to include "Every Exit" option
$pattern1 = @'
                                Row\(
                                    modifier = Modifier\.fillMaxWidth\(\),
                                    horizontalArrangement = Arrangement\.spacedBy\(8\.dp\)
                                \) \{
                                    listOf\("Daily", "Weekly", "Monthly"\)\.forEach \{ freq ->
'@

$replacement1 = @'
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Every Exit", "Daily", "Weekly", "Monthly").forEach { freq ->
'@

if ($content -match $pattern1) {
    $content = $content -replace $pattern1, $replacement1
    Write-Host "Step 1: Added 'Every Exit' frequency option - SUCCESS"
} else {
    Write-Host "Step 1: Pattern not found - SKIPPED"
}

# Step 2: Update frequency labels to include "Every Exit"
$pattern2 = @'
                                        val label = when \(freq\) \{
                                            "Daily" -> when \(settings\.language\) \{
                                                "German" -> "Täglich"
                                                "Spanish" -> "Diario"
                                                else -> "Daily"
                                            \}
                                            "Weekly" -> when \(settings\.language\) \{
                                                "German" -> "Wöchentlich"
                                                "Spanish" -> "Semanal"
                                                else -> "Weekly"
                                            \}
                                            "Monthly" -> when \(settings\.language\) \{
                                                "German" -> "Monatlich"
                                                "Spanish" -> "Mensual"
                                                else -> "Monthly"
                                            \}
                                            else -> freq
                                        \}
'@

$replacement2 = @'
                                        val label = when (freq) {
                                            "Every Exit" -> when (settings.language) {
                                                "German" -> "Jedes Mal"
                                                "Spanish" -> "Cada vez"
                                                else -> "Every Exit"
                                            }
                                            "Daily" -> when (settings.language) {
                                                "German" -> "Täglich"
                                                "Spanish" -> "Diario"
                                                else -> "Daily"
                                            }
                                            "Weekly" -> when (settings.language) {
                                                "German" -> "Wöchentlich"
                                                "Spanish" -> "Semanal"
                                                else -> "Weekly"
                                            }
                                            "Monthly" -> when (settings.language) {
                                                "German" -> "Monatlich"
                                                "Spanish" -> "Mensual"
                                                else -> "Monthly"
                                            }
                                            else -> freq
                                        }
'@

if ($content -match $pattern2) {
    $content = $content -replace $pattern2, $replacement2
    Write-Host "Step 2: Added 'Every Exit' label translations - SUCCESS"
} else {
    Write-Host "Step 2: Pattern not found - SKIPPED"
}

# Step 3: Replace slider with custom input and infinity option
$pattern3 = @'
                                // Max Backup Count
                                Text\(
                                    text = when \(settings\.language\) \{
                                        "German" -> "Maximale Anzahl Backups: \$autoBackupMaxCount"
                                        "Spanish" -> "Número máximo de copias: \$autoBackupMaxCount"
                                        else -> "Max Backup Count: \$autoBackupMaxCount"
                                    \},
                                    style = MaterialTheme\.typography\.titleSmall
                                \)
                                Text\(
                                    text = when \(settings\.language\) \{
                                        "German" -> "Älteste Backups werden automatisch gelöscht"
                                        "Spanish" -> "Las copias más antiguas se eliminan automáticamente"
                                        else -> "Oldest backups are automatically deleted"
                                    \},
                                    style = MaterialTheme\.typography\.bodySmall,
                                    color = MaterialTheme\.colorScheme\.onSurface\.copy\(alpha = 0\.6f\),
                                    modifier = Modifier\.padding\(bottom = 8\.dp\)
                                \)
                                
                                Slider\(
                                    value = autoBackupMaxCount\.toFloat\(\),
                                    onValueChange = \{ 
                                        autoBackupMaxCount = it\.toInt\(\)
                                        SettingsManager\.saveAutoBackupMaxCount\(context, autoBackupMaxCount\)
                                    \},
                                    valueRange = 1f\.\.5f,
                                    steps = 3,
                                    modifier = Modifier\.fillMaxWidth\(\)
                                \)
'@

$replacement3 = @'
                                // Max Backup Count
                                Text(
                                    text = when (settings.language) {
                                        "German" -> "Maximale Anzahl Backups"
                                        "Spanish" -> "Número máximo de copias"
                                        else -> "Max Backup Count"
                                    },
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = when (settings.language) {
                                        "German" -> if (autoBackupMaxCount == 0) "Unbegrenzt - Keine Backups werden gelöscht" else "Älteste Backups werden automatisch gelöscht"
                                        "Spanish" -> if (autoBackupMaxCount == 0) "Ilimitado - No se eliminan copias" else "Las copias más antiguas se eliminan automáticamente"
                                        else -> if (autoBackupMaxCount == 0) "Unlimited - No backups will be deleted" else "Oldest backups are automatically deleted"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = if (autoBackupMaxCount == 0) "∞" else autoBackupMaxCount.toString(),
                                        onValueChange = { newValue ->
                                            if (newValue == "∞" || newValue.isEmpty()) {
                                                autoBackupMaxCount = 0
                                                SettingsManager.saveAutoBackupMaxCount(context, 0)
                                            } else {
                                                newValue.toIntOrNull()?.let { count ->
                                                    if (count >= 0) {
                                                        autoBackupMaxCount = count
                                                        SettingsManager.saveAutoBackupMaxCount(context, count)
                                                    }
                                                }
                                            }
                                        },
                                        label = { 
                                            Text(
                                                when (settings.language) {
                                                    "German" -> "Anzahl"
                                                    "Spanish" -> "Cantidad"
                                                    else -> "Count"
                                                }
                                            ) 
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Button(
                                        onClick = {
                                            autoBackupMaxCount = 0
                                            SettingsManager.saveAutoBackupMaxCount(context, 0)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = when (settings.language) {
                                                "German" -> "Unbegrenzt"
                                                "Spanish" -> "Ilimitado"
                                                else -> "Unlimited"
                                            }
                                        )
                                    }
                                }
'@

if ($content -match $pattern3) {
    $content = $content -replace $pattern3, $replacement3
    Write-Host "Step 3: Replaced slider with custom input and infinity button - SUCCESS"
} else {
    Write-Host "Step 3: Pattern not found - SKIPPED"
}

# Write back
Set-Content $file $content

Write-Host "`nEnhancements complete!"
Write-Host "- Added 'Every Exit' frequency option"
Write-Host "- Added custom number input for max count"
Write-Host "- Added 'Unlimited' button (infinity option)"
