<%--
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
--%>
<%@page import="com.matrixone.apps.framework.tree.Tree"%>
<%@include file = "emxNavigatorNoDocTypeInclude.inc"%>
<%@page import="com.matrixone.apps.domain.util.XSSUtil"%>
<%@page import="com.matrixone.apps.domain.util.FrameworkUtil"%>
<jsp:useBean id="shortcutInfo" class="com.matrixone.apps.framework.ui.UIShortcut" scope="session" />
<%@page import="jakarta.json.JsonArray"%>
<%@page import="jakarta.json.Json"%>
<%@page import="jakarta.json.JsonObject"%>
<%@page import="jakarta.json.JsonObjectBuilder"%>
<%@page import="jakarta.json.JsonArrayBuilder"%>
<%@page import="java.io.StringReader"%>

<%@page import = "matrix.db.JPO"%>

<!DOCTYPE html>

<html>
<%!
    static public boolean isStructureExists(ServletContext application,
                                            HttpSession session,
                                            HttpServletRequest request,
                                            Context context,
                                            String objectId,
                                            String emxSuiteDirectory,
                                            String relId,
                                            String treemenu
                                            ) throws FrameworkException
    {

    String structureMenuObj = "";

try {
        UIMenu emxTreeObject = new UIMenu();
        Vector userRoleList = PersonUtil.getAssignments(context);


        if(treemenu == null || treemenu.trim().length()==0){
            structureMenuObj=UITreeUtil.getTreeMenuName(application, session, context, objectId, emxSuiteDirectory);
        }else{
            structureMenuObj=treemenu;
        }

        HashMap topStructureMenuMap = emxTreeObject.getMenu(context, structureMenuObj);
        String sStructureMenu = emxTreeObject.getSetting(topStructureMenuMap, "Structure Menu");
        HashMap menuJSNodeMap = UITreeUtil.getJSNodeMap(application, session, request, context, topStructureMenuMap, objectId, relId);

        MapList structureTreeMenuMap = new MapList();
        MapList sMapList = new MapList();
        HashMap firstCommand = null;

        if(sStructureMenu != null && sStructureMenu.trim().length() > 0)
        {
            structureTreeMenuMap = emxTreeObject.getOnlyCommands(context, sStructureMenu,userRoleList);
            if (structureTreeMenuMap != null && structureTreeMenuMap.size()> 0)
            {
                for (int i=0; i < structureTreeMenuMap.size(); i++)
                {
                    firstCommand = (HashMap)structureTreeMenuMap.get(i);
                    if(firstCommand != null){
                        return true;
                    }

                }
            }
        }

}catch(Exception mx){
  throw new FrameworkException(mx.toString());
}
   return false;
}
%>

<head>

<link rel="stylesheet" type="text/css" href="../common/styles/emxUIDefault.css">
<script type="text/javascript" src="scripts/emxUIConstants.js"></script>
<script type="text/javascript" src="scripts/emxUICore.js"></script>
<script type="text/javascript" src="scripts/emxUICoreMenu.js"></script>
<script language="javascript" src="scripts/emxUICoreTree.js"></script>
<script language="javascript" src="scripts/emxUIHistoryTree.js"></script>
<script language="javascript" src="scripts/emxUIDetailsTree.js"></script>
<script language="javascript" src="scripts/emxUIStructureTree.js"></script>
<script language="javascript" src="scripts/emxUITreeUtil.js"></script>
<script language="javascript" src="scripts/emxUIModal.js"></script>
    <%--add by chenyan 2025/07/30 新增 绑定onload时间将ECR首页设置缓存为true防止切换时再次刷新--%>
<script>
    window.addEventListener('load', function() {
        const tab0 = document.getElementById("unique0");
        if (tab0){
            const tabName = tab0.getAttribute("tab-name");
            if ("type_JFNewECR" == tabName || "type_JFECR" == tabName|| "type_JFFormalECR" == tabName){
                tab0.setAttribute("cachePage",'true');
            }
        }
    });
</script>
<script>
	if(getTopWindow().isMobile || getTopWindow().isPCTouch){
		addStyleSheet("emxUIMobile", "../common/mobile/styles/");
	}
</script>
<script language="javascript">
<%
  ContextUtil.startTransaction(context,true);  
  String suiteKey = emxGetParameter(request,"suiteKey");
  JsonArray strucTree=null;
  String targetLocation = emxGetParameter(request, "targetLocation");
  if("slidein".equals(targetLocation)){
%>
    getTopWindow().closeSlideInDialog();
<%
  }
  String objectId = emxGetParameter(request,"objectId");
  String objTypeName="";  
  
  boolean noTree = false;
  if (objectId != null && !"".equals(objectId.trim()) && !"null".equals(objectId.trim())) {
  try
  {
      String info = MqlUtil.mqlCommand(context, "print bus $1 select $2 $3 dump $4", objectId, "type", "current.access[read]", "|");
      StringList infoList = FrameworkUtil.split(info, "|");
      objTypeName = (String) infoList.get(0);
     if (!Boolean.valueOf((String)infoList.get(1))) {
        noTree = true;
      }
      objTypeName = FrameworkUtil.getAliasForAdmin(context,"Type", objTypeName,true);
  }catch(Exception e){
	noTree = true;
	  }
  }
if(!noTree){
%>

var url = location.href;
var urlEncoded = url.substring(0, url.indexOf("?")+1);
var splitUrl = url.substring(url.indexOf("?")+1).split("&");
for ( var j = 0 ;  j < splitUrl.length ; j++){
	splitUrl[j] = splitUrl[j].replace("%2526", "&amp;");
	if(j>0){
		urlEncoded += "&";
	}
	urlEncoded += splitUrl[j];
}
<%
} else {
%>
if(getTopWindow().getWindowOpener() && getTopWindow() == self){
	window.location.href = "emxTreeNoDisplay.jsp";
}
<%
}
  String isStructure = emxGetParameter(request, "isStructure");
  String relId = emxGetParameter(request,"relId");
  String emxSuiteDirectory = emxGetParameter(request,"emxSuiteDirectory");
  String sTreeMenuName = emxGetParameter(request, "treeMenu");
  String sTreeLabel = emxGetParameter(request, "treeLabel");
  String treemode = emxGetParameter(request, "treemode");
  String blnAppendParams = emxGetParameter(request, "AppendParameters");
  String strOtherParam = "otherTollbarParams=" + emxGetParameter(request, "otherTollbarParams");
  String defaultCategory = emxGetParameter(request, "DefaultCategory");
  String objectName  = emxGetParameter(request, "objectName");
  String aGName  = emxGetParameter(request, "AGName");
  session.setAttribute("StoredtreeLabel",sTreeLabel);
  session.setAttribute("StoredobjectName",objectName);
  session.setAttribute("StoredAGName",aGName);
  boolean refreshTree=true;
  if(UIUtil.isNotNullAndNotEmpty(emxGetParameter(request, "refreshTree"))) {
      refreshTree=Boolean.valueOf(emxGetParameter(request, "refreshTree"));
  }
  String headerLifecycle = emxGetParameter(request, "headerLifecycle");
  
  boolean isThereStructure = false;
  if(treemode==null) {
	  treemode="details";
  }
  String treeNodeUrl = "emxTreeNoDisplay.jsp";
  String bcLabel = UINavigatorUtil.getI18nString("emxFramework.BreadCrumb.Home", "emxFrameworkStringResource", request.getHeader("Accept-Language"));
  
  
   String isLinked="";
  if(!noTree){
    //  ContextUtil.startTransaction(context,false);  // read-only transaction - added to avoid menu performance overhead
    // check objectId for the valid string and trim if required
    if (objectId != null && !"".equals(objectId.trim()) && !"null".equals(objectId.trim()))
    {
          try
          {
          isThereStructure=(boolean)isStructureExists(application,
                                                                session,
                                                                request,
                                                                context,
                                                                objectId,
                                                                emxSuiteDirectory,
                                                                relId,
                                                                sTreeMenuName);
     }
          catch(Exception ex)
          {
        	  noTree = true;
          }
     }
     //ContextUtil.commitTransaction(context);  // end read-only transaction - added to avoid menu performance overhead
 }

boolean dobc = false;
String treelabel = "";
String mode = emxGetParameter(request,"mode");
String mappedTreeName = "";
if(noTree){
	treeNodeUrl = "emxTreeNoDisplay.jsp";
} else {

		mappedTreeName = UITreeUtil.getCategoryTreeName(application, session, context, objectId, emxSuiteDirectory, sTreeMenuName);
	HashMap requestMap  = UINavigatorUtil.getRequestParameterMap(request);
	HashMap tempMap = UITreeUtil.getCategoryTreeURL(application, session, request, context, mappedTreeName, isThereStructure);
	treeNodeUrl = (String)tempMap.get("URL");
	bcLabel = (String)tempMap.get("Label");
	
	if(UIUtil.isNullOrEmpty(defaultCategory)) {
		defaultCategory=UIUtil.isNotNullAndNotEmpty((String)tempMap.get("defaultCategoryForObject"))? (String)tempMap.get("defaultCategoryForObject") : null ;
	}
	
	String strOtherTollbarParams = "";
	String treeScopeIDName = UIComponent.getSetting(UIMenu.getMenu(context, mappedTreeName), "Tree Scope ID");
	if (treeScopeIDName.length() > 0) {
		treeNodeUrl += "&" + treeScopeIDName + "=" + XSSUtil.encodeForURL(context, objectId); 
		if(!"".equals(strOtherTollbarParams)){
			strOtherTollbarParams += ",";
		}
		requestMap.put(treeScopeIDName,objectId);
		strOtherTollbarParams += treeScopeIDName;
	}
	if("true".equalsIgnoreCase(blnAppendParams)){
		StringBuffer requestParams = new StringBuffer(100);
				
		Enumeration enumParamNames = emxGetParameterNames(request);
        while(enumParamNames.hasMoreElements()) {
            String paramName = (String) enumParamNames.nextElement();
            String paramValue = Request.getParameter(request, paramName);

            if (!"appendparameters".equalsIgnoreCase(paramName) && !"objectId".equalsIgnoreCase(paramName) && !"suitekey".equalsIgnoreCase(paramName)  && !"treeMenu".equalsIgnoreCase(paramName) && paramValue != null ) {
			
        		if(!"".equals(strOtherTollbarParams)){
        			strOtherTollbarParams += ",";
        		}
        		strOtherTollbarParams += XSSUtil.encodeForURL(context,paramName);
        	}
            
	        requestParams.append(XSSUtil.encodeForURL(context,paramName)); 
	        requestParams.append("="); 
	        requestParams.append(XSSUtil.encodeForURL(context,paramValue)); 

	        if(enumParamNames.hasMoreElements()){
	        	requestParams.append("&");
	        }
		}
		treeNodeUrl += "&" +requestParams.toString();
	}
	
	if (isThereStructure){
		if(!"".equals(strOtherTollbarParams)){
			strOtherTollbarParams += ",";
		}
		strOtherTollbarParams += "isStructure";
		treeNodeUrl += "&isStructure=true";
		requestMap.put("isStructure","true");
	}

	if(!"".equals(strOtherTollbarParams)){
		if(treeNodeUrl.contains("otherTollbarParams=")){
			String strParam = strOtherParam;
            StringList paramList = FrameworkUtil.split(strOtherTollbarParams, ",");
            for(int ii =0; ii < paramList.size(); ii++){
          		String paramName = (String)paramList.get(ii);
          		strParam = strParam + "," + paramName;	
          	}			 
			// values are encoded in treeNodeURL
			strOtherParam = FrameworkUtil.findAndReplace(strOtherParam,",","%2C");
			treeNodeUrl = FrameworkUtil.findAndReplace(treeNodeUrl,strOtherParam,strParam);
		}			
		else{	
			treeNodeUrl += "&otherTollbarParams=" + strOtherTollbarParams;
		}
	}
	if (mappedTreeName != null && mappedTreeName.length() > 0)
    {
		String timeZone=(String)session.getAttribute("timeZone");
        HashMap treeMenuMap = UIMenu.getMenu(context, mappedTreeName);
        if (treeMenuMap != null && UIMenu.isMenu(treeMenuMap))
        {
        	treeMenuMap = UITreeUtil.getJSNodeMap(application, session, request, context, treeMenuMap, objectId, relId);
        	treelabel = (String)treeMenuMap.get("nodeLabel");
        	if(sTreeLabel != null && !"".equals(sTreeLabel)){
        		treelabel = sTreeLabel;
        		if(!treeNodeUrl.contains("&treeLabel=")){
            		treeNodeUrl += "&treeLabel=" + XSSUtil.encodeForURL(context, treelabel);
        		}
        	}
        }
    	//Getting object for category toolbar
        Vector userRoleList = PersonUtil.getAssignments(context);
		String language = request.getHeader("Accept-Language");
		requestMap.put("otherTollbarParams", strOtherTollbarParams);
		HashMap treeMap = UITreeUtil.getToolbar(context,mappedTreeName,userRoleList,objectId,requestMap,language);
		
		String documentDropRelationship = "";
		String documentCommand = "";
		String showStatesInHeader = "";
		String showDescriptionInHeader = "";
		String imageDropRelationship = "";
		String imageManagerToolbar = "";
		String imageUploadCommand = "";
		String showUploadImageInPersonHeader = "";
		MapList menuMapList = UIToolbar.getChildren(treeMap);
		if(menuMapList.size() >0){
			HashMap categoryMenuHashMap = (HashMap)(menuMapList.get(0));
			documentDropRelationship = UIToolbar.getSetting(categoryMenuHashMap,"DocumentDrop Relationship");
			documentCommand = UIToolbar.getSetting(categoryMenuHashMap,"Document Command");
			imageDropRelationship = UIToolbar.getSetting(categoryMenuHashMap,"ImageDrop Relationship");
			showStatesInHeader = UIToolbar.getSetting(categoryMenuHashMap,"Header Lifecycle"); 
			showDescriptionInHeader = UIToolbar.getSetting(categoryMenuHashMap,"Header Description"); 
			imageManagerToolbar = UIToolbar.getSetting(categoryMenuHashMap,"Image Manager Toolbar"); 
			imageUploadCommand = UIToolbar.getSetting(categoryMenuHashMap,"Image Upload Command");
			showUploadImageInPersonHeader = UIToolbar.getSetting(categoryMenuHashMap,"Header Image Upload");			
			if(UIUtil.isNullOrEmpty(showStatesInHeader) && UIUtil.isNotNullAndNotEmpty(headerLifecycle))
			{
				showStatesInHeader = headerLifecycle;
			}
		}		

		// String sMCSURL = request.getRequestURL().toString();		
		// sMCSURL	= sMCSURL.replace("/common/emxTree.jsp", "");
		String sMCSURL = Framework.getFullClientSideURL(request, response, "");
		
		JsonObject jsonCategories  	= UIToolbar.loadCategoryToolbar(context, treeMap, requestMap, null);
		JsonArrayBuilder jsonItemsBuilder = FrameworkUtil.getBuilderArrayClone(jsonCategories.getJsonArray("items"));
		String initargs[] = {};
		HashMap paramsJPO = new HashMap();
		paramsJPO.put("objectId", objectId);		
		paramsJPO.put("language", language);		
		paramsJPO.put("treeMenu", mappedTreeName);		
		paramsJPO.put("MCSURL", sMCSURL);
		paramsJPO.put("documentDropRelationship", documentDropRelationship);
		paramsJPO.put("documentCommand", documentCommand);
		paramsJPO.put("showStatesInHeader", showStatesInHeader);
		paramsJPO.put("showDescriptionInHeader", showDescriptionInHeader);
		paramsJPO.put("imageDropRelationship", imageDropRelationship);
		paramsJPO.put("timeZone", timeZone);
		paramsJPO.put("imageManagerToolbar", imageManagerToolbar);
		paramsJPO.put("imageUploadCommand", imageUploadCommand);
		paramsJPO.put("showUploadImageInPersonHeader", showUploadImageInPersonHeader);
	
		if(UIUtil.isNotNullAndNotEmpty(objectId)){
			
			StringBuilder sbHeaderContents	= (StringBuilder)JPO.invoke(context, "emxExtendedHeader", initargs, "getHeaderContents", JPO.packArgs (paramsJPO), StringBuilder.class); 		
			JsonObjectBuilder jsonHeader = BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder());
			jsonHeader.add("isHeader", "true");
			jsonHeader.add("text", sbHeaderContents.toString());
			jsonItemsBuilder.add(jsonHeader.build());
		}
		
		JsonArray jsonItems = jsonItemsBuilder.build();
		JsonObjectBuilder jsonCategoriesBuilderClone = FrameworkUtil.getBuilderObjectClone(jsonCategories);
		jsonCategoriesBuilderClone.add("items",jsonItems);
		
		if(UIUtil.isNotNullAndNotEmpty(defaultCategory)){
			jsonCategoriesBuilderClone.add("defaultObjCategoryName", defaultCategory);
		}
		
       %>
        
        var catObj = <%= jsonCategoriesBuilderClone.build() %>
        if(getTopWindow().setUWAPref){
       getTopWindow().setUWAPref('objectId','<%=XSSUtil.encodeForURL(context, objectId)%>');
	   getTopWindow().setUWATitle('<%=XSSUtil.encodeForJavaScript(context, treelabel)%>');
    	}
       <%  	 
       
    }
	
	dobc = true;
if("structure".equals(treemode)){
	String isRoot = emxGetParameter(request,"isRoot");
	treeNodeUrl += "&isRoot=" + XSSUtil.encodeForURL(context, isRoot) + "&treemode=structure"; 
	dobc = false;
}

}
//escape the double quote
treelabel	= FrameworkUtil.findAndReplace(treelabel,"\"","\\\"");
bcLabel		= FrameworkUtil.findAndReplace(bcLabel,"\"","\\\"");

/********Shortcut********/

String recentlyViewLength = EnoviaResourceBundle.getProperty(context, "emxFramework.Shortcut.RecentlyViewedList.count");
String id = objectId;

if(UIUtil.isNullOrEmpty(id) ){
	id = relId+"|"+sTreeMenuName+"|"+ treelabel;
}
%>
try{
getTopWindow().updateShortcutMap("recentlyViewed", "<xss:encodeForJavaScript><%=id%></xss:encodeForJavaScript>");

if(getTopWindow().jQuery.inArray("facet0", getTopWindow().shortcut) == -1){
	getTopWindow().shortcut.push("facet0");
}
getTopWindow().refreshShortcut();
}catch(e){}
/********Shortcut********/
<%
 if(UIUtil.isNotNullAndNotEmpty(objectId)){
 		String strTitle = UIUtil.getWindowTitleName(context, null,objectId, null, true);%>
 		if(getTopWindow().document.location.href.indexOf("emxNavigatorDialog.jsp") >= 0){
 			var strTitle = "<xss:encodeForJavaScript><%=strTitle%></xss:encodeForJavaScript>";
 			if(strTitle.indexOf("$<APPNAME>") != -1){
 				getTopWindow().document.title = strTitle.replace("$<APPNAME>", getContentWindow().applicationProductInfo.appName);
 			} else {
 				getTopWindow().document.title = strTitle;
 			}
 		}
 <%}%>
 
var jsonOpts = null;
if(getTopWindow() != self){
var objStructureTree = getTopWindow().objStructureTree? getTopWindow().objStructureTree : getTopWindow().initStructureTree();
var objStructureFancyTree = getTopWindow().objStructureFancyTree;
var nodeExists=objStructureFancyTree.getNodeById("<%=XSSUtil.encodeForJavaScript(context, objectId)%>");
var currObjId = "<%=XSSUtil.encodeForJavaScript(context, objectId)%>";
var objDefaultCategory = "<%=defaultCategory%>";
var cntFrame = findFrame(getTopWindow(), "content");
var cntFrameURL = cntFrame.location.href;
var appInfoJSONObj;
var switchContextTreeData = localStorage.getItem('SC~'+currObjId);
if(<%=refreshTree%> && !nodeExists) {
	objStructureFancyTree.destroy();
<%
StringBuffer treeDisplayURL = null;
	if (isThereStructure && !"structure".equals(treemode)){
	    String appendParams = emxGetQueryString(request);
	    treeDisplayURL = new StringBuffer(80);
	    treeDisplayURL.append("emxTreeInnerFrame.jsp?mode=");
	    treeDisplayURL.append(XSSUtil.encodeForURL(context, mode));
	    treeDisplayURL.append("&treemode=structure");
	    treeDisplayURL.append("&");
	    treeDisplayURL.append(XSSUtil.encodeURLwithParsing(context, appendParams));
	    
	    
	    
	    UIMenu emxTreeObject = new UIMenu();
	    Vector userRoleList = PersonUtil.getAssignments(context);

	    String structureMenuObj = "";
	    if(sTreeMenuName == null || sTreeMenuName.trim().length()==0){
		structureMenuObj=UITreeUtil.getTreeMenuName(application, session, context, objectId, emxSuiteDirectory);
	    }else{
		structureMenuObj=sTreeMenuName;
	    }

	    HashMap topStructureMenuMap = emxTreeObject.getMenu(context, structureMenuObj);
	    String sStructureMenu = emxTreeObject.getSetting(topStructureMenuMap, "Structure Menu");
	  
	    //Added for show parent heirarchy enhancement
	    boolean showParentHierarchy = false;
		MapList structureTreeMenuCmdList = UIMenu.getOnlyCommands(context, sStructureMenu, userRoleList);
		
		if(structureTreeMenuCmdList!= null && structureTreeMenuCmdList.size()>0){
			HashMap command = (HashMap) structureTreeMenuCmdList.get(0);
			
			String showParentHie 	= UIComponent.getSetting(command,"ShowParentHierarchy"); //Added for enhancements
			showParentHierarchy 	= UIUtil.isNotNullAndNotEmpty(showParentHie) && "true".equalsIgnoreCase(showParentHie);
		}
	    
	    treeDisplayURL.append("&structureTreeName=");
	    treeDisplayURL.append(sStructureMenu);
	    Tree treeObj = new Tree(context);
		     treeObj.setStructureMenuName(sTreeMenuName);
		     treeObj.setApplication(application);
		     treeObj.setSession(session);
		     treeObj.setObjectId(objectId);
		     treeObj.setEmxSuiteDirectory(emxSuiteDirectory);
		     treeObj.setStructureTreeMenuName(sStructureMenu);
		     treeObj.setRequest(request);
		     treeObj.setRelId(relId);

		     strucTree=UITreeUtil.createFancyTree(treeObj);
%>
		jsonOpts = {
				structureTree : "<%=XSSUtil.encodeForJavaScript(context, strucTree.toString())%>" ,
				showParentHierarchy: "<%=showParentHierarchy%>"
		};
		
		objStructureFancyTree.init(jsonOpts);	
<%
	}
%>
}else if(<%=refreshTree%> && nodeExists){
    objStructureFancyTree.expandAndFocusNode("<%=XSSUtil.encodeForJavaScript(context, objectId)%>", "<%=XSSUtil.encodeForJavaScript(context, treelabel)%>");
}
if(jsonOpts==null && nodeExists) {
	jsonOpts=getTopWindow().bclist.getCurrentBC().fancyTreeData;
}
if(switchContextTreeData){
	jsonOpts = JSON.parse(switchContextTreeData);
	objStructureFancyTree.reInit(jsonOpts, true);
	localStorage.removeItem('SC~'+currObjId);
}
<%if(!"structure".equals(treemode) && ( mode==null || mode.trim().length()==0 || mode.equals("replace"))){%>
   if(objStructureTree != null){
        objStructureTree.clear();
        //objStructureTree.clear();
        objStructureTree.structureMode="";
        objStructureTree.menuName="";
        objStructureTree.selectedStructure="";
        objStructureTree.root=null;
    }
<%}%>
<%if(dobc){%>
	emxUICore.addToPageHistory("<%=XSSUtil.encodeForURL(context, treelabel)%>", "<%=treeNodeUrl%>", "<%=XSSUtil.encodeForHTML(context, sTreeMenuName)%>", "", "<%=XSSUtil.encodeForHTML(context, targetLocation)%>", "<%=XSSUtil.encodeForHTML(context, suiteKey)%>", "tree");
<%if( mode==null || mode.trim().length()==0 || mode.equals("replace")){%>
	if(getTopWindow().bclist != null && getTopWindow().bclist.getCurrentBC() != null && getTopWindow().bclist.getCurrentBC().id != "MyDesk" && getTopWindow().bclist.getCurrentBC().id != currObjId) {
       getTopWindow().bclist.clear();
    }
<%}%>

<%
String fromhomepage = emxGetParameter(request, "fromhomepage");
if(!"true".equalsIgnoreCase(fromhomepage)){
	String objAppName = "ENOBUPS_AP";
	if (!FrameworkLicenseUtil.isCPFUser(context)) {
		objAppName = "ENO_AEF_TP";
	}

    String licobjapplist = "";
	String regsuite = UIComponent.getSetting(UIMenu.getMenu(context, mappedTreeName), "Registered Suite");
	StringList appList = PersonUtil.getLicenseProductsBySuiteKey(regsuite);
	boolean gotLiceApp = false;
	for(int i =0; i<appList.size();i++){
		try{
			String app = (String)appList.get(i);
			FrameworkLicenseUtil.checkLicenseReserved(context, app,"");
			if(!gotLiceApp){
				gotLiceApp = true;
				objAppName = app;
			}
			licobjapplist += app + ",";
		}catch(Exception e){
				
		}
	}
	if("".equals(licobjapplist)){
	    licobjapplist = "ENOBUPS_AP";
	    if (!FrameworkLicenseUtil.isCPFUser(context)) {
	    	licobjapplist = "ENO_AEF_TP";
	    }
	}

	try {
			Map	appMap = UIMenu.getProductMenu(context, "My Desk", objAppName);
			String appName = (String) appMap.get(UIMenu.KEY_PRODUCT_NAME);
			String brandName = "";
			if(appName != null && !"".equals(appName)){
				brandName = appName.substring(0, appName.indexOf(" "));
				appName = appName.substring(appName.indexOf(" ") + 1);
			}
			ArrayList menuList = (ArrayList)appMap.get(UIMenu.KEY_PRODUCT_MENUS);
%>
		getTopWindow().jQuery('#navBar').empty();
		var shortcutTooltip =getTopWindow().emxUIConstants.STR_SHORTCUTS;
		var appMenuDiv = getTopWindow().jQuery("<div id='appMenu' class=''></div>");
		var appMenuButton = getTopWindow().jQuery("<button id='buttonAppMenu' onclick='showAppMenu();' class='btn mydesk' title='"+getTopWindow().myDeskMenuLabel+"'></button>");
		var fonticonMenu = getTopWindow().jQuery("<span class='fonticon fonticon-menu' style='div#ExtpageHeadDiv button.btn.mydesk:inherit;'></span>");
		appMenuDiv.append(appMenuButton);
		appMenuButton.append(fonticonMenu);
		var shortcutMenuDiv = getTopWindow().jQuery("<div id='' class='tab-bar button-bar'></div>");
		var appShortcutButton = getTopWindow().jQuery("<button id='shortcutAppMenu' onclick='showShortcutPanel();' class='btn shortcut' title='"+shortcutTooltip+"'></button>");
		var shortcutIcon =getTopWindow().jQuery("<span title='"+shortcutTooltip+"' class='fonticon fonticon-forward site-icon'></span>");
		shortcutMenuDiv.append(appShortcutButton);
		appShortcutButton.append(shortcutIcon);
		var nav = getTopWindow().jQuery('#navBar');
		nav.append(appMenuDiv);
		nav.append(shortcutMenuDiv);
		if(getTopWindow().changeProduct && !getTopWindow().getWindowOpener() && !isNavigatorDialog(parent.location.href)){
			var appsMenuList = new Array();
<%
		for(int i=0;i<menuList.size();i++){
%>
			appsMenuList[<%=i%>] = 'JF_Desk';//add by caipan 20240809 <%=(String)menuList.get(i)%>
<%
		}
%>
			getTopWindow().changeProduct('<%=brandName%>', '<%=appName%>', '<%=objAppName%>', appsMenuList, '<%=licobjapplist%>');
			appInfoJSONObj = { "brandName" : "<%=brandName%>", 
							"appName" : "<%=appName%>",
							"appTrigram" : "<%=objAppName%>",
							"appMenuList" : appsMenuList,
							"licensedAppList"  : "<%=licobjapplist%>"
				  };
			getTopWindow().setApplicationProductInfo(appInfoJSONObj);				  
		}
<%
	} catch(Exception e){
		}
}

  ContextUtil.commitTransaction(context);  // end write transaction

if(treeDisplayURL == null) {%>
	if(getTopWindow().bclist.getCurrentBC() && getTopWindow().bclist.getCurrentBC().id == currObjId) {
    	getTopWindow().loadBreadCrumbPage("<%=XSSUtil.encodeForURL(context, objectId)%>", "<%=XSSUtil.encodeForURL(context, treelabel)%>", ":", "<%=XSSUtil.encodeForJavaScript(context, bcLabel)%>", null, "<%=treeNodeUrl%>", true, "<%=XSSUtil.encodeForJavaScript(context, XSSUtil.decodeFromURL(treelabel))%>", catObj); 
	} else {
	getTopWindow().loadBreadCrumbPage("<%=XSSUtil.encodeForURL(context, objectId)%>", "<%=XSSUtil.encodeForURL(context, treelabel)%>", ":", "<%=XSSUtil.encodeForJavaScript(context, bcLabel)%>", null, "<%=treeNodeUrl%>", null, "<%=XSSUtil.encodeForJavaScript(context, XSSUtil.decodeFromURL(treelabel))%>", catObj, jsonOpts, appInfoJSONObj);
	}
<%} else {%>
    <%if("insert".equals(mode)){%>
    var url = (getTopWindow().bclist && getTopWindow().bclist.getCurrentBC()) ? getTopWindow().bclist.getCurrentBC().structureURL : null;
    url = url ? url : "<%=treeDisplayURL.toString()%>";
    if(getTopWindow().bclist.getCurrentBC() && getTopWindow().bclist.getCurrentBC().id == currObjId) {
    	getTopWindow().loadBreadCrumbPage("<%=XSSUtil.encodeForURL(context, objectId)%>", "<%=XSSUtil.encodeForURL(context, treelabel)%>", ":", "<%=XSSUtil.encodeForJavaScript(context, bcLabel)%>", url, "<%=treeNodeUrl%>", true, "<%=XSSUtil.decodeFromURL(treelabel)%>", catObj,jsonOpts);
    	if(objDefaultCategory != null && objDefaultCategory != "null" && objDefaultCategory!= 'undefined'){
    		getTopWindow().showCategoryTree();
    	}
    } else {
getTopWindow().loadBreadCrumbPage("<%=XSSUtil.encodeForURL(context, objectId)%>", "<%=XSSUtil.encodeForURL(context, treelabel)%>", ":", "<%=XSSUtil.encodeForJavaScript(context, bcLabel)%>", url, "<%=treeNodeUrl%>", null, "<%=XSSUtil.decodeFromURL(treelabel)%>", catObj,jsonOpts,appInfoJSONObj);
    }
    <%} else {%>
		if(getTopWindow().bclist.getCurrentBC() && getTopWindow().bclist.getCurrentBC().id == currObjId) {
			getTopWindow().loadBreadCrumbPage("<%=XSSUtil.encodeForURL(context, objectId)%>", "<%=XSSUtil.encodeForURL(context, treelabel)%>", ":", "<%=XSSUtil.encodeForJavaScript(context, bcLabel)%>", "<%=treeDisplayURL.toString()%>", "<%=treeNodeUrl%>", true, "<%=XSSUtil.encodeForJavaScript(context, XSSUtil.decodeFromURL(treelabel))%>", catObj,jsonOpts);
		} else {
    getTopWindow().loadBreadCrumbPage("<%=XSSUtil.encodeForURL(context, objectId)%>", "<%=XSSUtil.encodeForURL(context, treelabel)%>", ":", "<%=XSSUtil.encodeForJavaScript(context, bcLabel)%>", "<%=treeDisplayURL.toString()%>", "<%=treeNodeUrl%>", null, "<%=XSSUtil.encodeForJavaScript(context, XSSUtil.decodeFromURL(treelabel))%>", catObj,jsonOpts,appInfoJSONObj);
		}
    <%}%>       
<%}%>
var currentBC = getTopWindow().bclist.getCurrentBC();
if( currentBC.id === "null" && cntFrameURL.indexOf('relId=') != -1 && cntFrameURL.indexOf('treeLabel=') != -1 ){
	<% if(UIUtil.isNotNullAndNotEmpty(relId) && UIUtil.isNotNullAndNotEmpty(sTreeLabel)){ %>
			cntFrameURL=updateURLParameter(cntFrameURL, "relId", "<%=XSSUtil.encodeForURL(context,relId)%>");
			cntFrameURL=updateURLParameter(cntFrameURL, "treeLabel", "<%=XSSUtil.encodeForJavaScript(context, sTreeLabel)%>");
			currentBC.treeURL = cntFrameURL;
	<%	} %>
	
}else{
	currentBC.treeURL = cntFrameURL;
}
<%} else {%>
<%  if (!noTree) { %>

var contentFrame = findFrame(getTopWindow(), "content");
var matchFound = false;
var curTabNum = 0;
for(var ii = 0; ii < 5; ii++){
	var tmpTab = contentFrame.document.getElementById("unique"+ii);
	if(tmpTab.style.display == 'block'){
		curTabNum = ii;
	}
}
var displayDetailsFrame = findFrame(getTopWindow(), "detailsDisplay"+curTabNum);
displayDetailsFrame.name = "detailsDisplay";
displayDetailsFrame.document.location.href = "<%=treeNodeUrl%>";
<%  } else { %>
var contentFrame = findFrame(getTopWindow(), "content");
contentFrame.document.location.href = "<%=treeNodeUrl%>";
<%  }%>
//loading of category tab based on the structure tree load
if(catObj){
	getTopWindow().emxUICategoryTab.destroy();
	catObj.category = "<%=XSSUtil.encodeForJavaScript(context, bcLabel)%>";
	catObj.objectId = "<%=XSSUtil.encodeForJavaScript(context, objectId)%>";
	getTopWindow().emxUICategoryTab.init(catObj);
	var currbc = getTopWindow().bclist.getCurrentBC();
	currbc.categoryObj = currbc && !currbc.categoryObj ? catObj : currbc.categoryObj;
}		
<%}%>
}else{
	getTopWindow().location.href = "emxNavigator.jsp?isPopup=true&mode=tree&"+"<%=XSSUtil.encodeURLwithParsing(context, emxGetQueryString(request))%>";
}

if(!getTopWindow().isPanelOpen){
	getTopWindow().togglePanel(); 
}
</script>
</head>
</html>
