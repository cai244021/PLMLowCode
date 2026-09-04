        <%--  Idm_uploadPartPreCheckin.jsp

    Copyright (c) 1992-2020 Dassault Systemes.
    All Rights Reserved  This program contains proprietary and trade secret
    information of MatrixOne, Inc.
    Copyright notice is precautionary only and does not evidence any
    actual or intended publication of such program

    Description : This jsp is a listing of upload type IdmInspectPart、IdmMouldPart、IdmProdLinKPart;
--%>
        <%@ page pageEncoding="utf-8" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
        <%@ page import="java.util.Arrays" %>
        <%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
        <%@ page import="com.matrixone.apps.domain.DomainObject" %>
        <%@ page import="matrix.util.StringList" %>
        <%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
        <%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../components/emxComponentsNoCache.inc"%>
<%@include file = "../components/emxComponentsUtil.inc"%>
<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>
<jsp:useBean id="tableBean" class="com.matrixone.apps.framework.ui.UITable" scope="session"/>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<html>
<head>
</head>
<body>
<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_MBOMExportPreFS.jsp");
%>
<%
    String mess = "";
    String objectId = (String) emxGetParameter(request, "objectId");
    LOGGER.info("JF_MBOMExportPreFS.jsp----objectId:{}",objectId);
    StringBuffer sbUrl = new StringBuffer( "JF_MBOMExportDialogFS.jsp?objectId="+objectId);

    DomainObject projectObj = DomainObject.newInstance(context,objectId);
    String mbomId = projectObj.getInfo(context, "from[JF_relProject2MBOM].to.id");
    if (UIUtil.isNullOrEmpty(mbomId)){
        mess = "没有MBOM数据，不允许导出";
    }
%>
</body>

<script language="JavaScript">
    let mess = "<%=mess%>";
    if (mess) {
        alert(mess);
    } else {
        window.open ("<%=sbUrl%>", '', 'width=500,height=300,left=600,top=350,scrollbars=no,location=no,fullscreen=no','false');
    }
</script>
</html>
