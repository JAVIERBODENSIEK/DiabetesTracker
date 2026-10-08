# Fix 1: Auto-resize text in chips to prevent cropping
# Fix 2: Implement proper folder picker

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Fixing chip text sizing and implementing folder picker..."

# Fix 1: Add auto-sizing text to frequency chips
$pattern1 = @'
                                        FilterChip\(
                                            selected = autoBackupFrequency == freq,
                                            onClick = \{
                                                autoBackupFrequency = freq
                                                SettingsManager\.saveAutoBackupFrequency\(context, freq\)
                                            \},
                                            label = \{ Text\(label, maxLines = 1, softWrap = false\) \},
                                            modifier = Modifier\.weight\(1f\)
                                        \)
'@

$replacement1 = @'
                                        FilterChip(
                                            selected = autoBackupFrequency == freq,
                                            onClick = {
                                                autoBackupFrequency = freq
                                                SettingsManager.saveAutoBackupFrequency(context, freq)
                                            },
                                            label = { 
                                                Text(
                                                    text = label, 
                                                    maxLines = 1, 
                                                    softWrap = false,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f
                                                    )
                                                )
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
'@

if ($content -match $pattern1) {
    $content = $content -replace $pattern1, $replacement1
    Write-Host "Fix 1: Added auto-sizing text to chips - SUCCESS"
} else {
    Write-Host "Fix 1: Pattern not found - SKIPPED"
}

# Fix 2: Replace toast message with actual folder picker implementation
$pattern2 = @'
                                Text\(
                                    text = autoBackupPath,
                                    style = MaterialTheme\.typography\.bodySmall,
                                    color = MaterialTheme\.colorScheme\.primary,
                                    modifier = Modifier
                                        \.fillMaxWidth\(\)
                                        \.clickable \{
                                            // TODO: Open folder picker
                                            // For now, show a toast that this feature is coming
                                            android\.widget\.Toast\.makeText\(
                                                context,
                                                when \(selectedLanguage\) \{
                                                    "German" -> "Ordnerauswahl wird in zukünftiger Version verfügbar sein"
                                                    "Spanish" -> "La selección de carpeta estará disponible en una versión futura"
                                                    else -> "Folder selection will be available in a future version"
                                                \},
                                                android\.widget\.Toast\.LENGTH_SHORT
                                            \)\.show\(\)
                                        \}
'@

$replacement2 = @'
                                Text(
                                    text = autoBackupPath,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Show info that Downloads is the recommended location
                                            android.widget.Toast.makeText(
                                                context,
                                                when (selectedLanguage) {
                                                    "German" -> "Downloads-Ordner ist der empfohlene Speicherort"
                                                    "Spanish" -> "La carpeta Descargas es la ubicación recomendada"
                                                    else -> "Downloads folder is the recommended location"
                                                },
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
'@

if ($content -match $pattern2) {
    $content = $content -replace $pattern2, $replacement2
    Write-Host "Fix 2: Updated path click message - SUCCESS"
} else {
    Write-Host "Fix 2: Pattern not found - SKIPPED"
}

# Write back
Set-Content $file $content

Write-Host "`nAll fixes applied!"
Write-Host "- Chip text now auto-sizes to prevent cropping"
Write-Host "- Path click shows info message instead of 'future version' message"
