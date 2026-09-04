<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%
    try{
        final String SUITE_KEY = "emxComponentsStringResource";
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        StringList notColorPartList = new StringList();
        StringList tableRowParamsIds = new StringList();
        StringList typeList = new StringList();
        typeList.add(DomainConstants.SELECT_NAME);
        typeList.add(DomainConstants.SELECT_CURRENT);
        String isColorPart = "attribute[JF_VPMReference.JF_ColorPart].value";
        String partNum = "attribute[EnterpriseExtension.V_PartNumber].value";
        typeList.add(isColorPart);
        typeList.add(partNum);
        boolean isAlert = false;
        for (int i = 0; i < tableRowIdList.length; i++) {
            String tableRowIds = tableRowIdList[i];
            StringList stringList = FrameworkUtil.splitString(tableRowIds,"|");
            String partId = stringList.get(1);
            String relId = stringList.get(0);
            tableRowParamsIds.add(relId + ";" + partId);
            DomainObject part = DomainObject.newInstance(context,partId);
            //判断是否是颜色件
            Map info = part.getInfo(context,typeList);
            String strPartNum = (String) info.get(partNum);
            String colorPart = (String) info.get(isColorPart);
            if ("N".equals(colorPart)){
                //不是颜色件 不能创建颜色定义
                notColorPartList.add(strPartNum);
                isAlert = true;
                continue;
            }
        }
        String alertmsg = "";
        if (isAlert){
            //选择错误  选择零件中不是颜色件的是
            String strStateErrorMess = EnoviaResourceBundle.getProperty(context, SUITE_KEY, context.getLocale(), "emxComponents.ColorPart.SelectError");
            alertmsg = strStateErrorMess + notColorPartList.join(",");
        }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    var msg = "<%=alertmsg%>";
    if (msg.length>0){
        alert(msg.substring(1));
    }else{
        var tableRowParamsIds = "<%=tableRowParamsIds.join(",")%>";
        var url = "../common/emxCreate.jsp?nameField=both&autoNameChecked=true&type=type_JFColorPart&policy=policy_JFColorPart&form=JFColorPartForm&submitAction=refreshCaller&mode=create&createJPO=JF_ColorPart:createJFColorPartAddRel&postProcessURL=./JF_TrimCreateProcess.jsp&header=emxFramework.JFDefineColor.CreateNew&showApply=true&tableRowParamsIds="+tableRowParamsIds;
        window.open(url,'_blank',"width=800,height=600");
    }
    // var refreshURL = window.parent.location.href;
    // window.parent.location.href = refreshURL;
</script>
<%
    }catch (Exception e){

    }
%>