# PLM低代码设计器使用手册

版本：内部运行版 0.5  
日期：2026-09-04

## 1. 功能定位

本系统用于通过 AMIS Editor 设计表单、Table 和展示页面，并为页面组件配置 3DEXPERIENCE/ENOVIA Scheme 字段及 JPO 动作绑定。

当前版本已经完成：

- AMIS 页面可视化设计与即时预览。
- 页面列表、空白新建、复制当前页面和页面切换。
- 页面软删除及确认提示，保留已保存内容与历史版本。
- 页面 JSON 保存及不可变历史版本。
- PLM Scheme 字段库维护。
- `actionCode → JPO名称/执行方法` 动作库维护。
- 表单控件与 PLM 字段绑定。
- 按钮或表单事件与 JPO 动作绑定。
- Table 查询动作、结果路径、对象 ID 和关系 ID 字段绑定。
- 页面 Schema 与 PLM 配置包导出。

当前版本的配置包可由 3DSpace JSP Runtime 和 Dashboard Widget Runtime 共同解析。编辑器服务器不直接执行 JPO；真实动作始终在登录用户所在的 PLM 环境执行。

## 2. 启动和访问

本项目根目录为 `D:\PLMLowCode`。编辑器在 `app`，PLM增量在 `plm`，Widget在 `dashboard/TWX_ENOPS_app`。

本地页面包目录：`D:\PLMLowCode\plm\3dspace\common\JFLowCode\pages`。
本文部署说明中的 `3dspace/common/...` 指服务器路径；本地对应路径需要加上 `D:\PLMLowCode\plm\`，不要把这个前缀加到浏览器URL中。

运行：

```powershell
D:\PLMLowCode\app\scripts\start-backend.ps1
```

浏览器访问：

```text
http://127.0.0.1:8080/
```

如果页面显示旧资源，使用 `Ctrl + F5` 强制刷新。

## 3. 页面设计

### 3.1 新建、复制与切换页面

1. 点击顶部导航“页面管理”，查看已保存的页面、编码和版本。
2. 点击“新建页面”，填写编码（例如 `JF_PART_QUERY`）和名称（例如“零件查询页面”）。
3. 编码只允许大写字母、数字、下划线，长度1～100；名称不能为空且最多200字。
4. 点击“创建并打开”，系统保存独立的V1页面并打开空白画布。编码已存在时会报错，不会覆盖原页面。
5. 拖入组件后点击“保存页面”，只更新当前编码的页面及其版本。
6. 回到“页面管理”，点击页面卡片可打开其他页面。未保存时会提示是否放弃修改；选择取消可继续编辑。

复制页面：先打开要复制的源页面，再进入“页面管理”点击“复制当前页面”。副本包含当前画布JSON和PLM绑定，但使用新编码、独立的V1版本，不复制源页面的历史版本。源页面有未保存修改时，确认后将这些内容复制到新页面，源页面数据库内容保持不变。

页面名称与画布标题是两项配置：顶部名称用于页面管理；画布标题在AMIS右侧属性中调整。编码创建后不能在界面修改。PLM字段库和JPO动作库是所有页面共享的，页面绑定则各自保存。

首次进入优先打开竞品BOM示例；如果示例不存在则打开最近更新的页面；无任何页面时显示页面管理。刷新或关闭浏览器时，未保存内容会触发浏览器提醒（以浏览器行为为准）。本版不提供自动草稿恢复。

### 3.2 删除页面

1. 进入“页面管理”，点击目标页面卡片下方的“删除页面”。
2. 确认框会显示名称和编码，请核对后点击“确认删除”；点击“取消”不改变页面。
3. 删除当前正在编辑的页面时，会同时退出该页面并清空编辑状态；未保存内容会明确提示将丢失。删除其他页面不会清空当前页面的修改。

本版采用软删除：页面从列表隐藏，正常读取返回404，旧窗口保存返回410；数据库中的页面JSON、PLM绑定和历史版本保留。删除后编码仍被占用，不能以相同编码创建新页。重复删除同一页面返回204，不会重复写历史版本。

本版暂无回收站和恢复按钮，误删后需要管理员核对记录再恢复。不影响已下载的JSON文件，也不会删除或下线PLM上的已部署页面。

### 3.3 编辑页面内容

1. 点击顶部“页面设计”。
2. 从左侧组件区选择表单、文本框、下拉框、按钮、Table 等组件。
3. 在中间画布选择组件。
4. 在右侧配置组件名称、数据 Key、标题、校验规则、布局和样式。
5. 建议为需要绑定 PLM 的表单字段填写唯一 `name`，例如 `title`、`description`、`partType`。
6. 点击顶部“保存页面”，Schema 和 PLM 绑定会共同保存并产生新版本。

AMIS Editor 会给组件生成稳定 ID，例如 `u:title-field`。页面PLM绑定通过该 ID 定位组件，组件调整位置不会导致绑定失效；删除组件后需要同步删除对应绑定。

## 4. 页面预览

在“页面设计”中点击顶部“预览”：

- 直接渲染当前内存中的页面 JSON，不要求先保存。
- 隐藏组件面板和属性面板。
- 可以查看表单、Table、布局、文字和按钮的基础效果。
- 点击“设计”返回编辑状态。

![页面预览](images/页面预览.png)

## 5. PLM字段库

点击顶部“PLM字段库”。

字段配置说明：

| 配置项 | 说明 | 示例 |
| --- | --- | --- |
| 字段编码 | 低代码平台内部唯一编码 | `PART_TYPE` |
| 显示名称 | 设计人员可识别的名称 | 零件子类型 |
| 对象类型 | 字段所属 PLM Type，`*` 表示通用 | `JF_CompetitiveBOM` |
| 字段来源 | BASIC、ATTRIBUTE、RELATIONSHIP、PROGRAM | ATTRIBUTE |
| Scheme字段 | PLM真实字段或Select表达式对应名称 | `JF_VPMReference.JF_PartType` |
| 数据类型 | 文本、数字、日期、枚举等 | enum |
| 国际化Key | PLM已有资源文件 Key | `emxFramework.Attribute.JF_PartType` |
| Range来源 | 无、固定选项、PLM Range、JPO | FIXED |
| Range配置JSON | 固定选项或JPO Range参数 | `{"options":[...]}` |
| 必填/可编辑/多值 | 字段元数据 | 按Scheme填写 |

固定下拉选项示例：

```json
{
  "options": [
    {"label": "整椅", "value": "C"},
    {"label": "面套", "value": "T"},
    {"label": "发泡", "value": "U"}
  ]
}
```

## 6. JPO动作库

点击顶部“JPO动作库”。

动作库由开发人员或管理员维护。页面设计人员只选择 `actionCode`，不直接输入任意 JPO。

| 配置项 | 说明 | 示例 |
| --- | --- | --- |
| 动作编码 | 页面调用的唯一白名单编码 | `CREATE_COMPETITIVE_BOM` |
| 动作名称 | 业务名称 | 创建竞品BOM |
| 动作类型 | CREATE、UPDATE、QUERY、ACTION、NAVIGATION | CREATE |
| 请求方式 | Runtime调用动作页使用的方法 | POST |
| JPO名称 | Spinner/JPO程序名，不含 `_mxJPO` | `JF_CompetitiveBOM` |
| 执行方法 | JPO公开方法 | `createCompetitiveBOM` |
| 输入映射 | 页面数据和上下文到JPO参数的映射 | `{"title":"${title}"}` |
| 输出映射 | JPO返回结果到标准数据的映射 | `{"objectId":"data.objectId"}` |
| 启用 | 禁用后页面不能选择或执行该动作 | true |

查询动作应统一返回：

```json
{
  "status": 0,
  "msg": "",
  "data": {
    "items": [],
    "total": 0
  }
}
```

创建或更新动作应统一返回：

```json
{
  "status": 0,
  "msg": "创建成功",
  "data": {
    "objectId": "21798.25570.25798.37575",
    "name": "BM-110001"
  }
}
```

## 7. 页面PLM绑定

点击顶部“页面PLM绑定”。系统会扫描当前 AMIS Schema 中带 ID 的组件，并按用途分组。

![页面PLM绑定](images/页面PLM绑定.png)

### 7.1 页面上下文

配置运行时 URL 参数名称：

- 对象 ID：默认 `objectId`。
- 父对象 ID：默认 `parentOID`。
- 关系 ID：默认 `relId`。

JSP 和 Widget Runtime 应将这些参数转换为统一的 `plmContext`。

### 7.2 表单字段绑定

1. 点击“新增绑定”。
2. 选择 AMIS 页面组件。
3. 选择 PLM 字段库中的 Scheme 字段。
4. 填写页面数据 Key。

示例：

```text
页面组件：标题 · input-text · u:title-field
PLM字段：标题 · PLMEntity.V_Name
数据Key：title
```

### 7.3 按钮与表单事件

1. 选择按钮或表单组件。
2. 选择点击、提交或值变化事件。
3. 选择 JPO 动作库中的 actionCode。
4. 配置成功后的页面行为。

成功后行为包括：

- 不处理。
- 刷新页面或Table。
- 关闭弹窗。
- 使用返回的 `data.objectId` 打开对象详情。

### 7.4 Table查询和对象ID

1. 选择 Table、Table 2.0 或 CRUD 组件。
2. 选择类型为 QUERY 的 JPO 动作。
3. 配置列表数据路径，默认 `data.items`。
4. 配置总数路径，默认 `data.total`。
5. 配置每行对象 ID 字段，默认 `objectId`。
6. 配置每行关系 ID 字段，默认 `relId`。

后续行按钮可以通过当前行的 `objectId` 打开详情或执行JPO，通过 `relId` 操作连接关系。

## 8. 保存与版本

“保存页面”和“保存页面及绑定”执行相同的版本操作：

- 更新 `lc_page` 当前版本。
- 向 `lc_page_version` 写入不可变历史版本。
- 同一个版本同时保存 AMIS Schema 和 PLM Config。

因此回退页面时，不会出现页面布局已经回退但PLM动作仍是新配置的问题。

## 9. 导出配置包

在“页面PLM绑定”点击“导出配置包”，生成：

```text
JF_COMPETITIVE_BOM_CREATE-V6.json
```

导出使用当前页面的真实编码。未保存修改也可导出，但文件名使用 `页面编码-DRAFT.json`，导出不会自动保存数据库；需要正式版本文件时先保存再导出。

配置包结构：

```json
{
  "formatVersion": 1,
  "pageCode": "JF_COMPETITIVE_BOM_CREATE",
  "pageName": "竞品BOM低代码配置示例",
  "version": 6,
  "schema": {},
  "plmConfig": {
    "context": {},
    "fieldBindings": [],
    "actionBindings": [],
    "tableBindings": []
  }
}
```

该结构可以同时供 3DSpace JSP Runtime 和 Dashboard Vue Widget Runtime 使用，不需要生成两份页面JSON。导出后将文件名调整为页面编码，例如 `JF_COMPETITIVE_BOM_CREATE.json`，部署到 `3dspace/common/JFLowCode/pages/`。

## 10. 运行端部署与验证

3DSpace需要部署：

- `common/JF_LowCodeRuntime.jsp`：3DSpace页面入口。
- `common/JF_LowCodeAction.jsp`：受控动作入口。
- `common/JFLowCode/runtime.js`：JSP与Widget共用的配置编译和动作分发逻辑。
- `common/JFLowCode/amis/`：固定版本的AMIS离线SDK。
- `common/JFLowCode/pages/`：设计器导出的页面配置包。

3DSpace访问示例：

```text
/3dspace/common/JF_LowCodeRuntime.jsp?pageCode=JF_COMPETITIVE_BOM_CREATE
```

Widget侧加载同一个页面配置包和 `runtime.js`，但动作适配器可复用Widget已有REST。例如当前竞品BOM示例在Widget中复用现有 `createVPMReferenceV5ByRest`，在3DSpace中通过受控Action JSP调用JPO。

运行端只允许已登记的 `actionCode`。页面配置包中的JPO名称、方法名、Type、Policy或MQL即使被人为篡改，也不会直接执行。

## 11. 当前示例效果

系统内置“竞品BOM低代码配置示例”：

- 创建表单：标题、描述、零件子类型。
- 标题绑定 `PLMEntity.V_Name`。
- 描述绑定 `description`。
- 零件子类型绑定 `JF_VPMReference.JF_PartType`。
- 表单提交绑定 `CREATE_COMPETITIVE_BOM`。
- 创建成功配置为打开返回对象详情。
- Table绑定 `QUERY_COMPETITIVE_BOM`。
- Table行对象ID字段为 `objectId`，关系ID字段为 `relId`。

## 12. 当前边界和后续工作

进入真实PLM运行前仍需开发：

- PLM字段库从Scheme自动同步。
- PLM国际化资源加载。
- 动态页面、字段和按钮权限。
- 自动草稿恢复、发布、回退和目标环境管理（页面列表、新建、复制、切换已完成）。

新增页面在设计器中可以保存、预览、导出；部署到PLM后是否可打开仍受Runtime页面白名单和动作白名单约束，不代表新增页面自动发布到PLM。

无论前端是否隐藏按钮或字段，真实写入JPO都必须再次检查当前用户、对象状态、角色和字段权限。
