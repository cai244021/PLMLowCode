<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    String alertMess = DomainConstants.EMPTY_STRING;
    try {
        String objectId = emxGetParameter(request, "objectId");
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        StringList partIds = new StringList();
        if (tableRowIdList != null) {
            for (String tableRowId : tableRowIdList) {
                StringList tableIds = FrameworkUtil.split(tableRowId, "|");
                System.out.println("tableIds:"+tableIds.size());
                String partId = DomainConstants.EMPTY_STRING;
                if (tableIds.size() > 1) {
                    partId = (String) tableIds.get(0);
                } else if (tableIds.size() > 0) {
                    partId = (String) tableIds.get(0);
                }
                if (UIUtil.isNotNullAndNotEmpty(partId) && !partIds.contains(partId)) {
                    partIds.add(partId.trim());
                }
            }
        }
        Map requestMap = new HashMap();
        requestMap.put("objectId", objectId);
        requestMap.put("partIds", partIds);
        JPO.invoke(context, "JF_ProductConfig", new String[0], "addServicePartsListAllRevisionParts", JPO.packArgs(requestMap));
    } catch (Exception e) {
        e.printStackTrace();
        alertMess = e.getMessage();
    }
%>
<html>
<script>
    var alertMess = "<%=alertMess%>";
    if (alertMess != "") {
        alert(alertMess);
    }
    getTopWindow().closeWindow();
    var refreshURL = getTopWindow().getWindowOpener().location.href;
    getTopWindow().getWindowOpener().location.href = refreshURL;
</script>
</html>
