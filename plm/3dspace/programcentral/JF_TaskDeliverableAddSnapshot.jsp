<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="matrix.db.JPO" %>
<%@include file="../common/emxNavigatorInclude.inc"%>
<%@include file="../common/enoviaCSRFTokenValidation.inc"%>
<%
    String taskId = emxGetParameter(request, "objectId");
    String[] selectedRows = request.getParameterValues("emxTableRowId");
    String[] snapshotIds = selectedRows == null ? new String[0]
            : ComponentsUIUtil.getSplitTableRowIds(selectedRows);
    HashMap params = new HashMap();
    params.put("taskId", taskId);
    params.put("snapshotIds", snapshotIds);
    String code = "500";
    String message = "";
    try {
        Map result = (Map) JPO.invoke(context, "JF_Snapshot", null,
                "addTaskDeliverableSnapshots", JPO.packArgs(params), Map.class);
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
