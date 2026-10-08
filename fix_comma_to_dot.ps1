# Read the file
$content = Get-Content 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt' -Raw

# Find and replace the onValueChange handler to normalize comma to dot
$pattern = 'onValueChange = \{ newValue ->\s+// Allow normal editing - confirmation dialog is now handled on focus\s+onValueChange\(newValue\)'
$replacement = 'onValueChange = { newValue ->
                            // Allow normal editing - confirmation dialog is now handled on focus
                            // Normalize comma to dot for decimal input (supports both European and US formats)
                            val normalizedText = newValue.text.replace('','', ''.'')
                            val normalizedValue = newValue.copy(text = normalizedText)
                            onValueChange(normalizedValue)'

$content = $content -replace $pattern, $replacement

# Write back
Set-Content 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt' $content

Write-Host "Added comma-to-dot normalization for decimal input!"
