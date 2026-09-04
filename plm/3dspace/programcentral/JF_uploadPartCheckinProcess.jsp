<%--  emxComponentsCheckinProcess.jsp -
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = "$Id: emxCommonDocumentCheckinProcess.jsp.rca 1.22 Wed Oct 22 16:18:50 2008 przemek Experimental przemek $"
--%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap,java.util.List"%>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.io.File" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="org.apache.poi.ss.usermodel.Workbook" %>
<%@ page import="org.apache.poi.ss.usermodel.WorkbookFactory" %>
<%@ page import="org.apache.poi.ss.usermodel.Sheet" %>
<%@ page import="org.apache.poi.ss.usermodel.Row" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.ProgramUtil" %>

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
        private static final Logger NIO_LOG = LoggerFactory.getLogger("info");
%>
<%
        formBean.processForm(session, request);
        java.io.File fileDoc = (java.io.File) formBean.getElementValue("bfile");
        String objectId = (String) formBean.getElementValue("objectId");
        String strType = (String)emxGetParameter(request, "type");
        String mode = (String)emxGetParameter(request, "mode");
        String strLanguage = (String) formBean.getElementValue("strLanguage");
        NIO_LOG.info("mode:{}",mode);
        String mess = null;
        StringList strResultMess =new StringList();
        String strErrorMess = "";
        List files = new ArrayList();
        files.add(fileDoc);
        //文件校验：判断上传文件是否是Excel文件
        Boolean fileIsExcel = true;
        Iterator iteratorFile = files.iterator();
        // 文件校验：
        // 校验上传的文件是否是Excel
        while (iteratorFile.hasNext()) {
                File file = (File)iteratorFile.next();
                String fileName = file.getName();
                NIO_LOG.info("fileName:{}",fileName);
                // 文件校验：判断文件的类型是否为Excel:xls slsx .xlsm,非两种文件，直接退出
                if (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls") && !fileName.toLowerCase().endsWith(".xlsm")) {
                        NIO_LOG.info("fileName:{}", fileName);
                        mess = ComponentsUIUtil.getI18NString(context, strLanguage, "emxComponents.Mess.UpLoadInspectionPartFileTypeError", new String[]{});
%>
<script language="JavaScript" type="text/javascript">
        alert("<%=mess%>");
        getTopWindow().closeWindow();
        top.opener.document.location.href = top.opener.document.location.href;
</script>
<%
                return;
                }
        }

        String initargs[] = {};
        HashMap params = new HashMap();
        params.put("objectId",objectId);
        params.put("files",files);
        params.put("timezone", (String)session.getAttribute("timeZone"));
        params.put("suiteKey", "Components");
        params.put("type", strType);
        params.put("HttpServletRequest", request);

    String failedMess = "";
        if ("project".equalsIgnoreCase(mode)) {
                strResultMess = (StringList) JPO.invoke(context, "JF_ProcessExcel", initargs, "importProjectTask", JPO.packArgs (params), StringList.class);
        } else  if ("Trim".equalsIgnoreCase(mode)) {
                strResultMess = (StringList) JPO.invoke(context, "JF_ProcessExcel", initargs, "importTrimPart", JPO.packArgs (params), StringList.class);
        } else if ("NewECR".equalsIgnoreCase(mode)){
                strResultMess = (StringList) JPO.invoke(context, "JF_ProcessExcel", initargs, "importECRItemPart", JPO.packArgs (params), StringList.class);
        }  else if ("ChairManager".equalsIgnoreCase(mode)){
            strResultMess = (StringList) JPO.invoke(context, "JF_ProcessExcel", initargs, "importProjectESOTask", JPO.packArgs (params), StringList.class);
        } else if ("PartList".equalsIgnoreCase(mode)) {
            strResultMess = (StringList) JPO.invoke(context, "JF_ProcessExcel", initargs, "importJFPartLists", JPO.packArgs (params), StringList.class);
        } else {
                //返回信息
//                strResultMess = (String) JPO.invoke(context, "Idm_InspectionModuleProdLinePart", initargs, "uploadFileInspectionMouldProdLinePartCreate", JPO.packArgs(params), String.class);
        }
        //包含error：报错有异常
        if ("NewECR".equalsIgnoreCase(mode)) {
%>
<script language="JavaScript" type="text/javascript">
    alert("<%=strResultMess%>");
    getTopWindow().closeWindow();
    top.opener.document.location.href = top.opener.document.location.href;
</script>
<%
            return;
        } else {
            if (strResultMess.size()>0) {
                strErrorMess =strResultMess.toString();// ComponentsUIUtil.getI18NString(context, strLanguage, "emxComponents.Mess.UpLoadListFileTypeError", new String[]{});
                  out.println("");
                  out.println("<html>");
                  out.println("<head>");
                  out.println("<title>Error Notice</title>");
                  out.println("<style>body { background-color: #f0f0f0; } h1 { color: red; } h2 { color: blue; }</style>");
                  out.println("</head>");
                  out.println("<body>");
                  out.println("<table>");
                  out.println("<thead>");
                  out.println("<h1 color='red'>Import Error Notice</h1>");
                  out.println("</thead>");
                  out.println("<tbody>");
                  for(int i=0;i<strResultMess.size();i++) {
                          out.println("<tr><td>");
                          out.println("<h2>"+strResultMess.get(i)+"</h2>");
                          out.println("</td></tr>");
                  }
                  out.println("</tbody>");
                  out.println("</table>");
                  out.println("</body>");
                  out.println("</html>");
%>
<script language="JavaScript" type="text/javascript">
        <%--alert("<%=strErrorMess%>");--%>

        //getTopWindow().closeWindow();
        //top.opener.document.location.href = top.opener.document.location.href;
</script>
<%
                    return;
                } else if (strResultMess.size()==0) {
                //上传成功 关闭窗口
%>
<script language="JavaScript" type="text/javascript">
                alert("Modified successfully");
                getTopWindow().closeWindow();
                top.opener.document.location.href = top.opener.document.location.href;
</script>
<%
                return;
            }
        }


%>