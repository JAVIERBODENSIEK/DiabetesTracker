# Update backup logic to handle "Every Exit" frequency and unlimited backups

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Updating backup logic..."

# Update the shouldBackup logic to include "Every Exit"
$pattern = @'
                        val shouldBackup = when \(frequency\) \{
                            "Daily" -> lastBackupDate != currentDate
                            "Weekly" -> \{
'@

$replacement = @'
                        val shouldBackup = when (frequency) {
                            "Every Exit" -> true // Always backup on every exit
                            "Daily" -> lastBackupDate != currentDate
                            "Weekly" -> {
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Step 1: Added 'Every Exit' to backup logic - SUCCESS"
} else {
    Write-Host "Step 1: Pattern not found - SKIPPED"
}

# Update file rotation logic to handle unlimited (0 = infinity)
$pattern2 = @'
                            // Delete oldest backup if we've reached the limit
                            if \(existingBackups\.size >= maxBackupCount\) \{
                                val toDelete = existingBackups\.take\(existingBackups\.size - maxBackupCount \+ 1\)
                                toDelete\.forEach \{ it\.delete\(\) \}
                            \}
'@

$replacement2 = @'
                            // Delete oldest backup if we've reached the limit (unless unlimited)
                            if (maxBackupCount > 0 && existingBackups.size >= maxBackupCount) {
                                val toDelete = existingBackups.take(existingBackups.size - maxBackupCount + 1)
                                toDelete.forEach { it.delete() }
                            }
                            // If maxBackupCount is 0 (unlimited), don't delete any backups
'@

if ($content -match $pattern2) {
    $content = $content -replace $pattern2, $replacement2
    Write-Host "Step 2: Updated rotation logic for unlimited backups - SUCCESS"
} else {
    Write-Host "Step 2: Pattern not found - SKIPPED"
}

# Write back
Set-Content $file $content

Write-Host "`nBackup logic updated!"
Write-Host "- 'Every Exit' frequency: Creates backup every time app closes"
Write-Host "- Unlimited backups (0): Never deletes old backups"
