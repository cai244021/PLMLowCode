$ErrorActionPreference = 'Stop'

Set-Location -LiteralPath 'D:\PLMLowCode\app\frontend'
pnpm install --frozen-lockfile
pnpm build

Set-Location -LiteralPath 'D:\PLMLowCode\app\backend'
mvn clean package

Copy-Item -LiteralPath 'target\plm-lowcode-platform-0.1.0-SNAPSHOT.jar' `
    -Destination 'D:\PLMLowCode\app\plm-lowcode-platform.jar' -Force

Write-Host 'Build completed: D:\PLMLowCode\app\plm-lowcode-platform.jar'

