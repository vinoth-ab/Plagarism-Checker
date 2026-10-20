Set-Location "$PSScriptRoot\PlagiarismChecker"
if (-not (Test-Path "out")) { New-Item -ItemType Directory -Path "out" | Out-Null }
Write-Host "Compiling Plagiarism Checker..." -ForegroundColor Cyan
javac -d out src\*.java
if ($LASTEXITCODE -eq 0) {
    Write-Host "Launching Plagiarism Checker..." -ForegroundColor Green
    java -cp out Main
} else {
    Write-Host "Compilation failed." -ForegroundColor Red
}
