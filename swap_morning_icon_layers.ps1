# Swap morning icon layers using line-by-line replacement

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$lines = Get-Content $file

Write-Host "Swapping morning icon layers..."

$inMorningIcon = $false
$foundMountain = $false
$mountainLines = @()
$sunLines = @()
$currentSection = ""
$newLines = @()

for ($i = 0; $i -lt $lines.Count; $i++) {
    $line = $lines[$i]
    
    # Detect start of Morning icon
    if ($line -match 'val Morning: ImageVector') {
        $inMorningIcon = $true
        $newLines += $line
        continue
    }
    
    # Detect end of Morning icon
    if ($inMorningIcon -and $line -match '^\s*}\.build\(\)') {
        # Output sun first, then mountain
        $newLines += $sunLines
        $newLines += $mountainLines
        $newLines += $line
        $inMorningIcon = $false
        $foundMountain = $false
        $mountainLines = @()
        $sunLines = @()
        $currentSection = ""
        continue
    }
    
    # Inside Morning icon - collect mountain and sun sections
    if ($inMorningIcon) {
        if ($line -match '// Mountain silhouette') {
            $currentSection = "mountain"
            $mountainLines += "            // Mountain silhouette - drawn second so it's in front"
            continue
        }
        elseif ($line -match '// Rising sun') {
            $currentSection = "sun"
            $sunLines += "            // Rising sun (half circle) - drawn first so it's behind the mountain"
            continue
        }
        
        if ($currentSection -eq "mountain") {
            $mountainLines += $line
        }
        elseif ($currentSection -eq "sun") {
            $sunLines += $line
        }
        else {
            $newLines += $line
        }
    }
    else {
        $newLines += $line
    }
}

# Write back
Set-Content $file $newLines

Write-Host "Successfully swapped morning icon layers!"
Write-Host "Sun is now behind the mountain for proper sunrise effect"
