<%--  emxCommonDocumentPreCheckin.jsp

    Copyright (c) 1992-2020 Dassault Systemes.
    All Rights Reserved  This program contains proprietary and trade secret
    information of MatrixOne, Inc.
    Copyright notice is precautionary only and does not evidence any
    actual or intended publication of such program

    Description : pre-Document Create Wizard, Step 1

    static const char RCSID[] = "$Id: emxCommonDocumentPreCheckin.jsp.rca 1.25 Wed Apr  2 16:26:55 2008 przemek Experimental przemek $";
--%>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "emxComponentsNoCache.inc"%>
<%@include file = "emxComponentsUtil.inc"%>
<%@page import="com.matrixone.apps.common.util.DocumentUtil"%>
<%@ page import="com.matrixone.apps.common.Company" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.common.CommonDocument" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="com.matrixone.apps.common.VCDocument" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.common.CommonDocumentable" %>
<%@ page import="com.matrixone.apps.framework.ui.UICache" %>
<%@ page import="com.matrixone.apps.framework.ui.UIMenu" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>

<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>
<jsp:useBean id="tableBean" class="com.matrixone.apps.framework.ui.UITable" scope="session"/>
<html>
<head>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js" type="text/javascript"></script>
</head>

<body>
<%
    String objectAction = (String)emxGetParameter(request, "objectAction");
    String projectOrTaskId = (String)emxGetParameter(request, "objectId");
    DomainObject domainObject = DomainObject.newInstance(context, projectOrTaskId);
    String typeName = domainObject.getTypeName(context);
    String statesWithIds = (String) emxGetParameter(request, "statesWithIds");
    System.out.println("JF_CommonDocumentPreCheckin.jsp====statesWithIds::"+statesWithIds);
	if(UIUtil.isNotNullAndNotEmpty(statesWithIds)){
		session.setAttribute("statesWithIds", statesWithIds);	
	}
    String calledPage = (String)emxGetParameter(request, "calledPage");
    System.out.println("JF_CommonDocumentPreCheckin.jsp====calledPage::"+calledPage);
    String largeFileUpdate = (String)emxGetParameter(request, "largeFileUpdate");
    System.out.println("JF_CommonDocumentPreCheckin.jsp====largeFileUpdate::"+largeFileUpdate);
    //  Added:24-Feb-09:NZF:R207:Bug:368948
    String strIsAccessFieldRequired = (String)emxGetParameter(request, "showAccessType");
    System.out.println("JF_CommonDocumentPreCheckin.jsp====strIsAccessFieldRequired::"+strIsAccessFieldRequired);
    DomainObject object = DomainObject.newInstance(context);
    String selectFolderId = "";
    String[] tableRowId = emxGetParameterValues(request, "emxTableRowId");
    System.out.println("JF_CommonDocumentPreCheckin.jsp====tableRowId::"+tableRowId);
    if (tableRowId !=null){
        System.out.println("JF_CommonDocumentPreCheckin.jsp====tableRowId::"+tableRowId[0]);
        String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
        System.out.println("JF_CommonDocumentPreCheckin.jsp====splitTableRowIds::"+splitTableRowIds[0]);
        object.setId(splitTableRowIds[0]);
        String type = object.getInfo(context, DomainConstants.SELECT_TYPE);
        if (DomainConstants.TYPE_WORKSPACE_VAULT.equalsIgnoreCase(type) || DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(type)) {
            selectFolderId = splitTableRowIds[0];
        }
    }
    //技术文档创建，如果阶段为空，提示用户“阶段不存在，请联系项目经理创建阶段”，若创建者勾选项目节点，创建交付物，提示用户“请勿勾选项目”
    String strType = DomainConstants.EMPTY_STRING;
    if (UIUtil.isNotNullAndNotEmpty(selectFolderId)) {
        object.setId(selectFolderId);
        strType = object.getInfo(context, DomainConstants.SELECT_TYPE);
        if (DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(strType)) {
            String strMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Common.selectPSError", new String[]{});
%>
<script language="Javascript">
    alert("<%=strMess%>");
    window.close();
</script>
<%
            return;
        }
    }
    //判断是否有阶段
    System.out.println("wwwwwwwwwwwwwwwww");
    System.out.println("projectOrTaskId:" + projectOrTaskId);
    if (UIUtil.isNotNullAndNotEmpty(projectOrTaskId)) {
        object.setId(projectOrTaskId);
        strType = object.getInfo(context, DomainConstants.SELECT_TYPE);
        String strPhase = JPO.invoke(context, "JF_PublicProjectQuery", null, "getTaskPhase", new String[]{projectOrTaskId, strType}, String.class);
        System.out.println("wwwwwwwwwwwwwwwwwgetPhase:"+ strPhase);
        if (strPhase.length() <= 2) {
            String strMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Common.NoPhase", new String[]{});
%>
<script language="Javascript">
    alert("<%=strMess%>");
    window.close();
</script>
<%
            return;
        }
    }
    //判断如果不是研发部门，并且没有其他的文件夹，不允许创建文档，让项目经理创建
    String connDepartmentId = DomainConstants.EMPTY_STRING;
    String user = context.getUser();
    DomainObject personObject = PersonUtil.getPersonObject(context, user);
    //获取部门
    MapList mapList = personObject.getRelatedObjects(context,
            DomainRelationship.RELATIONSHIP_MEMBER,
            DomainConstants.TYPE_DEPARTMENT,
            StringList.create(DomainConstants.SELECT_ATTRIBUTE_TITLE, DomainConstants.SELECT_ID),
            new StringList(DomainRelationship.SELECT_ID),
            true,
            false,
            (short) 1,
            "",
            "",
            0);
    if (!mapList.isEmpty()) {
        Map map = (Map) mapList.get(0);
        connDepartmentId = UIUtil.getValue(map, DomainConstants.SELECT_ID);
    }
    //判断是不是研发部门
    String tempId = JPO.invoke(context, "JF_PublicMethodClass", null, "getBasicUrl", new String[]{"JF_RDCenterDepartment.id"}, String.class);
    String[] split = tempId.split(",");
    if (!connDepartmentId.equalsIgnoreCase(split[0]) && UIUtil.isNullOrEmpty(selectFolderId)){
        //不是研发部门
        //需要校验是否有文件夹
        String folder = JPO.invoke(context, "JF_PublicProjectQuery", null, "getWorkspaceVault", new String[]{projectOrTaskId, connDepartmentId}, String.class);
        if (folder.length() <= 2) {
            String strMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Common.NoFolder", new String[]{});
%>
<script language="Javascript">
    alert("<%=strMess%>");
    window.close();
</script>
<%
            return;
        }
    }

//    else if (DomainConstants.TYPE_PROJECT_SPACE.equalsIgnoreCase(typeName)) {
//        String strMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.TechnicalDocument.ErrorMess", new String[]{});
//        return;
//    }
    if( strIsAccessFieldRequired == null )
    {
        strIsAccessFieldRequired = "false";
    }
//  End:R207:PRG:R207:Bug:368948
    if( largeFileUpdate == null )
    {
        largeFileUpdate = "false";
    }
    String forceApplet = (String)emxGetParameter(request, "forceApplet");
    if( forceApplet == null )
    {
        forceApplet = "false";
    }
    Map emxCommonDocumentCheckinData = new HashMap();
    String timeStamp = emxGetParameter(request, "timeStamp");

    // Get request data from session level Table bean
    // these are set in the configurable Command definition by each application
    //The below part of code for Flat table check can be removed once all the
    //usecases of Flat Table to SB conversion are covered.
    String uiType = emxGetParameter(request, "uiType");
    Map tableData = null;
    Map requestMap = null;
    if("table".equalsIgnoreCase(uiType)){
    	tableData = (HashMap)tableBean.getTableData(timeStamp);
    	if(tableData != null){
    		requestMap = (HashMap)tableBean.getRequestMap((HashMap)tableData);
    	}
    }else{
    	tableData = (HashMap)indentedTableBean.getTableData(timeStamp);
    	if(tableData != null){
    		requestMap = (HashMap)indentedTableBean.getRequestMap((HashMap)tableData);
    	}
    }
    System.out.println("JF_CommonDocumentPreCheckin.jsp====requestMap::"+requestMap);
        if ( requestMap != null )
        {
            String parentRelName   = (String)requestMap.get("parentRelName");
            String showName        = (String)requestMap.get("showName");
            String showDescription = (String)requestMap.get("showDescription");
            String showOwner       = (String)requestMap.get("showOwner");
            String showType        = (String)requestMap.get("showType");
            String showPolicy      = (String)requestMap.get("showPolicy");
            String showTitle       = (String)requestMap.get("showTitle");
            String showAccessType  = (String)requestMap.get("showAccessType");
            String showRevision    = (String)requestMap.get("showRevision");
            String showFormat      = (String)requestMap.get("showFormat");
            String showFolder      = (String)requestMap.get("showFolder");
            String folderURL       = (String)requestMap.get("folderURL");
            String defaultType     = (String)requestMap.get("defaultType");
            String appDir          = (String)requestMap.get("appDir");
            String appProcessPage  = (String)requestMap.get("appProcessPage");
//          Added for Bug #371651 starts
            String customSortColumns  = (String)requestMap.get("customSortColumns");
            String customSortDirections  = (String)requestMap.get("customSortDirections");
            //by cyl 25050424
            String drawingFlag  = (String)requestMap.get("drawingFlag");
            emxCommonDocumentCheckinData.put("drawingFlag"  ,drawingFlag);
            //by cyl 25050424 end
            String table  = (String)requestMap.get("table");
            //Added for Bug #371651 ends
            emxCommonDocumentCheckinData.put("parentRelName"  ,parentRelName);
            emxCommonDocumentCheckinData.put("showName"       ,showName);
            emxCommonDocumentCheckinData.put("showDescription",showDescription);
            emxCommonDocumentCheckinData.put("showOwner"      ,showOwner);
            emxCommonDocumentCheckinData.put("showType"       ,showType);
            emxCommonDocumentCheckinData.put("showPolicy"     ,showPolicy);
            emxCommonDocumentCheckinData.put("showTitle"      ,showTitle);
            emxCommonDocumentCheckinData.put("showAccessType" ,showAccessType);
            emxCommonDocumentCheckinData.put("showRevision"   ,showRevision);
            emxCommonDocumentCheckinData.put("showFormat"     ,showFormat);
            emxCommonDocumentCheckinData.put("showFolder"     ,showFolder);
            emxCommonDocumentCheckinData.put("folderURL"      ,folderURL);
            emxCommonDocumentCheckinData.put("defaultType"    ,defaultType);
            emxCommonDocumentCheckinData.put("appDir"         ,appDir);
            emxCommonDocumentCheckinData.put("appProcessPage" ,appProcessPage);
            //Added for Bug #371651 starts
            emxCommonDocumentCheckinData.put("customSortColumns" ,customSortColumns);
            emxCommonDocumentCheckinData.put("customSortDirections" ,customSortDirections);
            emxCommonDocumentCheckinData.put("table" ,table);
            emxCommonDocumentCheckinData.put("timeStamp" ,timeStamp);
            //Added for Bug #371651 ends
        }
    // get the Company wide Applet property setting

    String appletProperty = "false";
    try {
      appletProperty = EnoviaResourceBundle.getProperty(context, "emxFramework.UseApplet");
    } catch(Exception e) {
      appletProperty = "false";
    }

    // allow the application to force applet usage
    if(forceApplet.equalsIgnoreCase("true") || "force".equalsIgnoreCase(appletProperty))
        appletProperty = "true";

    Enumeration enumParam = request.getParameterNames();
    // Loop through the request elements and
    // stuff into emxCommonDocumentCheckinData
    String storeFromBL = DocumentUtil.getStoreFromBL(context, "Document");
	System.out.println("L48 Collab & approve emxCommonDocumentPreCheckin getStoreFromBL : " + storeFromBL);

	boolean storeFound = false;		
    while (enumParam.hasMoreElements())
    {
        String name  = (String) enumParam.nextElement();
        String value = emxGetParameter(request, name);
        
        if(name.equals("store"))
        {
      	  storeFound = true;
      	    if(storeFromBL != null && !"".equals(storeFromBL) && !"null".equals(storeFromBL))
				value = storeFromBL;	  
        }
        
        emxCommonDocumentCheckinData.put(name, value);
    }
    if(!storeFound)
    {
    	emxCommonDocumentCheckinData.put("store", storeFromBL);
    }
    String forwardURL = (String) emxCommonDocumentCheckinData.get("forwardURL");
    String objectId = (String) emxCommonDocumentCheckinData.get("objectId");
    String routeId = (String) emxCommonDocumentCheckinData.get("objectId");
    //add by caipan for partList
    System.out.println("routeId=" + routeId);
    if(UIUtil.isNotNullAndNotEmpty(routeId)) {
        DomainObject obj = DomainObject.newInstance(context,routeId);
        String type = obj.getInfo(context, DomainConstants.SELECT_TYPE);
        if(type.equalsIgnoreCase("JFPartList")) {
            emxCommonDocumentCheckinData.put("partListId", routeId);
            routeId = obj.getInfo(context, "to[JFProject2PartList].from.id");
        }
    }

    emxCommonDocumentCheckinData.put("routeId", routeId);
    if("true".equalsIgnoreCase(largeFileUpdate) && !appletProperty.equalsIgnoreCase("true") )
    {
       emxCommonDocumentCheckinData.put("objectAction" ,objectAction);
    }

    if ( objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_CREATE_MASTER))
    {
        objectId = (String) emxCommonDocumentCheckinData.remove("objectId");
        System.out.println("JF_CommonDocumentPreCheckin.jsp====objectId::"+objectId);
        String parentId = (String) emxCommonDocumentCheckinData.get("parentId");
        if ((parentId == null || "".equals(parentId) || "null".equals(parentId)) && objectId != null && !"".equals(objectId) && !"null".equals(objectId))
        {
            emxCommonDocumentCheckinData.put("parentId", objectId);
        }

    }

    if ( objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_CREATE_MASTER))
    {
        forwardURL = "JF_CommonDocumentCreateDialogFS.jsp";
    }
    emxCommonDocumentCheckinData.put("selectFolderId", selectFolderId);
    emxCommonDocumentCheckinData.put("flag", UIUtil.isNotNullAndNotEmpty(selectFolderId) ? "Y" : "N");
    emxCommonDocumentCheckinData.put("typeName", typeName);
    System.out.println("JF_CommonDocumentPreCheckin.jsp====emxCommonDocumentCheckinData::"+emxCommonDocumentCheckinData);
    session.setAttribute("emxCommonDocumentCheckinData", emxCommonDocumentCheckinData);

%>
    <form name="application" action="<%=XSSUtil.encodeForHTML(context, forwardURL)%>" >
      <input type="hidden" name="xyz" value="xyz" />
    </form>
    <script>
        document.application.submit();
    </script>
</body></html>
