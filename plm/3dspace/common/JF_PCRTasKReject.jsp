<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_PCRTasKPromote.jsp");
%>
<%
    String suiteKey = "emxComponentsStringResource";
    String RELATIONSHIP_REFERENCE_DOCUMENT = "Reference Document";
    StringBuffer stringBuffer = new StringBuffer();
    StringList bosel = new StringList();
    bosel.add(DomainObject.SELECT_ID);
    bosel.add(DomainObject.SELECT_NAME);
    bosel.add(DomainObject.SELECT_CURRENT);
    String alertResult = "";
    Boolean flag = false;
    try {
        String owner = context.getUser();
        ContextUtil.pushContext(context);
        String objectId = emxGetParameter(request, "objectId");//PCR
        String[] tableRowId = emxGetParameterValues(request, "emxTableRowIdActual");
        String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
        LOGGER.info("splitTableRowIds:{} objectId：{}",splitTableRowIds,objectId);
        String rejectDescription = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.PCR.rejectNullMessage");
        StringBuffer sb=new StringBuffer();
        for(String rowid:splitTableRowIds){
            sb.append(rowid).append("@@");
        }

        //先判断如果驳回的情况需要把选中的评估任务添加驳回意见
        for(int i=0;i<splitTableRowIds.length;i++){
            LOGGER.info("rowID:{}",splitTableRowIds[i]);
            String description = DomainObject.newInstance(context,splitTableRowIds[i]).getDescription(context);
            if(UIUtil.isNullOrEmpty(description)){
                flag = true;
                alertResult=rejectDescription;
                break;
            }
        }
        //点击同意后提升当前任务
        if(!flag) {
            JPO.invoke(context, "JF_PCR", new String[0], "rejectPCRTask", new String[]{objectId, owner, sb.toString()}, void.class);
        }
    }catch (Exception e) {
        e.printStackTrace();
    }finally {
        ContextUtil.popContext(context);
    }

%>
<html>
<script >
    if(<%=flag%>) {
        alert("<%=alertResult%>");
    }else {
        window.parent.location.href=window.parent.location.href
    }

</script>
</html>
