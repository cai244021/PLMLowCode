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
    <link rel="stylesheet" href="JFLowCode/runtime.css?v=20260908-2">
</head>
<body>
<div id="jf-lowcode-root">页面加载中...</div>
<form id="jf-lowcode-csrf" style="display:none">
    <%@include file="./enoviaCSRFTokenInjection.inc"%>
</form>
<script src="JFLowCode/amis/sdk.js"></script>
<script src="JFLowCode/runtime.js?v=20260910-5"></script>
<script src="scripts/emxUIConstants.js"></script>
<script src="scripts/emxUICore.js"></script>
<script src="scripts/emxUIModal.js"></script>
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
        var actionUrl = 'JF_LowCodeAction.jsp?pageCode=' + encodeURIComponent(pageCode)
            + '&actionCode=' + encodeURIComponent(actionCode);
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

    function prepareSpaceSearchParams(searchParams) {
        var params = new URLSearchParams(String(searchParams || '').replace(/^\?/, ''));

        function wrapProgram(parameterName, delegateParameterName, wrapperMethod) {
            var configuredProgram = params.get(parameterName);
            var wrapperProgram = 'JF_LowCode:' + wrapperMethod;
            if (!configuredProgram || configuredProgram === wrapperProgram) {
                return;
            }
            if (!/^[A-Za-z0-9_$.-]{1,150}:[A-Za-z0-9_$.-]{1,150}$/.test(configuredProgram)) {
                throw new Error(parameterName + '\u683c\u5f0f\u4e0d\u6b63\u786e\uff0c\u5e94\u4e3aJPO\u540d:\u65b9\u6cd5\u540d');
            }
            params.set(delegateParameterName, configuredProgram);
            params.set(parameterName, wrapperProgram);
        }

        wrapProgram('includeOIDprogram', 'lowCodeIncludeOIDprogram', 'filterIncludeSearchOIDsLowCode');
        wrapProgram('excludeOIDprogram', 'lowCodeExcludeOIDprogram', 'filterExcludeSearchOIDsLowCode');
        params.set('lowCodeSearchClient', 'space');
        return params.toString();
    }

    function openSearch(searchTarget) {
        return new Promise(function (resolve, reject) {
            if (!searchTarget || !searchTarget.searchParams) {
                reject(new Error('\u672a\u914d\u7f6eemxFullSearch\u53c2\u6570'));
                return;
            }
            var requestId = 'jf-lowcode-' + Date.now() + '-' + Math.random().toString(16).substring(2);
            var timeoutId;
            var onMessage = function (event) {
                var data = event.data || {};
                if (event.origin !== window.location.origin
                        || data.type !== 'JF_LOWCODE_SEARCH_SELECTED'
                        || data.requestId !== requestId) {
                    return;
                }
                window.removeEventListener('message', onMessage);
                window.clearTimeout(timeoutId);
                if (!data.objectId) {
                    reject(new Error(data.message || '\u672a\u9009\u62e9\u9879\u76ee'));
                    return;
                }
                resolve(data);
            };
            window.addEventListener('message', onMessage);
            timeoutId = window.setTimeout(function () {
                window.removeEventListener('message', onMessage);
                reject(new Error('\u9879\u76ee\u641c\u7d22\u5df2\u8d85\u65f6'));
            }, 300000);

            //20260909 update by caipan Space Runtime使用标准达索搜索窗口，并统一包装候选ID程序
            var preparedSearchParams = prepareSpaceSearchParams(searchTarget.searchParams);
            var configuredParams = new URLSearchParams(preparedSearchParams);
            var configuredSubmitURL = configuredParams.get('submitURL');
            configuredParams.delete('requestId');
            configuredParams.set('selection', 'single');
            configuredParams.set('submitAction', 'refreshCaller');
            //20260909 update by caipan Space保留页面配置的原生提交JSP，未配置时使用统一回填页
            configuredParams.set('submitURL', configuredSubmitURL || '../common/JF_LowCodeSearchSubmit.jsp');
            configuredParams.set('requestId', requestId);
            var searchUrl = '../common/emxFullSearch.jsp?' + configuredParams.toString();
            showModalDialog(searchUrl, 850, 630, true, 'Large');
            if (configuredSubmitURL) {
                window.removeEventListener('message', onMessage);
                window.clearTimeout(timeoutId);
                resolve({ cancelled: true });
            }
        });
    }

    function loadStaticPackage() {
        return fetch('JFLowCode/pages/' + encodeURIComponent(pageCode) + '.json?_=' + Date.now(), {
            credentials: 'same-origin',
            cache: 'no-store'
        }).then(function (response) {
            if (!response.ok) {
                throw new Error('页面配置加载失败: HTTP ' + response.status);
            }
            return response.json();
        });
    }

    function loadPagePackage() {
        return fetch('JF_LowCodePage.jsp?pageCode=' + encodeURIComponent(pageCode) + '&_=' + Date.now(), {
            credentials: 'same-origin',
            cache: 'no-store'
        }).then(function (response) {
            if (response.status === 404) {
                return loadStaticPackage();
            }
            if (!response.ok) {
                throw new Error('已发布页面加载失败: HTTP ' + response.status);
            }
            return response.text().then(JFLowCodeRuntime.parseJson);
        });
    }

    loadPagePackage().then(function (pagePackage) {
        document.getElementById('jf-lowcode-root').innerHTML = '';
        return JFLowCodeRuntime.embed({
            container: '#jf-lowcode-root',
            pagePackage: pagePackage,
            adapter: {
                context: context,
                executeAction: executeAction,
                openSearch: openSearch,
                notifyError: function (error) {
                    alert(String(error && error.message ? error.message : error));
                },
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
