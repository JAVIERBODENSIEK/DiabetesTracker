# Fix afternoon icon structure - add missing .build()

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$lines = Get-Content $file

Write-Host "Fixing afternoon icon structure..."

$newLines = @()

for ($i = 0; $i -lt $lines.Count; $i++) {
    $line = $lines[$i]
    
    # After afternoon mountain close brace, ensure we have }.build()
    if ($line -match '^\s*\}\s*$' -and $i -gt 350 -and $i -lt 365) {
        # Check if next line is }.build()
        if ($i + 1 -lt $lines.Count -and $lines[$i + 1] -match '^\s*}\.build\(\)') {
            # Already correct
            $newLines += $line
        }
        elseif ($i + 1 -lt $lines.Count -and $lines[$i + 1] -match '^\s*val Evening') {
            # Missing }.build() - add it
            $newLines += $line
            $newLines += "        }.build()"
            $newLines += ""
            Write-Host "Added missing }.build() for Afternoon icon at line $i"
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

Write-Host "Done!"
