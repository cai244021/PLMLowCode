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
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_ESOExportPreFS.jsp");
%>
<%
    String mess = "";
    String objectId = (String) emxGetParameter(request, "objectId");
    String[] tableRowId = emxGetParameterValues(request, "emxTableRowId");
    LOGGER.info("JF_ESOExportPreFS.jsp======tableRowId:{},objectId：{}", Arrays.asList(tableRowId),objectId);
    String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
    String ids = String.join(",", splitTableRowIds);
    DomainObject projectObj = DomainObject.newInstance(context,objectId);
    String projectType = projectObj.getInfo(context,"type");
    if (!"Project Space".equals(projectType)){
        //如果不是项目 则获取项目id
        StringList slBusSelects = new StringList(2);
        slBusSelects.add(DomainConstants.SELECT_NAME);
        slBusSelects.add(DomainConstants.SELECT_ID);
        com.matrixone.apps.program.Task taskObj = new com.matrixone.apps.program.Task();
        taskObj.setId(objectId);
        Map taskInfo = taskObj.getProject(context, slBusSelects);
        objectId = UIUtil.getValue(taskInfo,DomainConstants.SELECT_ID);
    }
    StringBuffer sbUrl = new StringBuffer( "JF_ESOExportCheckout.jsp?esoTaskId="+ids+"&objectId="+objectId);
    String notesY = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ESO.ExportComplete", new String[]{});
    String notesN = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ESO.ExportFailed", new String[]{});
    String header = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ESO.Export", new String[]{});
    String content =ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ESO.Exporting", new String[]{});
    String srcLink = "../common/JF_windowOpenDialogFS.jsp?header=" + header + "&content=" + content;
    DomainObject taskObj = DomainObject.newInstance(context,splitTableRowIds[0]);
    String type = taskObj.getInfo(context,"type");
    if (!"JF_ESOTask".equals(type)){
        mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ESO.Type", new String[]{});
    }
%>
<% if (mess != null && !mess.isEmpty())
    {
%>
<script>
    alert("<%=mess%>");
</script>
<%
    } else {
%>
<!-- 新增：隐藏表单用于提交 -->
<form id="exportForm" action="JF_ESOExportCheckout.jsp" method="post" style="display:none;">
    <input type="hidden" name="esoTaskId" value="<%=ids%>" />
    <input type="hidden" name="objectId" value="<%=objectId%>" />
</form>


<script>
    // 显示 loading 弹窗
    var l = window.open("<%=srcLink%>", '', 'width=500,height=250,left=600,top=350,scrollbars=no,location=no,fullscreen=no', 'false');
    document.getElementById('exportForm').submit();
</script>
<% } %>
</body>
</html>
