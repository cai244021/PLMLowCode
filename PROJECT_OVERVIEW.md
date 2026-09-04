# PLM低代码平台项目说明

项目根目录为 `D:\PLMLowCode`。本文记录拆分后的模块结构、开发入口和路径约定；修改PLM代码前，仍应先追踪Spinner与JSP/JPO调用链。

## 项目定位

当前项目包含AMIS编辑器及Spring Boot后端、3DSpace增量、Vue 3 Dashboard Widget。仅 `plm` 模块属于3DE/ENOVIA增量包，大量未改动的OOTB文件仍在服务器环境中。

本地路径均以项目根目录为基准。服务器部署目录和HTTP地址仍使用原来的 `/3dspace/...`，不需要因为源码移动而修改。文中后半部分保留的ECO等经验属于原项目参考资料，不代表低代码平台已经实现这些业务功能。

部署形态大致分两类：

- 数据库配置：Spinner 表、schema、MQL、Page Object、JPO 等最终通过部署工具导入 3DE 数据库。
- Tomcat 前端覆盖：JSP、JS、国际化资源、模板、jar 等放入 `plm/3dspace` 对应 Web 应用目录。

## 顶层目录

| 目录 | 作用 | 修改频率 |
| --- | --- | --- |
| `app/frontend` | React + AMIS Editor页面设计器。 | 页面设计与PLM配置功能。 |
| `app/backend` | Spring Boot API、数据库访问和Flyway迁移。 | 保存页面、版本、字段库和动作库。 |
| `app/scripts` | 编辑器启动和构建脚本。 | 启动、打包时使用。 |
| `app/docs` | 设计器使用手册和截图。 | 随功能更新。 |
| `plm/3dspace` | JSP、Runtime、AMIS资源和其他PLM Web增量。 | PLM运行端。 |
| `plm/spinner` | Spinner配置及JPO源码。 | PLM业务适配。 |
| `dashboard/TWX_ENOPS_app` | Vue 3 Widget源码；`dist`为构建结果。 | Dashboard适配。 |
| `docs/enovia-help` | ENOVIA帮助索引、手册及配置字典。 | 只作为参考。 |
| `config`、`data`、`logs`、`uploads`、`backup` | 本地配置和运行资料目录。 | 不等同于可提交的源码。 |

## 启动、构建和版本管理

- 启动后端：`D:\PLMLowCode\app\scripts\start-backend.ps1`。
- 启动前端开发服务：`D:\PLMLowCode\app\scripts\start-frontend.ps1`。
- 编辑器整体打包：`D:\PLMLowCode\app\scripts\build.ps1`。
- Widget构建工作目录：`D:\PLMLowCode\dashboard\TWX_ENOPS_app`。
- PLM Runtime本地入口：`plm/3dspace/common/JF_LowCodeRuntime.jsp`。
- 页面包本地目录：`plm/3dspace/common/JFLowCode/pages`。

当前目录由原项目复制而来，已配置Git远端 `git@github.com:cai244021/PLMLowCode.git`，主分支为 `main`。原SVN历史不迁入本仓库。不要把密码文件、API密钥、数据库数据、日志、上传文件、备份及 `node_modules` 纳入版本管理；具体排除项见 `docs/GIT_SUBMISSION_SCOPE.md`。

原项目的 `different_config`、`scripts/mql` 和 `spinner_ootb` 不属于本次迁移目录；下文若提及这些内容，应视为历史参考，不在新根目录盲目执行相关命令。

## `plm/spinner` 目录

核心路径是 `plm/spinner/schema_custom/Business`。

常见文件类型：

- `SpinnerCommandData_ALL.xls`：Command 配置。定义按钮/命令名、Label、Href、Access Program/Function、Target Location 等。
- `SpinnerMenuData_ALL.xls`：Menu/树节点/对象导航入口。常把对象类型与默认页面、子 Command 绑定。
- `SpinnerTableData_ALL.xls`：Table 基本定义。
- `SpinnerTableColumnData_ALL.xls`：Table 列定义。列可以直接取业务对象表达式，也可以配置 `program/function` 调 JPO 渲染或更新。
- `SpinnerWebFormData_ALL.xls`：Form 基本定义。
- `SpinnerWebFormFieldData_ALL.xls`：Form 字段定义。字段可配置 Range Program/Function、Update Program/Function、HTML Program/Function 等。
- `SpinnerTypeData_ALL.xls`：业务类型定义及属性绑定。
- `SpinnerRelationshipData_ALL.xls`：关系定义。
- `SpinnerPolicyData_ALL.xls`、`Policy/*.xls`：生命周期策略和状态。
- `SpinnerTriggerData_ALL.xls`：触发器配置。
- `SpinnerProgramData_ALL.xls`：JPO 程序注册清单。
- `PageFiles`：Page Object、邮件模板、白名单、属性 XML、国际化资源等。
- `SourceFiles`：JPO Java 源码，文件名通常为 `xxx_mxJPO.java`，部署后程序名为去掉 `_mxJPO` 的部分。

Spinner 文件虽然扩展名是 `.xls`，但当前很多文件是制表符分隔文本，可直接用 `rg`、`Get-Content` 搜索和阅读。

### Spinner 调用链

常见链路：

1. Command/Menu 暴露入口。
2. Href 指向 OOTB 页面或自定义 JSP，例如 `emxIndentedTable.jsp`、`emxForm.jsp`、`emxCreate.jsp`、`emxFullSearch.jsp`、`JF_*.jsp`。
3. URL 参数或表单字段配置指定 JPO，例如：
   - `program=JF_ECRService:getECRsByLoginUser`
   - `postProcessJPO=JF_DataOutSource:createJFDataOutSourceApply`
   - `Access Program=JF_VPMReferenceEBOM`、`Access Function=JFVPMPartListCmdAccess`
4. JPO 通过 `JPO.unpackArgs(args)` 获取 `requestMap`、`paramMap`、`objectList`、`fieldMap` 等上下文。
5. JPO 使用 `DomainObject`、`DomainRelationship`、`MqlUtil`、`ContextUtil` 操作 3DE 数据。
6. JSP 侧负责弹窗、全搜索提交、文件上传下载、刷新父窗口、关闭窗口等浏览器交互。

## `plm/3dspace` 目录

`plm/3dspace` 是 Tomcat Web 层覆盖目录，主要内容：

- `common`：通用 JSP、AEF 页面覆盖、自定义业务 JSP、公共 JS/CSS、前端流程页。
- `components`：文档、流程、组件相关 JSP 覆盖和自定义页。
- `engineeringcentral`：工程中心相关 JSP，常见于 VPM/EBOM/项目零件连接等操作。
- `enterprisechangemgtapp`：变更管理应用相关 JSP。
- `configuration`：配置管理相关 OOTB 页面覆盖。
- `programcentral`：项目管理相关 OOTB 页面覆盖。
- `WEB-INF/classes`：国际化 `.properties`、系统配置。
- `WEB-INF/lib`：第三方或项目 jar，例如 REST、OnlyOffice、PDF、Excel 等依赖。
- `jf_template`：Excel 导入导出模板。
- `webapps`：widget 或 3DEXPERIENCE Web App 前端资源，例如 `EditPropWidget`、`FolderEditor`、`ENOXEngineer`、`ENONewWidget`、`Visualization`。

### JSP 常见职责

JSP 多数不是完整 MVC 页面，而是 3DE 页面流中的“动作页”：

- 从请求中读取 `objectId`、`emxTableRowId`、`relId`、`parentOID` 等。
- 校验 CSRF：常包含 `enoviaCSRFTokenValidation.inc` 或注入文件。
- 调用 JPO：`JPO.invoke(context, "Program", null, "method", JPO.packArgs(map), ReturnType.class)`。
- 直接操作对象或关系：使用 `DomainObject`、`DomainRelationship`。
- 返回脚本刷新/关闭窗口：`getTopWindow().closeWindow()`、刷新 opener、alert 提示。
- 文件导入导出：通过 multipart、fetch、base64、Blob 或 checkout/checkin JSP 完成。

注意：部分中文注释在当前工作区显示为乱码，通常是源文件编码与读取终端编码不一致造成。修改这类 JSP 时应尽量保持原文件编码，避免无关大面积重写。

## 重点业务模块

从配置和 JPO 命名看，当前项目主要模块包括：

- ECR/新 ECR/正式 ECR：`JF_ECRService`、`JF_ECRProcess`、`JF_ECRServiceDeffer`、`JF_NewECRService`、`JF_FormalECRService` 等。
- ECO/变更执行：`JF_ChangeExecutionECOSource`、`JF_ChangeEvent`。
- VPM/EBOM/MBOM/项目根件：`JF_VPMReferenceEBOM`、`JF_VPMReference`、`JF_VPMT`、`JF_MBOMService`、`JF_MBOMUtil`、`JF_MBOM`。
- 项目空间/任务/路线：`emxProjectSpace` 覆盖、`JF_ProjectSpace`、`JF_ProjectTaskUtils`、`JF_Route`、`JF_SignTask`。
- 文档/图纸/OnlyOffice/PDF：`JF_DocumentService`、`JF_DocumentLibrary`、`CUS_DocCustomAction`、`CUS_OnlyOfficeOnlineAction`、`JF_WaterMarkUtils`。
- 数据外发：`JF_DataOutSource`，包含外发对象、内容、审批、生成记录、下载清单等。
- 偏离申请 DA：`JF_DeviationApplicationSource`。
- PCR：`JF_PCR`。
- 成本/报价：`JF_Cost`、`JF_CostAnalysis`、`JF_RapidOffer`。
- 颜色件/色系/快照/竞品 BOM：`JF_ColorPart`、`JF_Snapshot`、`JF_CompetitiveBOM`。
- 外部接口/REST/后台任务：`JF_WebServiceHandler`、`JF_ExternalInterface`、`JF_BackendImportProgram`、`JF_CAACallRestfulService`。

## 常见修改入口

### 改按钮、菜单、页面入口

优先查：

1. `plm/spinner/schema_custom/Business/SpinnerCommandData_ALL.xls`
2. `plm/spinner/schema_custom/Business/SpinnerMenuData_ALL.xls`
3. Href 中指向的 JSP 或 OOTB 页面参数
4. Href 参数里的 `program`、`postProcessJPO`、`submitURL`
5. 相关 JPO 方法

常用搜索：

```powershell
rg -n "命令名|LabelKey|JSP文件名|JPO名|方法名" plm/spinner plm/3dspace
```

### 改表格数据或列显示

优先查：

1. Command Href 中的 `table=xxx` 和 `program=Program:method`。
2. `SpinnerTableData_ALL.xls` 找 table 是否存在。
3. `SpinnerTableColumnData_ALL.xls` 找列定义、表达式、Href、Update Program/Function。
4. JPO 里对应的 table 数据方法，通常返回 `MapList`。
5. 列方法通常返回 `StringList` 或 HTML 字符串列表。

### 改表单字段

优先查：

1. Command Href 或 Menu Href 中的 `form=xxx`。
2. `SpinnerWebFormData_ALL.xls`。
3. `SpinnerWebFormFieldData_ALL.xls`。
4. 字段的 Range Program/Function、Update Program/Function、Access Expression、OnChange Handler。
5. 自定义表单 JSP，例如 `plm/3dspace/common/JF_ECRForm.jsp`。

### 改对象模型、属性、关系、生命周期

优先查：

1. `SpinnerTypeData_ALL.xls`
2. `SpinnerAttributeData_ALL.xls`
3. `SpinnerRelationshipData_ALL.xls`
4. `SpinnerPolicyData_ALL.xls` 与 `Business/Policy/*.xls`
5. `SpinnerTriggerData_ALL.xls`
6. JPO 常量：`JF_PLMConstants_mxJPO.java`

### 改弹窗提交、全搜索选择、文件导入导出

优先查：

1. Command Href 中的 `submitURL`、`submitAction`、`targetLocation`、`Popup Modal`。
2. `plm/3dspace/common`、`plm/3dspace/components`、`plm/3dspace/engineeringcentral` 下对应 JSP。
3. JSP 中 `emxGetParameterValues(request, "emxTableRowId")` 的解析方式。
4. JSP 中 `JPO.invoke` 或直接 `DomainRelationship.connect/disconnect`。
5. 关闭/刷新脚本是否符合调用场景。

## 本次 ECO 需求经验记录

### ECO 表单字段需求

本次给 `JFECOChangeExecuteViewForm` 增加 `JFBreakpointMode` 和 `JFBreakpointTime` 时，主要问题集中在 Spinner 表格文件的填写格式：

- `plm/spinner/schema_custom/Business/*.xls` 虽然后缀是 `.xls`，实际多为制表符分隔文本。新增字段不要把多行合成一行，也不要删除行尾大量空列。
- 修改 `SpinnerWebFormFieldData_ALL.xls` 时，应复制相邻字段行的完整列结构，只改字段名、Label、Expression、Settings、Order 等必要列。
- `Settings` 这类多值列内部使用 ` | ` 分隔，例如 `Admin Type | Editable | Field Type | Input Type | Registered Suite`，对应值列也必须一一对齐。
- 字段顺序要同时检查前后字段的 `Order`，本次 `Description` 后新增字段时，需要把后面的 `JFConnectECR` 顺序后移。
- 只读字段要配置 `Editable=false`，不要只依赖页面显示表达式。日期字段除属性类型外，Form 字段还要配置 `format=date` 等显示设置。
- 新增属性必须同时检查 `SpinnerAttributeData_ALL.xls`、`SpinnerTypeData_ALL.xls`、JPO 常量和国际化资源。
- 国际化不要只加中文，至少同步检查默认、`en`、`zh`、`zh_CN` 资源文件。

### ECO 创建时复制 ECR 属性

ECO 从 ECR 创建时复制属性，不一定在 Form 配置里完成。本次 `JFBreakpointMode` 是在 `JF_ChangeExecutionECOSource_mxJPO.java` 的 ECR 同步属性列表中补充：

- 先搜索已有属性复制逻辑，例如 `getECRSyncAttributeList`。
- 优先沿用已有同步列表，不要新增一套独立复制逻辑。
- 如果属性在 ECR 已有、ECO 新增绑定，Type 配置和 JPO 同步列表都要检查。

### ECO 状态提升 Trigger 需求

本次在 ECO 从 `In_Work` 提升到 `ExecuteFeedback` 时增加校验和赋值，关键链路如下：

- 状态流转先查 `plm/spinner/schema_custom/Business/Policy/*.xls`，确认源状态、目标状态和 promote 时机。
- Trigger 定义在 `SpinnerTriggerData_ALL.xls`，Trigger 参数在 `plm/spinner/schema_custom/Objects/bo_eService Trigger Program Parameters.xls`，两边名称必须一致。
- `check` Trigger 适合做阻断校验，返回非 0 并 `mqlNotice` 提示；`action` Trigger 适合做通过后的属性回写。
- 本次校验沿用已有 `PolicyJFECOInWorkPromoteCheck`，在断点方式不为 `NA` 时检查指定执行任务是否存在。
- 本次新增 `PolicyJFECOInWorkPromoteAction`，提升通过后读取执行任务的 `Task Estimated Finish Date`，回写 ECO 的 `JFBreakpointTime`。
- Trigger 调 JPO 时，要确认方法签名、返回类型和异常处理符合现有写法。已有方法多数使用 `Context context, String[] args`，对象 ID 通过 `${OBJECTID}` 传入。
- 任务标题存在中英文差异时，代码里应同时兼容中文标题、英文标题和可能的中英文拼接标题。
- Java 源码中直接写中文字符串可能受文件编码影响，必要时使用 Unicode escape，避免部署编译环境乱码。

### 修改后的格式检查

Spinner 文件格式非常容易被误改，改完后建议至少做这些检查：

- 用 `svn diff` 看是否只改了目标行，避免整文件换行或编码变化。
- 检查新增 Spinner 行的列数是否和同文件同类行一致，尤其是 `bo_eService Trigger Program Parameters.xls` 这类列很多的文件。
- 检查 CRLF/LF 是否被混用。本项目部分 Spinner 文件是 CRLF，部分 properties 是 LF，修改时应保持原文件风格。
- 检查文件末尾是否原本有换行，不要无意改变 final newline。
- 对 `.properties` 文件新增 key 后，用 `rg` 同时确认四个语言文件都有对应项。

### 新增自动编号类型

如果新增类型需要通过 `FrameworkUtil.autoName`、`emxCreate.jsp` 自动命名或 Object Generator 生成名称，不能只加类型和策略，还要同步维护编号器相关配置：

- `plm/spinner/schema_custom/Objects/bo_eService Object Generator.xls`：新增 `eService Object Generator` 行，配置类型、前缀、策略和 vault。
- `plm/spinner/schema_custom/Objects/bo_eService Number Generator.xls`：新增对应 `eService Number Generator` 行，初始化 next number。
- `scripts/mql/mql.txt`：在文件最后追加 Object Generator 和 Number Generator 的连接语句，并增加注释说明本次新增类型。

追加 MQL 时保持放在 `mql.txt` 最后，例如：

```mql
# JFDelayedFiling object generator number connection
add connection 'eService Number Generator' from 'eService Object Generator' type_JFDelayedFiling '' to 'eService Number Generator' type_JFDelayedFiling '' ;
```

注意：这类 MQL 不要插到文件中间，避免影响既有脚本阅读顺序和部署排查。

## 编码和安全注意事项

- JSP、properties、旧 Java 文件可能混用编码。修改前先查看原文件内容和乱码情况，尽量小范围编辑。
- 业务动作页涉及对象连接、删除、生命周期推进时，要确认 `ContextUtil.pushContext/popContext`、事务、权限和异常路径。
- 避免直接拼接 MQL where 或 URL 参数引入注入风险。已有代码大量使用字符串拼接，新增代码应优先使用平台 API 或最小化输入面。
- `emxTableRowId` 常见格式可能是 `relId|objectId` 或单独 objectId，不能假设固定格式。
- 表格列更新时，`New Value`、`relId`、`objectId` 等 key 通常来自 `paramMap`，要跟现有字段更新方法保持一致。
- 如果修改资源文件，要同步检查 `zh_CN`、`zh`、`en` 等多语言文件。
- 如果新增 JPO，需要同时检查 `SpinnerProgramData_ALL.xls` 是否注册，以及文件名是否符合 `_mxJPO.java` 约定。

## 后续开发前检查清单

1. 先用 `rg` 全局搜索需求关键词、Command 名、Form/Table 名、JPO 名。
2. 从 Spinner 入口确认页面、表单、表格、权限和 JPO 方法。
3. 再读 `plm/3dspace` 中被 Href 或 `submitURL` 指向的 JSP。
4. 最后读 `SourceFiles` 中实际 JPO 实现，确认参数结构和返回类型。
5. 若涉及部署配置，检查本项目的 `config` 和 `app/scripts`；原项目 `different_config`、`scripts/mql` 需另行取得，不假定已复制。
6. 修改前记录涉及文件，避免碰 `spinner_ootb` 和无关 OOTB 覆盖文件。
7. 修改后至少做文本级验证：搜索配置引用是否闭环、JPO 方法名是否一致、资源 key 是否存在。

## 快速定位示例

查一个业务入口：

```powershell
rg -n "JFZeroPartCmd|JFProjectVPMAddCmd|JF_ECRFormEditCmd" plm/spinner plm/3dspace
```

查某个 JPO 方法被谁调用：

```powershell
rg -n "JF_VPMReferenceEBOM:getZeroPart|getZeroPart\\(" plm/spinner plm/3dspace
```

查 JSP 里直接调用的 JPO：

```powershell
rg -n "JPO\\.invoke\\(context" plm/3dspace
```

查国际化 key：

```powershell
rg -n "emxFramework\\.Command\\.JFDataOutSourceCmd" plm/3dspace plm/spinner
```

## 当前阅读结论

这个项目的核心不是单一 Java 应用，而是 3DE 平台上的配置驱动开发：Spinner 配置决定入口和 UI 元数据，`plm/3dspace` JSP 承接页面流和前端交互，JPO 执行业务查询、校验、对象/关系操作和外部接口调用。后续改动应优先沿着“Command/Menu -> JSP/OOTB 页面参数 -> Table/Form 配置 -> JPO 方法 -> Type/Relationship/Policy/Trigger”的顺序追踪。
