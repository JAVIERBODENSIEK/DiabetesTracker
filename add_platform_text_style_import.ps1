# Add PlatformTextStyle import

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Adding PlatformTextStyle import..."

# Add import after other text imports
$pattern = 'import androidx\.compose\.ui\.text\.TextStyle'
$replacement = @'
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Added PlatformTextStyle import - SUCCESS"
    Set-Content $file $content
} else {
    Write-Host "Pattern not found"
}

Write-Host "Done!"
