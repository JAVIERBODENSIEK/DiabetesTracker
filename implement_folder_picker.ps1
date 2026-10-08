# Implement proper folder picker for auto-backup path selection

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Implementing folder picker for auto-backup path..."

# Step 1: Add folder picker launcher in AdvancedSettingsDialog
$pattern1 = '    var autoBackupPath by remember \{ mutableStateOf\(SettingsManager\.loadAutoBackupPath\(context\)\) \}\s+    var useSymbolsInCharts'

$replacement1 = @'
    var autoBackupPath by remember { mutableStateOf(SettingsManager.loadAutoBackupPath(context)) }
    
    // Folder picker launcher for auto-backup path
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                // Get the actual file path from URI
                val path = uri.path?.let { uriPath ->
                    // Extract the path after "primary:" or "tree/primary:"
                    when {
                        uriPath.contains("primary:") -> {
                            val afterPrimary = uriPath.substringAfter("primary:")
                            "/storage/emulated/0/$afterPrimary"
                        }
                        else -> uriPath
                    }
                } ?: uri.toString()
                
                autoBackupPath = path
                SettingsManager.saveAutoBackupPath(context, path)
                
                android.widget.Toast.makeText(
                    context,
                    when (selectedLanguage) {
                        "German" -> "Pfad aktualisiert: $path"
                        "Spanish" -> "Ruta actualizada: $path"
                        else -> "Path updated: $path"
                    },
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    context,
                    when (selectedLanguage) {
                        "German" -> "Fehler beim Auswählen des Ordners"
                        "Spanish" -> "Error al seleccionar carpeta"
                        else -> "Error selecting folder"
                    },
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    var useSymbolsInCharts
'@

if ($content -match $pattern1) {
    $content = $content -replace $pattern1, $replacement1
    Write-Host "Step 1: Added folder picker launcher - SUCCESS"
} else {
    Write-Host "Step 1: Pattern not found - SKIPPED"
}

# Step 2: Update path click to launch folder picker
$pattern2 = @'
                                        \.clickable \{
                                            // Show info that Downloads is the recommended location
                                            android\.widget\.Toast\.makeText\(
                                                context,
                                                when \(selectedLanguage\) \{
                                                    "German" -> "Downloads-Ordner ist der empfohlene Speicherort"
                                                    "Spanish" -> "La carpeta Descargas es la ubicación recomendada"
                                                    else -> "Downloads folder is the recommended location"
                                                \},
                                                android\.widget\.Toast\.LENGTH_SHORT
                                            \)\.show\(\)
                                        \}
'@

$replacement2 = @'
                                        .clickable {
                                            // Launch folder picker
                                            folderPickerLauncher.launch(null)
                                        }
'@

if ($content -match $pattern2) {
    $content = $content -replace $pattern2, $replacement2
    Write-Host "Step 2: Updated path click to launch folder picker - SUCCESS"
} else {
    Write-Host "Step 2: Pattern not found - SKIPPED"
}

# Step 3: Add import for ActivityResultContracts at the top of the file
if ($content -notmatch 'import androidx\.activity\.compose\.rememberLauncherForActivityResult') {
    # Find the imports section and add the new import
    $importPattern = '(import androidx\.compose\.runtime\.\*)'
    $importReplacement = @'
$1
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
'@
    
    if ($content -match $importPattern) {
        $content = $content -replace $importPattern, $importReplacement
        Write-Host "Step 3: Added required imports - SUCCESS"
    } else {
        Write-Host "Step 3: Import pattern not found - SKIPPED (may already exist)"
    }
}

# Write back
Set-Content $file $content

Write-Host "`nFolder picker implementation complete!"
Write-Host "Users can now tap the path to select a custom backup folder."
