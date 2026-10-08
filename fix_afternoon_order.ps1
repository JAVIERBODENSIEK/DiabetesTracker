# Fix afternoon icon layer order - glow and sun should be before mountain

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$lines = Get-Content $file

Write-Host "Fixing afternoon icon layer order..."

$inAfternoon = $false
$mountainLines = @()
$glowLines = @()
$sunLines = @()
$currentSection = ""
$newLines = @()

for ($i = 0; $i -lt $lines.Count; $i++) {
    $line = $lines[$i]
    
    # Detect start of Afternoon icon
    if ($line -match 'val Afternoon: ImageVector') {
        $inAfternoon = $true
        $newLines += $line
        continue
    }
    
    # Detect end of Afternoon icon
    if ($inAfternoon -and $line -match '^\s*}\.build\(\)') {
        # Output in correct order: glow, sun, then mountain
        $newLines += $glowLines
        $newLines += $sunLines
        $newLines += $mountainLines
        $newLines += $line
        $inAfternoon = $false
        $mountainLines = @()
        $glowLines = @()
        $sunLines = @()
        $currentSection = ""
        continue
    }
    
    # Inside Afternoon icon - collect sections
    if ($inAfternoon) {
        if ($line -match '// Mountain silhouette') {
            $currentSection = "mountain"
            $mountainLines += $line
            continue
        }
        elseif ($line -match '// Sun glow') {
            $currentSection = "glow"
            $glowLines += $line
            continue
        }
        elseif ($line -match '// Full sun') {
            $currentSection = "sun"
            $sunLines += $line
            continue
        }
        
        if ($currentSection -eq "mountain") {
            $mountainLines += $line
        }
        elseif ($currentSection -eq "glow") {
            $glowLines += $line
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

Set-Content $file $newLines

Write-Host "Fixed afternoon icon layer order!"
Write-Host "Order: Glow -> Sun -> Mountain"
