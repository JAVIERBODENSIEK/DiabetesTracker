# Fix selectedLanguage reference in folder picker

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Fixing language reference in folder picker..."

# Replace selectedLanguage with settings.language
$pattern = @'
                android\.widget\.Toast\.makeText\(
                    context,
                    when \(selectedLanguage\) \{
                        "German" -> "Pfad aktualisiert: \$path"
                        "Spanish" -> "Ruta actualizada: \$path"
                        else -> "Path updated: \$path"
                    \},
                    android\.widget\.Toast\.LENGTH_SHORT
                \)\.show\(\)
            \} catch \(e: Exception\) \{
                android\.widget\.Toast\.makeText\(
                    context,
                    when \(selectedLanguage\) \{
                        "German" -> "Fehler beim Auswählen des Ordners"
                        "Spanish" -> "Error al seleccionar carpeta"
                        else -> "Error selecting folder"
                    \},
'@

$replacement = @'
                android.widget.Toast.makeText(
                    context,
                    when (settings.language) {
                        "German" -> "Pfad aktualisiert: $path"
                        "Spanish" -> "Ruta actualizada: $path"
                        else -> "Path updated: $path"
                    },
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    context,
                    when (settings.language) {
                        "German" -> "Fehler beim Auswählen des Ordners"
                        "Spanish" -> "Error al seleccionar carpeta"
                        else -> "Error selecting folder"
                    },
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Fixed language reference - SUCCESS"
    Set-Content $file $content
} else {
    Write-Host "Pattern not found - FAILED"
}

Write-Host "Done!"
