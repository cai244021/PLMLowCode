<%-- emxCommonDocumentCheckinDialogFS.jsp - used for Checkin of file into Document Object
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   emxCommonDocumentMultiFileUploadFS.jsp
   static const char RCSID[] = "$Id: emxCommonDocumentCheckinDialogFS.jsp.rca 1.21 Wed Oct 22 16:18:21 2008 przemek Experimental przemek $"
--%>
<%@page import="com.matrixone.apps.domain.util.EnoviaBrowserUtility.Browsers"%>
<%@page import="com.matrixone.apps.domain.util.FrameworkUtil"%>
<%@page import="com.matrixone.apps.common.util.DocumentUtil"%>
<link rel="stylesheet" type="text/css" href="../common/mobile/styles/emxUIMobile.css">
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "emxComponentsUtil.inc"%>
<%
    String strRowId = (String)emxGetParameter(request, "rowId");
  Map emxCommonDocumentCheckinData = (Map) session.getAttribute("emxCommonDocumentCheckinData");

  if(emxCommonDocumentCheckinData == null)
  {
    emxCommonDocumentCheckinData = new HashMap();
    session.setAttribute("emxCommonDocumentCheckinData", emxCommonDocumentCheckinData);

  }
    emxCommonDocumentCheckinData.put("rowId", strRowId);

    String objectAction =  (String)emxCommonDocumentCheckinData.get("objectAction");
  String objectId = (String) emxCommonDocumentCheckinData.get("objectId");
//  if ( objectAction == null || objectAction.equals("image") || objectAction.equals(CommonDocument.OBJECT_ACTION_CREATE_MASTER) )
  {
    Enumeration enumParam = request.getParameterNames();

    // Loop through the request elements and
    // stuff into emxCommonDocumentCheckinData
    String storeFromBL = DocumentUtil.getStoreFromBL(context, "Document");
	System.out.println("L48 Collab & approve emxCommonDocumentPreCheckin getStoreFromBL : " + storeFromBL);
	boolean storeFound = false;	
    while (enumParam.hasMoreElements())
    {
        String name  = (String) enumParam.nextElement();
        String value = emxGetParameter(request,name);
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
  }
  if ( objectAction == null )
  {
     objectAction =  (String)emxCommonDocumentCheckinData.get("objectAction");
  }
  String documentType = (String) emxCommonDocumentCheckinData.get("realType");
  emxCommonDocumentCheckinData.put("type", documentType);

  if(documentType == null)
  {
      documentType = CommonDocument.TYPE_DOCUMENT;
  }

  // put the document attribute values into formBean
  // since JPO expects the attributes in a map, stuff the formBean with attribute map
  // get the list of Attribute names
  MapList attributeMapList = mxType.getAttributes( context, documentType);

  Iterator i = attributeMapList.iterator();
  String attributeName = null;
  String attrValue = "";
  String attrType = "";
  double tz = Double.parseDouble((String) session.getAttribute ( "timeZone" ));
  Map attributeMap = new HashMap();
  while(i.hasNext())
  {
      Map attrMap = (Map)i.next();
      attributeName = (String)attrMap.get("name");
      attrValue = (String) emxCommonDocumentCheckinData.get(attributeName);
      attrType = (String)attrMap.get("type");
      if ( attrValue != null && !"".equals(attrValue) && !"null".equals(attrValue) )
      {
          if("timestamp".equals(attrType))
          {
             attrValue = eMatrixDateFormat.getFormattedInputDate(context, attrValue, tz,request.getLocale());
          }
          attributeMap.put( attributeName, attrValue);
      }
  }
  String accessType = (String)emxCommonDocumentCheckinData.get("AccessType");
  String accessAttrStr = PropertyUtil.getSchemaProperty(context, "attribute_AccessType");
  if ( accessType != null && !"".equals(accessType) && !"null".equals(accessType))
  {
      attributeMap.put( accessAttrStr, accessType);
  }

  // stuff the formBean with attribute map
  emxCommonDocumentCheckinData.put( "attributeMap", attributeMap);

  if (  objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_COPY_FROM_VC) ||
        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_FILE_FOLDER) ||
        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_STATE_SENSITIVE_CONNECT_VC_FILE_FOLDER) ||
        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONNECT_VC_FILE_FOLDER) ||
        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONVERT_CHECKIN_VC_FILE_FOLDER) ||
        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONVERT_VC_FILE_FOLDER) ||
        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ZIP_TAR_GZ) ||
        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ON_DEMAND))
    {
      //save type ahead values
      String typeAheadFormName = emxGetParameter(request, "typeAheadFormName");
      String tagDisplayValue = emxGetParameter(request, "path");
%>
      <emxUtil:saveTypeAheadValues
        context="<%= context %>"
        form="<%= typeAheadFormName%>"
        field="path"
        displayFieldValue="<%= XSSUtil.encodeForHTML(context, tagDisplayValue) %>"
        />
<%
      tagDisplayValue = emxGetParameter(request, "selector");
%>
      <emxUtil:saveTypeAheadValues
        context="<%= context %>"
        form="<%= typeAheadFormName%>"
        field="selector"
        displayFieldValue="<%= XSSUtil.encodeForXML(context, tagDisplayValue) %>"
        />
      <emxUtil:commitTypeAheadValues context="<%= context %>" />
<%     
      emxCommonDocumentCheckinData.put("showFormat", "false");
      String jpoName = (String)emxCommonDocumentCheckinData.get("JPOName");
      String methodName = (String)emxCommonDocumentCheckinData.get("vcMethodName");
      String selector = (String)emxCommonDocumentCheckinData.get("selector");
      if (selector == null || "".equals(selector) || "null".equals(selector) )
      {
        emxCommonDocumentCheckinData.put("selector","Trunk:Latest");
      }
		String storeFromBL = DocumentUtil.getStoreFromBL(context, "Document");
		System.out.println("L48 Collab & approve emxCommonDocumentCheckinDialogFS getStoreFromBL : " + storeFromBL);

      String server = (String)emxCommonDocumentCheckinData.get("server");
      if ((server != null) && !("".equals(server)) && !("null".equals(server)))
      {
        String symbolicName = FrameworkUtil.getAliasForAdmin(context, "store", server, true);
		if(!storeFromBL.isEmpty())
			symbolicName = storeFromBL;
			
        emxCommonDocumentCheckinData.put("store", symbolicName);
      }
      else
      	emxCommonDocumentCheckinData.put("store", storeFromBL);
      
      String[] args = JPO.packArgs(emxCommonDocumentCheckinData);
      if ( "".equals(objectId) || "null".equals(objectId) )
      {
        objectId = null;
      }
      if (jpoName == null || "".equals(jpoName) || "null".equals(jpoName) )
      {
        jpoName = "emxVCDocument";
      }
      if (methodName == null || "".equals(methodName) || "null".equals(methodName) )
      {
        methodName = "vcDocumentConnectCheckin";
      }
      
      // store the parameter when accessed from the previous page in the wizard
      String fromPage = (String)emxCommonDocumentCheckinData.get("fromPage");
      if(fromPage == null){
        fromPage = "";
      }
      String sDesignSyncError = (String)session.getAttribute("DesignSync.error");
      if(!fromPage.equals("previous") || "true".equals(sDesignSyncError))
      {
        if("true".equals(sDesignSyncError)) {
            session.removeAttribute("DesignSync.error");
        }
		FrameworkUtil.validateMethodBeforeInvoke(context, jpoName, methodName, "Program");
        Map objectMap = (Map)JPO.invoke(context, jpoName, null, methodName, args, Map.class);
        objectId = (String)objectMap.get("objectId");
        emxCommonDocumentCheckinData.put( "objectId", objectId);
      }
      else if ("previous".equalsIgnoreCase(fromPage) && objectId!= null){
        DomainObject object = new DomainObject();
        object.setId(objectId);
        String prevType = (String) object.getInfo(context,DomainConstants.SELECT_TYPE);
		String type = (String) emxCommonDocumentCheckinData.get("type");
        if(!prevType.equals(type)){
            object.deleteObject(context);
			FrameworkUtil.validateMethodBeforeInvoke(context, jpoName, methodName, "Program");
            Map objectMap = (Map)JPO.invoke(context, jpoName, null, methodName, args, Map.class);
            objectId = (String)objectMap.get("objectId");
            emxCommonDocumentCheckinData.put( "objectId", objectId);
        }
        else{
			FrameworkUtil.validateMethodBeforeInvoke(context, jpoName, "modifyObject", "Program");
            Map objectMap = (Map)JPO.invoke(context, jpoName, null, "modifyObject", args, Map.class);
        }
      }
  }


  String actionCommand = null;
  boolean isVersionable = true;
  if ( documentType != null && !"".equals(documentType) && !"null".equals(documentType) )
  {
      if( CommonDocument.TYPE_DOCUMENTS.equals(CommonDocument.getParentType(context, documentType)) )
      {
        CommonDocumentable commonDocument = (CommonDocumentable)DomainObject.newInstance(context,documentType);
        actionCommand = commonDocument.getCheckinCommand(context);
        if ( (objectId != null && !"".equals(objectId) && !"null".equals(objectId)) )
        {
            isVersionable = CommonDocument.allowFileVersioning(context, objectId);
        } else {
            isVersionable = CommonDocument.checkVersionableType(context, documentType);
        }
      } else
      {
          isVersionable = false;
      }
  }
  if( !isVersionable )
  {
      emxCommonDocumentCheckinData.put("isVersionable", Boolean.valueOf(isVersionable));
  }
  if ( !isVersionable && !CommonDocument.OBJECT_ACTION_CHECKIN_WITHOUT_VERSION.equals(objectAction) && !"image".equals(objectAction) )
  {
        emxCommonDocumentCheckinData.put("objectAction", CommonDocument.OBJECT_ACTION_CREATE_CHECKIN);
  }
      if ( actionCommand != null )
      {
          Map commandMap  = UICache.getCommand(context, actionCommand);
          String actionURL = UIMenu.getHRef(commandMap);
%>
              <!-- //XSSOK -->
          <form name="integration" action="<%=actionURL%>" >
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

  // check for this request parameter, this is set if the required version of
  // Java plug in is not found on the client machine
  String plugInNotFoundAlert = request.getParameter("plugInNotFoundAlert");
  if("true".equalsIgnoreCase(plugInNotFoundAlert))
  {
      emxCommonDocumentCheckinData.put("plugInNotFoundAlert", "true");
  }
  request.setAttribute("contentPageIsDialog", "true");
  String sHelpMarker = "emxhelpfileuploadnoapplet";
  String heading = (String) emxCommonDocumentCheckinData.get("header");
  if(heading == null || "".equals(heading) || "null".equals(heading)) {
      if (objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_COPY_FROM_VC) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_FILE_FOLDER) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ZIP_TAR_GZ) ||
	        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CHECKIN_VC_FILE) ||
	        objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CHECKIN_VC_FOLDER))
      {
        sHelpMarker = "emxhelpdsfacheckinpage";
      }

      if ( CommonDocument.OBJECT_ACTION_CREATE_MASTER.equalsIgnoreCase(objectAction) )
      {
          heading = "emxComponents.CommonDocument.Step2UploadFiles";
      } else if ( CommonDocument.OBJECT_ACTION_UPDATE_MASTER.equalsIgnoreCase(objectAction) ) {
          heading = "emxComponents.CommonDocument.UpdateFiles";
          sHelpMarker = "emxhelpfileupdate";
      } else if ( CommonDocument.OBJECT_ACTION_CREATE_MASTER_PER_FILE.equalsIgnoreCase(objectAction) ) {
          heading = "emxComponents.CommonDocument.UploadFilesToIndividualDocuments";
      } else if ( CommonDocument.OBJECT_ACTION_CHECKIN_WITH_VERSION.equalsIgnoreCase(objectAction) ) {
          heading = "emxComponents.CommonDocument.CheckinFiles";
      } else if (CommonDocument.OBJECT_ACTION_UPDATE_HOLDER.equalsIgnoreCase(objectAction) ) {
          heading = "emxComponents.Common.UpdateDocuments";
          sHelpMarker = "emxhelpfileupdate";
      } else if ("image".equalsIgnoreCase(objectAction) ) {
          heading = "emxComponents.ImageManager.UploadImages";
          sHelpMarker = "emxhelpfileupdate";
      } else {
          heading = "emxComponents.Common.CheckinDocuments";
      }
  }
  String pageHeading = ComponentsUtil.i18nStringNow(heading,request.getHeader("Accept-Language"));
  boolean showPrev = (CommonDocument.OBJECT_ACTION_CREATE_MASTER.equalsIgnoreCase(objectAction) || 
          			  CommonDocument.OBJECT_ACTION_CREATE_CHECKIN.equalsIgnoreCase(objectAction) || 
          			  VCDocument.OBJECT_ACTION_CREATE_VC_FILE_FOLDER.equalsIgnoreCase(objectAction));
  
  
  
%>
	<html>
    	<head>
		  	<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
			<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
			<script language="JavaScript" src="../common/scripts/emxUICoreMenu.js"></script>
			<script language="JavaScript" src="../common/scripts/emxUIToolbar.js"></script>
			<script language="JavaScript" src="../common/scripts/emxUIFilterUtility.js"></script>
			<script language="JavaScript" src="../common/scripts/emxUIActionbar.js"></script>
			<script language="JavaScript" src="../common/scripts/emxUIModal.js"></script>
			<script language="JavaScript" src="../common/scripts/emxNavigatorHelp.js"></script>
			<script language="JavaScript" src="../emxUIPageUtility.js"></script>
			<script language="JavaScript" src="../common/scripts/emxUIBottomPageJavaScriptInclude.js"></script>
			<script language="JavaScript" type="text/JavaScript">
				addStyleSheet("emxUIDefault");    
				addStyleSheet("emxUIToolbar");    
				addStyleSheet("emxUIMenu");    
				addStyleSheet("emxUIDOMLayout");
				addStyleSheet("emxUIDialog");
				function toggleMaxInactiveInterval(){
					jQuery.ajax({
						url: '../components/emxCommonDocumentSetMaxInactiveInterval.jsp',
						type: 'GET',
						cache: false,
						contentType: false,
						processData: false
					});  
				}
                function UploadFileAndFlushCell() {
                    var parentDoc = getTopWindow().getWindowOpener().window;
                    const rowId = "<%=strRowId%>";
                    console.log("rowId:",rowId)
                    this.frames['checkinFrame'].checkinFile(rowId)
                    var JF_PCRFunction=parentDoc.document.getElementById("JF_PCRFunction");
                    var JF_Participant=parentDoc.document.getElementById("JF_Participant");
                    console.log("JF_PCRFunction:",JF_PCRFunction);
                    console.log("parentDoc:",parentDoc);
                    if(JF_PCRFunction||JF_Participant){
                        var reloadURL = parentDoc.document.location.href;
                        parentDoc.document.location.href=reloadURL;
                        setTimeout(function() {
                            window.close();
                        }, 1000);
                    }

                }
			</script>
			<link rel="shortcut icon" href="../favicon.ico" type="image/x-icon" />
  		</head>
    	<%
		boolean isIE = EnoviaBrowserUtility.is(request,Browsers.IE);
		if(isIE){%>
			<body onload=turnOffProgress(); onunload=toggleMaxInactiveInterval();>	
		<%} else {%>
			<body onload=turnOffProgress(); onBeforeunload=toggleMaxInactiveInterval();>	
		<%}%>		
	    	<div id="pageHeadDiv">
	    		<form>
	    			<table>
						<tr>
							<td class="page-title"><h2 id="ph"><%=XSSUtil.encodeForHTML(context, pageHeading)%></h2></td>
							<td class="functions">
            					<table>
            						<tr>
           					 			<td class="progress-indicator"><div id="imgProgressDiv"></div></td>
									</tr>
								</table>
							</td>
						</tr>
					</table>
					<jsp:include page = "../common/emxToolbar.jsp" flush="true">
					    <jsp:param name="toolbar" value=""/>
					    <jsp:param name="suiteKey" value="Components"/>
					    <jsp:param name="PrinterFriendly" value="false"/>
					    <jsp:param name="helpMarker" value="<%=sHelpMarker%>"/>
					    <jsp:param name="export" value="false"/>
					</jsp:include>
				</form>							
			</div>
			
			<div id='divPageBody'>
				<iframe name='checkinFrame' id='checkinFrame' src="JF_ECOCommonDocumentCheckinCharsetUTF8.jsp" width='100%' height='100%' frameborder='0' border='0' scrolling="auto"></iframe>
			</div>
			<div id="divPageFoot">
				  <table width="100%" border="0" align="center" cellspacing="2" cellpadding="3">
					<tr>
					  <td class="buttons" align="right">
						<table border="0" cellspacing="0">
						  <tr>
						    <framework:ifExpr expr="<%=showPrev%>">
								<td>
									<a class="footericon" href="javascript:this.frames['checkinFrame'].goBack()">
										<img src="../common/images/buttonDialogPrevious.gif" border="0" alt=""/>
									</a>
								</td>
								<td>
										<a href="javascript:this.frames['checkinFrame'].goBack()" class="button"><button class="btn-default" type="button">
											<emxUtil:i18n localize="i18nId">emxComponents.Button.Previous</emxUtil:i18n></button>
										</a>
								</td>
				    
						    </framework:ifExpr>
							<td>
								<a class="footericon" href="javascript:UploadFileAndFlushCell()">
									<img src="../common/images/buttonDialogDone.gif" border="0" alt=""/>
								</a>
								
							</td>
							<td>
									<a href="javascript:UploadFileAndFlushCell()" class="button"><button class="btn-primary" type="button">
										<emxUtil:i18n localize="i18nId">emxComponents.Button.Done</emxUtil:i18n></button>
									</a>
							</td>
						  	<td>
									<a class="footericon" href="javascript:this.frames['checkinFrame'].checkinCancel()">
										<img src="../common/images/buttonDialogCancel.gif" border="0" alt=""/>
									</a>
							</td>
							<td>
									<a href="javascript:this.frames['checkinFrame'].checkinCancel()" class="button"><button class="btn-default" type="button">
										<emxUtil:i18n localize="i18nId">emxComponents.Button.Cancel</emxUtil:i18n></button>
									</a>
							</td>
						  </tr>
						</table>
					  </td>
					</tr>
				  </table>
				</div>			
    	</body>
   	</html>
<%
  }
%>
