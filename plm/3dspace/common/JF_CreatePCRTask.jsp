<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="java.util.*" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_CreatePCRTask.jsp");
%>
<%
    try {
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        String strObjectId = emxGetParameter(request, "parentOID");
//        String strObjectId = emxGetParameter(request, "parentOID");
        /*for (int i = 0; i < strSelectIds.length; i++) {
            String id = strSelectIds[i].split("\\|")[1];
            list.add(id);
            if (i == strSelectIds.length-1) list.add(strSelectIds[i].split("\\|")[2]);
        }*/
        String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(strSelectIds);
        LOGGER.info("list:{} strObjectId:{}",splitTableRowIds,strObjectId);
        Map requestMap = new HashMap();
        requestMap.put("objectId",strObjectId);
        requestMap.put("list",splitTableRowIds);
        JPO.invoke(context,"JF_PCR",null,"createPCRExecuteTask", JPO.packArgs(requestMap));
    }catch (Exception e) {
        e.printStackTrace();
    }

%>
<html>
<script>
    window.top.getWindowOpener().top.refreshTablePage();
    parent.top.close();
</script>
</html>
