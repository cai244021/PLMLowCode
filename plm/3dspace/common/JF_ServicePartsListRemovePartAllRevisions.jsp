<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<%@include file="../emxUICommonAppInclude.inc"%>
<%@include file="../common/enoviaCSRFTokenValidation.inc"%>
<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%
    String alertMess = DomainConstants.EMPTY_STRING;
    String suiteKey = "emxComponentsStringResource";
    try {
        String objectId = emxGetParameter(request, "objectId");
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        StringList partIds = new StringList();
        if (tableRowIdList != null) {
            for (String tableRowId : tableRowIdList) {
                StringList tableIds = FrameworkUtil.split(tableRowId, "|");
                String partId = DomainConstants.EMPTY_STRING;
                if (tableIds.size() > 1) {
                    partId = (String) tableIds.get(1);
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
        JPO.invoke(context, "JF_ProductConfig", new String[0], "removeServicePartsListAllRevisionParts", JPO.packArgs(requestMap));
        alertMess = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.Remove.Successful");
    } catch (Exception e) {
        e.printStackTrace();
        alertMess = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.action.error");
    }
%>
<html>
<script>
    alert("<%=alertMess%>");
    parent.getTopWindow().findFrame(parent.getTopWindow(), "detailsDisplay").location.href = parent.getTopWindow().findFrame(parent.getTopWindow(), "detailsDisplay").location.href.replace("persist=true", "persist=false");
</script>
</html>
