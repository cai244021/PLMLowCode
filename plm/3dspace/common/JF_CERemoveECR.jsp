<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<!--
ce删除ECR
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%
    String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
    for (String tableRowId : tableRowIdList) {
        StringList tableIds = FrameworkUtil.split(tableRowId,"|");
        String id =  tableIds.get(0);
        DomainObject domainObject = DomainObject.newInstance(context, id);
        String relId = domainObject.getInfo(context, "relationship[JFChangeEventECR].id");
        DomainRelationship.disconnect(context, relId);
    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    var refreshURL = window.parent.location.href;
    window.parent.location.href = refreshURL;
    console.log('refreshURL='+refreshURL);
</script>