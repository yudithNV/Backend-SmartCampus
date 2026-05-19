@echo off
cd /d "%~dp0"
echo Compilando proyecto Smart Campus...
call mvnw.cmd clean compile 2>&1
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo COMPILACION EXITOSA
    echo ========================================
    exit /b 0
) else (
    echo.
    echo ========================================
    echo ERROR DE COMPILACION
    echo ========================================
    exit /b 1
)
