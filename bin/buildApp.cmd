set LUOGO=%~dp0
cd /d "%LUOGO%\.."
pushd "%LUOGO%"
cd

pwsh -f buildApp.ps1
popd
pause 