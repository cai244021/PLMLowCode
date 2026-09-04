<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
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
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_ProductConfigDelete.jsp");
%>
<%
    String strMess = DomainConstants.EMPTY_STRING;
    String[] tableRowIds = request.getParameterValues("emxTableRowId");
    String expectedType = emxGetParameter(request, "expectedType");
    try {
        String[] objectIds = ComponentsUIUtil.getSplitTableRowIds(tableRowIds);
        if (objectIds == null || objectIds.length == 0) {
            throw new Exception("Please select an item.");
        }
        StringList deleteIds = new StringList();
        for (String objectId : objectIds) {
            if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                deleteIds.add(objectId);
            }
        }
        Map params = new HashMap();
        params.put("objectIds", deleteIds);
        params.put("expectedType", expectedType);
        JPO.invoke(context, "JF_ProductConfig", null, "deleteProjectConfigLists", JPO.packArgs(params), void.class);
    } catch (Exception e) {
        strMess = e.getMessage();
        LOGGER.error("JF_ProductConfigDelete error", e);
    }
    String escapedMess = strMess == null ? "" : strMess.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "\\n");
%>
<html>
<script>
    var strMess = "<%=escapedMess%>";
    if (strMess !== "" && strMess != null) {
        alert(strMess);
    } else {
        window.parent.location.reload();
    }
</script>
</html>
