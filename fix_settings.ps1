# Read the file
$content = Get-Content 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt' -Raw

# Replace all instances of creating new AppSettings with loading existing and copying
$pattern1 = 'SettingsManager\.saveSettings\(context, AppSettings\('
$replacement1 = 'val tempExisting = SettingsManager.loadSettings(context); SettingsManager.saveSettings(context, tempExisting.copy('

$content = $content -replace $pattern1, $replacement1

# Write back
Set-Content 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt' $content

Write-Host "Settings save calls have been fixed to preserve customization flags!"
