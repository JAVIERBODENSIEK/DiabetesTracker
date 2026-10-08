# Add the missing auto-backup functions

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Adding auto-backup save/load functions..."

# Add after loadAutoSaveOnExit function
$pattern = '    fun loadAutoSaveOnExit\(context: Context\): Boolean \{\s+val prefs = context\.getSharedPreferences\(SETTINGS_NAME, Context\.MODE_PRIVATE\)\s+return prefs\.getBoolean\("auto_save_on_exit", true\) // Default to true \(enabled\)\s+\}\s+    \s+    // Custom header text persistence methods'

$replacement = @'
    fun loadAutoSaveOnExit(context: Context): Boolean {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("auto_save_on_exit", true) // Default to true (enabled)
    }
    
    // Auto-backup on exit setting methods
    fun saveAutoBackupOnExit(context: Context, autoBackup: Boolean) {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("auto_backup_on_exit", autoBackup).apply()
    }
    
    fun loadAutoBackupOnExit(context: Context): Boolean {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("auto_backup_on_exit", true) // Default to true (activated)
    }
    
    // Custom header text persistence methods
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Added auto-backup functions - SUCCESS"
    Set-Content $file $content
} else {
    Write-Host "Pattern not found - FAILED"
}

Write-Host "Done!"
