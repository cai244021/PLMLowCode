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
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_PCRPerformTasKPromote.jsp");
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
    Boolean flag = true;
    try {
        String owner = context.getUser();
        ContextUtil.pushContext(context);
        String objectId = emxGetParameter(request, "objectId");//PCR
        String[] tableRowId = emxGetParameterValues(request, "emxTableRowIdActual");
        String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
        LOGGER.info("splitTableRowIds:{} objectId：{}",splitTableRowIds,objectId);
        DomainObject taskObj = DomainObject.newInstance(context);
        String isNullMessage = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.PCR.PerformTasknoticeNullMessage");
        String DocisNullMessage = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.PCR.PerformTasknoticeDocNullMessage");
        String onlyowner = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.PCR.onlyOwner");
        //查询当前PCR下面的执行任务，条件的owner是当前登录人
        DomainObject pcrObj = DomainObject.newInstance(context,objectId);
        for(int i=0;i<splitTableRowIds.length;i++){
            taskObj.setId(splitTableRowIds[i]);
            String endDate = taskObj.getAttributeValue(context, "JF_PCRActualFinishTime");
            String taskowner  = taskObj.getInfo(context,DomainConstants.SELECT_OWNER);
            LOGGER.info("taskowner:{} owner:{}",taskowner,owner);
            if(owner.equalsIgnoreCase(taskowner)) {
                if (UIUtil.isNullOrEmpty(endDate) ) {
                    stringBuffer.append(isNullMessage);
                }
                StringList docidlist=taskObj.getInfoList(context,"from[Reference Document].to.id");
                if(docidlist.size()==0){
                    //没有附件
                    stringBuffer.append(DocisNullMessage);
                }

            }else{
                stringBuffer = new StringBuffer();
                stringBuffer.append(onlyowner);
                break;
            }
        }
        LOGGER.info("stringBuffer:{}",stringBuffer);
        if(stringBuffer.length() == 0){
            flag = false;
//            taskObj.promote(context);
            //设置状态未审核中，如果是PCR评估类型
            for(int i=0;i<splitTableRowIds.length;i++) {
//                if ("JF_PCRTask".equalsIgnoreCase(type)) {
                    MqlUtil.mqlCommand(context, false, "mod bus " + splitTableRowIds[i] + " current Review", true);
                    JPO.invoke(context, "JF_PCR", new String[0], "setDocStateForzen", new String[]{splitTableRowIds[i]}, void.class);
                    //调用下方法判断下是否是最后一个评估任务,如果是最后一个执行任务，就生成执行流程
                 JPO.invoke(context, "JF_PCR", new String[0], "getActivePerformTask", new String[]{objectId}, void.class);
//                }
            }
        }
        alertResult = stringBuffer.toString();
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
