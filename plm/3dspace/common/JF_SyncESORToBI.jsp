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
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_SyncMBOMToMDM.jsp");
%>
<%
    String msg="同步已完成。";
    try {
        StringList list = new StringList();
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        for (int i = 0; i < strSelectIds.length; i++) {
            String id = strSelectIds[i].split("\\|")[1];
            list.add(id);
        }
        Map packMap=new HashMap();
        packMap.put("idlist",list);

        String sResult = JPO.invoke(context, "JF_ProjectSpace", null, "CommandBatchSendESOR2BI", JPO.packArgs (packMap),String.class);



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