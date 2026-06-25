@echo off
title Khoi Dong Lai Server Zappy
echo ===================================================
echo   KHOI DONG LAI SERVER ZAPPY BACKEND
echo ===================================================
echo.

:: 1. Tat server cu neu dang chay
echo [1/2] Dang tat Server cu...
FOR /F "tokens=5" %%T IN ('netstat -a -n -o ^| findstr :8080') DO (
    taskkill /F /PID %%T >nul 2>&1
)
timeout /t 2 /nobreak >nul

:: 2. Chay server moi
echo [2/2] Dang bat Server moi...
start cmd /c "run_server.bat"

echo.
echo DA KHOI DONG LAI! Mot cua so moi da duoc mo.
pause
