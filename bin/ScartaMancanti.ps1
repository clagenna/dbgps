$dirSrc = "F:\My Foto\2024\Peio Febbraio 2024"
$dstDir = "F:\My Foto\2024\2024-02-19 Peio"

$arr = [System.Collections.ArrayList]::new()

Get-ChildItem -Path $dirSrc -File | ForEach-Object {
    $fi = $_.PSChildName
    $dst = "{0}\{1}" -f $dstDir, $fi
    if ( ! (Test-Path -Path $dst)) {
        $arr.Add($_)
    }
}
foreach ( $f in $arr ) {
    Write-Host $f
}