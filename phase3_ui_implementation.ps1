# Phase 3: Implement expandable auto-backup settings UI

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Implementing expandable auto-backup settings UI..."

# Step 1: Add state variables in AdvancedSettingsDialog
$pattern1 = '    var autoBackupOnExit by remember \{ mutableStateOf\(SettingsManager\.loadAutoBackupOnExit\(context\)\) \}\s+var useSymbolsInCharts'

$replacement1 = @'
    var autoBackupOnExit by remember { mutableStateOf(SettingsManager.loadAutoBackupOnExit(context)) }
    var showAutoBackupSettings by remember { mutableStateOf(false) }
    var autoBackupMaxCount by remember { mutableStateOf(SettingsManager.loadAutoBackupMaxCount(context)) }
    var autoBackupFrequency by remember { mutableStateOf(SettingsManager.loadAutoBackupFrequency(context)) }
    var autoBackupPath by remember { mutableStateOf(SettingsManager.loadAutoBackupPath(context)) }
    var useSymbolsInCharts
'@

if ($content -match $pattern1) {
    $content = $content -replace $pattern1, $replacement1
    Write-Host "Step 1: Added state variables - SUCCESS"
} else {
    Write-Host "Step 1: Pattern not found - SKIPPED"
}

# Write intermediate result
Set-Content $file $content

Write-Host "`nPhase 3 Step 1 complete."
Write-Host "Next: Run phase 3b to add the expandable UI section."
