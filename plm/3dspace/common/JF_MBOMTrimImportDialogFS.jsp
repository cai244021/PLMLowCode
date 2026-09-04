<%@ page pageEncoding="utf-8" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@include file="../emxUICommonAppInclude.inc"%>
<%@include file="../components/emxComponentsNoCache.inc"%>
<%@include file="../components/emxComponentsUtil.inc"%>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<html>
<head>
    <link rel="stylesheet" type="text/css" href="../common/styles/emxUIDefault.css">
    <link rel="stylesheet" type="text/css" href="../common/styles/emxUIList.css">
    <style type="text/css">
        #formalImportErrors {
            display: none;
            margin: 12px;
            padding: 10px;
            max-height: 180px;
            overflow: auto;
            white-space: pre-wrap;
            color: #b00020;
            border: 1px solid #d9a5ad;
            background: #fff7f8;
        }
        #formalImportOverlay {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 9999;
            align-items: center;
            justify-content: center;
            color: #ffffff;
            font-size: 18px;
            background: rgba(0, 0, 0, 0.45);
        }
    </style>
</head>
<%
    String objectId = emxGetParameter(request, "objectId");
    String safeObjectId = XSSUtil.encodeForHTMLAttribute(context, objectId == null ? "" : objectId);
    String stringResource = "emxComponentsStringResource";
    String header = EnoviaResourceBundle.getProperty(context, stringResource, context.getLocale(),
            "emxComponents.MBOMTrimImport.Header");
    String fileLabel = EnoviaResourceBundle.getProperty(context, stringResource, context.getLocale(),
            "emxComponents.MBOMTrimImport.FileLabel");
    String importing = EnoviaResourceBundle.getProperty(context, stringResource, context.getLocale(),
            "emxComponents.MBOMTrimImport.Importing");
    String selectFile = EnoviaResourceBundle.getProperty(context, stringResource, context.getLocale(),
            "emxComponents.MBOMTrimImport.SelectFile");
    String importLabel = EnoviaResourceBundle.getProperty(context, stringResource, context.getLocale(),
            "emxComponents.MBOMTrimImport.Import");
    String cancelLabel = EnoviaResourceBundle.getProperty(context, stringResource, context.getLocale(),
            "emxComponents.MBOMTrimImport.Cancel");
    String unsupportedFormat = EnoviaResourceBundle.getProperty(context, stringResource, context.getLocale(),
            "emxComponents.MBOMTrimImport.UnsupportedFormat");
    String importSuccess = EnoviaResourceBundle.getProperty(context, stringResource, context.getLocale(),
            "emxComponents.MBOMTrimImport.Success");
    String updatedObjects = EnoviaResourceBundle.getProperty(context, stringResource, context.getLocale(),
            "emxComponents.MBOMTrimImport.UpdatedObjects");
    String updatedRelationships = EnoviaResourceBundle.getProperty(context, stringResource, context.getLocale(),
            "emxComponents.MBOMTrimImport.UpdatedRelationships");
    String importFailed = EnoviaResourceBundle.getProperty(context, stringResource, context.getLocale(),
            "emxComponents.MBOMTrimImport.Failed");
%>
<body>
<div id="formalImportOverlay"><%=XSSUtil.encodeForHTML(context, importing)%></div>
<div id="divPageBody">
    <form id="formalImportForm" name="formalImportForm" enctype="multipart/form-data" method="post">
        <input type="hidden" name="objectId" value="<%=safeObjectId%>">
        <table class="list">
            <tbody>
            <tr>
                <th colspan="2"><%=XSSUtil.encodeForHTML(context, header)%></th>
            </tr>
            <tr class="even">
                <td class="label" style="width: 30%;"><%=XSSUtil.encodeForHTML(context, fileLabel)%></td>
                <td class="field">
                    <input id="formalImportFile" name="formalImportFile" type="file" accept=".xlsx,.xls">
                </td>
            </tr>
            </tbody>
        </table>
    </form>
    <div id="formalImportErrors"></div>
</div>
<div id="divPageFoot">
    <table width="100%" border="0" cellspacing="2" cellpadding="3">
        <tbody>
        <tr>
            <td class="buttons" align="right">
                <button id="formalImportButton" class="btn-primary" type="button" onclick="formalImportTrimMBOM(this)">
                    <%=XSSUtil.encodeForHTML(context, importLabel)%>
                </button>
                <button class="btn-default" type="button" onclick="getTopWindow().closeWindow()">
                    <%=XSSUtil.encodeForHTML(context, cancelLabel)%>
                </button>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<script type="text/javascript">
    function formalImportShowErrors(errors) {
        var errorBox = document.getElementById("formalImportErrors");
        errorBox.textContent = (errors || []).join("\n");
        errorBox.style.display = "block";
    }

    function formalImportRefreshOpener() {
        var topWindow = getTopWindow();
        var openerWindow = topWindow.getWindowOpener ? topWindow.getWindowOpener() : window.opener;
        if (!openerWindow) {
            return;
        }
        try {
            if (openerWindow.emxEditableTable && openerWindow.emxEditableTable.refreshStructure) {
                openerWindow.emxEditableTable.refreshStructure();
            } else {
                openerWindow.location.reload();
            }
        } catch (ignore) {
            openerWindow.location.reload();
        }
    }

    function formalImportTrimMBOM(button) {
        var fileInput = document.getElementById("formalImportFile");
        if (!fileInput.files || fileInput.files.length === 0) {
            alert("<%=XSSUtil.encodeForJavaScript(context, selectFile)%>");
            return;
        }
        var fileName = fileInput.files[0].name.toLowerCase();
        if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) {
            alert("<%=XSSUtil.encodeForJavaScript(context, unsupportedFormat)%>");
            return;
        }

        document.getElementById("formalImportErrors").style.display = "none";
        document.getElementById("formalImportOverlay").style.display = "flex";
        button.disabled = true;
        fetch("JF_MBOMTrimImport.jsp", {
            method: "POST",
            body: new FormData(document.getElementById("formalImportForm"))
        }).then(function (response) {
            return response.json();
        }).then(function (result) {
            if (result.flag === "Y") {
                var report = result.report || {};
                alert((result.message || "<%=XSSUtil.encodeForJavaScript(context, importSuccess)%>")
                    + "\n<%=XSSUtil.encodeForJavaScript(context, updatedObjects)%>: " + (report.objectCount || 0)
                    + "\n<%=XSSUtil.encodeForJavaScript(context, updatedRelationships)%>: " + (report.relationshipCount || 0));
                formalImportRefreshOpener();
                getTopWindow().closeWindow();
                return;
            }
            formalImportShowErrors(result.errors && result.errors.length
                ? result.errors
                : [result.message || "<%=XSSUtil.encodeForJavaScript(context, importFailed)%>"]);
            document.getElementById("formalImportOverlay").style.display = "none";
            button.disabled = false;
        }).catch(function (error) {
            formalImportShowErrors([error.message || "<%=XSSUtil.encodeForJavaScript(context, importFailed)%>"]);
            document.getElementById("formalImportOverlay").style.display = "none";
            button.disabled = false;
        });
    }
</script>
</body>
</html>
