

<%-- Common Includes --%>
<%@page import="com.dassault_systemes.enovia.e6wv2.foundation.db.ContextUtil"%>
<%@page import="com.dassault_systemes.enovia.tskv2.ProjectSequence"%>
<%@page import="com.matrixone.apps.common.TaskDateRollup"%>
<%@page import="jakarta.json.JsonObjectBuilder"%>
<%@page import="jakarta.json.Json"%>
<%@page import="jakarta.json.JsonObject"%>
<%@page import="jakarta.json.JsonObjectBuilder"%>
<%@page import="jakarta.json.JsonArrayBuilder"%>
<%@page import="java.lang.reflect.Method"%>
<%@page import="com.matrixone.apps.program.Currency"%>
<%@page import="com.matrixone.apps.common.WorkCalendar"%>
<%@page import="com.matrixone.apps.program.fiscal.Helper"%>
<%@page import="com.matrixone.apps.common.Search"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Collections"%>
<%@page import=" com.dassault_systemes.enovia.e6wv2.foundation.db.ObjectUtil"%>

<%@ page import="java.util.HashMap,java.util.Date"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="com.matrixone.apps.domain.util.MessageUtil"%>

<%@include file="emxProgramGlobals2.inc"%>
<%@include file="../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file="../emxUICommonAppInclude.inc"%>
<%@ include file = "../emxUICommonHeaderBeginInclude.inc" %>
<%@include file = "../emxUICommonHeaderEndInclude.inc" %>
<%@include file = "../common/emxUIConstantsInclude.inc"%>

<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@page import="matrix.db.MQLCommand"%>
<%@page import="com.matrixone.apps.common.Company,matrix.util.StringList" %>
<%@page import="com.matrixone.apps.program.ProgramCentralUtil"%>
<%@page import="com.matrixone.apps.domain.util.MapList"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="com.matrixone.apps.common.ProjectManagement"%>

<%@page import="java.util.Enumeration"%>
<%@page import="com.matrixone.apps.program.ProgramCentralConstants"%>
<%@page import="com.matrixone.apps.domain.util.XSSUtil"%>
<%@page import="com.matrixone.apps.domain.util.FrameworkUtil"%>
<%@page import="com.matrixone.apps.domain.util.EnoviaResourceBundle"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.matrixone.apps.program.FTE"%>
<%@page import="com.matrixone.apps.program.ResourceRequest"%>
<%@page import="com.matrixone.apps.program.Question"%>

<%@page import="com.matrixone.apps.program.fiscal.CalendarType"%>
<%@page import="com.matrixone.apps.program.fiscal.Interval"%>
<%@page import="com.matrixone.apps.program.fiscal.Helper"%>
<%@page import="com.dassault_systemes.enovia.dpm.ProjectService"%>
<%@page import="com.matrixone.apps.program.ProgramCentralUtil"%>

<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>
<jsp:useBean id="formBean" scope="session" class="com.matrixone.apps.common.util.FormBean"/>
<SCRIPT language="javascript" src="../common/scripts/emxUICore.js"></SCRIPT>
<script language="javascript" src="../common/scripts/emxUIConstants.js"></script>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script src="../programcentral/emxProgramCentralUIFormValidation.js" type="text/javascript"></script>

<%
	 String projectType = DomainConstants.TYPE_PROJECT_SPACE;
	 String sLanguage = request.getHeader("Accept-Language");
	 String languageStr = context.getSession().getLanguage();
	 String strMode = emxGetParameter(request, "mode");
	 strMode = XSSUtil.encodeURLForServer(context, strMode);
     String objId = emxGetParameter(request, "objectId");
     objId = XSSUtil.encodeURLForServer(context, objId);
     String contentURL = DomainObject.EMPTY_STRING;
     
     if("refreshStructure".equals(strMode)) {
    		%>
    		<script language="javascript">
			   var detailsDisplay = findFrame(getTopWindow(), "PMCProjectRisk");
			   if(null==detailsDisplay){
					detailsDisplay=findFrame(getTopWindow().parent.window, "detailsDisplay");
					var WBSFrame=findFrame(detailsDisplay, "PMCWBS");
					if(null==WBSFrame){
					detailsDisplay.location.href = detailsDisplay.location.href;
				}else{
						WBSFrame.emxEditableTable.refreshStructureWithOutSort();
					}
				}else{
				    detailsDisplay.refreshSBTable();
				}
    	     </script>
    		<%

     }
			else if ("CreateSubQuestion".equalsIgnoreCase(strMode)) 	{
	    		String selectedNodeId = emxGetParameter(request, "emxTableRowId");
				String objectId = emxGetParameter(request, "objectId");
	    		String parentId = emxGetParameter(request, "objectId");
				if(UIUtil.isNotNullAndNotEmpty(selectedNodeId)){
					objectId = (String)(ProgramCentralUtil.parseTableRowId(context,selectedNodeId)).get("objectId");
	    		//String rowId = (String)(ProgramCentralUtil.parseTableRowId(context,selectedNodeId)).get("rowId");
	    		}
	    		objectId = XSSUtil.encodeURLForServer(context, objectId);
	    		parentId = XSSUtil.encodeURLForServer(context, parentId);
				
				String relTypeTemp = "relationship_Question";
				DomainObject selectedDomainObj = new DomainObject(objectId);
				if(selectedDomainObj.isKindOf(context,DomainObject.TYPE_PROJECT_TEMPLATE) || selectedDomainObj.isKindOf(context,DomainObject.TYPE_QUESTION)){
				if(selectedDomainObj.isKindOf(context,DomainObject.TYPE_PROJECT_TEMPLATE)){
					relTypeTemp = "relationship_ProjectQuestion";
				}
				String strURL = "../common/emxCreate.jsp?type=type_Question&typeChooser=false&nameField=keyin&form=PMCCreateQuestionForm&header=emxProgramCentral.Common.CreateQuestion&HelpMarker=emxhelpquestioncreatedialog&findMxLink=false&submitAction=refreshCaller&relationship="+relTypeTemp+"&direction=From&showPageURLIcon=false&postProcessJPO=emxQuestionBase:changeRevision&showApply=true&showApply=true&suiteKey=ProgramCentral&StringResourceFileId=emxProgramCentralStringResource&SuiteDirectory=programcentral&objectId="+objectId+"&parentOID="+parentId+"&widgetId=null&=undefined&targetLocation=slidein&timeStamp=1647316877508&openerFrame=detailsDisplay&createJPO=emxQuestion:createNewQuestion";
	    		
				%>
      <script language="javascript">
    <%-- XSSOK--%>
    var strUrl = "<%=strURL%>";
      window.location.href = strUrl;
       </script>
	<%
				}
				else{
					%>
          	<script language="javascript" type="text/javaScript">
         	alert("Please select either Project Template or Question to create new Question");
			getTopWindow().closeSlideInDialog();
         	</script>
         	<%
				}
				
	    	}
	else if ("GetSubQuestions".equalsIgnoreCase(strMode)){
		
		String responseDataGivenByUser = (String)emxGetParameter(request, "selectedOptionArr");
 CacheUtil.setCacheObject(context, "dpm_questionResponseCachedData", responseDataGivenByUser);
		
		String objectId = (String)emxGetParameter(request, "objectId");
		String selectedResponse = (String)emxGetParameter(request, "selectedResponse");
		Map argsMapTemp = new HashMap(1);
		argsMapTemp.put("objectId",objectId);
		String[] argsMapTempPacked = JPO.packArgs(argsMapTemp);
		ProjectSpace tempProjectSpaceObj = new ProjectSpace();
		MapList tempMapList = JPO.invoke(context, "emxProjectSpaceBase", null, "getActiveQuestionList", argsMapTempPacked , MapList.class);
		
		JsonArrayBuilder subQueJSONArr = Json.createArrayBuilder();
		JsonObjectBuilder subQueJSONObj = Json.createObjectBuilder();
		
		String xmlMessage = "";
		for(int tempMapListIndex = 0;tempMapListIndex<tempMapList.size();tempMapListIndex++){
			Map tempMap = (Map) tempMapList.get(tempMapListIndex);
			String taskTransfer = (String) tempMap.get("attribute[Task Transfer]");
			if(taskTransfer.equalsIgnoreCase(selectedResponse)){
				String questionId = (String) tempMap.get("id");
				String questionDesc = (String) tempMap.get("description");
				String questionName = (String) tempMap.get("name");
				
				subQueJSONObj.add("id",questionId);
				subQueJSONObj.add("description",questionDesc);
				subQueJSONObj.add("name",questionName);
				subQueJSONArr.add(subQueJSONObj);
				xmlMessage += "<mxRoot><action><![CDATA[add]]></action>";
				xmlMessage +="<data status=\"committed\" fromRMB=\"false\">";
				xmlMessage +="<item oid=\""+questionId+"\" relId=\"\" pid=\"" + objectId + "\"/>";
				xmlMessage +="</data></mxRoot>";
			}
		}
		
		out.clear();
		//out.write(subQueJSONArr.build().toString());
		out.write(xmlMessage.toString());
		return;
	}
			
	 else if ("manageDependencies".equalsIgnoreCase(strMode)) {
		String selectedTaskId = emxGetParameter(request, "objectId");
		selectedTaskId = XSSUtil.encodeURLForServer(context, selectedTaskId);
		String strURL = "../common/emxIndentedTable.jsp?table=PMCTaskDependency&toolbar=PMCManageTaskDependenciesActionToolbar&rowGroupingColumnNames=Name&freezePane=Name&selection=multiple&header=emxProgramCentral.Common.DependencyHeading&program=emxTaskBase:getTaskDependencies&HelpMarker=emxhelpeditdepend&suiteKey=ProgramCentral&SuiteDirectory=programcentral&StringResourceFileId=emxProgramCentralStringResource&emxSuiteDirectory=programcentral";
		if(ProgramCentralUtil.isNotNullString(objId))
			strURL = strURL + "&projectId=" + XSSUtil.encodeForURL(context, objId);
		strURL = strURL + "&objectId=" + XSSUtil.encodeForURL(context, selectedTaskId);
		strURL = strURL + "&customize=false&rowGrouping=false&Export=false&multiColumnSort=false&showPageURLIcon=false&displayView=details&showClipboard=false&objectCompare=false&massUpdate=false&findMxLink=false&autoFilter=false&mode=refreshWBS&showRMB=false";

%>
   <script language="javascript">
 <%-- XSSOK--%>
    var strUrl = "<%=strURL%>";
     var manageDependencies = findFrame(getTopWindow(),"PMCWBSManageDependencies");
     manageDependencies.location.href = strUrl;
   </script>
 <%
  	}

  	else if ("addInternalDependencies".equalsIgnoreCase(strMode))
  	{
  		String taskId = emxGetParameter(request, "objectId");
  		taskId = XSSUtil.encodeURLForServer(context, taskId);
  		String projectId = emxGetParameter(request, "projectId");
  		projectId = XSSUtil.encodeURLForServer(context, projectId);

 		String strURL = "../common/emxIndentedTable.jsp?table=PMCAddTaskDependencyTable&freezePane=Name&selection=multiple&header=emxProgramCentral.Dependency.AssignWBSTaskDependency&expandProgram=emxTask:getWBSIndependentTaskList&toolbar=PMCAddTaskDependenciesActionToolbar&HelpMarker=emxhelpdependencyadddialog&suiteKey=ProgramCentral&SuiteDirectory=programcentral&StringResourceFileId=emxProgramCentralStringResource&emxSuiteDirectory=programcentral&hideRootSelection=true";
  		strURL = strURL + "&objectId=" + XSSUtil.encodeForURL(context, projectId);
  		strURL = strURL + "&selectedTaskId=" + XSSUtil.encodeForURL(context, taskId);
  		strURL = strURL + "&customize=true&rowGrouping=false&Export=false&multiColumnSort=false&displayView=details&showClipboard=false&objectCompare=false&massUpdate=false&findMxLink=false&PrinterFriendly=false&autoFilter=false&expandLevelFilter=true&sortColumnName=ID&uiType=structureBrowser&mode=refreshWBS&showRMB=false";
  	    session.setAttribute("rootObjectId",projectId);
%>
      <script language="javascript">
   <%-- XSSOK--%>
    var strUrl = "<%=strURL%>";
     var internalTaskDependencies = findFrame(getTopWindow(),"PMCAddInternalTaskDependencies");
     internalTaskDependencies.location.href = strUrl;
       </script>
<%
  	}

  	else if ("addExternalDependencies".equalsIgnoreCase(strMode))	{
  		String selectedTaskId = emxGetParameter(request, "objectId");
  		String strProjectId = emxGetParameter(request, "projectId");

  	String strURL = "../common/emxIndentedTable.jsp?table=PMCAddExternalTaskDependencyTable&freezePane=Title&selection=single&header=eServiceSuiteProgramCentral.ExternalCrossProjectDependencyStep1.heading&program=emxProjectSpace:getExternalProjects&HelpMarker=emxhelpprojectcopypage2&suiteKey=ProgramCentral&SuiteDirectory=programcentral&StringResourceFileId=emxProgramCentralStringResource&emxSuiteDirectory=programcentral";
  		strURL = strURL + "&customize=false";
  		strURL = strURL + "&projectId=" + XSSUtil.encodeForURL(context,strProjectId);
  		strURL = strURL + "&rowGrouping=false&toolbar=PMCOpenExternalProjectToolBar&Export=false&multiColumnSort=false&showPageURLIcon=false&displayView=details&showClipboard=false&objectCompare=false&massUpdate=false&findMxLink=false&PrinterFriendly=false&autoFilter=false&triggerValidation=false&massPromoteDemote=false&externalDependency=true";
  		strURL = strURL + "&selectedTaskId=" + XSSUtil.encodeForURL(context,selectedTaskId);
  		strURL = strURL + "&showRMB=false";
  %>
      <script language="javascript">
    <%-- XSSOK--%>
   var strUrl = "<%=strURL%>";
     var externalTaskDependencies = findFrame(getTopWindow(),"PMCAddExternalProjectTaskDependencies");
     externalTaskDependencies.location.href = strUrl;

       </script>
<%
  	}

	else if ("addExternalTasks".equalsIgnoreCase(strMode))
  	{
  		String selectedTaskId = emxGetParameter(request, "selectedTaskId");
  		selectedTaskId = XSSUtil.encodeURLForServer(context, selectedTaskId);
  		String[] projectIds = emxGetParameterValues(request, "emxTableRowId");
  		String projectId = "";

  		  if(projectIds!=null) {
  		  for(int i=0;i<projectIds.length;i++){
            StringList slTemp = FrameworkUtil.split(projectIds[i].substring( (projectIds[i].indexOf("|"))+1 ),"|");
          	projectId = (String)slTemp.get(0);
  			}
  		}

  	     if( projectIds == null){
         	%>
          	<script language="javascript" type="text/javaScript">
         	alert("<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Project.SelectProject</emxUtil:i18nScript>");
         	</script>
         	<%
         	return;
         }


  		String strURL = "../common/emxIndentedTable.jsp?table=PMCAddTaskDependencyTable&freezePane=Name&selection=multiple&header=emxProgramCentral.Dependency.AssignWBSTaskDependency&toolbar=PMCAddTaskDependenciesActionToolbar&HelpMarker=emxhelpdependencyadddialog&suiteKey=ProgramCentral&expandProgram=emxTask:getWBSIndependentTaskList&SuiteDirectory=programcentral&StringResourceFileId=emxProgramCentralStringResource&emxSuiteDirectory=programcentral&hideRootSelection=true";
  		strURL = strURL + "&objectId=" + XSSUtil.encodeForURL(context, projectId);
  		strURL = strURL + "&selectedTaskId=" + XSSUtil.encodeForURL(context, selectedTaskId);
  		strURL = strURL + "&customize=false&rowGrouping=false&Export=false&multiColumnSort=false&showPageURLIcon=false&displayView=details&showClipboard=false&objectCompare=false&massUpdate=false&findMxLink=false&PrinterFriendly=false&autoFilter=false&expandLevelFilter=true&sortColumnName=ID&uiType=structureBrowser&mode=refreshWBS";


  %>
      <script language="javascript">
    <%-- XSSOK--%>
    var strUrl = "<%=strURL%>";
      window.parent.location.href = strUrl;
       </script>
<%

  	}
	else if("cloneAsDeliverableTemplate".equalsIgnoreCase(strMode))
	{
		String selectedDeliverableId = emxGetParameter(request, "objectId");
		DeliverableIntent.saveAsDeliverableTemplate(context, selectedDeliverableId);
		%>
		<script language="javascript" type="text/javaScript">
		alert("Saved as Deliverable Template");
	    </script>
	    <%
	}
	else if("deleteDeliverableTemplate".equalsIgnoreCase(strMode))
	{
		String[] deliverableTemplateIds = emxGetParameterValues(request,"emxTableRowId");
		String sObjId = "";
		String sTempRowId = "";
		String partialXML = "";
		String[] strObjectIDArr    = new String[deliverableTemplateIds.length];
		for(int i=0; i<deliverableTemplateIds.length; i++)
		{
			String sTempObj = deliverableTemplateIds[i];
			Map mParsedObject = ProgramCentralUtil.parseTableRowId(context,sTempObj);
			sObjId = (String)mParsedObject.get("objectId");
			strObjectIDArr[i] = sObjId;
			sTempRowId = (String)mParsedObject.get("rowId");
			partialXML += "<item id=\"" + XSSUtil.encodeForURL(context, sTempRowId) + "\" />";
		}
		DomainObject.deleteObjects(context,strObjectIDArr) ;
		String xmlMessage = "<mxRoot>";
		String message = "";
		xmlMessage += "<action refresh=\"true\" fromRMB=\"\"><![CDATA[remove]]></action>";
		xmlMessage += partialXML;
		xmlMessage += "<message><![CDATA[" + message + "]]></message>";
		xmlMessage += "</mxRoot>";
		 %>
		 <script type="text/javascript" language="JavaScript">
	  <%-- XSSOK--%>
	 window.parent.removedeletedRows('<%= xmlMessage %>');
         window.parent.closeWindow();
         </script>
		 <%
	}
	else if("deleteDeliverable".equalsIgnoreCase(strMode))
	{
		String[] deliverableIds = emxGetParameterValues(request,"emxTableRowId");
		String strDeliverableId = "";
		String sTempRowId = ProgramCentralConstants.EMPTY_STRING;
		String partialXML = ProgramCentralConstants.EMPTY_STRING;
		String[] strObjectIDArr    = new String[deliverableIds.length];
		for(int i = 0; i < deliverableIds.length; i++)
		{
			String sTempObj = deliverableIds[i];
			Map mParsedObject = ProgramCentralUtil.parseTableRowId(context,sTempObj);
			strDeliverableId = (String)mParsedObject.get("objectId");
			DomainObject dmoObject = DomainObject.newInstance(context,strDeliverableId);
		    String strPrjObjId = dmoObject.getInfo(context, "from[Governing Project].to.id");
		    if(strPrjObjId != null)
		    {
		    	ProjectSpace project = new ProjectSpace(strPrjObjId);
		     	((BusinessObject)project).remove(context);
		    }
			strObjectIDArr[i] = strDeliverableId;
			sTempRowId = (String)mParsedObject.get("rowId");
			partialXML += "<item id=\"" + XSSUtil.encodeForURL(context, sTempRowId) + "\" />";
		}
		DomainObject.deleteObjects(context,strObjectIDArr) ;
		String xmlMessage = "<mxRoot>";
		String message = ProgramCentralConstants.EMPTY_STRING;
		xmlMessage += "<action refresh=\"true\" fromRMB=\"\"><![CDATA[remove]]></action>";
		xmlMessage += partialXML;
		xmlMessage += "<message><![CDATA[" + message + "]]></message>";
		xmlMessage += "</mxRoot>";
		 %>
		 <script type="text/javascript" language="JavaScript">
	  <%-- XSSOK--%>
	 window.parent.removedeletedRows('<%= xmlMessage %>');
         window.parent.closeWindow();
         </script>
		 <%
	}
	else if ("isPortalMode".equalsIgnoreCase(strMode)) 	{
		boolean isModifyOp = false;
		projectType = DomainConstants.TYPE_PROJECT_SPACE;
		String projectTemplateType = DomainConstants.TYPE_PROJECT_TEMPLATE;
		String projectConceptType= DomainConstants.TYPE_PROJECT_CONCEPT;

		String selectedids[] = request.getParameterValues("emxTableRowId");
		StringList slIds = com.matrixone.apps.domain.util.FrameworkUtil.split(selectedids[0], "|");

		Map <String,String>selectedRowIdMap = ProgramCentralUtil.parseTableRowId(context,selectedids[0]);
		String selectedTaskid = selectedRowIdMap.get("objectId");
		String strRowLevel = selectedRowIdMap.get("rowId");

		if(ProgramCentralUtil.isNullString(selectedTaskid)){
			selectedTaskid = (String) slIds.get(0);
			isModifyOp = true;
		}

		Map objectInfoMap =  ProgramCentralUtil.isPortalMode(context, selectedTaskid);
	    String isProjectSpace = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
	    String isProjectTemplate = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE);
	    String isProjectConcept = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_CONCEPT);
	    String selectedTaskStates = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_CURRENT);
	    boolean hasRelObj = (Boolean)objectInfoMap.get("hasRelObj");
	    String taskPolicy = (String)objectInfoMap.get("taskPolicy");

	 	if(strRowLevel.equals("0") && ("TRUE".equalsIgnoreCase(isProjectSpace) || "TRUE".equalsIgnoreCase(isProjectTemplate) || "TRUE".equalsIgnoreCase(isProjectConcept))){
				 %>
				 <script language="javascript" type="text/javaScript">
					 alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Alert.CannotPerform</framework:i18nScript>");
				 </script>
				 <%
				 return;
		}


		if(selectedTaskStates.equals(ProgramCentralConstants.STATE_PROJECT_SPACE_COMPLETE)){
			%>
            <script language="javascript" type="text/javaScript">
             var msg = "<framework:i18nScript localize="i18nId">emxProgramCentral.Dependency.TaskComplete</framework:i18nScript>";
             alert (msg);
	            </script>
	             <%
	         return;
		}
				
		if(selectedTaskStates.equals(ProgramCentralConstants.STATE_PROJECT_SPACE_REVIEW) ){
			%>
                   <script language="javascript" type="text/javaScript">
                    var msg = "<framework:i18nScript localize="i18nId">emxProgramCentral.Project.ParentInState1</framework:i18nScript>" +
                     " <%=i18nNow.getStateI18NString(taskPolicy,ProgramCentralConstants.STATE_PROJECT_SPACE_ACTIVE,sLanguage)%> " +
                     "<framework:i18nScript localize="i18nId">emxProgramCentral.Project.TaskInState2</framework:i18nScript>";
                    alert ( msg );
      	            </script>
     	             <%
     	         return;
                }

	     if(hasRelObj){
	    	 %>
	           <script language="javascript" type="text/javaScript">
	           alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.TaskHasBeenMarkedForDeletion</framework:i18nScript>");
	           </script>
	           <%
	           return;
          }

		String strURL = "../common/emxPortal.jsp?portal=PMCWBSTaskDependencyPortal&showPageHeader=false";

		if(ProgramCentralUtil.isNotNullString(objId) && !isModifyOp){
			DomainObject task = DomainObject.newInstance(context,selectedTaskid);
			if(strRowLevel.equals("0") && task.isKindOf(context,DomainObject.TYPE_TASK_MANAGEMENT)){
	  			String projectId = task.getInfo(context, ProgramCentralConstants.SELECT_TASK_PROJECT_ID);
				strURL = strURL + "&projectId=" + XSSUtil.encodeForURL(context, projectId);
			}else{
				strURL = strURL + "&projectId=" + XSSUtil.encodeForURL(context, objId);
			}
		}else{
			DomainObject task = DomainObject.newInstance(context,selectedTaskid);
	  		String projectId = task.getInfo(context, ProgramCentralConstants.SELECT_TASK_PROJECT_ID);

	  		strURL = strURL + "&projectId=" + XSSUtil.encodeForURL(context, projectId);
	  		}

		strURL = strURL + "&objectId=" + XSSUtil.encodeForURL(context, selectedTaskid);

%>
  	  <script language="javascript">
    <%-- XSSOK--%>
	   var strUrl = "<%=strURL%>";
  	   showModalDialog(strUrl,1000,1000);
  	   </script>
 <%
  	}
	else if ("addPerson".equals(strMode)) {
		String parentOID = emxGetParameter( request, "parentOID");
		parentOID = XSSUtil.encodeURLForServer(context, parentOID);
		String url="../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=state_Active:USERROLE=External Project User,Project User&table=PMCCommonPersonSearchTable&mode=addMember&form=PMCCommonPersonSearchForm&selection=multiple&excludeOIDprogram=emxCommonPersonSearch:getMembersIdsToExclude&submitAction=refreshCaller&submitURL=../programcentral/emxProgramCentralCommonPersonSearchUtil.jsp?mode=addMember&&objectId="+XSSUtil.encodeForURL(context, objId)+"&parentOID="+XSSUtil.encodeForURL(context, parentOID);

 %>
       <script language="javascript">
      <%-- XSSOK--%>
   document.location.href ='<%=url%>';
  	   </script>
<%
}else if (ProgramCentralConstants.INSERT_EXISTING_PROJECT_ABOVE.equalsIgnoreCase(strMode)) {

		String currentFrame = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "portalCmdName"));
		String tableRowIdList[] = emxGetParameterValues(request,"emxTableRowId");
		String levelId = ProgramCentralConstants.EMPTY_STRING;
		if (tableRowIdList != null) {
			for (int i = 0; i < tableRowIdList.length; i++) {
				StringTokenizer strtk = new StringTokenizer(
						tableRowIdList[i], "|");
				int tokens = strtk.countTokens();
				if (tokens == 2) {
					for (int j = 0; j < tokens; j++) {
						levelId = strtk.nextToken();
						if (levelId.equals("0")) {
%>
					<script language="javascript">
						if(parent.emxEditableTable.checkDataModified()){
	    					alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
						} else{
			        	  	alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Project.CannotInsertOnRoot</framework:i18nScript>");
			        	  	getTopWindow().window.closeWindow();
						}
	 		        </script>
<%
							return;
						}
					}
				}
			}
		}
		String requiredTaskId = emxGetParameter(request,"emxTableRowId");

		Map mpRow=null;
		String requiredTaskId1 = ProgramCentralConstants.EMPTY_STRING;
		String requiredTaskId2 = ProgramCentralConstants.EMPTY_STRING;
		if (null != requiredTaskId) {

			 mpRow = ProgramCentralUtil.parseTableRowId(context,requiredTaskId);
			 requiredTaskId1 = (String) mpRow.get("objectId");
			 requiredTaskId2 = (String) mpRow.get("parentOId");
			requiredTaskId = requiredTaskId1 + "|" + requiredTaskId2;

		}
		String strSelectedTaskObjID=(String) mpRow.get("objectId");
		String strSelectedParentObjID=(String) mpRow.get("parentOId");

		String SELECT_PARENT_STATE = "to[" + ProgramCentralConstants.RELATIONSHIP_SUBTASK + "].from.current";
		String SELECT_PARENT_ID = "to[" + ProgramCentralConstants.RELATIONSHIP_SUBTASK + "].from.id";
		String SELECT_IS_MARK_AS_DELETED = "to[" + ProgramCentralConstants.RELATIONSHIP_SUBTASK + "].from.to["+DomainConstants.RELATIONSHIP_DELETED_SUBTASK+"]";

		StringList busSelect = new StringList(6);
		busSelect.addElement(ProgramCentralConstants.SELECT_PROJECT_ID);
		busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
		busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
		busSelect.addElement(ProgramCentralConstants.SELECT_IS_TASK_MANAGEMENT);
		busSelect.addElement(SELECT_PARENT_STATE);
		busSelect.addElement(SELECT_PARENT_ID);
		busSelect.addElement(SELECT_IS_MARK_AS_DELETED);

		MapList objectInfoList 	= ProgramCentralUtil.getObjectDetails(context, new String[]{strSelectedTaskObjID}, busSelect);
		Map selectedObjInfo 	= (Map)objectInfoList.get(0);

		String parentObjState = (String)selectedObjInfo.get(SELECT_PARENT_STATE);
		String isParentMarkAsDeleted 	= (String) selectedObjInfo.get(SELECT_IS_MARK_AS_DELETED);

		if("true".equalsIgnoreCase(isParentMarkAsDeleted)){
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
		}else if(DomainObject.STATE_PROJECT_SPACE_REVIEW.equalsIgnoreCase(parentObjState)||
				DomainObject.STATE_PROJECT_SPACE_COMPLETE.equalsIgnoreCase(parentObjState)){

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
			}else{
			String isTaskMgt = (String)selectedObjInfo.get(ProgramCentralConstants.SELECT_IS_TASK_MANAGEMENT);
			String isProject = (String)selectedObjInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			String isProjectConcept = (String)selectedObjInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
			String projectId = DomainObject.EMPTY_STRING;

			if("true".equalsIgnoreCase(isTaskMgt)){
				projectId = (String)selectedObjInfo.get(ProgramCentralConstants.SELECT_PROJECT_ID);

			}else if("true".equalsIgnoreCase(isProject) || "TRUE".equalsIgnoreCase(isProjectConcept)){
				String parentObjId = (String)selectedObjInfo.get(SELECT_PARENT_ID);
				DomainObject obj = DomainObject.newInstance(context,parentObjId);
				projectId = obj.getInfo(context, ProgramCentralConstants.SELECT_PROJECT_ID);

				if(ProgramCentralUtil.isNullString(projectId)){
					projectId = requiredTaskId2;
           		}
        }

			ProjectSpace project 	= new ProjectSpace(projectId);
			String isExperimentProj = project.getInfo(context, ProgramCentralConstants.SELECT_KINDOF_EXPERIMENT_PROJECT);
			boolean isExpProj 		= "true".equalsIgnoreCase(isExperimentProj)?true:false;
			StringList excludeOIDList = ProgramCentralUtil.getProjectExclusionList(context, project,isExpProj);

			if(!excludeOIDList.contains(projectId)){
				excludeOIDList.addElement(projectId);
        }

			String excludeOIDs = StringUtil.join(excludeOIDList,ProgramCentralConstants.COMMA);

			String strURL = "../common/emxFullSearch.jsp?field=TYPES=type_ProjectSpace,type_ProjectConcept:Type!=type_Experiment,type_ProjectBaseline:Current!=policy_ProjectSpace.state_Complete,policy_ProjectSpaceHoldCancel.state_Cancel,policy_ProjectSpaceHoldCancel.state_Hold,policy_ProjectSpace.state_Archive&table=PMCProjectSummaryForProjects&selection=multiple&toolbar=&editLink=true&submitURL=../programcentral/emxprojectCreateWizardAction.jsp&fromProgram=null&fromWBS=false&fromAction=false&wizType=AddExistingSubtaskProject&addType=Sibling&copyExistingSearchShowSubTypes=true&pageName=fromAddExistingSubProject&p_button=Next&fromSubProjects=true&calledMethod=submitInsertProject&PMCWBSQuickTaskTypeToAddBelow=Task&portalMode=true&copyExistingProjectType=type_ProjectManagement&fromClone=null&wbsForm=false&insertExistingProjectAboveMode=insertExistingProjectAbove&suiteKey=ProgramCentral&HelpMarker=emxhelpinsertprojectsastasks";
			strURL = strURL + "&objectId=" + XSSUtil.encodeForURL(context, objId) + "&requiredTaskId="+ XSSUtil.encodeForURL(context, requiredTaskId) + "&parentProjectId=" + XSSUtil.encodeForURL(context, objId) + "&portalCmdName="+currentFrame+ "&requiredTaskId1="+ XSSUtil.encodeForURL(context, requiredTaskId1)+ "&requiredTaskId2="+ XSSUtil.encodeForURL(context, requiredTaskId2);
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

    	} else if (ProgramCentralConstants.ADD_EXISTING_RELATED_PROJECTS.equalsIgnoreCase(strMode)) {

    		String strURL = "../common/emxFullSearch.jsp?field=TYPES=type_ProjectSpace,type_ProjectConcept:Type!=type_Experiment,type_ProjectBaseline:Current!=policy_ProjectSpace.state_Complete,policy_ProjectSpaceHoldCancel.state_Cancel,policy_ProjectSpaceHoldCancel.state_Hold,policy_ProjectSpace.state_Archive:id!=" + objId + "&table=PMCProjectSummaryForProjects&selection=multiple&toolbar=&editLink=true&submitURL=../programcentral/emxprojectCreateWizardAction.jsp&fromProgram=null&fromWBS=false&fromAction=false&wizType=AddExistingRelProject&addType=Child&copyExistingSearchShowSubTypes=true&pageName=fromAddExistingRelProject&p_button=Next&fromRelatedProjects=true&copyExistingProjectType=type_ProjectManagement&fromClone=false&wbsForm=false&addRelatedProjectMode=addRelatedProjectMode";
    		strURL = strURL + "&objectId=" + XSSUtil.encodeForURL(context, objId) + "&parentOID=" + XSSUtil.encodeForURL(context, objId)+ "&parentProjectId="+XSSUtil.encodeForURL(context, objId);
%>
	 <script language="javascript">
  <%-- XSSOK--%>
	  var strUrl ="<%=strURL%>";
  	 //window.parent.location.href = strUrl;
  	 showModalDialog(strUrl);

  	   </script>

<%
 	}else if("addExistingProjectBelow".equalsIgnoreCase(strMode)){

 		String strURL = "../common/emxFullSearch.jsp?field=TYPES=type_ProjectSpace:Type!=type_Experiment,type_ProjectBaseline&table=PMCProjectSummaryForProjects&selection=multiple&toolbar=&editLink=true&submitURL=../programcentral/emxProgramCentralUtil.jsp&mode=addProjectToProgram";
 		strURL = strURL + "&objectId=" + XSSUtil.encodeForURL(context, objId)+"&excludeOIDprogram=emxProgramCentralUtil:getExcludeOIDForProjectSearchInProgram" ;
			%>
			 <script language="javascript">
		  		<%-- XSSOK--%>
				var strUrl ="<%=strURL%>";
		  	 	showModalDialog(strUrl,1000,1000);
		  	 </script>
			<%



 	}else if("addProjectToProgram".equals(strMode)) {
	    String programId = emxGetParameter(request, "objectId");
	    String[] ids = emxGetParameterValues(request, "emxTableRowId");
	    String portalCommandName = emxGetParameter(request, "portalCmdName");

	    ProjectSpace project =(ProjectSpace)DomainObject.newInstance(context,
				ProgramCentralConstants.TYPE_PROJECT_SPACE,DomainConstants.PROGRAM);

		   try{
			   for(int i =0, len = ids.length; i<len;i++){
			    	StringList projectIdList = StringUtil.split(ids[i], "|");

			    	if( projectIdList.size() >= 1 ){
			    		String projectId = (String)projectIdList.get(0);
			    		project.setId(projectId);
			    		//project.setProgram(context, programId);
			    		project.addRelatedObject(context, new RelationshipType(DomainConstants.RELATIONSHIP_PROGRAM_PROJECT), true, programId);
			    	}
 	}
		   }catch(Exception e){
			   e.printStackTrace();
		   }

				%>
					<script language="javascript" type="text/javaScript">
						
						<%
						if(!ProgramCentralUtil.is3DSearchEnabled(context)){
						%>
							getTopWindow().close();
							getTopWindow().getWindowOpener().parent.location.reload();
						<%}else{%> 
						 	var frame = "<%=portalCommandName%>";
				    	  	var topFrame = findFrame(getTopWindow(), frame);
				    	 	if(null == topFrame){
				    		  topFrame = findFrame(getTopWindow(),"detailsDisplay");
				    	  	}
						 	
						if (topFrame != null) {
							topFrame.location.href = topFrame.location.href;                        
						}else{
							parent.location.href = parent.location.href;
						}
                <%}%>
		            </script>
		       		<%

 	}else if("deletequestion".equals(strMode)) {
    		String[] questionIds = emxGetParameterValues(request,"emxTableRowId");
    		String sObjId = "";
    		String sTempRowId = "";
    		String partialXML = "";
    		String[] strObjectIDArr    = new String[questionIds.length];
    		for(int i=0; i<questionIds.length; i++)
    		{
    			String sTempObj = questionIds[i];
				Map mParsedObject = ProgramCentralUtil.parseTableRowId(context,sTempObj);
				sObjId = (String)mParsedObject.get("objectId");
				strObjectIDArr[i] = sObjId;
				sTempRowId = (String)mParsedObject.get("rowId");
				partialXML += "<item id=\"" + XSSUtil.encodeForURL(context, sTempRowId) + "\" />";
    		}
    		DomainObject.deleteObjects(context,strObjectIDArr) ;
    		String xmlMessage = "<mxRoot>";
    		String message = "";
    		xmlMessage += "<action refresh=\"true\" fromRMB=\"\"><![CDATA[remove]]></action>";
    		xmlMessage += partialXML;
    		xmlMessage += "<message><![CDATA[" + message + "]]></message>";
    		xmlMessage += "</mxRoot>";
    		 %>
    		 <script type="text/javascript" language="JavaScript">
      <%-- XSSOK--%>
		 window.parent.removedeletedRows('<%= xmlMessage %>');
             window.parent.closeWindow();
             </script>
    		 <%

    	}
    	else if("deletequestionset".equals(strMode)) {
    		String[] questionsetIds = emxGetParameterValues(request,"emxTableRowId");
    		String sObjId = "";
    		String sTempRowId = "";
    		String partialXML = "";
    		String[] strObjectIDArr    = new String[questionsetIds.length];
    		for(int i=0; i<questionsetIds.length; i++)
    		{
    			String sTempObj = questionsetIds[i];
				Map mParsedObject = ProgramCentralUtil.parseTableRowId(context,sTempObj);
				sObjId = (String)mParsedObject.get("objectId");
				strObjectIDArr[i] = sObjId;
				sTempRowId = (String)mParsedObject.get("rowId");
				partialXML += "<item id=\"" + XSSUtil.encodeForURL(context, sTempRowId) + "\" />";
    		}
    		DomainObject.deleteObjects(context,strObjectIDArr) ;
    		String xmlMessage = "<mxRoot>";
    		String message = "";
    		xmlMessage += "<action refresh=\"true\" fromRMB=\"\"><![CDATA[remove]]></action>";
    		xmlMessage += partialXML;
    		xmlMessage += "<message><![CDATA[" + message + "]]></message>";
    		xmlMessage += "</mxRoot>";
    		 %>
    		 <script type="text/javascript" language="JavaScript">
      <%-- XSSOK--%>
		 window.parent.removedeletedRows('<%= xmlMessage %>');
             window.parent.closeWindow();
             </script>
    		 <%

    	}
    	else if ("clearAll".equalsIgnoreCase(strMode)) 	{
%>
    	<script language="javascript">
    	parent.document.location.href = parent.document.location.href;
        </script>
<%
    	}
    	else if("deletedelivers".equals(strMode)) {
    		String[] deliversIds = emxGetParameterValues(request,"emxTableRowId");
    		String sObjId = ProgramCentralConstants.EMPTY_STRING;
    		String sRelId = ProgramCentralConstants.EMPTY_STRING;
    		String sTempRowId = ProgramCentralConstants.EMPTY_STRING;
    		String partialXML = ProgramCentralConstants.EMPTY_STRING;
    		String[] strObjectIDArr  = new String[deliversIds.length];
    		String[] strRelIdArr     = new String[deliversIds.length];
    		for(int i=0; i<deliversIds.length; i++)
    		{
    			String sTempObj = deliversIds[i];
				Map mParsedObject = ProgramCentralUtil.parseTableRowId(context,sTempObj);
				sObjId = (String)mParsedObject.get("objectId");
				sRelId = (String)mParsedObject.get("relId");
				strObjectIDArr[i] = sObjId;
				strRelIdArr[i] = sRelId;
				sTempRowId = (String)mParsedObject.get("rowId");
				partialXML += "<item id=\"" + XSSUtil.encodeForURL(context, sTempRowId) + "\" />";
    		}
    		DomainRelationship.disconnect(context,strRelIdArr);
            //DomainObject.deleteObjects(context,strObjectIDArr) ;
    		String xmlMessage = "<mxRoot>";
    		String message = "";
    		xmlMessage += "<action refresh=\"true\" fromRMB=\"\"><![CDATA[remove]]></action>";
    		xmlMessage += partialXML;
    		xmlMessage += "<message><![CDATA[" + message + "]]></message>";
    		xmlMessage += "</mxRoot>";
    		 %>
    		 <script type="text/javascript" language="JavaScript">
    	  <%-- XSSOK--%>
	 window.parent.removedeletedRows('<%= xmlMessage %>');
             window.parent.closeWindow();
             </script>
    		 <%
    	}
    	else if("AddDelivers".equalsIgnoreCase(strMode)) {
        	String DeliversID = DomainObject.EMPTY_STRING;
        	String sObjIDToConnect = DomainObject.EMPTY_STRING;
        	String sDeliverIntentObjId = DomainObject.EMPTY_STRING;
        	String sTableRowId[] = emxGetParameterValues( request, "emxTableRowId" );
        	StringList slObjIDToConnect = new StringList();
			for(int i=0; i<sTableRowId.length; i++){
				String sTempObj = sTableRowId[i];
				Map mParsedObject = ProgramCentralUtil.parseTableRowId(context,sTempObj);
				slObjIDToConnect.add(mParsedObject.get("objectId").toString());
				sDeliverIntentObjId = (String)mParsedObject.get("parentOId");
			}
			try {
				DeliverableIntent.connectDelivers(context, sDeliverIntentObjId, slObjIDToConnect);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			%>
			<script language="javascript" type="text/javaScript">
				 getTopWindow().window.getWindowOpener().location.href=getTopWindow().window.getWindowOpener().location.href;
				 getTopWindow().window.closeWindow();
			</script>
			<%
		}
    	else if("SearchDelivers".equalsIgnoreCase(strMode)) {
    		String targetSearchPage = DomainConstants.EMPTY_STRING;
    		String objectId = emxGetParameter( request, "objectId");
    		targetSearchPage = "../common/emxFullSearch.jsp?table=PMCDeliverableSearchSummary&cancelLabel=emxProgramCentral.Common.Close&suiteKey=ProgramCentral&selection=multiple&excludeOIDprogram=emxDeliverable:getExcludeOIDForDelivers&objectId="+XSSUtil.encodeForURL(context,objectId)+"&submitURL=../programcentral/emxProgramCentralUtil.jsp?mode=AddDelivers";
			%>
			<script language="javascript" type="text/javaScript">
		  <%-- XSSOK--%>
		var strUrl ="<%=targetSearchPage%>";
				document.location.href = strUrl;
			</script>
			<%
		}
    	else if ("submitResourceRequest".equalsIgnoreCase(strMode)){
    		String sErrMsg  = "";
    		String selectedids[] = request.getParameterValues("emxTableRowId");
    		StringList requestIdList = new StringList();
    		boolean isInvalidRequest = false;
    		//Extarct all request ids from selected ids.
    		for(String selectedId : selectedids){
    			StringList selectedIdList = FrameworkUtil.split(selectedId, "|");
    			if(selectedIdList.size() >=4){
    				String requestId = (String) selectedIdList.get(1);
    				requestId = XSSUtil.encodeURLForServer(context, requestId);
    				requestIdList.add(requestId);
    			}
    		}
    		if(!requestIdList.isEmpty()){
    	   		//Get all request ids in an array from stringlist
        		String[] requestIdArray = new String[requestIdList.size()];
        		requestIdList.toArray(requestIdArray);

        		//Get Types of all the ids in one shot.
        		StringList slSelectable = new StringList();
				String SELECT_REL_ATTRIBUTE_FTE = "to["+ DomainConstants.RELATIONSHIP_RESOURCE_PLAN+ "].attribute["+ DomainConstants.ATTRIBUTE_FTE + "]";
				slSelectable.add(ProgramCentralConstants.SELECT_ID);
				slSelectable.add(ProgramCentralConstants.SELECT_TYPE);
        		slSelectable.add(ProgramCentralConstants.SELECT_CURRENT);
        		slSelectable.add(SELECT_REL_ATTRIBUTE_FTE);

        		MapList infoMapList = DomainObject.getInfo(context, requestIdArray, slSelectable);
        		java.util.Iterator itrInfoMapList = infoMapList.iterator();

        		//Check if selected is a Resource Request that too in Create state or Rejected states with valid FTE.
        		while(itrInfoMapList.hasNext()){
        			Map objectInfoMap = (Map)itrInfoMapList.next();
        			String resourceRequestId = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_ID);
        			String type = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_TYPE);
                	String state = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_CURRENT);
        			String sFTE = (String)objectInfoMap.get(SELECT_REL_ATTRIBUTE_FTE);

        			//Check if selected object is Resource Request
        			if(!ProgramCentralConstants.TYPE_RESOURCE_REQUEST.equals(type)){
        				sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.SelectResourceRequest", languageStr);
        				isInvalidRequest = true;
        				break;
        			}

        			//Check if selected Resource Request is in Create and it is connected to a valid resource pool
        			if(ProgramCentralConstants.STATE_RESOURCE_REQUEST_CREATE.equals(state)){
        				String[] arrRequestId = {resourceRequestId};
        				sErrMsg = ResourceRequest.triggerCheckResourcePoolMessage(context, arrRequestId);
        				if(ProgramCentralUtil.isNotNullString(sErrMsg)){
        					isInvalidRequest = true;
            				break;
            			}
        			}
        			//Check if Request FTE is not zero
        			FTE oFTE = FTE.getInstance(context);
					if (ProgramCentralUtil.isNotNullString(sFTE)){
						oFTE = FTE.getInstance(context, sFTE);
					}
					Map mapFTEValues = null;
					mapFTEValues = oFTE.getAllFTE();
					boolean isValidFTE = false;
					int count = 0;
					if (null != mapFTEValues && !"null".equals(mapFTEValues) && !"".equals(mapFTEValues)){
						for (Iterator iter = mapFTEValues.keySet().iterator(); iter.hasNext();){
							String strTimeFrame = (String) iter.next();
							Double dFTEValue = 0D;
							dFTEValue = (Double) mapFTEValues.get(strTimeFrame);
							if (dFTEValue <= 0){
								count++;
							}
						}
							if(mapFTEValues.size()==count){
								isInvalidRequest = true;
								sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.ResourceRequest.InvalidRequestForSubmission", languageStr);
								break;
							}
						}
					}
    		}else{
    			sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.SelectResourceRequest", languageStr);
    			isInvalidRequest = true;
    		}
    		if(isInvalidRequest){
				%>
				<script language="javascript" type="text/javaScript">
				alert("<%= sErrMsg%>" );
				</script>
				<%
				return;
    		}else{
    			String strURL = "../common/emxForm.jsp?form=PMCResourceRequestSubmitForm&mode=edit&formHeader=emxProgramCentral.ResourcePlan.HeaderForSubmition&suiteKey=ProgramCentral&SuiteDirectory=programcentral&postProcessURL=../programcentral/emxProgramCentralResourceRequestUtil.jsp&submitAction=doNothing&submode=SubmitRequestComment&toolbar=null&HelpMarker=emxhelpresourcerequestsubmitcomment";
				strURL += "&objectId=" + requestIdList.get(0);
				String rowIds = "";
				for(int index=0; index<requestIdList.size(); index++){
					String nextId = (String) requestIdList.get(index);
					if(index == 0){
						rowIds += XSSUtil.encodeForURL(context, nextId);
					}else{
						rowIds += "," + XSSUtil.encodeForURL(context, nextId);
					}
				}
				strURL += "&rowIds=" + XSSUtil.encodeForURL(context, rowIds);
				%>
				<script language="javascript" type="text/javaScript">
	  <%-- XSSOK--%>
		  	   var url = "<%=strURL%>";
			  	 getTopWindow().showSlideInDialog(url,false);
				</script>
				<%
    		}
    	} else if("createSnapshot".equalsIgnoreCase(strMode)) {
    		String objectId = (String) emxGetParameter(request, "objectId");
   	  if(ProgramCentralUtil.isNotNullString(objectId)){
   		  String sPlanId = com.matrixone.apps.program.ProjectSpace.getGoverningProjectPlanId(context,objectId);
   		    if(!ProgramCentralUtil.isNullString(sPlanId)) {
   		    	Map mapObjInfo = ProgramCentralUtil.createSnapshot(context, sPlanId);
   %>
    <script language="javascript" type="text/javaScript">
     var topFrame = findFrame(getTopWindow(), "PMCProjectSnapshots");
    	if(topFrame != null)
    	{
    		topFrame.location.href = topFrame.location.href;
    	}
    </script>
   <%
   	}
   	  }
     }else if("deleteSnapshot".equalsIgnoreCase(strMode)){
    	 String objectId = (String) emxGetParameter(request, "objectId");
   	  if(ProgramCentralUtil.isNotNullString(objectId)){
   		  ProjectSnapshot dmoSnapshot = (ProjectSnapshot)ProjectSnapshot.newInstance(context, ProgramCentralConstants.TYPE_PROJECT_SNAPSHOT,ProgramCentralConstants.PROGRAM);
   		  dmoSnapshot.setId(objectId);
   		  dmoSnapshot.delete(context);
   	   %>
   	<script language="javascript" type="text/javaScript">
   		var topFrame = findFrame(getTopWindow(), "PMCProjectSnapshots");
   		if(topFrame != null)
   		{
    	 		topFrame.location.href = topFrame.location.href;
    	 	}
   	</script>
   <%
   	}
     }else if("compareWBS".equalsIgnoreCase(strMode)){
 	    String strSelectedTaskRowId = request.getParameter("emxTableRowId");

 		Map mapRowId = (Map)ProgramCentralUtil.parseTableRowId(context,strSelectedTaskRowId);
 		String sParentId = (String)mapRowId.get("parentOId");
 		String sChildId = (String)mapRowId.get("objectId");

 		if(ProgramCentralUtil.isNotNullString(sParentId)){
 	contentURL = "../common/emxIndentedTable.jsp?table=PMCWhatIfCompareViewTable&showRMB=false&expandProgram=emxWhatIf:getExperimentWBSSubtasks&reportType=Complete_Summary_Report&IsStructureCompare=TRUE&expandLevel=0&connectionProgram=emxWhatIf:updateMasterProject&resequenceRelationship=relationship_Subtask&refreshTableContent=true&objectId=";
 	contentURL += XSSUtil.encodeForURL(context,sChildId) +","+ XSSUtil.encodeForURL(context, sParentId);
 	contentURL += "&ParentobjectId="+XSSUtil.encodeForURL(context, sParentId)+"&objectId1="+XSSUtil.encodeForURL(context, sChildId)+"&objectId2="+XSSUtil.encodeForURL(context, sParentId);
 	contentURL += "&compareBy=Name,Dependency,ConstraintType,Constraint Date,PhaseEstimatedDuration,PhaseEstimatedStartDate,PhaseEstimatedEndDate,Description&objectCompare=false&showClipboard=false&customize=false&rowGrouping=false&inlineIcons=false&displayView=details&syncEntireRow=true&SortDirection=ascending&SortColumnName=dupId&matchBasedOn=TaskId&selection=multiple&editRootNode=false&emxSuiteDirectory=programcentral&suiteKey=ProgramCentral&hideRootSelection=true";
 		}else {
 	String strMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
 			"emxProgramCentral.ProjectSnapshot.WBSCompareWarningMessage", languageStr);
 %>
 			<script language="javascript" type="text/javaScript">
 	       		var vMsg = "<%=strMsg%>";
 	       		alert(vMsg);
 	 		</script>
 			<%
 				return;
 					}
 			%>
    		<script language="javascript">
    	  <%-- XSSOK--%>
		var url = "<%=contentURL%>";
    			var topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
    			topFrame.location.href = url;
    		</script>
    		<%
    			}else if("launchWBS".equalsIgnoreCase(strMode)){
    			String objectId = (String) emxGetParameter(request, "objectid");
    			session.setAttribute("ObjectId",objectId);
    			contentURL = "../common/emxIndentedTable.jsp?table=PMCWhatIfWBSViewTable&jsTreeID=null&parentOID="+XSSUtil.encodeForURL(context, objectId)+"&objectId="+XSSUtil.encodeForURL(context, objectId)+"&emxSuiteDirectory=programcentral&showRMB=false&suiteKey=ProgramCentral&HelpMarker=emxhelpwbstasklist&findMxLink=false&freezePane=Name&showPageHeader=false&header=emxProgramCentral.Common.WorkBreakdownStructureSB&editLink=false&selection=multiple&sortColumnName=ID&postProcessJPO=emxTask:postProcessRefresh&StringResourceFileId=emxProgramCentralStringResource&editRelationship=relationship_Subtask&expandProgram=emxTask:getWBSSubtasks&expandLevel=1&massPromoteDemote=false&rowGrouping=false&objectCompare=false&showClipboard=false&showPageURLIcon=false&triggerValidation=false&displayView=details&multiColumnSort=false&showRMB=false";
    		%>
 	<script language="javascript" type="text/javaScript">
   <%-- XSSOK--%>
		var url = "<%=contentURL%>";
 		var topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
 		topFrame.location.href = url;
 	</script>
 	<%
    	} else if("addResourceManager".equalsIgnoreCase(strMode)) {

    		String[] selectedRowArray = emxGetParameterValues(request, "emxTableRowId");

    		if (selectedRowArray != null && selectedRowArray.length > 0) {
	    		Map paramMap = new HashMap(2);
    			String companyId=DomainConstants.EMPTY_STRING;
    			List<String> resourceManagerIdList = new ArrayList<String>();

	    		for (int i=0;i<selectedRowArray.length;i++) {
	    			StringList idList = FrameworkUtil.split(selectedRowArray[i], "|");
	    			resourceManagerIdList.add((String)idList.get(0));
	    			companyId = (String)idList.get(1);
	    		}

    			paramMap.put("objectId",companyId);
    			paramMap.put("resourceManagerIdList", resourceManagerIdList);
    			JPO.invoke(context, "emxResourcePool", null, "addResourceManagerToCompany",JPO.packArgs(paramMap));
    		}
    %>
       <script language="javascript" type="text/javaScript">
       		getTopWindow().getWindowOpener().refreshSBTable(getTopWindow().getWindowOpener().configuredTableName);
       		getTopWindow().closeWindow();
 		</script>
    <%
    	} else if("removeResourceManager".equalsIgnoreCase(strMode)) {

    		String[] selectedRowArray = emxGetParameterValues(request, "emxTableRowId");

    		if (selectedRowArray != null && selectedRowArray.length > 0) {
	    		Map paramMap = new HashMap(2);
    			String companyId=DomainConstants.EMPTY_STRING;
    			List<String> resourceManagerIdList = new ArrayList<String>();

	    		for (int i=0;i<selectedRowArray.length;i++) {
	    			StringList idList = FrameworkUtil.split(selectedRowArray[i], "|");
	    			resourceManagerIdList.add((String)idList.get(0));
	    			companyId = (String)idList.get(1);
	    		}

    			paramMap.put("objectId",companyId);
    			paramMap.put("resourceManagerIdList", resourceManagerIdList);
    			JPO.invoke(context, "emxResourcePool", null, "removeResourceManagerFromCompany",JPO.packArgs(paramMap));
    		}
    %>
       <script language="javascript" type="text/javaScript">
       		var topFrame = findFrame(getTopWindow(),"detailsDisplay");
       		if (topFrame != null) {
	 			topFrame.location.href = topFrame.location.href;
	        }else{
	        	parent.location.href = parent.location.href;
	        }
 		</script>
		<%
			}  else if("deleteRisk".equalsIgnoreCase(strMode)){
			   Risk risk = (Risk) DomainObject.newInstance(context,DomainConstants.TYPE_RISK,"PROGRAM");
	 		   String[] risks = emxGetParameterValues(request,"emxTableRowId");
	 		   risks = ProgramCentralUtil.parseTableRowId(context,risks);
				String parentObjId = emxGetParameter(request, "objectId");
				StringList completeRiskNames = new StringList();
	 		   if(risks != null && risks.length > 0){
	 			  completeRiskNames = Risk.removeOrDeleteRisksfromParent(context, parentObjId, risks);
	 				  }
 			   if(!completeRiskNames.isEmpty()){
	 				  String errorMessage =ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Risk.UnableToDeleteRisk",sLanguage);
 						 errorMessage=errorMessage+"\n"+completeRiskNames;
	    	    		 %>
	    	    		 <script language="javascript" type="text/javaScript">
	     		 				alert("<%=XSSUtil.encodeForJavaScript(context,errorMessage)%>");
  	 		 			</script>
	    	    		  <%
	 			   }
	 	    		%>
	 		 	<script language="javascript" type="text/javaScript">
	 		 		var topFrame = findFrame(getTopWindow(), "PMCProjectRisk");
	 		 		if (topFrame != null) {
	 		 			topFrame.location.href = topFrame.location.href;
	 		 			topFrame.refreshSBTable(topFrame.configuredTableName);
	 		        }else{
	 		        	parent.location.href = parent.location.href;
	 		        }
	 		 	</script>
	 		  <%}else if("findAssessor".equalsIgnoreCase(strMode)){
		    String fieldNameDisplay = emxGetParameter(request, "fieldNameDisplay");
		    fieldNameDisplay = XSSUtil.encodeURLForServer(context, fieldNameDisplay);
		    String fieldNameActual = emxGetParameter(request, "fieldNameActual");
		    fieldNameActual = XSSUtil.encodeURLForServer(context, fieldNameActual);
		    String fieldNameOID = emxGetParameter(request, "fieldNameOID");
		    fieldNameOID = XSSUtil.encodeURLForServer(context, fieldNameOID);

		    String assessmentId = emxGetParameter(request, "objectId");
		    Assessment assessment = new Assessment(assessmentId);
            StringList busSelects = new StringList();
            busSelects.add(ProgramCentralConstants.SELECT_ID);
		    Map projectMap = assessment.getRelatedObject(context, Assessment.RELATIONSHIP_PROJECT_ASSESSMENT, false, busSelects, null);
		    String projectId = (String) projectMap.get(ProgramCentralConstants.SELECT_ID);
			 String strURL = "../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=policy_Person.state_Active&table=PMCCommonPersonSearchTable&selection=multiple&includeOIDprogram=emxAssessment:getPeopleForAssessor&showInitialResults=true";
			 strURL +="&objectId=" + XSSUtil.encodeForURL(context, projectId);
			 strURL +="&submitURL=../common/AEFSearchUtil.jsp";
			 strURL +="&fieldNameDisplay="+fieldNameDisplay;
			 strURL +="&fieldNameActual="+fieldNameActual;
			 strURL +="&fieldNameOID="+XSSUtil.encodeForURL(context, fieldNameOID);
			%>
				<script language="javascript">
	  				<%-- XSSOK--%>
					var url = "<%=strURL%>";
					url = url + "&frameNameForField="+parent.name;
					showModalDialog(url);
				</script>
			<%
    	   }else if("deleteProject".equalsIgnoreCase(strMode)){
    		   String[] projects = emxGetParameterValues(request,"emxTableRowId");
    		   for (int i=0;i<projects.length;i++){
    			      Map mpRow = ProgramCentralUtil.parseTableRowId(context,projects[i]);
    			      projects[i] = (String) mpRow.get("objectId");
    		   }
    		   if(projects.length >0){
    			   session.setAttribute("selectedProjectId", projects);
    		   }
    		   String url = "../programcentral/emxProgramCentralProjectDeleteProcess.jsp?invokedFrom=StructureBrowser";
    	    		 %>
    		 	<script language="javascript" type="text/javaScript">
	    		 	var topFrame = findFrame(getTopWindow(), "PMCProjectSpaceMyDesk");
	    		 	if(topFrame== null){
	    		 		topFrame = findFrame(getTopWindow(), "content");
	    		 		turnOnProgress();
<%--XSSOK--%>	    		 		document.location.href = "<%=url%>";
	    		 	}else{
	    			setTimeout(function() {
	    				topFrame.toggleProgress('visible');
<%--XSSOK--%>	    				document.location.href = "<%=url%>";
		    		    },100);
	    		 	}


    		 	</script>
    		  <%
    	   }else if("deleteProjectTemplate".equalsIgnoreCase(strMode)){
    		   String[] projectTemplates = emxGetParameterValues(request,"emxTableRowId");
    		    String[] projectTemplatesSelected = new String[projectTemplates.length];
    		   for (int i=0;i<projectTemplates.length;i++){
    			      Map mpRow = ProgramCentralUtil.parseTableRowId(context,projectTemplates[i]);
                      if (objId != null && objId.equalsIgnoreCase((String) mpRow.get("objectId"))) {
              %>
              <script language="javascript" type="text/javaScript">
              alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Alert.CannotPerform</framework:i18nScript>");
              </script>
              <%
                      } else {
                          projectTemplatesSelected[i] = (String) mpRow.get("objectId");
                      }
    		   }
    		   if(projectTemplatesSelected.length >0 && projectTemplatesSelected[0] != null){
    			   session.setAttribute("selectedProjectTemplateId", projectTemplatesSelected);

    		   }

    		   String url = "../programcentral/emxProgramCentralUtil.jsp?mode=TemplateDeleteProcess";
    	    		 %>
    		 	<script language="javascript" type="text/javaScript">
	    		 	var topFrame = findFrame(getTopWindow(), "PMCProjectTemplateMyDesk");
	    		 	if(topFrame== null){
	    		 		topFrame = findFrame(getTopWindow(), "content");
	    		 		turnOnProgress();
<%--XSSOK--%>	    		 		document.location.href = "<%=url%>";
	    		 	}else{
	    			setTimeout(function() {
	    				topFrame.toggleProgress('visible');
<%--XSSOK--%>	    				document.location.href = "<%=url%>";
		    		    },100);
	    		 	}


    		 	</script>
    		  <%
    	   }else if("TemplateDeleteProcess".equalsIgnoreCase(strMode)){

    		   com.matrixone.apps.program.ProjectTemplate projectTemplate =
    				    (com.matrixone.apps.program.ProjectTemplate) DomainObject.newInstance(context,
    				    DomainConstants.TYPE_PROJECT_TEMPLATE, DomainConstants.PROGRAM);
    				  com.matrixone.apps.program.Task task =
    				    (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
    				    DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);

    				  String[] projectTemplates = emxGetParameterValues(request,"emxTableRowId");
    				  projectTemplates = ( String[])session.getAttribute("selectedProjectTemplateId");
    				  session.removeAttribute("selectedProjectTemplateId");
    				  //Commenting below lines as no need to parse, its already parsed.
    				  //projectTemplates = ProgramCentralUtil.parseTableRowId(context,projectTemplates);
    				  StringList projectList = new StringList();
    				  StringList projectRemoveAccessList = new StringList();
    				  StringList projectCreatedUsingTemplate = new StringList();
    				  boolean bFlag = false;

    				  try{
    					  if ( projectTemplates != null ) {

    					    StringList busSelects = new StringList(1);
    					    busSelects.add(task.SELECT_CURRENT);
    					    busSelects.add(task.SELECT_NAME);

    					    String policyName = task.getDefaultPolicy(context);
    					    String createStateName = PropertyUtil.getSchemaProperty(context,"policy",policyName,"state_Create");

    					    boolean deleteProj = true;
    					    // get the number of project
    					    int numProjectTemplates = projectTemplates.length;
    					    // need to check it to avoid zero using JS.
    					    for (int i=0; numProjectTemplates>i; i++) {
    						      deleteProj = true;
    						      projectTemplate.setId(projectTemplates[i]);
    						      StringList leafNodes = null;
    						    //PRG:RG6:R213:Mql Injection:parameterized Mql:8-Nov-2011:start
    						      String sCommandStatement = "expand bus $1 dump $2";
    						      String output =  MqlUtil.mqlCommand(context, sCommandStatement, true, projectTemplates[i], "|");
    						    //PRG:RG6:R213:Mql Injection:parameterized Mql:8-Nov-2011:End

    						      leafNodes = FrameworkUtil.split(output, "\n");
    						      ListIterator listItr = leafNodes.listIterator();
    						      String strRelationship = DomainConstants.RELATIONSHIP_INITIATED_TEMPLATE_PROJECT;
    						      while (listItr.hasNext())
    						      {
    						        String item = (String) listItr.next();
    						        StringList itemInfo = FrameworkUtil.split(item, "|");
    						        String id = (String) itemInfo.get(1);
    						        if(id.equals(strRelationship)){
    						        	projectCreatedUsingTemplate.add(projectTemplates[i]);
    						        }
    						      }

    						      MapList taskList = new MapList();
    						      if(projectCreatedUsingTemplate.size()==0){
    						    	  taskList = projectTemplate.getTasks(context, 0, busSelects, null, false);
    						      }
    						      if(!taskList.isEmpty()) {
    						        Iterator taskItr = taskList.iterator();
    						        while(taskItr.hasNext()) {
    						          Map theTask = (Map) taskItr.next();
    						          if(!createStateName.equals((String) theTask.get(task.SELECT_CURRENT))) {
    						            projectList.add(projectTemplate.getName(context));
    						            deleteProj = false;
    						            break;
    						          }
    						        }
    						      }

    						      if(deleteProj) {
    						    	  boolean isOwnerOrCoOwner = projectTemplate.isOwnerOrCoOwner(context,projectTemplates[i]);
    						          if(isOwnerOrCoOwner){
    						          		if (projectCreatedUsingTemplate.size()==0){
    						                	projectTemplate.remove(context);
    						          		}
    									}
    						     		else
    						     		{
    						            	projectRemoveAccessList.add(projectTemplate.getName(context));
    						        	}
    						      }
    					    }
    					  }
    				  }
    				  catch (Exception e)
    				  {
    				      bFlag = true;
    				      e.printStackTrace();
    				      session.putValue("error.message", e.getMessage());
    				  }

    				  %>
    				  <script language="javascript" type="text/javaScript">//<![CDATA[
    				    //hide JavaScript from non-JavaScript browsers
    				 	<% if(!projectList.isEmpty()) { %>
    				      alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.UnableToDeleteProject</framework:i18nScript>");
    				     <% }

    				    if(!projectRemoveAccessList.isEmpty()) { %>
    				        alert("<framework:i18nScript localize="i18nId">emxProgramCentral.ProjectTemplate.NotOwner</framework:i18nScript>");
    				 	<%}%>

    					<%if(!projectCreatedUsingTemplate.isEmpty() && projectRemoveAccessList.isEmpty()) { %>
    				     	alert("<framework:i18nScript localize="i18nId">emxProgramCentral.ProjectTemplate.ProjectCreatedUsingTemplate</framework:i18nScript>");
    					<%}%>

    				 	parent.document.location.href=parent.document.location.href;
    				 	// Stop hiding here -->//]]>
    				  </script>
    				  <%

    	   }else if("refreshQualityPage".equalsIgnoreCase(strMode)){
    	    		 %>
    		 	<script language="javascript" type="text/javaScript">
	    		 	var topFrame = findFrame(getTopWindow(), "PMCQuality");
	    		 	topFrame.location.href = topFrame.location.href;
    		 	</script>
    		  <%
    	   }else if("timesheetReminder".equalsIgnoreCase(strMode)){
    			Date date = new Date();
    			String strMemberId = emxGetParameter(request,"memberId");
    			String timesheetName = emxGetParameter(request,"timesheetName");
    			NotificationUtil.sendTimesheetSubmissionNotification(context, strMemberId, timesheetName);
    	   }else if("quickCreateQuestion".equalsIgnoreCase(strMode)){

                // nx5 - TP6569
               // String projectTemplateId = emxGetParameter( request, "parentOID");
                String projectTemplateId = emxGetParameter( request, "objectId");
        	Map<String,String> parameterMap = new HashMap<String,String>();
        	parameterMap.put("projectTemplateId",projectTemplateId);

        	Question question = new Question();
        	Map<String,String> questionInfoMap = question.createAndConnectQuestion(context, projectTemplateId);

        	String questionId = questionInfoMap.get("questionId");
        	String questionProjectRelId = questionInfoMap.get("questionProjectRelId");

	       	String xmlMessage = "<mxRoot><action><![CDATA[add]]></action>";
	       		   	xmlMessage +="<data status=\"committed\" fromRMB=\"" + false + "\">";
	       		   	xmlMessage +="<item oid=\"" + questionId + "\" relId=\"" + questionProjectRelId + "\" pid=\"" + projectTemplateId + "\"/>";
	       			xmlMessage +="</data></mxRoot>";
 	    	%>
 		 	<script language="javascript" type="text/javaScript">
	 		 	var topFrame = findFrame(getTopWindow(),"detailsDisplay");
	            topFrame.emxEditableTable.addToSelected('<%=XSSUtil.encodeForJavaScript(context,xmlMessage)%>');
	            topFrame.refreshStructureWithOutSort();
 		 	</script>
 		  <%
     } else if("deleteQuestion".equalsIgnoreCase(strMode)){

    		   String[] selectedRowArray = emxGetParameterValues(request,"emxTableRowId");
    		   //String[] questionIdArray  = new String[selectedRowArray.length];
    		   String partialXML = "";
			   StringList slAllQuestionIds = new StringList();

    		   for(int i=0; i<selectedRowArray.length; i++) {
					String selectedRowString = selectedRowArray[i];
					Map parsedObjectMap = ProgramCentralUtil.parseTableRowId(context,selectedRowString);
					String objectId = (String)parsedObjectMap.get("objectId");
					String rowId = (String)parsedObjectMap.get("rowId");
					StringList busSelectList = new StringList();
					busSelectList.add(ProgramCentralConstants.SELECT_ID);
					DomainObject questionObj = new DomainObject(objectId);
					MapList mlSubquestions = questionObj.getRelatedObjects(context,DomainObject.RELATIONSHIP_QUESTION,DomainConstants.TYPE_QUESTION,busSelectList,null,false,true,(short)0,null,null);
					for(int mlItr=0; mlItr<mlSubquestions.size();mlItr++){
						Map queMap = (Map) mlSubquestions.get(mlItr);
						String questionIdTemp = (String) queMap.get(ProgramCentralConstants.SELECT_ID);
						slAllQuestionIds.add(questionIdTemp);
					}
					
					//questionIdArray[i] = objectId;
					slAllQuestionIds.add(objectId);
					partialXML += "<item id=\"" + rowId + "\" />";
				}
				String[] questionIdArray  = new String[slAllQuestionIds.size()];
				for(int slItr=0; slItr<slAllQuestionIds.size();slItr++){
					String queIdTemp = (String) slAllQuestionIds.get(slItr);
					questionIdArray[slItr] = queIdTemp;
				}

    		   if(questionIdArray != null && questionIdArray.length > 0){

	    		   boolean isOfQuestionType =
	    				   ProgramCentralUtil.isOfGivenTypeObject(context,DomainConstants.TYPE_QUESTION,questionIdArray);

	    		   if (isOfQuestionType) {

	    			   DomainObject.deleteObjects(context,questionIdArray);

	    			   String xmlMessage = "<mxRoot>";
	    			   String message = "";
	    			   xmlMessage += "<action refresh=\"true\" fromRMB=\"\"><![CDATA[remove]]></action>";
	    			   xmlMessage += partialXML;
	    			   xmlMessage += "<message><![CDATA[" + message + "]]></message>";
	    			   xmlMessage += "</mxRoot>";

	    			%>
   	    		 		<script language="javascript" type="text/javaScript">
		   	    		 	var topFrame = findFrame(getTopWindow(), "detailsDisplay");
		   		            topFrame.removedeletedRows('<%= XSSUtil.encodeForJavaScript(context,xmlMessage) %>');
		   		            topFrame.refreshStructureWithOutSort();
	 		 			</script>
   	    		  	<%
	    		   } else {
	    	    		String errorMessage =
	    	    				ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Question.PleaseSelectQuestionToRemove",sLanguage);
	    	    		 %>
	    	    		 	<script language="javascript" type="text/javaScript">
	     		 				alert("<%=XSSUtil.encodeForJavaScript(context,errorMessage)%>");
    	 		 			</script>
	    	    		  <%
	    	    	}
    		   }

      } else if("assignTaskToQuestion".equalsIgnoreCase(strMode)){

	   		   	String[] questionIdArray = emxGetParameterValues(request,"emxTableRowId");
	   		 	String projectTemplateId = objId;
	   			String errorMessage = null;
	   			String listTaskToAssignURL = null;

	   			if (questionIdArray != null && questionIdArray.length != 0) {
	   			   if (questionIdArray.length == 1) {
	   					String questionId =  ProgramCentralUtil.parseTableRowId(context,questionIdArray)[0];
	   					boolean isOfQuestionType =
	 	    				   ProgramCentralUtil.isOfGivenTypeObject(context,DomainConstants.TYPE_QUESTION,questionId);
	   					if (isOfQuestionType) {
	   						projectTemplateId = XSSUtil.encodeURLForServer(context,projectTemplateId);
		   					questionId = XSSUtil.encodeURLForServer(context,questionId);

		   					listTaskToAssignURL =
			   						"../common/emxIndentedTable.jsp?program=emxProjectTemplate:getTaskMapListToAssignQuestion"+
			   						"&table=PMCTaskListTable&selection=multiple&sortColumnName=Question Response"+
			   						"&suiteKey=ProgramCentral&SuiteDirectory=programcentral"+
			   						"&header=emxProgramCentral.Common.Tasks&helpMarker=emxhelpassigntotasks"+
			   						"&submitLabel=emxProgramCentral.Common.Assign"+
			   			  			"&submitURL=../programcentral/emxProgramCentralUtil.jsp"+
			   			  			"&mode=connectTaskToQuestion"+
			   						"&projectTemplateId="+projectTemplateId+"&questionId="+questionId;

	   					} else {
	   	   				 errorMessage = ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Question.PleaseSelectQuestionToAssignTasks",sLanguage);
	 	   			   }
	   			   } else {
	   				 errorMessage = ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Question.SelectOnlyOneQuestionToAssign",sLanguage);
	   			   }
	   		   	} else {
	 				errorMessage = ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Question.MustSelectQuestionToAssign",sLanguage);
	   		   	}

				if (ProgramCentralUtil.isNullString(errorMessage)) {
	   				%>
	     		 	<script language="javascript" type="text/javaScript">
	     		 	 	var listTaskToAssignURL = "<%=XSSUtil.encodeForJavaScript(context,listTaskToAssignURL)%>";
	     		 	 	getTopWindow().location.href = listTaskToAssignURL;
	     		 	</script>
	     			<%
	   		   	} else {
	   		   		%>
     		 		<script language="javascript" type="text/javaScript">
	     		 		alert("<%=XSSUtil.encodeForJavaScript(context,errorMessage)%>");
	     		 		getTopWindow().closeWindow();
    	 		 	</script>
     				<%
     				return;
	   		   	}
   	   	}  else if("removeQuestionTask".equalsIgnoreCase(strMode)){

   	   	   String[] selectedRowArray = emxGetParameterValues(request,"emxTableRowId");
		   String[] questionTaskIdArray  = new String[selectedRowArray.length];
		   String partialXML = "";

		   for(int i=0; i<selectedRowArray.length; i++) {
				String selectedRowString = selectedRowArray[i];
				Map parsedObjectMap = ProgramCentralUtil.parseTableRowId(context,selectedRowString);
				String objectId = (String)parsedObjectMap.get("objectId");
				String rowId = (String)parsedObjectMap.get("rowId");
				questionTaskIdArray[i] = objectId;
				partialXML += "<item id=\"" + rowId + "\" />";
			}

 		   	if(selectedRowArray != null && selectedRowArray.length > 0) {
 		   		 String errorMessage ="";
 		   		 boolean isOfTaskType =
 				   			ProgramCentralUtil.isOfGivenTypeObject(context,DomainConstants.TYPE_TASK_MANAGEMENT,questionTaskIdArray);

 		   		 if (isOfTaskType) {
					Question question = new Question();
 		    		errorMessage = question.disconnectTaskList(context, selectedRowArray);

 		   	     } else {
	 		   		errorMessage =
	 	   	    			ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.common.PleaseSelectTasksToRemove",sLanguage);
 		   	     }

	  	    	 if (ProgramCentralUtil.isNullString(errorMessage)) {
	  	    		String xmlMessage = "<mxRoot>";
	    			   String message = "";
	    			   xmlMessage += "<action refresh=\"true\" fromRMB=\"\"><![CDATA[remove]]></action>";
	    			   xmlMessage += partialXML;
	    			   xmlMessage += "<message><![CDATA[" + message + "]]></message>";
	    			   xmlMessage += "</mxRoot>";
	  	    	 %>
  	    	 		<script language="javascript" type="text/javaScript">
		    	 		var topFrame = findFrame(getTopWindow(), "detailsDisplay");
			            topFrame.removedeletedRows('<%= XSSUtil.encodeForJavaScript(context,xmlMessage) %>');
			            topFrame.refreshStructureWithOutSort();
    		 		</script>
		 		<% } else {
		 		%>
		 			<script language="javascript" type="text/javaScript">
  	    	 			alert("<%=XSSUtil.encodeForJavaScript(context,errorMessage)%>");
		 			</script>
	  	    	<%
 	     	}
   	   	  }
   	   	} else if("connectTaskToQuestion".equalsIgnoreCase(strMode)){

   	   	 	String[] selectedRowArray = emxGetParameterValues(request,"emxTableRowId");

		   	if(selectedRowArray != null && selectedRowArray.length > 0){
		   		String[] taskIdArray = ProgramCentralUtil.parseTableRowId(context,selectedRowArray);
		   		String[] questionResponseArray = new String[taskIdArray.length];
		   		 for(int i=0;i<taskIdArray.length;i++){
			    	 String selectedId = taskIdArray[i];
			    	 String questionResponse = request.getParameter(selectedId);
			    	 questionResponseArray[i]=questionResponse;
			    }
		   		String questionId 	 = emxGetParameter(request,"questionId");
	 			Question question = new Question(questionId);
				question.connectTaskArray(context,taskIdArray,questionResponseArray);

		 		%>
		 		<script language="javascript" type="text/javaScript">
		 			getTopWindow().getWindowOpener().refreshSBTable(getTopWindow().getWindowOpener().configuredTableName);
	       			getTopWindow().close();
 		 		</script>
	  	    	<%
	   	  }else{
	   		  String errorMessage =
	   	    			ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Common.Noobjectsselected",sLanguage);
		 	 %>
		 	<script language="javascript" type="text/javaScript">
		 		alert('<%= errorMessage %>');     <%--XSSOK--%>
				parent.location.href = parent.location.href;
	 	 	</script>
	 	 	<%
	   	  }
   	   	} else if("createResourcePlanTemplate".equalsIgnoreCase(strMode)){

	   	   	String projectTemplateId = emxGetParameter( request, "parentOID");
	   	 	String resourcePlanTemplateName = new DomainObject().getUniqueName("RPT-");

			ResourcePlanTemplate resourcePlanTemplate = new ResourcePlanTemplate();
			resourcePlanTemplate.createResourcePlanTemplate(context,projectTemplateId,resourcePlanTemplateName,"-");

	    	%>
		 	<script language="javascript" type="text/javaScript">
 		 	var topFrame = findFrame(getTopWindow(), "PMCResourcePlanTemplateSummaryTable");
	 		if (topFrame != null) {
	 			topFrame.location.href = topFrame.location.href;
	        }else{
	        	parent.location.href = parent.location.href;
	        }
		 	</script>
	<%
   	   	}  else if("resourcePlanTemplateResourceRequestCreate".equalsIgnoreCase(strMode)){
	   		String projectTemplateId = emxGetParameter(request, "parentOID");
   	 		String resourcePlanTemplateId = emxGetParameter(request, "objectId");
   			String resourcePlanTemplateRelId = emxGetParameter(request, "relId");

			ResourcePlanTemplate resourcePlanTemplate = new ResourcePlanTemplate();
			resourcePlanTemplate.createResourceRequestFromResourcePlanTemplate(context,
												resourcePlanTemplateId, projectTemplateId, resourcePlanTemplateRelId);

			%>
				<script language="javascript" type="text/javaScript">
	 		 	var topFrame = findFrame(getTopWindow(), "PMCResourcePlanTemplateRequestSummaryTable");
		 		if (topFrame != null) {
		 			topFrame.location.href = topFrame.location.href;
		        }else{
		        	parent.location.href = parent.location.href;
		        }
			 	</script>
			<%
   	   	} else if("createQuestionsAndConnectToTemplateTask".equalsIgnoreCase(strMode)){

 		   	String[] selectedRowArray = emxGetParameterValues(request,"emxTableRowId");
 		   	String projectTemplateId = emxGetParameter(request, "parentOID");
 		   	projectTemplateId = XSSUtil.encodeURLForServer(context,projectTemplateId);
			String portalCommandName = emxGetParameter(request, "openerFrame");  //SR
			portalCommandName = XSSUtil.encodeURLForServer(context, portalCommandName);//SR
   		   	if (selectedRowArray != null && selectedRowArray.length >= 1) {

				String[] taskIdArray = ProgramCentralUtil.parseTableRowId(context,selectedRowArray);

				StringList busSelectList= new StringList();
				busSelectList.add("to[" + DomainObject.RELATIONSHIP_QUESTION + "]");
				busSelectList.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
				String SELECT_TASK_TEMPLATE_QUESTION_ID =	"to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
				busSelectList.add(SELECT_TASK_TEMPLATE_QUESTION_ID);

				List<Map<String,String>> taskInfoMapList = DomainObject.getInfo(context,taskIdArray,busSelectList);

				for(Map<String,String> taskInfoMap :  taskInfoMapList) {

					String isOfTaskMgmtType = taskInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
					
					if("true".equalsIgnoreCase(isOfTaskMgmtType)){
						projectTemplateId = taskInfoMap.get(SELECT_TASK_TEMPLATE_QUESTION_ID);
					}
					
					if(!"true".equalsIgnoreCase(isOfTaskMgmtType)){
						String errorMessage =
		 	   	    			ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.common.PleaseSelectTasksOnly",sLanguage);
						%>
						<script language="javascript" type="text/javaScript">
							alert('<%=errorMessage%>');  <%--XSSOK--%>
							getTopWindow().closeSlideInDialog();
						</script>
						<%
						return;
					}

					String connectedToQuestion = taskInfoMap.get("to[" + DomainObject.RELATIONSHIP_QUESTION + "]");
					/*
					if ("true".equalsIgnoreCase(connectedToQuestion)){
						String errorMessage =
		 	   	    			ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Common.QuestionAlreadyAssigned",sLanguage);
						%>
						<script language="javascript" type="text/javaScript">
							alert('<%=XSSUtil.encodeForJavaScript(context,errorMessage)%>');
							getTopWindow().closeSlideInDialog();
					 	</script>
						<%
						return;
					}
					*/
				}

				String taskIdString = taskIdArray[0];
				for(int i=1;i<taskIdArray.length;i++) {
					taskIdString +="_"+taskIdArray[i];
				}
				String createQuestionsAndConnectToTemplateTaskURL =
					"../common/emxCreate.jsp?type=type_Question"+
					"&createJPO=emxQuestion:createNewQuestion"+
					"&typeChooser=false&nameField=keyin&form=PMCCreateQuestionForm"+
					"&suiteKey=ProgramCentral&SuiteDirectory=programcentral"+
					"&header=emxProgramCentral.Common.CreateQuestion"+
					"&HelpMarker=emxhelpquestioncreatedialog&findMxLink=false"+
					"&submitAction=nothing&showPageURLIcon=false"+
					"&postProcessURL=../programcentral/emxProgramCentralUtil.jsp?mode=postRefresh"+
					"&postProcessJPO=emxQuestion:connectQuestionToTask"+
					"&showQuestionResponseDD=true"+"&portalCmdName="+portalCommandName+
					"&projectTemplateId="+XSSUtil.encodeURLForServer(context,projectTemplateId)+"&taskIdString="+XSSUtil.encodeURLForServer(context,taskIdString);

					//createQuestionsAndConnectToTemplateTaskURL =XSSUtil.encodeURLForServer(context,createQuestionsAndConnectToTemplateTaskURL);
				%>
				<script language="javascript" type="text/javaScript">
				<%--XSSOK--%>	var createQuestionsAndConnectToTemplateTaskURL = "<%=createQuestionsAndConnectToTemplateTaskURL%>";
					getTopWindow().showSlideInDialog(createQuestionsAndConnectToTemplateTaskURL,true);
			 	</script>
				<%
   		   	}
   	   }  else if("listQuestionsToAssignTemplateTask".equalsIgnoreCase(strMode)){

   		   	String[] selectedRowArray = emxGetParameterValues(request,"emxTableRowId");
   		   	String projectTemplateId = emxGetParameter(request, "parentOID");
   		   	projectTemplateId = XSSUtil.encodeURLForServer(context,projectTemplateId);


      		 DomainObject domProjectTemplate  = DomainObject.newInstance(context,projectTemplateId);
      		StringList busSelect = new StringList();
      		
       		 String SELECT_QUESTION_ID =	"from[" + DomainConstants.RELATIONSHIP_PROJECT_QUESTION + "]";
      		String SELECT_TASK_TEMPLATE_QUESTION =	"to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.from[" + DomainConstants.RELATIONSHIP_PROJECT_QUESTION + "]";
      		String SELECT_TASK_TEMPLATE_QUESTION_ID =	"to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
      		busSelect.add(SELECT_QUESTION_ID);
      		busSelect.add(SELECT_TASK_TEMPLATE_QUESTION);
      		busSelect.add(SELECT_TASK_TEMPLATE_QUESTION_ID);
      		busSelect.add(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
      		
      		Map infoMap = domProjectTemplate.getInfo(context,busSelect);
      		
    		String hasQuestions = (String) infoMap.get(SELECT_QUESTION_ID);
    		String taskTemplateId = (String) infoMap.get(SELECT_TASK_TEMPLATE_QUESTION_ID);
    		String taskTemplateHasQuestions = (String) infoMap.get(SELECT_TASK_TEMPLATE_QUESTION);
    		String isTemplate = (String) infoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
    		
      		
    		if ("false".equalsIgnoreCase(hasQuestions) && "true".equalsIgnoreCase(isTemplate) || "false".equalsIgnoreCase(taskTemplateHasQuestions)){
   				String errorMessage = 
   	   	    			ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Common.NoQuestions",sLanguage);
   				%>
   				<script language="javascript" type="text/javaScript">
   					alert('<%=XSSUtil.encodeForJavaScript(context,errorMessage)%>');
   					getTopWindow().closeWindow(); 
   			 	</script>
   				<%
   				return;
   			}
        		  
     		if (selectedRowArray != null && selectedRowArray.length >= 1) {

				String[] taskIdArray = ProgramCentralUtil.parseTableRowId(context,selectedRowArray);

  				StringList busSelectList= new StringList();
  				busSelectList.add("to[" + DomainObject.RELATIONSHIP_QUESTION + "]");
  				List<Map<String,String>> taskInfoMapList = DomainObject.getInfo(context,taskIdArray,busSelectList);

  				for(Map<String,String> taskInfoMap :  taskInfoMapList) {
  					String connectedToQuestion = taskInfoMap.get("to[" + DomainObject.RELATIONSHIP_QUESTION + "]");
  					/*
  					if ("true".equalsIgnoreCase(connectedToQuestion)){
  						String errorMessage =
  		 	   	    			ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Common.QuestionAlreadyAssigned",sLanguage);
  						%>
  						<script language="javascript" type="text/javaScript">
  							alert('<%=XSSUtil.encodeForJavaScript(context,errorMessage)%>');
  							getTopWindow().closeWindow();
  					 	</script>
  						<%
  						return;
  					}
					*/
  				}

  				String taskIdString = taskIdArray[0];
  				for(int i=1;i<taskIdArray.length;i++) {
  					taskIdString +="_"+taskIdArray[i];
  				}
  				if("false".equalsIgnoreCase(isTemplate)){
  					projectTemplateId = taskTemplateId;
  				}
  				String listQuestionToAssignURL =
  					"../common/emxIndentedTable.jsp?"+
  					"&program=emxProjectTemplate:getProjectTemplateQuestionList"+
  					//"&table=PMCQuestionListTable&selection=single&sortColumnName=Name"+
  					"&table=PMCQuestionListTable&selection=multiple&sortColumnName=Name"+
  					"&suiteKey=ProgramCentral&SuiteDirectory=programcentral"+
  					"&header=emxFramework.Command.Question&helpMarker=emxhelptemplatewbsassignquestion"+
  					"&submitLabel=emxProgramCentral.Common.Assign"+
  		  			"&submitURL=../programcentral/emxProgramCentralUtil.jsp&mode=connectQuestiontoTask"+
  		  			"&projectTemplateId="+projectTemplateId+"&taskIdString="+taskIdString+"isTemplate="+isTemplate;

  		  		listQuestionToAssignURL = XSSUtil.encodeURLForServer(context,listQuestionToAssignURL);

  				%>
  				<script language="javascript" type="text/javaScript">
  					var listQuestionToAssignURL = "<%=listQuestionToAssignURL%>";
  					//getTopWindow().location.href = listQuestionToAssignURL;
  					showModalDialog(listQuestionToAssignURL);
  			 	</script>
  				<%
     		}
     	 } else if("connectQuestiontoTask".equalsIgnoreCase(strMode)) {
 		   	String[] selectedRowArray = emxGetParameterValues(request,"emxTableRowId");

 		   	//if (selectedRowArray != null && selectedRowArray.length == 1) {
 		   	if (selectedRowArray != null) {
				for(int selectedRowArrayItr = 0; selectedRowArrayItr<selectedRowArray.length; selectedRowArrayItr++){
	 		   	Map<String,String> infoMap = ProgramCentralUtil.parseTableRowId(context,selectedRowArray[selectedRowArrayItr]);
	 		   	String questionId    = infoMap.get("objectId");
	 		   	String selectedIndex = infoMap.get("rowId").split(",")[1];
	 		   	int questionIndex    = Integer.parseInt(selectedIndex);

	 		   	String taskIdString  = emxGetParameter(request,"taskIdString");
		   		String[] taskIdArray = taskIdString.split("_");
		    	String selectedQuestionResponse = emxGetParameter(request, questionId);
		    	String[] questionResponseArray = new String[taskIdArray.length];

		    	for(int i=0;i<taskIdArray.length;i++) {
					questionResponseArray[i]=selectedQuestionResponse;
				}

		    	Question question = new Question(questionId);
    			question.connectTaskArray(context,taskIdArray,questionResponseArray);
				}
		 	%>
		 	<script language="javascript" type="text/javaScript">

 		 	if(null != getTopWindow().getWindowOpener() && getTopWindow().getWindowOpener().configuredTableName != undefined){
		 		getTopWindow().getWindowOpener().refreshSBTable(getTopWindow().getWindowOpener().configuredTableName);
		 	}else{
		 		   var topFrame = window.parent.getTopWindow().getWindowOpener().parent;
		 		   topFrame.emxEditableTable.refreshStructureWithOutSort(); 	
		 	} 
   				getTopWindow().closeWindow();
			</script>
	  	    <%
 		   	 }else{
     			 String errorMessage =
  		 	   	    			ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Common.Noobjectsselected",sLanguage);
     			 %>
     			 <script language="javascript" type="text/javaScript">
     				 alert('<%= errorMessage %>');   <%--XSSOK--%>
					 getTopWindow().location.href = getTopWindow().location.href;
			 	 </script>
			 	 <%
 		   	}
   	 } else if("createQuestionForTemplateTask".equalsIgnoreCase(strMode)){

   		   	String taskIdString = emxGetParameter(request,"objectId");

    		if (ProgramCentralUtil.isNotNullString(taskIdString)) {
    		   	String SELECT_PROJECT_ID = "to["+DomainObject.RELATIONSHIP_SUBTASK+"].from.id";

    		   	taskIdString = XSSUtil.encodeURLForServer(context,taskIdString);
    		   	DomainObject taskObject  = DomainObject.newInstance(context,taskIdString);

   		 		String projectTemplateId = taskObject.getInfo(context,SELECT_PROJECT_ID);
   		 		projectTemplateId = XSSUtil.encodeURLForServer(context,projectTemplateId);

 				String createQuestionForTemplateTaskURL =
 					"../common/emxCreate.jsp?type=type_Question"+
					"&createJPO=emxQuestion:createNewQuestion"+
 					"&typeChooser=false&nameField=keyin&form=PMCCreateQuestionForm"+
 					"&suiteKey=ProgramCentral&SuiteDirectory=programcentral"+
 					"&header=emxProgramCentral.Common.CreateQuestion"+
 					"&HelpMarker=emxhelpquestioncreatedialog&findMxLink=false"+
 					"&submitAction=refreshCaller&showPageURLIcon=false"+
 					"&postProcessJPO=emxQuestion:connectQuestionToTask"+
 					"&showQuestionResponseDD=true"+
 					"&projectTemplateId="+XSSUtil.encodeURLForServer(context,projectTemplateId)+"&taskIdString="+XSSUtil.encodeURLForServer(context,taskIdString);

 					createQuestionForTemplateTaskURL =
 						XSSUtil.encodeURLForServer(context,createQuestionForTemplateTaskURL);
 				%>
 				<script language="javascript" type="text/javaScript">
 					var createQuestionForTemplateTaskURL = "<%=createQuestionForTemplateTaskURL%>"; <%--XSSOK--%>
 					getTopWindow().showSlideInDialog(createQuestionForTemplateTaskURL, true);
 			 	</script>
 				<%
    		}

     } else if("assignQuestionForTemplateTask".equalsIgnoreCase(strMode)){

      		String taskIdString = emxGetParameter(request,"objectId");

       		if (ProgramCentralUtil.isNotNullString(taskIdString)) {

       		   	String SELECT_PROJECT_TEMPLATE_ID =
       		   			"to["+DomainObject.RELATIONSHIP_PROJECT_ACCESS_KEY+"].from.from["+DomainObject.RELATIONSHIP_PROJECT_ACCESS_LIST+"].to.id";
       		   	taskIdString = XSSUtil.encodeURLForServer(context,taskIdString);

       		   	DomainObject taskObject  = DomainObject.newInstance(context,taskIdString);

      		 	String projectTemplateId = taskObject.getInfo(context,SELECT_PROJECT_TEMPLATE_ID);
      		 	projectTemplateId = XSSUtil.encodeURLForServer(context,projectTemplateId);

      		 	String listQuestionToAssignURL =
      	  				"../common/emxIndentedTable.jsp?"+
      	  				"&program=emxProjectTemplate:getProjectTemplateQuestionList"+
      	  				"&table=PMCQuestionListTable&selection=multiple&sortColumnName=Name"+
      	  				"&suiteKey=ProgramCentral&SuiteDirectory=programcentral"+
      	  				"&header=emxFramework.Command.Question&helpMarker=emxhelptemplatewbsassignquestion"+
      	  				"&submitLabel=emxProgramCentral.Common.Assign"+
      	  		  		"&submitURL=../programcentral/emxProgramCentralUtil.jsp&mode=connectQuestiontoTask"+
      	  		  		"&projectTemplateId="+projectTemplateId+"&taskIdString="+taskIdString;

      	  		listQuestionToAssignURL = XSSUtil.encodeURLForServer(context,listQuestionToAssignURL);

      	  		%>
      	  		<script language="javascript" type="text/javaScript">
      	  			var listQuestionToAssignURL = "<%=listQuestionToAssignURL%>";
      	  			getTopWindow().location.href = listQuestionToAssignURL;
      	  		</script>
      	  		<%
       		}
      } else if("unAssignTaskQuestion".equalsIgnoreCase(strMode)){

    	  String taskId = emxGetParameter(request, "objectId");

    	  if (ProgramCentralUtil.isNotNullString(taskId)) {
    		  String SELECT_QUESTION_REL_ID = "to["+ DomainObject.RELATIONSHIP_QUESTION+"].id";

	    	  DomainObject taskObject  = DomainObject.newInstance(context,taskId);
	    	  String questionConnectId = taskObject.getInfo(context,SELECT_QUESTION_REL_ID);

	    	  DomainRelationship.disconnect(context,questionConnectId);

	    	%>
    	  	<script language="javascript" type="text/javaScript">
	    	  	parent.document.location.href = parent.document.location.href;
    	  	</script>
    	  	<%
    	  }
  	  } else if ("wbsAssignmentView".equalsIgnoreCase(strMode) || "wbsAllocationView".equalsIgnoreCase(strMode) ) {

			String sMode = emxGetParameter(request,"subMode");
			String sRelationship = request.getParameter("relationship");
			String sObjectId = request.getParameter("objectId");
			String sRelId = request.getParameter("relId");
			String sRowID = request.getParameter("rowId");
			String sPersonOID = request.getParameter("personId");
			String sFrom = request.getParameter("from");
			String sPercent = request.getParameter("percent");
			boolean isModeAssigneeAllocation = request.getParameter("mode").equalsIgnoreCase("wbsAllocationView");
			DomainObject domObjTask = new DomainObject(sObjectId);
			String isCallForDataGrid = emxGetParameter(request,"isCallForDataGrid");

			DomainObject domObjPerson = new DomainObject(sPersonOID);
			StringList memberSelects = new StringList();
			memberSelects.add("attribute[First Name]");
			memberSelects.add("attribute[Last Name]");

			Map personInfo = domObjPerson.getInfo(context, memberSelects);
			String fName = (String)personInfo.get("attribute[First Name]");
			String lName = (String)personInfo.get("attribute[Last Name]");

			//String personColumnName = fName + " " + lName;
			String personColumnName = sPersonOID; 
			personColumnName = personColumnName.trim();

			boolean isAllocationView = "allocate".equalsIgnoreCase(sMode);
			boolean isAssignmentView = "assign".equalsIgnoreCase(sMode)||"unassign".equalsIgnoreCase(sMode);
			String allocationColumnName = "TotalAllocation";
			String totalAllocationhtmlOutputstr = "<c><head></head><body>";
			String personhtmlOutputstr = "<c><head></head><body>";

			try {
				DomainRelationship dRelationship = new DomainRelationship();
				if("assign".equalsIgnoreCase(sMode)) {
					domObjTask.addFromObject(context, new RelationshipType(sRelationship), sPersonOID);
				} else if ("unassign".equalsIgnoreCase(sMode))  {
						String attribute_PercentComplete=(String)PropertyUtil.getSchemaProperty(context,"attribute_PercentComplete");
						String completedPercent = domObjTask.getAttributeValue(context,attribute_PercentComplete);
						dRelationship = new DomainRelationship(sRelId);
						dRelationship.remove(context);
						domObjTask.setAttributeValue(context,DomainConstants.ATTRIBUTE_PERCENT_COMPLETE, completedPercent);
				} else if ("allocate".equalsIgnoreCase(sMode))  {
					if(sPercent.equals("0.0")) { //Remove both Assignment & Allocation
						dRelationship = new DomainRelationship(sRelId);
						dRelationship.remove(context);
					} else {
						DomainObject taskObj = DomainObject.newInstance(context, sObjectId);
						boolean taskCheckAccess = taskObj.checkAccess(context, (short) AccessConstants.cModify);
						if(taskCheckAccess){
							ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
							if (ProgramCentralUtil.isNullString(sRelId)) { //Assigning & Allocating
								dRelationship = domObjTask.addFromObject(context, new RelationshipType(sRelationship), sPersonOID);
							} else { //Re-allocating
								dRelationship = new DomainRelationship(sRelId);
							}
							dRelationship.setAttributeValue(context, ProgramCentralConstants.ATTRIBUTE_PERCENT_ALLOCATION, sPercent);
						}						
					}
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
				Map settings = new HashMap();
				settings.put("Project Policy", "Project Space");
				settings.put("personId", sPersonOID);
				settings.put("isCallForDataGrid", isCallForDataGrid);

				Map columnMap = new HashMap();
				columnMap.put("settings",settings);

				Map ObjectMap = new HashMap();
				ObjectMap.put("id" , sObjectId);
				ObjectMap.put("id[level]" , sRowID);

				MapList objectList = new MapList();
				objectList.add(ObjectMap);

				Map paramList = new HashMap();
				paramList.put("languageStr" , context.getLocale().getLanguage());

				Map paramMap = new HashMap();
				paramMap.put("paramList" , paramList);
				paramMap.put("objectList" , objectList);
				paramMap.put("columnMap" , columnMap);

				String[] args = JPO.packArgs(paramMap);
				Vector vresult = new Vector();
				vresult.add(0, ProgramCentralConstants.EMPTY_STRING);

				if(isAssignmentView) {
				 vresult = JPO.invoke(context, "emxProgramUIBase", null, "getAssignmentViewMembersColumnData", args , Vector.class);
					personhtmlOutputstr = personhtmlOutputstr + (String)vresult.get(0)+"</body></c>";
				}else if(isAllocationView){
					 vresult = JPO.invoke(context, "emxProgramUIBase", null, "getAllocationViewMembersColumnData", args , Vector.class);
					personhtmlOutputstr = personhtmlOutputstr + (String)vresult.get(0)+"</body></c>";

					//To update TotalAllocation column
					Vector vresult1 = JPO.invoke(context, "emxProgramUIBase", null, "getTaskTotalAllocation", args , Vector.class);
					totalAllocationhtmlOutputstr = totalAllocationhtmlOutputstr + (String)vresult1.get(0)+"</body></c>";
					totalAllocationhtmlOutputstr = totalAllocationhtmlOutputstr.replace("\"", "\\\"");
				}
				personhtmlOutputstr = personhtmlOutputstr.replace("\"", "\\\"");
%>
		<script language="javascript" type="text/javaScript">
		var isAllocationView = <%=isAllocationView%>;
			parent.editableTable.mode = "edit";
			parent.emxEditableTable.setCellHTMLValueByRowId("<%=sRowID%>","<%=personColumnName%>","<%=personhtmlOutputstr%>","false",false);
		var isModeAssigneeAllocation = <%=isModeAssigneeAllocation%>;

			if(isAllocationView){
				parent.emxEditableTable.setCellHTMLValueByRowId("<%=sRowID%>","<%=allocationColumnName%>","<%=totalAllocationhtmlOutputstr%>","false",false);
				if(isModeAssigneeAllocation){
					parent.emxEditableTable.refreshStructureWithOutSort();
				}
			}
			parent.editableTable.mode = "view";
		</script>
<%
  	  }else if("getCurrentDate".equalsIgnoreCase(strMode)){
			ProjectSpace project = new ProjectSpace();
			Map programMap = new HashMap();
			Map requestMap = new HashMap();

			programMap.put("requestMap",requestMap);
			requestMap.put("timeZone", (String)session.getValue("timeZone"));

			String []args = JPO.packArgs(programMap);
			String currentProjectDate = project.getCurrentDate(context, args);
			JsonObject jsonObject = Json.createObjectBuilder()
					                .add("ProjectDate",currentProjectDate)
					                .build();

			out.clear();
			out.write(jsonObject.toString());
			return;

		}else if("QuestionTxt".equalsIgnoreCase(strMode)){
			String subMode = emxGetParameter(request,"subMode");
			JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

			String noQuestionText = EnoviaResourceBundle.getProperty(context,ProgramCentralConstants.PROGRAMCENTRAL,
					"emxProgramCentral.Common.Project.QuestionToResponed", languageStr);

			String toRespondQuestionText = EnoviaResourceBundle.getProperty(context,ProgramCentralConstants.PROGRAMCENTRAL,
					"emxProgramCentral.Common.Project.QuestionBeforeResponse", languageStr);

			if(ProgramCentralUtil.isNotNullString(subMode) && "NoQuestion".equalsIgnoreCase(subMode)){
				jsonObjectBuilder.add("questionsDisplay",noQuestionText);
			}else{
				jsonObjectBuilder.add("questionsDisplay",toRespondQuestionText);
			}

			out.clear();
			out.write(jsonObjectBuilder.build().toString());
			return;
		}else if("QuestionResponse".equalsIgnoreCase(strMode)){
			CacheUtil.removeCacheObject(context, "dpm_questionResponseCachedData");
		    String fieldNameDisplay = request.getParameter("fieldNameDisplay");
		    fieldNameDisplay = XSSUtil.encodeURLForServer(context, fieldNameDisplay);
		    
		    String fieldNameActual = request.getParameter("fieldNameActual");
		    fieldNameActual = XSSUtil.encodeURLForServer(context, fieldNameActual);
		    
		    String fieldNameOID = request.getParameter("fieldNameOID");
		    fieldNameOID = XSSUtil.encodeURLForServer(context, fieldNameOID);
		    
		    String templateObjectId = (String)session.getValue("selectedTemplateId");
		    templateObjectId = XSSUtil.encodeURLForServer(context, templateObjectId);

		    Map questionTaskListMap = new HashMap();
		    String urlProgram = "emxProjectSpace:getActiveQuestionList";
		    String strQusURL =  Question.getQuestionResponceURL(questionTaskListMap,templateObjectId,urlProgram,fieldNameDisplay,fieldNameActual,fieldNameOID);
			strQusURL+="&expandProgram=emxQuestion:getEmptyMapListForSubQuestion&emxExpandFilter=All&expandLevelFilter=false";
		    %>
				<script language="javascript">
					//document.location.href ='<%=strQusURL%>';
					showModalDialog('<%=strQusURL%>');
				</script>
			<%
		}else if("updateQuestionValue".equalsIgnoreCase(strMode)){
		    String[] QR = emxGetParameterValues(request,"QR");
		    String[] commentInput = emxGetParameterValues(request,"CommentInput");
			

		    String strFieldNameDisplay = (String)emxGetParameter(request, Search.REQ_PARAM_FIELD_NAME_DISPLAY);
			String strFieldNameActual = (String)emxGetParameter(request, Search.REQ_PARAM_FIELD_NAME_ACTUAL);

			strFieldNameDisplay = XSSUtil.encodeURLForServer(context, strFieldNameDisplay);
			strFieldNameActual = XSSUtil.encodeURLForServer(context, strFieldNameActual);

		    String questionResponseValue = DomainObject.EMPTY_STRING;
		    String questionResponseCommentValue = DomainObject.EMPTY_STRING;
		    for(int i =0;i<QR.length;i++){
			    String questionResponse = QR[i];
			    String commentInputTemp = commentInput[i];
				if(ProgramCentralUtil.isNotNullString(questionResponse)){
			    StringList questionResponseValueList = FrameworkUtil.split(questionResponse, "|");
			    questionResponseValue += (String)questionResponseValueList.get(0)+"="+(String)questionResponseValueList.get(1)+"|";
			    questionResponseCommentValue += (String)questionResponseValueList.get(0)+"="+commentInputTemp+"|";
		    }
		    }
			if(ProgramCentralUtil.isNotNullString(questionResponseValue)){
		    questionResponseValue = questionResponseValue.substring(0, questionResponseValue.length()-1);
			}
			if(questionResponseCommentValue.length()>0){
		    questionResponseCommentValue = questionResponseCommentValue.substring(0, questionResponseCommentValue.length()-1);
			}
		    String loggedinUser = context.getUser();
		    CacheUtil.setCacheObject(context, loggedinUser+"_QuestionsResponse", questionResponseValue);
		    CacheUtil.setCacheObject(context, loggedinUser+"_QuestionsResponseComment", questionResponseCommentValue);
		    String textAfterResponse = EnoviaResourceBundle.getProperty(context,ProgramCentralConstants.PROGRAMCENTRAL,
					"emxProgramCentral.Common.Project.QuestionAfterResponse", languageStr);
		    %>
			<script language="javascript">
				var strfieldDisplay = "<%=strFieldNameDisplay%>";		<%-- XSSOK --%>
		      	var strfieldNameActual = "<%=strFieldNameActual%>" ; 	<%-- XSSOK --%>

		      	var slideInFrame = findFrame(getTopWindow().getWindowOpener().window.getTopWindow(), "slideInFrame");
		      	var txtTypeDisplay = slideInFrame.document.forms[0].elements[strfieldDisplay];
		        var txtTypeActual = slideInFrame.document.forms[0].elements[strfieldNameActual];

			    txtTypeDisplay.value = "<%=textAfterResponse%>"; <%-- XSSOK --%>
			    txtTypeActual.value = "<%=textAfterResponse%>";  <%-- XSSOK --%>

			    getTopWindow().close();
			</script>
			<%

		}else if("importProjectProcess".equalsIgnoreCase(strMode)){
			CacheUtil.removeCacheObject(context, "typeList");
			CacheUtil.removeCacheObject(context, "PersonInfo");
			String importSubProject = (String)emxGetParameter(request, "importSubProject");
			Context contextDB = (matrix.db.Context)request.getAttribute("context");
			ProjectSpace project = new ProjectSpace();
			JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
			boolean invalidConfig = false;

			String fileName = emxGetParameter(request,"fileName");
			String fileNameExt[] = fileName.split("\\.");
			String fileExt = fileNameExt[1];

			String delimiter = ",";
			if(fileExt.equals("txt")){
				delimiter = "\t";
			}
			String projectDescription = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.ProjectImport.ProjectDescription", languageStr);
			//Need to remove fakepath from filePath IR-750690
			int fakePathIndex =fileName.lastIndexOf("fakepath")+9;
			
			if(fileName.contains("fakepath")){
				fileName = fileName.substring(fakePathIndex);
			}
			projectDescription += fileName;
			Map requestMap = new HashMap();
			requestMap.put("timeZone", (String)session.getValue("timeZone"));
			requestMap.put("importSubProject", importSubProject);

			InputStream in = request.getInputStream();
			MapList importedObjectList = project.getImportedFileDetails(context, in, delimiter,requestMap);
			Map<String,Map<String, String>> projectWBSIdToLevelMap = (HashMap)requestMap.get("projectWBSIdToLevelMap");
			session.setAttribute("ProjectImport_projectWBSIdToLevelMap", projectWBSIdToLevelMap);
			//check file
			Map errorMap = (Map)importedObjectList.get(0);
			String errorValue = DomainObject.EMPTY_STRING;
			Set keySet = errorMap.keySet();

			Iterator itr = keySet.iterator();
			while (itr.hasNext()){
				String key = (String) itr.next();
				if(key.equalsIgnoreCase("error")){
					invalidConfig = true;
					errorValue = (String)errorMap.get(key);
					break;
				}
			}

			if(!invalidConfig){
				session.setAttribute("importList", importedObjectList);

				FrameworkServlet framework = new FrameworkServlet();
				
				boolean hasCircularDependency = ProjectSpace.hasCircularDependency(context, importedObjectList,projectWBSIdToLevelMap);
				String fileContent = project.getFilePreviewViewContent(context,importedObjectList);
				
				framework.setGlobalCustomData(session,context,"fileContent",fileContent);

				boolean isFileCurrpted = hasCircularDependency;
				int impListSize = importedObjectList.size();
				for(int i=0;i<impListSize;i++){
					Map importedObjectMap = (Map)importedObjectList.get(i);
					if(!project.hasCorrectValue(context, importedObjectMap)){
						isFileCurrpted = true;
						break;
					}
				}
				session.setAttribute("isFileCurrpted",isFileCurrpted);

				for(int i=0;i<impListSize;i++){
					Map importedObjectMap = (Map)importedObjectList.get(i);

					if(!isFileCurrpted){
						String objectType = (String)importedObjectMap.get("type");
						String objectTypeDisplay = EnoviaResourceBundle.getAdminI18NString(context, "Type", objectType, languageStr);
						
						String excludeTypes = ProgramCentralConstants.TYPE_PROJECT_BASELINE+","+ProgramCentralConstants.TYPE_EXPERIMENT;
						List<String> ProjectSpacederivatives = ObjectUtil.getTypeDerivatives(context, ProgramCentralConstants.TYPE_PROJECT_SPACE, true);
						List<String> excludeDerivatives = ObjectUtil.getTypeDerivatives(context, excludeTypes, true);
						
						ProjectSpacederivatives.removeAll(excludeDerivatives);
						
						if(ProjectSpacederivatives.contains(objectType)){
							String objectName = (String)importedObjectMap.get("name");
							String objectStartDate = (String)importedObjectMap.get("Task Estimated Start Date");

							try{
								TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
								double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
								double dClientTimeZoneOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue(); 
								objectStartDate = eMatrixDateFormat.getFormattedDisplayDate(objectStartDate, dClientTimeZoneOffset,context.getLocale());
							}catch(Exception e){
								e.printStackTrace();
							}

							jsonObjectBuilder.add("Name",objectName.trim());
							jsonObjectBuilder.add("TypeActual",objectType.trim());
							jsonObjectBuilder.add("TypeActualDisplay",objectTypeDisplay);
							jsonObjectBuilder.add("ProjectDate",objectStartDate.trim());
							jsonObjectBuilder.add("description",projectDescription.trim());
							jsonObjectBuilder.add("title",EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.ImportFrom.FileHasNoErrors", languageStr));

		   	        		break;
						}
					}else{
						jsonObjectBuilder.add("error",EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.ImportFrom.FileHasSomeErrors", languageStr));
						break;
					}

				}
			}else{
				jsonObjectBuilder.add("error",errorValue);
			}
			out.clear();
			out.write(jsonObjectBuilder.build().toString());
			return;

		}else if("searchProjectData".equalsIgnoreCase(strMode)){
			String loggedinUser = context.getUser();
			CacheUtil.removeCacheObject(context, loggedinUser+"_QuestionsResponse");
			ProjectSpace project = new ProjectSpace();
			JsonArrayBuilder jsonArray = Json.createArrayBuilder();
			JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
			
			Map <String,String>programMap = new HashMap();
			
			String selectedProjectId = emxGetParameter(request,"searchProjectId");
			StringList sourceTaskIdList = FrameworkUtil.split(selectedProjectId,"|");
			String[] sourceTaskIdArray = new String[sourceTaskIdList.size()];
			sourceTaskIdList.toArray(sourceTaskIdArray);
			
			session.setAttribute("selectedTemplateId", selectedProjectId);
			
			if(ProgramCentralUtil.isNotNullString(selectedProjectId)){

				MapList projectInfoMapList = project.getSelectedProjectInfo(context, sourceTaskIdArray, (String)session.getValue("timeZone"));
				
				for (Iterator iterator = projectInfoMapList.iterator(); iterator.hasNext();) {
					Map projectInfoMap = (Map) iterator.next();
					
					String projectId = (String)projectInfoMap.get("projectId");
					String kindOfProjectTemplate  = (String)projectInfoMap.get("KindOfProjectTemplate");		
					String questionInfo = (String)projectInfoMap.get("QuestionsInfo");
					boolean hasQuestion = ProgramCentralUtil.isNotNullString(questionInfo) ? true : false ;
				
					//No need to pass these key's to javascript function.
					projectInfoMap.remove("projectId");
					//projectInfoMap.remove("KindOfProjectTemplate");
					projectInfoMap.remove("QuestionsInfo");
					
					DomainObject projectObj = new DomainObject(projectId);
					String bookmarkPhysicalId = projectObj.getAttributeValue(context, "Bookmark Physical Id");
					projectInfoMap.put("folders",bookmarkPhysicalId);
					
					Set keys = projectInfoMap.keySet();
					for (Iterator i = keys.iterator(); i.hasNext();){
						String key = (String) i.next();
					    String value = (String) projectInfoMap.get(key);
					    if(!DomainObject.TYPE_PROJECT_TEMPLATE.equalsIgnoreCase(value)){
					    	if(!("TypeActualDisplay".equalsIgnoreCase(key) || "TypeActual".equalsIgnoreCase(key))){
					    		jsonObjectBuilder.add(key,value);	
							}
						}
					}
					
					if(Boolean.valueOf(kindOfProjectTemplate)){
						ResourcePlanTemplate resourcePlanTemplteObj = new ResourcePlanTemplate();
						MapList mlResourceRequest = resourcePlanTemplteObj.getResourceRequestMap(context,projectId);
					
						Map paramMap = new HashMap();
						paramMap.put("objectId", projectId);
			            MapList resultList  = JPO.invoke(context, "emxProjectSpace", null, "getActiveQuestionList", JPO.packArgs(paramMap), MapList.class);
						
			            if(resultList.isEmpty()){
			            	jsonObjectBuilder.add("Question","false");	
			            }else{
			            	jsonObjectBuilder.add("Question","true");	
						}
						
						if(mlResourceRequest.size()>0){
							jsonObjectBuilder.add("RT","true");	
						}else{
							jsonObjectBuilder.add("RT","false");
						}
					}
					jsonArray.add(jsonObjectBuilder);
				}
				out.clear();
				out.write(jsonArray.build().toString());
			}
			return;

		}else if("launchProject".equalsIgnoreCase(strMode)){
			Context contextDB = (matrix.db.Context)request.getAttribute("context");
			String loggedinUser = context.getUser();
			CacheUtil.removeCacheObject(context, loggedinUser+"_QuestionsResponse");

			StringBuffer treeUrl = new StringBuffer("../common/emxTree.jsp?AppendParameters=true"+
	                  "&treeNodeKey=node.ProjectSpace&suiteKey=eServiceSuiteProgramCentral&objectId="+
	                  XSSUtil.encodeForURL(request.getParameter("newObjectId")) + "&DefaultCategory=PMCGateDashboardCommandPowerView" +
	                  "&emxSuiteDirectory=programcentral&treeTypeKey=type.Project");
			%>
		    <script language="javascript" type="text/javaScript">

			  // var parentContentDetailsFrame = findFrame(getTopWindow().getWindowOpener().getTopWindow(), "content");
			  var parentContentDetailsFrame = findFrame(getTopWindow(), "content");
               parentContentDetailsFrame.document.location.href = "<%= treeUrl %>"; <%-- XSSOK --%>
              // parent.window.closeWindow();
              getTopWindow().closeSlideInDialog();

		    </script>
		    <%
	   }else if("launchImportProject".equalsIgnoreCase(strMode)){
		    FrameworkServlet framework = new FrameworkServlet();
			framework.removeFromGlobalCustomData(session, context, "fileContent");

			boolean isBackgoundImportOptionChecked = "TRUE".equalsIgnoreCase(emxGetParameter(request, "BackgoundImportOption"));
			String strURL = DomainObject.EMPTY_STRING;

		    Context contextDB = (matrix.db.Context)request.getAttribute("context");
			String newlyProjectCreatedId = request.getParameter("newObjectId");
			
			if(! isBackgoundImportOptionChecked){
			MapList importObjList = (MapList)session.getAttribute("importList");
			session.removeAttribute("importList");

			String cmd 		= "print $1";
		    String result 	= MqlUtil.mqlCommand(contextDB, cmd, "transaction");
						
			    Enumeration requestParams = emxGetParameterNames(request);
			    HashMap parameterMap = new HashMap();
	  		  	if(requestParams != null){
	        		  while(requestParams.hasMoreElements()){
		        		  String param = (String)requestParams.nextElement();
		        		  String value = emxGetParameter(request,param);
		        		  parameterMap.put(param, value);
			
	        		  }
	  		  	}
				  		 
	  		     Map projectWBSIdToLevelMap = (HashMap)session.getAttribute("ProjectImport_projectWBSIdToLevelMap");
	  		     parameterMap.put("projectWBSIdToLevelMap", projectWBSIdToLevelMap);
	  		     session.removeAttribute("ProjectImport_projectWBSIdToLevelMap");
				ProjectService.completeImportProcess(contextDB, newlyProjectCreatedId, importObjList, parameterMap);
				StringBuffer treeUrl = new StringBuffer("../common/emxTree.jsp?AppendParameters=true"+
		                  "&treeNodeKey=node.ProjectSpace&suiteKey=eServiceSuiteProgramCentral&objectId="+
		               	  XSSUtil.encodeForURL(newlyProjectCreatedId) + "&DefaultCategory=PMCGateDashboardCommandPowerView" +
		                  "&emxSuiteDirectory=programcentral&treeTypeKey=type.Project");
				strURL = treeUrl.toString();
			}else{
				String loggedinUser = context.getUser();
				strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=importCSVBackgroundJob&isBackgroundProcess=true&isCreateProjectOrCopyScheduleFromFile=createNewProjectFromFile&loggedInUser=" + loggedinUser;
				/*
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
	  		  	*/
				strURL = ProgramCentralUtil.appendRequestParameters(context,request,strURL);
			}
						
			%>
		    <script language="javascript" type="text/javaScript">
			  //  var parentContentDetailsFrame = findFrame(getTopWindow().getWindowOpener().getTopWindow(), "content");
			   var parentContentDetailsFrame = findFrame(getTopWindow(), "content");
                var url = "<%= strURL %>";
               
               var isBackgroundChecked = "<%= isBackgoundImportOptionChecked %>";
               
               if(isBackgroundChecked == "true"){
             	   emxUICore.getData(url);
             	   alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Import.BackgoundImportOptionMessage.CreateProjectFromFile</framework:i18nScript>");
               }else{
            	   parentContentDetailsFrame.document.location.href = url;	<%-- XSSOK --%> //gke1
               }
               
             //  parent.window.closeWindow();
              getTopWindow().closeSlideInDialog();


		    </script>
		    <%
			} else if ("importCSVBackgroundJob".equals(strMode)) {
		    		
		    		Context contextDB = context; //(matrix.db.Context)request.getAttribute("context");
		    		String newlyProjectCreatedId = request.getParameter("newObjectId");
		    		if (ProgramCentralUtil.isNullString(newlyProjectCreatedId)) {
		    			newlyProjectCreatedId = request.getParameter("objectId");
		    		}
		    		MapList importObjList = (MapList) session.getAttribute("importList");
		    		session.removeAttribute("importList");

				/*
		    		String cmd = "print $1";
		    		String result = MqlUtil.mqlCommand(contextDB, cmd, "transaction");
				*/

		    		ProjectService ProjectServiceInstance = new ProjectService();
		    		HashMap parameterMap = new HashMap();
		    		parameterMap.put("requestObject", request);


		    		Enumeration requestParams = emxGetParameterNames(request);

		  		  	if(requestParams != null){
		        		  while(requestParams.hasMoreElements()){
			        		  String param = (String)requestParams.nextElement();
			        		  String value = emxGetParameter(request,param);
			        		  parameterMap.put(param, value);
		        		  }
		  		  	}
		    		
		  		  Map projectWBSIdToLevelMap = (HashMap)session.getAttribute("ProjectImport_projectWBSIdToLevelMap");
		  		  parameterMap.put("projectWBSIdToLevelMap", projectWBSIdToLevelMap);
		  		  session.removeAttribute("ProjectImport_projectWBSIdToLevelMap");
		  		  
		  		  ProjectService.completeImportBackgroundProcess(contextDB, newlyProjectCreatedId, importObjList, parameterMap);
		    		
		  		/*Object[] importParameters = new Object[] { contextDB, newlyProjectCreatedId, importObjList, parameterMap };
		    		ContextUtil.submitPostTransactionJob(context, ProjectServiceInstance, "completeImportBackgroundProcess", importParameters );
		    		ContextUtil.processPostTransactionJob(context, true);*/
		    		return;
		    		
	   	} else if ("blankResourcePoolChart".equals(strMode)) {
				String strMessage = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.ResourcePlanning.Report.ClickOnChart", languageStr);
	%>
				<div align='center'><%=strMessage%></div>
	<%
				return;
			} else if ("displayResourcePoolChart".equalsIgnoreCase(strMode)) {
				String strTimeStamp = emxGetParameter(request, "timeStamp");
				String url = "../programcentral/emxProgramCentralResourcePoolReportChart.jsp?timeStamp=" + XSSUtil.encodeForURL(context, strTimeStamp) ;
	%>
				<script language="javascript" type="text/javaScript">
					var url = "<%=url%>";	<%-- XSSOK --%>
					var topFrame = findFrame(getTopWindow(), "PMCResourcePoolReportChart");
					topFrame.location.href = url;
				</script>
	<%
			}else if ("AddTaskBelow".equalsIgnoreCase(strMode)) 	{
			    String portalCommandName = emxGetParameter(request, "portalCmdName");
			    portalCommandName = XSSUtil.encodeURLForServer(context, portalCommandName);
			    
	    		String selectedNodeId = emxGetParameter(request, "emxTableRowId");
	    		String objectId = (String)(ProgramCentralUtil.parseTableRowId(context,selectedNodeId)).get("objectId");
	    		objectId = XSSUtil.encodeURLForServer(context, objectId);
	    		
	    		String parentId = (String)(ProgramCentralUtil.parseTableRowId(context,selectedNodeId)).get("parentOId");
	    		parentId = XSSUtil.encodeURLForServer(context, parentId);
	    		
	    		String rowId = (String)(ProgramCentralUtil.parseTableRowId(context,selectedNodeId)).get("rowId");
	    		
	    		String SELECT_IS_MARK_AS_DELETED = "to["+DomainConstants.RELATIONSHIP_DELETED_SUBTASK+"]";
	    		String SELECT_PARENT_CURRENT = "to[" + DomainConstants.RELATIONSHIP_SUBTASK + "].from.current";
	    		
                StringList selectList = new StringList(5);
                selectList.add(DomainObject.SELECT_CURRENT);
                selectList.add(SELECT_PARENT_CURRENT);
                selectList.add(SELECT_IS_MARK_AS_DELETED);
                selectList.add(ProgramCentralConstants.SELECT_HAS_PASSIVE_TASK);
                
                StringList stateList = new StringList(3);
	    		stateList.addElement(ProgramCentralConstants.STATE_PROJECT_TASK_REVIEW);
	    		stateList.addElement(ProgramCentralConstants.STATE_PROJECT_REVIEW_COMPLETE);
	    		stateList.addElement(ProgramCentralConstants.STATE_PROJECT_REVIEW_ARCHIEVE);
	    		
	    		objectId = XSSUtil.encodeURLForServer(context, objectId);
	    		parentId = XSSUtil.encodeURLForServer(context, parentId);

	    		DomainObject object = DomainObject.newInstance(context, objectId);
	    		
	    		Map objectInfo = object.getInfo(context, selectList);
	    		String selectedObjectState = (String)objectInfo.get(DomainObject.SELECT_CURRENT);
	    		String parentObjectState = (String)objectInfo.get(SELECT_PARENT_CURRENT);
	    		String isMarkAsDeleted = (String)objectInfo.get(SELECT_IS_MARK_AS_DELETED);
	    		String hasPassiveProject = (String)objectInfo.get(ProgramCentralConstants.SELECT_HAS_PASSIVE_TASK);

	    		if("0".equals(rowId) && stateList.contains(selectedObjectState)){
	    			%>
	    	    	   <script language="javascript" type="text/javaScript">
		    	    	   	if(parent.emxEditableTable.checkDataModified()){
				                alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
				            } else{
		    	    	   		alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Project.TaskInState3</framework:i18nScript>");
				            }
	    	           </script>
	    	    	<%
	    	    	return;
	    		}else if("true".equalsIgnoreCase(isMarkAsDeleted)){
	    			 %>
	    	          <script language="javascript" type="text/javaScript">
	    	          	if(parent.emxEditableTable.checkDataModified()){
			                alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
			            } else{
	    	          		alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.TaskHasBeenMarkedForDeletion</framework:i18nScript>");
			            }
	    	          </script>
	    	         <%
	    	         return;
	    		}else if("true".equalsIgnoreCase(hasPassiveProject)){
	    			 %>
	    	          <script language="javascript" type="text/javaScript">
	    	          	if(parent.emxEditableTable.checkDataModified()){
			                alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
			            } else{
	    	          		alert("<framework:i18nScript localize="i18nId">emxProgramCentral.PassiveProjectSpace.AddSubtask.InvalidOperation</framework:i18nScript>");
			            }
	    	          </script>
	    	         <%
	    	         return;
	    		}else if(stateList.contains(parentObjectState) && stateList.contains(selectedObjectState)){
	    			%>
	    	    	   <script language="javascript" type="text/javaScript">
		    	    	   	if(parent.emxEditableTable.checkDataModified()){
				                alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
				            } else{
		    	    	   		alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Project.TaskInState3</framework:i18nScript>");
				            }
	    	           </script>
	    	    	<%
	    	    	  return;
	    		}else{
	    			String strURL = "../common/emxCreate.jsp?typeChooser=false&nameField=both&type=Task&form=PMCProjectTaskCreateForm&mode=create&addTask=addTaskBelow&createJPO=emxTask:createNewTask&showApply=true&suiteKey=ProgramCentral&HelpMarker=emxhelpwbsadddialog&StringResourceFileId=emxProgramCentralStringResource&SuiteDirectory=programcentral&submitAction=doNothing&postProcessURL=../programcentral/emxProgramCentralUtil.jsp?mode=addSubTaskBelow&preProcessJavaScript=changeDurationAndPolicy&objectId="+objectId+"&parentId="+parentId+"&rowId="+rowId+"&portalCmdName="+portalCommandName+"&PolicyName=policy_ProjectTask&showPageURLIcon=false";
		    		%>
		        	<script language="javascript">
			        	if(parent.emxEditableTable.checkDataModified()){
			                alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.SaveWBSModification</framework:i18nScript>");
			            }else {
			        		var url = "<%=strURL%>"; <%-- XSSOK --%>
					  	    top.showSlideInDialog(url,true);
			            }
		            </script>
		    		<%
	    		}
				
	    	}else if ("addTaskAssignee".equalsIgnoreCase(strMode)) 	{
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
	    		/*String strURL = "../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=state_Active:USERROLE=Project User,VPLMViewer&table=PMCCommonPersonSearchTable&form=PMCCommonPersonSearchForm&includeOIDprogram=emxProgramCentralUtil:getIncludeOIDforProjectMemberPersonSearch&searchMode=GeneralPeopleTypeMode&selection=multiple&includeOIDprogram=emxTask:includeMembersToAddAsAssignee";
	    		 
	    		strURL +="&objectId="+objectId;
	    		strURL +="&submitURL=../common/AEFSearchUtil.jsp";
	    		strURL +="&fieldNameDisplay="+fieldNameDisplay;
	    		strURL +="&fieldNameActual="+fieldNameActual;
	    		strURL +="&fieldNameOID="+fieldNameOID;*/
	    		
	    		com.matrixone.apps.program.Task taskObj = new com.matrixone.apps.program.Task();
	            taskObj.setId(objectId);
	            StringList slBusSelects = new StringList(1);
	            slBusSelects.add(DomainConstants.SELECT_NAME);
	            Map projectInfo = taskObj.getProject(context, slBusSelects);
	            String strProjectSpace  = (String) projectInfo.get(DomainConstants.SELECT_NAME);
	  	        strProjectSpace = XSSUtil.encodeForURL(context, strProjectSpace);
	    		 
	    		String strURL = "../common/emxFullSearch.jsp?table=PMCCommonPersonSearchTable&field=TYPES=type_Person:CURRENT=policy_Person.state_Active&form=PMCCommonPersonSearchForm&searchMode=GeneralPeopleTypeMode&selection=multiple";
	    		strURL +="&objectId="+objectId;
	    		strURL +="&default=PRG_PEOPLE_PROJECT_MANAGEMENT="+strProjectSpace;
	    		strURL +="&submitURL=../common/AEFSearchUtil.jsp";
	    		strURL +="&fieldNameDisplay="+fieldNameDisplay;
	    		strURL +="&fieldNameActual="+fieldNameActual;
	    		strURL +="&fieldNameOID="+fieldNameOID; 
	    	
	    		%>
    			<script language="javascript">

    				var url = "<%=strURL%>";	<%-- XSSOK --%>
    				url = url + "&frameNameForField="+parent.name;
    				showModalDialog(url);
    			</script>
	    		<%
			}else if ("makeTaskOwner".equalsIgnoreCase(strMode)) 	{
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

	    		 String strURL = "../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=policy_Person.state_Active:USERROLE=Project User,VPLMViewer&table=PMCCommonPersonSearchTable&form=PMCCommonPersonSearchForm&excludeOIDprogram=emxProgramCentralUtil:getexcludeOIDforPersonSearch&searchMode=GeneralPeopleTypeMode&selection=single&includeOIDprogram=emxTask:includeMembersToAddAsAssignee";
	    		strURL +="&objectId="+objectId;
	    		strURL +="&submitURL=../common/AEFSearchUtil.jsp";
	    		strURL +="&fieldNameDisplay="+fieldNameDisplay;
	    		strURL +="&fieldNameActual="+fieldNameActual;
	    		strURL +="&fieldNameOID="+fieldNameOID;

	    		%>
	    			<script language="javascript">
	    				var url = "<%=strURL%>";	<%-- XSSOK --%>
	    				url = url + "&frameNameForField="+parent.name;
	    				showModalDialog(url);
	    			</script>
	    		<%
			}else if ("addSubTaskBelow".equalsIgnoreCase(strMode)) 	{	    		
	    		Context contextDB = (matrix.db.Context)request.getAttribute("context");
	    		String portalCommandName = (String)emxGetParameter(request, "portalCmdName");
	    		portalCommandName = XSSUtil.encodeURLForServer(context, portalCommandName);
	    		String newTaskId = emxGetParameter(request, "newObjectId");
	    		newTaskId = XSSUtil.encodeURLForServer(context, newTaskId);
	    		String objectId = emxGetParameter(request, "objectId");
	    		objectId = XSSUtil.encodeURLForServer(context, objectId);
	    		String mode = emxGetParameter(request, "InsertAboveBelow");
	    		String rowId = emxGetParameter(request, "rowId");
				String selectedId = null;
				boolean isAddTaskAbove = "addTaskAbove".equalsIgnoreCase(mode);
	    	
	    		MapList taskMapList = (MapList) CacheUtil.getCacheObject(context, "newTasksMapList");
	    		CacheUtil.removeCacheObject(context, "newTasksMapList");
	    		
	    		Map<String,String> projectScheduleMap = ProgramCentralUtil.getProjectSchedule(context, objectId);
	    		String projectSchedule = projectScheduleMap.get(objectId);
	    		if(projectSchedule == null || projectSchedule.isEmpty()){
	    			projectSchedule = ProgramCentralConstants.PROJECT_SCHEDULE_AUTO;
	    		}
	    		
	    		
	    		boolean isFromRMB = "true".equalsIgnoreCase(emxGetParameter(request, "isFromRMB"));
			  
	    		StringBuilder sBuff = new StringBuilder();
				String xmlMessage = DomainConstants.EMPTY_STRING;
				
				//Added for IR-1132099: Reversing the taskList for gridView to display IDs in sorted order when created using "Above"
				if("PMCWBSDataGridView".equalsIgnoreCase(portalCommandName) || "PMCProjectTemplateWBSDataGridView".equalsIgnoreCase(portalCommandName)){
					Collections.reverse(taskMapList);
				}
				sBuff.append("<mxRoot>");
				Iterator itrNewTask = taskMapList.iterator();
				ArrayList taskIds = new ArrayList();
				while (itrNewTask.hasNext()) {
					Map taskInfo = (Map) itrNewTask.next();
					String toId = (String) taskInfo.get(ProgramCentralConstants.SELECT_ID);
					taskIds.add(toId);
					String fromId = (String) taskInfo.get("to[Subtask].from.id");
					selectedId = fromId;
					String relId = (String) taskInfo.get("to[Subtask].id");
					sBuff.append("<action><![CDATA[add]]></action>");

					if (isFromRMB) {
	    		        sBuff.append("<data fromRMB=\"true\" status=\"committed\" ");
    		    	} else {
	    		    	sBuff.append("<data status=\"committed\" ");
	    		    }
					if("addTaskAbove".equalsIgnoreCase(mode)){
						sBuff.append("pasteBelowOrAbove=\"true\" >");
						sBuff.append("<item oid=\"" + toId + "\" relId=\"" + relId + "\" pid=\"" + fromId
								+ "\" pasteAboveToRow=\"" + rowId + "\" direction=\"" + "from" + "\" />");
						sBuff.append("</data>");
					}else { 
						sBuff.append(">");
						sBuff.append("<item oid=\"" + toId + "\" relId=\"" + relId + "\" pid=\"" + fromId
								+ "\"  direction=\"" + "from" + "\" />");
						sBuff.append("</data>");
					}
				}
				sBuff.append("</mxRoot>");
	    	  %>
	    	  <script language="javascript">
	    	  var frame = "<%=portalCommandName%>";
	    	  var schedule = "<%=projectSchedule%>";
			  var selectedObjId = "<%=selectedId%>";
			  var isAddTaskAbove = "<%=isAddTaskAbove%>";
	    	  var topFrame = findFrame(getTopWindow(), frame);

	    	  if(null == topFrame){
	    	  	topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
	    	      if(null == topFrame)
	    	  		topFrame = findFrame(getTopWindow(), "detailsDisplay");
	    	  }
			  
			 if(!topFrame.dataGridEnabled &&  "false" === isAddTaskAbove){ //Added for DataGrid
				var nRow = emxUICore.selectSingleNode(topFrame.oXML, "/mxRoot/rows//r[@o = '" + selectedObjId + "']");
				if(nRow)
				{
					nRow.setAttribute("checked", "checked");
				}
			 }
			  
			//Added by DI7
			  if("Manual" == schedule){
				  topFrame.emxEditableTable.addToSelected('<%=sBuff.toString()%>');
				top.jQuery('.fonticon-refresh').addClass('fonticon-refresh-info');
				toggleRollupIcon(topFrame,"iconActionUpdateDates","iconActionUpdateDatesActive");
			  }else{
				topFrame.rebuildViewInProcess = true;
	    	    topFrame.syncSBInProcess = true;
	    	    topFrame.emxEditableTable.addToSelected('<%=sBuff.toString()%>');
				topFrame.rebuildViewInProcess = false;
	    	    topFrame.syncSBInProcess = false;
	    	  	topFrame.emxEditableTable.refreshStructureWithOutSort();
			  }
			     topFrame.toggleProgress('hidden');
	         </script>
	    	  <%

	    	}else if ("refreshInsertTaskAbove".equalsIgnoreCase(strMode)) 	{
	    		Context contextDB = (matrix.db.Context)request.getAttribute("context");
		    	String portalCommandName = emxGetParameter(request, "portalCmdName");
		    	portalCommandName = XSSUtil.encodeURLForServer(context, portalCommandName);
	    		String newTaskId = emxGetParameter(request, "newObjectId");
	    		newTaskId = XSSUtil.encodeURLForServer(context, newTaskId);
	    		String objectId = emxGetParameter(request, "parentId");
	    		objectId = XSSUtil.encodeURLForServer(context, objectId);
	    		String rowId = emxGetParameter(request, "rowId");

	    		DomainObject newObject = DomainObject.newInstance(contextDB, newTaskId);
	    		String relId =  newObject.getInfo(contextDB,"to[" + DomainRelationship.RELATIONSHIP_SUBTASK + "].id");
	    		relId = XSSUtil.encodeURLForServer(context, relId);

	    		Map<String,String> projectScheduleMap = ProgramCentralUtil.getProjectSchedule(context, newTaskId);
	    		String projectSchedule = projectScheduleMap.get(newTaskId);
	    		if(projectSchedule == null || projectSchedule.isEmpty()){
	    			projectSchedule = ProgramCentralConstants.PROJECT_SCHEDULE_AUTO;
	    		}
	    		
	    		 boolean isFromRMB = "true".equalsIgnoreCase(emxGetParameter(request, "isFromRMB"));
	    		  StringBuffer sBuff = new StringBuffer();
	    		    sBuff.append("<mxRoot>");
	    		    sBuff.append("<action><![CDATA[add]]></action>");

	    		    if (isFromRMB) {
	    		        sBuff.append("<data fromRMB=\"true\" status=\"committed\" pasteBelowOrAbove=\"true\">");
		    	}else {
	    		    sBuff.append("<data status=\"committed\" pasteBelowOrAbove=\"true\">");
	    		    }
	    		    sBuff.append("");
	    		    sBuff.append("<item oid=\""+newTaskId+"\" relId=\""+relId+"\" pid=\""+objectId+"\"  direction=\""+"from"+"\" pasteAboveToRow=\"" + rowId + "\"/>");
	    		    sBuff.append("</data>");

	    		    sBuff.append("</mxRoot>");
	    	  %>
	    	  <script language="javascript">
	    		var frame = "<%=portalCommandName%>";
	    		  var schedule = "<%=projectSchedule%>";
	    	  	var topFrame = findFrame(getTopWindow(), frame);
	
	    	  if(null == topFrame){
	    	  	topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
				     if(null == topFrame){
	    	  		topFrame = findFrame(getTopWindow(), "detailsDisplay");
	    	  }
			     }

			     topFrame.emxEditableTable.addToSelected('<%=sBuff.toString()%>');
			if(!topFrame.dataGridEnabled){
			     var taskObjectId = '<%=newTaskId%>';
		    	  var xmlRef 	= topFrame.oXML;
		    	  var nParent = emxUICore.selectSingleNode(xmlRef, "/mxRoot/rows//r[@o = '" + taskObjectId + "']");
		    	  nParent.setAttribute("display","block");
		    	  }
		    	  //Added by DI7
				  if("Manual" == schedule){
					top.jQuery('.fonticon-refresh').addClass('fonticon-refresh-info');
					toggleRollupIcon(topFrame,"iconActionUpdateDates","iconActionUpdateDatesActive");
				  }else{
	    	  topFrame.emxEditableTable.refreshStructureWithOutSort();
				  }
	
	         </script>
	    	  <%

	    	} else if ("showCalendar".equalsIgnoreCase(strMode)) {

	    	    String fieldNameDisplay = emxGetParameter(request,"fieldNameDisplay");
	    	    fieldNameDisplay = XSSUtil.encodeURLForServer(context, fieldNameDisplay);
	    	    String fieldNameActual = emxGetParameter(request,"fieldNameActual");
	    	    fieldNameActual = XSSUtil.encodeURLForServer(context, fieldNameActual);
	    	    String fieldNameOID = emxGetParameter(request,"fieldNameOID");
	    	    fieldNameOID = XSSUtil.encodeURLForServer(context, fieldNameOID);
	    	    String objectId = emxGetParameter(request,"objectId");

	    		 if(ProgramCentralUtil.isNullString(objectId)){
	    			 objectId = request.getParameter("parentOID");
	    		 }
	    	    objectId = XSSUtil.encodeURLForServer(context, objectId);
	    		 String strURL = "../common/emxFullSearch.jsp?field=TYPES=type_WorkCalendar:CURRENT=state_Active&table=PMCTaskCalendarSearchTable&selection=single&hideHeader=true&HelpMarker=emxhelpfullsearch&sortColumnName=ProjectCalendar&sortDirection=descending";
	    		 strURL +="&objectId="+objectId;
	    		 strURL +="&submitURL=../common/AEFSearchUtil.jsp";
		    		strURL +="&fieldNameDisplay="+fieldNameDisplay;
		    		strURL +="&fieldNameActual="+fieldNameActual;
		    		strURL +="&fieldNameOID="+fieldNameOID;
	    		%>
	    			<script language="javascript">
	    				var url = "<%=strURL%>";	<%-- XSSOK --%>
	    				url = url + "&frameNameForField="+parent.name;
	    				showModalDialog(url);
	    			</script>
	    		<%
			}else if ("addCalendarToProject".equalsIgnoreCase(strMode)) 	{
	    	    String fieldNameDisplay = emxGetParameter(request,"fieldNameDisplay");
	    	    fieldNameDisplay = XSSUtil.encodeURLForServer(context, fieldNameDisplay);
	    	    String fieldNameActual = emxGetParameter(request,"fieldNameActual");
	    	    fieldNameActual = XSSUtil.encodeURLForServer(context, fieldNameActual);
	    	    String fieldNameOID = emxGetParameter(request,"fieldNameOID");
	    	    fieldNameOID = XSSUtil.encodeURLForServer(context, fieldNameOID);
	    	    String objectId = emxGetParameter(request,"objectId");

	    		 if(ProgramCentralUtil.isNullString(objectId)){
	    			 objectId = request.getParameter("parentOID");
	    		 }
	    		 objectId = XSSUtil.encodeURLForServer(context, objectId);
	    		 String strURL = "../common/emxFullSearch.jsp?field=TYPES=type_WorkCalendar&table=PMCProjectCalendarSearchTable&selection=multiple&hideHeader=true&submitURL=../programcentral/emxProgramCentralUtil.jsp?mode=addCalendarToProjectProcess&chooserType=CalendarChooser&fieldNameActual=Calendar&fieldNameDisplay=CalendarDisplay&suiteKey=ProgramCentral&SuiteDirectory=programcentral&HelpMarker=emxhelpfullsearch";
	    		 strURL +="&objectId="+objectId;

	    		%>
	    			<script language="javascript">
	    				var url = "<%=strURL%>";	<%-- XSSOK --%>
	    				showModalDialog(url, "812", "700", "true", "popup");
	    			</script>
	    		<%
			} else if ("addCalendarToProjectProcess".equalsIgnoreCase(strMode)) {

				    String[] calendarIds = emxGetParameterValues(request,"emxTableRowId");
			        calendarIds = ProgramCentralUtil.parseTableRowId(context, calendarIds);
			        String defaultCalendarId = emxGetParameter(request,"DefaultCalendar");
				    String projectID = emxGetParameter(request,"objectId");

				    DomainObject calendarObject    = DomainObject.newInstance(context);
				    DomainRelationship connnection = null;
				    ProjectSpace newProject =(ProjectSpace)DomainObject.newInstance(context,ProgramCentralConstants.TYPE_PROJECT_SPACE,DomainConstants.PROGRAM);
				    newProject.setId(projectID);
                    //StringList existingCalenderIDs= new StringList();
				   // StringList existingCalenderNames= new StringList();
					StringList existedSelectedCalendarsList = new StringList();
				    Map existingCalemdarMap = new HashMap();
					
				    MapList calendarList = newProject.getProjectCalendars(context);
					int iSize = calendarList.size();
				    for(int i=0;i<iSize;i++) {
			   			 Map calInfo = (Map) calendarList.get(i);
                         String calenderId = (String)calInfo.get(ProgramCentralConstants.SELECT_ID);
			   			 String calendarName =(String)calInfo.get(ProgramCentralConstants.SELECT_NAME);
			   			
			   			 //existingCalenderIDs.add(calenderId); //Storing connected calendar Ids.
			   			//existingCalenderNames.add(calendarName);
						existingCalemdarMap.put(calenderId, calendarName);
			   			 String relationshipName    = (String)calInfo.get(ProgramCentralConstants.KEY_RELATIONSHIP);
			   			 String connectionID = (String)calInfo.get(DomainRelationship.SELECT_ID);
			   			 if(ProgramCentralUtil.isNotNullString(defaultCalendarId) && ProgramCentralConstants.RELATIONSHIP_DEFAULT_CALENDAR.equalsIgnoreCase(relationshipName)){
			   				DomainRelationship.setType(context, connectionID, ProgramCentralConstants.RELATIONSHIP_CALENDAR);
                            //break; We cannnot do becasue we are collecting connected calendar ids
			   			 }
			   		}
					
					Set existingCalenderIDs= existingCalemdarMap.keySet();
					iSize = calendarIds.length;
				    for(int j=0; j<iSize; j++){
						String strCalendarId = calendarIds[j];
                        if(existingCalenderIDs.contains(strCalendarId)){
							existedSelectedCalendarsList.add((String)existingCalemdarMap.get(strCalendarId));
							continue;
						}
					    calendarObject.setId(strCalendarId);


						if(ProgramCentralUtil.isNotNullString(defaultCalendarId) && defaultCalendarId.equalsIgnoreCase(strCalendarId)){

							DomainRelationship.connect(context, newProject, ProgramCentralConstants.RELATIONSHIP_DEFAULT_CALENDAR, calendarObject);
						}else {
							DomainRelationship.connect(context, newProject, ProgramCentralConstants.RELATIONSHIP_CALENDAR, calendarObject);
						}
					}

				    //If default calendar is changed then we need to call the rollup
				    if(ProgramCentralUtil.isNotNullString(defaultCalendarId) && ProgramCentralUtil.isNotNullString(projectID)){
				    	
				    	//Added code to support Auto/Manual rollup feature 
				    	String projectSchedule = newProject.getSchedule(context);
				    	if(ProgramCentralUtil.isNullString(projectSchedule) 
				    			||ProgramCentralConstants.PROJECT_SCHEDULE_AUTO.equalsIgnoreCase(projectSchedule)){
				    	Task project = new Task(projectID);
				    	project.rollupAndSave(context);
				    }

				    }
				    boolean is3DSearchEnabled = ProgramCentralUtil.is3DSearchEnabled(context);
					if(!existedSelectedCalendarsList.isEmpty())
				    {
				    %>
				    <script language="javascript" type="text/javaScript">
				    
         			alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Calendar.AlreadyConnectedCalendar</framework:i18nScript>" +"<%= existedSelectedCalendarsList.toString()%>");
         			</script>

				    <%
				    }
				    %>
				 	<script language="javascript" type="text/javaScript">
				 	getTopWindow().getWindowOpener().refreshSBTable(getTopWindow().getWindowOpener().configuredTableName);
					<%if(!is3DSearchEnabled){%>
	       			getTopWindow().close();
	       			<%}%>
				 	</script>
					<%

			} else if ("removeCalendar".equalsIgnoreCase(strMode)) {

	     	                String calendarIdArray[] = emxGetParameterValues(request,"emxTableRowId");
				//calendarIdArray = FrameworkUtil.getSplitTableRowIds(calendarIdArray);
		  		calendarIdArray = ProgramCentralUtil.parseTableRowId(context, calendarIdArray);
		  		String sSelectedTableRowIds = FrameworkUtil.join(calendarIdArray,",");
		  		String projectID = XSSUtil.encodeURLForServer(context,emxGetParameter(request,"objectId"));

			    StringList calendarIdsList = new StringList();
			    for(int i=0; i<calendarIdArray.length;i++){
			    	calendarIdsList.add( XSSUtil.encodeURLForServer(context,calendarIdArray[i]));
			    }

			    boolean isDefault=false;

			    ProjectSpace project = (ProjectSpace) DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.PROGRAM);
		  	 	project.setId(projectID);
		  	 	String defaultCalendarId = project.getInfo(context, "from["+ProgramCentralConstants.RELATIONSHIP_DEFAULT_CALENDAR+"].to.id");

		  	 	if(ProgramCentralUtil.isNotNullString(defaultCalendarId) && calendarIdsList.contains(defaultCalendarId)){
		  	 		isDefault = true;
		  	 	}


				if(isDefault){
		  	 	 	 String strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=removeDefaultCalender&calIds="+sSelectedTableRowIds+"&projectID="+projectID;
	     	      	 %>
	 			  	<script language="javascript" type="text/javaScript">
	 			  	var result = confirm("<framework:i18nScript localize="i18nId">emxProgramCentral.Calendar.DefaultCalendarRemoveWarningMsg</framework:i18nScript>");
	     		  	if(result){
	     				  var URL = "<%=strURL%>";
	     			 	 document.location.href = URL;
	     	      	}
	     		 	</script><%
	  	 	  	} else {
		  	 		project.removeCalendars(context, calendarIdsList);
			    %>
			 	<script language="javascript" type="text/javaScript">
			 	 	var topFrame = findFrame(getTopWindow(), "frameFieldCalendar");	
				 	if(topFrame == null){
				 		topFrame = findFrame(getTopWindow(), "detailsDisplay");
			    		topFrame.location.href = topFrame.location.href; 		
				 	}else{
				 		topFrame.refreshSBTable(topFrame.configuredTableName);
				 	}
			 	</script>
				  <%
	  	 	  	}

			}else if("removeDefaultCalender".equalsIgnoreCase(strMode)) {

			     String calIds = emxGetParameter(request, "calIds");
			     String projectID = emxGetParameter(request, "projectID");
				 StringList calendarIdsList = FrameworkUtil.splitString(calIds,",");
				 ProjectSpace projectSpace = (ProjectSpace) DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.PROGRAM);
				 projectSpace.setId(projectID);
				 projectSpace.removeCalendars(context, calendarIdsList);

				 //If default calendar is removed then we need to call the rollup
				    if(ProgramCentralUtil.isNotNullString(projectID)){
				    	
				    	//Added code to support Auto/Manual rollup feature 
				    	String projectSchedule = projectSpace.getSchedule(context);
				    	if(ProgramCentralUtil.isNullString(projectSchedule)
				    			||ProgramCentralConstants.PROJECT_SCHEDULE_AUTO.equalsIgnoreCase(projectSchedule)){
				    	Task project = new Task(projectID);
				    	project.rollupAndSave(context);
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
				  } else if("removeDefaultProjectCalendar".equalsIgnoreCase(subMode)){
					  
					  DomainRelationship.setType(context, sRelId, ProgramCentralConstants.RELATIONSHIP_CALENDAR);
				  }
				    
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
			}else if ("DnDWarningMsg".equalsIgnoreCase(strMode)) {
				String warnMsg = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.DragAndDrop.WarningMessage1",
																							languageStr);
				JsonObject jsonObject = Json.createObjectBuilder()
						                .add("error",warnMsg)
						                .build();

				out.clear();
				out.write(jsonObject.toString());
				return;
			}else if ("DragAndDrop".equalsIgnoreCase(strMode)) {
				String targetObjectId 		= emxGetParameter(request,"targetObjectId");
				String targetObjectLevel 	= emxGetParameter(request,"targetObjectLevel");
				String targetObjectType 	= emxGetParameter(request,"targetObjectType");
				String parentDragObjectId 	= emxGetParameter(request,"parentDragObjectId");
				String parentDropObjectId 	= emxGetParameter(request,"parentDropObjectId");

				String draggedObjectId 		= emxGetParameter(request,"draggedObjectId");
				String draggedObjectLevel 	= emxGetParameter(request,"draggedObjectLevel");
				String draggedObjectType 	= emxGetParameter(request,"draggedObjectType");

				Map <String,String>programMap = new HashMap();
				programMap.put("targetObjectId",targetObjectId);
				programMap.put("targetObjectLevel",targetObjectLevel);
				programMap.put("targetObjectType",targetObjectType);
				programMap.put("draggedObjectId",draggedObjectId);
				programMap.put("draggedObjectLevel",draggedObjectLevel);
				programMap.put("draggedObjectType",draggedObjectType);
				programMap.put("parentDragObjectId",parentDragObjectId);
				programMap.put("parentDropObjectId",parentDropObjectId);

				String []args 				= JPO.packArgs(programMap);
				Map<String,String> validMap = ProgramCentralUtil.isValidDnDOperation(context, args);
				String action 				= validMap.get("Action");
				String errorMsg 			= validMap.get("Error");

				JsonObject jsonObject = Json.createObjectBuilder()
						                .add("status",action)
						                .add("error",errorMsg)
						                .build();

				out.clear();
				out.write(jsonObject.toString());
				return;

		} else if ("updateSessionWithSummaryTasks".equalsIgnoreCase(strMode)) {
			StatusReport report = (StatusReport) session.getAttribute("store");
			MapList mlSummaryTasks = (MapList)report.getWBSSummaryTasks();
			session.putValue("objectList", mlSummaryTasks);
		    return;
		} else if ("getDueTasks".equalsIgnoreCase(strMode)) {
			MapList finalObjectList = new MapList();
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
			for(int i = 0; i<mlObjectSize; i++){
				Map objectMap = (Map)mlObjects.get(i);
				if(!(finalObjectList.contains(objectMap))){
					finalObjectList.add(objectMap);
				}
			}
			session.putValue("objectList", finalObjectList);
			return;
		} else if ("getBadChars".equalsIgnoreCase(strMode)) {
		    String sInvalidChars = EnoviaResourceBundle.getProperty(context, "emxFramework.Javascript.NameBadChars");
		    sInvalidChars = sInvalidChars.trim();
		    JsonObject jsonObject = Json.createObjectBuilder()
		    		                .add("badChars", sInvalidChars)
		    		                .build();
			out.clear();
			out.write(jsonObject.toString());
			return;
		}else if ("getBadCharsNotName".equalsIgnoreCase(strMode)) {
		    String sInvalidChars = EnoviaResourceBundle.getProperty(context, "emxFramework.Javascript.BadChars");
		    sInvalidChars = sInvalidChars.trim();
		    JsonObject jsonObject = Json.createObjectBuilder()
		    		                .add("badChars", sInvalidChars)
		    		                .build();
			out.clear();
			out.write(jsonObject.toString());
			return;
		}else if("isRiskSelected".equalsIgnoreCase(strMode)){
			 String sErrMsg  = "";
			 String[] selectedids = request.getParameterValues("emxTableRowId");
			 String projectId = emxGetParameter( request, "parentOID");
	   			String errorMessage = null;
	   			String riskCreationURL = null;
    		StringList requestIdList = new StringList();
    		boolean isInvalidRequest = false;

    		if (selectedids != null && selectedids.length != 0) {
	   			   if (selectedids.length == 1) {
	   					String riskId =  ProgramCentralUtil.parseTableRowId(context,selectedids)[0];
	   					boolean isOfRiskType =
	 	    				   ProgramCentralUtil.isOfGivenTypeObject(context,DomainConstants.TYPE_RISK,riskId);
	   					if (isOfRiskType) {
	   						StringList busSelects = new StringList();
	   						busSelects.add(ProgramCentralConstants.SELECT_CURRENT);
	   						DomainObject domObj = DomainObject.newInstance(context, riskId);
	   						Map infoMap = domObj.getInfo(context, busSelects);
	   						String riskCurrentState = (String) infoMap.get(DomainConstants.SELECT_CURRENT);
	   						if("Complete".equalsIgnoreCase(riskCurrentState)){
	   							//To disallow creating RPN under the Risk in complete state
	   						errorMessage = ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.RPN.RiskInCompleteState",sLanguage);
		   						%>
			 	   			  	<script language="javascript" type="text/javaScript">
		         					alert("<emxUtil:i18nScript localize="i18nId">emxProgramCentral.RPN.RiskInCompleteState</emxUtil:i18nScript>");
		         					var topFrame = findFrame(getTopWindow(),"PMCProjectRisk");
		         	     		 	 if(null == topFrame){
		         	     		 		topFrame = findFrame(getTopWindow(),"detailsDisplay");
		         	     		 	 }
		         	    	         topFrame.location.href = topFrame.location.href; 
		         				</script>
			 	   			  	<%
			 	   			return;
	   						}
	   						
	   						projectId = XSSUtil.encodeURLForServer(context,projectId);
		   					riskId = XSSUtil.encodeURLForServer(context,riskId);
		   					String submitLabel = ProgramCentralUtil.getPMCI18nString(context,"emxCommonButton.OK",sLanguage);
		   					riskCreationURL =
		   							"../common/emxForm.jsp?mode=edit&form=CreateNewRPN&portalMode=false&suiteKey=ProgramCentral&targetLocation=slidein&formHeader=emxProgramCentral.Risk.CreateRPN&PrinterFriendly=false&HelpMarker=emxhelprpncreatedialog&postProcessJPO=emxRPNBase:createNewRPN&findMxLink=false"+
		   			   						"&objectId="+riskId+"&postProcessURL=../programcentral/emxProgramCentralUtil.jsp?mode=createRiskRPN&submitAction=doNothing&submitLabel="+submitLabel;

	   					} else {
	   	   				 errorMessage = ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Common.PleaseSelectOneRisk",sLanguage);
	 	   			  %>
	 	   			  	<script language="javascript" type="text/javaScript">
         					alert("<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Common.PleaseSelectOneRisk</emxUtil:i18nScript>");
         				</script>
	 	   			  <%
	 	   			 return;
	 	   			 	}
	   			   }
	   		   	}

				if (ProgramCentralUtil.isNullString(errorMessage)) {
	   				%>
	     		 	<script language="javascript" type="text/javaScript">
	     		 	 	var riskCreationURL = "<%=XSSUtil.encodeForJavaScript(context,riskCreationURL)%>";
	     		 	 	getTopWindow().showSlideInDialog(riskCreationURL,true);
	     		 	</script>
	     			<%
	   		   	}
		}else if("quickCreateRisk".equalsIgnoreCase(strMode)){
				String[] selectedIds = request.getParameterValues("emxTableRowId");
	        	String projectId = emxGetParameter( request, "parentOID");
	        	String objectId = emxGetParameter( request, "objectId");
	        	String errorMessage = ProgramCentralConstants.EMPTY_STRING;
	        	String xmlMessage = ProgramCentralConstants.EMPTY_STRING;

	        	if(!ProgramCentralUtil.isNullString(objectId)){
	        		if(ProgramCentralUtil.isNullString(projectId) || objectId != projectId){
	        			projectId = objectId;
	        		}
	        	}

	        	if (selectedIds != null && selectedIds.length != 0) {
		   			if (selectedIds.length >= 1) {
			   				String selectedId =  ProgramCentralUtil.parseTableRowId(context,selectedIds)[0];
		   					boolean isOfRiskType =  ProgramCentralUtil.isOfGivenTypeObject(context,DomainConstants.TYPE_RISK,selectedId);
		   					boolean isOfRPNType =   ProgramCentralUtil.isOfGivenTypeObject(context,DomainConstants.TYPE_RPN,selectedId);
		   					if ((isOfRiskType || isOfRPNType)) {

		   						errorMessage = ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Project.SelectProject",sLanguage);
				 	   			  %>
				 	   			  	<script language="javascript" type="text/javaScript">
			        					alert("<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Risk.SelectNodeForCreateRisk</emxUtil:i18nScript>");
			        					getTopWindow().closeSlideInDialog();
			        				</script>
				 	   			  <%
				 	   			 return;
		   					}
		   			   }
		   		}

	        	boolean isOfProjectType = ProgramCentralUtil.isOfGivenTypeObject(context,DomainConstants.TYPE_PROJECT_SPACE,projectId);
	        	Risk risk = new Risk();
	        	String name = new DomainObject().getUniqueName("R-");
	            risk.create(context, name,risk.POLICY_PROJECT_RISK);
	            String riskId = risk.getId(context);
				String timezone = (String)session.getValue("timeZone");
				String rValue = "1";

            	int eDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
       			java.text.DateFormat format = DateFormat.getDateTimeInstance(eDateFormat, eDateFormat, Locale.US);
            	Date  currentDate =  new Date();
            	String eDate=format.format(currentDate);
    			double clientTZOffset = new Double(timeZone).doubleValue();

            	Map paramMap = new HashMap();
            	paramMap.put("objectId", riskId);
            	paramMap.put("languageStr", sLanguage);

            	Map requestMap = new HashMap();
            	requestMap.put("mode", "quickCreateRisk");
            	requestMap.put("Probability",rValue);
            	requestMap.put("Impact",rValue);
            	requestMap.put("RPN", rValue);
            	requestMap.put("timeZone",timezone);
            	requestMap.put("EffectiveDate", eDate);
            	requestMap.put("timeZone",timezone);
            	requestMap.put("EstimatedEndDate", eDate);
            	requestMap.put("EstimatedStartDate", eDate);
            	requestMap.put("localeObj", Locale.US);
            	requestMap.put("parentOID", projectId);


            	Map ProgramMap = new HashMap();
            	ProgramMap.put("paramMap",paramMap);
            	ProgramMap.put("requestMap",requestMap);
            	Risk riskObject = new Risk();

            	riskObject = risk.createRisk(context, ProgramMap);
            	String rpnRiskRelId = riskObject.getInfo(context,"to[Risk].id");

    	       	xmlMessage = "<mxRoot><action><![CDATA[add]]></action>";
    	       		   	xmlMessage +="<data status=\"committed\" fromRMB=\"" + false + "\">";
    	       		   	xmlMessage +="<item oid=\"" + riskId + "\" relId=\"" + rpnRiskRelId + "\" pid=\"" + projectId + "\"/>";
    	       			xmlMessage +="</data></mxRoot>";
     	    	%>
     		 	<script language="javascript" type="text/javaScript">
     		 	if(<%=isOfProjectType%> == true){
     		 	var topFrame = findFrame(getTopWindow(),"PMCProjectRisk");
     		 	if(null == topFrame)
        	  		topFrame = findFrame(getTopWindow(), "frameDashboard");
     		 	}else{
     		 		var topFrame = findFrame(getTopWindow(),"detailsDisplay");
     		 	}
    	            topFrame.emxEditableTable.addToSelected('<%=XSSUtil.encodeForJavaScript(context,xmlMessage)%>');
    	            topFrame.location.href = topFrame.location.href; 
     		 	</script>
     		  <%

     } else if ("businessUnitSelectorForEditDetails".equalsIgnoreCase(strMode)) {
			String fieldNameDisplay = request.getParameter("fieldNameDisplay");
		    fieldNameDisplay = XSSUtil.encodeURLForServer(context, fieldNameDisplay);
		    String fieldNameActual = request.getParameter("fieldNameActual");
		    fieldNameActual = XSSUtil.encodeURLForServer(context, fieldNameActual);
		    String fieldNameOID = request.getParameter("fieldNameOID");
		    fieldNameOID = XSSUtil.encodeURLForServer(context, fieldNameOID);
		    String url="../common/emxFullSearch.jsp?field=TYPES=type_Company,type_BusinessUnit&table=PMCOrganizationSummary&hideHeader=true&selection=single&submitAction=refreshCaller&showInitialResults=true";
		    url +="&submitURL=../common/AEFSearchUtil.jsp";
		    url +="&fieldNameDisplay="+XSSUtil.encodeURLForServer(context,fieldNameDisplay);
		    url +="&fieldNameActual="+XSSUtil.encodeURLForServer(context,fieldNameActual);
		    url +="&fieldNameOID="+XSSUtil.encodeURLForServer(context,fieldNameOID);
		    %>
	          <script language="javascript">
		        <%-- XSSOK--%>
		      	var strUrl ="<%=url%>";
		      	strUrl = strUrl + "&frameNameForField="+parent.name;
		  	 	showModalDialog(strUrl);
	    	  </script>
		   <%
		} else if("createQuickFolder".equalsIgnoreCase(strMode)){
				String portalCommandName 	= XSSUtil.encodeURLForServer(context,(String)emxGetParameter(request, "portalCmdName"));
    		    boolean isRefresh 			= true;
    		    String xmlMessage 			= DomainObject.EMPTY_STRING;
    		    StringList newFolderIdList 	= new StringList();
    			String tableRowId 			= request.getParameter("emxTableRowId");
    			Map rowIdMap 				= ProgramCentralUtil.parseTableRowId(context, tableRowId);
    			String parentId 			= (String)rowIdMap.get("objectId");
    			String rowId 				= (String)rowIdMap.get("rowId");
    			String relationship 		= DomainConstants.RELATIONSHIP_PROJECT_VAULTS;

    			String folderAccessType 	= request.getParameter("PMCFolderInheritAccess");
    			String selectedFolderType 	= request.getParameter("PMCFolderTypeToAddBelow");
    			String folderToAdd 			= request.getParameter("PMCFolderToAddBelow");
    			int nfolderToAdd 			= 1;

                        boolean isOfProjectTemplateType = ProgramCentralUtil.isOfGivenTypeObject(context, DomainConstants.TYPE_PROJECT_TEMPLATE,objId);

    			if(selectedFolderType.equalsIgnoreCase("Workspace Folder")){
    				selectedFolderType = DomainObject.TYPE_WORKSPACE_VAULT;
    			}

    			try{
    				nfolderToAdd = Integer.parseInt(folderToAdd);
    			}catch(Exception e){
    				nfolderToAdd = 1;
    			}

    			if(ProgramCentralUtil.isNullString(parentId)){
    				parentId = request.getParameter("parentOID");
    			}

    			if(ProgramCentralUtil.isNotNullString(parentId)){
    				JsonObjectBuilder folderInfoBuilder 		= Json.createObjectBuilder();
    				DomainObject parentObject 	= DomainObject.newInstance(context, parentId);

    				try{
    					PMCWorkspaceVault folder 	= new PMCWorkspaceVault();

    					StringList selectable 		= new StringList(2);
    					selectable.addElement(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
    					selectable.addElement(ProgramCentralConstants.SELECT_KINDOF_PROJECT_MANAGEMENT);
    					selectable.addElement(DomainObject.SELECT_TYPE);
						selectable.addElement(DomainObject.SELECT_CURRENT);

    					Map parentObjectInfoMap = parentObject.getInfo(context, selectable);
    					String parentType 		= (String)parentObjectInfoMap.get(DomainObject.SELECT_TYPE);
    					String isWorkspaceVault = (String)parentObjectInfoMap.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
    					String isProjectManagement = (String)parentObjectInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_MANAGEMENT);
						String currentState 	= (String)parentObjectInfoMap.get(ProgramCentralConstants.SELECT_CURRENT);

    					if (! ("True".equalsIgnoreCase(isWorkspaceVault) || "True".equalsIgnoreCase(isProjectManagement))) {     
    						String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Folders.SelectProjectOrFoldersOnly", languageStr);
  					             %>
    					       <script language="JavaScript" type="text/javascript">
    					        alert("<%=XSSUtil.encodeForJavaScript(context,sErrMsg)%>");
    					       </script>
    					        <%return;
    					    }

    					folderInfoBuilder.add("parentId", parentId);
    					folderInfoBuilder.add("parentType", parentType);
    					folderInfoBuilder.add("folderType", selectedFolderType);
    					folderInfoBuilder.add("folderToAdd", String.valueOf(nfolderToAdd));
    					folderInfoBuilder.add("folderAccessType", folderAccessType);
    					folderInfoBuilder.add("isWorkspaceVault", isWorkspaceVault);
	    				folderInfoBuilder.add("rowId", rowId);
	    				folderInfoBuilder.add("relationship", relationship);
	    				JsonObject folderInfo = folderInfoBuilder.build();

	    				if("true".equalsIgnoreCase(isWorkspaceVault)){
	    					relationship = DomainConstants.RELATIONSHIP_SUB_VAULTS;

	    					if(selectedFolderType.equalsIgnoreCase(parentType)){
								if(currentState.equals(ProgramCentralConstants.STATE_CONTROLLED_FOLDER_RELEASE) && selectedFolderType.equalsIgnoreCase(ProgramCentralConstants.TYPE_CONTROLLED_FOLDER)){
								String warnMsg = EnoviaResourceBundle.getProperty(context,"ProgramCentral",
	    									"emxProgramCentral.Folders.PasteObject.NoConnectInsideControlledFolderInReleaseStateNotice", languageStr);
		    						 isRefresh = false;
			    						%>
			    		    			<script language="javascript">
			    		    				var alertMsg = "<%=warnMsg%>";
			    			    			alert(alertMsg);
			    		    			</script>
			    		    			<%
	    							
	    						} else {
									newFolderIdList = folder.create(context,folderInfo);
								}
	    						 
	    					}else{
	    						 String warnMsg = ProgramCentralUtil.getPMCI18nString(context,
	    								"emxProgramCentral.QuickFolder.WarningMessage", languageStr);
	    						 isRefresh = false;
	    						%>
	    		    			<script language="javascript">
	    		    				var alertMsg = "<%=warnMsg%>";
	    			    			alert(alertMsg);
	    		    			</script>
	    		    			<%
	    					}
	    				}else{
	    					 newFolderIdList = folder.create(context,folderInfo);
	    				}


                        //NX5 - #6868 Quick create toolbar on Template not setting Co-Owner access
                        // Template revision is not happening
   							//if(isOfProjectTemplateType) {
                            for ( Object obj: newFolderIdList) {
                                String id = (String) obj;
                                DomainObject objFolder 	= DomainObject.newInstance(context, id);
                                HashMap programMap = new HashMap();
                                HashMap paramMap = new HashMap();
                                HashMap requestMap = new HashMap();

                                paramMap.put("objectId",  id);

                                // Transaztion - object is returning "auto_11111" name type
                             // requestMap.put("Name",       (String) objFolder.getInfo(context, DomainConstants.SELECT_NAME));
								String sName = DomainConstants.EMPTY_STRING;
								
								StringList busSelects = new StringList();
								busSelects.add(DomainConstants.SELECT_NAME);
								busSelects.add(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
								busSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TITLE);
								
								Map taskInfoMap = objFolder.getInfo(context, busSelects);
								String isKindOfWorkSpaceVault = (String)taskInfoMap.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);

                                if("true".equalsIgnoreCase(isKindOfWorkSpaceVault)){
                                	sName = (String)taskInfoMap.get(DomainConstants.SELECT_NAME);
                                }
                                else{
                                	sName = (String)taskInfoMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TITLE);
                                }
                                requestMap.put("Name",       sName);
                                requestMap.put("AccessType", folderAccessType);
                                requestMap.put("Policy",     selectedFolderType);

                                requestMap.put("parentOID", parentId);         // The Project Template
                                requestMap.put("IgnoreNameChk", "true");    // Added for this invocation of JPO

                                // requestMap.put("IsClone", "false");
                                // requestMap.put("emxTableRowId", "true");
                                // requestMap.put("DefaultAccess", null);    // Can be null
                                // requestMap.put("Description", "");
                                // requestMap.put("oldObjectId", "true");  // Only if isClone

                                programMap.put("paramMap", paramMap);
                                programMap.put("requestMap", requestMap);

                                String[] progArgs;
                                progArgs = JPO.packArgs(programMap);
                                JPO.invoke(context, "emxProjectFolder", null, "postProcessActions", progArgs);
                            }
                  //      }
                        // NX5 - End

	    				xmlMessage = folder.refreshFolderStructure(context, parentObject, newFolderIdList, folderInfo);

    				}catch(Exception e){
    					isRefresh = false;
    					e.printStackTrace();
    				}
    			}
    			%>
    			<script language="javascript">
    				var isRefreshes = "<%=isRefresh%>";
    				if(isRefreshes=="true"){
    					var topFrame = findFrame(getTopWindow(), "detailsDisplay");
    					if(topFrame.emxEditableTable==undefined || topFrame.emxEditableTable==null){
    						var frameName = "<%=portalCommandName%>";
    						topFrame = findFrame(getTopWindow(), frameName);
    					}
    					if(topFrame.emxEditableTable!=undefined && topFrame.emxEditableTable!=null){
    						topFrame.emxEditableTable.addToSelected('<%=xmlMessage%>');
           					topFrame.refreshStructureWithOutSort();
    					}
    				}
    			</script>
    			<%
    		} else if("chainTask".equalsIgnoreCase(strMode)){

				String strSelectedIds = (String) emxGetParameter(request, "selectedIds");
				StringList selectedIds = FrameworkUtil.split(strSelectedIds, ",");
				String currentFrame = (String) emxGetParameter(request, "currentFrame");
				String projectId = (String) emxGetParameter(request, "parentOID");
				Map<String,String> projectScheduleMap = ProgramCentralUtil.getProjectSchedule(context, projectId);
				String projectSchedule = projectScheduleMap.get(projectId);
				Task task = new Task();
				Map errorMassageMap = task.chainWBSTask(context, selectedIds);

				if(!errorMassageMap.isEmpty()){
					Object errorMessage = errorMassageMap.get("error");

	    		 %>
	    		 	<script language="javascript" type="text/javaScript">
 		 				alert("<%=XSSUtil.encodeForJavaScript(context,errorMessage.toString())%>");
 		 			</script>
	    		  <%
	    		 return;
				}
%>
				<script language="javascript" type="text/javaScript">
					var schedule = "<%=projectSchedule%>";
				 var portalName = "<%=currentFrame%>";
				 var topFrame = findFrame(getTopWindow(), portalName);

  				if(topFrame == null){
  					topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
  					if(null == topFrame){
	     				    topFrame = findFrame(getTopWindow(), "detailsDisplay");
  					}
  				}
				if("Manual"==(schedule)){
					top.jQuery('.fonticon-refresh').addClass('fonticon-refresh-info');
					toggleRollupIcon(topFrame,"iconActionUpdateDates","iconActionUpdateDatesActive");
				  }
				
				topFrame.emxEditableTable.refreshStructureWithOutSort();
					//parent.rebuildView();
				</script>
<%
	    } else if("deleteRelatedProject".equalsIgnoreCase(strMode)){

         			String[] emxTableRowId = emxGetParameterValues(request, "emxTableRowId");
         			if ( emxTableRowId != null )
    			       {
    			    	   String projId="";
    			           String relId = "";
    			           String intermediate="";
    			           int numProjects = emxTableRowId.length;
    			           int index;
    			           DomainObject domObj;
    			           String projectIdList[] = new String[numProjects];
    			           String state = DomainConstants.EMPTY_STRING;
    			           boolean beyondCreate =false;
    			           String beyondcreateprojects = DomainConstants.EMPTY_STRING;
    			           StringList selectable =new StringList();
    			           selectable.add(DomainConstants.SELECT_CURRENT);
    			           selectable.add(DomainConstants.SELECT_NAME);
    			           selectable.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_CONCEPT);
    			           boolean flag=false;
    			           for (int i=0; numProjects>i; i++)
    			           {
    			             if(emxTableRowId[i].indexOf("|") != -1 )
    			             {
    			                  relId = emxTableRowId[i].substring(0,emxTableRowId[i].indexOf("|"));
    			                  if("".equalsIgnoreCase(relId))
    			                  {
    			                	  flag = true;
    			                      break;
    			                  }else if(!"".equalsIgnoreCase(relId))
    			                  {
    			                    index=emxTableRowId[i].indexOf("|");
    			                    intermediate=emxTableRowId[i].substring(index+1,emxTableRowId[i].lastIndexOf("|"));
    			                    projId=intermediate.substring(0,intermediate.indexOf("|"));
    			                    projectIdList[i]=projId;
    			                  }
    			             }
    			          }
    			           if(flag){
      	    	        	 %>
  	    	                   <script language="javascript" type="text/javaScript">
  	    	                    alert("<framework:i18nScript localize="i18nId">emxProgramCentral.RootProjectDelete</framework:i18nScript>");
  	    	                   </script>
  	    	                <%
  	    	             } else {

    			           try{
    			        	    BusinessObjectWithSelectList bwsl = ProgramCentralUtil.getObjectWithSelectList(context, projectIdList, selectable);
    			        	    for(int j=0, bsize = bwsl.size(); j <bsize ; j++){
    				  	 			BusinessObjectWithSelect bws = bwsl.getElement(j);
    				  	 			state = bws.getSelectData(DomainConstants.SELECT_CURRENT);//(String)info.get(DomainConstants.SELECT_CURRENT);
    				  	 			String isProjectConcept = bws.getSelectData(ProgramCentralConstants.SELECT_KINDOF_PROJECT_CONCEPT);
    				  	 			beyondCreate = !(ProgramCentralConstants.STATE_PROJECT_SPACE_CREATE.equalsIgnoreCase(state)
    				  	 					|| ("TRUE".equalsIgnoreCase(isProjectConcept) && ProgramCentralConstants.STATE_PROJECT_CONCEPT_CONCEPT.equalsIgnoreCase(state))) ;
    				  	 			if(beyondCreate){
    				  	 				beyondcreateprojects +=  bws.getSelectData(DomainConstants.SELECT_NAME) +"\\n";
    				  	 			}
    				  	 		}
    			        	    if(beyondCreate){
    			        		   %>
			                        <script language="javascript" type="text/javaScript">
			                        alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Project.UnableToDeleteProjects</framework:i18nScript>\n\n<%=beyondcreateprojects%>");
			                        </script>
			                      <%
    			        	    }else{
			                    DomainObject.deleteObjects(context,projectIdList);
			                    %>
			                    <script language="javascript" type="text/javaScript">
			                    var topFrame = findFrame(getTopWindow(), "PMCRelatedProjects");
			    	    		topFrame.location.href = topFrame.location.href;
			                     </script>
			                    <%
    			        	   }
			               } catch(Exception e) {
			                    	%>
			                        <script language="javascript" type="text/javaScript">
			                        alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Project.UnableToDeleteProjects</framework:i18nScript>");
			                        </script>
			                        <%
			               }
    	            }
   	        }
       } else  if("removeRelatedProject".equalsIgnoreCase(strMode)) {
    	    	String[] emxTableRowId = emxGetParameterValues(request, "emxTableRowId");
    	   	    final String RELATIONSHIP_RELATED_PROJECTS = PropertyUtil.getSchemaProperty (context, "relationship_RelatedProjects");
    	    	boolean flag=false;
    	    	      if ( emxTableRowId != null )
    	    	        {
    	    	          int numProjects = emxTableRowId.length;
    	    	          String strProjObjId = "";
    	    	           flag=false;
    	    	           String projectIdList[] = new String[numProjects];
    	    	           for (int i=0; numProjects>i; i++)
    	    	           {
    	    	              if(emxTableRowId[i].indexOf("|") != -1 )
    	    	              {
    	    	              strProjObjId = emxTableRowId[i].substring(0,emxTableRowId[i].indexOf("|"));
    	    	                if(strProjObjId.equalsIgnoreCase(""))
    	    	                {
    	    	                  flag=true;
    	    	                  break;
    	    	                }
    	    	              } else {
    	    	              strProjObjId = emxTableRowId[i];
    	    	              }
    	    	              projectIdList[i] =strProjObjId;
    	    	         }
    	    	         if(flag){
    	    	        	 %>
	    	                   <script language="javascript" type="text/javaScript">
	    	                   alert("<framework:i18nScript localize="i18nId">emxProgramCentral.RootProjectRemove</framework:i18nScript>");
	    	                   </script>
	    	                <%
	    	             }else if(!flag){
   	         	           DomainRelationship.disconnect(context, projectIdList);
   	         	           %>
   	         	           <script language="javascript" type="text/javaScript">
    	    			   var topFrame = findFrame(getTopWindow(), "PMCRelatedProjects");
						   topFrame.location.href = topFrame.location.href;
						  </script>
   	         	          <%
   	    	            }
    	    	    }
	} else  if("deleteCalender".equalsIgnoreCase(strMode)) {

			String accessUsers = "role_OrganizationManager,role_CompanyRepresentative,role_VPLMAdmin";
			if( !PersonUtil.hasAnyAssignment(context, accessUsers) ) {
				return;
			}
			 String calendarIdArray[] = emxGetParameterValues(request,"emxTableRowId");
			 boolean hasTaskConected = false;
			 String taskId = DomainConstants.EMPTY_STRING;
		  	 StringList selectableList = new StringList();
		  	 selectableList.add("to[Calendar].from.id");

		  	 if(calendarIdArray != null) {

		  		calendarIdArray = FrameworkUtil.getSplitTableRowIds(calendarIdArray);
		  		String sSelectedTableRowIds = FrameworkUtil.join(calendarIdArray,",");

	  	 	  	MapList infoList = DomainObject.getInfo(context, calendarIdArray, selectableList);

	  	 		for(int j=0; j<infoList.size(); j++) {
	  	 			Map info = (Map)infoList.get(j);
	  	 			taskId = (String)info.get("to[Calendar].from.id");
	  	 			if(taskId != null) {
	  	 				hasTaskConected=true;
	  	 				break;
	  	 		 	}
	  	 		}
	  	 	  	if(hasTaskConected){
		  	 	 	 String strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=deleteCalenderAction&calIds="+sSelectedTableRowIds;
	     	      	 %>
	 			  	<script language="javascript" type="text/javaScript">
	 			  	var result = confirm("<framework:i18nScript localize="i18nId">emxProgramCentral.Common.UnableToDeleteCalendar</framework:i18nScript>");
	     		  	if(result){
	     				  var URL = "<%=strURL%>";
	     			 	 document.location.href = URL;
	     	      	}
	     		 	</script><%
	  	 	  	} else {
		  	 	  	DomainObject.deleteObjects(context,calendarIdArray);
		  	 	 	%>
				  	 <script language="Javascript">
		 	  	  	 parent.location.href = parent.location.href;
		           	</script><%
	  	 	  	}
		 	}
   } else if("deleteCalenderAction".equalsIgnoreCase(strMode)) {

		     String calIds = emxGetParameter(request, "calIds");
			 StringList calenderIdList = FrameworkUtil.splitString(calIds,",");
			 int length = calenderIdList.size();
			 String[] resultStringList=new String[length];
			 for(int i=0; i<length; i++){
					 resultStringList[i]=(String)calenderIdList.get(i);
			 }
			 try {
				 ProgramCentralUtil.pushUserContext(context);
				 DomainObject.deleteObjects(context, resultStringList);
			 } finally {
				 ProgramCentralUtil.popUserContext(context);
			 }
	    %>
		    <script language="Javascript">
	 		parent.location.href = parent.location.href;
		    </script>
<%
		} else if("contentReport".equalsIgnoreCase(strMode)){
		String selectedIds="";
	        String formURL = "";
	   	String objectId    = emxGetParameter(request,"objectId");
		String[] tableRowIds = emxGetParameterValues(request,"emxTableRowId");
		String[] strProjectVaultRowIds = emxGetParameterValues(request, "emxTableRowId");
		String[] ProjectVaultTokensArr = new String[4];
		String strProjectVaultId = null;
		DomainObject domainObject = new DomainObject();
		domainObject.setId(objectId);
		int size = strProjectVaultRowIds.length;
		String IS_KINDOF_WORKSPACE_VAULT = "type.kindof["+ DomainConstants.TYPE_WORKSPACE_VAULT + "]";

		String[] vaultIds = new String[size];
		for (int i = 0; i < size; i++) {
			  ProjectVaultTokensArr = strProjectVaultRowIds[i].split("\\|");
			  strProjectVaultId = ProjectVaultTokensArr[1];
			vaultIds[i] = strProjectVaultId;
		}

		StringList objectSelects = new StringList();
		objectSelects.add(DomainConstants.SELECT_ID);
		objectSelects.add(IS_KINDOF_WORKSPACE_VAULT);
		MapList objectList = domainObject.getInfo(context, vaultIds,objectSelects);
		  String objectType = (String)domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
	String rowIds = "";
		for (int i = 0; i < size; i++) {
			Map objectMap = (Map) objectList.get(i);
		if ("FALSE".equalsIgnoreCase((String)objectMap.get(IS_KINDOF_WORKSPACE_VAULT))) {
				String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.Common.SelectFoldersOnly",languageStr);
			              %>
						<script language="JavaScript" type="text/javascript">
							    alert("<%=XSSUtil.encodeForJavaScript(context,sErrMsg)%>");
							    window.closeWindow();
						</script>
		<%return;
			     }
		String vaultId = (String)objectMap.get(DomainConstants.SELECT_ID);
	if(i == 0){
		rowIds += XSSUtil.encodeForURL(context, vaultId);
	}else{
		rowIds += "," + XSSUtil.encodeForURL(context, vaultId);
	}
		  }
		CacheUtil.setCacheObject(context, "dpm_contentReport_selectedIds", rowIds);
		 if(ProgramCentralConstants.TYPE_PROJECT_TEMPLATE.equalsIgnoreCase(objectType)){
	 formURL="../common/emxForm.jsp?form=PMCProjectTemplateFolderContentReportForm&suiteKey=ProgramCentral&formHeader=emxProgramCentral.Common.FolderContentReport&objectId="+XSSUtil.encodeForURL(context, objectId)+"&HelpMarker=emxhelpfoldercontentreport";
		 }
		 else{
		 formURL="../common/emxForm.jsp?form=PMCProjectFolderContentReportForm&suiteKey=ProgramCentral&formHeader=emxProgramCentral.Common.FolderContentReport&objectId="+XSSUtil.encodeForURL(context, objectId)+"&HelpMarker=emxhelpfoldercontentreport";
		 }
		// response.sendRedirect(formURL);
		 %>
		<script language="javascript" type="text/javaScript">
			 var strUrl = "<%=formURL%>";
  			 document.location.href = strUrl;
		</script>
<%
   } else if("translateErrorMsg".equalsIgnoreCase(strMode)){
		String key = request.getParameter("key");
		String errorMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
				key, languageStr);
		JsonObject jsonObject = Json.createObjectBuilder()
				                .add("Error",errorMsg)
				                .build();

		out.clear();
		out.write(jsonObject.toString());
		return;
  } else if("importProjectSchedule".equalsIgnoreCase(strMode)){

	  	//ProjectSpace project =(ProjectSpace) DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
		//Context contextDB = (matrix.db.Context)request.getAttribute("context");
		FrameworkServlet framework = new FrameworkServlet();
		framework.removeFromGlobalCustomData(session, context, "fileContent");
		String portalCommandName = (String)emxGetParameter(request, "portalCmdName");
		portalCommandName = XSSUtil.encodeURLForServer(context, portalCommandName);
		String objectId = emxGetParameter(request,"objectId");
		MapList importObjList = (MapList)session.getAttribute("importList");
		
		String selectedTaskid = emxGetParameter(request,"selectedObjectId");
		
		Enumeration requestParams = emxGetParameterNames(request);
	    HashMap parameterMap = new HashMap();
	  	if(requestParams != null){
  		  while(requestParams.hasMoreElements()){
        		  String param = (String)requestParams.nextElement();
        		  String value = emxGetParameter(request,param);
        		  parameterMap.put(param, value);
  		  }
	  	}
		
	  	boolean isPMCFileCurrupted = (boolean)session.getAttribute("isFileCurrpted");
		session.removeAttribute("isFileCurrpted");
		if(!isPMCFileCurrupted){
		//String xmlMessage =DomainObject.EMPTY_STRING;
		Map<String,String> projectScheduleMap = ProgramCentralUtil.getProjectSchedule(context, objectId);
		String projectSchedule = projectScheduleMap.get(objectId);
		if(projectSchedule == null || projectSchedule.isEmpty()){
			projectSchedule = ProgramCentralConstants.PROJECT_SCHEDULE_AUTO;
		}
		
		//String loggedinUser = context.getUser();
		boolean isBackgoundImportOptionChecked = "TRUE".equalsIgnoreCase(emxGetParameter(request, "BackgoundImportOption"));
		 String strURL = "";
		 
		if(! isBackgoundImportOptionChecked){
		String cmd 		= "print $1";
	    String result 	= MqlUtil.mqlCommand(context, cmd, "transaction");
		session.removeAttribute("importList");
		
	    /* Enumeration requestParams = emxGetParameterNames(request);
	    HashMap parameterMap = new HashMap();
		  	if(requestParams != null){
	   		  while(requestParams.hasMoreElements()){
	       		  String param = (String)requestParams.nextElement();
	       		  String value = emxGetParameter(request,param);
	       		  parameterMap.put(param, value);
	   		  }
		  } */
			
 		Map projectWBSIdToLevelMap = (HashMap)session.getAttribute("ProjectImport_projectWBSIdToLevelMap");
 		parameterMap.put("projectWBSIdToLevelMap", projectWBSIdToLevelMap);
 		session.removeAttribute("ProjectImport_projectWBSIdToLevelMap");
		ProjectService.completeImportProcess(context, objectId, importObjList, parameterMap);
		}else{
			strURL = ProjectSpace.getImportProjectScheduleURL(context,parameterMap);
			
			/* strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=importCSVBackgroundJob&isBackgroundProcess=true&isCreateProjectOrCopyScheduleFromFile=copyScheduleFromFile&loggedInUser=" + loggedinUser ;
			
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
 		  	} */
		}
		
		/* String cmd 		= "print $1";
	    String result 	= MqlUtil.mqlCommand(context, cmd, "transaction");
		
		
		ProjectService.completeImportProcess(context, objectId, importObjList, request); */
		
		/* project.setId(objectId);
		project.completeImportProcess(context, objectId, importObjList); */
		
		
		if("Manual".equalsIgnoreCase(projectSchedule)){
		 String isWholePageRefreshRequierd="yes";
		CacheUtil.setCacheObject(context, "isWholePageRefreshRequierd", isWholePageRefreshRequierd);
	}
		%><script language="javascript" type="text/javaScript">
		
		 var frame = "<%=portalCommandName%>";
   	  var schedule = "<%=projectSchedule%>";
   	  var topFrame = findFrame(getTopWindow(), frame);
   	var strURL = "<%=strURL%>";

   	   if(topFrame == null || topFrame.location.href=="about:blank"){
	     		topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
	     		if(topFrame == null || topFrame.location.href=="about:blank"){
	     		topFrame = findFrame(getTopWindow(), "PMCWBS");
	     		if(topFrame == null || topFrame.location.href=="about:blank"){
	     			topFrame = findFrame(getTopWindow(), "PMCWBSDataGridView");
	     			if(topFrame == null || topFrame.location.href=="about:blank"){
	     				topFrame = findFrame(getTopWindow(), "PMCProjectTemplateWBS");	
	     				if(topFrame == null || topFrame.location.href=="about:blank"){
	     					topFrame = findFrame(getTopWindow(), "PMCProjectTemplateWBSDataGridView");
	     					if(topFrame == null || topFrame.location.href=="about:blank"){
						   			topFrame = findFrame(getTopWindow(), "detailsDisplay");
         	        			}
					   		}
	     				}
	     				
     	        	}
	     		}
		     }
   	  
   	var isBackgroundChecked = "<%= isBackgoundImportOptionChecked %>";
   	if(isBackgroundChecked == "true"){
  	  
	  	  emxUICore.getData(strURL);
  	  alert("<framework:i18nScript localize="i18nId">emxProgramCentral.Import.BackgoundImportOptionMessage</framework:i18nScript>");
    }
   	  
   	if("Manual" == schedule){

   		top.jQuery('.fonticon-refresh').addClass('fonticon-refresh-info');
   		toggleRollupIcon(topFrame,"iconActionUpdateDates","iconActionUpdateDatesActive");
		var selectedObjId = "<%=selectedTaskid%>";
		var nRow = emxUICore.selectSingleNode(topFrame.oXML, "/mxRoot/rows//r[@o = '" + selectedObjId + "']");
		if(nRow)
		{
			nRow.setAttribute("expandedLevels", 0);
			nRow.setAttribute("hChild", "true");
			var selectedObjRowId = new Array();
			selectedObjRowId.push(nRow.getAttribute("id"));
			topFrame.emxEditableTable.expand(selectedObjRowId, "1");
		}
   		
	  }else{
		  
		  if(!topFrame.dataGridEnabled){
			  topFrame.refreshSBTable(topFrame.configuredTableName);
		  
		  }else{
		   topFrame.location.href = topFrame.location.href;
		  }
	  }
	</script>
<%
		}else{
			String strErrorMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.ProjectTemplate.CannotCreateTemplateStructure", languageStr);
			%><script language="javascript" type="text/javaScript">
			{
				alert("<%= strErrorMsg %>");
			}
			</script><%
			}
 } else if("getScheduleTasksPreview".equalsIgnoreCase(strMode)) {

		String selectedProjectId = emxGetParameter(request,"searchProjectId");
	  	StringList sourceTaskIdList = FrameworkUtil.split(selectedProjectId,"|");
	  	String[] sourceTaskIdArray = new String[sourceTaskIdList.size()];
	  	sourceTaskIdList.toArray(sourceTaskIdArray);

	  	CacheUtil.setCacheObject(context, "taskIdList", sourceTaskIdList);
	     	return;

}  else if("copyRisk".equalsIgnoreCase(strMode)) {
	    String parentId = emxGetParameter(request,"parentOID");
	    String selectedObjId = emxGetParameter(request,"objectId");
	    boolean isOfProjectType = ProgramCentralUtil.isOfGivenTypeObject(context,DomainConstants.TYPE_PROJECT_SPACE,parentId);
	    String[] selectedRiskRowId = emxGetParameterValues(request,"emxTableRowId");
	    selectedRiskRowId = ProgramCentralUtil.parseTableRowId(context, selectedRiskRowId);
		if(ProgramCentralUtil.isNullString(parentId)){
			parentId = selectedObjId;
		}
		try{
		    ContextUtil.startTransaction(context, true);
	    	 boolean copyFile = true;
	    for(int i=0;i<selectedRiskRowId.length;i++){
	    	 String selectedRiskId = selectedRiskRowId[i];
	    	 Risk risk = new Risk(selectedRiskId);
	    	 risk.copyRisk(context, parentId, copyFile);
	    }
			ContextUtil.commitTransaction(context);
		}catch(Exception e){
			ContextUtil.abortTransaction(context);
		}
		%>
		 	<script language="javascript" type="text/javaScript">
		 	var riskFrame;
		 	if(<%=isOfProjectType%> == true){
		 		riskFrame = findFrame(getTopWindow().window.getWindowOpener().parent,"PMCProjectRisk");
		 	}else{
		 		riskFrame = findFrame(getTopWindow().window.getWindowOpener().parent,"detailsDisplay");
		 		if(null == riskFrame){
		 			riskFrame = findFrame(getTopWindow(), "detailsDisplay");
		 		}
		 	}
		 	riskFrame.location.href = riskFrame.location.href;
		 	<%
	    	if(!ProgramCentralUtil.is3DSearchEnabled(context)){
	    	%>
	    		parent.window.closeWindow();
	    	<%}%>
		 	</script>
			  <%
	} else if("updateTaskPercentageComplete".equalsIgnoreCase(strMode)){
		String sOID = emxGetParameter(request, "objectId");
		String sValue = emxGetParameter(request, "newValue");

		String SUBTASK_IDS = "from[" + DomainConstants.RELATIONSHIP_SUBTASK + "].to.id";
		StringList busSelects = new StringList();
		busSelects.add(DomainConstants.SELECT_NAME);
		busSelects.add(SUBTASK_IDS);

		DomainObject task = DomainObject.newInstance(context, sOID);
		Map taskInfoMap = task.getInfo(context, busSelects);
		StringList subTasksList = (StringList)taskInfoMap.get(SUBTASK_IDS);
		String taskName = (String)taskInfoMap.get(DomainConstants.SELECT_NAME);
        boolean isSummaryTask = (subTasksList != null && subTasksList.size() != 0);

        if(!isSummaryTask){
		Map paramMap = new HashMap();
		paramMap.put("objectId", sOID);
		paramMap.put("New Value", sValue);
		paramMap.put("isSummary", Boolean.toString(isSummaryTask));
		paramMap.put("taskName", taskName);

		Map programMap = new HashMap();
		programMap.put("paramMap", paramMap);
		try{
		JPO.invoke(context, "emxTask", null, "updateTaskPercentageComplete", JPO.packArgs(programMap), Map.class);
			}
			catch(Exception e)
			{
				String ErrMSg = e.getMessage();
				String alertmsg = ErrMSg.replaceFirst("\n","*");
				alertmsg = alertmsg.replaceAll("\n", "");
				alertmsg = alertmsg.replace("*", "\\n");
				alertmsg = alertmsg.replaceAll("System Error: #5000001:", "");
%>
		<script language="javascript" type="text/javaScript">
			alert("<%= alertmsg %>");
			</script>
			<%
			}%>
		<script language="javascript" type="text/javaScript">
        	parent.emxEditableTable.refreshStructureWithOutSort();
		</script>
<%
        } else {
        	String errorMessage = "emxProgramCentral.WBS.PercentageCompletedCannotChangeForParent";
        	String key[] = {"TaskName"};
        	String value[] = {(String)taskInfoMap.get(DomainConstants.SELECT_NAME)};
        	errorMessage = ProgramCentralUtil.getMessage(context, errorMessage, key, value, null);
    %>
			<script language="javascript" type="text/javaScript">
				alert("<%= errorMessage %>");   									<%--XSSOK--%>
		 	</script>
	<%
        }
	} else if("postRefresh".equalsIgnoreCase(strMode)) {
		String portalCommandName = XSSUtil.encodeURLForServer(context,(String)emxGetParameter(request, "portalCmdName"));
	%>
		<script language="javascript" type="text/javaScript">
  	  var frame = "<%=portalCommandName%>";
  	  if("null" == frame){
  		var topFrame = findFrame(getTopWindow(), "PMCProjectTemplateWBS");
  		if(null == topFrame){
  			var topFrame = findFrame(getTopWindow(), "PMCProjectTemplateWBSDataGridView");	
  		}
		if(null == topFrame){
  			var topFrame = findFrame(getTopWindow(), "detailsDisplay");	
  		}
  		topFrame.emxEditableTable.refreshStructureWithOutSort();
  	  }else{
	  var topFrame = findFrame(getTopWindow(), frame);
	      if(null == topFrame)
	  		topFrame = findFrame(getTopWindow(), "detailsDisplay");

	  topFrame.emxEditableTable.refreshStructureWithOutSort();
  	  }

		</script>
	<%
	}else if("taskIndentation".equalsIgnoreCase(strMode)){
		//long start = System.currentTimeMillis();
	     		String sMode 			= emxGetParameter(request,"SubMode");
	     		String portalCmd 		= (String)emxGetParameter(request,"portalCmdName");
	     		String strProjectId 	= (String)emxGetParameter(request,"parentOID");
	     		String []strTableRowId 	= emxGetParameterValues(request,"emxTableRowId");
	     		strTableRowId = ProgramCentralUtil.excludeChildRows(context, strTableRowId);

	     		if(strTableRowId == null){
	     			return;
	     		}

 		int selectedObjSize = strTableRowId.length;
 		String[] objIdArray = new String[selectedObjSize];
 		String[] rowIdArray = new String[selectedObjSize];
 		
 		for(int i=0;i<selectedObjSize;i++){
					String sRowId 				= strTableRowId[i];
					Map mpRowDetails 			= (Map)ProgramCentralUtil.parseTableRowId(context,sRowId);
			String selectedTaskId 	= (String)mpRowDetails.get("objectId");
					String rowId 				= (String)mpRowDetails.get("rowId");

			objIdArray[i] = selectedTaskId;
			rowIdArray[i] = rowId;
			         }

 		MapList selectedObjInfoList = 
 				ProgramCentralUtil.getObjectDetails(context, objIdArray, new StringList(DomainConstants.SELECT_CURRENT), true);
 		
 		MapList finalMapList = new MapList();
 		for(int i=0,size = selectedObjInfoList.size();i<size;i++){
			Map selectObjMap 			= (Map)selectedObjInfoList.get(i);
			String selectedTaskState 	= (String)selectObjMap.get(DomainConstants.SELECT_CURRENT);
			
			if(!(ProgramCentralConstants.STATE_PROJECT_TASK_REVIEW.equalsIgnoreCase(selectedTaskState) || 
					ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE.equalsIgnoreCase(selectedTaskState))){
				selectObjMap.put("objectId", objIdArray[i]);
				selectObjMap.put("rowId", rowIdArray[i]);
				
				finalMapList.add(selectObjMap);
			    }
			    }
 		session.setAttribute("IndentaionInfoMap", finalMapList);

 		String postURL = "../programcentral/emxProgramCentralUtil.jsp?mode=IndentationProcess&action="+sMode+"&portalCmdName="+portalCmd+"&projectId="+strProjectId;
 		
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
   				    document.location.href = "<%=postURL%>";
   			    },10);

	     			</script>
	     		<%

	}else if("IndentationProcess".equalsIgnoreCase(strMode)){
		//long start = System.currentTimeMillis();
		String action 			= emxGetParameter(request,"action");
 		String portalCmd 		= (String)emxGetParameter(request,"portalCmdName");

 		MapList finalMapList = (MapList)session.getAttribute("IndentaionInfoMap");
			     	session.removeAttribute("IndentaionInfoMap");

     	int size = finalMapList.size();
     	List<String> selectedTaskIds = new ArrayList<>();
 		String[] rowIdArray = new String[size];

     	for(int i=0;i<size;i++){
			Map selectObjMap 			= (Map)finalMapList.get(i);
			String selecctedObjId 		= (String)selectObjMap.get("objectId");
			String selecctedObjRowId 	= (String)selectObjMap.get("rowId");

			selectedTaskIds.add(selecctedObjId);
			rowIdArray[i] = selecctedObjRowId;
			     	}

     	String projectId = (String)emxGetParameter(request,"projectId");
		Map<String,String> projectScheduleMap = ProgramCentralUtil.getProjectSchedule(context, projectId);
		String projectSchedule = projectScheduleMap.get(projectId);
		Map<String, Set> indentationParentIds = null;
		Set<String> parentIdList = null;
		Set<String> nonIndentedObjectsList = null;
		boolean isLeftIndetation = "LEFT".equalsIgnoreCase(action);
		try{
			indentationParentIds = Task.taskIndentation(context, selectedTaskIds, isLeftIndetation);
			//Indented parent ids and non indented object ids are feched
			parentIdList = indentationParentIds.get("IndentedObjects");
			nonIndentedObjectsList = indentationParentIds.get("NonIndentedObjects");		
			if (nonIndentedObjectsList !=null && !nonIndentedObjectsList.isEmpty()){
				 String strMessage = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.TaskIndentation.InvalidOperation", languageStr);
				 %> 
				 <script language="javascript" type="text/javaScript">
					alert('<%=strMessage%>');
					</script>
					<%
				}
     	 
		}catch(Exception e){
			String respMsg = e.getLocalizedMessage();
			if(respMsg.contains("Error: Circular dependency detected") || respMsg.contains("Subtasks/Subprojects are not allowed under Milestones/Gates.")){
			
			%> 
				<script language="javascript" type="text/javaScript">
					alert('<%=respMsg%>');
					var portalName = "<%=portalCmd%>";
		     		var topFrame = findFrame(getTopWindow(), portalName);

     				if(topFrame == null){
     					topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
     					if(null == topFrame){
	     				    topFrame = findFrame(getTopWindow(), "detailsDisplay");
     					}
     				}
					topFrame.toggleProgress('hidden');
				</script>
			<%
			return;
			}
		}
     	List<String> parentObjIdList = new ArrayList<>();
     	List<String> nonIndentedObjIdList = new ArrayList<>();
     	List<String> objRowIdList = new ArrayList<>();
     	boolean isRefreshStructure = true;

     	if(parentIdList.isEmpty()){
	    	isRefreshStructure = false;
     	}else{
     		String[] parentObjIdArray = new String[parentIdList.size()];
     		parentIdList.toArray(parentObjIdArray);
     		
     		//nonindented object array created
     		String[] nonIndentedIdArray = new String[nonIndentedObjectsList.size()];
     		nonIndentedObjectsList.toArray(nonIndentedIdArray);
     		
     		List<String> objPhysicalIds = new ArrayList<>();
     		objPhysicalIds.addAll(parentIdList);
     		objPhysicalIds.addAll(nonIndentedObjectsList);
     		String[] objPhysicalIdsArray = new String[objPhysicalIds.size()];
     		objPhysicalIds.toArray(objPhysicalIdsArray);
     		
     		StringList selects = new StringList();
     		selects.add(ProgramCentralConstants.SELECT_ID);
     		selects.add("physicalid");
     		
     		BusinessObjectWithSelectList objectWithSelectList =
					ProgramCentralUtil.getObjectWithSelectList(context, objPhysicalIdsArray, selects,true);
     		for(int i=0;i<objectWithSelectList.size();i++){
     			BusinessObjectWithSelect bws = objectWithSelectList.getElement(i);
     			String ObjId = bws.getSelectData(ProgramCentralConstants.SELECT_ID);
     			String physicalId = bws.getSelectData("physicalid");
     			if(parentIdList.contains(physicalId)){
     				parentObjIdList.add(ObjId);
     			}
     			if(nonIndentedObjectsList.contains(physicalId)){
     				nonIndentedObjIdList.add(ObjId);
     			}
			}	
     		
     		//if finalMaplist (selected objects) are not indented then those removed from finalmaplist
     		for(int i=0;i<finalMapList.size();i++){
    			Map selectObjMap 			= (Map)finalMapList.get(i);
    			String selecctedObjId 		= (String)selectObjMap.get("objectId");
    			if(nonIndentedObjIdList.contains(selecctedObjId)){
    				finalMapList.remove(i);
    				}
         	}         	

         	for(int i=0;i<finalMapList.size();i++){
        		Map selectObjMap 			= (Map)finalMapList.get(i);
        		String selecctedObjId 		= (String)selectObjMap.get("objectId");    			
        		if(!parentObjIdList.contains(selecctedObjId)){
        			String selecctedObjRowId 	= (String)selectObjMap.get("rowId");	
        			objRowIdList.add(selecctedObjRowId);
    			}
    		}       		
		}
					
     	
		     		%>
		     		<script language="javascript" type="text/javaScript">
			     		var isRefresh = "<%=isRefreshStructure%>";
						var schedule = "<%=projectSchedule%>";
			     		var portalName = "<%=portalCmd%>";

			     		var cBoxArray = new Array();
     		var selectedObjIdArr = new Array();

			     		var topFrame = findFrame(getTopWindow(), portalName);

	     				if(topFrame == null){
	     					topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
	     					if(null == topFrame){
		     				    topFrame = findFrame(getTopWindow(), "detailsDisplay");
	     					}
	     				}
			
			if(isRefresh == "true"){
					    <%for (int i = 0, len = selectedTaskIds.size(); i < len; i++) {%>
					selectedObjIdArr["<%=i%>"]="<%=selectedTaskIds.get(i)%>";
						<%}
				for (int i=0,len = objRowIdList.size(); i<len; i++) {
							String tableRowId = " | | |" + objRowIdList.get(i);%>
					cBoxArray["<%=i%>"]="<%=tableRowId%>";
						<%}%>
							topFrame.emxEditableTable.removeRowsSelected(cBoxArray);
	     		var parentObjId = new Array();
				     	<%for (String parentId : parentObjIdList) {%>
		     		parentObjId.push("<%=parentId%>");
					    <%}%>
				     		setTimeout(function() {
					var expRowId = new Array();
					if(!topFrame.dataGridEnabled){
		    		for(var rw=0,len = parentObjId.length; rw<len; rw++){
		    			var nRow = emxUICore.selectSingleNode(topFrame.oXML, "/mxRoot/rows//r[@o = '" + parentObjId[rw] + "']");
						            nRow.setAttribute("expand", false);
						            nRow.setAttribute("hChild", "true");
						            nRow.setAttribute("expandedLevels", 0);
			            expRowId.push(nRow.getAttribute("id"));
					    		}
							}else{
						for(var rw=0,len = parentObjId.length; rw<len; rw++){
							expRowId.push(topFrame.emxEditableTable.getAttributeFromNode("rowId",parentObjId[rw]));

					}
				}
		    		topFrame.emxEditableTable.expand(expRowId, "1");
					    		var selectedObjRowId = new Array();
					if(topFrame.dataGridEnabled){
						for(var i=0;i<selectedObjIdArr.length;i++){
									selectedObjRowId[i] = topFrame.emxEditableTable.getAttributeFromNode("rowId",selectedObjIdArr[i]);
								}
						}else{
		    		for(var i=0,len=selectedObjIdArr.length;i<len;i++){
		    			var nRow = emxUICore.selectSingleNode(topFrame.oXML, "/mxRoot/rows//r[@o = '" + selectedObjIdArr[i] + "']");
					    			selectedObjRowId[i] = nRow.getAttribute("id");
					    		}
}
					    		if("Manual"==schedule){
						top.jQuery('.fonticon-refresh').addClass('fonticon-refresh-info');
						toggleRollupIcon(topFrame,"iconActionUpdateDates","iconActionUpdateDatesActive");
					  }else{
						  topFrame.emxEditableTable.select(selectedObjRowId);
						  topFrame.emxEditableTable.refreshStructureWithOutSort();
					  }
		    		topFrame.toggleProgress('hidden');
			    },10);
				if("PMCWBSDataGridView"==portalName || "PMCProjectTemplateWBSDataGridView"==portalName){
					topFrame.emxEditableTable.setDataModifiedFlag(false);
					topFrame.emxEditableTable.refreshStructureWithOutSort();
					topFrame.rebuildView();
				}
			     }
				 else{
     			setTimeout(function() {
     				topFrame.emxEditableTable.refreshStructureWithOutSort();
 					topFrame.toggleProgress('hidden');
 			    },100);
     		}
		     		</script>
		     		<%
     	   }else if("MoveTasks".equals(strMode)){
     		  Map requestMap 			= new HashMap();
    		  StringList slSelectedId 	= new StringList();

	  		  String action 			= emxGetParameter(request,"SubMode");
			  String projectId 			= (String)emxGetParameter(request,"parentOID");
			  String selectedTableRowId = (String)emxGetParameter(request,"emxTableRowId");
			  String portalCmd 			= (String)emxGetParameter(request,"portalCmdName");

			  Map mpRowDetails 			= ProgramCentralUtil.parseTableRowId(context,selectedTableRowId);
			  String selectedTaskId 	= (String)mpRowDetails.get("objectId");
			  String rowID 				= (String)mpRowDetails.get("rowId");
			  String selectedTaskParentId 	= (String)mpRowDetails.get("parentOId");
			  String strErrorMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
						"emxProgramCentral.MoveTask.CannnotMoveTask", languageStr);
			  %>
			    <script language="javascript" type="text/javaScript">
			       //IR-1211503-3DEXPERIENCER2024x  To use topframe for alert when childList is less than 2
				    var portalName 		= "<%=portalCmd%>";   
				    var topFrame = findFrame(getTopWindow(), portalName);
	   				if(topFrame == null){
	   					topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
	   					if(null == topFrame){
		     				    topFrame = findFrame(getTopWindow(), "detailsDisplay");
	   					}
	   				}
	   				</script>
				<%
			  if("0".equalsIgnoreCase(rowID)){
				  %>
				  
					<script language="javascript" type="text/javaScript">
			 		alert('<%= strErrorMsg %>');     <%--XSSOK--%>
			 		if(topFrame.dataGridEnabled){
			 		topFrame.emxEditableTable.abortLoading();
			 		}
		 	 		</script>
		 	 		
				<%
				  return;
			  }

			  String mqlCmd = "print bus $1 select $2 dump $3";
			  String SELECT_CHILD_TASK_IDS  =   "from["+DomainConstants.RELATIONSHIP_SUBTASK+"].to.id";
			  String childTasks = MqlUtil.mqlCommand(context, 
							true,
							true,
							mqlCmd, 
							true,
							selectedTaskParentId,
							SELECT_CHILD_TASK_IDS,
							"|");
			  StringList childIdList = FrameworkUtil.split(childTasks, "|");
			  if(childIdList == null || childIdList.size() < 2){
				  
				  
				 %>
					<script language="javascript" type="text/javaScript">
			 		alert('<%= strErrorMsg %>');     <%--XSSOK--%>
			 		if(topFrame.dataGridEnabled){
				 		topFrame.emxEditableTable.abortLoading();
				 		}
		 	 		</script>
				<%
		  					
				  return;
			  }

			  String toMoveUpOrDown 	= DomainObject.EMPTY_STRING;
			  String parentRowID		= DomainObject.EMPTY_STRING;

			  int posi 					= rowID.lastIndexOf(",");
			  String val 				= rowID.substring(0,posi);
			  String val1 				= rowID.substring(posi+1,rowID.length());

			  //move operation
			  boolean done = Task.moveTasks(context, selectedTaskParentId, selectedTaskId, action);
			  Task task = new Task(selectedTaskId);
			  //code added for refresh dependent tasks
			  MapList dependentTaskList = task.getSuccessors(context, new StringList(Task.SELECT_ID), null, null);
			  
			  if(Task.MOVE_UP.equalsIgnoreCase(action)){
			      int ilevel 		= Integer.parseInt(val1)-1;
			      toMoveUpOrDown 	= val+","+ilevel;
				  StringList parentRowIdList = task.getParentRowIds(context, Task.MOVE_UP, new StringList(rowID));
				  parentRowID = (String)parentRowIdList.get(0);
			  }else{
			      int ilevel 		= Integer.parseInt(val1)+1;
			      toMoveUpOrDown 	= val+","+ilevel;
				  StringList parentRowIdList = task.getParentRowIds(context, Task.MOVE_DOWN, new StringList(rowID));
				  parentRowID 		= (String)parentRowIdList.get(0);
			  }

			  %>
			    <script language="javascript" type="text/javaScript">
				    var portalName 		= "<%=portalCmd%>";
				    var action 		= "<%=action%>";
				    var refresh 		= <%=done%>;
				    var topFrame = findFrame(getTopWindow(), portalName);
     				if(topFrame == null){
     					topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
     					if(null == topFrame){
	     				    topFrame = findFrame(getTopWindow(), "detailsDisplay");
     					}
     				}
				    if(refresh){
				    

     				var selectdObjId 	= "<%=selectedTaskId%>";
				    var pId 			= "<%=parentRowID%>";
						    var impObjIdArr = [];
							<%
				    		for (int i=0,len=dependentTaskList.size();i<len;i++) {
				    			Map<String,String> dependentMap = (Map<String,String>)dependentTaskList.get(i);
				    			String taskId = dependentMap.get(DomainObject.SELECT_ID);
								%>impObjIdArr.push("<%=taskId%>");<%
							}
							%>

							var impObjRowIdArr = new Array();
							var count = 0;
							for(var i=0,len =impObjIdArr.length ;i<len;i++){
				if(topFrame.dataGridEnabled){
					var selectedRowId = topFrame.emxEditableTable.getAttributeFromNode("rowId",selectdObjId);
					if(selectedRowId != null){
									impObjRowIdArr[count++] = selectedRowId;
								}
				}
				else{
								var nRow = emxUICore.selectSingleNode(topFrame.oXML, "/mxRoot/rows//r[@o = '" + impObjIdArr[i] +"']");
								if(nRow != null){
									impObjRowIdArr[count++] = nRow.getAttribute("id");
								}
							}
				}

						    var selectedObjRowId = null;
					if(topFrame.dataGridEnabled){
								selectedObjRowId = topFrame.emxEditableTable.getAttributeFromNode("rowId",selectdObjId);
							}else{
				    var selectedRow 	= emxUICore.selectSingleNode(topFrame.oXML, "/mxRoot/rows//r[@o = '" + selectdObjId + "']");
						    	 selectedObjRowId = selectedRow.getAttribute("id");
					}

		            if(topFrame.dataGridEnabled){
						
							var childRows = topFrame.emxEditableTable.getChildNodesFromAttribute("rowId",pId);
							//SR Changes Start
								var WUXModelChangeStates = {
										Unchanged: 0,
										AttributesChanged: 1,
										NodeAdded: 2,
										NodeToBeRemoved: 4
								};
							//SR Changes End
							var index = 0;
								for(var i=0,len=childRows.length;i<len;i++){
									if(topFrame.emxEditableTable.getAttribute(childRows[i],"rowId") == selectedObjRowId){
										break;
									}
									index++;
								}
								if("moveUp"==action){
									var abvRow = childRows[index - 1];
									impObjRowIdArr[count++] = topFrame.emxEditableTable.getAttribute(abvRow,"rowId");
									impObjRowIdArr[count++] = topFrame.emxEditableTable.getAttribute(childRows[index],"rowId");
									
									abvRow.getParent().insertBefore(childRows[index],childRows[index - 1]);
									
									//SR Changes Start
									(childRows[index-1]._setChangeStates(WUXModelChangeStates.NodeToBeRemoved, "remove"),childRows[index-1].notifyModelUpdate());
									(childRows[index-1]._setChangeStates(WUXModelChangeStates.NodeAdded, "remove"),childRows[index-1].notifyModelUpdate());
									//SR Changes end
									
									//abvRow.parentNode.setAttribute("display",'block');
									
								}else{
									var abvRow = childRows[index+1];
									impObjRowIdArr[count++] = topFrame.emxEditableTable.getAttribute(abvRow,"rowId");
									impObjRowIdArr[count++] = topFrame.emxEditableTable.getAttribute(childRows[index],"rowId");
									
									abvRow.getParent().insertBefore(childRows[index+1],childRows[index]);
									
									//SR Changes Start
									(childRows[index]._setChangeStates(WUXModelChangeStates.NodeToBeRemoved, "remove"),childRows[index].notifyModelUpdate());
                                    (childRows[index]._setChangeStates(WUXModelChangeStates.NodeAdded, "remove"),childRows[index].notifyModelUpdate());
									//SR Changes end
									//abvRow.parentNode.setAttribute("display",'block');
								} 
								
							}else{
								var childRows = emxUICore.selectNodes(topFrame.oXML,"/mxRoot/rows//r[@id='"+pId+"']//r");
							
								var index = 0;
								for(var i=0,len=childRows.length;i<len;i++){
									if(childRows[i].getAttribute("id") == selectedObjRowId){
										break;
									}
									index++;
								}
								
								if("moveUp"==action){
									var childIndex = 1;
									while(index+childIndex<childRows.length && childRows[index+childIndex].getAttribute("level")!=childRows[index].getAttribute("level")){
										impObjRowIdArr[count++] = childRows[index+childIndex].getAttribute("id");
										childIndex++;
									}
									//Find correct index of previous row which is at same level as selected row
									var previousIndex= index - 1 ;
									var selectedObjectRowIdLevelSize = selectedObjRowId.split(',').length;
									//traversing in reverse direction to fetch immediate previous obejct which is at same level
									for(var i= previousIndex ; i>0 ; i--){
											var previousElemLevelSize = childRows[i].getAttribute("id").split(',').length;
											//if size is same then both objects are at same level
											if(previousElemLevelSize == selectedObjectRowIdLevelSize){
												break;
											}
											
											previousIndex--;
											
									}
									
									//if previousIndex is negative then user is trying to move up 1st element... so need to perform any operation
									if( previousIndex >= 0){
									var swapRowIndex = previousIndex;
									var childIndexForSwappedRow = 1;
									while(swapRowIndex+childIndexForSwappedRow<childRows.length && childRows[swapRowIndex+childIndexForSwappedRow].getAttribute("level")!=childRows[swapRowIndex].getAttribute("level")){
										impObjRowIdArr[count++] = childRows[swapRowIndex+childIndexForSwappedRow].getAttribute("id");
										childIndexForSwappedRow++;
									}
									var abvRow = childRows[previousIndex];
									impObjRowIdArr[count++] = abvRow.getAttribute("id");
									impObjRowIdArr[count++] = childRows[index].getAttribute("id");
									
									abvRow.parentNode.insertBefore(childRows[index],childRows[previousIndex]);
									//abvRow.parentNode.setAttribute("display",'block');
									}
									
								}else{
									var childIndex = 1;
									while(index+childIndex<childRows.length && childRows[index+childIndex].getAttribute("level")!=childRows[index].getAttribute("level")){
										impObjRowIdArr[count++] = childRows[index+childIndex].getAttribute("id");
										childIndex++;
									}
                                                                        var childIndexForSwappedRow = 1;
									var swapRowIndex = index+childIndex;
									//Add Swapped row's child row ids to refresh
									while(swapRowIndex+childIndexForSwappedRow<childRows.length && childRows[swapRowIndex+childIndexForSwappedRow].getAttribute("level")!=childRows[swapRowIndex].getAttribute("level")){
										impObjRowIdArr[count++] = childRows[swapRowIndex+childIndexForSwappedRow].getAttribute("id");
										childIndexForSwappedRow++;
									}
									var abvRow = childRows[index+childIndex];
									impObjRowIdArr[count++] = abvRow.getAttribute("id");
									impObjRowIdArr[count++] = childRows[index].getAttribute("id");
									
									abvRow.parentNode.insertBefore(childRows[index+childIndex],childRows[index]);
									//abvRow.parentNode.setAttribute("display",'block');
								}
								}
								if(topFrame.dataGridEnabled){
									topFrame.emxEditableTable.setDataModifiedFlag(false);
								}
								topFrame.emxEditableTable.refreshRowByRowId(impObjRowIdArr,false);
								topFrame.rebuildView();
					}else{
						alert('<%= strErrorMsg %>');  
						topFrame.emxEditableTable.abortLoading();
					}

				  
			    </script>
			    <%

             	  }else if ("PMCWBSEffortFilter".equalsIgnoreCase(strMode)) {
               		String strURL = "../common/emxIndentedTable.jsp?tableMenu=PMCWBSTableMenu&expandProgramMenu=PMCWBSListMenu&toolbar=PMCWBSToolBar&freezePane=Name&selection=multiple&suiteKey=ProgramCentral&SuiteDirectory=programcentral&HelpMarker=emxhelpwbstasklist&header=emxProgramCentral.Common.WorkBreakdownStructureSB&sortColumnName=ID&findMxLink=false&postProcessJPO=emxTask:postProcessRefresh&editRelationship=relationship_Subtask&resequenceRelationship=relationship_Subtask&connectionProgram=emxTask:cutPasteTasksInWBS";
               		String objectId = emxGetParameter(request, "objectId");

               		Map paramMap = new HashMap(1);
         	    	    paramMap.put("objectId",objectId);

                     String[] methodArgs = JPO.packArgs(paramMap);
                    boolean hasModifyAccess  = (boolean) JPO.invoke(context,"emxTask", null, "hasModifyAccess", methodArgs, Boolean.class);
					/*
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
					*/
					strURL = ProgramCentralUtil.appendRequestParameters(context,request,strURL);
               		if(hasModifyAccess){
               			strURL = strURL + "&editLink=true";
               		}
                  %>
                   <script language="javascript">
                 		 var strUrl = "<%=strURL%>";
                 		 document.location.href = strUrl;
                   </script>
                 <%
                }else if ("PMCWhatIfProjectExperimentsList".equalsIgnoreCase(strMode)) {
               		String strURL = "../common/emxIndentedTable.jsp?table=PMCWhatIfExperimentSummaryTable&toolbar=PMCWhatIfActions&postProcessJPO=emxProjectBaseline:postProcessRefresh&freezePane=Name&relationship=relationship_Experiment&direction=from&selection=multiple&HelpMarker=emxhelpexperiments&hideHeader=true&customize=false&rowGrouping=false&showPageURLIcon=false&hideLaunchButton=true&export=false&displayView=details&export=false&objectCompare=false&showClipboard=false&multiColumnSort=false&findMxLink=false&showRMB=false&massPromoteDemote=false&expandLevelFilter=false&triggerValidation=false&cellwrap=false&suiteKey=ProgramCentral&SuiteDirectory=programcentral&hideRootSelection=true";
               		String objectId = emxGetParameter(request, "objectId");

               		Map paramMap = new HashMap(1);
         	    	    paramMap.put("objectId",objectId);

                     String[] methodArgs = JPO.packArgs(paramMap);
                    boolean hasModifyAccess  = (boolean) JPO.invoke(context,"emxTask", null, "hasModifyAccess", methodArgs, Boolean.class);
					/*
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
					*/
					strURL = ProgramCentralUtil.appendRequestParameters(context,request,strURL);
               		if(hasModifyAccess){
               			strURL = strURL + "&editLink=true";
               		}
                  %>
                   <script language="javascript">
                 		 var strUrl = "<%=strURL%>";
                 		 document.location.href = strUrl;
                   </script>
                 <%
                }else if("validateCost".equalsIgnoreCase(strMode)) {

					String costValue =  XSSUtil.encodeURLForServer(context,emxGetParameter(request,"costValue"));
					Currency currencyObj = new Currency();
					Boolean iscurrencyValid = currencyObj.validateFinancialInput(context, costValue);
					String responce =iscurrencyValid.toString();
					JsonObject jsonObject = Json.createObjectBuilder()
							                .add("isValidCurrency",responce)
							                .build();

					out.clear();
					out.write(jsonObject.toString());
					return;
				} else if("AddProjectToSelectedDashboard".equalsIgnoreCase(strMode)) {
                    String projectId = emxGetParameter(request,"emxParentIds");
                    StringList slIds = FrameworkUtil.split(projectId, "~");
                    String[] projects = new String[slIds.size()];
                    for(int i=0; i<slIds.size(); i++){
                    	String sId = (String)slIds.get(i);
                    	 StringList allIds = FrameworkUtil.split(sId, "|");
                    	 projects[i] = (String)allIds.get(0);
                    }

                    String dashboardName = emxGetParameter(request,"emxTableRowId");
                    if(ProgramCentralUtil.isNotNullString(dashboardName)){
                     try {
                 	   Map mpRow = ProgramCentralUtil.parseTableRowId(context,dashboardName);
                 	   dashboardName = (String) mpRow.get("objectId");
                 	  // start a write transaction and lock business object
                       ContextUtil.startTransaction( context, true );
                       //to find the set for this dashboard name
                       SetList sl = matrix.db.Set.getSets( context , true);
                       Iterator setItr = sl.iterator();
                       while ( setItr.hasNext() ){
                         matrix.db.Set curSet = ( matrix.db.Set )setItr.next();
                         if ( curSet.getName().equals( dashboardName ) ){
                           //add the projects to this set
                           BusinessObjectList busList = curSet.getBusinessObjects( context );
                           for ( int i = 0; i < projects.length; i++ ){
                             String busId = projects[i];
                             BusinessObject bo = new BusinessObject(busId);
                             curSet.add(bo);
                           }
                       // commit the data
                           curSet.setBusinessObjects( context );
                           break; // don't need to continue looping
                     } // end if
                       } // end while
                         ContextUtil.commitTransaction( context );
                    }catch (Exception e){
                    	ContextUtil.abortTransaction( context );
                	    throw e;
                	  }
                    }else{
            			 String errorMessage =
   		 	   	    			ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Common.Noobjectsselected",sLanguage);
      			 %>
				<script language="javascript" type="text/javaScript">
		 		alert('<%= errorMessage %>');     <%--XSSOK--%>
	 	 		</script>
				<%
	  					}
                %>
				<script language="javascript" type="text/javaScript">
					getTopWindow().getWindowOpener().refreshSBTable(
							getTopWindow().getWindowOpener().configuredTableName);
					getTopWindow().closeWindow();
				</script>
				<%
				}else if ("PolicyName".equalsIgnoreCase(strMode)) {
			String objectId = emxGetParameter(request,"objectId");
			DomainObject task = DomainObject.newInstance(context,objectId);

			StringList busSelect = new StringList();
			busSelect.addElement(DomainConstants.SELECT_POLICY);
			busSelect.addElement(ProgramCentralConstants.SELECT_IS_SUMMARY_TASK);

			Map<String,String> taskInfo = task.getInfo(context, busSelect);
			String policyName 	= taskInfo.get(DomainConstants.SELECT_POLICY);
			String isSummary	= taskInfo.get(ProgramCentralConstants.SELECT_IS_SUMMARY_TASK);

			JsonObject jsonObject = Json.createObjectBuilder()
									.add("Policy",policyName)
		    						.add("SummaryTask",isSummary)
		    						.build();

			out.clear();
			out.write(jsonObject.toString());
			return;
		}else if("People".equalsIgnoreCase(strMode)){
			String strURL = "../common/emxIndentedTable.jsp?program=emxDomainAccess:getObjectAccessList&table=PMCProjectPeople&toolbar=PMCMemberSummaryToolBar&expandProgram=emxTaskBase:getGroupMembersforProject&header=emxProgramCentral.Common.Members&HelpMarker=emxhelpprojectmembers&objectBased=false&hideRootSelection=true&displayView=details&showClipboard=false&HelpMarker=emxhelpmultipleownershipaccess&massUpdate=false&selection=multiple&Export=false&sortColumnName=Name&sortDirection=ascending&isViewMode=true&level=0&expandLevelFilter=true&freezePane=Project,Organization&massUpdate=true&persistent=false&showRMB=false&parallelLoading=true&maxCellsToDraw=2000&scrollPageSize=50&applyURL=javascript:checkroles";
			/*
			Enumeration requestParams = emxGetParameterNames(request);
  		  	StringBuilder url = new StringBuilder();

	  		if(requestParams != null){
	      	  while(requestParams.hasMoreElements()){
		       		  String param = (String)requestParams.nextElement();
		       		  String value = emxGetParameter(request,param);
		       		  url.append("&"+param);
		       		  url.append("="+XSSUtil.encodeForURL(context, value));
	      	  }
	  		}

  		  	strURL = strURL + url.toString();
			*/
			strURL = ProgramCentralUtil.appendRequestParameters(context,request,strURL);
    		String objectId = emxGetParameter(request, "objectId");

    		Map paramMap = new HashMap(1);
	    	paramMap.put("objectId",objectId);

          	String[] methodArgs = JPO.packArgs(paramMap);
          	boolean hasModifyAccess  = (boolean) JPO.invoke(context,"emxProjectMember", null, "hasAccessToModifyProject", methodArgs, Boolean.class);

      		if(hasModifyAccess){
      			strURL = strURL + "&editLink=true";
      		}

      		%>
            <script language="javascript">
          		 var url = "<%=strURL%>";
          		 document.location.href = url;
            </script>
          <%

      	}else if ("errorMessage".equalsIgnoreCase(strMode)) {
			String errorKey = emxGetParameter(request,"errorKey");
			if(ProgramCentralUtil.isNullString(errorKey)){
				errorKey = (String) emxGetParameter(request, "key");
			}
			String errorMessage = EnoviaResourceBundle.getProperty(context, ProgramCentralConstants.PROGRAMCENTRAL, errorKey, languageStr);
			String fileExtension = emxGetParameter(request, "fileExtension");
			fileExtension = XSSUtil.encodeURLForServer(context, fileExtension);
			if(!(fileExtension == null || fileExtension == "")){
				errorMessage = errorMessage.replace("{0}",fileExtension);
			}
			out.clear();
			out.write(errorMessage);
			return;
      	}else if ("PMCQuestion".equalsIgnoreCase(strMode)) {

       		String strURL = "../common/emxIndentedTable.jsp?table=PMCQuestionSummaryTable&selection=multiple&sortColumnName=Name&header=emxFramework.Command.Question&toolbar=PMCQuestionToolbar&massPromoteDemote=true&expandProgram=emxQuestion:getQuestionORQuestionTaskList&helpMarker=emxhelpquestionsummary&postProcessJPO=emxQuestionBase:postProcessRefresh";
         		String objectId = emxGetParameter(request, "objectId");

         		Map paramMap = new HashMap(1);
   	    	    paramMap.put("objectId",objectId);

               String[] methodArgs = JPO.packArgs(paramMap);
               boolean hasModifyAccess  = (boolean) JPO.invoke(context,"emxProjectTemplate", null, "hasAccessToQuestionCommands", methodArgs, Boolean.class);
					/*
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
					*/
					strURL = ProgramCentralUtil.appendRequestParameters(context,request,strURL);
         		if(hasModifyAccess){
         			strURL += "&editLink=true";
         		}
         		%>
                <script language="javascript">
              		 var strUrl = "<%=strURL%>";
              		 document.location.href = strUrl;
                </script>
                <%
      	}else if ("PMCProjectRisk".equalsIgnoreCase(strMode)) {

       		String strURL = "../common/emxIndentedTable.jsp?table=PMCRisksSummary&selection=multiple&Export=true&sortColumnName=Title&sortDirection=ascending&toolbar=PMCRisksSummaryToolBar&header=emxProgramCentral.ProgramTop.RisksProject&HelpMarker=emxhelprisksummary&freezePane=Name&postProcessJPO=emxTask:postProcessRefresh&hideLaunchButton=true&expandProgramMenu=PMCRiskListMenu&showPageURLIcon=false";
         		String objectId = emxGetParameter(request, "objectId");

         		Map paramMap = new HashMap(1);
   	    	    paramMap.put("parentOID",objectId);

               String[] methodArgs = JPO.packArgs(paramMap);
               boolean hasModifyAccess  = (boolean) JPO.invoke(context,"emxRisk", null, "checkEditAccess", methodArgs, Boolean.class);
					/*
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
					*/
					strURL = ProgramCentralUtil.appendRequestParameters(context,request,strURL);
         		if(hasModifyAccess){
         			strURL = strURL + "&editLink=true";
         		}
		%>
			<script language="javascript">
              		 var strUrl = "<%=strURL%>";
              		 document.location.href = strUrl;
            </script>
       <%
     	}else if("PMCFolder".equalsIgnoreCase(strMode)) {

                String objectId = emxGetParameter(request, "objectId");

		DomainObject dom = DomainObject.newInstance(context, objectId);
		
        String ATTRIBUTE_BOOKMARK_PHYSICAL_ID = PropertyUtil.getSchemaProperty("attribute_BookmarkPhysicalId");
        String SELECT_BOOKMARK_PHYSICAL_ID = DomainObject.getAttributeSelect(ATTRIBUTE_BOOKMARK_PHYSICAL_ID);
        String INTERFACE_DPMBOOKMARKROOT = PropertyUtil.getSchemaProperty("interface_DPMBookmarkRoot");

 		StringList busSelect = new StringList(3);
   		busSelect.addElement(DomainConstants.SELECT_HAS_MODIFY_ACCESS);
   		busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
   		busSelect.addElement(SELECT_BOOKMARK_PHYSICAL_ID);
 		Map<String,String> objInfo = dom.getInfo(context, busSelect);
 		String bookmarkWSId = objInfo.get(SELECT_BOOKMARK_PHYSICAL_ID);

        // FZS if this attribute exists then we have converted the root of the folders to a workspace so send that id in
        if((bookmarkWSId != null) && (!bookmarkWSId.equals(""))){
            try{
                DomainObject domObjBookmarkWS = DomainObject.newInstance(context,bookmarkWSId);
                if (domObjBookmarkWS != null) {
                    objectId = bookmarkWSId;
                }
            }
            catch(Exception ex) {
                String existingBookmark = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 where $4 limit 1 dump", 
                    "*", "*", "*", "physicalid=="+bookmarkWSId);
                if (!"".equalsIgnoreCase(existingBookmark))
				{
                    objectId = bookmarkWSId;
				}
				else 
				{
					// the bookmark root was deleted so remove the interface and use the Project Space as the root
					String strCommand = "modify $1 $2 remove interface $3";
					String strMessage = MqlUtil.mqlCommand(context,strCommand, "bus", objectId, INTERFACE_DPMBOOKMARKROOT);
				}
			}
        }

        String strURL = "../common/emxIndentedTable.jsp?tableMenu=PMCFolderTableMenu&toolbar=PMCFolderSummaryToolBar&"+
		  				  "header=emxProgramCentral.Common.Folders&objectCompare=true&HelpMarker=emxhelpfoldersummary&Export=true&displayView=details"+
				  		  "&selection=multiple&expandProgram=emxProjectFolder:getTableExpandProjectVaultData&freezePane=Name&"+
		  				  "sortColumnName=Name&preProcessJPO=emxProjectFolder:preProcessCheckForEdit"+
				  		  "&connectionProgram=emxProjectFolder:cutPasteObjectsInFolderStructureBrowser&"+
		  				  "editRelationship=relationship_SubVaults,relationship_VaultedDocumentsRev2&"+
				  		  "resequenceRelationship=relationship_ProjectVaults,relationship_SubVaults,relationship_VaultedDocumentsRev2"+
		  				  "&massPromoteDemote=false&objectId="+objectId+"&postProcessJPO=emxTask:postProcessRefresh";
 		
 		String isProjectTemplate = objInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
 		String hasModifyAccess  = objInfo.get(DomainConstants.SELECT_HAS_MODIFY_ACCESS);
 		boolean editFlag = false;
 		if("true".equalsIgnoreCase(isProjectTemplate)){
 			Map paramMap = new HashMap(1);
	    	paramMap.put("objectId",objectId);              
           	String[] methodArgs = JPO.packArgs(paramMap);
 			editFlag  = (boolean) JPO.invoke(context,"emxProjectTemplate", null, "hasAccessToTemplateWBSActionMenu", methodArgs, Boolean.class);
 		}else{
 			editFlag = "true".equalsIgnoreCase(hasModifyAccess);
 		}
 		
		if(editFlag){
			strURL += "&editLink=true";
		}
		/*
		Enumeration requestParams = emxGetParameterNames(request);
	  	StringBuilder url = new StringBuilder();

	  	if(requestParams != null){
		  while(requestParams.hasMoreElements()){
			  String param = (String)requestParams.nextElement();
			  String value = emxGetParameter(request,param);
			  url.append("&"+param);
			  url.append("="+XSSUtil.encodeForURL(context, value));
		  }
		  strURL += url.toString();
	  	}
	  	*/
	  	strURL = ProgramCentralUtil.appendRequestParameters(context,request,strURL);
%>
    <script language="javascript">
  		 var strUrl = "<%=strURL%>";
  		 document.location.href = strUrl;
    </script>
<%
	} else if("PMCShowArchievedStructure".equalsIgnoreCase(strMode)){
		String strURL = "../common/emxTree.jsp?AppendParameters=true&PMCShowArchievedStructure=true";
		String objectId = emxGetParameter(request, "objectId");
		Map paramMap = new HashMap(1);
		paramMap.put("objectId",objectId);
		String[] methodArgs = JPO.packArgs(paramMap);
		/*
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
		*/
		strURL = ProgramCentralUtil.appendRequestParameters(context,request,strURL);
%>
		<script language="javascript">
			var strUrl = "<%=strURL%>";
			document.location.href = strUrl;
		</script>
<%
	}else if("FindCompanyAdminList".equalsIgnoreCase(strMode)) {
		String objectId = emxGetParameter(request, "objectId");

		String URL = "../common/emxIndentedTable.jsp?program=emxProjectTemplate:getAllAdminFromCompany&table=PMCProjectTemplateCoOwnersTable&sortColumnName=ID&suiteKey=ProgramCentral&SuiteDirectory=programcentral&header=emxProgramCentral.PersonDialog.ProjectAdministrator&submitLabel=emxProgramCentral.Common.Assign";
		URL += "&submitURL=../programcentral/emxProgramCentralUtil.jsp&mode=AddTemplateCoOwners&objectId="+objectId+"&selection=multiple&customize=false&displayView=details&showClipboard=false&findMxLink=false&showRMB=false&showPageURLIcon=false&hideLaunchButton=true&objectCompare=false&autoFilter=false&rowGrouping=false&Export=false&PrinterFriendly=false&multiColumnSort=false&HelpMarker=false&cellwrap=false";
%>
		 	<script language="javascript" type="text/javaScript">
		 	 	var url = "<%=XSSUtil.encodeForJavaScript(context,URL)%>";
		 	 	getTopWindow().location.href = url;
		 	</script>
<%
	} else if("AddTemplateCoOwners".equalsIgnoreCase(strMode)) {
		String[] selectedCoOwnerList = emxGetParameterValues(request, "emxTableRowId");
		String templateId = emxGetParameter(request, "objectId");

		ProjectTemplate template = (ProjectTemplate) DomainObject.newInstance(context, templateId, DomainConstants.PROGRAM, DomainConstants.TYPE_PROJECT_TEMPLATE);
		template.addCoOwners(context, selectedCoOwnerList);
%>
	 	<script language="javascript" type="text/javaScript">
		 	getTopWindow().getWindowOpener().refreshSBTable(getTopWindow().getWindowOpener().configuredTableName);
   			getTopWindow().close();
	 	</script>
<%
	} else if("RemoveTemplateCoOwner".equalsIgnoreCase(strMode)) {
		String[] selectedCoOwnerList = emxGetParameterValues(request, "emxTableRowId");

		ProjectTemplate template = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
		template.removeCoOwners(context, selectedCoOwnerList);
%>
	 	<script language="javascript" type="text/javaScript">
		 	 var topFrame = findFrame(getTopWindow(), "detailsDisplay");
    		 topFrame.location.href = topFrame.location.href;
	 	</script>
<%
				 }else if("TemplateQuestion".equalsIgnoreCase(strMode)) {
					String URL = "../common/emxIndentedTable.jsp?table=PMCQuestionSummaryTable&selection=multiple&sortColumnName=Name&header=emxFramework.Command.Question&toolbar=PMCQuestionToolbar&massPromoteDemote=true&expandProgram=emxQuestion:getQuestionORQuestionTaskList&helpMarker=emxhelpquestionsummary&postProcessJPO=emxQuestionBase:postProcessRefresh";
					String templateId = XSSUtil.encodeURLForServer(context,emxGetParameter(request, "objectId"));
					URL += "&objectId="+templateId;
					ProjectTemplate projectTemplate = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
			 		boolean isOwnerOrCoOwner = projectTemplate.isOwnerOrCoOwner(context, templateId);
					if(isOwnerOrCoOwner){
						URL += "&editLink=true";
					}
%>
		             <script language="javascript">
		             	var topFrame = findFrame(getTopWindow(), "detailsDisplay");
		             	topFrame.location.href = "<%=URL%>";
		             </script>
<%
      	}else if("JFCopyPartialSchedule".equalsIgnoreCase(strMode)) {
			// add by ljr 20260512  需要改成单选

				String fieldNameDisplay = emxGetParameter(request, "fieldNameDisplay");
				fieldNameDisplay = XSSUtil.encodeURLForServer(context, fieldNameDisplay);
				String fieldNameActual = emxGetParameter(request, "fieldNameActual");
				fieldNameActual = XSSUtil.encodeURLForServer(context, fieldNameActual);
				String fieldNameOID = emxGetParameter(request, "fieldNameOID");
				fieldNameOID = XSSUtil.encodeURLForServer(context, fieldNameOID);

				//String searchURL = "../common/emxFullSearch.jsp?field=TYPES=type_ProjectSpace,type_ProjectTemplate&table=PMCProjectSummaryForProjects&multiColumnSort=false&sortColumnName=ID&selection=multiple&header=emxProgramCentral.Common.Search&excludeOIDprogram=emxWhatIf:excludeExperimentProject&expandProgram=emxTask:getSubTasks&submitAction=refreshCaller&hideHeader=true&showInitialResults=false&suiteKey=ProgramCentral&cancelLabel=emxProgramCentral.Common.Close";
				//String searchURL = "../common/emxIndentedTable.jsp?program=emxProjectSpace:getProjectTemplatesAndProjects&table=PMCProjectSummaryForProjects&selection=multiple&header=emxProgramCentral.CopySchedule.CopyPartialSchedule&sortColumnName=Name&sortDirection=ascending&Export=false&expandProgram=emxTask:getSubTasks&freezePane=Name&showClipboard=false&showPageURLIcon=false&triggerValidation=false&massPromoteDemote=false&displayView=details&suiteKey=ProgramCentral&StringResourceFileId=emxProgramCentralStringResource";

				//String searchURL = "../common/emxFullSearch.jsp?field=TYPES=type_TaskManagement,type_ProjectSpace,type_ProjectTemplate:Type!=type_Experiment,type_ProjectBaseline,type_VPLMTask,type_PQPTask:CURRENT!=Inactive";
				String searchURL = "../common/emxFullSearch.jsp?field=TYPES=type_TaskManagement,type_ProjectSpace,type_ProjectTemplate:CURRENT!=policy_ProjectTemplate.state_Draft,policy_ProjectTemplate.state_InApproval,policy_ProjectTemplate.state_Obsolete:Type!=type_Experiment,type_ProjectBaseline,type_VPLMTask,type_PQPTask:CURRENT!=Inactive";

				searchURL +=":TSK_TASK_HAS_CONTEXT!=NO_CONTEXT";


				String[] typeArray = new String[]{
				ProgramCentralConstants.TYPE_EXPERIMENT,
				ProgramCentralConstants.TYPE_PROJECT_BASELINE};

				Map<String,StringList> derivativeMap = ProgramCentralUtil.getDerivativeTypeListFromUtilCache(context, typeArray);

				StringList finalExcludeParentTypeList = new StringList();
				finalExcludeParentTypeList.addAll(derivativeMap.get(ProgramCentralConstants.TYPE_EXPERIMENT));
				finalExcludeParentTypeList.addAll(derivativeMap.get(ProgramCentralConstants.TYPE_PROJECT_BASELINE));
				StringBuffer excludeRootTypeStrbuff = new StringBuffer();
				for (int i=0, listSize = finalExcludeParentTypeList.size(); i< listSize; i++){
				excludeRootTypeStrbuff.append(finalExcludeParentTypeList.get(i));
				if(i != (listSize-1)){
				excludeRootTypeStrbuff.append(',');
				}

				}

				if(finalExcludeParentTypeList.size() > 0){
				searchURL +=":PRG_TASK_ROOT_TYPE!="+excludeRootTypeStrbuff.toString();
				}

				searchURL +=":PRG_TASK_ROOT_CURRENT!=Inactive";

				searchURL +="&table=PMCProjectSummaryForProjects&multiColumnSort=false&sortColumnName=ID&selection=single&excludeOIDprogram=emxWhatIf:excludeExperimentProject&expandProgram=emxTask:getSubTasks&submitAction=refreshCaller&hideHeader=true&showInitialResults=false&suiteKey=ProgramCentral&cancelLabel=emxProgramCentral.Common.Close";
				searchURL +="&showRMB=false&submitURL=../programcentral/emxProgramCentralUtil.jsp?mode=copyScheduleSearchPreProcess";
				searchURL +="&fieldNameDisplay="+fieldNameDisplay;
				searchURL +="&fieldNameActual="+fieldNameActual;
				searchURL +="&fieldNameOID="+fieldNameOID;

			%>
			<script language="javascript" type="text/javaScript">
				var strUrl = "<%=searchURL%>";
				strUrl = strUrl + "&frameNameForField="+parent.name;
				//showModalDialog(strUrl, "812", "700", "true", "popup");
				showModalDialog(strUrl);
				//document.location.href = strUrl;
			</script>
			<%
	} else if("CopyPartialSchedule".equalsIgnoreCase(strMode)) {

        	 String fieldNameDisplay = emxGetParameter(request, "fieldNameDisplay");
 		    fieldNameDisplay = XSSUtil.encodeURLForServer(context, fieldNameDisplay);
 		    String fieldNameActual = emxGetParameter(request, "fieldNameActual");
 		    fieldNameActual = XSSUtil.encodeURLForServer(context, fieldNameActual);
 		    String fieldNameOID = emxGetParameter(request, "fieldNameOID");
 		    fieldNameOID = XSSUtil.encodeURLForServer(context, fieldNameOID);

    		//String searchURL = "../common/emxFullSearch.jsp?field=TYPES=type_ProjectSpace,type_ProjectTemplate&table=PMCProjectSummaryForProjects&multiColumnSort=false&sortColumnName=ID&selection=multiple&header=emxProgramCentral.Common.Search&excludeOIDprogram=emxWhatIf:excludeExperimentProject&expandProgram=emxTask:getSubTasks&submitAction=refreshCaller&hideHeader=true&showInitialResults=false&suiteKey=ProgramCentral&cancelLabel=emxProgramCentral.Common.Close";
    		//String searchURL = "../common/emxIndentedTable.jsp?program=emxProjectSpace:getProjectTemplatesAndProjects&table=PMCProjectSummaryForProjects&selection=multiple&header=emxProgramCentral.CopySchedule.CopyPartialSchedule&sortColumnName=Name&sortDirection=ascending&Export=false&expandProgram=emxTask:getSubTasks&freezePane=Name&showClipboard=false&showPageURLIcon=false&triggerValidation=false&massPromoteDemote=false&displayView=details&suiteKey=ProgramCentral&StringResourceFileId=emxProgramCentralStringResource";
			
			//String searchURL = "../common/emxFullSearch.jsp?field=TYPES=type_TaskManagement,type_ProjectSpace,type_ProjectTemplate:Type!=type_Experiment,type_ProjectBaseline,type_VPLMTask,type_PQPTask:CURRENT!=Inactive";
			String searchURL = "../common/emxFullSearch.jsp?field=TYPES=type_TaskManagement,type_ProjectSpace,type_ProjectTemplate:CURRENT!=policy_ProjectTemplate.state_Draft,policy_ProjectTemplate.state_InApproval,policy_ProjectTemplate.state_Obsolete:Type!=type_Experiment,type_ProjectBaseline,type_VPLMTask,type_PQPTask:CURRENT!=Inactive";
			
			searchURL +=":TSK_TASK_HAS_CONTEXT!=NO_CONTEXT";


                String[] typeArray = new String[]{
						ProgramCentralConstants.TYPE_EXPERIMENT,
		                ProgramCentralConstants.TYPE_PROJECT_BASELINE};

		        Map<String,StringList> derivativeMap = ProgramCentralUtil.getDerivativeTypeListFromUtilCache(context, typeArray);

		        StringList finalExcludeParentTypeList = new StringList();
		        finalExcludeParentTypeList.addAll(derivativeMap.get(ProgramCentralConstants.TYPE_EXPERIMENT));
		        finalExcludeParentTypeList.addAll(derivativeMap.get(ProgramCentralConstants.TYPE_PROJECT_BASELINE));
		        StringBuffer excludeRootTypeStrbuff = new StringBuffer();
		        for (int i=0, listSize = finalExcludeParentTypeList.size(); i< listSize; i++){
		        	excludeRootTypeStrbuff.append(finalExcludeParentTypeList.get(i));
		        	if(i != (listSize-1)){
		        		excludeRootTypeStrbuff.append(',');
		        	}
		        	
		        }
				
				if(finalExcludeParentTypeList.size() > 0){
					searchURL +=":PRG_TASK_ROOT_TYPE!="+excludeRootTypeStrbuff.toString();
				}
				
				searchURL +=":PRG_TASK_ROOT_CURRENT!=Inactive";
			
			searchURL +="&table=PMCProjectSummaryForProjects&multiColumnSort=false&sortColumnName=ID&selection=multiple&excludeOIDprogram=emxWhatIf:excludeExperimentProject&expandProgram=emxTask:getSubTasks&submitAction=refreshCaller&hideHeader=true&showInitialResults=false&suiteKey=ProgramCentral&cancelLabel=emxProgramCentral.Common.Close";
    		searchURL +="&showRMB=false&submitURL=../programcentral/emxProgramCentralUtil.jsp?mode=copyScheduleSearchPreProcess";
    		searchURL +="&fieldNameDisplay="+fieldNameDisplay;
    		searchURL +="&fieldNameActual="+fieldNameActual;
    		searchURL +="&fieldNameOID="+fieldNameOID;

    %>
    	 	<script language="javascript" type="text/javaScript">
    	 	 var strUrl = "<%=searchURL%>";
		 strUrl = strUrl + "&frameNameForField="+parent.name;
    	 	//showModalDialog(strUrl, "812", "700", "true", "popup");
		 showModalDialog(strUrl);
    	 	//document.location.href = strUrl;
    	 	</script>
    <%
      	}else if("copyScheduleSearchPreProcess".equalsIgnoreCase(strMode)){

		  	String[] rows = (String[]) emxGetParameterValues(request, "emxTableRowId");
		  	String errorMsg = ProgramCentralConstants.EMPTY_STRING;
		  	StringBuilder url = new StringBuilder();
		  	StringList emxTableRowIds = new StringList();
		    if(null==rows)
		    {
		    	errorMsg = EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource",context.getLocale(), "emxProgramCentral.Common.SelectItem"); 
			}else{
			String fieldNameDisplay = emxGetParameter(request, "fieldNameDisplay");
			fieldNameDisplay = XSSUtil.encodeURLForServer(context, fieldNameDisplay);
			String fieldNameActual = emxGetParameter(request, "fieldNameActual");
			fieldNameActual = XSSUtil.encodeURLForServer(context, fieldNameActual);
			String fieldNameOID = emxGetParameter(request, "fieldNameOID");
			fieldNameOID = XSSUtil.encodeURLForServer(context, fieldNameOID);
			String frameNameForField = emxGetParameter(request, "frameNameForField");
			frameNameForField = XSSUtil.encodeURLForServer(context, frameNameForField);

      			  	Task tsk = new Task();
	      				emxTableRowIds = tsk.copySchedulePreProcess(context, request);

			//Make AEFSearchUtil url with valid task ids.
				url.append("../common/AEFSearchUtil.jsp?mode=prg&typeAhead=false");
			//StringBuilder url = new StringBuilder("./FullSearchUtil.jsp?mode=Chooser&typeAhead=false");
			url.append("&fieldNameDisplay="+fieldNameDisplay);
			url.append("&fieldNameActual="+fieldNameActual);
			url.append("&fieldNameOID="+fieldNameOID);
			url.append("&frameNameForField="+frameNameForField);

  		  	Enumeration requestParams = emxGetParameterNames(request);
  		  	while(requestParams.hasMoreElements()){
    		  String param = (String) requestParams.nextElement();
    		  if("emxTableRowId".equalsIgnoreCase(param)){
    			  for(Iterator itrValidTasks = emxTableRowIds.iterator(); itrValidTasks.hasNext();){
    				  String row = (String) itrValidTasks.next();
    			  	  url.append("&emxTableRowId=");
    			  	  url.append(row);
					}
				}
			}
		    }
	
		    int size = emxTableRowIds.size();
		    if(size <= 0 && (errorMsg == null || errorMsg=="")){
		    	errorMsg = EnoviaResourceBundle.getProperty(context,"emxProgramCentralStringResource",context.getLocale(), "emxProgramcentral.WBS.Copy.NoValidTaskFound"); 
		    }
		    
  		  	if(emxTableRowIds.size() > 0 && (errorMsg == null || errorMsg=="")){
  		  	%>
     	 	<script language="javascript" type="text/javaScript">
     	 	 var strUrl = "<%=url.toString()%>";
     	 	//strUrl = strUrl + "&frameNameForField="+parent.parent.getWindowOpener().parent.name;
       		 document.location.href = encodeURI(strUrl);
     	 	</script>
     	 	<%
  		  	}else{
      		  if(errorMsg != null && errorMsg!=""){
      		  	%>
      		  	<script>
    		  	alert('<%=errorMsg%>');
      		  	getTopWindow().location.href=getTopWindow().location.href;
      		  	 <%
      		  }
      		%>
					parent.setSubmitURLRequestCompleted();
      		  	</script>
      		  	<%
  		  	}
				}else if ("showTask".equalsIgnoreCase(strMode)) {
 		      		String objectId = XSSUtil.encodeURLForServer(context,emxGetParameter(request, "objectId"));
 		      		DomainObject task = DomainObject.newInstance(context, objectId);

		      		String palId = emxGetParameter(request, "palId");
		      		DomainObject pal = DomainObject.newInstance(context, palId);

		      		String projectId = (String) pal.getInfo(context, "from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id");
		      		DomainObject project = DomainObject.newInstance(context, projectId);

		      		String url = "../common/emxTree.jsp?objectId=" + objectId;
		      		url += "&suiteKey=ProgramCentral";

		      		if(project.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_TEMPLATE)
		      				&& !task.isKindOf(context, ProgramCentralConstants.TYPE_GATE)){
		      			url += "&treeMenu=type_TaskTemplate";
					}
				%>
					<script language="javascript">
	              		 var strUrl = "<%=url%>";
	              		 showModalDialog(strUrl, "812", "700", "true", "popup");
		            </script>
		       <%
	   } else if("ProjectDetailsPreferences".equalsIgnoreCase(strMode)){
      		String strURL = "../common/emxForm.jsp?form=PMCProjectPreferences&mode=view&HelpMarker=emxhelpprojectspacealertsettingsdialog&Export=false&postProcessJPO=emxProjectSpace:updateProjectUserPreference";

      		String objectId = emxGetParameter(request, "objectId");
      		/*
      		Enumeration requestParams = emxGetParameterNames(request);

      		StringBuilder url = new StringBuilder();
	  		if(requestParams != null){
	      	  while(requestParams.hasMoreElements()){
		       		  String param = (String)requestParams.nextElement();
		       		  String value = emxGetParameter(request,param);
		       		  url.append("&"+param);
		       		  url.append("="+XSSUtil.encodeForURL(context, value));
	      	  }
	  		}

  		  	strURL = strURL + url.toString();
			*/
			strURL = ProgramCentralUtil.appendRequestParameters(context,request,strURL);
    		Map paramMap = new HashMap(1);
	    	paramMap.put("objectId",objectId);

          	String[] methodArgs = JPO.packArgs(paramMap);
          	boolean hasModifyAccess  = (boolean) JPO.invoke(context,"emxProjectMember", null, "hasAccessToModifyProject", methodArgs, Boolean.class);

      		if(hasModifyAccess){
      			strURL = strURL + "&editLink=true";
      		}

      		%>
	            <script language="javascript">
	          		 var url = "<%=strURL%>";
	          		 document.location.href = url;
	            </script>
           <%
      	}else if("addMember".equalsIgnoreCase(strMode)){
				   String projectId = emxGetParameter(request, "parentOID");
   				   if(ProgramCentralUtil.isNullString(projectId)){
					   projectId = emxGetParameter(request, "objectId");   
				   } 
				   
				   String[] ids = emxGetParameterValues(request, "emxTableRowId");

				   try{
					   for(int i =0, len = ids.length; i<len;i++){
					    	StringList personIdList = StringUtil.split(ids[i], "|");

					    	if( personIdList.size() >= 1 ){
					    		String personId = (String)personIdList.get(0);

					    		DomainRelationship.connect(context,
				                		projectId,
				                		DomainConstants.RELATIONSHIP_MEMBER,
				                		personId,
				                		false);
					    	}
					    }
				   }catch(Exception e){
					   e.printStackTrace();
				   }finally{
					   CacheUtil.removeCacheObject(context, projectId);
				   }

				   	%>
					<script language="javascript">
						getTopWindow().getWindowOpener().location.reload();
						getTopWindow().closeWindow();
		            </script>
		       		<%
		    }else if("deleteMember".equalsIgnoreCase(strMode)){

		    	String projectId 	= emxGetParameter(request, "parentOID");
		    	String objectId 	= emxGetParameter(request, "objectId");
		    	String[] ids 		= emxGetParameterValues(request, "emxTableRowId");
				String portalCmd 	= XSSUtil.encodeURLForServer(context,(String)emxGetParameter(request,"portalCmdName"));

		    	String contextUser = context.getUser();
		    	String owner = new DomainObject(projectId).getOwner(context).getName();
		    	StringList deleteOwnershipIds = new StringList();

		    	try{
		    		boolean isOwnerOrContextUser = false;
		    		int len = ids.length;

					StringList deletionList = new StringList();


		    		for(int i =0; i<len;i++){
						boolean isOwnerOrLoggedInUser = false;
		    			StringList idList = StringUtil.split(ids[i], "|");
		    			if(idList.size() > 2){
		    				String ownerName = "";
		    				String strProject = "";
		    				String strOrganization = "";
				            String strOwnership = "";

		    				String secContextId = (String)idList.get(0);
				        	int length = secContextId.length();
		    				String busId = (String)idList.get(1);

		    				int fIndex = -1,sIndex = -1,tIndex = -1;
		    				fIndex =secContextId.indexOf(":");

		    				if (fIndex != -1){
		    					sIndex =secContextId.indexOf(":", fIndex+1);
		    				}

		    				if (sIndex != -1){
		    					tIndex =secContextId.indexOf(":", sIndex+1);
		    				}

		    				if (fIndex != -1 && sIndex != -1){
		    					strOrganization = secContextId.substring(fIndex+1, sIndex);
		    				}

		    				if (sIndex != -1 && tIndex != -1){
		    					strProject = secContextId.substring(sIndex+1, tIndex);
		    				}

			                if (tIndex+1 <= length){
			                	strOwnership = secContextId.substring(tIndex+1, length);
		                	}

		    				if(ProgramCentralUtil.isNotNullString(strProject) && ProgramCentralUtil.isNullString(strOrganization)){
		    					String cmd 		= "print role $1 select person dump";
		    					String result 	= MqlUtil.mqlCommand(context, cmd, strProject);
		    					
		    					StringList users = FrameworkUtil.split(result, ProgramCentralConstants.COMMA);
		    					
		    					//Following condition added for user group
		    					if(users.size()==1 && strProject.endsWith("_PRJ")){
		    					
		    					String personId = PersonUtil.getPersonObjectID(context, result);

		    					String relationship_Member = PropertyUtil.getSchemaProperty(context,"relationship_Member");
		    					String mqlQueryString = "print bus $1 select $2 dump";
		    					String relId = MqlUtil.mqlCommand(context, mqlQueryString, true,busId,"from["+ relationship_Member +"|to.id==" + personId +"].id");
		    					isOwnerOrLoggedInUser =  (contextUser.equalsIgnoreCase(result) || owner.equalsIgnoreCase(result));

		    					if( relId != null && !"".equals(relId) && ! isOwnerOrLoggedInUser){
		    						DomainRelationship.disconnect(context, relId);
		    						deleteOwnershipIds.add(ids[i]);
		    					}
		    					if(isOwnerOrLoggedInUser){
		    						isOwnerOrContextUser = true;
		    					}
		    					}else{
		    						deleteOwnershipIds.add(ids[i]);
		    					}
			                 }else if(ProgramCentralUtil.isNotNullString(strProject) && ProgramCentralUtil.isNotNullString(strOrganization)){
			                	 if("Primary".equalsIgnoreCase(strOwnership)){
			                		isOwnerOrContextUser = true;
			                		isOwnerOrLoggedInUser = true;
			                	 }else{
			                	 	deleteOwnershipIds.add(ids[i]);
			                	 }
		    				}
		    			}

				        if(!isOwnerOrLoggedInUser){
				        	deletionList.addElement(ids[i]);
				        }
		    		}

		    		//Delete access
		    		String jpoName = "emxDomainAccess";
		    		String methodName = "deleteAccess";

		    		Map paramMapForJPO = new HashMap();
		    		paramMapForJPO.put("busObjId", objectId);
		    		String[] idsArray = new String[deleteOwnershipIds.size()];
		    		deleteOwnershipIds.toArray(idsArray);
		    		paramMapForJPO.put("emxTableRowIds" ,idsArray);
		    		String[] args = JPO.packArgs(paramMapForJPO);
		    		JPO.invoke(context, jpoName, null, methodName, args);
		    		if (isOwnerOrContextUser)
		    		{
		    			String sErrMsg = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Message.CannotDeletetheOwner",  sLanguage);
		    			%>
		    			<script language="javascript" type="text/javaScript">
		    			alert("<%=XSSUtil.encodeForJavaScript(context,sErrMsg)%>");
		    			</script>
		    			<%
		    		}
				  	String[] deleteRowArr = new String[deletionList.size()];
				  	deletionList.toArray(deleteRowArr);

		    		%>
					<script language="javascript">
						var portalName = "<%=portalCmd%>";
			    		var topFrame = findFrame(getTopWindow(), portalName);
			      		if(topFrame == null){
			 				if(null == topFrame){
			 				    topFrame = findFrame(getTopWindow(), "detailsDisplay");
								if(null == topFrame){
				 					  topFrame = top.window;
				 				 }
			 				}
			 			}
			      		var arrDeleteIds=[];
			      	<%
				      	for(int i=0,size=deleteRowArr.length;i<size;i++){
				      		%>
							arrDeleteIds.push("<%=deleteRowArr[i]%>");
							<%
				      	}
			      	%>
			      		topFrame.emxEditableTable.removeRowsSelected(arrDeleteIds);
						//getTopWindow().refreshTablePage();
			        </script>
					<%


		    	}catch(Exception e){
		    		e.printStackTrace();
		    	}
      	}else if("createRiskRPN".equalsIgnoreCase(strMode)) {
			String riskId = XSSUtil.encodeURLForServer(context,emxGetParameter(request, "objectId"));
		%>
 		 	<script language="javascript" type="text/javaScript">
 		 	 	var topFrame = findFrame(getTopWindow(), "PMCProjectRisk");
 		 	 	if(! topFrame){
 		 	 		topFrame = findFrame(getTopWindow(),"detailsDisplay");
 		 	 	}
	    		var xmlRef = topFrame.oXML;
	    		var riskId = '<%=riskId%>';
		    	var nParent = emxUICore.selectSingleNode(xmlRef, "/mxRoot/rows//r[@o = '" + riskId + "']");
		    	var rowid = nParent.getAttribute("id");
				topFrame.emxEditableTable.expand([rowid], "1");
				topFrame.refreshStructureWithOutSort(); 
 		 	</script>
		<%
		}else if ("errorMessageWithPlaceHolder".equalsIgnoreCase(strMode)) {
			String errorKey = emxGetParameter(request,"errorKey");
			String msgValue = emxGetParameter(request,"msgValue");
            String[] messageValues = new String[1];
            messageValues[0] = msgValue;
			String sMessage = MessageUtil.getMessage(context, null, errorKey, messageValues, null, context.getLocale(), "emxProgramCentralStringResource");
			out.clear();
			out.write(sMessage);
			return;
      	}else if("refreshBudget".equalsIgnoreCase(strMode)) {
    		String portalCommandName = (String)emxGetParameter(request, "portalCmdName");
    		if(ProgramCentralUtil.isNullString(portalCommandName))
    		{
    		portalCommandName = (String)emxGetParameter(request, "openerFrame");
    		if(ProgramCentralUtil.isNullString(portalCommandName))
    		{
    			portalCommandName ="PMCProjectBudget";
    		}
    		}
    		%>
    			<script language="javascript" type="text/javaScript">
    	  	  var frame = "<%=portalCommandName%>";
    		  var topFrame = findFrame(getTopWindow(), frame);
    		      if(null == topFrame)
    		  		topFrame = findFrame(getTopWindow(), "detailsDisplay");

    		  topFrame.parent.location.href =topFrame.parent.location.href;
    			</script>
    		<%
      	}else if("createRisk".equalsIgnoreCase(strMode)){
			 String[] selectedIds = request.getParameterValues("emxTableRowId");
			 String projectId = emxGetParameter( request, "parentOID");
			 String objectId = emxGetParameter( request, "objectId");
			 String currentFrame = XSSUtil.encodeURLForServer(context, emxGetParameter(request, "portalCmdName"));
	   			String errorMessage = ProgramCentralConstants.EMPTY_STRING;
	   			String riskCreationURL = ProgramCentralConstants.EMPTY_STRING;

  				if (selectedIds != null && selectedIds.length != 0) {
	   			   if (selectedIds.length == 1) {
	   					String selectedId =  ProgramCentralUtil.parseTableRowId(context,selectedIds)[0];
	   					boolean isOfRiskType =  ProgramCentralUtil.isOfGivenTypeObject(context,DomainConstants.TYPE_RISK,selectedId);
	   					boolean isOfRPNType =   ProgramCentralUtil.isOfGivenTypeObject(context,DomainConstants.TYPE_RPN,selectedId);
	   					if (!(isOfRiskType || isOfRPNType)) {
	   						riskCreationURL =
	   								"../common/emxCreate.jsp?type=type_Risk&typeChooser=true&policy=policy_ProjectRisk&form=PMCCreateRiskForm&formHeader=emxProgramCentral.Risk.CreateRisk&HelpMarker=emxhelpriskcreatedialog&findMxLink=false&nameField=autoName&showApply=true&createJPO=emxRisk:createAndConnectRisk&postProcessJPO=emxRiskBase:createRisk&postProcessURL=../programcentral/emxProgramCentralUtil.jsp?mode=addNewObjectThroughXML&submitAction=doNothing&relName="+DomainRelationship.RELATIONSHIP_RISK;
	   						Enumeration requestParams = emxGetParameterNames(request);
	   		  		  	StringBuilder url = new StringBuilder();
	   						if(requestParams != null){
	   		        		  while(requestParams.hasMoreElements()){
	   			        		  String param = (String)requestParams.nextElement();
	   			        		  String value = emxGetParameter(request,param);
	   			        		  // NX5 - Turn off the toolbars
	   			        		  if ("hideToolbar".equalsIgnoreCase(param)) value = "true";
	   			        		  if ("hideHeader".equalsIgnoreCase(param)) value = "true";
	   			        		  if ("toolbar".equalsIgnoreCase(param)) value ="";
                                  if ("editLink".equalsIgnoreCase(param)) value = "false";
                                  if ("Export".equalsIgnoreCase(param)) value ="false";
	   			        		  url.append("&"+param);
	   			        		  url.append("="+XSSUtil.encodeForURL(context, value));
	   		        		  }
	   		        		  riskCreationURL = riskCreationURL + url.toString();
	   		  		  	}

	   					}else{
		   	   				 errorMessage = ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Project.SelectProject",sLanguage);
			 	   			  %>
			 	   			  	<script language="javascript" type="text/javaScript">
		        					alert("<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Risk.SelectNodeForCreateRisk</emxUtil:i18nScript>");
		        					getTopWindow().closeSlideInDialog();
		        				</script>
			 	   			  <%
			 	   			 return;


			 	   		}
	   			   }
	   		   	}


				if (ProgramCentralUtil.isNullString(errorMessage)) {
	   				%>
	     		 	<script language="javascript" type="text/javaScript">
	     		 	 	var riskCreationURL = "<%=XSSUtil.encodeForJavaScript(context,riskCreationURL)%>";
	     		 	 	getTopWindow().showSlideInDialog(riskCreationURL,true);
	     		 	</script>
	     			<%
	   		   	}

        } else if ("addNewObjectThroughXML".equalsIgnoreCase(strMode)) 	{
    		Context contextDB = (matrix.db.Context)request.getAttribute("context");
    		String portalCommandName = (String)emxGetParameter(request, "portalCmdName");
    		String newTaskId = emxGetParameter(request, "newObjectId");
    		newTaskId = XSSUtil.encodeURLForServer(context, newTaskId);
    		String objectId = emxGetParameter(request, "objectId");
    		objectId = XSSUtil.encodeURLForServer(context, objectId);
    		String relationship = (String)emxGetParameter(request, "relName");

    		DomainObject newObject = DomainObject.newInstance(contextDB, newTaskId);
    		String relId =  newObject.getInfo(contextDB,"to[" + relationship + "].id");
    		relId = XSSUtil.encodeURLForServer(context, relId);



    		 boolean isFromRMB = "true".equalsIgnoreCase(emxGetParameter(request, "isFromRMB"));
    		  StringBuffer sBuff = new StringBuffer();
    		    sBuff.append("<mxRoot>");
    		    sBuff.append("<action><![CDATA[add]]></action>");

    		    if (isFromRMB) {
    		        sBuff.append("<data fromRMB=\"true\" status=\"committed\">");
		    	} else {
    		    	sBuff.append("<data status=\"committed\">");
    		    }

    		    sBuff.append("<item oid=\""+newTaskId+"\" relId=\""+relId+"\" pid=\""+objectId+"\"/>");
    		    sBuff.append("</data>");
    		    sBuff.append("</mxRoot>");
    	  %>
    	  <script language="javascript">
    	  var frame = "<%=portalCommandName%>";
    	  var topFrame = findFrame(getTopWindow(), frame);

    	  if(null == topFrame){
    	  	topFrame = findFrame(getTopWindow(), "PMCProjectRisk");
    	      if(null == topFrame)
    	  		topFrame = findFrame(getTopWindow(), "frameDashboard");
    	      if(null == topFrame)
    	  		topFrame = findFrame(getTopWindow(), "detailsDisplay");
    	  }

    	  topFrame.emxEditableTable.addToSelected('<%=XSSUtil.encodeForJavaScript(context,sBuff.toString())%>');
    	  topFrame.refreshStructureWithOutSort();
         </script>
    	  <%
    	}else if ("PMCAssignee".equalsIgnoreCase(strMode)) {
         	//This mode is forming the href for PMCAssignee command
         	String objectId = emxGetParameter(request, "objectId");
			String mqlCommand = "print bus $1 select $2 $3 dump $4";
    		String sResult = MqlUtil.mqlCommand(context, mqlCommand, objectId,"current", ProgramCentralConstants.SELECT_IS_EXPERIMENT_TASK, "|");
    		
    		StringList resultList = FrameworkUtil.split(sResult, "|");
    		int size = resultList.size();
    		String strState = DomainConstants.EMPTY_STRING;
    		String isExperimentTask = DomainConstants.EMPTY_STRING;
    		if(size>=2){
    			strState = resultList.get(0);
    			isExperimentTask = resultList.get(1);
    		}else if(size==1){
    			strState = resultList.get(0);
    		}
			
         	//DomainObject dom = DomainObject.newInstance(context, objectId);
         	//String strState = dom.getCurrentState(context).getName();

         	String strURL = "../common/emxIndentedTable.jsp?program=emxTask:getMembers&table=PMCTaskAssigneeSummary&selection=multiple&calculations=true&objectCompare=true&chart=true&toolbar=PMCAssigneeSummaryToolbarActions&expandProgram=emxTaskBase:getGroupMembers&expandLevelFilter=false&header=emxProgramCentral.Common.TaskAssignees&HelpMarker=emxhelpassigneesummary&Export=false&sortColumnName=Name&sortDirection=ascending&preProcessJPO=emxTask:preProcessCheckForEdit&objectId="+objectId;

         	if(!DomainObject.STATE_PROJECT_SPACE_COMPLETE.equalsIgnoreCase(strState) && !("true".equalsIgnoreCase(isExperimentTask))){
         		strURL = strURL + "&editLink=true";
         	}
			/*
         	Enumeration requestParams = emxGetParameterNames(request);
    	  	StringBuilder url = new StringBuilder();

    	  	if(requestParams != null){
    		  while(requestParams.hasMoreElements()){
    			  String param = (String)requestParams.nextElement();
    			  String value = emxGetParameter(request,param);
    			  url.append("&"+param);
    			  url.append("="+XSSUtil.encodeForURL(context, value));
    		  }
    		  strURL += url.toString();
    	  	}
			*/
			strURL = ProgramCentralUtil.appendRequestParameters(context,request,strURL);
         	%>
         	<script language="javascript">
         	var strUrl = "<%=strURL%>";
         	document.location.href = strUrl;
         	</script>
         	<%
    	}else if("GateApproved".equalsIgnoreCase(strMode)){

           	JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
           	String objectId = emxGetParameter(request,"objectId");
       		DomainObject gate = DomainObject.newInstance(context,objectId);

       		StringList busSelect = new StringList(2);
       		busSelect.addElement(DomainConstants.SELECT_POLICY);
       		busSelect.addElement(ProgramCentralConstants.SELECT_IS_GATE);

       		Map<String,String> taskInfo = gate.getInfo(context, busSelect);
       		String policyName 	= taskInfo.get(DomainConstants.SELECT_POLICY);
       		String isGate	= taskInfo.get(ProgramCentralConstants.SELECT_IS_GATE);

       		if("true".equalsIgnoreCase(isGate)){
       			String prog 		= "emxProjectHoldAndCancel";
       			String method 		= "triggerCheckLastDecision";
       			String[] arguments 	= new String[]{objectId,"","","","JSP"};
       			String[] constructor = new String[1];

       			int sucess = JPO.invoke(context, prog, constructor, method, arguments);
       			if(sucess == 1){
       				String errMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
       						"emxProgramCentral.Gate.errMsg1", sLanguage);
       				jsonObjectBuilder.add("Action","stop");
       				jsonObjectBuilder.add("Error",errMsg);
       			}else{
       				jsonObjectBuilder.add("Action","continue");
       			}

       		}else{
       			jsonObjectBuilder.add("Action","continue");
       		}
       		out.clear();
       		out.write(jsonObjectBuilder.build().toString());

       		return;
            }else if ("checkObjectPolicy".equalsIgnoreCase(strMode)) {
 			String objectId = emxGetParameter(request,"objectId");
			DomainObject task = DomainObject.newInstance(context,objectId);

			StringList busSelect = new StringList();
			busSelect.addElement(DomainConstants.SELECT_POLICY);
			busSelect.addElement(ProgramCentralConstants.SELECT_IS_SUMMARY_TASK);

			Map<String,String> taskInfo = task.getInfo(context, busSelect);
			String policyName 	= taskInfo.get(DomainConstants.SELECT_POLICY);
			String isSummary	= taskInfo.get(ProgramCentralConstants.SELECT_IS_SUMMARY_TASK);

			JsonObject jsonObject = Json.createObjectBuilder()
									.add("Policy",policyName)
		    						.add("SummaryTask",isSummary)
		    						.build();

			out.clear();
			out.write(jsonObject.toString());
			return;
    	}else if("refreshStructureExperiment".equals(strMode)) {
       		%>
       		<script language="javascript">
   			   var detailsDisplay_1 = findFrame(getTopWindow(), "PMCWhatIfProjectChangeList");
   			   var detailsDisplay_2 = findFrame(getTopWindow(), "PMCWhatIfProjectExperimentsList");
   				    detailsDisplay_1.refreshSBTable();
   				    detailsDisplay_2.refreshSBTable();

       	     </script>
       		<%
	} else if("cutPreProcess".equalsIgnoreCase(strMode)) {
		String objIds = (String) emxGetParameter(request, "objIds");
		StringList slIds = FrameworkUtil.split(objIds, ",");
		StringList slInvalidIds = new StringList();
		StringList slInvalidTaskNames = new StringList();

		String[] ids = new String[slIds.size()];
		slIds.toArray(ids);
		StringList taskSelect = new StringList();
		taskSelect.add(ProgramCentralConstants.SELECT_CURRENT);
		taskSelect.add(ProgramCentralConstants.SELECT_ID);
		taskSelect.add(ProgramCentralConstants.SELECT_NAME);
		taskSelect.add("from[Deleted Subtask]");

		//Add following select only when Mandatory Task Enforcement is active.
		if(Task.checkEnforceMandatoryTasks(context)){
			taskSelect.add("attribute[Task Requirement]");
		}

		MapList taskInfoList = DomainObject.getInfo(context, ids, taskSelect);
		int taskSize = taskInfoList.size();
		boolean isMandatoryTask = false;
		for(int index=0; index<taskSize; index++){
			Map<String, String> taskInfo = (Map<String, String>) taskInfoList.get(index);
			String current = (String) taskInfo.get(ProgramCentralConstants.SELECT_CURRENT);
			String id = (String) taskInfo.get(ProgramCentralConstants.SELECT_ID);
			String hasDeletedSubtask = (String) taskInfo.get("from[Deleted Subtask]");
			isMandatoryTask = false;
			String taskReq = (String) taskInfo.get("attribute[Task Requirement]");
			if("Mandatory".equalsIgnoreCase(taskReq)){
				isMandatoryTask = true;
			}
			if((!ProgramCentralConstants.STATE_PROJECT_TASK_CREATE.equals(current)
					&& !ProgramCentralConstants.STATE_PROJECT_TASK_ASSIGN.equals(current)
					&& !ProgramCentralConstants.STATE_PROJECT_TASK_ACTIVE.equals(current))
					|| "TRUE".equalsIgnoreCase(hasDeletedSubtask)
					|| isMandatoryTask){
				slInvalidIds.add(id);
				String name = (String) taskInfo.get(ProgramCentralConstants.SELECT_NAME);
				slInvalidTaskNames.add(name);
			}
		}
		JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
		if(slInvalidIds.size() > 0){
			String sInvalidTasks = FrameworkUtil.join(slInvalidIds, ",");
			String sInvalidTaskNames = FrameworkUtil.join(slInvalidTaskNames, ",");
			String message = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramcentral.WBS.CutPaste.TaskCannotBeCut",
					context.getLocale().getLanguage());
			jsonObjectBuilder.add("error", message + "\n" + slInvalidTaskNames);
			jsonObjectBuilder.add("invalidTasks", sInvalidTasks);
		}
		out.clear();
		out.write(jsonObjectBuilder.build().toString());
		return;
	}else if("pastePreProcess".equalsIgnoreCase(strMode)) {
		JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
		String objIds = (String) emxGetParameter(request, "objIds");
		String pasteTaskAbove = (String) emxGetParameter(request, "pasteTaskAbove");
		StringList slIds = FrameworkUtil.split(objIds, ",");
		if(slIds.size() > 1){
			String err = EnoviaResourceBundle.getProperty(context, "Framework",
					"emxFramework.Common.PleaseSelectOneItemOnly",
					context.getLocale().getLanguage());
			jsonObjectBuilder.add("error", err);
		}else{
			StringList taskSelect = new StringList();
			if("true".equalsIgnoreCase(pasteTaskAbove)){
				taskSelect.add("to[Subtask].from.current");
				taskSelect.add("to[Subtask].from.name");
			}else{
				taskSelect.add(ProgramCentralConstants.SELECT_CURRENT);
				taskSelect.add(ProgramCentralConstants.SELECT_NAME);
			}
			DomainObject task = DomainObject.newInstance(context, (String)slIds.get(0));
			Map taskInfo = task.getInfo(context, taskSelect);
			String current = ProgramCentralConstants.EMPTY_STRING;
			String name = ProgramCentralConstants.EMPTY_STRING;

			if("true".equalsIgnoreCase(pasteTaskAbove)){
				current = (String) taskInfo.get("to[Subtask].from.current");
				name = (String) taskInfo.get("to[Subtask].from.name");
			}else{
				current = (String) taskInfo.get(ProgramCentralConstants.SELECT_CURRENT);
				name = (String) taskInfo.get(ProgramCentralConstants.SELECT_NAME);
			}
			if(ProgramCentralConstants.STATE_PROJECT_TASK_REVIEW.equals(current)
					|| ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE.equals(current)
					|| ProgramCentralConstants.STATE_PROJECT_SPACE_ARCHIVE.equals(current)){
				String message = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
						"emxProgramCentral.Project.TaskInState3",
						context.getLocale().getLanguage());
				jsonObjectBuilder.add("error", message);
			}
		}
		out.clear();
		out.write(jsonObjectBuilder.build().toString());
		return;
        }else if("remove".equalsIgnoreCase(strMode)){
        	String portalCmd 	= (String)emxGetParameter(request,"portalCmdName");
        	
        	String selectedProjectIds 	= (String) emxGetParameter(request, "selectedProjectIds");
	       		StringList deletedObjIdList = FrameworkUtil.split(selectedProjectIds, ",");
        	
	       		
	       		
	    		
	    		Map<String,StringList> deletedObjectIds = new HashMap<>();
	    		
	    		for(int i = 0, j = deletedObjIdList.size(); i<j; i++) {
	    			String deletedProjectId = deletedObjIdList.get(i);

	    			StringList parentIdList = FrameworkUtil.split(deletedProjectId, "|");
	    			String taskId = parentIdList.get(0);
	    			String parentId = parentIdList.get(1);

	    			if(deletedObjectIds.containsKey(parentId)) {
	    				deletedObjectIds.get(parentId).add(taskId);
	    			}else {
	    				StringList deletedTaskSet = new StringList();
	    				deletedTaskSet.add(taskId);
	    				deletedObjectIds.put(parentId, deletedTaskSet);
	    			}
	    		}
	    		
        	
        	String selectedIds = (String) emxGetParameter(request, "selectedIds");
            StringList idList = FrameworkUtil.split(selectedIds, ",");
            String[] relationshipIds = (String []) idList.toArray(new String[] {});
            List impactedObjList = new ArrayList();
            
            try {
            ContextUtil.startTransaction(context, true);
                DomainRelationship relRemove = new DomainRelationship(DomainConstants.RELATIONSHIP_SUBTASK);
                relRemove.disconnect(context, relationshipIds,false);

               // Task project = new Task();
               //     project.reSequence(context, objId);
               
               	//Start- New re-squenece logic
                
       					String mqlCmd = "print bus $1 select $2 $3 dump $4";
       					
       					Set<String> parentIdSet = deletedObjectIds.keySet();
       					
       					Iterator<String> itr = parentIdSet.iterator();
       					while(itr.hasNext()) {
       					
       						String parentId = itr.next();
       						StringList deletedTaskList = deletedObjectIds.get(parentId);
       						
       						
       						
       						String rootNodePALPhysicalId = MqlUtil.mqlCommand(context, 
       								true,
       								true,
       								mqlCmd, 
       								true,
       								parentId,
       								ProgramCentralConstants.SELECT_PAL_PHYSICALID_FROM_PROJECT,
       								ProgramCentralConstants.SELECT_PAL_PHYSICALID_FROM_TASK,
       								"|");
       					

       					ProjectSequence ps = new ProjectSequence(context, rootNodePALPhysicalId);
       		       		ps.unAssignSequence(context, deletedTaskList);
       					//End
       					}
                
                //Added for Auto vs Manual
                Map<String,String> projectScheduleMap = ProgramCentralUtil.getProjectSchedule(context, objId);
                String projectSchedule = projectScheduleMap.get(objId);
                if(ProgramCentralUtil.isNullString(projectSchedule)||
                		ProgramCentralConstants.PROJECT_SCHEDULE_AUTO.equalsIgnoreCase(projectSchedule)){
                	Map rollupMap = TaskDateRollup.rolloutProject(
							context, 
							new StringList(objId),
							false);
					impactedObjList = (List)rollupMap.get("impactedObjectIds");	
                }
                
                ContextUtil.commitTransaction(context);
            }
            catch (Exception e) {
                ContextUtil.abortTransaction(context);
                throw (new FrameworkException(e));
            }
                %>
               <script language="javascript" type="text/javaScript">
               	var rowObjIdArr =[];
               	var relIdArr =[];
               	var portalName = "<%=portalCmd%>";
		        var topFrame = findFrame(getTopWindow(), portalName);
		        if(topFrame == null || topFrame.location.href=="about:blank"){
		     		topFrame = findFrame(getTopWindow(), "PMCWBS");
		     		if(topFrame == null || topFrame.location.href=="about:blank"){
		     			topFrame = findFrame(getTopWindow(), "PMCWBSDataGridView");
		     			if(topFrame == null || topFrame.location.href=="about:blank"){
		     			topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
						   
						   if(topFrame == null || topFrame.location.href=="about:blank"){
						topFrame = findFrame(getTopWindow(), "detailsDisplay");
            	}
		     		}
      		     }
      		     }
               <%
               	int size = impactedObjList.size();
		 		String[] objIdArray = new String[size];	
		 		for (int i=0; i<size; i++) {
				    %>
				    rowObjIdArr.push("<%=(String)impactedObjList.get(i)%>");
				    <%
			    }
		 		
		 		for(int i=0;i<relationshipIds.length;i++){
		 			%>
		 			relIdArr.push("<%=relationshipIds[i]%>");
             <%
		 		}
				%>

				var relRowIdArr = new Array();
				if(!topFrame.dataGridEnabled){
				for(var i=0;i<relIdArr.length;i++){
					var rowObj = emxUICore.selectSingleNode(topFrame.oXML,"/mxRoot/rows//r[@r ='" + relIdArr[i] + "']");
					relRowIdArr[i] = " | | |"+rowObj.getAttribute("id");
				}
				}
				else{
				for(var i=0;i<relIdArr.length;i++){
						relRowIdArr[i] = " | | |"+topFrame.emxEditableTable.getAttributeFromNode("rowId",null, relIdArr[i]);
				}
			}
				topFrame.rebuildViewInProcess = true;
				topFrame.emxEditableTable.removeRowsSelected(relRowIdArr);
				var impObjRowIdArr = new Array();
				var count = 0;
				if(!topFrame.dataGridEnabled){
				for(var i=0;i<rowObjIdArr.length;i++){
					var rowObj = emxUICore.selectSingleNode(topFrame.oXML,"/mxRoot/rows//r[@o ='" + rowObjIdArr[i] + "']");
					if(rowObj != null){
						impObjRowIdArr[count] = rowObj.getAttribute("id");
						count++;
            }
            }
			}
				else{
					for(var i=0;i<rowObjIdArr.length;i++){
						impObjRowIdArr[count] = topFrame.emxEditableTable.getAttributeFromNode("rowId",rowObjIdArr[i]);
						count++;											}
				}
				topFrame.rebuildViewInProcess = false;
				topFrame.emxEditableTable.refreshRowByRowId(impObjRowIdArr);
				topFrame.emxEditableTable.refreshStructureWithOutSort();
	               	
             	</script>
             <%

    	}else if("DeleteProgram".equalsIgnoreCase(strMode)){

        	com.matrixone.apps.program.Program program = (com.matrixone.apps.program.Program) DomainObject.newInstance(context, DomainConstants.TYPE_PROGRAM,"PROGRAM");

        	  String[] programs = emxGetParameterValues(request,"emxTableRowId");
        	  programs = ProgramCentralUtil.parseTableRowId(context,programs);
        	  boolean hasAssociation = false;

        	  if ( programs != null ) {
        	    // get the number of program
        	    int numPrograms = programs.length;
        	    // need to check it to avoid zero using JS.
        	    for (int i=0; numPrograms>i; i++) {
        	      program.setId(programs[i]);
        	      // make sure no projects are assoicated
        	      MapList projects = program.getProjects(context, null, null);
        	      if(projects.isEmpty()) {
        	        program.deleteObject(context, true);
        	      }
        	      else  {
        	        hasAssociation = true;
        	      }

        	    }
        	  }
        	  %>
        	  <script language="javascript" type="text/javaScript">
        	    //hide JavaScript from non-JavaScript browsers
        	    if(<%=hasAssociation%>) {
        	      alert("<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Common.ProgramCanNotBeDeleted</emxUtil:i18nScript>");
        	    }
        	    parent.document.location.href = parent.document.location.href;
        	    // Stop hiding here
        	  </script>
        	  <%

        }else if("ReviseTemplate".equalsIgnoreCase(strMode)){
        	boolean bFlag = false;
            try {
                // TODO - Take the first object selected at the object to clone
                // Change the decription to indicate version from?

                String[] projectTemplates = emxGetParameterValues(request, "objectId");

                if (projectTemplates != null) {
                    //int numProjectTemplates = projectTemplates.length;
                    //for (int i=0; numProjectTemplates>i; i++)

                    // This is a do nothing loop since everything in the list will be a revision
                    // of the 0 item so we are terminating it at item 1
                    for (int i = 0; i < 1; i++) {
                        // Get the last revision of whatever object comes in
                        String projectTemplateId = projectTemplates[i];
                        DomainObject domTemp     = DomainObject.newInstance(context, projectTemplateId);
                        BusinessObject boLatest  = domTemp.getLastRevision(context);
                        projectTemplateId        = boLatest.getObjectId(context);

                        StringList busSelects = new StringList(9);
                        busSelects.addElement(DomainConstants.SELECT_TYPE);
                        busSelects.addElement(DomainConstants.SELECT_NAME);
                        busSelects.addElement(DomainConstants.SELECT_DESCRIPTION);
                        busSelects.addElement(DomainConstants.SELECT_CURRENT);
                        busSelects.addElement(DomainConstants.SELECT_VAULT);
                        busSelects.addElement("attribute[Currency]");
                        busSelects.addElement("attribute[Schedule From]");
						busSelects.addElement("type.kindof[Submission Template]");
                        busSelects.addElement("type.kindof[Design Project Template]");

                        DomainObject domPT = DomainObject.newInstance(context, projectTemplateId);
                        Map map = domPT.getInfo(context, busSelects);

                        // check to make sure it has been released / active
                        String boState  = (String) map.get(DomainConstants.SELECT_CURRENT);
                        if ("Draft".equalsIgnoreCase(boState) || "In Approval".equalsIgnoreCase(boState)) {
                            String errorMessage = ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.ProjectTemplate.LatestRevisionMustBeActive", languageStr);
        				%>
        				<script language="javascript" type="text/javaScript">
        				    alert("<%=XSSUtil.encodeForJavaScript(context,errorMessage)%>");
        				</script>
        				<%
                            continue;
                        }

                        // Not sure why a vault check is here - there has to be one
                        String sVault = (String) map.get(DomainConstants.SELECT_VAULT);
                        if (UIUtil.isNullOrEmpty(sVault))
                        {
                            sVault = "eService Production";
                        }

                        ContextUtil.startTransaction(context, true);
                        BusinessObject boProjectTemplate = new BusinessObject(projectTemplateId);
                        String strRev = boProjectTemplate.getNextSequence(context);

                        BusinessObject boNewRev = boProjectTemplate.revise(context, strRev, sVault);
                        String strNewProjectTemplateOID = boNewRev.getObjectId(context);

                        HashMap hMap = new HashMap(3);
                        hMap.put("objectId", projectTemplateId);

                        HashMap paramMap = new HashMap(1);
                        paramMap.put("objectId", strNewProjectTemplateOID);
                        hMap.put("paramMap", paramMap);

                        HashMap requestMap = new HashMap(8);
                        requestMap.put("objectId", projectTemplateId);
                        requestMap.put("Name", map.get(DomainConstants.SELECT_NAME));
                        requestMap.put("defaultVault", sVault);
                        requestMap.put("ProjectTemplateDescription", map.get(DomainConstants.SELECT_DESCRIPTION));
                        requestMap.put("Currency", map.get("attribute[Currency]"));
                        requestMap.put("ScheduleFrom", map.get("attribute[Schedule From]"));
                        requestMap.put("IsVersion", "true");
                        //While ReviseTemplate same reference document should use - no need to create a copy.
                        requestMap.put("ReferenceDocument", "Reference");
                        hMap.put("requestMap", requestMap);

                        String[] progArgs;
                        progArgs = JPO.packArgs(hMap);
                        JPO.invoke(context, "emxProjectTemplate", null, "performClonePostProcessActions", progArgs);

                        //For LSA			
						if("true".equalsIgnoreCase((String) map.get("type.kindof[Submission Template]")) || "true".equalsIgnoreCase((String) map.get("type.kindof[Design Project Template]"))){
                        	JPO.invoke(context, "com.dassault_systemes.enovia.projectmgmtextensions.ui.ProjectTemplate", null, "performRevisePostProcessActions", progArgs);
                        }

                        ContextUtil.commitTransaction(context);
                    }
                }
            }
            catch (Exception e)
            {
                if (ContextUtil.isTransactionActive(context)) {
                    ContextUtil.abortTransaction(context);
                }
                bFlag = true;
                e.printStackTrace();
                session.putValue("error.message", e.getMessage());
            }
            %>
            <script language="javascript" type="text/javaScript">
                parent.document.location.href=parent.document.location.href;
                getTopWindow().closeWindow();
            </script>
            <%
        }else if("ProjectExport".equalsIgnoreCase(strMode)){
	        	String exportFormat = "";
	        	String errorMessage ="";
	        	String selectedNodeId = emxGetParameter(request, "emxTableRowId");
	        	String exportSubProject = emxGetParameter(request, "exportSubProject");
	        	String objectId = (String)(ProgramCentralUtil.parseTableRowId(context,selectedNodeId)).get("objectId");
	         	String fileName = "Default";
	        	String language = request.getHeader("Accept-Language");

	        	exportFormat = PersonUtil.getExportFormat(context);

	    		String url = "?objectId="+objectId;
	        	url =url+"&exportFormat="+exportFormat;
	        	url =url+"&Accept-Language="+language;
	        	
	        	if("True".equalsIgnoreCase(exportSubProject)){
	        		url =url+"&exportSubProject=true";
	        	}

	        	request.setAttribute("context", context) ;

	        	String contentURL1 ="../programcentral/emxProgramCentralProjectExportProcess.jsp"+url;
	        	  framesetObject fs = new framesetObject();
	        	  fs.setStringResourceFile("emxProgramCentralStringResource");
	        	fs.initFrameset("emxProgramCentral.Common.Export","emxhelpprojectexportdialog",contentURL1,false,true,false,false);
	        	  fs.writeSelectPage(out);

	            if("HTML".equalsIgnoreCase(exportFormat)){
	        		errorMessage = ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.Common.ProjectExportFormat",language);
		   			  %>
		   			  	<script language="javascript" type="text/javaScript">
						alert("<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Common.ProjectExportFormat</emxUtil:i18nScript>");
						</script>
		   			  <%
	            }
	        	return;
        }else if("hasAccessProjectLaborReport".equalsIgnoreCase(strMode)){

        	String[] selectedItems = emxGetParameterValues(request,"emxTableRowId");
        	  StringBuffer sbTableRowIds=new StringBuffer();
			  for(int index=0;index<selectedItems.length;index++){
				if(selectedItems[index].contains("0,"))
					selectedItems[index] = (String)FrameworkUtil.split(selectedItems[index], "|").get(0);
				
				  sbTableRowIds.append(selectedItems[index]);
				  if(selectedItems.length-1 != index)
				  sbTableRowIds.append("|");
			  }
			String selectedItem = XSSUtil.encodeURLForServer(context, sbTableRowIds.toString());

        	String strProjectName = "";
        	StringList slValues = FrameworkUtil.split(selectedItem, "|");
        	DomainObject dObj = DomainObject.newInstance(context);
			int iSize = slValues.size();
        	for(int i = 0; i < iSize; i++){
        		  String strItems = (String)slValues.get(i);
	        		      dObj.setId(strItems);
	        		      if(dObj.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_SPACE)){
					if(!dObj.checkAccess(context, (short) AccessConstants.cModify)){
		        				%>
				        	      <script language="JavaScript" type="text/javascript">
							var strAlertMsg = "<%=EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.LaborReport.CancelState", context.getLocale().toString())%>";
				        	    	    alert(strAlertMsg);
				        	    	    getTopWindow().closeWindow();
				        	      </script>
		        	   	  		<%
		        	    	  	return;
		        	          }
	        	          }
        	        }
        }else if("ProjectLaborReport".equalsIgnoreCase(strMode)){

		 	  String YearType = emxGetParameter(request, "ReportYearBy");
		 	  String selYear = emxGetParameter(request, "ReportingYear");
		 	  String selTimeline = emxGetParameter(request, "DefaultTimeLineInterval");
		 	  Date currDate = new Date();
		 	  String currYear = Integer.toString(currDate.getYear()+1900);

		 	  String passFromJsp = "true";
		 	  String resourceBundle="emxProgramCentralStringResource";
		 	  String[] selectedItems = emxGetParameterValues(request,"emxTableRowId");

			  StringBuffer sbTableRowIds=new StringBuffer();
			  int iSize = selectedItems.length;
			  for(int index=0;index<iSize;index++){
				  if(selectedItems[index].contains("0,")){
					   selectedItems[index] = (String)FrameworkUtil.split(selectedItems[index], "|").get(0);
				  }
				  sbTableRowIds.append(selectedItems[index]);
				  if(selectedItems.length-1 != index)
				  sbTableRowIds.append("|");
			  }
			  String emxTableRowId   =  sbTableRowIds.toString();

		 	  int indexOfFiscal = YearType.indexOf("Fiscal");
		 	  boolean isFiscal = true;
		 	  CalendarType calendarType = null;
		 	  if(indexOfFiscal == -1)
		 	  {
		 		  calendarType = CalendarType.ENGLISH;
		 	  }
		 	  else
		 	  {
		 		  calendarType = CalendarType.FISCAL;
		 	  }
		 	  Interval interval = Helper.yearInterval(calendarType,Integer.parseInt(selYear));
		 	    Date startDate = interval.getStartDate();
		 	    Date endDate = interval.getEndDate();
		 	  SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
		 	  String strStartDate = sdf.format(startDate);
		 	  String strEndDate = sdf.format(endDate);
		 	  String[] messageValues = new String[3];
		 	  messageValues[0] = strStartDate;
		 	  messageValues[1] = strEndDate;
		 	  String subHeader = MessageUtil.getMessage(context,null,
		 	          "emxProgramCentral.WeeklyTimeSheet.Reports.Label",
		 	          messageValues,null,
		 	          context.getLocale(),resourceBundle);
		 	    //String contentURL = "../common/emxIndentedTable.jsp?expandProgramMenu=PMCWTSLaborReportMenu&table=PMCLaborReport&toolbar=PMCWeeklyTimeSheetReportViewToolbarMenu&freezePane=Name&selection=multiple&suiteKey=ProgramCentral&header=emxProgramCentral.WeeklyTimeSheet.PMCLaborReportInHrsTable.Heading&HelpMarker=emxhelpresourceplanlist";
		 	  contentURL = "../common/emxIndentedTable.jsp?program=emxWeeklyTimeSheet:getMembershipChildLaborReport&expandProgramMenu=PMCWTSLaborReportMenu&table=PMCLaborReport&toolbar=PMCWeeklyTimeSheetReportViewFilterToolbarMenu&freezePane=Name&selection=multiple&suiteKey=ProgramCentral&header=emxProgramCentral.WeeklyTimeSheet.PMCLaborReportInHrsTable.Heading&HelpMarker=emxhelpresourceplanlist";
		 	  // add these parameters to each content URL, and any others the App needs
		 	  contentURL += "&subHeader=" + subHeader;
		 	  contentURL += "&YearType=" + YearType + "&selYear=" + selYear + "&selTimeline=" + selTimeline;
		 	  contentURL += "&emxTableRowId=" +emxTableRowId+ "&passFromJsp=" + passFromJsp+"&PMCWTSReportingYearFilter="+selYear+"&PMCWTSViewFilter="+selTimeline;
		 	  contentURL += "&customize=true&objectCompare=false&showPageURLIcon=false&multiColumnSort=false";
		 %>
		 	<script language="javascript">
		 	var strUrl ="<%=contentURL%>";
			strUrl = encodeURI(strUrl);
		 	getTopWindow().location.href = strUrl;
			</script>
		 <%
        }else if("AddToFolder".equalsIgnoreCase(strMode)){

        	String[] emxTableRowId = emxGetParameterValues(request,"emxTableRowId");
        	String language = request.getHeader("Accept-Language");
        	String selectedItem = "";

            StringList slDocTokenIds= null;
            String docId = null;
            String[] arrParsedDocObjIds = new String[emxTableRowId.length];
            String[] objIds = new String[emxTableRowId.length];
            
            com.matrixone.apps.common.WorkspaceVault workspaceVault =
		  			(com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT);
            workspaceVault.setContentRelationshipType(workspaceVault.RELATIONSHIP_VAULTED_OBJECTS_REV2);
            //get allowed types for folder
  		    StringList allowedTypes = workspaceVault.getContentBusinessTypes(
  		                                              context,
  		                                              true);    //includeSubtypes

            for(int i=0;i<emxTableRowId.length;i++)
            {
               	String sTableRowId = emxTableRowId[i];
                Map mParsedObjects = ProgramCentralUtil.parseTableRowId(context,sTableRowId);
                String sRowId = (String) mParsedObjects.get("rowId");
                String sObjectId = (String) mParsedObjects.get("objectId");
                objIds[i] = sObjectId;

             	// show warning message message for Root selection
                if("0".equalsIgnoreCase(sRowId))
                {
                    String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                    		"emxProgramCentral.Folders.AddToSelect.doNotSelectRootObject",language);
                    %>
 					<script language="JavaScript" type="text/javascript">
                    	alert("<%=sErrMsg%>");
                    	window.closeWindow();
                    </script>
                    <%
                   return;
            	}

                //removed code
             }

            StringList busSelect = new StringList(32);
            busSelect.addElement(ProgramCentralConstants.SELECT_TYPE);   
            busSelect.addElement(ProgramCentralConstants.SELECT_ID);  
            busSelect.addElement(DomainConstants.SELECT_KINDOF_WORKSPACE_VAULT);  
            BusinessObjectWithSelectList objectWithSelectList = ProgramCentralUtil.getObjectWithSelectList(context, objIds, busSelect);

            for(int i=0, size = emxTableRowId.length;i<size;i++){
                BusinessObjectWithSelect bws = objectWithSelectList.getElement(i);
                String type          = bws.getSelectData(ProgramCentralConstants.SELECT_TYPE);
                String strId          = bws.getSelectData(ProgramCentralConstants.SELECT_ID);
                String isWorkspace          = bws.getSelectData(DomainConstants.SELECT_KINDOF_WORKSPACE_VAULT);


                if(!allowedTypes.contains(type) || "TRUE".equalsIgnoreCase(isWorkspace))
                {
                    String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
              			  "emxProgramCentral.Common.TypeNotAllowed", language);
                    %>
                    <script language="JavaScript">
                    alert("<%=XSSUtil.encodeForJavaScript(context,sErrMsg)%>");
                     window.closeWindow();
                    </script>
                    <%
                    return;
                }
                if(ProgramCentralUtil.isNotNullString(strId)){
                       arrParsedDocObjIds[i] = strId;
                }
                
                
             }

             MapList mlBusData = PMCWorkspaceVault.getPMCFolderData(context,arrParsedDocObjIds);

             for(Iterator busDataIterator = mlBusData.iterator(); busDataIterator.hasNext();)
             {
             	   Map<String,String> mDocData = (Map)busDataIterator.next();

                 String sDocObjId = mDocData.get(DomainConstants.SELECT_ID);
                 String sIsVersionedObjType = mDocData.get(com.matrixone.apps.common.CommonDocument.SELECT_IS_VERSION_OBJECT);

                 // show warning message message
                 if("true".equalsIgnoreCase(sIsVersionedObjType)) {

                     String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
               			  "emxProgramCentral.Folders.AddToSelect.selectOnlyDocument", language);
			         	%>
				           <script language="JavaScript" type="text/javascript">
				            alert("<%=sErrMsg%>");

				               if(getTopWindow().closeSlideInDialog) {

				                getTopWindow().closeSlideInDialog();
				            }
				               else {
				                window.closeWindow();
				            }
				           </script>
						<%
          			return;
        		}

                 selectedItem+=sDocObjId + ",";
			}

    		String objectId = emxGetParameter(request, "objectId");
    		String folderId = "";
    		if(emxTableRowId.length <= 1){
    			Map rowInfo = ProgramCentralUtil.parseTableRowId(context,emxTableRowId[0]);
    			folderId = (String) rowInfo.get("parentOId");

        }

		 	String strURL = "../common/emxIndentedTable.jsp?table=PMCAddToFolder&program=emxProjectFolder:getProjectFolderList&suiteKey=ProgramCentral&header=emxProgramCentral.Common.SelectFolders&HelpMarker=emxhelpfolderselectdialog" +
		 					"&freezePane=Name&sortColumnName=Name&expandProgram=emxProjectFolder:getAddToFolderExpand&nameColumnHyperlink=false" +
		 					"&objectId=" + objectId + "&docIds=" + selectedItem + "&folderId=" + folderId +
		 					"&selection=single&submitURL=../programcentral/emxProgramCentralUtil.jsp?mode=AddToFolderPostProcess&Export=false&showRMB=false" +
		 					"&autoFilter=false&showClipboard=false&showPageURLIcon=false&rowGrouping=false&customize=false&displayView=details&findMxLink=false&multiColumnSort=false&expandLevelFilter=true&submitLabel=emxFramework.Button.Submit&cancelLabel=emxProgramCentral.Button.Cancel";

			%>
			<script language="javascript">
					 var strUrl = "<%=strURL%>";
					 document.location.href = strUrl;
			</script>
			<%
        }else if("AddToFolderPostProcess".equalsIgnoreCase(strMode)){

			com.matrixone.apps.common.WorkspaceVault workspaceVault =
			  			(com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT);

			com.matrixone.apps.common.Document document =
			  		(com.matrixone.apps.common.Document) DomainObject.newInstance(context, DomainConstants.TYPE_DOCUMENT);

			String selectedIds = emxGetParameter(request, "docIds");
			String selectedNodeId = emxGetParameter(request, "emxTableRowId");
        	String folderId = (String)(ProgramCentralUtil.parseTableRowId(context,selectedNodeId)).get("objectId");
			//String folderId    = emxGetParameter(request, "radio");

			String sErrMsg= "";
  		  	StringList idList = null;

  		    ContextUtil.startTransaction(context, true);
  		    try
	  	    {
	  		    if (selectedIds.endsWith(","))
	  		    {
	  		      //trim trailing pipe
	  		      selectedIds = selectedIds.substring(0, selectedIds.length()-1);
	  		    }

	  		    idList = FrameworkUtil.split(selectedIds, ",");
	  		    String[] docIds = (String []) idList.toArray(new String[] {});

	  		    StringList objectSelects = new StringList();
	  		    objectSelects.add(workspaceVault.SELECT_ID);

	  		    workspaceVault.setId(folderId);
	  		    workspaceVault.setContentRelationshipType(workspaceVault.RELATIONSHIP_VAULTED_OBJECTS_REV2);

	  		    MapList items = workspaceVault.getItems(context,
	  		                                            objectSelects,
	  		                                            null,   // relationshipSelects
	  		                                            null,   // objectWhere
	  		                                            null);  // relationshipWhere

	  		    //convert maplist to stringlist of ids
	  		    StringList folderItemIds = new StringList();
	  		    Iterator itr = items.iterator();
	  		    while (itr.hasNext())
	  		    {
	  		      Map map = (Map) itr.next();
	  		      String id = (String) map.get(workspaceVault.SELECT_ID);
	  		      folderItemIds.add(id);
	  		    }

	  		    //get allowed types for folder
	  		    StringList allowedTypes = workspaceVault.getContentBusinessTypes(
	  		                                              context,
	  		                                              true);    //includeSubtypes

	  		    //get information about each document
	  		    objectSelects.add("current.access[toconnect]");
	  		    objectSelects.add(document.SELECT_TYPE);
	  		    objectSelects.add(document.SELECT_NAME);
	  		    objectSelects.add(document.SELECT_REVISION);
	  		    objectSelects.add(document.SELECT_TITLE);

	  		    MapList selectedItems = DomainObject.getInfo(context, docIds, objectSelects);

	  		    String language = request.getHeader("Accept-Language");

	  		    StringList connectList = new StringList();
	  		    itr = selectedItems.iterator();
	  		    while (itr.hasNext())
	  		    {
	  		      Map map = (Map) itr.next();
	  		      String access = (String) map.get("current.access[toconnect]");
	  		      String type = (String) map.get(document.SELECT_TYPE);
	  		      String docId = (String) map.get(document.SELECT_ID);

	  		      if (!"True".equalsIgnoreCase(access))
	  		      {
	  		      	sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.Common.NoConnectAccess",language);
	  		      	%>
		  			<script language="JavaScript" type="text/javascript">
						alert('<%=sErrMsg%>');
		  			</script>
		  			<%
	  		      }
	  		      else if (allowedTypes.indexOf(type) == -1)
	  		      {
	  		      	sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.Common.TypeNotAllowed",language);
	  		     	 %>
		  			<script language="JavaScript" type="text/javascript">
						alert('<%=sErrMsg%>');
		  			</script>
		  			<%
	  		      }
	  		      else if (folderItemIds.indexOf(docId) != -1)
	  		      {
	  		      	sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.Common.ConnectAlready",language);
	  		      	%>
		  			<script language="JavaScript" type="text/javascript">
						alert('<%=sErrMsg%>');
		  			</script>
		  			<%
	  		      }
	  		      else
	  		      {
	  		        connectList.add(docId);
	  		      	sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.Common.AllProcessedSuccesfully",language);
	  		      	%>
		  			<script language="JavaScript" type="text/javascript">
						alert('<%=sErrMsg%>');
						parent.opener.location.href=parent.opener.location.href;
						window.closeWindow();
		  			</script>
		  			<%
	  		      }
	  		    }

	  		    if (connectList.size() > 0)
	  		    {
	  		      String[] relatedIds = (String []) connectList.toArray(new String[] {});
	  		      RelationshipType rel = new RelationshipType(workspaceVault.RELATIONSHIP_VAULTED_OBJECTS_REV2);
	  		      Map results = DomainRelationship.connect(
	  		                                  context,           //Context
	  		                                  workspaceVault,    //DomainObject
	  		                                  rel,               //RelationshipType
	  		                                  true,              //isFrom
	  		                                  relatedIds,        //relatedIds
	  		                                  true);             //ignoreConnectErrors

	  		      java.util.Set keys = results.keySet();
	  		      itr = keys.iterator();
	  		      Map errors = new HashMap();
	  		      while (itr.hasNext())
	  		      {
	  		        String relatedId = (String) itr.next();
	  		        Object result = results.get(relatedId);
	  		        if (result instanceof Exception)
	  		        {
	  		          errors.put(relatedId, ((Exception) result).getMessage());
	  		        }
	  		      }
	  		    }

	  		    ContextUtil.commitTransaction(context);

	  		  }
	  		  catch (Exception e)
	  		  {
	  		    ContextUtil.abortTransaction(context);
	  		    throw e;
	  		  }

  			 %>
             <script language="javascript" type="text/javaScript">
                 //parent.document.location.href=parent.document.location.href;
             </script>
             <%
       }else if("DependencyDeleteProcess".equalsIgnoreCase(strMode)){

		Task task = (Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);
		DependencyRelationship dependency = (DependencyRelationship) DomainRelationship.newInstance(context, DomainConstants.RELATIONSHIP_DEPENDENCY);
		String tasks[] = emxGetParameterValues(request, "selectedIds");
		String busId = (String) emxGetParameter(request, "busId");
		String popup = (String) emxGetParameter(request, "popup");
		String fromPage = (String) emxGetParameter(request, "fromPage");
		String refreshMode = (String) emxGetParameter(request, "refreshMode");
		String rootNodeObjectId = (String) session.getAttribute("rootObjectId");
		boolean blStructureBrowser = false;
		String prjId = DomainConstants.EMPTY_STRING;
		String[] connectionIds=null;
		if (fromPage != null && "StructureBrowser".equalsIgnoreCase(fromPage)) {
			blStructureBrowser = true;
		}

		/*External Cross project Dependency*/
		//Added below code after converting depedency summary as emxTable.
		if (ProgramCentralUtil.isNullString(busId)) {
			busId = (String) emxGetParameter(request, "objectId");
		}
		if (tasks == null) {
			String[] emxTableRowIds = emxGetParameterValues(request, "emxTableRowId");
			if (emxTableRowIds != null) {
				int length = emxTableRowIds.length;
				tasks = new String[length];
				connectionIds = new String[length];
				for (int i = 0; i < length; i++) {
					StringList slTemp = FrameworkUtil
							.split(emxTableRowIds[i].substring((emxTableRowIds[i].indexOf("|")) + 1), "|");
					StringList slTemp1 = FrameworkUtil.split(emxTableRowIds[i],"|");
					tasks[i] = (String) slTemp.get(0);
					connectionIds[i]=slTemp1.get(0);
				}
			} //end of for loop

		}
		//set the Id of the selected task and obtain its parent
		task.setId(busId);
					
		ProjectSpace proj = task.getProject(context);
		String projID = proj.getId();
					
		boolean isStartDateInvalid = false;
		/*  Identifies the constraint value ASAP per task/project*/
		final String CONSTRAINT_AS_SOON_AS_POSSIBLE = "Project Start Date";
		/*  Identifies the constraint value ALAP per task/project*/
		final String CONSTRAINT_AS_LATE_AS_POSSIBLE = "Project Finish Date";

		ProjectSpace project = (ProjectSpace) DomainObject.newInstance(context,
				DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
		project.setId(projID);

		StringList selectLists = new StringList(3);
		selectLists.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
		selectLists.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
		selectLists.add(ProgramCentralConstants.SELECT_ATTRIBUTE_SCHEDULED_FROM);
		Map mpProj = project.getInfo(context, selectLists);
		String strProjStartDate = (String) mpProj.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
		String strProjEndDate = (String) mpProj.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
		String strProjScheduledFrom = (String) mpProj.get(ProgramCentralConstants.SELECT_ATTRIBUTE_SCHEDULED_FROM);
		Date dtProjStartDate = eMatrixDateFormat.getJavaDate(strProjStartDate);
		Date dtProjEndDate = eMatrixDateFormat.getJavaDate(strProjEndDate);

		StringList busSelects = new StringList(3);
		StringList relSelects = new StringList(1);
		busSelects.add(task.SELECT_ID);
		relSelects.add(dependency.SELECT_DEPENDENCY_TYPE);

		if (tasks != null) {
			// get the number of tasks
			int numTasks = tasks.length;
			for (int i = 0; numTasks > i; i++) {
				MapList predecessorList = task.getPredecessors(context, busSelects, relSelects, null);
				Iterator predecessorItr = predecessorList.iterator();
				try {
					// start a write transaction and lock business object
					task.startTransaction(context, true);

					while (predecessorItr.hasNext()) {
						Map predecessorObj = (Map) predecessorItr.next();
						String predecessorId = (String) predecessorObj.get(task.SELECT_ID);
						String connectionId = (String) predecessorObj.get(dependency.SELECT_ID);
						if (predecessorId.equals(tasks[i]) && connectionId.equals(connectionIds[i])) {
							task.removePredecessor(context, connectionId);
							if (CONSTRAINT_AS_LATE_AS_POSSIBLE.equals(strProjScheduledFrom)) {
								Task tempTask = new Task();
								tempTask.setId(predecessorId);
								tempTask.setAttributeValue(context,DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE, strProjEndDate);
								(new com.matrixone.apps.common.TaskDateRollup(predecessorId)).validateTask(context);
							}
							break;
						}
					}
					// Set the immediate parent est start date once the dependency is removed

					if (CONSTRAINT_AS_SOON_AS_POSSIBLE.equals(strProjScheduledFrom)) {
						task.setAttributeValue(context, DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE,strProjStartDate);
						(new com.matrixone.apps.common.TaskDateRollup(busId)).validateTask(context);
					}
					ContextUtil.commitTransaction(context);
				} catch (Exception e) {
					ContextUtil.abortTransaction(context);
					throw e;
				}
			}
		}
%>
<script language="javascript">
if(<%=popup%> == true) {
    //parent.window.getWindowOpener().parent.document.focus();
    parent.window.getWindowOpener().parent.document.location.reload();
    parent.window.closeWindow();
  } else if(<%=blStructureBrowser%> == true) {
		//parent.window.getWindowOpener().parent.document.location.href = parent.window.getWindowOpener().parent.document.location.href;
		parent.window.getWindowOpener().parent.emxEditableTable.refreshStructure();
		parent.window.closeWindow();
	}else if("<%=refreshMode%>" == "refreshWBS"){
	 var topFrame = findFrame(getTopWindow().getWindowOpener().getTopWindow(), "PMCWBS");
     if(topFrame==null)  {
		topFrame=findFrame(getTopWindow().getWindowOpener().getTopWindow(), "PMCProjectTemplateWBS");
		if(topFrame==null){
			topFrame = findFrame(getTopWindow().getWindowOpener().getTopWindow(), "detailsDisplay");
       }
	  }
    topFrame.emxEditableTable.refreshStructure();
    var strTaskId = '<%=busId%>';
    var projectId = '<%=rootNodeObjectId%>';
    var strUrl = "../common/emxPortal.jsp?portal=PMCWBSTaskDependencyPortal&showPageHeader=false;&objectId=" + strTaskId + "&projectId=" + projectId;
    getTopWindow().window.location.href = strUrl;
}else{
	  parent.document.location.href = parent.document.location.href;
}
</script>
<%
	}
%>


<%@include file = "../emxUICommonEndOfPageInclude.inc" %>
<%@include file = "../components/emxComponentsDesignBottomInclude.inc"%>
