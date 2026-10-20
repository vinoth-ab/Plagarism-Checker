@echo off
cd /d "%~dp0PlagiarismChecker"
if not exist "out" mkdir out
echo Compiling Plagiarism Checker...
javac -d out src\*.java
if %ERRORLEVEL% equ 0 (
    echo Launching Plagiarism Checker...
    start javaw -cp out Main
) else (
    echo Compilation failed.
    pause
)
