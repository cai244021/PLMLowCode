<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.Map"%>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
    for (int i = 0; i < tableRowIdList.length; i++) {
        StringList tableRowId = FrameworkUtil.split(tableRowIdList[i],"|");
        String relId = tableRowId.get(0);
        DomainRelationship.disconnect(context,relId);
    }
%>
<script>
var refreshURL = window.parent.location.href;
window.parent.location.href = refreshURL;
</script>