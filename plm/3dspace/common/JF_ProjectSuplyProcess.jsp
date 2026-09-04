<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<!--
ce删除ECR
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%
    String strObjectId = emxGetParameter(request, "objectId");
    String supplyValue = emxGetParameter(request, "JFSupplyInputContainsCmd");
    try {
        DomainObject project = DomainObject.newInstance(context, strObjectId);
        ContextUtil.pushContext(context);
        project.setAttributeValue(context, "JF_SupplyAffirm", "Y");

    }catch (Exception e){
            e.printStackTrace();
    }finally {
        ContextUtil.popContext(context);
    }
    //emxFramework.Label.JFSupplySubmitMessage
    String message= (String) EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Label.JFSupplySubmitMessage", context.getLocale());

%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<html>
<script >
 alert("<%=message%>")
 parent.location.href =parent.location.href;
</script>
</html>