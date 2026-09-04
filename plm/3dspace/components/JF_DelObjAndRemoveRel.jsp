<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    try {
        String objectId = emxGetParameter(request, "objectId");
        String mode = emxGetParameter(request, "mode");
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        ContextUtil.pushContext(context);
        if ("remove".equalsIgnoreCase(mode)) {
            for(String tableRowId : strSelectIds) {
                StringList tableIds = FrameworkUtil.split(tableRowId,"|");
                DomainRelationship.disconnect(context,tableIds.get(0));
            }
        }

    }catch (Exception e) {
        e.printStackTrace();
    }finally {
        ContextUtil.popContext(context);
    }

%>
<html>
<script >
    getTopWindow().closeWindow();
    window.top.getWindowOpener().top.refreshTablePage();
</script>
</html>
