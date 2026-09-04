<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %><%--  emxTreeRefresh.jsp
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,Inc.
   Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

      static const char RCSID[] = $Id: emxTreeRefresh.jsp.rca 1.5 Wed Oct 22 15:47:53 2008 przemek Experimental przemek $
--%>

<%@include file = "../common/emxNavigatorNoDocTypeInclude.inc"%>
<%!
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_AddCostBreakDownSubmit.jsp");
%>
<%
    LOGGER.info("@@@@@@@@@@@@@@@@@@2");
    String strObjId = emxGetParameter(request, "objectId");
    LOGGER.info("strObjId:{}", strObjId);
    //获取search页面选中的数据
    String strContextObjectId[] = emxGetParameterValues(request,"emxTableRowId");
    //选中数据oid
    StringList selectedBuildIds = new StringList();
    String strShowData = "";
    for(int i=0; i<strContextObjectId.length; i++){
        StringTokenizer strTokenizer = new StringTokenizer(strContextObjectId[i] ,"|");
        selectedBuildIds.add((String) strTokenizer.nextElement());
    }
    LOGGER.info("selectedBuildIds:{}", selectedBuildIds);
    //新建对象，关联关系
    HashMap hashMap = new HashMap();
    hashMap.put("objectId", strObjId);
    hashMap.put("selectedBuildIds", selectedBuildIds);
    String res = (String) JPO.invoke(context, "JF_Cost", JPO.packArgs (hashMap), "AddCostBreakDownCreate", JPO.packArgs (hashMap), String.class);
%>


<script language="Javascript">
    alert("<%=res%>");
    const xxx = parent.findFrame(parent, "JFManufactureFeeCmd");
    xxx.refreshSBTable();
</script>

