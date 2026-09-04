<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.apache.batik.gvt.filter.BackgroundRable8Bit" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Objects" %>
<%@ page import="com.matrixone.apps.domain.util.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_PMCAssigneeDelete.jsp");
%>

<%
    String strMess = DomainConstants.EMPTY_STRING;
    String partialXML = "";
    String xmlMessage = "<mxRoot>";
    String strObjectId = emxGetParameter(request, "objectId");
    String user = context.getUser();
    String[] tableRowId = emxGetParameterValues(request, "emxTableRowIdActual");
    LOGGER.info("JF_PMCAssigneeDelete.jsp======tableRowId:{}", Arrays.asList(tableRowId));
    String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
    LOGGER.info("JF_PMCAssigneeDelete.jsp======splitTableRowIds:{}",Arrays.asList(splitTableRowIds));
    StringList strings = new StringList();
    try {

        DomainObject taskObj = DomainObject.newInstance(context,strObjectId);
        String taskOwner = taskObj.getInfo(context,"owner");
        ContextUtil.pushContext(context,taskOwner,"","");
        StringList nameList = taskObj.getInfoList(context, "to[Assigned Tasks].from.name");
        boolean isAuthorized = Objects.equals(user, taskOwner) || nameList.contains(user);
        if (!isAuthorized) {
            strMess = "只有任务的owner或者分派人才能移除";
        }
        if (UIUtil.isNullOrEmpty(strMess)){
            for (String s : tableRowId) {
                Map mParsedRowId = ProgramCentralUtil.parseTableRowId(context,s);
                LOGGER.info("JF_PMCAssigneeDelete.jsp======mParsedRowId:{}",mParsedRowId);
                String sTempObjectId = null;
                String sRelId = null;
                String sTempRowId = null;
                String rowId = null;
                if(null != mParsedRowId) {
                    sTempObjectId = (String)mParsedRowId.get("objectId");
                    sRelId = (String)mParsedRowId.get("relId");
                    sTempRowId = (String)mParsedRowId.get("rowId");
                    rowId = sTempRowId;
                }
                if(ProgramCentralUtil.isNotNullString(rowId)) {
                    partialXML += "<item id=\"" + rowId + "\" />";
                }
                LOGGER.info("JF_PMCAssigneeDelete.jsp======sTempObjectId:{}",sTempObjectId);
                LOGGER.info("JF_PMCAssigneeDelete.jsp======sRelId:{}",sRelId);
                LOGGER.info("JF_PMCAssigneeDelete.jsp======rowId:{}",rowId);
                strings.add(sRelId);
            }

            if (strings.size() > 0){
                DomainRelationship.disconnect(context,strings.toStringArray());
                LOGGER.info("JF_PMCAssigneeDelete.jsp======移除成功");
            }

        }

        String message = "";
        xmlMessage += "<action refresh=\"true\" fromRMB=\"\"><![CDATA[remove]]></action>";
        xmlMessage += partialXML;
        xmlMessage += "<message><![CDATA[" + message + "]]></message>";
        xmlMessage += "</mxRoot>";
    }catch (Exception e) {
        strMess = e.getMessage();
        LOGGER.info("JF_PMCAssigneeDelete.jsp======error",e);
    }finally {
        ContextUtil.popContext(context);
    }

%>
<html>
<script>
    var strMess = "<%=strMess%>";
    if (strMess !== "" && strMess != null){
        alert("<%=strMess%>");
    }else {
        // var refreshURL = window.parent.location.href;
        // window.parent.location.href = refreshURL;
        window.parent.removedeletedRows('<%=xmlMessage%>');
        window.parent.emxEditableTable.refreshStructureWithOutSort();
        window.parent.getTopWindow().RefreshHeader();
    }
</script>
</html>
