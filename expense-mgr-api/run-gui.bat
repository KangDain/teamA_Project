@echo off
chcp 65001 > nul
echo =========================================================
echo  expense-mgr-api REST API Swing GUI Tester Launching...
echo =========================================================
java -cp "%~dp0target\expense-mgr-api-1.0.0.jar" gui.ApiTestGui
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo target jar not found or failed. Running via Maven...   
    call C:\Java\apache-maven-3.9.6\bin\mvn.cmd exec:java -Dexec.mainClass="gui.ApiTestGui" -f "%~dp0pom.xml"
)
pause
