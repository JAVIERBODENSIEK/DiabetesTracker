# Fix frequency labels and count UI with correct variable names

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Fixing frequency labels and count UI..."

# Step 1: Add "Every Exit" to frequency labels
$pattern1 = @'
                                        val label = when \(freq\) \{
                                            "Daily" -> when \(selectedLanguage\) \{
'@

$replacement1 = @'
                                        val label = when (freq) {
                                            "Every Exit" -> when (selectedLanguage) {
                                                "German" -> "Jedes Mal"
                                                "Spanish" -> "Cada vez"
                                                else -> "Every Exit"
                                            }
                                            "Daily" -> when (selectedLanguage) {
'@

if ($content -match $pattern1) {
    $content = $content -replace $pattern1, $replacement1
    Write-Host "Step 1: Added 'Every Exit' label - SUCCESS"
} else {
    Write-Host "Step 1: Pattern not found - SKIPPED"
}

# Step 2: Find and replace the slider section
$pattern2 = 'Spacer\(modifier = Modifier\.height\(16\.dp\)\)\s+// Max Backup Count'

if ($content -match $pattern2) {
    Write-Host "Step 2: Found max backup count section"
    
    # Now find the full slider section
    $sliderPattern = @'
Spacer\(modifier = Modifier\.height\(16\.dp\)\)
                                
                                // Max Backup Count
                                Text\(
                                    text = when \(selectedLanguage\) \{
                                        "German" -> "Maximale Anzahl Backups: \$autoBackupMaxCount"
                                        "Spanish" -> "Número máximo de copias: \$autoBackupMaxCount"
                                        else -> "Max Backup Count: \$autoBackupMaxCount"
                                    \},
                                    style = MaterialTheme\.typography\.titleSmall
                                \)
                                Text\(
                                    text = when \(selectedLanguage\) \{
                                        "German" -> "Älteste Backups werden automatisch gelöscht"
                                        "Spanish" -> "Las copias más antiguas se eliminan automáticamente"
                                        else -> "Oldest backups are automatically deleted"
                                    \},
                                    style = MaterialTheme\.typography\.bodySmall,
                                    color = MaterialTheme\.colorScheme\.onSurface\.copy\(alpha = 0\.6f\),
                                    modifier = Modifier\.padding\(bottom = 8\.dp\)
                                \)
                                
                                Slider\(
                                    value = autoBackupMaxCount\.toFloat\(\),
                                    onValueChange = \{ 
                                        autoBackupMaxCount = it\.toInt\(\)
                                        SettingsManager\.saveAutoBackupMaxCount\(context, autoBackupMaxCount\)
                                    \},
                                    valueRange = 1f\.\.5f,
                                    steps = 3,
                                    modifier = Modifier\.fillMaxWidth\(\)
                                \)
'@

    $sliderReplacement = @'
Spacer(modifier = Modifier.height(16.dp))
                                
                                // Max Backup Count
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> "Maximale Anzahl Backups"
                                        "Spanish" -> "Número máximo de copias"
                                        else -> "Max Backup Count"
                                    },
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = when (selectedLanguage) {
                                        "German" -> if (autoBackupMaxCount == 0) "Unbegrenzt - Keine Backups werden gelöscht" else "Älteste Backups werden automatisch gelöscht"
                                        "Spanish" -> if (autoBackupMaxCount == 0) "Ilimitado - No se eliminan copias" else "Las copias más antiguas se eliminan automáticamente"
                                        else -> if (autoBackupMaxCount == 0) "Unlimited - No backups will be deleted" else "Oldest backups are automatically deleted"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = if (autoBackupMaxCount == 0) "∞" else autoBackupMaxCount.toString(),
                                        onValueChange = { newValue ->
                                            if (newValue == "∞" || newValue.isEmpty()) {
                                                autoBackupMaxCount = 0
                                                SettingsManager.saveAutoBackupMaxCount(context, 0)
                                            } else {
                                                newValue.toIntOrNull()?.let { count ->
                                                    if (count >= 0) {
                                                        autoBackupMaxCount = count
                                                        SettingsManager.saveAutoBackupMaxCount(context, count)
                                                    }
                                                }
                                            }
                                        },
                                        label = { 
                                            Text(
                                                when (selectedLanguage) {
                                                    "German" -> "Anzahl"
                                                    "Spanish" -> "Cantidad"
                                                    else -> "Count"
                                                }
                                            ) 
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Button(
                                        onClick = {
                                            autoBackupMaxCount = 0
                                            SettingsManager.saveAutoBackupMaxCount(context, 0)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = when (selectedLanguage) {
                                                "German" -> "Unbegrenzt"
                                                "Spanish" -> "Ilimitado"
                                                else -> "Unlimited"
                                            }
                                        )
                                    }
                                }
'@

    if ($content -match $sliderPattern) {
        $content = $content -replace $sliderPattern, $sliderReplacement
        Write-Host "Step 2: Replaced slider with custom input - SUCCESS"
    } else {
        Write-Host "Step 2: Slider pattern not found - SKIPPED"
    }
} else {
    Write-Host "Step 2: Max backup count section not found - SKIPPED"
}

# Write back
Set-Content $file $content

Write-Host "`nDone!"
