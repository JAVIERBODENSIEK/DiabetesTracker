# Fix auto-backup UI issues: text wrapping, clickable path, reset button

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Fixing auto-backup UI issues..."

# Fix 1: Prevent text wrapping in frequency chips by adding softWrap = false
$pattern1 = @'
                                        FilterChip\(
                                            selected = autoBackupFrequency == freq,
                                            onClick = \{
                                                autoBackupFrequency = freq
                                                SettingsManager\.saveAutoBackupFrequency\(context, freq\)
                                            \},
                                            label = \{ Text\(label\) \},
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
                                            label = { Text(label, maxLines = 1, softWrap = false) },
                                            modifier = Modifier.weight(1f)
                                        )
'@

if ($content -match $pattern1) {
    $content = $content -replace $pattern1, $replacement1
    Write-Host "Fix 1: Added softWrap = false to frequency chips - SUCCESS"
} else {
    Write-Host "Fix 1: Pattern not found - SKIPPED"
}

# Fix 2 & 3: Make path clickable and replace hint with reset button
$pattern2 = @'
                                Text\(
                                    text = autoBackupPath,
                                    style = MaterialTheme\.typography\.bodySmall,
                                    color = MaterialTheme\.colorScheme\.primary,
                                    modifier = Modifier
                                        \.fillMaxWidth\(\)
                                        \.padding\(vertical = 4\.dp\)
                                        \.background\(
                                            MaterialTheme\.colorScheme\.primaryContainer\.copy\(alpha = 0\.3f\),
                                            RoundedCornerShape\(4\.dp\)
                                        \)
                                        \.padding\(8\.dp\)
                                \)
                                
                                // Note about path \(Downloads is default and recommended\)
                                Text\(
                                    text = when \(selectedLanguage\) \{
                                        "German" -> "📁 Standard: Downloads-Ordner \(empfohlen\)"
                                        "Spanish" -> "📁 Predeterminado: Carpeta Descargas \(recomendado\)"
                                        else -> "📁 Default: Downloads folder \(recommended\)"
                                    \},
                                    style = MaterialTheme\.typography\.bodySmall,
                                    color = MaterialTheme\.colorScheme\.onSurface\.copy\(alpha = 0\.6f\),
                                    modifier = Modifier\.padding\(top = 4\.dp\)
                                \)
'@

$replacement2 = @'
                                Text(
                                    text = autoBackupPath,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // TODO: Open folder picker
                                            // For now, show a toast that this feature is coming
                                            android.widget.Toast.makeText(
                                                context,
                                                when (selectedLanguage) {
                                                    "German" -> "Ordnerauswahl wird in zukünftiger Version verfügbar sein"
                                                    "Spanish" -> "La selección de carpeta estará disponible en una versión futura"
                                                    else -> "Folder selection will be available in a future version"
                                                },
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .padding(vertical = 4.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(8.dp)
                                )
                                
                                // Reset to default button
                                Button(
                                    onClick = {
                                        val defaultPath = android.os.Environment.getExternalStoragePublicDirectory(
                                            android.os.Environment.DIRECTORY_DOWNLOADS
                                        ).absolutePath
                                        autoBackupPath = defaultPath
                                        SettingsManager.saveAutoBackupPath(context, defaultPath)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text(
                                        text = when (selectedLanguage) {
                                            "German" -> "Auf Standard zurücksetzen"
                                            "Spanish" -> "Restablecer a predeterminado"
                                            else -> "Reset to Default"
                                        }
                                    )
                                }
'@

if ($content -match $pattern2) {
    $content = $content -replace $pattern2, $replacement2
    Write-Host "Fix 2 & 3: Made path clickable and added reset button - SUCCESS"
} else {
    Write-Host "Fix 2 & 3: Pattern not found - SKIPPED"
}

# Write back
Set-Content $file $content

Write-Host "`nAll fixes applied!"
Write-Host "Note: Folder picker functionality marked as TODO for future implementation."
