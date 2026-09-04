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
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.TYPE_PERSON" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.SELECT_RELATIONSHIP_ID" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.*" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Map" %>

<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_RefreshJFFunctionModuleProcess.jsp");
%>
<%
    String strObjectId = emxGetParameter(request, "objectId");
%>
<html>
<script>
    // findFrame(getTopWindow(),"JFRapidOfferModuleListCmd").emxEditableTable.reloadCell('JF_EvaluatePrice','')
    findFrame(getTopWindow(),"JFRapidOfferModuleListCmd").emxEditableTable.refreshStructure()
    // findFrame(getTopWindow(),"JFRapidOfferModuleListCmd").document.location.reload()


</script>
</html>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>

