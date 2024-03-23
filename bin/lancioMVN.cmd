@echo off
:: mvn exec:java -Dexec.mainClass=sm.clagenna.dbgps.javafx.MainAppGpsInfo
cd /d %~dp0

for %%i in (%CD%) do set curr=%%~ni
:: echo Curr Dir=%curr%
if "%curr%" == "bin" cd ..

mvn exec:java -Dexec.mainClass=sm.clagenna.dbgps.javafx.MainAppGpsInfo
pause
