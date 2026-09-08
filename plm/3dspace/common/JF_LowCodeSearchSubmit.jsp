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
    String[] selectedRows = emxGetParameterValues(request, "emxTableRowId");
    String selectedId = "";
    String selectedName = "";
    String displayName = "";
    String message = "";

    if (selectedRows == null || selectedRows.length != 1) {
        message = "请选择一个项目";
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
            selects.add("type.kindof[" + DomainConstants.TYPE_PROJECT_SPACE + "]");
            Map projectInfo = project.getInfo(context, selects);
            if (!"true".equalsIgnoreCase(String.valueOf(projectInfo.get(
                    "type.kindof[" + DomainConstants.TYPE_PROJECT_SPACE + "]")))) {
                selectedId = "";
                message = "选择的对象不是项目";
            } else {
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
            message: "<%=XSSUtil.encodeForJavaScript(context, message)%>"
        };
        var targetWindow = null;
        try {
            targetWindow = getTopWindow().getWindowOpener
                    ? getTopWindow().getWindowOpener()
                    : window.opener;
        } catch (ignore) {
            targetWindow = window.opener;
        }
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
