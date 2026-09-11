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
        var completed = false;
        var closeMonitor = null;
        var searchWindow = null;

        function publishToCaller(data) {
            if (caller && !caller.closed && caller.postMessage) {
                caller.postMessage(data, "*");
            }
            if (window.BroadcastChannel) {
                var resultChannel = new BroadcastChannel("JF_LOWCODE_SEARCH_" + requestId);
                resultChannel.postMessage(data);
                window.setTimeout(function () { resultChannel.close(); }, 100);
            }
        }

        function closeLauncherAsCancelled() {
            if (completed) {
                return;
            }
            completed = true;
            if (closeMonitor) {
                window.clearInterval(closeMonitor);
            }
            window.removeEventListener("message", forwardResult);
            publishToCaller({
                type: "JF_LOWCODE_SEARCH_CLOSED",
                requestId: requestId,
                cancelled: true
            });
            //20260909 update by caipan 为跨窗口消息留出发送时间后再关闭Launcher
            window.setTimeout(function () { window.close(); }, 100);
        }

        function forwardResult(event) {
            var data = event.data || {};
            if (event.origin !== window.location.origin
                    || data.type !== "JF_LOWCODE_SEARCH_SELECTED"
                    || data.requestId !== requestId) {
                return;
            }
            completed = true;
            if (closeMonitor) {
                window.clearInterval(closeMonitor);
            }
            publishToCaller(data);
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
        var configuredSubmitURL = configuredParams.get("submitURL") || "";
        configuredParams.delete("submitURL");
        configuredParams.delete("requestId");
        configuredParams.set("selection", "single");
        configuredParams.set("submitAction", "refreshCaller");
        var bridgeSubmitURL = "../common/JF_LowCodeSearchSubmit.jsp";
        if (configuredSubmitURL) {
            bridgeSubmitURL += "?lowCodeSubmitURL=" + encodeURIComponent(configuredSubmitURL);
        }
        configuredParams.set("submitURL", bridgeSubmitURL);
        configuredParams.set("requestId", requestId);
        //20260909 update by caipan Widget保留Launcher桥接层，以便执行自定义提交JSP后仍能通知Widget
        searchWindow = showModalDialog("../common/emxFullSearch.jsp?" + configuredParams.toString(),
                850, 630, true, "Large");
        closeMonitor = window.setInterval(function () {
            try {
                if (searchWindow && searchWindow.closed) {
                    closeLauncherAsCancelled();
                }
            } catch (ignore) {
                closeLauncherAsCancelled();
            }
        }, 300);
        window.addEventListener("focus", function () {
            window.setTimeout(function () {
                if (!completed && (!searchWindow || searchWindow.closed)) {
                    closeLauncherAsCancelled();
                }
            }, 100);
        });
        window.setTimeout(function () {
            window.blur();
        }, 100);
    }());
</script>
</body>
</html>
