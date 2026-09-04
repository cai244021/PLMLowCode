<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="matrix.db.JPO" %>
<%@include file="../common/emxNavigatorInclude.inc"%>
<%@include file="../common/enoviaCSRFTokenValidation.inc"%>
<%
    //搜索结果提交后再次执行完整规则校验，避免仅依赖搜索条件造成非法关联。
    String batchReviewId = emxGetParameter(request, "objectId");
    String[] selectedRows = request.getParameterValues("emxTableRowId");
    String[] documentIds = ComponentsUIUtil.getSplitTableRowIds(selectedRows);
    HashMap params = new HashMap();
    params.put("batchReviewId", batchReviewId);
    params.put("docIds", documentIds);
    String code = "500";
    String message = "";
    try {
        Map result = (Map) JPO.invoke(context, "JF_BatchDocumentReview", null,
                "addBatchDocumentReviewDocuments", JPO.packArgs(params), Map.class);
        code = String.valueOf(result.get("code"));
    } catch (Exception e) {
        message = e.getMessage() == null ? e.toString() : e.getMessage();
    }
%>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8" /><script src="../common/scripts/emxUICore.js" type="text/javascript"></script></head>
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
