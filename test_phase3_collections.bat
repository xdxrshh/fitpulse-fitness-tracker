@echo off
echo ======================================================================
echo   Running FitPulse Phase 3 Collections & Generics Verification Test
echo ======================================================================
java -cp "bin;lib/mysql-connector-j-8.3.0.jar;lib/flatlaf-3.4.1.jar;src/resources" com.fitpulse.Phase3CollectionsVerificationTest
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Test failed with error code %ERRORLEVEL%.
)
