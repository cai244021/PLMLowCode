# PLM低代码平台启停SOP

版本：1.0
日期：2026-09-05
适用范围：`D:\PLMLowCode` 编辑器服务器及其 PostgreSQL 数据库

## 1 目的和结论

本SOP用于指导运维或开发人员安全启动、验证和停止PLM低代码平台。生产运行只需PostgreSQL和Spring Boot，访问`http://服务器地址:8080/`；Vite 5173仅用于开发调试。PLM 3DEXPERIENCE服务器的Tomcat、3DSpace及Dashboard平台不由本SOP启停。

启动顺序：PostgreSQL → Spring Boot → 按需启动Vite。
停止顺序：Vite → Spring Boot → 按维护需要停止PostgreSQL。

## 2 角色和安全要求

| 角色 | 职责 |
| --- | --- |
| 操作人 | 按步骤执行、记录时间和结果，不显示或复制数据库密码 |
| 复核人 | 生产环境复核目标服务器、端口、进程命令行和业务可用性 |
| PLM管理员 | 处理3DEXPERIENCE、3DSpace、JPO、Widget及PLM侧发布，不在本SOP内操作 |

禁止直接批量结束所有`java.exe`、`node.exe`或`pwsh.exe`。停止进程前必须同时确认端口、PID和命令行均属于`D:\PLMLowCode`。禁止把数据库密码写进命令历史、日志、截图或Git仓库。

## 3 服务清单

| 服务 | 当前约定 | 是否生产必需 | 成功判据 |
| --- | --- | --- | --- |
| PostgreSQL | Windows服务`postgresql-x64-17` | 是 | 服务状态为Running，数据库连接正常 |
| Spring Boot | 默认端口8080 | 是 | `/api/pages`返回HTTP 200，首页可访问 |
| React Vite | 默认端口5173 | 否，仅开发 | 开发页面可访问，`/api`代理到8080 |
| 3DEXPERIENCE | 独立PLM服务器 | 由PLM环境决定 | 由PLM管理员按平台SOP确认 |

如果服务器上的PostgreSQL服务名或端口与上表不同，先更新本SOP中的参数再操作，不要凭名称猜测。

## 4 启动前检查

以普通PowerShell执行只读检查；启动或停止Windows服务时使用管理员PowerShell。

```powershell
$ProjectRoot = 'D:\PLMLowCode'
Test-Path -LiteralPath "$ProjectRoot\app\scripts\start-backend.ps1"
Test-Path -LiteralPath "$ProjectRoot\app\scripts\start-frontend.ps1"
Test-Path -LiteralPath "$ProjectRoot\app\plm-lowcode-platform.jar"
java -version
mvn -version
node -v
pnpm -v
Get-Service -Name 'postgresql-x64-17'
```

开发启动至少要求前两个脚本存在；生产JAR方式还要求JAR存在。Java必须兼容17。随后检查三个Machine级数据库变量是否已配置，只返回True或False，不打印值：

```powershell
'PLM_LOWCODE_DB_URL','PLM_LOWCODE_DB_USERNAME','PLM_LOWCODE_DB_PASSWORD' |
  ForEach-Object {
    [PSCustomObject]@{
      Name = $_
      Configured = -not [string]::IsNullOrWhiteSpace(
        [Environment]::GetEnvironmentVariable($_, 'Machine'))
    }
  }
```

三项必须全部为True。再检查端口占用：

```powershell
Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue |
  Where-Object LocalPort -in 8080,5173 |
  Select-Object LocalAddress,LocalPort,OwningProcess
```

计划启动的端口应为空；若已占用，先按第8节识别进程。若确认平台已经运行，则转到第7节健康检查，不重复启动。

## 5 开发环境启动

### 5.0 一键启动

在资源管理器中双击项目根目录下的`Start-PLMLowCode.cmd`。双击入口使用本机`C:\Program Files\PowerShell\7\pwsh.exe`执行脚本；脚本会依次检查本机PostgreSQL、数据库环境变量、8080和5173端口，在后台启动Spring Boot和Vite，并在健康检查通过后显示访问地址。运行日志位于`D:\PLMLowCode\app\logs`，进程PID记录位于`D:\PLMLowCode\app\runtime`。

脚本重复执行时不会重复启动本项目已监听的服务；如果端口被其他程序占用，则停止启动并显示占用PID。

### 5.1 启动PostgreSQL

```powershell
Start-Service -Name 'postgresql-x64-17'
Get-Service -Name 'postgresql-x64-17'
```

成功判据：状态为Running。失败时记录完整错误，不要修改数据库数据目录或重新初始化数据库。

### 5.2 启动Spring Boot

打开新的PowerShell窗口，执行：

```powershell
& 'D:\PLMLowCode\app\scripts\start-backend.ps1'
```

该窗口必须保持打开。脚本从Machine级环境变量读取数据库连接信息，并在`app\backend`执行`mvn spring-boot:run`。看到Spring Boot启动完成且无Flyway、数据库连接或端口冲突异常后，另开窗口验证：

```powershell
(Invoke-WebRequest 'http://127.0.0.1:8080/api/pages' -UseBasicParsing).StatusCode
```

成功判据：返回200。页面列表可以为空；HTTP 200才是本步骤判据。

### 5.3 按需启动Vite

仅在修改前端源码时执行。打开第三个PowerShell窗口：

```powershell
& 'D:\PLMLowCode\app\scripts\start-frontend.ps1'
```

访问`http://127.0.0.1:5173/`。成功判据：设计器显示，浏览器访问页面管理时没有API连接错误。若只做内部使用或生产演示，不需要启动Vite，直接访问8080。

## 6 生产或内部使用启动

首次部署或前端代码更新后，在维护窗口构建：

```powershell
& 'D:\PLMLowCode\app\scripts\build.ps1'
```

成功判据：生成`D:\PLMLowCode\app\plm-lowcode-platform.jar`且构建无失败。生产启动前先启动PostgreSQL，然后在专用PowerShell窗口执行：

```powershell
$env:PLM_LOWCODE_DB_URL = [Environment]::GetEnvironmentVariable('PLM_LOWCODE_DB_URL', 'Machine')
$env:PLM_LOWCODE_DB_USERNAME = [Environment]::GetEnvironmentVariable('PLM_LOWCODE_DB_USERNAME', 'Machine')
$env:PLM_LOWCODE_DB_PASSWORD = [Environment]::GetEnvironmentVariable('PLM_LOWCODE_DB_PASSWORD', 'Machine')
java -jar 'D:\PLMLowCode\app\plm-lowcode-platform.jar'
```

保持窗口打开并访问`http://服务器地址:8080/`。正式无人值守运行前，应把同一条JAR命令配置为受控的Windows服务或任务；在尚未配置服务包装器时，不把手工窗口启动当作永久部署。

## 7 启动后健康检查

按顺序执行：

```powershell
Get-Service -Name 'postgresql-x64-17'
Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction Stop |
  Select-Object LocalAddress,LocalPort,OwningProcess
(Invoke-WebRequest 'http://127.0.0.1:8080/api/pages' -UseBasicParsing).StatusCode
```

开发模式再检查5173。人工验证应包括：首页正常打开；进入页面管理能读取页面列表；打开一个页面能切换设计和预览；不得为健康检查新增、修改、删除或发布业务页面。

任一必需检查失败，启动结果记为失败。不要因为首页静态文件能打开就忽略API或数据库错误。

## 8 安全停止

### 8.0 一键关闭

双击项目根目录下的`Stop-PLMLowCode.cmd`。脚本只停止由一键启动脚本记录且命令行属于当前项目的Vite和Spring Boot进程，不按进程名称批量结束Java或Node。PostgreSQL保持运行；数据库仅在维护窗口按8.3节单独停止。

### 8.1 正常停止Vite

如果运行了Vite，在对应PowerShell窗口按`Ctrl+C`并确认结束。检查5173不再监听：

```powershell
Get-NetTCPConnection -State Listen -LocalPort 5173 -ErrorAction SilentlyContinue
```

### 8.2 正常停止Spring Boot

在运行`mvn spring-boot:run`或`java -jar`的窗口按`Ctrl+C`，等待进程退出，再检查：

```powershell
Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue
```

正常结果：无输出。若窗口已丢失，先识别PID及命令行：

```powershell
$Connection = Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction Stop
$Process = Get-CimInstance Win32_Process -Filter "ProcessId=$($Connection.OwningProcess)"
$Process | Select-Object ProcessId,Name,CommandLine
```

只有命令行明确属于`D:\PLMLowCode`或本平台JAR时，才执行：

```powershell
Stop-Process -Id $Process.ProcessId
Wait-Process -Id $Process.ProcessId -Timeout 30 -ErrorAction SilentlyContinue
```

若30秒后仍未退出，保留日志并升级处理；不要直接使用`Stop-Process -Force`，除非已确认普通停止无效且已评估未完成请求。

### 8.3 按维护需要停止PostgreSQL

日常停止编辑器通常不需要停止数据库。只有维护窗口、整机下线，且确认没有其他系统共用该PostgreSQL实例时执行：

```powershell
Stop-Service -Name 'postgresql-x64-17'
Get-Service -Name 'postgresql-x64-17'
```

成功判据：状态为Stopped。严禁在Spring Boot仍监听8080时先停数据库。

## 9 异常处理

### 9.1 8080或5173端口被占用

```powershell
$Port = 8080
$Connection = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction Stop
Get-CimInstance Win32_Process -Filter "ProcessId=$($Connection.OwningProcess)" |
  Select-Object ProcessId,Name,CommandLine
```

若为本平台已启动进程，执行健康检查；若为其他应用，不停止对方进程，协调修改端口。后端可临时通过`SERVER_PORT`调整，但Vite代理默认指向8080，两端必须同步配置后再使用。

### 9.2 数据库服务未运行或连接失败

先检查服务状态和三个环境变量是否存在。环境变量在进程启动时读取，修改Machine变量后必须重启后端进程。不要在排障输出中显示密码。Flyway失败时停止启动，保存异常堆栈，禁止手工改写已执行迁移记录。

### 9.3 页面打开但API报错

直接请求`/api/pages`并检查HTTP状态；确认浏览器访问的是8080生产入口还是5173开发入口。5173依赖Vite代理到8080，所以后端未启动时编辑器会显示API错误。

### 9.4 停止后端后端口仍被占用

重新读取PID，防止PID变化。核对命令行后再停止，不复用旧PID。若为Windows服务或计划任务自动拉起，应先在对应服务管理中停止并禁用本次自动重启，而不是循环杀进程。

## 10 重启流程

1. 通知正在使用设计器的人员保存页面并退出。
2. 按第8节停止Vite和Spring Boot。
3. 仅在数据库维护时停止PostgreSQL。
4. 确认8080和5173无旧监听。
5. 按第5节或第6节启动。
6. 按第7节完成自动和人工健康检查。
7. 记录启停时间、操作人、原因、版本、PID、检查结果和异常。

## 11 操作记录模板

| 项目 | 记录内容 |
| --- | --- |
| 环境和服务器 |  |
| 操作类型 | 启动／停止／重启 |
| 日期时间 |  |
| 操作人和复核人 |  |
| 版本或Git提交 |  |
| PostgreSQL结果 |  |
| Spring Boot PID及8080结果 |  |
| Vite PID及5173结果 | 不适用／通过／失败 |
| `/api/pages`结果 |  |
| 人工页面检查 |  |
| 异常及处理 |  |

## 12 PLM服务器边界

编辑器启停不会自动启停3DEXPERIENCE，也不会自动发布或撤回JSON、JSP、JPO、Spinner或Widget。PLM侧运行页面已经部署后，编辑器服务器停止通常不影响静态页面包的运行；如果未来PLM Runtime改为实时请求编辑器API，应另行更新依赖关系和停机顺序。

PLM服务器启停、Tomcat集群、反向代理、3DSpace会话、缓存清理和JPO部署必须遵循甲方及达索平台SOP。本手册不得作为直接停止PLM生产服务的授权。

## 13 最终检查清单

启动完成：PostgreSQL为Running；8080监听；`/api/pages`为200；页面可读；开发模式下5173可用。
停止完成：5173无监听；8080无监听；按计划决定PostgreSQL是否保持Running；无误停其他Java或Node进程；操作记录完整。
