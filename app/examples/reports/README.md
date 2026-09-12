# PLM智能报表示例

本目录包含三个可复用的完整页面配置包：

- `JF_APPROVAL_TASK_REPORT.json`：审核任务、临期任务、逾期任务和处理趋势。
- `JF_PROJECT_TASK_STATUS_REPORT.json`：项目任务进度、里程碑偏差、责任人负荷和延期风险。
- `JF_CHANGE_EXECUTION_REPORT.json`：变更执行状态、变更类型分布、积压趋势和关闭周期。

## 页面交互

每个页面都使用同一个“汇总Service + 动态指标卡 + 动态图表 + 明细CRUD”结构。指标卡由统一的`metrics[]`返回数据驱动，报表可以返回任意数量的统计项；点击数字后，AMIS通过`reload`向名称固定为`reportDetailList`的CRUD传递`scope`并重新查询第一页。例如：

```text
reportDetailList?scope=overdue&page=1
```

筛选表单、分页、排序参数也会随CRUD查询提交。JPO应按请求参数执行服务端过滤和排序；不要先返回全部PLM数据再在浏览器统计。

## 需要配置的动作库

所有报表只共用两个动作，页面通过只读的`reportCode`区分报表场景。仓库已通过`V12__add_generic_plm_report_actions.sql`登记动作，并在`JF_LowCode_mxJPO`实现以下两个入口；部署数据库迁移和JPO后需要重新发布报表页面，使Page保存最新动作快照：

| 动作 | 用途 |
| --- | --- |
| `QUERY_PLM_REPORT_SUMMARY` | 按`reportCode`返回指标、图表、提示和筛选选项 |
| `QUERY_PLM_REPORT_DETAILS` | 按`reportCode`、`scope`和筛选条件返回分页明细 |

两个通用动作建议把`inputMapping`配置为`{}`，让现有动作网关原样传递`reportCode`、`scope`、分页、排序以及JSON后续增加的筛选字段。这样新增筛选条件不需要同步修改动作库。通用JPO必须按每个`reportCode`维护允许参数白名单，并使用服务端白名单将`reportCode`映射到固定报表实现，不能把它当作JPO名、方法名、MQL或select表达式直接执行。实际用户必须从`Context`获取，不允许由JSON传入用户名代替权限判断。

建议动作定义：

```json
[
  {
    "actionCode": "QUERY_PLM_REPORT_SUMMARY",
    "actionKind": "QUERY",
    "jpoName": "JF_LowCode",
    "methodName": "getReportSummaryLowCode",
    "httpMethod": "POST",
    "inputMapping": {},
    "outputMapping": {},
    "enabled": true
  },
  {
    "actionCode": "QUERY_PLM_REPORT_DETAILS",
    "actionKind": "QUERY",
    "jpoName": "JF_LowCode",
    "methodName": "getReportDetailsLowCode",
    "httpMethod": "POST",
    "inputMapping": {},
    "outputMapping": {"items": "data.items", "total": "data.total"},
    "enabled": true
  }
]
```

建议的动作库输入映射：

```json
{
  "QUERY_PLM_REPORT_SUMMARY": {
    "reportCode": "${reportCode}"
  },
  "QUERY_PLM_REPORT_DETAILS": {
    "reportCode": "${reportCode}",
    "scope": "${scope}",
    "page": "${page}",
    "perPage": "${perPage}",
    "orderBy": "${orderBy}",
    "orderDir": "${orderDir}",
    "filters": "${filters}"
  }
}
```

如果当前动作库不支持把整个筛选对象映射为`filters`，可以把各页面的筛选字段逐项加入明细动作输入映射；汇总、指标、图表和明细返回协议不需要改变。

## 汇总动作返回契约

所有场景使用相同返回结构。`metrics`驱动指标卡，`charts`直接承载JSON格式的ECharts Option，`filterOptions`保存筛选项：

```json
{
  "status": 0,
  "msg": "",
  "data": {
    "reportCode": "APPROVAL_TASK",
    "insight": {
      "level": "warning",
      "message": "2项任务已经逾期，其中1项阻塞产品发布，建议今天优先处理。"
    },
    "metrics": [
      {"code": "pending", "label": "待我审核", "value": 18, "unit": "", "scope": "pending", "level": "primary", "description": "点击查看待处理任务"},
      {"code": "overdue", "label": "已逾期", "value": 2, "unit": "", "scope": "overdue", "level": "danger", "description": "点击定位风险任务"}
    ],
    "charts": [
      {
        "code": "statusDistribution",
        "title": "任务状态分布",
        "option": {
          "tooltip": {"trigger": "item"},
          "series": [{"type": "pie", "data": [{"name": "待处理", "value": 18}]}]
        }
      },
      {
        "code": "taskTrend",
        "title": "近14天处理趋势",
        "option": {
          "xAxis": {"type": "category", "data": ["09-01", "09-02"]},
          "yAxis": {"type": "value"},
          "series": [{"name": "完成", "type": "line", "data": [2, 6]}]
        }
      }
    ],
    "filterOptions": {
      "taskType": [{"label": "批准任务", "value": "Approval"}],
      "riskLevel": [{"label": "逾期", "value": "overdue"}]
    }
  }
}
```

三个页面都只读取`insight`、`metrics`、`charts`、`filterOptions`四个通用节点。不同报表只改变数组内容、筛选字段和Table列，不再改变整体协议。

## 明细动作返回契约

所有明细动作统一返回。`filterOptions`由当前用户有权查看的全量数据生成，不随当前分页或筛选条件收缩：

```json
{
  "status": 0,
  "msg": "",
  "data": {
    "items": [],
    "total": 0,
    "filterOptions": {
      "current": [{"label": "草稿", "value": "Create"}],
      "changeType": [{"label": "客户需求", "value": "CustomerRequirement"}]
    }
  }
}
```

`items`可直接使用ENOVIA `MapList`。基础字段使用`name`、`current`、`owner`、`originated`等标准key，属性字段直接使用`attribute[属性名]`，不需要再转换一遍。风险等级、逾期天数、完成度等报表计算字段使用稳定的通用key，由报表实现补充。

## 权限和统计口径

- 汇总和明细必须复用同一份对象可见权限、角色权限和协作区权限，避免“数字看得到但明细打不开”。
- 点击指标后的`scope`只代表统计口径，不能替代JPO权限校验。
- 审核任务默认只查询当前用户的Inbox任务；管理视图必须由独立权限控制。
- 项目任务状态报表默认查询当前用户拥有的Task；如需项目经理查看项目全量任务，应新增独立的管理报表编码和权限判断，不能由JSON传入用户名扩大范围。
- 变更执行报表默认查询当前用户拥有的DA，并用`state[Close].actual`作为实际关闭时间，不能用计划关闭日期代替。
- 统计时间统一由服务端按用户时区计算，避免临期、逾期口径在Widget和Space中不一致。
- 大数据量场景在PLM端分页和聚合，不把数千条对象传给浏览器后再计算。
