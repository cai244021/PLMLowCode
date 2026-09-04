<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="java.util.Enumeration" %>
<!--
    DR添加物理产品
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_CostFunctionLibSubmit.jsp");
%>
<%
    String objectId = emxGetParameter(request, "selectId");
    String interfaceName = emxGetParameter(request, "emxTableRowId");
    JF_LOGGER.info("objectId:{} ,interfaceName:{} {}",objectId,interfaceName);
//设置选中对象的JF_ConnInterface值
    DomainObject obj = DomainObject.newInstance(context,objectId);
    obj.setAttributeValue(context, "JF_ConnInterface",interfaceName);
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    getTopWindow().getWindowOpener().parent.document.location.href = getTopWindow().getWindowOpener().parent.document.location.href;
    getTopWindow().closeWindow();
</script>
