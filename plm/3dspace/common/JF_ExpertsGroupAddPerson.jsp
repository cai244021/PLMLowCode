
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkException" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.util.StringUtil" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>

<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_ExpertsGroup.jsp");
%>
<%
    String projectId = emxGetParameter(request, "parentOID");
    if(ProgramCentralUtil.isNullString(projectId)){
        projectId = emxGetParameter(request, "objectId");
    }

    String[] ids = emxGetParameterValues(request, "emxTableRowId");

    try{
        ContextUtil.pushContext(context);
        for(int i =0, len = ids.length; i<len;i++){
            StringList personIdList = StringUtil.split(ids[i], "|");

            if( personIdList.size() >= 1 ){
                String personId = (String)personIdList.get(0);

                DomainRelationship.connect(context,
                        projectId,
                        "JFProject2ExpertsGroup",
                        personId,
                        false);
            }
        }
    }catch(Exception e){
        e.printStackTrace();
    }finally{
        ContextUtil.popContext(context);
    }
%>
<script>

    // getTopWindow().getWindowOpener().location.reload();

    // getTopWindow().closeWindow();

    console.log(window.top);
    console.log(window.top.getWindowOpener());
    console.log(window.top.getWindowOpener().top);
    window.top.getWindowOpener().location.reload();
    window.top.close();
    // var refreshURL = window.top.opener.document.location.href;
    // console.log(refreshURL);
    // window.top.opener.document.location.href = refreshURL;
    // window.top.close();


</script>