<%--<!DOCTYPE html>--%>
<%--<html>--%>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
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
    private static final Logger _logger =  LoggerFactory.getLogger("JF_PMCWBSAssignSelected.jsp");
%>
<%
    //?? oid
    String strObjectId = emxGetParameter(request, "objectId");
    String[] tableRowId = emxGetParameterValues(request, "emxTableRowId");
    _logger.info("JF_PMCWBSAssignSelected.jsp======tableRowId:{}", Arrays.asList(tableRowId));
    String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
    String ids = String.join(",", splitTableRowIds);
    StringBuffer sbUrl = new StringBuffer("../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=policy_Person.state_Active&showInitialResults=true&table=AEFGeneralSearchResults&selection=multiple");
    sbUrl.append("&objectId=");
    sbUrl.append(strObjectId);
    sbUrl.append("&ids=");
    sbUrl.append(ids);
    sbUrl.append("&hideHeader=true&cancelLabel=emxFramework.Command.Cancel");
    sbUrl.append("&submitURL=../common/JF_PMCWBSAssignSelectedPostProcess.jsp");


%>
<script>
    var strUrl = "<%=sbUrl%>";
    showWizard(strUrl);
</script>
<%--MQL??--%>
<jsp:include  page="../components/emxMQLNotice.jsp" flush="true"/>
</html>