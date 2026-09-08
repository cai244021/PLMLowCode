@echo off
setlocal
cd /d "%~dp0"
"C:\Program Files\PowerShell\7\pwsh.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0app\scripts\start-all.ps1"
set "EXIT_CODE=%ERRORLEVEL%"
echo.
if not "%EXIT_CODE%"=="0" echo Start failed. See the message and app\logs.
pause
exit /b %EXIT_CODE%
