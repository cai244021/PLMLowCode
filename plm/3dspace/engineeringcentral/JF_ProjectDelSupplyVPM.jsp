<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.domain.util.*" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<!--
DR添加物理产品
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%!
    private static final Logger log = LoggerFactory.getLogger("JF_ProjectDelSupplyVPM.jsp");
%>
<%
    String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
    String objectId = emxGetParameter(request, "objectId");
    Map requestMap = new HashMap();
    requestMap.put("projectId",objectId);
    for (int i = 0; i < tableRowIdList.length; i++) {
        StringList tableRowId = FrameworkUtil.split(tableRowIdList[i],"|");
        String partId = tableRowId.get(1);
        log.info("partId:{}",partId);
        requestMap.put("partId",partId);
        JPO.invoke(context, "JF_VPMReferenceEBOM", new String[0], "JFProject2RootPartDelete", JPO.packArgs(requestMap), void.class);
    }
%>
<script>
    var refreshURL = window.parent.location.href;
    window.parent.location.href = refreshURL;
</script>
