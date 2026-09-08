[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

$AppRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$ProjectRoot = (Resolve-Path -LiteralPath (Join-Path $AppRoot '..')).Path
$RuntimeDir = Join-Path $AppRoot 'runtime'
$BackendPidFile = Join-Path $RuntimeDir 'backend.pid'
$FrontendPidFile = Join-Path $RuntimeDir 'frontend.pid'

function Get-ProcessTreePostOrder {
    param(
        [int]$RootPid,
        [object[]]$AllProcesses
    )

    $result = @()
    foreach ($child in $AllProcesses | Where-Object ParentProcessId -eq $RootPid) {
        $result += Get-ProcessTreePostOrder -RootPid $child.ProcessId -AllProcesses $AllProcesses
    }
    $result += $RootPid
    return $result
}

function Stop-TrackedProcess {
    param(
        [string]$Name,
        [string]$PidFile
    )

    if (-not (Test-Path -LiteralPath $PidFile)) {
        Write-Host "$Name：没有PID记录，跳过"
        return
    }

    $savedPidText = (Get-Content -LiteralPath $PidFile -Raw).Trim()
    $savedPid = 0
    if (-not [int]::TryParse($savedPidText, [ref]$savedPid)) {
        Remove-Item -LiteralPath $PidFile -Force
        Write-Warning "$Name 的PID文件无效，已清理。"
        return
    }

    $rootProcess = Get-CimInstance Win32_Process -Filter "ProcessId=$savedPid" -ErrorAction SilentlyContinue
    if ($null -eq $rootProcess) {
        Remove-Item -LiteralPath $PidFile -Force
        Write-Host "$Name：进程已经停止"
        return
    }

    if ([string]::IsNullOrWhiteSpace($rootProcess.CommandLine) -or
        $rootProcess.CommandLine.IndexOf($ProjectRoot, [StringComparison]::OrdinalIgnoreCase) -lt 0) {
        throw "$Name 的PID $savedPid 已被其他程序使用，脚本拒绝停止该进程。"
    }

    $allProcesses = @(Get-CimInstance Win32_Process)
    $processIds = @(Get-ProcessTreePostOrder -RootPid $savedPid -AllProcesses $allProcesses)
    foreach ($processId in $processIds) {
        Stop-Process -Id $processId -ErrorAction SilentlyContinue
    }

    $deadline = (Get-Date).AddSeconds(15)
    while ((Get-Date) -lt $deadline -and $null -ne (Get-Process -Id $savedPid -ErrorAction SilentlyContinue)) {
        Start-Sleep -Milliseconds 300
    }
    Remove-Item -LiteralPath $PidFile -Force
    Write-Host "$Name：已停止"
}

Write-Host '正在关闭 PLM 低代码编辑器环境...'
Stop-TrackedProcess -Name 'Vite' -PidFile $FrontendPidFile
Stop-TrackedProcess -Name 'Spring Boot' -PidFile $BackendPidFile

foreach ($port in 5173, 8080) {
    $connection = Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($null -ne $connection) {
        $process = Get-CimInstance Win32_Process -Filter "ProcessId=$($connection.OwningProcess)"
        Write-Warning "端口 $port 仍由PID $($connection.OwningProcess)监听。未自动停止未记录的进程：$($process.Name)。"
    }
}

Write-Host '编辑器前后端关闭完成。PostgreSQL保持运行。' -ForegroundColor Green
