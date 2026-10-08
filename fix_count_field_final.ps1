# Fix count field to match standard text field size with proper text sizing

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Fixing count field to match standard text fields..."

# Replace the OutlinedTextField with properly sized version
$pattern = @'
                                    OutlinedTextField\(
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
                                        placeholder = \{ 
                                            Text\(
                                                "∞",
                                                style = MaterialTheme\.typography\.displayLarge\.copy\(
                                                    fontSize = 40\.sp,
                                                    fontWeight = FontWeight\.ExtraBold,
                                                    brush = Brush\.linearGradient\(
                                                        colors = listOf\(
                                                            Color\(0xFF00BCD4\),
                                                            Color\(0xFF2196F3\),
                                                            Color\(0xFF9C27B0\)
                                                        \)
                                                    \),
                                                    platformStyle = PlatformTextStyle\(
                                                        includeFontPadding = false
                                                    \)
                                                \),
                                                textAlign = TextAlign\.Center,
                                                modifier = Modifier\.fillMaxWidth\(\)
                                            \)
                                        \},
                                        keyboardOptions = KeyboardOptions\(
                                            keyboardType = KeyboardType\.Number,
                                            imeAction = ImeAction\.Done
                                        \),
                                        keyboardActions = KeyboardActions\(
                                            onDone = \{
                                                focusManager\.clearFocus\(\)
                                            \}
                                        \),
                                        singleLine = true,
                                        textStyle = MaterialTheme\.typography\.displayMedium\.copy\(
                                            textAlign = TextAlign\.Center,
                                            fontSize = 32\.sp,
                                            fontWeight = FontWeight\.Bold,
                                            platformStyle = PlatformTextStyle\(
                                                includeFontPadding = false
                                            \)
                                        \),
                                        modifier = Modifier
                                            \.width\(80\.dp\)
                                            \.height\(56\.dp\)
                                    \)
'@

$replacement = @'
                                    OutlinedTextField(
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
                                        placeholder = { 
                                            Text(
                                                "∞",
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(
                                                            Color(0xFF00BCD4),
                                                            Color(0xFF2196F3),
                                                            Color(0xFF9C27B0)
                                                        )
                                                    ),
                                                    platformStyle = PlatformTextStyle(
                                                        includeFontPadding = false
                                                    ),
                                                    baselineShift = androidx.compose.ui.text.style.BaselineShift(-0.1f)
                                                ),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                focusManager.clearFocus()
                                            }
                                        ),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            textAlign = TextAlign.Center,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Normal,
                                            platformStyle = PlatformTextStyle(
                                                includeFontPadding = false
                                            )
                                        ),
                                        modifier = Modifier.width(70.dp)
                                    )
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Fixed count field sizing - SUCCESS"
} else {
    Write-Host "Pattern not found - FAILED"
}

# Write back
Set-Content $file $content

Write-Host "`nDone!"
Write-Host "- Box width: 70dp (matches other fields)"
Write-Host "- Box height: Standard (auto from OutlinedTextField)"
Write-Host "- Infinity: 20sp, centered with baseline shift"
Write-Host "- Numbers: 16sp, normal weight"
