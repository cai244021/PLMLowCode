<%@include file="./emxNavigatorInclude.inc"%>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%
    response.setHeader("Cache-Control", "no-store");
    String requestId = emxGetParameter(request, "requestId");
    String searchParams = emxGetParameter(request, "searchParams");
    if (requestId == null) {
        requestId = "";
    }
    if (searchParams == null) {
        searchParams = "";
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>PLM Search Launcher</title>
    <script src="scripts/emxUIConstants.js" type="text/javascript"></script>
    <script src="scripts/emxUICore.js" type="text/javascript"></script>
    <script src="scripts/emxUIModal.js" type="text/javascript"></script>
</head>
<body>
<script type="text/javascript">
    (function () {
        "use strict";
        var caller = window.opener || window.parent;
        var requestId = "<%=XSSUtil.encodeForJavaScript(context, requestId)%>";
        var searchParams = "<%=XSSUtil.encodeForJavaScript(context, searchParams)%>";

        function forwardResult(event) {
            var data = event.data || {};
            if (event.origin !== window.location.origin
                    || data.type !== "JF_LOWCODE_SEARCH_SELECTED"
                    || data.requestId !== requestId) {
                return;
            }
            if (caller && !caller.closed && caller.postMessage) {
                caller.postMessage(data, window.location.origin);
            }
            try {
                var searchWindow = event.source && event.source.top;
                if (searchWindow && searchWindow !== window && !searchWindow.closed) {
                    searchWindow.close();
                }
            } catch (ignore) {
                // 提交页会同时执行关闭，Launcher这里只做兼容兜底。
            }
            window.removeEventListener("message", forwardResult);
            window.setTimeout(function () { window.close(); }, 0);
        }

        if (!requestId || !searchParams) {
            document.body.textContent = "低代码搜索参数不完整";
            return;
        }
        window.addEventListener("message", forwardResult);

        var configuredParams = new URLSearchParams(searchParams.replace(/^\?/, ""));
        configuredParams.delete("submitURL");
        configuredParams.delete("requestId");
        configuredParams.set("selection", "single");
        configuredParams.set("submitAction", "refreshCaller");
        configuredParams.set("submitURL", "../common/JF_LowCodeSearchSubmit.jsp");
        configuredParams.set("requestId", requestId);
        showModalDialog("../common/emxFullSearch.jsp?" + configuredParams.toString(),
                850, 630, true, "Large");
        window.setTimeout(function () {
            window.blur();
        }, 100);
    }());
</script>
</body>
</html>
