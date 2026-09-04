<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.UUID" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ include file="../programcentral/emxProgramGlobals2.inc" %>
<%@ include file="../emxUICommonAppInclude.inc" %>
<%@ include file="../common/enoviaCSRFTokenValidation.inc" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%!
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_ProductConfigTransferOwner.jsp");
%>
<%
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Pragma", "no-cache");

    String mode = emxGetParameter(request, "mode");
    String transferAction = emxGetParameter(request, "jfTransferAction");
    boolean isSubmit = "submit".equals(transferAction);
    String transferType = emxGetParameter(request, "transferType");
    if (UIUtil.isNullOrEmpty(transferType)) {
        transferType = emxGetParameter(request, "jfTransferType");
    }
    boolean isServicePartsList = "servicePartsList".equals(transferType);
    String refreshToken = emxGetParameter(request, "jfRefreshToken");
    if (!isSubmit && UIUtil.isNullOrEmpty(refreshToken)) {
        refreshToken = UUID.randomUUID().toString();
    }
    String code = "404";
    String message = DomainConstants.EMPTY_STRING;
    String searchUrl = DomainConstants.EMPTY_STRING;
    try {
        StringList selectedObjectIds = new StringList();
        if (isSubmit) {
            String selectedIds = emxGetParameter(request, "jfTransferIds");
            if (UIUtil.isNotNullAndNotEmpty(selectedIds)) {
                for (String selectedId : selectedIds.split(",")) {
                    if (UIUtil.isNotNullAndNotEmpty(selectedId)) {
                        selectedObjectIds.add(selectedId);
                    }
                }
            }
        } else {
            String[] tableRowIds = request.getParameterValues("emxTableRowId");
            String[] objectIds = ComponentsUIUtil.getSplitTableRowIds(tableRowIds);
            if (objectIds != null) {
                for (String objectId : objectIds) {
                    if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                        selectedObjectIds.add(objectId);
                    }
                }
            }
        }

        Map params = new HashMap();
        params.put(isServicePartsList ? "servicePartsListIds" : "productConfigIds", selectedObjectIds);
        Map result;
        if (isSubmit) {
            String[] personRows = request.getParameterValues("emxTableRowId");
            String[] personIds = ComponentsUIUtil.getSplitTableRowIds(personRows);
            params.put("personId", personIds != null && personIds.length > 0 ? personIds[0] : "");
            String transferMethod = isServicePartsList
                    ? "transferServicePartsListOwner" : "transferProductConfigOwner";
            result = (Map) JPO.invoke(context, "JF_ProductConfig", null,
                    transferMethod, JPO.packArgs(params), Map.class);
        } else {
            String checkMethod = isServicePartsList
                    ? "checkServicePartsListTransferOwner" : "checkProductConfigTransferOwner";
            result = (Map) JPO.invoke(context, "JF_ProductConfig", null,
                    checkMethod, JPO.packArgs(params), Map.class);
        }
        code = result.get("code") == null ? "" : String.valueOf(result.get("code"));
        message = result.get("mess") == null ? "" : String.valueOf(result.get("mess"));

        if (!isSubmit && "200".equals(code)) {
            String joinedIds = selectedObjectIds.join(",");
            //20260828 update by caipan 转移Owner仅可选择当前项目研发部门的在职人员
            searchUrl = "../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=policy_Person.state_Active"
                    + "&table=AEFGeneralSearchResults&showInitialResults=true"
                    + "&includeOIDprogram=JF_ProductConfig:getTransferOwnerCandidatePersonIds"
                    + "&selection=single"
                    + "&submitAction=refreshCaller"
                    + "&submitURL=../common/JF_ProductConfigTransferOwner.jsp"
                    + "&jfTransferAction=submit&jfTransferType=" + XSSUtil.encodeForURL(context, transferType == null ? "" : transferType)
                    + "&jfTransferIds=" + XSSUtil.encodeForURL(context, joinedIds)
                    + "&jfRefreshToken=" + XSSUtil.encodeForURL(context, refreshToken);
        }
    } catch (Exception e) {
        message = e.getMessage();
        LOGGER.error("JF_ProductConfigTransferOwner error, mode={}", mode, e);
    }
    String escapedMessage = XSSUtil.encodeForJavaScript(context, message == null ? "" : message);
    String escapedSearchUrl = XSSUtil.encodeForJavaScript(context, searchUrl);
    String escapedRefreshToken = XSSUtil.encodeForJavaScript(context, refreshToken == null ? "" : refreshToken);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
    <script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
</head>
<body>
<script type="text/javascript">
    var isSubmit = <%=isSubmit%>;
    var code = "<%=code%>";
    var message = "<%=escapedMessage%>";
    var refreshToken = "<%=escapedRefreshToken%>";
    if (!isSubmit) {
        if (code !== "200") {
            alert(message);
        } else {
            var sourceTableWindow = window.parent;
            var sourceTopWindow = getTopWindow();
            var refreshStorageKey = "jfTransferOwnerRefresh." + refreshToken;
            if (!sourceTopWindow.jfTransferOwnerRefreshCallbacks) {
                sourceTopWindow.jfTransferOwnerRefreshCallbacks = {};
            }
            if (!sourceTopWindow.jfTransferOwnerStorageHandlers) {
                sourceTopWindow.jfTransferOwnerStorageHandlers = {};
            }
            var refreshSourceTable = function () {
                var registeredHandler = sourceTopWindow.jfTransferOwnerStorageHandlers[refreshToken];
                if (registeredHandler) {
                    sourceTopWindow.removeEventListener("storage", registeredHandler, false);
                }
                delete sourceTopWindow.jfTransferOwnerStorageHandlers[refreshToken];
                delete sourceTopWindow.jfTransferOwnerRefreshCallbacks[refreshToken];
                sourceTableWindow.location.href = sourceTableWindow.location.href;
            };
            var storageHandler = function (event) {
                if (event.key === refreshStorageKey) {
                    refreshSourceTable();
                }
            };
            sourceTopWindow.jfTransferOwnerRefreshCallbacks[refreshToken] = refreshSourceTable;
            sourceTopWindow.jfTransferOwnerStorageHandlers[refreshToken] = storageHandler;
            sourceTopWindow.addEventListener("storage", storageHandler, false);
            getTopWindow().showModalDialog("<%=escapedSearchUrl%>");
        }
    } else if (code !== "200") {
        alert(message);
        getTopWindow().closeWindow();
    } else {
        if (message) {
            alert(message);
        }
        var popupTopWindow = getTopWindow();
        var openerWindow = popupTopWindow.getWindowOpener ? popupTopWindow.getWindowOpener() : window.top.opener;
        var refreshHosts = [popupTopWindow, openerWindow];
        if (openerWindow && openerWindow.top) {
            refreshHosts.push(openerWindow.top);
        }
        if (window.top.opener) {
            refreshHosts.push(window.top.opener);
            if (window.top.opener.top) {
                refreshHosts.push(window.top.opener.top);
            }
        }
        var refreshed = false;
        for (var i = 0; i < refreshHosts.length && !refreshed; i++) {
            try {
                var callbacks = refreshHosts[i] && refreshHosts[i].jfTransferOwnerRefreshCallbacks;
                if (callbacks && typeof callbacks[refreshToken] === "function") {
                    callbacks[refreshToken]();
                    refreshed = true;
                }
            } catch (ignore) {
            }
        }
        if (!refreshed) {
            try {
                localStorage.setItem("jfTransferOwnerRefresh." + refreshToken, String(new Date().getTime()));
            } catch (ignore) {
            }
        }
        getTopWindow().closeWindow();
    }
</script>
</body>
</html>
