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

    String[] selectedRows = request.getParameterValues("emxTableRowId");
    String[] selectedObjectIds = selectedRows == null ? new String[0] : ComponentsUIUtil.getSplitTableRowIds(selectedRows);
    String selectedObjectId = selectedObjectIds != null && selectedObjectIds.length > 0 ? selectedObjectIds[0] : "";
    HashMap params = new HashMap();
    //20260828 update by caipan 支持从产品配置表或售后件清单入口发起审批
    params.put("selectedObjectId", selectedObjectId);
    Map result;
    try {
        result = (Map) JPO.invoke(context, "JF_ProductConfig", null, "checkProductConfigApprovalDuplicate",
                JPO.packArgs(params), Map.class);
    } catch (Exception e) {
        result = new HashMap();
        result.put("code", "404");
        result.put("mess", e.getMessage());
    }
    String code = result.get("code") == null ? "" : String.valueOf(result.get("code"));
    String message = result.get("mess") == null ? "" : String.valueOf(result.get("mess"));

    if ("200".equals(code)) {
        StringBuilder createUrl = new StringBuilder("emxCreate.jsp?type=type_JFConfigTableRoute&policy=policy_JFConfigTableRoute&form=JFConfigTableRouteCreateForm&header=emxFramework.Command.JFSubmitProductConfigApprovalCmd&autoNameChecked=true&nameField=autoName&submitAction=treePopup&postProcessJPO=JF_ProductConfig:connectProductConfigApprovalAfterCreate&preProcessJavaScript=JFProductConfigApprovalInit");
        if (selectedRows != null) {
            for (String selectedRow : selectedRows) {
                createUrl.append("&emxTableRowId=").append(XSSUtil.encodeForURL(context, selectedRow));
            }
        }
        String objectId = request.getParameter("objectId");
        String parentOID = request.getParameter("parentOID");
        if (objectId != null && !objectId.isEmpty()) {
            createUrl.append("&objectId=").append(XSSUtil.encodeForURL(context, objectId));
        }
        if (parentOID != null && !parentOID.isEmpty()) {
            createUrl.append("&parentOID=").append(XSSUtil.encodeForURL(context, parentOID));
        }
        response.sendRedirect(createUrl.toString());
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <script src="scripts/emxUICore.js" type="text/javascript"></script>
    <script src="scripts/emxUIModal.js" type="text/javascript"></script>
</head>
<body>
<script type="text/javascript">
    alert("<%=XSSUtil.encodeForJavaScript(context, message)%>");
    getTopWindow().closeWindow();
</script>
</body>
</html>
