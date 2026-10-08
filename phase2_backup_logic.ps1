# Phase 2: Update backup logic with frequency check and file rotation

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Updating auto-backup logic..."

# Replace the auto-backup logic with advanced version
$pattern = @'
                // Auto-backup on exit if enabled
                if \(autoBackupOnExit\) \{
                    try \{
                        val backupData = BackupManager\.createBackup\(context, BackupType\.ALL\)
                        val timestamp = java\.time\.LocalDateTime\.now\(\)\.format\(java\.time\.format\.DateTimeFormatter\.ofPattern\("yyyy-MM-dd_HH-mm-ss"\)\)
                        val backupFileName = "DiabetesTracker_AutoBackup_\$timestamp\.json"
                        
                        // Save to Downloads folder for easy access
                        val downloadsDir = android\.os\.Environment\.getExternalStoragePublicDirectory\(android\.os\.Environment\.DIRECTORY_DOWNLOADS\)
                        val backupFile = java\.io\.File\(downloadsDir, backupFileName\)
                        val backupUri = android\.net\.Uri\.fromFile\(backupFile\)
                        BackupManager\.exportBackupToFile\(context, backupData, backupUri\)
                    \} catch \(e: Exception\) \{
                        // Silent fail - don't interrupt app exit
                    \}
                \}
'@

$replacement = @'
                // Auto-backup on exit if enabled
                if (autoBackupOnExit) {
                    try {
                        // Check if backup is needed based on frequency
                        val frequency = SettingsManager.loadAutoBackupFrequency(context)
                        val lastBackupDate = SettingsManager.loadLastAutoBackupDate(context)
                        val currentDate = java.time.LocalDate.now().toString()
                        
                        val shouldBackup = when (frequency) {
                            "Daily" -> lastBackupDate != currentDate
                            "Weekly" -> {
                                if (lastBackupDate.isBlank()) true
                                else {
                                    val lastDate = java.time.LocalDate.parse(lastBackupDate)
                                    val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastDate, java.time.LocalDate.now())
                                    daysBetween >= 7
                                }
                            }
                            "Monthly" -> {
                                if (lastBackupDate.isBlank()) true
                                else {
                                    val lastDate = java.time.LocalDate.parse(lastBackupDate)
                                    val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastDate, java.time.LocalDate.now())
                                    daysBetween >= 30
                                }
                            }
                            else -> true // Default: always backup
                        }
                        
                        if (shouldBackup) {
                            val backupData = BackupManager.createBackup(context, BackupType.ALL)
                            val timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
                            val backupFileName = "auto-backup_DiabetesTracker_$timestamp.json"
                            
                            // Get backup path and max count
                            val backupPath = SettingsManager.loadAutoBackupPath(context)
                            val maxBackupCount = SettingsManager.loadAutoBackupMaxCount(context)
                            
                            // Create backup directory
                            val backupDir = java.io.File(backupPath)
                            if (!backupDir.exists()) {
                                backupDir.mkdirs()
                            }
                            
                            // Get existing auto-backup files and sort by date (oldest first)
                            val existingBackups = backupDir.listFiles { _, name -> 
                                name.startsWith("auto-backup_DiabetesTracker_") && name.endsWith(".json")
                            }?.sortedBy { it.lastModified() } ?: emptyList()
                            
                            // Delete oldest backup if we've reached the limit
                            if (existingBackups.size >= maxBackupCount) {
                                val toDelete = existingBackups.take(existingBackups.size - maxBackupCount + 1)
                                toDelete.forEach { it.delete() }
                            }
                            
                            // Save new backup
                            val backupFile = java.io.File(backupDir, backupFileName)
                            val backupUri = android.net.Uri.fromFile(backupFile)
                            BackupManager.exportBackupToFile(context, backupData, backupUri)
                            
                            // Update last backup date
                            SettingsManager.saveLastAutoBackupDate(context, currentDate)
                        }
                    } catch (e: Exception) {
                        // Silent fail - don't interrupt app exit (stealth mode)
                    }
                }
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Updated auto-backup logic with frequency check and rotation - SUCCESS"
} else {
    Write-Host "Pattern not found - FAILED"
}

# Write back
Set-Content $file $content

Write-Host "`nPhase 2 complete. Backup logic updated."
