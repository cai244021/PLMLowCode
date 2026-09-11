<%@include file="./emxNavigatorInclude.inc"%>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.StringTokenizer" %>
<%
    response.setHeader("Cache-Control", "no-store");
    String requestId = emxGetParameter(request, "requestId");
    String lowCodeSubmitURL = emxGetParameter(request, "lowCodeSubmitURL");
    String[] selectedRows = emxGetParameterValues(request, "emxTableRowId");
    String selectedId = "";
    String selectedName = "";
    String displayName = "";
    String message = "";

    if (selectedRows == null || selectedRows.length != 1) {
        message = "请选择一个对象";
    } else {
        StringTokenizer tokenizer = new StringTokenizer(selectedRows[0], "|");
        if (tokenizer.hasMoreTokens()) {
            selectedId = tokenizer.nextToken();
        }
        if (UIUtil.isNotNullAndNotEmpty(selectedId)) {
            DomainObject project = DomainObject.newInstance(context, selectedId);
            StringList selects = new StringList();
            selects.add(DomainConstants.SELECT_NAME);
            selects.add(DomainConstants.SELECT_DESCRIPTION);
            selects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
            Map projectInfo = project.getInfo(context, selects);
            selectedName = UIUtil.getValue(projectInfo, DomainConstants.SELECT_NAME);
            displayName = UIUtil.getValue(projectInfo, DomainConstants.SELECT_DESCRIPTION);
            if (UIUtil.isNullOrEmpty(displayName)) {
                displayName = UIUtil.getValue(projectInfo, DomainConstants.SELECT_ATTRIBUTE_TITLE);
            }
            if (UIUtil.isNullOrEmpty(displayName)) {
                displayName = selectedName;
            }
        }
    }
%>
<script src="scripts/emxUICore.js" type="text/javascript"></script>
<script type="text/javascript">
    (function () {
        var result = {
            type: "JF_LOWCODE_SEARCH_SELECTED",
            requestId: "<%=XSSUtil.encodeForJavaScript(context, requestId)%>",
            objectId: "<%=XSSUtil.encodeForJavaScript(context, selectedId)%>",
            name: "<%=XSSUtil.encodeForJavaScript(context, selectedName)%>",
            displayName: "<%=XSSUtil.encodeForJavaScript(context, displayName)%>",
            message: "<%=XSSUtil.encodeForJavaScript(context, message)%>",
            success: <%=UIUtil.isNotNullAndNotEmpty(selectedId)%>
        };
        var configuredSubmitURL = "<%=XSSUtil.encodeForJavaScript(context, lowCodeSubmitURL)%>";
        var submitParameters = new URLSearchParams();
<%
        Map parameterMap = request.getParameterMap();
        for (Object entryObject : parameterMap.entrySet()) {
            Map.Entry parameterEntry = (Map.Entry) entryObject;
            String parameterName = String.valueOf(parameterEntry.getKey());
            if ("lowCodeSubmitURL".equals(parameterName)) {
                continue;
            }
            String[] parameterValues = (String[]) parameterEntry.getValue();
            if (parameterValues == null) {
                continue;
            }
            for (String parameterValue : parameterValues) {
%>
        submitParameters.append(
                "<%=XSSUtil.encodeForJavaScript(context, parameterName)%>",
                "<%=XSSUtil.encodeForJavaScript(context, parameterValue)%>");
<%
            }
        }
%>
        var targetWindow = null;
        try {
            targetWindow = getTopWindow().getWindowOpener
                    ? getTopWindow().getWindowOpener()
                    : window.opener;
        } catch (ignore) {
            targetWindow = window.opener;
        }
        function publishResult() {
            if (targetWindow && targetWindow.postMessage) {
                targetWindow.postMessage(result, window.location.origin);
            }
            if (window.BroadcastChannel) {
                var resultChannel = new BroadcastChannel("JF_LOWCODE_SEARCH_" + result.requestId);
                resultChannel.postMessage(result);
                window.setTimeout(function () {
                    resultChannel.close();
                    closeSearchWindow();
                }, 100);
            } else {
                closeSearchWindow();
            }
        }

        function invokeConfiguredSubmit() {
            if (!configuredSubmitURL || !result.success) {
                return Promise.resolve();
            }
            var submitURL = new URL(configuredSubmitURL, window.location.href);
            var contextRoot = "<%=XSSUtil.encodeForJavaScript(context, request.getContextPath())%>/";
            if (submitURL.origin !== window.location.origin
                    || submitURL.pathname.indexOf(contextRoot) !== 0
                    || submitURL.pathname === window.location.pathname
                    || !/\.jsp$/i.test(submitURL.pathname)) {
                return Promise.reject(new Error("submitURL必须是当前3DSpace内的JSP地址"));
            }
            return fetch(submitURL.toString(), {
                method: "POST",
                credentials: "same-origin",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                    "X-Requested-With": "XMLHttpRequest"
                },
                body: submitParameters.toString()
            }).then(function (response) {
                return response.text().then(function (text) {
                    if (!response.ok) {
                        throw new Error("HTTP " + response.status);
                    }
                    if (/^\s*\{/.test(text)) {
                        var responseData = JSON.parse(text);
                        if (responseData.success === false
                                || (responseData.status != null
                                && Number(responseData.status) !== 0
                                && Number(responseData.status) !== 200)) {
                            throw new Error(responseData.msg || responseData.message || "业务提交失败");
                        }
                    }
                });
            });
        }

        //20260909 update by caipan Widget自定义submitURL执行完成后再回传选择结果并关闭搜索窗口
        invokeConfiguredSubmit().then(publishResult).catch(function (error) {
            result.success = false;
            result.objectId = "";
            result.message = "提交处理失败：" + (error && error.message ? error.message : String(error));
            publishResult();
        });

        function closeSearchWindow() {
            var searchWindow = window.top;
            try {
                if (searchWindow && typeof searchWindow.closeWindow === "function") {
                    searchWindow.closeWindow();
                }
            } catch (ignore) {
                // SearchUI.html弹窗不提供平台closeWindow时，继续使用浏览器窗口关闭。
            }
            try {
                if (searchWindow && !searchWindow.closed) {
                    searchWindow.close();
                }
            } catch (ignore) {
                window.close();
            }
        }
    }());
</script>
