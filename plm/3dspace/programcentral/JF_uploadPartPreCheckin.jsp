<%--  Idm_uploadPartPreCheckin.jsp

    Copyright (c) 1992-2020 Dassault Systemes.
    All Rights Reserved  This program contains proprietary and trade secret
    information of MatrixOne, Inc.
    Copyright notice is precautionary only and does not evidence any
    actual or intended publication of such program

    Description : This jsp is a listing of upload type IdmInspectPart、IdmMouldPart、IdmProdLinKPart;
--%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../components/emxComponentsNoCache.inc"%>
<%@include file = "../components/emxComponentsUtil.inc"%>
<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>
<jsp:useBean id="tableBean" class="com.matrixone.apps.framework.ui.UITable" scope="session"/>
<html>
<head>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js" type="text/javascript"></script>
</head>
<body>
<%!
    private static final Logger NIO_LOG = LoggerFactory.getLogger("JF_uploadPartPreCheckin.jsp");
%>
<%

    //车型项目id
    String objectId = (String)emxGetParameter(request, "objectId");
    //类型 ： 检具(InspectionPart) 模具(MouldPart) 产线(ProdLinePart)
    String strType = (String)emxGetParameter(request, "Type");
    String mode = (String)emxGetParameter(request, "mode");
    NIO_LOG.info("mode:{}",mode);
    String strLanguage = request.getHeader("Accept-Language");   //PRG:RG6:R212:1-Jun-2011:IR-111810V6R2012x

    Map emxCommonDocumentCheckinData = new HashMap();
    emxCommonDocumentCheckinData.put("objectId", objectId);
    emxCommonDocumentCheckinData.put("type", strType);
    emxCommonDocumentCheckinData.put("strLanguage", strLanguage);
    emxCommonDocumentCheckinData.put("mode", mode);

    String forwardURL = "JF_uploadPartCheckinDialogFS.jsp";
    session.setAttribute("emxCommonDocumentCheckinData", emxCommonDocumentCheckinData);
%>
          <form name="application" action="<%=XSSUtil.encodeForHTML(context, forwardURL)%>" >
            <input type="hidden" name="xyz" value="xyz" />
          </form>
          <script>
              document.application.submit();
          </script>
</body></html>
