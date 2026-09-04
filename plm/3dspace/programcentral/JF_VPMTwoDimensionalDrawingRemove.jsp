<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.apache.batik.gvt.filter.BackgroundRable8Bit" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="java.util.Arrays" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_VPMTwoDimensionalDrawingRemove.jsp");
%>

<%
    String strMess = DomainConstants.EMPTY_STRING;
    String partialXML = "";
    String xmlMessage = "<mxRoot>";
    String[] tableRowId = emxGetParameterValues(request, "emxTableRowIdActual");
    LOGGER.info("JF_VPMTwoDimensionalDrawingRemove.jsp======tableRowId:{}", Arrays.asList(tableRowId));
    String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
    LOGGER.info("JF_VPMTwoDimensionalDrawingRemove.jsp======splitTableRowIds:{}",Arrays.asList(splitTableRowIds));
    StringList strings = new StringList();
    try {
     /*   for (String s : splitTableRowIds) {
            DomainObject domainObject = DomainObject.newInstance(context, s);
            String current  = domainObject.getInfo(context,"current");
            if ("FROZEN".equals(current) || "RELEASED".equals(current)){
                strMess="冻结和发布状态不可移除";
                break;
            }
        }*/
        if (UIUtil.isNullOrEmpty(strMess)){
            for (String s : tableRowId) {
                Map mParsedRowId = ProgramCentralUtil.parseTableRowId(context,s);
                LOGGER.info("JF_VPMTwoDimensionalDrawingRemove.jsp======mParsedRowId:{}",mParsedRowId);
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
                LOGGER.info("JF_VPMTwoDimensionalDrawingRemove.jsp======sTempObjectId:{}",sTempObjectId);
                LOGGER.info("JF_VPMTwoDimensionalDrawingRemove.jsp======sRelId:{}",sRelId);
                LOGGER.info("JF_VPMTwoDimensionalDrawingRemove.jsp======rowId:{}",rowId);
//                DomainObject domainObject = DomainObject.newInstance(context, sTempObjectId);
//                String type  = domainObject.getInfo(context,"type");
//                if ("Document".equals(type)){
//                    strings.add(sRelId);
//                }
                strings.add(sRelId);
            }

            if (strings.size() > 0){
//                DomainObject.deleteObjects(context,strings.toStringArray());
                DomainRelationship.disconnect(context,strings.toStringArray());
                LOGGER.info("JF_VPMTwoDimensionalDrawingRemove.jsp======移除成功");
            }
        }

        String message = "";
        xmlMessage += "<action refresh=\"true\" fromRMB=\"\"><![CDATA[remove]]></action>";
        xmlMessage += partialXML;
        xmlMessage += "<message><![CDATA[" + message + "]]></message>";
        xmlMessage += "</mxRoot>";
    }catch (Exception e) {
        strMess = e.getMessage();
        LOGGER.info("JF_VPMTwoDimensionalDrawingRemove.jsp======error",e);
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
