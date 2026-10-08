# Remove extra closing braces

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$lines = Get-Content $file

Write-Host "Removing extra braces..."

$newLines = @()

for ($i = 0; $i -lt $lines.Count; $i++) {
    $line = $lines[$i]
    
    # Skip standalone closing braces that are errors
    if ($line -match '^\s*}\s*$' -and $i -gt 270 -and $i -lt 350) {
        # Check if previous line already has a closing brace
        if ($i > 0 -and $newLines[-1] -match '^\s*}\s*$') {
            Write-Host "Skipping extra brace at line $i"
            continue
        }
        # Check if this is right after a close() which shouldn't have another }
        if ($i > 0 -and $newLines[-1] -match 'close\(\)') {
            Write-Host "Skipping extra brace after close() at line $i"
            continue
        }
    }
    
    $newLines += $line
}

Set-Content $file $newLines

Write-Host "Done!"
