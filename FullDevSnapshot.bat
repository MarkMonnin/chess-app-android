@echo off
setlocal enabledelayedexpansion
set OUTPUT=project_dev_snapshot.txt
if exist "%OUTPUT%" del "%OUTPUT%"
echo Exporting relevant development files to %OUTPUT%...
echo.

set EXTENSIONS=kt java xml gradle properties json md txt yaml yml gitignore
set IGNORE_DIRS=.git .idea build .gradle node_modules app\build
set fileCount=0

:: Create a temporary file to track processed files
set TEMP_PROCESSED=%TEMP%\processed_files_%RANDOM%.tmp
if exist "%TEMP_PROCESSED%" del "%TEMP_PROCESSED%"

call :ProcessDir "%CD%"

:: Clean up temporary file
if exist "%TEMP_PROCESSED%" del "%TEMP_PROCESSED%"

echo.
echo Export complete. Total files exported: %fileCount%
echo Output file created: %OUTPUT%
pause
exit /b

:ProcessDir
set "currentDir=%~1"

:: Loop through subdirectories first
for /d %%D in ("%currentDir%\*") do (
    set "subdir=%%~nxD"
    set "skipDir=0"
    for %%I in (%IGNORE_DIRS%) do (
        if /I "%%I"=="%%~nxD" set "skipDir=1"
    )
    if !skipDir! EQU 0 (
        call :ProcessDir "%%D"
    ) else (
        echo Skipping directory: %%D
    )
)

:: Process files in current directory
for %%F in ("%currentDir%\*.*") do (
    set "filepath=%%F"
    set "ext=%%~xF"
    set "ext=!ext:~1!"
    
    :: Only process if it's actually a file (not a directory)
    if exist "!filepath!" (
        if not exist "!filepath!\*" (
            call :ProcessFile "!filepath!" "!ext!"
        )
    )
)
exit /b

:ProcessFile
set "filepath=%~1"
set "ext=%~2"

:: Check if file extension is allowed
set "allowed=0"
for %%E in (%EXTENSIONS%) do (
    if /I "!ext!"=="%%E" set "allowed=1"
)

if !allowed! EQU 1 (
    :: Check if file was already processed using temporary file
    findstr /x /c:"!filepath!" "%TEMP_PROCESSED%" >nul 2>&1
    if errorlevel 1 (
        :: File not processed yet - add it to the list
        echo !filepath!>>"%TEMP_PROCESSED%"
        
        set /a fileCount+=1
        echo Exporting file #!fileCount!: !filepath!
        
        :: Write to output file with error handling
        >>"%OUTPUT%" (
            echo -------------------------------------------------------------
            echo File: !filepath!
            echo -------------------------------------------------------------
            type "!filepath!" 2>nul || echo [Error reading file content]
            echo.
            echo.
        ) 2>nul
    ) else (
        echo Skipping duplicate: !filepath!
    )
) else (
    echo Checking file: !filepath! ^(extension: !ext!^) - not in allowed list
)
exit /b