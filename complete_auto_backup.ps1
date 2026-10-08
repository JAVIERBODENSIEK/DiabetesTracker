# Complete the auto-backup implementation

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Completing auto-backup implementation..."

# Add autoBackupOnExit variable in main DiabetesTrackerContent
$pattern1 = '    var autoSaveOnExit by remember \{ mutableStateOf\(SettingsManager\.loadAutoSaveOnExit\(context\)\) \}\s+var showExitSnackbar'
$replacement1 = @'
    var autoSaveOnExit by remember { mutableStateOf(SettingsManager.loadAutoSaveOnExit(context)) }
    var autoBackupOnExit by remember { mutableStateOf(SettingsManager.loadAutoBackupOnExit(context)) }
    var showExitSnackbar
'@

if ($content -match $pattern1) {
    $content = $content -replace $pattern1, $replacement1
    Write-Host "Added autoBackupOnExit variable in DiabetesTrackerContent - SUCCESS"
} else {
    Write-Host "autoBackupOnExit variable pattern not found - SKIPPED"
}

# Add UI toggle in AdvancedSettingsDialog (after autoSaveOnExit toggle)
$pattern2 = '                    \}\s+                \}\s+                \s+                HorizontalDivider\(modifier = Modifier\.padding\(vertical = 12\.dp\)\)\s+                \s+                // Day symbols'
$replacement2 = @'
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
                
                // Day symbols
'@

if ($content -match $pattern2) {
    $content = $content -replace $pattern2, $replacement2
    Write-Host "Added auto-backup UI toggle - SUCCESS"
} else {
    Write-Host "UI toggle pattern not found - SKIPPED"
}

# Update the refresh logic in showAdvancedSettings dialog close
$pattern3 = '                autoSaveOnExit = SettingsManager\.loadAutoSaveOnExit\(context\)\s+            \}\s+        \)\s+    \}'
$replacement3 = @'
                autoSaveOnExit = SettingsManager.loadAutoSaveOnExit(context)
                autoBackupOnExit = SettingsManager.loadAutoBackupOnExit(context)
            }
        )
    }
'@

if ($content -match $pattern3) {
    $content = $content -replace $pattern3, $replacement3
    Write-Host "Updated refresh logic for autoBackupOnExit - SUCCESS"
} else {
    Write-Host "Refresh logic pattern not found - SKIPPED"
}

# Write back
Set-Content $file $content

Write-Host "`nAuto-backup feature implementation complete!"
