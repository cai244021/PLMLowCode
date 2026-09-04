<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.Job" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.db.Access" %>
<!--
dr移除关联的物理产品
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%
    StringBuffer msg = new StringBuffer();
    String taskErrorMsg = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.removeCompetitiveMsg");
    String strNotDelAccess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.notDelAccess");

    try {
        String strObjectId = emxGetParameter(request, "objectId");
        String emxTableRowId = emxGetParameter(request, "emxTableRowId");
        StringList tableIds = FrameworkUtil.split(emxTableRowId,"|");
        DomainObject object = new DomainObject();
        if(emxTableRowId.startsWith("|")){
            object.setId(tableIds.get(0));
        }else {
            object.setId(tableIds.get(1));
        }
        String type = object.getInfo(context,"type");
        String relationship = "JFModel2CompetitiveBOM";
        if("JF_CompetitiveBOM".equals(type)){
            relationship = "VPMInstance";
        }
        StringList child = object.getInfoList(context,"from["+relationship+"].to.name");
        if(child.isEmpty()){
            //移除
            String relId = "";
            if("JF_CompetitiveBOM".equals(type)){
                //两条关系JFModel2CompetitiveBOM和VPMInstance都有可能
                relId = UIUtil.isNullOrEmpty(object.getInfo(context,"to[VPMInstance].id"))?object.getInfo(context,"to[VPMInstance].id"):object.getInfo(context,"to[JFModel2CompetitiveBOM].id");
            }else{
                relId = object.getInfo(context,"to["+relationship+"].id");
            }

            if(UIUtil.isNotNullAndNotEmpty(relId)) {
                DomainRelationship.disconnect(context, relId);
            }
            // add by chenyan 新增竞品删除逻辑
            Access objAccess = object.getAccessMask(context);
            if (objAccess.hasDeleteAccess()) {
                object.delete(context);
            } else {
%>
                <script>
                    alert("<%=strNotDelAccess%>");
                </script>
<%
            }
%>
<script>
    var refreshURL = window.parent.location.href;
    window.parent.location.href = refreshURL;
</script>
<%
        }else{
            taskErrorMsg = taskErrorMsg.replace("{}",object.getInfo(context,"name"));
%>
<script>
    alert("<%=taskErrorMsg%>");
</script>

<%
        }
    }catch (Exception e){
        e.printStackTrace();
    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
