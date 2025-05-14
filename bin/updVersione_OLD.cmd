pushd "%~dp0"
cd
pwsh -f "UpdVersionJava.ps1"
popd
@echo..
@echo ... done !
pause