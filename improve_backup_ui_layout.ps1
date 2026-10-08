# Improve auto-backup UI layout and behavior

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Improving auto-backup UI layout..."

# Step 1: Separate "Every Exit" from other frequency options
$pattern1 = @'
                                Row\(
                                    modifier = Modifier\.fillMaxWidth\(\),
                                    horizontalArrangement = Arrangement\.spacedBy\(8\.dp\)
                                \) \{
                                    listOf\("Every Exit", "Daily", "Weekly", "Monthly"\)\.forEach \{ freq ->
'@

$replacement1 = @'
                                // "Every Exit" option - separate row
                                FilterChip(
                                    selected = autoBackupFrequency == "Every Exit",
                                    onClick = {
                                        autoBackupFrequency = "Every Exit"
                                        SettingsManager.saveAutoBackupFrequency(context, "Every Exit")
                                    },
                                    label = { 
                                        Text(
                                            text = when (selectedLanguage) {
                                                "German" -> "Jedes Mal beim Beenden"
                                                "Spanish" -> "Cada vez que se cierra"
                                                else -> "Every Time on Exit"
                                            },
                                            maxLines = 1,
                                            softWrap = false,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Other frequency options - single row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Daily", "Weekly", "Monthly").forEach { freq ->
'@

if ($content -match $pattern1) {
    $content = $content -replace $pattern1, $replacement1
    Write-Host "Step 1: Separated 'Every Exit' to its own row - SUCCESS"
} else {
    Write-Host "Step 1: Pattern not found - SKIPPED"
}

# Step 2: Remove "Every Exit" from the forEach loop label generation
$pattern2 = @'
                                        val label = when \(freq\) \{
                                            "Every Exit" -> when \(selectedLanguage\) \{
                                                "German" -> "Jedes Mal"
                                                "Spanish" -> "Cada vez"
                                                else -> "Every Exit"
                                            \}
                                            "Daily" -> when \(selectedLanguage\) \{
'@

$replacement2 = @'
                                        val label = when (freq) {
                                            "Daily" -> when (selectedLanguage) {
'@

if ($content -match $pattern2) {
    $content = $content -replace $pattern2, $replacement2
    Write-Host "Step 2: Removed 'Every Exit' from loop labels - SUCCESS"
} else {
    Write-Host "Step 2: Pattern not found - SKIPPED"
}

# Step 3: Improve Count input field size and unlimited behavior
$pattern3 = @'
                                Row\(
                                    modifier = Modifier\.fillMaxWidth\(\),
                                    horizontalArrangement = Arrangement\.spacedBy\(8\.dp\),
                                    verticalAlignment = Alignment\.CenterVertically
                                \) \{
                                    OutlinedTextField\(
                                        value = if \(autoBackupMaxCount == 0\) "∞" else autoBackupMaxCount\.toString\(\),
                                        onValueChange = \{ newValue ->
                                            if \(newValue == "∞" \|\| newValue\.isEmpty\(\)\) \{
                                                autoBackupMaxCount = 0
                                                SettingsManager\.saveAutoBackupMaxCount\(context, 0\)
                                            \} else \{
                                                newValue\.toIntOrNull\(\)\?\.let \{ count ->
                                                    if \(count >= 0\) \{
                                                        autoBackupMaxCount = count
                                                        SettingsManager\.saveAutoBackupMaxCount\(context, count\)
                                                    \}
                                                \}
                                            \}
                                        \},
                                        label = \{ 
                                            Text\(
                                                when \(selectedLanguage\) \{
                                                    "German" -> "Anzahl"
                                                    "Spanish" -> "Cantidad"
                                                    else -> "Count"
                                                \}
                                            \) 
                                        \},
                                        keyboardOptions = KeyboardOptions\(keyboardType = KeyboardType\.Number\),
                                        singleLine = true,
                                        modifier = Modifier\.weight\(1f\)
                                    \)
                                    
                                    Button\(
                                        onClick = \{
                                            autoBackupMaxCount = 0
                                            SettingsManager\.saveAutoBackupMaxCount\(context, 0\)
                                        \},
                                        modifier = Modifier\.weight\(1f\)
                                    \) \{
                                        Text\(
                                            text = when \(selectedLanguage\) \{
                                                "German" -> "Unbegrenzt"
                                                "Spanish" -> "Ilimitado"
                                                else -> "Unlimited"
                                            \}
                                        \)
                                    \}
                                \}
'@

$replacement3 = @'
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when (selectedLanguage) {
                                            "German" -> "Anzahl:"
                                            "Spanish" -> "Cantidad:"
                                            else -> "Count:"
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    
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
                                            Text(
                                                "∞",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            textAlign = TextAlign.Center
                                        ),
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(56.dp)
                                    )
                                }
'@

if ($content -match $pattern3) {
    $content = $content -replace $pattern3, $replacement3
    Write-Host "Step 3: Improved count input size and unlimited behavior - SUCCESS"
} else {
    Write-Host "Step 3: Pattern not found - SKIPPED"
}

# Write back
Set-Content $file $content

Write-Host "`nUI improvements complete!"
Write-Host "- 'Every Exit' now in separate row above other options"
Write-Host "- Count field reduced to appropriate size (100dp width)"
Write-Host "- ∞ appears automatically when field is empty"
