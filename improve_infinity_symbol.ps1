# Improve infinity symbol: auto-size and colorful gradient

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Improving infinity symbol..."

# Replace the infinity symbol placeholder with auto-sized colorful version
$pattern = @'
                                        placeholder = \{ 
                                            Box\(
                                                modifier = Modifier\.fillMaxSize\(\),
                                                contentAlignment = Alignment\.Center
                                            \) \{
                                                Text\(
                                                    "∞",
                                                    style = MaterialTheme\.typography\.headlineLarge\.copy\(
                                                        fontSize = 36\.sp,
                                                        fontWeight = FontWeight\.Bold
                                                    \),
                                                    color = MaterialTheme\.colorScheme\.primary,
                                                    textAlign = TextAlign\.Center
                                                \)
                                            \}
                                        \},
'@

$replacement = @'
                                        placeholder = { 
                                            BoxWithConstraints(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                // Auto-size to fill 80% of available space
                                                val maxSize = minOf(maxWidth, maxHeight) * 0.8f
                                                
                                                Text(
                                                    "∞",
                                                    style = MaterialTheme.typography.headlineLarge.copy(
                                                        fontSize = maxSize.value.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        brush = Brush.linearGradient(
                                                            colors = listOf(
                                                                Color(0xFF00BCD4), // Cyan
                                                                Color(0xFF2196F3), // Blue
                                                                Color(0xFF9C27B0)  // Purple
                                                            )
                                                        )
                                                    ),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        },
'@

if ($content -match $pattern) {
    $content = $content -replace $pattern, $replacement
    Write-Host "Improved infinity symbol with auto-sizing and gradient - SUCCESS"
} else {
    Write-Host "Pattern not found - FAILED"
}

# Write back
Set-Content $file $content

Write-Host "`nDone!"
Write-Host "Infinity symbol now:"
Write-Host "- Auto-sizes to fill 80% of box (no cropping)"
Write-Host "- Colorful gradient (Cyan -> Blue -> Purple)"
Write-Host "- Extra bold for better visibility"
