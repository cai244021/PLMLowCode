<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%
    try{
    final String RELATIONSHIP_AssignedTasks = "to[Assigned Tasks].from.name";
    final String SUITE_KEY = "emxComponentsStringResource";
    String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
    String partId = "";
    StringBuffer msg = new StringBuffer();
    String contextUser = context.getUser();
    for (String tableRowIds : tableRowIdList) {
        System.out.println("tableRowIds=" + tableRowIds);
        StringList stringList = FrameworkUtil.splitString(tableRowIds, "|");
        partId = stringList.get(1);
        System.out.println("partId=" + partId);
        DomainObject part = DomainObject.newInstance(context, partId);
        //判断是否选中的责任人是不是Owner自己
        StringList assignOwners = part.getInfoList(context, RELATIONSHIP_AssignedTasks);
        if (!assignOwners.contains(contextUser)) {
            msg.append(EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DelayRequest.error"));
        }
        //20260828 update by caipan 上一次延期流程完成后允许再次发起
        StringList delayFilingStates = part.getInfoList(context, "to[JFDelayedFiling2Task].from.current");
        boolean existUnfinishedDelay = false;
        for (String delayFilingState : delayFilingStates) {
            if (!"Complete".equalsIgnoreCase(delayFilingState)) {
                existUnfinishedDelay = true;
                break;
            }
        }
        if (existUnfinishedDelay) {
            if(!msg.isEmpty()){
                msg.append("\n");
            }
            msg.append(EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DelayRequest.repetition"));
        }
        //判断选中的任务状态，必须为工作中才可以点击延期申请流程
        String current = part.getInfo(context,"current");
        if(!"Active".equalsIgnoreCase(current)){
            if(!msg.isEmpty()){
                msg.append("\n");
            }
            msg.append(EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DelayRequest.stateError"));
        }
    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    var msg = "<%=msg%>";
    var partId = "<%=partId%>";
    if (msg.length>0){
        alert(msg);
        window.close();
    }else{
        var url = "../common/emxCreate.jsp?nameField=autoName&autoNameChecked=true&type=type_JFDelayedFiling&policy=policy_JFDelayedFiling&form=JFECOExecutionPlanDelayRequestForm&mode=create&postProcessJPO=JF_ChangeExecutionECOSource:createECOExecutionPlanDelayFiling&submitAction=treePopup&header=emxFramework.Command.JFECOExecutionPlanDelayRequest&parentId="+partId;
        // window.close();
        window.location.href = url;
        // window.open(url,'_blank',"width=800,height=600");
    }
</script>
<%
    }catch (Exception e){
        e.printStackTrace();
    }
%>