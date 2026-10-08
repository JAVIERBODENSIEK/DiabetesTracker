# Add ExposedDropdownMenu imports

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Adding dropdown menu imports..."

# Add imports after material3 imports
$pattern = 'import androidx\.compose\.material3\.\*'
$replacement = @'
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Added ExposedDropdownMenu imports - SUCCESS"
    Set-Content $file $content
} else {
    Write-Host "Pattern not found - imports may already exist"
}

Write-Host "Done!"
