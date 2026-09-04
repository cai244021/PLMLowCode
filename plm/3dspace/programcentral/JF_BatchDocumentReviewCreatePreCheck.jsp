<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@include file="../common/emxNavigatorInclude.inc"%>
<%
    //项目文件夹只把选中的文档ID交给后台统一校验，校验失败时不打开创建页面。
    String projectId = emxGetParameter(request, "objectId");
    String[] selectedRows = request.getParameterValues("emxTableRowId");
    String[] selectedObjectIds = selectedRows == null ? new String[0] : ComponentsUIUtil.getSplitTableRowIds(selectedRows);
    HashMap params = new HashMap();
    params.put("docIds", selectedObjectIds);
    params.put("projectId", projectId);
    Map result;
    String createUrl = "";
    String message = "";
    String code = "";
    try {
        //校验选择的文档是否是工作中，当前用户的文档，类型必须为同一个项目、同一文档类型、同一专业。
        // 所选文档的编制人必须为同一人，否则不允许批量审核。
        // 所需文档类型不能存在回执（发起人回执和专家回执流程）。
        result = (Map) JPO.invoke(context, "JF_BatchDocumentReview", null, "validateBatchReviewDocuments", JPO.packArgs(params), Map.class);
        code = String.valueOf(result.get("code"));
        message = result.get("mess") == null ? "" : String.valueOf(result.get("mess"));
        String selectedObjectId = selectedObjectIds[0];
        DomainObject object = DomainObject.newInstance(context, selectedObjectId);
        String attributeValue = object.getAttributeValue(context, "JF_DocDepartmentManager");
        String documentIds = String.join(",", selectedObjectIds);
        createUrl = "JF_BatchDocumentReviewCreate.jsp?docIds="
                + URLEncoder.encode(documentIds, "UTF-8")
                + "&projectId="
                + URLEncoder.encode(projectId, "UTF-8")
                + "&Department="
                + URLEncoder.encode(attributeValue, "UTF-8");
    } catch (Exception e) {
        e.printStackTrace();
        result = new HashMap();
        message = e.getMessage() == null ? e.toString() : e.getMessage();
        code = "500";
        result.put("code", "500");
        result.put("mess", e.getMessage() == null ? e.toString() : e.getMessage());
    }

%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <script src="../common/scripts/emxUIConstants.js" type="text/javascript"></script>
    <script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
    <script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
</head>
<body>
<script type="text/javascript">
<% if ("200".equals(code)) { %>
    showModalDialog("<%=XSSUtil.encodeForJavaScript(context, createUrl)%>", 520, 300, true, "Medium");
<% } else { %>
    alert("<%=XSSUtil.encodeForJavaScript(context, message)%>");
<% } %>
</script>
</body>
</html>
