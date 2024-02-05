Add-Type -AssemblyName System.Windows.Forms

Set-Location (Split-Path $PSCommandPath)

$zipApp = ".\dbgps.zip"
$zipJdk = "openjdk-21.0.1_windows-x64_bin.zip"
$zipJfx = "openjfx-21.0.1_windows-x64_bin-sdk.zip"

$javaDir = "C:\Program Files\java"
$jdkDir  = "$javaDir\jdk-21.0.1"
$jfxDir  = "$javaDir\javafx-sdk-21.0.1"

$FileBrowser = New-Object System.Windows.Forms.FolderBrowserDialog -Property @{ 
    InitialDirectory = [Environment]::GetFolderPath('Desktop');
    Description ="Scegli il direttorio dove vuoi installare Imaging"
}
$dirName=$null
if ( $FileBrowser.showDialog() -eq [System.Windows.Forms.DialogResult]::OK) {
    $dirName = $FileBrowser.SelectedPath
    Write-Host "Dir selected = $dirName"
}
if ( $null -eq $dirName ) {
    Write-Host "Non hai selezionato nulla!" -ForegroundColor Red
    exit
}

Expand-Archive -DestinationPath "$dirName\dbgps" -force -LiteralPath $zipApp

# test se eseguito come amministratore di sistema
$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
$isAdmin = $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
# test verifica se esistono gia' i direttori
$isDirs = (Test-Path -Path $javaDir ) -or (Test-Path -Path $jdkDir ) -or (Test-Path -Path $jfxDir  )
if (! $isDirs ) {
    if ( ! $isAdmin) {
        Write-host  "Non sei amministratore!" -ForegroundColor Red
        exit
    }
}
# sono amministratore, posso partire con l'istallazione
Write-host  "Eseguo come amministratore!" -ForegroundColor green

$addPath = $false
if (! (Test-Path -Path $javaDir )) {
   Write-host  ("Creo dir:" + $javaDir) -ForegroundColor green
   New-Item -Path $javaDir -ItemType Directory -Force | out-null
}
$jdkPath = $null
if (! (Test-Path -Path $jdkDir )) {
    Write-host  ("Creo dir:" + $jdkDir) -ForegroundColor green
    Expand-Archive -DestinationPath $javaDir -force -LiteralPath $zipJdk
    [Environment]::SetEnvironmentVariable("JAVA_HOME", $jdkDir, "Machine")
    $addPath = $true
    $pth = Get-ChildItem -Path $javaDir -Filter "java.exe" -Recurse
    $jdkPath = $pth.Directory.FullName
}

if (! (Test-Path -Path $jfxDir) ) {
    Write-host  ("Creo dir:" + $jfxDir) -ForegroundColor green
    Expand-Archive -DestinationPath $javaDir -force -LiteralPath $zipJfx
    [Environment]::SetEnvironmentVariable("JAVAFX_HOME", $jfxDir, "Machine")
}
if ( $addPath -and $null -ne $jdkPath) {
    $PathVar = [Environment]::GetEnvironmentVariable("PATH", "Machine")
    if (! $PathVar.contains("jdk-21")) {
       Write-host  ("Aggiungo a PATH il javadir") -ForegroundColor green
       $PathVar = $PathVar  + [IO.Path]::PathSeparator + ( "{0}\bin" -f $jdkDir )
       $PathVar = $PathVar.Replace(";;",";")
      [Environment]::SetEnvironmentVariable( "Path", $PathVar, "Machine" )
       foreach ( $sz in $PathVar.split(";")) {
         Write-Host ("PATH={0}" -f $sz)
       }
    } else {
      Write-host  ("Gia presente in PATH il javadir") -ForegroundColor yellow
    }
}
