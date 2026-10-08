# Fix count field with simpler approach - use OutlinedTextField with proper centering

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Fixing count field with simpler approach..."

# Step 1: Remove the bad import line
$content = $content -replace 'import androidx\.compose\.ui\.text\.style\.TextAlign as ComposeTextAlign as ComposeTextStyle', ''
$content = $content -replace 'import androidx\.compose\.ui\.text\.style\.TextAlign as ComposeTextAlign', ''

# Step 2: Replace the entire Box/BasicTextField section with OutlinedTextField
$pattern = @'
                                    Box\(
                                        modifier = Modifier
                                            \.width\(80\.dp\)
                                            \.height\(56\.dp\),
                                        contentAlignment = Alignment\.Center
                                    \) \{
                                        if \(autoBackupMaxCount == 0\) \{
                                            // Show infinity symbol when unlimited
                                            Text\(
                                                "∞",
                                                style = MaterialTheme\.typography\.displayLarge\.copy\(
                                                    fontSize = 42\.sp,
                                                    fontWeight = FontWeight\.ExtraBold,
                                                    brush = Brush\.linearGradient\(
                                                        colors = listOf\(
                                                            Color\(0xFF00BCD4\), // Cyan
                                                            Color\(0xFF2196F3\), // Blue
                                                            Color\(0xFF9C27B0\)  // Purple
                                                        \)
                                                    \),
                                                    platformStyle = PlatformTextStyle\(
                                                        includeFontPadding = false
                                                    \)
                                                \),
                                                textAlign = TextAlign\.Center,
                                                modifier = Modifier\.offset\(y = \(-2\)\.dp\)
                                            \)
                                        \}
                                        
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
                                            textStyle = ComposeTextStyle\(
                                                textAlign = ComposeTextAlign\.Center,
                                                fontSize = 32\.sp,
                                                fontWeight = FontWeight\.Bold,
                                                color = MaterialTheme\.colorScheme\.onSurface,
                                                platformStyle = PlatformTextStyle\(
                                                    includeFontPadding = false
                                                \)
                                            \),
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
                                            modifier = Modifier
                                                \.fillMaxWidth\(\)
                                                \.padding\(horizontal = 8\.dp\),
                                            decorationBox = \{ innerTextField ->
                                                Box\(
                                                    modifier = Modifier\.fillMaxSize\(\),
                                                    contentAlignment = Alignment\.Center
                                                \) \{
                                                    innerTextField\(\)
                                                \}
                                            \}
                                        \)
                                    \}
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
                                                style = MaterialTheme.typography.displayLarge.copy(
                                                    fontSize = 40.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(
                                                            Color(0xFF00BCD4),
                                                            Color(0xFF2196F3),
                                                            Color(0xFF9C27B0)
                                                        )
                                                    ),
                                                    platformStyle = PlatformTextStyle(
                                                        includeFontPadding = false
                                                    )
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
                                        textStyle = MaterialTheme.typography.displayMedium.copy(
                                            textAlign = TextAlign.Center,
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            platformStyle = PlatformTextStyle(
                                                includeFontPadding = false
                                            )
                                        ),
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(56.dp)
                                    )
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Replaced with OutlinedTextField - SUCCESS"
} else {
    Write-Host "Pattern not found - FAILED"
}

# Write back
Set-Content $file $content

Write-Host "`nDone!"
Write-Host "Using OutlinedTextField with proper centering and no font padding"
