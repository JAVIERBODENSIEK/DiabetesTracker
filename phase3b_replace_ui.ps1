# Phase 3b: Replace simple toggle with expandable UI

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw
$uiCode = Get-Content 'phase3b_expandable_ui.kt' -Raw

Write-Host "Replacing simple toggle with expandable UI..."

# Find and replace the simple auto-backup toggle
$pattern = @'
                // Auto-backup on exit toggle
                Row\(
                    modifier = Modifier
                        \.fillMaxWidth\(\)
                        \.clickable \{
                            autoBackupOnExit = !autoBackupOnExit
                            SettingsManager\.saveAutoBackupOnExit\(context, autoBackupOnExit\)
                        \}
                        \.padding\(vertical = 8\.dp\),
                    horizontalArrangement = Arrangement\.SpaceBetween,
                    verticalAlignment = Alignment\.CenterVertically
                \) \{
                    Column\(modifier = Modifier\.weight\(1f\)\) \{
                        Text\(
                            text = when \(selectedLanguage\) \{
                                "German" -> "Auto-Backup beim Beenden"
                                "Spanish" -> "Copia de seguridad automática al salir"
                                else -> "Auto-Backup on Exit"
                            \},
                            style = MaterialTheme\.typography\.bodyMedium
                        \)
                        Text\(
                            text = when \(selectedLanguage\) \{
                                "German" -> "Erstellt automatisch ein Backup im Downloads-Ordner beim Schließen der App"
                                "Spanish" -> "Crea automáticamente una copia de seguridad en la carpeta Descargas al cerrar la aplicación"
                                else -> "Automatically creates a backup in Downloads folder when closing the app"
                            \},
                            style = MaterialTheme\.typography\.bodySmall,
                            color = MaterialTheme\.colorScheme\.onSurface\.copy\(alpha = 0\.7f\)
                        \)
                    \}
                    Switch\(
                        checked = autoBackupOnExit,
                        onCheckedChange = \{ 
                            autoBackupOnExit = it
                            SettingsManager\.saveAutoBackupOnExit\(context, autoBackupOnExit\)
                        \}
                    \)
                \}
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $uiCode
    Write-Host "Replaced simple toggle with expandable UI - SUCCESS"
    Set-Content $file $content
} else {
    Write-Host "Pattern not found - FAILED"
    Write-Host "The UI section may need manual adjustment."
}

Write-Host "`nPhase 3b complete."
