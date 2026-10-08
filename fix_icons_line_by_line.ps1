# Fix mountain opacity and add sun glow using line-by-line processing

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$lines = Get-Content $file

Write-Host "Fixing icons..."

$newLines = @()
$i = 0

while ($i -lt $lines.Count) {
    $line = $lines[$i]
    
    # Fix Morning icon - change fillAlpha line
    if ($line -match 'fillAlpha = 0\.7f' -and $i -gt 270 -and $i -lt 295) {
        # Skip this line (remove fillAlpha)
        Write-Host "Removed morning mountain transparency at line $i"
        $i++
        continue
    }
    
    # Fix Afternoon icon - change fillAlpha line  
    if ($line -match 'fillAlpha = 0\.7f' -and $i -gt 300 -and $i -lt 330) {
        # Skip this line (remove fillAlpha)
        Write-Host "Removed afternoon mountain transparency at line $i"
        $i++
        continue
    }
    
    # Add glow before morning sun
    if ($line -match '// Rising sun \(half circle\) - drawn first') {
        $newLines += "            // Sun glow - drawn first (furthest back)"
        $newLines += "            path("
        $newLines += "                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color(0xFFFFE082)),"
        $newLines += "                fillAlpha = 0.4f"
        $newLines += "            ) {"
        $newLines += "                moveTo(20f, 12f)"
        $newLines += "                arcTo(5f, 5f, 0f, false, false, 10f, 12f)"
        $newLines += "                close()"
        $newLines += "            }"
        $newLines += "            // Rising sun (half circle) - drawn second"
        Write-Host "Added morning sun glow at line $i"
        $i++
        continue
    }
    
    # Add glow before afternoon sun
    if ($line -match '// Full sun' -and $i -gt 300) {
        $newLines += "            // Sun glow - drawn first (back)"
        $newLines += "            path("
        $newLines += "                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color(0xFFFFE082)),"
        $newLines += "                fillAlpha = 0.5f"
        $newLines += "            ) {"
        $newLines += "                moveTo(17f, 6f)"
        $newLines += "                arcTo(5f, 5f, 0f, true, true, 7f, 6f)"
        $newLines += "                arcTo(5f, 5f, 0f, true, true, 17f, 6f)"
        $newLines += "                close()"
        $newLines += "            }"
        $newLines += "            // Full sun - drawn second"
        Write-Host "Added afternoon sun glow at line $i"
        $i++
        continue
    }
    
    $newLines += $line
    $i++
}

Set-Content $file $newLines

Write-Host "`nDone!"
Write-Host "- Removed mountain transparency"
Write-Host "- Added sun glow effects"
