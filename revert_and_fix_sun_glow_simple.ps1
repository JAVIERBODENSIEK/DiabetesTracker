# Revert to simple solid color glow (full circles) without gradients

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$lines = Get-Content $file

Write-Host "Reverting to simple full-circle glow..."

$newLines = @()
$skipUntilCloseBrace = 0

for ($i = 0; $i -lt $lines.Count; $i++) {
    $line = $lines[$i]
    
    # Skip broken glow sections
    if ($line -match '// Sun glow - full circle with radial gradient') {
        Write-Host "Removing broken glow at line $i"
        # Skip until we find the closing }
        while ($i -lt $lines.Count) {
            $i++
            if ($lines[$i] -match '^\s*}\s*$') {
                # Found closing brace, add simple glow instead
                $newLines += "            // Sun glow - full circle"
                $newLines += "            path("
                $newLines += "                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color(0xFFFFE082)),"
                $newLines += "                fillAlpha = 0.4f"
                $newLines += "            ) {"
                $newLines += "                moveTo(21f, 12f)"
                $newLines += "                arcTo(6f, 6f, 0f, true, true, 9f, 12f)"
                $newLines += "                arcTo(6f, 6f, 0f, true, true, 21f, 12f)"
                $newLines += "                close()"
                $newLines += "            }"
                break
            }
        }
        continue
    }
    
    $newLines += $line
}

Set-Content $file $newLines

Write-Host "Done! Reverted to simple full-circle glow"
