$DirDelFiles="F:\My Foto\2020\onedrive"
$DirCompare="F:\My Foto\2024\2024-03-01 Marocco"

$FilesToDel = New-Object System.Collections.ArrayList(4)

Get-ChildItem -Path $DirDelFiles -File | ForEach-Object {
    $filNam = $_.PSChildName
    $second = "{0}\{1}" -f $DirCompare, $filNam
    if ( Test-Path $second -PathType Leaf) {
        $FilesToDel.Add($_)
    }
}
foreach ( $fi in $FilesToDel) {
    Remove-Item -Path $fi -Force
    Write-Host ("Remove-Item  {0}" -f $fi)
}