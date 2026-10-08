@echo off
echo ======================================================================
echo   Starting FitPulse MySQL/MariaDB Server
echo ======================================================================
start "MariaDB Server" "C:\Program Files\MariaDB 13.0\bin\mysqld.exe" --console
echo Server process initiated on port 3306.
timeout /t 2 >nul
