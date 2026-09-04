<%--  emxForm.jsp - Handling of FORM - EDIT and VIEW modes

   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,Inc.
   Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxForm.jsp.rca 1.28 Wed Oct 22 15:48:22 2008 przemek Experimental przemek $
--%>

<%@include file = "emxNavigatorInclude.inc"%>

<html>
<head>
<%@include file = "emxNavigatorTopErrorInclude.inc"%>
<%@include file = "emxFormConstantsInclude.inc"%>
<%@include file = "emxUIConstantsInclude.inc"%>
<%@page import="com.matrixone.apps.framework.ui.UIUtil,com.matrixone.apps.framework.ui.UIComponent" %>
<script src="emxFormConstantsJavascriptInclude.jsp" type="text/javascript"></script>

<%
HashMap requestMap = UINavigatorUtil.getRequestParameterMap(pageContext);
String objectId = (String) requestMap.get("objectId");
String accessResult = null;
boolean hasReadAccess = true;
	if(UIComponent.isShowAccessEnabled(context) && UIUtil.isNotNullAndNotEmpty(objectId)){
		hasReadAccess = UIComponent.hasReadAccess(context, objectId);
	}
	if(!hasReadAccess){
		%>
		<!-- //XSSOK -->
		<jsp:forward page="emxTreeNoDisplay.jsp" > <jsp:param name ="objectId" value  ="<%=objectId%>" />
     	</jsp:forward>
	<%
	}else{
	String HelpMarker = emxGetParameter(request, "HelpMarker");
	String actionBar = emxGetParameter(request, "actionBarName");
	String tipPage = emxGetParameter(request, "TipPage");
	StringBuffer appendParams = new StringBuffer(100);

	String appendURL = (String) requestMap.get("appendURL");
	if(appendURL != null && !"".equalsIgnoreCase(appendURL)) {
		HashMap requestMapNew = UINavigatorUtil.appendURLParams(context, (HashMap)requestMap.clone(), "Form");
		StringBuffer paramlist = new StringBuffer();
		Iterator itr = requestMapNew.keySet().iterator();
		int i = 0;
		while(itr.hasNext()){
		    String key = (String)itr.next();
		    if(!requestMap.containsKey(key)) {
			    if(i > 0) {
			    	paramlist.append("&");
			    }
			    try {
			    paramlist.append(key).append("=").append(XSSUtil.encodeForURL(context, (String)requestMapNew.get(key)));
			    i++;
			    }catch(Exception e){
		
			    }
		    }
		}
		appendParams.append(paramlist.toString());
		requestMap = requestMapNew;
	}

	String mode = emxGetParameter(request, "mode");
	String targetLocation = emxGetParameter(request, "targetLocation");
	String slideinType = emxGetParameter(request, "slideinType");
	String showTabHeader = emxGetParameter(request, "showTabHeader");
	String form = (String) requestMap.get("form");
	String accessUsers = MqlUtil.mqlCommand(context, "print form $1 select $2 dump", form, "property[AccessUsers].value");
	if(UIUtil.isNotNullAndNotEmpty(accessUsers)) {
		if(!PersonUtil.hasAnyAssignment(context, accessUsers)) {
	    	return;
		}
	}
	String portalMode = (String) requestMap.get("portalMode");//emxGetParameter(request, "portalMode");
	String parentOID = (String) requestMap.get("parentOID");
	String objectName = (String) requestMap.get("objectName");
	String relId = (String) requestMap.get("relId");//emxGetParameter(request, "relId");
	String header = (String) requestMap.get("formHeader");//emxGetParameter(request, "formHeader");
	String subHeader = (String) requestMap.get("subHeader");//emxGetParameter(request, "formHeader");
	String originalHeader=(String) requestMap.get("formHeader");//emxGetParameter(request, "formHeader");
	
	// iNoOfToolbars is used to decide the  frame size for the Header
	String strTitle="";
	String toolbar = (String) (String)emxGetParameter(request,"toolbar");
	int iNoOfToolbars = 1;
	if ( (toolbar != null) || (!"null".equalsIgnoreCase(toolbar)))
	{
	    matrix.util.StringList strList = com.matrixone.apps.domain.util.FrameworkUtil.split(toolbar,",");
	
	    iNoOfToolbars = strList.size();
	
	    if(iNoOfToolbars == 0)
	    {
	        iNoOfToolbars = 1;
	    }
	}
	
	
	String timeStamp = (String) requestMap.get("timestamp");//emxGetParameter(request, "timestamp");
	String suiteKey = (String) requestMap.get("suiteKey");//emxGetParameter(request, "suiteKey");
	String registeredSuite = suiteKey;
	String findMxLink = (String) requestMap.get("findMxLink");//emxGetParameter(request, "findMxLink");
	
	//This Param will use an alternative stylesheet for displaying the page
	String altCSS = (String) requestMap.get("altCSS");//emxGetParameter(request, "altCSS");
	
	// The objectId , relId from the emxTable.jsp is passd as "emxTableRowId"
	String tableRowIdList[] = Request.getParameterValues(request, "emxTableRowId");
	String frmLanguage = request.getHeader("Accept-Language");
	
	//String tableRowId = "";
	String parsedHeader = "";
	
	if (timeStamp == null || timeStamp.trim().length() == 0)
	{
	    timeStamp = UIComponent.getTimeStamp();
	}
	
	String transactionType = emxGetParameter(request, "TransactionType");
	boolean updateTransaction = (transactionType != null && transactionType.equalsIgnoreCase("update"));
	
	try {
	    ContextUtil.startTransaction(context, updateTransaction);
	    Vector userRoleList = PersonUtil.getAssignments(context);
	
	    // If the From is invoked from the emxTable page, then
	    // the object id will be in the format of - "relId|busId,relId2|busId2.."
	    // Get the first parameter and then parse it to get objectId
		if (tableRowIdList != null && tableRowIdList.length > 0)
	    {
	        for (int ii = 0; ii < tableRowIdList.length; ii++)
	        {
	            String[] tokens = tableRowIdList[ii].trim().split("[|]");
	
	            if (ii == 0)
	            {
	                if (tokens.length > 1)
	                {
	                    relId = tokens[0];
	                    objectId = tokens[1];
	
	                    appendParams.insert(0, "relId=" + XSSUtil.encodeForURL(context,relId) + "&");
	                }
	                else if (tokens.length == 1)
	                {
	                    objectId = tokens[0];
	                }
	                // Prepend the objectId parameter to the request params:
	                appendParams.insert(0, "objectId=" +  XSSUtil.encodeForURL(context,objectId) + "&");
	
	                // Start appending the rowIds as a comma-separated string:
	                appendParams.append("&rowIds=");
	                appendParams.append(XSSUtil.encodeForURL(context,objectId));
	            }
	            else
	            {
	                appendParams.append(",");
	                appendParams.append( XSSUtil.encodeForURL(context,tokens[tokens.length > 1 ? 1 : 0]));
	            }
	        }
	    }
	
	    if ( (form == null) || (form.equals("")))
	        emxNavErrorObject.addMessage(MSG_INVALID_FORM_NAME);
	
	
	    parsedHeader = UIForm.getFormHeaderString(context, pageContext, header, objectId, suiteKey, frmLanguage);
	    if( UIUtil.isNotNullAndNotEmpty(mode) && mode.compareToIgnoreCase("edit")==0){
		    strTitle = UIUtil.getWindowTitleName(context, null,objectId, EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.WindowTitle.Edit",context.getLocale()), true);
	    }
	    else{
			strTitle = UIUtil.getWindowTitleName(context, null,objectId, null, true);
	    }
	    if ( altCSS != null && !"".equals(altCSS) && !"null".equals(altCSS)){
	      appendParams.append("&altCSS=");
	      appendParams.append(XSSUtil.encodeForURL(context, altCSS));
	    }
		String addCustAttributes= emxGetParameter(request, "addCustAttributes");
	    if (UIUtil.isNotNullAndNotEmpty(addCustAttributes)){
		      appendParams.append("&addCustAttributes=");
		      appendParams.append(XSSUtil.encodeForURL(context,addCustAttributes));
		    }
	    appendParams.append("&originalHeader=");
	    appendParams.append(XSSUtil.encodeForURL(context, originalHeader));
	    appendParams.append("&parsedHeader=");
	    appendParams.append(XSSUtil.encodeForURL(context, parsedHeader));
	    appendParams.append("&timeStamp=");
	    appendParams.append( XSSUtil.encodeForURL(context,timeStamp));
	    appendParams.append("&findMxLink=");
	    appendParams.append(XSSUtil.encodeForURL(context, findMxLink));
	    appendParams.append("&objectName=");
	    appendParams.append(XSSUtil.encodeForURL(context, objectName));
	
	    if(!"edit".equalsIgnoreCase(mode)){
	        appendParams.append("&submitMethod=");
	        appendParams.append(XSSUtil.encodeForURL(context, request.getMethod()));
	    }
	
	} catch (Exception ex) {
	    ContextUtil.abortTransaction(context);
	    if (ex.toString() != null && (ex.toString().trim()).length() > 0)
	        emxNavErrorObject.addMessage(ex.toString().trim());
	
	} finally {
	    ContextUtil.commitTransaction(context);
	}
	
	boolean showPortal = false;
	if(portalMode != null && "true".equals(portalMode)) {
		showPortal = true;
	}
	boolean showHeader = true;
	if((showTabHeader == null ||!"true".equalsIgnoreCase(showTabHeader)) && showPortal ){
	    showHeader= false; 
	}
	String editDisplayURL = "emxFormEditDisplay.jsp?" + appendParams.toString() + "&portalMode=" + XSSUtil.encodeForURL(context, portalMode)+ "&fromForm=true";
	String viewDisplayURL = "emxFormViewDisplay.jsp?" + appendParams.toString();
		
	String editFooterURL = "emxFormEditFooter.jsp?" + appendParams.toString();
	editFooterURL += "&slideinType=" + XSSUtil.encodeForURL(context, slideinType) + "&targetLocation=" + XSSUtil.encodeForURL(context, targetLocation) + "&isSelfTargeted=" + XSSUtil.encodeForURL(context, emxGetParameter(request, "isSelfTargeted"));
	String slideinviewFooterURL = "emxFormViewFooterSlidein.jsp?" + appendParams.toString();
	String portaleditFooterURL = "emxFormPortalEditFooter.jsp?" + appendParams.toString();
	
	String bodyUrl = "";
	String footerURL = "";
	if("slidein".equals(targetLocation) && !"edit".equalsIgnoreCase(mode)){
		footerURL = slideinviewFooterURL;
	}else{
		footerURL = editFooterURL;
	}
	footerURL = FrameworkUtil.findAndReplace(footerURL,"'","\\'");
	
	String bodyframe = "";
	String hiddenFramename = "";
	if ( (mode != null) && mode.equalsIgnoreCase("edit")) {
		bodyUrl = editDisplayURL;
		//bodyframe = "formEditDisplay";
		hiddenFramename = "formEditHidden";
	} else {
		bodyUrl = viewDisplayURL;
		//bodyframe = "formViewDisplay";
		hiddenFramename = "formViewHidden";
	}
	header = UIForm.getFormHeaderString(context, pageContext, originalHeader, objectId, suiteKey, frmLanguage);
	if ( (subHeader != null) && (subHeader.trim().length() > 0) ) {
		subHeader = UINavigatorUtil.parseHeader(context, pageContext, subHeader, objectId, suiteKey, frmLanguage);
	}
	
	String printerFriendly = emxGetParameter(request, "PrinterFriendly");
	String export = emxGetParameter(request, "Export");
	String editLink = emxGetParameter(request, "editLink");
	if ( (mode != null) && mode.equalsIgnoreCase("edit")) {
		editLink = "false";
		printerFriendly = "false";
		export = "false";
	}
	
	%>
	<title><xss:encodeForHTML><%=strTitle%></xss:encodeForHTML></title>
	<script type="text/javascript">
	    var numtoolbars = <%=iNoOfToolbars%>;
	</script>
	  <script language="javascript" src="scripts/emxUIModal.js"></script>
	<script type="text/javascript">
		addStyleSheet("emxUIDefault");
		addStyleSheet("emxUIDOMLayout");
		
	<%
		if ( (mode != null) && mode.equalsIgnoreCase("edit")) {
	%>
			addStyleSheet("emxUIDialog");
	<%
		}
	
		if(showPortal) {
	%>
			addStyleSheet("emxUIChannelDefault");
	<%
		}
	%>
		addStyleSheet("emxUIToolbar");
	    addStyleSheet("emxUIMenu");
	<%
	if (mode != null && mode.equalsIgnoreCase("edit")) {
	%>
	    addStyleSheet("emxUIForm");
	<%
	} else {
	%>
	    addStyleSheet("emxUIProperties");
	<%    
	}
	%>
	    addStyleSheet("emxUIList");
	</script>
	<link rel="stylesheet" href="styles/emxUICalendar.css"></link>
	<script language="JavaScript" src="scripts/emxUICoreMenu.js"></script>
	<script language="JavaScript" src="scripts/emxUIActionbar.js"></script>
	<script language="JavaScript" src="scripts/emxUIToolbar.js"></script>
	<script language="javascript" src="scripts/emxNavigatorHelp.js"></script>
	
	<script language="javascript" src="../common/scripts/emxUIObjMgr.js"></script>
	<script language="JavaScript" src="../common/scripts/jquery-latest.js" type="text/javascript"></script>
	<script language="JavaScript" src="../common/scripts/emxUITableUtil.js" type="text/javascript"></script>
	<script language="javascript" src="../common/scripts/emxUIFormUtil.js" type="text/javascript"></script>
	<script language="JavaScript" src="../common/scripts/emxUIPopups.js" type="text/javascript"></script>
	<script language="javascript" src="scripts/emxUICalendar.js"></script>
	<script language="javascript" src="scripts/emxQuery.js"></script>
    <script type="text/javascript" src="../webapps/AmdLoader/AmdLoader.js"></script>
    <script type="text/javascript">window.dsDefaultWebappsBaseUrl = "../webapps/";</script>
    <script type="text/javascript" src="../webapps/WebappsUtils/WebappsUtils.js"></script>
    <script type="text/javascript" src="../webapps/c/UWA/js/UWA_W3C_Alone.js"></script> 
    <link rel="stylesheet" type="text/css" href="../webapps/UIKIT/UIKIT.css">
    <link rel="stylesheet" href="../plugins/libs/jqueryui/1.10.3/css/jquery-ui.css"/>
    <link rel="stylesheet" href="../common/styles/emxUIImageManagerInPlace.css"/>
	
	<%
	if ( altCSS != null && !"".equals(altCSS)){
	  if (altCSS.indexOf(".css") > -1)
	    altCSS = altCSS.substring(0,altCSS.indexOf(".css"));
	%>
	<script type="text/javascript">
	  addStyleSheet("<%= XSSUtil.encodeForURL(context, altCSS) %>");
	</script>
	<%
	}
	
	if (mode != null && mode.equalsIgnoreCase("edit")) {
		String preProcessJavaScript = emxGetParameter(request,"preProcessJavaScript");
	%>
	 <script>  
	  //To reposition the dynamic textarea div when window is resized .     
		emxUICore.addEventHandler(window, "resize", calculateXYCoordinates);
		//To apply the css if the application is opened in IE for Dynamic textarea. 
		emxUICore.addEventHandler(window, "load", applyCss);
	    </script>
	
	<%
	    String typeAheadEnabled = "true";
	    try
	    {
	        typeAheadEnabled = EnoviaResourceBundle.getProperty(context, "emxFramework.TypeAhead");
	    }
	    catch (Exception e)
	    {
	    }
	    
	    
	    // add the type ahead javascript and style sheet if the feature is enabled
	    if ("true".equalsIgnoreCase(typeAheadEnabled))
	    {
	%>
	<script language="javascript" src="scripts/emxTypeAhead.js"></script>
	<%
	    }
	    
	    StringList incFileList = UIForm.getJSValidationFileList(context, suiteKey);
	    String fileTok = "";
	    for(StringItr keyItr = new StringItr(incFileList); keyItr.next();)
	    {
	        fileTok = keyItr.obj();
	        if(fileTok.endsWith(".jsp")) {
	%>
				<!-- XSSOK -->
	            <jsp:include page = "<%=fileTok%>" flush="true" />
	<%
	        } else if(fileTok.endsWith(".js")) {
	%>
	        <script language="javascript" src="<%=fileTok%>"></script><!-- XSSOK -->
	<%
	        }
	    }
	%>
	<script type="text/javascript" src="scripts/emxUIFormHandler.js"></script>
	
	<%@include file = "emxRTE.inc"%>
	
	<%
	}
	%>
	
	<script language="javascript">
		var popup = 'true';
		function submitPage(targetLocation)
		{
			var submitMultipleTimes = "false";
			var editFrom = this.document.forms["editDataForm"];
			if(editFrom != null && editFrom != undefined) {
				var submitMultipleTimesEle = this.document.forms["editDataForm"].submitMultipleTimes;
				if(submitMultipleTimesEle != null && submitMultipleTimesEle != undefined) {
					submitMultipleTimes = submitMultipleTimesEle.value;
				}
			}
	
			if(submitMultipleTimes == "true") {
				setFormSubmitAction(true);
			}
			
			if(targetLocation != "slidein"){
		    	//parent.formEditDisplay.document.forms["editDataForm"].isPopup.value = popup;
		        this.document.forms["editDataForm"].isPopup.value = popup;
		    }
			saveChangesECR(targetLocation);
		}
	
		function loadpage()
		{
			var toppage = getTopWindow().document.location.href;
			if(toppage.indexOf("emxForm.jsp") < 0)
			{
				popup = 'false';
				//parent.formEditDisplay.document.forms[0].isPopup.value = 'false';
				//parent.frames[1].document.forms[0].isPopup.value = 'false';
				var cnlImg = document.getElementById('cancelImage');
				var cnlTxt = document.getElementById('cancelText');
				if(cnlTxt && cnlImg){
		            cnlTxt.innerHTML = "";
		            cnlImg.innerHTML = "";
				}
				
			}
		}
	
	<%
		if ( (mode != null) && mode.equalsIgnoreCase("edit")) {
	%>
		emxUICore.addEventHandler(window, "load", loadpage);
	<%
		}
		else{ //listen to object change event
	%>
	  require(["UWA/Utils/InterCom"], function(InterCom){
			"use strict";
			var formSocket = new InterCom.Socket(); 
			formSocket.subscribeServer('enovia.bus.server', getTopWindow());   
			formSocket.addListener('objectChanged', function (data) {
				var oid = data.oid;
				if(!oid || oid != "<%=objectId%>"){
					return;
				}
				window.location.href = window.location.href;
			});	
		});
	<%
		}
	%>
		function setWindowTitle(){
		  if(getTopWindow().document.location.href.indexOf("emxNavigatorDialog.jsp") >= 0){
			var strTitle = "<xss:encodeForJavaScript><%=strTitle%></xss:encodeForJavaScript>";
			 if(strTitle.indexOf("$<APPNAME>") != -1){
				 getTopWindow().document.title = strTitle.replace("$<APPNAME>", getContentWindow().applicationProductInfo.appName);
	 			} else {
	 				getTopWindow().document.title = strTitle;
	 			}
		  }
		}
		 if(getTopWindow().isMobile || getTopWindow().isPCTouch){
		 		addStyleSheet("emxUIMobile", '../common/mobile/styles/');
		 	 }	
	</script>
	<%@include file = "emxNavigatorBottomErrorInclude.inc"%>
	
	</head>
	<%
	String bdcls = "";
	String stronload = "";
		
	if ( (mode != null) && mode.equalsIgnoreCase("edit")) {
		stronload = "JavaScript:loadFramesNew('" + bodyUrl;
	
		String searchMode = emxGetParameter(request, "searchmode");
		String preProcessJavaScript = emxGetParameter(request,"preProcessJavaScript");
		if(preProcessJavaScript == null || "".equals(preProcessJavaScript)){
			preProcessJavaScript = "";
		}
	
		stronload += "', '" + XSSUtil.encodeForJavaScript(context,preProcessJavaScript);
		if (searchMode != null && searchMode.equals("globalsearch")) {
			stronload += "', true);";
		} else {
			stronload += "');";
		}
	
		bdcls = "editable ";
	if("slidein".equals(targetLocation)){
				if("wider".equals(slideinType)){
					bdcls += "dialog";
				} else {
			bdcls += "slide-in-panel";
				}
		}else{
			bdcls += " dialog";
		}
	}else{
		bdcls = "properties ";
		if("slidein".equals(targetLocation)){
			stronload = "JavaScript:loadFramesNew('" + bodyUrl + "');";
				if("wider".equals(slideinType)){
					bdcls += "dialog";
				} else {
			bdcls += " slide-in-panel";
				}
		}else{
			stronload = "JavaScript:loadFramesNew('" + bodyUrl + "');addOrHideHeaderItems();";
			bdcls += " no-footer";
		}
	}
	stronload += " closeAutoFilterSlideIn('"+XSSUtil.encodeForJavaScript(context,targetLocation)+"');";
	%>
	<!-- //XSSOK -->
	<body class="<%=bdcls%>" onload="setWindowTitle();<%=stronload%>" onunload="JavaScript:purgeViewFormData('<%= XSSUtil.encodeForURL(context,timeStamp) %>')">
	<div id="pageHeadDiv">
	   <%if((portalMode == "false") || (showHeader)){%>
	   <table id="contentHeader">
	     <tr>
	<%
	if(showHeader || "slidein".equals(targetLocation)) {
	%>     
	   <td class="page-title">
	      <h2><xss:encodeForHTML><%=header%></xss:encodeForHTML></h2>
	<%
	      if(subHeader != null && !"".equals(subHeader)) {
	%>
	        <h3><xss:encodeForHTML><%=subHeader%></xss:encodeForHTML></h3>
	<%
	        }
	%>
	    </td>
	<%
	}
	         String progressImage = "../common/images/utilProgressBlue.gif";
	         String processingText = UINavigatorUtil.getProcessingText(context, frmLanguage);
	 %>
	        <td class="functions">
	            <table><tr>
	                <td class="progress-indicator"><div id="imgProgressDiv"><%=processingText%></div></td>
	            </tr></table>
	        </td>
	        </tr>
	      </table>
	      <% 
	      }
	   %>
	<!-- XSSOK -->
	<jsp:include page = "emxToolbar.jsp" flush="true"> <jsp:param name="toolbar" value="<%=toolbar%>"/> <jsp:param name="objectId" value="<%=objectId%>"/> <jsp:param name="relId" value="<%=relId%>"/> <jsp:param name="parentOID" value="<%=objectId%>"/> <jsp:param name="timeStamp" value="<%=timeStamp%>"/> <jsp:param name="header" value="<%=header%>"/> <jsp:param name="uiType" value="form"/> <jsp:param name="form" value="<%=form%>"/> <jsp:param name="helpMarker" value="<%=HelpMarker%>"/> <jsp:param name="suiteKey" value="<%=registeredSuite%>"/> <jsp:param name="findMxLink" value="<%=findMxLink%>"/> <jsp:param name="topActionbar" value="<%=actionBar%>"/> <jsp:param name="tipPage" value="<%=tipPage%>"/> <jsp:param name="editLink" value="<%=editLink%>"/> <jsp:param name="PrinterFriendly" value="<%=printerFriendly%>"/> <jsp:param name="export" value="<%=export%>"/> <jsp:param name="mode" value="<%=mode%>"/> <jsp:param name="submitMethod" value="<%=request.getMethod()%>"/>    <jsp:param name="targetLocation" value="<%=targetLocation%>"/>
	</jsp:include>
	</div>
	        <div id="divPageBody">
	<jsp:include page = "<%=bodyUrl%>" flush="true"/> 
			</div>
	<%
	if ( (mode != null) && mode.equalsIgnoreCase("edit") || "slidein".equals(targetLocation)) {
	%>
	<%--addby chenyan 去除OOTB的底部按钮--%>
<%--	        <div id="divPageFoot">--%>
<%--		<jsp:include page="<%=footerURL%>" flush="true" />--%>
<%--			</div>--%>
	<%
		}
	%>
	</body>
	</html>
	<%
}
%>
