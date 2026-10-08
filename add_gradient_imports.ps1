# Add Brush and Color imports for gradient

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Adding gradient imports..."

# Add imports after other graphics imports
$pattern = 'import androidx\.compose\.ui\.graphics\.\*'
$replacement = @'
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Added Brush and Color imports - SUCCESS"
    Set-Content $file $content
} else {
    Write-Host "Pattern not found - imports may already exist"
}

Write-Host "Done!"
