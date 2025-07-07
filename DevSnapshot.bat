@echo off
setlocal enabledelayedexpansion
set OUTPUT=essential_dev_files.txt
if exist "%OUTPUT%" del "%OUTPUT%"
echo Exporting ESSENTIAL development files to %OUTPUT%...
echo.

:: Only the most critical extensions
set EXTENSIONS=kt java xml gradle md

:: Directories to ignore
set IGNORE_DIRS=.git .idea build .gradle node_modules

:: Files to ignore - expanded list of non-essential files
set IGNORE_FILES=ExampleInstrumentedTest.kt ExampleUnitTest.kt gradle-wrapper.properties backup_rules.xml data_extraction_rules.xml colors.xml themes.xml local.properties gradle.properties

:: Path patterns to ignore
set IGNORE_PATHS=drawable mipmap androidTest test wrapper

:: Filename patterns to ignore
set IGNORE_PATTERNS=ic_launcher

:: Essential files to always include (even if they match ignore patterns)
set ESSENTIAL_FILES=MainActivity.kt AndroidManifest.xml build.gradle settings.gradle Theme.kt strings.xml Color.kt Type.kt ChessBoard.kt ReadMe.md TODO.md

set fileCount=0
set scannedCount=0

:: Create a temporary file to track processed files
set TEMP_PROCESSED=%TEMP%\processed_files_%RANDOM%.tmp
if exist "%TEMP_PROCESSED%" del "%TEMP_PROCESSED%"

echo Scanning for essential development files...
echo Starting from: %CD%
echo.

call :ProcessDir "%CD%" 0

:: Clean up temporary file
if exist "%TEMP_PROCESSED%" del "%TEMP_PROCESSED%"

echo.
echo ===============================================
echo Essential Files Export Summary:
echo - Total files scanned: %scannedCount%
echo - Essential files exported: %fileCount%
echo ===============================================
echo.
if %fileCount% EQU 0 (
    echo No essential files found to export.
    echo Missing critical files like MainActivity.kt, build.gradle, etc.
)
echo Output file created: %OUTPUT%
pause
exit /b

:ProcessDir
set "currentDir=%~1"
set "depth=%~2"
set "indent="
for /l %%i in (1,1,%depth%) do set "indent=!indent!  "

echo !indent!Scanning: !currentDir:%CD%\=!

:: Process files in current directory FIRST
for %%F in ("%currentDir%\*.*") do (
    set "filepath=%%F"
    set "ext=%%~xF"
    set "ext=!ext:~1!"
    
    :: Only process if it's actually a file (not a directory)
    if exist "!filepath!" (
        if not exist "!filepath!\*" (
            set /a scannedCount+=1
            echo !indent!  Found file: %%~nxF
            call :ProcessFile "!filepath!" "!ext!"
        )
    )
)

:: Then loop through subdirectories
for /d %%D in ("%currentDir%\*") do (
    set "subdir=%%~nxD"
    set "skipDir=0"
    for %%I in (%IGNORE_DIRS%) do (
        if /I "%%I"=="%%~nxD" set "skipDir=1"
    )
    if !skipDir! EQU 0 (
        set /a nextDepth=!depth!+1
        call :ProcessDir "%%D" !nextDepth!
    ) else (
        echo !indent!  Skipping directory: %%D
    )
)
exit /b

:ProcessFile
set "filepath=%~1"
set "ext=%~2"
set "filename=%~nx1"
set "relpath=!filepath:%CD%\=!"

:: Check if file extension is allowed first
set "allowed=0"
for %%E in (%EXTENSIONS%) do (
    if /I "!ext!"=="%%E" set "allowed=1"
)

:: If extension is not allowed, skip immediately
if !allowed! EQU 0 (
    echo     Skip ^(ext^): !relpath!
    exit /b
)

:: Check if this is an essential file that should always be included
set "isEssential=0"
for %%E in (%ESSENTIAL_FILES%) do (
    if /I "!filename!"=="%%E" set "isEssential=1"
)

:: If it's essential, skip all other ignore checks
if !isEssential! EQU 1 (
    goto :ExportFile
)

:: Check if file should be ignored by exact filename match
set "ignore=0"
for %%I in (%IGNORE_FILES%) do (
    if /I "!filename!"=="%%I" set "ignore=1"
)

:: Check if file should be ignored by path pattern
if !ignore! EQU 0 (
    for %%P in (%IGNORE_PATHS%) do (
        echo !relpath! | findstr /i "%%P" >nul && set "ignore=1"
    )
)

:: Check if file should be ignored by filename pattern
if !ignore! EQU 0 (
    for %%P in (%IGNORE_PATTERNS%) do (
        echo !filename! | findstr /i "%%P" >nul && set "ignore=1"
    )
)

if !ignore! EQU 1 (
    echo     Skip ^(ignore^): !relpath!
    exit /b
)

:ExportFile
:: Check if file was already processed
findstr /x /c:"!filepath!" "%TEMP_PROCESSED%" >nul 2>&1
if errorlevel 1 (
    :: File not processed yet - add it to the list
    echo !filepath!>>"%TEMP_PROCESSED%"
    
    set /a fileCount+=1
    if !isEssential! EQU 1 (
        echo     [!fileCount!] ESSENTIAL: !relpath!
    ) else (
        echo     [!fileCount!] Exporting: !relpath!
    )
    
    :: Write to output file with error handling
    >>"%OUTPUT%" (
        echo ----
        echo File: !relpath!
        echo ----
        type "!filepath!" 2>nul || echo [Error reading file content]
        echo.
        echo.
    ) 2>nul
) else (
    echo     Skip ^(duplicate^): !relpath!
)
exit /b