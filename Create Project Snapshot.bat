@echo off
setlocal enabledelayedexpansion

:: Name of output file
set OUTPUT=project_snapshot.txt

:: List of ignored directories or file patterns (case insensitive)
set IGNORE_LIST=node_modules .git build .gradle .idea *.iml *.class *.exe *.dll

:: Delete output file if it exists
if exist %OUTPUT% del %OUTPUT%

echo Exporting project files to %OUTPUT%...

:: Recursively process files
for /r %%F in (*) do (
    set "skipFile=0"
    set "filepath=%%F"

    :: Check if filename or path contains any ignored patterns
    for %%I in (%IGNORE_LIST%) do (
        echo !filepath! | findstr /i /c:"%%I" >nul && (
            set skipFile=1
        )
    )

    if !skipFile! EQU 0 (
        echo ------------------------------------------------------------- >> %OUTPUT%
        echo File: %%F >> %OUTPUT%
        echo ------------------------------------------------------------- >> %OUTPUT%
        type "%%F" >> %OUTPUT%
        echo. >> %OUTPUT%
        echo. >> %OUTPUT%
    )
)

echo Done. Output file created: %OUTPUT%
pause
exit /b