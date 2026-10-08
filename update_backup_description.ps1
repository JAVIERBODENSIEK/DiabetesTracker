# Update the UI description to mention Downloads folder

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Updating auto-backup description..."

# Update the description text
$pattern = @'
                            text = when \(selectedLanguage\) \{
                                "German" -> "Erstellt automatisch ein Backup beim Schließen der App"
                                "Spanish" -> "Crea automáticamente una copia de seguridad al cerrar la aplicación"
                                else -> "Automatically creates a backup when closing the app"
                            \},
'@

$replacement = @'
                            text = when (selectedLanguage) {
                                "German" -> "Erstellt automatisch ein Backup im Downloads-Ordner beim Schließen der App"
                                "Spanish" -> "Crea automáticamente una copia de seguridad en la carpeta Descargas al cerrar la aplicación"
                                else -> "Automatically creates a backup in Downloads folder when closing the app"
                            },
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Updated description - SUCCESS"
    Set-Content $file $content
} else {
    Write-Host "Pattern not found - FAILED"
}

Write-Host "Done!"
