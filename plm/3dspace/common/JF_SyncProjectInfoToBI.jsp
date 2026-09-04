<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.util.StringList" %>
<%@ page pageEncoding="utf-8" %>
<%@include file = "emxNavigatorInclude.inc"%>
<script src="./scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="./scripts/emxUICore.js"></script>
<script language="JavaScript" src="./scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="./scripts/emxUISearch.js"></script>
<script language="JavaScript" src="../webapps/AmdLoader/AmdLoader.js"></script>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>


<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_SyncProjectInfoToBI.jsp");
%>
<%
    String msg="同步已完成。";
    try {

        String strObjectId = emxGetParameter(request, "objectId");
        String sResult = JPO.invoke(context, "JF_ProjectSpace", null, "JFCommandSendProjectSendBI", new String[]{strObjectId},String.class);


        if("T".equals(sResult)){
            msg="同步已完成。";
        }else {
            msg="同步失败。";
        }
    }catch (Exception e){
        e.printStackTrace();
    }


%>
<html>
<script>

    alert("<%=msg%>");


</script>
</html>