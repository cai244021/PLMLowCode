# PLMLowCode
PLM低代码平台

## 模块

- `app/frontend`：React + AMIS Editor 页面设计器。
- `app/backend`：Spring Boot API、PostgreSQL、Flyway。
- `plm`：3DEXPERIENCE Web/Spinner/JPO 增量，依赖已有PLM环境。
- `dashboard/TWX_ENOPS_app`：Vue 3 Widget。
- `app/docs`：使用手册及验证记录。

开发前阅读 `AGENTS.md`、`PROJECT_OVERVIEW.md` 和 `app/docs/PLM低代码设计器使用手册.md`。

## 本地开发

后端需要Java 17、Maven和PostgreSQL，前端需要兼容Vite的Node.js及pnpm。
先创建PostgreSQL数据库，并在本机配置以下环境变量，勿将真实密码写进仓库：

- `PLM_LOWCODE_DB_URL`：例如 `jdbc:postgresql://127.0.0.1:5432/plm_lowcode`
- `PLM_LOWCODE_DB_USERNAME`
- `PLM_LOWCODE_DB_PASSWORD`

当前Windows启动脚本读取Machine级环境变量，默认工作目录为 `D:\PLMLowCode`。
如克隆到其他目录，需要调整脚本工作目录，或直接在相应模块中运行下列命令。

1. `app/frontend` 中执行 `pnpm install --frozen-lockfile`。
2. `app/backend` 中执行 `mvn spring-boot:run`（需上述环境变量）。
3. `app/frontend` 中执行 `pnpm dev`。
4. 访问 `http://127.0.0.1:5173/`。

也可使用 `app/scripts/start-backend.ps1` 和 `app/scripts/start-frontend.ps1`。
Flyway自动创建、迁移编辑器数据表；仓库不包含本机保存的页面数据，空库可通过“新建页面”开始。

验证：后端 `mvn test`，前端 `pnpm build`。整体打包使用 `app/scripts/build.ps1`。

## 提交边界

运行数据、密码、真实 `.env`、依赖目录、构建产物、平台JAR和历史演示交付物不纳入Git。
具体排除项及PLM部署依赖见 `docs/GIT_SUBMISSION_SCOPE.md`。
此仓库不是完整3DEXPERIENCE安装包；请在有权限的PLM测试环境部署增量。
