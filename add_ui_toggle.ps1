# Add the UI toggle for auto-backup

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Adding auto-backup UI toggle..."

# Add the toggle between autoSaveOnExit and Day symbols section
$pattern = '                \}\s+                \s+                HorizontalDivider\(modifier = Modifier\.padding\(vertical = 12\.dp\)\)\s+                \s+                // Day symbols vs written date toggle with nested symbol style selector'
$replacement = @'
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
                
                // Day symbols vs written date toggle with nested symbol style selector
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Added auto-backup UI toggle - SUCCESS"
    Set-Content $file $content
} else {
    Write-Host "Pattern not found - FAILED"
    Write-Host "Trying alternative pattern..."
    
    # Try simpler pattern
    $simplePattern = 'HorizontalDivider\(modifier = Modifier\.padding\(vertical = 12\.dp\)\)\s+                \s+                // Day symbols vs written date toggle with nested symbol style selector'
    if ($content -match $simplePattern) {
        $simpleReplacement = @'
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
                
                // Day symbols vs written date toggle with nested symbol style selector
'@
        $content = $content -replace $simplePattern, $simpleReplacement
        Write-Host "Added auto-backup UI toggle with alternative pattern - SUCCESS"
        Set-Content $file $content
    } else {
        Write-Host "Alternative pattern also not found - FAILED"
    }
}

Write-Host "`nDone!"
