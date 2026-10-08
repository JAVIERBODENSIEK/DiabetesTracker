# Fix compilation errors - use correct TextStyle and BasicTextField syntax

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Fixing compilation errors..."

# Step 1: Fix the conflicting TextStyle import
$pattern1 = @'
import androidx\.compose\.ui\.text\.TextStyle
import androidx\.compose\.ui\.text\.PlatformTextStyle
'@

$replacement1 = @'
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign as ComposeTextAlign
'@

if ($content -match $pattern1) {
    $content = $content -replace $pattern1, $replacement1
    Write-Host "Step 1: Fixed TextStyle import - SUCCESS"
} else {
    Write-Host "Step 1: Pattern not found - SKIPPED"
}

# Step 2: Fix BasicTextField to use correct Compose TextStyle
$pattern2 = @'
                                        BasicTextField\(
                                            value = if \(autoBackupMaxCount == 0\) "" else autoBackupMaxCount\.toString\(\),
                                            onValueChange = \{ newValue ->
                                                if \(newValue\.isEmpty\(\)\) \{
                                                    autoBackupMaxCount = 0
                                                    SettingsManager\.saveAutoBackupMaxCount\(context, 0\)
                                                \} else \{
                                                    newValue\.toIntOrNull\(\)\?\.let \{ count ->
                                                        if \(count > 0\) \{
                                                            autoBackupMaxCount = count
                                                            SettingsManager\.saveAutoBackupMaxCount\(context, count\)
                                                        \}
                                                    \}
                                                \}
                                            \},
                                            textStyle = MaterialTheme\.typography\.displayMedium\.copy\(
                                                textAlign = TextAlign\.Center,
                                                fontSize = 32\.sp,
                                                fontWeight = FontWeight\.Bold,
                                                color = MaterialTheme\.colorScheme\.onSurface,
                                                platformStyle = PlatformTextStyle\(
                                                    includeFontPadding = false
                                                \)
                                            \),
'@

$replacement2 = @'
                                        BasicTextField(
                                            value = if (autoBackupMaxCount == 0) "" else autoBackupMaxCount.toString(),
                                            onValueChange = { newValue ->
                                                if (newValue.isEmpty()) {
                                                    autoBackupMaxCount = 0
                                                    SettingsManager.saveAutoBackupMaxCount(context, 0)
                                                } else {
                                                    newValue.toIntOrNull()?.let { count ->
                                                        if (count > 0) {
                                                            autoBackupMaxCount = count
                                                            SettingsManager.saveAutoBackupMaxCount(context, count)
                                                        }
                                                    }
                                                }
                                            },
                                            textStyle = ComposeTextStyle(
                                                textAlign = ComposeTextAlign.Center,
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                platformStyle = PlatformTextStyle(
                                                    includeFontPadding = false
                                                )
                                            ),
'@

if ($content -match $pattern2) {
    $content = $content -replace $pattern2, $replacement2
    Write-Host "Step 2: Fixed BasicTextField textStyle - SUCCESS"
} else {
    Write-Host "Step 2: Pattern not found - SKIPPED"
}

# Write back
Set-Content $file $content

Write-Host "`nDone!"
Write-Host "Fixed TextStyle conflicts and BasicTextField syntax"
