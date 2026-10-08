# Completely reconstruct TimeIcons object with correct structure

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$lines = Get-Content $file

Write-Host "Reconstructing TimeIcons object..."

$newLines = @()
$skipUntilEvening = $false

for ($i = 0; $i -lt $lines.Count; $i++) {
    $line = $lines[$i]
    
    # Start skipping from "object TimeIcons" until we find "val Evening"
    if ($line -match 'object TimeIcons') {
        $skipUntilEvening = $true
        # Add corrected TimeIcons
        $newLines += "object TimeIcons {"
        $newLines += "    val Morning: ImageVector"
        $newLines += "        get() = ImageVector.Builder("
        $newLines += "            name = `"Morning`","
        $newLines += "            defaultWidth = 24.0.dp,"
        $newLines += "            defaultHeight = 24.0.dp,"
        $newLines += "            viewportWidth = 24.0f,"
        $newLines += "            viewportHeight = 24.0f"
        $newLines += "        ).apply {"
        $newLines += "            // Sun glow - full circle"
        $newLines += "            path("
        $newLines += "                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color(0xFFFFE082)),"
        $newLines += "                fillAlpha = 0.5f"
        $newLines += "            ) {"
        $newLines += "                moveTo(21f, 12f)"
        $newLines += "                arcTo(6f, 6f, 0f, true, true, 9f, 12f)"
        $newLines += "                arcTo(6f, 6f, 0f, true, true, 21f, 12f)"
        $newLines += "                close()"
        $newLines += "            }"
        $newLines += "            // Rising sun (half circle)"
        $newLines += "            path("
        $newLines += "                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color(0xFFFFB74D))"
        $newLines += "            ) {"
        $newLines += "                moveTo(18f, 12f)"
        $newLines += "                arcTo(3f, 3f, 0f, false, false, 12f, 12f)"
        $newLines += "                close()"
        $newLines += "            }"
        $newLines += "            // Mountain silhouette"
        $newLines += "            path("
        $newLines += "                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color.Black)"
        $newLines += "            ) {"
        $newLines += "                moveTo(2f, 18f)"
        $newLines += "                lineTo(8f, 10f)"
        $newLines += "                lineTo(12f, 14f)"
        $newLines += "                lineTo(16f, 8f)"
        $newLines += "                lineTo(22f, 16f)"
        $newLines += "                lineTo(22f, 18f)"
        $newLines += "                close()"
        $newLines += "            }"
        $newLines += "        }.build()"
        $newLines += ""
        $newLines += "    val Afternoon: ImageVector"
        $newLines += "        get() = ImageVector.Builder("
        $newLines += "            name = `"Afternoon`","
        $newLines += "            defaultWidth = 24.0.dp,"
        $newLines += "            defaultHeight = 24.0.dp,"
        $newLines += "            viewportWidth = 24.0f,"
        $newLines += "            viewportHeight = 24.0f"
        $newLines += "        ).apply {"
        $newLines += "            // Sun glow - full circle"
        $newLines += "            path("
        $newLines += "                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color(0xFFFFE082)),"
        $newLines += "                fillAlpha = 0.6f"
        $newLines += "            ) {"
        $newLines += "                moveTo(17f, 6f)"
        $newLines += "                arcTo(5f, 5f, 0f, true, true, 7f, 6f)"
        $newLines += "                arcTo(5f, 5f, 0f, true, true, 17f, 6f)"
        $newLines += "                close()"
        $newLines += "            }"
        $newLines += "            // Full sun"
        $newLines += "            path("
        $newLines += "                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color(0xFFFFB74D))"
        $newLines += "            ) {"
        $newLines += "                moveTo(15f, 6f)"
        $newLines += "                arcTo(3f, 3f, 0f, true, true, 9f, 6f)"
        $newLines += "                arcTo(3f, 3f, 0f, true, true, 15f, 6f)"
        $newLines += "                close()"
        $newLines += "            }"
        $newLines += "            // Mountain silhouette"
        $newLines += "            path("
        $newLines += "                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color.Black)"
        $newLines += "            ) {"
        $newLines += "                moveTo(2f, 18f)"
        $newLines += "                lineTo(8f, 10f)"
        $newLines += "                lineTo(12f, 14f)"
        $newLines += "                lineTo(16f, 8f)"
        $newLines += "                lineTo(22f, 16f)"
        $newLines += "                lineTo(22f, 18f)"
        $newLines += "                close()"
        $newLines += "            }"
        $newLines += "        }.build()"
        $newLines += ""
        continue
    }
    
    # Stop skipping when we find Evening
    if ($skipUntilEvening -and $line -match 'val Evening') {
        $skipUntilEvening = $false
        $newLines += $line
        continue
    }
    
    # Skip lines while in TimeIcons reconstruction zone
    if ($skipUntilEvening) {
        continue
    }
    
    $newLines += $line
}

Set-Content $file $newLines

Write-Host "Done! Reconstructed TimeIcons with proper structure"
