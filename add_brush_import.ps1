# Add Brush import

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Adding Brush import..."

# Add import after Color import
$pattern = 'import androidx\.compose\.ui\.graphics\.Color'
$replacement = @'
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Added Brush import - SUCCESS"
    Set-Content $file $content
} else {
    Write-Host "Pattern not found"
}

Write-Host "Done!"
