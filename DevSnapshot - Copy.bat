@echo off
setlocal enabledelayedexpansion
set OUTPUT=project_dev_snapshot.txt
if exist "%OUTPUT%" del "%OUTPUT%"
echo Exporting essential development files to %OUTPUT%...
echo.

:: Essential extensions for Android development
set EXTENSIONS=kt java xml gradle properties

:: Directories to ignore
set IGNORE_DIRS=.git .idea build .gradle node_modules

:: Specific files to ignore
set IGNORE_FILES=ExampleInstrumentedTest.kt ExampleUnitTest.kt gradle-wrapper.properties backup_rules.xml data_extraction_rules.xml

:: Path patterns to ignore
set IGNORE_PATHS=ui\theme drawable mipmap

:: Filename patterns to ignore
set IGNORE_PATTERNS=ic_launcher

set fileCount=0
set scannedCount=0

:: Create a temporary file to track processed files
set TEMP_PROCESSED=%TEMP%\processed_files_%RANDOM%.tmp
if exist "%TEMP_PROCESSED%" del "%TEMP_PROCESSED%"

echo Scanning project structure...
echo Starting from: %CD%
echo.

:: First, let's specifically check for the key files we expect
echo ===============================================
echo DIAGNOSTIC: Checking for key files...
echo ===============================================

set "mainActivity=%CD%\app\src\main\java\com\example\android\chessapp\MainActivity.kt"
if exist "!mainActivity!" (
    echo FOUND: MainActivity.kt at !mainActivity!
) else (
    echo MISSING: MainActivity.kt expected at !mainActivity!
)

set "manifest=%CD%\app\src\main\AndroidManifest.xml"
if exist "!manifest!" (
    echo FOUND: AndroidManifest.xml at !manifest!
) else (
    echo MISSING: AndroidManifest.xml expected at !manifest!
)

set "appBuild=%CD%\app\build.gradle"
if exist "!appBuild!" (
    echo FOUND: app/build.gradle at !appBuild!
) else (
    echo MISSING: app/build.gradle expected at !appBuild!
)

set "rootBuild=%CD%\build.gradle"
if exist "!rootBuild!" (
    echo FOUND: build.gradle at !rootBuild!
) else (
    echo MISSING: build.gradle expected at !rootBuild!
)

set "settings=%CD%\settings.gradle"
if exist "!settings!" (
    echo FOUND: settings.gradle at !settings!
) else (
    echo MISSING: settings.gradle expected at !settings!
)

echo.
echo ===============================================
echo DIAGNOSTIC: Listing all files in key directories...
echo ===============================================

echo Files in root directory:
dir /b "%CD%\*.gradle" "%CD%\*.properties" "%CD%\*.kt" "%CD%\*.java" "%CD%\*.xml" 2>nul

echo.
echo Files in app directory:
dir /b "%CD%\app\*.gradle" "%CD%\app\*.properties" "%CD%\app\*.kt" "%CD%\app\*.java" "%CD%\app\*.xml" 2>nul

echo.
echo Files in app\src\main:
dir /b "%CD%\app\src\main\*.gradle" "%CD%\app\src\main\*.properties" "%CD%\app\src\main\*.kt" "%CD%\app\src\main\*.java" "%CD%\app\src\main\*.xml" 2>nul

echo.
echo Files in main package directory:
dir /b "%CD%\app\src\main\java\com\example\android\chessapp\*.gradle" "%CD%\app\src\main\java\com\example\android\chessapp\*.properties" "%CD%\app\src\main\java\com\example\android\chessapp\*.kt" "%CD%\app\src\main\java\com\example\android\chessapp\*.java" "%CD%\app\src\main\java\com\example\android\chessapp\*.xml" 2>nul

echo.
echo ===============================================
echo Starting normal scan...
echo ===============================================

call :ProcessDir "%CD%" 0

:: Clean up temporary file
if exist "%TEMP_PROCESSED%" del "%TEMP_PROCESSED%"

echo.
echo ===============================================
echo Scan Summary:
echo - Total files scanned: %scannedCount%
echo - Files exported: %fileCount%
echo ===============================================
echo.
if %fileCount% EQU 0 (
    echo No essential files found to export.
    echo This might be a new project without MainActivity.kt or build files yet.
    echo.
    echo Expected files that weren't found:
    echo - MainActivity.kt
    echo - AndroidManifest.xml
    echo - build.gradle files
    echo - settings.gradle
    echo - gradle.properties
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

:: Check if file was already processed
findstr /x /c:"!filepath!" "%TEMP_PROCESSED%" >nul 2>&1
if errorlevel 1 (
    :: File not processed yet - add it to the list
    echo !filepath!>>"%TEMP_PROCESSED%"
    
    set /a fileCount+=1
    echo     [!fileCount!] Exporting: !relpath!
    
    :: Write to output file with error handling
    >>"%OUTPUT%" (
        echo -------------------------------------------------------------
        echo File: !relpath!
        echo -------------------------------------------------------------
        type "!filepath!" 2>nul || echo [Error reading file content]
        echo.
        echo.
    ) 2>nul
) else (
    echo     Skip ^(duplicate^): !relpath!
)
exit /b