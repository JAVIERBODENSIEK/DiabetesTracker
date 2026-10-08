# Add LocalFocusManager import

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Adding LocalFocusManager import..."

# Add import after other platform imports
$pattern = 'import androidx\.compose\.ui\.platform\.LocalContext'
$replacement = @'
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Added LocalFocusManager import - SUCCESS"
    Set-Content $file $content
} else {
    Write-Host "Pattern not found - import may already exist"
}

Write-Host "Done!"
