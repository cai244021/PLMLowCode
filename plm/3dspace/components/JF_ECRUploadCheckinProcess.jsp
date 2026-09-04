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
        private static final Logger _logger = LoggerFactory.getLogger("JF_ECRUploadCheckinProcess");
%>
<%
        String strObjectId = (String)emxGetParameter(request, "objectId");
        String strUploadType = (String)emxGetParameter(request, "type");

        _logger.info("--------------------FileUpload begin -----------------------------");
        Gson gson = new Gson();
        formBean.processForm(session, request);
        java.io.File fileDoc = (java.io.File) formBean.getElementValue("bfile");
        List files = new ArrayList();
        files.add(fileDoc);
        String initargs[] = {};
        HashMap params = new HashMap();
        params.put("files",files);
        params.put("timezone", (String)session.getAttribute("timeZone"));
        params.put("suiteKey", "Components");
        params.put("objectId", strObjectId);
        params.put("type", strUploadType);
        String strJpoName = "";
        String strFunName = "";
        if ("JFNewECRCosting".equals(strUploadType) || "JFNewECRController".equals(strUploadType)){
                strJpoName = "JF_NewECRService";
                strFunName = "readNewECRExcelAndUpdateData";
        }else if ("JFECRCosting".equals(strUploadType) || "JFECRController".equals(strUploadType)){
                strJpoName = "JF_ECRService";
                strFunName = "readECRExcelAndUpdateData";
        } else if ("JFFormalECRCosting".equals(strUploadType) || "JFFormalECRController".equals(strUploadType)){
                strJpoName = "JF_FormalECRService";
                strFunName = "readFormalECRExcelAndUpdateData";
        }else if ("JFFormalInitialCostBuy".equals(strUploadType) || "JFFormalInitialCostMake".equals(strUploadType)){
                strJpoName = "JF_FormalECRService";
                strFunName = "readFormalInitialCostExcelAndUpdateData";
        }
        String failedMess = "";
        //返回信息
        Map res = (Map) JPO.invoke(context, strJpoName, initargs, strFunName, JPO.packArgs (params), Map.class);
        String strMess = (String) res.get("mess");
        _logger.info("strMess:{}",strMess);
%>
<html>
<body>
<script>
        alert("<%=strMess%>");
        //刷新table
        parent.getTopWindow().getWindowOpener().refreshSBTable();
        parent.close();
</script>
</body>
</html>
