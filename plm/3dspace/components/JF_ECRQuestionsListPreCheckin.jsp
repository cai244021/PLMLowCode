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
    private static final Logger _logger =  LoggerFactory.getLogger("Idm_IPPAPPreCheckin");
%>
<%
    String objectId = (String)emxGetParameter(request, "objectId");
    _logger.info("Idm_IPPAPPreCheckin : objectId:{}",objectId);
    StringBuffer sbUrl = new StringBuffer( "JF_ECRQuestionsListCheckinDialogFS.jsp");
    sbUrl.append("?objectId=");
    sbUrl.append(objectId);
%>
          <form name="application" action="<%=XSSUtil.encodeForHTML(context, sbUrl.toString())%>" >
            <input type="hidden" name="objectId" value="<%=objectId%>" />
          </form>
          <script>
              document.application.submit();
          </script>
</body></html>
