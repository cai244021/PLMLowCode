<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="matrix.db.JPO" %>
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
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_ProductConfigRevise.jsp");

    private String escapeJavaScript(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "\\n");
    }
%>
<%
    String errorMessage = "";
    String successMessage = "";
    String failedMessage = EnoviaResourceBundle.getProperty(context,
            "emxFrameworkStringResource",
            context.getLocale(),
            "emxFramework.JFProductConfigRevision.Failed");
    try {
        String[] tableRowIds = request.getParameterValues("emxTableRowId");
        String[] objectIds = ComponentsUIUtil.getSplitTableRowIds(tableRowIds);
        if (objectIds == null || objectIds.length != 1 || UIUtil.isNullOrEmpty(objectIds[0])) {
            throw new Exception(EnoviaResourceBundle.getProperty(context,
                    "emxFrameworkStringResource",
                    context.getLocale(),
                    "emxFramework.JFProductConfigRevision.SelectOne"));
        }
        Map params = new HashMap();
        params.put("objectId", objectIds[0]);
        Map result = (Map) JPO.invoke(context,
                "JF_ProductConfig",
                null,
                "reviseProductConfigTable",
                JPO.packArgs(params),
                Map.class);
        if (result == null || !"200".equals(String.valueOf(result.get("code")))) {
            String resultMessage = result == null ? "" : String.valueOf(result.get("mess"));
            throw new Exception(UIUtil.isNullOrEmpty(resultMessage) || "null".equals(resultMessage) ? failedMessage : resultMessage);
        }
        successMessage = String.valueOf(result.get("mess"));
    } catch (Exception e) {
        errorMessage = UIUtil.isNullOrEmpty(e.getMessage()) ? failedMessage : e.getMessage();
        LOGGER.error("JF_ProductConfigRevise error", e);
    }
%>
<html>
<script>
    var errorMessage = "<%=escapeJavaScript(errorMessage)%>";
    var successMessage = "<%=escapeJavaScript(successMessage)%>";

    function stopRevisionProgress() {
        var tableFrame = window.parent;
        if (tableFrame && tableFrame.portalMode == "true" && typeof tableFrame.toggleProgress === "function") {
            tableFrame.toggleProgress("hidden");
        } else if (tableFrame && typeof tableFrame.turnOffProgress === "function") {
            tableFrame.turnOffProgress();
        }
    }

    function refreshCurrentTable() {
        var tableFrame = window.parent;
        if (tableFrame && typeof tableFrame.refreshSBTable === "function") {
            tableFrame.refreshSBTable(tableFrame.configuredTableName || "");
        } else {
            tableFrame.location.reload();
        }
        var topWindow = typeof tableFrame.getTopWindow === "function" ? tableFrame.getTopWindow() : null;
        if (topWindow && typeof topWindow.RefreshHeader === "function") {
            topWindow.RefreshHeader();
        }
    }

    stopRevisionProgress();
    if (errorMessage !== "") {
        alert(errorMessage);
    } else {
        if (successMessage !== "") {
            alert(successMessage);
        }
        refreshCurrentTable();
    }
</script>
</html>
