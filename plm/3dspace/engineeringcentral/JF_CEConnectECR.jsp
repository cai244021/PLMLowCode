<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<!--
CE添加ECR
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%
    try {
        String strObjectId = emxGetParameter(request, "objectId");
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        DomainObject dr = DomainObject.newInstance(context, strObjectId);
        for (String tableRowId : tableRowIdList) {
            StringList tableIds = FrameworkUtil.split(tableRowId,"|");
            dr.addToObject(context, new RelationshipType("JFChangeEventECR"), tableIds.get(0));
        }
    }catch (Exception e){
        e.printStackTrace();
    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    var refreshURL = getTopWindow().getWindowOpener().location.href;
    getTopWindow().getWindowOpener().location.href = refreshURL;
    getTopWindow().closeWindow();
</script>