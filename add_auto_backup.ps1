# PowerShell script to add auto-backup on exit feature

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

# Step 1: Add save/load methods for autoBackupOnExit in SettingsManager
$pattern1 = '    fun loadAutoSaveOnExit\(context: Context\): Boolean \{'
$replacement1 = @'
    fun loadAutoSaveOnExit(context: Context): Boolean {
'@

# Add the new methods after loadAutoSaveOnExit
$pattern2 = '    fun loadAutoSaveOnExit\(context: Context\): Boolean \{\s+val prefs = context\.getSharedPreferences\(SETTINGS_NAME, Context\.MODE_PRIVATE\)\s+return prefs\.getBoolean\("auto_save_on_exit", true\) // Default to true\s+\}'
$replacement2 = @'
    fun loadAutoSaveOnExit(context: Context): Boolean {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("auto_save_on_exit", true) // Default to true
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
'@

$content = $content -replace $pattern2, $replacement2

# Write back
Set-Content $file $content

Write-Host "Step 1: Added autoBackupOnExit save/load methods"
