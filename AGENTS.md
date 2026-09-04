# PLM低代码平台项目修改原则

本文件用于约束后续在当前仓库中修改达索 3DEXPERIENCE/ENOVIA 增量代码的方式。适用于本项目全部目录，除非子目录下另有更具体的 `AGENTS.md`。

当前项目根目录为 `D:\PLMLowCode`，包含编辑器、PLM增量和Dashboard Widget三个模块。本文中的ENOVIA业务约束主要用于PLM模块；编辑器和Widget仍需遵守最小修改、权限校验和验证原则。

## 当前开发目录

- 编辑器前端：`app/frontend`（React + AMIS Editor）。
- 编辑器后端：`app/backend`（Spring Boot）。
- 编辑器启动与构建：`app/scripts`；使用手册：`app/docs`。
- PLM Web增量：`plm/3dspace`；Spinner和JPO：`plm/spinner/schema_custom`。
- Dashboard Widget：`dashboard/TWX_ENOPS_app`（Vue 3）。
- ENOVIA参考文档：根目录 `ENOVIA_UI_Config_AI_Handbook.md` 和 `docs/enovia-help`。

未特别注明的本地源码路径均相对于 `D:\PLMLowCode`。服务器部署路径和HTTP URL中的 `/3dspace/` 保持不变，不添加本地目录前缀 `plm/`。PLM模块是增量包，不是完整产品源码；修改时仍需追踪Spinner、JPO、JSP和资源文件的完整调用链。

## 基本原则

1. 先查链路，再改代码。
   修改按钮、表格、表单、导入导出、权限、状态流转前，先从 Spinner 配置入口追到实际 JPO/JSP 方法，再判断真正的业务控制点。

2. 最小化改动范围。
   只改当前需求涉及的配置、方法和字段。不要顺手重构无关模块，不要批量格式化历史文件，不要调整无关注释、换行、编码或导入顺序。

3. 优先复用现有模式。
   新增逻辑应优先沿用项目已有的 `JF_*Util_mxJPO`、`JF_*Service_mxJPO`、`JF_*StaticMethod_mxJPO`、`JF_PublicMethodClass_mxJPO`、Spinner 表配置和 JSP 调用方式。不要引入新的框架式抽象，除非现有模式无法承载需求。

4. 权限逻辑必须集中。
   表格列编辑权限、按钮入口权限、上传写入权限、下载权限可能不是同一处代码。修改权限时必须确认：
   - Spinner column 的 `Edit Access Program/Function`
   - Command 的 `Access Program/Function`
   - 表格数据方法里生成的 `isEdit` / `rowEdit`
   - 上传或 REST 写入时是否复用同一份 `isEdit`

5. 状态判断必须精确。
   3DE 生命周期状态、任务状态、路线状态通常是精确字符串，例如 `Countersign`、`APR`、`Active`、`Review`、`Complete`。不要使用模糊包含判断。业务要求“工作中”时，应明确判断生产枚举值。

6. 不绕过业务权限。
   不能因为 JSP、导入、REST、批处理走后台上下文就直接写对象属性。写入前必须复用或等价校验页面权限、角色权限、任务状态和行字段权限。

7. PLM代码来源于原SVN工作副本；当前新目录使用Git，远端为 `git@github.com:cai244021/PLMLowCode.git`，主分支为 `main`。不要把本目录当成SVN工作副本。本文中的SVN diff检查在当前项目改用Git diff；不得强制推送覆盖远端历史。提交范围遵守根目录 `.gitignore` 和 `docs/GIT_SUBMISSION_SCOPE.md`。

8. 追加代码、包括Excel文件写入scheme 原则上在最后面追加


## 常见修改入口

### Spinner 配置

优先检查：

- `plm/spinner/schema_custom/Business/SpinnerCommandData_ALL.xls`
- `plm/spinner/schema_custom/Business/SpinnerMenuData_ALL.xls`
- `plm/spinner/schema_custom/Business/SpinnerTableData_ALL.xls`
- `plm/spinner/schema_custom/Business/SpinnerTableColumnData_ALL.xls`
- `plm/spinner/schema_custom/Business/SpinnerWebFormData_ALL.xls`
- `plm/spinner/schema_custom/Business/SpinnerWebFormFieldData_ALL.xls`
- `plm/spinner/schema_custom/Business/SpinnerProgramData_ALL.xls`
- `plm/spinner/schema_custom/Business/PageFiles`

这些 `.xls` 文件多数是制表符分隔文本，可以直接用 `rg` 搜索。修改配置时要同时确认程序名、方法名、Href 参数、Access Function、Update Function、Validate、Range Function 等字段是否匹配。

### 本地 ENOVIA 配置帮助文档

本机帮助目录 `D:\文档\本地帮助文档` 中保存了 Dassault/ENOVIA form、command、table、menu、toolbar、Structure Browser、emxEditableTable JS、Markup API、Selectables 等 SingleFile HTML 文档。

后续修改 UI 配置、表格列、表单字段、toolbar/menu、前端表格 JS 或 select 表达式时，优先参考仓库内已整理的帮助手册：

- `ENOVIA_UI_Config_AI_Handbook.md`

`AGENTS.md` 只记录必须遵守的项目规则；详细参数、API 名称和配置项说明以帮助手册为索引，不要把大段帮助文档复制到业务代码或主规则文件里。

### Structure Browser / Table 配置追踪

Structure Browser 常见入口是 `../common/emxIndentedTable.jsp`。追踪 table 页面时必须确认 URL 参数和 Spinner 配置是否一致，重点检查：

- 数据来源：`program`、`expandProgram`、`relationship`、`direction`、`inquiry`
- 表定义：`table`、`appendColumns`
- 工具栏和菜单：`toolbar`、`tableMenu`、`showRMB`
- 模式和编辑：`mode`、`editLink`、`selection`、`showApply`、`triggerValidation`
- 回调和写入：`submitURL`、`applyURL`、`connectionProgram`、`lookupJPO`

修改 Table Column 时，不能只改显示列。必须同步确认：

- 数据来源：`expression`、`program`、`function`、`Column Type`
- 编辑能力：`Editable`、`Input Type`、`Allow Manual Edit`、`Required`
- 字段权限：`Access Program/Function`、`Edit Access Program/Function`
- 写入逻辑：`Update Program/Function`
- 校验和联动：`Validate`、`Validate Type`、`Range Program/Function`、`Reload Program/Function`
- 样式：`Style Program/Function`、`Style Column`

页面列可编辑、JS 可改值、导入可写入是三件事。新增或放开字段时，必须分别确认 Column 配置、表格数据方法和后台写入方法。

### Web Form 配置追踪

Form 常见入口是 `../common/emxForm.jsp`。修改 form 或 field 前必须确认：

- URL 参数：`form`、`mode`、`objectId`、`relId`、`toolbar`、`editLink`
- 提交处理：`postProcessJPO`、`postProcessURL`、`preProcessJavaScript`、`submitAction`
- Field 数据来源：`program`、`function`、`Field Type`
- Field 编辑和写入：`Editable`、`Input Type`、`Required`、`Update Program/Function`
- Field 校验和范围：`Validate`、`Validate Type`、`Range Program/Function`
- Field 权限：`Access Program/Function`、`Access Mask`、`Access Expression`

Form 页面上的必填、只读、隐藏只代表前端配置，后台 JPO/JSP 写入仍必须做业务校验。

### emxEditableTable JS 修改规则

修改表格前端 JS 时，优先使用平台已有 `emxEditableTable` API，不手工拼 DOM 状态。常见 API 包括：

- 取值：`getCurrentCell`、`getCellValueByRowId`、`getCellValueByObjectRelId`
- 设值：`setCellValueByRowId`、`setCellValueByObjectRelId`
- HTML 列：`setCellHTMLValueByRowId`，仅适用于 `Column Type=programHTMLOutput`
- 编辑控制：`setCellEditableByRowId`、`setCellEditableByObjectRelId`
- 刷新：`refreshRowByRowId`、`refreshSelectedRows`、`refreshStructure`、`reloadCell`
- 行关系：`getParentRowId`、`getChildrenRowIds`、`getParentColumnValue`、`getChildrenColumnValues`

JS 改单元格值不等于后台已保存。凡是涉及业务字段落库，必须继续追到 Column 的 `Update Program/Function`、JSP 提交或导入 JPO。

### Selectables 和关系字段规则

读取 ENOVIA 对象和关系数据时优先使用 select 表达式和 API：

- 对象基础字段：`id`、`type`、`name`、`revision`、`description`、`owner`、`originated`、`modified`、`current`、`policy`
- 对象属性：`attribute[ATTRIBUTE_NAME]`
- 状态时间：`state[STATE_NAME].actual`
- 关系遍历：`from[RELATIONSHIP].to.*`、`to[RELATIONSHIP].from.*`
- 关系字段：`id[connection]`、`attribute[ATTRIBUTE_NAME]`、`originated`

连接本身的业务信息必须从关系 select 读取。例如零件和图纸连接创建时间应取关系 `originated`，不能误用图纸对象的 `originated`。

### JPO 源码

主要路径：

- `plm/spinner/schema_custom/Business/SourceFiles`

常见约定：

- 文件名通常为 `ProgramName_mxJPO.java`，Spinner/JPO 调用名通常去掉 `_mxJPO`。
- JPO 方法通常通过 `JPO.unpackArgs(args)` 获取 `requestMap`、`paramMap`、`objectList`、`fieldMap` 等上下文。
- 操作 3DE 对象优先使用 `DomainObject`、`DomainRelationship`、`MapList`、`StringList`、`MqlUtil`、`ContextUtil`。
- 需要后台权限时必须使用 `ContextUtil.pushContext` / `popContext`，并确保 `finally` 中恢复上下文。
- 涉及多对象写入时应使用事务，失败时回滚。

### JSP / Web 层

主要路径：

- `plm/3dspace`

JSP 多数是动作页，不应承载复杂业务规则。JSP 应负责读取请求、校验 token、调用 JPO、返回刷新/关闭/提示脚本。复杂业务判断、权限、数据写入应放在 JPO 或公共方法中。

修改 JSP 时必须注意：

- 是否包含 CSRF 校验。
- 是否从 `objectId`、`parentOID`、`emxTableRowId`、`relId` 等参数取值。
- 是否调用了 `JPO.invoke`。
- 是否需要刷新父页面、关闭弹窗、返回 JSON 或下载文件。

### Vue 页面调用达索搜索

Vue/HTML 页面如果放在 `3dspace/common/xxx/` 子目录下，不要在 Vue 页面内直接调用 `showModalDialog("../common/emxFullSearch.jsp...")` 或绝对 `/3dspace/common/emxFullSearch.jsp` 后再依赖平台二次跳转。达索 Full Search 的 `emxUIModal.js` 内部会使用相对路径 `../common/SearchUI.html`，从子目录页面触发时容易被解析成 `/3dspace/common/common/SearchUI.html`，导致 404。

推荐模式：

1. Vue 页面只负责创建隐藏 iframe 或打开一个位于 `3dspace/common/` 根目录下的 launcher JSP。
2. launcher JSP 中加载标准脚本 `scripts/emxUIConstants.js`、`scripts/emxUICore.js`、`scripts/emxUIModal.js`。
3. launcher JSP 使用标准达索方式调用：

```javascript
showModalDialog("../common/emxFullSearch.jsp?field=TYPES=type_VPMReference&table=AEFGeneralSearchResults&showInitialResults=true&selection=multiple&submitAction=refreshCaller&submitURL=...", 850, 630, true, "Large");
```

4. `submitURL` 指向 `3dspace/common/` 下的提交 JSP，提交 JSP 读取 `emxTableRowId`，调用 JPO 立即落库。
5. 提交 JSP 完成后通过 `postMessage({type: "..."}, "*")` 通知 Vue 页面刷新；如果提交 JSP 的 opener 是 top window，Vue 页面需要同时监听自身 `window` 和 `window.top` 的 `message`。

不要在 Vue 页面里直接调用 `emxUICore.getDataPost` 去替代标准搜索流程；这会绕开平台的弹窗链路，还容易缺少 `emxUIConstants`、CSRF token 或 top-window 上下文。

## 权限和状态修改规则

修改权限时必须分别检查三类权限：

1. 入口权限  
   Command/Button 是否显示或可点击。通常在 `SpinnerCommandData_ALL.xls` 的 `Access Program/Function`。

2. 字段权限  
   表格列或表单字段是否可编辑。通常在 column/field 的 `Edit Access Program/Function`，或由表格数据方法返回的 `isEdit` 控制。

3. 写入权限  
   Update Function、上传导入、REST、后台任务是否真正限制写入字段。不能只修页面可编辑性而忽略导入或接口。

状态相关需求必须写成清晰条件，例如：

```java
if (!"Active".equals(strTaskCurrent)) {
    continue;
}
```

如果业务要求 APR 复用 Countersign 规则，应保留 Countersign 原有排除条件和角色范围，不应无意扩大原状态下的角色权限。

## 导入导出修改规则

导入导出通常涉及以下链路：

- Command 配置
- JSP 上传/下载页面
- JPO 读取模板或解析 Excel
- 表格数据方法生成当前用户可见/可编辑数据
- 按 `isEdit` 或映射 XML 写入属性

修改导入写入前必须确认：

- Excel 表头和 XML/配置映射是否一致。
- 当前用户是否有对应角色任务。
- 当前任务状态是否允许写入。
- 每一列是否在当前行的 `isEdit` 中。
- 行上关系属性为空时是否需要 fallback 到 ECR 头属性。

导入不应比页面编辑拥有更大的字段权限。若业务要求一致，应让导入复用表格数据方法生成的 `isEdit`，不要另写一套角色字段判断。

## 新增代码原则

1. 新增 JPO 方法前，先查是否已有类似方法可复用或扩展。
2. 公共规则放在已有 StaticMethod/Util 类中，避免复制粘贴到多个 Service。
3. 方法入参和返回结构应贴合 3DE 现有模式，例如 `Map`、`MapList`、`StringList`。
4. 日志应记录关键对象、状态、角色、权限判断结果，但不要打印大体量对象或敏感数据。
5. 异常不要静默吞掉。需要给前端提示时返回明确 `code` / `mess`，需要中断事务时抛出异常。
6. 属性名、关系名、类型名优先使用 `JF_PLMConstants_mxJPO` 或已有常量，不要散落硬编码。
7. 新增配置、JPO、JSP、资源文件时，要同步检查部署清单或 Spinner 注册配置。
8. 新增或修改 Java/JPO 方法时，方法上方必须补充 Javadoc 注释，格式统一如下，并按实际业务含义填写说明、参数、返回值、异常、作者和日期：

```java
/**
 * 产品配置表新建按钮显示权限
 **
 * @param context
 * @param args 请求参数
 * @return boolean 当前项目下不存在产品配置表时返回true
 * @throws Exception
 * @author caipan by codex
 * @date 2026/6/29 16:13
 */
```
由 Codex 新增的方法，Javadoc 作者必须统一填写 `@author caipan by codex`，禁止填写 `@author codex`、`@author codex caipan` 或其他 Codex 作者格式。

9. 代码行注释按新增和修改场景区分：
   - 当天新增的方法或新增代码块，方法内部直接写业务注释，不添加 `add by`、`update by` 或日期前缀；新增方法的作者和日期统一记录在方法 Javadoc 中。
   - 在已有方法或已有 JPO 逻辑中插入、调整代码时，关键修改点必须添加日期、修改人和修改说明，例如：`//20260728 update by ljr 修改说明`。
   - 同一个需求中，如果在已有方法中增加对新方法的调用，调用位置属于已有逻辑修改，需要保留 `日期 + update by + 修改人` 注释；新方法内部仍使用普通业务注释。

## 编码和编辑要求

- Java/JSP/配置文件按 UTF-8 处理。
- 不批量重排历史中文注释。
- 使用 `rg` 优先搜索调用链。
- 修改文件时尽量使用 ASCII 代码行作为 patch 锚点，避免中文注释编码导致误匹配。
- Spinner `.xls` 是文本格式时可以直接修改，但要保留制表符分隔结构。
- 每次修改制表符文本格式的 Spinner `.xls` 前，必须重新读取本规则，并检查目标文件当前的编码、行尾、表头列数和末尾记录格式；不能仅以 Excel 中的显示效果判断文件格式。
- Spinner 文本 `.xls` 必须保持 UTF-8 无 BOM，所有记录统一使用 `CRLF` 行尾，禁止混入单独的 `LF` 或 `CR`，并在文件末尾保留一个 `CRLF`。
- Spinner 文本 `.xls` 必须按原表头使用 Tab 分隔并保持固定列数，不允许插入空行；其中 `SpinnerTriggerData_ALL.xls` 每行必须为 7 列，`bo_eService Trigger Program Parameters.xls` 每行必须为 50 列。
- 向 Spinner 文本 `.xls` 追加配置时，只在文件末尾增加新记录，不替换或重写原末行。修改后必须按字节检查编码和行尾、逐行校验列数，并确认 SVN diff 只显示预期新增或修改的记录，不出现原末行被删除后重新添加。
- 不修改 `spinner_ootb`，除非明确用于对比或用户明确要求。
- 不修改、不读取 `3ddashboard、3dsearch`，除非明确用于对比或用户明确要求。
- 非必要不修改旧代码
- 尽量复用旧代码，以及旧的逻辑
- 不随意新增加方法

## 验证清单

提交或交付前至少确认：

- 改动入口是否正确：Command/Menu/Table/Form/JSP/JPO 是否都追踪到。
- 相关状态是否覆盖：目标状态允许，其他状态不允许。
- 相关角色是否覆盖：新增允许角色不会扩大旧状态权限。
- 页面编辑、导入写入、REST 或后台写入是否一致。
- 采购件、自制件、根节点、子节点、0 级件、游离件等业务分支是否受影响。
- 是否存在同名旧方法、REST 方法、下载模板或上传模板需要同步。
- 未引入无关格式化、编码变化或大范围换行变化。

## 本项目高风险点

- ECR/正式 ECR 权限常同时涉及状态、会签任务、角色、行关系、零件类型、采购类型、变更来源。
- `JFChangeSource` 可能存在于行关系上，也可能需要 fallback 到 ECR 头。
- 页面列可编辑不等于导入可写，必须检查上传处理方法。
- `ContextUtil.pushContext` 会绕过普通权限，使用时必须先完成业务权限校验。
- Spinner 配置和 JPO 方法名不一致时，页面可能无报错但功能不生效。
