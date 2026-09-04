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
	String statesWithIds = (String) emxGetParameter(request, "statesWithIds");
	if(UIUtil.isNotNullAndNotEmpty(statesWithIds)){
		session.setAttribute("statesWithIds", statesWithIds);	
	}
    String calledPage = (String)emxGetParameter(request, "calledPage");
    String largeFileUpdate = (String)emxGetParameter(request, "largeFileUpdate");
    //  Added:24-Feb-09:NZF:R207:Bug:368948
    String strIsAccessFieldRequired = (String)emxGetParameter(request, "showAccessType");
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
    // based on this property set the actionURL parameter
    com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
    String personName = person.getName();
    Company company = person.getCompany(context);

    String appletProperty = "false";
    try {
      appletProperty = EnoviaResourceBundle.getProperty(context, "emxFramework.UseApplet");
    } catch(Exception e) {
      appletProperty = "false";
    }
    boolean forceSystemLevelApplet = false;
    if( "force".equalsIgnoreCase(appletProperty) )
    {
      forceSystemLevelApplet = true;
    }
    // allow the application to force applet usage
    if(forceApplet.equalsIgnoreCase("true") || "force".equalsIgnoreCase(appletProperty))
        appletProperty = "true";
    // since there is no java plug-in with version 1.4 or above available for Mac OS 10.2.6
    // do not use Applet for Mac requests
    //<Fix Mx373728>
    //PS reported enabling Applet on MAC 10.5 is working file
    //Instead of following the fix provided by PS, we are removing (commenting) follwoing statements(appletProperty = "false"; incase of use-agent is Mac)
    //String userAgent = request.getHeader("User-Agent");
    //if(userAgent.indexOf("Mac") != -1)
    //{
    //   appletProperty = "false";
    //}
    //</Fix Mx373728>

    if(appletProperty.equalsIgnoreCase("true"))
    {
       emxCommonDocumentCheckinData.put("actionURL" ,"emxCommonDocumentCheckinAppletDialogFS.jsp");
    }
    else
    {
       emxCommonDocumentCheckinData.put("actionURL", "emxCommonDocumentCheckinDialogFS.jsp");
       if("true".equalsIgnoreCase(largeFileUpdate) )
       {
          objectAction = CommonDocument.OBJECT_ACTION_UPDATE_MASTER;
       }
    }
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
    emxCommonDocumentCheckinData.put("routeId", routeId);
    if("true".equalsIgnoreCase(largeFileUpdate) && !appletProperty.equalsIgnoreCase("true") )
    {
       emxCommonDocumentCheckinData.put("objectAction" ,objectAction);
    }
	

    if ( objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_CREATE_MASTER) ||
          objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_CREATE_MASTER_PER_FILE) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ZIP_TAR_GZ) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_COPY_FROM_VC) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_FILE_FOLDER) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_STATE_SENSITIVE_CONNECT_VC_FILE_FOLDER) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONNECT_VC_FILE_FOLDER)  ||
          objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_HOLDER) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ON_DEMAND) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONNECT_VC_ON_DEMAND) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONNECT_VC_FROM_ISSUE_SEARCH) )
    {
      objectId = (String) emxCommonDocumentCheckinData.remove("objectId");
      String parentId = (String) emxCommonDocumentCheckinData.get("parentId");
      if (   (parentId == null || "".equals(parentId) || "null".equals(parentId) )
          && (objectId != null && !"".equals(objectId) && !"null".equals(objectId))
      && (!objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ON_DEMAND) &&
          !objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONNECT_VC_ON_DEMAND) &&
          !objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONNECT_VC_FROM_ISSUE_SEARCH)))
      {
         emxCommonDocumentCheckinData.put("parentId", objectId);
         parentId = objectId;
      }
      if (objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ON_DEMAND) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONNECT_VC_ON_DEMAND) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONNECT_VC_FROM_ISSUE_SEARCH)){
          String selector = emxGetParameter(request,"selector");
          String server = emxGetParameter(request,"server");
          String path = emxGetParameter(request,"path");
          emxCommonDocumentCheckinData.put("server",server);
          emxCommonDocumentCheckinData.put("selector",selector);
          emxCommonDocumentCheckinData.put("path",path);
          emxCommonDocumentCheckinData.put("format","generic");
          emxCommonDocumentCheckinData.put("noOfFiles", "1");
      }

      // Get inherited DesignSync Info from Project Space.
      if (objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_STATE_SENSITIVE_CONNECT_VC_FILE_FOLDER))
      {
              // Store is in 0, Selector is in 1.
              StringList data = VCDocument.getProjectStoreSelector(context, parentId);
        emxCommonDocumentCheckinData.put("server", (String)data.get(0));
        emxCommonDocumentCheckinData.put("selector", (String)data.get(1));
      }
    }

    if( objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONVERT_CHECKIN_VC_FILE_FOLDER) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONVERT_VC_FILE_FOLDER) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONVERT_COPY_FROM_VC) )
    {
        forwardURL = "emxCommonDocumentConversionDialogFS.jsp";
    } else  if ( objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_CREATE_MASTER) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ZIP_TAR_GZ) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_COPY_FROM_VC) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_FILE_FOLDER) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_STATE_SENSITIVE_CONNECT_VC_FILE_FOLDER) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONNECT_VC_FILE_FOLDER) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ON_DEMAND) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONNECT_VC_ON_DEMAND) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONNECT_VC_FROM_ISSUE_SEARCH))
    {
        forwardURL = "emxCommonDocumentCreateDialogFS.jsp";
    } 	else {
        if((appletProperty.equalsIgnoreCase("true") && (!objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_MASTER) )
              && (!objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_HOLDER) ) ) || forceSystemLevelApplet)
        {
             forwardURL = "emxCommonDocumentCheckinAppletDialogFS.jsp";
        }
        else if((appletProperty.equalsIgnoreCase("true") && (objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_MASTER)) )             )
          {
               forwardURL = "emxCommonDocumentUpdateAppletDialogFS.jsp";
               				   
        	//forwardURL = "emxCommonDocumentUpdateAppletPreferencesDialog.jsp";
               
          }
        else
        {
            forwardURL = "emxCommonDocumentCheckinDialogFS.jsp";
        }

    }
    if ( objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_MASTER) )
    {
        objectId = (String) emxCommonDocumentCheckinData.get("objectId");
        CommonDocument commonDocument = (CommonDocument) DomainObject.newInstance(context, objectId);
        StringList objectSelects = new StringList(8);
        objectSelects.add(CommonDocument.SELECT_TITLE);
        objectSelects.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
        objectSelects.add(CommonDocument.SELECT_MASTER_ID);
        objectSelects.add(CommonDocument.SELECT_MASTER_FILE_NAME);
        objectSelects.add(CommonDocument.SELECT_MASTER_FILE_FORMAT);

        Map objectMap = commonDocument.getInfo(context, objectSelects);
        String isVersionObject = (String) objectMap.get(CommonDocument.SELECT_IS_VERSION_OBJECT);
        if ( "true".equalsIgnoreCase(isVersionObject) )
        {
            String fileName = (String)objectMap.get(CommonDocument.SELECT_TITLE);
            StringList fileList = (StringList)objectMap.get(CommonDocument.SELECT_MASTER_FILE_NAME);
            StringList formatList = (StringList)objectMap.get(CommonDocument.SELECT_MASTER_FILE_FORMAT);
            int index = fileList.indexOf(fileName);
            if ( index > -1 && formatList.size() >= index )
            {
                emxCommonDocumentCheckinData.put("oldFileName", fileName);
                emxCommonDocumentCheckinData.put("objectId", objectMap.get(CommonDocument.SELECT_MASTER_ID));
                emxCommonDocumentCheckinData.put("format",formatList.get(index));
                emxCommonDocumentCheckinData.put("deleteFromTree",objectId);
            }
        }
    }
    session.setAttribute("emxCommonDocumentCheckinData", emxCommonDocumentCheckinData);

    if( calledPage != null && !"".equals(calledPage) && !"null".equals(calledPage) && calledPage.equals("createIssue")){
        String appProcessPage ="../components/emxCommonFS.jsp?functionality=IssueCreateFSInstanceOnDemand&suiteKey=Components";
        emxCommonDocumentCheckinData.put("appProcessPage", appProcessPage);
    }

    objectId = (String) emxCommonDocumentCheckinData.get("objectId");
    boolean override = true;
    String overrideStr = (String) emxCommonDocumentCheckinData.get("override");
    if ( overrideStr != null && "false".equalsIgnoreCase(overrideStr) )
    {
        override = false;
    }
    boolean isVersionable = true;
    if ( (objectId != null && !"".equals(objectId) && !"null".equals(objectId)) )
    {
        isVersionable = CommonDocument.allowFileVersioning(context, objectId);
    }
    if ( !isVersionable && !objectAction.equalsIgnoreCase("image") )
    {
        emxCommonDocumentCheckinData.put("objectAction", CommonDocument.OBJECT_ACTION_CHECKIN_WITHOUT_VERSION);
        emxCommonDocumentCheckinData.put("isVersionable", (Boolean.valueOf(isVersionable)));
        emxCommonDocumentCheckinData.put("showComments", "false");
    }
    if ( override && (objectId != null && !"".equals(objectId) && !"null".equals(objectId)) )
    {
        CommonDocumentable commonDocument = (CommonDocumentable)DomainObject.newInstance(context,objectId);
        String actionCommand = commonDocument.getCheckinCommand(context);
        if ( actionCommand != null )
        {
            Map commandMap  = UICache.getCommand(context, actionCommand);
            String actionURL = UIMenu.getHRef(commandMap);
%>
            <form name="integration" action="<%=XSSUtil.encodeForHTML(context, actionURL)%>" >
              <table>
<%
            java.util.Set set = emxCommonDocumentCheckinData.keySet();
            Iterator itr = set.iterator();
            // Loop through the request elements and
            // stuff into emxCommonDocumentCheckinData
            while (itr.hasNext())
            {
                String name  = (String) itr.next();
                Object value = (Object)emxCommonDocumentCheckinData.get(name);
%>
                <input type="hidden" name="<%=name%>" value="<xss:encodeForHTMLAttribute><%=value%></xss:encodeForHTMLAttribute>" />
<%
            }
%>
              </table>
            </form>
            <script language="javascript">
              document.integration.submit();
            </script>
<%
        } else {
            CommonDocument object = (CommonDocument)DomainObject.newInstance(context,objectId);
            StringList selects = new StringList(8);
            selects.add(CommonDocument.SELECT_VCFILE_LOCKED);
            selects.add(CommonDocument.SELECT_VCFILE_LOCKER);
            selects.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
            boolean vcDocument=false;
            Map objectMap = object.getInfo(context, selects);
            boolean vcFileLock= (Boolean.valueOf((String) objectMap.get(CommonDocument.SELECT_VCFILE_LOCKED))).booleanValue();
            String vcFileLocker= (String)objectMap.get(CommonDocument.SELECT_VCFILE_LOCKER);
            String vcInterface = (String)objectMap.get(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
            vcDocument = "TRUE".equalsIgnoreCase(vcInterface)?true:false;
            if ((!vcDocument) || ((vcDocument && !vcFileLock) || (vcDocument && vcFileLock && vcFileLocker.equals(context.getUser()))))
            {

%>
          <form name="application" action="<%=XSSUtil.encodeForHTML(context, forwardURL)%>" >
            <input type="hidden" name="xyz" value="xyz" />
          </form>
          <script>
              document.application.submit();
          </script>
<%
          } else {
%>
          <script>
            alert("<emxUtil:i18nScript localize="i18nId">emxComponents.CommonDocument.VCDocumentCheckinError</emxUtil:i18nScript> <%=XSSUtil.encodeForJavaScript(context, vcFileLocker)%>");
            getTopWindow().closeWindow();
          </script>
<%
          }
        }
    } else {//  Added:24-Feb-09:NZF:R207:Bug:368948
%>
    <form name="application" action="<%=XSSUtil.encodeForHTML(context, forwardURL)%>" >
    <%
    if("true".equalsIgnoreCase(strIsAccessFieldRequired)){
        %>
        <input type="hidden" name="showAccessType" value="<xss:encodeForHTMLAttribute><%=strIsAccessFieldRequired%></xss:encodeForHTMLAttribute>" />
        <% 
    }
    %>
      <input type="hidden" name="xyz" value="xyz" />
    </form>
    <script>
        document.application.submit();
    </script>
<%//End:R207:PRG:R207:Bug:368948 
    }
%>
</body></html>
