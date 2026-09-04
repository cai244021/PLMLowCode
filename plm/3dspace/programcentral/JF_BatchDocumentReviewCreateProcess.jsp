<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@include file="../common/emxNavigatorInclude.inc"%>
<%@include file="../common/enoviaCSRFTokenValidation.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_BatchDocumentReview_mxJPO.class");
%>
<%
    //保存和提交共用后台创建入口，由submitReview区分是否立即发起流程。
    String title = emxGetParameter(request, "title");
    String description = emxGetParameter(request, "description");
    String documentIds = emxGetParameter(request, "docIds");
    String projectId = emxGetParameter(request, "projectId");
    //20260811 update by ljr 将创建页面选择的会签人员ID传入批量审批单创建入口。
    String signPersonIds = emxGetParameter(request, "signPersonNameOID");
    String departPersonIds = emxGetParameter(request, "departPersonNameOID");
    String submitReview = emxGetParameter(request, "submitReview");
    JF_LOGGER.info("signPersonIds:{}", signPersonIds);
    JF_LOGGER.info("departPersonIds:{}", departPersonIds);
    JF_LOGGER.info("submitReview:{}", submitReview);
    HashMap params = new HashMap();
    params.put("title", title);
    params.put("description", description);
    params.put("projectId", projectId);
    params.put("docIds", FrameworkUtil.split(documentIds == null ? "" : documentIds, ","));
    params.put("signPersonIds", FrameworkUtil.split(signPersonIds == null ? "" : signPersonIds, "|"));
    params.put("departPersonIds", FrameworkUtil.split(departPersonIds == null ? "" : departPersonIds, "|"));
    params.put("submitReview", submitReview);
    String code = "500";
    String message = "";
    String objectId = "";
    Map result = (Map) JPO.invoke(context, "JF_BatchDocumentReview", null, "createBatchDocumentReview", JPO.packArgs(params), Map.class);
    code = String.valueOf(result.get("code"));
    if ("200".equalsIgnoreCase(code)) {
        objectId = result.get("objectId") == null ? "" : String.valueOf(result.get("objectId"));
    } else {
        message = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorPart.CreateFailed");
    }
%>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8" /><script src="../common/scripts/emxUICore.js" type="text/javascript"></script></head>
<body>
<script type="text/javascript">

<% if ("200".equals(code)) { %>
    var openerWindow = getTopWindow().getWindowOpener();
    openerWindow.close();
    // if (openerWindow && openerWindow.refreshTablePage) {
    //     openerWindow.refreshTablePage();
    // } else if (openerWindow) {
    //     openerWindow.location.reload();
    // }
    getTopWindow().location.href = "../common/emxTree.jsp?objectId=<%=XSSUtil.encodeForURL(context, objectId)%>";
<% } else { %>
    alert("<%=XSSUtil.encodeForJavaScript(context, message)%>");
    var openerWindow = getTopWindow().getWindowOpener();
    openerWindow.close();
<% } %>
</script>
</body>
</html>
