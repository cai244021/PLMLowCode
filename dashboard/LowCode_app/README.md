# LowCode_app

3DEXPERIENCE Dashboard 通用低代码 Widget 运行壳。它不包含具体业务页面，运行时根据 URL 的 `pageCode` 从 PLM Page 对象读取已发布配置，并使用 PLM 服务器上的统一 AMIS Runtime 渲染。

## 构建

```powershell
npm install
npm run build
```

将 `dist` 部署为：

```text
/3dspace/webapps/LowCode_app/dist/
```

Dashboard Widget URL 示例：

```text
https://r2024.v6.com/3dspace/webapps/LowCode_app/dist/index.html?pageCode=JF_DA_LIST_DEMO
```

可选对象上下文参数：`objectId`、`parentOID`、`relId`。新增页面时只需发布新的 PLM Page，并修改 URL 中的 `pageCode`，无需修改或重新构建此 Widget。

公共运行能力集中在 `src/platform.ts` 和 `src/runtime.ts`，包括 3DSpace 地址、Security Context、WAFData认证请求、Page加载、动作调用、原生搜索、Dashboard事件及页面跳转。业务查询和写入不放在 Widget 源码中，由设计器动作库和发布 Page 配置扩展。
