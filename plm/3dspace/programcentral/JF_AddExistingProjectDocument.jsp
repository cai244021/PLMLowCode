<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="matrix.util.StringList" %>
<%@include file="../common/emxNavigatorInclude.inc"%>
<%@include file="../common/enoviaCSRFTokenValidation.inc"%>
<%
    String usage = emxGetParameter(request, "usage");
    String contextObjectId = emxGetParameter(request, "objectId");
    String[] selectedRows = request.getParameterValues("emxTableRowId");
    String[] documentIds = selectedRows == null
            ? new String[0]
            : ComponentsUIUtil.getSplitTableRowIds(selectedRows);
    HashMap params = new HashMap();
    params.put("contextObjectId", contextObjectId);
    params.put("documentIds", documentIds);

    //按受控用途映射上下文类型、可编辑状态及关系，禁止请求直接指定后台关系。
    if ("ECR".equals(usage)) {
        //20260828 update by liujr ECR添加现有文档不再传递或校验项目关系。
        params.put("documentRelationship", "Reference Document");
        params.put("allowedContextTypes", StringList.create("JFECR"));
        params.put("editableStates", StringList.create("Create", "Submit"));
    }

    String code = "500";
    String message = "";
    try {
        Map result = (Map) JPO.invoke(
                context,
                "JF_DocumentService",
                null,
                "addExistingProjectDocuments",
                JPO.packArgs(params),
                Map.class);
        code = String.valueOf(result.get("code"));
    } catch (Exception e) {
        message = e.getMessage() == null ? e.toString() : e.getMessage();
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
</head>
<body>
<script type="text/javascript">
<% if ("200".equals(code)) { %>
    var openerWindow = getTopWindow().getWindowOpener();
    if (openerWindow && openerWindow.refreshTablePage) {
        openerWindow.refreshTablePage();
    } else if (openerWindow) {
        openerWindow.location.reload();
    }
    getTopWindow().closeWindow();
<% } else { %>
    alert("<%=XSSUtil.encodeForJavaScript(context, message)%>");
<% } %>
</script>
</body>
</html>
