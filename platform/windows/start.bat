@echo off
setlocal enabledelayedexpansion

set PWD=%~dp0
set JAVA=%PWD%jre8/bin
set DATA=%PWD%data
set "PATH=%JAVA%;%PWD%;%PATH%"
set "directory=%PWD%lib"
set "classpath=%PWD%classes"

for /r "%directory%" %%I in (*) do (
    set "classpath=!classpath!;%%~fI"
)
start /b %PWD%aria2c.exe --conf-path=%PWD%aria2.conf > aria2.log
echo "aria2RPC start"
chcp 65001
java -Dfile.encoding=utf-8 -classpath "!classpath!" -Dpodcast2.data.data-path=%DATA% io.github.yajuhua.podcast2.Podcast2Application
endlocal