# Fix auto-backup to save to Downloads folder instead of private app directory

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Changing auto-backup location to Downloads folder..."

# Replace the auto-backup file location code
$pattern = @'
                // Auto-backup on exit if enabled
                if \(autoBackupOnExit\) \{
                    try \{
                        val backupData = BackupManager\.createBackup\(context, BackupType\.ALL\)
                        val timestamp = java\.time\.LocalDateTime\.now\(\)\.format\(java\.time\.format\.DateTimeFormatter\.ofPattern\("yyyy-MM-dd_HH-mm-ss"\)\)
                        val backupFileName = "auto_backup_\$timestamp\.json"
                        val backupFile = java\.io\.File\(context\.getExternalFilesDir\(null\), backupFileName\)
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
                        val backupData = BackupManager.createBackup(context, BackupType.ALL)
                        val timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
                        val backupFileName = "DiabetesTracker_AutoBackup_$timestamp.json"
                        
                        // Save to Downloads folder for easy access
                        val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                        val backupFile = java.io.File(downloadsDir, backupFileName)
                        val backupUri = android.net.Uri.fromFile(backupFile)
                        BackupManager.exportBackupToFile(context, backupData, backupUri)
                    } catch (e: Exception) {
                        // Silent fail - don't interrupt app exit
                    }
                }
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Changed auto-backup location to Downloads folder - SUCCESS"
    Set-Content $file $content
} else {
    Write-Host "Pattern not found - FAILED"
    Write-Host "Trying simpler pattern..."
    
    # Try simpler pattern focusing on just the file location lines
    $simplePattern = 'val backupFile = java\.io\.File\(context\.getExternalFilesDir\(null\), backupFileName\)'
    $simpleReplacement = @'
// Save to Downloads folder for easy access
                        val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                        val backupFile = java.io.File(downloadsDir, backupFileName)
'@
    
    if ($content -match $simplePattern) {
        $content = $content -replace $simplePattern, $simpleReplacement
        
        # Also update the filename to be more descriptive
        $content = $content -replace 'val backupFileName = "auto_backup_\$timestamp\.json"', 'val backupFileName = "DiabetesTracker_AutoBackup_$timestamp.json"'
        
        Write-Host "Changed auto-backup location with simpler pattern - SUCCESS"
        Set-Content $file $content
    } else {
        Write-Host "Simpler pattern also not found - FAILED"
    }
}

Write-Host "`nDone!"
