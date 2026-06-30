@echo off
setlocal

cd /d "%~dp0"

set "PORT=8080"
if not "%~1"=="" (
    if "%~1"=="/?" goto :help
    if /I "%~1"=="-h" goto :help
    if /I "%~1"=="--help" goto :help
    set "PORT=%~1"
)

where powershell >nul 2>nul
if errorlevel 1 (
    echo [ERROR] PowerShell is required but was not found.
    echo Please install or enable Windows PowerShell, then run this file again.
    pause
    exit /b 1
)

echo Starting Airport Flight Simulation...
echo.
echo Local URL: http://localhost:%PORT%/dashboard
echo Data import: http://localhost:%PORT%/operations/data
echo.
echo Keep this window open while using the system.
echo Press Ctrl+C to stop the server.
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-anywhere.ps1" -Port %PORT%
set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
    echo.
    echo [ERROR] Server exited with code %EXIT_CODE%.
    pause
)

exit /b %EXIT_CODE%

:help
echo Usage:
echo   start.bat        Start on default port 8080
echo   start.bat 8090   Start on port 8090
echo.
echo Then open:
echo   http://localhost:PORT/dashboard
exit /b 0
