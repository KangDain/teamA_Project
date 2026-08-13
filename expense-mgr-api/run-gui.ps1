[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host " 💰 expense-mgr-api REST API Swing GUI Tester Launching..." -ForegroundColor Green
Write-Host "=========================================================" -ForegroundColor Cyan

$jarPath = "$PSScriptRoot\target\expense-mgr-api-1.0.0.jar"

if (Test-Path $jarPath) {
    java -cp $jarPath gui.ApiTestGui
} else {
    Write-Host "Jar file not found. Building and running via Maven..." -ForegroundColor Yellow
    & C:\Java\apache-maven-3.9.6\bin\mvn.cmd exec:java -Dexec.mainClass="gui.ApiTestGui" -f "$PSScriptRoot\pom.xml"
}
