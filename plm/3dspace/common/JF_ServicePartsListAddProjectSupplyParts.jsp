<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<%@include file="../emxUICommonAppInclude.inc"%>
<%@include file="../common/enoviaCSRFTokenValidation.inc"%>
<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%
    String alertMess = DomainConstants.EMPTY_STRING;
    try {
        String objectId = emxGetParameter(request, "objectId");
        Map requestMap = new HashMap();
        requestMap.put("objectId", objectId);
        JPO.invoke(context, "JF_ProductConfig", new String[0], "addProjectSupplyPartsToServicePartsList", JPO.packArgs(requestMap));
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
    parent.getTopWindow().findFrame(parent.getTopWindow(), "detailsDisplay").location.href = parent.getTopWindow().findFrame(parent.getTopWindow(), "detailsDisplay").location.href.replace("persist=true", "persist=false");
</script>
</html>
