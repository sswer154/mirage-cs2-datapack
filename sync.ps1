param(
    [string]$Title = "sync: 更新数据包/模组/资源包",
    [string]$Body = "",
    [switch]$Push
)
$repo = "F:\mc\sync\mirage-cs2-datapack"
$pairs = @(
    @{ From = "F:\mc\cs2server\world\datapacks\cs2_bomb_datapack"; To = "$repo\数据包\cs2_bomb_datapack"; Data = $true },
    @{ From = "F:\mc\cs2server\mods"; To = "$repo\模组\mods\服务端模组"; Data = $false },
    @{ From = "F:\mc\.minecraft\versions\1.21.1 tacz1.1.8\mods"; To = "$repo\模组\mods\客户端模组"; Data = $false },
    @{ From = "F:\mc\.minecraft\versions\1.21.1 tacz1.1.8\resourcepacks"; To = "$repo\资源包"; Data = $false }
)
foreach ($p in $pairs) {
    if (-not (Test-Path $p.From)) { continue }
    New-Item -ItemType Directory -Path $p.To -Force | Out-Null
    if ($p.Data) {
        Copy-Item "$($p.From)\*" $p.To -Recurse -Force
    } else {
        Get-ChildItem $p.From -File | Where-Object { $_.Name -like 'cs2*' } | Copy-Item -Destination $p.To -Force
    }
}

# 数据包目录是源：复制完成后重打包 zip，避免两者不一致
$zipSrc = Join-Path $repo "数据包\cs2_bomb_datapack"
$zipPath = Join-Path $repo "数据包\cs2_bomb_datapack.zip"
if (Test-Path $zipSrc) {
    if (Test-Path $zipPath) { Remove-Item $zipPath -Force }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::CreateFromDirectory($zipSrc, $zipPath, [System.IO.Compression.CompressionLevel]::Optimal, $false)
    Write-Output "已重打包数据包 zip"
}

Set-Location $repo
$changes = & git status --porcelain
if (-not $changes) { Write-Output "没有变更需要同步"; return }
Write-Output "待提交变更："
$changes | ForEach-Object { "  " + $_.Trim() }
& git add -A
if ($Body) { & git commit -m $Title -m $Body | Out-Null } else { & git commit -m $Title | Out-Null }
if ($LASTEXITCODE -ne 0) { Write-Output "提交失败"; return }
Write-Output "已本地提交：$Title"
if ($Push) { & git push origin HEAD 2>&1 | Select-Object -Last 3 } else { Write-Output "未推送（加 -Push 才推）" }
