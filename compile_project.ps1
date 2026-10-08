$ErrorActionPreference = 'Stop'

$javac = $null
if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin/javac.exe'))) {
    $javac = Join-Path $env:JAVA_HOME 'bin/javac.exe'
} else {
    $command = Get-Command javac -ErrorAction SilentlyContinue
    if ($command) { $javac = $command.Source }
}

if (-not $javac) {
    Write-Error 'JDK 17 javac was not found. Install JDK 17 and add it to PATH or set JAVA_HOME.'
    exit 1
}

$classpath = 'lib\mysql-connector-j-8.3.0.jar;lib\flatlaf-3.4.1.jar;lib\h2-2.2.224.jar'
$output = 'bin'
$sourceFiles = Get-ChildItem -Recurse -Filter '*.java' -Path 'src' | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' }
if (-not $sourceFiles) {
    Write-Error 'No Java sources found under src.'
    exit 1
}

$sourcesPath = Join-Path (Get-Location) 'sources.txt'
[System.IO.File]::WriteAllLines($sourcesPath, $sourceFiles, [System.Text.Encoding]::ASCII)
if (-not (Test-Path $output)) { New-Item -ItemType Directory -Path $output | Out-Null }

& $javac '-encoding' 'UTF-8' '-d' $output '-cp' $classpath '@sources.txt'
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Copy-Item -Path 'src\resources\*' -Destination $output -Recurse -Force
Write-Host 'Build successful.'
