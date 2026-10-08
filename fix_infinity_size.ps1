# Fix infinity symbol size - make it fill the box properly

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Fixing infinity symbol size..."

# Replace the auto-sizing logic with a simpler, more effective approach
$pattern = @'
                                        placeholder = \{ 
                                            BoxWithConstraints\(
                                                modifier = Modifier\.fillMaxSize\(\),
                                                contentAlignment = Alignment\.Center
                                            \) \{
                                                // Auto-size to fill 80% of available space
                                                val maxSize = minOf\(maxWidth, maxHeight\) \* 0\.8f
                                                
                                                Text\(
                                                    "∞",
                                                    style = MaterialTheme\.typography\.headlineLarge\.copy\(
                                                        fontSize = maxSize\.value\.sp,
                                                        fontWeight = FontWeight\.ExtraBold,
                                                        brush = Brush\.linearGradient\(
                                                            colors = listOf\(
                                                                Color\(0xFF00BCD4\), // Cyan
                                                                Color\(0xFF2196F3\), // Blue
                                                                Color\(0xFF9C27B0\)  // Purple
                                                            \)
                                                        \)
                                                    \),
                                                    textAlign = TextAlign\.Center
                                                \)
                                            \}
                                        \},
'@

$replacement = @'
                                        placeholder = { 
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "∞",
                                                    style = MaterialTheme.typography.displayLarge.copy(
                                                        fontSize = 48.sp,
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
    Write-Host "Fixed infinity symbol size to 48sp - SUCCESS"
} else {
    Write-Host "Pattern not found - FAILED"
}

# Write back
Set-Content $file $content

Write-Host "`nDone!"
Write-Host "Infinity symbol now uses fixed 48sp size (large and visible)"
