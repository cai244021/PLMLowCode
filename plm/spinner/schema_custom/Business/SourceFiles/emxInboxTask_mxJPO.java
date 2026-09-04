/*
 *  emxInboxTask.java
 *
 * Copyright (c) 1992-2020 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import com.matrixone.apps.common.Route;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.*;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;

import java.lang.*;
import java.util.*;

import static com.matrixone.apps.domain.DomainConstants.*;
import static com.matrixone.apps.domain.DomainObject.getAttributeSelect;

/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxInboxTask_mxJPO extends emxInboxTaskBase_mxJPO
{


    private static final String sAttrReviewersComments = PropertyUtil.getSchemaProperty(context1, "attribute_ReviewersComments");
    private static final String sAttrReviewTask = PropertyUtil.getSchemaProperty(context1, "attribute_ReviewTask");
    private static final String sAttrReviewCommentsNeeded = PropertyUtil.getSchemaProperty(context1, "attribute_ReviewCommentsNeeded");
    private static final String sAttrRouteAction = PropertyUtil.getSchemaProperty(context1, "attribute_RouteAction");
    private static final String sAttrScheduledCompletionDate = PropertyUtil.getSchemaProperty(context1, "attribute_ScheduledCompletionDate");
    private static final String sAttrTitle = PropertyUtil.getSchemaProperty(context1, "attribute_Title");
    private static final String selTaskCompletedDate = PropertyUtil.getSchemaProperty(context1, "attribute_ActualCompletionDate");
    private static final String sTypeInboxTask = PropertyUtil.getSchemaProperty(context1, "type_InboxTask");
    private static final String sRelProjectTask = PropertyUtil.getSchemaProperty(context1, "relationship_ProjectTask");
    private static final String sRelRouteTask = PropertyUtil.getSchemaProperty(context1, "relationship_RouteTask");
    private static final String sRelRouteScope = PropertyUtil.getSchemaProperty(context1, "relationship_RouteScope");
    private static final String sRelObjectRoute = PropertyUtil.getSchemaProperty(context1, "relationship_ObjectRoute");
    private static final String policyTask = PropertyUtil.getSchemaProperty(context1, "policy_InboxTask");
    private static final String strAttrRouteAction = "attribute["+sAttrRouteAction +"]";
    private static final String strAttrCompletionDate ="attribute["+sAttrScheduledCompletionDate+"]";
    private static final String strAttrTitle="attribute["+sAttrTitle+"]";
    private static final String strAttrTaskCompletionDate ="attribute["+selTaskCompletedDate+"]";
    private static final String strAttrTaskApprovalStatus  = getAttributeSelect(DomainObject.ATTRIBUTE_APPROVAL_STATUS);
    private static final String sAttrRouteScope = PropertyUtil.getSchemaProperty(context1, "attribute_RestrictMembers");
    private static final String sAttrRouteOwnerUGChoice = PropertyUtil.getSchemaProperty(context1, "attribute_RouteOwnerUGChoice");
    private static final String sAttrRouteOwnerTask = PropertyUtil.getSchemaProperty(context1, "attribute_RouteOwnerTask");
    private static final String sAttrRouteStatus = PropertyUtil.getSchemaProperty(context1, "attribute_RouteStatus");

    private static String routeIdSelectStr="from["+sRelRouteTask+"].to.id";
    private static String routeTypeSelectStr="from["+sRelRouteTask+"].to.type";
    private static String routeNameSelectStr ="from["+sRelRouteTask+"].to.name";
    private static String routeOwnerSelectStr="from["+sRelRouteTask+"].to.owner";
    private static String objectNameSelectStr="from["+sRelRouteTask+"].to.to["+sRelRouteScope+"].from.name";
    private static String objectTypeSelectStr="from["+sRelRouteTask+"].to.to["+sRelRouteScope+"].from.type";
    private static String objectTitleSelectStr="from["+sRelRouteTask+"].to.to["+sRelRouteScope+"].from."+DomainConstants.SELECT_ATTRIBUTE_TITLE;
    private static String objectIdSelectStr="from["+sRelRouteTask+"].to.to["+sRelRouteScope+"].from.id";
    private static final String routeApprovalStatusSelectStr ="from["+sRelRouteTask+"].to."+Route.SELECT_ROUTE_STATUS ;
    private i18nNow loc = new i18nNow();
    protected String lang=null;
    protected String rsBundle=null;

    private static final String sRelAssignedTask = PropertyUtil.getSchemaProperty(context1, "relationship_AssignedTasks");
    private static final String sRelSubTask = PropertyUtil.getSchemaProperty(context1, "relationship_Subtask");
    private static final String sRelWorkflowTask = PropertyUtil.getSchemaProperty(context1, "relationship_WorkflowTask");
    private static final String sRelWorkflowTaskAssinee = PropertyUtil.getSchemaProperty(context1, "relationship_WorkflowTaskAssignee");
    private static final String sRelWorkflowTaskDeliverable = PropertyUtil.getSchemaProperty(context1, "relationship_TaskDeliverable");

    private static final String workflowIdSelectStr = "to["+sRelWorkflowTask+"].from.id";
    private static final String workflowNameSelectStr = "to["+sRelWorkflowTask+"].from.name";
    private static final String workflowTypeSelectStr = "to["+sRelWorkflowTask+"].from.type";

    private static final String sTypeWorkflowTask = PropertyUtil.getSchemaProperty(context1, "type_WorkflowTask");

    private static final String policyWorkflowTask = PropertyUtil.getSchemaProperty(context1, "policy_WorkflowTask");
    private static final String attrworkFlowDueDate = PropertyUtil.getSchemaProperty(context1, "attribute_DueDate");
    private static final String attrTaskEstinatedFinishDate = PropertyUtil.getSchemaProperty(context1, "attribute_TaskEstimatedFinishDate");
    private static final String attrworkFlowActCompleteDate = PropertyUtil.getSchemaProperty(context1, "attribute_ActualCompletionDate");
    private static final String attrworkFlowInstructions = PropertyUtil.getSchemaProperty(context1, "attribute_Instructions");
    private static final String attrTaskFinishDate = PropertyUtil.getSchemaProperty(context1, "attribute_TaskActualFinishDate");

    private static String strAttrworkFlowDueDate = "attribute[" + attrworkFlowDueDate + "]";
    private static String strAttrTaskEstimatedFinishDate = "attribute[" + attrTaskEstinatedFinishDate + "]";
    private static String strAttrTaskFinishDate = "attribute[" + attrTaskFinishDate + "]";
    private static String strAttrworkFlowCompletinDate = "attribute[" + attrworkFlowActCompleteDate + "]";
    private static final String strAttrRouteOwnerUGChoice ="attribute["+sAttrRouteOwnerUGChoice+"]";
    private static final String strAttrRouteOwnerTask ="attribute["+sAttrRouteOwnerTask+"]";

    private static final String TYPE_INBOX_TASK_STATE_REVIEW = PropertyUtil.getSchemaProperty(context1, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Review");
    private static final String TYPE_INBOX_TASK_STATE_ASSIGNED = PropertyUtil.getSchemaProperty(context1, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Assigned");


    // added for IR - 043921V6R2011
    public static final String  SELECT_TEMPLATE_OWNING_ORG_ID =  "from["+ RELATIONSHIP_ROUTE_TASK + "].to.from[" + RELATIONSHIP_INITIATING_ROUTE_TEMPLATE
            + "].to.to[" + RELATIONSHIP_OWNING_ORGANIZATION + "].from.id";

    public static final String SELECT_ROUTE_NODE_ID = getAttributeSelect(ATTRIBUTE_ROUTE_NODE_ID);
    public static final String SELECT_TASK_ASSIGNEE_CONNECTION = "from[" + RELATIONSHIP_PROJECT_TASK + "].id";
    protected static final String PERSON_WORKSPACE_LEAD_GRANTOR = PropertyUtil.getSchemaProperty(context1, "person_WorkspaceLeadGrantor");
    protected static final String SELECT_TASK_ASSIGNEE_NAME = "from[" + RELATIONSHIP_PROJECT_TASK + "].to.name";
    protected static final String SELECT_TASK_ASSIGNEE_TITLE = "from[" + RELATIONSHIP_PROJECT_TASK + "].to."+DomainConstants.SELECT_ATTRIBUTE_TITLE;
    protected static final String SELECT_TASK_ASSIGNEE_TYPE        = "from[" + RELATIONSHIP_PROJECT_TASK + "].to.type";

    private static final String TASK_PROJECT_ID = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
    private static final String TASK_PROJECT_TYPE = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";
    private static final String TASK_PROJECT_NAME = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.name";
    private static final String TASK_PROJECT_ATTRIBUTE_TITLE = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to."+DomainConstants.SELECT_ATTRIBUTE_TITLE;
    private static final String ROUTE_TASK_PROJECT_ID = "from["+sRelRouteTask+"].to.to["+sRelObjectRoute+"].from."+TASK_PROJECT_ID;
    private static final String ROUTE_TASK_PROJECT_NAME = "from["+sRelRouteTask+"].to.to["+sRelObjectRoute+"].from."+TASK_PROJECT_NAME;
    private static final String ROUTE_TASK_PROJECT_TYPE = "from["+sRelRouteTask+"].to.to["+sRelObjectRoute+"].from."+TASK_PROJECT_TYPE;
    private static final String ROUTE_TASK_PROJECT_ATTRIBUTE_TITLE = "from["+sRelRouteTask+"].to.to["+sRelObjectRoute+"].from."+TASK_PROJECT_ATTRIBUTE_TITLE;
    private static final String ROUTE_PROJECT_ID = "from["+sRelRouteTask+"].to.to["+sRelObjectRoute+"].from.id";
    private static final String ROUTE_PROJECT_NAME = "from["+sRelRouteTask+"].to.to["+sRelObjectRoute+"].from.name";
    private static final String ROUTE_PROJECT_TYPE = "from["+sRelRouteTask+"].to.to["+sRelObjectRoute+"].from.type";
    private static final String ROUTE_PROJECT_Title_NAME = "from["+sRelRouteTask+"].to.to["+sRelObjectRoute+"].from."+DomainConstants.SELECT_ATTRIBUTE_TITLE;
    private static final String SELECT_KINDOF_TASK = "type.kindof[" + TYPE_TASK_MANAGEMENT+ "]";
    private static final String SELECT_KINDOF_WORKFLOW_TASK = "type.kindof[" + sTypeWorkflowTask+ "]";
    private static final String SELECT_KINDOF_INBOX_TASK = "type.kindof[" + sTypeInboxTask+ "]";
    private static String routeScopeSelectStr="from["+sRelRouteTask+"].to.attribute["+sAttrRouteScope +"]";
    private static final String TASK_PROJECT_POLICY = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.policy";
    private static final String POLICY_PROJECT_SPACE_HOLD_CANCEL = PropertyUtil.getSchemaProperty(context1, "policy_ProjectSpaceHoldCancel");

    private static final String sRelTaskDeliverable = PropertyUtil.getSchemaProperty(context1, "relationship_TaskDeliverable");
    private static final String ROUTE_TASK_PROJECT_POLICY = "from["+sRelRouteTask+"].to.to["+sRelObjectRoute+"].from.to["+sRelTaskDeliverable+"].from."+TASK_PROJECT_POLICY;
    private static final String ATTRIBUTE_REQUIRES_ESIGN = PropertyUtil.getSchemaProperty(context1, "attribute_RequiresESign");

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public emxInboxTask_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }


    /**
     * getActiveTasks - gets the list of Tasks in Assigned State
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since 10.5
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getActiveTasks(Context context, String[] args) throws Exception
    {

        String stateInboxTaskAssigned = PropertyUtil.getSchemaProperty(context,"policy", DomainObject.POLICY_INBOX_TASK,"state_Assigned");
        String stateWorkFlowTaskAssigned = PropertyUtil.getSchemaProperty(context,"policy", policyWorkflowTask, "state_Assigned");
        String stateTaskAssign = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROJECT_TASK,"state_Assign");
        String stateTaskActive = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROJECT_TASK,"state_Active");
        String stateTaskReview = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROJECT_TASK,"state_Review");

        //commented for Bug NO:338177
        /* String WBSWhereExp = "(type == 'Task'";
        if(stateTaskReview == null || "".equals(stateTaskReview) || "null".equals(stateTaskReview))
        {
          WBSWhereExp = WBSWhereExp+")";
        } else {
          WBSWhereExp = WBSWhereExp +" && current == " + stateTaskReview + ")";
        }*/
        StringBuffer sbf=new StringBuffer();
        if(stateInboxTaskAssigned != null && !"".equals(stateInboxTaskAssigned))
        {
            sbf.append("(current == "+stateInboxTaskAssigned);
            sbf.append(" && " + "from[" + RELATIONSHIP_ROUTE_TASK + "].to.attribute[" + ATTRIBUTE_ROUTE_STATUS + "] != \"Stopped\") ");
        }
        if(stateWorkFlowTaskAssigned!=null &&!"".equals(stateWorkFlowTaskAssigned))
        {
            if(sbf.length()!=0) {
                sbf.append(" || (");
            }
            sbf.append("type == \"" + sTypeWorkflowTask + "\" && ");
            sbf.append("current == "+stateWorkFlowTaskAssigned + ")");
        }
        if( stateTaskAssign!=null &&!"".equals( stateTaskAssign))
        {
            if(sbf.length()!=0) {
                sbf.append(" || ");
            }
            sbf.append("current == "+ stateTaskAssign);
        }
        if( stateTaskActive!=null &&!"".equals( stateTaskActive))
        {
            if(sbf.length()!=0) {
                sbf.append(" || ");
            }
            sbf.append("current == "+ stateTaskActive);
        }
        if(stateTaskReview!=null &&!"".equals( stateTaskReview))
        {
            if(sbf.length()!=0) {
                sbf.append(" || ");
            }
            sbf.append("current == "+ stateTaskReview);
        }
        // commented for Bug NO:338177
        /*  if(  WBSWhereExp!=null &&!"".equals(  WBSWhereExp))
        {
            if(sbf.length()!=0) {
              sbf.append(" || ");
            }
            sbf.append(WBSWhereExp);
        }*/

        return getTasks(context,sbf.toString()) ;
    }


    /**
     * getMyDeskTasks - gets the list of Tasks the user has access
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getMyDeskTasks(Context context, String[] args)
            throws Exception
    {

        try
        {
            HashMap programMap        = (HashMap) JPO.unpackArgs(args);
            DomainObject taskObject = DomainObject.newInstance(context);
            DomainObject boPerson     = PersonUtil.getPersonObject(context);
            String selRouteTaskUser       = getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_TASK_USER);
            StringList selectTypeStmts = new StringList();
            StringList selectRelStmts  = new StringList();
            selectTypeStmts.add(DomainConstants.SELECT_NAME);
            selectTypeStmts.add(DomainConstants.SELECT_ID);
            selectTypeStmts.add(SELECT_TYPE);
            selectTypeStmts.add(DomainConstants.SELECT_DESCRIPTION);
            selectTypeStmts.add(DomainConstants.SELECT_OWNER);
            selectTypeStmts.add(DomainConstants.SELECT_CURRENT);
            selectTypeStmts.add(strAttrRouteAction);
            selectTypeStmts.add(strAttrCompletionDate);
            selectTypeStmts.add(strAttrTaskCompletionDate);
            selectTypeStmts.add(strAttrTaskApprovalStatus);
            selectTypeStmts.add(getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_ACTION));
            selectTypeStmts.add("attribute[" + DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]");
            selectTypeStmts.add(strAttrTitle);
            selectTypeStmts.add(objectTypeSelectStr);
            selectTypeStmts.add(objectIdSelectStr);
            selectTypeStmts.add(objectNameSelectStr);
            selectTypeStmts.add(objectTitleSelectStr);
            selectTypeStmts.add(routeIdSelectStr);
            selectTypeStmts.add(routeNameSelectStr);
            selectTypeStmts.add(routeOwnerSelectStr);
            selectTypeStmts.add(routeApprovalStatusSelectStr);
            selectTypeStmts.add(SELECT_TASK_ASSIGNEE_TYPE);
            selectTypeStmts.add(SELECT_TASK_ASSIGNEE_TITLE);
            selectTypeStmts.add(SELECT_TASK_ASSIGNEE_NAME);
            selectTypeStmts.add(selRouteTaskUser);
            selectTypeStmts.add(DomainConstants.SELECT_TYPE);
            selectTypeStmts.add(routeTypeSelectStr);
            selectTypeStmts.add(workflowIdSelectStr);
            selectTypeStmts.add(workflowNameSelectStr);
            selectTypeStmts.add(workflowTypeSelectStr);
            selectTypeStmts.add(strAttrworkFlowDueDate);
            selectTypeStmts.add(strAttrTaskEstimatedFinishDate);
            selectTypeStmts.add(strAttrworkFlowCompletinDate);
            selectTypeStmts.add(strAttrTaskFinishDate);
            selectTypeStmts.add(TASK_PROJECT_ID);
            selectTypeStmts.add(TASK_PROJECT_TYPE);
            selectTypeStmts.add(TASK_PROJECT_NAME);
            selectTypeStmts.add(TASK_PROJECT_ATTRIBUTE_TITLE);
            selectTypeStmts.add(SELECT_KINDOF_TASK);
            selectTypeStmts.add(SELECT_KINDOF_WORKFLOW_TASK);
            selectTypeStmts.add(SELECT_KINDOF_INBOX_TASK);
            selectTypeStmts.add(ROUTE_PROJECT_ID);
            selectTypeStmts.add(ROUTE_PROJECT_NAME);
            selectTypeStmts.add(ROUTE_PROJECT_TYPE);
            selectTypeStmts.add(ROUTE_PROJECT_Title_NAME);
            selectTypeStmts.add(ROUTE_TASK_PROJECT_TYPE);
            selectTypeStmts.add(ROUTE_TASK_PROJECT_ID);
            selectTypeStmts.add(ROUTE_TASK_PROJECT_NAME);
            selectTypeStmts.add(ROUTE_TASK_PROJECT_ATTRIBUTE_TITLE);
            selectTypeStmts.add(TASK_PROJECT_POLICY);
            selectTypeStmts.add(routeScopeSelectStr);
            selectTypeStmts.add(ROUTE_TASK_PROJECT_POLICY);
            /*  selectTypeStmts.add(Route.SELECT_APPROVAL_STATUS);*/
            String sPersonId = boPerson.getObjectId();



            Pattern relPattern = new Pattern(sRelProjectTask);
            relPattern.addPattern(sRelAssignedTask);
            relPattern.addPattern(sRelWorkflowTaskAssinee);

            Pattern typePattern = new Pattern(sTypeInboxTask);

            //yb start 2024.8.13

//            typePattern.addPattern(DomainObject.TYPE_TASK);
//            typePattern.addPattern(sTypeWorkflowTask);
//            typePattern.addPattern(DomainObject.TYPE_CHANGE_TASK);

            //yb end

            taskObject.setId(sPersonId);
            String busWhere = null;

            ContextUtil.startTransaction(context,false);
            ExpansionIterator expItr = taskObject.getExpansionIterator(context,
                    relPattern.getPattern(),
                    typePattern.getPattern(),
                    selectTypeStmts,
                    selectRelStmts,
                    true,
                    true,
                    (short)2,
                    busWhere,
                    null,
                    (short)0,
                    false,
                    false,
                    (short)100,
                    false);

            com.matrixone.apps.domain.util.MapList taskMapList = null;
            try {
                taskMapList = FrameworkUtil.toMapList(expItr,(short)0,null,null,null,null);
            } finally {
                expItr.close();
            }
            ContextUtil.commitTransaction(context);

            // Added for 318463
            // Get the context (top parent) object for WBS Tasks to dispaly appropriate tree for WBS Tasks
            String GROUPTYPE = PropertyUtil.getSchemaProperty(context,"type_Group");
            String PROXYGROUPTYPE = PropertyUtil.getSchemaProperty(context,"type_GroupProxy");
            String strUserGroup = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",context.getLocale(), "emxFramework.Type.Group");
            MQLCommand mql = new MQLCommand();
            String sTaskType = "";
            String sTaskId = "";
            String assigneeType = "";
            String taskAssingee = "";
            String routeTaskUser = "";
            String sMql = "";
            boolean bResult = false;
            String sResult = "";
            StringTokenizer sResultTkz = null;
            String kindOffTask="";

            MapList finalTaskMapList = new MapList();
            Iterator objectListItr = taskMapList.iterator();
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                sTaskType = (String)objectMap.get(DomainObject.SELECT_TYPE);
                assigneeType = (String)objectMap.get(SELECT_TASK_ASSIGNEE_TYPE);
                kindOffTask = (String)objectMap.get(SELECT_KINDOF_TASK);

                if(DomainConstants.TYPE_PERSON.equals(assigneeType)){
                    taskAssingee = (String)objectMap.get(SELECT_TASK_ASSIGNEE_NAME);
                    objectMap.put("TaskAssignee", PersonUtil.getFullName(context, taskAssingee));
                }else if(PROXYGROUPTYPE.equals(assigneeType) || GROUPTYPE.equals(assigneeType)){
                    taskAssingee = (String)objectMap.get(SELECT_TASK_ASSIGNEE_TITLE) + "("+strUserGroup+")";
                    objectMap.put("TaskAssignee", taskAssingee);
                }else {
                    routeTaskUser = (String)objectMap.get(selRouteTaskUser);
                    if (routeTaskUser != null && !"".equals(routeTaskUser)) {
                        String isRoleGroup = "";
                        if(routeTaskUser.indexOf("_") > -1){
                            isRoleGroup = routeTaskUser.substring(0,routeTaskUser.indexOf("_"));
                        }
                        if("role".equals(isRoleGroup)) {
                            objectMap.put("TaskAssignee", i18nNow.getAdminI18NString("Role",PropertyUtil.getSchemaProperty(context, routeTaskUser),context.getLocale().getLanguage())+"("+EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.Common.Role")+")");
                        } else if ("group".equals(isRoleGroup)) {
                            objectMap.put("TaskAssignee", i18nNow.getAdminI18NString("Group", PropertyUtil.getSchemaProperty( context,routeTaskUser),context.getLocale().getLanguage())+"("+EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", context.getLocale(),"emxComponents.Common.Group")+")");
                        }else {
                            objectMap.put("TaskAssignee", routeTaskUser);
                        }
                    }
                }
                // if Task is WBS then add the context (top) object information
                if ("TRUE".equalsIgnoreCase(kindOffTask))
                {
                    sTaskId = (String)objectMap.get(taskObject.SELECT_ID);
                    String projectPolicy = (String)objectMap.get(TASK_PROJECT_POLICY);
                    if(POLICY_PROJECT_SPACE_HOLD_CANCEL.equalsIgnoreCase(projectPolicy)) {
                        continue;
                    }
                    sMql = "expand bus "+sTaskId+" to rel "+sRelSubTask+" recurse to 1 select bus id dump |";
                    bResult = mql.executeCommand(context, sMql);
                    if(bResult) {
                        sResult = mql.getResult().trim();
                        //Bug 318325. Added if condition to check sResult object as not null and not empty.
                        if(sResult!=null && !"".equals(sResult)) {
                            sResultTkz = new StringTokenizer(sResult,"|");
                            sResultTkz.nextToken();
                            sResultTkz.nextToken();
                            sResultTkz.nextToken();
                            objectMap.put("Context Object Type",(String)sResultTkz.nextToken());
                            objectMap.put("Context Object Name",(String)sResultTkz.nextToken());
                            sResultTkz.nextToken();
                            objectMap.put("Context Object Id",(String)sResultTkz.nextToken());
                        }
                    }
                }

                if ((DomainObject.TYPE_INBOX_TASK).equalsIgnoreCase(sTaskType))
                {
                    Object projectPolicy = (Object)objectMap.get(ROUTE_TASK_PROJECT_POLICY);
                    StringList projectPolicies = new StringList();
                    if(projectPolicy != null) {
                        if(projectPolicy instanceof StringList) {
                            projectPolicies = (StringList)projectPolicy;
                        }else {
                            projectPolicies.add((String)projectPolicy);
                        }
                        if(projectPolicies.contains(POLICY_PROJECT_SPACE_HOLD_CANCEL)) {
                            continue;
                        }
                    }
                }

                finalTaskMapList.add(objectMap);
            }
            return finalTaskMapList;
        }
        catch (Exception ex)
        {
            System.out.println("Error in getMyDeskTasks = " + ex.getMessage());
            throw ex;
        }
    }

        /**
         * getCompletedTasks - gets the list of Tasks in Complete State
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns Object
         * @throws Exception if the operation fails
         * @since 10.5
         * @grade 0
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public Object getCompletedTasks(Context context, String[] args) throws Exception
        {

            String stateInboxTaskComplete = PropertyUtil.getSchemaProperty(context,"policy", DomainObject.POLICY_INBOX_TASK,"state_Complete");
            String stateWorkFlowTaskComplete = PropertyUtil.getSchemaProperty(context,"policy", policyWorkflowTask, "state_Completed");
            String stateTaskComplete = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROJECT_TASK,"state_Complete");
            //added for the 325218
            StringBuffer sbf=new StringBuffer();
            if(stateInboxTaskComplete !=null && !"".equals(stateInboxTaskComplete))
                sbf.append("  current == "+ stateInboxTaskComplete);
            if(stateWorkFlowTaskComplete!=null &&!"".equals(stateWorkFlowTaskComplete))
            {
                if(sbf.length()!=0)
                    sbf.append(" || ");
                sbf.append("current == "+stateWorkFlowTaskComplete);
            }
            if(stateTaskComplete!=null&&!"".equals(stateTaskComplete))
            {
                if(sbf.length()!=0)
                    sbf.append(" || ");
                sbf.append("current == "+stateTaskComplete);
            }
            return getTasks(context,sbf.toString());
            //till here
        }


        /**
         * getTasksToBeAccepted - gets the list of Tasks assigned to any of the person assignments
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns Object
         * @throws Exception if the operation fails
         * @since AEF Rossini
         * @grade 0
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public Object getTasksToBeAccepted(Context context, String[] args)
            throws Exception
        {
            MapList taskMapList = new MapList();
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            String languageStr = (String)programMap.get("languageStr");
            try
            {
                final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");
                final String GROUPTYPE = PropertyUtil.getSchemaProperty(context,"type_Group");
                final String PROXYGROUPTYPE = PropertyUtil.getSchemaProperty(context,"type_GroupProxy");
                String selRouteTaskUser       = getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_TASK_USER);
                StringList selectTypeStmts = new StringList();
                selectTypeStmts.add(SELECT_NAME);
                selectTypeStmts.add(SELECT_ID);
                selectTypeStmts.add(SELECT_TYPE);
                selectTypeStmts.add(SELECT_DESCRIPTION);
                selectTypeStmts.add(SELECT_OWNER);
                selectTypeStmts.add(SELECT_CURRENT);
                selectTypeStmts.add(strAttrRouteAction);
                selectTypeStmts.add(strAttrCompletionDate);
                selectTypeStmts.add(strAttrTaskCompletionDate);
                selectTypeStmts.add(SELECT_TASK_ASSIGNEE_TYPE);
                selectTypeStmts.add(SELECT_TASK_ASSIGNEE_TITLE);
                selectTypeStmts.add(SELECT_TASK_ASSIGNEE_NAME);
                selectTypeStmts.add(selRouteTaskUser);


                selectTypeStmts.add("attribute[" + DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]");
                selectTypeStmts.add("attribute[" + DomainObject.ATTRIBUTE_ROUTE_TASK_USER + "]");

                selectTypeStmts.add(strAttrTitle);
                selectTypeStmts.add(objectIdSelectStr);
                selectTypeStmts.add(objectNameSelectStr);
                selectTypeStmts.add(objectTypeSelectStr);
                selectTypeStmts.add(objectTitleSelectStr);
                selectTypeStmts.add(routeIdSelectStr);
                selectTypeStmts.add(routeNameSelectStr);
                selectTypeStmts.add(routeOwnerSelectStr);

                selectTypeStmts.add(SELECT_TYPE);
                selectTypeStmts.add(routeTypeSelectStr);
                selectTypeStmts.add(workflowIdSelectStr);
                selectTypeStmts.add(workflowNameSelectStr);
                selectTypeStmts.add(workflowTypeSelectStr);
                selectTypeStmts.add(strAttrworkFlowDueDate);
                selectTypeStmts.add(strAttrTaskEstimatedFinishDate);
                selectTypeStmts.add(strAttrworkFlowCompletinDate);
                selectTypeStmts.add(strAttrTaskFinishDate);
                selectTypeStmts.add(TASK_PROJECT_ID);
                selectTypeStmts.add(TASK_PROJECT_NAME);
                selectTypeStmts.add(TASK_PROJECT_TYPE);
                selectTypeStmts.add(TASK_PROJECT_ATTRIBUTE_TITLE);
                selectTypeStmts.add(SELECT_KINDOF_TASK);
                selectTypeStmts.add(SELECT_KINDOF_WORKFLOW_TASK);
                selectTypeStmts.add(SELECT_KINDOF_INBOX_TASK);

                selectTypeStmts.add(ROUTE_PROJECT_ID);
                selectTypeStmts.add(ROUTE_PROJECT_NAME);
                selectTypeStmts.add(ROUTE_PROJECT_TYPE);
                selectTypeStmts.add(ROUTE_PROJECT_Title_NAME);
                selectTypeStmts.add(ROUTE_TASK_PROJECT_ID);
                selectTypeStmts.add(ROUTE_TASK_PROJECT_TYPE);
                selectTypeStmts.add(ROUTE_TASK_PROJECT_NAME);
                selectTypeStmts.add(ROUTE_TASK_PROJECT_ATTRIBUTE_TITLE);
                selectTypeStmts.add(routeScopeSelectStr);
                selectTypeStmts.add(TASK_PROJECT_POLICY);

                String strPersonAssignments = "";
                //Vector personAssignments = PersonUtil.getAssignments(context);
                // To fetch the person user group from db instead of cache - issue when adding new user group/ person to user group //
                Vector personAssignments = PersonUtil.getUserRoles(context,context.getUser());

                personAssignments.remove(context.getUser());

                Map<String,String> rolesMap = PersonUtil.getAssignmentsMap(context, context.getUser(), personAssignments);
                Iterator assignmentsItr = personAssignments.iterator();
                //Begin : Bug 346478
                Role roleObj = null;
                Group groupObj = null;
                StringList slParents = new StringList();
                StringList userGroupList = new StringList();
                StringList slParentRolesOrGroups = new StringList();
                String isRoleGroupOrUserGroup = null;
                //End : Bug 346478
                while(assignmentsItr.hasNext())
                {
                    String assignment = (String)assignmentsItr.next();
                    isRoleGroupOrUserGroup = rolesMap.get(assignment);
                    try {
                        if("projectgroup".equals(isRoleGroupOrUserGroup)){
                            userGroupList.add(assignment);
                        }else if("role".equals(isRoleGroupOrUserGroup)){
                            roleObj = new Role(assignment);
                            roleObj.open(context);
                            // Find all its parents
                            slParents = roleObj.getParents(context, true);
                            if (slParents != null) {
                                slParentRolesOrGroups.addAll(slParents);
                            }
                            roleObj.close(context);
                        }else if("group".equals(isRoleGroupOrUserGroup)){
                            groupObj = new Group(assignment);
                            groupObj.open(context);

                            // Find all its parents
                            slParents = groupObj.getParents(context, true);
                            if (slParents != null) {
                                slParentRolesOrGroups.addAll(slParents);
                            }

                            groupObj.close(context);
                        }
                    } catch (MatrixException me){}
                    //End : Bug 346478 code modification

                }
                //Remove the last ","
                //strPersonAssignments = strPersonAssignments.substring(0,(strPersonAssignments.length())-1);

                // Begin : Bug 346478 code modification
                slParentRolesOrGroups.addAll(personAssignments);
                if(!userGroupList.isEmpty()){
                    String ROLE_USER_GROUP = PropertyUtil.getSchemaProperty(context,"role_USERGROUPOWNER");
                    slParentRolesOrGroups.add(ROLE_USER_GROUP);
                }
                strPersonAssignments = FrameworkUtil.join(slParentRolesOrGroups,",");
                strPersonAssignments = strPersonAssignments ;
                // End : Bug 346478 code modification
                StringBuffer objWhere = new StringBuffer();
                objWhere.append("("+DomainObject.SELECT_OWNER + " matchlist " + "\"" + strPersonAssignments + "\" \",\"");
                objWhere.append(")  &&  (current != "+POLICY_INBOX_TASK_STATE_COMPLETE);
                objWhere.append(" && " + "from[" + RELATIONSHIP_ROUTE_TASK + "].to.attribute[" + ATTRIBUTE_ROUTE_STATUS + "] != \"Stopped\") ");

                Pattern typePattern = new Pattern(TYPE_INBOX_TASK);
                //yb start 2024.8.13
                //typePattern.addPattern(TYPE_TASK);
                //yb end
                //typePattern.addPattern(sTypeWorkflowTask);// For Bug 346478, we shall find the WF tasks later

                taskMapList = DomainObject.findObjects(context,
                        typePattern.getPattern(),
                        null,
                        objWhere.toString(),
                        selectTypeStmts);

                DomainObject doPerson = PersonUtil.getPersonObject(context);
                String RELATIONSHIP_ASSIGNED_TASKS_CANDIDATE = PropertyUtil.getSchemaProperty(context,"relationship_AssignedTasksCandidate");

                addProjectTasks(context, doPerson, taskMapList, RELATIONSHIP_ASSIGNED_TASKS_CANDIDATE, selectTypeStmts, null);
                // Removing those 'Inbox Tasks' that satisfy the following criteria
                // 1) The connected Route has a Route Template that has 'Owning Organization' relationship &
                // 2) The context user is not a member of that Organization
// IR-043921V6R2011 - Changes START
                StringList slInboxTasks = new StringList( taskMapList.size() );
                for( Iterator mlItr = taskMapList.iterator(); mlItr.hasNext(); ) {
                    Map mTask = (Map) mlItr.next();
                    if( TYPE_INBOX_TASK.equals( (String) mTask.get( SELECT_TYPE ) ) ) {
                        slInboxTasks.add( (String) mTask.get( SELECT_ID ) );
                    }
                }

                StringList busSelects = new StringList(2);
                busSelects.add( SELECT_ID );
                busSelects.add( SELECT_TYPE );
                String taskAssingee = "";

                MapList mlOrganizations = doPerson.getRelatedObjects( context, RELATIONSHIP_MEMBER, TYPE_ORGANIZATION,
                        busSelects, new StringList( SELECT_RELATIONSHIP_ID ), true, false, (short) 1, "", "", 0 );

                StringList slMember = new StringList( mlOrganizations.size() );
                for( Iterator mlItr = mlOrganizations.iterator(); mlItr.hasNext(); ) {
                    Map mOrg = (Map) mlItr.next();
                    slMember.add( (String) mOrg.get( SELECT_ID ) );
                }

                String proxyGoupType = PropertyUtil.getSchemaProperty(context,"type_GroupProxy");
                String selAsigneeproxyGrpType    = "from["+DomainObject.RELATIONSHIP_PROJECT_TASK+"].to.type.kindof["+ proxyGoupType +"]";
                String selAsigneeType   = "from["+DomainObject.RELATIONSHIP_PROJECT_TASK+"].to.type.kindof["+ PropertyUtil.getSchemaProperty(context,"type_Group") +"]";

                busSelects.add( SELECT_TEMPLATE_OWNING_ORG_ID );
                busSelects.add( selAsigneeproxyGrpType );
                busSelects.add( selAsigneeType );

                MapList mlIboxTasksInfo = DomainObject.getInfo(context, (String[])slInboxTasks.toArray(new String[slInboxTasks.size()]), busSelects );
                StringList slToRemoveTask = new StringList( mlIboxTasksInfo.size() );
                for( Iterator mlItr = mlIboxTasksInfo.iterator(); mlItr.hasNext(); ) {
                    Map mTask = (Map) mlItr.next();
                    String sOrgId = (String) mTask.get( SELECT_TEMPLATE_OWNING_ORG_ID );
                    String taskAsssignedToProxyUG = (String) mTask.get( selAsigneeproxyGrpType );
                    String taskAsssignedToUG = (String) mTask.get( selAsigneeType );

                    if("true".equalsIgnoreCase(taskAsssignedToProxyUG) || "true".equalsIgnoreCase(taskAsssignedToUG) ) {
                        continue;
                    }
                    if( sOrgId !=null && !"null".equals( sOrgId ) && !"".equals( sOrgId ) && !(slMember.contains( sOrgId ))) {
                        slToRemoveTask.add( (String) mTask.get( SELECT_ID ) );
                    }
                }
                for( Iterator mlItr = taskMapList.iterator(); mlItr.hasNext(); ) {
                    Map mTask = (Map) mlItr.next();
                    if( slToRemoveTask.contains( (String) mTask.get( SELECT_ID ))) {
                        mlItr.remove();
                    }
                }
// IR-043921V6R2011 - Changes END


                // Added for 318463
                // Get the context (top parent) object for WBS Tasks to dispaly appropriate tree for WBS Tasks
                MQLCommand mql = new MQLCommand();
                String sTaskType = "";
                String sTaskId = "";
                String assigneeType = "";
                String routeTaskUser = "";
                String sMql = "";
                boolean bResult = false;
                String sResult = "";
                StringTokenizer sResultTkz = null;
                String kindOffTask="";

                MapList finalTaskMapList = new MapList();
                Iterator objectListItr = taskMapList.iterator();
                String strUserGroup = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale(languageStr), "emxFramework.Type.Group");
                while(objectListItr.hasNext())
                {
                    Map objectMap = (Map) objectListItr.next();
                    sTaskType = (String)objectMap.get(DomainObject.SELECT_TYPE);
                    assigneeType = (String)objectMap.get(SELECT_TASK_ASSIGNEE_TYPE);
                    kindOffTask = (String)objectMap.get(SELECT_KINDOF_TASK);

                    boolean addToFinalMap =true;
                    if(DomainConstants.TYPE_PERSON.equals(assigneeType)){
                        taskAssingee = (String)objectMap.get(SELECT_TASK_ASSIGNEE_NAME);
                        objectMap.put("TaskAssignee", PersonUtil.getFullName(context, taskAssingee));
                    }else if(PROXYGROUPTYPE.equals(assigneeType) || GROUPTYPE.equals(assigneeType)){
                        if(!userGroupList.contains((String) objectMap.get(SELECT_TASK_ASSIGNEE_NAME))){
                            addToFinalMap = false;
                        }
                        taskAssingee = (String)objectMap.get(SELECT_TASK_ASSIGNEE_TITLE) + "("+strUserGroup+")";
                        objectMap.put("TaskAssignee", taskAssingee);
                    }else {
                        routeTaskUser = (String)objectMap.get(selRouteTaskUser);
                        if (routeTaskUser != null && !"".equals(routeTaskUser)) {
                            String isRoleGroup = "";
                            if(routeTaskUser.indexOf("_") > -1){
                                isRoleGroup = routeTaskUser.substring(0,routeTaskUser.indexOf("_"));
                            }
                            if("role".equals(isRoleGroup)) {
                                objectMap.put("TaskAssignee", i18nNow.getAdminI18NString("Role",PropertyUtil.getSchemaProperty(context, routeTaskUser),languageStr)+"("+EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Common.Role")+")");
                            } else if ("group".equals(isRoleGroup)) {
                                objectMap.put("TaskAssignee", i18nNow.getAdminI18NString("Group", PropertyUtil.getSchemaProperty( context,routeTaskUser),languageStr)+"("+EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Common.Group")+")");
                            }else {
                                objectMap.put("TaskAssignee", routeTaskUser);
                            }
                        }
                    }
                    // if Task is WBS then add the context (top) object information
                    if ("TRUE".equalsIgnoreCase(kindOffTask))
                    {
                        sTaskId = (String)objectMap.get(DomainObject.SELECT_ID);
                        String projectPolicy = (String)objectMap.get(TASK_PROJECT_POLICY);
                        if(POLICY_PROJECT_SPACE_HOLD_CANCEL.equalsIgnoreCase(projectPolicy)) {
                            continue;
                        }
                        taskAssingee = (String)objectMap.get("attribute[" + DomainObject.ATTRIBUTE_ROUTE_TASK_USER + "]");
                        sMql = "expand bus "+sTaskId+" to rel "+sRelSubTask+" recurse to 1 select bus id dump |";
                        bResult = mql.executeCommand(context, sMql);
                        if(bResult) {
                            sResult = mql.getResult().trim();
                            //Bug 318325. Added if condition to check sResult object as not null and not empty.
                            if(sResult!=null && !"".equals(sResult)) {
                                sResultTkz = new StringTokenizer(sResult,"|");
                                sResultTkz.nextToken();
                                sResultTkz.nextToken();
                                sResultTkz.nextToken();
                                objectMap.put("Context Object Type",(String)sResultTkz.nextToken());
                                objectMap.put("Context Object Name",(String)sResultTkz.nextToken());
                                sResultTkz.nextToken();
                                objectMap.put("Context Object Id",(String)sResultTkz.nextToken());
                            }
                        }
                    }
                    if(addToFinalMap) {
                        finalTaskMapList.add(objectMap);
                    }
                }
                return finalTaskMapList;

            }
            catch(Exception e)
            {
                throw new FrameworkException(e.getMessage());
            }
        }

    private void addProjectTasks(Context context, DomainObject personObject, MapList taskMapList,String relationshipName, StringList selectTypeStmts, String whereClause) throws FrameworkException {

        String stateTaskAssign = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROJECT_TASK,"state_Assign");
        String stateTaskActive = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROJECT_TASK,"state_Active");
        String stateTaskReview = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROJECT_TASK,"state_Review");


        String loggedinUser = context.getUser();
        String RELATIONSHIP_ASSIGNED_TASKS_CANDIDATE = PropertyUtil.getSchemaProperty(context,"relationship_AssignedTasksCandidate");
        Pattern typePattern = new Pattern(TYPE_TASK_MANAGEMENT);
        MapList groupAssignedTasksMapList = new MapList();

        if(RELATIONSHIP_ASSIGNED_TASKS_CANDIDATE.equalsIgnoreCase(relationshipName)) {

            StringBuilder busWhere = new StringBuilder();
            busWhere.append(" ( current == "+ stateTaskAssign);
            busWhere.append(" || current == "+ stateTaskActive);
            busWhere.append(" || current == "+ stateTaskReview+ ")");
            String candidateWhereClause = busWhere.toString();

            selectTypeStmts.add("to["+DomainConstants.RELATIONSHIP_ASSIGNED_TASKS+"].from.name");
            selectTypeStmts.add(SELECT_POLICY);
            StringList groupSelect = new StringList();
            groupSelect.add(DomainConstants.SELECT_PHYSICAL_ID);
            MapList groupMapList = personObject.getRelatedObjects(context,DomainConstants.RELATIONSHIP_GROUPMEMBER, "*",groupSelect,null,true,false,(short) 1,"",null,0);
            int groupMapListSize = groupMapList.size();
            StringBuilder groupWhereCaluse = new StringBuilder();


            if(groupMapListSize > 0) {
                Map groupInfo = (Map) groupMapList.get(0);
                String groupId = (String) groupInfo.get(DomainConstants.SELECT_PHYSICAL_ID);

                groupWhereCaluse.append(" &&( to["+relationshipName+"].from.physicalid == "+groupId);

                for(int i = 1; i<groupMapListSize; i++) {
                    groupInfo = (Map) groupMapList.get(i);
                    groupId = (String) groupInfo.get(DomainConstants.SELECT_PHYSICAL_ID);
                    groupWhereCaluse.append(" || to["+relationshipName+"].from.physicalid == "+groupId);
                }
                groupWhereCaluse.append(" )");
            }

            candidateWhereClause+= groupWhereCaluse.toString();

            MapList groupTaskMapList = DomainObject.findObjects(context,typePattern.getPattern(),null,candidateWhereClause,selectTypeStmts);

            for(int i=0,j=groupTaskMapList.size();i<j;i++) {
                Map groupTaskMap = (Map)groupTaskMapList.get(i);
                Object assignees = groupTaskMap.get("to["+DomainConstants.RELATIONSHIP_ASSIGNED_TASKS+"].from.name");
                String taskPolicy = (String)groupTaskMap.get(SELECT_POLICY);
                String currentState = (String)groupTaskMap.get(SELECT_CURRENT);
                String taskProjectPolicy = (String)groupTaskMap.get(TASK_PROJECT_POLICY);

                //Don't list Gate/Milestone if not in In Approval state
                if("Project Peview".equals(taskPolicy) && !DomainConstants.STATE_PROJECT_SPACE_REVIEW.equalsIgnoreCase(currentState)) {
                    continue;
                }
                //Don't list Task/Phase if not in ToDO or In work state.
                if(DomainConstants.POLICY_PROJECT_TASK.equals(taskPolicy) &&
                        !(DomainConstants.STATE_PROJECT_SPACE_ASSIGN.equalsIgnoreCase(currentState) || DomainConstants.STATE_PROJECT_SPACE_ACTIVE.equalsIgnoreCase(currentState))) {
                    continue;
                }

                //Don't list task if it's Project state is HOLD or CANCEL.
                if(POLICY_PROJECT_SPACE_HOLD_CANCEL.equalsIgnoreCase(taskProjectPolicy)) {
                    continue;
                }

                //Add taskmap if it doesn't any have assignee.
                if(assignees == null) {
                    groupAssignedTasksMapList.add(groupTaskMap);
                } else {
                    //Add taskmap if logged in user is not assignee of it.
                    if (assignees instanceof StringList) {
                        StringList assigneeList = (StringList)assignees;
                        if(!assigneeList.contains(loggedinUser)) {
                            groupAssignedTasksMapList.add(groupTaskMap);
                        }
                    } else if (assignees instanceof String && (!loggedinUser.equalsIgnoreCase(assignees.toString()))) {
                        groupAssignedTasksMapList.add(groupTaskMap);
                    }
                }
            }
        } else {
            String SELECT_IS_EXPERIMENT_TASK = "to[Project Access Key].from.from[Project Access List].to.type.kindof[Experiment]";
            String SELECT_IS_BASELINE_TASK = "to[Project Access Key].from.from[Project Access List].to.type.kindof[Project Baseline]";

            selectTypeStmts.add(SELECT_IS_EXPERIMENT_TASK);
            selectTypeStmts.add(SELECT_IS_BASELINE_TASK);

            MapList groupMapList = personObject.getRelatedObjects(context,DomainConstants.RELATIONSHIP_ASSIGNED_TASKS,
                    typePattern.getPattern(),selectTypeStmts,null,false,true,(short) 1,whereClause,null,0);

            for(int i=0,j=groupMapList.size();i<j;i++) {
                Map taskMap = (Map)groupMapList.get(i);
                String isExperimentTask = (String)taskMap.get(SELECT_IS_EXPERIMENT_TASK);
                String isBaselineTask = (String)taskMap.get(SELECT_IS_BASELINE_TASK);
                String taskProjectPolicy = (String)taskMap.get(TASK_PROJECT_POLICY);

                //Don't add task if it is of Experiment, Baseline or it's Project state is HOLD or CANCEL.
                if("FALSE".equalsIgnoreCase(isExperimentTask) && "FALSE".equalsIgnoreCase(isBaselineTask) && !POLICY_PROJECT_SPACE_HOLD_CANCEL.equalsIgnoreCase(taskProjectPolicy)) {
                    groupAssignedTasksMapList.add(taskMap);
                }
            }
        }

        if(groupAssignedTasksMapList!=null && !groupAssignedTasksMapList.isEmpty()) {

            if(taskMapList.isEmpty()) {//Add all Group assignments if taskMapList is empty.
                taskMapList.addAll(groupAssignedTasksMapList);
            } else {
                MapList finalGroupMapListToAdd = new MapList();
                int taskMapListSize = taskMapList.size();
                for(int i=0,j=groupAssignedTasksMapList.size();i<j;i++) {
                    Map groupMap = (Map)groupAssignedTasksMapList.get(i);
                    String groupTaskId	= (String)groupMap.get(SELECT_ID);
                    boolean isDuplicateTask = false;

                    for(int k=0;k<taskMapListSize;k++) {
                        Map taskMap = (Map)taskMapList.get(k);
                        String taskId	= (String)taskMap.get(SELECT_ID);
                        if(groupTaskId.equalsIgnoreCase(taskId)) {
                            isDuplicateTask = true;
                            break;
                        }
                    }
                    if(!isDuplicateTask) {
                        finalGroupMapListToAdd.add(groupMap);
                    }
                }
                //Add only tasks which were not present in taskMapList before
                taskMapList.addAll(finalGroupMapListToAdd);
            }
        }
    }


    /**
     * getTasks - gets the list of Tasks depending on condition
     * @param context the eMatrix <code>Context</code> object
     * @param busWhere condition to query
     * @returns Object
     * @throws Exception if the operation fails
     * @since 10.5
     * @grade 0
     */
    public Object getTasks(Context context, String busWhere ) throws Exception
    {

        try
        {
            DomainObject taskObject = DomainObject.newInstance(context);
            DomainObject boPerson     = PersonUtil.getPersonObject(context);
            String stateInboxTaskReview = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_INBOX_TASK,"state_Review");
            String selRouteTaskUser       = getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_TASK_USER);
            StringList selectRelStmts  = new StringList();
            //Added for Bug No 338177 Begin
            StringList selectTypeStmtId = new StringList();
            selectTypeStmtId.add(SELECT_ID);

            //Added for Bug No 338177 End
            StringList selectTypeStmts = new StringList();
            selectTypeStmts.add(SELECT_NAME);
            selectTypeStmts.add(SELECT_TYPE);
            selectTypeStmts.add(SELECT_ID);
            selectTypeStmts.add(SELECT_DESCRIPTION);
            selectTypeStmts.add(SELECT_OWNER);
            selectTypeStmts.add(SELECT_MODIFIED);
            selectTypeStmts.add(SELECT_CURRENT);
            selectTypeStmts.add(strAttrRouteAction);
            selectTypeStmts.add(strAttrCompletionDate);
            selectTypeStmts.add(strAttrTaskCompletionDate);
            selectTypeStmts.add("attribute[" + DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]");
            selectTypeStmts.add(strAttrTitle);
            selectTypeStmts.add(objectTypeSelectStr);
            selectTypeStmts.add(objectIdSelectStr);
            selectTypeStmts.add(objectNameSelectStr);
            selectTypeStmts.add(objectTitleSelectStr);
            selectTypeStmts.add(routeIdSelectStr);
            selectTypeStmts.add(routeNameSelectStr);
            selectTypeStmts.add(routeOwnerSelectStr);
            selectTypeStmts.add(SELECT_TASK_ASSIGNEE_TYPE);
            selectTypeStmts.add(SELECT_TASK_ASSIGNEE_TITLE);
            selectTypeStmts.add(SELECT_TASK_ASSIGNEE_NAME);
            selectTypeStmts.add(selRouteTaskUser);

            selectTypeStmts.add(SELECT_TYPE);
            selectTypeStmts.add(routeTypeSelectStr);
            selectTypeStmts.add(workflowIdSelectStr);
            selectTypeStmts.add(workflowNameSelectStr);
            selectTypeStmts.add(workflowTypeSelectStr);
            selectTypeStmts.add(strAttrworkFlowDueDate);
            selectTypeStmts.add(strAttrTaskEstimatedFinishDate);
            selectTypeStmts.add(strAttrworkFlowCompletinDate);
            selectTypeStmts.add(strAttrTaskFinishDate);
            selectTypeStmts.add(TASK_PROJECT_ID);
            selectTypeStmts.add(TASK_PROJECT_NAME);
            selectTypeStmts.add(TASK_PROJECT_TYPE);
            selectTypeStmts.add(TASK_PROJECT_ATTRIBUTE_TITLE);
            selectTypeStmts.add(TASK_PROJECT_POLICY);
            selectTypeStmts.add(ROUTE_TASK_PROJECT_POLICY);



            selectTypeStmts.add(SELECT_KINDOF_TASK);
            selectTypeStmts.add(SELECT_KINDOF_WORKFLOW_TASK);
            selectTypeStmts.add(SELECT_KINDOF_INBOX_TASK);

            selectTypeStmts.add(ROUTE_PROJECT_ID);
            selectTypeStmts.add(ROUTE_PROJECT_NAME);
            selectTypeStmts.add(ROUTE_PROJECT_TYPE);
            selectTypeStmts.add(ROUTE_PROJECT_Title_NAME);
            selectTypeStmts.add(ROUTE_TASK_PROJECT_ID);
            selectTypeStmts.add(ROUTE_TASK_PROJECT_TYPE);
            selectTypeStmts.add(ROUTE_TASK_PROJECT_NAME);
            selectTypeStmts.add(ROUTE_TASK_PROJECT_ATTRIBUTE_TITLE);
            selectTypeStmts.add(routeScopeSelectStr);
            selectTypeStmts.add(strAttrTaskApprovalStatus);

            /*  selectTypeStmts.add(Route.SELECT_APPROVAL_STATUS);*/
            String sPersonId = boPerson.getObjectId();

            Pattern relPattern = new Pattern(sRelProjectTask);

            relPattern.addPattern(sRelAssignedTask);
            relPattern.addPattern(sRelWorkflowTaskAssinee);

            //yb start 2024.8.13
            Pattern typePattern = new Pattern(sTypeInboxTask);
//            typePattern.addPattern(DomainObject.TYPE_TASK);
//            typePattern.addPattern(sTypeWorkflowTask);
//            typePattern.addPattern(DomainObject.TYPE_CHANGE_TASK);
            //yb end

            SelectList selectStmts = new SelectList();
            taskObject.setId(sPersonId);
            // get the list of tasks that needs owner review
            //Added for bug 352071
            String strCommand = "temp query bus $1 $2 $3 where $4 select $5 dump $6";
            StringBuffer bufWhereClause = new StringBuffer(100);
            bufWhereClause.append("attribute[");
            bufWhereClause.append(sAttrReviewCommentsNeeded);
            bufWhereClause.append("]==Yes && current==");
            bufWhereClause.append(stateInboxTaskReview);
            bufWhereClause.append("&& from[");
            bufWhereClause.append(sRelRouteTask);
            bufWhereClause.append("].to.owner==");
            bufWhereClause.append("'").append(context.getUser()).append("'");
            String strResult = MqlUtil.mqlCommand(context, strCommand, sTypeInboxTask, "*", "*", bufWhereClause.toString(), "id", "|" );
            //end of bug 352071
            //Added for Bug No 338177 Begin
            com.matrixone.apps.domain.util.MapList taskMapList =  taskObject.getRelatedObjects(context,
                    relPattern.getPattern(),
                    typePattern.getPattern(),
                    selectTypeStmtId,
                    selectRelStmts,
                    true,
                    true,
                    (short)2,
                    busWhere,
                    null,
                    null,
                    null,
                    null);


//            addProjectTasks(context, taskObject, taskMapList, DomainConstants.RELATIONSHIP_ASSIGNED_TASKS, selectTypeStmts, busWhere);
	  /*StringList groupSelect = new StringList();
	  groupSelect.add(DomainConstants.SELECT_PHYSICAL_ID);
	  MapList groupMapList = taskObject.getRelatedObjects(context,DomainConstants.RELATIONSHIP_GROUPMEMBER, "*",groupSelect,null,true,false,(short) 1,"",null,0);
	  int groupMapListSize = groupMapList.size();

	  for(int i = 0; i<groupMapListSize; i++){
		  Map groupInfo = (Map) groupMapList.get(i);
		  String groupId = (String) groupInfo.get(DomainConstants.SELECT_PHYSICAL_ID);
		  DomainObject groupObj = DomainObject.newInstance(context);
		  groupObj.setId(groupId);
		  MapList groupAssignedTasksMapList = groupObj.getRelatedObjects(context,DomainConstants.RELATIONSHIP_ASSIGNED_TASKS, "*",selectTypeStmts,null,false,true,(short) 0,busWhere,null,0);

		  if(groupAssignedTasksMapList!=null && !groupAssignedTasksMapList.isEmpty()) {

			  if(taskMapList.isEmpty()) {//Add all Group assignments if taskMapList is empty.
				  taskMapList.addAll(groupAssignedTasksMapList);
			  } else {
				  MapList groupMapListToAdd = new MapList();
				  int taskMapListSize = taskMapList.size();
				  int groupAssignedTasksMapListSize = groupAssignedTasksMapList.size();

				  for(int j=0;j<groupAssignedTasksMapListSize;j++) {
					  Map groupAssignedTasksMap = (Map)groupAssignedTasksMapList.get(j);
					  String groupTaskId	= (String)groupAssignedTasksMap.get(SELECT_ID);
					  boolean isDuplicateTask = false;

					  for(int k=0;k<taskMapListSize;k++) {
						  Map taskMap = (Map)taskMapList.get(k);
						  String taskId	= (String)taskMap.get(SELECT_ID);
						  String taskName	= (String)taskMap.get(SELECT_NAME);
						  if(groupTaskId.equalsIgnoreCase(taskId)) {
							  isDuplicateTask = true;
							  break;
						  }
					  }
					  if(!isDuplicateTask) {
						  groupMapListToAdd.add(groupAssignedTasksMap);
					  }
				  }
				  //Add only tasks which were not present in taskMapList before
				  taskMapList.addAll(groupMapListToAdd);
			  }
		  }
	  }*/


            //Added for bug 352071
            if(strResult!=null && !"".equals(strResult))
            {
                String taskInbox = "";
                StringList strlResult = new StringList();
                String strTemp = "";
                StringList taskIds =FrameworkUtil.split(strResult,"\n");
                Iterator taskIdIterator=taskIds.iterator();
                while(taskIdIterator.hasNext())
                {
                    Map tempMap= new HashMap();
                    taskInbox=(String)taskIdIterator.next();
                    strlResult = FrameworkUtil.split(taskInbox,"|");
                    strTemp=(String)strlResult.get(3);
                    boolean isPresent = false;
                    for( int i=0; i<taskMapList.size(); i++){
                        Map map = (Map)taskMapList.get(i);
                        String id = (String)map.get("id");
                        if(strTemp.equals(id)){
                            isPresent = true;
                            break;
                        }
                    }
                    if(!isPresent){
                        tempMap.put("id",strTemp);
                        taskMapList.add(tempMap);
                    }
                }
            }
            //end for bug 352071
            String[] objectIds=new String[taskMapList.size()];
            Iterator idsIterator=taskMapList.iterator();

            for(int i=0;idsIterator.hasNext();i++){
                Map map=(Map)idsIterator.next();
                objectIds[i]=(String)map.get("id");
            }
            taskMapList=DomainObject.getInfo(context,objectIds,selectTypeStmts);
            //Added for Bug No 338177 End


            // Added for 318463
            // Get the context (top parent) object for WBS Tasks to dispaly appropriate tree for WBS Tasks
            MQLCommand mql = new MQLCommand();
            final String GROUPTYPE = PropertyUtil.getSchemaProperty(context,"type_Group");
            final String PROXYGROUPTYPE = PropertyUtil.getSchemaProperty(context,"type_GroupProxy");
            final String strUserGroup = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",context.getLocale(), "emxFramework.Type.Group");
            String sTaskType = "";
            String sTaskId = "";
            String sMql = "";
            String assigneeType = "";
            String taskAssingee = "";
            String routeTaskUser = "";
            boolean bResult = false;
            String sResult = "";
            StringTokenizer sResultTkz = null;
            MapList finalTaskMapList = new MapList();
            Iterator objectListItr = taskMapList.iterator();
            String kindOffTask="";
            while(objectListItr.hasNext())
            {

                Map objectMap = (Map) objectListItr.next();
                sTaskType = (String)objectMap.get(DomainObject.SELECT_TYPE);
                assigneeType = (String)objectMap.get(SELECT_TASK_ASSIGNEE_TYPE);
                kindOffTask = (String)objectMap.get(SELECT_KINDOF_TASK);

                if(DomainConstants.TYPE_PERSON.equals(assigneeType)){
                    taskAssingee = (String)objectMap.get(SELECT_TASK_ASSIGNEE_NAME);
                    objectMap.put("TaskAssignee", PersonUtil.getFullName(context, taskAssingee));
                }else if(PROXYGROUPTYPE.equals(assigneeType) || GROUPTYPE.equals(assigneeType)){
                    taskAssingee = (String)objectMap.get(SELECT_TASK_ASSIGNEE_TITLE) + "("+strUserGroup+")";
                    objectMap.put("TaskAssignee", taskAssingee);
                }else {
                    routeTaskUser = (String)objectMap.get(selRouteTaskUser);
                    if (routeTaskUser != null && !"".equals(routeTaskUser)) {
                        String isRoleGroup = "";
                        if(routeTaskUser.indexOf("_") > -1){
                            isRoleGroup = routeTaskUser.substring(0,routeTaskUser.indexOf("_"));
                        }
                        if("role".equals(isRoleGroup)) {
                            objectMap.put("TaskAssignee", i18nNow.getAdminI18NString("Role",PropertyUtil.getSchemaProperty(context, routeTaskUser),context.getLocale().getLanguage())+"("+EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", context.getLocale(),"emxComponents.Common.Role")+")");
                        } else if ("group".equals(isRoleGroup)) {
                            objectMap.put("TaskAssignee", i18nNow.getAdminI18NString("Group", PropertyUtil.getSchemaProperty( context,routeTaskUser),context.getLocale().getLanguage())+"("+EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", context.getLocale(),"emxComponents.Common.Group")+")");
                        }else {
                            objectMap.put("TaskAssignee", routeTaskUser);
                        }
                    }
                }
                // if Task is WBS then add the context (top) object information
                if ("TRUE".equalsIgnoreCase(kindOffTask))
                {

                    sTaskId = (String)objectMap.get(taskObject.SELECT_ID);
                    String projectPolicy = (String)objectMap.get(TASK_PROJECT_POLICY);
                    if(POLICY_PROJECT_SPACE_HOLD_CANCEL.equalsIgnoreCase(projectPolicy)) {
                        continue;
                    }
                    sMql = "expand bus "+sTaskId+" to rel "+sRelSubTask+" recurse to 1 select bus id dump |";
                    bResult = mql.executeCommand(context, sMql);
                    if(bResult) {

                        sResult = mql.getResult().trim();
                        //Bug 318325. Added if condition to check sResult object as not null and not empty.
                        if(sResult!=null && !"".equals(sResult)) {

                            sResultTkz = new StringTokenizer(sResult,"|");
                            sResultTkz.nextToken();
                            sResultTkz.nextToken();
                            sResultTkz.nextToken();
                            objectMap.put("Context Object Type",(String)sResultTkz.nextToken());
                            objectMap.put("Context Object Name",(String)sResultTkz.nextToken());
                            sResultTkz.nextToken();
                            objectMap.put("Context Object Id",(String)sResultTkz.nextToken());
                        }
                    }
                }

                if ((DomainObject.TYPE_INBOX_TASK).equalsIgnoreCase(sTaskType))
                {
                    Object projectPolicy = (Object)objectMap.get(ROUTE_TASK_PROJECT_POLICY);
                    StringList projectPolicies = new StringList();
                    if(projectPolicy != null) {
                        if(projectPolicy instanceof StringList) {
                            projectPolicies = (StringList)projectPolicy;
                        }else {
                            projectPolicies.add((String)projectPolicy);
                        }
                        if(projectPolicies.contains(POLICY_PROJECT_SPACE_HOLD_CANCEL)) {
                            continue;
                        }
                    }
                }
                finalTaskMapList.add(objectMap);

            }
            return finalTaskMapList;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
}
