$env:PLM_LOWCODE_DB_URL = [Environment]::GetEnvironmentVariable('PLM_LOWCODE_DB_URL', 'Machine')
$env:PLM_LOWCODE_DB_USERNAME = [Environment]::GetEnvironmentVariable('PLM_LOWCODE_DB_USERNAME', 'Machine')
$env:PLM_LOWCODE_DB_PASSWORD = [Environment]::GetEnvironmentVariable('PLM_LOWCODE_DB_PASSWORD', 'Machine')
$ProjectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
$PlmConfigPath = Join-Path $ProjectRoot 'config\plm-integration.yml'
if (-not (Test-Path -LiteralPath $PlmConfigPath)) {
    throw "未找到PLM发布配置：$PlmConfigPath。请复制app/docs/plm-integration.example.yml后填写。"
}
$env:SPRING_CONFIG_ADDITIONAL_LOCATION = 'file:' + ($PlmConfigPath -replace '\\', '/')

Set-Location -LiteralPath 'D:\PLMLowCode\app\backend'
mvn spring-boot:run
