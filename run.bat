@echo off
title FitPulse Fitness Tracker
setlocal
cd /d "%~dp0"

set "JAVA_CMD=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
set "CP=bin;lib\mysql-connector-j-8.3.0.jar;lib\flatlaf-3.4.1.jar;lib\h2-2.2.224.jar"

"%JAVA_CMD%" -cp "%CP%" com.fitpulse.App
if errorlevel 1 (
    echo.
    echo Application exited with an error.
    pause
)
