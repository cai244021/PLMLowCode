
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
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_ChiefEngineerConfirmProcess.jsp");
%>
<%
    String strObjectId = (String)emxGetParameter(request, "objectId");
    JF_LOGGER.info("JF_ChiefEngineerConfirmProcess.jsp-------strObjectId:{}",strObjectId);
    Gson gson = new Gson();
    formBean.processForm(session, request);
    String personId  = (String) formBean.getElementValue("JF_ChiefEngineerReviewPersonNameOID");
    JF_LOGGER.info("JF_ChiefEngineerConfirmProcess.jsp-------personId:{}",personId);
    HashMap params = new HashMap();
    params.put("personId", personId);
    params.put("objectId", strObjectId);
    String failedMess = "";

    JPO.invoke(context, "JF_ESO", null, "chiefEngineerConfirmProcess", JPO.packArgs (params), Void.class);


%>
<html>
<body>
<script>
    getTopWindow().closeWindow();
    window.top.getWindowOpener().top.refreshTablePage();
</script>
</body>
</html>
