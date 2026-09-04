<%-- emxComponentsMultiFileUploadDialogCode.inc - used for Checkin of file into Document Object
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = "$Id: emxCommonDocumentCheckinDialogCode.inc.rca 1.31 Tue Oct 28 19:01:03 2008 przemek Experimental przemek przemek $"
--%>
<style type="text/css" >
form.myclass {
	height: 0px;
}
</style>
<%@include file = "emxComponentsCheckin.inc"%>
<%@ page import = "com.matrixone.apps.framework.ui.UIUtil,com.matrixone.apps.common.Person" %>
<%@ page import="com.dscn.plm.util.NioJDUtils" %>
<%@ page import="java.util.Properties" %>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<script type="text/javascript" src="../common/scripts/emxUICore.js"></script>

<%
    Document document = (Document)DomainObject.newInstance(context, CommonDocument.TYPE_DOCUMENT);
    // Define variables for request parameters and some global
    // required variables.
    String languageStr = request.getHeader("Accept-Language");
    StringList usedParamList = new StringList();
    Map emxCommonDocumentCheckinData = (Map) session.getAttribute("emxCommonDocumentCheckinData");

    // Begin : Bug 347223 code
    if (emxCommonDocumentCheckinData == null) {
        emxCommonDocumentCheckinData = new HashMap();
    }
    Map mapPreviousCheckinFormData = (Map)emxCommonDocumentCheckinData.get("mapPreviousCheckinFormData");
    if (mapPreviousCheckinFormData == null) {
        mapPreviousCheckinFormData = new HashMap();
    }
    // End : Bug 347223 code

    // Reading request parameters and storing into variables
    String plugInNotFoundAlert = (String) emxCommonDocumentCheckinData.get("plugInNotFoundAlert");
    String parentId = (String) emxCommonDocumentCheckinData.get("parentId");
    String parentRelName = (String) emxCommonDocumentCheckinData.get("parentRelName");
    String objectId = (String) emxCommonDocumentCheckinData.get("objectId");
    String showDescription     = (String) emxCommonDocumentCheckinData.get("showDescription");

    String showComments = (String) emxCommonDocumentCheckinData.get("showComments");
    String noOfFilesStr        = (String) emxCommonDocumentCheckinData.get("noOfFiles");
    //用于修改文件最大上传数量
    String store               = (String) emxCommonDocumentCheckinData.get("store");
    String showFormat          = (String) emxCommonDocumentCheckinData.get("showFormat");
    String showFileTitle          = (String) emxCommonDocumentCheckinData.get("showFileTitle");
    String showTitle          = (String) emxCommonDocumentCheckinData.get("showTitle");
    String objectAction        = (String) emxCommonDocumentCheckinData.get("objectAction");
    String allowFileNameChange = (String) emxCommonDocumentCheckinData.get("allowFileNameChange");
    String objectAliasType = (String) emxCommonDocumentCheckinData.get("defaultType");
    String fileFormat      = (String) emxCommonDocumentCheckinData.get("format");
    String oldFileName = (String) emxCommonDocumentCheckinData.get("oldFileName");
    String encodedFileName = (String) emxCommonDocumentCheckinData.get("encodedFileName");
    String allowDuplicateFiles = (String) emxCommonDocumentCheckinData.get("allowDuplicateFiles");
    String objectType = (String) emxCommonDocumentCheckinData.get("type");
    String objectPolicy = (String) emxCommonDocumentCheckinData.get("policy");
    StringList formatList = new StringList();
    String defaultFormat = document.FORMAT_GENERIC;
    String fileRequired = (String)emxCommonDocumentCheckinData.get("fileRequired");
    
    String target = (String) emxCommonDocumentCheckinData.get("target");

    try {
                Properties properties = NioJDUtils.readPageObject(context,"JFJDConfig");
                if("image".equalsIgnoreCase(objectAction)){
                    noOfFilesStr = (String)properties.get("3dspace.Image.maxCount");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
  	if(UIUtil.isNullOrEmpty(target)){
    	target="";
    }
    
    objectAliasType = UIUtil.isNullOrEmpty(objectAliasType) ? "type_Document" : objectAliasType;
    
    objectType = UIUtil.isNullOrEmpty(objectType) ? PropertyUtil.getSchemaProperty(context, objectAliasType) : objectType;
    emxCommonDocumentCheckinData.put("type", objectType);
    
    oldFileName = !UIUtil.isNullOrEmpty(encodedFileName) ? FrameworkUtil.decodeURL(encodedFileName, "UTF-8") : oldFileName;
    	
    System.out.println("L48 Collab & approve emxCommonDocumentCheckinDialogCode store: " + store);
    store = UIUtil.isNullOrEmpty(store) ? Person.getPerson(context).getInfo(context, CommonDocument.SELECT_COMPANY_STORE) : store;
    System.out.println("L48 Collab & approve emxCommonDocumentCheckinDialogCode after store: " + store);
    allowFileNameChange = !UIUtil.isNullOrEmpty(allowFileNameChange) ? allowFileNameChange : "true";
    
    allowDuplicateFiles = !UIUtil.isNullOrEmpty(allowDuplicateFiles) ? allowDuplicateFiles : "false";

    showFormat = !UIUtil.isNullOrEmpty(showFormat) ? showFormat : "true";
    showFileTitle=!UIUtil.isNullOrEmpty(showFileTitle) ? showFileTitle : "false";
    
    showTitle = !UIUtil.isNullOrEmpty(showTitle) ? showTitle : "true";

    showDescription = !UIUtil.isNullOrEmpty(showDescription) ? showDescription : "true";

    showComments = !UIUtil.isNullOrEmpty(showComments) ? showComments : "true";
    
    String DocumentFileRequired = FrameworkProperties.getProperty(context,"emxComponents.CommonDocument.DocumentFileRequired");
    if ("true".equalsIgnoreCase(DocumentFileRequired))
    {
        if ( fileRequired == null || fileRequired.equals("") || "true".equalsIgnoreCase(fileRequired))
        {
            fileRequired = "true";
        }
        else
        {
            fileRequired = "false";
        }
    } 
    else
    {
        if ( fileRequired == null || fileRequired.equals("") || "false".equalsIgnoreCase(fileRequired))
        {
            fileRequired = "false";
        }
        else
        {
            fileRequired = "true";
        }
    }
    
    objectAction = !UIUtil.isNullOrEmpty(objectAction) ? objectAction : "create";
    
    if(!CommonDocument.OBJECT_ACTION_CREATE_MASTER_PER_FILE.equalsIgnoreCase(objectAction) ){
        showDescription = "false";
        showTitle = "false";
    } else {
        showComments = "false";
    }

    noOfFilesStr = !UIUtil.isNullOrEmpty(noOfFilesStr) ? noOfFilesStr : FrameworkProperties.getProperty(context,"emxComponents.MultiFileUpload.NoOfFiles");
    objectId = !UIUtil.isNullOrEmpty(objectId) ? objectId : "";

    Boolean versionableFromMap = (Boolean)emxCommonDocumentCheckinData.get("isVersionable");
    showComments = (versionableFromMap != null && !(versionableFromMap.booleanValue())) ? "false" : showComments;
    
    boolean isDescriptionRequired = showDescription.equalsIgnoreCase("required");
    boolean canShowDescription = showDescription.equalsIgnoreCase("true");
    
    boolean isCommentsRequired = showComments.equalsIgnoreCase("required");
    boolean canShowComments = showComments.equalsIgnoreCase("true");
    
    boolean canShowFormat = showFormat.equalsIgnoreCase("true");
    boolean canShowFileTitle = showFileTitle.equalsIgnoreCase("true");
    boolean isFileTitleRequired = showFileTitle.equalsIgnoreCase("required");

    boolean isFormatRequired = showFormat.equalsIgnoreCase("required");
    boolean isFormatReadOnly = showFormat.equalsIgnoreCase("readonly");
    
    boolean canShowTitle =  showTitle.equalsIgnoreCase("true");
    boolean isTitleRequired = showTitle.equalsIgnoreCase("required");
    
    boolean isFileRequired = "true".equalsIgnoreCase(fileRequired);
    
    boolean isAbstract = false;
    int noOfFiles = new Integer (noOfFilesStr).intValue();

    MapList docList = new MapList();
    Map objectMap = new HashMap();
    if ( parentId != null && !"null".equals(parentId) && !"".equals(parentId) && objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_HOLDER) )
    {
        String objectWhere = CommonDocument.SELECT_LOCKER + "== '" + context.getUser() +"'";
        DomainObject parent = DomainObject.newInstance(context, parentId);
        StringList objectSelects = new StringList();

        objectSelects.add(CommonDocument.SELECT_FILE_NAME);
        objectSelects.add(CommonDocument.SELECT_FILE_FORMAT);
        objectSelects.add(CommonDocument.SELECT_TITLE);
        objectSelects.add(CommonDocument.SELECT_CHECKIN_REASON);
        objectSelects.add(CommonDocument.SELECT_LOCKER);
        objectSelects.add(CommonDocument.SELECT_ID);

        objectSelects.add(CommonDocument.SELECT_FORMATS);
        objectSelects.add(CommonDocument.SELECT_MASTER_FILE_FORMAT);
        objectSelects.add(CommonDocument.SELECT_MASTER_FILE_NAME);
        objectSelects.add(CommonDocument.SELECT_MASTER_TITLE);
        objectSelects.add(CommonDocument.SELECT_MASTER_FORMATS);
        objectSelects.add(CommonDocument.SELECT_MASTER_ID);

        if ( parentRelName != null )
        {
            parentRelName = PropertyUtil.getSchemaProperty(context,parentRelName);
        } else {
            parentRelName = CommonDocument.RELATIONSHIP_REFERENCE_DOCUMENT;
        }
        MapList documentList = parent.getRelatedObjects(context,                       // context.
                                          CommonDocument.RELATIONSHIP_ACTIVE_VERSION + "," + parentRelName,  // rel filter.
                                          CommonDocument.TYPE_DOCUMENTS,               // type filter.
                                          objectSelects,                   // business selectables.
                                          null,                   // relationship selectables.
                                          true,                        // expand to direction.
                                          true,                         // expand from direction.
                                          (short) 2,                    // level
                                          null,                  // object where clause
                                          CommonDocument.EMPTY_STRING);

        Iterator itr = documentList.iterator();
        String contextUser = context.getUser();
        while(itr.hasNext())
        {
            Map m = (Map)itr.next();
            String locker = (String) m.get(CommonDocument.SELECT_LOCKER);
            String masterId = (String) m.get(CommonDocument.SELECT_MASTER_ID);
            if( masterId != null &&  !"".equals(masterId) && locker.equals(contextUser) )
            {
                docList.add(m);
            }

        }
        noOfFiles = docList.size();

    }
    if (objectAction.equals(CommonDocument.OBJECT_ACTION_UPDATE_MASTER) )
    {
        String objectWhere = CommonDocument.SELECT_LOCKER + "== '" + context.getUser() +"'";
        if ( oldFileName != null && !"".equals(oldFileName) && !"null".equals(oldFileName))
        {
            objectWhere += " && (" + CommonDocument.SELECT_TITLE + "== \"" + oldFileName + "\")";
        }
        StringList objectSelects = new StringList();
        objectSelects.add(CommonDocument.SELECT_ID);
        objectSelects.add(CommonDocument.SELECT_TITLE);
        objectSelects.add(CommonDocument.SELECT_CHECKIN_REASON);

        DomainObject object = DomainObject.newInstance(context, objectId);
        docList = object.getRelatedObjects(context,                       // context.
                                          CommonDocument.RELATIONSHIP_ACTIVE_VERSION,  // rel filter.
                                          CommonDocument.TYPE_DOCUMENTS,               // type filter.
                                          objectSelects,                   // business selectables.
                                          null,                   // relationship selectables.
                                          false,                        // expand to direction.
                                          true,                         // expand from direction.
                                          (short) 1,                    // level
                                          objectWhere,                  // object where clause
                                          CommonDocument.EMPTY_STRING);
        noOfFiles = docList.size();

        objectSelects = new StringList(3);
        objectSelects.add(CommonDocument.SELECT_FILE_NAME);
        objectSelects.add(CommonDocument.SELECT_FILE_FORMAT);
        objectMap = object.getInfo(context, objectSelects);
    } else if (objectAction.equals(VCDocument.OBJECT_ACTION_CHECKIN_VC_FILE) )
    {
        StringList objectSelects = new StringList();
        objectSelects.add(CommonDocument.SELECT_ID);
        objectSelects.add("vcfile[1].comment");
        objectSelects.add("vcfile[1].format");
        objectSelects.add("vcfile");
        objectSelects.add("vcfile[1].vcname");
        DomainObject object = DomainObject.newInstance(context, objectId);
        Map map = object.getInfo(context,objectSelects);
        String vcfile =(String) map.get("vcfile");
        if(vcfile.equalsIgnoreCase("true")){
           HashMap tempMap = new HashMap();
           tempMap.put(CommonDocument.SELECT_ID,map.get(CommonDocument.SELECT_ID));
           tempMap.put(CommonDocument.SELECT_TITLE,map.get("vcfile[1].vcname"));
           tempMap.put(CommonDocument.SELECT_CHECKIN_REASON,map.get("vcfile[1].comment"));
           docList.add(tempMap);
           objectMap.put(CommonDocument.SELECT_FILE_NAME,new StringList(map.get("vcfile[1].vcname").toString()));
           objectMap.put(CommonDocument.SELECT_FILE_FORMAT,new StringList(map.get("vcfile[1].format").toString()));
           noOfFiles = 1;
      }
    }
    String policyStore = "";

    if ( objectId != null && !objectId.equals("") && !objectAction.equalsIgnoreCase("image")) {
        document.setId(objectId);
        StringList selectList = new StringList();
        selectList.add(CommonDocument.SELECT_POLICY);
        selectList.add(CommonDocument.SELECT_DEFAULT_FORMAT);
        selectList.add(CommonDocument.SELECT_FORMATS);
        selectList.add(CommonDocument.SELECT_STORE);
        selectList.add(CommonDocument.SELECT_ALL_FORMATS);
        Map commonDocMap = document.getInfo(context, selectList);
        policyStore = (String)commonDocMap.get(CommonDocument.SELECT_STORE);
        defaultFormat = (String)commonDocMap.get(CommonDocument.SELECT_DEFAULT_FORMAT);
        formatList = (StringList)commonDocMap.get(CommonDocument.SELECT_FORMATS);
        String strPolicy = "";
        if(null==formatList){ 
        	if(null==(String)commonDocMap.get(CommonDocument.SELECT_ALL_FORMATS)){
        		strPolicy = (String)commonDocMap.get(CommonDocument.SELECT_POLICY);

        		String strError = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Alert.FormatNotFound") + strPolicy;        		
        		throw new MatrixException(strError);  
        	}else{
        formatList = new StringList((String)commonDocMap.get(CommonDocument.SELECT_ALL_FORMATS));
        }
        }	
        
        //375100   start
        String policy = (String)commonDocMap.get(CommonDocument.SELECT_POLICY);
        boolean boolSPCInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionSpecificationCentral",false,null,null);
        if(boolSPCInstalled && policy.equalsIgnoreCase(PropertyUtil.getSchemaProperty(context,"policy_TechnicalSpecification")))
        {
        String  formatListItem=com.matrixone.apps.domain.util.FrameworkProperties.getProperty(context,"emxSpecificationCentral.ImageManager.AllowedFormats");
        formatList.clear();
        formatList= com.matrixone.apps.domain.util.FrameworkUtil.split(formatListItem,",");
        }
        //375100   end
    } else if ( objectAction.equalsIgnoreCase("image") || !objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_HOLDER) ) {
        BusinessType bType = new BusinessType(objectType, context.getVault());
        isAbstract = bType.isAbstract(context);

        MapList policyList = mxType.getPolicies(context,objectType,true);
        Iterator itr = policyList.iterator();
        while( itr.hasNext() )
        {
            Map m = (Map)itr.next();
            String typePolicy = (String)m.get("name");
            if ( objectPolicy == null || objectPolicy.equals(typePolicy) )
            {
                if (policyStore!= null && policyStore.equals("") )
                {
                    policyStore = (String)m.get("store");
                }
                formatList = (StringList) m.get("formats");
                defaultFormat = (String)m.get("defaultFormat");
            }
        }
    }
    
    if(formatList.contains(DomainObject.FORMAT_MX_MEDIUM_IMAGE)){
    	formatList.remove(DomainObject.FORMAT_MX_MEDIUM_IMAGE);
    }
    String appDir = (String) emxCommonDocumentCheckinData.get("appDir");
    String isVCDoc = (String)emxCommonDocumentCheckinData.get("isVcDoc");
    if("true".equals(isVCDoc) && store.equals("")) {
       store = (String)emxCommonDocumentCheckinData.get("server");
       
    }
    else{
    if ( store != null && (!(store.trim()).equals("")) )
    {
        store = PropertyUtil.getSchemaProperty(context,store);
    } else if (policyStore == null || (policyStore.trim().equals("")) ) {
        store = PropertyUtil.getSchemaProperty(context,"store_STORE");
    } else {
        store = policyStore;
    }
    }
    String targetPage = "/"+ appDirectory +"/emxComponentsCheckinSuccess.jsp";
    String errorPage = "/"+ appDirectory +"/emxComponentsError.jsp";
    if("true".equals(isVCDoc)) {
       errorPage = "/"+ appDirectory +"/emxCommonDocumentVCError.jsp";
    }
	String storeFromBL = DocumentUtil.getStoreFromBL(context, "Document");
	if(storeFromBL != null && (!(storeFromBL .trim()).equals("")))
		store = storeFromBL;
     System.out.println("L48 Collab & approve emxCommonDocumentCheckinDialogCode before getFCSURLInfo store: " + store);
    String processPage = "/"+ appDirectory +"/JF_ECOCommonDocumentCheckinProcess.jsp";
    Map checkinURLMap = DocumentUtil.getFCSURLInfo(context,store.trim(),noOfFiles,processPage,targetPage,errorPage,request,response);
    String checkinURL = (String)checkinURLMap.get("action");
    String ticketStr = (String)checkinURLMap.get("ticket");
    String jobTicket = (String)checkinURLMap.get("jobTicket");
    String failurePageName = (String)checkinURLMap.get("failurePageName");
    String failurePageValue = (String)checkinURLMap.get("failurePageValue");
    // Getting the display name for the page.
    String error = (String)session.getValue("error.message");

    Store storeObject = new Store(store);
    storeObject.open(context);
    boolean storeLocked = storeObject.getLocked(context);
    storeObject.close(context);

    String fileExtensionFormats = FrameworkProperties.getProperty(context,"emxComponents.VersionControl.FolderAllowedFormats");
    if (error != null) {
%>
      &nbsp;
      <table width="90%" border="0"  cellspacing="0" cellpadding="3"  class="formBG" align="center" >
        <tr >
        <%-- //XSSOK --%>
          <td class="errorHeader"><%=ComponentsUtil.i18nStringNow("emxComponents.Error.Header", request.getHeader("Accept-Language"))%></td>
        </tr>
        <tr align="center">
          <!-- //XSSOK -->
          <td class="errorMessage" align="center"><%=error%></td>
        </tr>
      </table>
<%
      session.removeValue("error.message");
    }
%>
<script language="javascript">
   var isDescriptionRequired     = "";
   var isCommentsRequired = "";
   var isTitleRequired = "";
   var validateTitle     = "";
   var validateDescription = "";
   var validateComments = "";

<%
  if ( isDescriptionRequired )
  {
%>
    isDescriptionRequired = true;
    validateDescription = true;
<%
  }

  if ( isCommentsRequired )
  {
%>
    isCommentsRequired = true;
    validateComments = true;
<%
  }
  if ( isTitleRequired )
  {
%>
    isTitleRequired = true;
    validateTitle = true;
<%
  }

  if ( canShowDescription )
  {
%>
    validateDescription = true;
<%
  }

  if ( canShowComments )
  {
%>
    validateComments = true;
<%
  }
  if ( canShowTitle )
  {
%>
    validateTitle = true;
<%
  }
%>


  function startProgressBar() {
    document.progress.src = "../common/images/utilProgressBlue.gif";
  }

  function stopProgressBar(status, message) {
    document.progress.src = "../common/images/utilSpacer.gif";

    if(status == "true") {
      alert(message);
      window.closeWindow();
      parent.window.getWindowOpener().parent.location.reload();
    } else {
      alert(message);
    }
  }
  // function to be called on click of previous button
  function goBack() {

<%   if( objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONVERT_CHECKIN_VC_FILE_FOLDER))
    {
 %>
      document.miscAction.action="emxCommonDocumentConversionDialogFS.jsp?fromAction=previous";
 <%   }
    else{
%>
    // submit with values to process page of step 4; then from there redirect to step 3
    document.miscAction.action="emxCommonDocumentCreateDialogFS.jsp?fromAction=previous";
    
    //Begin : Bug 347223 code modification -->
    // Add the information on this dialog page to request
    // thus it will be added to session and will be available
    // when we return back to this page from page1
	var divFileInformation = document.getElementById("divFileInformation");
	if (divFileInformation) {
		var objCheckinForm = document.forms["checkinForm"];
		if (objCheckinForm) {
			var element = objCheckinForm.elements["noOfFiles"];
			
			var nNoOfFiles = 0;
			if (element) {
				nNoOfFiles = element.value;
			}
			divFileInformation.innerHTML = "<input type=\"hidden\" name=\"prev_noOfFiles\" value=\"" + nNoOfFiles + "\"/>";
			
			for (var i = 0; i < nNoOfFiles; i++) {
				divFileInformation.innerHTML += "<input type=\"hidden\" name=\"prev_bfile"+i+"\" value=\"" + objCheckinForm.elements["bfile" + i].value + "\"/>";
				<%if(!showComments.equalsIgnoreCase("false")){%>
				divFileInformation.innerHTML += "<input type=\"hidden\" name=\"prev_comments"+i+"\" value=\"" + objCheckinForm.elements["comments" + i].value + "\"/>";
				<%}%>
				divFileInformation.innerHTML += "<input type=\"hidden\" name=\"prev_format"+i+"\" value=\"" + objCheckinForm.elements["format" + i].value + "\"/>";
			}
		}
	}
	//End : Bug 347223 code modification -->
    
 <%   }  %>
    addSecureToken(document.miscAction);
    document.miscAction.submit();
    removeSecureToken(document.miscAction);
    return;

  }


<%
    if (defaultFormat == null || "".equals(defaultFormat) || "null".equals(defaultFormat) ) {
%>
      alert ("<emxUtil:i18nScript localize="i18nId">emxComponents.CommonDocument.InvalidFileFormat</emxUtil:i18nScript> ");
<%
    }
%>

/**
* This function will filter the contents of value as per the property: emxFramework.FilterParameterPattern*
*/
function filterParameter(value){
      try{
        var startIndex = -1;        		      
      	<%
	      	String filterParameterPattern = FrameworkProperties.getProperty(context,"emxFramework.FilterParameterPattern");
	      	if(filterParameterPattern != null && !filterParameterPattern.trim().equals(""))
	      	{				
	      		StringTokenizer st = new StringTokenizer(filterParameterPattern, ",");
	          	String [] filters = new String[st.countTokens()];
		        for (int count = 0; st.hasMoreTokens(); count++)
		        {
	    	    	// convert to lower case for easier matching	    	    	
	        	    filters[count] = st.nextToken().trim().toLowerCase();
	    	    	if(!filters[count].equals("</script>"))
	    	    	{    
	        %>    		startIndex = value.toLowerCase().indexOf("<%=filters[count]%>");				        
	      	<%		}
	    	    	else
	    	    	{
	    	%>	    	startIndex = value.toLowerCase().indexOf("</script");
	    				if(startIndex > -1)	
	    				{
	    					if(!value.substring(startIndex + 8, startIndex + 9 ) == ">")
	    					{
	    						startIndex = -1;
	    					}
	    				}	    				
	    	<%    	}
	      	%>
	        	    if(startIndex > -1)
	           		{
	              		var subString1 = value.substring(0,startIndex);
	              		var subString2 = "";
	              		try{
	              			subString2 = value.substring(startIndex + <%=filters[count].length()%> , value.length);
	              		}catch(e){
	              			subString2 = value.substring(startIndex + <%=filters[count].length()%> , value.length());
	              		}
	              		value = subString1 + subString2;	              	
	              	}
	        <%	              
	   			}
			
	      	}%>
      	}catch (e){}
return value;
}

  function checkinFile(rowId) {
    //var parentDoc = getTopWindow().getWindowOpener().window;
  //把多选的文件分到checkinForm中的单选中
    const fileInput = document.getElementsByClassName("fileMul");
    if(fileInput.length!=0){
      var files = fileInput[0].files;
        console.log(files.length);
        if(files.length>0){
            var ff = files;
            for(let i=0;i<files.length;i++){
            const signf = document.getElementsByClassName("filesign")[i];
            const fileList1 = new DataTransfer();
            fileList1.items.add(ff[i]);
            signf.files = fileList1.files;
          }
      }
  }
<%
    if ( noOfFiles == 0 )
    {
%>
      window.closeWindow();
<%
    } else if (defaultFormat == null || "".equals(defaultFormat) || "null".equals(defaultFormat) ) {
%>
      alert ("<emxUtil:i18nScript localize="i18nId">emxComponents.CommonDocument.InvalidFileFormat</emxUtil:i18nScript> ");
      return;
<%
    } else if ( storeLocked && isVCDoc == null) {
%>
      alert ("<emxUtil:i18nScript localize="i18nId">emxComponents.CommonDocument.InvalidStore</emxUtil:i18nScript> ");
      return;
<%
    } else if ( storeLocked && "true".equalsIgnoreCase(isVCDoc)) {
     String[] messageValues = new String[1];
     messageValues[0] = store;
     String invalidStoreAlert = MessageUtil.getMessage(context,null,"emxComponents.VCDocument.InvalidStore",messageValues,null,context.getLocale(),"emxComponentsStringResource");
%>
      //XSSOK
      alert ("<%=invalidStoreAlert%>");
      return;
<%
    }
    else  if ( isAbstract ) {
%>
      alert("<emxUtil:i18nScript localize="i18nId">emxComponents.CommonDocument.InValidType</emxUtil:i18nScript>");
      return;
<%
    } else {
%>
      var filenames  = new Array(1);
      var descriptions  = new Array(1);
      var titles  = new Array(1);
      var comments  = new Array(1);
      var oldFileNames = new Array(1);
      var namecount  = 0;
      var filecount = 0;
      var badCharName;
      var macPath = "C:\\fakepath\\";
      var fileAllBadCharName = getAllFileBadChars();
      var showBadFileAlert = false;
      var badCharinFile = "";
	  var badCharFileList = "\n";
      for ( var i = 0; i < document.checkinForm.elements.length; i++ ) {
    if (document.checkinForm.elements[i].name.substring(0,5)== "bfile"){
          tempFileName = document.checkinForm.elements[i].value;
          console.log("qwewqrq="+tempFileName);
          var fileSep = "\\";

          if(isMac){
        	  tempFileName = tempFileName.replace(macPath,"");
         }else if(isLinux){
         	fileSep = "\\";
         }else if (isUnix) {
              fileSep = "/";
          }
      //Added for Bug 282356-Start
      if ( isMac && isMinMoz1)
      {
         if(tempFileName.lastIndexOf("/") != -1 && tempFileName.lastIndexOf(":") != -1 ) {
         alert ("<emxUtil:i18nScript localize='i18nId'>emxComponents.Common.FileNameContainsSlash</emxUtil:i18nScript>");
         document.checkinForm.elements[i].focus();
         return;
         }
      }
      //Bug 282356 - End

          tempFileName = tempFileName.substring(tempFileName.lastIndexOf(fileSep)+1);

    // File Name can not have special characters
    badCharName = checkForFileNameBadChars(tempFileName);

    if (badCharName.length != 0)
    {
        	showBadFileAlert = true;
        badCharinFile = badCharinFile + badCharName;
 		badCharFileList = badCharFileList + tempFileName +"\n";

    }

          filenames[namecount] = tempFileName;
          namecount = namecount +1;
        }
        if (document.checkinForm.elements[i].name.substring(0,11)== "oldFileName"){
          oldFileNames[namecount] = document.checkinForm.elements[i].value;
        }

      }

      if(showBadFileAlert){
    	 var uniqueList=badCharinFile.split(' ');
         var result = [];
         for(var i =0; i < uniqueList.length ; i++){
            if(result.indexOf(uniqueList[i]) == -1){
				result.push(uniqueList[i]);
			}
         }
         badCharinFile = result.join(" ");
         var alertMessage="<emxUtil:i18nScript localize="i18nId">emxComponents.ErrorMsg.InvalidFileName</emxUtil:i18nScript>";
         alertMessage=alertMessage.replace("{0}",badCharinFile);
         alert(alertMessage+badCharFileList+"<emxUtil:i18nScript localize="i18nId">emxComponents.Common.AlertInvalidInput</emxUtil:i18nScript>"+fileAllBadCharName+"<emxUtil:i18nScript localize="i18nId">emxComponents.Common.AlertRemoveInValidChars</emxUtil:i18nScript>");
         return;
     }
      var descriptioncount     = 0;
      var commentsCount = 0;
      var titlesCount = 0;

      for ( var i = 0; i < document.checkinForm.elements.length; i++ ) {

        if (document.checkinForm.elements[i].name.substring(0,5)== "fileN"){
          document.checkinForm.elements[i].value = filenames[filecount];
          //alert(document.checkinForm.elements[i].value );
          filecount = filecount +1;
        } else if(document.checkinForm.elements[i].name.substring(0,11)== "description") {
          document.checkinForm.elements[i].value = filterParameter(document.checkinForm.elements[i].value);
          descriptions[descriptioncount] = document.checkinForm.elements[i].value;
          descriptioncount = descriptioncount +1;
        } else if(document.checkinForm.elements[i].name.substring(0,7)== "comment") {
          document.checkinForm.elements[i].value = filterParameter(document.checkinForm.elements[i].value).trim();
          comments[commentsCount] = document.checkinForm.elements[i].value;
          commentsCount = commentsCount +1;
        }  else if(document.checkinForm.elements[i].name.substring(0,5)== "title") {
          document.checkinForm.elements[i].value = filterParameter(document.checkinForm.elements[i].value);
          titles[titlesCount] = document.checkinForm.elements[i].value;
          titlesCount = titlesCount +1;
        }
      }

      for ( var i = 0; i < document.checkinForm.elements.length; i++ ) {
	      if (document.checkinForm.elements[i].name.substring(8,15)== "comment"){
            document.checkinForm.elements[i].value = comments[0];
          }
      }

      // check for required fileds, if the file is selected
      // loop thru each element in filenames array
      var noFiles = true;
      //XSSOK
      var fileRequired = <%=fileRequired%>;
<%
      if ( "false".equalsIgnoreCase(allowDuplicateFiles) )
      {
%>
        var iFileName;
        var jFileName;
        for ( var i = 0; i < filenames.length; i++ ) {
          if( filenames[i].length > 0 ) {
            iFileName = filenames[i];
            for ( var j = 0; j < filenames.length; j++ ) {
              if( filenames[j].length > 0 ) {

              //added for bug 347132
              <%

	      if(objectId!=null && !"".equals(objectId) && docList.size()>0)
	      {
            Map  m = (Map)docList.get(0);
               String lockedId    = (String) m.get(CommonDocument.SELECT_ID);
              DomainObject object = DomainObject.newInstance(context, lockedId);
              StringList objectList= new StringList();
              objectList.add(CommonDocument.SELECT_TITLE);
              objectList.add(CommonDocument.SELECT_LOCKER);

              Map map = object.getInfo(context,objectList);
              String title=(String)map.get(CommonDocument.SELECT_TITLE);
              String locker=(String)map.get(CommonDocument.SELECT_LOCKER);
          	  String conUser=context.getUser();

                if(!UIUtil. isNullOrEmpty (locker)){
                  %>
				      var vLocker="<%=XSSUtil.encodeForJavaScript(context, locker)%>";
				      var contextUser="<%=conUser%>";
				      var vOldfile="<%=XSSUtil.encodeForJavaScript(context, title)%>";
				      if(iFileName == vOldfile && contextUser != vLocker)
				      {
				      alert("<emxUtil:i18nScript localize="i18nId">emxComponents.CommonDocument.DocumentsAreNotLockedByUser</emxUtil:i18nScript>");
				      return;
				    }

				  <%

				  }
                }
                  %>
                  // end of 347132
                if( i != j )
                {
                  jFileName = filenames[j];
                  if ( iFileName == jFileName )
                  {
                      alert("<emxUtil:i18nScript localize="i18nId">emxComponents.CommonDocument.DuplicateFileNames</emxUtil:i18nScript>");
                      return;
                  }
                }
              }
            }
          }
        }
<%
      }
%>

<%
      if ( "image".equalsIgnoreCase(objectAction) )
      {

        StringList images = null;
        String allowedFormats = FrameworkProperties.getProperty(context,"emxComponents.ImageManager.AllowedFormats");
        DomainObject obj = DomainObject.newInstance(context, objectId);
        StringList imageNames = obj.getInfoList(context, DomainObject.SELECT_IMAGE_HOLDER_MX_SMALL_IMAGE_FILE_NAMES);
        if ( imageNames != null )
        {
          images =  new StringList(imageNames.size());
          Iterator imageItr = imageNames.iterator();
          while(imageItr.hasNext())
          {
              String imageName = (String)imageItr.next();
	      if(imageName != null && !"".equals(imageName.trim()))
	      {
	      	images.add(imageName.substring(0, imageName.lastIndexOf(".")));
	      }
          }
        }
        String currentImages = FrameworkUtil.join(images, ",");


%>
       //XSSOK
       var allowedFormats = "<%=allowedFormats%>";
       //XSSOK
       var currentImages = "<%=currentImages%>";
       allowedFormats = allowedFormats.toLowerCase();


        var imageFileName;
        var imageExt;
        for ( var i = 0; i < filenames.length; i++ ) {
          if( filenames[i].length > 0 ) {
            imageFileName = filenames[i];
            imageExt = imageFileName.substring(imageFileName.lastIndexOf(".")+1, imageFileName.length);
            imageName = imageFileName.substring(0,imageFileName.lastIndexOf("."));
            if ( allowedFormats.indexOf(imageExt.toLowerCase()) < 0 )
            {
                alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Image.NotSupportedFormat</emxUtil:i18nScript>" + allowedFormats);
                return;
            }
            if ( currentImages.indexOf(imageName) >= 0 )
            {
                if(!confirm("<emxUtil:i18nScript localize="i18nId">emxComponents.Image.ConfirmFileOverwrite</emxUtil:i18nScript>" ))
                {
                   eval("document.checkinForm.bfile" + i + ".focus()");
                   return;
                }
//                alert("file already exsists");
//                return;
            }
          }
        }
<%
      } else {
        String restrictedFormats = FrameworkProperties.getProperty(context,"emxComponents.Commondocument.RestrictedFormats");
        boolean allowedFormatsEnable = false;
        String CDMFileExtensionFormats = "";
        try
        {
        	CDMFileExtensionFormats = FrameworkProperties.getProperty(context,"emxComponents.Commondocument.SupportedFormats");
        	if (CDMFileExtensionFormats != null && !"".equals(CDMFileExtensionFormats))
        	{
        		allowedFormatsEnable = true;
        	}
        } catch(Exception ex)
        {
        	//Do Nothing
        }
%>
       var restrictedFormats = "<%=restrictedFormats%>";
       restrictedFormats = restrictedFormats.toLowerCase();
       restrictedFormatArr = restrictedFormats.split(",");
<%
		if(allowedFormatsEnable)
		{
%>
       var CDMFileExtensionFormats = "<%=CDMFileExtensionFormats%>";
       CDMFileExtensionFormats = CDMFileExtensionFormats.toLowerCase();
       CDMFileExtensionFormatArr = CDMFileExtensionFormats.split(",");
<%
		}
%>
        var vFileName;
        var fileExt;
        for ( var i = 0; i < filenames.length; i++ ) {
          if( filenames[i].length > 0 ) {
            vFileName = filenames[i];
            fileExt = vFileName.substring(vFileName.lastIndexOf(".")+1, vFileName.length);
               	if(jQuery.inArray(fileExt.toLowerCase(), restrictedFormatArr) >= 0){
	                alert("<emxUtil:i18nScript localize="i18nId">emxComponents.CommonDocument.NotSupportedFormat</emxUtil:i18nScript>" + restrictedFormats);
	                return;
	            }
<%
		if(allowedFormatsEnable)
		{
%>
               	if(jQuery.inArray(fileExt.toLowerCase(), CDMFileExtensionFormatArr) < 0){
	                alert("<emxUtil:i18nScript localize="i18nId">emxComponents.CommonDocument.SupportedFormat</emxUtil:i18nScript>" + CDMFileExtensionFormats);
	                return;
	            }
 <%
          }
 %>
          }
        }
<%
      }
%>

      var strMessage = "";
      var iFocusField;
      for ( var i = 0; i < filenames.length; i++ )
      {
        if (isDescriptionRequired)
        {
          if( filenames[i].length == 0 && descriptions[i].length > 0)
          {
            noFiles = true;
          }

        }
        if( filenames[i].length > 0 )
        {
          noFiles = false;
          if( isDescriptionRequired && descriptions[i].length == 0) {
            alert ("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.FillDescription</emxUtil:i18nScript>");
            eval("document.checkinForm.description" + i + ".focus()");
            return;
          }
          if ( validateDescription )
          {
              badCharName = checkForBadChars(eval("document.checkinForm.description" + i));
              if (badCharName.length != 0)
              {
                alert("<emxUtil:i18nScript localize="i18nId">emxComponents.ErrorMsg.InvalidInputMsg</emxUtil:i18nScript>"+badCharName+"<emxUtil:i18nScript localize="i18nId">emxComponents.Common.AlertRemoveInValidChars</emxUtil:i18nScript>");
                eval("document.checkinForm.description" + i + ".focus()");
                return;
              }
          }

          if( isCommentsRequired && comments[i].length == 0) {
            alert ("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.FillComments</emxUtil:i18nScript>");
            eval("document.checkinForm.comments" + i + ".focus()");
            return;
          }
          if ( validateComments )
          {
              badCharName = checkForBadChars(eval("document.checkinForm.comments" + i));
              if (badCharName.length != 0)
              {
              	var commentAllBadCharName = getAllBadChars(eval("document.checkinForm.comments" + i));
      			alert("<emxUtil:i18nScript localize="i18nId">emxComponents.ErrorMsg.InvalidInputMsg</emxUtil:i18nScript>"+badCharName+"<emxUtil:i18nScript localize="i18nId">emxComponents.Common.AlertInvalidInput</emxUtil:i18nScript>"+commentAllBadCharName+"<emxUtil:i18nScript localize="i18nId">emxComponents.Common.AlertRemoveInValidChars</emxUtil:i18nScript>");
		        //alert("<emxUtil:i18nScript localize="i18nId">emxComponents.ErrorMsg.InvalidInputMsg</emxUtil:i18nScript>"+badCharName+"<emxUtil:i18nScript localize="i18nId">emxComponents.Common.AlertRemoveInValidChars</emxUtil:i18nScript>");
                eval("document.checkinForm.comments" + i + ".focus()");
                return;
              }
          }
          if ( isTitleRequired && titles[i].length == 0 ) {
            alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.TitleError</emxUtil:i18nScript>");
            eval("document.checkinForm.title" + i + ".focus()");
            return;
          }
          if ( validateTitle )
          {
              badCharName = checkForBadChars(eval("document.checkinForm.title" + i));
              if (badCharName.length != 0)
              {
                alert("<emxUtil:i18nScript localize="i18nId">emxComponents.ErrorMsg.InvalidInputMsg</emxUtil:i18nScript>"+badCharName+"<emxUtil:i18nScript localize="i18nId">emxComponents.Common.AlertRemoveInValidChars</emxUtil:i18nScript>");
                eval("document.checkinForm.title" + i + ".focus()");
                return;
              }
          }

<%
          if ( "true".equalsIgnoreCase(allowFileNameChange) ) {
%>
            if (oldFileNames[i] != "" && oldFileNames[i].toLowerCase() != filenames[i].toLowerCase())
            {
              if(strMessage == ""){
                iFocusField  = i;
              }
              strMessage = strMessage + "\n" + oldFileNames[i] ;
            }
<%
          } else {
%>
            if (oldFileNames[i] != "" && oldFileNames[i].toLowerCase() != filenames[i].toLowerCase()) {
              eval("document.checkinForm.bfile" + i + ".focus()");
              alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.FileDifferent</emxUtil:i18nScript>");
              return;
            }
<%
          }
//Added OR condition for module checkin Jan 02 2009
         if ( objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_FILE_FOLDER) ||
              objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONVERT_CHECKIN_VC_FILE_FOLDER) ||
              objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ZIP_TAR_GZ) ||
               objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CHECKIN_VC_FOLDER) ||

               objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ON_DEMAND) )
             //  objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CHECKIN_VC_MODULE))
         {
            String fileORfolder = (String) emxCommonDocumentCheckinData.get("vcDocumentType");
            if(objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ZIP_TAR_GZ) ||
                      ( fileORfolder != null && (fileORfolder.equalsIgnoreCase("Folder") || fileORfolder.equalsIgnoreCase("Module"))) || objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CHECKIN_VC_FOLDER))
            {
%>
                var fileName =  filenames[i];
                var fileExtensionFormats = "<%=fileExtensionFormats%>";
                var fileExt = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length);
                fileExt= fileExt.toLowerCase();
                if(fileExtensionFormats.indexOf(fileExt.toLowerCase()) < 0){
                    alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Image.NotSupportedFormat</emxUtil:i18nScript>" + fileExtensionFormats);
                    return;
                }
<%
            } //inner if
          }//outer if
         if ( objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CHECKIN_VC_FILE) ||
          objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ON_DEMAND)){
               DomainObject checkObj = new DomainObject(objectId);
               String checking = checkObj.getInfo(context, CommonDocument.SELECT_VCFOLDER);
               String checkingModule = checkObj.getInfo(context, CommonDocument.SELECT_VCMODULE);
               if("true".equalsIgnoreCase(checking) || "true".equalsIgnoreCase(checkingModule)){
 %>
                var fileName =  filenames[i];
                var fileExtensionFormats = "<%=fileExtensionFormats%>";
                var fileExt = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length);
                fileExt= fileExt.toLowerCase();
                if(fileExtensionFormats.indexOf(fileExt.toLowerCase()) < 0){
                    alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Image.NotSupportedFormat</emxUtil:i18nScript>" + fileExtensionFormats);
                    return;
                }
<%           }// eof Checking if
    } // eof object action to Checkin VC FIle
 %>
        }
      }
      if(strMessage != "")
      {
        if(!confirm("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.ConfirmChangeOfFileName</emxUtil:i18nScript>" + strMessage + "\n" +"<emxUtil:i18nScript localize="i18nId">emxComponents.Common.ConfirmMessege</emxUtil:i18nScript>"))
        {
           eval("document.checkinForm.bfile" + iFocusField + ".focus()");
           return;
        }
      }

      if ( noFiles && fileRequired)
      {
        alert ("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.SelectFile</emxUtil:i18nScript>");
        return;
      }
      if(noFiles){
    	  document.checkinForm.action = "../<%=appDirectory%>/JF_ECOCommonDocumentCheckinProcess.jsp";
    	  document.checkinForm.enctype = "application/x-www-form-urlencoded";
      }

      parent.turnOnProgress();
      // prevent the multliple submit actions

      if (jsDblClick()) {
	    toggleMaxInactiveInterval();
        document.checkinForm.submit();
        commonDocTurnOnProgress(true);
      }

<%
    }
%>
  }
 function checkinCancel() {
<%
    if ( objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_FILE_FOLDER)
        || objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ZIP_TAR_GZ)
        || objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONVERT_CHECKIN_VC_FILE_FOLDER)) {
 %>
     document.miscAction.action = "emxCommonDocumentCancelProcess.jsp?objectId=<%=XSSUtil.encodeForURL(context, objectId)%>";
     //document.miscAction.enctype = "application/x-www-form-urlencoded";
     addSecureToken(document.miscAction);
     document.miscAction.submit();
     removeSecureToken(document.miscAction);
     //getTopWindow().close();
<%
   }
    else {
 %>
      getTopWindow().closeWindow();
  <%
    }
  %>
  } // eof Checkin Cancel
  function commonDocTurnOnProgress() {
    if (parent){
      if (parent.frames[0].document.progress){
  parent.frames[0].document.progress.src="../common/images/utilProgressBlue.gif";
      }
    }
    turnOnProgress();
  }

  function toggleMaxInactiveInterval(){
	jQuery.ajax({
              url: '../components/emxCommonDocumentSetMaxInactiveInterval.jsp',
              type: 'GET',
              cache: false,
              contentType: false,
              processData: false
              });
  }
  function displaymessage(objElement,event)
  {
	  // for bug IR-226836V6R2014 checkin use case
  	var code = (event.keyCode ? event.keyCode : event.which);
	if(code == "0")
		return ;
	// For Bug 345330 - Empty the value of the text field after some charcter is typed in manually.
  	objElement.value = "";

    alert("<emxUtil:i18nScript localize="i18nId">emxComponents.Common.BrowseWithButton</emxUtil:i18nScript>");
    return false;
  }
    /**
    *
    * add by chenyan 2025/04/08 选择文件时联动标题
    *
    *
    */
    function writeDocumentTitle(objElement,strDomName){
        const domArr  = document.getElementsByName(strDomName);
        if(domArr){
            const dom = domArr[0];
            const files = objElement.files;
            if( files && files.length >= 1){
                const fileObj = files[0];
                  const filename = fileObj.name
                domArr[0].value = filename.substring(0, filename.lastIndexOf('.'));
            }
        }
    }
</script>
<body class="editable">
<%
if("image".equalsIgnoreCase(objectAction)){
%>
<form>
<table class="list">
<tr>
<th class="required" width="100%">
<emxUtil:i18n localize="i18nId">emxComponents.Common.File</emxUtil:i18n>
</th>
</tr>
<tr>
<td>
<input type="file" name="bbfile" size="30" onpaste="return false;" onKeyDown="this.blur()"  onkeypress="displaymessage(this);return false;" class="filemul" multiple/>
</td>
</tr>
</table>
</form>
<form name = "checkinForm" class="myclass" method = "post" enctype = "multipart/form-data" action="<%=XSSUtil.encodeForHTMLAttribute(context, checkinURL)%>" target="<%=XSSUtil.encodeForHTMLAttribute(context, target)%>" style="display: none;">
<%
}else{%>
<form name = "checkinForm" class="myclass" method = "post" enctype = "multipart/form-data" action="<%=XSSUtil.encodeForHTMLAttribute(context, checkinURL)%>" target="<%=XSSUtil.encodeForHTMLAttribute(context, target)%>" >
<%
}%>
<%@include file = "../common/enoviaCSRFTokenInjection.inc"%>
<%
  String requiredText = ComponentsUtil.i18nStringNow("emxComponents.Commom.RequiredText",request.getHeader("Accept-Language"));
  boolean showPlugInNotFoundAlert = "true".equalsIgnoreCase(plugInNotFoundAlert);
%>
<framework:ifExpr expr="<%=showPlugInNotFoundAlert%>">
	<table>
		<tr>
			<td align="center" style="height:40px;font-size:11px;font-weight:bold;font-style:italic;color:#555;background-color:transparent;border:none;">
				<emxUtil:i18n localize="i18nId">emxComponents.Applet.RequiredJavaPlugInNotFound</emxUtil:i18n>
			</td>
		</tr>

	</table>
</framework:ifExpr>

            <!-- //XSSOK -->
          <input type=hidden name=noOfFiles value="<%=noOfFiles%>"/>
          <input type=hidden name=parentId value="<%=XSSUtil.encodeForHTMLAttribute(context, parentId)%>"/>
          <!-- //XSSOK -->
		  <input type=hidden name=objectId value="<%=XSSUtil.encodeForHTMLAttribute(context, objectId)%>"/>
	      <!-- //XSSOK -->
          <framework:ifExpr expr="<%=DocumentUtil.isFCSEnable(context, store)%>">
              <input type="hidden" name="<%=jobTicket%>" value="<xss:encodeForHTMLAttribute><%=ticketStr%></xss:encodeForHTMLAttribute>"/>
              <input type="hidden" name="<%=failurePageName%>" value="<xss:encodeForHTMLAttribute><%=failurePageValue%></xss:encodeForHTMLAttribute>" />
          </framework:ifExpr>
          <input type="hidden" name="store" value="<xss:encodeForHTMLAttribute><%=store%></xss:encodeForHTMLAttribute>"/>
<script>
	addStyleSheet("emxUIDefault");
	addStyleSheet("emxUIList");
</script>
<table class="list">

   <tr>
<%
        boolean showCurrentAndUpdateFile = objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_MASTER) || objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_HOLDER);

        int fileColWidth = 25;
        int titleColWidth = 25;
        int descColWidth = 25;
        int commentsColWidth = 25;

        fileColWidth = !(canShowTitle || isTitleRequired) ? fileColWidth + titleColWidth : fileColWidth;
        fileColWidth = !(canShowDescription || isDescriptionRequired) ? fileColWidth + descColWidth : fileColWidth;
        fileColWidth = !(canShowComments || isCommentsRequired) ? fileColWidth + commentsColWidth : fileColWidth;

%>
            <!-- //XSSOK -->
            <th class="<%=isFileRequired ? "required" : ""%>" width="<%=fileColWidth%>%">
            	<framework:ifExpr expr="<%=showCurrentAndUpdateFile%>">
		           <emxUtil:i18n localize="i18nId">emxComponents.Common.CurrentFile</emxUtil:i18n> |
		           <emxUtil:i18n localize="i18nId">emxComponents.Common.UpdatedFile</emxUtil:i18n>
            	</framework:ifExpr>
            	<framework:ifExpr expr="<%=!showCurrentAndUpdateFile%>">
            		<emxUtil:i18n localize="i18nId">emxComponents.Common.File</emxUtil:i18n>
            	</framework:ifExpr>
            	<framework:ifExpr expr="<%=canShowFormat || isFormatRequired || isFormatReadOnly%>">
            		| <emxUtil:i18n localize="i18nId">emxComponents.Common.Format</emxUtil:i18n>
            	</framework:ifExpr>
        	</th>

        <framework:ifExpr expr="<%=canShowTitle || isTitleRequired%>">
	        <th class="<%=isTitleRequired ? "required" : "" %>" width="<%=titleColWidth%>%">
    	      <emxUtil:i18n localize="i18nId">emxComponents.Common.Title</emxUtil:i18n>
	        </th>
        </framework:ifExpr>
         <framework:ifExpr expr="<%=canShowFileTitle || isFileTitleRequired%>">
	        <th class="<%=isFileTitleRequired ? "required" : "" %>" width="<%=titleColWidth%>%">
    	      <emxUtil:i18n localize="i18nId">emxComponents.Common.Category.FileTitle</emxUtil:i18n>
	        </th>
        </framework:ifExpr>
        <framework:ifExpr expr="<%=canShowDescription || isDescriptionRequired%>">
	        <th class="<%=isDescriptionRequired ? "required" : ""%>" width="<%=descColWidth%>%">
    	      <emxUtil:i18n localize="i18nId">emxComponents.Common.Description</emxUtil:i18n>
	        </th>
        </framework:ifExpr>
        <framework:ifExpr expr="<%=canShowComments || isCommentsRequired%>">
	        <th class="<%=isCommentsRequired ? "required" : ""%>" width="<%=commentsColWidth%>%">
    	      <emxUtil:i18n localize="i18nId">emxComponents.Common.Comments</emxUtil:i18n>
	        </th>
        </framework:ifExpr>
     </tr>

<%

      String format = "";
      StringList objectFiles = (StringList)objectMap.get(CommonDocument.SELECT_FILE_NAME);
      StringList objectFileFormats = (StringList)objectMap.get(CommonDocument.SELECT_FILE_FORMAT);
      String prevId = "";
      String displayTitle = "";
      for(int i=0; i < noOfFiles; i++)
      {
          Map m = new HashMap();
          String lockedId = "";
          String masterId = "";
          String commentsName = com.matrixone.client.fcs.FcsClient.resolveFcsParam(com.matrixone.fcs.mcs.McsBase.getCommentParamName(i));
          oldFileName = UIUtil.isNullOrEmpty(oldFileName) ? "" : oldFileName;

          String description = "";
          if ( !objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_HOLDER) )
          {
              if ( docList.size() > i )
              {
                  m = (Map)docList.get(i);
                  lockedId    = (String) m.get(CommonDocument.SELECT_ID);
                  oldFileName = (String) m.get(CommonDocument.SELECT_TITLE);
                  description = (String) m.get(CommonDocument.SELECT_CHECKIN_REASON);
                  int index = objectFiles.indexOf(oldFileName);
                  if ( index != -1 && objectFileFormats.size() >= index)
                  {
                      defaultFormat = (String)objectFileFormats.get(index);
                  }
                  if (oldFileName == null || "".equals(oldFileName) || "null".equals(oldFileName) )
                  {
                      oldFileName = (String) m.get(CommonDocument.SELECT_FILE_NAME);
                      description = (String) m.get(CommonDocument.SELECT_DESCRIPTION);
                  }

              }
          } else {
              if( docList.size() > i )
              {
                    m = (Map)docList.get(i);
                    masterId = (String) m.get(CommonDocument.SELECT_MASTER_ID);
                    lockedId = (String) m.get(CommonDocument.SELECT_ID);
                    oldFileName = (String) m.get(CommonDocument.SELECT_TITLE);
                    description = (String) m.get(CommonDocument.SELECT_CHECKIN_REASON);
                    displayTitle = (String) m.get(CommonDocument.SELECT_MASTER_TITLE);
                    try
                    {
                        formatList = (StringList)m.get(CommonDocument.SELECT_MASTER_FORMATS);
                    } catch (ClassCastException cex)
                    {
                        formatList.add((String)m.get(CommonDocument.SELECT_MASTER_FORMATS));
                    }
                    try
                    {
                        objectFiles = (StringList)m.get(CommonDocument.SELECT_MASTER_FILE_NAME);
                        objectFileFormats = (StringList)m.get(CommonDocument.SELECT_MASTER_FILE_FORMAT);
                        int index = objectFiles.indexOf(oldFileName);
                        if ( index != -1 && objectFileFormats.size() >= index)
                        {
                            defaultFormat = (String)objectFileFormats.get(index);
                        }

                    } catch(ClassCastException cex)
                    {
                        defaultFormat = (String)m.get(CommonDocument.SELECT_MASTER_FILE_FORMAT);
                    }
              }
          }
          String descCellLength = "40";
          if ( objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_HOLDER) )
          {
              if(!prevId.equals(masterId) )
              {
%>
<tr>
	<td class="label" colspan="2">
		<%=XSSUtil.encodeForHTML(context, displayTitle)%>
	</td>
</tr>
<%
              }
              prevId = masterId;
          }
%>
	 <tr class='<framework:swap id="1"/>'>
        <!-- //XSSOK -->
        <td width="<%=fileColWidth%>%">
          	<table>
	          	<tr>
    	      		<td style="font-weight:bold;">
			          <input type="hidden" name="oldFileName<%=i%>" value="<xss:encodeForHTMLAttribute><%=oldFileName%></xss:encodeForHTMLAttribute>"/> <%=oldFileName%>
			          <input type="hidden" name="oid<%=i%>" value="<%=XSSUtil.encodeForHTMLAttribute(context, lockedId)%>"/>
					  <!-- //XSSOK -->
			          <input type="hidden" name="<%=commentsName%>" value=""/>
					</td>
				</tr>
				<tr>
					<td>
<%
				//Begin: Bug 347223 code
				// Select previous value for format if available
				String strPrevFile = (String)mapPreviousCheckinFormData.get("prev_bfile"+i);
				strPrevFile = UIUtil.isNullOrEmpty(strPrevFile) ? "" : strPrevFile;
				// End : Bug 347223 code
%>
          	<!-- For Bug 345330 - Empty the value of the text field after some charcter is typed in manually. -->
			          	<input type="file" name="bfile<%=i%>" onchange="writeDocumentTitle(this,'title<%=i%>')" size="30" onpaste="return false;" onKeyDown="this.blur()"  onkeypress="displaymessage(this);return false;" value="<xss:encodeForHTMLAttribute><%=strPrevFile%></xss:encodeForHTMLAttribute>" class="filesign"/>
			          	<input type=hidden name="fileName<%=i%>" value=""/>
			          	<!-- //XSSOK -->
						<framework:ifExpr expr="<%=objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_HOLDER)%>">
			          		<input type="hidden" name="objectId<%=i%>" value="<%=XSSUtil.encodeForHTMLAttribute(context, masterId)%>"/>
			          	</framework:ifExpr>
					</td>
				</tr>
				<tr>
					<td  style="padding:3px;">
<%
				        String i18nFormat = i18nNow.getMXI18NString(defaultFormat,"",languageStr,"Format");
				        if ( canShowFormat || isFormatRequired ) {
				            // Begin: Bug 347223 code
				            // Select previous value for format if available
				            String strFormatToSelect = (String)mapPreviousCheckinFormData.get("prev_format"+i);
				        	if (strFormatToSelect == null) {
				        	    strFormatToSelect = defaultFormat;
				        	}
				            // End : Bug 347223 code
%>
			            	<select name="format<%=i%>" size="1">
<%
				              Iterator formatItr = formatList.iterator();
				              while (formatItr.hasNext() )	{
					                format = (String)formatItr.next();
					                i18nFormat = EnoviaResourceBundle.getMXI18NString(context,format,"",languageStr,"Format");
					                String selected = format.equals(strFormatToSelect) ? "selected" : "";
%>
								  <!-- //XSSOK -->
				                  <option value="<%=XSSUtil.encodeForHTMLAttribute(context, format)%>" <%=selected%>><%=i18nFormat%></option>
<%
			              		}
%>
			            	</select>
<%
        				} else if ( isFormatReadOnly ) {
%>
					          <input type="hidden" name="format<%=i%>" value="<%=XSSUtil.encodeForHTMLAttribute(context, defaultFormat)%>"/> <%=i18nFormat%>
<%
				        } else if(fileFormat != null && !"".equals(fileFormat) && !"null".equals(fileFormat)){
					           // String sFormat = PropertyUtil.getSchemaProperty(context, fileFormat);
%>
					          <input type=hidden name="format<%=i%>" value="<%=XSSUtil.encodeForHTMLAttribute(context, fileFormat)%>"/>
<%
				        } else {
%>
					          <input type=hidden name="format<%=i%>" value="<%=XSSUtil.encodeForHTMLAttribute(context, defaultFormat)%>"/>
<%
				        }
%>
				</td>
			</tr>
		</table>
      </td>
      <framework:ifExpr expr="<%=(canShowTitle || isTitleRequired)%>">
			<td width="<%=titleColWidth%>%">   
				<input type="text" name="title<%=i%>" value="" />
			</td>   	
      </framework:ifExpr>
      <framework:ifExpr expr="<%=(canShowFileTitle || isFileTitleRequired)%>">
			<td width="<%=titleColWidth%>%">   
				<input type="text" name="fileTitle<%=i%>" value="" />
			</td>   	
      </framework:ifExpr>
      <framework:ifExpr expr="<%=(canShowDescription || isDescriptionRequired)%>">
      		<td width="<%=descColWidth%>%">
      			<textarea rows="3" name="description<%=i%>" cols="<%=descCellLength%>" wrap><xss:encodeForHTML><%=description%></xss:encodeForHTML></textarea>
      		</td>
      </framework:ifExpr>
<%
        // Begin : Bug 347223 code
        String strPrevComment = (String)mapPreviousCheckinFormData.get("prev_comments"+i);
		strPrevComment = !UIUtil.isNullOrEmpty(strPrevComment) ? strPrevComment : "";
		// End : Bug 347223 code  
%>
		<framework:ifExpr expr="<%=(canShowComments || isCommentsRequired)%>">
			<td width="<%=commentsColWidth%>%">
				<textarea rows="3" name="comments<%=i%>" cols="<%=descCellLength%>" wrap><xss:encodeForHTML><%=strPrevComment%></xss:encodeForHTML></textarea>
			</td>
		</framework:ifExpr>
    </tr>
<%
      }
%>
  	  <framework:ifExpr expr="<%= noOfFiles == 0 %>">
  	  	<tr>
  	  		 <td class="error" align="center">
  	  		 	<emxUtil:i18n localize="i18nId">emxComponents.MultiFileUpload.NoFilesLocked</emxUtil:i18n>
  	  		 </td>
  	  	</tr>
  	  </framework:ifExpr>	  
    </table>
<%
  Enumeration enumParam = request.getParameterNames();
  while ( enumParam.hasMoreElements())
  {
      String paramName =  (String)enumParam.nextElement();
      String paramValue = emxGetParameter(request,paramName);
      if ( paramName.indexOf("show") != 0 && !usedParamList.contains(paramName) )
      {
%>
        <input type="hidden" name="<xss:encodeForHTMLAttribute><%=paramName%></xss:encodeForHTMLAttribute>" value="<xss:encodeForHTMLAttribute><%=paramValue%></xss:encodeForHTMLAttribute>"/>
<%
      }
  }
%>
</form>
</body>

<form name="miscAction" class="myclass" method="post" target="_parent">
<input type="hidden" name=fromAction value="previous" />
<input type="hidden" name=objectId value=<%=XSSUtil.encodeForHTMLAttribute(context, objectId)%> />

<!-- Begin : Bug 347223 code modification -->
<div id="divFileInformation"></div>
<!-- End : Bug 347223 code modification -->

</form>
