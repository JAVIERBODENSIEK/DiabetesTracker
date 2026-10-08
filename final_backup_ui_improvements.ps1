# Final UI improvements: dropdown for frequency, larger centered infinity symbol

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Applying final UI improvements..."

# Step 1: Replace frequency chips with dropdown menu
$pattern1 = @'
                                // "Every Exit" option - separate row
                                FilterChip\(
                                    selected = autoBackupFrequency == "Every Exit",
                                    onClick = \{
                                        autoBackupFrequency = "Every Exit"
                                        SettingsManager\.saveAutoBackupFrequency\(context, "Every Exit"\)
                                    \},
                                    label = \{ 
                                        Text\(
                                            text = when \(selectedLanguage\) \{
                                                "German" -> "Jedes Mal beim Beenden"
                                                "Spanish" -> "Cada vez que se cierra"
                                                else -> "Every Time on Exit"
                                            \},
                                            maxLines = 1,
                                            softWrap = false,
                                            style = MaterialTheme\.typography\.bodySmall\.copy\(
                                                fontSize = MaterialTheme\.typography\.bodySmall\.fontSize \* 0\.85f
                                            \)
                                        \)
                                    \},
                                    modifier = Modifier\.fillMaxWidth\(\)
                                \)
                                
                                Spacer\(modifier = Modifier\.height\(8\.dp\)\)
                                
                                // Other frequency options - single row
                                Row\(
                                    modifier = Modifier\.fillMaxWidth\(\),
                                    horizontalArrangement = Arrangement\.spacedBy\(8\.dp\)
                                \) \{
                                    listOf\("Daily", "Weekly", "Monthly"\)\.forEach \{ freq ->
                                        val label = when \(freq\) \{
                                            "Daily" -> when \(selectedLanguage\) \{
                                                "German" -> "Täglich"
                                                "Spanish" -> "Diario"
                                                else -> "Daily"
                                            \}
                                            "Weekly" -> when \(selectedLanguage\) \{
                                                "German" -> "Wöchentlich"
                                                "Spanish" -> "Semanal"
                                                else -> "Weekly"
                                            \}
                                            "Monthly" -> when \(selectedLanguage\) \{
                                                "German" -> "Monatlich"
                                                "Spanish" -> "Mensual"
                                                else -> "Monthly"
                                            \}
                                            else -> freq
                                        \}
                                        
                                        FilterChip\(
                                            selected = autoBackupFrequency == freq,
                                            onClick = \{
                                                autoBackupFrequency = freq
                                                SettingsManager\.saveAutoBackupFrequency\(context, freq\)
                                            \},
                                            label = \{ 
                                                Text\(
                                                    text = label, 
                                                    maxLines = 1, 
                                                    softWrap = false,
                                                    style = MaterialTheme\.typography\.bodySmall\.copy\(
                                                        fontSize = MaterialTheme\.typography\.bodySmall\.fontSize \* 0\.85f
                                                    \)
                                                \)
                                            \},
                                            modifier = Modifier\.weight\(1f\)
                                        \)
                                    \}
                                \}
'@

$replacement1 = @'
                                // Frequency dropdown menu
                                var expandedFrequency by remember { mutableStateOf(false) }
                                
                                ExposedDropdownMenuBox(
                                    expanded = expandedFrequency,
                                    onExpandedChange = { expandedFrequency = it }
                                ) {
                                    OutlinedTextField(
                                        value = when (autoBackupFrequency) {
                                            "Every Exit" -> when (selectedLanguage) {
                                                "German" -> "Jedes Mal beim Beenden"
                                                "Spanish" -> "Cada vez que se cierra"
                                                else -> "Every Time on Exit"
                                            }
                                            "Daily" -> when (selectedLanguage) {
                                                "German" -> "Täglich"
                                                "Spanish" -> "Diario"
                                                else -> "Daily"
                                            }
                                            "Weekly" -> when (selectedLanguage) {
                                                "German" -> "Wöchentlich"
                                                "Spanish" -> "Semanal"
                                                else -> "Weekly"
                                            }
                                            "Monthly" -> when (selectedLanguage) {
                                                "German" -> "Monatlich"
                                                "Spanish" -> "Mensual"
                                                else -> "Monthly"
                                            }
                                            else -> autoBackupFrequency
                                        },
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrequency) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )
                                    
                                    ExposedDropdownMenu(
                                        expanded = expandedFrequency,
                                        onDismissRequest = { expandedFrequency = false }
                                    ) {
                                        listOf("Every Exit", "Daily", "Weekly", "Monthly").forEach { freq ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        when (freq) {
                                                            "Every Exit" -> when (selectedLanguage) {
                                                                "German" -> "Jedes Mal beim Beenden"
                                                                "Spanish" -> "Cada vez que se cierra"
                                                                else -> "Every Time on Exit"
                                                            }
                                                            "Daily" -> when (selectedLanguage) {
                                                                "German" -> "Täglich"
                                                                "Spanish" -> "Diario"
                                                                else -> "Daily"
                                                            }
                                                            "Weekly" -> when (selectedLanguage) {
                                                                "German" -> "Wöchentlich"
                                                                "Spanish" -> "Semanal"
                                                                else -> "Weekly"
                                                            }
                                                            "Monthly" -> when (selectedLanguage) {
                                                                "German" -> "Monatlich"
                                                                "Spanish" -> "Mensual"
                                                                else -> "Monthly"
                                                            }
                                                            else -> freq
                                                        }
                                                    )
                                                },
                                                onClick = {
                                                    autoBackupFrequency = freq
                                                    SettingsManager.saveAutoBackupFrequency(context, freq)
                                                    expandedFrequency = false
                                                },
                                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                            )
                                        }
                                    }
                                }
'@

if ($content -match $pattern1) {
    $content = $content -replace $pattern1, $replacement1
    Write-Host "Step 1: Replaced chips with dropdown menu - SUCCESS"
} else {
    Write-Host "Step 1: Pattern not found - SKIPPED"
}

# Step 2: Fix infinity symbol size and centering
$pattern2 = @'
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
                                            Text\(
                                                "∞",
                                                style = MaterialTheme\.typography\.titleLarge,
                                                color = MaterialTheme\.colorScheme\.primary
                                            \)
                                        \},
                                        keyboardOptions = KeyboardOptions\(keyboardType = KeyboardType\.Number\),
                                        singleLine = true,
                                        textStyle = MaterialTheme\.typography\.bodyLarge\.copy\(
                                            textAlign = TextAlign\.Center
                                        \),
                                        modifier = Modifier
                                            \.width\(100\.dp\)
                                            \.height\(56\.dp\)
                                    \)
'@

$replacement2 = @'
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
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                                            textAlign = TextAlign.Center,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(56.dp)
                                    )
'@

if ($content -match $pattern2) {
    $content = $content -replace $pattern2, $replacement2
    Write-Host "Step 2: Fixed infinity symbol size and centering - SUCCESS"
} else {
    Write-Host "Step 2: Pattern not found - SKIPPED"
}

# Write back
Set-Content $file $content

Write-Host "`nFinal improvements complete!"
Write-Host "- Frequency selection: Clean dropdown menu"
Write-Host "- Infinity symbol: Large (36sp), centered, bold"
Write-Host "- Count box: Reduced to 80dp width"
