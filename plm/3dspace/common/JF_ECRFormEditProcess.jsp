<%--  emxFormEditProcess.jsp

   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,Inc.
   Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxFormEditProcess.jsp.rca 1.24.2.1 Fri Nov  7 09:37:00 2008 ds-kvenkanna Experimental $
--%>

<%@include file = "emxNavigatorNoDocTypeInclude.inc"%>
<%@include file = "emxUIConstantsInclude.inc"%>
<html>
<%@include file = "emxNavigatorTopErrorInclude.inc"%>
<%@include file = "enoviaCSRFTokenValidation.inc"%>
<jsp:useBean id="formEditBean" class="com.matrixone.apps.framework.ui.UIForm" scope="session"/>
    <script language="JavaScript" src="scripts/emxUIConstants.js" type="text/javascript"></script>
    <script language="javascript" src="scripts/emxUICore.js"></script>
    <script language="javascript" src="scripts/emxUIModal.js"></script>
    <script language="javascript" src="scripts/emxUIFormUtil.js"></script>
<%

String isSelfTargeted = emxGetParameter(request, "isSelfTargeted");
String isPopup = emxGetParameter(request, "isPopup");
String portalMode = emxGetParameter(request, "portalMode");
String form = emxGetParameter(request, "form");
String objectId = emxGetParameter(request, "objectId");
String relId = emxGetParameter(request, "relId");
String timeStamp = XSSUtil.encodeForJavaScript(context,emxGetParameter(request, "timeStamp"));
String postProcessURL = emxGetParameter(request, "postProcessURL");
String postProcessJPO = emxGetParameter(request, "postProcessJPO");
String languageStr = request.getHeader("Accept-Language");
String suiteKey = emxGetParameter(request,"suiteKey");
String targetLocation = emxGetParameter(request, "targetLocation");
String openerFrame = emxGetParameter(request, "openerFrame");
String viewformheader = emxGetParameter(request, "viewformHeader");
String viewtoolbar = emxGetParameter(request, "viewtoolbar");


String emxSuiteDirectory = emxGetParameter(request, "emxSuiteDirectory");
if (emxSuiteDirectory == null || emxSuiteDirectory.length() == 0) {
    emxSuiteDirectory = emxGetParameter(request, "SuiteDirectory");
}
String strRootNodeLabel="";
String newObjectId = "";
boolean isPortal = (portalMode != null && "true".equalsIgnoreCase(portalMode));
String sessionRemove= emxGetParameter(request, "sessionRemove");
String wrongURL = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.FormComponent.WrongURLForPostProcessURL", new Locale(request.getHeader("Accept-Language")));
// time zone to be used for the date fields
String timeZone = (String)session.getAttribute("timeZone");

HashMap returnMap =  new HashMap();
String action  = "CONTINUE"; // Default Action is Continue - Commits transaction
String message = "";
String fieldallowKeyableDates="";

boolean updateComplete = true;
boolean errorFromDate = true;
String invaliddate = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Common.InvalidDate", new Locale(request.getHeader("Accept-Language")));
String allowKeyableDates = "false";
long msvalue1=0;
try {
                allowKeyableDates = EnoviaResourceBundle.getProperty(context, "emxFramework.AllowKeyableDates");
} catch(Exception e) {
        allowKeyableDates = "false";
}
String typeAheadEnabled = "true";
    try
    {
        typeAheadEnabled = EnoviaResourceBundle.getProperty(context, "emxFramework.TypeAhead");
    }
    catch (Exception e)
    {
    }

try {
        MapList fields = formEditBean.getFormFields(timeStamp);
        for (int k = 0; k < fields.size(); k++)
        {
                HashMap field = (HashMap)fields.get(k);
                if(formEditBean.isDateField(field) && formEditBean.isFieldEditable(field)) {
                        fieldallowKeyableDates = (String)formEditBean.getSetting(field, "Allow Manual Edit");
                        if((allowKeyableDates != null && "true".equalsIgnoreCase(allowKeyableDates)) || (fieldallowKeyableDates != null && "true".equalsIgnoreCase(fieldallowKeyableDates))) {
                                String fieldName = formEditBean.getName(field);
                                java.text.DateFormat df = java.text.DateFormat.getDateInstance(eMatrixDateFormat.getEMatrixDisplayDateFormat(), request.getLocale());
                                df.setLenient(false);
                                String fieldValue = emxGetParameter(request, fieldName);
                                if( (fieldValue == null || fieldValue.equals("")) && (!formEditBean.isFieldRequired(field)) )
                                    continue;
                                java.util.Date formattedDate = df.parse(fieldValue);
                        }
                }
        }
        errorFromDate = false;

        // save field values to store
        HashMap field;
        String fieldName;
        for (int k = 0; k < fields.size(); k++)
        {
            field = (HashMap) fields.get(k);
            fieldName = formEditBean.getName(field);
            // add the type ahead javascript if the feature is enabled
            String strTypeAheadSetting=formEditBean.getSetting(field,"TypeAhead");
            String rangeHelperURL = formEditBean.getRangeHelperURL(field);

            if (strTypeAheadSetting==null||"null".equals(strTypeAheadSetting)||"".equals(strTypeAheadSetting))
                {
                    strTypeAheadSetting="true";
                }
            if (((formEditBean.isTextBoxField(field) && formEditBean.isFieldEditable(field))
                ||(formEditBean.isFieldManualEditable(field) && rangeHelperURL != null && rangeHelperURL.length() > 0)
                || (formEditBean.isDateField(field) && formEditBean.isFieldManualEditable(field))) && "true".equalsIgnoreCase(typeAheadEnabled) && "true".equalsIgnoreCase(strTypeAheadSetting))
            {
                String tagTypeAheadSavedValuesLimit = formEditBean.getSetting(field, "TypeAhead Saved Values Limit");
                String tagDisplayFieldValue;
                String tagHiddenFieldValue;

                if (rangeHelperURL != null && rangeHelperURL.length() > 0)
                {
                    tagDisplayFieldValue = emxGetParameter(request, fieldName + "Display");
                    tagHiddenFieldValue = emxGetParameter(request, fieldName);
                }
                else
                {
                    tagDisplayFieldValue = emxGetParameter(request, fieldName);
                    tagHiddenFieldValue = "";
                    if (formEditBean.isDateField(field))
                    {
                        tagHiddenFieldValue = emxGetParameter(request, fieldName+"_msvalue");
                        if (tagDisplayFieldValue!=null && !"".equals(tagDisplayFieldValue) && !"".equals(tagDisplayFieldValue)){
                        if ((fieldallowKeyableDates != null && "true".equalsIgnoreCase(fieldallowKeyableDates))) {
                                    int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
                                    double iClientTimeOffset = (new Double(timeZone)).doubleValue();
                                    long msvalue=DateUtil.getMilliseconds(context, tagDisplayFieldValue, iDateFormat, iClientTimeOffset, request.getLocale());
                                    Long lgMsvalue=new Long(msvalue);
                                    tagHiddenFieldValue=lgMsvalue.toString();
                             }
                        }
                    }
                }
%>
                <!-- //XSSOK -->
				<emxUtil:saveTypeAheadValues
                    context="<%= context %>"
                    form="<%=form%>"
                    field="<%= fieldName %>"
                    displayFieldValue="<%=tagDisplayFieldValue%>"
                    hiddenFieldValue="<%=tagHiddenFieldValue%>"
                    count="<%= tagTypeAheadSavedValuesLimit %>"
                />
<%
            }
        }

    // commit the type ahead values
%>
    <emxUtil:commitTypeAheadValues context="<%= context %>" />
<%
    ContextUtil.startTransaction(context, true);

    String objectBased = emxGetParameter(request, "objectBased");

    if (form != null)
    {
        if (objectBased == null || !objectBased.equalsIgnoreCase("false"))
        {
            newObjectId = formEditBean.commitFields(request, context, objectId, relId, timeZone);
            // commitFields(application, session, request, context, objectId, relId);
        }
        else
        {
            newObjectId = objectId;
        }

    }

    //added condition to handle the postProcessURL parameter by Ranganath
    if(postProcessURL!= null && !"".equals (postProcessURL))
    {
        request.setAttribute("context", context);

        //Added for BUG:347348
        String tempURL = postProcessURL;
        int valIndex   = tempURL.indexOf("?");
        if(valIndex!=-1){
            tempURL = tempURL.substring(0,valIndex);
        }

        //Modified for bugs 369162 & 014937
        if(postProcessURL.indexOf("/")==-1){
            tempURL = "/common/"+tempURL; 
        }else{
            tempURL = FrameworkUtil.findAndReplace(tempURL,"../","/");
        }

        ServletContext servletContext  = getServletConfig().getServletContext();
        InputStream inputStream        = servletContext.getResourceAsStream(tempURL);

        if(inputStream!= null){
        	postProcessURL = UIUtil.addSecureTokeninURL(context, session, postProcessURL);
            %><jsp:include page = "<%=postProcessURL%>" flush="true" /><%
        }else{
            throw new com.matrixone.apps.domain.util.FrameworkException(wrongURL);
        }
    }
    //added condition to handle the postProcessJPO parameter by Ranganath
    if(postProcessJPO != null && !"".equals(postProcessJPO) && postProcessJPO.indexOf(":") > 0)
    {
         HashMap programMap = new HashMap(6);
         HashMap paramMap = new HashMap(6);

         Enumeration paramNames = emxGetParameterNames(request);
         while(paramNames.hasMoreElements()) {
          String paramName = (String)paramNames.nextElement();
          String paramValue = emxGetParameter(request,paramName);
          paramMap.put(paramName, paramValue);
         }
         //Added EditAction paramter
         paramMap.put("EditAction", "done");
         programMap.put("requestMap", UINavigatorUtil.getRequestParameterMap(pageContext));
         programMap.put("requestValuesMap", UINavigatorUtil.getRequestParameterValuesMap(request));
         programMap.put("paramMap", paramMap);
         HashMap formMap = formEditBean.getFormData(timeStamp);
         programMap.put("formMap", formMap);

         String[] methodargs = JPO.packArgs(programMap);
         String strJPOName = postProcessJPO.substring(0, postProcessJPO.indexOf(":"));
         String strMethodName = postProcessJPO.substring (postProcessJPO.indexOf(":") + 1, postProcessJPO.length());
         try {
			FrameworkUtil.validateMethodBeforeInvoke(context, strJPOName, strMethodName,"postProcess");
          returnMap = (HashMap)JPO.invoke(context, strJPOName, null, strMethodName, methodargs, HashMap.class);
         } catch (Exception exJPO) {
               throw (new FrameworkException(exJPO.toString()));
         }
         if(returnMap != null && returnMap.size() > 0)
         {
            message = (String)returnMap.get("Message");
            action  = (String)returnMap.get("Action");
            if(message != null && !"".equals(message))
            {
               String suiteDir = "";
               String registeredSuite = suiteKey;

               if ( suiteKey != null && suiteKey.startsWith("eServiceSuite") )
                 registeredSuite = suiteKey.substring("eServiceSuite".length());

               String stringResFileId = UINavigatorUtil.getStringResourceFileId(context, registeredSuite);
               if(stringResFileId == null || stringResFileId.length()==0)
                 stringResFileId="emxFrameworkStringResource";

               String alertMessage = EnoviaResourceBundle.getProperty(context, stringResFileId, new Locale(request.getHeader("Accept-Language")), message);               
               if ((alertMessage == null) || ("".equals(alertMessage)))
               {
                  alertMessage = message;
               }

               alertMessage = FrameworkUtil.findAndReplace(alertMessage,"\n","\\n");
               alertMessage = FrameworkUtil.findAndReplace(alertMessage,"\r","\\r");
               alertMessage = FrameworkUtil.findAndReplace(alertMessage,"\"", "\\\"");

%>
<script language="javascript" type="text/javaScript">
alert("<xss:encodeForJavaScript><%=alertMessage%></xss:encodeForJavaScript>");
</script>
<%
            }
         }
     }

    if(action!=null && action.equalsIgnoreCase("continue"))
    {
        ContextUtil.commitTransaction(context);
        updateComplete = true;
    }
    else
    {
        ContextUtil.abortTransaction(context);
        updateComplete = false;
    }

} catch (Exception ex) {
	newObjectId= objectId;
    ContextUtil.abortTransaction(context);
    String msg = ex.getMessage();
    if(msg.contains("ORA-12899") || msg.contains("ORA-01461")){
		String errorMessage = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Create.NameColumn", new Locale(request.getHeader("Accept-Language")));            		
	  	emxNavErrorObject.addMessage(errorMessage);
	}else if(errorFromDate) {
            emxNavErrorObject.addMessage(invaliddate);
	 } else if(msg.contains("System Error: #5000001: java.lang.Exception:")) {
			int pos = msg.indexOf("System Error: #5000001: java.lang.Exception:");
	    	msg =msg.substring(pos+44).trim();
	        emxNavErrorObject.addMessage(msg);
    }else if (ex.toString().contains("Business object 'type name revision' not unique")) {
    	DomainObject dobObject= new DomainObject(newObjectId);
    	String type = (String)dobObject.getInfo(context, DomainConstants.SELECT_TYPE);
    	String errorMessage = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.DuplicateObjectName.Message", new Locale(request.getHeader("Accept-Language")));
    	errorMessage =errorMessage.replace("{0}", type);
        emxNavErrorObject.addMessage(errorMessage);
    } else if (ex.toString() != null && (ex.toString().trim()).length() > 0) {
            emxNavErrorObject.addMessage(ex.toString().trim());
    }
    updateComplete = false;

} finally {
       // Clean the session HashMap for this entry with timestamp
    //if (!isPortal && updateComplete && "true".equalsIgnoreCase(isPopup))
    if (!isPortal && updateComplete && !"false".equalsIgnoreCase(sessionRemove))
    {
        formEditBean.removeFormData(timeStamp);
    }

}

try
{
	UIMenu emxMenu = new UIMenu();
    //get the tree information to update the tree
    String stMenuName=UITreeUtil.getTreeMenuName(application, session, context, newObjectId, emxSuiteDirectory);
    HashMap objMenuMap = emxMenu.getMenu(context, stMenuName);
    HashMap menuJSNodeMap = UITreeUtil.getJSNodeMap(application, session, request, context, objMenuMap, newObjectId, relId);
    strRootNodeLabel = (String)menuJSNodeMap.get("nodeLabel");
    strRootNodeLabel = XSSUtil.encodeForJavaScript(context, strRootNodeLabel);
    //To fix 362564
    if (strRootNodeLabel != null) {
        strRootNodeLabel = FrameworkUtil.findAndReplace(strRootNodeLabel, "\n", " ");
    }
}
catch(Exception e)
{

}
%>
<%@include file = "emxNavigatorBottomErrorInclude.inc"%>
<script language="JavaScript">
var contentFrame;
<%
if (updateComplete){
	if (!isPortal)
	{
	    // In the case of non-object based forms, we need to
	    // be able to control the submit action.
	
	    String submitAction = emxGetParameter(request, "submitAction");
	    if (submitAction != null && submitAction.equalsIgnoreCase("refreshCaller"))
	    {
%>
			var target = "<%= targetLocation %>";
			if(getTopWindow().changeObjectLabelInTree && "slidein" != target) {
				getTopWindow().changeObjectLabelInTree("<xss:encodeForJavaScript><%=objectId%></xss:encodeForJavaScript>", "<%=strRootNodeLabel%>", true, false);
			}
<%
			if(isSelfTargeted != null && isSelfTargeted.equalsIgnoreCase("true")){
				if("slidein".equalsIgnoreCase(targetLocation)){
%>
					contentFrame = getFormContentFrame('slidein');
					if(contentFrame){
					    formUrl = contentFrame.location.href;
					    contentFrame.location.href = changeURLToViewMode(formUrl);
					}				
					contentFrame = getFormContentFrame();
					if(contentFrame){
					    contentFrame.location.href = contentFrame.location.href;
					}				
<%
					} else {
%>
						contentFrame = getFormContentFrame();
						if(getTopWindow().opener && getTopWindow().opener.getTopWindow().RefreshHeader){
							getTopWindow().opener.getTopWindow().RefreshHeader();
                            getTopWindow().opener.getTopWindow().deletePageCache();
						}else if(getTopWindow().RefreshHeader){
							getTopWindow().RefreshHeader();
                            getTopWindow().deletePageCache();
						}

						if(contentFrame){
							formUrl = contentFrame.location.href;
						    contentFrame.location.href = changeURLToViewMode(formUrl);
						}				

<%
					}
				} else if("slidein".equalsIgnoreCase(targetLocation)){
%>			
					contentFrame = findFrame(getTopWindow(), '<xss:encodeForJavaScript><%=openerFrame%></xss:encodeForJavaScript>');
					contentFrame = contentFrame ? contentFrame : getFormContentFrame();
					if(contentFrame.location.href.indexOf("emxIndentedTable.jsp") >= 0){
						  contentFrame.location.href = contentFrame.location.href.replace("persist=true", "");
	                	if(getTopWindow().RefreshHeader){
							getTopWindow().RefreshHeader();
	                        getTopWindow().deletePageCache();
						}else if(windowOpenerObject && getTopWindow().opener.getTopWindow().RefreshHeader){
							getTopWindow().opener.getTopWindow().RefreshHeader();
	                        getTopWindow().opener.getTopWindow().deletePageCache();
						 }
						contentFrame.refreshSBTable(contentFrame.configuredTableName);
	                } else {
	                	var windowOpenerObject;
	            		try{
	            			windowOpenerObject = getTopWindow().opener && getTopWindow().opener.getTopWindow();
	            		}catch(e){
	            			windowOpenerObject = null;
	            		} 
	                	if(getTopWindow().RefreshHeader){
						getTopWindow().RefreshHeader();
                        getTopWindow().deletePageCache();
						}else if(windowOpenerObject && getTopWindow().opener.getTopWindow().RefreshHeader){
							getTopWindow().opener.getTopWindow().RefreshHeader();
                        	getTopWindow().opener.getTopWindow().deletePageCache();
					 }
						contentFrame.location.href = contentFrame.location.href;
	                 }
				    setTimeout("getTopWindow().closeSlideInDialog()", 75);
				  
<%
			} else {
%>
	    		if(getTopWindow().getWindowOpener()){
	    			if(getTopWindow().getWindowOpener().location.href.indexOf("emxIndentedTable.jsp") >= 0){
		    			getTopWindow().getWindowOpener().refreshSBTable(getTopWindow().getWindowOpener().configuredTableName);
	                    } else if (getTopWindow().getWindowOpener().name == "listDisplay"){
	                    	   getTopWindow().getWindowOpener().parent.document.location.href = getTopWindow().getWindowOpener().parent.document.location.href;
	                    }else{
	                    	getTopWindow().getWindowOpener().document.location.href = getTopWindow().getWindowOpener().document.location.href;
	                    }   	
		    		if(getTopWindow().closePopupWindow){
		    			getTopWindow().closePopupWindow(getTopWindow());
		    		}
		    	}
<%
			}
	    }
	    else if (submitAction != null && submitAction.equalsIgnoreCase("doNothing"))
	    {
	    	if("slidein".equalsIgnoreCase(targetLocation)){
				%>
                                getTopWindow().closeSlideInDialog();
                <%
                    }
	    }
	    else
	    {
%>
			try
			{
				var topWindow = getTopWindow();
				var formUrl = null;
				
<%
			   if(isSelfTargeted != null && isSelfTargeted.equalsIgnoreCase("true")){
%>
					contentFrame = getFormContentFrame('<%= XSSUtil.encodeForJavaScript(context,targetLocation) %>');
					if(getTopWindow().opener && getTopWindow().opener.getTopWindow().RefreshHeader){
						getTopWindow().opener.getTopWindow().RefreshHeader();
                        getTopWindow().opener.getTopWindow().deletePageCache();
					}else if(getTopWindow().RefreshHeader){
						getTopWindow().RefreshHeader();
                        getTopWindow().deletePageCache();
					}

					if(contentFrame){
					    formUrl = contentFrame.location.href;
					    formUrl = changeURLToViewMode(formUrl);
					}				
<%
			    } else if("slidein".equalsIgnoreCase(targetLocation)){
%>
					topWindow = getTopWindow();
					//contentFrame = getFormContentFrame('slidein');
				    //if(!contentFrame){
				        contentFrame = getFormContentFrame();
				    //}
					if(getTopWindow().opener && getTopWindow().opener.getTopWindow().RefreshHeader){
						getTopWindow().opener.getTopWindow().RefreshHeader();
                        getTopWindow().opener.getTopWindow().deletePageCache();
					}else if(getTopWindow().RefreshHeader){
						getTopWindow().RefreshHeader();
                        getTopWindow().deletePageCache();
					}
				    
				    if(contentFrame){
				        formUrl = contentFrame.location.href;
				    }
<%
				} else {
%>
					if(getTopWindow().getWindowOpener().getFormContentFrame){
				    	contentFrame = getTopWindow().getWindowOpener().getFormContentFrame();
				    }else {
				    contentFrame = getFormContentFrame();
				    }
				    if(contentFrame){
				        formUrl = contentFrame.location.href;
				    } else {
				    	formUrl = getTopWindow().getWindowOpener().document.location.href;
				    }
<%
			    }
%>
			
				//Remove the duplicate object id parameters and append the one got from request
			    var newURL = formUrl;
			    if(newURL.indexOf("objectId") < 0)
			    {
			        newURL += (newURL.indexOf('?') > -1 ? '&' : '?') + "objectId=<%=XSSUtil.encodeForURL(context, objectId)%>";
			    }
			
			    var changeOBJID = false;
			    if (getTopWindow().getWindowOpener() != null && !getTopWindow().getWindowOpener().closed)
			    {
			        if (getTopWindow().getWindowOpener() && getTopWindow().getWindowOpener().getTopWindow().modalDialog)
			        {
			            if(!(getTopWindow().getWindowOpener().getTopWindow().modalDialog.contentWindow.closed))
			            {
			                getTopWindow().getWindowOpener().getTopWindow().modalDialog.releaseMouse();
			            }
			        }
			    }
<%
				if (objectId != null && newObjectId != null && !objectId.equals(newObjectId)){
%>
			    	changeOBJID = true;
<%
				}
%>
			
			    var arrURLParts = newURL.split("?");
			    if(changeOBJID)
			    {
			        newURL = removeObjectIDParam(formUrl);
			        arrURLParts[1]="objectId=<%=XSSUtil.encodeForURL(context, newObjectId)%>&"+arrURLParts[1];
			        newURL=arrURLParts.join("?");
			
<%
			    	if (relId != null && !"".equals(relId))
			    	{
%>
					    if (newURL.indexOf('relId=') == -1)
					    {
							newURL += (newURL.indexOf('?') > -1 ? '&' : '?') + "relId=<%=XSSUtil.encodeForURL(context, relId)%>";
					    }
<%
			    	}
%>
			    }
			
			} catch (e) {
				if(-2146828218 != e.number && -2147418094 != e.number && !'undefined' == e.number) {
					alert(emxUIConstants.STR_JS_AnExceptionOccurred + " " + emxUIConstants.STR_JS_ErrorName + " " + e.name
         	    				+ emxUIConstants.STR_JS_ErrorNumber + " " + e.number
         	    				+ emxUIConstants.STR_JS_ErrorMessage + " " + e.message);
			    }
			}
			
<%
			if(isSelfTargeted != null && isSelfTargeted.equalsIgnoreCase("true")){
%>
				if(topWindow.changeObjectLabelInTree) {
					if(getTopWindow().getWindowOpener() && getTopWindow().changeObjectLabelInTree) {
						getTopWindow().changeObjectLabelInTree("<xss:encodeForJavaScript><%=objectId%></xss:encodeForJavaScript>", "<%=strRootNodeLabel%>", true, true);
					}					
					topWindow.changeObjectLabelInTree("<xss:encodeForJavaScript><%=objectId%></xss:encodeForJavaScript>", "<%=strRootNodeLabel%>", true, true);
				}
					contentFrame = getFormContentFrame('<%= XSSUtil.encodeForJavaScript(context,targetLocation) %>');
					if(contentFrame){
						contentFrame.location.href = newURL;
					}				
<%
			} else if("slidein".equalsIgnoreCase(targetLocation)){
%>
				if(topWindow.changeObjectLabelInTree){
				topWindow.changeObjectLabelInTree("<xss:encodeForJavaScript><%=objectId%></xss:encodeForJavaScript>", "<%=strRootNodeLabel%>", true, true);
				}
				
				//contentFrame = getFormContentFrame('slidein');
				//if(!contentFrame){
				    contentFrame = getFormContentFrame();
				//}
				getTopWindow().closeSlideInDialog();
				if(contentFrame){
					contentFrame.location.href = newURL;
				}				
<%
			} else {
%>
				var contextTree = null;
				var objNode = null;
				if (findFrame(topWindow, "treeDisplay"))
				{
				    contextTree = topWindow.objStructureTree;
				    if(contextTree)
				    {
				            contextTree.doNavigate = true;
				            objNode = contextTree.findNodeByObjectID("<xss:encodeForJavaScript><%=objectId%></xss:encodeForJavaScript>");
				    }
				}
				
				if(objNode)
				{
				    if(changeOBJID)
				    {
				    	topWindow.changeObjectIDInTree("<xss:encodeForJavaScript><%=objectId%></xss:encodeForJavaScript>", "<xss:encodeForJavaScript><%=newObjectId%></xss:encodeForJavaScript>", true, true);
				    }
				    else if(getTopWindow().getWindowOpener())
				    {
				
				        var formOpener = getTopWindow().getWindowOpener().document.location.href;
				        var pMode = formOpener.indexOf("portalMode=true");
				        if((formOpener.indexOf("emxForm.jsp")>-1 || formOpener.indexOf("emxTable.jsp") >-1 ||  formOpener.indexOf("emxIndentedTable.jsp") >-1) && (pMode > -1))
				        {
				            getTopWindow().getWindowOpener().document.location.href = formOpener;
				        }
				        else
				        {
				
				            var formOpenerParent = getTopWindow().getWindowOpener().parent.document.location.href;
				            pMode = formOpenerParent.indexOf("portalMode=true");
				            if((formOpenerParent.indexOf("emxForm.jsp")>-1 || formOpenerParent.indexOf("emxTable.jsp") >-1 ||  formOpenerParent.indexOf("emxIndentedTable.jsp") >-1) && (pMode > -1))
				            {
				                getTopWindow().getWindowOpener().parent.document.location.href = formOpenerParent;
				            }else {
								if(topWindow.changeObjectLabelInTree){
				            	topWindow.changeObjectLabelInTree("<xss:encodeForJavaScript><%=objectId%></xss:encodeForJavaScript>", "<%=strRootNodeLabel%>", true, true);
								}
				            }
				        }
				    }
				}
				else
				{
				    try
				    {
					    if(getTopWindow().getWindowOpener())
					    {
					        var openerURL="";
					        var isIndentedTablePage=false;
					        if(getTopWindow().getWindowOpener().document.location.href.indexOf("emxIndentedTable.jsp")>-1)
					        {
					            openerURL = getTopWindow().getWindowOpener().document.location.href;
					            isIndentedTablePage=true;
					        }
					        else
					        {
					            openerURL = getTopWindow().getWindowOpener().parent.document.location.href;
					        }
					        if(openerURL.indexOf("emxTable.jsp") >= 0) {
						        var reloadURL = getTopWindow().getWindowOpener().parent.frames[1].document.location.href;
						        reloadURL += "&clearValuesMap=true";
						        getTopWindow().getWindowOpener().parent.frames[1].document.location.href = reloadURL;
						    } else {
							    if(getTopWindow().getWindowOpener().parent.FullSearch)
					            {
					                getTopWindow().getWindowOpener().parent.FullSearch.formSearch();
					            }
					            else if(isIndentedTablePage)
					            {
					                getTopWindow().getWindowOpener().document.location.href = openerURL;
					            }
					            else
					            {
						            //getTopWindow().getWindowOpener().parent.document.location.href = newURL;
					                getTopWindow().getWindowOpener().document.location.href = changeURLToViewMode(newURL);
					            }
					        }
				        }
				    }
					catch (e) {
					    if(-2146828218 != e.number && -2147418094 != e.number && !'undefined' == e.number)
					     {
					        alert(emxUIConstants.STR_JS_AnExceptionOccurred + " " + emxUIConstants.STR_JS_ErrorName + " " + e.name
         	    				+ emxUIConstants.STR_JS_ErrorNumber + " " + e.number
         	    				+ emxUIConstants.STR_JS_ErrorMessage + " " + e.message);
					     }
					}
				}
<%
			}
		}
	} else {
%>
		if(getTopWindow().changeObjectLabelInTree){
		getTopWindow().changeObjectLabelInTree("<xss:encodeForJavaScript><%=objectId%></xss:encodeForJavaScript>", "<%=strRootNodeLabel%>", true, false);
		getTopWindow().refreshStructureTree();
		}
		var strurl = parent.location.href;
<%
		   if(isSelfTargeted != null && isSelfTargeted.equalsIgnoreCase("true")){
%>
		strurl = changeURLToViewMode(strurl);
		if(getTopWindow().opener && getTopWindow().opener.getTopWindow().RefreshHeader){
			getTopWindow().opener.getTopWindow().RefreshHeader();
            getTopWindow().opener.getTopWindow().deletePageCache();
		}else{
			getTopWindow().RefreshHeader();
            getTopWindow().deletePageCache();
		}

		parent.location.href = strurl;
<%
			} else if("slidein".equalsIgnoreCase(targetLocation)){
%>
				contentFrame = openerFindFrame(getTopWindow(), '<xss:encodeForJavaScript><%=openerFrame%></xss:encodeForJavaScript>');
				contentFrame = contentFrame == null ? contentFrame = getFormContentFrame(): contentFrame;
			    getTopWindow().closeSlideInDialog();
				if(getTopWindow().opener && getTopWindow().opener.getTopWindow().RefreshHeader){
					getTopWindow().opener.getTopWindow().RefreshHeader();
                    getTopWindow().opener.getTopWindow().deletePageCache();
				}else{
					getTopWindow().RefreshHeader();
                    getTopWindow().deletePageCache();
				}

				if(contentFrame){
					contentFrame.location.href = contentFrame.location.href;
				}	
<%
		   }
%>
		
<%
	}
}
%>
  parent.setFormSubmitAction(true);
//请求成功 往请求页面发送消息刷新页面
const channel = new BroadcastChannel("JF_ECR_EDIT_REFRESH_NOTICE");
channel.postMessage("200");
<%
if (!(!isPortal && updateComplete && "true".equalsIgnoreCase(isPopup))) {
        //!!IMPORTANT!! unload are being cancelled for everything except powerviews
        //unload events are calling purgeEditFormData()
        //this is being called directly here and
        //BEFORE closing a dialog
		// Make sure the progress indicator is off.
		// The following is to undo what is done in
		// emxUIFormUtil.js:saveChanges().
%>
    turnOffProgress();
    parent.document.body.style.cursor = "default";
<%
}

if (!isPortal && updateComplete) {
%>
		if(getTopWindow().purgeEditFormData){
		    getTopWindow().purgeEditFormData("<%=timeStamp%>","done");
		}
<%
		if(isSelfTargeted != null && isSelfTargeted.equalsIgnoreCase("true")){
		} else if("popup".equalsIgnoreCase(targetLocation)){
%>
			getTopWindow().closeWindow();
<%
		}
}
%>
</script>

</html>

