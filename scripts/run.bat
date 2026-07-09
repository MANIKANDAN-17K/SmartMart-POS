@echo off
REM SmartMart POS - Run script for Windows

cd /d "%~dp0.."

REM Check if Java is installed
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo Java is not installed. Please install Java 21 LTS.
    pause
    exit /b 1
)

REM Build the project if needed
if not exist "target\SmartMart-POS-1.0-SNAPSHOT.jar" (
    echo Building project...
    mvn clean package
)

REM Run the application
echo Starting SmartMart POS...
java -jar target\SmartMart-POS-1.0-SNAPSHOT.jar
pause