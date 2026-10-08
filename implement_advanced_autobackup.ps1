# Comprehensive Auto-Backup Settings Implementation

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Implementing advanced auto-backup settings..."

# Step 1: Add new save/load methods for advanced settings in SettingsManager
$pattern1 = '    fun loadAutoBackupOnExit\(context: Context\): Boolean \{\s+val prefs = context\.getSharedPreferences\(SETTINGS_NAME, Context\.MODE_PRIVATE\)\s+return prefs\.getBoolean\("auto_backup_on_exit", true\) // Default to true \(activated\)\s+\}'

$replacement1 = @'
    fun loadAutoBackupOnExit(context: Context): Boolean {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("auto_backup_on_exit", true) // Default to true (activated)
    }
    
    // Advanced auto-backup settings
    fun saveAutoBackupMaxCount(context: Context, maxCount: Int) {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("auto_backup_max_count", maxCount).apply()
    }
    
    fun loadAutoBackupMaxCount(context: Context): Int {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("auto_backup_max_count", 5) // Default: 5 backups
    }
    
    fun saveAutoBackupFrequency(context: Context, frequency: String) {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("auto_backup_frequency", frequency).apply()
    }
    
    fun loadAutoBackupFrequency(context: Context): String {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("auto_backup_frequency", "Daily") ?: "Daily"
    }
    
    fun saveAutoBackupPath(context: Context, path: String) {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("auto_backup_path", path).apply()
    }
    
    fun loadAutoBackupPath(context: Context): String {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        val defaultPath = android.os.Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_DOWNLOADS
        ).absolutePath
        return prefs.getString("auto_backup_path", defaultPath) ?: defaultPath
    }
    
    fun saveLastAutoBackupDate(context: Context, date: String) {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("last_auto_backup_date", date).apply()
    }
    
    fun loadLastAutoBackupDate(context: Context): String {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("last_auto_backup_date", "") ?: ""
    }
'@

if ($content -match $pattern1) {
    $content = $content -replace $pattern1, $replacement1
    Write-Host "Step 1: Added advanced auto-backup save/load methods - SUCCESS"
} else {
    Write-Host "Step 1: Pattern not found - SKIPPED"
}

# Write back
Set-Content $file $content

Write-Host "`nPhase 1 complete. Advanced settings methods added."
Write-Host "Next: Run phase 2 to update backup logic and UI."
