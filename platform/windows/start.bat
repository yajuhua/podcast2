set PWD=%~dp0
set JAVA=%PWD%jre8/bin
set DATA=%PWD%data
set "PATH=%JAVA%;%PWD%;%PATH%"
java -version
start /b %PWD%aria2c.exe --conf-path=%PWD%aria2.conf > aria2.log
echo "aria2RPC start"
chcp 65001
java -jar -Dfile.encoding=utf-8 app.jar --podcast2.data.data-path=%DATA% 
