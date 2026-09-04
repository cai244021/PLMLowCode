<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="org.apache.commons.text.StringEscapeUtils" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="java.net.URLEncoder" %><%--  emxRoleGroupFilter.jsp   -
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,Inc.
   Copyright notice is precautionary only and does not evidence any actual or intended publication of such program
	
   To Filter Role and Group
--%>
<%@include file="../common/emxNavigatorInclude.inc"%>
<%@include file="../common/emxNavigatorTopErrorInclude.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_emxGroupsFilter.jsp");
%>
<%
    try {
        String strJFColorStyleInputContainsCmd  = emxGetParameter(request, "JFColorStyleInputContainsCmd");
        JF_LOGGER.info("JF_emxGroupsFilter.jsp");
        JF_LOGGER.info("strJFColorStyleInputContainsCmd:{}", strJFColorStyleInputContainsCmd);
        strJFColorStyleInputContainsCmd = URLEncoder.encode(strJFColorStyleInputContainsCmd, StandardCharsets.UTF_8);
        JF_LOGGER.info("strJFColorStyleInputContainsCmd:{}", strJFColorStyleInputContainsCmd);

        String sTableName           = emxGetParameter(request, "table");
        String sAnalysis             = "analysis";
        String mess =   ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ColorAnalysis.NotGroupStyle", new String[]{});
%>
<html>
<head></head>
<body>
    <script language="JavaScript">
        let groupValue = "<%=XSSUtil.encodeForJavaScript(context, strJFColorStyleInputContainsCmd)%>";
        if (groupValue == "") {
            alert("<%=XSSUtil.encodeForJavaScript(context, mess)%>");
        } else {
            //往url上添加参数
            parent.resetParameter("JFColorStyleInputContainsCmd", "<%=XSSUtil.encodeForJavaScript(context, strJFColorStyleInputContainsCmd)%>");
            parent.resetParameter("mode","<%=XSSUtil.encodeForJavaScript(context, sAnalysis)%>");

            //刷新table
            parent.refreshSBTable("<%=XSSUtil.encodeForJavaScript(context, sTableName)%>","Name","ascending");
        }
    </script>
<%
    } catch (Exception ex){
         if (ex.toString() != null && ex.toString().length() > 0){
            emxNavErrorObject.addMessage(ex.toString());
         }
         ex.printStackTrace();
    }
%>
</body>
</html>
<%@include file="../common/emxNavigatorBottomErrorInclude.inc"%>

