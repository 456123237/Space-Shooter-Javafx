@echo off
REM =============================================================================
REM Configuration
REM =============================================================================

REM Check if the PATH_TO_FX environment variable is set
if "%PATH_TO_FX%"=="" (
    echo [ERROR] PATH_TO_FX environment variable is not set.
    echo Please set the environment variable pointing to your JavaFX 'lib' folder.
    echo Example command: setx PATH_TO_FX "C:\Program Files\Java\javafx-sdk-21\lib"
    pause
    exit /b 1
)

echo Using JavaFX Path: "%PATH_TO_FX%"

REM =============================================================================
REM Compilation
REM =============================================================================

echo Cleaning previous build...
if exist bin (
    rmdir /s /q bin
)
mkdir bin

echo Compiling source code...
REM Compiles all java files in the application package
javac --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.media -d bin src\application\*.java

if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

REM =============================================================================
REM Execution
REM =============================================================================

echo Starting Space Shooter...
java --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.media -cp bin application.Main

if %errorlevel% neq 0 (
    echo [ERROR] Application crashed or failed to start.
    pause
)