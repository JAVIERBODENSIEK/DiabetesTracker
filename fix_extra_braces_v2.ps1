# Remove extra closing braces

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$lines = Get-Content $file

Write-Host "Removing extra braces..."

$newLines = @()

for ($i = 0; $i -lt $lines.Count; $i++) {
    $line = $lines[$i]
    
    # Skip standalone closing braces that are errors (lines 281, 321, 322)
    if ($line -match '^\s*}\s*$') {
        if ($i -eq 281 -or $i -eq 321 -or $i -eq 322) {
            Write-Host "Skipping extra brace at line $i"
            continue
        }
    }
    
    $newLines += $line
}

Set-Content $file $newLines

Write-Host "Done!"
