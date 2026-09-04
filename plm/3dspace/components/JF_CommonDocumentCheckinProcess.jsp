<%--jsp--%>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="org.apache.commons.fileupload.DiskFileUpload" %>
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
    private static final Logger _logger = LoggerFactory.getLogger("JF_CommonDocumentCheckinProcess.jsp");
%>
<%
    response.setContentType("application/json"); // 设置响应类型为JSON
    PrintWriter printWriter = response.getWriter();
    Gson gson = new Gson();
    Map<String, Object> responseMap = new HashMap<>();
    try {
        String strObjectId = (String) emxGetParameter(request, "objectId");
        Map emxCommonDocumentCheckinData = (Map) session.getAttribute("emxCommonDocumentCheckinData");
        _logger.info("--------------------FileUpload begin -----------------------------");
        _logger.info("emxCommonDocumentCheckinData:{}", emxCommonDocumentCheckinData);

//        formBean.processForm(session, request);
        _logger.info("formBean:{}", gson.toJson(formBean));
        DiskFileUpload upload	= new DiskFileUpload();
        //List items 				= upload.parseRequest(request);
        List files = upload.parseRequest(request);
        _logger.info("files:{}", files);

//        List<java.io.File> files = new ArrayList<>();
//        for (int i = 0; i < 20; i++) {
//            java.io.File fileDoc = (java.io.File) formBean.getElementValue("bfile" + i);
//            if (fileDoc != null) {
//                files.add(fileDoc);
//                _logger.info("fileDoc{}:{}", i, fileDoc);
//            }
//        }
        emxCommonDocumentCheckinData.put("files", files);
        emxCommonDocumentCheckinData.put("suiteKey", "Components");
        String[] initargs = {};
        Map res = (Map) JPO.invoke(context, "JF_PublicProjectQuery", initargs, "uploadFile", JPO.packArgs(emxCommonDocumentCheckinData), Map.class);

        responseMap.put("code", res.get("code"));
        responseMap.put("message", res.get("message"));
        _logger.info("--------------------FileUpload end -----------------------------");
    } catch (Exception e) {
        _logger.error("JF_CommonDocumentCheckinProcess.jsp-------error::", e);
        responseMap.put("code", "500");
        responseMap.put("message", "An error occurred: " + e.getMessage());
    } finally {
        printWriter.write(gson.toJson(responseMap)); // 返回JSON数据
        printWriter.flush();
        printWriter.close();
    }
%>