# Add real radial gradient glow to sun icons

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$lines = Get-Content $file

Write-Host "Adding real gradient glow effects..."

$newLines = @()
$i = 0

while ($i -lt $lines.Count) {
    $line = $lines[$i]
    
    # Replace morning sun glow with full circle radial gradient
    if ($line -match '// Sun glow - drawn first \(furthest back\)' -and $i -gt 260 -and $i -lt 290) {
        Write-Host "Replacing morning sun glow at line $i"
        # Skip old glow implementation (9 lines)
        $i += 9
        
        # Add new radial gradient glow
        $newLines += "            // Sun glow - full circle with radial gradient"
        $newLines += "            group {"
        $newLines += "                path("
        $newLines += "                    fill = androidx.compose.ui.graphics.Brush.radialGradient("
        $newLines += "                        colors = listOf("
        $newLines += "                            androidx.compose.ui.graphics.Color(0xFFFFE082).copy(alpha = 0.6f),"
        $newLines += "                            androidx.compose.ui.graphics.Color(0xFFFFB74D).copy(alpha = 0.3f),"
        $newLines += "                            androidx.compose.ui.graphics.Color(0xFFFF9800).copy(alpha = 0.0f)"
        $newLines += "                        ),"
        $newLines += "                        center = androidx.compose.ui.geometry.Offset(15f, 12f),"
        $newLines += "                        radius = 6f"
        $newLines += "                    )"
        $newLines += "                ) {"
        $newLines += "                    moveTo(21f, 12f)"
        $newLines += "                    arcTo(6f, 6f, 0f, true, true, 9f, 12f)"
        $newLines += "                    arcTo(6f, 6f, 0f, true, true, 21f, 12f)"
        $newLines += "                    close()"
        $newLines += "                }"
        $newLines += "            }"
        continue
    }
    
    # Replace afternoon sun glow with full circle radial gradient
    if ($line -match '// Sun glow - drawn first \(back\)' -and $i -gt 300) {
        Write-Host "Replacing afternoon sun glow at line $i"
        # Skip old glow implementation (9 lines)
        $i += 9
        
        # Add new radial gradient glow
        $newLines += "            // Sun glow - full circle with radial gradient"
        $newLines += "            group {"
        $newLines += "                path("
        $newLines += "                    fill = androidx.compose.ui.graphics.Brush.radialGradient("
        $newLines += "                        colors = listOf("
        $newLines += "                            androidx.compose.ui.graphics.Color(0xFFFFE082).copy(alpha = 0.7f),"
        $newLines += "                            androidx.compose.ui.graphics.Color(0xFFFFB74D).copy(alpha = 0.4f),"
        $newLines += "                            androidx.compose.ui.graphics.Color(0xFFFF9800).copy(alpha = 0.0f)"
        $newLines += "                        ),"
        $newLines += "                        center = androidx.compose.ui.geometry.Offset(12f, 6f),"
        $newLines += "                        radius = 6f"
        $newLines += "                    )"
        $newLines += "                ) {"
        $newLines += "                    moveTo(18f, 6f)"
        $newLines += "                    arcTo(6f, 6f, 0f, true, true, 6f, 6f)"
        $newLines += "                    arcTo(6f, 6f, 0f, true, true, 18f, 6f)"
        $newLines += "                    close()"
        $newLines += "                }"
        $newLines += "            }"
        continue
    }
    
    $newLines += $line
    $i++
}

Set-Content $file $newLines

Write-Host "`nDone!"
Write-Host "- Added full circle radial gradient glow to morning sun"
Write-Host "- Added full circle radial gradient glow to afternoon sun"
Write-Host "- Glow fades from bright center to transparent edges"
