<#  Verifica se all'interno del direttorio
    $DirDelFiles
    esistono foto con lo stesso nome presenti anche in
    $DirCompare
    Se presente e $remove è true allora elimina 
    la foto in
    $DirDelFiles
#>

$DirDelFiles="F:\My Foto\2023\onedrive"
$DirCompare="F:\My Foto\2023"
$remove = $true

class FotoFile {
    [string]$Nome
    [string]$path
    [string]$parent
    [long]$size
    [datetime]$creationTime

}

$map = @{}
$dels = New-Object System.Collections.ArrayList(4)


Get-ChildItem -Path $DirCompare -File -Recurse | ForEach-Object {
    $fotCom = New-Object -TypeName FotoFile
    $fotCom.nome = $_.PSChildName
    $fotCom.path = $_
    $fotCom.size = $_.Length
    $fotCom.creationTime = $_.CreationTime
    $fotCom.parent = Split-Path $_ -Parent
    if ( ! ( $fotCom.parent.equals($DirDelFiles) ) ) {
      $map[$fotCom.nome] = $fotCom
    } else {
        Write-Debug "nel dir Dels"
    }
}
Write-Host ("Qta Files {0}" -f $map.Count )
Get-ChildItem -Path $DirDelFiles -File | ForEach-Object {
    $fotCom = New-Object -TypeName FotoFile
    $fotCom.nome = $_.PSChildName
    $fotCom.path = $_
    $fotCom.parent = $_.Parent.FullName
    if ( $map.Keys  -iContains $fotCom.nome ) {
      $dels.Add($_) | out-null
      if ( $remove) {
        Write-Host ("del `"{0}`"" -f $_.FullName )
        Remove-Item -Path $_ -Force
      }
    }
}
Write-Host ("Qta Deletes {0}" -f $dels.Count )
write-host "Fine"