$startPath="F:\my Foto\2022"
$map = [ordered]@{}
$dupl = 0
class Filetto {
    [string]$name
    $fullPath
    [int]$refs
    [string] ToString() {
        $sz = $($this.fullPath -join ",")
        return "[{0}]{1}`t{2} " -f $this.refs, $this.name,$sz;
    }
}
Get-ChildItem -Path $startPath  -Recurse  | where { ! $_.PSIsContainer } | ForEach-Object {
    $curr = $_
    $obj = New-Object Filetto
    $nam = $curr.PSChildName
    if (  $curr.Extension -ieq ".jpg" ) {
        $obj.name = $nam
        $obj.fullPath = New-Object System.Collections.ArrayList(4)
        $obj.fullPath.Add($curr.FullName) | out-null
        $obj.refs = 1
        if ( ! $map.Contains($nam) ) {
            $map[$nam] = $obj
        } else {
            # Write-Host $nam
            $dupl++
            $obj = $map[$nam]
            $obj.refs += 1
            $obj.fullPath.add($curr.FullName)  | out-null
            $map[$nam] = $obj
            Write-Host $obj.ToString()
        }
    }
}
$qta = $map.Keys.Count
Write-Host ("Qta = {0}, Dupl={1}" -f $qta, $dupl)