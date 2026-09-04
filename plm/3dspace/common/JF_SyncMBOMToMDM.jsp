<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
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
    String strObjectId = emxGetParameter(request, "objectId");
    String strExportType = emxGetParameter(request, "exportType");

    System.out.println("strObjectId--->"+strObjectId);
    strExportType=strExportType.replaceAll(",","@@");
    System.out.println("strExportType--->"+strExportType);

//    JPO.invoke(context,"JF_ProjectSpace",null,"triggerSendPartlistToBI",new String[]{"35845.4994.36342.5706"});
//    JPO.invoke(context,"JF_ProjectSpace",null,"triggerSendPartlistToBI",new String[]{"35845.4994.13831.18954"});
//    JPO.invoke(context,"JF_ProjectSpace",null,"TestgetPartListinfoSendSRM",new String[]{});

    Map packMap=new HashMap();
    packMap.put("Factory",strExportType);
    packMap.put("ProjectID",strObjectId);

    com.alibaba.fastjson.JSONObject sResult = JPO.invoke(context, "JF_DataInterface", null, "syncMBOMToMDM", JPO.packArgs (packMap),com.alibaba.fastjson.JSONObject.class);
    String code=(String)sResult.get("code");

    String msg=(String)sResult.get("msg");
    if("200".equals(code)){
        msg="同步至MDM已完成。。。";
    }else {
        msg="同步失败,"+msg;
    }
%>
<html>
<script>


    console.log("strObjectId-->"+'<%=strObjectId%>');
    console.log("strExportType-->"+'<%=strExportType%>');
    alert("<%=msg%>");




</script>
</html>