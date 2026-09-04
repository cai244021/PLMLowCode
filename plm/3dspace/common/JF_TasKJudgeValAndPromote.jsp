<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    String suiteKey = "emxComponentsStringResource";
    String RELATIONSHIP_REFERENCE_DOCUMENT = "Reference Document";
    StringBuffer stringBuffer = new StringBuffer();
    StringList bosel = new StringList();
    bosel.add(DomainObject.SELECT_ID);
    bosel.add(DomainObject.SELECT_NAME);
    bosel.add(DomainObject.SELECT_CURRENT);
    String alertMess = "";
    String alertDocMess = "";
    String alertResult = "";
    String TaskReviewing = "";
    Boolean flag = true;
    String whetherToPostpone="";
    try {
        String objectId = emxGetParameter(request, "objectId");
        DomainObject taskObj = DomainObject.newInstance(context, objectId);
        String taskType = taskObj.getInfo(context, DomainConstants.SELECT_TYPE);
        if("JF_ECOTask".equalsIgnoreCase(taskType)  ) {
            whetherToPostpone = taskObj.getAttributeValue(context, "JF_ECPwhetherToPostpone");
        }else if("JF_DATask".equalsIgnoreCase(taskType)){
             whetherToPostpone = taskObj.getAttributeValue(context,"JF_DAwhetherToPostpone");
        }
        String value = taskObj.getAttributeValue(context, "JF_DAActualFinishTime");
        MapList mapList = taskObj.getRelatedObjects(context, RELATIONSHIP_REFERENCE_DOCUMENT, "Document", bosel, null, false, true, (short) 1, "", "", 0);
        alertMess = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.action.noticeDAActualTime");
        TaskReviewing = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.action.TaskReviewing");
        alertDocMess = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.action.noticeDATaskDoc");
        ContextUtil.pushContext(context);
        if("".equals(value)){
            stringBuffer.append(alertMess).append(" ");
        }
        if("Process".equalsIgnoreCase(whetherToPostpone)){
            if(stringBuffer.length() != 0){
                stringBuffer.append("\\n");
            }
            stringBuffer.append(TaskReviewing);
        }
        if(mapList.isEmpty()){
            if(stringBuffer.length() != 0){
                stringBuffer.append("\\n");
            }
            stringBuffer.append(alertDocMess);
        }

        alertResult = stringBuffer.toString();

        if(stringBuffer.length() == 0){
            flag = false;
//            taskObj.promote(context);
            //设置状态未审核中，如果是DA类型
            String type = taskObj.getInfo(context, DomainConstants.SELECT_TYPE);
            if("JF_DATask".equalsIgnoreCase(type) || "JF_ECOTask".equalsIgnoreCase(type)){
                MqlUtil.mqlCommand(context,false,"mod bus "+objectId+" current Review", true);
                JPO.invoke(context, "JF_ChangeExecutionECOSource", new String[0], "setJF_TaskFileToPromote", new String[]{objectId,"FROZEN"}, void.class);
            }else{
                taskObj.promote(context);
            }
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
