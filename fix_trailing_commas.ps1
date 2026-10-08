# Remove trailing commas after Color.Black

$file = 'app/src/main/java/com/j4/diabetestracker/MainActivity.kt'
$content = Get-Content $file -Raw

Write-Host "Removing trailing commas..."

# Remove trailing comma after Color.Black),
$content = $content -replace 'Color\.Black\),\s*\n\s*\)', 'Color.Black))'

Set-Content $file $content

Write-Host "Done!"
