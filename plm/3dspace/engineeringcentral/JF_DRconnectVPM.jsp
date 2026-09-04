<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<!--
    DR添加物理产品
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%
    try {
        String ATTRIBUTE_JFISFOLLOW = "JFIsFollow";
        String strObjectId = emxGetParameter(request, "objectId");
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        DomainObject dr = DomainObject.newInstance(context, strObjectId);
        for (String tableRowId : tableRowIdList) {
            StringList tableIds = FrameworkUtil.split(tableRowId,"|");
            DomainRelationship connection = dr.addToObject(context, new RelationshipType("JFDR2VPMReference"), tableIds.get(0));
            connection.setAttributeValue(context,ATTRIBUTE_JFISFOLLOW,"Y");
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
