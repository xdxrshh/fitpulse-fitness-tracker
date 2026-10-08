$files = Get-ChildItem -Recurse -Filter *.java -Path src
$files | ForEach-Object { $_.FullName } | Out-File -FilePath sources.txt -Encoding UTF8
Write-Host ("Written " + $files.Count + " source files to sources.txt")
