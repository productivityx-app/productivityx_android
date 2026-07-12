param(
    [string]$EnglishFile = "app/src/main/res/values/strings.xml"
)

$ErrorActionPreference = "Stop"
$rootDir = $PWD.Path

# Parse English strings.xml - extract all name="..." and their raw XML lines
Write-Host "Parsing English strings.xml..."
$enLines = Get-Content (Join-Path $rootDir $EnglishFile)
$enEntries = @{}
$currentName = $null
$currentLines = @()

foreach ($line in $enLines) {
    if ($line -match '<string name="([^"]+)">') {
        $name = $matches[1]
        if ($currentName) {
            $enEntries[$currentName] = $currentLines
        }
        $currentName = $name
        $currentLines = @($line)
        if ($line -match '</string>$') {
            $enEntries[$currentName] = $currentLines
            $currentName = $null
            $currentLines = @()
        }
    } elseif ($currentName) {
        $currentLines += $line
        if ($line -match '</string>$') {
            $enEntries[$currentName] = $currentLines
            $currentName = $null
            $currentLines = @()
        }
    }
}
if ($currentName) {
    $enEntries[$currentName] = $currentLines
}

Write-Host "Found $($enEntries.Count) English keys"

$langs = @("ar", "de", "es", "fr", "hi", "in", "it", "ja", "ko", "nl", "pt", "tr", "vi", "zh", "zh-rTW")

# Handle Chinese (zh)
$zhPath = Join-Path $rootDir "app/src/main/res/values-zh"
if (-not (Test-Path $zhPath)) {
    New-Item -ItemType Directory -Path $zhPath -Force | Out-Null
    $zhContent = @('<?xml version="1.0" encoding="utf-8"?>', '<resources>', '</resources>')
    Set-Content -Path (Join-Path $zhPath "strings.xml") -Value $zhContent
    Write-Host "Created values-zh/strings.xml"
}

foreach ($lang in $langs) {
    $langFile = Join-Path $rootDir "app/src/main/res/values-$lang/strings.xml"
    if (-not (Test-Path $langFile)) {
        Write-Host "WARNING: $langFile does not exist, skipping"
        continue
    }
    
    $foundKeys = Select-String -Path $langFile -Pattern 'name="([^"]+)"' | ForEach-Object { $_.Matches.Groups[1].Value }
    $langKeySet = @{}
    foreach ($k in $foundKeys) {
        $langKeySet[$k] = $true
    }
    
    $missing = @()
    foreach ($k in $enEntries.Keys) {
        if (-not $langKeySet.ContainsKey($k)) {
            $missing += $k
        }
    }
    
    $missingCount = $missing.Count
    if ($missingCount -eq 0) {
        Write-Host "${lang}: up to date (0 missing)"
        continue
    }
    
    $linesToAdd = @("    <!-- Auto-added from English (values) - translate me -->")
    foreach ($k in $missing) {
        $linesToAdd += $enEntries[$k]
    }
    
    $langContent = Get-Content $langFile
    $newContent = @()
    $added = $false
    foreach ($line in $langContent) {
        if ($line -match '^\s*</resources>\s*$' -and -not $added) {
            $newContent += $linesToAdd
            $added = $true
        }
        $newContent += $line
    }
    
    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($langFile, ($newContent -join "`r`n"), $utf8NoBom)
    Write-Host "${lang}: added $missingCount missing keys"
}

Write-Host "Done! All language files synced."
