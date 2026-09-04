<%--  ECMFullSearchPreProcess.jsp

	Copyright (c) 1992-2020 Dassault Systemes.
	All Rights Reserved.
	This program contains proprietary and trade secret information of MatrixOne, Inc.
	Copyright notice is precautionary only and does not evidence any actual or intended publication of such program
	
	ECMFullSearchPreProcess.jsp which gets the search types from the DB, and forms the search url, used in all addexisting operations in ECM.
--%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "ECMDesignTopInclude.inc"%>				
<%@page import="com.dassault_systemes.enovia.enterprisechange.modeler.ChangeCommon" %>

<jsp:useBean id="changeUtil" class="com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil" scope="session"/>
<jsp:useBean id="changeUXUtil" class="com.dassault_systemes.enovia.enterprisechangemgtapp.common.ChangeUXUtil" scope="session"/>

<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<%
	final String CO_PORTAL_CMD = "ECMMyChangeOrders";
	String functionality    = emxGetParameter(request,"functionality");
	String objectId         = emxGetParameter(request, "objectId");
	String isFrom			= emxGetParameter(request, "isFrom");
	String targetRelName	= emxGetParameter(request, "targetRelName");
	String suiteKey         = emxGetParameter(request, "suiteKey");
	String languageStr      = context.getSession().getLanguage();
	String emxTableRowIds[] = emxGetParameterValues(request, "emxTableRowId");
	String[] strTableRowIds = emxGetParameterValues(request, "emxTableRowId");
	String currentCriteria  = emxGetParameter(request, "CURRENT");
	String portalCmdName = emxGetParameter(request, "portalCmdName");
	String frameName = emxGetParameter(request, "frameName");
	currentCriteria  		= !changeUtil.isNullOrEmpty(currentCriteria) ? currentCriteria : ""; 

	boolean fullSearch = (UIUtil.isNotNullAndNotEmpty((String)emxGetParameter(request, "isFullSearch")));
	String excludeTypes        = emxGetParameter(request, "excludeTypes");
	StringList excludeTypeList = !changeUtil.isNullOrEmpty(excludeTypes) ? FrameworkUtil.splitString(excludeTypes,",") : new StringList();
	String proposedChangeAllowedTypes = "";
	String searchTypes 	       = "";
	String searchForbiddenTypes= "";
	String searchURL 		   = "../common/emxFullSearch.jsp?showInitialResults=false&HelpMarker=emxhelpfullsearch&objectId="+objectId+"&targetRelName="+targetRelName+"&isFrom="+isFrom+"&currentCriteria="+currentCriteria+"&functionality="+functionality+"&suiteKey="+suiteKey+"&";
	String searchProject = "";	
	
	String excludeOIDprogram   = emxGetParameter(request, "excludeOIDprogram");
	excludeOIDprogram		   = !changeUtil.isNullOrEmpty(excludeOIDprogram) ? excludeOIDprogram : ""; 
	String targetRelActualName = !changeUtil.isNullOrEmpty(targetRelName) ? PropertyUtil.getSchemaProperty(context,targetRelName) : ""; 

	
	StringList objSelects = new StringList(2);
	Map orgMap = null;
	String orgNameSelect   = ChangeConstants.SELECT_ORGANIZATION;
	String orgIdSelect     = ChangeConstants.SELECT_ORGANIZATION+".id";
	String errorMessage  = "";
	String errorMessageProjectCheck  = "";
	long timeinMilli = System.currentTimeMillis();
	Locale locale    = context.getLocale();
	String itemConnectedToChangeAlready = EnoviaResourceBundle.getProperty(context,ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, locale,"EnterpriseChangeMgt.Warning.ContextItemAlreadyConnectedWarning");
	String portalFrame = emxGetParameter(request, "portalFrame");
	portalFrame 	= (UIUtil.isNullOrEmpty(portalFrame) || "undefined".equalsIgnoreCase(portalFrame)) ? "listHidden" : portalFrame;
	
	String strProposedActivityList="";
	strProposedActivityList=changeUtil.getListOfAllowedTypesForGivenCategory(context, ChangeConstants.PROPOSEDACTIVITY);
	strProposedActivityList =strProposedActivityList.replace(", ", ",");

	if(!changeUtil.isNullOrEmpty(objectId)) {
	searchProject = DomainObject.newInstance(context,objectId).getInfo(context, ChangeConstants.SELECT_PROJECT);
	} else {
            Map ObjMap = changeUXUtil.getObjectIdsRelIdsMapFromTableRowID((String[])emxTableRowIds);
			StringList AffeObjList = (StringList)ObjMap.get("ObjId");
			String strLastObjectProject = "";
			String strCurrentObjectProject = "";
			for(int index = 0; index < AffeObjList.size(); index++)
     		{
				String strCurrentObjectId = (String) AffeObjList.get(index);
				strCurrentObjectProject = DomainObject.newInstance(context,strCurrentObjectId).getInfo(context, ChangeConstants.SELECT_PROJECT);
				if(index != 0) {
					if(!strCurrentObjectProject.equalsIgnoreCase(strLastObjectProject)){
						errorMessageProjectCheck = EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR,locale,"EnterpriseChangeMgt.Alert.InvalidObjectSelectInProjectMsg");
						break;
					}					
				}
				strLastObjectProject = strCurrentObjectProject;				
     		}
			searchProject = strLastObjectProject;
	}

	try{
	    ContextUtil.startTransaction(context,true);
	    //For IR-430176
	    if("AddToNewChange".equalsIgnoreCase(functionality)||
	    		"AddToNewChangeRequest".equalsIgnoreCase(functionality)||
	    		"AddToNewChangeAction".equalsIgnoreCase(functionality)||
	    		"AddToExistingChange".equalsIgnoreCase(functionality)||
	    		"AddToExistingChangeRequest".equalsIgnoreCase(functionality)||
	    		"AddToExistingChangeAction".equalsIgnoreCase(functionality)){
	    	StringList affeObjList = changeUXUtil.getObjectIdsFromTableRowID((String[])emxTableRowIds);
	    	Map mapControlledItems = ChangeUtil.checkForChangeControlledItems(context, affeObjList);
	    	String strInvalidAffectedItems = EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR,locale,"EnterpriseChangeMgt.Message.InvalidRelatedItem");
	    	errorMessage = mapControlledItems.get("ErrorMsg").toString();
	    	if(!errorMessage.isEmpty()){
	    		errorMessage = strInvalidAffectedItems+":"+errorMessage;
	    	}
	    }
		if ("AddExisting".equalsIgnoreCase(functionality)) 
		{						
			boolean isSerachForToSide = "false".equalsIgnoreCase(isFrom)? false : true;
			
			searchTypes  =  changeUtil.getRelationshipTypes(context,targetRelName,isSerachForToSide,false,excludeTypeList);
			searchTypes  =  !"".equals(currentCriteria) ? searchTypes+":CURRENT="+currentCriteria : searchTypes;
			
			excludeOIDprogram = !"".equals(excludeOIDprogram) ? excludeOIDprogram : "enoECMChangeUtil:excludeConnectedObjects";			
			searchURL        += "table=AEFGeneralSearchResults&selection=multiple&excludeOID="+objectId+"&showSavedQuery=True&excludeOIDprogram="+excludeOIDprogram+"&searchCollectionEnabled=True&formInclusionList=Description&field=TYPES="+searchTypes+"&submitURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp?submitAction=refreshCaller";		
		}
		if ("AddExistingResolvedItems".equalsIgnoreCase(functionality)) 
		{	
			searchTypes  =  ChangeConstants.TYPE_ISSUE;
			searchTypes  =  !"".equals(currentCriteria) ? searchTypes+":CURRENT="+currentCriteria : searchTypes;
			
			excludeOIDprogram = !"".equals(excludeOIDprogram) ? excludeOIDprogram : "enoECMChangeUtil:excludeConnectedObjects";			
			searchURL        += "table=AEFGeneralSearchResults&selection=multiple&excludeOID="+objectId+"&showSavedQuery=True&excludeOIDprogram="+excludeOIDprogram+"&searchCollectionEnabled=True&formInclusionList=Description&field=TYPES="+searchTypes+"&submitURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp?submitAction=refreshCaller";		
		}

		/**
		 * 		2024.7.25  yb
		 * 		"CAAffectedItemsAddExisting".equalsIgnoreCase(functionality)
		 * 	原始url
		 * 	searchURL	+= "&table=AEFGeneralSearchResults&selection=multiple&showSavedQuery=True&searchCollectionEnabled=True&field=TYPES="+searchTypes+"&submitURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp?submitAction=refreshCaller";
		 */
		else if("CAAffectedItemsAddExisting".equalsIgnoreCase(functionality))
		{
			//Modeler API to get the allowed seach type
			searchTypes=strProposedActivityList;
			searchURL	+= "&table=AEFGeneralSearchResults&selection=multiple&showSavedQuery=True&searchCollectionEnabled=True&field=TYPES=type_Document,type_Drawing:owner=" + context.getUser() + ":CURRENT=policy_VPLM_SMB_Definition_MajorRev.state_InWork,policy_VPLM_SMB_Definition_MajorRev.state_Review&submitURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp?submitAction=refreshCaller";
		}

		else if("AffectedItemsAddExisting".equalsIgnoreCase(functionality)) 
		{
			//Modeler API to get the allowed seach type
			searchTypes=strProposedActivityList;
			searchURL	+= "&table=AEFGeneralSearchResults&selection=multiple&showSavedQuery=True&searchCollectionEnabled=True&field=TYPES="+searchTypes+"&submitURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp?submitAction=refreshCaller";
		}
		else if("AffectedItemsAddExistingForCR".equalsIgnoreCase(functionality)) 
		{							
			//Modeler API to get the allowed seach type
			searchTypes=strProposedActivityList;
			searchURL	+= "&table=AEFGeneralSearchResults&selection=multiple&showSavedQuery=True&searchCollectionEnabled=True&field=TYPES="+searchTypes+"&submitURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp?submitAction=refreshCaller";
		}
		else if("MoveToExistingCO".equalsIgnoreCase(functionality)) 
		{	
			ChangeOrder changeOrder = new ChangeOrder(objectId);
			String crId =  changeOrder.getInfo(context, "to["+ChangeConstants.RELATIONSHIP_CHANGE_ORDER+"].from.id");
			session.setAttribute("sourceAffectedItemRowIds",emxTableRowIds);
			if(UIUtil.isNullOrEmpty(crId)){
				searchTypes    = "type_ChangeOrder:CURRENT=policy_FormalChange.state_Propose,policy_FormalChange.state_Prepare,policy_FasttrackChange.state_Prepare";
				//:Organization=" + changeOrder.getInfo(context, orgNameSelect);
				searchURL	  += "&table=AEFGeneralSearchResults&hideHeader=true&selection=single&showSavedQuery=True&excludeOID="+objectId+"&HelpMarker=emxhelpfullsearch&searchCollectionEnabled=True&field=TYPES="+searchTypes+"&submitURL=../enterprisechangemgtapp/ECMDisconnectProcess.jsp?submitAction=refreshCaller";
			} else{
				searchTypes    = "type_ChangeOrder:CURRENT=policy_FormalChange.state_Propose,policy_FormalChange.state_Prepare,policy_FasttrackChange.state_Prepare";
				// This selectable is not correct
				//:Organization=" + changeOrder.getInfo(context, orgNameSelect);
				searchURL	  += "&table=AEFGeneralSearchResults&includeOIDprogram=enoECMChangeRequest:includeCOOIDs&excludeOID="+objectId+"&hideHeader=true&selection=single&showSavedQuery=True&HelpMarker=emxhelpfullsearch&searchCollectionEnabled=True&field=TYPES="+searchTypes+"&submitURL=../enterprisechangemgtapp/ECMDisconnectProcess.jsp?submitAction=refreshCaller";
			}
		}
		else if("MoveToNewCO".equalsIgnoreCase(functionality)) 
		{				
			ChangeOrder changeOrder = new ChangeOrder(objectId);
			String crId =  changeOrder.getInfo(context, "to["+ChangeConstants.RELATIONSHIP_CHANGE_ORDER+"].from.id");
			objSelects.addElement(orgNameSelect);
			orgMap = changeOrder.getInfo(context,objSelects);
			orgMap.put(orgIdSelect,ChangeUtil.getRtoIdFromName(context, (String)orgMap.get(orgNameSelect)));
			session.setAttribute("sourceAffectedItemRowIds",emxTableRowIds);
			searchURL  = "../common/emxCreate.jsp?form=type_CreateChangeOrderSlidein&type=type_ChangeOrder&CreateMode=CreateCO&typeChooser=true&header=EnterpriseChangeMgt.Command.CreateChange&nameField=autoname&createJPO=enoECMChangeOrderUX:createChange&submitAction=refreshCaller&preProcessJavaScript=preProcessInCreateCO&objectId="+objectId+"&functionality="+functionality+"&suiteKey="+suiteKey+"&postProcessURL=../enterprisechangemgtapp/ECMDisconnectProcess.jsp&HelpMarker=emxhelpchangeordercreate&objectId="+objectId+"&mode=create&SuiteDirectory=enterprisechangemgt";
			if(UIUtil.isNotNullAndNotEmpty(crId)){
				searchURL+="&isconnectedtoCR=true";
			}
		}
		else if("MoveToExistingCR".equalsIgnoreCase(functionality)) 
		{	
			session.setAttribute("sourceAffectedItemRowIds",emxTableRowIds);
			searchTypes    = "type_ChangeRequest:CURRENT=policy_ChangeRequest.state_Create,policy_ChangeRequest.state_Evaluate";
			// This needs to be fixed using correct selectable
			//:Organization=" + DomainObject.newInstance(context,objectId).getInfo(context, orgNameSelect);			
			searchURL	  += "&table=AEFGeneralSearchResults&hideHeader=true&excludeOID="+objectId+"&selection=single&showSavedQuery=True&HelpMarker=emxhelpfullsearch&searchCollectionEnabled=True&field=TYPES="+searchTypes+"&submitURL=../enterprisechangemgtapp/ECMDisconnectProcess.jsp?submitAction=refreshCaller";
		}
		else if("MoveToNewCR".equalsIgnoreCase(functionality)) 
		{				
			objSelects.addElement(orgNameSelect);
			orgMap     = DomainObject.newInstance(context,objectId).getInfo(context, objSelects);
			orgMap.put(orgIdSelect, ChangeUtil.getRtoIdFromName(context, (String)orgMap.get(orgNameSelect)));
			session.setAttribute("sourceAffectedItemRowIds",emxTableRowIds);
			searchURL  = "../common/emxCreate.jsp?form=type_CreateChangeRequest&type=type_ChangeRequest&typeChooser=true&header=EnterpriseChangeMgt.Command.CreateChangeRequest&nameField=autoname&createJPO=enoECMChangeRequest:createChangeRequest&submitAction=refreshCaller&preProcessJavaScript=setRO&suiteKey="+suiteKey+"&postProcessURL=../enterprisechangemgtapp/ECMDisconnectProcess.jsp&HelpMarker=emxhelpchangerequestcreate&functionality="+functionality+"&suiteKey="+suiteKey;
	
		}
		else if("AddExistingPrerequisiteCOs".equalsIgnoreCase(functionality)) 
		{	
			searchTypes  = "type_ChangeOrder:CURRENT=policy_FormalChange.state_Propose,policy_FormalChange.state_Prepare,policy_FormalChange.state_InReview,policy_FasttrackChange.state_Prepare";
			searchURL   += "table=AEFGeneralSearchResults&excludeOID="+objectId+"&excludeOIDprogram=enoECMChangeUtil:excludePrerequisites&selection=multiple&showSavedQuery=True&searchCollectionEnabled=True&formInclusionList=Description&field=TYPES="+searchTypes+"&submitURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp";
		}
		else if("AddExistingRelatedCAs".equalsIgnoreCase(functionality)) 
		{	
			searchTypes = "type_ChangeAction";
			ChangeAction caObj = new ChangeAction(objectId);
			String coObjId = caObj.getInfo(context, "to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.id");
			if(!UIUtil.isNullOrEmpty(coObjId)) {
				searchTypes += ":CONNECTED_CO="+coObjId;
			}
			searchURL   += "table=AEFGeneralSearchResults&excludeOID="+objectId+"&excludeOIDprogram=enoECMChangeUtil:excludePrerequisites&selection=multiple&showSavedQuery=True&searchCollectionEnabled=True&formInclusionList=Description&field=TYPES="+searchTypes+"&submitURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp?submitAction=refreshCaller";
		}
		else if("MoveToExistingCA".equalsIgnoreCase(functionality)) 
		{	
			session.setAttribute("sourceAffectedItemRowIds",emxTableRowIds);
            Map mapObjIdRelId       = changeUXUtil.getObjRelRowIdsMapFromTableRowID(emxTableRowIds);
			StringList affectedRels = (StringList)mapObjIdRelId.get("RelId");								
			MapList objectList    =  DomainRelationship.getInfo(context, (String[])affectedRels.toArray(new String[affectedRels.size()]), new StringList(ChangeConstants.SELECT_FROM_ID));
			StringList caIdsList = changeUtil.getStringListFromMapList(objectList,ChangeConstants.SELECT_FROM_ID); 	
			
			ChangeOrder chOrder=new ChangeOrder(objectId);
			chOrder.checkForMoveToExistingCA(context, caIdsList);
			searchTypes    = "type_ChangeAction:CURRENT=policy_ChangeAction.state_Prepare";
			searchURL	  += "&table=AEFGeneralSearchResults&hideHeader=true&excludeOID="+ FrameworkUtil.join(caIdsList, ",") +"&selection=single&showSavedQuery=True&HelpMarker=emxhelpfullsearch&searchCollectionEnabled=True&field=TYPES="+searchTypes+"&submitURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp?submitAction=refreshCaller";
		}
		else if ("AddToExistingChange".equalsIgnoreCase(functionality) || "ECMAddToExistingCO".equalsIgnoreCase(functionality)) 
		{							
			session.setAttribute("sourceAffectedItemRowIds",emxTableRowIds);
			searchTypes  = "type_ChangeOrder:CURRENT=policy_FormalChange.state_Propose,policy_FormalChange.state_Prepare,policy_FasttrackChange.state_Prepare";
			searchURL   += "field=TYPES="+searchTypes+"&table=AEFGeneralSearchResults&selection=single&showSavedQuery=True&searchCollectionEnabled=True&functionality="+functionality+"&objectId="+objectId+"&formInclusionList=Description&field=TYPES="+searchTypes+"&submitURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp?submitAction=doNothing";
			
		}
		else if ("AddToNewChange".equalsIgnoreCase(functionality) || "ECMAddToNewCO".equalsIgnoreCase(functionality)) 
		{
			session.setAttribute("sourceAffectedItemRowIds",emxTableRowIds);
			searchURL	 = "../common/emxCreate.jsp?form=type_CreateChangeOrderSlidein&header=EnterpriseChangeMgt.Command.CreateChange&type=type_ChangeOrder&nameField=autoname&createJPO=enoECMChangeOrderUX:createChange&CreateMode=CreateCO&typeChooser=true&submitAction=doNothing&suiteKey="+suiteKey+"&preProcessJavaScript=preProcessInCreateCO&postProcessURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp&SuiteDirectory=enterprisechangemgt&mode=create&functionality="+functionality+"&HelpMarker=emxhelpchangeordercreate&objectId="+objectId+"";
			
		}
		else if ("AddToExistingChangeAction".equalsIgnoreCase(functionality) || "AddToExistingCA".equalsIgnoreCase(functionality)) 
		{							
            //Map ObjMap = changeUXUtil.getObjectIdsRelIdsMapFromTableRowID((String[])emxTableRowIds);
			//StringList AffeObjList  = (StringList)ObjMap.get("ObjId");
			StringList AffeObjList = changeUXUtil.getObjectIdsFromTableRowID((String[])emxTableRowIds);
			session.setAttribute("sourceAffectedItemRowIds",AffeObjList);
			searchTypes  = "type_ChangeAction:CURRENT=policy_ChangeAction.state_Prepare,policy_ChangeAction.state_InWork:ECM_CHG_ON_HOLD!=TRUE";
			searchURL   += "field=TYPES="+searchTypes+"&table=AEFGeneralSearchResults&selection=single&showSavedQuery=True&searchCollectionEnabled=True&functionality="+functionality+"&objectId="+objectId+"&formInclusionList=Description&field=TYPES="+searchTypes+"&submitURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp?submitAction=doNothing"+"&frameName="+frameName+"";
			
		}
		else if ("AddToNewChangeAction".equalsIgnoreCase(functionality) || "AddToNewCA".equalsIgnoreCase(functionality)) 
		{
            //Map ObjMap = changeUXUtil.getObjectIdsRelIdsMapFromTableRowID((String[])emxTableRowIds);
			//StringList AffeObjList  = (StringList)ObjMap.get("ObjId");
			StringList AffeObjList = changeUXUtil.getObjectIdsFromTableRowID((String[])emxTableRowIds);
			session.setAttribute("sourceAffectedItemRowIds",AffeObjList);
			searchURL	 = "../common/emxCreate.jsp?form=type_CreateChangeActionSlidein&functionality="+functionality+"&header=EnterpriseChangeMgt.Command.CreateChangeAction&type=type_ChangeAction&nameField=autoname&createJPO=enoECMChangeActionUX:createChangeAction&CreateMode=CreateCA&submitAction=doNothing&suiteKey="+suiteKey+"&postProcessURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp&SuiteDirectory=enterprisechangemgt&mode=create&functionality="+functionality+"&HelpMarker=emxhelpchangeactioncreate&objectId="+objectId+"&frameName="+frameName+"";
			
		}
		else if ("AddToExistingChangeRequest".equalsIgnoreCase(functionality) || "ECMAddToExistingCR".equalsIgnoreCase(functionality))  
		{							
            Map ObjMap = changeUXUtil.getObjectIdsRelIdsMapFromTableRowID((String[])emxTableRowIds);
			StringList AffeObjList  = (StringList)ObjMap.get("ObjId");
			
			session.setAttribute("sourceAffectedItemRowIds",AffeObjList);
			//IR-861257-3DEXPERIENCER2022x
			//searchTypes  = "type_ChangeRequest:CURRENT=policy_ChangeRequest.state_Create,policy_ChangeRequest.state_Evaluate";
			searchTypes  = "type_ChangeRequest:CURRENT=policy_RequestForChange.state_Prepare,policy_RequestForChange.state_InWork";
			searchURL   += "field=TYPES="+searchTypes+"&table=AEFGeneralSearchResults&selection=single&showSavedQuery=True&searchCollectionEnabled=True&functionality="+functionality+"&objectId="+objectId+"&formInclusionList=Description&submitURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp?submitAction=doNothing"+"&frameName="+frameName;
						
		}
		else if ("AddToNewChangeRequest".equalsIgnoreCase(functionality) || "ECMAddToNewCR".equalsIgnoreCase(functionality))  
		{
            Map ObjMap = changeUXUtil.getObjectIdsRelIdsMapFromTableRowID((String[])emxTableRowIds);
			StringList AffeObjList = (StringList)ObjMap.get("ObjId");
			session.setAttribute("sourceAffectedItemRowIds",AffeObjList);
			//Modified for IR-863749-3DEXPERIENCER2022x
			//searchURL	 = "../common/emxCreate.jsp?form=type_CreateChangeRequest&header=EnterpriseChangeMgt.Command.CreateChangeRequest&type=type_ChangeRequest&nameField=autoname&createJPO=enoECMChangeRequest:createChangeRequest&typeChooser=true&submitAction=doNothing&suiteKey="+suiteKey+"&preProcessJavaScript=setRO&postProcessURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp&functionality="+functionality+"&HelpMarker=emxhelpchangerequestcreate&objectId="+objectId+"";
			searchURL	 = "../common/emxCreate.jsp?form=type_CreateChangeRequest&header=EnterpriseChangeMgt.Command.CreateChangeRequest&type=type_ChangeRequest&nameField=autoname&createJPO=enoECMChangeRequestUX:createChangeRequest&typeChooser=true&submitAction=doNothing&suiteKey="+suiteKey+"&preProcessJavaScript=setRO&postProcessURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp&mode=create&functionality="+functionality+"&HelpMarker=emxhelpchangerequestcreate&objectId="+objectId+"&frameName="+frameName+"";
			
		}
		else if("AddExistingCandidate".equalsIgnoreCase(functionality))
		{
			searchTypes =  strProposedActivityList;
			searchURL	+= "&table=AEFGeneralSearchResults&excludeOIDprogram=enoECMChangeOrder:excludeCandidateItems&selection=multiple&showSavedQuery=True&searchCollectionEnabled=True&field=TYPES="+searchTypes+"&submitURL=../enterprisechangemgtapp/ECMUtil.jsp?mode=AddExisting&submitAction=refreshCaller";		
		}
		else if("MassRelease".equalsIgnoreCase(functionality)) 
		{
			session.setAttribute("functionality",functionality);
			functionality=ChangeConstants.FOR_RELEASE;
            Map ObjMap = changeUXUtil.getObjectIdsRelIdsMapFromTableRowID((String[])emxTableRowIds);
			StringList AffeObjList = (StringList)ObjMap.get("ObjId");
			changeUtil.checkForMassReleaseOrObsolete(context,changeUtil.getTypeNamePolicyForObjList(context,AffeObjList),ChangeConstants.FOR_RELEASE);
			session.setAttribute("sourceAffectedItemRowIds",AffeObjList);
			searchURL  = "../common/emxCreate.jsp?form=type_CreateChangeOrderSlidein&header=EnterpriseChangeMgt.Command.MassRelease&type=type_ChangeOrder&policy=policy_FasttrackChange&nameField=autoname&createJPO=enoECMChangeOrderUX:createChange&submitAction=doNothing&preProcessJavaScript=preProcessInCreateCO&CreateMode=CreateCO&functionality="+functionality+"&suiteKey="+suiteKey+"&postProcessURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp&HelpMarker=emxhelpchangeordercreate&mode=create&SuiteDirectory=enterprisechangemgt";
		}
		else if("MassObsolete".equalsIgnoreCase(functionality)) 
		{
			session.setAttribute("functionality",functionality);
			functionality=ChangeConstants.FOR_OBSOLETE;
            Map ObjMap = changeUXUtil.getObjectIdsRelIdsMapFromTableRowID((String[])emxTableRowIds);
			StringList AffeObjList = (StringList)ObjMap.get("ObjId");
			changeUtil.checkForMassReleaseOrObsolete(context,changeUtil.getTypeNamePolicyForObjList(context,AffeObjList),ChangeConstants.FOR_OBSOLETE);
			MapList ObjInfoList = changeUtil.getTypeNamePolicyForObjList(context,AffeObjList);
			session.setAttribute("sourceAffectedItemRowIds",AffeObjList);
			searchURL  = "../common/emxCreate.jsp?form=type_CreateChangeOrderSlidein&header=EnterpriseChangeMgt.Command.MassObsolete&type=type_ChangeOrder&policy=policy_FasttrackChange&nameField=autoname&createJPO=enoECMChangeOrderUX:createChange&submitAction=doNothing&preProcessJavaScript=preProcessInCreateCO&CreateMode=CreateCO&functionality="+functionality+"&suiteKey="+suiteKey+"&postProcessURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp&HelpMarker=emxhelpchangeordercreate&mode=create&SuiteDirectory=enterprisechangemgt";
		}
		else if("CRSupportingDocAddExisting".equalsIgnoreCase(functionality)) 
		{
			session.setAttribute("functionality",functionality);
			searchTypes  = "type_Sketch,type_Markup";
			searchURL   += "&table=AEFGeneralSearchResults&selection=multiple&field=TYPES="+searchTypes+":IS_SUPPORTING_DOCUMENT_CONNECTED=FALSE&submitURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp?submitAction=refreshCaller&relName="+targetRelName+"";
		}
		else if("AddCAReferential".equalsIgnoreCase(functionality)) 
		{
			session.setAttribute("functionality",functionality);
			searchTypes=changeUtil.getListOfAllowedTypesForGivenCategory(context, ChangeConstants.REFERENTIAL);
			searchForbiddenTypes = changeUXUtil.getListOfForbiddenTypesForGivenCategory(context, ChangeConstants.REFERENTIAL);
			searchTypes=searchTypes.replace(", ", ",");
			searchForbiddenTypes=searchForbiddenTypes.replace(", ", ",");
			searchURL   += "&table=AEFGeneralSearchResults&selection=multiple&fieldQueryProgram=enoECMChangeUX:fieldQueryProgramForexcludeContextOID&tag=ECM_RELATED_USAGE&field=TYPES!="+searchForbiddenTypes+"&submitURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp?submitAction=refreshCaller&relName="+targetRelName+"";
		}
		else if ("CraeteNewCOUnderCR".equalsIgnoreCase(functionality)) 
		{
			session.setAttribute("sourceAffectedItemRowIds",emxTableRowIds);
			searchURL	 = "../common/emxCreate.jsp?form=type_CreateChangeOrderSlidein&header=EnterpriseChangeMgt.Command.CreateChange&type=type_ChangeOrder&nameField=autoname&createJPO=enoECMChangeOrderUX:createChange&CreateMode=CreateCO&typeChooser=true&submitAction=doNothing&suiteKey="+suiteKey+"&preProcessJavaScript=preProcessInCreateCO&postProcessURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp&SuiteDirectory=enterprisechangemgt&mode=create&functionality="+functionality+"&HelpMarker=emxhelpchangeordercreate&objectId="+objectId+"";
		}
		else if("DocAddExisting".equalsIgnoreCase(functionality))
		{
			session.setAttribute("functionality",functionality);
			searchTypes=changeUtil.getListOfAllowedTypesForGivenCategory(context, ChangeConstants.REFERENTIAL);
			searchForbiddenTypes = changeUXUtil.getListOfForbiddenTypesForGivenCategory(context, ChangeConstants.REFERENTIAL);
			searchTypes=searchTypes.replace(", ", ",");
			searchForbiddenTypes=searchForbiddenTypes.replace(", ", ",");
			searchURL   += "&table=AEFGeneralSearchResults&selection=multiple&fieldQueryProgram=enoECMChangeUX:fieldQueryProgramForexcludeContextOID&tag=REFERENTIAL_USAGE&field=TYPES!="+searchForbiddenTypes+"&submitURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp?submitAction=refreshCaller&relName="+targetRelName+"";
			
		}
		
		
		//This block is added for create the sibling/Child Change Order	
		else if ("CreateNewCOUnderCO".equalsIgnoreCase(functionality)) 
		{
			
             String strRowSelectSingleError = EnoviaResourceBundle.getProperty(context,ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, locale,"EnterpriseChangeMgt.Error.RowSelect.Single");
             String strParrentError = EnoviaResourceBundle.getProperty(context,ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, locale,"EnterpriseChangeMgt.Error.ParentBeyondInWork");
 
			 if(emxTableRowIds != null && emxTableRowIds.length > 1){
	              %>
	              <script language="javascript" type="text/javaScript">
	                    alert("<%=XSSUtil.encodeForJavaScript(context,strRowSelectSingleError)%>");                
	              </script>
	             <%       
	          }
			 else{
				 
				 //For Child Change Order creation
				 if(strTableRowIds!=null){
       			  StringTokenizer strTokenizer = new StringTokenizer(strTableRowIds[0] , "|"); 
                     if(strTableRowIds[0].indexOf("|") > 0){                       
                         strTokenizer.nextToken() ;
                         objectId = strTokenizer.nextToken() ;
                         if(strTokenizer.hasMoreTokens()){
                             strTokenizer.nextToken();                        
                         }
                     }
                     else{
                    	 objectId = strTokenizer.nextToken();
                     }
                     
				 }
				 if(!changeUtil.isNullOrEmpty(objectId))
				 {
					 String strParentOID = emxGetParameter(request, "parentOID");
					 String strUIContext = changeUtil.isNullOrEmpty(strParentOID)? "MyDesk" : "ChangeOrder" ;
					 String strCurrent = new DomainObject(objectId).getInfo(context, DomainObject.SELECT_CURRENT);
					 ChangeCommon changeObj = new ChangeCommon(objectId);
					 if((ChangeConstants.STATE_FORMALCHANGE_PROPOSE.equalsIgnoreCase(strCurrent)||
							 ChangeConstants.STATE_FORMALCHANGE_PREPARE.equalsIgnoreCase(strCurrent)||
							 ChangeConstants.STATE_FORMALCHANGE_INREVIEW.equalsIgnoreCase(strCurrent)||
							 ChangeConstants.STATE_FORMALCHANGE_INWORK.equalsIgnoreCase(strCurrent)) && 
							 (changeObj !=null && !changeObj.isOnHold(context))){
						 
                     %>
                     <body>   
                     <form name="ECMChangeOrderCreate" method="post">
                     <script language="Javascript">
                     var submitURL	 = "../common/emxCreate.jsp?form=type_CreateChangeOrderSlidein&header=EnterpriseChangeMgt.Command.CreateChange&type=type_ChangeOrder&nameField=autoname&createJPO=enoECMChangeOrderUX:createChange&CreateMode=CreateCO&typeChooser=true&submitAction=doNothing&suiteKey=<%=XSSUtil.encodeForURL(context,suiteKey)%>&preProcessJavaScript=preProcessInCreateCO&postProcessURL=../enterprisechangemgtapp/ECMFullSearchPostProcess.jsp&SuiteDirectory=enterprisechangemgt&mode=create&functionality=<%=XSSUtil.encodeForURL(context,functionality)%>&HelpMarker=emxhelpchangeordercreate&objectId=<%=XSSUtil.encodeForURL(context,objectId)%>&UIContext=<%=XSSUtil.encodeForURL(context,strUIContext)%>";
                     getTopWindow().showSlideInDialog(submitURL, "true");
                     </script>
                     </form>
                     </body>
                     <%  
					 }else{
						 
						 %>
			              <script language="javascript" type="text/javaScript">
			                    alert("<%=XSSUtil.encodeForJavaScript(context,strParrentError)%>");                
			              </script>
			             <% 
						 
					 }
                     
				 }    
       			 
				 
				 //Creating the Stand alone CO (CO not connected to other CO)
				  else{
       				  
       				 %>
                     <body>   
                     <form name="ECMChangeOrderCreate" method="post">
                     <script language="Javascript">
                     debugger;
                     var submitURL	 = "../common/emxCreate.jsp?form=type_CreateChangeOrderSlidein&header=EnterpriseChangeMgt.Command.CreateChange&type=type_ChangeOrder&nameField=autoname&createJPO=enoECMChangeOrderUX:createChange&CreateMode=CreateCO&typeChooser=true&submitAction=refreshCaller&suiteKey=<%=XSSUtil.encodeForURL(context,suiteKey)%>&preProcessJavaScript=preProcessInCreateCO&SuiteDirectory=enterprisechangemgt&mode=create&functionality=<%=XSSUtil.encodeForURL(context,functionality)%>&HelpMarker=emxhelpchangeordercreate";
                     <%
						if(portalCmdName!=null && portalCmdName.trim().equals(CO_PORTAL_CMD)){                     
                     %>
                     	getTopWindow().showSlideInDialog(submitURL, "true", '<%=CO_PORTAL_CMD%>');
                     <%
						}else{
                     %>
                     	getTopWindow().showSlideInDialog(submitURL, "true");
                     	<%
						}
                     %>
                     </script>
                     </form>
                     </body>
                     <%  
       				  
       			  }
 
 
			  
			 }
 
		}
		
		
		
		
		
		ContextUtil.commitTransaction(context);
	}
	catch (Exception ex) {
		errorMessage = ex.getMessage();
		ex.printStackTrace();
		ContextUtil.abortTransaction(context);
	}

%>

<html>
<head>
</head>
<body>	
<form name="ECMfullsearch" method="post">
<input type="hidden" name="field" value="<xss:encodeForHTMLAttribute><%= "TYPES="+searchTypes %></xss:encodeForHTMLAttribute>"/>
<input type="hidden" name="objectId" value="<xss:encodeForHTMLAttribute><%=objectId%></xss:encodeForHTMLAttribute>"/>
<input type="hidden" name="functionality" value="<xss:encodeForHTMLAttribute><%=functionality %></xss:encodeForHTMLAttribute>"/>
<input type="hidden" name="isFrom" value="<xss:encodeForHTMLAttribute><%= isFrom%></xss:encodeForHTMLAttribute>"/>
<input type="hidden" name="targetRelName" value="<xss:encodeForHTMLAttribute><%=targetRelName%></xss:encodeForHTMLAttribute>"/>
<%
if("MoveToNewCO".equalsIgnoreCase(functionality) || "MoveToNewCR".equalsIgnoreCase(functionality)) {
%>
<input type="hidden" name="ROOID" value="<xss:encodeForHTMLAttribute><%= (String)orgMap.get(orgIdSelect) %></xss:encodeForHTMLAttribute>"/>	
<input type="hidden" name="RODisplay" value="<xss:encodeForHTMLAttribute><%= (String)orgMap.get(orgNameSelect) %></xss:encodeForHTMLAttribute>"/>
<%
}
if (emxTableRowIds != null){
	%>
	<input type="hidden" name="selectedObjIdList" value="<xss:encodeForHTMLAttribute><%=com.matrixone.apps.domain.util.StringUtil.join(emxTableRowIds, "~")%></xss:encodeForHTMLAttribute>"/>
	<%
}

errorMessage = !changeUtil.isNullOrEmpty(errorMessageProjectCheck)? errorMessageProjectCheck : errorMessage;
%>
<script language="Javascript">
//XSSOK
var error 		= "<%=errorMessage%>";
var portalFrame = "<%=XSSUtil.encodeForJavaScript(context,portalFrame)%>";
//alert("Javascript..");

<%
if(!"".equals(errorMessage)){
%>
	alert(error);
<%} else if(ChangeConstants.FOR_RELEASE.equalsIgnoreCase(functionality)||
		ChangeConstants.FOR_OBSOLETE.equalsIgnoreCase(functionality) ||
		"AddToNewChangeRequest".equalsIgnoreCase(functionality) ||
		"AddToNewChange".equalsIgnoreCase(functionality) || 
		"ECMAddToNewCR".equalsIgnoreCase(functionality) || 
		"ECMAddToNewCO".equalsIgnoreCase(functionality) || 
		"AddToNewChangeAction".equalsIgnoreCase(functionality) || 
		"AddToNewCA".equalsIgnoreCase(functionality) ||
		"CraeteNewCOUnderCR".equalsIgnoreCase(functionality)||
		"MoveToNewCO".equalsIgnoreCase(functionality) || 
		"MoveToNewCR".equalsIgnoreCase(functionality)){
%>
		
document.ECMfullsearch.action="<%=XSSUtil.encodeForJavaScript(context,searchURL)%>";
<%
if(!fullSearch){
	if("MoveToNewCO".equalsIgnoreCase(functionality) || "MoveToNewCR".equalsIgnoreCase(functionality))
	{
%>
		var currentFrame = findFrame(getTopWindow(),"ECMCRCOAffectedItems").frameElement.name;
<%
	}
	else
	{
%>	
	var currentFrame = this.parent.frameElement.name;
<%
	}
%>
	getTopWindow().showSlideInDialog("../common/emxAEFSubmitSlideInAction.jsp?portalFrame="+portalFrame+"&parentFrame="+currentFrame+"&url=" + encodeURIComponent(document.ECMfullsearch.action + "&targetLocation=slidein"), "true");
<%
}else{
%>	
	showModalDialog("../common/emxAEFSubmitPopupAction.jsp?portalFrame="+portalFrame+"&url=" + encodeURIComponent(document.ECMfullsearch.action + "&targetLocation=popup"), "600", "500");
<%
	}
} else if("AddToExistingChangeRequest".equalsIgnoreCase(functionality) || "AddToExistingChange".equalsIgnoreCase(functionality) || "AddToExistingChangeAction".equalsIgnoreCase(functionality) || "AddToExistingCA".equalsIgnoreCase(functionality) ||
		  "ECMAddToExistingCO".equalsIgnoreCase(functionality) || "ECMAddToExistingCR".equalsIgnoreCase(functionality)){
%>
//alert("<%=XSSUtil.encodeForJavaScript(context,searchURL)%>");
document.ECMfullsearch.action="<%=XSSUtil.encodeForJavaScript(context,searchURL)%>";
showModalDialog(document.ECMfullsearch.action, "600", "500", true);

<%
}else if(!"CreateNewCOUnderCO".equalsIgnoreCase(functionality)){
%>
//alert("<%=XSSUtil.encodeForJavaScript(context,searchURL)%>");
showModalDialog("<%=XSSUtil.encodeForJavaScript(context,searchURL)%>", "600", "500", true);
//document.ECMfullsearch.action="<%=XSSUtil.encodeForJavaScript(context,searchURL)%>";
//document.ECMfullsearch.submit();
<%
}
%>	
</script>
</form>
</body>
</html>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
