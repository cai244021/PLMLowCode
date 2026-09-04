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
    final String RELATIONSHIP_JFDR2VPMREFERENCE = "JFDR2VPMReference";
    final String SUITE_KEY = "emxComponentsStringResource";
    String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
    String partId = "";
    StringBuffer connectPartIds = new StringBuffer(); //需要关联的零件
    StringBuffer msg = new StringBuffer();
    StringList notInWorkStateList = new StringList();
    StringList typeList = new StringList();
    typeList.add(DomainConstants.SELECT_NAME);
    typeList.add(DomainConstants.SELECT_CURRENT);
    boolean isAlert = false;
    for (String tableRowIds : tableRowIdList) {
        System.out.println("tableRowIds="+tableRowIds);
        StringList stringList = FrameworkUtil.splitString(tableRowIds,"|");
//        if(tableRowIds.startsWith("|")){
//            partId = stringList.get(0);
//        }else{
//            partId = stringList.get(1);
//        }
        partId = stringList.get(1);
        System.out.println("partId="+partId);
        DomainObject part = DomainObject.newInstance(context,partId);
        //判断零件状态是否是工作中
        Map info = part.getInfo(context,typeList);
        String strPartCurrent = (String) info.get(DomainConstants.SELECT_CURRENT);
        String strPartName = (String) info.get(DomainConstants.SELECT_NAME);
        if (!"IN_WORK".equals(strPartCurrent)){
            notInWorkStateList.add(strPartName);
            isAlert = true;
        }
        StringList drList = part.getInfoList(context,"to["+RELATIONSHIP_JFDR2VPMREFERENCE+"].from.id");
        if(!drList.isEmpty()) {
            msg.append(",").append(strPartName);
            continue;
        }
        connectPartIds.append(partId).append(",");
    }
    String alertmsg = "";
    if(!msg.isEmpty()){
        alertmsg = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.DR.connectDRError");
        alertmsg = alertmsg.replace("{}",msg.substring(1));
    }
    if (isAlert){
        //如果提示信息不为空，需要拼接分号
        if (UIUtil.isNotNullAndNotEmpty(alertmsg)){
            StringBuffer sb = new StringBuffer(alertmsg);
            sb.append(";");
            String strStateErrorMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.MESS.DRStateError");
            strStateErrorMess = strStateErrorMess.replace("{}",notInWorkStateList.join(","));
            sb.append(strStateErrorMess);
            alertmsg = sb.toString();
        }else {
            alertmsg = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.MESS.DRStateError");
            alertmsg = alertmsg.replace("{}",notInWorkStateList.join(","));
        }
    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    var msg = "<%=alertmsg%>";
    if (msg.length>0){
        alert(msg.substring(1));
    }else{
        var connectIds = "<%=connectPartIds.toString()%>";
        var url = "../common/emxCreate.jsp?type=type_JFDR&policy=policy_JFDR&form=JFDRForm&header=emxComponents.Command.JFCreateDRCmd&suiteKey=Components&StringResourceFileId=emxComponentsStringResource&SuiteDirectory=components&HelpMarker=emxhelpprogramcreatedialog&autoNameChecked=true&submitAction=treePopup&nameField=autoName&createJPO=JF_DR:createDR&postProcessJPO=JF_DR:connectRel2Person&connectIds="+connectIds+"";
        window.open(url,'_blank',"width=800,height=600");

    }
    // var refreshURL = window.parent.location.href;
    // window.parent.location.href = refreshURL;
</script>
<%
    }catch (Exception e){
        e.printStackTrace();
    }
%>