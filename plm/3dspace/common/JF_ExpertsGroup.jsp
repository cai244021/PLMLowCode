
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkException" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>

<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_ExpertsGroup.jsp");
%>
<%
    String strURL="../common/emxIndentedTable.jsp?program=JF_ExpertsGroupPeopleService:getAllExpertsGroupPeople&table=JF_ExpertsGroupPeople&toolbar=JF_ExpertsGroupPeopleToolBar&selection=multiple";
    try {

        strURL = ProgramCentralUtil.appendRequestParameters(context,request,strURL);

        Map paramMap = new HashMap();


        String[] methodArgs = JPO.packArgs(paramMap);
        boolean hasModifyAccess  = (boolean) JPO.invoke(context,"JF_ExpertsGroupPeopleService", null, "checkjfLibAdminAccess", methodArgs, Boolean.class);

        if(hasModifyAccess){
            strURL = strURL + "&editLink=true";
        }
    } catch (FrameworkException e) {
        LOGGER.info("JF_ExpertsGroup.jsp======error",e);
    }
%>
<script>

    var url = "<%=strURL%>";
    console.log(url);
    document.location.href = url;
</script>