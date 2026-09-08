[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

$AppRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$ProjectRoot = (Resolve-Path -LiteralPath (Join-Path $AppRoot '..')).Path
$RuntimeDir = Join-Path $AppRoot 'runtime'
$LogDir = Join-Path $AppRoot 'logs'
$BackendPidFile = Join-Path $RuntimeDir 'backend.pid'
$FrontendPidFile = Join-Path $RuntimeDir 'frontend.pid'
$BackendScript = Join-Path $PSScriptRoot 'start-backend.ps1'
$FrontendScript = Join-Path $PSScriptRoot 'start-frontend.ps1'

function Get-ListenerProcess {
    param([int]$Port)

    $connection = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($null -eq $connection) {
        return $null
    }
    return Get-CimInstance Win32_Process -Filter "ProcessId=$($connection.OwningProcess)"
}

function Test-ProjectListener {
    param(
        [int]$Port,
        [string]$PidFile
    )

    $process = Get-ListenerProcess -Port $Port
    if ($null -eq $process) {
        return $false
    }

    if (Test-Path -LiteralPath $PidFile) {
        $rootPidText = (Get-Content -LiteralPath $PidFile -Raw).Trim()
        $rootPid = 0
        if ([int]::TryParse($rootPidText, [ref]$rootPid)) {
            $currentPid = $process.ProcessId
            while ($currentPid -gt 0) {
                if ($currentPid -eq $rootPid) {
                    return $true
                }
                $current = Get-CimInstance Win32_Process -Filter "ProcessId=$currentPid" -ErrorAction SilentlyContinue
                if ($null -eq $current -or $current.ParentProcessId -eq $currentPid) {
                    break
                }
                $currentPid = $current.ParentProcessId
            }
        }
    }

    if (-not [string]::IsNullOrWhiteSpace($process.CommandLine) -and
        $process.CommandLine.IndexOf($ProjectRoot, [StringComparison]::OrdinalIgnoreCase) -ge 0) {
        return $true
    }
    throw "端口 $Port 已被其他程序占用：PID $($process.ProcessId) $($process.Name)。未启动平台。"
}

function Wait-HttpReady {
    param(
        [string]$Url,
        [int]$TimeoutSeconds,
        [System.Diagnostics.Process]$Process,
        [string]$ErrorLog
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if ($null -ne $Process -and $Process.HasExited) {
            $tail = if (Test-Path -LiteralPath $ErrorLog) {
                (Get-Content -LiteralPath $ErrorLog -Tail 20 -ErrorAction SilentlyContinue) -join [Environment]::NewLine
            } else {
                '未生成错误日志。'
            }
            throw "进程在健康检查前退出。错误日志：$ErrorLog`n$tail"
        }
        try {
            $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 3
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 400) {
                return
            }
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    throw "等待 $Url 超时。请检查日志：$ErrorLog"
}

function Start-TrackedProcess {
    param(
        [string]$ScriptPath,
        [string]$PidFile,
        [string]$OutputLog,
        [string]$ErrorLog
    )

    $powerShellPath = (Get-Process -Id $PID).Path
    $process = Start-Process -FilePath $powerShellPath `
        -ArgumentList @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $ScriptPath) `
        -WorkingDirectory $PSScriptRoot `
        -WindowStyle Hidden `
        -RedirectStandardOutput $OutputLog `
        -RedirectStandardError $ErrorLog `
        -PassThru
    Set-Content -LiteralPath $PidFile -Value $process.Id -Encoding ascii
    return $process
}

Write-Host '正在启动 PLM 低代码编辑器环境...'

New-Item -ItemType Directory -Force -Path $RuntimeDir, $LogDir | Out-Null

foreach ($commandName in 'mvn', 'pnpm') {
    if ($null -eq (Get-Command $commandName -ErrorAction SilentlyContinue)) {
        throw "未找到命令 $commandName，请先完成开发环境安装并重新打开终端。"
    }
}

$databaseUrl = [Environment]::GetEnvironmentVariable('PLM_LOWCODE_DB_URL', 'Machine')
$databaseUsername = [Environment]::GetEnvironmentVariable('PLM_LOWCODE_DB_USERNAME', 'Machine')
$databasePassword = [Environment]::GetEnvironmentVariable('PLM_LOWCODE_DB_PASSWORD', 'Machine')
if ([string]::IsNullOrWhiteSpace($databaseUrl) -or
    [string]::IsNullOrWhiteSpace($databaseUsername) -or
    [string]::IsNullOrWhiteSpace($databasePassword)) {
    throw 'Machine级数据库环境变量 PLM_LOWCODE_DB_URL、PLM_LOWCODE_DB_USERNAME、PLM_LOWCODE_DB_PASSWORD 必须全部配置。'
}

if ($databaseUrl -match '(?i)(localhost|127\.0\.0\.1)') {
    $postgresService = Get-Service -Name 'postgresql-x64-17' -ErrorAction SilentlyContinue
    if ($null -eq $postgresService) {
        throw '数据库地址为本机，但未找到Windows服务 postgresql-x64-17。'
    }
    if ($postgresService.Status -ne 'Running') {
        try {
            Start-Service -Name $postgresService.Name
            $postgresService.WaitForStatus('Running', [TimeSpan]::FromSeconds(30))
        } catch {
            throw 'PostgreSQL未启动。请使用管理员PowerShell启动 postgresql-x64-17 后重试。'
        }
    }
    Write-Host 'PostgreSQL：运行中'
} else {
    Write-Host 'PostgreSQL：使用远程数据库地址，跳过本机服务检查'
}

$backendProcess = $null
if (Test-ProjectListener -Port 8080 -PidFile $BackendPidFile) {
    Write-Host 'Spring Boot：已经运行，跳过重复启动'
} else {
    $backendProcess = Start-TrackedProcess `
        -ScriptPath $BackendScript `
        -PidFile $BackendPidFile `
        -OutputLog (Join-Path $LogDir 'backend.log') `
        -ErrorLog (Join-Path $LogDir 'backend-error.log')
    Wait-HttpReady `
        -Url 'http://127.0.0.1:8080/api/pages' `
        -TimeoutSeconds 120 `
        -Process $backendProcess `
        -ErrorLog (Join-Path $LogDir 'backend-error.log')
    Write-Host "Spring Boot：启动成功，PID $($backendProcess.Id)"
}

$frontendProcess = $null
if (Test-ProjectListener -Port 5173 -PidFile $FrontendPidFile) {
    Write-Host 'Vite：已经运行，跳过重复启动'
} else {
    $frontendProcess = Start-TrackedProcess `
        -ScriptPath $FrontendScript `
        -PidFile $FrontendPidFile `
        -OutputLog (Join-Path $LogDir 'frontend.log') `
        -ErrorLog (Join-Path $LogDir 'frontend-error.log')
    Wait-HttpReady `
        -Url 'http://127.0.0.1:5173/' `
        -TimeoutSeconds 60 `
        -Process $frontendProcess `
        -ErrorLog (Join-Path $LogDir 'frontend-error.log')
    Write-Host "Vite：启动成功，PID $($frontendProcess.Id)"
}

Write-Host ''
Write-Host 'PLM低代码编辑器已启动：http://127.0.0.1:5173/' -ForegroundColor Green
Write-Host "运行日志：$LogDir"
