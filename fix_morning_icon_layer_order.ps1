# Fix morning icon - put sun behind mountain

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Fixing morning icon layer order..."

# Find and swap the order of sun and mountain paths
$pattern = @'
        \)\.apply \{
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
            // Rising sun \(half circle\)
            path\(
                fill = androidx\.compose\.ui\.graphics\.SolidColor\(androidx\.compose\.ui\.graphics\.Color\(0xFFFFB74D\)\)
            \) \{
                moveTo\(18f, 12f\)
                arcTo\(3f, 3f, 0f, false, false, 12f, 12f\)
                close\(\)
            \}
        \}\.build\(\)
'@

$replacement = @'
        ).apply {
            // Rising sun (half circle) - drawn first so it's behind the mountain
            path(
                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color(0xFFFFB74D))
            ) {
                moveTo(18f, 12f)
                arcTo(3f, 3f, 0f, false, false, 12f, 12f)
                close()
            }
            // Mountain silhouette - drawn second so it's in front
            path(
                fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color.Black),
                fillAlpha = 0.7f
            ) {
                moveTo(2f, 18f)
                lineTo(8f, 10f)
                lineTo(12f, 14f)
                lineTo(16f, 8f)
                lineTo(22f, 16f)
                lineTo(22f, 18f)
                close()
            }
        }.build()
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Swapped sun and mountain order - SUCCESS"
    Set-Content $file $content
} else {
    Write-Host "Pattern not found - FAILED"
}

Write-Host "`nDone!"
Write-Host "Sun is now drawn first (behind mountain) for proper sunrise effect"
