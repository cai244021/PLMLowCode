<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@include file="../common/emxNavigatorInclude.inc"%>
<%
    String versionFilter = emxGetParameter(request, "JFDocumentVersionFilter");
    if (!"latestReleased".equals(versionFilter)
            && !"latest".equals(versionFilter)
            && !"all".equals(versionFilter)) {
        versionFilter = "latestReleased";
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <script src="../common/scripts/emxUIConstants.js" type="text/javascript"></script>
    <script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
</head>
<body>
<script type="text/javascript">
    //20260827 update by caipan 仅刷新项目文件夹结构表，保留项目详情中的后续页签。
    (function () {
        //20260828 update by caipan 项目文件夹Structure Browser可能嵌套在动态命名Frame中，按配置表名递归定位实际内容Frame。
        function findFolderTableFrame(currentWindow) {
            if (!currentWindow) {
                return null;
            }
            try {
                var currentHref = currentWindow.location && currentWindow.location.href
                        ? currentWindow.location.href : "";
                if (currentWindow.configuredTableName === "PMCFolderSummary"
                        || /[?&]table=PMCFolderSummary(?:&|$)/.test(currentHref)) {
                    return currentWindow;
                }
                for (var i = 0; i < currentWindow.frames.length; i++) {
                    var matchedFrame = findFolderTableFrame(currentWindow.frames[i]);
                    if (matchedFrame) {
                        return matchedFrame;
                    }
                }
            } catch (ignore) {
                // 跨域或尚未完成加载的Frame不可读时继续查找其他Frame。
            }
            return null;
        }

        var topWindow = getTopWindow();
        var folderFrame = findFolderTableFrame(topWindow);
        if (!folderFrame) {
            folderFrame = findFrame(topWindow, "PMCFolder");
        }
        if (!folderFrame) {
            folderFrame = findFrame(topWindow, "detailsDisplay");
        }
        if (!folderFrame || folderFrame.location.href === "about:blank") {
            return;
        }

        var currentHref = folderFrame.location.href;
        var hashIndex = currentHref.indexOf("#");
        var hash = hashIndex >= 0 ? currentHref.substring(hashIndex) : "";
        var hrefWithoutHash = hashIndex >= 0 ? currentHref.substring(0, hashIndex) : currentHref;
        var queryIndex = hrefWithoutHash.indexOf("?");
        var baseUrl = queryIndex >= 0 ? hrefWithoutHash.substring(0, queryIndex) : hrefWithoutHash;
        var query = queryIndex >= 0 ? hrefWithoutHash.substring(queryIndex + 1) : "";
        var queryItems = query ? query.split("&") : [];
        var retainedItems = [];

        for (var i = 0; i < queryItems.length; i++) {
            var parameterName = queryItems[i].split("=")[0];
            if (parameterName !== "JFDocumentVersionFilter"
                    && parameterName !== "expandLevel"
                    && parameterName !== "previousExpandLevel"
                    && parameterName !== "expandByDefault") {
                retainedItems.push(queryItems[i]);
            }
        }
        retainedItems.push("JFDocumentVersionFilter=<%=XSSUtil.encodeForJavaScript(context, versionFilter)%>");
        //20260903 update by liujr 切换文档版本视图后自动展开全部项目文件夹。
        retainedItems.push("expandLevel=All");
        retainedItems.push("previousExpandLevel=All");
        retainedItems.push("expandByDefault=true");
        folderFrame.location.href = baseUrl + "?" + retainedItems.join("&") + hash;
    }());
</script>
</body>
</html>
