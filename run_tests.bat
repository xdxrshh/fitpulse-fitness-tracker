@echo off
setlocal enabledelayedexpansion
title FitPulse - Rubric Verification Test Suite

echo =====================================================================
echo  Running FitPulse Automated Rubric Verification Suite...
echo =====================================================================

cd /d "%~dp0"

if not exist "bin\com\fitpulse\App.class" (
    echo Binaries not found. Running compile.bat first...
    call compile.bat
    if !errorlevel! neq 0 (
        echo Compilation failed. Exiting.
        pause
        exit /b !errorlevel!
    )
)

set "JAVA_CMD=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"

"%JAVA_CMD%" -cp "bin;lib\mysql-connector-j-8.3.0.jar;lib\flatlaf-3.4.1.jar;lib\h2-2.2.224.jar" com.fitpulse.RubricVerificationTest
pause
