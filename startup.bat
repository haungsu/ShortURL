@echo off
title ShortURLPro Startup Script

echo ================================
echo ShortURLPro Startup Script
echo ================================
echo.

echo Checking environments...

REM Check Java
echo [1/4] Checking Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found. Please install JDK 21+
    echo Download: https://adoptium.net/
    pause
    exit /b 1
)
echo [OK] Java environment ready

REM Check Node.js
echo [2/4] Checking Node.js...
node --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js not found. Please install Node.js
    echo Download: https://nodejs.org/
    pause
    exit /b 1
)
echo [OK] Node.js environment ready

REM Check frontend dependencies
echo [3/4] Checking frontend dependencies...
cd /d "%~dp0ShortURLPro\vue"
if not exist "node_modules" (
    echo Installing frontend dependencies...
    call npm install
    if errorlevel 1 (
        echo [ERROR] Failed to install frontend dependencies
        pause
        exit /b 1
    )
    echo [OK] Frontend dependencies installed
) else (
    echo [OK] Frontend dependencies ready
)
cd /d "%~dp0"

REM Check backend dependencies
echo [4/4] Checking backend dependencies...
call mvnw dependency:resolve >nul 2>&1
echo [OK] Backend dependencies checked

echo.
echo ================================
echo Environment check completed!
echo ================================
echo.
echo Please select startup mode:
echo 1. Start full development environment (requires Docker)
echo 2. Start basic development environment (manual DB setup)
echo 3. Start frontend only
echo 4. Start backend only
echo 5. Exit
echo.

set /p choice=Enter option (1-5): 

if "%choice%"=="1" (
    call :start_full
) else if "%choice%"=="2" (
    call :start_basic
) else if "%choice%"=="3" (
    call :start_frontend
) else if "%choice%"=="4" (
    call :start_backend
) else if "%choice%"=="5" (
    echo Goodbye!
    exit /b 0
) else (
    echo Invalid option
    pause
    goto :eof
)

:start_full
echo Starting full development environment...
echo Starting database and Redis services...
docker compose up -d mysql redis
timeout /t 10 /nobreak >nul

echo Starting backend service...
start "Backend Service" cmd /c "cd /d "%~dp0" && mvnw spring-boot:run && pause"

echo Starting frontend development server...
timeout /t 5 /nobreak >nul
cd /d "%~dp0ShortURLPro\vue"
start "Frontend Dev Server" cmd /c "npm run dev && pause"
cd /d "%~dp0"

echo.
echo ================================
echo Full development environment started!
echo ================================
echo Frontend: http://localhost:5173
echo Backend API: http://localhost:8080
echo Swagger Docs: http://localhost:8080/swagger-ui.html
echo Database: MySQL (localhost:3306)
echo Cache: Redis (localhost:6379)
echo.
pause
goto :eof

:start_basic
echo Starting basic development environment...
echo Note: Please ensure MySQL and Redis are running manually

echo Starting backend service...
start "Backend Service" cmd /c "cd /d "%~dp0" && mvnw spring-boot:run && pause"

echo Starting frontend development server...
timeout /t 5 /nobreak >nul
cd /d "%~dp0ShortURLPro\vue"
start "Frontend Dev Server" cmd /c "npm run dev && pause"
cd /d "%~dp0"

echo.
echo ================================
echo Basic development environment started!
echo ================================
echo Please ensure these services are running:
echo - MySQL database (localhost:3306)
echo - Redis cache service (localhost:6379)
echo.
echo Frontend: http://localhost:5173
echo Backend API: http://localhost:8080
echo Swagger Docs: http://localhost:8080/swagger-ui.html
echo.
pause
goto :eof

:start_frontend
echo Starting frontend development server only...
cd /d "%~dp0ShortURLPro\vue"
npm run dev
goto :eof

:start_backend
echo Starting backend service only...
mvnw spring-boot:run
goto :eof