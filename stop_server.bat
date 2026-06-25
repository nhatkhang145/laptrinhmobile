@echo off
title Dung Server Zappy
echo ===================================================
echo   DANG TAT SERVER ZAPPY (PORT 8080)...
echo ===================================================
echo.

:: Tim va tat tien trinh dang chay o cong 8080
FOR /F "tokens=5" %%T IN ('netstat -a -n -o ^| findstr :8080') DO (
    echo Dang tat tien trinh PID: %%T...
    taskkill /F /PID %%T >nul 2>&1
)

echo.
echo DA TAT THANH CONG! Ban co the tat cua so nay.
pause
