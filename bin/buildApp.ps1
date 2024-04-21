Set-Location (Split-Path $PSCommandPath)
Set-Location ".."
Get-Location

$zipFile = "dbgps.zip"
$mvnCmd = "{0}\bin\mvn.cmd" -f ${Env:\MAVEN_HOME}

if ( Test-Path $zipFile ) {
  Remove-Item -Path $zipFile
}

Start-Process -Wait -FilePath $mvnCmd -ArgumentList 'clean','package'

Get-ChildItem -path ".\bin\lancio.cmd", "GpsInfo.properties", "target\dbgps.jar", ".\bin\installApp.ps1", ".\bin\installApp.cmd" |
    Compress-Archive  -CompressionLevel Fastest -DestinationPath $zipFile

