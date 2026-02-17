@echo off
echo Building REST API Tester for Windows...

:: Cleanup
if exist build rmdir /s /q build
if exist dist rmdir /s /q dist
mkdir build
mkdir dist

:: Compile
echo Compiling Java source...
javac -d build RestApiTester.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed
    exit /b %ERRORLEVEL%
)

:: Create JAR
echo Creating JAR file...
jar cfe dist\RestApiTester.jar com.apitester.RestApiTester -C build .
if %ERRORLEVEL% NEQ 0 (
    echo JAR creation failed
    exit /b %ERRORLEVEL%
)

:: Build Windows app-image or EXE
echo Creating Windows application...
:: Check if jpackage is in path
where jpackage >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo jpackage not found in PATH. Please ensure JDK 17+ is installed and in PATH.
    exit /b 1
)

jpackage ^
    --type app-image ^
    --input dist ^
    --dest dist ^
    --name RestApiTester ^
    --main-jar RestApiTester.jar ^
    --main-class com.apitester.RestApiTester ^
    --icon RestApiTester.ico ^
    --win-shortcut ^
    --win-menu ^
    --verbose

if %ERRORLEVEL% EQU 0 (
    echo Windows Application created in dist\RestApiTester
    echo To run the application:
    echo   dist\RestApiTester\RestApiTester.exe
) else (
    echo Failed to create Windows application
)

pause
