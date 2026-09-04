<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_ESOTaskBatchComplete.jsp");
%>
<%
    String alertMess = DomainConstants.EMPTY_STRING;
    String resultCode = "1";
    try {
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        String[] splitTableRowIds = new String[0];
        if (strSelectIds != null && strSelectIds.length > 0) {
            splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(strSelectIds);
        }
        String objectId = emxGetParameter(request, "objectId");
        String parentOID = emxGetParameter(request, "parentOID");

        Map requestMap = new HashMap();
        requestMap.put("objectId", objectId);
        requestMap.put("parentOID", parentOID);
        requestMap.put("list", splitTableRowIds);
        Map resultMap = (Map) JPO.invoke(context, "JF_ESO", null, "completeESOSubTaskByBatch", JPO.packArgs(requestMap), Map.class);
        alertMess = (String) resultMap.get("message");
        resultCode = (String) resultMap.get("code");
    } catch (Exception e) {
        JF_LOGGER.error("JF_ESOTaskBatchComplete.jsp error", e);
        alertMess = e.getMessage();
    }
    if (alertMess == null) {
        alertMess = DomainConstants.EMPTY_STRING;
    }
    if (resultCode == null) {
        resultCode = "1";
    }
    alertMess = alertMess.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "\\n");
%>
<html>
<script>
    var resultCode = "<%=resultCode%>";
    var alertMess = "<%=alertMess%>";
    if (alertMess) {
        alert(alertMess);
    }
    if ("0" === resultCode) {
        try {
            //20260814 update by ljr 批量完成可能联动父任务状态，成功后刷新完整结构以同步父子任务成熟度。
            if (parent && parent.emxEditableTable && parent.emxEditableTable.refreshStructureWithOutSort) {
                parent.emxEditableTable.refreshStructureWithOutSort();
            } else if (parent && parent.emxEditableTable && parent.emxEditableTable.refreshSelectedRows) {
                parent.emxEditableTable.refreshSelectedRows();
            }
        } catch (e) {
        }
    }
</script>
</html>
