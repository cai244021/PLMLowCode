<%--  emxComponentsCheckinProcess.jsp -
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = "$Id: emxCommonDocumentCheckinProcess.jsp.rca 1.22 Wed Oct 22 16:18:50 2008 przemek Experimental przemek $"
--%>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.commons.fileupload.DiskFileUpload" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@ include file = "../emxUICommonHeaderBeginInclude.inc" %>
<%@include file = "../common/emxUIConstantsInclude.inc"%>
<%@ include file = "../emxJSValidation.inc"%>
<%@include file = "../components/emxComponentsUtil.inc"%>
<%@include file = "../components/emxComponentsSetCompanyKeyInRPE.inc"%>
<jsp:useBean id="formBean" scope="page" class="com.matrixone.apps.common.util.FormBean" />
<jsp:useBean id="requestBean" scope="page" class="com.matrixone.apps.domain.util.Request"/>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%!
        private static final Logger _logger = LoggerFactory.getLogger("JF_ECRQQFileCheckinProcess");
%>
<%
        String strObjectId = (String)emxGetParameter(request, "objectId");
        _logger.info("--------------------FileUpload begin -----------------------------");
        Gson gson = new Gson();
        formBean.processForm(session, request);
        String revision = (String) formBean.getElementValue("revision");
        HashMap params = new HashMap();
        params.put("objectId", strObjectId);
        params.put("revision", revision);
        String failedMess = "";
        //返回信息
        //Idm_PPAPService
        Boolean res = (Boolean) JPO.invoke(context, "JF_ColorPart", JPO.packArgs (params), "colorMatrixUpgradeVersion", JPO.packArgs (params), Boolean.class);
        _logger.info("strDocHtml:{}",res);
        String mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ColorMatrix.UpgradeVersionSuccess", new String[]{});
        if (!res) {
                mess =   ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ColorMatrix.UpgradeVersionError", new String[]{});
        }
        _logger.info("--------------------FileUpload end -----------------------------");
%>
<html>
<body>
<script>
        <%--var parentDoc = getTopWindow().getWindowOpener().document;--%>
        <%--console.log(parentDoc.getElementById("JFECRQQFileId").value);--%>
        <%--parentDoc.getElementById("JFECRQQFileId").value = "<%=res.get("newId")%>";--%>
        <%--parentDoc.getElementById("JFReplace").innerHTML = `<%=res.get("html")%>`;--%>
        alert("<%=mess%>");
        getTopWindow().closeWindow();
        // window.top.getWindowOpener().top.refreshTablePage();
        getTopWindow().getWindowOpener().parent.document.location.href = getTopWindow().getWindowOpener().parent.document.location.href;

        // getTopWindow().findFrame(parent.getTopWindow(),"detailsDisplay").location.href = parent.getTopWindow().findFrame(parent.getTopWindow(),"detailsDisplay").location.href.replace("persist=true", "persist=false");
</script>
</body>
</html>
