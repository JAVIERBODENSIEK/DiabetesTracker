# Fix mountain opacity and add sun glow effects

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Fixing mountain opacity and adding sun glow..."

# Fix 1: Morning icon - remove mountain transparency and add sun glow
$pattern1 = @'
            // Rising sun \(half circle\) - drawn first so it's behind the mountain
            path\(
                fill = androidx\.compose\.ui\.graphics\.SolidColor\(androidx\.compose\.ui\.graphics\.Color\(0xFFFFB74D\)\)
            \) \{
                moveTo\(18f, 12f\)
                arcTo\(3f, 3f, 0f, false, false, 12f, 12f\)
                close\(\)
            \}
            // Mountain silhouette - drawn second so it's in front
            path\(
                fill = androidx\.compose\.ui\.graphics\.SolidColor\(androidx\.compose\.ui\.graphics\.Color\.Black\),
                fillAlpha = 0\.7f
            \) \{
'@

$replacement1 = @'
            // Sun glow - drawn first (furthest back)
            path(
                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color(0xFFFFE082)),
                fillAlpha = 0.4f
            ) {
                moveTo(20f, 12f)
                arcTo(5f, 5f, 0f, false, false, 10f, 12f)
                close()
            }
            // Rising sun (half circle) - drawn second
            path(
                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color(0xFFFFB74D))
            ) {
                moveTo(18f, 12f)
                arcTo(3f, 3f, 0f, false, false, 12f, 12f)
                close()
            }
            // Mountain silhouette - drawn third (front), fully opaque
            path(
                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color.Black)
            ) {
'@

if ($content -match $pattern1) {
    $content = $content -replace $pattern1, $replacement1
    Write-Host "Step 1: Fixed morning icon - SUCCESS"
} else {
    Write-Host "Step 1: Pattern not found - SKIPPED"
}

# Fix 2: Afternoon icon - remove mountain transparency and add sun glow
$pattern2 = @'
            // Mountain silhouette
            path\(
                fill = androidx\.compose\.ui\.graphics\.SolidColor\(androidx\.compose\.ui\.graphics\.Color\.Black\),
                fillAlpha = 0\.7f
            \) \{
                moveTo\(2f, 18f\)
                lineTo\(8f, 10f\)
                lineTo\(12f, 14f\)
                lineTo\(16f, 8f\)
                lineTo\(22f, 16f\)
                lineTo\(22f, 18f\)
                close\(\)
            \}
            // Full sun
            path\(
                fill = androidx\.compose\.ui\.graphics\.SolidColor\(androidx\.compose\.ui\.graphics\.Color\(0xFFFFB74D\)\)
            \) \{
                moveTo\(15f, 6f\)
                arcTo\(3f, 3f, 0f, true, true, 9f, 6f\)
                arcTo\(3f, 3f, 0f, true, true, 15f, 6f\)
                close\(\)
            \}
'@

$replacement2 = @'
            // Sun glow - drawn first (back)
            path(
                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color(0xFFFFE082)),
                fillAlpha = 0.5f
            ) {
                moveTo(17f, 6f)
                arcTo(5f, 5f, 0f, true, true, 7f, 6f)
                arcTo(5f, 5f, 0f, true, true, 17f, 6f)
                close()
            }
            // Full sun - drawn second
            path(
                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color(0xFFFFB74D))
            ) {
                moveTo(15f, 6f)
                arcTo(3f, 3f, 0f, true, true, 9f, 6f)
                arcTo(3f, 3f, 0f, true, true, 15f, 6f)
                close()
            }
            // Mountain silhouette - drawn third (front), fully opaque
            path(
                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color.Black)
            ) {
                moveTo(2f, 18f)
                lineTo(8f, 10f)
                lineTo(12f, 14f)
                lineTo(16f, 8f)
                lineTo(22f, 16f)
                lineTo(22f, 18f)
                close()
            }
'@

if ($content -match $pattern2) {
    $content = $content -replace $pattern2, $replacement2
    Write-Host "Step 2: Fixed afternoon icon - SUCCESS"
} else {
    Write-Host "Step 2: Pattern not found - SKIPPED"
}

# Write back
Set-Content $file $content

Write-Host "`nDone!"
Write-Host "- Mountains now fully opaque (no transparency)"
Write-Host "- Added visible glow to morning sun"
Write-Host "- Added visible glow to afternoon sun"
