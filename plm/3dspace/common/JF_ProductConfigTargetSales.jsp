<%@include file="./emxNavigatorInclude.inc"%>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Pragma", "no-cache");
    String objectId = emxGetParameter(request, "objectId");
    if (objectId == null || objectId.length() == 0) {
        objectId = emxGetParameter(request, "parentOID");
    }
    if (objectId == null) {
        objectId = "";
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>Target Sales</title>
</head>
<body>
<script type="text/javascript">
    var objectId = "<%=XSSUtil.encodeForJavaScript(context, objectId)%>";
    var targetUrl = "JFProductConfigTargetSales/index.html";
    if (objectId) {
        targetUrl += "?objectId=" + encodeURIComponent(objectId);
    }
    window.location.replace(targetUrl);
</script>
</body>
</html>
