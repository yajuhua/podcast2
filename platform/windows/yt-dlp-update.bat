set PWD=%~dp0
set "PATH=%PWD%;%PATH%"
for /f %%i in ('yt-dlp --version') do set NOW_VERSION=%%i
@echo off
setlocal EnableDelayedExpansion


for /f "delims=" %%i in ('powershell -Command "(Invoke-WebRequest -Uri 'https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest' -UseBasicParsing).Content | ConvertFrom-Json | Select -ExpandProperty tag_name"') do (
    set "LATEST_VERSION=%%i"
)

set "ARCH=%PROCESSOR_ARCHITECTURE%"

if "%LATEST_VERSION%" == "%NOW_VERSION%" (
    echo no update
    echo %NOW_VERSION%
) else (
    echo has update
    powershell -Command "Invoke-WebRequest -Uri 'https://github.moeyy.xyz/https://github.com/yt-dlp/yt-dlp/releases/download/%LATEST_VERSION%/yt-dlp.exe' -OutFile '%PWD%yt-dlp.exe'"
    echo update completed 
    yt-dlp --version
)

endlocal
