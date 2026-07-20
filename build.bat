@echo off
:: ═══════════════════════════════════════════════════════════════
::  build.bat — Compile and run Gym Management System (Windows)
::  Usage: Double-click or run from VS Code terminal
:: ═══════════════════════════════════════════════════════════════

set SRC_DIR=src
set OUT_DIR=out
set LIB_DIR=lib
set MAIN_CLASS=com.gym.server.GymServer
set JAR_NAME=gym-server.jar

echo.
echo   IronPulse Gym - Build Script (Windows)
echo.

:: Verify Java
where javac >nul 2>nul
if errorlevel 1 (
  echo   ERROR: javac not found. Install JDK 17+ and set JAVA_HOME.
  pause & exit /b 1
)
echo   [OK] Java detected

:: Verify MySQL driver
if not exist "%LIB_DIR%\mysql-connector-j.jar" (
  echo   ERROR: lib\mysql-connector-j.jar not found.
  echo   Download from: https://dev.mysql.com/downloads/connector/j/
  pause & exit /b 1
)
echo   [OK] MySQL connector found

:: Compile
echo.
echo   Compiling...
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

dir /s /b %SRC_DIR%\*.java > sources.txt
javac -cp "%LIB_DIR%\mysql-connector-j.jar" -d "%OUT_DIR%" @sources.txt
del sources.txt

if errorlevel 1 (
  echo   ERROR: Compilation failed.
  pause & exit /b 1
)
echo   [OK] Compilation successful

:: Package JAR
echo   Packaging JAR...
echo Main-Class: %MAIN_CLASS%> "%OUT_DIR%\manifest.txt"
echo Class-Path: lib/mysql-connector-j.jar>> "%OUT_DIR%\manifest.txt"
jar cfm %JAR_NAME% "%OUT_DIR%\manifest.txt" -C "%OUT_DIR%" .
echo   [OK] %JAR_NAME% created

:: Run
echo.
echo   Starting Gym Server...
echo   Open browser: http://localhost:8080
echo   Press Ctrl+C to stop
echo.
java -cp "%JAR_NAME%;%LIB_DIR%\mysql-connector-j.jar" %MAIN_CLASS%
pause
