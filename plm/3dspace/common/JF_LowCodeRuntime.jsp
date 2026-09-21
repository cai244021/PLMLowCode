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
    <link rel="stylesheet" href="JFLowCode/runtime.css?v=20260912-2">
</head>
<body>
<div id="jf-lowcode-root">页面加载中...</div>
<form id="jf-lowcode-csrf" style="display:none">
    <%@include file="./enoviaCSRFTokenInjection.inc"%>
</form>
<script src="JFLowCode/amis/sdk.js"></script>
<script src="JFLowCode/runtime.js?v=20260921-2"></script>
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
            + '&actionCode=' + encodeURIComponent(actionCode) + '&protocolVersion=1';
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
            var result = JFLowCodeRuntime.parseJson(text);
            if (!result || result.protocolVersion !== 1) {
                throw new Error('Action协议不兼容：需要版本1，请更新服务端JPO和JSP');
            }
            return result;
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
            //20260921 update by caipan Space统一经过搜索提交桥，先执行自定义submitURL再可靠回填当前表单
            var bridgeSubmitURL = '../common/JF_LowCodeSearchSubmit.jsp';
            if (configuredSubmitURL) {
                bridgeSubmitURL += '?lowCodeSubmitURL=' + encodeURIComponent(configuredSubmitURL);
            }
            configuredParams.set('submitURL', bridgeSubmitURL);
            configuredParams.set('requestId', requestId);
            var searchUrl = '../common/emxFullSearch.jsp?' + configuredParams.toString();
            showModalDialog(searchUrl, 850, 630, true, 'Large');
        });
    }

    function bindTableDrop(target, onDrop) {
        return new Promise(function (resolve, reject) {
            var requireFunctions = [];
            function appendRequire(frame) {
                try {
                    if (frame && typeof frame.require === 'function'
                            && requireFunctions.indexOf(frame.require) < 0) {
                        requireFunctions.push(frame.require);
                    }
                } catch (ignore) {}
            }
            appendRequire(window.parent);
            appendRequire(window.top);
            appendRequire(window);
            if (!requireFunctions.length) {
                reject(new Error('\u5f53\u524dSpace\u9875\u9762\u4e0d\u652f\u6301\u8fbe\u7d22\u62d6\u62fd\u6a21\u5757'));
                return;
            }
            function loadFrom(index) {
                if (index >= requireFunctions.length) {
                    reject(new Error('\u8fbe\u7d22\u62d6\u62fd\u6a21\u5757\u52a0\u8f7d\u5931\u8d25'));
                    return;
                }
                try {
                    requireFunctions[index](['DS/DataDragAndDrop/DataDragAndDrop'], function (DataDragAndDrop) {
                        DataDragAndDrop.droppable(target, {
                            drop: function (data) {
                                target.classList.remove('jf-lowcode-drop-active');
                                onDrop(data);
                            },
                            enter: function () { target.classList.add('jf-lowcode-drop-active'); },
                            over: function () {},
                            leave: function () { target.classList.remove('jf-lowcode-drop-active'); }
                        });
                        resolve(function () { DataDragAndDrop.unbind(target); });
                    }, function () { loadFrom(index + 1); });
                } catch (error) {
                    loadFrom(index + 1);
                }
            }
            loadFrom(0);
        });
    }

    function loadPagePackage() {
        return fetch('JF_LowCodePage.jsp?pageCode=' + encodeURIComponent(pageCode) + '&_=' + Date.now(), {
            credentials: 'same-origin',
            cache: 'no-store'
        }).then(function (response) {
            //20260917 update by caipan 发布页加载失败直接报错，禁止静默加载静态旧配置。
            if (!response.ok) {
                throw new Error('已发布页面加载失败: HTTP ' + response.status);
            }
            return response.text().then(JFLowCodeRuntime.parseJson).then(function (result) {
                if (!result || result.status !== 0) {
                    throw new Error(result && result.msg || '页面加载响应格式不正确，请检查服务端部署版本');
                }
                if (!result.data || !result.data.schema || !result.data.plmConfig) {
                    throw new Error('已发布页面配置包格式不正确');
                }
                return result.data;
            });
        });
    }

    loadPagePackage().then(function (pagePackage) {
        if (JFLowCodeRuntime.protocolVersion !== 1) {
            throw new Error('Runtime协议不兼容：需要版本1，请更新runtime.js');
        }
        document.getElementById('jf-lowcode-root').innerHTML = '';
        return JFLowCodeRuntime.embed({
            container: '#jf-lowcode-root',
            pagePackage: pagePackage,
            adapter: {
                protocolVersion: 1,
                context: context,
                bindTableDrop: bindTableDrop,
                executeAction: executeAction,
                openSearch: openSearch,
                notifyError: function (error) {
                    JFLowCodeRuntime.notify('error', String(error && error.message ? error.message : error));
                },
                notify: function (level, message) { JFLowCodeRuntime.notify(level, message); },
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
