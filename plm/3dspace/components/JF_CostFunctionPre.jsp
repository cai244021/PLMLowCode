<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.SELECT_ORIGINATED" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.context1" %>
<%@ page import="java.util.Enumeration" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_CostFunctionPre.jsp");
%>
<%
    String strMess = DomainConstants.EMPTY_STRING;
    String objectId = emxGetParameter(request, "objectId");
    String[] tableRowIds = emxGetParameterValues(request, "emxTableRowId");
   String selectId = tableRowIds[0];
    StringList selList = FrameworkUtil.split(selectId, "|");
    selectId = selList.get(0);
    StringBuffer sbUrl = new StringBuffer();
    if (UIUtil.isNullOrEmpty(strMess)) {
        sbUrl = new StringBuffer("../common/emxTable.jsp?table=JFInterfaceList&program=JF_Cost:getCostInterface&");
        sbUrl.append("&selection=single&sortColumnName=Name&sortDirection=ascending");
        sbUrl.append("&showApply=true");
        sbUrl.append("&SubmitLabel=emxFramework.Common.ok&CancelLabel=emxFramework.Button.Cancel&CancelButton=true");
        sbUrl.append("&submitAction=refreshCaller&SubmitURL=../components/JF_CostFunctionLibSubmit.jsp?selectId="+selectId);
        JF_LOGGER.info("tableRowIdList:{}", sbUrl);
    }
%>
<html>
<script>
    let sbUrl = "<%=sbUrl%>";
        window.open(sbUrl, '', 'width=700,height=600,left=300,top=350,scrollbars=no,location=no,fullscreen=no', 'false');
</script>
</html>
