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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_SelectFunctionCheckin.jsp");
%>
<%
    String strMess = DomainConstants.EMPTY_STRING;
    String[] tableRowIds = emxGetParameterValues(request, "emxTableRowId");
    StringList tableRowIdList = StringList.create(tableRowIds);
    String typeWarn = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.TypeError.Message", new String[]{});
    StringList costReferConstList = new StringList();
    StringList errorList = new StringList();

    for(int i=0; i < tableRowIdList.size(); i++)
    {
        StringList sList = FrameworkUtil.split(tableRowIdList.get(i),"|");
        if(sList.size() == 4) {
            String id = sList.get(1);
            DomainObject domainObject = DomainObject.newInstance(context, id);
            String type = domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
            if ("JFCostReferConst".equalsIgnoreCase(type)) {
                costReferConstList.add(sList.get(1));
            } else {
                errorList.add(domainObject.getInfo(context, DomainConstants.SELECT_NAME));
            }
        }
    }
    StringBuffer sbUrl = new StringBuffer();
    if (errorList.size() > 0) {
        strMess = typeWarn + errorList.join(",");
    } else {
        if (UIUtil.isNullOrEmpty(strMess)) {
            //emxIndentedTable   emxTable
            sbUrl = new StringBuffer("../common/emxIndentedTable.jsp?table=JFSelectFunctionModuleTemplate&program=JF_Cost:getSelectFunctionModuleTemplate");
            sbUrl.append("&selection=multiple&sortColumnName=Name&sortDirection=ascending");
            sbUrl.append("&header=emxFramework.Common.SelectFunction");
            sbUrl.append("&submitLabel=emxFramework.Common.ok&cancelButton=emxFramework.Button.Cancel&cancelLabel=emxFramework.Command.JFCostQuotationCancel");
            sbUrl.append("&submitAction=refreshCaller");
            sbUrl.append("&");
            if (costReferConstList.size() > 0) {
                sbUrl.append("ids=");
                sbUrl.append(costReferConstList.join(","));
            }
            sbUrl.append("&submitURL=../components/JF_SelectFunctionSubmit.jsp?");
            JF_LOGGER.info("tableRowIdList:{}", tableRowIdList.toString());
            if (costReferConstList.size() > 0) {
                sbUrl.append("ids=");
                sbUrl.append(costReferConstList.join(","));
            }
        }
    }
%>
<html>
<script>
    const mess = "<%=strMess%>";
    if (mess.length > 0) {
        alert(mess);
    } else {
        showModalDialog("<%=sbUrl%>");
    }

</script>
</html>
