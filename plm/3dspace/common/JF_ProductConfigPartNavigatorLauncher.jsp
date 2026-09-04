<%@include file="./emxNavigatorInclude.inc"%>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Pragma", "no-cache");
    String objectId = emxGetParameter(request, "objectId");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <script src="scripts/emxUIConstants.js" type="text/javascript"></script>
    <script src="scripts/emxUICore.js" type="text/javascript"></script>
    <script src="scripts/emxUIModal.js" type="text/javascript"></script>
</head>
<body>
<script type="text/javascript">
    (function () {
        var objectId = "<%=XSSUtil.encodeForJavaScript(context, objectId)%>";
        var treeUrl = "../common/emxTree.jsp?objectId=" + encodeURIComponent(objectId);
        showModalDialog(treeUrl, 1000, 700, true, "Large");
        window.setTimeout(function () {
            window.close();
        }, 300);
    }());
</script>
</body>
</html>
