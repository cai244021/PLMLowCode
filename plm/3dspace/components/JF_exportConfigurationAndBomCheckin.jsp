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
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
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
<%
    String objectId = (String)emxGetParameter(request, "objectId");
    StringBuffer sbUrl = new StringBuffer( "JF_exportConfigurationAndBomProcess.jsp");
    sbUrl.append("?objectId=");
    sbUrl.append(objectId);
    String notesY = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ExportResult.Successful");
    String notesN = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ExportResult.Failed");
    String header = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Command.exportConfigurationAndBom");
    String content = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ExportDoing");
    String srcLink = "JF_exportConfigurationAndBomFS.jsp?header=" + header + "&content=" + content;
%>
</body>
</html>
<script language="JavaScript">
         let url = "<%=sbUrl%>";
         var l = window.open ("<%=srcLink%>", '', 'width=500,height=250,left=600,top=350,scrollbars=no,location=no,fullscreen=no','false');
         let mess = "";
         setTimeout(function() {
             jQuery.ajax({
                 url: url,
                 type: "GET",
                 async: true,
                 timeout: 60000000,
                 success: function (res) {
                     res = res.trim();
                     let words = res.split("{");
                     let data = JSON.parse("{" + words[1]);
                     let fileName = data.fileName;
                     let base64File = data.base64File;
                     if (fileName.length > 0) {
                         let bstr = atob(base64File),//解析buse-64编码的字符串
                             n = bstr.length,
                             u8arr = new Uint8Array(n);//创建初始化为0的,包含Length个元素的无符号整型数组
                         while (n--) {
                             u8arr[n] = bstr.charCodeAt(n);//返回字符串第一个字符的Unicode 编码
                         }
                         let blob = new Blob([u8arr]);
                         console.log("blob:", blob);
                         //创建一个a标签并设置href属性,之后模拟人为点击下载文件
                         let link = document.createElement('a');
                         link.href = window.URL.createObjectURL(blob);
                         link.download = fileName;//设置下载文件名
                         link.click();//模拟点击
                         //释放资源并删除创建的a标签
                         window.URL.revokeObjectURL(url);
                         mess = "<%=notesY%>";
                     } else {
                         mess = "<%=notesN%>";
                     }
                     l.close();

                     setTimeout(function() {
                         alert(mess);
                     }, 1000);
                 }
             });
         }, 2000); // 等待 100 毫秒后执行 Ajax 请求
</script>

