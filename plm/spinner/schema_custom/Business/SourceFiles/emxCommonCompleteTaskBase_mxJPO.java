/*
 **   emxCommonCompleteTaskBase.java
 **
 **   Copyright (c) 1992-2020 Dassault Systemes.
 **   All Rights Reserved.
 **   This program contains proprietary and trade secret information of MatrixOne,
 **   Inc.  Copyright notice is precautionary only
 **   and does not evidence any actual or intended publication of such program
 **
 */

import matrix.db.*;
import matrix.util.*;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.RouteIRPCContentPromote;
import com.matrixone.apps.common.InboxTask;
import com.matrixone.apps.common.RouteTaskNotification;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.SubscriptionUtil;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIMenu;

import com.matrixone.apps.framework.ui.UIUtil;

/**
 * The <code>emxCommonCompleteTaskBase</code> class contains
 *
 * @version AEF 9.5.0.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxCommonCompleteTaskBase_mxJPO
{
	private static final String  sRelRouteTask                  = PropertyUtil.getSchemaProperty("relationship_RouteTask");
	private static final String  sRelObjectRoute                = PropertyUtil.getSchemaProperty("relationship_ObjectRoute");
	private static final String  sRelRouteNode                  = PropertyUtil.getSchemaProperty("relationship_RouteNode");
	private static final String  sAttActualCompletionDate       = PropertyUtil.getSchemaProperty("attribute_ActualCompletionDate");
	private static final String  sAttComments                   = PropertyUtil.getSchemaProperty("attribute_Comments");
	private static final String  sAttReviewersComments          = PropertyUtil.getSchemaProperty("attribute_ReviewersComments");
	private static final String  sAttApprovalStatus             = PropertyUtil.getSchemaProperty("attribute_ApprovalStatus");
	private static final String  sAttRouteNodeID                = PropertyUtil.getSchemaProperty("attribute_RouteNodeID");
	private static final String  sAttCurrentRouteNode           = PropertyUtil.getSchemaProperty("attribute_CurrentRouteNode");
	private static final String  sAttRouteStatus                = PropertyUtil.getSchemaProperty("attribute_RouteStatus");
	private static final String  sAttRouteCompletionAction      = PropertyUtil.getSchemaProperty("attribute_RouteCompletionAction");
	private static final String  sAttRouteDemoteAction      = PropertyUtil.getSchemaProperty("attribute_DemoteOnRejection");
	private static final String  sAttRouteAction                = PropertyUtil.getSchemaProperty("attribute_RouteAction");
	private static final String  sAttReviewTask                = PropertyUtil.getSchemaProperty("attribute_ReviewTask");
	private static final String  sAttParallelNodeProcessionRule = PropertyUtil.getSchemaProperty("attribute_ParallelNodeProcessionRule");
	private static final String  sAttRouteSequence 			 = PropertyUtil.getSchemaProperty("attribute_RouteSequence");
	private static final String  sAttRouteBaseState             = PropertyUtil.getSchemaProperty("attribute_RouteBaseState");
	private static final String  sAttRouteBasePolicy            = PropertyUtil.getSchemaProperty("attribute_RouteBasePolicy");
	private static final String  sRouteDelegationGrantor        = PropertyUtil.getSchemaProperty("person_RouteDelegationGrantor");
	private static final String  sAttAutoStopOnRejection        = PropertyUtil.getSchemaProperty("attribute_AutoStopOnRejection");
	private static final String  sAttFirstName        = PropertyUtil.getSchemaProperty("attribute_FirstName");
	private static final String  sAttLastName        = PropertyUtil.getSchemaProperty("attribute_LastName");
	/* Added for Choose users from User Group assignee implementation */
	public static final String  sAttUsreGroupAction        =  PropertyUtil.getSchemaProperty("attribute_UserGroupAction");
	public static final String  sAttUserGroupInfo        =  PropertyUtil.getSchemaProperty("attribute_UserGroupLevelInfo");
	public static final String  sAttTitle                    =  PropertyUtil.getSchemaProperty("attribute_Title");
	public static final String  sAttRouteInstructions        =  PropertyUtil.getSchemaProperty("attribute_RouteInstructions");
	public static final String sAttrDueDateOffset= PropertyUtil.getSchemaProperty("attribute_DueDateOffset");
	public static final String sAttrAssigneeDueDate= PropertyUtil.getSchemaProperty("attribute_AssigneeSetDueDate");
	public static final String sAttScheduledCompletionDate = PropertyUtil.getSchemaProperty("attribute_ScheduledCompletionDate");
	//done
	protected static emxMailUtil_mxJPO mailUtil = null;
	protected static emxSubscriptionManager_mxJPO SubscriptionManager = null;
	protected static emxCommonInitiateRoute_mxJPO InitiateRoute = null;
	protected static emxcommonPushPopShadowAgentBase_mxJPO ShadowAgent = null;

	private static final String SELECT_TASK_ASSIGNEE_ID = "from[" + DomainObject.RELATIONSHIP_PROJECT_TASK + "].to.id";
	private static final String SELECT_TASK_ASSIGNEE_TYPE = "from[" + DomainObject.RELATIONSHIP_PROJECT_TASK + "].to.type";
	private static final String SELECT_TASK_ASSIGNEE_TITLE = "from[" + DomainObject.RELATIONSHIP_PROJECT_TASK + "].to."+DomainConstants.SELECT_ATTRIBUTE_TITLE;
	private static final String SELECT_TASK_ASSIGNEE_NAME = "from[" + DomainObject.RELATIONSHIP_PROJECT_TASK + "].to.name";

	private static final String SELECT_SUB_ROUTE_ID = "from["+DomainConstants.RELATIONSHIP_TASK_SUBROUTE+"].to.physicalid";
	private static final String SELECT_SUB_ROUTE_STATUS = "from["+DomainConstants.RELATIONSHIP_TASK_SUBROUTE+"].to.attribute["+DomainConstants.ATTRIBUTE_ROUTE_STATUS+"]";

	/**
	 * Constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since AEF 9.5.0.0
	 * @grade 0
	 */
	public emxCommonCompleteTaskBase_mxJPO (Context context, String[] args)
			throws Exception
	{
	}

	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @returns nothing
	 * @throws Exception if the operation fails
	 * @since AEF 9.5.0.0
	 */
	public int mxMain(Context context, String[] args)
			throws Exception
	{
		if (true)
		{
			throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonCompleteTask.SpecifyMethodOnServiceCommonCompleteTaskInvocation", context.getLocale().getLanguage()));
		}
		return 0;
	}

	/**
	 * emxServicecommonCompleteTask method to remove the proicess from the tcl
	 *
	 * The method is supposed to be invoked on completion of any task of route.
	 * The high level operations performed by this method are:-
	 * If the task is rejected, and the attribute Auto Stop On Rejection is Immediate, then the route will be stops.
	 * If the task is rejected, and the attribute Auto Stop On Rejection is Deferred, then the route will be stopped on completion of all the task on this level.
	 * If the task is completed, and there are no more tasks on the level, then decides if the route is to be stopped or finished. It is stopped if 
	 * one of the tasks on this level is rejected and Auto Stop On Rejection was set to Deferred.
	 * If all the tasks on this level is completed, none of the task is rejected, and there are more tasks in the route, then next level tasks are activated.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * @returns booelan
	 * @throws Exception if the operation fails
	 * @since AEF 10 minor 1
	 */
	public static int completeTask (Context context, String args[]) throws Exception
	{
		String fromAutoComplete = MqlUtil.mqlCommand(context, "get env $1", "MX_TASK_AUTO_COMPLETE");
		String contextUser  = context.getUser();
		String routeId = "";
		boolean isRouteCompleted = false;
		boolean isContentPromoted = true;
		if ("true".equals(fromAutoComplete)) {
			return 0;
		}
		boolean enforceSignature = true;
		try {
			String enforceSignatureStr = EnoviaResourceBundle.getProperty(context,"emxSystem.Route.EnforceSignature");
			enforceSignature = Boolean.valueOf(enforceSignatureStr);
		}catch(Exception ex) {
			enforceSignature = true;
		}
		try {
			ShadowAgent = new emxcommonPushPopShadowAgentBase_mxJPO(context,null);
			ShadowAgent.pushContext(context,null);

			String  sAttRouteTargetState             = PropertyUtil.getSchemaProperty(context,"attribute_RouteTargetState");
			String  sStateAssigned                  = FrameworkUtil.lookupStateName(context, DomainConstants.POLICY_INBOX_TASK ,"state_Assigned");
			String  sStateReview                   = FrameworkUtil.lookupStateName(context, DomainConstants.POLICY_INBOX_TASK ,"state_Review");
			String  sStateComplete                  = FrameworkUtil.lookupStateName(context, DomainConstants.POLICY_ROUTE ,"state_Complete");
			String message= "";

			String sObjectsNotSatisfied  = "";
			String sRoutesInProcess  = "";
			String sPromotedObjects ="" ;
			StringBuffer sBufNotPromotedObjects = new StringBuffer();
			String sNotPromotedObjs="";
			StringBuffer sBufObjectsNotSatisfied  = new StringBuffer();
			StringBuffer sBufRoutesInProcess  = new StringBuffer();
			StringBuffer sBufPromotedObjects =new StringBuffer();
			StringBuffer sBufInCompleteRoutes = new StringBuffer();
			StringBuffer sBufPromotedObjectsNotification =new StringBuffer();
			String sInCompleteRoutes ="";
			// Get absolute names from symbolic names
			String  sDate =  MqlUtil.mqlCommand(context,"get env $1", "TIMESTAMP");

			//Initializing the jpos to be used
			mailUtil = new emxMailUtil_mxJPO(context, null);

			InitiateRoute = new emxCommonInitiateRoute_mxJPO(context, null);

			// Getting the type name rev from teh argument's passed
			String sType = args[0];
			String sName = args[1];
			String sRev  = args[2];
			//the below line is commented for the bug 319223
			// String bConsiderAdhocRoutes ="FALSE";
			// Get setting from emxSystem.properties file to
			// check if Ad Hoc routes should be considered or not

			// Bug 293332
			String arguments[] = new String[4];
			/* arguments[0]= "emxFramework.AdHocRoutesBlockLifecycle";
           arguments[1]=  "0";
           arguments[2] = "";
           arguments[3]= "emxSystem";
           String bConsiderAdhocRoutes =mailUtil.getMessage(context,arguments);
           // set default to false if property doesn't exists
           bConsiderAdhocRoutes = bConsiderAdhocRoutes.toUpperCase();
           if (!bConsiderAdhocRoutes.equals("TRUE")) {
               bConsiderAdhocRoutes = "FALSE";
           }*/

			// Set Actual Completion Date attribute in Inbox Task
			BusinessObject bObject = new BusinessObject(sType,sName,sRev,"");
			bObject.open(context);
			String ObjectId = bObject.getObjectId();
			bObject.close(context);


			DomainObject inboxTask = new DomainObject(ObjectId);
			inboxTask.setAttributeValue(context, sAttActualCompletionDate, sDate);



			SelectList objectSelects = new SelectList();
			objectSelects.add("attribute["+sAttRouteNodeID+"]");
			objectSelects.add("attribute["+sAttApprovalStatus+"]");
			objectSelects.add("attribute["+sAttActualCompletionDate+"]");
			objectSelects.add("attribute["+sAttComments+"]");
			objectSelects.add("attribute["+sAttRouteAction+"]");
			objectSelects.add("attribute["+sAttReviewTask+"]");
			objectSelects.add(SELECT_TASK_ASSIGNEE_NAME);
			objectSelects.add(DomainConstants.SELECT_OWNER);
			objectSelects.add(InboxTask.SELECT_TITLE);
			objectSelects.add(InboxTask.SELECT_NAME);
			objectSelects.add(DomainConstants.SELECT_PHYSICAL_ID);
			objectSelects.add("attribute["+sAttReviewersComments+"]");
			objectSelects.add("from[Route Task].to.id");
			objectSelects.add(Route.SELECT_ATTRIBUTE_OBJECT_ROUTE_MODELER_TYPE_WITH_TASK);
			objectSelects.add(Route.SELECT_REVISION);
			objectSelects.add("from["+sRelRouteTask+"].businessobject.from["+sRelRouteNode+"].id");
			objectSelects.add("from["+sRelRouteTask+"].businessobject.from["+sRelRouteNode+"].attribute["+sAttRouteNodeID+"]");
						
			
			List multivalueList = new ArrayList();
			multivalueList.add("from["+sRelRouteTask+"].businessobject.from["+sRelRouteNode+"].id");
			multivalueList.add("from["+sRelRouteTask+"].businessobject.from["+sRelRouteNode+"].attribute["+sAttRouteNodeID+"]");
			
			Map objectMap = inboxTask.getInfo(context, objectSelects,multivalueList);

			routeId = (String)objectMap.get("from[Route Task].to.id");
			String sRouteNodeIDOnIB      = (String)objectMap.get("attribute["+sAttRouteNodeID+"]");
			String sApprovalStatus       = (String)objectMap.get("attribute["+sAttApprovalStatus+"]");
			String sActualCompletionDate = (String)objectMap.get("attribute["+sAttActualCompletionDate+"]");
			String sComments             = (String)objectMap.get("attribute["+sAttComments+"]");
			String sReviewersComments    = (String)objectMap.get("attribute["+sAttReviewersComments+"]");
			String sRouteActionOfTask    = (String)objectMap.get("attribute["+sAttRouteAction+"]");
			String sReviewTask  		 = (String)objectMap.get("attribute["+sAttReviewTask+"]");
			String sTaskAssignee  		 = (String)objectMap.get(DomainConstants.SELECT_OWNER);
			String sTaskPhysicalId  	 = (String)objectMap.get(DomainConstants.SELECT_PHYSICAL_ID);
			String sTaskTitle 			 = (String)objectMap.get(InboxTask.SELECT_TITLE);
			String sTaskName 			 = (String)objectMap.get(InboxTask.SELECT_NAME);
		
			if(UIUtil.isNullOrEmpty(sTaskTitle)){
				sTaskTitle = sName;
			}
			StringList lRouteNodeId      =  (StringList) objectMap.get("from["+sRelRouteTask+"].businessobject.from["+sRelRouteNode+"].id");
			StringList lRouteNodeIdAttr  =  (StringList) objectMap.get( "from["+sRelRouteTask+"].businessobject.from["+sRelRouteNode+"].attribute["+sAttRouteNodeID+"]");

			String sRouteNodeId = "";
			String sRouteNodeIdAttr ="";

			// Get matching relationship id
			int bRouteNodeIdFound = 0;
			// need to update this for loop

			for(int i=0; i< lRouteNodeId.size(); i++) {

				sRouteNodeId = (String)lRouteNodeId.get(i);

				sRouteNodeIdAttr = (String) lRouteNodeIdAttr.get(i);
				if (sRouteNodeIDOnIB.equals(sRouteNodeIdAttr))
				{
					bRouteNodeIdFound = 1;
					break;
				}
			}

			// If Route Node Id not found then
			// Error out
			if (bRouteNodeIdFound == 0)
			{
				String arguments1[] = new String[13];
				arguments1[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.InvalidRouteNodeId";
				arguments1[1]  = "3";
				arguments1[2]  = "type";
				arguments1[3]  = sType;
				arguments1[4]  = "name";
				arguments1[5]  = sName;
				arguments1[6]  = "rev";
				arguments1[7]  = sRev;
				arguments1[8]  = "";
				message=mailUtil.getMessage(context,arguments1);
				MqlUtil.mqlCommand(context, "notice $1",message);
				return 1;
			}
			String mailEnabled = EnoviaResourceBundle.getProperty(context,"emxFramework.Routes.EnableMails");
			String notifyType = "iconmail";
			if("true".equalsIgnoreCase(mailEnabled)){
				notifyType = "both";
			}
			Map map = new HashMap();
			map.put(sAttApprovalStatus,sApprovalStatus);
			map.put(sAttActualCompletionDate,sActualCompletionDate);
			map.put(sAttComments,sComments);
			DomainRelationship.setAttributeValues(context,sRouteNodeId,map);
			String relationshipIds[] = new String[1];
			relationshipIds[0]= sRouteNodeId;
			SelectList RelSelects = new SelectList();
			RelSelects.add("from.id");
			RelSelects.add("from.physicalid");
			RelSelects.add("from.owner");
			RelSelects.add("from.type");
			RelSelects.add("from.name");
			RelSelects.add("from.attribute[Title]");
			RelSelects.add("from.revision");
			RelSelects.add("from.attribute["+sAttCurrentRouteNode+"]");
			RelSelects.add("from.attribute["+sAttRouteStatus+"]");
			RelSelects.add("from.attribute["+sAttRouteCompletionAction+"]");
			RelSelects.add("from.attribute["+sAttRouteDemoteAction+"]");
			RelSelects.add("to.name");
			RelSelects.add("to.attribute["+sAttFirstName+"]");
			RelSelects.add("to.attribute["+sAttLastName+"]");
			RelSelects.add("attribute["+sAttParallelNodeProcessionRule+"]");
			/*Added for choose users from UG*/
			RelSelects.add("attribute["+sAttUsreGroupAction+"]");
			RelSelects.add("attribute["+sAttUserGroupInfo+"]");
			//End
			RelSelects.add("attribute["+sAttRouteSequence+"]");
			RelSelects.add("from.to["+sRelObjectRoute+"].from.id");
			RelSelects.add("from.to["+sRelObjectRoute+"].from.current.satisfied");
			RelSelects.add("from.to["+sRelObjectRoute+"].from.type");
			RelSelects.add("from.to["+sRelObjectRoute+"].from.name");
			RelSelects.add("from.to["+sRelObjectRoute+"].from.revision");
			RelSelects.add("from.to["+sRelObjectRoute+"].from.current");
			RelSelects.add("from.to["+sRelObjectRoute+"].from.policy");

			MapList relMapList = DomainRelationship.getInfo(context, relationshipIds, RelSelects);
			// Get information on attached route
			String taskType = "";
			Map relMap = (Map)relMapList.get(0);
			String sRouteId          =  (String)relMap.get("from.id");
			String sRoutePhysicalId  =  (String)relMap.get("from.physicalid");
			String sOwner            =  (String)relMap.get("from.owner");
			String sRouteType        =  (String)relMap.get("from.type");
			String sRouteName        =  (String)relMap.get("from.name");
			String sRouteTitle        =  (String)relMap.get("from.attribute[Title]");
			String sRouteRev         =  (String)relMap.get("from.revision");
			String sRouteStatus      =  (String)relMap.get("from.attribute["+sAttRouteStatus+"]");
			String sRouteCompletionAction      =  (String)relMap.get("from.attribute["+sAttRouteCompletionAction+"]");
			String sRouteDemoteAction      =  (String)relMap.get("from.attribute["+sAttRouteDemoteAction+"]");
			String sPerson           =  (String)relMap.get("to.name");
			String sUsergroupAction = (String)relMap.get("attribute["+sAttUsreGroupAction+"]");
			String sUsergroupInfo = (String)relMap.get("attribute["+sAttUserGroupInfo+"]");
			//lvc
			String sFirstName = (String)relMap.get("to.attribute["+sAttFirstName+"]");
			String sLastName = (String)relMap.get("to.attribute["+sAttLastName+"]");
			String sProcessionRule   =  (String)relMap.get("attribute["+sAttParallelNodeProcessionRule+"]");
			String sRouteSequence = (String)relMap.get("attribute["+sAttRouteSequence+"]");
			int sCurrentRouteNode    = Integer.parseInt((String)relMap.get("from.attribute["+sAttCurrentRouteNode+"]"));




			DomainObject Route = new DomainObject(sRouteId);

			//modified for the 327641   12/28/2006-- Begin
			objectSelects = new SelectList();
			objectSelects.add(DomainConstants.SELECT_ID);
			objectSelects.add("current");
			objectSelects.add("attribute["+sAttRouteNodeID+"]");
			objectSelects.add("owner");
			objectSelects.add(DomainConstants.SELECT_TYPE);
			objectSelects.add(DomainConstants.SELECT_NAME);
			objectSelects.add(DomainConstants.SELECT_REVISION);
			objectSelects.add(SELECT_TASK_ASSIGNEE_ID);
			objectSelects.add(SELECT_TASK_ASSIGNEE_NAME);
			objectSelects.add(SELECT_TASK_ASSIGNEE_TYPE);
			objectSelects.add(SELECT_TASK_ASSIGNEE_TITLE);
			objectSelects.add(InboxTask.SELECT_TITLE);
			objectSelects.add(DomainConstants.SELECT_PHYSICAL_ID);
			objectSelects.add("attribute["+sAttUsreGroupAction+"]");
			objectSelects.add("attribute["+sAttUserGroupInfo+"]");
			objectSelects.add(SELECT_SUB_ROUTE_ID);
			objectSelects.add(SELECT_SUB_ROUTE_STATUS);
			
			//FUN135735  task content selector
			objectSelects.add("to["+DomainConstants.RELATIONSHIP_TASK_CONTENT+"].fromrel["+DomainConstants.RELATIONSHIP_OBJECT_ROUTE+"].from.name");			
			SelectList relSelects = new SelectList();

			MapList ObjectsList= Route.getRelatedObjects(context,
					sRelRouteTask,
					"*",
					objectSelects,
					relSelects,
					true,
					false,
					(short)0,
					null,
					null);
			//modified for the 327641  12/28/2006-- Ends

			//Start: Resume Process
			// Due to Resume Process algorithm, there can be some tasks which are connected to route but are not connected to person
			// These tasks should be removed from the ObjectsList.
			MapList mlFilteredObjectsList = new MapList();
			Map mapCurrentObjectInfo = null;
			for (Iterator itrObjectsList = ObjectsList.iterator(); itrObjectsList.hasNext(); ) {
				mapCurrentObjectInfo = (Map)itrObjectsList.next();

				if (mapCurrentObjectInfo.get(SELECT_TASK_ASSIGNEE_ID) != null) {
					mlFilteredObjectsList.add(mapCurrentObjectInfo);
				}
			}
			ObjectsList = mlFilteredObjectsList;
			//End: Resume Process
			// Get all the Route content information - Start
			objectSelects = new SelectList();
			objectSelects.add(DomainConstants.SELECT_ID);
			objectSelects.add(DomainConstants.SELECT_PHYSICAL_ID);
			objectSelects.add(DomainConstants.SELECT_TYPE);
			objectSelects.add(DomainConstants.SELECT_NAME);
			objectSelects.add(DomainConstants.SELECT_REVISION);
			objectSelects.add(DomainConstants.SELECT_DESCRIPTION);
			objectSelects.add("current.satisfied");
			objectSelects.add(DomainConstants.SELECT_CURRENT);
			objectSelects.add(DomainConstants.SELECT_POLICY);
			objectSelects.add(DomainConstants.SELECT_STATES);
			objectSelects.add(DomainConstants.SELECT_ATTRIBUTE_TITLE);
			objectSelects.add(DomainConstants.SELECT_ATTRIBUTE_PLMCoreRepReference_V_isOnceInstantiable);
			objectSelects.add(DomainConstants.SELECT_KINDOF_PLMENTITY);
			com.matrixone.apps.common.Route.addTitleSelectables(objectSelects);
			relSelects = new SelectList();
			relSelects.add("attribute["+sAttRouteBaseState+"].value");
			relSelects.add("attribute["+sAttRouteTargetState+"].value");
			relSelects.add("attribute["+sAttRouteBasePolicy+"].value");
			relSelects.add("attribute["+ DomainConstants.ATTRIBUTE_MODELER_TYPE + "]");
			MapList routeContentList = Route.getRelatedObjects(context, sRelObjectRoute, "*", objectSelects, relSelects, true, false, (short) 0, null, null, 0);
			routeContentList.addSortKey(DomainConstants.SELECT_NAME, DomainConstants.SortDirection.ASCENDING.toString(), String.class.getSimpleName());
			routeContentList.sort();

			StringBuffer sRouteContent = new StringBuffer();
			StringBuffer sRouteContentTitle = new StringBuffer();
			StringBuffer sRouteContent3D = new StringBuffer();
			StringBuffer sRouteContent3DTitles = new StringBuffer();
			StringBuffer sRouteContent3DIds = new StringBuffer();
			StringBuffer sRouteContent3DTypes = new StringBuffer();
			StringBuffer sRouteContentDescription = new StringBuffer();
			String modelerType = "";
			for(int i = 0; i < routeContentList.size(); i++) {
				// Map of the Objects
				Map routeContent = (Map) routeContentList.get(i);

				// build content links for notification table
				if(i > 0 && i < routeContentList.size()) {
					sRouteContent.append("<br/><br/>");
					sRouteContentDescription.append("<br/><br/>");

					sRouteContent3D.append(", ");
					sRouteContent3DTitles.append(", ");
					sRouteContent3DIds.append(", ");
					sRouteContent3DTypes.append(", ");
				}
				sRouteContent3D.append((String) routeContent.get(DomainConstants.SELECT_NAME));
				sRouteContent3DTitles.append(com.matrixone.apps.common.Route.getDisplayTitle(routeContent));
				sRouteContent3DIds.append((String) routeContent.get(DomainConstants.SELECT_ID));
				sRouteContent3DTypes.append((String) routeContent.get(DomainConstants.SELECT_TYPE));
				String resourceRefAttr = (String) routeContent.get("attribute["+ DomainConstants.ATTRIBUTE_MODELER_TYPE + "]");
				if(UIUtil.isNotNullAndNotEmpty(resourceRefAttr)) {
					modelerType = resourceRefAttr;
				}
				sRouteContentTitle.append(routeContent.get(DomainConstants.SELECT_ATTRIBUTE_TITLE));
				sRouteContent.append(emxNotificationUtil_mxJPO.getObjectLinkHTML(context, (String) routeContent.get(DomainConstants.SELECT_NAME), (String) routeContent.get(DomainConstants.SELECT_ID)));
				sRouteContentDescription.append(FrameworkUtil.stripString((String) routeContent.get(DomainConstants.SELECT_DESCRIPTION), InboxTask.MAX_LEN_OF_CNT_DESC_FOR_ROUTE_NOTIFICATIONS, FrameworkUtil.StripStringType.WORD_STRIP));
			}
			String sInboxTaskLink = emxNotificationUtil_mxJPO.getObjectLinkHTML(context, sName, ObjectId);
			String sRouteLink = emxNotificationUtil_mxJPO.getObjectLinkHTML(context, sRouteName, sRouteId);
			// Get all the Route content information -End
			//Start: Auto-Stop
			// If this task is rejected then send the rejection notice

			//Find if all tasks are completed on this level and Find at least one task is rejected on this level
			final String SELECT_CURRENT_ROUTE_NODE = "attribute[" + sAttCurrentRouteNode + "]";
			final String SELECT_ROUTE_NODE_ID = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_NODE_ID + "]";
			final String SELECT_AUTO_STOP_ON_REJECTION = "attribute[" + sAttAutoStopOnRejection + "]";
			final String SELECT_APPROVAL_STATUS = "attribute["+sAttApprovalStatus+"]";
			final boolean GET_TO = true;
			final boolean GET_FROM = true;
			StringList slBusSelect = new StringList();
			slBusSelect.add(com.matrixone.apps.common.Route.SELECT_ROUTE_STATUS);
			slBusSelect.add(SELECT_CURRENT_ROUTE_NODE);
			slBusSelect.add(SELECT_AUTO_STOP_ON_REJECTION);
			slBusSelect.add(DomainConstants.SELECT_OWNER);

			Map mapInfo = Route.getInfo(context, slBusSelect);

			String strCurrentRouteLevel = (String)mapInfo.get(SELECT_CURRENT_ROUTE_NODE);
			String sAutoStopOnRejection  = (String)mapInfo.get(SELECT_AUTO_STOP_ON_REJECTION);

			// Expand route and get 'Route Node ID' on relationship 'Route Node' for 'Route Sequence' = 'Current Route Node'
			slBusSelect = new StringList();
			StringList slRelSelect = new StringList();
			slRelSelect.add(SELECT_ROUTE_NODE_ID);

			String strRelPattern = DomainObject.RELATIONSHIP_ROUTE_NODE;
			slRelSelect.add("attribute["
					+ sAttrDueDateOffset + "]");
			slRelSelect.add("attribute[" + sAttrAssigneeDueDate
					+ "]");
			slRelSelect.add("attribute[" + sAttScheduledCompletionDate
					+ "]");
			String strTypePattern = "*";
			String strBusWhere = "";
			String strRelWhere = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_SEQUENCE + "]==" + strCurrentRouteLevel;
			short nRecurseLevel = (short)1;

			MapList mlRouteNodes = Route.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseLevel, strBusWhere, strRelWhere);

			StringBuffer sbufMatchList = new StringBuffer(64);
			Map mapRouteNode = null;
			for (Iterator itrRouteNodes = mlRouteNodes.iterator(); itrRouteNodes.hasNext();) {
				mapRouteNode = (Map)itrRouteNodes.next();

				if (sbufMatchList.length() > 0) {
					sbufMatchList.append(",");
				}
				sbufMatchList.append(mapRouteNode.get(SELECT_ROUTE_NODE_ID));
			}

			// Expand route and get id for tasks with 'Route Node ID' = 'Route Node ID' just found.
			slBusSelect = new StringList(DomainObject.SELECT_ID);
			slBusSelect.add(DomainObject.SELECT_CURRENT);
			slBusSelect.add(SELECT_APPROVAL_STATUS);

			slRelSelect = new StringList();
			strRelPattern = DomainObject.RELATIONSHIP_ROUTE_TASK;
			strTypePattern = DomainObject.TYPE_INBOX_TASK;
			strBusWhere = "(attribute[" + DomainObject.ATTRIBUTE_ROUTE_NODE_ID + "] matchlist \"" + sbufMatchList.toString() +"\" \",\")";
			strRelWhere = "";
			nRecurseLevel = (short)1;

			MapList mlTasks = Route.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, GET_TO, !GET_FROM, nRecurseLevel, strBusWhere, strRelWhere);

			String dislpayCompletionDateUsersTZ = sActualCompletionDate;
			// Get server timezone offset //
			double userTZOffset = 0.0;
			try{
				TimeZone tz = TimeZone.getDefault();
				// To get server timezone offset //
				userTZOffset = (double) tz.getOffset(System.currentTimeMillis());
				// Server timezone offset into Hours //
				userTZOffset = (double) (userTZOffset / (60*60*1000));
				// Server timezone offset change to +- GMT
				userTZOffset = userTZOffset - (2 * userTZOffset);
				dislpayCompletionDateUsersTZ = eMatrixDateFormat.getFormattedDisplayDateTime(context, sOwner, dislpayCompletionDateUsersTZ, true, DateFormat.MEDIUM, userTZOffset, Locale.ENGLISH);
			}catch(Exception e){}

			// Find if at least one task is incomplete on this level
			Map mapTaskInfo = null;
			String strTaskState = "";
			String strApprovalStatus = "";
			boolean isLevelCompleted = true;
			boolean isTaskRejectedOnThisLevel = false;
			String isBranchExist = Boolean.TRUE.toString();
			String groupType = PropertyUtil.getSchemaProperty(context,"type_Group");
			String proxyGoupType = PropertyUtil.getSchemaProperty(context,"type_GroupProxy");
			for (Iterator itrRouteNodes = mlTasks.iterator(); itrRouteNodes.hasNext();) {
				mapTaskInfo = (Map)itrRouteNodes.next();

				strTaskState = (String)mapTaskInfo.get(DomainObject.SELECT_CURRENT);
				strApprovalStatus = (String)mapTaskInfo.get(SELECT_APPROVAL_STATUS);

				if(!strTaskState.equals(sStateComplete)) {
					isLevelCompleted = false;
				}

				if ("Reject".equals(strApprovalStatus) && DomainConstants.STATE_INBOX_TASK_COMPLETE.equalsIgnoreCase(strTaskState)) {
					isTaskRejectedOnThisLevel = true;
				}
			}

			boolean isRouteToBeStopped = false;
			if ("Approve".equals(sRouteActionOfTask))
			{
				if ("Reject".equals(sApprovalStatus)) {
					isRouteToBeStopped = true;
				}
			}
			// Bug 346841 : Removed following if from another else part to also consider Approve kind of tasks. 
			if (isTaskRejectedOnThisLevel) {
				isRouteToBeStopped = true;
			}

			if (!isLevelCompleted) {
				// For Auto Complete(mark as Complete command), "any" processing rule doesn't apply.
				// This means, if task is auto completed, another task on same level wont complete. 
				// This isn't required as isLevelCompleted variable doesn't affect auto complete operation, however in that scenario, this variable value will be wrong.
				if ("any".equalsIgnoreCase(sProcessionRule) && !"Auto Complete".equals(sApprovalStatus)) {
					isLevelCompleted = true;
				}
			}
			if (isRouteToBeStopped) {
				if ("Immediate".equals(sAutoStopOnRejection) || isLevelCompleted) {

					StringJoiner contentsDemoted = new StringJoiner(",");
					StringJoiner contentsNotDemoted = new StringJoiner(",");
					if("Yes".equalsIgnoreCase(sRouteDemoteAction) ) {
						for(int count=0; count<routeContentList.size();count++) {
							Map object = (Map) routeContentList.get(count);
							String sObjName = (String) object.get(DomainConstants.SELECT_NAME);
							String sObjId = (String) object.get(DomainConstants.SELECT_ID);
							String sObjCurrent = (String) object.get(DomainConstants.SELECT_CURRENT);
							String kindOfPLM = (String) object.get(DomainConstants.SELECT_KINDOF_PLMENTITY);

							StringList  lObjState=new StringList();
							if(object.get(DomainConstants.SELECT_STATES) instanceof StringList) {
								lObjState = (StringList) object.get(DomainConstants.SELECT_STATES);
							}
							String sObjBaseState = (String) object.get("attribute["+sAttRouteBaseState+"].value");
							String sObjBasePolicy= (String) object.get("attribute["+sAttRouteBasePolicy+"].value");
							sObjBasePolicy= PropertyUtil.getSchemaProperty(context,sObjBasePolicy);
							sObjBaseState= FrameworkUtil.lookupStateName(context, sObjBasePolicy, sObjBaseState);
							if ("Ad Hoc".equals(sObjBaseState) || sObjCurrent == (lObjState.get(0)) || "true".equalsIgnoreCase(kindOfPLM)) {
								continue;
							}
							DomainObject dObject = new DomainObject(sObjId);
							boolean pushToRouteOwner = false;
							boolean needToContinue = false;
							try {
								String userAgentName = PropertyUtil.getSchemaProperty(context, "person_UserAgent");
								if((sOwner != null && !"".equals(sOwner)) && context.getUser().equals(userAgentName) ) {
									ContextUtil.pushContext(context, sOwner, null, context.getVault().getName());
									pushToRouteOwner = true;
								}
								StringList selects  = new StringList(2);
								selects.add("current.access[demote]");
								selects.add("current");
								Map currentDetails = dObject.getInfo(context,selects );
								String hasDemoteAccess = (String)currentDetails.get("current.access[demote]");
								String currentState = (String)currentDetails.get("current");
								if("true".equalsIgnoreCase(hasDemoteAccess) && currentState.equals(sObjBaseState)) {
									try {
										MqlUtil.mqlCommand(context, "set env $1 $2", "MX_CONTENT_DEMOTE_TASK_REJECTION","true");
										MqlUtil.mqlCommand(context,"demote bus $1", sObjId);
										contentsDemoted.add(sObjName);
									}catch(FrameworkException ex) {										
										System.out.println("Exception while demoting : "+ex.getMessage());
										List<String> errorMsgList = getEKLArchiveTemplateErrorList(context);
										boolean isArchiveOrDraftErrorMsg = errorMsgList.stream().anyMatch(errorMsg -> ex.getMessage().contains(errorMsg));
										if(isArchiveOrDraftErrorMsg) {
											throw new Exception(ex.getMessage());
										}
										contentsNotDemoted.add(sObjName);
										needToContinue = true;
									}finally {
										MqlUtil.mqlCommand(context, "unset env $1", "MX_CONTENT_DEMOTE_TASK_REJECTION");
									}
								}else {
									contentsNotDemoted.add(sObjName);
									needToContinue = true;
								}
							}finally {
								if( pushToRouteOwner ) {
									ContextUtil.popContext(context);
								}
								if(needToContinue) {
									continue;
								}
							}
							objectSelects = new SelectList();
							objectSelects.add("attribute["+DomainConstants.ATTRIBUTE_ROUTE_STATUS+"]");
							objectSelects.add(DomainConstants.SELECT_ID);
							relSelects = new SelectList();
							relSelects.add("attribute["+sAttRouteBaseState+"].value");
							relSelects.add("attribute["+sAttRouteBasePolicy+"].value");
							Pattern typePattern = new Pattern(DomainObject.TYPE_ROUTE);
							MapList ObjectList= dObject.getRelatedObjects(context,
									sRelObjectRoute,
									typePattern.getPattern(),
									objectSelects,
									relSelects,
									false,
									true,
									(short)1,
									"",
									"");
							for(int i=0; i<ObjectList.size() ; i++) {
								Map objectsMap = (Map)ObjectList.get(i);
								String routeStatus =(String) objectsMap.get("attribute["+DomainConstants.ATTRIBUTE_ROUTE_STATUS+"]");
								String rId = (String) objectsMap.get(DomainConstants.SELECT_ID);
								String baseState = (String) object.get("attribute["+sAttRouteBaseState+"].value");
								String basePolicy= (String) object.get("attribute["+sAttRouteBasePolicy+"].value");
								basePolicy= PropertyUtil.getSchemaProperty(context,basePolicy);
								baseState= FrameworkUtil.lookupStateName(context, basePolicy, baseState);
								if("Started".equalsIgnoreCase(routeStatus) && !sRouteId.equals(rId) && sObjCurrent.equals(baseState)) {
									com.matrixone.apps.common.Route.stopRoute(context, (String) objectsMap.get(DomainConstants.SELECT_ID), false, null, null, null, null, null,null, true);
								}
							}
						}			
					}
					// Set Route Status attribute to Stopped
					com.matrixone.apps.common.Route.stopRoute(context, Route.getId(context),true,sComments,sTaskTitle,ObjectId,(String) objectMap.get(SELECT_TASK_ASSIGNEE_NAME),contentsDemoted.toString(),contentsNotDemoted.toString(),false);
					sendNotification(context, sRouteActionOfTask, ObjectId,sTaskPhysicalId, sRouteId,sRoutePhysicalId,(String)mapInfo.get(DomainConstants.SELECT_OWNER),  sApprovalStatus,  sRouteName,  sLastName,  sFirstName,  sPerson, sType, sName,sTaskTitle, sRev, sRouteType, sRouteRev, dislpayCompletionDateUsersTZ, sComments, sReviewersComments,sRouteLink, sInboxTaskLink, routeContentList.size(), sRouteContent,sRouteContent3DIds,sRouteContent3DTitles,sRouteContent3DTypes, sRouteContentDescription,sRouteContent3D.toString(),sReviewTask,sTaskAssignee,sRouteTitle,modelerType,sTaskName);

					Map objectDetails= null;
					String sState = null;
					for( int i=0; i< ObjectsList.size() ; i++)
					{
						objectDetails = (Map) ObjectsList.get(i);
						sState = (String) objectDetails.get("current");
						if (sState.equals(sStateAssigned) || sState.equals(sStateReview))
						{
							// Below commented for IR-792857. In both cases Any/All the Inbox task should be deleted when same order task is rejected //
							// The disconnect is commented because this is the Rode node object. This should not be disconnected. //
							//if ((sProcessionRule.toLowerCase()).equals("any"))
							//{
							//DomainRelationship.disconnect(context, sRouteNodeID);

							// Delete unsigned/non-completed tasks
							//DomainObject.deleteObjects(context, new String[]{(String)objectDetails.get(DomainConstants.SELECT_ID)});
							//added below code to fix issue reported in IR-884543-3DEXPERIENCER2021x. Instead of deleting the task, we re autocompleting the task.
							try {
								ContextUtil.pushContext(context);
								completeAssignTaskOnRejection(context, objectMap, ObjectsList, sDate, sRouteId, sRouteSequence, sRoutePhysicalId, sRouteName, sOwner, sRouteContent3DIds.toString(), sRouteContent.toString(), sRouteContent3DTypes.toString(), sRouteContent3DTitles.toString(),sRouteTitle);
								break;
							} finally {
								ContextUtil.popContext(context);
							}


							//}
						}
					}//for
					return 0;
				}
			} // if (isRouteToBeStopped)
			//End: Auto-Stop

			// If Approval Status == Reject

			if (!"Reject".equals(sApprovalStatus))
			{
				// Expand route and get the current state of all Inbox Tasks associated with it

				//commented for the BugNo 327641   12/28/2006-- Ends
				int bFound =0;
				// Added Boolean variable to check if there are any tasks having status != Complete for the bug no 340260
				boolean isNonCompleteTasksThere = false;
				// Logic to check if there any tasks that are not in Complete State
				// Till here
				//sUsergroupInfo
				StringList tasksToBeCompletedList = new StringList();
				StringList subRouteToBeDeleted = new StringList();
				if(UIUtil.isNotNullAndNotEmpty(sUsergroupAction) && UIUtil.isNotNullAndNotEmpty(sUsergroupInfo) && sUsergroupAction.equals("All") && sProcessionRule.equals("Any")) {
					boolean pendingtasks = false;         	
					for( int i=0; i< ObjectsList.size() ; i++)
					{
						Map objectDetails= (Map) ObjectsList.get(i);
						String sState =(String) objectDetails.get("current");
						String sCurentUGInfo = (String) objectDetails.get("attribute["+sAttUserGroupInfo+"]");
						String sCurentUGAction = (String) objectDetails.get("attribute["+sAttUsreGroupAction+"]");
						if(UIUtil.isNotNullAndNotEmpty(sCurentUGInfo) && UIUtil.isNotNullAndNotEmpty(sUsergroupInfo) && sUsergroupInfo.equals(sCurentUGInfo) && sCurentUGAction.equals("All")){
							if(!sState.equals("Complete")) {
								pendingtasks = true;
								break;
							}
						}
					}
					if(pendingtasks) {
						sProcessionRule = sUsergroupAction;
					}

				} else {                	 
					for( int i=0; i< ObjectsList.size() ; i++)
					{
						Map objectDetails= (Map) ObjectsList.get(i);
						String sState =(String) objectDetails.get("current");
						String sCurentUGInfo = (String) objectDetails.get("attribute["+sAttUserGroupInfo+"]");
						String sCurentUGAction = (String) objectDetails.get("attribute["+sAttUsreGroupAction+"]");
						if(UIUtil.isNotNullAndNotEmpty(sCurentUGInfo) && UIUtil.isNotNullAndNotEmpty(sUsergroupInfo) && sUsergroupInfo.equals(sCurentUGInfo) && sCurentUGAction.equals("Any") && !"Auto Complete".equals(sApprovalStatus)){ //conditions to check if the tasks are created
							//from the same user group at the same level(time)
							if(!sState.equals("Complete")){
								tasksToBeCompletedList.add((String) objectDetails.get("id"));
								getSubRouteDetails(subRouteToBeDeleted, objectDetails);
							}	
						}
						// Added for bug no 340260
						if(!sState.equals("Complete"))
							isNonCompleteTasksThere = true;
					}
				}

				Locale strLocale = context.getLocale();
				for(String sTaskId: tasksToBeCompletedList){
					DomainObject inboxTaskToComplete = new DomainObject(sTaskId);
					inboxTaskToComplete.setAttributeValue(context, sAttActualCompletionDate, sDate);
					// inboxTaskToComplete.setAttributeValue(context, sAttApprovalStatus, sApprovalStatus);
					inboxTaskToComplete.setAttributeValue(context, sAttApprovalStatus, "None");
					inboxTaskToComplete.setAttributeValue(context, sAttComments, EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", strLocale, "emxFramework.ProgramObject.eServicecommonCompleteTask.AutoCompleteCommment")+" "+sTaskTitle);
					MqlUtil.mqlCommand(context, "set env $1  $2", "MX_TASK_AUTO_COMPLETE", "true");
					try{
						inboxTaskToComplete.setState(context, "Complete");
					} finally {
						MqlUtil.mqlCommand(context, "set env $1  $2", "MX_TASK_AUTO_COMPLETE", "false");
					}
				}
				for( int i=0; i< ObjectsList.size() ; i++)
				{
					Map objectDetails= (Map) ObjectsList.get(i);
					String sState =(String) objectDetails.get("current");
					// till here
					String currTaskId = (String) objectDetails.get("id");
					if(tasksToBeCompletedList.contains(currTaskId)){ //which means this task has been completed.
						continue;
					}
					String sRouteNodeID = (String) objectDetails.get("attribute["+sAttRouteNodeID+"]");
					if (sState.equals(sStateAssigned) || sState.equals(sStateReview))
					{
						// For Auto Complete(mark as Complete command), "any" processing rule doesn't apply.
						// This means, if task is auto completed, another task on same level wont complete.
 						if ((sProcessionRule.toLowerCase()).equals("any")  && !"Auto Complete".equals(sApprovalStatus) )
						{

							// Added "If Approval Status is Abstain" and "there are still tasks which are not completed", then don't promote the Route for bug no 340260
							if(sApprovalStatus.equals("Abstain") && isNonCompleteTasksThere)
							{
								bFound=1;
								break;
							}
							//Till here
							String sRouteNodeConnectionId = (String) objectDetails.get("id");
							/*bRouteNodeIdFound = 1;
                            for(int count=0; count< lRouteNodeId.size(); count++)
                            {
                            sRouteNodeId = (String)lRouteNodeId.elementAt(i);
                            sRouteNodeIdAttr = (String) lRouteNodeIdAttr.elementAt(i);
                            if(sRouteNodeID.equals(sRouteNodeIdAttr))
                            {
                            bRouteNodeIdFound = 1;
                            break;
                            }
                            }
                            // If Route Node Id not found then
                            // Error out
                            if (bRouteNodeIdFound == 0)
                            {
                            String sCurrTaskType = "";
                            String sCurrTaskName = "";
                            String sCurrTaskRev  = "";
                            arguments = new String[9];
                            arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.InvalidRouteNodeId";
                            arguments[1]  = "3";
                            arguments[2]  = "type";
                            arguments[3]  = sCurrTaskType;
                            arguments[4]  = "name";
                            arguments[5]  = sCurrTaskName;
                            arguments[6]  = "rev";
                            arguments[7]  = sCurrTaskRev;
                            arguments[8]  = "";
                            message= mailUtil.getMessage(context,arguments);
                            MqlUtil.mqlCommand(context,"notice "+message+"");
                            return 1;
                            }
							 */

							//DomainRelationship.disconnect(context, sRouteNodeId);
							//Added code for the Bug NO:330220
							com.matrixone.apps.common.Route route = (com.matrixone.apps.common.Route)DomainObject.newInstance(context,sRouteId);
							String orgRelId=route.getRouteNodeRelId(context,sRouteNodeID);
							String[] relId = new String[1];
							relId[0] = orgRelId;
							MapList relToCheckMapList = DomainRelationship.getInfo(context, relId, RelSelects);
							Map relToCheck = (Map) relToCheckMapList.get(0);
							String curRouteSequence = (String)relToCheck.get("attribute["+sAttRouteSequence+"]");
							if (curRouteSequence.equalsIgnoreCase(sRouteSequence)) {
								//Added code for the Bug NO:330220
								// Delete unsigned/non-completed tasks
								String sTaskId = (String)objectDetails.get(DomainConstants.SELECT_ID);
								String[] objectIds = new String[1];
								objectIds [0] = sTaskId;
								//Locale strLocale = context.getLocale();
								DomainObject inboxTaskToComplete = new DomainObject(sTaskId);
								inboxTaskToComplete.setAttributeValue(context, sAttActualCompletionDate, sDate);
								//inboxTaskToComplete.setAttributeValue(context, sAttApprovalStatus, sApprovalStatus);
								inboxTaskToComplete.setAttributeValue(context, sAttApprovalStatus, "None");
								inboxTaskToComplete.setAttributeValue(context, sAttComments, EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", strLocale, "emxFramework.ProgramObject.eServicecommonCompleteTask.AutoCompleteCommment")+" "+sTaskTitle);
								MqlUtil.mqlCommand(context, "set env $1  $2", "MX_TASK_AUTO_COMPLETE", "true");
								try{
									inboxTaskToComplete.setState(context, "Complete");
								} finally {
									String type= (String) objectDetails.get(DomainConstants.SELECT_TYPE) ;
									String name= (String) objectDetails.get(DomainConstants.SELECT_NAME);
									String revision= (String) objectDetails.get(DomainConstants.SELECT_REVISION);
									String taskTitle = (String) objectDetails.get(DomainConstants.SELECT_ATTRIBUTE_TITLE);

									String mql1 =  "modify bus $1 add history $2 comment $3";
									String historyEntryAll= " Task :'" +" "+ type +" "+ name+" "+ revision +"|"+taskTitle+ "' has been Auto-Completed as "+ sType+" "+ sName+" "+sRev+ "|"+sTaskTitle+"  is complete.";
									MqlUtil.mqlCommand(context, mql1, true, sRouteId, sApprovalStatus, historyEntryAll);
									MqlUtil.mqlCommand(context, "set env $1  $2", "MX_TASK_AUTO_COMPLETE", "false");
								}
								getSubRouteDetails(subRouteToBeDeleted, objectDetails);
								// Send mail to owner of task about deletion
								String toListOfPerson = "";
								if(proxyGoupType.equals((String) objectDetails.get(SELECT_TASK_ASSIGNEE_TYPE)) || groupType.equals((String) objectDetails.get(SELECT_TASK_ASSIGNEE_TYPE)) ){
									toListOfPerson = FrameworkUtil.getPersonsMailListFromUserGroup(context, (String) objectDetails.get(SELECT_TASK_ASSIGNEE_ID));
								}else {
									toListOfPerson = (String) objectDetails.get(DomainConstants.SELECT_OWNER);
								}
								Map payload = new HashMap();
								if(routeContentList.size() > 0) {
									payload.put("tableHeader", "emxFramework.ProgramObject.eServicecommonCompleteTask.TableWithContentHeader");
									payload.put("tableRow", "emxFramework.ProgramObject.eServicecommonCompleteTask.TableWithContentData");
//									String[] tableKeys = {"TaskType", "RouteName", "TaskName", "TaskOwner", "CompletionDate", "Comments", "Content", "ContentDescription"};
//									String[] tableValues = {sRouteActionOfTask, sRouteLink, sInboxTaskLink, sPerson, sActualCompletionDate, sComments, sRouteContent.toString(), sRouteContentDescription.toString()};
//									DomainObject inboxTask = DomainObject.newInstance(context, sTaskPhysicalId);
									String strTaskTitle = inboxTask.getInfo(context, DomainConstants.SELECT_ATTRIBUTE_TITLE);
									// mod by chenyan 修改审核任务完成是邮件的表格列 2026/03/04
									String[] tableKeys = {"TaskType", "RouteName", "RouteTitle","TaskName", "TaskOwner", "CompletionDate", "Comments", "Content", "ContentTitle","ContentDescription"};
									String[] tableValues = {sRouteActionOfTask, sRouteLink,strTaskTitle, sInboxTaskLink, sPerson, dislpayCompletionDateUsersTZ, sComments, sRouteContent.toString(),sRouteContentTitle.toString(), sRouteContentDescription.toString()};


									payload.put("tableRowKeys", tableKeys);
									payload.put("tableRowValues", tableValues);
								} else {
									payload.put("tableHeader", "emxFramework.ProgramObject.eServicecommonCompleteTask.TableHeader");
									payload.put("tableRow", "emxFramework.ProgramObject.eServicecommonCompleteTask.TableData");
									String[] tableKeys = {"TaskType", "RouteName", "TaskName", "TaskOwner", "CompletionDate", "Comments"};
									String[] tableValues = {sRouteActionOfTask, sRouteLink, sInboxTaskLink, sPerson, sActualCompletionDate, sComments};
									payload.put("tableRowKeys", tableKeys);
									payload.put("tableRowValues", tableValues);
								}

								payload.put("subject", "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectTaskAutoComplete");
								payload.put("message", "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageTaskAutoComplete");
								String[] messageKeys = {"IBType", "IBName", "IBRev", "IBRev", "IBType2", "IBName2", "IBRev2"};
								String[] messageValues = {taskType, (String) objectDetails.get(DomainConstants.SELECT_NAME), (String) objectDetails.get(DomainConstants.SELECT_REVISION), sType, sName, sRev, sRouteRev};
								payload.put("messageKeys", messageKeys);
								payload.put("messageValues", messageValues);
								payload.put("click", "emxFramework.ProgramObject.eServicecommonInitiateRoute.ClickMyTasks");
								String sBaseURL = emxMailUtil_mxJPO.getBaseURL(context, null);
								String[] clickKeys = {"url"};
								String[] clickValues = {sBaseURL};
								payload.put("clickKeys", clickKeys);
								payload.put("clickValues", clickValues);

								String[] toList = toListOfPerson.split(",");
								payload.put("toList", toList);
								emxNotificationUtil_mxJPO.objectNotification(context, ObjectId, "APPRouteTaskDelegatedEvent", payload,notifyType); // using APPRouteTaskDelegatedEvent notification object, as we want to mention toList in payload explicitly
								List<String> toListPerson = new ArrayList<String>();
								for(String userName:toListOfPerson.split(",")) {
									toListPerson.add(userName);
								}
								String isUserGroupTask = "No";
								String userGroupTitle = null;
								if(proxyGoupType.equals((String) objectDetails.get(SELECT_TASK_ASSIGNEE_TYPE)) || groupType.equals((String) objectDetails.get(SELECT_TASK_ASSIGNEE_TYPE)) ){
									isUserGroupTask = "Yes";
									userGroupTitle = (String) objectDetails.get(SELECT_TASK_ASSIGNEE_TITLE);
									if(UIUtil.isNullOrEmpty(userGroupTitle)){
										userGroupTitle = (String) objectDetails.get(SELECT_TASK_ASSIGNEE_NAME);
									}
								}
								String completedTaskTitle = (String) objectDetails.get(InboxTask.SELECT_TITLE);
								if(UIUtil.isNullOrEmpty(completedTaskTitle)){
									completedTaskTitle = (String) objectDetails.get(DomainConstants.SELECT_NAME);
								}
								Map<String, String> taskAndRouteInfo = new HashMap<String, String>();
								taskAndRouteInfo.put(RouteTaskNotification.ROUTE_ID, sRoutePhysicalId);
								taskAndRouteInfo.put(RouteTaskNotification.ROUTE_NAME, sRouteTitle);
								taskAndRouteInfo.put(RouteTaskNotification.ROUTE_OWNER, sOwner);
								taskAndRouteInfo.put(RouteTaskNotification.TASK_ID, sTaskId);
								taskAndRouteInfo.put(RouteTaskNotification.COMPLETED_TASK_NAME, completedTaskTitle);
								taskAndRouteInfo.put(RouteTaskNotification.TASK_NAME, sTaskTitle);
								taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENT_IDS, sRouteContent3DIds.toString());
								taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENTS, sRouteContent.toString());
								taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENT_TYPES, sRouteContent3DTypes.toString());
								taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENT_TITLES, sRouteContent3DTitles.toString());
								taskAndRouteInfo.put(RouteTaskNotification.SENDER_USER, sOwner);
								taskAndRouteInfo.put(RouteTaskNotification.IS_USER_GROUP_TASK, isUserGroupTask);
								taskAndRouteInfo.put(RouteTaskNotification.TASK_ATTACHMENTS, InboxTask.getTaskAttachment(context, sTaskId));
								if("Yes".equalsIgnoreCase(isUserGroupTask)){
									taskAndRouteInfo.put(RouteTaskNotification.TASK_ASSIGNEE, userGroupTitle);
									taskAndRouteInfo.put(RouteTaskNotification.ASSIGNEE_TYPE, "usergroup");
								}else {
									taskAndRouteInfo.put(RouteTaskNotification.TASK_ASSIGNEE, (String) objectDetails.get(SELECT_TASK_ASSIGNEE_NAME));
									taskAndRouteInfo.put(RouteTaskNotification.ASSIGNEE_TYPE, "person");
								}
								String currentTaskContent = InboxTask.getTaskContent(context, routeId, sTaskName);
								taskAndRouteInfo.put(RouteTaskNotification.TASK_CONTENTS, currentTaskContent);
								RouteTaskNotification notifyObj = RouteTaskNotification.getInstance(context, sRouteContent3DTypes.toString(),modelerType);
								notifyObj.sendAutoTaskCompletion3DNotification(context, taskAndRouteInfo, toListPerson);
							}
						}
						else {
							bFound =1;
							break;
						}
					}
				}//for

				// If None of the Inbox Task objects are returned and Route Status == Started
				if (bFound == 0 && sRouteStatus.equals("Started")) {

					// Increment Current Route Node attribute on attached Route object
					sCurrentRouteNode++;
					Route.setAttributeValue(context,sAttCurrentRouteNode,""+sCurrentRouteNode);

					// Expand Route Node relationship and get all Relationship Ids whose
					// Route Sequence == Current Route Node value
					arguments = new String[5];
					arguments[0]=sRouteType;
					arguments[1]=sRouteName;
					arguments[2]=sRouteRev;
					arguments[3]=""+sCurrentRouteNode;
					arguments[4]="0";
					int outStr1 =0;
					MqlUtil.mqlCommand(context, "set env $1  $2", "TITLE", sTaskTitle);
					if(mlRouteNodes.size() != mlTasks.size()){
						for (int i = 0; i < mlRouteNodes.size(); i++) {
							Map nodeObjectMap = (Map) mlRouteNodes.get(i);
							//String sRouteSequenceNumber = (String) nodeObjectMap.get("attribute["
							//+ sAttRouteSequence + "]");
							String strScheduledCompletionDate = (String) nodeObjectMap.get("attribute[" + sAttScheduledCompletionDate + "]");
							String strDueDateOffset = (String) nodeObjectMap.get("attribute[" + sAttrDueDateOffset + "]");
							String strAssigneeSetDueDate = (String) nodeObjectMap.get("attribute[" + sAttrAssigneeDueDate + "]");
							if ((strScheduledCompletionDate.equals("")) && (strDueDateOffset.equals(""))
									&& (strAssigneeSetDueDate.equals("No"))) {
								outStr1 = 1;
							}

						}
					}	        
					if(outStr1 == 0){
						outStr1 =  InitiateRoute.InitiateRoute(context, arguments);
					}
					isRouteCompleted = false;
					// Return 0 if no more tasks for route
					if (outStr1 == 0) {
						MqlUtil.mqlCommand(context,"override bus $1", sRouteId);
						Route.promote(context);
						isRouteCompleted = true;
						Route.setAttributeValue(context,sAttRouteStatus,"Finished");
						com.matrixone.apps.common.Route rtObj   = new Route();
						rtObj.setId(sRouteId);
						rtObj.customhistory(context,sRouteId,"Finish");

						// Send notification to subscribers
						String conArgs[]= {sRouteId};

						if(routeContentList.size() > 0) {
							for(int i1=0; i1< routeContentList.size(); i1++) {
								Map objectmap =(Map) routeContentList.get(i1);
								String sObjectId= (String)objectmap.get(DomainConstants.SELECT_ID);

								String out11=MqlUtil.mqlCommand(context,"print bus $1 select $2 dump", sObjectId, "grantor["+sRouteDelegationGrantor+"]");

								if (!out11.equals("FALSE")) {
									//modified for the bug 316518
									MqlUtil.mqlCommand(context,"modify bus $1 revoke grantor $2", sObjectId, sRouteDelegationGrantor);
									//modified for the bug 316518
								}
							}
						}

						if (sRouteCompletionAction.equals("Promote Connected Object")) {
							String isPLMEntity = "false";
							for(int count=0; count<routeContentList.size();count++) {

								Map object = (Map) routeContentList.get(count);
								String sObjType = (String) object.get(DomainConstants.SELECT_TYPE);
								String plmCoreRepReference = (String) object.get(DomainConstants.SELECT_ATTRIBUTE_PLMCoreRepReference_V_isOnceInstantiable);
								isPLMEntity = (String) object.get(DomainConstants.SELECT_KINDOF_PLMENTITY);
								String sObjName = (String) object.get(DomainConstants.SELECT_NAME);
								String sObjRev = (String) object.get(DomainConstants.SELECT_REVISION);
								String sObjId = (String) object.get(DomainConstants.SELECT_ID);
								String physicalId = (String) object.get(DomainConstants.SELECT_PHYSICAL_ID);
								String sIsObjSatisfied = (String) object.get("current.satisfied");
								String sObjCurrent = (String) object.get(DomainConstants.SELECT_CURRENT);
								String sObjPolicy = (String) object.get(DomainConstants.SELECT_POLICY);

								StringList  lObjState=new StringList();
								if(object.get(DomainConstants.SELECT_STATES) instanceof StringList) {
									lObjState = (StringList) object.get(DomainConstants.SELECT_STATES);
								}

								//String lObjState = (String) object.get("state");
								String sObjBaseState = (String) object.get("attribute["+sAttRouteBaseState+"].value");
								String sObjBaseTState = (String) object.get("attribute["+sAttRouteTargetState+"].value");
								String sObjBasePolicy= (String) object.get("attribute["+sAttRouteBasePolicy+"].value");
								int bPromoteObject = 1;

								if(!com.matrixone.apps.common.Route.isStateBlockingAllowed(context, sObjType) || "true".equalsIgnoreCase(plmCoreRepReference)) {
									continue;
								}
								// Check if object state and policy maches with base state and policy
								if (!sObjBaseState.equals("Ad Hoc")) {
									sObjBasePolicy= PropertyUtil.getSchemaProperty(context,sObjBasePolicy);
									sObjBaseState= FrameworkUtil.lookupStateName(context, sObjBasePolicy, sObjBaseState);

									// Get names from properties
									String sTempStore =sObjBaseState;
									if (sObjBaseState.equals("")) {
										arguments = new String[13];
										arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.InvalidPolicy";
										arguments[1]  = "5";
										arguments[2]  = "State";
										arguments[3]  = sTempStore;
										arguments[4]  = "Type";
										arguments[5]  = sRouteType;
										arguments[6]  = "OType";
										arguments[7]  = sObjType;
										arguments[8]  = "OName";
										arguments[9]  = sObjName;
										arguments[10]  = "ORev";
										arguments[11]  = sObjRev;
										arguments[12]  = "";

										message= mailUtil.getMessage(context,arguments);
										MqlUtil.mqlCommand(context, "notice $1",message);

										return 1;
									}

									sTempStore =sObjBasePolicy;

									if (sObjBasePolicy.equals("")) {

										arguments = new String[13];
										arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.InvalidState";
										arguments[1]  = "5";
										arguments[2]  = "Policy";
										arguments[3]  = sTempStore;
										arguments[4]  = "Type";
										arguments[5]  = sRouteType;
										arguments[6]  = "OType";
										arguments[7]  = sObjType;
										arguments[8]  = "OName";
										arguments[9]  = sObjName;
										arguments[10]  = "ORev";
										arguments[11]  = sObjRev;
										arguments[12]  = "";
										message= mailUtil.getMessage(context,arguments);
										MqlUtil.mqlCommand(context, "notice $1",message);

										return 1;
									}
								}

								//the below else block is commented for the bug 319223 -- this functionality regarding this bug
								/* else
                                {
                                if (bConsiderAdhocRoutes.equals("FALSE"))
                                {
                                continue;
                                }
                                }*/

								if (sObjBaseState.equals("Ad Hoc") && (!sObjBaseState.equals(sObjCurrent) || !sObjBasePolicy.equals(sObjPolicy))) {
									continue;
								} 

								// Check if object is in the last state
								/* if ([lsearch $lObjState "$sObjCurrent"] == [expr [llength $lObjState] - 1]) {
                                continue;
                                }*/

								//Modified for Bug No: 293332 and Bug no: 293506
								if(lObjState.indexOf(sObjCurrent) == (lObjState.size()-1)) {
									continue;
								}

								objectSelects = new SelectList();
								objectSelects.add(DomainConstants.SELECT_CURRENT);
								objectSelects.add(DomainConstants.SELECT_TYPE);
								objectSelects.add(DomainConstants.SELECT_NAME);
								objectSelects.add(DomainConstants.SELECT_REVISION);
								objectSelects.add("attribute[Route Completion Action]");
								relSelects = new SelectList();
								relSelects.add("attribute["+sAttRouteBaseState+"].value");
								relSelects.add("attribute["+sAttRouteTargetState+"].value");
								relSelects.add("attribute["+sAttRouteBasePolicy+"].value");
								DomainObject dObject = new DomainObject(sObjId);

								//should retrieve only Route objects
								//Include Route sub_types if applicable, use addPattern()
								Pattern typePattern = new Pattern(DomainObject.TYPE_ROUTE);

								//Modified for Bug No: 293332 and Bug no: 293506
								MapList ObjectList= dObject.getRelatedObjects(context,
										sRelObjectRoute,
										typePattern.getPattern(),//"*",
										objectSelects,
										relSelects,
										false,
										true,
										(short)1,
										"",
										"");

								// Check for each object if there is any route which is not complete

								for(int i=0; i<ObjectList.size() ; i++) {
									Map objectsMap = (Map)ObjectList.get(i);
									String sObjRouteBaseState =(String) objectsMap.get("attribute["+sAttRouteBaseState+"].value");
									String sObjRouteBaseTState =(String) objectsMap.get("attribute["+sAttRouteTargetState+"].value");
									String sObjRouteBasePolicy =(String) objectsMap.get("attribute["+sAttRouteBasePolicy+"].value");
									String sObjRouteType =(String) objectsMap.get(DomainConstants.SELECT_TYPE);
									String sObjRouteName =(String) objectsMap.get(DomainConstants.SELECT_NAME);
									String sObjRouteRev =(String) objectsMap.get(DomainConstants.SELECT_REVISION);
									String sObjRouteAction =(String) objectsMap.get("attribute[Route Completion Action]");
									String sObjRouteCurrent =(String) objectsMap.get("current");

									if (sObjRouteBaseState.equals("")) {
										sObjRouteBaseState = "Ad Hoc";
									}

									if (!sObjRouteBaseState.equals("Ad Hoc")) {
										//Get names from properties

										// Bug 293332
										String sTempStore = sObjRouteBasePolicy;
										sObjRouteBasePolicy= PropertyUtil.getSchemaProperty(context,sObjRouteBasePolicy);

										if (sObjRouteBasePolicy.equals("")) {
											arguments = new String[13];
											arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.InvalidPolicy";
											arguments[1]  = "5";
											arguments[2]  = "State";
											arguments[3]  = sTempStore;
											arguments[4]  = "Type";
											arguments[5]  = sRouteType;
											arguments[6]  = "OType";
											arguments[7]  = sObjType;
											arguments[8]  = "OName";
											arguments[9]  = sObjName;
											arguments[10]  = "ORev";
											arguments[11]  = sObjRev;
											arguments[12]  = "";

											message= mailUtil.getMessage(context,arguments);
											MqlUtil.mqlCommand(context, "notice $1",message);

											return 1;
										}
										// Bug 293332

										sTempStore = sObjRouteBaseState;
										sObjRouteBaseState = FrameworkUtil.lookupStateName(context, sObjRouteBasePolicy, sObjRouteBaseState);
										if (sObjRouteBaseState.equals("")) {
											arguments = new String[13];
											arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.InvalidState";
											arguments[1]  = "5";
											arguments[2]  = "Policy";
											arguments[3]  = sTempStore;
											arguments[4]  = "Type";
											arguments[5]  = sRouteType;
											arguments[6]  = "OType";
											arguments[7]  = sObjType;
											arguments[8]  = "OName";
											arguments[9]  = sObjName;
											arguments[10]  = "ORev";
											arguments[11]  = sObjRev;
											arguments[12]  = "";

											message= mailUtil.getMessage(context,arguments);
											MqlUtil.mqlCommand(context, "notice $1",message);

											return 1;
										}
									}

									// block the content promotion if the state blocking is set to current state.
									// promotion should be blocked for both "Promote connected objects" or notify owner Route.
									if (sObjRouteBaseState.equals(sObjCurrent) && sObjRouteBasePolicy.equals(sObjPolicy) && (UIUtil.isNullOrEmpty(sObjBaseTState) || UIUtil.isNullOrEmpty(sObjRouteBaseTState) || sObjBaseTState.equalsIgnoreCase(sObjRouteBaseTState))) {
										// Set flag if Route still in work
										if (!sObjRouteCurrent.equals(sStateComplete)) {
											sBufInCompleteRoutes.append(sObjRouteType+" ");
											sBufInCompleteRoutes.append(sObjRouteName+" ");
											sBufInCompleteRoutes.append(sObjRouteRev+",");
											// Bug 293332
											bPromoteObject = 0;
										}
									}
								}//for

								if (!"".equals(sBufInCompleteRoutes.toString())) {
									sBufRoutesInProcess.append(sObjType+" ");
									sBufRoutesInProcess.append(sObjName+" ");
									sBufRoutesInProcess.append(sObjRev+" : ");
									if(bPromoteObject != 1) {
										sBufNotPromotedObjects.append(sObjName+" ");
									}
									sBufRoutesInProcess.append(sBufInCompleteRoutes.toString()+"\n");
								}

								// Check if all the signatures are approved
								if (enforceSignature && "FALSE".equals(sIsObjSatisfied)  && !"True".equalsIgnoreCase(isPLMEntity))  {
									sBufObjectsNotSatisfied.append(sObjType+" ");
									sBufObjectsNotSatisfied.append(sObjName+" ");
									sBufObjectsNotSatisfied.append(sObjRev+"\n");
									// Bug 293332
									bPromoteObject = 0;
								}

								if (bPromoteObject == 1) {
									StringList selects = new StringList(2);
									selects.add(DomainConstants.SELECT_CURRENT);
									Map contentInfo = dObject.getInfo(context, selects);
									String currState = (String) contentInfo.get(DomainConstants.SELECT_CURRENT);	
									String targetState = getTargetState(context, physicalId, lObjState, sObjBaseTState,sObjBasePolicy, currState);
									if(UIUtil.isNotNullAndNotEmpty(sObjBaseTState) && UIUtil.isNullOrEmpty(targetState)) {
										isBranchExist = Boolean.FALSE.toString();
									}
									if(currState.equals(sObjCurrent) && UIUtil.isNotNullAndNotEmpty(targetState)){
										if("TRUE".equalsIgnoreCase(isPLMEntity)) {
											RouteIRPCContentPromote.changeMaturityIRPCContents(context, physicalId, currState, targetState);
										}else {
											if(!enforceSignature) {
												RouteIRPCContentPromote.autoApproveSignature(context, sObjId, currState, targetState, sOwner);
											}
											int currentStateIndex = lObjState.indexOf(currState);
											int targetStateIndex = lObjState.indexOf(targetState);
											
											if(currentStateIndex+1 == targetStateIndex) {
											MqlUtil.mqlCommand(context,"promote bus $1", sObjId);
											}else {
												com.matrixone.apps.common.Route.promoteObject(context, dObject, targetState);
										}
									}
									sBufPromotedObjects.append(sObjType+" ");
									sBufPromotedObjects.append(sObjName+" ");
									sBufPromotedObjects.append(sObjRev+"\n");
									if(sBufPromotedObjectsNotification.length() > 0){
										sBufPromotedObjectsNotification.append(", ");
									}
									sBufPromotedObjectsNotification.append(sObjName);
								}
								}
							}//for

							sInCompleteRoutes = sBufInCompleteRoutes.toString();
							sRoutesInProcess  = sBufRoutesInProcess.toString();
							sObjectsNotSatisfied = sBufObjectsNotSatisfied.toString();
							sPromotedObjects = sBufPromotedObjects.toString();
							sNotPromotedObjs = sBufNotPromotedObjects.toString();

							if((sInCompleteRoutes.length() > 0 || sRoutesInProcess.length() > 0 || sBufObjectsNotSatisfied.length() > 0) && sPromotedObjects.length() == 0 ) {
								isContentPromoted = false;
							}
							if ("".equals(sObjectsNotSatisfied) && "".equals(sRoutesInProcess)) {
								if ("".equals(sPromotedObjects)) {
									arguments = new String[3];
									arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.None";
									arguments[1]  = "0";
									arguments[2]  = "";

									sPromotedObjects =mailUtil.getMessage(context,arguments);
								}
								StringList toList = new StringList();
								toList.add(sOwner);
								String[] messageKeys = {"RType","RName","RRev","PromotedObj","RType","RName","RRev"};
								String[] messageValues = {sRouteType,sRouteName,sRouteRev,sPromotedObjects,sRouteType,sRouteName,sRouteRev};
								StringList objectIdList = new StringList();
								objectIdList.add(sRouteId);
								//by cyl 20250512 任务完成的邮件不发
//								mailUtil.sendNotification(context, toList, null, null, "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectComplete", null, null, "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageCompletePromoteConnectedObject", messageKeys, messageValues, objectIdList, null,"both".equalsIgnoreCase(notifyType));
							}
							else {
								isRouteCompleted = false;
								if ("".equals(sRoutesInProcess)) {
									arguments = new String[3];
									arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.None";
									arguments[1]  = "0";
									arguments[2]  = "";

									sRoutesInProcess =mailUtil.getMessage(context,arguments);
								}
								if ("".equals(sObjectsNotSatisfied)) {
									arguments = new String[3];
									arguments[0]  = "emxFramework.ProgramObject.eServicecommonCompleteTask.None";
									arguments[1]  = "0";
									arguments[2]  = "";
									sObjectsNotSatisfied =mailUtil.getMessage(context,arguments);
								}
								StringList toList = new StringList();
								toList.add(sOwner);
								String[] messageKeys = {"RType","RName","RRev","PromotedObj","RInProcess","ONotApproved"};
								String[] messageValues = {sRouteType,sRouteName,sRouteRev,sPromotedObjects,sRoutesInProcess,sObjectsNotSatisfied};
								mailUtil.sendNotification(context, toList, null, null, "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectNotComplete", null, null, "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageNotComplete", messageKeys, messageValues, null, null,"both".equalsIgnoreCase(notifyType));
								Map<String, String> taskAndRouteInfo = new HashMap<>();
								taskAndRouteInfo.put(RouteTaskNotification.ROUTE_ID, sRoutePhysicalId);
								taskAndRouteInfo.put(RouteTaskNotification.ROUTE_NAME, sRouteTitle);
								taskAndRouteInfo.put(RouteTaskNotification.ROUTE_OWNER, sOwner);
								taskAndRouteInfo.put(RouteTaskNotification.SENDER_USER, context.getUser());
								taskAndRouteInfo.put(RouteTaskNotification.PROMOTED_OBJECT, sBufPromotedObjectsNotification.toString());
								taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENTS, sRouteContent3D.toString());
								taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENT_TITLES, sRouteContent3DTitles.toString());
								taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENT_TYPES, sRouteContent3DTypes.toString());
								taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENT_IDS, sRouteContent3DIds.toString());
								taskAndRouteInfo.put(RouteTaskNotification.PROMOTED_OBJECT, sBufPromotedObjectsNotification.toString());
								if("true".equalsIgnoreCase(isPLMEntity)) {
									taskAndRouteInfo.put(RouteTaskNotification.FAILED_CONTENTS, sObjectsNotSatisfied);
								}
								taskAndRouteInfo.put(RouteTaskNotification.INPROCESS_ROUTE, sRoutesInProcess);
								taskAndRouteInfo.put(RouteTaskNotification.NOT_PROMOTED_OBJECT, sNotPromotedObjs);
								RouteTaskNotification notifyObj = RouteTaskNotification.getInstance(context, sRouteContent3DTypes.toString(),modelerType);
								notifyObj.sendConnectedObjectFailed3DNotification(context, taskAndRouteInfo, toList);
							}
						}
						else {

							// if there are any existing routes with Route completion as "Promote connected objects".
							// Code to promote content objects if they were not promoted due to open routes 

							for(int count=0; count < routeContentList.size(); count++) {

								Map object = (Map) routeContentList.get(count);
								String sObjType = (String) object.get(DomainConstants.SELECT_TYPE);
								String sObjName = (String) object.get(DomainConstants.SELECT_NAME);
								String sObjRev = (String) object.get(DomainConstants.SELECT_REVISION);
								String sObjId = (String) object.get(DomainConstants.SELECT_ID);
								String sIsObjSatisfied = (String) object.get("current.satisfied");
								String sObjCurrent = (String) object.get(DomainConstants.SELECT_CURRENT);
								String sObjPolicy = (String) object.get(DomainConstants.SELECT_POLICY);
								String isPLMEntity = (String) object.get(DomainConstants.SELECT_KINDOF_PLMENTITY);
								String physicalId = (String) object.get(DomainConstants.SELECT_PHYSICAL_ID);

								StringList  lObjState=new StringList();
								if(object.get(DomainConstants.SELECT_STATES) instanceof StringList) {
									lObjState = (StringList) object.get(DomainConstants.SELECT_STATES);
								}

								//String lObjState = (String) object.get("state");
								String sObjBaseState = (String) object.get("attribute["+sAttRouteBaseState+"].value");
								String sObjBaseTState = (String) object.get("attribute["+sAttRouteTargetState+"].value");
								String sObjBasePolicy= (String) object.get("attribute["+sAttRouteBasePolicy+"].value");
								int bPromoteObject = 1;
								boolean promoteContent = false;

								if (sObjBaseState.equals("Ad Hoc") && (!sObjBaseState.equals(sObjCurrent) || !sObjBasePolicy.equals(sObjPolicy))) {
									continue;
								} 


								//Modified for Bug No: 293332 and Bug no: 293506
								if(lObjState.indexOf(sObjCurrent) == (lObjState.size()-1)) {
									continue;
								}

								objectSelects = new SelectList();
								objectSelects.add("current");
								objectSelects.add(DomainConstants.SELECT_TYPE);
								objectSelects.add(DomainConstants.SELECT_NAME);
								objectSelects.add(DomainConstants.SELECT_REVISION);
								objectSelects.add("attribute[Route Completion Action]");
								relSelects = new SelectList();
								relSelects.add("attribute["+sAttRouteBaseState+"].value");
								relSelects.add("attribute["+sAttRouteBasePolicy+"].value");
								DomainObject dObject = new DomainObject(sObjId);

								//should retrieve only Route objects
								//Include Route sub_types if applicable, use addPattern()
								Pattern typePattern = new Pattern(DomainObject.TYPE_ROUTE);

								//Modified for Bug No: 293332 and Bug no: 293506
								MapList ObjectList= dObject.getRelatedObjects(context,
										sRelObjectRoute,
										typePattern.getPattern(),//"*",
										objectSelects,
										relSelects,
										false,
										true,
										(short)1,
										"",
										"");

								// Check for each object if there is any route which is not complete							

								for(int i=0; i<ObjectList.size() ; i++) {
									Map objectsMap = (Map)ObjectList.get(i);
									String sObjRouteBaseState =(String) objectsMap.get("attribute["+sAttRouteBaseState+"].value");
									String targetState =(String) objectsMap.get("attribute["+sAttRouteTargetState+"].value");
									String sObjRouteBasePolicy =(String) objectsMap.get("attribute["+sAttRouteBasePolicy+"].value");
									String sObjRouteType =(String) objectsMap.get(DomainConstants.SELECT_TYPE);
									String sObjRouteName =(String) objectsMap.get(DomainConstants.SELECT_NAME);
									String sObjRouteRev =(String) objectsMap.get(DomainConstants.SELECT_REVISION);
									String sObjRouteAction =(String) objectsMap.get("attribute[Route Completion Action]");
									String sObjRouteCurrent =(String) objectsMap.get("current");

									if (sObjRouteBaseState.equals("")) {
										sObjRouteBaseState = "Ad Hoc";
									}
									String policyName = sObjRouteBasePolicy;
									sObjRouteBasePolicy= PropertyUtil.getSchemaProperty(context,sObjRouteBasePolicy);
									sObjRouteBaseState = FrameworkUtil.lookupStateName(context, sObjRouteBasePolicy, sObjRouteBaseState);


									// block the content promotion if the state blocking is set to current state.
									// promotion should be blocked for both "Promote connected objects" or notify owner Route.
									if (sObjRouteBaseState != null && sObjRouteBaseState.equals(sObjCurrent) && sObjRouteBasePolicy.equals(sObjPolicy)) {
										// Set flag if Route still in work
										if (!sObjRouteCurrent.equals(sStateComplete)) {
											sBufInCompleteRoutes.append(sObjRouteType+" ");
											sBufInCompleteRoutes.append(sObjRouteName+" ");
											sBufInCompleteRoutes.append(sObjRouteRev+",");
											// Bug 293332
											bPromoteObject = 0;
										}
										if (sObjRouteCurrent.equals(sStateComplete) && "Promote Connected Object".equals(sObjRouteAction)) {
											promoteContent = true;
										}
									}									

								}//for


								if (bPromoteObject == 1 && promoteContent == true) {
									String currState = dObject.getInfo(context, "current");
									sObjBasePolicy= PropertyUtil.getSchemaProperty(context,sObjBasePolicy);
									String targetState = getTargetState(context, physicalId, lObjState, sObjBaseTState,sObjBasePolicy, currState);
									if(UIUtil.isNotNullAndNotEmpty(sObjBaseTState) && UIUtil.isNullOrEmpty(targetState)) {
										isBranchExist = Boolean.FALSE.toString();
									}
									if(currState.equals(sObjCurrent) && UIUtil.isNotNullAndNotEmpty(targetState)){
										if("TRUE".equalsIgnoreCase(isPLMEntity)) {
											RouteIRPCContentPromote.changeMaturityIRPCContents(context, physicalId, currState, targetState);
										}else {
											if(!enforceSignature) {												
												RouteIRPCContentPromote.autoApproveSignature(context, sObjId, currState, targetState, sOwner);
											}
											
											int currentStateIndex = lObjState.indexOf(currState);
											int targetStateIndex = lObjState.indexOf(targetState);
											
											if(currentStateIndex+1 == targetStateIndex) {
											MqlUtil.mqlCommand(context,"promote bus $1", sObjId);
											}else {
												com.matrixone.apps.common.Route.promoteObject(context, dObject, targetState);
										}
									}
									sBufPromotedObjects.append(sObjType+" ");
									sBufPromotedObjects.append(sObjName+" ");
									sBufPromotedObjects.append(sObjRev+"\n");
									if(sBufPromotedObjectsNotification.length() > 0){
										sBufPromotedObjectsNotification.append(", ");
									}
									sBufPromotedObjectsNotification.append(sObjName);
										
									}
								}
							}//for

							sInCompleteRoutes = sBufInCompleteRoutes.toString();							
							sPromotedObjects = sBufPromotedObjects.toString();

							if(sInCompleteRoutes.length() > 0 && sPromotedObjects.length() == 0 ) {
								isContentPromoted = false;
							}
							if (!"".equals(sPromotedObjects) && isContentPromoted == true) {

								StringList toList = new StringList();
								toList.add(sOwner);
								String[] messageKeys = {"RType","RName","RRev","PromotedObj","RType","RName","RRev"};
								String[] messageValues = {sRouteType,sRouteName,sRouteRev,sPromotedObjects,sRouteType,sRouteName,sRouteRev};
								StringList objectIdList = new StringList();
								objectIdList.add(sRouteId);
								//by cyl 20250512 任务完成的邮件不发
//								mailUtil.sendNotification(context, toList, null, null, "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectComplete", null, null, "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageCompletePromoteConnectedObject", messageKeys, messageValues, objectIdList, null,"both".equalsIgnoreCase(notifyType));
							}		

							//Content promotion code ends here ****************************************

							String routeSymbolicName = FrameworkUtil.getAliasForAdmin(context, "type", Route.getType(context), true);
							String mappedTreeName = UIMenu.getTypeToTreeNameMapping(context, routeSymbolicName);
							String[] treeMenu = {mappedTreeName};
							mailUtil.setTreeMenuName(context, treeMenu);
							// Added for the bug no 335211 - Begin
							String oldagentName=mailUtil.getAgentName(context,args);
							String user=context.getUser();
							mailUtil.setAgentName(context,new String[]{user});
							//mailUtil.sendNotificationToUser(context,arguments);
							StringList toList = new StringList();
							toList.add(sOwner);
							String[] messageKeys = {"RType","RName","RRev"};
							String[] messageValues = {sRouteType,sRouteName,sRouteRev};
							StringList objectList = new StringList();
							objectList.add(sRouteId);
							mailUtil.sendNotification(context, toList, null, null, "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectRouteComplete", null, null, "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageRouteComplete", messageKeys, messageValues, objectList, null,"both".equalsIgnoreCase(notifyType));
							mailUtil.setAgentName(context,new String[]{oldagentName});
							// Added for the bug no 335211 - Ends
						}
						Map payload = new HashMap();
						payload.put("objectId", sRouteId);
						payload.put("notifyType", notifyType);
						payload.put("notificationName", "APPObjectRouteCompletedEvent");
						JPO.invoke(context, "emxNotificationUtil", null, "objectNotificationFromMap", JPO.packArgs(payload));
					}
				}
				
				if(subRouteToBeDeleted.size()>0) {
					for(String subRouteId: subRouteToBeDeleted) {
						com.matrixone.apps.common.Route.deleteRouteAndSubRoutes(context, subRouteId);
					}
				}
			}
			sendNotification(context, sRouteActionOfTask, ObjectId, sTaskPhysicalId,sRouteId,sRoutePhysicalId, (String)mapInfo.get(DomainConstants.SELECT_OWNER), sApprovalStatus,  sRouteName,  sLastName,  sFirstName,  sPerson, sType, sName,sTaskTitle, sRev, sRouteType, sRouteRev, dislpayCompletionDateUsersTZ, sComments,sReviewersComments, sRouteLink, sInboxTaskLink, routeContentList.size(), sRouteContent, sRouteContent3DIds,sRouteContent3DTitles,sRouteContent3DTypes,sRouteContentDescription,sRouteContent3D.toString(),sReviewTask,sTaskAssignee,sRouteTitle,modelerType,sTaskName);
			MqlUtil.mqlCommand(context, "unset env $1  $2", "TITLE", sTaskTitle);
			if(isRouteCompleted){
				Map<String, String> taskAndRouteInfo = new HashMap<>();
				taskAndRouteInfo.put(RouteTaskNotification.ROUTE_ID, sRoutePhysicalId);
				taskAndRouteInfo.put(RouteTaskNotification.ROUTE_NAME, sRouteTitle);
				taskAndRouteInfo.put(RouteTaskNotification.ROUTE_OWNER, sOwner);
				taskAndRouteInfo.put(RouteTaskNotification.TASK_ID, sTaskPhysicalId);
				taskAndRouteInfo.put(RouteTaskNotification.TASK_NAME, sTaskTitle);
				taskAndRouteInfo.put(RouteTaskNotification.TASK_ACTION, sRouteActionOfTask);
				taskAndRouteInfo.put(RouteTaskNotification.TASK_ASSIGNEE, sPerson);
				taskAndRouteInfo.put(RouteTaskNotification.SENDER_USER, contextUser);
				taskAndRouteInfo.put(RouteTaskNotification.PROMOTED_OBJECT, sBufPromotedObjectsNotification.toString());
				taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENTS, sRouteContent3D.toString());
				taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENT_TITLES, sRouteContent3DTitles.toString());
				taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENT_TYPES, sRouteContent3DTypes.toString());
				taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENT_IDS, sRouteContent3DIds.toString());
				
				RouteTaskNotification notifyObj = RouteTaskNotification.getInstance(context, sRouteContent3DTypes.toString(),modelerType);
				List<String> toList = new ArrayList<String>();
				toList.add(sOwner);
				notifyObj.sendRouteCompletion3DNotification(context, taskAndRouteInfo, toList);
				StringList subscribersList = SubscriptionUtil.getSubscribersList(context,  sRouteId, "APPObjectRouteCompletedEvent","object", true);
				toList = new ArrayList<>();
				for(String personName : subscribersList){
					if(personName.contains("|")){
						toList.add(personName.substring(0,personName.indexOf("|")));
					}
				}
				if(toList.size() > 0 ) {
					notifyObj.sendPublishEvent3DNotification(context, taskAndRouteInfo, "Route Completed", null, toList);
				}

				com.matrixone.apps.common.Route.createAndStartRouteFromLinkRouteTemplate(context, sRoutePhysicalId, sOwner,isContentPromoted);
				String route_id = com.matrixone.apps.common.Route.getNewRouteId(context);
				if(UIUtil.isNotNullAndNotEmpty(route_id)) {
					try {
						ContextUtil.pushContext(context, sOwner, null, context.getVault().getName());
					com.matrixone.apps.common.Route routeObj = new Route(route_id);
					routeObj.addRouteContentsFromPreviousRoute(context, sRouteId);
				}
					finally {
						ContextUtil.popContext(context);
					}
				}
			}
		} catch(Exception e) {
			if(e.getMessage().toString().contains("promote business object failed")) {
				Locale strLocale = context.getLocale();
				String strDateError = EnoviaResourceBundle.getProperty(context,
						"emxComponentsStringResource",
						strLocale,
						"emxComponents.Common.PromoteConnectObjectFailed");
				String errorMsg = e.getMessage().toString();
				errorMsg = errorMsg.replaceAll("promote business object failed", strDateError + " : ");				
				errorMsg = errorMsg.replaceAll("System Error: #5000001:", "").replaceAll("java.lang.Exception:", "").replaceAll("Error: #1900068:", "");
				//For IR-1050623 
				if(errorMsg.contains("CfgException(ErrorDuringSave)")) {
					errorMsg = errorMsg.substring(errorMsg.lastIndexOf("CfgException(ErrorDuringSave) :")+"CfgException(ErrorDuringSave) :".length());
				}
				emxContextUtil_mxJPO.mqlNotice(context, errorMsg);
			} else if(e.getMessage().toString().contains("emxComponents.Common.InactiveRouteOwner")){
				String inactiveOwnerMsg= EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),
						"emxComponents.Common.InactiveRouteOwner");
				emxContextUtil_mxJPO.mqlNotice(context,inactiveOwnerMsg);
			} else {
				throw e;
			}
		} finally {
			if(UIUtil.isNotNullAndNotEmpty(routeId)) {
				Route.updateRouteActivityStatus(context, routeId);
			}

			ShadowAgent.popContext(context,null);
		}

		return 0;
	}// end of method
	
	private static List<String> getEKLArchiveTemplateErrorList(Context context){
		List<String> langs= Arrays.asList("en", "de", "fr", "ja", "ko", "ru", "zh", "es", "it", "cs", "pl", "pt-br" );
		List<String> errorMsgList= new ArrayList<>();
		for(String lang: langs) {
			String archiveErrorMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(lang), "emxComponents.EKLRouteMgmt.ArchiveTemplateTaskRejectErrorMsg");
			errorMsgList.add(archiveErrorMsg);
		}
		return errorMsgList;
	}

	private static void getSubRouteDetails(StringList subRouteToBeDeleted, Map objectDetails) {
		StringList sbrouteIds = new StringList();
		StringList sbrouteStatus =  new StringList();
		Object resultObj = objectDetails.get(SELECT_SUB_ROUTE_ID);
		if(resultObj != null ) {
			if(resultObj instanceof StringList) {
				sbrouteIds = (StringList)resultObj;
				sbrouteStatus= (StringList)objectDetails.get(SELECT_SUB_ROUTE_STATUS);
			}else {
				sbrouteIds.add((String)resultObj);
				sbrouteStatus.add((String)objectDetails.get(SELECT_SUB_ROUTE_STATUS));
			}
		}
		
		if(sbrouteIds.size() >0) {
			for(int index=0;index<sbrouteIds.size();index++) {
				if(!"Finished".equalsIgnoreCase(sbrouteStatus.get(index))) {
					subRouteToBeDeleted.add(sbrouteIds.get(index));
				}
			}
		}
	}

	private static String getTargetState(Context context, String physicalId, StringList lObjState,
			String sObjBaseTState, String sObjBasePolicy, String currState) throws FrameworkException, Exception {
		String targetState = null;
		if(UIUtil.isNotNullAndNotEmpty(sObjBaseTState)) {
			targetState = FrameworkUtil.lookupStateName(context,sObjBasePolicy, sObjBaseTState);
			if(UIUtil.isNotNullAndNotEmpty(targetState)) {
				// if data is corrupted. if target state is not set but blocking state is modified (added because route can be modified in C&A app)  Or if branch is removed from the system.
				Map<String, LinkedList<String>>  branchesFromCurrent = AccessControlUtility.getBranches(context, physicalId, currState, sObjBasePolicy);
				LinkedList<String> allowedBranches = branchesFromCurrent.get(currState);
				if(!allowedBranches.contains(targetState)){
					targetState = "";
				}
			}
		}else {
			targetState =AccessControlUtility.getNextState(context, physicalId, lObjState, currState);
		}
		return targetState;
	}

	private static void sendNotification(Context context, String sRouteActionOfTask, String ObjectId,String sTaskPhysicalId ,String sRouteId,String sRoutePhysicalId,String routeOwner,String sApprovalStatus, String sRouteName, String sLastName, String sFirstName, String sPerson,String sType,String sName,String sTaskTitle,String sRev,String sRouteType,String sRouteRev, String dislpayCompletionDateUsersTZ, String sComments,String sReviewersComments, String sRouteLink, String sInboxTaskLink, int routeContentListSize, StringBuffer sRouteContent,StringBuffer sRouteContentIds, StringBuffer sRouteContentTitles, StringBuffer sRouteContentTypes, StringBuffer sRouteContentDescription,String sRouteContent3D,String sReviewTask,String sTaskAssignee, String sRouteTitle, String resourceReference,  String sTaskName) throws Exception {
		//20260814 update by ljr JFESOReview审批任务完成后，OOTB通知仅保留站内消息，不再向ESO审核员和总工发送外部邮件。
		boolean isESOReview = sRouteContentTypes.toString().contains("JFESOReview");
		String strTranslatedRouteActionOfTask = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Range.Route_Action." + sRouteActionOfTask.replace(" ", "_"), new Locale(context.getSession().getLanguage()));
		String strTranslatedSType = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Type." + sType.replace(" ", "_"), new Locale(context.getSession().getLanguage()));
		String strTranslatedRouteType = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Type." + sRouteType.replace(" ", "_"), new Locale(context.getSession().getLanguage()));	   
		String mailEnabled = EnoviaResourceBundle.getProperty(context,"emxFramework.Routes.EnableMails");
		String notifyType = "iconmail";
		if("true".equalsIgnoreCase(mailEnabled)){
			notifyType = "both";
		}
		if (isESOReview) {
			notifyType = "iconmail";
		}
		String logged_In_User = PropertyUtil.getGlobalRPEValue(context,ContextUtil.MX_LOGGED_IN_USER_NAME);
		if("User Agent".equalsIgnoreCase(logged_In_User)) {
			logged_In_User = sPerson;
		}
		Map<String, String> taskAndRouteInfo = new HashMap<>();
		taskAndRouteInfo.put(RouteTaskNotification.ROUTE_ID, sRoutePhysicalId);
		taskAndRouteInfo.put(RouteTaskNotification.ROUTE_NAME, sRouteTitle);
		taskAndRouteInfo.put(RouteTaskNotification.ROUTE_OWNER, routeOwner);
		taskAndRouteInfo.put(RouteTaskNotification.TASK_ID, sTaskPhysicalId);
		taskAndRouteInfo.put(RouteTaskNotification.TASK_NAME, sTaskTitle);
		taskAndRouteInfo.put(RouteTaskNotification.TASK_ACTION, sRouteActionOfTask);
		taskAndRouteInfo.put(RouteTaskNotification.TASK_ASSIGNEE, sPerson);
		taskAndRouteInfo.put(RouteTaskNotification.SENDER_USER, sPerson);
		taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENTS, sRouteContent3D);
		taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENT_IDS, sRouteContentIds.toString());
		taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENT_TITLES, sRouteContentTitles.toString());
		taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENT_TYPES, sRouteContentTypes.toString());
		taskAndRouteInfo.put(RouteTaskNotification.TASK_ATTACHMENTS, InboxTask.getTaskAttachment(context, sTaskPhysicalId));
		
		String currentTaskContent = InboxTask.getTaskContent(context, sRouteId, sTaskName);
		taskAndRouteInfo.put(RouteTaskNotification.TASK_CONTENTS, currentTaskContent);
		
		RouteTaskNotification notifyObj = RouteTaskNotification.getInstance(context, sRouteContentTypes.toString(),resourceReference);
		Map payload = new HashMap();
		if(routeContentListSize > 0) {
			payload.put("tableHeader", "emxFramework.ProgramObject.eServicecommonCompleteTask.TableWithContentHeader");
			payload.put("tableRow", "emxFramework.ProgramObject.eServicecommonCompleteTask.TableWithContentData");
			DomainObject inboxTask = DomainObject.newInstance(context, sTaskPhysicalId);
			String strTaskTitle = inboxTask.getInfo(context, DomainConstants.SELECT_ATTRIBUTE_TITLE);
			// mod by chenyan 修改审核任务完成是邮件的表格列 2026/03/04
//			String[] tableKeys = {"TaskType", "RouteName", "TaskName", "TaskOwner", "CompletionDate", "Comments", "Content", "ContentDescription"};
//			String[] tableValues = {strTranslatedRouteActionOfTask, sRouteLink, sInboxTaskLink, sPerson, dislpayCompletionDateUsersTZ, sComments, sRouteContent.toString(), sRouteContentDescription.toString()};
			String[] tableKeys = {"TaskType", "RouteName", "RouteTitle","TaskName", "TaskOwner", "CompletionDate", "Comments", "Content", "ContentTitle","ContentDescription"};
			String[] tableValues = {strTranslatedRouteActionOfTask, sRouteLink,strTaskTitle, sInboxTaskLink, sPerson, dislpayCompletionDateUsersTZ, sComments, sRouteContent.toString(),sRouteContentTitles.toString(), sRouteContentDescription.toString()};
			payload.put("tableRowKeys", tableKeys);
			payload.put("tableRowValues", tableValues);
		} else {
			payload.put("tableHeader", "emxFramework.ProgramObject.eServicecommonCompleteTask.TableHeader");
			payload.put("tableRow", "emxFramework.ProgramObject.eServicecommonCompleteTask.TableData");
			String[] tableKeys = {"TaskType", "RouteName", "TaskName", "TaskOwner", "CompletionDate", "Comments"};
			String[] tableValues = {strTranslatedRouteActionOfTask, sRouteLink, sInboxTaskLink, sPerson, dislpayCompletionDateUsersTZ, sComments};
			payload.put("tableRowKeys", tableKeys);
			payload.put("tableRowValues", tableValues);
		}
		
		if("Auto Complete".equals(sApprovalStatus)) {
			List<String> toList = new ArrayList<String>();
			
			if("USERGROUPOWNER".equals(sTaskAssignee)) {
				String mailList = FrameworkUtil.getPersonsMailListFromUserGroup(context, taskAndRouteInfo.get("TASK_ASSIGNEE"));
				List<String> tempList = new ArrayList<String>(Arrays.asList(mailList.split(",")));
				toList.addAll(tempList);
			}else {
				toList.add(taskAndRouteInfo.get("TASK_ASSIGNEE"));
			}
			if(!toList.contains(routeOwner)) {
				toList.add(routeOwner);
			}
			
			notifyObj.sendTaskComplete3DNotification(context, taskAndRouteInfo, toList);
			
		}else if ("Approve".equals(sRouteActionOfTask)) {
			if ("Reject".equals(sApprovalStatus)) {
				payload.put("subject", "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectReject");
				payload.put("message", "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageReject");
				String[] messageKeys = {"name", "IBType", "IBName", "IBRev", "RType", "RName", "RRev", "TComments"};
				String[] messageValues = {sLastName + "," + sFirstName+""+" (" + sPerson + ")",
						strTranslatedSType, sName, sRev, strTranslatedRouteType, sRouteName, sRouteRev, sComments};
				payload.put("messageKeys", messageKeys);
				payload.put("messageValues", messageValues);
				String oldAgentName = mailUtil.getAgentName(context, new String[] {});
				mailUtil.setAgentName(context, new String[] { sPerson });
				emxNotificationUtil_mxJPO.objectNotification(context, ObjectId, "APPObjectRouteTaskRejectedEvent", payload,notifyType);
				mailUtil.setAgentName(context, new String[] { oldAgentName });
				HashMap hmParam		= new HashMap();
				hmParam.put("id", ObjectId);
				String[] args1 = (String[])JPO.packArgs(hmParam);
				StringList tolist =  (StringList) JPO.invoke(context, "emxInboxTaskNotificationBase", null, "getRejectionMailList", args1, StringList.class);
				tolist.add(routeOwner);
				taskAndRouteInfo.put(RouteTaskNotification.REJECT_COMMENTS, sComments);
				notifyObj.sendTaskRejection3DNotification(context, taskAndRouteInfo, tolist);
			}
			else if ("Approve".equals(sApprovalStatus)) {
				payload.put("subject", "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectApprove");
				payload.put("message", "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageApprove");
				String[] messageKeys = {"name", "IBType", "IBName", "IBRev", "RType", "RName", "RRev"};
				String[] messageValues = {sLastName + "," + sFirstName+""+" (" + sPerson + ")",
						strTranslatedSType, sName, sRev, strTranslatedRouteType, sRouteName, sRouteRev};
				payload.put("messageKeys", messageKeys);
				payload.put("messageValues", messageValues);

				String oldAgentName = mailUtil.getAgentName(context, new String[] {});
				mailUtil.setAgentName(context, new String[] { sPerson });
				new emxNotificationUtilBase_mxJPO(context,null).objectNotification(context, ObjectId, "APPObjectRouteTaskApprovedEvent", payload,notifyType);
				mailUtil.setAgentName(context, new String[] { oldAgentName });
				String sActualCompletionDate= new DomainObject(ObjectId).getInfo(context,"attribute["+sAttActualCompletionDate+"]");
				List<String> toList = new ArrayList<>();
				toList.add(routeOwner);

				if("Yes".equalsIgnoreCase(sReviewTask)){
					taskAndRouteInfo.put(RouteTaskNotification.REVIEW_COMMENTS, sReviewersComments);
					taskAndRouteInfo.put(RouteTaskNotification.REVIEW_STATUS, "completed");
					taskAndRouteInfo.put(RouteTaskNotification.TASK_ASSIGNEE, sTaskAssignee);
					taskAndRouteInfo.put(RouteTaskNotification.SENDER_USER, logged_In_User);
					notifyObj.sendTaskReviewCompleted3DNotification(context, taskAndRouteInfo);
					StringList toSelectList = new StringList();
					toSelectList.add("$<owner>");
					emxNotificationUtilBase_mxJPO.createNotification(context, ObjectId,  new StringList(),  new StringList(), "emxComponents.Notification.NotifyTaskAsignee.SubjectReviewCompleted", "emxComponents.Notification.NotifyTaskAsignee.MessageReviewCompletedPromote",  new StringList(), "emxComponentsStringResource", toSelectList, new StringList(), "", "", "", "", "",notifyType);
				}else {
					taskAndRouteInfo.put(RouteTaskNotification.APPROVE_COMMENTS, sComments);
					notifyObj.sendTaskApproval3DNotification(context, taskAndRouteInfo, toList);
				}
			}else if("Abstain".equals(sApprovalStatus)) {
				payload.put("subject", "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectAbstain");
				payload.put("message", "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageAbstain");
				String[] messageKeys = {"name", "IBType", "IBName", "IBRev", "RType", "RName", "RRev"};
				String[] messageValues = {sLastName + "," + sFirstName+""+" (" + sPerson + ")",
						strTranslatedSType, sName, sRev, strTranslatedRouteType, sRouteName, sRouteRev};
				payload.put("messageKeys", messageKeys);
				payload.put("messageValues", messageValues);

				String oldAgentName = mailUtil.getAgentName(context, new String[] {});
				mailUtil.setAgentName(context, new String[] { sPerson });
				new emxNotificationUtilBase_mxJPO(context,null).objectNotification(context, ObjectId, "APPObjectRouteTaskApprovedEvent", payload,notifyType);
				mailUtil.setAgentName(context, new String[] { oldAgentName });
				String sActualCompletionDate= new DomainObject(ObjectId).getInfo(context,"attribute["+sAttActualCompletionDate+"]");
				List<String> toList = new ArrayList<>();
				toList.add(routeOwner);
				if("Yes".equalsIgnoreCase(sReviewTask)){
					taskAndRouteInfo.put(RouteTaskNotification.REVIEW_COMMENTS, sReviewersComments);
					taskAndRouteInfo.put(RouteTaskNotification.REVIEW_STATUS, "completed");
					taskAndRouteInfo.put(RouteTaskNotification.TASK_ASSIGNEE, sTaskAssignee);
					notifyObj.sendTaskReviewCompleted3DNotification(context, taskAndRouteInfo);
					StringList toSelectList = new StringList();
					toSelectList.add("$<owner>");
					emxNotificationUtilBase_mxJPO.createNotification(context, ObjectId,  new StringList(),  new StringList(), "emxComponents.Notification.NotifyTaskAsignee.SubjectReviewCompleted", "emxComponents.Notification.NotifyTaskAsignee.MessageReviewCompletedPromote",  new StringList(), "emxComponentsStringResource", toSelectList, new StringList(), "", "", "", "", "",notifyType);
				}else {
					taskAndRouteInfo.put(RouteTaskNotification.APPROVE_COMMENTS, sComments);
					notifyObj.sendTaskAbstain3DNotification(context, taskAndRouteInfo, toList);
				}
			}
		} else if("Comment".equals(sRouteActionOfTask)){
			payload.put("subject", "emxFramework.ProgramObject.eServicecommonCompleteTask.SubjectCommented");
			payload.put("message", "emxFramework.ProgramObject.eServicecommonCompleteTask.MessageCommented");
			String[] messageKeys = {"name", "IBType", "IBName", "IBRev", "RType", "RName", "RRev"};
			String[] messageValues = {sLastName + "," + sFirstName+""+" (" + sPerson + ")",
					strTranslatedSType, sName, sRev, strTranslatedRouteType, sRouteName, sRouteRev};
			payload.put("messageKeys", messageKeys);
			payload.put("messageValues", messageValues);
			String oldAgentName = mailUtil.getAgentName(context, new String[] {});
			mailUtil.setAgentName(context, new String[] { sPerson });
			new emxNotificationUtilBase_mxJPO(context,null).objectNotification(context, ObjectId, "APPObjectRouteTaskApprovedEvent", payload,notifyType);
			mailUtil.setAgentName(context, new String[] { oldAgentName });
			String sActualCompletionDate= new DomainObject(ObjectId).getInfo(context,"attribute["+sAttActualCompletionDate+"]");
			List<String> toList = new ArrayList<>();
			toList.add(routeOwner);
			taskAndRouteInfo.put(RouteTaskNotification.APPROVE_COMMENTS, sComments);
			notifyObj.sendTaskCommented3DNotification(context, taskAndRouteInfo, toList);
		}
	}
	/** below method is used to complete the task if any of the task is rejected and route is stopped.
	 * @param context
	 * @param taskObjectMap
	 * @param pendingTaskList
	 * @param completionDate
	 * @param sRouteId
	 * @param sRouteSequence
	 * @param sRoutePhysicalId
	 * @param sRouteName
	 * @param sRouteOwner
	 * @param sRouteContent3DIds
	 * @param sRouteContent
	 * @param sRouteContent3DTypes
	 * @param sRouteContent3DTitles
	 * @throws Exception
	 */
	private static void completeAssignTaskOnRejection(Context context, Map taskObjectMap, MapList pendingTaskList, String completionDate, String sRouteId, String sRouteSequence, String sRoutePhysicalId, String sRouteName, String sRouteOwner, String sRouteContent3DIds, String sRouteContent, String sRouteContent3DTypes, String sRouteContent3DTitles, String sRouteTitle) throws Exception {
		String groupType = PropertyUtil.getSchemaProperty(context,"type_Group");
		String proxyGoupType = PropertyUtil.getSchemaProperty(context,"type_GroupProxy");
		String  sStateAssignedInboxTask                  = FrameworkUtil.lookupStateName(context, DomainConstants.POLICY_INBOX_TASK ,"state_Assigned");
		String  sStateReviewInboxTask                    = FrameworkUtil.lookupStateName(context, DomainConstants.POLICY_INBOX_TASK ,"state_Review");
		String  sStateCompleteInboxTask                    = FrameworkUtil.lookupStateName(context, DomainConstants.POLICY_INBOX_TASK ,"state_Complete");
		String  sStateCompleteRoute                  = FrameworkUtil.lookupStateName(context, DomainConstants.POLICY_ROUTE ,"state_Complete");
		String sTaskTitle 			 = (String)taskObjectMap.get(InboxTask.SELECT_TITLE);
		if(UIUtil.isNullOrEmpty(sTaskTitle)){
			sTaskTitle = (String)taskObjectMap.get(InboxTask.SELECT_NAME);
		}
		String modelerType = (String)taskObjectMap.get(Route.SELECT_ATTRIBUTE_OBJECT_ROUTE_MODELER_TYPE_WITH_TASK);
		String autoComments = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource", context.getLocale(), "emxFramework.ProgramObject.eServicecommonCompleteTaskOnRejection.AutoCompleteCommment");
		autoComments = autoComments.replace("<TASKNAME>", sTaskTitle);
		Locale strLocale = context.getLocale();
		for( int i=0; i< pendingTaskList.size() ; i++)
		{
			Map objectDetails= (Map) pendingTaskList.get(i);
			String sState =(String) objectDetails.get(DomainConstants.SELECT_CURRENT);
			String currTaskId = (String) objectDetails.get(DomainConstants.SELECT_ID);
			String sRouteNodeID = (String) objectDetails.get("attribute["+sAttRouteNodeID+"]");
			if (sStateAssignedInboxTask.equals(sState) || sStateReviewInboxTask.equals(sState))
			{
				String sRouteNodeConnectionId = (String) objectDetails.get("id");
				Route route = (Route)DomainObject.newInstance(context,sRouteId);
				String orgRelId=route.getRouteNodeRelId(context,sRouteNodeID);
				String[] relId = new String[1];
				relId[0] = orgRelId;
				StringList RelSelects = new SelectList();
				RelSelects.add("attribute["+sAttRouteSequence+"]");
				MapList relToCheckMapList = DomainRelationship.getInfo(context, relId, RelSelects);
				Map relToCheck = (Map) relToCheckMapList.get(0);
				String curRouteSequence = (String)relToCheck.get("attribute["+sAttRouteSequence+"]");
				if (curRouteSequence.equalsIgnoreCase(sRouteSequence)) {
					String sTaskId = (String)objectDetails.get(DomainConstants.SELECT_ID);
					DomainObject inboxTaskToComplete = new DomainObject(sTaskId);
					inboxTaskToComplete.setAttributeValue(context, sAttActualCompletionDate, completionDate);
					inboxTaskToComplete.setAttributeValue(context, sAttApprovalStatus, "None");
					inboxTaskToComplete.setAttributeValue(context, sAttComments, autoComments);
					MqlUtil.mqlCommand(context, "set env $1  $2", "MX_TASK_AUTO_COMPLETE", "true");
					try{
						inboxTaskToComplete.setState(context, sStateCompleteInboxTask);
						String type= (String) objectDetails.get(DomainConstants.SELECT_TYPE) ;
						String name= (String) objectDetails.get(DomainConstants.SELECT_NAME);
						String revision= (String) objectDetails.get(DomainConstants.SELECT_REVISION);
						String title = (String)objectDetails.get(DomainConstants.SELECT_ATTRIBUTE_TITLE);
						String mql1 =  "modify bus $1 add history $2 comment $3";
						String historyEntryAll= " Task :'" +" "+ type +" "+ name+" "+ revision +"|"+ title + "' has been Auto-Completed as "+ (String)taskObjectMap.get(InboxTask.SELECT_TYPE)+" "+ (String)taskObjectMap.get(InboxTask.SELECT_NAME)+" "+(String)taskObjectMap.get(InboxTask.SELECT_REVISION)+ "|" + (String)taskObjectMap.get(InboxTask.SELECT_ATTRIBUTE_TITLE) + "  is complete.";
						MqlUtil.mqlCommand(context, mql1, true, sRouteId, "Reject", historyEntryAll);
					} finally {
						MqlUtil.mqlCommand(context, "set env $1  $2", "MX_TASK_AUTO_COMPLETE", "false");
					}
					String toListOfPerson = "";
					if(proxyGoupType.equals((String) objectDetails.get(SELECT_TASK_ASSIGNEE_TYPE)) || groupType.equals((String) objectDetails.get(SELECT_TASK_ASSIGNEE_TYPE)) ){
						toListOfPerson = FrameworkUtil.getPersonsMailListFromUserGroup(context, (String) objectDetails.get(SELECT_TASK_ASSIGNEE_ID));
					}else {
						toListOfPerson = (String) objectDetails.get(DomainConstants.SELECT_OWNER);
					}
					String[] toList = toListOfPerson.split(",");
					List<String> toListPerson = new ArrayList<String>();
					for(String userName:toListOfPerson.split(",")) {
						toListPerson.add(userName);
					}
					String isUserGroupTask = "No";
					String userGroupTitle = null;
					if(proxyGoupType.equals((String) objectDetails.get(SELECT_TASK_ASSIGNEE_TYPE)) || groupType.equals((String) objectDetails.get(SELECT_TASK_ASSIGNEE_TYPE)) ){
						isUserGroupTask = "Yes";
						userGroupTitle = (String) objectDetails.get(SELECT_TASK_ASSIGNEE_TITLE);
						if(UIUtil.isNullOrEmpty(userGroupTitle)){
							userGroupTitle = (String) objectDetails.get(SELECT_TASK_ASSIGNEE_NAME);
						}
					}
					String completedTaskTitle = (String) objectDetails.get(InboxTask.SELECT_TITLE);
					if(UIUtil.isNullOrEmpty(completedTaskTitle)){
						completedTaskTitle = (String) objectDetails.get(DomainConstants.SELECT_NAME);
					}
					Map<String, String> taskAndRouteInfo = new HashMap<String, String>();
					taskAndRouteInfo.put(RouteTaskNotification.ROUTE_ID, sRoutePhysicalId);
					taskAndRouteInfo.put(RouteTaskNotification.ROUTE_NAME, sRouteTitle);
					taskAndRouteInfo.put(RouteTaskNotification.ROUTE_OWNER, sRouteOwner);
					taskAndRouteInfo.put(RouteTaskNotification.TASK_ID, sTaskId);
					taskAndRouteInfo.put(RouteTaskNotification.COMPLETED_TASK_NAME, completedTaskTitle);
					taskAndRouteInfo.put(RouteTaskNotification.TASK_NAME, sTaskTitle);
					taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENT_IDS, sRouteContent3DIds);
					taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENTS, sRouteContent);
					taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENT_TYPES, sRouteContent3DTypes);
					taskAndRouteInfo.put(RouteTaskNotification.ROUTE_CONTENT_TITLES, sRouteContent3DTitles);
					taskAndRouteInfo.put(RouteTaskNotification.SENDER_USER, context.getUser());
					taskAndRouteInfo.put(RouteTaskNotification.IS_USER_GROUP_TASK, isUserGroupTask);
					taskAndRouteInfo.put(RouteTaskNotification.TASK_ATTACHMENTS, InboxTask.getTaskAttachment(context, sTaskId));
					if("Yes".equalsIgnoreCase(isUserGroupTask)){
						taskAndRouteInfo.put(RouteTaskNotification.TASK_ASSIGNEE, userGroupTitle);
						taskAndRouteInfo.put(RouteTaskNotification.ASSIGNEE_TYPE, "usergroup");
					}else {
						taskAndRouteInfo.put(RouteTaskNotification.TASK_ASSIGNEE, (String) objectDetails.get(SELECT_TASK_ASSIGNEE_NAME));
						taskAndRouteInfo.put(RouteTaskNotification.ASSIGNEE_TYPE, "person");
					}
					
					String currentTaskContent = InboxTask.getTaskContent(context, sRouteId, (String)taskObjectMap.get(DomainConstants.SELECT_NAME));
					taskAndRouteInfo.put(RouteTaskNotification.TASK_CONTENTS, currentTaskContent);
					
					RouteTaskNotification notifyObj = RouteTaskNotification.getInstance(context, sRouteContent3DTypes.toString(),modelerType);
					notifyObj.sendAutoTaskCompletion3DNotificationOnTaskRejection(context, taskAndRouteInfo, toListPerson);
					
					StringList sbrouteIds = new StringList();
					getSubRouteDetails(sbrouteIds, objectDetails);
					
					for(String subRouteId: sbrouteIds) {
						Route.deleteRouteAndSubRoutes(context, subRouteId);
					}
				}
			}
		}
	}	  
}// eof class
