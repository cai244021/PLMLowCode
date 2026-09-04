# ENOVIA / 3DEXPERIENCE UI 配置 AI 帮助手册

本文档基于本机帮助目录 `D:\文档\本地帮助文档` 中的 SingleFile HTML 文档整理，用于后续让 AI 或开发人员快速理解并修改 3DEXPERIENCE / ENOVIA 的 form、command、table、menu、toolbar、Structure Browser、emxEditableTable JS 等配置。

适用项目：`D:\PLMLowCode`；PLM源码在 `plm` 子目录。本文本地源码路径相对于项目根目录，服务器URL保持不变。

## 1. 使用原则

修改 ENOVIA 配置时不要直接猜页面行为。优先按下面链路追踪：

1. 从 Spinner 配置入口确认 Command/Menu/Table/Form/Column/Field。
2. 找到 Href、table、form、toolbar、program、function、Access Program/Function。
3. 追到 JPO/JSP 实际方法，确认读取、展示、编辑、导入、导出、写入是否共用同一套逻辑。
4. 再做最小改动，不批量格式化历史文件，不绕过业务权限。

## 2. 本地帮助文档清单

帮助目录共有 34 个 SingleFile HTML：

- 通用配置：`Settings.html`、`URLParameters.html`、`Selectables.html`、`MQL_Parser_Keywords.html`
- Structure Browser：`emxIndentedTable_URL.html`、`TableColumns_Setting.html`、`Toolbar_Setting.html`
- Web Form：`FormSetting_URL.html`、`FormURLParameters.html`
- emxEditableTable JS API：
  - `Complete Cell Information.html`
  - `Getting Current Cell Details.html`
  - `Getting Cell Values by Row ID.html`
  - `Getting Cell Values by Object ID.html`
  - `Setting Cell Value by Row ID.html`
  - `Setting Cell Values By Object _ Relationship ID.html`
  - `Setting the HTML Value of a Cell by Row ID.html`
  - `Enabling _ Disabling Cell Edit by Row ID.html`
  - `Enabling _ Disabling Cell Edit by Object _ Relationship ID.html`
  - `Getting Parent ID.html`
  - `Getting Child IDs.html`
  - `Getting Parent Column Values.html`
  - `Getting Child Column Values.html`
  - `Selecting Rows.html`
  - `Expanding Rows.html`
  - `Refreshing Rows.html`
  - `Refreshing Table Columns.html`
  - `Refreshing the Header Subtitle.html`
  - `Reloading Cell Values.html`
  - `Removing Selected Rows.html`
  - `Displaying Validation Errors.html`
- Markup APIs：
  - `Load Mark Up.html`
  - `Adding Rows to Selected Rows.html`
  - `Deleting Rows.html`

SingleFile 文档很大，正文通常在 `class=text-area` 后面；代码示例通常在 `pre.codeblock`；配置表通常是 `tr/td/th`。抽取时应过滤 `script/style/svg`，不要把左侧目录树当正文。

## 3. Spinner 配置文件对应关系

仓库中的主要配置入口：

- Command：`plm/spinner/schema_custom/Business/SpinnerCommandData_ALL.xls`
- Menu：`plm/spinner/schema_custom/Business/SpinnerMenuData_ALL.xls`
- Table：`plm/spinner/schema_custom/Business/SpinnerTableData_ALL.xls`
- Table Column：`plm/spinner/schema_custom/Business/SpinnerTableColumnData_ALL.xls`
- Web Form：`plm/spinner/schema_custom/Business/SpinnerWebFormData_ALL.xls`
- Web Form Field：`plm/spinner/schema_custom/Business/SpinnerWebFormFieldData_ALL.xls`
- Program/JPO 注册：`plm/spinner/schema_custom/Business/SpinnerProgramData_ALL.xls`
- JPO 源码：`plm/spinner/schema_custom/Business/SourceFiles`
- JSP/Page：`plm/spinner/schema_custom/Business/PageFiles`、`plm/3dspace`

Spinner `.xls` 多数是制表符文本，适合用 `rg` 搜索命令名、表名、列名、JPO 方法名。

## 4. Command / Menu 修改方法

Command 通常控制按钮、分类页入口、toolbar 按钮或右键菜单项。重点字段：

- `Name`：命令名，通常也是业务入口名称。
- `Label`：显示文案或资源 key。
- `Href`：JSP 或 common 页面入口，例如 `../common/emxIndentedTable.jsp?...`。
- `Target Location`：页面打开位置，例如 `popup`、`slidein`、`content`。
- `Access Program` / `Access Function`：按钮是否显示或是否可用。
- `Registered Suite`：资源目录和国际化归属。

Menu 通常是命令集合。追链路时先搜 Command，再搜它属于哪个 Menu；也要反向搜 Menu 是否被 table toolbar、form toolbar、RMB Menu、tree category 使用。

Toolbar menu 对象常用 setting：

- 权限：`Access Expression`、`Access Mask`、`Access Program`、`Access Function`、`Access Behavior`
- 动态菜单：`Dynamic Command Program`、`Dynamic Command Function`
- 显示：`Image`、`Maximum Length`、`Mode`、`Pull Right`、`showAsSubMenu`
- 右键：`RMB Menu`
- 平台隐藏：`Hide Mode`，值可用 `Desktop`、`Mobile`、`Cloud`、`!Desktop`、`!Mobile`、`!Cloud`

## 5. Structure Browser / Table 配置

Structure Browser 常用入口是：

```text
../common/emxIndentedTable.jsp?table=<TableName>&program=<JPO:method>&toolbar=<ToolbarName>&objectId=${OBJECTID}
```

核心 URL 参数：

- 数据来源：`program`、`expandProgram`、`relationship`、`direction`、`inquiry`
- 表定义：`table`、`appendColumns`
- 工具栏：`toolbar`、`tableMenu`、`showRMB`
- 页面标题：`header`、`subHeader`、`HelpMarker`
- 模式：`mode=view|edit`、`editLink`、`selection=multiple|single|none`
- 展开：`emxExpandFilter`、`expandLevelFilter`、`expandProgramMenu`
- 编辑：`showApply`、`triggerValidation`、`massUpdate`、`insertNewRow`
- 排序：`sortColumnName`、`sortDirection`、`multiColumnSort`
- 导出/打印：`Export`、`PrinterFriendly`
- 过滤：`autoFilter`、`directionFilter`、`relationshipFilter`、`typeFilter`
- 性能/展示：`pageSize`、`parallelLoading`、`freezePane`、`cellwrap`
- 提交/回调：`submitURL`、`applyURL`、`connectionProgram`、`lookupJPO`

Table Column 是最常修改的对象。重点 setting：

- 数据来源：`expression`、`program`、`function`、`Column Type`
- 展示：`Label`、`format`、`Width`、`Sortable`、`Nowrap`、`Group Header`
- 链接：`href`、`Target Location`、`Show Link`、`Alternate OID expression`、`Alternate Type expression`
- 编辑：`Editable`、`Input Type`、`Allow Manual Edit`、`Required`
- 权限：`Access Program` / `Access Function`、`Edit Access Program` / `Edit Access Function`
- Range：`Range Program` / `Range Function`、`Sort Range Values`
- 更新：`Update Program` / `Update Function`
- 校验：`Validate`、`Validate Type`
- 刷新：`Reload Program` / `Reload Function`
- Type Ahead：`Type Ahead Mapping`、`Type Ahead Validate`
- 样式：`Style Program` / `Style Function`、`Style Column`
- 统计：`Calculate Sum`、`Calculate Average`、`Calculate Minimum`、`Calculate Maximum`
- 拖拽：`Draggable`、`Droppable`、`Drag Types`、`Drop Types`、`Drop Relationships`、`Drop JPO`

修改列可编辑性时必须同时检查三层：

1. Column 上的 `Editable` 和 `Edit Access Program/Function`。
2. 表格数据方法里是否返回 `isEdit`、`rowEdit` 或自定义字段权限。
3. `Update Program/Function`、导入 JPO 或 REST 写入是否复用同一套权限。

## 6. Web Form 配置

Form 常用入口：

```text
../common/emxForm.jsp?form=<FormName>&mode=view|edit&objectId=${OBJECTID}
```

常用 URL 参数：

- `form`：Web Form 管理对象，必填。
- `mode`：`view` 或 `edit`。
- `objectId`、`relId`：当前对象或关系。
- `toolbar`：form toolbar。
- `editLink`：view 页面是否展示默认 Edit。
- `appendFields`：追加字段 form。
- `postProcessJPO` / `postProcessURL`：提交后处理。
- `preProcessJavaScript`：提交前 JS。
- `submitAction`、`submitLabel`、`submitMultipleTimes`
- `targetLocation`、`showTabHeader`、`PrinterFriendly`

Form Field 重点 setting：

- 数据来源：`program`、`function`、`Field Type`
- 编辑：`Editable`、`Input Type`、`Required`、`Read Only Check box`
- 更新：`Update Program` / `Update Function`
- 校验：`Validate`、`Validate Type`
- Range：`Range Program` / `Range Function`
- 动态字段：`Dynamic Field Program` / `Dynamic Field Function`
- 权限：`Access Program` / `Access Function`、`Access Mask`、`Access Expression`
- 展示：`format`、`Display Time`、`Display Format`、`Field Size`、`Rows`、`Cols`
- 分组：`Group Name`、`Vertical Group Name`、`Section Level`
- 链接/计数：`Show Link`、`Counter Link`、`Show Counter`
- 表格型字段：`table`、`Table Columns`、`Table Actions`、`Table Link`
- TypeAhead：`TypeAhead`、`TypeAhead Program`、`TypeAhead Function`

## 7. JPO 回调约定

常见 JPO 方法形态：

- Table/expand 数据：返回 `MapList`，行里通常包含 `id`、`id[connection]`、`relid`、`level` 等。
- Column Program：返回 `Vector`，按 `objectList` 顺序输出每行列值。
- Access Program：返回 `boolean` 或框架要求的 Boolean 值。
- Range Program：返回可选值集合，常见为 `HashMap` 或 `StringList`，按项目现有代码为准。
- Update Program：接收 `JPO.unpackArgs(args)` 后的 `requestMap`、`paramMap`、`objectList` 等上下文，写对象或关系属性前必须校验权限。

写 JPO 时优先使用：

- `DomainObject`
- `DomainRelationship`
- `MapList`
- `StringList`
- `ContextUtil.pushContext/popContext`
- `JPO.unpackArgs(args)`
- 项目已有 `JF_PLMConstants_mxJPO` 常量和 `JF_*Service_mxJPO`、`JF_*StaticMethod_mxJPO`、`JF_PublicMethodClass_mxJPO`

后台提升权限只解决系统权限，不等于业务权限。写入前必须先判断角色、任务、状态、行权限、字段权限。

## 8. Selectables 使用

`Selectables.html` 说明可以通过 select 表达式读取业务对象、关系和管理对象信息。常用业务对象 select：

```text
id
type
name
revision
description
owner
originated
modified
current
policy
state[STATE].actual
attribute[ATTRIBUTE_NAME]
from[RELATIONSHIP].to.id
from[RELATIONSHIP].to.name
to[RELATIONSHIP].from.id
to[RELATIONSHIP].from.name
relationship[RELATIONSHIP]
```

关系 select 常用于查询连接属性和连接创建时间：

```text
id[connection]
attribute[ATTRIBUTE_NAME]
originated
from.id
from.name
to.id
to.name
```

在项目中，常见规则是：属性名、关系名、类型名优先用 `JF_PLMConstants_mxJPO`，不要散落硬编码。

## 9. emxEditableTable JS API

这些 API 多数由 `emxEditableTable` 暴露，用于 Structure Browser 的前端行列操作。

### 读取单元格

- 当前焦点单元格：

```javascript
emxEditableTable.getCurrentCell()
```

- 按 rowId 和列名读取：

```javascript
emxEditableTable.getCellValueByRowId(rowId, colName)
```

- 按 relId/objectId 和列名读取：

```javascript
emxEditableTable.getCellValueByObjectRelId(relId, objectId, colName)
```

返回值为 `CompleteCellInfo`，常见字段：

```text
rowID
columnName
relid
objectid
type
```

### 设置单元格

- 按 rowId 设置实际值和显示值：

```javascript
emxEditableTable.setCellValueByRowId(rowId, colName, cellValue, cellDisplayValue, isRefreshView)
```

- 按 relId/objectId 设置实际值和显示值：

```javascript
emxEditableTable.setCellValueByObjectRelId(relId, objectId, colName, cellValue, cellDisplayValue, isRefreshView)
```

- 设置 HTML 值，仅用于 `Column Type=programHTMLOutput` 的列：

```javascript
emxEditableTable.setCellHTMLValueByRowId(rowId, colName, html, isRefreshView)
```

### 控制可编辑

- 按 rowId 控制单元格编辑：

```javascript
emxEditableTable.setCellEditableByRowId(rowId, colName, true, isRefreshView)
emxEditableTable.setCellEditableByRowId(rowId, colName, false, isRefreshView)
```

- 按 relId/objectId 控制单元格编辑：

```javascript
emxEditableTable.setCellEditableByObjectRelId(relId, objectId, colName, true, isRefreshView)
```

### 父子行

```javascript
emxEditableTable.getParentRowId(rowId)
emxEditableTable.getChildrenRowIds(rowId)
emxEditableTable.getParentColumnValue(rowId, colName, level)
emxEditableTable.getChildrenColumnValues(rowId, colName, level)
```

### 行选择和展开

```javascript
emxEditableTable.select(rowIds)
emxEditableTable.expand(rowIds, level)
```

`rowIds` 可以是数组，例如：

```javascript
var rowIds = [];
rowIds.push("0,0");
rowIds.push("0,0,0");
emxEditableTable.select(rowIds);
```

### 刷新

```javascript
emxEditableTable.refreshRowByRowId(rowIdOrArray)
emxEditableTable.refreshSelectedRows()
emxEditableTable.refreshStructure()
emxEditableTable.refreshStructureWithOutSort()
emxEditableTable.reloadCell("Column1,Column2")
emxEditableTable.setSubHeader("new subtitle")
```

`reloadCell` 依赖列配置中的 `Reload Program` 和 `Reload Function`。

### 删除/移除/校验错误

- 只从页面移除选中行，不删除数据：

```javascript
emxEditableTable.removeRowsSelected()
```

- 展示校验错误：

```javascript
emxEditableTable.displayValidationMessags(xml)
```

XML 示例：

```xml
<mxRoot>
  <object rowId="0,5">
    <error>Error message</error>
  </object>
</mxRoot>
```

## 10. Markup APIs

Markup XML 用于结构浏览器新增、移除、刷新等前端变更。

新增行示例：

```xml
<mxRoot>
  <object objectId="PARENT_OBJECT_ID">
    <object objectId="CHILD_OBJECT_ID" relId="" relType="relationship_EBOM" markup="add"></object>
  </object>
</mxRoot>
```

删除行示例：

```javascript
var removeXML = "<mxRoot>";
removeXML += "<action refresh=\"true\" fromRMB=\"true\"><![CDATA[remove]]></action>";
removeXML += "<item id=\"" + rowId + "\" />";
removeXML += "<message><![CDATA[]]></message>";
removeXML += "</mxRoot>";
removedeletedRows(removeXML);
```

添加到选中行上方/下方时，XML 常见属性包括：

```text
oid
relId
pid
pasteAboveToRow
pasteBelowToRow
pasteBelowOrAbove
RowEditable
```

## 11. 常见任务操作模板

### 新增按钮

1. 在 `SpinnerCommandData_ALL.xls` 增加 Command。
2. 配置 `Href` 指向 JSP 或 common 组件。
3. 配置 `Access Program/Function`。
4. 把 Command 加到对应 `SpinnerMenuData_ALL.xls` 的 toolbar/menu。
5. 如果 Href 进入表格或表单，继续检查 table/form/column/field 配置。

### 修改表格列

1. 搜列名或表名，定位 `SpinnerTableColumnData_ALL.xls`。
2. 确认列属于哪个 `Table`。
3. 查看 `expression/program/function` 决定值来源。
4. 查看 `Editable`、`Edit Access Program/Function`、`Update Program/Function`。
5. 若是导入导出字段，继续追导出模板和导入解析 JPO。

### 修改表单字段

1. 搜 form 名和字段名。
2. 检查 field 的 `program/function`、`Editable`、`Required`。
3. 如果字段可写，检查 `Update Program/Function` 和后端权限。
4. 如果字段是选择值，检查 `Range Program/Function` 或 `TypeAhead`。

### 修改导入导出

1. 从导出/导入按钮 Command 找 JSP 或 JPO。
2. 找模板生成方法和 Excel 表头。
3. 找导入解析方法和列序号/字段映射。
4. 确认导入写入是否复用页面可编辑权限。
5. 新增列时导出和导入必须同步。

## 12. 交付前检查清单

- 是否从 Command/Menu/Table/Form/Column/Field 追到实际 JPO/JSP？
- 是否确认按钮入口权限、字段编辑权限、后端写入权限一致？
- 是否确认目标状态、角色、任务状态不会扩大旧权限？
- 是否确认导入、导出、页面编辑都同步？
- 是否使用 `JF_PLMConstants_mxJPO` 或已有常量？
- 是否避免修改 `spinner_ootb`、`3ddashboard`、`3dsearch`？
- 是否没有批量格式化、编码变化、大范围无关换行？
- 若使用 `pushContext`，是否在 `finally` 中 `popContext`？

## 13. 可直接喂给其他 AI 的提示词

```text
你正在修改 D:\PLMLowCode 项目中的 ENOVIA / 3DEXPERIENCE 增量模块。
项目根目录是 D:\PLMLowCode；PLM源码位于 plm 子目录。以下路径相对于项目根目录。
修改前必须先追 Spinner 配置链路：Command/Menu/Table/Form/Column/Field -> JSP/JPO -> 后端写入。
优先搜索这些文件：
- plm/spinner/schema_custom/Business/SpinnerCommandData_ALL.xls
- plm/spinner/schema_custom/Business/SpinnerMenuData_ALL.xls
- plm/spinner/schema_custom/Business/SpinnerTableData_ALL.xls
- plm/spinner/schema_custom/Business/SpinnerTableColumnData_ALL.xls
- plm/spinner/schema_custom/Business/SpinnerWebFormData_ALL.xls
- plm/spinner/schema_custom/Business/SpinnerWebFormFieldData_ALL.xls
- plm/spinner/schema_custom/Business/SpinnerProgramData_ALL.xls
- plm/spinner/schema_custom/Business/SourceFiles

不要只改页面显示；必须确认入口权限、字段编辑权限、后端写入权限、导入导出是否一致。
优先复用项目已有 JPO、常量和业务权限方法。不要改 spinner_ootb，不要批量格式化历史文件。

Structure Browser 常用入口是 emxIndentedTable.jsp，关键参数包括 table、program、expandProgram、relationship、direction、toolbar、mode、selection、submitURL、applyURL、showApply、triggerValidation。
Form 常用入口是 emxForm.jsp，关键参数包括 form、mode、objectId、relId、toolbar、postProcessJPO、postProcessURL。
Table Column 常查 Editable、Input Type、Edit Access Program/Function、Update Program/Function、Range Program/Function、Validate、Reload Program/Function。
Form Field 常查 Editable、Input Type、Required、Access Program/Function、Update Program/Function、Range Program/Function、Validate。

涉及表格 JS 时可使用 emxEditableTable：
- getCellValueByRowId(rowId, colName)
- setCellValueByRowId(rowId, colName, cellValue, cellDisplayValue, isRefreshView)
- setCellEditableByRowId(rowId, colName, isEditable, isRefreshView)
- refreshRowByRowId(rowIdOrArray)
- refreshStructure()
- reloadCell("Column1,Column2")
- displayValidationMessags(xml)
```
