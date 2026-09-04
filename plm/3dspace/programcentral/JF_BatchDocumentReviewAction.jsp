<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@include file="../common/emxNavigatorInclude.inc"%>
<%@include file="../common/enoviaCSRFTokenValidation.inc"%>
<%
    //20260812 update by ljr 统一解析列表行：提交/撤回使用对象ID，移除文档或人员使用关系ID。
    String mode = emxGetParameter(request, "mode");
    String objectId = emxGetParameter(request, "objectId");
    String[] selectedRows = request.getParameterValues("emxTableRowId");
    StringList relationshipIds = new StringList();
    if (selectedRows != null) {
        for (String selectedRow : selectedRows) {
            Map rowMap = ProgramCentralUtil.parseTableRowId(context, selectedRow);
            String relationshipId = rowMap.get("relId") == null ? "" : String.valueOf(rowMap.get("relId"));
            String selectedObjectId = rowMap.get("objectId") == null ? "" : String.valueOf(rowMap.get("objectId"));
            if ("withdraw".equals(mode) && (objectId == null || objectId.length() == 0)) {
                objectId = selectedObjectId;
            }
            if (relationshipId.length() > 0) {
                relationshipIds.add(relationshipId);
            }
        }
    }
    String code = "500";
    String message = "";
    boolean transactionStarted = false;
    try {
        ContextUtil.startTransaction(context, true);
        transactionStarted = true;
        DomainRelationship.disconnect(context, relationshipIds.toStringArray());
        ContextUtil.commitTransaction(context);
        transactionStarted = false;
        code = "200";
    } catch (Exception e) {
        if (transactionStarted) {
            ContextUtil.abortTransaction(context);
        }
        message = e.getMessage() == null ? e.toString() : e.getMessage();
    }
%>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8" /><script src="../common/scripts/emxUICore.js" type="text/javascript"></script></head>
<body>
<script type="text/javascript">
<% if ("200".equals(code)) { %>
<% if ("removeSignPerson".equals(mode)) { %>
    // 会签人员表嵌套在详情Form中，移除后优先刷新当前嵌入Table，避免整页跳转。
    if (window.parent && window.parent.refreshTablePage) {
        window.parent.refreshTablePage();
    } else if (getTopWindow().refreshTablePage) {
        getTopWindow().refreshTablePage();
    } else {
        getTopWindow().location.reload();
    }
<% } else { %>
    if (getTopWindow().refreshTablePage) {
        getTopWindow().refreshTablePage();
    } else {
        getTopWindow().location.reload();
    }
<% } %>
<% } else { %>
    alert("<%=XSSUtil.encodeForJavaScript(context, message)%>");
<% } %>
</script>
</body>
</html>
