param(
    [string]$Title = "sync: 更新数据包/模组/资源包",
    [string]$Body = "",
    [switch]$Push,
    [string]$ServerRoot = "F:\mc\cs2server",
    [string]$ClientRoot = "F:\mc\.minecraft\versions\1.21.1 tacz1.1.8"
)

$ErrorActionPreference = 'Stop'
$repo = $PSScriptRoot
$branch = & git -C $repo branch --show-current
if ($LASTEXITCODE -ne 0 -or -not $branch -or $branch -in @('master', 'main')) {
    throw '请在仓库的独立工作分支运行 sync.ps1。'
}
$legacyModPath = Join-Path $repo '模组\mods'
if ((Test-Path -LiteralPath $legacyModPath) -and
        (Get-ChildItem -LiteralPath $legacyModPath -File -Filter 'cs2*.jar')) {
    throw '模组/mods 根目录有旧位置的 JAR。请先核对并处理重复的构建产物。'
}
$changes = & git -C $repo status --porcelain
if ($LASTEXITCODE -ne 0 -or $changes) {
    throw '当前工作区有未提交的改动，请使用干净的工作分支同步。'
}

$datapack = '数据包/cs2_bomb_datapack'
$archive = '数据包/cs2_bomb_datapack.zip'
$pairs = @(
    @{ From = Join-Path $ServerRoot 'world\datapacks\cs2_bomb_datapack'; To = $datapack; Filter = $null },
    @{ From = Join-Path $ServerRoot 'mods'; To = '模组/mods/服务端模组'; Filter = 'cs2*.jar' },
    @{ From = Join-Path $ClientRoot 'mods'; To = '模组/mods/客户端模组'; Filter = 'cs2*.jar' },
    @{ From = Join-Path $ClientRoot 'resourcepacks'; To = '资源包'; Filter = 'cs2*.zip' }
)
foreach ($pair in $pairs) {
    if (-not (Test-Path -LiteralPath $pair.From -PathType Container)) { continue }
    $destination = Join-Path $repo $pair.To
    New-Item -ItemType Directory -Path $destination -Force | Out-Null
    if ($pair.Filter) {
        Get-ChildItem -LiteralPath $pair.From -File -Filter $pair.Filter |
            Copy-Item -Destination $destination -Force
    } else {
        Get-ChildItem -LiteralPath $pair.From -Force |
            Copy-Item -Destination $destination -Recurse -Force
    }
}

$zipSrc = Join-Path $repo $datapack
$zipPath = Join-Path $repo $archive
$packChanges = & git -C $repo status --porcelain -- $datapack
if ($LASTEXITCODE -ne 0) { throw '无法检查数据包变更。' }
if ((Test-Path -LiteralPath $zipSrc -PathType Container) -and
        ($packChanges -or -not (Test-Path -LiteralPath $zipPath))) {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $temporaryZip = "$zipPath.$([Guid]::NewGuid()).tmp"
    try {
        [System.IO.Compression.ZipFile]::CreateFromDirectory(
            $zipSrc, $temporaryZip, [System.IO.Compression.CompressionLevel]::Optimal, $false)
        if (Test-Path -LiteralPath $zipPath) {
            [System.IO.File]::Replace($temporaryZip, $zipPath, [NullString]::Value)
        } else {
            [System.IO.File]::Move($temporaryZip, $zipPath)
        }
    } finally {
        if (Test-Path -LiteralPath $temporaryZip) { Remove-Item -LiteralPath $temporaryZip -Force }
    }
    Write-Output '已重打包数据包 zip'
}

$paths = @($pairs.To) + $archive
$changes = & git -C $repo status --porcelain -- $paths
if ($LASTEXITCODE -ne 0) { throw '无法检查同步结果。' }
if (-not $changes) { Write-Output '没有变更需要同步'; return }
$changes | ForEach-Object { "  " + $_.Trim() }
& git -C $repo add -A -- $paths
if ($LASTEXITCODE -ne 0) { throw '无法暂存同步文件。' }
$commitArgs = @('-m', $Title)
if ($Body) { $commitArgs += @('-m', $Body) }
& git -C $repo commit --only @commitArgs -- $paths
if ($LASTEXITCODE -ne 0) { throw '提交失败。' }
Write-Output "已本地提交：$Title"
if ($Push) {
    & git -C $repo push origin HEAD
    if ($LASTEXITCODE -ne 0) { throw '推送失败，已保留本地提交。' }
} else {
    Write-Output '未推送（加 -Push 才推）'
}

