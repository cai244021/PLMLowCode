<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="matrix.db.JPO" %>
<%@include file="./emxNavigatorInclude.inc"%>
<%
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Pragma", "no-cache");

    //完整接收Structure Browser提交的多选行，并转换为业务对象ID后统一校验。
    String[] selectedRows = request.getParameterValues("emxTableRowId");
    String[] selectedObjectIds = selectedRows == null
            ? new String[0]
            : ComponentsUIUtil.getSplitTableRowIds(selectedRows);
    HashMap params = new HashMap();
    params.put("selectedObjectIds", selectedObjectIds);

    Map result;
    try {
        result = (Map) JPO.invoke(
                context,
                "JF_DocumentLibrary",
                null,
                "validateDocumentReferenceQuerySelection",
                JPO.packArgs(params),
                Map.class);
    } catch (Exception e) {
        result = new HashMap();
        result.put("code", "500");
        result.put("mess", e.getMessage() == null ? e.toString() : e.getMessage());
    }

    String code = result.get("code") == null ? "" : String.valueOf(result.get("code"));
    String message = result.get("mess") == null ? "" : String.valueOf(result.get("mess"));
    String tableUrl = "";
    if ("200".equals(code)) {
        String selectedDocumentIds = String.valueOf(result.get("selectedDocumentIds"));
        //把校验后的文档ID放入结果表URL，保证表格二次加载及导出时仍能取得全部选择。
        tableUrl = new StringBuilder("emxIndentedTable.jsp")
                .append("?program=JF_DocumentLibrary:getDocumentReferenceQueryList")
                .append("&table=JFProjectFolderDocumentReferenceQueryTable")
                .append("&selection=none")
                .append("&objectBased=false")
                .append("&rowGroupingColumnNames=DocumentName")
                .append("&header=emxProgramCentral.Command.JFProjectFolderDocumentReferenceQueryCmd")
                .append("&StringResourceFileId=emxProgramCentralStringResource")
                .append("&Export=true")
                .append("&selectedDocumentIds=")
                .append(XSSUtil.encodeForURL(context, selectedDocumentIds))
                .toString();
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <script src="scripts/emxUIConstants.js" type="text/javascript"></script>
    <script src="scripts/emxUICore.js" type="text/javascript"></script>
    <script src="scripts/emxUIModal.js" type="text/javascript"></script>
</head>
<body>
<script type="text/javascript">
<% if ("200".equals(code)) { %>
    //校验通过后才打开结果窗口，避免校验失败时先出现空白弹窗。
    showModalDialog("<%=XSSUtil.encodeForJavaScript(context, tableUrl)%>", 1100, 650, true, "Large");
<% } else { %>
    alert("<%=XSSUtil.encodeForJavaScript(context, message)%>");
<% } %>
</script>
</body>
</html>
