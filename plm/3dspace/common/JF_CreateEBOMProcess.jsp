<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.alibaba.fastjson.JSON" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<%@ include file="../programcentral/emxProgramGlobals2.inc" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%!
    private static final Logger NIO_LOG = LoggerFactory.getLogger("JF_TrimCreateProcess.jsp");
%>
<%
    NIO_LOG.info("JF_CreateEBOMProcess start");
    String parentOID = emxGetParameter(request, "parentOID");
    String pcListStr = emxGetParameter(request, "pcList");
    StringList pcList = FrameworkUtil.split(pcListStr,",");
/*    String[]	objectIds = emxGetParameterValues(request, "emxTableRowId");
    StringList pcList = new StringList();
    for(int i=0; i < objectIds.length; i++)
    {
        StringList sList = FrameworkUtil.split(objectIds[i],"|");
        if(sList.size() == 1 || sList.size() == 2)
            pcList.add((String)sList.get(0));
        else if(sList.size() == 3)
            pcList.add((String)sList.get(0));
        else if(sList.size() == 4)
            pcList.add((String)sList.get(1));
    }*/
    NIO_LOG.info("pcList:{}",pcList);
    Map map = new HashMap<>();
    map.put("HttpServletRequest", request);
    map.put("pcList", pcList);
    map.put("objectId",parentOID );
    String[] restParams = JPO.packArgs(map);
    boolean flag = JPO.invoke(context, "JF_Config", null, "createSingleBOM",restParams, boolean.class);
    response.setContentType("application/json; charset=UTF-8");
    NIO_LOG.info("JF_CreateEBOMProcess end:{}",flag);
    Map resultMap = new HashMap();
    resultMap.put("success",flag);
    if(flag){
        resultMap.put("code", 200);
    }else{
        resultMap.put("code", 500);
        resultMap.put("message", "create fail,Please Contact administrator!!!");
    }
    String jsonResult = JSON.toJSONString(resultMap);


    PrintWriter writer = response.getWriter();
    writer.print(jsonResult);
    writer.flush();
    NIO_LOG.info("jsonResult:{}",jsonResult);
/*    out.print(jsonResult);
    out.flush();*/
    %>
