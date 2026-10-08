# PowerShell script to implement complete auto-backup on exit feature

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Implementing auto-backup on exit feature..."

# Step 1: Add autoBackupOnExit variable in DiabetesTrackerContent
$pattern1 = '    var autoSaveOnExit by remember \{ mutableStateOf\(SettingsManager\.loadAutoSaveOnExit\(context\)\) \}'
$replacement1 = @'
    var autoSaveOnExit by remember { mutableStateOf(SettingsManager.loadAutoSaveOnExit(context)) }
    var autoBackupOnExit by remember { mutableStateOf(SettingsManager.loadAutoBackupOnExit(context)) }
'@

if ($content -match [regex]::Escape($pattern1)) {
    $content = $content -replace [regex]::Escape($pattern1), $replacement1
    Write-Host "Step 1: Added autoBackupOnExit variable - SUCCESS"
} else {
    Write-Host "Step 1: Pattern not found - SKIPPED"
}

# Step 2: Add auto-backup logic in BackHandler (when exiting app)
$pattern2 = '                // Second press: save \(if needed\) and exit\s+if \(hasUnsavedChanges\) \{\s+DataManager\.saveEntries\(context, entries\)'
$replacement2 = @'
                // Second press: save (if needed) and exit
                if (hasUnsavedChanges) {
                    DataManager.saveEntries(context, entries)
                }
                
                // Auto-backup on exit if enabled
                if (autoBackupOnExit) {
                    try {
                        val backupData = BackupManager.createBackup(context, BackupType.ALL)
                        val timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
                        val backupFileName = "auto_backup_$timestamp.json"
                        val backupFile = java.io.File(context.getExternalFilesDir(null), backupFileName)
                        val backupUri = android.net.Uri.fromFile(backupFile)
                        BackupManager.exportBackupToFile(context, backupData, backupUri)
                    } catch (e: Exception) {
                        // Silent fail - don't interrupt app exit
                    }
                }
                
                if (hasUnsavedChanges) {
'@

if ($content -match $pattern2) {
    $content = $content -replace $pattern2, $replacement2
    Write-Host "Step 2: Added auto-backup logic in BackHandler - SUCCESS"
} else {
    Write-Host "Step 2: Pattern not found - SKIPPED"
}

# Step 3: Add UI toggle in AdvancedSettingsDialog
$pattern3 = '    var autoSaveOnExit by remember \{ mutableStateOf\(SettingsManager\.loadAutoSaveOnExit\(context\)\) \}\s+var useSymbolsInCharts'
$replacement3 = @'
    var autoSaveOnExit by remember { mutableStateOf(SettingsManager.loadAutoSaveOnExit(context)) }
    var autoBackupOnExit by remember { mutableStateOf(SettingsManager.loadAutoBackupOnExit(context)) }
    var useSymbolsInCharts
'@

if ($content -match [regex]::Escape('    var autoSaveOnExit by remember { mutableStateOf(SettingsManager.loadAutoSaveOnExit(context)) }')) {
    $content = $content -replace '    var autoSaveOnExit by remember \{ mutableStateOf\(SettingsManager\.loadAutoSaveOnExit\(context\)\) \}\s+var useSymbolsInCharts', $replacement3
    Write-Host "Step 3: Added autoBackupOnExit variable in AdvancedSettingsDialog - SUCCESS"
} else {
    Write-Host "Step 3: Pattern not found - SKIPPED"
}

# Step 4: Add the UI toggle after autoSaveOnExit toggle
$pattern4 = '                \}\s+\}\s+HorizontalDivider\(modifier = Modifier\.padding\(vertical = 12\.dp\)\)\s+// Day symbols vs written date toggle'
$replacement4 = @'
                }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Auto-backup on exit toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            autoBackupOnExit = !autoBackupOnExit
                            SettingsManager.saveAutoBackupOnExit(context, autoBackupOnExit)
                        }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Auto-Backup beim Beenden"
                                "Spanish" -> "Copia de seguridad automática al salir"
                                else -> "Auto-Backup on Exit"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = when (selectedLanguage) {
                                "German" -> "Erstellt automatisch ein Backup beim Schließen der App"
                                "Spanish" -> "Crea automáticamente una copia de seguridad al cerrar la aplicación"
                                else -> "Automatically creates a backup when closing the app"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = autoBackupOnExit,
                        onCheckedChange = { 
                            autoBackupOnExit = it
                            SettingsManager.saveAutoBackupOnExit(context, autoBackupOnExit)
                        }
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Day symbols vs written date toggle
'@

if ($content -match $pattern4) {
    $content = $content -replace $pattern4, $replacement4
    Write-Host "Step 4: Added auto-backup UI toggle - SUCCESS"
} else {
    Write-Host "Step 4: Pattern not found - SKIPPED"
}

# Write back
Set-Content $file $content

Write-Host "`nAuto-backup on exit feature implementation complete!"
Write-Host "Please rebuild the app to see the changes."
