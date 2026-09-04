
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkException" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>

<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_RecordSignState.jsp");
%>
<%
    String[] tableRowId = emxGetParameterValues(request, "emxTableRowId");
    String[] splitTableRowIds = new String[0];
    String head = "";
    String mess = "";
    try {
        splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
        LOGGER.info("splitTableRowIds===>{}",splitTableRowIds[0]);
        DomainObject domainObject = DomainObject.newInstance(context, splitTableRowIds[0]);
        String type = domainObject.getInfo(context,"type");
        if (!"JF_ESOTask".equals(type)){
            mess = "fail";
        }
    } catch (FrameworkException e) {
        LOGGER.info("JF_RecordSignState.jsp======error",e);
    }
%>
<script>
    if ("<%=mess%>"==="fail"){
        alert("\u53ea\u80fd\u9009\u62e9ESO\u4efb\u52a1");
        window.close();
    }else {
        var url = "../common/emxIndentedTable.jsp?table=JF_ESORecordTable&program=JF_ESO:getEsoReviewInformation&cancelButton=true&cancelLabel=emxFramework.State.Incident.Close&header=\u5ba1\u6838\u8bb0\u5f55&objectId=<%=splitTableRowIds[0]%>&editLink=true&toolbar=JFESOReviewRecordToolBar&selection=multiple" ;
        console.log(url);
        window.location.href = url;
    }
</script>
