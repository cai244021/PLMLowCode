# Git提交范围

本仓库保存编辑器、PLM增量和Dashboard Widget的开发源码与必要文档。
本地项目由原SVN增量包拆分而来；Git不包含原SVN历史。

## 不上传的本机数据

- 根目录 `数据库信息.txt`，以及 `config/data/logs/uploads/backup`。
- 真实 `.env` 文件、私钥及证书密钥文件。
- `node_modules`、`dist`、`target`、日志、编译缓存及JAR。
- `deliverables` 历史演示、Word/PPT和渲染检查产物。

数据库连接通过环境变量配置，已设计页面的数据保存在PostgreSQL，不在Git中。
请单独制定数据库备份方案；不要将数据库密码、业务数据或完整数据库备份推送到仓库。

## 暂缓提交的旧PLM源码

以下文件存在硬编码凭据或登录示例，待凭据清理后再提交，本地文件未修改：

- `plm/spinner/schema_custom/Business/SourceFiles/JF_SendEmailUtils_mxJPO.java`
- `plm/spinner/schema_custom/Business/SourceFiles/JF_WebServiceHandler_mxJPO.java`
- `plm/spinner/schema_custom/Business/SourceFiles/JF_VPLMReference_mxJPO.java`

它们是已有平台集成的一部分，不能把当前Git快照当作独立可部署的完整PLM包。
依赖的旧JPO、平台库和项目JAR需从已授权的PLM环境或内部制品库提供。
不应为了补齐部署依赖而直接提交上述原始凭据文件。

Widget的环境变量使用 `.env.example` 作为参考，真实环境配置由部署人员在本机维护。
Spinner文本`.xls`必须保留原字节、Tab列及CRLF，不由Git自动转换换行。

涉及企业内部源码，建议仓库保持Private，并仅授予必要人员访问权限。
