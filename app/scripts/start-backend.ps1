$env:PLM_LOWCODE_DB_URL = [Environment]::GetEnvironmentVariable('PLM_LOWCODE_DB_URL', 'Machine')
$env:PLM_LOWCODE_DB_USERNAME = [Environment]::GetEnvironmentVariable('PLM_LOWCODE_DB_USERNAME', 'Machine')
$env:PLM_LOWCODE_DB_PASSWORD = [Environment]::GetEnvironmentVariable('PLM_LOWCODE_DB_PASSWORD', 'Machine')

Set-Location -LiteralPath 'D:\PLMLowCode\app\backend'
mvn spring-boot:run

