<%@include file="./emxNavigatorInclude.inc"%>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Pragma", "no-cache");
    String objectId = emxGetParameter(request, "objectId");
    String positionIds = emxGetParameter(request, "positionIds");
    if (positionIds == null || positionIds.length() == 0) {
        positionIds = emxGetParameter(request, "positionId");
    }
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
        var positionIds = "<%=XSSUtil.encodeForJavaScript(context, positionIds)%>";

        window.addEventListener("message", function (event) {
            if (event.data && event.data.type === "JF_PRODUCT_CONFIG_PARTS_CHANGED" && window.parent) {
                window.parent.postMessage(event.data, "*");
            }
        });

        var submitUrl = "./JF_ProductConfigAddPartSubmit.jsp?objectId=" + encodeURIComponent(objectId) + "&positionIds=" + encodeURIComponent(positionIds);
        var searchUrl = "../common/emxFullSearch.jsp?field=TYPES=type_VPMReference:bo.PLMReference.V_isLastVersion=true&table=AEFGeneralSearchResults&showInitialResults=true&selection=multiple&submitAction=refreshCaller&submitURL=" + encodeURIComponent(submitUrl);
        showModalDialog(searchUrl, 850, 630, true, "Large");
    }());
</script>
</body>
</html>
