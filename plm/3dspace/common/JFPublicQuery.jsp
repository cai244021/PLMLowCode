
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkException" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>

<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JFPublicQuery.jsp");
%>
<%
    String[] tableRowId = emxGetParameterValues(request, "emxTableRowId");
    String[] splitTableRowIds = new String[0];
    String head = "";
    try {
        splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
        LOGGER.info("splitTableRowIds===>{}",splitTableRowIds[0]);
        DomainObject domainObject = DomainObject.newInstance(context, splitTableRowIds[0]);
        String name = domainObject.getInfo(context,"attribute[EnterpriseExtension.V_PartNumber]");
        String revision = domainObject.getInfo(context,"revision");
        head = name+"_"+revision;
    } catch (FrameworkException e) {
        LOGGER.info("JFPublicQuery.jsp======error",e);
    }
%>
<script>
    var url = "../common/emxIndentedTable.jsp?table=JF_ProjectInfoTable&program=JF_PublicProjectQuery:getVpmRefAndProjectInformation&cancelButton=true&cancelLabel=emxFramework.State.Incident.Close&header=\u5171\u7528\u9879\u76ee\u4fe1\u606f <%=head%>&objectId=<%=splitTableRowIds[0]%>" ;
    console.log(url);
    window.location.href = url;
</script>