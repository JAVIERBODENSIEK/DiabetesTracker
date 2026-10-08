# Read the file
$content = Get-Content 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt' -Raw

# Replace KeyboardType.Number with KeyboardType.Decimal for numeric input cells
$pattern = 'keyboardType = KeyboardType\.Number'
$replacement = 'keyboardType = KeyboardType.Decimal'

$content = $content -replace $pattern, $replacement

# Write back
Set-Content 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt' $content

Write-Host "Keyboard type changed from Number to Decimal to support comma and dot!"
