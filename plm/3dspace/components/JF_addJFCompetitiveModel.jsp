<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<!--
创建竞品车型，根据选择的是品牌还是零件关联关系
-->
<%@include file="../emxUIFramesetUtil.inc"%>
<script>
    function openwindow(url,name,iWidth,iHeight)
    {
        var url; //转向网页的地址;
        var name; //网页名称，可为空;
        var iWidth; //弹出窗口的宽度;
        var iHeight; //弹出窗口的高度;
        var iTop = (window.screen.availHeight-30-iHeight)/2; //获得窗口的垂直位置;
        var iLeft = (window.screen.availWidth-10-iWidth)/2; //获得窗口的水平位置;
        window.open(url,name,'height='+iHeight+',,innerHeight='+iHeight+',width='+iWidth+',innerWidth='+iWidth+',top='+iTop+',left='+iLeft+',toolbar=no,menubar=no,scrollbars=auto,resizeable=no,location=no,status=no');
    }
</script>
<%
    String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
    String mode = emxGetParameter(request, "mode");
    if(tableRowIdList==null){
        %>
<script>
    //刷新table
    var refreshURL = window.parent.location.href;
    window.parent.location.href = refreshURL;
</script>
<%
    }else{
        String tableRowId = tableRowIdList[0];
        StringList tableIds = FrameworkUtil.split(tableRowId,"|");
        String objId = tableIds.get(1);
        if(UIUtil.isNullOrEmpty(objId)){
            objId = tableIds.get(0);
        }
        System.out.println("objId=="+objId);
        DomainObject obj = DomainObject.newInstance(context,objId);
        String type = obj.getInfo(context,"type");
        if("addJFCompetitiveModel".equals(mode)){
            //车型
            if(!"JFCompetitiveBrand".equals(type)){
                String msg = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.CompetitiveMode.CreateError");
            //报错 选择的不是品牌
            %>
<script>
    alert("<%=msg%>");
</script>
<%
}else{
    StringBuffer url = new StringBuffer("../common/emxCreate.jsp?type=type_JFCompetitiveModel&policy=policy_JFCompetitive&form=JFCompetitiveModelForm&mode=create&nameField=autoname&submitAction=refreshCaller&relationship=relationship_JFModel2CompetitiveBOM");
    url.append("&objectId="+objId);
%>
<script>
    var url = "<%=url%>"
    openwindow(url,"",800,600);
</script>
<%
            }
        }else{
            if("JFCompetitiveBrand".equals(type)){
                String msg = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.CompetitiveBom.CreateError");
                %>
<script>
    alert("<%=msg%>");
</script>
<%
    }else{
    StringBuffer url = new StringBuffer("../common/emxCreate.jsp?type=type_JF_CompetitiveBOM&policy=policy_VPLM_SMB_Definition_MajorRev&form=JFCompetitiveBomForm&mode=create&nameField=autoname&submitAction=refreshCaller&createJPO=JF_CompetitiveBOM:createCompetitiveBOM&header=emxComponents.Command.addJFCompetitiveBomCMd&suiteKey=Components&StringResourceFileId=emxComponentsStringResource&SuiteDirectory=components");
    url.append("&objectId="+objId);
    if("JFCompetitiveModel".equals(type)){
        url.append("&relationship=relationship_JFModel2CompetitiveBOM");
    }else {
        //零件
        url.append("&relationship=relationship_VPMInstance");
    }
%>
<script>
    var url = "<%=url%>"
    openwindow(url,"",800,600);

</script>
<%
            }
        }
    }
%>