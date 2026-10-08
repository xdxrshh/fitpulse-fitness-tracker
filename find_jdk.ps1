# Find JDK with javac
$javacPath = $null

# Check JAVA_HOME first
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    $javacPath = "$env:JAVA_HOME\bin\javac.exe"
}

# Search common install directories
if (-not $javacPath) {
    $searchRoots = @(
        "C:\Program Files\Java",
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Microsoft",
        "C:\Program Files\OpenJDK"
    )
    foreach ($root in $searchRoots) {
        if (Test-Path $root) {
            $found = Get-ChildItem $root -ErrorAction SilentlyContinue | 
                     Where-Object { $_.Name -like "*jdk*" -or $_.Name -like "*JDK*" } |
                     ForEach-Object { "$($_.FullName)\bin\javac.exe" } |
                     Where-Object { Test-Path $_ } |
                     Select-Object -First 1
            if ($found) { $javacPath = $found; break }
        }
    }
}

if ($javacPath) {
    Write-Host "Found javac: $javacPath"
    & $javacPath -version
} else {
    Write-Host "javac NOT FOUND in any standard location"
}
