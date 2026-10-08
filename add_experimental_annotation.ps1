# Add experimental annotation for ExposedDropdownMenu

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Adding experimental annotation..."

# Add @OptIn annotation to AdvancedSettingsDialog function
$pattern = '@Composable\s+fun AdvancedSettingsDialog\('
$replacement = @'
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsDialog(
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Added @OptIn annotation - SUCCESS"
    Set-Content $file $content
} else {
    Write-Host "Pattern not found - FAILED"
}

Write-Host "Done!"
