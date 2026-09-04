<%--  emxProjectManagementUtil.jsp

   Copyright (c) Dassault Systemes, 1992-2020 .All rights reserved

--%>
<%@page import="com.dassault_systemes.enovia.e6wv2.foundation.jaxb.Datacollections"%> 
<%@page import="com.dassault_systemes.enovia.e6wv2.foundation.jaxb.Dataobject"%>
<%@page import="java.util.Set"%>
<%@page import="com.matrixone.apps.common.TaskDateRollup"%>
<%@page import="jakarta.json.JsonObjectBuilder"%>
<%@page import="jakarta.json.Json"%>
<%@page import="jakarta.json.JsonObject"%>
<%@page import="java.lang.reflect.Method"%>
<%@page import="java.util.List"%>
<%@page import="com.matrixone.apps.common.TaskDateRollup"%>

<%@include file="emxProgramGlobals2.inc"%>
<%@include file="../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file="../emxUICommonAppInclude.inc"%>
<%@ include file = "../emxUICommonHeaderBeginInclude.inc" %>
<%@include file = "../emxUICommonHeaderEndInclude.inc" %>
<%@include file = "../common/emxUIConstantsInclude.inc"%>


<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@page import="com.matrixone.apps.common.Company,matrix.util.StringList" %>

<%@page import="java.util.Enumeration"%>
<%@page import="com.matrixone.apps.program.ProgramCentralConstants"%>
<%@page import="com.matrixone.apps.domain.util.XSSUtil"%>
<%@page import="com.matrixone.apps.domain.DomainConstants"%>


<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>
<jsp:useBean id="formBean" scope="session" class="com.matrixone.apps.common.util.FormBean"/>
<SCRIPT language="javascript" src="../common/scripts/emxUICore.js"></SCRIPT>
<script language="javascript" src="../common/scripts/emxUIConstants.js"></script>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script src="../programcentral/emxProgramCentralUIFormValidation.js" type="text/javascript"></script>

<%


	 String strMode = emxGetParameter(request, "mode");
	 strMode = XSSUtil.encodeURLForServer(context, strMode);


    if("PMCDeliverableURL".equalsIgnoreCase(strMode)) {
                String strGridActive = "false";
               /*try {
			strGridActive = EnoviaResourceBundle.getProperty(context, "emxFramework.Activate.GridView");
		} catch (FrameworkException e) {
			strGridActive = "false";
		}*/
		String strURL = "../common/emxIndentedTable.jsp?program=emxTask:getURLDeliverables&table=PMCBookmarkSummary&toolbar=PMCDeliverablesBookmarkToolBar&selection=multiple&Export=false&sortColumnName=Name&sortDirection=ascending&header=emxProgramCentral.Common.Bookmarks&HelpMarker=emxhelpdeliverables&hideLaunchButton=true&showRMB=false&postProcessJPO=emxProgramCentralUtil:postProcessRefresh&customize=false&displayView=details&autoFilter=false&showPageURLIcon=false&rowGrouping=false&findMxLink=false&multiColumnSort=false&objectCompare=false&showClipboard=false";
		//HondaGrid-FUN087459
		// replace common/emxIndentedTable.jsp with webapps/ENXSBGridConnectorClient/ENXSBGridConnectorClient.html 
		if ("true".equalsIgnoreCase(strGridActive)) {
			strURL = "../webapps/ENXSBGridConnector/ENXSBGridConnectorClient.html?program=emxTask:getURLDeliverables&table=PMCBookmarkSummary&toolbar=PMCDeliverablesBookmarkToolBar&selection=multiple&Export=false&sortColumnName=Name&sortDirection=ascending&header=emxProgramCentral.Common.Bookmarks&HelpMarker=emxhelpdeliverables&hideLaunchButton=true&showRMB=false&postProcessJPO=emxProgramCentralUtil:postProcessRefresh&customize=false&displayView=details&autoFilter=false&showPageURLIcon=false&rowGrouping=false&findMxLink=false&multiColumnSort=false&objectCompare=false&showClipboard=false";

		}
		String objectId = emxGetParameter(request, "objectId");

		DomainObject dom = DomainObject.newInstance(context, objectId);
		StringList slBookmarkSelect = new StringList();
		slBookmarkSelect.add(DomainConstants.SELECT_CURRENT);
		//slBookmarkSelect.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
		Map bookmarkInfoMap = dom.getInfo(context, slBookmarkSelect);

		String strCurrentState = (String) bookmarkInfoMap.get(DomainConstants.SELECT_CURRENT);
		boolean editFlag = true;
		if (ProgramCentralConstants.STATE_PROJECT_TASK_REVIEW.equalsIgnoreCase(strCurrentState)
				|| ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE.equalsIgnoreCase(strCurrentState)) {
			editFlag = false;
		}
		if (editFlag) {
			strURL += "&editLink=true";
		}

		Enumeration requestParams = emxGetParameterNames(request);
		StringBuilder url = new StringBuilder();

		if (requestParams != null) {
			while (requestParams.hasMoreElements()) {
				String param = (String) requestParams.nextElement();
				String value = emxGetParameter(request, param);
				url.append("&" + param);
				url.append("=" + XSSUtil.encodeForURL(context, value));
			}
			strURL += url.toString();
		}
%>
<script language="javascript">
 var strUrl = "<%=strURL%>";
 document.location.href = strUrl;
</script>
<%
	} else if ("PMCReferenceDocumentURL".equalsIgnoreCase(strMode)) {
		String strURL = "../common/emxIndentedTable.jsp?program=emxTask:getURLReferenceDocuments&table=PMCBookmarkSummary&toolbar=PMCReferenceDocumentsBookmarkToolBar&selection=multiple&Export=false&sortColumnName=Name&sortDirection=ascending&header=emxProgramCentral.Common.Bookmarks&HelpMarker=emxhelpdeliverables&hideLaunchButton=true&showRMB=false&postProcessJPO=emxProgramCentralUtil:postProcessRefresh&customize=false&displayView=details&autoFilter=false&showPageURLIcon=false&rowGrouping=false&findMxLink=false&multiColumnSort=false&objectCompare=false&showClipboard=false";
		String objectId = emxGetParameter(request, "objectId");

		DomainObject dom = DomainObject.newInstance(context, objectId);
		StringList slBookmarkSelect = new StringList();
		slBookmarkSelect.add(DomainConstants.SELECT_CURRENT);
		//slBookmarkSelect.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
		Map bookmarkInfoMap = dom.getInfo(context, slBookmarkSelect);

		String strCurrentState = (String) bookmarkInfoMap.get(DomainConstants.SELECT_CURRENT);
		boolean editFlag = true;
		if (ProgramCentralConstants.STATE_PROJECT_TASK_REVIEW.equalsIgnoreCase(strCurrentState)
				|| ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE.equalsIgnoreCase(strCurrentState)) {
			editFlag = false;
		}
		if (editFlag) {
			strURL += "&editLink=true";
		}

		Enumeration requestParams = emxGetParameterNames(request);
		StringBuilder url = new StringBuilder();

		if (requestParams != null) {
			while (requestParams.hasMoreElements()) {
				String param = (String) requestParams.nextElement();
				String value = emxGetParameter(request, param);
				url.append("&" + param);
				url.append("=" + XSSUtil.encodeForURL(context, value));
			}
			strURL += url.toString();
		}
%>
<script language="javascript">
 var strUrl = "<%=strURL%>";
 document.location.href = strUrl;
</script>
<%

}else if("searchResolvesRIO".equalsIgnoreCase(strMode)) {
    String objId   = emxGetParameter(request, "objectId");
    objId          = XSSUtil.encodeURLForServer(context, objId);

  //  String strURL = "../common/emxIndentedTable.jsp?program=emxProjectSpace:getRIOItems&table=PMCResolvesRIOTable&selection=multiple&header=emxProgramCentral.AddResolvesRIO&sortColumnName=Name&sortDirection=ascending&Export=false&freezePane=Name&showClipboard=false&showPageURLIcon=false&triggerValidation=false&massPromoteDemote=false&displayView=details&suiteKey=ProgramCentral&StringResourceFileId=emxProgramCentralStringResource&submitURL=../programcentral/emxProjectManagementUtil.jsp?mode=connectRIOItems&&hideLaunchButton=true&hideToolbar=false";
    String strURL = "../common/emxFullSearch.jsp?field=TYPES=type_Issue,type_Opportunity,type_Risk:CURRENT!=policy_ProjectRisk.state_Complete,policy_Issue.state_Closed&table=PMCResolvesRIOTable&showInitialResults=false&selection=multiple&header=emxProgramCentral.AddResolvesRIO&submitURL=../programcentral/emxProjectManagementUtil.jsp?mode=connectRIOItems&suiteKey=ProgramCentral&StringResourceFileId=emxProgramCentralStringResource";
    strURL +="&objectId="+objId;
       %>
       <script language="javascript">
       var strUrl = "<%=strURL%>";
  	   //document.location.href = strUrl;	
  	   showModalDialog(strUrl, "812", "700", "true", "popup");
       </script>
       <%


} else if("searchResolves".equalsIgnoreCase(strMode)) {
    String objId   = emxGetParameter(request, "objectId");
    objId          = XSSUtil.encodeURLForServer(context, objId);

//  String strURL = "../common/emxIndentedTable.jsp?program=emxRisk:getResolvingProjectList&table=PMCProjectSummaryForProjects&selection=multiple&header=emxProgramCentral.AddResolvedBy&sortColumnName=Name&sortDirection=ascending&Export=false&freezePane=Name&showClipboard=false&showPageURLIcon=false&triggerValidation=false&massPromoteDemote=false&displayView=details&suiteKey=ProgramCentral&StringResourceFileId=emxProgramCentralStringResource&submitURL=../programcentral/emxProjectManagementUtil.jsp?mode=connectResolves&&hideLaunchButton=true&hideToolbar=false";
    String strURL = "../common/emxFullSearch.jsp?table=PMCResolvesRIOTable&selection=multiple&toolbar=&editLink=true&suiteKey=ProgramCentral&submitURL=../programcentral/emxProjectManagementUtil.jsp?mode=connectResolves";
    String strFilter = "&field=TYPES=type_ProjectSpace,type_Task:Current!=policy_ProjectTask.state_Complete,policy_ProjectSpace.state_Complete";
    strURL +=strFilter;
    strURL +="&objectId="+objId;
       %>
       <script language="javascript">
       var strUrl = "<%=strURL%>";
  	   //document.location.href = strUrl;	
  	   showModalDialog(strUrl, "812", "700", "true", "popup");
       </script>
       <%
       
       
} else if("searchAffectedItems".equalsIgnoreCase(strMode)) {
    String objId   = emxGetParameter(request, "objectId");
    objId          = XSSUtil.encodeURLForServer(context, objId);

//  String strURL = "../common/emxIndentedTable.jsp?program=emxRisk:getAffectedItemsList&table=PMCProjectSummaryForProjects&selection=multiple&header=emxProgramCentral.AddAffectedItem&sortColumnName=Name&sortDirection=ascending&Export=false&freezePane=Name&showClipboard=false&showPageURLIcon=false&triggerValidation=false&massPromoteDemote=false&displayView=details&suiteKey=ProgramCentral&StringResourceFileId=emxProgramCentralStringResource&submitURL=../programcentral/emxProjectManagementUtil.jsp?mode=connectAffectedItems&&hideLaunchButton=true&hideToolbar=false";
    String strURL = "../common/emxFullSearch.jsp?table=PMCProjectSummaryForProjects&selection=multiple&toolbar=&editLink=true&suiteKey=ProgramCentral&submitURL=../programcentral/emxProjectManagementUtil.jsp?mode=connectAffectedItems";
    String strFilter = "&field=TYPES=type_ProjectSpace,type_TaskManagement:Current!=policy_ProjectTask.state_Complete,policy_ProjectSpace.state_Complete";
    strURL +=strFilter;
     strURL +="&objectId="+objId;
       %>
       <script language="javascript">
       var strUrl = "<%=strURL%>";
  	   //document.location.href = strUrl;	
  	   showModalDialog(strUrl, "812", "700", "true", "popup");
       </script>
       <%

} else if("disconnectResolves".equalsIgnoreCase(strMode)) {

    String[] selectedRowArray = emxGetParameterValues (request, "emxTableRowId");
    ArrayList<String> resolvesIdList = new ArrayList<String>();

    // Get the RelID from the | delimited
    for (int i=0; i<selectedRowArray.length; i++) {
        String selectedRowString = selectedRowArray[i];
        StringList idList = FrameworkUtil.split(selectedRowArray[i], "|");
        resolvesIdList.add((String)idList.get(0));
    }

    if(resolvesIdList != null && resolvesIdList.size() > 0) {

        for (int i=0;i<resolvesIdList.size();i++) {
            String errorMessage ="";

            try {
                String relID = (String) resolvesIdList.get(i);
                DomainRelationship.disconnect (context,relID);
            } catch (Exception exp) {
                //TODO - Fix the Message
                errorMessage = exp.getMessage();
            %>
            <script language="javascript" type="text/javaScript">
                alert("<%=XSSUtil.encodeForJavaScript(context,errorMessage)%>");
            </script>
            <%
            }
        }
    %>
    <script language="javascript">
        var topFrame = findFrame(getTopWindow(), "PMCResolvesItems");
        if (topFrame != null) {
			topFrame.location.href = topFrame.location.href;                        
		}else{
			topFrame = findFrame(getTopWindow(), "detailsDisplay");
			if (topFrame != null) {
				topFrame.location.href = topFrame.location.href;  
			}
		}	
     </script>
    <%
   }
} else if("disconnectAffectedItems".equalsIgnoreCase(strMode)) {

    String[] selectedRowArray = emxGetParameterValues (request, "emxTableRowId");
    ArrayList<String> resolvesIdList = new ArrayList<String>();

    // Get the RelID from the | delimited
    for (int i=0; i<selectedRowArray.length; i++) {
        String selectedRowString = selectedRowArray[i];
        StringList idList = FrameworkUtil.split(selectedRowArray[i], "|");
        resolvesIdList.add((String)idList.get(0));
    }

    if(resolvesIdList != null && resolvesIdList.size() > 0) {

        for (int i=0;i<resolvesIdList.size();i++) {
            String errorMessage ="";

            try {
                String relID = (String) resolvesIdList.get(i);
                DomainRelationship.disconnect (context,relID);
            } catch (Exception exp) {
                //TODO - Fix the Message
                errorMessage = exp.getMessage();
            %>
            <script language="javascript" type="text/javaScript">
                alert("<%=XSSUtil.encodeForJavaScript(context,errorMessage)%>");
            </script>
            <%
            }
        }
    %>
    <script language="javascript">
        var topFrame = findFrame(getTopWindow(), "PMCAffectedItems");
        if (topFrame != null) {
			topFrame.location.href = topFrame.location.href;                        
		}else{
			topFrame = findFrame(getTopWindow(), "detailsDisplay");
			if (topFrame != null) {
				topFrame.location.href = topFrame.location.href;  
			}
		}	
     </script>
    <%
 }

} else if("disconnectResolvesRIO".equalsIgnoreCase(strMode)) {

        ArrayList<String> resolvesIdList = new ArrayList<String>();

		String[] emxTableRowIds = emxGetParameterValues(request, "emxTableRowId");
		String[] emxRelIds = new String[emxTableRowIds.length];
		for (int i = 0; i < emxTableRowIds.length; i++) {
			String relId = (String) ((Map) ProgramCentralUtil.parseTableRowId(context, emxTableRowIds[i]))
					.get("relId");
			emxRelIds[i] = relId;
		}
		String errorMessage = "";
		try {
			
			if (null != emxRelIds) {
				DomainRelationship.disconnect(context, emxRelIds);
			}
        
    				} catch (Exception exp) {
    					//TODO - Fix the Message
    					errorMessage = exp.getMessage();
    %>
            <script language="javascript" type="text/javaScript">
                alert("<%=XSSUtil.encodeForJavaScript(context,errorMessage)%>");
            </script>
            <%
            }
    %>
    <script language="javascript">
        var topFrame = findFrame(getTopWindow(), "PMCResolvesItems");
        if (topFrame != null) {
			topFrame.location.href = topFrame.location.href;                        
		}else{
			topFrame = findFrame(getTopWindow(), "detailsDisplay");
			if (topFrame != null) {
				topFrame.location.href = topFrame.location.href;  
			}
		}	
     </script>
    <%
} else if ("connectResolves".equalsIgnoreCase(strMode)) {

     String[] selectedRowArray = emxGetParameterValues(request, "emxTableRowId");
     String[] parentId          = emxGetParameterValues(request, "objectId");

	 DomainObject domObj=DomainObject.newInstance (context,parentId[0]);// as only one risk or opportunity is avaible at a given time
     ArrayList<String> selectedIdList = new ArrayList<String>();

     for (int i=0;i<selectedRowArray.length;i++) {
         StringList idList = FrameworkUtil.split(selectedRowArray[i], "|");
         selectedIdList.add(idList.get(0));
     }
     //String errMsg = "";
     DomainObject projectObj = DomainObject.newInstance (context);
     for (int i=0;i<selectedIdList.size();i++) {
         String errorMessage ="";

     try {
             String projectID = (String) selectedIdList.get(i);
             projectObj.setId(projectID);
             DomainRelationship.connect(context,domObj,RiskManagement.RELATIONSHIP_RESOLUTION_PROJECT,projectObj);
         } catch (Exception exp) {
             //TODO - Fix the Message
             errorMessage = exp.getMessage();
 %>
         <script language="javascript" type="text/javaScript">
             alert("<%=XSSUtil.encodeForJavaScript(context,errorMessage)%>");
</script>
<%
      }
     }     
%>
<script language="javascript">
			var topFrame = findFrame(getTopWindow().window.getWindowOpener().getTopWindow().parent, "PMCResolvesItems");
			if (topFrame != null) {
					topFrame.location.href = topFrame.location.href;                        
				}else{
					topFrame = findFrame(getTopWindow().window.getWindowOpener().getTopWindow().parent, "detailsDisplay");
					if (topFrame != null) {
						topFrame.location.href = topFrame.location.href;  
					}
				}				 
			parent.window.closeWindow();
			
</script>
<%

} else if ("connectAffectedItems".equalsIgnoreCase(strMode)) {

     String[] selectedRowArray = emxGetParameterValues(request, "emxTableRowId");
     String[] parentId          = emxGetParameterValues(request, "objectId");

	 DomainObject domObj=DomainObject.newInstance (context,parentId[0]);// as only one risk or opportunity is avaible at a given time
     ArrayList<String> selectedIdList = new ArrayList<String>();

     for (int i=0;i<selectedRowArray.length;i++) {
         StringList idList = FrameworkUtil.split(selectedRowArray[i], "|");
         selectedIdList.add(idList.get(0));
     }
     //String errMsg = "";
     DomainObject projectObj = DomainObject.newInstance (context);
     for (int i=0;i<selectedIdList.size();i++) {
         String errorMessage ="";

     try {
             String projectID = (String) selectedIdList.get(i);
             projectObj.setId(projectID);
             DomainRelationship.connect(context,domObj,RiskManagement.RELATIONSHIP_RISK_AFFECTED_ITEMS,projectObj);
         } catch (Exception exp) {
             //TODO - Fix the Message
             errorMessage = exp.getMessage();
 %>
         <script language="javascript" type="text/javaScript">
             alert("<%=XSSUtil.encodeForJavaScript(context,errorMessage)%>");
</script>
<%
      }
     }     
%>
<script language="javascript">
			var topFrame = findFrame(getTopWindow().window.getWindowOpener().getTopWindow().parent, "PMCAffectedItems");
			if (topFrame != null) {
					topFrame.location.href = topFrame.location.href;                        
				}else{
					topFrame = findFrame(getTopWindow().window.getWindowOpener().getTopWindow().parent, "detailsDisplay");
					if (topFrame != null) {
						topFrame.location.href = topFrame.location.href;  
					}
				}				 
			parent.window.closeWindow();
			
</script>
<%


	}else if("PMCTaskWBS".equalsIgnoreCase(strMode)){
		Enumeration requestParams = emxGetParameterNames(request);
		StringBuilder url = new StringBuilder();
		if (requestParams != null) {
			while (requestParams.hasMoreElements()) {
				String param = (String) requestParams.nextElement();
				//Added for special character.
				String value = emxGetParameter(request, param);
				url.append("&" + XSSUtil.encodeForURL(context, param) + "="
						+ XSSUtil.encodeForURL(context, value));
			}
		}
		String objectId = emxGetParameter(request,"objectId");
		String portalCmd = emxGetParameter(request, "portalCmdName");
		Map objectInfo = (Map)session.getAttribute("ctxObjInfo");
		session.removeAttribute("ctxObjInfo");

		if(objectInfo== null || objectInfo.isEmpty()){
			StringList selects = new StringList(6);
			selects.add(ProgramCentralConstants.SELECT_HAS_MODIFY_ACCESS);
			selects.add(ProgramCentralConstants.SELECT_PROJECT_POLICY_FROM_TASK);
			selects.add(ProgramCentralConstants.SELECT_IS_MILESTONE);
			selects.add(ProgramCentralConstants.SELECT_PROJECT_TYPE);
			selects.add(ProgramCentralConstants.SELECT_PROJECT_STATE);
			DomainObject domObj = DomainObject.newInstance(context,objectId);
			objectInfo = domObj.getInfo(context, selects);
		}

		String hasModifyAccess 	= (String) objectInfo.get(ProgramCentralConstants.SELECT_HAS_MODIFY_ACCESS);
		String projectPolicy 	= (String) objectInfo.get(ProgramCentralConstants.SELECT_PROJECT_POLICY_FROM_TASK);
		String projectType 		= (String) objectInfo.get(ProgramCentralConstants.SELECT_PROJECT_TYPE);
		String projectState 		= (String) objectInfo.get(ProgramCentralConstants.SELECT_PROJECT_STATE);


		boolean isMilesTone		 		= Boolean.parseBoolean((String) objectInfo.get(ProgramCentralConstants.SELECT_IS_MILESTONE));
		boolean isProjectBaselineTask 	= ProgramCentralConstants.TYPE_PROJECT_BASELINE.equalsIgnoreCase(projectType);
		boolean isProjectTemplate 		= ProgramCentralConstants.TYPE_PROJECT_TEMPLATE.equalsIgnoreCase(projectType);
		boolean isHoldOrCancelProject 	= ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equalsIgnoreCase(projectPolicy);
		
		String contentURL = DomainObject.EMPTY_STRING;

		if (isHoldOrCancelProject || isProjectBaselineTask) {
			contentURL = "../common/emxIndentedTable.jsp?table=PMCWBSViewTable&expandProgramMenu=PMCWBSTaskListMenu&freezePane=Name&selection=multiple&HelpMarker=emxhelpwbstasklist&header=emxProgramCentral.Common.WorkBreakdownStructureSB&sortColumnName=ID&findMxLink=false&massPromoteDemote=false&rowGrouping=false&objectCompare=false&showClipboard=false&showPageURLIcon=false&triggerValidation=false&displayView=details&multiColumnSort=false&showRMB=false"
					+ url.toString();
		} else {
			//by cyl 20250812 start 
//			contentURL = "../common/emxIndentedTable.jsp?table=PMCWBSViewTable"
			contentURL = "../common/emxIndentedTable.jsp?tableMenu=PMCWBSTableMenu" //by cyl 20250812 end
					+ "&expandProgramMenu=PMCWBSTaskListMenu" + "&freezePane=Name" + "&selection=multiple"
					+ "&HelpMarker=emxhelpwbstasklist"
					+ "&header=emxProgramCentral.Common.WorkBreakdownStructureSB" + "&sortColumnName=ID"
					+ "&findMxLink=false&cacheEditAccessProgram=true&parallelLoading=true";

			if (!isMilesTone) {
				contentURL += "&toolbar=PMCWBSToolBar";
				contentURL += "&editRelationship=relationship_Subtask";
				contentURL += "&resequenceRelationship=relationship_Subtask";
				contentURL += "&connectionProgram=emxTask:cutPasteTasksInWBS";
			}
			contentURL += url.toString();

			boolean isAnDInstalled = FrameworkUtil.isSuiteRegistered(context,
					"appVersionAerospaceProgramManagementAccelerator", false, null, null);
			if (isAnDInstalled) {
				boolean isLocked = Task.isParentProjectLocked(context, objectId);
				if (isLocked)
					contentURL = contentURL
							+ "&hideRootSelection=true&editRootNode=false&preProcessJPO=emxTask:preProcessCheckForLock";
			}
		}

		if ("true".equalsIgnoreCase(hasModifyAccess) && !isProjectBaselineTask && !ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL.equalsIgnoreCase(projectState)) {
			contentURL = contentURL + "&editLink=true";
			//contentURL += "&postProcessJPO=emxTask:updateScheduleChanges";
			//20260803 update by ljr WBS保存后统一校验ESO子任务估计日期范围，校验通过后继续执行OOTB排程处理；
			contentURL += "&postProcessJPO=JF_ESO:postProcessValidateESOTaskEstimatedDateRange";
		}
%>
<script language="javascript">
	      	var strUrl = "<%=contentURL%>";
	  		strUrl = strUrl + "&maxCellsToDraw=2000&scrollPageSize=50";
	        var portalName = "<%=portalCmd%>";
       		var displaytopFrame = findFrame(getTopWindow(), "detailsDisplay");
       		var topFrame;
       		if(displaytopFrame){
   			 	topFrame = findFrame(displaytopFrame, portalName);
       		}else{
       			topFrame = findFrame(getTopWindow(), portalName);
       		}
	        if(topFrame == null){
		     	topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
		     	if(null == topFrame){
		     		topFrame = findFrame(getTopWindow(), "detailsDisplay");
		     	}
	  		}
	        topFrame.location.href = strUrl;
        </script>
<%
	}else if("PMCWBS".equalsIgnoreCase(strMode)){
		//Modified for performance improvement of WBS
		String portalCmd = (String) emxGetParameter(request, "portalCmdName");
		String objectId = XSSUtil.encodeURLForServer(context,emxGetParameter(request, "objectId"));
		Map paramMap = new HashMap(1);
		paramMap.put("objectId", objectId);

		String[] methodArgs = JPO.packArgs(paramMap);
		boolean hasModify = (boolean) JPO.invoke(context, "emxTask", null, "hasModifyAccess", methodArgs,
				Boolean.class);
		boolean hasEdit = (boolean) JPO.invoke(context, "JF_ProjectTaskUtils", null, "editProjectTaskAccess", methodArgs,
				Boolean.class);
		boolean hasModifyAccess = hasModify || hasEdit;

		DomainObject dmoObject = DomainObject.newInstance(context, objectId);
		String projectPolicy = dmoObject.getInfo(context, ProgramCentralConstants.SELECT_POLICY);
		String subMode 		= emxGetParameter(request, "subMode");
        
		StringBuilder urlBuilder = new StringBuilder();
		

		if ("PMCWBSDataGridView".equalsIgnoreCase(subMode)) {
			
			String lazyLoad 		= emxGetParameter(request, "lazyLoad");
			String fastColumns 		= emxGetParameter(request, "fastColumns");
		
			urlBuilder.append("../webapps/ENXSBGridConnector/ENXSBGridConnectorClient.html?tableMenu=PMCWBSTableMenuENXSBGrid");
			if(ProgramCentralUtil.isNotNullString(lazyLoad)){
				urlBuilder.append("&lazyLoad="+XSSUtil.encodeForURL(context, lazyLoad));
			}
			
			if(ProgramCentralUtil.isNotNullString(fastColumns)){
				urlBuilder.append("&fastColumns="+XSSUtil.encodeForURL(context, fastColumns));
			} 
			
			//urlBuilder.append("&showTouchStyle=true");
        } else {
                	urlBuilder.append("../common/emxIndentedTable.jsp?tableMenu=PMCWBSTableMenu");
                }
		urlBuilder.append("&expandProgramMenu=PMCWBSListMenu");
		urlBuilder.append("&freezePane=Name");
		urlBuilder.append("&selection=multiple");
		urlBuilder.append("&header=emxProgramCentral.Common.WorkBreakdownStructureSB");		// IR-1132301
		if(!"PMCWBSDataGridView".equalsIgnoreCase(subMode)){
			urlBuilder.append("&HelpMarker=emxhelpwbstasklist");
		}
		else{
			urlBuilder.append("&HelpMarker=emxhelpdatagridtable");
		}
		urlBuilder.append("&sortColumnName=ID");
		urlBuilder.append("&findMxLink=false");
		urlBuilder.append("&editRelationship=relationship_Subtask");
		urlBuilder.append("&suiteKey=ProgramCentral");
		urlBuilder.append("&SuiteDirectory=programcentral");
		urlBuilder.append("&resequenceRelationship=relationship_Subtask");
		urlBuilder.append("&connectionProgram=emxTask:cutPasteTasksInWBS");
		//urlBuilder.append("&postProcessJPO=emxTask:updateScheduleChanges");
		//20260803 update by ljr WBS保存后统一校验ESO子任务估计日期范围，校验通过后继续执行OOTB排程处理；
		urlBuilder.append("&postProcessJPO=JF_ESO:postProcessValidateESOTaskEstimatedDateRange");
		urlBuilder.append("&hideLaunchButton=true");
		urlBuilder.append("&parallelLoading=true");
		urlBuilder.append("&objectId=" + objectId);
		urlBuilder.append("&showPageURLIcon=false");
		urlBuilder.append("&cacheEditAccessProgram=true");

		if (!ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equalsIgnoreCase(projectPolicy)) {
			urlBuilder.append("&toolbar=PMCWBSToolBar");
		}

		if (hasModifyAccess && !ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equalsIgnoreCase(projectPolicy)) {
			urlBuilder.append("&editLink=true");
			urlBuilder.append("&massPromoteDemote=true");
		}else {
			urlBuilder.append("&massPromoteDemote=false");
			urlBuilder.append("&rowGrouping=false");
			urlBuilder.append("&objectCompare=false");
			urlBuilder.append("&showClipboard=false");
			urlBuilder.append("&showPageURLIcon=false");
			urlBuilder.append("&triggerValidation=false");
			urlBuilder.append("&displayView=details");
			urlBuilder.append("&multiColumnSort=false");
			if(ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equalsIgnoreCase(projectPolicy)){
			urlBuilder.append("&showRMB=false");
		}
		}

		Enumeration requestParams = emxGetParameterNames(request);
	  	if(requestParams != null){
   		  while(requestParams.hasMoreElements()){
       		  String param = (String)requestParams.nextElement();
       		  String value = emxGetParameter(request,param);
       		  urlBuilder.append("&"+param);
       		  urlBuilder.append("="+XSSUtil.encodeForURL(context,value));
   		  }
	  	}

	  	boolean isAnDInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionAerospaceProgramManagementAccelerator",false,null,null);
		if(isAnDInstalled){
			boolean isLocked  = Task.isParentProjectLocked(context, objectId);
			if(isLocked){
				urlBuilder.append("&hideRootSelection=true");
				urlBuilder.append("&editRootNode=false");
				urlBuilder.append("&preProcessJPO=emxTask:preProcessCheckForLock");
			}
		}

		//Added by di7
		//Plz do not remove below lines of code. We are cacheing subTypelist of passed type to improve performance of WBS
		String[] typeArray = new String[]{DomainObject.TYPE_PROJECT_SPACE,
				DomainObject.TYPE_PROJECT_CONCEPT,
				DomainObject.TYPE_PROJECT_TEMPLATE,
				DomainObject.TYPE_TASK_MANAGEMENT,
				ProgramCentralConstants.TYPE_MILESTONE,
				ProgramCentralConstants.TYPE_TASK,
				ProgramCentralConstants.TYPE_PHASE,
				ProgramCentralConstants.TYPE_GATE};

		long start = System.currentTimeMillis();
		ProgramCentralUtil.getDerivativeType(context, typeArray);
		DebugUtil.debug("Time taken by getDerivativeType(ms)::"+(System.currentTimeMillis()-start));
		%>
        <script language="javascript">
	      	var strUrl = "<%=urlBuilder.toString()%>";
	  		strUrl = strUrl + "&maxCellsToDraw=2000&scrollPageSize=50";
	        var portalName = "<%=portalCmd%>";
        		var displaytopFrame = findFrame(getTopWindow(), "detailsDisplay");
        		var topFrame;
        		if(displaytopFrame){
    			 	topFrame = findFrame(displaytopFrame, portalName);
        		}else{
        			topFrame = findFrame(getTopWindow(), portalName);
        		}
	        if(topFrame == null){
		     	topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
		     	if(null == topFrame){
		     		topFrame = findFrame(getTopWindow(), "detailsDisplay");
		     	}
	  		}
	        topFrame.location.href = strUrl;
        </script>
       <%
	}else if("PMCProjectTemplateWBS".equalsIgnoreCase(strMode) || "type_ProjectTemplate".equalsIgnoreCase(strMode)){
		String objectId = emxGetParameter(request, "objectId");
 		Map paramMap = new HashMap(1);
   	    paramMap.put("objectId",objectId);

       String[] methodArgs = JPO.packArgs(paramMap);
        boolean hasModifyAccess  = (boolean) JPO.invoke(context,"emxProjectTemplate", null, "hasAccessToTemplateWBSActionMenu", methodArgs, Boolean.class);

        String subMode 	= emxGetParameter(request, "subMode");
                
		StringBuilder urlBuilder = new StringBuilder();
		//HondaGrid-FUN087459
		// replace common/emxIndentedTable.jsp with webapps/ENXSBGridConnectorClient/ENXSBGridConnectorClient.html 
		if("PMCProjectTemplateWBSDataGridView".equalsIgnoreCase(subMode)) {
			
			String lazyLoad 		= emxGetParameter(request, "lazyLoad");
			String fastColumns 		= emxGetParameter(request, "fastColumns");
			
                    urlBuilder.append("../webapps/ENXSBGridConnector/ENXSBGridConnectorClient.html?expandProgram=emxTask:getWBSProjectTemplateSubtasks");
                    if(hasModifyAccess){
			urlBuilder.append("&table=PMCWBSProjectTemplateViewTableENXSBGrid");
                    }else{
                    	//To show Readonly table for Template in "Released"/"Obsolete" state.
                    	urlBuilder.append("&table=PMCWBSProjectTemplateViewTableENXSBGridView");
                    }
			
			//urlBuilder.append("&showTouchStyle=true");
			
			 if(ProgramCentralUtil.isNotNullString(lazyLoad)){
					urlBuilder.append("&lazyLoad="+XSSUtil.encodeForURL(context, lazyLoad));
				}
				
			if(ProgramCentralUtil.isNotNullString(fastColumns)){
				urlBuilder.append("&fastColumns="+XSSUtil.encodeForURL(context, fastColumns));
			}
			
        } else {
             urlBuilder.append("../common/emxIndentedTable.jsp?expandProgram=emxTask:getWBSProjectTemplateSubtasks");
        	 urlBuilder.append("&table=PMCWBSProjectTemplateViewTable");
                }
		urlBuilder.append("&toolbar=PMCWBSProjectTemplateToolBar");
		urlBuilder.append("&freezePane=Name&selection=multiple");
		urlBuilder.append("&HelpMarker=emxhelpprojecttemplatewbs");
		urlBuilder.append("&header=emxProgramCentral.Common.WorkBreakdownStructureSB");
		urlBuilder.append("&sortColumnName=ID");
		urlBuilder.append("&findMxLink=false");
		urlBuilder.append("&postProcessJPO=emxTask:updateScheduleChanges");
		urlBuilder.append("&editRelationship=relationship_Subtask");
		urlBuilder.append("&resequenceRelationship=relationship_Subtask");
		urlBuilder.append("&suiteKey=ProgramCentral");
		urlBuilder.append("&SuiteDirectory=programcentral");
		urlBuilder.append("&connectionProgram=emxTask:cutPasteTasksInWBS");
		urlBuilder.append("&hideLaunchButton=true");
		urlBuilder.append("&displayView=details");
		urlBuilder.append("&cellwrap=false");
		urlBuilder.append("&showClipboard=false");
		urlBuilder.append("&parallelLoading=true");

        Enumeration requestParams = emxGetParameterNames(request);

		if(requestParams != null){
			while(requestParams.hasMoreElements()){
				String param = (String)requestParams.nextElement();
				String value = emxGetParameter(request,param);
				urlBuilder.append("&"+param);
				urlBuilder.append("="+XSSUtil.encodeForURL(context, value));
			}
		}

 		if(hasModifyAccess){
			urlBuilder.append("&editLink=true");
 		}

 		%>
        <script language="javascript">
	   		 var strUrl = "<%=urlBuilder.toString()%>";
	   		 document.location.href = strUrl;
        </script>
        <%
	}else if("Rollup".equalsIgnoreCase(strMode)){
    	String portalCmd 	= emxGetParameter(request,"portalCmdName");
		String objectId 	= emxGetParameter(request, "objectId");
		String subMode = emxGetParameter(request, "subMode");
    	String strUrl = "../programcentral/emxProjectManagementUtil.jsp?mode=postRollup&portalCmdName="+portalCmd+"&objectId="+objectId+"&subMode="+subMode;

 		%>
 			<script language="javascript">
 				var portalName = "<%=portalCmd%>";
 				var topFrame = findFrame(getTopWindow(), portalName);

 				if(topFrame == null){
 					topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
 					if(null == topFrame){
     				    topFrame = findFrame(getTopWindow(), "detailsDisplay");
 					}

 					if(null == topFrame){
 						topFrame = findFrame(getTopWindow(), "PMCWBS");
 					}
 				}
				setTimeout(function() {
 					topFrame.toggleProgress('visible');
 				    document.location.href = "<%=strUrl%>";
			    },25);

 			</script>
 			<%
    }else if("postRollup".equalsIgnoreCase(strMode)){
        String portalCmd 	= emxGetParameter(request,"portalCmdName");
		String objectId 	= emxGetParameter(request, "objectId");
		String subMode = emxGetParameter(request, "subMode");
		long start 			= System.currentTimeMillis();
		
		Map programMap = new HashMap();
        programMap.put( "objectId", objectId );
        programMap.put("subMode", subMode);

        // Pack arguments into string array.
        String[] args = JPO.packArgs(programMap);
	
		Map rollupMap = (Map) JPO.invoke(context, "emxTaskBase", null, "rollUpManualProject", args, Map.class);

		/*Map rollupMap = null;
		try{
			PropertyUtil.setGlobalRPEValue(context,"PERCENTAGE_COMPLETE", "true");
			rollupMap = TaskDateRollup.rolloutProject(
				context,
				new StringList(objectId),
				true,
				false);
		}catch(FrameworkException fe){
			fe.printStackTrace();
		}finally{
			PropertyUtil.setGlobalRPEValue(context,"PERCENTAGE_COMPLETE", "false");
		}*/

		List impactedObjList = (List)rollupMap.get("impactedObjectIds");
		Map updatedTaskMap = (Map)rollupMap.get("common.Task_updatedDates");
		DebugUtil.debug("Manual Rollup time:"+(System.currentTimeMillis()-start));

		String rollupMessage 	= DomainObject.EMPTY_STRING;
		String isWholePageRefreshRequierd=(String)CacheUtil.getCacheObject(context, "isWholePageRefreshRequierd");
		CacheUtil.removeCacheObject(context, "isWholePageRefreshRequierd");
		%>
          <script language="javascript">
        		var portalName = "<%=portalCmd%>";
        		var rollupMsg = "<%=rollupMessage%>";
				var isWholePageRefreshRequierd = "<%=isWholePageRefreshRequierd%>";
				
        		var topFrame = findFrame(getTopWindow(), portalName);
          		if(topFrame == null){
     				topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
     				if(null == topFrame){
     				    topFrame = findFrame(getTopWindow(), "detailsDisplay");
     				}
     			}

    	      setTimeout(function() {
     	        	toggleRollupIcon(topFrame,"iconActionUpdateDatesActive","iconActionUpdateDates");
					if("yes"==isWholePageRefreshRequierd)
     	        		{
     	        		topFrame.location.href=topFrame.location.href;
     	        		}else{
	    	    	  topFrame.emxEditableTable.refreshStructureWithOutSort();
						}
    	      	  topFrame.toggleProgress('hidden');
		 		  },25);

          </script>
        <%

    }else if("validateKindOf".equalsIgnoreCase(strMode)){
		JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
    	String objectType 		= emxGetParameter(request,"objectType");
    	Map<String,StringList> derivativeMap = ProgramCentralUtil.getDerivativeTypeListFromUtilCache(context, objectType);

    	if(derivativeMap.isEmpty()){
    		derivativeMap 	= ProgramCentralUtil.getDerivativeType(context, objectType);
    	}

    	StringList passTypeList = derivativeMap.get(objectType);
    	jsonObjectBuilder.add("isMilestone",passTypeList.contains(ProgramCentralConstants.TYPE_MILESTONE));
    	jsonObjectBuilder.add("isGate",passTypeList.contains(ProgramCentralConstants.TYPE_GATE));

    	out.clear();
   		out.write(jsonObjectBuilder.build().toString());

   		return;
    }else if("getDueTasks".equalsIgnoreCase(strMode)) {
		StringList finalObjectList = new StringList();
		String sMethodName = request.getParameter("method");
		StatusReport report = (StatusReport)session.getAttribute("store");
		Method method = null;
		try{
			method = report.getClass().getMethod(sMethodName);
		}catch(Exception e) {
			e.printStackTrace();
		}
		MapList mlObjects = (MapList)method.invoke(report);
		int mlObjectSize = mlObjects.size();
		String objIdListStr = "";
		DomainObject dobj = new DomainObject();
		for(int i = 0; i<mlObjectSize; i++){
			Map objectMap = (Map)mlObjects.get(i);
			String taskobjId = (String)objectMap.get(ProgramCentralConstants.SELECT_ID);
			if(!(finalObjectList.contains(taskobjId))){
				finalObjectList.add(taskobjId);
			}
		}
		//=======================================================================
		String[] taskIdArr = new String[finalObjectList.size()];
		
		for(int i = 0; i<finalObjectList.size(); i++){
			taskIdArr[i] = finalObjectList.get(i);
		}
				MapList phyObjects  = DomainObject.getInfo(context, taskIdArr, new StringList(ProgramCentralConstants.SELECT_PHYSICALID));
				for(int i = 0; i<phyObjects.size(); i++){
					Map objectMap = (Map)phyObjects.get(i);
					String taskPhyId = (String)objectMap.get(ProgramCentralConstants.SELECT_PHYSICALID);
					objIdListStr +=","+taskPhyId; 
				}
				//=======================================================================
		out.clear();
		out.write(objIdListStr);
		return;
    }else if ("ChangePolicy".equalsIgnoreCase(strMode)) {
		String selectedType = emxGetParameter(request, "SelectedType");
        String selectedPolicy = emxGetParameter(request, "selectedPolicy");
		JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
		String policyName=ProgramCentralConstants.EMPTY_STRING;
		String policyDisplayValue=ProgramCentralConstants.EMPTY_STRING;
		if (ProgramCentralUtil.isNotNullString(selectedType)) {
			Map<String, StringList> derivativeMap = ProgramCentralUtil
					.getDerivativeTypeListFromUtilCache(context, selectedType);

			if (derivativeMap.isEmpty()) {
				derivativeMap = ProgramCentralUtil.getDerivativeType(context, selectedType);
			}

			StringList passTypeList = derivativeMap.get(selectedType);

			MapList policyList = mxType.getPolicies(context, selectedType, true);
			if (policyList != null && policyList.size() > 0) {

			if(ProgramCentralUtil.isNotNullString(selectedPolicy)){
				selectedPolicy =  PropertyUtil.getSchemaProperty(context, selectedPolicy);
			Iterator itr = policyList.iterator();
			while (itr.hasNext())
			{
				Map policyMap = (Map) itr.next();
				policyName=(String)policyMap.get("name");

				if(policyName.equalsIgnoreCase(selectedPolicy)){
					policyName = (String) policyMap.get("name");
					policyDisplayValue = EnoviaResourceBundle.getAdminI18NString(context, "Policy",policyName, context.getSession().getLanguage());
					break;
				}
			}
			}
				else{
					policyList.sort("name", ProgramCentralConstants.ASCENDING_SORT,
				ProgramCentralConstants.SORTTYPE_STRING);
			policyList.sort("name", ProgramCentralConstants.ASCENDING_SORT,
			ProgramCentralConstants.SORTTYPE_STRING);
			Map policyMap = (Map) policyList.get(0);
			policyName = ProgramCentralUtil.getDefaultPolicy(context, selectedType);
				if(ProgramCentralUtil.isNullString(policyName)){
			policyName = (String) policyMap.get("name");
				    }

			policyDisplayValue = EnoviaResourceBundle.getAdminI18NString(context, "Policy",
					policyName, context.getSession().getLanguage());

			}
				if (passTypeList != null && (passTypeList.contains(ProgramCentralConstants.TYPE_MILESTONE)
						|| passTypeList.contains(ProgramCentralConstants.TYPE_GATE))) {
					jsonObjectBuilder.add("Duration", "0");
					jsonObjectBuilder.add("EstimatedStartDate", "");
					jsonObjectBuilder.add("EstimatedEndDate", "");
					jsonObjectBuilder.add("Policy", policyDisplayValue);
					jsonObjectBuilder.add("isMilestone",
							passTypeList.contains(ProgramCentralConstants.TYPE_MILESTONE));
					jsonObjectBuilder.add("isGate", passTypeList.contains(ProgramCentralConstants.TYPE_GATE));
				} else {
					jsonObjectBuilder.add("Duration", "1");
					jsonObjectBuilder.add("Policy", policyDisplayValue);
				}
			}
		}

		out.clear();
		out.write(jsonObjectBuilder.build().toString());
		return;
  }	else if ("EffortEstimatedEndDate".equalsIgnoreCase(strMode)) {
		%>
	 	<script language="javascript" type="text/javaScript">
			var topFrame = findFrame(getTopWindow(), "PMCWBS");
	    	  topFrame.emxEditableTable.refreshStructureWithOutSort();
	    	  getTopWindow().closeSlideInDialog();
	 	</script>
<%
}  else if("addGovernedItems".equals(strMode)) {
		String governingId = emxGetParameter( request, "objectId" );
        	DomainObject governing = DomainObject.newInstance(context, governingId);

        	String sTableRowId[] = emxGetParameterValues( request, "emxTableRowId" );
        	StringList slObjIDToConnect = new StringList();
        	DomainObject toBeGoverned = DomainObject.newInstance(context);
			for(int i=0; i<sTableRowId.length; i++){
				String sTempObj = sTableRowId[i];
				Map mParsedObject = ProgramCentralUtil.parseTableRowId(context,sTempObj);
				String toBeGovernedId = (String)mParsedObject.get("objectId");
				toBeGoverned.setId(toBeGovernedId);
				try {
					DomainRelationship.connect(context, toBeGoverned, ProgramCentralConstants.RELATIONSHIP_GOVERNING_PROJECT, governing);
		        	DomainRelationship.connect(context, toBeGoverned, ProgramCentralConstants.RELATIONSHIP_RELATED_PROJECT, governing);
				} catch (Exception e) {
					e.printStackTrace();
					throw new MatrixException(e);
				}
			}
			%>
			<script language="javascript" type="text/javaScript">
				 getTopWindow().window.getWindowOpener().location.href=getTopWindow().window.getWindowOpener().location.href;
				 getTopWindow().window.closeWindow();
			</script>
			<%
	} else if("removeGovernedItems".equals(strMode)) {
			String sTableRowId[] = emxGetParameterValues(request, "emxTableRowId");
		String[] objArrayIds = new String[sTableRowId.length];
		StringList relIds = new StringList();

		for (int i = 0,len=sTableRowId.length; i <len ; i++) {
			Map<String,String> mParsedObject = ProgramCentralUtil.parseTableRowId(context, sTableRowId[i]);
			objArrayIds[i] = mParsedObject.get("objectId");
			relIds.addElement(mParsedObject.get("relId"));
		}

		String SELECT_RELATED_PROJECT_REL_ID = "from[" + ProgramCentralConstants.RELATIONSHIP_RELATED_PROJECT+ "].id";

		StringList objectSelects = new StringList(1);
		objectSelects.addElement(SELECT_RELATED_PROJECT_REL_ID);

		BusinessObjectWithSelectList bwsl =
				ProgramCentralUtil.getObjectWithSelectList(context, objArrayIds, objectSelects, false);
		for(int i=0,len = objArrayIds.length;i<len;i++){
			BusinessObjectWithSelect bws = bwsl.getElement(i);
			String relatedProjectRelId = (String) bws.getSelectData(SELECT_RELATED_PROJECT_REL_ID);
			if(ProgramCentralUtil.isNotNullString(relatedProjectRelId)){
				relIds.addElement(relatedProjectRelId);
			}
		}

		String[] objRelIds = new String[relIds.size()];
		relIds.toArray(objRelIds);
		try {
			DomainRelationship.disconnect(context, objRelIds);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MatrixException(e);
		}

		%>
		<script language="javascript" type="text/javaScript">
						parent.location.href = parent.location.href
					</script>
		<%
	}else if("PMCtype_ProjectVault".equalsIgnoreCase(strMode)) {

		String strURL = "../common/emxIndentedTable.jsp?tableMenu=PMCFolderTableMenu&toolbar=PMCFolderSummaryToolBar&header=emxProgramCentral.Common.Folders&"+
	 					 "HelpMarker=emxhelpfoldersummary&Export=true&selection=multiple&expandProgram=emxProjectFolder:getTableExpandProjectVaultData"+
						"&freezePane=Name&sortColumnName=Name&preProcessJPO=emxProjectFolder:preProcessCheckForEdit&connectionProgram=emxProjectFolder:cutPasteObjectsInFolderStructureBrowser"
						+"&editRelationship=relationship_SubVaults,relationship_VaultedDocumentsRev2"
						+"&resequenceRelationship=relationship_ProjectVaults,relationship_SubVaults,relationship_VaultedDocumentsRev2&massPromoteDemote=false&postProcessJPO=emxTask:postProcessRefresh";

		String objectId = emxGetParameter(request, "objectId");
		Map paramMap = new HashMap(1);
		paramMap.put("objectId",objectId);
		String[] methodArgs = JPO.packArgs(paramMap);
		boolean editFlag  = (boolean) JPO.invoke(context,"emxProjectFolder", null, "hasAccessForFolderAndDocumentActions", methodArgs, Boolean.class);

		if(editFlag){
			strURL += "&editLink=true";
		}

		Enumeration requestParams = emxGetParameterNames(request);
		StringBuilder url = new StringBuilder();

		if(requestParams != null){
			while (requestParams.hasMoreElements()) {
				String param = (String) requestParams.nextElement();
				String value = emxGetParameter(request, param);
				url.append("&" + param);
				url.append("=" + XSSUtil.encodeForURL(context, value));
			}
			strURL += url.toString();
		}
%>
<script language="javascript">
	var strUrl = "<%=strURL%>";
	document.location.href = strUrl;
</script>
<%
}else if("PMCtype_ControlledFolder".equalsIgnoreCase(strMode)) {

	String strURL = "../common/emxIndentedTable.jsp?tableMenu=PMCFolderTableMenu&toolbar=PMCFolderSummaryToolBar&header=emxProgramCentral.Common.Folders&"+
 					 "HelpMarker=emxhelpfoldersummary&Export=true&selection=multiple&expandProgram=emxProjectFolder:getTableExpandProjectVaultData"+
					"&freezePane=Name&sortColumnName=Name&preProcessJPO=emxProjectFolder:preProcessCheckForEdit&connectionProgram=emxProjectFolder:cutPasteObjectsInFolderStructureBrowser"
					+"&editRelationship=relationship_SubVaults,relationship_VaultedDocumentsRev2"
					+"&resequenceRelationship=relationship_ProjectVaults,relationship_SubVaults,relationship_VaultedDocumentsRev2&massPromoteDemote=false&postProcessJPO=emxTask:postProcessRefresh";

	String objectId = emxGetParameter(request, "objectId");
	Map paramMap = new HashMap(1);
	paramMap.put("objectId",objectId);
	String[] methodArgs = JPO.packArgs(paramMap);
	boolean editFlag  = (boolean) JPO.invoke(context,"emxProjectFolder", null, "hasAccessForFolderAndDocumentActions", methodArgs, Boolean.class);

	if(editFlag){
		strURL += "&editLink=true";
	}

	Enumeration requestParams = emxGetParameterNames(request);
	StringBuilder url = new StringBuilder();

	if(requestParams != null){
		while (requestParams.hasMoreElements()) {
			String param = (String) requestParams.nextElement();
			String value = emxGetParameter(request, param);
			url.append("&" + param);
			url.append("=" + XSSUtil.encodeForURL(context, value));
		}
		strURL += url.toString();
	}
%>
<script language="javascript">
var strUrl = "<%=strURL%>";
document.location.href = strUrl;
</script>
<%
}else if("addExistingRisk".equalsIgnoreCase(strMode)){
		String addExistingRiskURL = "../common/emxIndentedTable.jsp?table=PMCExistingRisksSummary&selection=multiple&Export=false&sortColumnName=Title&sortDirection=ascending&header=emxProgramCentral.Common.AddExisting&hideLaunchButton=true&program=emxRisk:getRiskForAddExisting&submitURL=../programcentral/emxProjectManagementUtil.jsp?mode=addExistingRiskToTask&Export=false&showClipboard=false&showPageURLIcon=false&triggerValidation=false&massPromoteDemote=false&displayView=details&suiteKey=ProgramCentral&StringResourceFileId=emxProgramCentralStringResource&freezePane=Name&hideToolbar=false";
		Enumeration requestParams = emxGetParameterNames(request);
	  	StringBuilder urlParameters = new StringBuilder();
	  	if(requestParams != null){
	  	  String objectId = emxGetParameter(request,"objectId");
		  urlParameters.append("&objectId="+objectId);
		  addExistingRiskURL = addExistingRiskURL + urlParameters.toString();
 	  	}
%>
	<script language="javascript" type="text/javaScript">
	 var strUrl = "<%=addExistingRiskURL%>";
	 //document.location.href = strUrl;
	 showModalDialog(strUrl, "812", "700", "true", "popup");
	</script>
<%
	}else if("addExistingRiskToTask".equalsIgnoreCase(strMode)){
		 String errorMsg = ProgramCentralConstants.EMPTY_STRING;
		 String parentObjId = emxGetParameter(request, "objectId");
		 RiskHolder parentObj = new ProjectSpace(parentObjId);
		 boolean isOfProjectType = ProgramCentralUtil.isOfGivenTypeObject(context,DomainConstants.TYPE_PROJECT_SPACE,parentObjId);
		    String[] ids = emxGetParameterValues(request, "emxTableRowId");
		    if(null==ids)
		    {
		    	errorMsg = EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource",context.getLocale(), "emxProgramCentral.Common.SelectItem");
			}
			else
			{

			   try{
				   for(int i =0, len = ids.length; i<len;i++){
				    	StringList riskIdList = StringUtil.split(ids[i], "|");

				    	if( riskIdList.size() >= 1 ){
				    		String riskId = (String)riskIdList.get(1);
				    		 Risk risk    = new Risk(riskId);
				    		 // start a write transaction
				             ContextUtil.startTransaction(context, true);
				    		 DomainRelationship.connect(context, (DomainObject) parentObj, Risk.RELATIONSHIP_RISK, risk);
				    		 DomainRelationship.connect(context, risk, Risk.RELATIONSHIP_RISK_ITEM, (DomainObject) parentObj);
				    		 // commit work
				             ContextUtil.commitTransaction(context);
				    	}
				    }
			   }catch(Exception e){
				   e.printStackTrace();
	 	}
		}
		%>
		 	<script language="javascript" type="text/javaScript">
			var errorMsg = "<%=XSSUtil.encodeForJavaScript(context,errorMsg)%>";
		 	if(errorMsg != null && errorMsg!=""){
				alert(errorMsg);
				getTopWindow().location.href=getTopWindow().location.href;
			}
			else{
				var topFrame = findFrame(getTopWindow().window.getWindowOpener().getTopWindow().parent, "PMCProjectRisk");
				if (topFrame != null) {
						topFrame.location.href = topFrame.location.href;
					}else{
						topFrame = findFrame(getTopWindow().window.getWindowOpener().getTopWindow().parent, "detailsDisplay");
						if (topFrame != null) {
							topFrame.location.href = topFrame.location.href;
						}
					}
				parent.window.closeWindow();
				}
		 	</script>
			  <%
	} else if("DocumentPushSubscription".equalsIgnoreCase(strMode) || "BookmarkPushSubscription".equalsIgnoreCase(strMode)){
			
			StringBuilder errorMsg = new StringBuilder();
			
			String strURL = "../components/emxPushSubscriptionDialog.jsp?multiColumnSort=false&showClipboard=false&editLink=false&expandLevelFilter=false&objectBased=false&categoryTreeName=null";
			String tableRowIdList[] = emxGetParameterValues(request,"emxTableRowId");
			tableRowIdList = ProgramCentralUtil.parseTableRowId(context, tableRowIdList);
			String objectId = tableRowIdList[0]; // only single selection is allowed from UI
			String language	= context.getSession().getLanguage();
			
			if(ProgramCentralUtil.isNotNullString(objectId)){
				DomainObject dmoObject = DomainObject.newInstance(context, objectId);
				String SELECT_IS_TYPE_DOCUMENT = "type.kindof["+DomainConstants.TYPE_DOCUMENT+"]";
				String SELECT_IS_WORKSPACE_VAULT = "type.kindof["+DomainConstants.TYPE_WORKSPACE_VAULT+"]";
				StringList slSelects = new StringList();
				slSelects.add(SELECT_IS_TYPE_DOCUMENT);
				slSelects.add(SELECT_IS_WORKSPACE_VAULT);
				Map mapInfo = dmoObject.getInfo(context,slSelects);
				String sIsDocumentType = (String) mapInfo.get(SELECT_IS_TYPE_DOCUMENT);
				String sIsWorkspaceVaultType = (String) mapInfo.get(SELECT_IS_WORKSPACE_VAULT);
				
				if("FALSE".equalsIgnoreCase(sIsWorkspaceVaultType.trim()) && "FALSE".equalsIgnoreCase(sIsDocumentType.trim())){
					errorMsg.append(EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.FolderSubscription.SubscriptionMessage", language));
				}else{
					Enumeration requestParams = emxGetParameterNames(request);
					StringBuilder url = new StringBuilder();

					if (requestParams != null) {
						while (requestParams.hasMoreElements()) {
							String param = (String) requestParams.nextElement();
							String value = emxGetParameter(request, param);
							url.append("&" + param);
							url.append("=" + XSSUtil.encodeForURL(context, value));
						}
						strURL += url.toString();
					}
				}
			} 
			
		%>
			<script language="javascript">
				var errorMsg = "<%=XSSUtil.encodeForJavaScript(context,errorMsg.toString())%>";
				if(errorMsg == null || errorMsg ==""){
					 var strUrl =  "<%=XSSUtil.encodeForJavaScript(context,strURL)%>";
					showModalDialog(strUrl, '800', '575')
				}else{
					alert(errorMsg);

				}
			 </script>
		<%
	}else if("FlatView".equalsIgnoreCase(strMode)){

		String portal 		= (String) emxGetParameter(request,"portal");
		String portalCmd 	= (String) emxGetParameter(request, "portalCmdName");
		String objectId = emxGetParameter(request, "objectId");

		if("PMCWhatIfPortal".equalsIgnoreCase(portal)){
			session.setAttribute("isGanttChartloaded","TRUE");
			objectId 	= (String)session.getAttribute("ExperimentObjectId");
			if(objectId == null || objectId.isEmpty()){
				objectId = emxGetParameter(request, "objectId");
			}
			session.setAttribute("isFlattendedTableLoadedKey","TRUE");
		}

		String mqlCmd = "print bus $1 select $2 $3 $4 $5 $6 dump $7";
		String objectInfo = MqlUtil.mqlCommand(context,
				false,
				false,
				mqlCmd,
				false,
				objectId,
				"policy",
				"current.access[modify]",
				"type.kindof[Project Template]",
				"from[Object Route]",
				"current",
				"|");
		StringList objectInfoList = FrameworkUtil.split(objectInfo, "|");
		String projectPolicy 	= (String)objectInfoList.get(0);
		String hasModifyAccess 	= (String)objectInfoList.get(1);
		String isKindOfTemplate = (String)objectInfoList.get(2);
		String hasIssue = (String)objectInfoList.get(3);
		String projectState = (String)objectInfoList.get(4);


		StringBuilder url = new StringBuilder();
		//HondaGrid-FUN087459
		// replace common/emxIndentedTable.jsp with webapps/ENXSBGridConnectorClient/ENXSBGridConnectorClient.html 
		url.append("../common/emxIndentedTable.jsp?table=");

		if("True".equalsIgnoreCase(isKindOfTemplate)){
			url.append("PMCTemplateWBSFlatViewTable");
		}else{
			url.append("PMCWBSFlatViewTable");
		}

		url.append("&program=emxTask:getFlattenedWBS");
		url.append("&freezePane=Name");
		url.append("&selection=multiple");
		url.append("&HelpMarker=emxhelpflatview");
		url.append("&header=emxProgramCentral.Common.WorkBreakdownStructureSB");
		url.append("&sortColumnName=ID");
		url.append("&findMxLink=false");
		url.append("&suiteKey=ProgramCentral");
		url.append("&SuiteDirectory=programcentral");
		url.append("&postProcessJPO=emxTask:updateScheduleChanges");
		url.append("&hideLaunchButton=true");
		url.append("&parallelLoading=true");
		url.append("&pageSize=50");
		url.append("&maxCellsToDraw=2000");
		url.append("&scrollPageSize=50");
		url.append("&objectId=").append(objectId);
		url.append("&showPageURLIcon=false");
		url.append("&expandLevelFilter=false");
		url.append("&showRMB=false");
		url.append("&cacheEditAccessProgram=true");

		if (!ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equalsIgnoreCase(projectPolicy) && "true".equalsIgnoreCase(hasModifyAccess) && "false".equalsIgnoreCase(hasIssue) 
				&& !"Released".equalsIgnoreCase(projectState) && !"Obsolete".equalsIgnoreCase(projectState)) {
			url.append("&toolbar=PMCWBSFlatViewToolbar");
			url.append("&editLink=true");
			url.append("&massPromoteDemote=true");
		} else {
			url.append("&massPromoteDemote=false");
			url.append("&rowGrouping=false");
			url.append("&objectCompare=false");
			url.append("&showClipboard=false");
			url.append("&showPageURLIcon=false");
			url.append("&triggerValidation=false");
			url.append("&displayView=details");
			url.append("&multiColumnSort=false");
		}

		Enumeration requestParams = emxGetParameterNames(request);
		StringBuilder hiddenUrlParameter = new StringBuilder();

		if (requestParams != null) {
			while (requestParams.hasMoreElements()) {
				String param = (String) requestParams.nextElement();
				if(!param.equalsIgnoreCase("objectId")){
					
					if(param.equalsIgnoreCase("parentOID") && "PMCWhatIfPortal".equalsIgnoreCase(portal)){
						hiddenUrlParameter.append("&" + param);
						hiddenUrlParameter.append("=" + objectId);	
					}else{
					String value = emxGetParameter(request, param);
					hiddenUrlParameter.append("&" + param);
					hiddenUrlParameter.append("=" + value);
				}
			}
			}
			url.append(hiddenUrlParameter.toString());
		}

		//Added by di7
		//Plz do not remove below lines of code. We are cacheing subTypelist of passed type to improve performance of WBS
		String[] typeArray = new String[]{DomainObject.TYPE_PROJECT_SPACE, DomainObject.TYPE_PROJECT_CONCEPT,
				DomainObject.TYPE_PROJECT_TEMPLATE, DomainObject.TYPE_TASK_MANAGEMENT,
				ProgramCentralConstants.TYPE_MILESTONE, ProgramCentralConstants.TYPE_TASK,
				ProgramCentralConstants.TYPE_PHASE, ProgramCentralConstants.TYPE_GATE};

		long start = System.currentTimeMillis();
		ProgramCentralUtil.getDerivativeType(context, typeArray);
		DebugUtil.debug("Time taken by getDerivativeType(ms)::" + (System.currentTimeMillis() - start));
%>
<script language="javascript">
	var strUrl = "<%=url.toString()%>";
	var portalName = "<%=portalCmd%>";
	var topFrame = findFrame(getTopWindow(), portalName);
	if (topFrame == null) {
		topFrame = findFrame(getTopWindow(), "detailsDisplay");
	}
	document.location.href = strUrl;
</script>
<%
	} else if ("FlatViewDelete".equalsIgnoreCase(strMode)) {
		String portalCmd 				= (String) emxGetParameter(request, "portalCmdName");
		//String[] tableRowIdList = (String[]) session.getAttribute("emxTableRowId");
		String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");

	    boolean blProjSelected = false;
	    boolean blTaskSelected = false;
		
		StringList sList = new StringList(8);
	    sList.addElement(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
	    sList.addElement(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
	    
	    StringList slSelectedIds = new StringList();
	    
	    if(tableRowIdList != null){
	        for(int i=0; i<tableRowIdList.length ; i++){
	    	StringList idList = FrameworkUtil.split(tableRowIdList[i], "|");
	          String selectedId = idList.get(0);
	          
	          slSelectedIds.add(selectedId);
	        }
	        
	        String[] selectedIds = slSelectedIds.toStringArray();
	        
	        MapList objectInfoMap = DomainObject.getInfo(context, selectedIds, sList);
	    	
	        for(int i=0; i<selectedIds.length ; i++){
	          
	          Map objectInfoList = (Map)objectInfoMap.get(i);
	          
	          String isKindOfProject= (String)objectInfoList.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
	          String isKindOfTask= (String)objectInfoList.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
	          
	          if (isKindOfProject.equalsIgnoreCase("true")){
	        	  blProjSelected = true;
	          }
	          if (isKindOfTask.equalsIgnoreCase("true")){
	        	  blTaskSelected = true;
	          }
	        }
	    }

		String projectId = (String) emxGetParameter(request, "parentOID");
		session.setAttribute("emxTableRowId", emxGetParameterValues(request, "emxTableRowId"));
		session.setAttribute("emxTableRowIdForRefresh", emxGetParameterValues(request, "emxTableRowId"));

		StringBuilder url = new StringBuilder();
		url.append("../programcentral/emxProjectManagementUtil.jsp?mode=FlatViewDeleteCnf");
		url.append("&portalCmdName=").append(portalCmd);
		url.append("&projectId=").append(projectId);

		//String strUrl = "../programcentral/emxProjectManagementUtil.jsp?mode=FlatViewDeleteCnf&portalCmdName="+ portalCmd;
%>
<script language="javascript">

	var displaytopFrame = findFrame(getTopWindow(), "detailsDisplay");
	var topFrame = findFrame(displaytopFrame, "<%=portalCmd%>");
	if(topFrame == null){
		topFrame = findFrame(getTopWindow(), "<%=portalCmd%>");
	}

	var result = "";//confirm("<framework:i18nScript localize="i18nId">emxProgramCentral.Project.ConfirmTaskDelete</framework:i18nScript>");
	if('<%=blTaskSelected%>' == "true" && '<%=blProjSelected%>' == "true"){
		  var message = "<framework:i18nScript localize="i18nId">emxProgramCentral.Project.ConfirmTaskDelete</framework:i18nScript>" + "\n" +
		  				"<framework:i18nScript localize="i18nId">emxProgramCentral.Project.ConfirmProjectRemove</framework:i18nScript>"
		  result = confirm(message); 
	  }else if('<%=blTaskSelected%>' == "false" && '<%=blProjSelected%>' == "true"){
		  result = confirm("<framework:i18nScript localize="i18nId">emxProgramCentral.Project.ConfirmProjectRemove</framework:i18nScript>");
	  }else{
		  result = confirm("<framework:i18nScript localize="i18nId">emxProgramCentral.Project.ConfirmTaskDelete</framework:i18nScript>");
	  }
	if(result){
		setTimeout(function() {
			topFrame.toggleProgress('visible');
			document.location.href = "<%=url.toString()%>";
		}, 10);
	}
</script>
<%
	} else if ("FlatViewDeleteCnf".equalsIgnoreCase(strMode)) {
		long start = System.currentTimeMillis();
		String projectId = (String) emxGetParameter(request, "projectId");
		String portalCmd = (String) emxGetParameter(request, "portalCmdName");
		String[] emxTableRowIds = (String[]) session.getAttribute("emxTableRowId");
		String[] refreshRowIds = (String[]) session.getAttribute("emxTableRowIdForRefresh");

		session.removeAttribute("emxTableRowIdForRefresh");
		session.removeAttribute("emxTableRowId");

		String[] objectIds = ProgramCentralUtil.parseTableRowId(context, emxTableRowIds);

		StringList tobeDeletedTaskIdList = new StringList();
		for(int i=0,len =objectIds.length;i<len;i++ ){
			String objId = objectIds[i];
			tobeDeletedTaskIdList.add(objId);
		}

		Map<String, StringList> returnMap = Task.checkForMandatoryAndEffort(context, tobeDeletedTaskIdList);

		StringList tobeDeletedIdList 	= returnMap.get("FinalIdsList");
		StringList taskStateAndIdList 	= returnMap.get("TaskStateAndIdList");

		StringList finalToBeDeletedIdList 	= new StringList();
		List<String> finalDeletedIdList 	= new ArrayList<>();
		
		for(int i=0,len=taskStateAndIdList.size();i<len;i++ ){
			String taskNameIdState =  (String)taskStateAndIdList.get(i);

			for(int j=0;j<tobeDeletedIdList.size();j++){
				String tobeDeletedId =  (String)tobeDeletedIdList.get(j);

				if(taskNameIdState != null && taskNameIdState.contains(tobeDeletedId)){
					finalToBeDeletedIdList.add(taskNameIdState);
				}
			}
		}

		StringBuilder notToBeDeletedTaskNameList = new StringBuilder();

		for(int j=0;j<finalToBeDeletedIdList.size();j++){
			String taskNameIdState =  (String)finalToBeDeletedIdList.get(j);
			//Name|objId|State
			StringList nameIdStateList = FrameworkUtil.split(taskNameIdState, "|");
			String currentState =  (String)nameIdStateList.get(2);

			if(!("Create".equalsIgnoreCase(currentState)||
					"Assign".equalsIgnoreCase(currentState))){
				if(notToBeDeletedTaskNameList.length()==0){
					notToBeDeletedTaskNameList.append((String)nameIdStateList.get(0));
				}else{
					notToBeDeletedTaskNameList.append(", "+(String)nameIdStateList.get(0));
				}
			}else{
				finalDeletedIdList.add((String)nameIdStateList.get(1));
			}
		}
		String notToBeDeletedTaskName = notToBeDeletedTaskNameList.toString();
		StringList slMandExcludeTaskNames	= returnMap.get("MandExcludeTaskNames");
		StringList slTaskWithEfforts 		= returnMap.get("TasksWithEffort");

		boolean blMandatoryFlag = (slMandExcludeTaskNames.size()>0) ? true: false;
		boolean blTaskWithEffortsFlag = (slTaskWithEfforts.size()>0) ? true: false;
		DebugUtil.debug("Total time pre-process of delete operation::"+(System.currentTimeMillis()-start));

		boolean isSuccessfullyDeleted = true;
		String projectSchedule = ProgramCentralConstants.PROJECT_SCHEDULE_AUTO;
		int tobeDeletedObjSize = finalDeletedIdList.size();
		List<String> deletedObjList 	= new ArrayList<>();
		List<String> deletedObjIdList 	= new ArrayList<>();
		
		if(tobeDeletedObjSize > 0){
			try {
				ProjectSpace project = new ProjectSpace(projectId);
				projectSchedule 	 = project.getSchedule(context);
				boolean doRollup 	 = ProgramCentralConstants.PROJECT_SCHEDULE_AUTO.equalsIgnoreCase(projectSchedule);
				
				Map<String, Object> refreshMap = Task.deleteTasks(context, finalDeletedIdList, doRollup,projectId);
				deletedObjList = (List<String>)refreshMap.get("deletedObjectIdList");
				
			} catch (MatrixException me) {
				isSuccessfullyDeleted = false;
				me.printStackTrace();
				throw me;
			}
		}

%>
<script language="javascript">
		<%
		if(blMandatoryFlag){%>
			alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.CannotDeleteMandatoryTask</framework:i18nScript>" +"<%= slMandExcludeTaskNames.toString()%>");
		<%}
		if(blTaskWithEffortsFlag){%>
			alert("<framework:i18nScript localize="i18nId">emxProgramCentral.WeeklyTimeSheet.DeleteTask.TaskWithEffortCannotBeDeleted</framework:i18nScript>" +"<%= slTaskWithEfforts.toString()%>");
		<%}
		if(!notToBeDeletedTaskName.isEmpty()){%>
			alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.UnableToDeleteTasks</framework:i18nScript>\n\n" +"<%= notToBeDeletedTaskName%>");
		<%}%>

   		var tobeDeletedObjectSize = "<%=tobeDeletedObjSize%>";
   		var successfullyDeleted = "<%=isSuccessfullyDeleted%>";
   		
   		var displaytopFrame = findFrame(getTopWindow(), "detailsDisplay");
   		var topFrame = findFrame(displaytopFrame, "<%=portalCmd%>");
   		if(topFrame == null){
   			topFrame = findFrame(getTopWindow(), "<%=portalCmd%>");	
   		}
   		
   		if(parseInt(tobeDeletedObjectSize) > 0 && successfullyDeleted){
   			
   			var refreshRows=[];
   	   		<%
		      	for(int i=0,size=deletedObjList.size();i<size;i++){
		   	      		%>
		      		refreshRows.push("<%=deletedObjList.get(i)%>");
	   					<%
	   	      		}
	      	%>
	      	var impObjRowIdArr = new Array();
	      	var count = 0;
	      	var reloadPage = false;
	      	for(var i=0,len=refreshRows.length;i<len;i++){
	      		var nRow = emxUICore.selectSingleNode(topFrame.oXML, "/mxRoot/rows//r[@o= '"+ refreshRows[i] +"']");
	      		if(nRow != null){
	      			impObjRowIdArr[count] = "|||"+nRow.getAttribute("id");
	      			count++;
	      		}else{
	      			reloadPage =true;
	      			break;
	   	      	}
   	      	}

   	   		var projectScheduleValue = "<%=projectSchedule%>";

   	   		setTimeout(function() {
   	   			if(reloadPage){
   	   				topFrame.location.reload();
   	   			}else{
	   	   			topFrame.emxEditableTable.removeRowsSelected(impObjRowIdArr);
   				var rowsNode 	= emxUICore.selectSingleNode(topFrame.oXML, "/mxRoot/rows");
   	  			var totalRows 	= rowsNode.getAttribute("totalRows");

   	  			if(parseInt(totalRows) == 0){
   	  				topFrame.location.reload();
   	  			}else{
   	  				if("Manual" == projectScheduleValue){
   	  	   				top.jQuery('.fonticon-refresh').addClass('fonticon-refresh-info'); 
   	  	   				toggleRollupIcon(topFrame,"iconActionUpdateDates","iconActionUpdateDatesActive");
   	  	   			}
   	  				topFrame.toggleProgress('hidden');
   	  			}
   	   			}

   			},10);
   		}else{
   			topFrame.toggleProgress('hidden');
   		}

</script>
<%
	}else if("FlatViewAddAboveTask".equalsIgnoreCase(strMode)){
		String currentframe = XSSUtil.encodeForJavaScript(context,
				(String) emxGetParameter(request, "portalCmdName"));
		String manyTasksToAdd = (String) emxGetParameter(request, "PMCWBSQuickTasksToAddBelow"); //XSSOK
		String taskTypeToAdd = (String) emxGetParameter(request, "PMCWBSQuickTaskTypeToAddBelow");
		//String emxTableRowId = emxGetParameter(request, "emxTableRowId");

		session.setAttribute("emxTableRowId", emxGetParameter(request, "emxTableRowId"));

		String objectId = emxGetParameter(request, "objectId");

		StringBuilder url = new StringBuilder();
		url.append("../programcentral/emxProjectManagementUtil.jsp?mode=FlatViewAddAboveTaskCnf");
		url.append("&portalCmdName=").append(currentframe);
		url.append("&PMCWBSQuickTasksToAddBelow=").append(manyTasksToAdd);
		url.append("&PMCWBSQuickTaskTypeToAddBelow=").append(taskTypeToAdd);
		//url.append("&emxTableRowId=").append(emxTableRowId);
		url.append("&objectId=").append(objectId);
%>
<script language="javascript">

	var displaytopFrame = findFrame(getTopWindow(), "detailsDisplay");
	var topFrame = findFrame(displaytopFrame, "<%=currentframe%>");
	if(topFrame == null){
		topFrame = findFrame(getTopWindow(), "<%=currentframe%>");
	}
	setTimeout(function() {
		topFrame.toggleProgress('visible');
	    document.location.href = "<%=url.toString()%>";
	}, 10);
</script>
<%
	} else if ("FlatViewAddAboveTaskCnf".equalsIgnoreCase(strMode)) {

		String currentframe = XSSUtil.encodeForJavaScript(context,
				(String) emxGetParameter(request, "portalCmdName"));
		String urlTaskType = (String) emxGetParameter(request, "taskType");
		String manyTasksToAdd = (String) emxGetParameter(request, "PMCWBSQuickTasksToAddBelow"); //XSSOK
		String taskTypeToAdd = (String) emxGetParameter(request, "PMCWBSQuickTaskTypeToAddBelow");

		String emxTableRowId = (String) session.getAttribute("emxTableRowId");
		session.removeAttribute("emxTableRowId");

		String objectId 	= null;
		String pid 			= null;
		String rowId 		= null;
		boolean addAbove 	= true;

		if(emxTableRowId != null){
			Map<String, String> taskIdMap = ProgramCentralUtil.parseTableRowId(context, emxTableRowId);
			objectId = taskIdMap.get("objectId");
			pid = taskIdMap.get("parentOId"); //Added for refresh
			rowId = taskIdMap.get("rowId"); //Added for refresh
		}else{
			objectId = emxGetParameter(request, "objectId");
			pid = objectId;
			rowId = "0,0"; //Added for refresh
			addAbove = false;
		}

		Task task = new Task(objectId);
		String parentId = task.getInfo(context, "to[Subtask].from.id");
		if(parentId == null){
			parentId = objectId;
		}

		int howMany = Integer.parseInt(manyTasksToAdd);

		//Task Creation
		String message = ProgramCentralConstants.EMPTY_STRING;
		MapList newTasks = new MapList();
		String projectSchedule = ProgramCentralConstants.PROJECT_SCHEDULE_AUTO;
		try {

			ContextUtil.startTransaction(context, true);
			com.matrixone.apps.program.Task newTask = (com.matrixone.apps.program.Task) DomainObject
					.newInstance(context, ProgramCentralConstants.TYPE_TASK, ProgramCentralConstants.PROGRAM);
			newTasks = newTask.create(context, taskTypeToAdd, howMany, objectId, addAbove);
			ContextUtil.commitTransaction(context);

			Map<String, String> projectScheduleMap = ProgramCentralUtil.getProjectSchedule(context, parentId);
			projectSchedule = projectScheduleMap.get(parentId);

			if (ProgramCentralUtil.isNullString(projectSchedule)
					|| ProgramCentralConstants.PROJECT_SCHEDULE_AUTO.equalsIgnoreCase(projectSchedule)) {
				TaskDateRollup.rolloutProject(context, new StringList(parentId), true);
			}

		} catch (FrameworkException e) {
			message = e.getMessage();
			ContextUtil.abortTransaction(context);
		} catch (Exception e) {
			ContextUtil.abortTransaction(context);
		}

		if (UIUtil.isNullOrEmpty(message)) {
			StringBuffer sBuff = new StringBuffer();
			String xmlMessage = DomainConstants.EMPTY_STRING;
			sBuff.append("<mxRoot>");
			Iterator itrNewTask = newTasks.iterator();
			ArrayList taskIds = new ArrayList();
			while (itrNewTask.hasNext()) {
				Map taskInfo = (Map) itrNewTask.next();
				String toId = (String) taskInfo.get(ProgramCentralConstants.SELECT_ID);
				taskIds.add(toId);
				String fromId = (String) taskInfo.get("to[Subtask].from.id");
				String relId = (String) taskInfo.get("to[Subtask].id");
				sBuff.append("<action><![CDATA[add]]></action>");

				sBuff.append("<data status=\"committed\" pasteBelowOrAbove=\""+addAbove+"\" >");
				sBuff.append("<item oid=\"" + toId + "\" relId=\"" + relId + "\" pid=\"" + pid + "\" id=\"" + rowId
						+ "\" pasteAboveToRow=\"" + rowId + "\" />");
				sBuff.append("</data>");
			}
			sBuff.append("</mxRoot>");

	%><script language="javascript" type="text/javaScript">
			var displaytopFrame = findFrame(getTopWindow(), "detailsDisplay");
			var topFrame = findFrame(displaytopFrame, "<%=currentframe%>");
			if(topFrame== null){
				topFrame = findFrame(getTopWindow(), "<%=currentframe%>");
			}
			topFrame.emxEditableTable.addToSelectedMultiRoot('<%=XSSUtil.encodeForJavaScript(context, sBuff.toString())%>');
			var projectScheduleValue = "<%=projectSchedule%>";
			var refreshPage = "<%=addAbove%>";

			if("false" == refreshPage){ //When zero object present in flaten schedule view
				topFrame.location.reload();
			}else{
				//Added by DI7
				setTimeout(function() {
					if("Manual" == projectScheduleValue){
						top.jQuery('.fonticon-refresh').addClass('fonticon-refresh-info'); 
						toggleRollupIcon(topFrame,"iconActionUpdateDates","iconActionUpdateDatesActive");
					}
					topFrame.toggleProgress('hidden');
				}, 25);
			}

	    </script>
<%
	} else {
%>      <script language="javascript" type="text/javaScript">
		alert('<%=message%>')
		</script>
<%
	}
}else if("QuickWBS".equalsIgnoreCase(strMode)) {
		String currentframe = XSSUtil.encodeForJavaScript(context,
				(String) emxGetParameter(request, "portalCmdName"));
		String urlTaskType = (String) emxGetParameter(request, "taskType");
		String strTasksToAdd = (String) emxGetParameter(request, "PMCWBSQuickTasksToAddBelow"); //XSSOK
		String strTasksTypeToAdd = (String) emxGetParameter(request, "PMCWBSQuickTaskTypeToAddBelow");

		String calledMethod = emxGetParameter(request, "calledMethod");
		String projectId = emxGetParameter(request, "parentOID");
		String rowIds = emxGetParameter(request, "emxTableRowId");
		StringList slIds = FrameworkUtil.splitString(rowIds, "|");
		String parentId = (String) slIds.get(1);
		String taskLevel = (String) slIds.get(3);
		boolean addTaskAbove = false;
		if ("submitInsertTask".equalsIgnoreCase(calledMethod)) {
			addTaskAbove = true;
		}

		StringList busSelects = new StringList();
		busSelects.add(ProgramCentralConstants.SELECT_IS_MILESTONE);
		busSelects.add(ProgramCentralConstants.SELECT_IS_GATE);

		MapList parentMapList = ProgramCentralUtil.getObjectDetails(context,
				new String[]{parentId},
				busSelects,
				true);
		Map<String, String> parentMap = (Map) parentMapList.get(0);
		String isMilestone = parentMap.get(ProgramCentralConstants.SELECT_IS_MILESTONE);
		String isGate = parentMap.get(ProgramCentralConstants.SELECT_IS_GATE);

		if (("true".equalsIgnoreCase(isGate) || "true".equalsIgnoreCase(isMilestone)) && !addTaskAbove) {
			%>
			<script language="javascript" type="text/javaScript">
				 alert("<framework:i18nScript localize="i18nId">emxProgramcentral.Milestone.SubtaskCreationAlert</framework:i18nScript>");
			</script>
			<%
			return;
		}

		int howMany = Integer.parseInt(strTasksToAdd);

		//Task Creation
		String message = ProgramCentralConstants.EMPTY_STRING;
		MapList newTasks = new MapList();
		String projectSchedule = ProgramCentralConstants.PROJECT_SCHEDULE_AUTO;
		try {
			ContextUtil.startTransaction(context, true);
			com.matrixone.apps.program.Task newTask = (com.matrixone.apps.program.Task) DomainObject
					.newInstance(context, ProgramCentralConstants.TYPE_TASK, ProgramCentralConstants.PROGRAM);
			newTasks = newTask.create(context, strTasksTypeToAdd, howMany, parentId, addTaskAbove);
			ContextUtil.commitTransaction(context);

			Map<String, String> projectScheduleMap = ProgramCentralUtil.getProjectSchedule(context, projectId);
			projectSchedule = projectScheduleMap.get(projectId);

			if (ProgramCentralUtil.isNullString(projectSchedule)
					|| ProgramCentralConstants.PROJECT_SCHEDULE_AUTO.equalsIgnoreCase(projectSchedule)) {
				Task project = new Task(projectId);
				project.rollupAndSave(context);
			}

		} catch (FrameworkException e) {
			message = e.getMessage();
			ContextUtil.abortTransaction(context);
		} catch (Exception e) {
			ContextUtil.abortTransaction(context);
		}

		if (UIUtil.isNullOrEmpty(message)) {
			StringBuffer sBuff = new StringBuffer();
			String xmlMessage = DomainConstants.EMPTY_STRING;
			sBuff.append("<mxRoot>");
			Iterator itrNewTask = newTasks.iterator();
			ArrayList taskIds = new ArrayList();
			while (itrNewTask.hasNext()) {
				Map taskInfo = (Map) itrNewTask.next();
				String toId = (String) taskInfo.get(ProgramCentralConstants.SELECT_ID);
				taskIds.add(toId);
				String fromId = (String) taskInfo.get("to[Subtask].from.id");
				String relId = (String) taskInfo.get("to[Subtask].id");
				sBuff.append("<action><![CDATA[add]]></action>");

				if (addTaskAbove) {
					sBuff.append("<data status=\"noMarkupRows\" pasteBelowOrAbove=\"true\" >");
					sBuff.append("<item oid=\"" + toId + "\" relId=\"" + relId + "\" pid=\"" + fromId
							+ "\" pasteAboveToRow=\"" + slIds.get(3) + "\" direction=\"" + "from" + "\" />");
					sBuff.append("</data>");
				} else {
					sBuff.append("<data status=\"noMarkupRows\">");
					sBuff.append("<item oid=\"" + toId + "\" relId=\"" + relId + "\" pid=\"" + fromId
							+ "\"  direction=\"" + "from" + "\" />");
					sBuff.append("</data>");
				}
			}
			sBuff.append("</mxRoot>");
			%><script language="javascript" type="text/javaScript">
	    	var topFrame = findFrame(getTopWindow(), "<%=currentframe%>");
			if(null == topFrame){
				topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
				if(null == topFrame){
					topFrame = findFrame(getTopWindow(), "detailsDisplay");
				}
			}
			topFrame.emxEditableTable.addToSelected('<%=XSSUtil.encodeForJavaScript(context, sBuff.toString())%>');


			var taskObjectId = [<%int size = taskIds.size();
			for (int i = 0; i < size; i++) {%>"<%=taskIds.get(i)%>"<%=i + 1 < size ? "," : ""%><%}%>];

            var xmlRef 	= topFrame.oXML;
		  	var taskRowID =[];
		  	var taskObjectIdLength = taskObjectId.length;
			if(!topFrame.dataGridEnabled){
		  	for (var j = 0; j < taskObjectIdLength; j++) {
			  	var tempObjectId = taskObjectId[j];
	    	  	var nParent = emxUICore.selectSingleNode(xmlRef, "/mxRoot/rows//r[@o = '" + tempObjectId + "']");
   	  			nParent.setAttribute("display","block");
		  	}
			} else {
					for (var j = 0; j < taskObjectIdLength; j++) {
					var tempObjectId = taskObjectId[j];
					topFrame.emxEditableTable.getAttributeFromNode("rowId",null,tempObjectId);
				}

			}
			  //Added by DI7
			var projectScheduleValue = "<%=projectSchedule%>";
			if("Manual" == projectScheduleValue){
				top.jQuery('.fonticon-refresh').addClass('fonticon-refresh-info'); 
				toggleRollupIcon(topFrame,"iconActionUpdateDates","iconActionUpdateDatesActive");
			}else{
				topFrame.emxEditableTable.refreshStructureWithOutSort();
			}
			topFrame.toggleProgress('hidden');

	    </script>
<%
	} else {
		%><script language="javascript" type="text/javaScript">alert('<%=message%>')</script><%
	}
}else if("createNewRiskOpportunity".equalsIgnoreCase(strMode))
{
	String[] selectedIds = request.getParameterValues("emxTableRowId");
		String sLanguage = request.getHeader("Accept-Language");
		String projectId = emxGetParameter(request, "parentOID");
		String objectId = emxGetParameter(request, "objectId");
		String tableName = emxGetParameter(request, "table");
		String errorMessage = ProgramCentralConstants.EMPTY_STRING;
		String xmlMessage = ProgramCentralConstants.EMPTY_STRING;

		if (!ProgramCentralUtil.isNullString(objectId)) {
			if (ProgramCentralUtil.isNullString(projectId) || objectId != projectId) {
				projectId = objectId;
			}
		}

		boolean isOfRiskType = false;
		boolean isOfOpportunityType = false;
		if (selectedIds != null && selectedIds.length != 0) {
			if (selectedIds.length >= 1) {
				String selectedId = ProgramCentralUtil.parseTableRowId(context, selectedIds)[0];
				isOfRiskType = ProgramCentralUtil.isOfGivenTypeObject(context, DomainConstants.TYPE_RISK,
						selectedId);
				isOfOpportunityType = ProgramCentralUtil.isOfGivenTypeObject(context,
						RiskManagement.TYPE_OPPORTUNITY, selectedId);
			}
		}
		if ((isOfRiskType || isOfOpportunityType)) {

			errorMessage = ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Project.SelectProject", sLanguage);
            %>
             <script language="javascript" type="text/javaScript">
	 	   		alert("<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Risk.SelectNodeForCreateRisk</emxUtil:i18nScript>");
        	</script>
        	<%
            return;
		} else {
			if ("PMCRisksSummary".equalsIgnoreCase(tableName)) {
			%>
			<script language="javascript" type="text/javaScript">
			       getTopWindow().showSlideInDialog("../common/emxCreate.jsp?type=type_Risk&objectId=<%=XSSUtil.encodeForURL(context, objectId)%>&typeChooser=true&suiteKey=ProgramCentral&StringResourceFileId=emxProgramCentralStringResource&SuiteDirectory=programcentral&showApply=true&targetLocation=slidein&policy=policy_ProjectRisk&form=PMCCreateRiskForm&formHeader=emxProgramCentral.Risk.CreateRisk&HelpMarker=emxhelpriskcreatedialog&findMxLink=false&nameField=autoName&postProcessJPO=emxRiskBase:createRisk&submitAction=doNothing&postProcessURL=../programcentral/emxProgramCentralRiskUtil.jsp?mode=refreshStructure",true);
			       </script>
			<%
		} else if ("PMCOpportunitySummary".equalsIgnoreCase(tableName)) {
			%>
			<script language="javascript" type="text/javaScript">
				    getTopWindow().showSlideInDialog("../common/emxCreate.jsp?type=type_Opportunity&parentOID=<%=XSSUtil.encodeForURL(context, projectId)%>&typeChooser=true&suiteKey=ProgramCentral&StringResourceFileId=emxProgramCentralStringResource&SuiteDirectory=programcentral&showApply=true&targetLocation=slidein&policy=policy_ProjectRisk&form=PMCCreateOpportunityForm&formHeader=emxProgramCentral.Risk.CreateOpportunity&HelpMarker=emxhelpriskcreatedialog&findMxLink=false&nameField=autoName&showApply=true&createJPO=emxRisk:createAndConnectRisk&submitAction=doNothing&postProcessJPO=emxRiskBase:createOpportunity&postProcessURL=../programcentral/emxProgramCentralRiskUtil.jsp?mode=refreshStructureForOpportunity",true);
				   </script>
			<%
			}
		}
	}
else if("deleteBudgetBenefitItem".equalsIgnoreCase(strMode))
{
	String errorMsg = ProgramCentralConstants.EMPTY_STRING;
	String[] budgetOrBenefitIds = emxGetParameterValues(request,"emxTableRowId");
	String sObjId = "";
	String[] sTempRowId =new String[budgetOrBenefitIds.length];
	String[] strObjectIDArr    = new String[budgetOrBenefitIds.length];
	StringBuffer sbFinalIds = new StringBuffer();
	StringBuffer sbFinalRowIds = new StringBuffer();
	String fullrefresh="RowRefresh";
	for(int i=0; i<budgetOrBenefitIds.length; i++)
	{
		String sTempObj = budgetOrBenefitIds[i];
		Map mParsedObject = ProgramCentralUtil.parseTableRowId(context,sTempObj);
		sObjId = (String)mParsedObject.get("objectId");
		strObjectIDArr[i] = sObjId;
		sTempRowId[i] = (String)mParsedObject.get("rowId");
	}
	StringList selectables = new StringList();
	selectables.add(DomainConstants.SELECT_TYPE);
	MapList selectedObjectInfoList = DomainObject.getInfo(context, strObjectIDArr, selectables);
	int index=0;
	int length=selectedObjectInfoList.size();
	java.util.Set<String> typeSet=new HashSet<String>();
	for(int i=0; i<length; i++)
	{
		Map typeMap=(Map)selectedObjectInfoList.get(i);
		if(typeMap.containsValue("Budget") ||typeMap.containsValue("Benefit"))
		{	fullrefresh="page";
			index=i;
		}
		typeSet.add(typeMap.toString());
		if(typeSet.size()>1)
		{
			if(typeMap.containsValue("Budget")||typeMap.containsValue("Cost Item"))
			{
				errorMsg =EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource",context.getLocale(), "emxProgramCentral.Command.Budget.SelectItem");
			}
			else if(typeMap.containsValue("Benefit Item") || typeMap.containsValue("Benefit"))
			{
				errorMsg =EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource",context.getLocale(), "emxProgramCentral.Command.Benefit.SelectItem");
			}
			sbFinalIds.append((String) strObjectIDArr[index]);
			sbFinalRowIds.append((String) sTempRowId[index]);
			break;
		}
	}
	if(typeSet.size()==1)
	{
		errorMsg =EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource",context.getLocale(), "emxProgramCentral.Common.ConfirmDelete");
	  	for (int i = 0; i < strObjectIDArr.length; i++) {
		sbFinalIds.append((String) strObjectIDArr[i]);
		sbFinalRowIds.append((String) sTempRowId[i]);
		if (i != strObjectIDArr.length - 1) {
			sbFinalIds.append("|");
			sbFinalRowIds.append("|");
			}
		}
	}
	session.setAttribute("FinancialIdsToDelete",sbFinalIds.toString());
	session.setAttribute("RowIds",sbFinalRowIds.toString());
	String strURL = "../programcentral/emxProjectManagementUtil.jsp?&mode=deleteFinancialItemProcess&fullrefresh="+fullrefresh;

	%>
	 <script type="text/javascript" language="JavaScript">
	var result=confirm('<%=errorMsg%>');
	if (result)
	  {
		var strUrl = "<%=strURL%>";
		document.location.href = strUrl;
	  }
else{<%--XSSOK--%>
	 window.parent.location.href =  window.parent.location.href;
}
</script>
	 <%

}else if("deleteFinancialItemProcess".equalsIgnoreCase(strMode))
{
	String fullrefresh 	= emxGetParameter(request,"fullrefresh");

	String objectIds = (String)session.getAttribute("FinancialIdsToDelete");
	String rowIds = (String)session.getAttribute("RowIds");

	session.removeAttribute("FinancialIdsToDelete");
 	session.removeAttribute("RowIds");

	StringList rowIdList = FrameworkUtil.split(rowIds, "|");
	StringList idList = FrameworkUtil.split(objectIds, "|");
	String[] taskIds = (String []) idList.toArray(new String[] {});
	String[] rowListIds = (String []) rowIdList.toArray(new String[] {});

 	DomainObject.deleteObjects(context,taskIds);

 	String partialXML="";
 	for (int i = 0; i < rowListIds.length; i++) {
 		String sTempRowId = rowListIds[i];
 		partialXML += "<item id=\"" + sTempRowId + "\" />";
	}
 	String xmlMessage = "<mxRoot>";
	String message = "";
	xmlMessage += "<action refresh=\"true\" fromRMB=\"\"><![CDATA[remove]]></action>";
	xmlMessage += partialXML;
	xmlMessage += "<message><![CDATA[" + message + "]]></message>";
	xmlMessage += "</mxRoot>";
 	%>
	 <script type="text/javascript" language="JavaScript">
	 var fullrefresh = "<%=fullrefresh%>";
	 if(fullrefresh=="RowRefresh")
		 {
		 window.parent.removedeletedRows('<%= xmlMessage %>');
		 }
	 else
		 {
		 window.parent.location.href =  window.parent.location.href;
		 }
</script>
	 <%
}else if("BookmarkFolderRefresh".equalsIgnoreCase(strMode)){
	
	String strParentOID      	= emxGetParameter(request, "parentOID");
	String strParentType		= DomainObject.EMPTY_STRING;
	String strTypeofContract 	= DomainObject.EMPTY_STRING;
	String emxTableRowId		= (String) emxGetParameter(request,"emxTableRowId");
	String pasteBelowToRow 		= DomainObject.EMPTY_STRING;
	String openerFrame			= (String) emxGetParameter(request,"openerFrame");
	 
	if(null != emxTableRowId){
       StringList slPasteBelowToRow = FrameworkUtil.split(emxTableRowId, "|");
       if(null != slPasteBelowToRow){
           pasteBelowToRow = slPasteBelowToRow.get(slPasteBelowToRow.size()-1).toString();
           if(slPasteBelowToRow.size() == 4){
        	   strParentOID = slPasteBelowToRow.get(1).toString();
           }
           else if(slPasteBelowToRow.size() == 3){
        	   strParentOID = slPasteBelowToRow.get(0).toString();
           }
       }
    }
	
  	if(null!=strParentOID && ""!=strParentOID)
	{
	   	DomainObject dobj=DomainObject.newInstance(context, strParentOID);
	   	strParentType=dobj.getInfo(context, DomainConstants.SELECT_TYPE);
	   	strTypeofContract = MqlUtil.mqlCommand(context, "print type $1 select $2 dump", true, strParentType, "kindof[Contract]");
	}
	
    //Reitrive newCreatedFolderIdList to refresh fancyTree for the contract and program the page 
  	StringList newCreatedFolderIdList = (StringList) CacheUtil.getCacheObject(context, "newCreatedFolderIdList");
	CacheUtil.removeCacheObject(context, "newCreatedFolderIdList");
	
  	//Reitrive PMCBookmarkFolderRefreshXML to refresh the page 
	String xmlMessage = (String) CacheUtil.getCacheObject(context, "PMCBookmarkFolderRefreshXML");
	CacheUtil.removeCacheObject(context, "PMCBookmarkFolderRefreshXML");
	
	%>
	<script language="javascript">
	
		var topFrame = findFrame(getTopWindow(), "PMCFolder");
		if(topFrame==null || topFrame =='undefined'){
			topFrame = findFrame(getTopWindow(), "detailsDisplay");
		}
		if('<%=strParentType%>'=="Submission" || ('<%=openerFrame%>' != null && '<%=openerFrame%>' == "LRASubmissionLocalFolder"))
    	{
    		topFrame = findFrame(getTopWindow(), "LRASubmissionLocalFolder"); 
    		if(topFrame==null)
    			topFrame = findFrame(getTopWindow(), "LRASubmissionLocalFolder"); 
    		var leaderFrame=findFrame(getTopWindow(), "LRASubmissionLeaderFolder");
    		if(null!=leaderFrame)
    		{
	    		leaderFrame.emxEditableTable.addToSelected('<%=xmlMessage%>');
    			leaderFrame.refreshStructureWithOutSort();
    		}
    	}
    	else if('<%=strParentType%>'=="Submission Master Record" || ('<%=openerFrame%>' != null && '<%=openerFrame%>' == "LRASubmissionMasterFolder")){
    		topFrame = findFrame(getTopWindow(), "LRASubmissionMasterFolder");
    		
    	}else if('<%=strParentType%>'=="Contract Template" || '<%=strTypeofContract%>' == "TRUE"){
            var fancyTree = getTopWindow().objStructureFancyTree;
            if(fancyTree){
            	<%
            	for (int i=0;i<newCreatedFolderIdList.size();i++){
            	%>
            	 	fancyTree.addChild("<%=XSSUtil.encodeForJavaScript(context, strParentOID)%>", "<%=XSSUtil.encodeForJavaScript(context,newCreatedFolderIdList.get(i))%>");
				<%}%>
            } 
		}
		if(topFrame==null || topFrame =='undefined'){
			getTopWindow().location.href = getTopWindow().location.href;
		}
		else{
		topFrame.emxEditableTable.addToSelected('<%=xmlMessage%>');
		topFrame.refreshStructureWithOutSort();
		}
		
	</script>
	<%
}
else
	if ("refreshCopyPartialSchedule".equalsIgnoreCase(strMode)) {
    		String portalCommandName = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "portalCmdName"));
			String UseStartAndEndDatesAsEntered = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "UseStartAndEndDatesAsEntered"));
			String copyDeliverables = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "deliverables"));
			String copyAssignees = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "assignees"));
			String connectPassiveTasks = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "connectPassiveTasks"));
    		String objectId = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"objectId"));
    		String sourceTasks = emxGetParameter(request,"SeachProjectOID");
    		Map<String,String> projectScheduleMap = ProgramCentralUtil.getProjectSchedule(context, objectId);
			String projectSchedule = projectScheduleMap.get(objectId);
			String xmlMessage =DomainObject.EMPTY_STRING;
    		Map paramMap = new HashMap(1);
    		paramMap.put("objectId",objectId);
	    	paramMap.put("SeachProjectOID",sourceTasks);
			paramMap.put("UseStartAndEndDatesAsEntered",UseStartAndEndDatesAsEntered);
			paramMap.put("copyDeliverables",copyDeliverables);
			paramMap.put("copyAssignees",copyAssignees);
			paramMap.put("connectPassiveTasks",connectPassiveTasks);
			String[] methodArgs = JPO.packArgs(paramMap);
          	JPO.invoke(context,"emxProjectSpace", null, "copyPartialScheduleProcess", methodArgs, String[].class);
          	if("Manual".equalsIgnoreCase(projectSchedule)){
       		 String isWholePageRefreshRequierd="yes";
       		CacheUtil.setCacheObject(context, "isWholePageRefreshRequierd", isWholePageRefreshRequierd);
       	}
          	
			%><script language="javascript" type="text/javaScript">
			
    		 var frame = "<%=portalCommandName%>";
       	  var schedule = "<%=projectSchedule%>";
       	  var topFrame = findFrame(getTopWindow(), frame);
       	  
       	   if(null == topFrame){
       	  	topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
       	      if(null == topFrame)
       	  		topFrame = findFrame(getTopWindow(), "PMCWBS");
       	   if(null == topFrame)
      	  		topFrame = findFrame(getTopWindow(), "detailsDisplay");
       	  }
		  
       	  if(topFrame == null || topFrame.location.href=="about:blank"){
	     		topFrame = findFrame(getTopWindow(), "PMCWBS");
	     		if(topFrame == null || topFrame.location.href=="about:blank"){
	     			topFrame = findFrame(getTopWindow(), "PMCWBSDataGridView");
	     			if(topFrame == null || topFrame.location.href=="about:blank"){
	     				topFrame = findFrame(getTopWindow(), "PMCProjectTemplateWBS");	
	     				if(topFrame == null || topFrame.location.href=="about:blank"){
	     					topFrame = findFrame(getTopWindow(), "PMCProjectTemplateWBSDataGridView");
	     					if(topFrame == null || topFrame.location.href=="about:blank"){
						   		topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
					   			if(topFrame == null || topFrame.location.href=="about:blank"){
						   			topFrame = findFrame(getTopWindow(), "detailsDisplay");
         	        			}
					   		}
	     				}
	     				
     	        	}
	     		}
		     }
			 
       	if("Manual" == schedule){
       		//below if else block is added beicase in Manual Project in Structured View when task is added from another project the table doesnt show the tasks
    		  if(!topFrame.dataGridEnabled){
  				  //topFrame.refreshSBTable(topFrame.configuredTableName);
  					getTopWindow().refreshTablePage();
  			  }else{
  					topFrame.location.href=topFrame.location.href;
  			  }
       		
//      		top.jQuery('.fonticon-refresh').addClass('fonticon-refresh-info'); 
//       		toggleRollupIcon(topFrame,"iconActionUpdateDates","iconActionUpdateDatesActive");
    	  }else{
    		  if(!topFrame.dataGridEnabled){
				  // topFrame.refreshSBTable(topFrame.configuredTableName);
				  getTopWindow().refreshTablePage();
			  }else{
					topFrame.location.href=topFrame.location.href;
			  }
    	  }
    	</script>
    <%
} if ("JF_refreshCopyPartialSchedule".equalsIgnoreCase(strMode)) {
		// add by ljr 20260512  客制化 项目复制
		//是否卷积 live / none
		String strRollup = emxGetParameter(request, "rollup");
		String portalCommandName = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "portalCmdName"));
		String UseStartAndEndDatesAsEntered = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "UseStartAndEndDatesAsEntered"));
		String copyDeliverables = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "deliverables"));
		String copyAssignees = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "assignees"));
		String connectPassiveTasks = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "connectPassiveTasks"));
		String objectId = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"objectId"));
		String sourceTasks = emxGetParameter(request,"SeachProjectOID");
		Map<String,String> projectScheduleMap = ProgramCentralUtil.getProjectSchedule(context, objectId);
		String projectSchedule = projectScheduleMap.get(objectId);
		String xmlMessage =DomainObject.EMPTY_STRING;
		Map paramMap = new HashMap(1);
		paramMap.put("objectId",objectId);
		paramMap.put("SeachProjectOID",sourceTasks);
		if ("live".equalsIgnoreCase(strRollup)) {
			paramMap.put("UseStartAndEndDatesAsEntered", UseStartAndEndDatesAsEntered);
			paramMap.put("copyDeliverables", copyDeliverables);
			paramMap.put("copyAssignees", copyAssignees);
			paramMap.put("connectPassiveTasks", connectPassiveTasks);
		}
		String[] methodArgs = JPO.packArgs(paramMap);
//		JPO.invoke(context,"emxProjectSpace", null, "copyPartialScheduleProcess", methodArgs, String[].class);

		if ("live".equalsIgnoreCase(strRollup)) {
			//卷积
			JPO.invoke(context,"emxProjectSpace", null, "copyPartialScheduleProcess", methodArgs, String[].class);
		} else if ("none".equalsIgnoreCase(strRollup)){
			//不卷积
			JPO.invoke(context, "emxProjectSpace", null, "copyProjectPhaseTaskProcess", methodArgs, String[].class);
		}
		if("Manual".equalsIgnoreCase(projectSchedule)){
			String isWholePageRefreshRequierd="yes";
			CacheUtil.setCacheObject(context, "isWholePageRefreshRequierd", isWholePageRefreshRequierd);
		}

		%><script language="javascript" type="text/javaScript">

		var frame = "<%=portalCommandName%>";
		var schedule = "<%=projectSchedule%>";
		var topFrame = findFrame(getTopWindow(), frame);

		if(null == topFrame){
			topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
			if(null == topFrame)
				topFrame = findFrame(getTopWindow(), "PMCWBS");
			if(null == topFrame)
				topFrame = findFrame(getTopWindow(), "detailsDisplay");
		}

		if(topFrame == null || topFrame.location.href=="about:blank"){
			topFrame = findFrame(getTopWindow(), "PMCWBS");
			if(topFrame == null || topFrame.location.href=="about:blank"){
				topFrame = findFrame(getTopWindow(), "PMCWBSDataGridView");
				if(topFrame == null || topFrame.location.href=="about:blank"){
					topFrame = findFrame(getTopWindow(), "PMCProjectTemplateWBS");
					if(topFrame == null || topFrame.location.href=="about:blank"){
						topFrame = findFrame(getTopWindow(), "PMCProjectTemplateWBSDataGridView");
						if(topFrame == null || topFrame.location.href=="about:blank"){
							topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
							if(topFrame == null || topFrame.location.href=="about:blank"){
								topFrame = findFrame(getTopWindow(), "detailsDisplay");
							}
						}
					}

				}
			}
		}

		if("Manual" == schedule){
			//below if else block is added beicase in Manual Project in Structured View when task is added from another project the table doesnt show the tasks
			if(!topFrame.dataGridEnabled){
				//topFrame.refreshSBTable(topFrame.configuredTableName);
				getTopWindow().refreshTablePage();
			}else{
				topFrame.location.href=topFrame.location.href;
			}

	//      		top.jQuery('.fonticon-refresh').addClass('fonticon-refresh-info');
	//       		toggleRollupIcon(topFrame,"iconActionUpdateDates","iconActionUpdateDatesActive");
		}else{
			if(!topFrame.dataGridEnabled){
				// topFrame.refreshSBTable(topFrame.configuredTableName);
				getTopWindow().refreshTablePage();
			}else{
				topFrame.location.href=topFrame.location.href;
			}
		}
	</script>
	<%
	}else if("calculateFloatAndCriticalPath".equalsIgnoreCase(strMode)){
    String portalCmd 	= emxGetParameter(request,"portalCmdName");
	String objectId 	= emxGetParameter(request, "objectId");
    Map rollupMap = null;
    String projectSchedule = ProgramCentralConstants.EMPTY_STRING;
	try{
		Map<String,String> projectScheduleInfo = ProgramCentralUtil.getProjectSchedule(context, objectId);
		projectSchedule = projectScheduleInfo.get(objectId);
		
		rollupMap = TaskDateRollup.rolloutProject(
			context,
			new StringList(objectId),
			true,
			true);
	}catch(FrameworkException frameworkException){
		frameworkException.printStackTrace();
	}

	%>
      <script language="javascript">
    		var portalName = "<%=portalCmd%>";
    		var projectScheduleValue = "<%=projectSchedule%>";
    	
    		var topFrame = findFrame(getTopWindow(), portalName);
      		if(topFrame == null){
 				topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
 				if(null == topFrame){
 				    topFrame = findFrame(getTopWindow(), "detailsDisplay");
 				}
 			}
      		
	    	   setTimeout(function() {
	    	    	  if("Manual" == projectScheduleValue){
	     	        	toggleRollupIcon(topFrame,"iconActionUpdateDatesActive","iconActionUpdateDates");
	    	    	  }
      		 topFrame.emxEditableTable.refreshStructureWithOutSort();
			 		  },25);
	    
      </script>
    <%
}else if("calculateForecast".equalsIgnoreCase(strMode)){
	    String portalCmd 	= emxGetParameter(request,"portalCmdName");
		String objectId 	= emxGetParameter(request, "objectId");
	    Map rollupMap = null;
	    String projectSchedule = ProgramCentralConstants.EMPTY_STRING;
		try{
			
			Map<String,String> projectScheduleInfo = ProgramCentralUtil.getProjectSchedule(context, objectId);
			projectSchedule = projectScheduleInfo.get(objectId);
			
			rollupMap = TaskDateRollup.rolloutProject(
				context,
				new StringList(objectId),
				true,
				false,
				true);
	    	
		}catch(FrameworkException frameworkException){
			frameworkException.printStackTrace();
		}

		%>
	      <script language="javascript">
	    		var portalName = "<%=portalCmd%>";
	    		var projectScheduleValue = "<%=projectSchedule%>";
	    	
	    		var topFrame = findFrame(getTopWindow(), portalName);
	      		if(topFrame == null){
	 				topFrame = findFrame(getTopWindow(), "detailsDisplay");
	 			}
	      		 
 	    	   setTimeout(function() {
	    	    	  if("Manual" == projectScheduleValue){
	     	        	toggleRollupIcon(topFrame,"iconActionUpdateDatesActive","iconActionUpdateDates");
	    	    	  }
	      		 topFrame.emxEditableTable.refreshStructureWithOutSort();
			 		  },25);
		    
	      </script>
	    <%
    	}else if("addMemberToBudget".equalsIgnoreCase(strMode)){
    String objectId = emxGetParameter(request, "objectId");
    String access = emxGetParameter(request, "access");   
    String[] ids = emxGetParameterValues(request, "emxTableRowId");
    String owner = "";
    String ownerAccess = "";
    String defaultAccess = "";
    String OID = "";	
	
	   try{
		   for(int i =0, len = ids.length; i<len;i++){
		    	StringList idList = StringUtil.split(ids[i], "|");

		    		
		    		if( idList.size() >= 1 )
			    	{
			      		String busId = (idList.size() >2) ? (String)idList.get(1) : objectId;
			      		if(!busId.equals(OID))
			      		{
			      			OID = busId;
			      			owner =  new DomainObject(busId).getInfo(context, DomainObject.SELECT_OWNER);
			      			StringList accessNames = DomainAccess.getLogicalNames(context, busId);
			      			//System.out.println("ACCESSNAME ===== "+ accessNames);
			      			defaultAccess = (String)accessNames.get(0);
			      			ownerAccess = (String)accessNames.get(accessNames.size()-1);      			
			      		}

						String personId = (String)idList.get(0);
						String personName = new DomainObject(personId).getInfo(context, DomainObject.SELECT_NAME);
						if(personName.equals(owner))
						{
							DomainAccess.createObjectOwnership(context, busId, personId, ownerAccess, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
						} else {
							DomainAccess.createObjectOwnership(context, busId, personId, defaultAccess, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
						}
					
			    	}
		    }
	   }catch(Exception e){
		   e.printStackTrace();
	   }

	   	%>
		<script language="javascript">
		getTopWindow().parent.getWindowOpener().parent.location.href = getTopWindow().parent.getWindowOpener().parent.location.href;
			
        </script>
   		<%
}
else if ("connectRIOItems".equalsIgnoreCase(strMode))  {
	String portalCmd = (String) emxGetParameter(request, "portalCmdName");
	Set<String> ROIds = new HashSet<String>();
	Set<String> IssueIds =new HashSet<String>();
	final String TYPE_ISSUE = PropertyUtil.getSchemaProperty(context, "type_Issue");
//  final String  RELATIONSHIP_RESOLUTION_PROJECT   = "Resolution Project";  //from
	 final String  RELATIONSHIP_RESOLUTION_PROJECT   = "Contributes To";      //from
	 final String RELATIONSHIP_RESOLVED_TO        = PropertyUtil.getSchemaProperty("relationship_ResolvedTo");
	 String projectId = emxGetParameter(request, "parentOID");
	 
	 StringList issueNames = new StringList();
	 Map<String, String> existingIssueMap = new HashMap<String, String>();
	 Map<String, String> existingRiskOppMap = new HashMap<String, String>();
	 
	 if(ProgramCentralUtil.isNullString(projectId)){
		   projectId = emxGetParameter(request, "objectId");   
	   }
	
	 DomainObject projectObject =DomainObject.newInstance(context,projectId);
	 
	 String[] emxTableRowIds = emxGetParameterValues(request, "emxTableRowId");
	 String[] emxobejctIds = new String[emxTableRowIds.length];
	 try{
						
			 StringList busSelectable = new StringList();
			 busSelectable.add(DomainConstants.SELECT_ID);
			 busSelectable.add(ProgramCentralConstants.SELECT_IS_OPPORTUNITY); 
			 busSelectable.add(ProgramCentralConstants.SELECT_IS_RISK); 
			  
			 for(int i=0;i <emxTableRowIds.length;i++){
				 String objectId = (String)((Map)ProgramCentralUtil.parseTableRowId(context, emxTableRowIds[i])).get("objectId");
				 emxobejctIds[i] = objectId;
			 }
			 
			 BusinessObjectWithSelectList bwsl =	ProgramCentralUtil.getObjectWithSelectList(context,emxobejctIds, busSelectable, true);
			 
			 for(BusinessObjectWithSelect bws : bwsl)
			 {
				String id =bws.getSelectData(DomainConstants.SELECT_ID);
				String isOfRiskType 		= bws.getSelectData(ProgramCentralConstants.SELECT_IS_RISK);
				String isOfOpportunityType = bws.getSelectData(ProgramCentralConstants.SELECT_IS_OPPORTUNITY);
				if("TRUE".equalsIgnoreCase(isOfRiskType) || "TRUE".equalsIgnoreCase(isOfOpportunityType))
				{
					ROIds.add(id);
				}
				else{
					IssueIds.add(id); 
				}
			}
			StringList objSelects = new StringList(2);
			objSelects.add(DomainConstants.SELECT_ID);
			objSelects.add(DomainConstants.SELECT_NAME);
		
			StringList relSelects = new StringList(2);
			relSelects.add(DomainRelationship.SELECT_ID);
			relSelects.add(DomainRelationship.SELECT_NAME);
		
			MapList mplIssues = projectObject.getRelatedObjects(context,
		        										   RELATIONSHIP_RESOLVED_TO+","+RELATIONSHIP_RESOLUTION_PROJECT,
		        										   "*",
		        										   objSelects,relSelects,
		        										   true,false,
		        										   (short)1
		        										   ,null,null);
			
			for(int k=0;k<mplIssues.size();k++) {
				Map map = (Map)mplIssues.get(k);
				String relationshipName = (String)map.get(DomainConstants.SELECT_RELATIONSHIP_NAME);
				String id = (String)map.get(DomainConstants.SELECT_ID);
				String name = (String)map.get(DomainConstants.SELECT_NAME);
				if(RELATIONSHIP_RESOLVED_TO.equalsIgnoreCase(relationshipName)){
					existingIssueMap.put(id, name);
				}
				else{
					existingRiskOppMap.put(id, name);
				}
			}
			
			emxTableRowIds = com.matrixone.apps.common.util.ComponentsUIUtil.getSplitTableRowIds(emxTableRowIds);
			StringBuffer existingRIOs = new StringBuffer(200);
			for (int i = 0; i < emxTableRowIds.length; i++) {
				String strTableRowId = emxTableRowIds[i];	        
				if(existingIssueMap.containsKey(strTableRowId)) {
					existingRIOs.append(existingIssueMap.get(strTableRowId)).append(" ");
					IssueIds.remove(strTableRowId);
				}
				else if(existingRiskOppMap.containsKey(strTableRowId)){
					existingRIOs.append(existingRiskOppMap.get(strTableRowId)).append(" ");
					ROIds.remove(strTableRowId);
				}
			}
			
			Object[] riskOppIdsArray =ROIds.toArray(); //ROIds.stream().toArray(String[]::new);
			Object[] issueIdsArray = IssueIds.toArray();
			String[] RiskOpArrayString = Arrays.copyOf(riskOppIdsArray, riskOppIdsArray.length, String[].class);
			String[] IssueArrayString = Arrays.copyOf(issueIdsArray, issueIdsArray.length, String[].class);

			DomainRelationship.connect(context,projectObject, RELATIONSHIP_RESOLUTION_PROJECT,false, RiskOpArrayString);
			DomainRelationship.connect(context,projectObject, RELATIONSHIP_RESOLVED_TO,false, IssueArrayString);
			%>
				<script language="javascript">
				var existingRIOs = "<%=existingRIOs%>";
					if(existingRIOs!=null && existingRIOs!=""){
						alert("<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Workspace.AddExistingRIOAlert</emxUtil:i18nScript>" + " " +existingRIOs+" ");
					}
				</script>
			<%
			}
	 catch(Exception e)
	 {
		 e.printStackTrace();
	 }
	 %>
		<script language="javascript">
			var topFrame = findFrame(getTopWindow().window.getWindowOpener().getTopWindow().parent, "PMCResolvesRIOCommand");
			if (topFrame != null) {
					topFrame.location.href = topFrame.location.href;                        
				}else{
					topFrame = findFrame(getTopWindow().window.getWindowOpener().getTopWindow().parent, "detailsDisplay");
					if (topFrame != null) {
						topFrame.location.href = topFrame.location.href;  
					}
				}				 
			parent.window.closeWindow();
        </script>
   		<%
    	}
else if ("rmbTaskproperties".equalsIgnoreCase(strMode)) {
		boolean isFromRMB=true;
		String objectId= null;
		String selectedNodeId = emxGetParameter(request, "emxTableRowId");
		if(null==selectedNodeId){
			isFromRMB=false;
			 objectId = emxGetParameter(request, "objectId");
		}
		else{
			 objectId= (String) (ProgramCentralUtil.parseTableRowId(context, selectedNodeId)).get("objectId");
		}
		
		objectId = XSSUtil.encodeURLForServer(context, objectId);
		
		String strURL = "../common/emxForm.jsp?mode=view&form=PMCProjectTaskViewForm&formHeader=emxProgramCentral.Common.FormBasics&PrinterFriendly=true&HelpMarker=emxhelpwbsview&findMxLink=false&suiteKey=ProgramCentral&SuiteDirectory=programcentral&toolbar=PMCProjectTaskToolbar&Export=false&objectId="+ objectId;
%>
<script language="javascript" type="text/javaScript">
var isFromRMB = "<%=isFromRMB%>";
if(isFromRMB=="true"){
	if(parent.emxEditableTable.checkDataModified()){
		alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
	}else{
			var url = "<%=strURL%>";
			getTopWindow().showSlideInDialog(url,true);
	}	
}
else
	{
	var strUrl = "<%=strURL%>";
		document.location.href = strUrl;
	}
                     
        </script>
<%
	}
else if ("rmbPMCLifecycle".equalsIgnoreCase(strMode)) {
	boolean isFromRMB=true;
	String objectId= null;
	String selectedNodeId = emxGetParameter(request, "emxTableRowId");
	if(null==selectedNodeId){
		isFromRMB=false;
		 objectId = emxGetParameter(request, "objectId");
	}
	else{
		 objectId= (String) (ProgramCentralUtil.parseTableRowId(context, selectedNodeId)).get("objectId");
	}
	objectId = XSSUtil.encodeURLForServer(context, objectId);
	String strURL = "../common/emxLifecycle.jsp?export=false&mode=advanced&showPageURLIcon=false&objectId="+ objectId;
%>
<script language="javascript" type="text/javaScript">
var isFromRMB = "<%=isFromRMB%>";
if(isFromRMB=="true"){
	if(parent.emxEditableTable.checkDataModified()){
		alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
	}else{
			var url = "<%=strURL%>";
			showModalDialog(url,1000,1000);
	}	
}
else
	{
	var strUrl = "<%=strURL%>";
		document.location.href = strUrl;
	}
    </script>
<%
}else if("connectLatestRev".equalsIgnoreCase(strMode)){
	   	String docObjectId		= emxGetParameter(request, "objectId");
	   	String parentObjectId	= emxGetParameter(request, "parentOID");  //Id of the parent
	   	String relationType    	= emxGetParameter(request, "relType");  //relation type
    	String relationshipId   = emxGetParameter(request, "relId");  //relationship ID
		String portalCmd 		= emxGetParameter(request, "portalCmdName");
		 String physicalProductVersionId = emxGetParameter(request,"physicalProductVersionId"); 
		
		String strURL = "../programcentral/emxProjectManagementUtil.jsp?&mode=connectLatestRevProcess&objectId="+docObjectId+"&parentOID="+parentObjectId+
				"&relType="+relationType+"&portalCmdName="+portalCmd+"&relId="+relationshipId +"&physicalProductVersionId="+physicalProductVersionId ;
		
		%>
		<script type="text/javascript" language="JavaScript">
		
			var confirmMsg = confirm("<framework:i18nScript localize="i18nId">emxProgramCentral.Alert.HigherRevisionConfirmMsg</framework:i18nScript>");
			if (confirmMsg){
				var strUrl = "<%=strURL%>";
				document.location.href = strUrl;
			}
		</script>   	 
		<%
}else if("connectLatestRevProcess".equalsIgnoreCase(strMode)){
	
		String docObjectId		= emxGetParameter(request, "objectId");
    	String parentObjectId   = emxGetParameter(request, "parentOID");  //Id of the parent
    	String relationType    	= emxGetParameter(request, "relType");  //relation type
    	String relationshipId   = emxGetParameter(request, "relId");  //relationship ID
		String portalCmd 		= emxGetParameter(request,"portalCmdName");
		DomainObject dmoLatestRevision = null;
        String sLatestRevId = DomainObject.EMPTY_STRING;
		 String physicalProductVersionId = emxGetParameter(request,"physicalProductVersionId"); 
		
		DomainObject domObject		= DomainObject.newInstance(context,docObjectId);
    	String isMinorrevisionable 	= (String)domObject.getInfo(context, "current.minorrevisionable");

    	if(ProgramCentralUtil.isNotNullString(physicalProductVersionId)) {
            try{
        	String typePatternPrj = "VPMReference";
            StringList typeSelects = new StringList();
            typeSelects.add(ProgramCentralConstants.SELECT_ID);
            String whereClause = "attribute[PLMReference.V_VersionID] == '"+physicalProductVersionId+"' && attribute[PLMReference.V_isLastVersion] == TRUE";
            MapList listOfPhysicalProduct = DomainObject.findObjects(context,
                    typePatternPrj,
                    ProgramCentralConstants.QUERY_WILDCARD,
                    ProgramCentralConstants.QUERY_WILDCARD,
                    ProgramCentralConstants.QUERY_WILDCARD,
                    ProgramCentralConstants.QUERY_WILDCARD,
                    whereClause,
                    false,
                    typeSelects);
            int listOfProjectsSize = listOfPhysicalProduct.size();
            if(listOfProjectsSize > 0 ) {
            	Map extProjectMap = (Map) listOfPhysicalProduct.get(0);
            	sLatestRevId = (String)extProjectMap.get(ProgramCentralConstants.SELECT_ID);
                dmoLatestRevision  = DomainObject.newInstance(context, sLatestRevId);
            }
            }
            catch(Exception e){
            	e.printStackTrace();
          }
    	}
          else if("true".equalsIgnoreCase(isMinorrevisionable)){
        	//Minor Revisionable
        	BusinessObject boLatestRevision = domObject.getLastRevision(context);
    	    dmoLatestRevision 	= new DomainObject(boLatestRevision);
    	    sLatestRevId 		= dmoLatestRevision.getId(context);
        }else{
        	//Major Revisionable
        	String strLastId 	= domObject.getInfo(context,"majorid.lastmajorid.bestsofar.id");
        	dmoLatestRevision 	= DomainObject.newInstance(context, strLastId);
        	sLatestRevId 		= dmoLatestRevision.getId(context);
        }

		try {
			DomainRelationship.disconnect(context,relationshipId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MatrixException(e);
		}

		DomainObject domParentObj 	= DomainObject.newInstance(context,parentObjectId);
		RelationshipType rel 		= new RelationshipType(relationType);
		DomainRelationship rela 	= DomainRelationship.connect(context, domParentObj, rel, dmoLatestRevision);
        
    	%>
        <script type="text/javascript" language="JavaScript">
        
	    	var topFrame = findFrame(getTopWindow(), "<%=portalCmd%>");
	        if(topFrame == null){
 				topFrame = findFrame(getTopWindow(), "detailsDisplay");
 			}
	        if(topFrame != null){
            	topFrame.location.href = topFrame.location.href; 
            }
	        
        </script>
  		<%
} else if("PMCSchedule".equalsIgnoreCase(strMode)){//Added for 'Favorite Tab' feaure.
	
		Enumeration requestParams = emxGetParameterNames(request);
		StringBuilder url = new StringBuilder();
		if (requestParams != null) {
			while (requestParams.hasMoreElements()) {
				String param = (String) requestParams.nextElement();
				//Added for special character.
				String value = emxGetParameter(request, param);
				url.append("&" + XSSUtil.encodeForURL(context, param) + "="
						+ XSSUtil.encodeForURL(context, value));
			}
		}
		boolean flattenedView 	= false;
		boolean ganttChartView 	= false;
		boolean dataGridView 	= false;
		
		String objectId = emxGetParameter(request, "objectId");
		if(ProgramCentralUtil.isNotNullString(objectId)){
			ProjectSpace project = new ProjectSpace(objectId);
			Dataobject preference = project.getUserPreference(context);
			if(preference != null){
				String tabName = (String)preference.getDataelements().get("defTab");
				if("PMCWBSFlatView".equalsIgnoreCase(tabName)){flattenedView=true;}
				else if("PMCGantt".equalsIgnoreCase(tabName)){ganttChartView=true;}
				else if("PMCWBSDataGridView".equalsIgnoreCase(tabName)){dataGridView=true;}
			}
		}
				
		StringBuilder contentURL = new StringBuilder();
		contentURL.append("../common/emxPortal.jsp?");
		if(flattenedView){
			contentURL.append("portal=PMCFlattenedViewSchedule");
		}else if(ganttChartView){
			contentURL.append("portal=PMCGanttChartViewSchedule");
		}else if(dataGridView){
			contentURL.append("portal=PMCDataGridViewSchedule");
		}
		else{
			contentURL.append("portal=PMCSchedule");
			contentURL.append("&showPageHeader=false");
			//contentURL.append("&HelpMarker=emxhelpprojectadvancedreport");
		}
		contentURL.append(url.toString());
	
%>
	<script language="javascript">
	this.location.href = "<%=contentURL.toString()%>";
	</script>
<%
	}else if("PassiveProject".equalsIgnoreCase(strMode)){
		String subMode 		= emxGetParameter(request, "subMode");
		String portalCmd 	= (String) emxGetParameter(request, "portalCmdName");
		String objectId 	= XSSUtil.encodeURLForServer(context,emxGetParameter(request, "objectId"));
		
		if("SearchURL".equalsIgnoreCase(subMode)){
			String selectObjId = emxGetParameter(request, "emxTableRowId");
			StringList selectedObjIdList = StringUtil.split(selectObjId, "|");
			selectObjId = selectedObjIdList != null && selectedObjIdList.size()==4? selectedObjIdList.get(1):selectedObjIdList.get(0);
			String rowId = selectedObjIdList != null && selectedObjIdList.size()==4? selectedObjIdList.get(3):selectedObjIdList.get(2);
			
			String mqlCommand = "print bus $1 select $2 $3 $4 $5 $6 $7 $8 dump";
	        String hasSubTask = MqlUtil.mqlCommand(context, true, true, mqlCommand, true, selectObjId,"from[Subtask]", "from[Passive Subtask]",ProgramCentralConstants.SELECT_KINDOF_MILESTONE,ProgramCentralConstants.SELECT_KINDOF_GATE,ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE,ProgramCentralConstants.SELECT_KINDOF_PROJECT_CONCEPT,"current");
			StringList connectedList = StringUtil.split(hasSubTask, ",");
	        boolean isInvalidOp = false;
	        boolean isMilestoneSelected = "true".equalsIgnoreCase(connectedList.get(2));
	        boolean isGateSelected = "true".equalsIgnoreCase(connectedList.get(3));
	        //"true".equalsIgnoreCase(connectedList.get(1))||
	        //"true".equalsIgnoreCase(connectedList.get(2))||"true".equalsIgnoreCase(connectedList.get(3))
	        if("true".equalsIgnoreCase(connectedList.get(0))	              
	              ||"true".equalsIgnoreCase(connectedList.get(4)) ||"true".equalsIgnoreCase(connectedList.get(5))
	              ||ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE.equalsIgnoreCase(connectedList.get(6))
	              ||ProgramCentralConstants.STATE_PROJECT_TASK_REVIEW.equalsIgnoreCase(connectedList.get(6))){
	              isInvalidOp = true;
	        }

			
			if(isInvalidOp){
				String language = context.getSession().getLanguage();
				String errMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.PassiveProjectSpace.AddPassiveProjectAlert", language);
			%>
			 <script language="javascript">
			 alert("<%= errMsg %>");
				//return;
		  	 </script>
			<%
			}else{
				
				String mqlCommand1 = "print bus $1 select $2 dump";
		        String result = MqlUtil.mqlCommand(context, true, true, mqlCommand1, true, objectId,ProgramCentralConstants.SELECT_PROJECT_ID);
				StringList resultList = StringUtil.split(result, ",");
				String projectId = objectId;
				if(resultList!=null && resultList.size() > 0 && ProgramCentralUtil.isNotNullString(resultList.get(0))){
					projectId = resultList.get(0);
				}
				
				String[] typeArray = new String[]{
						ProgramCentralConstants.TYPE_EXPERIMENT,
		                ProgramCentralConstants.TYPE_PROJECT_BASELINE,
		                DomainObject.TYPE_PROJECT_TEMPLATE};

		        Map<String,StringList> derivativeMap = ProgramCentralUtil.getDerivativeTypeListFromUtilCache(context, typeArray);

		        StringList finalExcludeParentTypeList = new StringList();
		        finalExcludeParentTypeList.addAll(derivativeMap.get(ProgramCentralConstants.TYPE_EXPERIMENT));
		        finalExcludeParentTypeList.addAll(derivativeMap.get(ProgramCentralConstants.TYPE_PROJECT_BASELINE));
		        finalExcludeParentTypeList.addAll(derivativeMap.get(DomainObject.TYPE_PROJECT_TEMPLATE));
		        StringBuffer excludeRootTypeStrbuff = new StringBuffer();
		        for (int i=0, listSize = finalExcludeParentTypeList.size(); i< listSize; i++){
		        	excludeRootTypeStrbuff.append(finalExcludeParentTypeList.get(i));
		        	if(i != (listSize-1)){
		        		excludeRootTypeStrbuff.append(',');
		        	}
		        	
		        }
				StringBuilder urlBuilder = new StringBuilder();
				urlBuilder.append("../common/emxFullSearch.jsp?");
				//urlBuilder.append("../common/emxFullSearch.jsp?field=TYPES=type_Task,type_Phase,type_Milestone,type_Gate,type_ProjectSpace,type_ProjectConcept:Type!=type_Experiment,type_ProjectBaseline:CURRENT!=Complete,Cancel,Hold,Archive:PRG_TASK_PARENT_KINDOF_PROJECTBASELINE!=TRUE:PRG_TASK_PARENT_KINDOF_EXPERIMENT!=TRUE:PRG_TASK_PARENT_KINDOF_PROJECTTEMPLATE!=TRUE:PRG_TASK_PROJECTSPACE_TYPE!=Experiment,Project Baseline,Project Template:PRG_TASK_PROJECTSPACE_ID!=");
				//IR-1232414 - Allow all types as flexible links under Gate And Milestone
//				if(isMilestoneSelected){
//					urlBuilder.append("field=TYPES=type_Milestone:CURRENT!=Complete");
//				} else if(isGateSelected){
//					urlBuilder.append("field=TYPES=type_Gate:CURRENT!=Complete");
//				} else{
					urlBuilder.append("field=TYPES=type_TaskManagement,type_ProjectSpace,type_ProjectConcept:Type!=type_Experiment,type_ProjectBaseline,type_VPLMTask,type_PQPTask:CURRENT!=Complete,Cancel,Hold,Archive");
//				}
				urlBuilder.append(":PRG_TASK_PROJECTSPACE_ID!="+projectId);
				if(finalExcludeParentTypeList.size() > 0){
					urlBuilder.append(":PRG_TASK_ROOT_TYPE!="+excludeRootTypeStrbuff.toString());
				}
				
				//urlBuilder.append(":PRG_TASK_ROOT_POLICY!=Project Space Hold Cancel");
				urlBuilder.append(":PRG_TASK_ROOT_CURRENT!=Cancel,Hold");
				
				urlBuilder.append("&table=PMCProjectSummaryForProjects");
				urlBuilder.append("&selection=multiple&toolbar=");
				urlBuilder.append("&suiteKey=ProgramCentral");
				urlBuilder.append("&objectId=" + objectId);
				urlBuilder.append("&selectedObjectId=" + XSSUtil.encodeURLForServer(context,selectObjId));
				urlBuilder.append("&selectedObjectRowId=" + XSSUtil.encodeURLForServer(context,rowId));
				urlBuilder.append("&portalCmdName=" + portalCmd);
				urlBuilder.append("&submitURL=../programcentral/emxProjectManagementUtil.jsp&mode=PassiveProject&subMode=AddPassive");
				urlBuilder.append("&excludeOIDprogram=emxTask:getExcludeOIDForPassiveProjectSearch");
				%>
				 <script language="javascript">
					var url ="<%=urlBuilder.toString()%>";
				 	showModalDialog(url,1000,1000);
			  	 </script>
				<%
			}
			
		}else if("AddPassive".equalsIgnoreCase(subMode)){
			 String selectedObjectId 	= emxGetParameter(request, "selectedObjectId");
			 String selectedObjectRowId	= emxGetParameter(request, "selectedObjectRowId");
			 String[] selectedIds 		= emxGetParameterValues(request, "emxTableRowId");
			 
			 RelationshipType passiveSubtaskType = new RelationshipType(ProgramCentralConstants.RELATIONSHIP_PASSIVE_SUBTASK);
			 Task selectedObj = new Task(selectedObjectId);
			
			 try{
					int len = selectedIds.length;
					//String[] toObjIds = new String[len];
					//StringList toObjIdList = new StringList(); 
					
					StringList busSelectable = new StringList();
					 busSelectable.add("from[" + ProgramCentralConstants.RELATIONSHIP_PASSIVE_SUBTASK + "].to.id");
					 busSelectable.add(ProgramCentralConstants.SELECT_PAL_OBJECTID_FROM_TASK);

					BusinessObjectWithSelectList bwsl =	ProgramCentralUtil.getObjectWithSelectList(context,new String[]{selectedObjectId}, busSelectable, true);
					BusinessObjectWithSelect objectWithSelect = bwsl.get(0);
					StringList alreadyConnectedPassiveTaskIdList = objectWithSelect.getSelectDataList("from[" + ProgramCentralConstants.RELATIONSHIP_PASSIVE_SUBTASK + "].to.id");
					String placeholderPALId =  objectWithSelect.getSelectData(ProgramCentralConstants.SELECT_PAL_OBJECTID_FROM_TASK);
					
					//Prepare the probable Passive tasks to be added list
				 	StringList selectedPassiveTasksToBeAddedList = new StringList();
					for(int i =0; i<len;i++){
						StringList projectIdList = StringUtil.split(selectedIds[i], "|");
						selectedPassiveTasksToBeAddedList.add((String) projectIdList.get(0));
					}
					//Convert list into Array for Database Hit on Tasks to be added as Passive Task
					String[] selectedIdArray = selectedPassiveTasksToBeAddedList.toArray(new String[0]);
					
					StringList selects = new StringList();
					selects.add(ProgramCentralConstants.SELECT_PAL_OBJECTID_FROM_TASK);
					selects.add(DomainConstants.SELECT_NAME);
					BusinessObjectWithSelectList passiveTasksSelectList = ProgramCentralUtil.getObjectWithSelectList(context, selectedIdArray, selects, true);
					
					StringList tasksWithSamePALNameList	 = new StringList();
					StringList connectAsPassiveTaskIdList = new StringList();
					//Prepare a Namelist of the selected tasks which are of the same context (Project) as placeholder.
					for(int k =0; k<len;k++){
						BusinessObjectWithSelect passiveTasksSelect = passiveTasksSelectList.get(k);
						String passiveTaskPALId = passiveTasksSelect.getSelectData(ProgramCentralConstants.SELECT_PAL_OBJECTID_FROM_TASK);
						if(ProgramCentralUtil.isNotNullString(placeholderPALId) && ProgramCentralUtil.isNotNullString(passiveTaskPALId) && placeholderPALId.equalsIgnoreCase(passiveTaskPALId)){
							tasksWithSamePALNameList.add(passiveTasksSelect.getSelectData(DomainConstants.SELECT_NAME));
						}else if(alreadyConnectedPassiveTaskIdList==null || !alreadyConnectedPassiveTaskIdList.contains(selectedPassiveTasksToBeAddedList.get(k))){
							connectAsPassiveTaskIdList.add(selectedPassiveTasksToBeAddedList.get(k));
			        	}
					}
				 
					String[] toObjIdArr = new String[connectAsPassiveTaskIdList.size()];
					connectAsPassiveTaskIdList.toArray(toObjIdArr);
					if(toObjIdArr.length > 0){
						Map<String,String> connectedObjMap = (Map<String,String>)DomainRelationship.connect(context, selectedObj, passiveSubtaskType, true, toObjIdArr);
					}
					int invalidPassiveTaskNamesListSize = tasksWithSamePALNameList.size();
					if(invalidPassiveTaskNamesListSize > 0){
						String language = context.getSession().getLanguage();
						String errorString = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.PassiveSubtask.SameContextTask", language);
						//"Passive Subtask cannot be added under same project task. " + tasksWithSamePALNameList;
						errorString = errorString + " " + tasksWithSamePALNameList;
						
						%>
						 <script language="javascript">
						 alert("<%= errorString %>");
							//return;
					  	 </script>
						<%
					}
				 	
			 }catch(Exception e){
				 e.printStackTrace();
			 }
			 
		}else if("SwitchPassiveProject".equalsIgnoreCase(subMode)){
			String connectionId = emxGetParameter(request, "connectionId");
			String selectedObjectId = emxGetParameter(request, "objectId");
			String parentId = emxGetParameter(request, "parentId");
	
			try{
				
				ContextUtil.startTransaction(context, true);
				String mqlCommand = "delete connection $1";
					
				if(ProgramCentralUtil.isNotNullString(connectionId)) {
					MqlUtil.mqlCommand(context, true, true, mqlCommand, true, connectionId); //delete existing connection
				}
					
				if(ProgramCentralUtil.isNotNullString(selectedObjectId) && ProgramCentralUtil.isNotNullString(parentId)) {
					Task task = new Task(parentId);
					task.addExisting(context, new String[]{selectedObjectId}, null); //convert to subtask relationship
				}
			ContextUtil.commitTransaction(context);
			}catch(Exception e){
				ContextUtil.abortTransaction(context);
				e.printStackTrace();
				throw e;
			}
			
			
		}else if("RemovePassiveProject".equalsIgnoreCase(subMode)){

			String connectionId 		= emxGetParameter(request, "connectionId");
			String removeSuccess = "true";
			try{
				ContextUtil.startTransaction(context, true);
				String mqlCommand = "delete connection $1";
					
				if(ProgramCentralUtil.isNotNullString(connectionId)){
						MqlUtil.mqlCommand(context, true, true, mqlCommand, true, connectionId);
				}
				
				ContextUtil.commitTransaction(context);
			}catch(Exception e){
				ContextUtil.abortTransaction(context);
				removeSuccess = "false";
				throw e;
			}

			JsonObject jsonObject = Json.createObjectBuilder()
	                .add("removeSuccess",removeSuccess)
	                .build();

			out.clear();
			out.write(jsonObject.toString());
			return;
		
		}else if("SyncPassiveProject".equalsIgnoreCase(subMode)){

			String selectedId = emxGetParameter(request, "objectId");

			try{
				Task.syncPassiveProject(context, selectedId);
				%>
				<script language="javascript" type="text/javaScript">
				var portalName = "<%=portalCmd%>";
				
				var topFrame = findFrame(getTopWindow(), portalName);
				
				if(topFrame == null){
					  topFrame = findFrame(getTopWindow(), "detailsDisplay");
				}
				topFrame.emxEditableTable.refreshStructureWithOutSort();
	    	
			</script>
			<%
				
			}catch(Exception e){
				e.printStackTrace();
				throw e;
			}
	    }
	}else if ("errorMessageForDuration".equalsIgnoreCase(strMode)) {
		String errorKey = emxGetParameter(request,"errorKey");
		String decimalKey = (String) emxGetParameter(request, "decimalkey");
		
		String language = context.getSession().getLanguage();
		String errorMessageforDecimal = EnoviaResourceBundle.getProperty(null, decimalKey);
		
		String[] messageValues = new String[1];
        messageValues[0] = errorMessageforDecimal;
		String sMessage = MessageUtil.getMessage(context, null, errorKey, messageValues, null, context.getLocale(), "emxProgramCentralStringResource");
		JsonObject jsonObject = Json.createObjectBuilder()
                .add("errorMessage",sMessage)
                .add("decimalSymbol",errorMessageforDecimal)
                .build();

		out.clear();
		out.write(jsonObject.toString());
		return;
  	}
	else if (ProgramCentralConstants.INSERT_EXISTING_PROJECT_BELOW.equalsIgnoreCase(strMode)) {

		String objId = emxGetParameter(request, "objectId");
	     objId = XSSUtil.encodeURLForServer(context, objId);
		String currentFrame = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "portalCmdName"));
		Map mpRow =null;
		String selectedTaskid = DomainObject.EMPTY_STRING;
		String requiredTaskId = emxGetParameter(request,"emxTableRowId");
		String requiredTaskId1 = ProgramCentralConstants.EMPTY_STRING;
		String requiredTaskId2 = ProgramCentralConstants.EMPTY_STRING;
		
		if (null != requiredTaskId) {
			mpRow = ProgramCentralUtil.parseTableRowId(context,requiredTaskId);
			requiredTaskId1 = (String) mpRow.get("objectId");
			requiredTaskId2 = (String) (String) mpRow.get("parentOId");
			requiredTaskId = requiredTaskId1 + "|" + requiredTaskId2;
		}
		
		String strSelectedObjID=(String) mpRow.get("objectId");
		String SELECT_IS_MARK_AS_DELETED = "to["+DomainConstants.RELATIONSHIP_DELETED_SUBTASK+"]";
		DomainObject selectedObj = new DomainObject(strSelectedObjID);
		
        StringList selectList = new StringList(5);
        selectList.add(DomainObject.SELECT_CURRENT);
        selectList.add(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
        selectList.add(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
        selectList.add(ProgramCentralConstants.SELECT_IS_TASK_MANAGEMENT);
        selectList.add(ProgramCentralConstants.SELECT_PROJECT_ID);
        selectList.add(SELECT_IS_MARK_AS_DELETED);
        selectList.add(ProgramCentralConstants.SELECT_IS_GATE);
        selectList.add(ProgramCentralConstants.SELECT_IS_MILESTONE);
        selectList.add(ProgramCentralConstants.SELECT_HAS_PASSIVE_TASK);

        MapList objectInfoList 	= ProgramCentralUtil.getObjectDetails(context, new String[]{strSelectedObjID}, selectList,true);
		Map selectedObjInfo 	= (Map)objectInfoList.get(0);

		String isGate 				= (String)selectedObjInfo.get(ProgramCentralConstants.SELECT_IS_GATE);
		String isMilestone			= (String)selectedObjInfo.get(ProgramCentralConstants.SELECT_IS_MILESTONE);
		String selectedTaskState = (String)selectedObjInfo.get(DomainObject.SELECT_CURRENT);
		String isMarkAsDeleted 		= (String)selectedObjInfo.get(SELECT_IS_MARK_AS_DELETED);
		String hasPassiveProject 		= (String)selectedObjInfo.get(ProgramCentralConstants.SELECT_HAS_PASSIVE_TASK);

		if("true".equalsIgnoreCase(hasPassiveProject)){
			 %>
	          <script language="javascript" type="text/javaScript">
  	       		if(parent.emxEditableTable.checkDataModified()){
					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
		 		}else{
	         		alert("<framework:i18nScript localize="i18nId">emxProgramCentral.PassiveProjectSpace.AddSubtask.InvalidOperation</framework:i18nScript>");
	          		
		 		}
	          </script>
	         <%
			}else if("true".equalsIgnoreCase(isMarkAsDeleted)){
			 %>
	          <script language="javascript" type="text/javaScript">
   	       		if(parent.emxEditableTable.checkDataModified()){
					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
		 		}else{
	         		alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.TaskHasBeenMarkedForDeletion</framework:i18nScript>");
	          		window.close();
		 		}
	          </script>
	         <%
			}else if(DomainObject.STATE_PROJECT_SPACE_REVIEW.equalsIgnoreCase(selectedTaskState)||
				DomainObject.STATE_PROJECT_SPACE_COMPLETE.equalsIgnoreCase(selectedTaskState)){
			%>
                 <script language="javascript" type="text/javaScript">
                     if(parent.emxEditableTable.checkDataModified()){
 	  					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
 	 				 }else{
 	                  var msg = "<framework:i18nScript localize="i18nId">emxProgramCentral.Project.ParentInState</framework:i18nScript>" ;
 	                  alert ( msg );
 	                  window.closeWindow();
 	 				 }
                 </script>
     	    <%
     	         return;
                }else if("true".equalsIgnoreCase(isGate) || "true".equalsIgnoreCase(isMilestone)){
        			%>
        	 	   	<script language="javascript" type="text/javaScript">
            	 	   	if(parent.emxEditableTable.checkDataModified()){
   		                	alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
   		            	} else{
   		            		alert("<framework:i18nScript localize="i18nId">emxProgramcentral.Milestone.SubtaskCreationAlert</framework:i18nScript>");
   		            	}
        	        </script>
        	 		<%
        	 	  return;
                }else{
			String isTaskMgt = (String)selectedObjInfo.get(ProgramCentralConstants.SELECT_IS_TASK_MANAGEMENT);
			String isProject = (String)selectedObjInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			String isProjectConcept = (String)selectedObjInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
			String projectId = DomainObject.EMPTY_STRING;

			if("true".equalsIgnoreCase(isTaskMgt)){
				projectId = (String)selectedObjInfo.get(ProgramCentralConstants.SELECT_PROJECT_ID);

			}else if("true".equalsIgnoreCase(isProject) || "TRUE".equalsIgnoreCase(isProjectConcept)){
				projectId = strSelectedObjID;
			}

			ProjectSpace project 	= new ProjectSpace(projectId);
			String isExperimentProj = project.getInfo(context, ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);
			boolean isExpProj 		= "true".equalsIgnoreCase(isExperimentProj)?true:false;
			StringList excludeOIDList = ProgramCentralUtil.getProjectExclusionList(context, project,isExpProj);

			if(!excludeOIDList.contains(projectId)){
				excludeOIDList.addElement(projectId);
			}

			String excludeOIDs = StringUtil.join(excludeOIDList,ProgramCentralConstants.COMMA);

    		String strURL = "../common/emxFullSearch.jsp?field=TYPES=type_ProjectSpace,type_ProjectConcept:Type!=type_Experiment,type_ProjectBaseline:Current!=policy_ProjectSpace.state_Complete,policy_ProjectSpaceHoldCancel.state_Cancel,policy_ProjectSpaceHoldCancel.state_Hold,policy_ProjectSpace.state_Archive&table=PMCProjectSummaryForProjects&selection=multiple&toolbar=&editLink=true&submitURL=../programcentral/emxprojectCreateWizardAction.jsp&fromProgram=null&fromWBS=false&fromAction=false&wizType=AddExistingSubtaskProject&addType=Child&copyExistingSearchShowSubTypes=true&pageName=fromAddExistingSubProject&p_button=Next&fromSubProjects=true&calledMethod=submitAddProject&PMCWBSQuickTaskTypeToAddBelow=Task&portalMode=true&copyExistingProjectType=type_ProjectManagement&fromClone=false&wbsForm=false&insertExistingProjectBelowMode=insertExistingProjectBelow&suiteKey=ProgramCentral&HelpMarker=emxhelpinsertprojectsastasks";
			strURL = strURL + "&objectId=" + XSSUtil.encodeForURL(context, objId) + "&requiredTaskId=" + XSSUtil.encodeForURL(context, requiredTaskId) + "&parentProjectId=" + XSSUtil.encodeForURL(context, objId)+ "&portalCmdName="+currentFrame;
    		strURL = strURL + "&requiredTaskId1=" + XSSUtil.encodeForURL(context, requiredTaskId1)+ "&requiredTaskId2=" + XSSUtil.encodeForURL(context, requiredTaskId2);
			strURL = strURL + "&excludeOID="+XSSUtil.encodeForURL(context, excludeOIDs);

%>
 <script language="javascript">
<%-- XSSOK--%>
  	if(parent.emxEditableTable.checkDataModified()){
		alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
	}else{
		var strUrl ="<%=strURL%>";
	 	//window.parent.location.href = strUrl;
	 	showModalDialog(strUrl,1000,1000);
	}
	   </script>
<%
	}

	}else
		if ("PMCCopyPartialSchedule".equalsIgnoreCase(strMode)) {

		String currentFrame = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "portalCmdName"));
		String parentOID = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"parentOID"));
		String objectId = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"objectId"));
		String openerFrame = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"openerFrame"));
		String PMCWBSQuickTaskTypeToAddBelow = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"PMCWBSQuickTaskTypeToAddBelow"));
		String PMCWBSQuickTasksToAddBelow = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"PMCWBSQuickTasksToAddBelow"));
		Map mpRow =null;
		String selectedTaskid = DomainObject.EMPTY_STRING;
		String requiredTaskId = emxGetParameter(request,
				"emxTableRowId");
		if (null != requiredTaskId) {

			mpRow = ProgramCentralUtil.parseTableRowId(context,
					requiredTaskId);
			requiredTaskId = (String) mpRow.get("objectId") + "|" + (String) mpRow.get("parentOId");
		}
		String strSelectedObjID=(String) mpRow.get("objectId");

		DomainObject selectedObj = new DomainObject(strSelectedObjID);

		String SELECT_IS_MARK_AS_DELETED = "to["+DomainConstants.RELATIONSHIP_DELETED_SUBTASK+"]";
        StringList selectList = new StringList(3);
        selectList.add(DomainObject.SELECT_CURRENT);
        selectList.add(SELECT_IS_MARK_AS_DELETED);
        selectList.add(DomainObject.SELECT_POLICY);
        selectList.add(ProgramCentralConstants.SELECT_HAS_PASSIVE_TASK);

        Map taskInfo = selectedObj.getInfo(context, selectList);
        String selectedTaskStates = (String)taskInfo.get(DomainObject.SELECT_CURRENT);
		String isMarkAsDeleted 		= (String)taskInfo.get(SELECT_IS_MARK_AS_DELETED);
		String policy 		= (String)taskInfo.get(DomainObject.SELECT_POLICY);
		String hasPassiveProject = (String)taskInfo.get(ProgramCentralConstants.SELECT_HAS_PASSIVE_TASK);

		  if("true".equalsIgnoreCase(hasPassiveProject)){
			 %>
	          <script language="javascript" type="text/javaScript">
	       		if(parent.emxEditableTable.checkDataModified()){
					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
		 		}else{
	         		alert("<framework:i18nScript localize="i18nId">emxProgramCentral.PassiveProjectSpace.AddSubtask.InvalidOperation</framework:i18nScript>");
	          	}
	          </script>
	         <%
	         return;
			}else if("true".equalsIgnoreCase(isMarkAsDeleted)){
			 %>
	          <script language="javascript" type="text/javaScript">
	       		if(parent.emxEditableTable.checkDataModified()){
					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
		 		}else{
	         		alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.TaskHasBeenMarkedForDeletion</framework:i18nScript>");
	          		window.close();
		 		}
	          </script>
	         <%
	         return;
			}else if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equalsIgnoreCase(policy)){
    			%>
    	 	   	<script language="javascript" type="text/javaScript">
    	 	   if(parent.emxEditableTable.checkDataModified()){
	                alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
	            } else{
	            	alert("<framework:i18nScript localize="i18nId">emxProgramcentral.Milestone.SubtaskCreationAlert</framework:i18nScript>");
	            }
    	        </script>
    	 		<%
    	 	  return;
    	}

	    if(selectedTaskStates.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE) || selectedTaskStates.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_TASK_REVIEW) ){
			%>
                 <script language="javascript" type="text/javaScript">
                 if(parent.emxEditableTable.checkDataModified()){
    					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
    		 		}else{
                 		var msg = "<framework:i18nScript localize="i18nId">emxProgramCentral.Project.ParentInState</framework:i18nScript>" ;
                		alert ( msg );
                		window.closeWindow();
    		 		}
                 </script>
     	    <%
     	         return;
                }else{
                	String strURL = "../common/emxForm.jsp?form=PMCCopyPartialSchedule&mode=edit&submitAction=doNothing&StringResourceFileId=emxProgramCentralStringResource&SuiteDirectory=programcentral&suiteKey=ProgramCentral&preProcessJavaScript=disableQuestionField&formHeader=emxProgramCentral.CopySchedule.CopyPartialSchedule&HelpMarker=emxhelpwbscopyschedule";
					strURL += "&copyFrom=Project&parentOID="+parentOID+"&objectId="+strSelectedObjID+"&openerFrame="+openerFrame;
					strURL += "&PMCWBSQuickTaskTypeToAddBelow="+PMCWBSQuickTaskTypeToAddBelow+"&PMCWBSQuickTasksToAddBelow="+PMCWBSQuickTasksToAddBelow;
					strURL += "&postProcessURL=../programcentral/emxProjectManagementUtil.jsp?mode=refreshCopyPartialSchedule&formFieldsOnly=true";


					%>
						 <script language="javascript">
					  <%-- XSSOK--%>
						 if(parent.emxEditableTable.checkDataModified()){
								alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
						 }else{
							var strUrl ="<%=strURL%>";
							//window.parent.location.href = strUrl;
							top.showSlideInDialog(strUrl,true);
						}
						   </script>
					<%
			}

	}else if ("JF_PMCCopyPartialSchedule".equalsIgnoreCase(strMode)) {
				// add by ljr 20260512  客制化 项目复制

			String currentFrame = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "portalCmdName"));
			String parentOID = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"parentOID"));
			String objectId = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"objectId"));
			String openerFrame = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"openerFrame"));
			String PMCWBSQuickTaskTypeToAddBelow = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"PMCWBSQuickTaskTypeToAddBelow"));
			String PMCWBSQuickTasksToAddBelow = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"PMCWBSQuickTasksToAddBelow"));
			Map mpRow =null;
			String selectedTaskid = DomainObject.EMPTY_STRING;
			String requiredTaskId = emxGetParameter(request,
			"emxTableRowId");
			if (null != requiredTaskId) {

				mpRow = ProgramCentralUtil.parseTableRowId(context,
				requiredTaskId);
				requiredTaskId = (String) mpRow.get("objectId") + "|" + (String) mpRow.get("parentOId");
			}
			String strSelectedObjID=(String) mpRow.get("objectId");

			DomainObject selectedObj = new DomainObject(strSelectedObjID);

			String SELECT_IS_MARK_AS_DELETED = "to["+DomainConstants.RELATIONSHIP_DELETED_SUBTASK+"]";
			StringList selectList = new StringList(3);
			selectList.add(DomainObject.SELECT_CURRENT);
			selectList.add(SELECT_IS_MARK_AS_DELETED);
			selectList.add(DomainObject.SELECT_POLICY);
			selectList.add(ProgramCentralConstants.SELECT_HAS_PASSIVE_TASK);

			Map taskInfo = selectedObj.getInfo(context, selectList);
			String selectedTaskStates = (String)taskInfo.get(DomainObject.SELECT_CURRENT);
			String isMarkAsDeleted 		= (String)taskInfo.get(SELECT_IS_MARK_AS_DELETED);
			String policy 		= (String)taskInfo.get(DomainObject.SELECT_POLICY);
			String hasPassiveProject = (String)taskInfo.get(ProgramCentralConstants.SELECT_HAS_PASSIVE_TASK);

			if("true".equalsIgnoreCase(hasPassiveProject)){
			%>
				<script language="javascript" type="text/javaScript">
				if(parent.emxEditableTable.checkDataModified()){
					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
				}else{
					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.PassiveProjectSpace.AddSubtask.InvalidOperation</framework:i18nScript>");
				}
				</script>
			<%
				return;
			}else if("true".equalsIgnoreCase(isMarkAsDeleted)){
			%>
				<script language="javascript" type="text/javaScript">
				if(parent.emxEditableTable.checkDataModified()){
					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
				}else{
					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.TaskHasBeenMarkedForDeletion</framework:i18nScript>");
					window.close();
				}
				</script>
			<%
				return;
			}else if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equalsIgnoreCase(policy)){
			%>
				<script language="javascript" type="text/javaScript">
				if(parent.emxEditableTable.checkDataModified()){
					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
				} else{
					alert("<framework:i18nScript localize="i18nId">emxProgramcentral.Milestone.SubtaskCreationAlert</framework:i18nScript>");
				}
				</script>
			<%
				return;
			}

			if(selectedTaskStates.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE) || selectedTaskStates.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_TASK_REVIEW) ){
			%>
				<script language="javascript" type="text/javaScript">
				if(parent.emxEditableTable.checkDataModified()){
					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
				}else{
					var msg = "<framework:i18nScript localize="i18nId">emxProgramCentral.Project.ParentInState</framework:i18nScript>" ;
					alert ( msg );
					window.closeWindow();
				}
				</script>
			<%
				return;
		}else{
			String strURL = "../common/emxForm.jsp?form=JFPMCCopyPartialSchedule&mode=edit&submitAction=doNothing&StringResourceFileId=emxProgramCentralStringResource&SuiteDirectory=programcentral&suiteKey=ProgramCentral&preProcessJavaScript=disableQuestionField&formHeader=emxProgramCentral.CopySchedule.CopyPartialSchedule&HelpMarker=emxhelpwbscopyschedule";
			strURL += "&copyFrom=Project&parentOID="+parentOID+"&objectId="+strSelectedObjID+"&openerFrame="+openerFrame;
			strURL += "&PMCWBSQuickTaskTypeToAddBelow="+PMCWBSQuickTaskTypeToAddBelow+"&PMCWBSQuickTasksToAddBelow="+PMCWBSQuickTasksToAddBelow;
			strURL += "&postProcessURL=../programcentral/emxProjectManagementUtil.jsp?mode=JF_refreshCopyPartialSchedule&rollup=none&formFieldsOnly=true";

			%>
			<script language="javascript">
			<%-- XSSOK--%>
			if(parent.emxEditableTable.checkDataModified()){
				alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
			}else{
				var strUrl ="<%=strURL%>";
				//window.parent.location.href = strUrl;
				top.showSlideInDialog(strUrl,true);
			}
			</script>
			<%
		}
	} else if ("PMCImportScheduleFromFile".equalsIgnoreCase(strMode)) {

		String currentFrame = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "portalCmdName"));
		String parentOID = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"parentOID"));
		String objectId = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"objectId"));
		String openerFrame = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"openerFrame"));
		String PMCWBSQuickTaskTypeToAddBelow = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"PMCWBSQuickTaskTypeToAddBelow"));
		String PMCWBSQuickTasksToAddBelow = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"PMCWBSQuickTasksToAddBelow"));
		Map mpRow =null;
		final String SELECT_PARENTOBJECT_KINDOF_PROJECT_TEMPLATE = ProgramCentralConstants.SELECT_PROJECT_TYPE+".kindof["+DomainObject.TYPE_PROJECT_TEMPLATE+"]";
		final String SELECT_PARENTOBJECT_KINDOF_EXPERIMENT = ProgramCentralConstants.SELECT_PROJECT_TYPE+".kindof["+ProgramCentralConstants.TYPE_EXPERIMENT+"]";

		String selectedTaskid = DomainObject.EMPTY_STRING;
		String requiredTaskId = emxGetParameter(request,"emxTableRowId");
		if (null != requiredTaskId) {

			mpRow = ProgramCentralUtil.parseTableRowId(context,
					requiredTaskId);
			requiredTaskId = (String) mpRow.get("objectId") + "|" + (String) mpRow.get("parentOId");
		}
		String strSelectedObjID=(String) mpRow.get("objectId");

		DomainObject selectedObj = new DomainObject(strSelectedObjID);

		String SELECT_IS_MARK_AS_DELETED = "to["+DomainConstants.RELATIONSHIP_DELETED_SUBTASK+"]";
        StringList selectList = new StringList(3);
        selectList.add(DomainObject.SELECT_CURRENT);
        selectList.add(SELECT_IS_MARK_AS_DELETED);
        selectList.add(DomainObject.SELECT_POLICY);
        selectList.add(ProgramCentralConstants.SELECT_HAS_PASSIVE_TASK);
        selectList.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE);
        selectList.add(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);
        selectList.add(SELECT_PARENTOBJECT_KINDOF_PROJECT_TEMPLATE);
        selectList.add(SELECT_PARENTOBJECT_KINDOF_EXPERIMENT);

        Map taskInfo = selectedObj.getInfo(context, selectList);
        String selectedTaskStates = (String)taskInfo.get(DomainObject.SELECT_CURRENT);
		String isMarkAsDeleted 		= (String)taskInfo.get(SELECT_IS_MARK_AS_DELETED);
		String policy 		= (String)taskInfo.get(DomainObject.SELECT_POLICY);
		String hasPassiveProject = (String)taskInfo.get(ProgramCentralConstants.SELECT_HAS_PASSIVE_TASK);
		String isProjectTemplateTask = (String)taskInfo.get(SELECT_PARENTOBJECT_KINDOF_PROJECT_TEMPLATE);
		String isProjectTemplate = (String)taskInfo.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE);
		
		String isExperimentTask = (String)taskInfo.get(SELECT_PARENTOBJECT_KINDOF_EXPERIMENT);
		String isExperiment = (String)taskInfo.get(ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);

		if("true".equalsIgnoreCase(hasPassiveProject)){
			 %>
	          <script language="javascript" type="text/javaScript">
	       		if(parent.emxEditableTable.checkDataModified()){
					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
		 		}else{
	         		alert("<framework:i18nScript localize="i18nId">emxProgramCentral.PassiveProjectSpace.AddSubtask.InvalidOperation</framework:i18nScript>");
	          	}
	          </script>
	         <%
	         return;
			}else if("true".equalsIgnoreCase(isMarkAsDeleted)){
			 %>
	          <script language="javascript" type="text/javaScript">
	       		if(parent.emxEditableTable.checkDataModified()){
					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
		 		}else{
	         		alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.TaskHasBeenMarkedForDeletion</framework:i18nScript>");
	          		window.close();
		 		}
	          </script>
	         <%
	         return;
			}else if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equalsIgnoreCase(policy)){
    			%>
    	 	   	<script language="javascript" type="text/javaScript">
    	 	   if(parent.emxEditableTable.checkDataModified()){
	                alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
	            } else{
	            	alert("<framework:i18nScript localize="i18nId">emxProgramcentral.Milestone.SubtaskCreationAlert</framework:i18nScript>");
	            }
    	        </script>
    	 		<%
    	 	  return;
    	}

	    if(selectedTaskStates.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE) || selectedTaskStates.equalsIgnoreCase(ProgramCentralConstants.STATE_PROJECT_TASK_REVIEW) ){
			%>
                 <script language="javascript" type="text/javaScript">
                 if(parent.emxEditableTable.checkDataModified()){
    					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
    		 		}else{
                 		var msg = "<framework:i18nScript localize="i18nId">emxProgramCentral.Project.ParentInState</framework:i18nScript>" ;
                		alert ( msg );
                		window.closeWindow();
    		 		}
                 </script>
     	    <%
     	         return;
                }else{
                	String strURL = "../common/emxForm.jsp?form=PMCCopyPartialSchedule&mode=edit&postProcessURL=../programcentral/emxProgramCentralUtil.jsp?mode=importProjectSchedule&submitAction=doNothing&StringResourceFileId=emxProgramCentralStringResource&SuiteDirectory=programcentral&suiteKey=ProgramCentral&preProcessJavaScript=disableSchedulePreviewButton&importMode=Schedule&formHeader=emxProgramCentral.CopyWzrd.Import&HelpMarker=emxhelpwbscopyschedule";
					strURL += "&copyFrom=File&parentOID="+parentOID+"&objectId="+strSelectedObjID+"&openerFrame="+openerFrame;
					strURL += "&PMCWBSQuickTaskTypeToAddBelow="+PMCWBSQuickTaskTypeToAddBelow+"&PMCWBSQuickTasksToAddBelow="+PMCWBSQuickTasksToAddBelow+"&portalCmdName="+currentFrame+"&selectedObjectId="+strSelectedObjID;

					if("True".equalsIgnoreCase(isProjectTemplateTask) || "True".equalsIgnoreCase(isProjectTemplate) ||
							"True".equalsIgnoreCase(isExperimentTask) || "True".equalsIgnoreCase(isExperiment)	){
						strURL +="&importSubProject=false";
					}
					%>
						 <script language="javascript">
					  <%-- XSSOK--%>
					 	 if(parent.emxEditableTable.checkDataModified()){
								alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
						 }else{
							var strUrl ="<%=strURL%>";
					  	 	//window.parent.location.href = strUrl;
					  	 	top.showSlideInDialog(strUrl,true);
						}
					  	   </script>
					<%
			}
	}

    
	else if("downloadDocument".equalsIgnoreCase(strMode))
	{
		String[] selectedIds = request.getParameterValues("emxTableRowId");
			String sLanguage = request.getHeader("Accept-Language");
			String projectId = emxGetParameter(request, "parentOID");
			String objectId = emxGetParameter(request, "objectId");
			String tableName = emxGetParameter(request, "table");
			String errorMessage = ProgramCentralConstants.EMPTY_STRING;
			String xmlMessage = ProgramCentralConstants.EMPTY_STRING;

			
			for (int i=0, size = selectedIds.length;i<size;i++){
			      Map mpRow = ProgramCentralUtil.parseTableRowId(context,selectedIds[i]);
			      selectedIds[i] = (String) mpRow.get("objectId");
		   }
			StringList slBusSelect = new StringList(1);
	        slBusSelect.add(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
	        slBusSelect.add(ProgramCentralConstants.SELECT_IS_PRODUCT_CONFIGURATION);
	        slBusSelect.add(ProgramCentralConstants.SELECT_IS_ENOSTRREFINEMENTSPECIFICATION);
	        
			BusinessObjectWithSelectList objectWithSelectList = ProgramCentralUtil.getObjectWithSelectList(context, selectedIds, slBusSelect);
			boolean selectionContainsFolderOrProductConfigurationOrFilter = false;
			for (int i = 0, j = objectWithSelectList.size(); i < j ; i++) {
                BusinessObjectWithSelect objectWithSelect = objectWithSelectList.get(i);
                if("TRUE".equalsIgnoreCase( objectWithSelect.getSelectData(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT)) || "TRUE".equalsIgnoreCase( objectWithSelect.getSelectData(ProgramCentralConstants.SELECT_IS_PRODUCT_CONFIGURATION)) || "TRUE".equalsIgnoreCase( objectWithSelect.getSelectData(ProgramCentralConstants.SELECT_IS_ENOSTRREFINEMENTSPECIFICATION))){
                	selectionContainsFolderOrProductConfigurationOrFilter = true;
                break;
                }
			}
			
			
			if ((selectionContainsFolderOrProductConfigurationOrFilter)) {

	            %>
	             <script language="javascript" type="text/javaScript">
		 	   		alert("<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Documet.ConfirmMessage</emxUtil:i18nScript>");
	        	</script>
	        	<%
	            return;
			} else {
				
				String strURL = "../components/emxCommonDocumentPreCheckout.jsp?action=download";
				
				Enumeration requestParams = emxGetParameterNames(request);
	  		  	StringBuilder url = new StringBuilder();

	  		  	if(requestParams != null){
	        		  while(requestParams.hasMoreElements()){
		        		  String param = (String)requestParams.nextElement();
		        		  if("emxTableRowId".equalsIgnoreCase(param)){
							  String[] valueArr = emxGetParameterValues(request,param);
							  for(int i=0;i<valueArr.length;i++){
								  url.append("&"+param);
								  url.append("="+XSSUtil.encodeForURL(context, valueArr[i]));
							  }
						  }
						  else{
							  String value = emxGetParameter(request,param);
							  url.append("&"+param);
							  url.append("="+XSSUtil.encodeForURL(context, value));
						  }
	        		  }
	        		  strURL = strURL + url.toString();
	  		  	}
				
				
				
				%>
				<script language="javascript" type="text/javaScript">
				var strUrl = "<%=strURL%>";
				document.location.href = strUrl;
				       </script>
			<%	
			}
		}
    
	else if("deleteDocument".equalsIgnoreCase(strMode))
	{
		String[] selectedIds = request.getParameterValues("emxTableRowId");
			String sLanguage = request.getHeader("Accept-Language");
			String projectId = emxGetParameter(request, "parentOID");
			String objectId = emxGetParameter(request, "objectId");
			String tableName = emxGetParameter(request, "table");
			String errorMessage = ProgramCentralConstants.EMPTY_STRING;
			String xmlMessage = ProgramCentralConstants.EMPTY_STRING;

			
			for (int i=0, size = selectedIds.length;i<size;i++){
			      Map mpRow = ProgramCentralUtil.parseTableRowId(context,selectedIds[i]);
			      selectedIds[i] = (String) mpRow.get("objectId");
		   }
			StringList slBusSelect = new StringList(1);
	        slBusSelect.add(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
	        slBusSelect.add(ProgramCentralConstants.SELECT_IS_PRODUCT_CONFIGURATION);
	        slBusSelect.add(ProgramCentralConstants.SELECT_IS_ENOSTRREFINEMENTSPECIFICATION);
			BusinessObjectWithSelectList objectWithSelectList = ProgramCentralUtil.getObjectWithSelectList(context, selectedIds, slBusSelect);
			boolean selectionContainsFolderOrProductConfigurationOrFilter = false;
			for (int i = 0, j = objectWithSelectList.size(); i < j ; i++) {
                BusinessObjectWithSelect objectWithSelect = objectWithSelectList.get(i);
                if("TRUE".equalsIgnoreCase( objectWithSelect.getSelectData(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT)) || "TRUE".equalsIgnoreCase( objectWithSelect.getSelectData(ProgramCentralConstants.SELECT_IS_PRODUCT_CONFIGURATION)) || "TRUE".equalsIgnoreCase( objectWithSelect.getSelectData(ProgramCentralConstants.SELECT_IS_ENOSTRREFINEMENTSPECIFICATION))){
                	selectionContainsFolderOrProductConfigurationOrFilter = true;
                break;
                }
			}
			
			
			
			if ((selectionContainsFolderOrProductConfigurationOrFilter)) {

	            %>
	             <script language="javascript" type="text/javaScript">
		 	   		alert("<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Documet.ConfirmMessage</emxUtil:i18nScript>");
	        	</script>
	        	<%
	            return;
			} else {
				
				String strURL = "../components/emxCommonDocumentRemove.jsp?action=delete";
				
				Enumeration requestParams = emxGetParameterNames(request);
	  		  	StringBuilder url = new StringBuilder();

	  		  	if(requestParams != null){
	        		  while(requestParams.hasMoreElements()){
		        		  String param = (String)requestParams.nextElement();
						  if("emxTableRowId".equalsIgnoreCase(param)){
							  String[] valueArr = emxGetParameterValues(request,param);
							  for(int i=0;i<valueArr.length;i++){
								  url.append("&"+param);
								  url.append("="+XSSUtil.encodeForURL(context, valueArr[i]));
							  }
						  }
						  else{
							  String value = emxGetParameter(request,param);
							  url.append("&"+param);
							  url.append("="+XSSUtil.encodeForURL(context, value));
						  }
	        		  }
	        		  strURL = strURL + url.toString();
	  		  	}
				
				
				
				%>
				<script language="javascript" type="text/javaScript">
				var confirmMsg = confirm("<framework:i18nScript localize="i18nId">emxProgramCentral.Alert.Delete</framework:i18nScript>");
				if (confirmMsg){
				var strUrl = "<%=strURL%>";
				document.location.href = strUrl;
				}
				       </script>
			<%	
			}
		}
    
	else if("checkoutDocument".equalsIgnoreCase(strMode))
	{
		String[] selectedIds = request.getParameterValues("emxTableRowId");
			String sLanguage = request.getHeader("Accept-Language");
			String projectId = emxGetParameter(request, "parentOID");
			String objectId = emxGetParameter(request, "objectId");
			String tableName = emxGetParameter(request, "table");
			String errorMessage = ProgramCentralConstants.EMPTY_STRING;
			String xmlMessage = ProgramCentralConstants.EMPTY_STRING;

			
			for (int i=0, size = selectedIds.length;i<size;i++){
			      Map mpRow = ProgramCentralUtil.parseTableRowId(context,selectedIds[i]);
			      selectedIds[i] = (String) mpRow.get("objectId");
		   }
			StringList slBusSelect = new StringList(1);
	        slBusSelect.add(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
	        slBusSelect.add(ProgramCentralConstants.SELECT_IS_PRODUCT_CONFIGURATION);
	        slBusSelect.add(ProgramCentralConstants.SELECT_IS_ENOSTRREFINEMENTSPECIFICATION);
			BusinessObjectWithSelectList objectWithSelectList = ProgramCentralUtil.getObjectWithSelectList(context, selectedIds, slBusSelect);
			boolean selectionContainsFolderOrProductConfigurationOrFilter = false;
			for (int i = 0, j = objectWithSelectList.size(); i < j ; i++) {
                BusinessObjectWithSelect objectWithSelect = objectWithSelectList.get(i);
                if("TRUE".equalsIgnoreCase( objectWithSelect.getSelectData(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT)) || "TRUE".equalsIgnoreCase( objectWithSelect.getSelectData(ProgramCentralConstants.SELECT_IS_PRODUCT_CONFIGURATION)) || "TRUE".equalsIgnoreCase( objectWithSelect.getSelectData(ProgramCentralConstants.SELECT_IS_ENOSTRREFINEMENTSPECIFICATION))){
                	selectionContainsFolderOrProductConfigurationOrFilter = true;
                break;
                }
			}
			
			
			
			if ((selectionContainsFolderOrProductConfigurationOrFilter)) {

	            %>
	             <script language="javascript" type="text/javaScript">
		 	   		alert("<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Documet.ConfirmMessage</emxUtil:i18nScript>");
	        	</script>
	        	<%
	            return;
			} else {
				
				String strURL = "../components/emxCommonDocumentPreCheckout.jsp?action=checkout";
				
				Enumeration requestParams = emxGetParameterNames(request);
	  		  	StringBuilder url = new StringBuilder();

	  		  	if(requestParams != null){
	        		  while(requestParams.hasMoreElements()){
		        		  String param = (String)requestParams.nextElement();
		        		  String value = emxGetParameter(request,param);
		        		  url.append("&"+param);
		        		  url.append("="+XSSUtil.encodeForURL(context, value));
	        		  }
	        		  strURL = strURL + url.toString();
	  		  	}
				
				
				
				%>
				<script language="javascript" type="text/javaScript">
				
				
				var strUrl = "<%=strURL%>";
				document.location.href = strUrl;
				
				       </script>
			<%	
			}
		}
		else if ("taskRequirementAlert".equalsIgnoreCase(strMode)) {
    	String objectId = emxGetParameter(request,"objectId");

    	boolean showAlert= false;
    	String taskSourceId=DomainObject.EMPTY_STRING;
    	String templateId=DomainObject.EMPTY_STRING;
    	String isParentProjectSpace=DomainObject.EMPTY_STRING;
    	
    	StringList slObjectSelect = new StringList("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type.kindof["+DomainObject.TYPE_PROJECT_SPACE+"]");
    	slObjectSelect.add("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.from[Initiated Template Project].to.id");
    	slObjectSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_SOURCE_ID);
    	
    	BusinessObjectWithSelectList objectWithSelectList = null;
    	objectWithSelectList = ProgramCentralUtil.getObjectWithSelectList(context, new String[] {objectId}, slObjectSelect);
    		
    	BusinessObjectWithSelect objectWithSelect = objectWithSelectList.get(0);
    	taskSourceId = objectWithSelect.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_SOURCE_ID);
    	templateId = objectWithSelect.getSelectData("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.from[Initiated Template Project].to.id");
    	isParentProjectSpace = objectWithSelect.getSelectData("to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type.kindof["+DomainObject.TYPE_PROJECT_SPACE+"]");
    	    	
    	if("True".equalsIgnoreCase(isParentProjectSpace) && UIUtil.isNotNullAndNotEmpty(templateId)){
    			StringList templateTaskSourceIds=new StringList();
    			StringList busSelects = new StringList(ProgramCentralConstants.SELECT_ATTRIBUTE_SOURCE_ID);
    			ProjectTemplate template=new ProjectTemplate(templateId);
    			MapList templateTasks=ProjectSpace.getTemplateTasks(context, template, 0, busSelects, null, false, false,true, null);

    			templateTasks.parallelStream().forEach(taskMap ->{
    				String templateTaskSourceId = (String)	((Map)taskMap).get(ProgramCentralConstants.SELECT_ATTRIBUTE_SOURCE_ID);
    				templateTaskSourceIds.add(templateTaskSourceId);
    			});
    			if(templateTaskSourceIds.contains(taskSourceId)){
    				showAlert=true;
    			}
    		}
    	JsonObject jsonObject = Json.createObjectBuilder()
                .add("showAlert",showAlert)
                .build();

		out.clear();
		out.write(jsonObject.toString());
		return;
    }else if ("HoldGate".equalsIgnoreCase(strMode)){
			String strURL = "../common/emxForm.jsp?form=PMCGateDecision&postProcessJPO=emxProjectHoldAndCancel:projectPolicyChangeSequence&mode=edit&formHeader=emxProgramCentral.Header.CreateDecision&HelpMarker=emxhelpdecisioncreate&switch=Hold&submitAction=refreshCaller";
			Enumeration requestParams = emxGetParameterNames(request);
			StringBuilder url = new StringBuilder();

			if (requestParams != null) {
				while (requestParams.hasMoreElements()) {
					String param = (String) requestParams.nextElement();
					String value = emxGetParameter(request, param);
					url.append("&" + param);
					url.append("=" + XSSUtil.encodeForURL(context, value));
				}
				strURL += url.toString();
			}
			String confirmMessage =EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource",context.getLocale(), "emxProgramCentral.ProjectHoldCancel.HoldGate");
			%>
	        <script language="javascript" type="text/javaScript">
			var result=confirm('<%=confirmMessage%>');
			if (result)
			  {
				var strUrl = "<%=strURL%>";
				top.showSlideInDialog(strUrl,true);
			  }
			</script>
	 	    <%
		}else if ("TerminateGate".equalsIgnoreCase(strMode)){
			String strURL = "../common/emxForm.jsp?form=PMCGateDecision&postProcessJPO=emxProjectHoldAndCancel:projectPolicyChangeSequence&mode=edit&formHeader=emxProgramCentral.Header.CreateDecision&HelpMarker=emxhelpdecisioncreate&switch=Terminate";
			Enumeration requestParams = emxGetParameterNames(request);
			StringBuilder url = new StringBuilder();

			if (requestParams != null) {
				while (requestParams.hasMoreElements()) {
					String param = (String) requestParams.nextElement();
					String value = emxGetParameter(request, param);
					url.append("&" + param);
					url.append("=" + XSSUtil.encodeForURL(context, value));
				}
				strURL += url.toString();
			}
			String confirmMessage =EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource",context.getLocale(), "emxProgramCentral.ProjectHoldCancel.TerminateGate");
			%>
	        <script language="javascript" type="text/javaScript">
			var result=confirm("<%=confirmMessage%>");
			if (result)
			  {
				var strUrl = "<%=strURL%>";
				top.showSlideInDialog(strUrl,true);
			  }
			</script>
	 	    <%
		}else if("promoteSuccessor".equalsIgnoreCase(strMode)){
		    String portalCmd 	= emxGetParameter(request,"portalCmdName");
			
			String objectId 	= emxGetParameter(request, "objectId");
		    Datacollections promoteSuccesorReturnSet = new Datacollections();
			promoteSuccesorReturnSet =  ProjectSpace.promoteSuccessor(context, objectId);

			%>
		      <script language="javascript">
		    		var portalName = "<%=portalCmd%>";
		    	
		    		var topFrame = findFrame(getTopWindow(), portalName);
		      		if(topFrame == null){
		 				topFrame = findFrame(getTopWindow(), "detailsDisplay");
		 			}
		      		 
	 	    	   setTimeout(function() {
		    	    	  
		      		 topFrame.emxEditableTable.refreshStructureWithOutSort();
				 		  },25);
			    
		      </script>
		    <%
	    	} else if("refreshAfterEdit".equalsIgnoreCase(strMode)){
				String portalCmd = emxGetParameter(request,"portalCmdName");
			%>
			<script language="javascript">
				var portalName = "<%=portalCmd%>";
				var topFrame = findFrame(getTopWindow(), portalName);
				if(topFrame == null || topFrame.location.href=="about:blank"){
					topFrame = findFrame(getTopWindow(), "PMCWBS");
	     		if(topFrame == null || topFrame.location.href=="about:blank"){
	     			topFrame = findFrame(getTopWindow(), "PMCWBSDataGridView");
	     			if(topFrame == null || topFrame.location.href=="about:blank"){
	     				topFrame = findFrame(getTopWindow(), "PMCProjectTemplateWBS");	
	     				if(topFrame == null || topFrame.location.href=="about:blank"){
	     					topFrame = findFrame(getTopWindow(), "PMCProjectTemplateWBSDataGridView");
	     					if(topFrame == null || topFrame.location.href=="about:blank"){
						   		topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
					   			if(topFrame == null || topFrame.location.href=="about:blank"){
						   			topFrame = findFrame(getTopWindow(), "detailsDisplay");
         	        			}
					   		}
	     				}
	     				
     	        	}
	     		}
				}
				if(topFrame.emxEditableTable) {
					topFrame.emxEditableTable.refreshStructureWithOutSort();
				} else {
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
				}
		     </script>
				<%
	    	}else if ("defaultProjectCalendar".equalsIgnoreCase(strMode)) {
				    String projectID = emxGetParameter(request,"parentId");
				    String newCalId = emxGetParameter(request,"objectId");
				    String rowId = emxGetParameter(request,"rowId");
				    String sRelId = emxGetParameter(request,"relId");
				    String subMode = emxGetParameter(request,"subMode");
				    DomainRelationship connnection = null;
				   
				    ProjectSpace project = (ProjectSpace) DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.PROGRAM);
					project.setId(projectID);
					java.util.Calendar calDate = java.util.Calendar.getInstance();
					boolean projectScheduledFromStart = false;
					String cmd = "print bus $1 select $2 $3 dump";
					String mqlOutput = MqlUtil.mqlCommand(context, cmd,projectID,ProgramCentralConstants.SELECT_ATTRIBUTE_CONSTRAINT_DATE,
           					          ProgramCentralConstants.ATTRIBUTE_PROJECT_SCHEDULE_FROM);
           			String[] mqlOutputArray = mqlOutput.split(",");
					String oldTaskCnstrDate = mqlOutputArray.length >= 1 ? mqlOutputArray[0] : "";
           			String projectScheduleFrom = mqlOutputArray.length >= 2 ? mqlOutputArray[1] : ""; 
         			if(ProgramCentralConstants.ATTRIBUTE_SCHEDULED_FROM_RANGE_START.equalsIgnoreCase(projectScheduleFrom))
         				projectScheduledFromStart=true;
           			TimeZone tz                 = TimeZone.getTimeZone(context.getSession().getTimezone());
                    double dbMilisecondsOffset  = (double)(-1)*tz.getRawOffset();
                    double clientTZOffset       = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();
                    String clientTimezone = PropertyUtil.getRPEValue(context, "CLIENT_TIMEZONE", true);
                    if(ProgramCentralUtil.isNotNullString(clientTimezone)){
                        clientTZOffset = Double.parseDouble(clientTimezone);
                    }
                    SimpleDateFormat MATRIX_DATE_FORMAT = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
             		 Locale locale  = context.getLocale();             	     
             	     oldTaskCnstrDate=eMatrixDateFormat.getFormattedDisplayDateTime(context, oldTaskCnstrDate, true,iDateFormat, clientTZOffset,locale); 
         		    DateFormat format  = DateFormat.getDateTimeInstance(iDateFormat, iDateFormat, locale);
         		    calDate.setTime(format.parse(oldTaskCnstrDate));
         		   String newConstraintDate = oldTaskCnstrDate;
				    if("setAsDefaultProjectCalendar".equalsIgnoreCase(subMode)) {
					    MapList calendarList = new MapList();
					   
					    calendarList = project.getProjectCalendars(context);
					    for(Iterator iterator = calendarList.iterator(); iterator.hasNext();) {
				   			 Map temp    = (Map) iterator.next();
				   			 String id   = (String)temp.get(DomainConstants.SELECT_ID);
				   			 String relationshipName    = (String)temp.get(ProgramCentralConstants.KEY_RELATIONSHIP);
				   			 String connectionID = (String)temp.get(DomainRelationship.SELECT_ID);
				   			 if(ProgramCentralConstants.RELATIONSHIP_DEFAULT_CALENDAR.equalsIgnoreCase(relationshipName)){
				   				DomainRelationship.setType(context, connectionID, ProgramCentralConstants.RELATIONSHIP_CALENDAR);
				   			 }
				   			 if(newCalId.equalsIgnoreCase(id)){
				   				DomainRelationship.setType(context, connectionID, ProgramCentralConstants.RELATIONSHIP_DEFAULT_CALENDAR);
				   			 }
				   		}
							Map programMap = new HashMap();
				   			programMap.put("newCalId", newCalId);
				   			try{
					   			Map<String,Map<String,String>> result= JPO.invoke(context, "emxTask", null, "getTaskCalendarInfo",JPO.packArgs(programMap),Map.class);
	                            int day = calDate.get(java.util.Calendar.DAY_OF_WEEK);                                                                        
					   			Map<String,String>eventInfo = null;
	                            double startTime    = 0;
	                            double startTimeMinute = 0;
	                            double finishTime   = 0;
	                            double finishTimeMinute = 0;                          
	                            eventInfo   = result.get(String.valueOf(day));
	                            startTime   = Double.parseDouble(eventInfo.get("startTime"));
	                            startTimeMinute = startTime%1 * 60;
	                            finishTime  = Double.parseDouble(eventInfo.get("finishTime"));
	                            finishTimeMinute = finishTime%1 * 60;
	                            
	                            if(projectScheduledFromStart)
	                            {
	                        		calDate.set(Calendar.HOUR_OF_DAY, (int)startTime);
	                        		calDate.set(Calendar.MINUTE,(int)startTimeMinute);
	                            }
	                            else
	                            {
	                            	calDate.set(Calendar.HOUR_OF_DAY, (int)finishTime);
	                            	calDate.set(Calendar.MINUTE,(int)finishTimeMinute);
	                            }	                          
	                            calDate.set(Calendar.SECOND, 0);
	                            newConstraintDate =MATRIX_DATE_FORMAT.format(calDate.getTime());
				   			 }
				   			 catch(Exception e)
				   			 {
				   				 e.printStackTrace();
				   			 }
				  } else if("removeDefaultProjectCalendar".equalsIgnoreCase(subMode)){
					  
					  DomainRelationship.setType(context, sRelId, ProgramCentralConstants.RELATIONSHIP_CALENDAR);
					  if(projectScheduledFromStart)
		           	     calDate.set(Calendar.HOUR_OF_DAY, 8);
		           	  else
		          		 calDate.set(Calendar.HOUR_OF_DAY, 17);
           			  calDate.set(Calendar.MINUTE, 0);
                      calDate.set(Calendar.SECOND, 0);
                      newConstraintDate = MATRIX_DATE_FORMAT.format(calDate.getTime());
				  }
				    cmd = "modify bus $1 $2 $3";
           			MqlUtil.mqlCommand(context, cmd,projectID,"Task Constraint Date",newConstraintDate);
				  //If default calendar is changed then we need to call the rollup 
				  if(ProgramCentralUtil.isNotNullString(projectID)){
					  
				    	//Added code to support Auto/Manual rollup feature 
				    	String projectSchedule = project.getSchedule(context);
				    	if(ProgramCentralUtil.isNullString(projectSchedule)
				    			||ProgramCentralConstants.PROJECT_SCHEDULE_AUTO.equalsIgnoreCase(projectSchedule)){
					    	Task projectRollup = new Task(projectID);
					    	projectRollup.rollupAndSave(context);
				  		}
				  }
				 %>
				 <script language="javascript" type="text/javaScript">
				 	var topFrame = findFrame(getTopWindow(), "frameFieldCalendar");	
				 	if(topFrame == null){
				 		topFrame = findFrame(getTopWindow(), "detailsDisplay");
			    		topFrame.location.href = topFrame.location.href; 		
				 	}else{
				 		topFrame.refreshSBTable(topFrame.configuredTableName);
				 	}
				    //contentFrameObj.refreshSBTable(contentFrameObj.configuredTableName);
		    	</script>
				<%
			}else if("unAssignTaskQuestion".equalsIgnoreCase(strMode)){
    	String taskId = emxGetParameter(request, "objectId");
		String[] selectedNodeIds = emxGetParameterValues(request,"emxTableRowId");
	    StringList questionIds = new StringList();
		for(String nodeId : selectedNodeIds){
			String questionId = (String)(ProgramCentralUtil.parseTableRowId(context,nodeId)).get("objectId");
			questionIds.add(questionId);
		}
		String relatedQuestionId = "from.id";
		StringList selectables = new StringList();
		selectables.add(DomainConstants.SELECT_ID);
		selectables.add(relatedQuestionId);
		DomainObject taskObject  = DomainObject.newInstance(context,taskId);
		MapList taskQuestionsMapList = taskObject.getRelatedObjects(context, "Question", "*", null, selectables, true, false, (short)1, "", "",0);
		StringList connectedIds = new StringList();
		for(int i =0 ; i < taskQuestionsMapList.size() ; i++){
			Map questionMap = (Map) taskQuestionsMapList.get(i);
			String questionId = (String) questionMap.get(relatedQuestionId);
			if(questionIds.contains(questionId)){
				connectedIds.add((String) questionMap.get(DomainConstants.SELECT_ID));
			}
		}
		String[] array = connectedIds.toArray(new String[connectedIds.size()]);
		DomainRelationship.disconnect(context,array);
	    	%>
    	  	<script language="javascript" type="text/javaScript">
	    	  	parent.document.location.href = parent.document.location.href;
    	  	</script>
    	  	<%
  	  } else if ("addTaskCandidate".equalsIgnoreCase(strMode)) 	{
  	    String fieldNameDisplay = emxGetParameter(request,"fieldNameDisplay");
  	    fieldNameDisplay = XSSUtil.encodeURLForServer(context, fieldNameDisplay);
  	    String fieldNameActual = emxGetParameter(request,"fieldNameActual");
  	    fieldNameActual = XSSUtil.encodeURLForServer(context, fieldNameActual);
  	    String fieldNameOID = emxGetParameter(request,"fieldNameOID");
  	    fieldNameOID = XSSUtil.encodeURLForServer(context, fieldNameOID);
  	    String objectId = emxGetParameter(request,"objectId");
  	    objectId = XSSUtil.encodeURLForServer(context, objectId);

  		 if(ProgramCentralUtil.isNullString(objectId)){
  			 objectId = request.getParameter("parentOID");
  		 }
  		
  		com.matrixone.apps.program.Task taskObj = new com.matrixone.apps.program.Task();
        taskObj.setId(objectId);
        StringList slBusSelects = new StringList(1);
        slBusSelects.add(DomainConstants.SELECT_NAME);
        Map projectInfo = taskObj.getProject(context, slBusSelects);
        String strProjectSpace  = (String) projectInfo.get(DomainConstants.SELECT_NAME);
	    strProjectSpace = XSSUtil.encodeForURL(context, strProjectSpace);
		
		String strURL = "../common/emxFullSearch.jsp?table=AEFUserGroupsChooser&selection=multiple&form=AEFSearchUserGroupsForm&objectId="+objectId;
		strURL +="&default=PRG_PEOPLE_PROJECT_MANAGEMENT="+strProjectSpace;
  		strURL +="&submitURL=../common/AEFSearchUtil.jsp?&field=TYPES=type_GroupProxy:CURRENT=policy_Person.state_Active";
  		strURL +="&fieldNameDisplay="+fieldNameDisplay;
  		strURL +="&fieldNameActual="+fieldNameActual;
  		strURL +="&fieldNameOID="+fieldNameOID; //XSSOK 
		boolean isCloud = UINavigatorUtil.isCloud(context);
		if(isCloud) {
			strURL = strURL+"&source=usersgroup&rdfQuery=[ds6w:type]:(Group)&HelpMarker=emxhelpsearch&allGroups=true"; //XSSOK
		}
  		%>
		<script language="javascript">
			var url = "<%=strURL%>";	<%-- XSSOK --%>
			url = url + "&frameNameForField="+parent.name;
			showModalDialog(url);
		</script>
  		<%
  	  }else if("programProjectsSummaryReport".equalsIgnoreCase(strMode)){
  		String objectId = emxGetParameter(request, "objectId");
  		String url = "../programcentral/emxProgramCentralDashboardProjects.jsp?collapse=3&header=emxProgramCentral.String.ProjectsDashboard&HelpMarker=emxhelpprojectsummaryreport&objectId="+objectId;
  		%>
	  	    <html>
			 <body>
			 		<div id="pageHeadDiv">
	    				<table>
					        <tbody><tr><td class="page-title"></td>
					                   <td class="functions">
					                    <table>
					                    <tbody><tr><td class="progress-indicator">
					                            <div style="visibility: hidden;" id="imgProgressDiv"></div>
					                    </td></tr></tbody>
					                    </table>
					                    </td>
					        </tr></tbody>
	    				</table>
				    <div id="divToolbarContainer" class="toolbar-container"></div>
					</div>
			 </body>
		 	</html>
  	    
  		 	<script language="javascript" type="text/javaScript">
	    		 	var topFrame = findFrame(getTopWindow(), "content");
	    		 	if(topFrame){
	    		 		turnOnProgress();
	    		 	}
	    		 	document.location.href = "<%=url%>";
  		 	</script>
  		  <%
  	   }
%>

<%@include file = "../emxUICommonEndOfPageInclude.inc" %>
<%@include file = "../components/emxComponentsDesignBottomInclude.inc"%>
