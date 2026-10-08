# Fix cursor blinking after pressing Enter - should clear focus

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Fixing cursor behavior on Enter key..."

# Add focus manager and keyboard actions to clear focus on Enter
$pattern = @'
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
                                                    style = MaterialTheme\.typography\.headlineLarge\.copy\(
                                                        fontSize = 36\.sp,
                                                        fontWeight = FontWeight\.Bold
                                                    \),
                                                    color = MaterialTheme\.colorScheme\.primary,
                                                    textAlign = TextAlign\.Center
                                                \)
                                            \}
                                        \},
                                        keyboardOptions = KeyboardOptions\(keyboardType = KeyboardType\.Number\),
'@

$replacement = @'
                                    val focusManager = LocalFocusManager.current
                                    
                                    OutlinedTextField(
                                        value = if (autoBackupMaxCount == 0) "" else autoBackupMaxCount.toString(),
                                        onValueChange = { newValue ->
                                            if (newValue.isEmpty()) {
                                                // Empty = unlimited (show ∞)
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
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "∞",
                                                    style = MaterialTheme.typography.headlineLarge.copy(
                                                        fontSize = 36.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
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
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Added focus clearing on Enter/Done - SUCCESS"
} else {
    Write-Host "Pattern not found - FAILED"
}

# Write back
Set-Content $file $content

Write-Host "`nDone!"
Write-Host "Cursor will now disappear when Enter/Done is pressed"
