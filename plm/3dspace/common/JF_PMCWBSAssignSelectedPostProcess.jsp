<%--<!DOCTYPE html>--%>
<%--<html>--%>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.*" %>
<%@ page pageEncoding="utf-8" %>
<
<%--????--%>
<%@include file = "emxNavigatorInclude.inc"%>
<script src="./scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="./scripts/emxUICore.js"></script>
<script language="JavaScript" src="./scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="./scripts/emxUISearch.js"></script>
<script language="JavaScript" src="../webapps\ENOAEFCore\webroot\common\scripts/bpsTagNavSBInit.js"></script>

<%!
    //??
    private static final Logger _logger =  LoggerFactory.getLogger("JF_PMCWBSAssignSelectedPostProcess.jsp");
%>
<%
    String mess = "";
    String user = context.getUser();
    _logger.info("JF_PMCWBSAssignSelectedPostProcess.jsp======user:{}", user);
    String strObjectId = emxGetParameter(request, "objectId");
    String taskIds = emxGetParameter(request, "ids");
    _logger.info("JF_PMCWBSAssignSelectedPostProcess.jsp======taskIds:{}", taskIds);
    String[] tableRowId = emxGetParameterValues(request, "emxTableRowId");
    _logger.info("JF_PMCWBSAssignSelectedPostProcess.jsp======tableRowId:{}", Arrays.asList(tableRowId));
    String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
    String personIds = String.join(",", splitTableRowIds);
    String[] splitTaskIds = taskIds.split(",");
    for (String splitTaskId : splitTaskIds) {
        DomainObject taskObj = DomainObject.newInstance(context,splitTaskId);
        StringList assignPersons = taskObj.getInfoList(context,"to[Assigned Tasks].from.name");
        String taskOwner = taskObj.getInfo(context,"owner");
        if (assignPersons.contains(user)|| Objects.equals(taskOwner,user)){
            mess = "success";
        }else {
            mess = "当前登录人不是任务的owner或者被分派人,不允许添加";
            break;
        }
    }

    if ("success".equals(mess)){
        Map pack2Map = new HashMap<>();
        pack2Map.put("taskIds", taskIds);
        pack2Map.put("personIds", personIds);
        mess = JPO.invoke(context, "JF_ESO", null, "addAssignPerson", JPO.packArgs(pack2Map), String.class);
    }
    _logger.info("JF_PMCWBSAssignSelectedPostProcess.jsp======mess2:{}", mess);



%>
<script>
    const mess = "<%=mess%>";
    alert(mess);
    getTopWindow().closeWindow();
    // window.top.getWindowOpener().top.refreshTablePage();
</script>
<%--MQL??--%>
<jsp:include  page="../components/emxMQLNotice.jsp" flush="true"/>
</html>