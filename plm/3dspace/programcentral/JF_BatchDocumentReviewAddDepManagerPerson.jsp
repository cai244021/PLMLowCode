<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@include file="../common/emxNavigatorInclude.inc"%>
<%@include file="../common/enoviaCSRFTokenValidation.inc"%>
<%
    // 搜索结果提交后由后台再次校验审批单权限及人员状态，再创建会签人员关系。
    String batchReviewId = emxGetParameter(request, "objectId");
    String[] selectedRows = request.getParameterValues("emxTableRowId");
    String[] personIds = ComponentsUIUtil.getSplitTableRowIds(selectedRows);
    HashMap params = new HashMap();
    params.put("batchReviewId", batchReviewId);
    params.put("personIds", personIds);
    String code = "500";
    String message = "";
    DomainObject batchReview = DomainObject.newInstance(context, batchReviewId);
    StringList existingPersonIds = batchReview.getInfoList(context, "from[JFDocument2DepManager].to.id");
    boolean transactionStarted = false;
    try {
        ContextUtil.startTransaction(context, true);
        transactionStarted = true;
        // 已关联的会签人员直接跳过，保证重复提交不会生成重复关系。
        StringList stringList = new StringList();
        for (String personId : personIds) {
            if (!existingPersonIds.contains(personId)) {
                stringList.add(personId);
            }
        }
        batchReview.addRelatedObjects(context, new RelationshipType("JFDocument2DepManager"), true, stringList.toStringArray());
        ContextUtil.commitTransaction(context);
        transactionStarted = false;
        code = "200";
    } catch (Exception e) {
        if (transactionStarted) {
            ContextUtil.abortTransaction(context);
        }
        message = e.getMessage() == null ? e.toString() : e.getMessage();
    }


%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
</head>
<body>
<script type="text/javascript">
<% if ("200".equals(code)) { %>
    var openerWindow = getTopWindow().getWindowOpener();
    // 添加完成后仅刷新会签人员Table，然后关闭人员搜索窗口。
    if (openerWindow && openerWindow.refreshTablePage) {
        openerWindow.refreshTablePage();
    } else if (openerWindow) {
        openerWindow.location.reload();
    }
    getTopWindow().closeWindow();
<% } else { %>
    alert("<%=XSSUtil.encodeForJavaScript(context, message)%>");
<% } %>
</script>
</body>
</html>
