<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ include file="../programcentral/emxProgramGlobals2.inc" %>
<%@ include file="../emxUICommonAppInclude.inc" %>
<%@ include file="../common/enoviaCSRFTokenValidation.inc" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%!
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_DARelatedChange.jsp");
%>
<%
    String mode = emxGetParameter(request, "mode");
    String daId = emxGetParameter(request, "daId");
    if (daId == null || daId.length() == 0) {
        daId = emxGetParameter(request, "objectId");
    }
    String changeType = emxGetParameter(request, "changeType");
    String[] tableRowIds = request.getParameterValues("emxTableRowId");
    String code = "404";
    String message = DomainConstants.EMPTY_STRING;
    try {
        String[] objectIds = ComponentsUIUtil.getSplitTableRowIds(tableRowIds);
        StringList changeIds = new StringList();
        if (objectIds != null) {
            for (String objectId : objectIds) {
                if (objectId != null && objectId.length() > 0) {
                    changeIds.add(objectId);
                }
            }
        }
        Map params = new HashMap();
        params.put("daId", daId);
        params.put("mode", mode);
        params.put("changeType", changeType);
        params.put("changeIds", changeIds);
        Map result = (Map) JPO.invoke(context, "JF_DeviationApplicationSource", null,
                "updateDARelatedChanges", JPO.packArgs(params), Map.class);
        code = (String) result.get("code");
        message = (String) result.get("mess");
    } catch (Exception e) {
        message = e.getMessage();
        LOGGER.error("JF_DARelatedChange error", e);
    }
    String escapedMessage = XSSUtil.encodeForJavaScript(context, message == null ? "" : message);
%>
<html>
<script>
    var code = "<%=code%>";
    var message = "<%=escapedMessage%>";
    var isAdd = "add" === "<%=mode%>";
    if (code !== "200") {
        alert(message);
        if (isAdd) {
            getTopWindow().closeWindow();
        }
    } else if (isAdd) {
        var openerWindow = getTopWindow().getWindowOpener();
        if (openerWindow && openerWindow.refreshTablePage) {
            openerWindow.refreshTablePage();
        } else if (openerWindow) {
            openerWindow.location.reload();
        }
        getTopWindow().closeWindow();
    } else if (getTopWindow().refreshTablePage) {
        getTopWindow().refreshTablePage();
    } else {
        window.parent.location.reload();
    }
</script>
</html>
