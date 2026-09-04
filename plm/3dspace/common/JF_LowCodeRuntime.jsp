<%@ page pageEncoding="UTF-8" %>
<%@include file="./emxNavigatorInclude.inc"%>
<%!
    private static String lowCodeJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("<", "\\u003c")
                .replace(">", "\\u003e");
    }
%>
<%
    String pageCode = request.getParameter("pageCode");
    if (pageCode == null || !pageCode.matches("[A-Z0-9_]{1,100}")) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
    }
    response.setContentType("text/html; charset=UTF-8");
    response.setHeader("Cache-Control", "no-store");
    String objectId = request.getParameter("objectId");
    String parentOID = request.getParameter("parentOID");
    String relId = request.getParameter("relId");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>PLM Low-Code Runtime</title>
    <link rel="stylesheet" href="JFLowCode/amis/sdk.css">
    <link rel="stylesheet" href="JFLowCode/amis/helper.css">
    <link rel="stylesheet" href="JFLowCode/amis/iconfont.css">
    <link rel="stylesheet" href="JFLowCode/runtime.css">
</head>
<body>
<div id="jf-lowcode-root">页面加载中...</div>
<form id="jf-lowcode-csrf" style="display:none">
    <%@include file="./enoviaCSRFTokenInjection.inc"%>
</form>
<script src="JFLowCode/amis/sdk.js"></script>
<script src="JFLowCode/runtime.js"></script>
<script>
(function () {
    'use strict';
    var pageCode = "<%=lowCodeJs(pageCode)%>";
    var context = {
        objectId: "<%=lowCodeJs(objectId)%>",
        parentOID: "<%=lowCodeJs(parentOID)%>",
        relId: "<%=lowCodeJs(relId)%>"
    };

    function showError(error) {
        var root = document.getElementById('jf-lowcode-root');
        var message = document.createElement('div');
        message.className = 'jf-lowcode-error';
        message.textContent = String(error && error.message ? error.message : error);
        root.innerHTML = '';
        root.appendChild(message);
    }

    function executeAction(actionCode, data) {
        var payload = Object.assign({}, data || {}, { plmContext: context });
        var actionUrl = 'JF_LowCodeAction.jsp?actionCode=' + encodeURIComponent(actionCode);
        var tokenInputs = document.querySelectorAll('#jf-lowcode-csrf input[name]');
        Array.prototype.forEach.call(tokenInputs, function (input) {
            actionUrl += '&' + encodeURIComponent(input.name) + '=' + encodeURIComponent(input.value || '');
        });
        return fetch(actionUrl, {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest' },
            body: JSON.stringify(payload)
        }).then(function (response) {
            return response.text();
        }).then(function (text) {
            return JFLowCodeRuntime.parseJson(text);
        });
    }

    fetch('JFLowCode/pages/' + encodeURIComponent(pageCode) + '.json?_=' + Date.now(), {
        credentials: 'same-origin',
        cache: 'no-store'
    }).then(function (response) {
        if (!response.ok) {
            throw new Error('页面配置加载失败: HTTP ' + response.status);
        }
        return response.json();
    }).then(function (pagePackage) {
        document.getElementById('jf-lowcode-root').innerHTML = '';
        JFLowCodeRuntime.embed({
            container: '#jf-lowcode-root',
            pagePackage: pagePackage,
            adapter: {
                context: context,
                executeAction: executeAction,
                close: function () {
                    if (window.parent && window.parent !== window && window.parent.history.length > 1) {
                        window.parent.history.back();
                    } else {
                        window.history.back();
                    }
                },
                refresh: function () { window.location.reload(); },
                openDetail: function (createdObjectId) {
                    if (!createdObjectId) {
                        throw new Error('创建结果缺少objectId');
                    }
                    window.location.href = 'emxTree.jsp?objectId=' + encodeURIComponent(createdObjectId);
                },
                navigate: function (target) { window.location.href = target; }
            }
        });
    }).catch(showError);
}());
</script>
</body>
</html>
