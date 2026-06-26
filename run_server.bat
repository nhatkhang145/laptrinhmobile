@echo off
title Zappy Backend Server
echo ===================================================
echo   DANG KHOI DONG SERVER ZAPPY BACKEND (SPRING BOOT)
echo ===================================================
echo.
cd backend
call apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
pause
