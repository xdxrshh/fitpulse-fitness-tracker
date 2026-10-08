@echo off
echo ======================================================================
echo   Running FitPulse Phase 2 OOP Rubric Verification Test
echo ======================================================================
java -cp "bin;lib/mysql-connector-j-8.3.0.jar;lib/flatlaf-3.4.1.jar;src/resources" com.fitpulse.Phase2OOPVerificationTest
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Test failed with error code %ERRORLEVEL%.
)
