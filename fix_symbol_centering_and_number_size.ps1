# Fix infinity symbol centering and auto-size numbers

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Fixing symbol centering and number auto-sizing..."

# Find and replace the entire OutlinedTextField for count
$pattern = @'
                                    val focusManager = LocalFocusManager\.current
                                    
                                    OutlinedTextField\(
                                        value = if \(autoBackupMaxCount == 0\) "" else autoBackupMaxCount\.toString\(\),
                                        onValueChange = \{ newValue ->
                                            if \(newValue\.isEmpty\(\)\) \{
                                                // Empty = unlimited \(show ∞\)
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
                                            Box\(
                                                modifier = Modifier\.fillMaxSize\(\),
                                                contentAlignment = Alignment\.Center
                                            \) \{
                                                Text\(
                                                    "∞",
                                                    style = MaterialTheme\.typography\.displayLarge\.copy\(
                                                        fontSize = 48\.sp,
                                                        fontWeight = FontWeight\.ExtraBold,
                                                        brush = Brush\.linearGradient\(
                                                            colors = listOf\(
                                                                Color\(0xFF00BCD4\), // Cyan
                                                                Color\(0xFF2196F3\), // Blue
                                                                Color\(0xFF9C27B0\)  // Purple
                                                            \)
                                                        \)
                                                    \),
                                                    textAlign = TextAlign\.Center
                                                \)
                                            \}
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
                                        textStyle = MaterialTheme\.typography\.headlineSmall\.copy\(
                                            textAlign = TextAlign\.Center,
                                            fontSize = 24\.sp,
                                            fontWeight = FontWeight\.Bold
                                        \),
                                        modifier = Modifier
                                            \.width\(80\.dp\)
                                            \.height\(56\.dp\)
                                    \)
'@

$replacement = @'
                                    val focusManager = LocalFocusManager.current
                                    
                                    Box(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(56.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (autoBackupMaxCount == 0) {
                                            // Show infinity symbol when unlimited
                                            Text(
                                                "∞",
                                                style = MaterialTheme.typography.displayLarge.copy(
                                                    fontSize = 42.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(
                                                            Color(0xFF00BCD4), // Cyan
                                                            Color(0xFF2196F3), // Blue
                                                            Color(0xFF9C27B0)  // Purple
                                                        )
                                                    ),
                                                    platformStyle = PlatformTextStyle(
                                                        includeFontPadding = false
                                                    )
                                                ),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.offset(y = (-2).dp)
                                            )
                                        }
                                        
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
                                            textStyle = MaterialTheme.typography.displayMedium.copy(
                                                textAlign = TextAlign.Center,
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                platformStyle = PlatformTextStyle(
                                                    includeFontPadding = false
                                                )
                                            ),
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
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp),
                                            decorationBox = { innerTextField ->
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    innerTextField()
                                                }
                                            }
                                        )
                                    }
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Fixed symbol centering and number sizing - SUCCESS"
} else {
    Write-Host "Pattern not found - FAILED"
}

# Write back
Set-Content $file $content

Write-Host "`nDone!"
Write-Host "- Infinity symbol: Centered with offset, no font padding"
Write-Host "- Numbers: Auto-sized (32sp), centered, no font padding"
