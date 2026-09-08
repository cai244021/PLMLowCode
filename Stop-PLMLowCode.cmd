@echo off
setlocal
cd /d "%~dp0"
"C:\Program Files\PowerShell\7\pwsh.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0app\scripts\stop-all.ps1"
set "EXIT_CODE=%ERRORLEVEL%"
echo.
if not "%EXIT_CODE%"=="0" echo Stop failed. See the message above.
pause
exit /b %EXIT_CODE%
