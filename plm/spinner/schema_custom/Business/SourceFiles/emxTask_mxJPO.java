/* emxTask.java

   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxTask.java.rca 1.6 Wed Oct 22 16:21:23 2008 przemek Experimental przemek $
*/

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.Task;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The <code>emxTask</code> class represents the Task JPO
 * functionality for the AEF type.
 *
 * @version AEF 10.0.SP4 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxTask_mxJPO extends emxTaskBase_mxJPO
{
    private  static final Logger logger = LoggerFactory.getLogger(emxTask_mxJPO.class);

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     * @grade 0
     */
    public emxTask_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }

    /**
     * In the Structure Browser, For getting the Task SubTypes Ranges
     * @param context the ENOVIA <code>Context</code> object
     * @param args
     * @return HashMap of Task Type Values
     * @throws MatrixException
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public HashMap getTaskTypes(Context context, String[] args) throws MatrixException {

        String policy = ProgramCentralConstants.EMPTY_STRING;
        try {
            Map programMap = (HashMap) JPO.unpackArgs(args);
            Map requestMap = (Map) programMap.get("requestMap");
            policy = (String) requestMap.get("PolicyName");
        } catch (Exception e) {
            //e.printStackTrace();
            DebugUtil.debug("getTaskTypes "+ e.getMessage());
        }

        HashMap mapTaskTypeNames = new HashMap();
        StringList slTaskSubTypes = ProgramCentralUtil.getTaskSubTypesList(context);
        //移除掉自定义的Task对象 add by caipan
        logger.info("slTaskSubTypes:{}",slTaskSubTypes);
        StringList cuslistType = new StringList();
        cuslistType.add("JF_DATask");
        cuslistType.add("JF_APRTask");
        cuslistType.add("JF_CustomerTask");
        cuslistType.add("JF_SignTask");
        cuslistType.add("JF_ECOTask");
        cuslistType.add("JF_PCRVerificationTask");
        cuslistType.add("JF_PCRTask");
        cuslistType.add("JF_PCRExecuteTask");
        for(int i =0;i<cuslistType.size();i++){
            slTaskSubTypes.remove(cuslistType.get(i));
        }
        //end
        if(FrameworkUtil.isSuiteRegistered(context, "appVersionAerospaceProgramManagementAccelerator", false, null, null)) {
            slTaskSubTypes.remove(PropertyUtil.getSchemaProperty(context, "type_MilestoneOpportunity"));
            slTaskSubTypes.remove(PropertyUtil.getSchemaProperty(context, "type_MilestonePayment"));
            slTaskSubTypes.remove(PropertyUtil.getSchemaProperty(context, "type_MilestoneRisk"));
            slTaskSubTypes.remove(PropertyUtil.getSchemaProperty(context, "type_MilestoneFee"));
            slTaskSubTypes.remove(PropertyUtil.getSchemaProperty(context, "type_ValidationTask"));
        }
        // IR-621192 - NX5
        if( slTaskSubTypes.contains("ScientificStudyTask")) {
            slTaskSubTypes.remove("ScientificStudyTask");
        }
        StringList slTaskSubTypesIntNames = new StringList();
        if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equalsIgnoreCase(policy)){

            String[] typeArray = new String[]{
                    ProgramCentralConstants.TYPE_MILESTONE,
                    ProgramCentralConstants.TYPE_GATE};

            Map<String,StringList> derivativeMap = ProgramCentralUtil.getDerivativeTypeListFromUtilCache(context, typeArray);

            StringList gateSubType      = derivativeMap.get(ProgramCentralConstants.TYPE_GATE);
            StringList milestoneSubType = derivativeMap.get(ProgramCentralConstants.TYPE_MILESTONE);

            StringList reviewTypeList = new StringList(2);
            reviewTypeList.addAll(gateSubType);
            reviewTypeList.addAll(milestoneSubType);
            slTaskSubTypes.retainAll(reviewTypeList);
        }

        try {
            ComponentsUtil.checkLicenseReserved(context,ProgramCentralConstants.PRG_LICENSE_ARRAY);
        } catch (MatrixException e) {//To restrict DPJ users from creating Gate.
            if(slTaskSubTypes.contains(ProgramCentralConstants.TYPE_GATE)){
                slTaskSubTypes.remove(ProgramCentralConstants.TYPE_GATE);
            }
        }

        String language = context.getSession().getLanguage();
        for(int i=0,size=slTaskSubTypes.size();i<size;i++) {
            String type = slTaskSubTypes.get(i);
            String i18nTaskTypeName = EnoviaResourceBundle.getTypeI18NString(context, type, language);
            slTaskSubTypesIntNames.add(i18nTaskTypeName);
        }

        mapTaskTypeNames.put("field_choices", slTaskSubTypes);
        mapTaskTypeNames.put("field_display_choices", slTaskSubTypesIntNames);
        return mapTaskTypeNames;
    }


    /* Copy Base by caipan
      This method will update the value of Percentage Complete attribute of a Task.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - New Value String
     *        1 - Old Value  String
     * @return void
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public void updateTaskPercentageComplete(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map mpParamMap = (HashMap)programMap.get("paramMap");
        String strNewVal = (String)mpParamMap.get("New Value");
        String objId = (String)mpParamMap.get("objectId");
        String isSummary = (String)mpParamMap.get("isSummary");
        String strTaskName = (String)mpParamMap.get("taskName");
        boolean isSummaryTask =false;

        Task task = (Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
        task.setId(objId);
        if(ProgramCentralUtil.isNullString(isSummary)){

            ///ADDED for 358231
            StringList slSubtasks = task.getInfoList(context, SELECT_SUBTASK_IDS);
            isSummaryTask = !(slSubtasks == null || slSubtasks.size() == 0);
        }else{
            isSummaryTask = Boolean.parseBoolean(isSummary);
        }

        if (isSummaryTask)
        {
            //Error
            if(ProgramCentralUtil.isNullString(strTaskName)){
                strTaskName = task.getInfo(context, DomainObject.SELECT_NAME);
            }
            String strErrorMsg = "emxProgramCentral.WBS.PercentageCompletedCannotChangeForParent";
            String sKey[] = {"TaskName"};
            String sValue[] = {strTaskName};

            String companyName = null;
            strErrorMsg  = emxProgramCentralUtil_mxJPO.getMessage(context,
                    strErrorMsg,
                    sKey,
                    sValue,
                    companyName);
            emxContextUtilBase_mxJPO.mqlNotice(context, strErrorMsg);
            return;
        }
        ///END

        //updateTaskMap(context, objId, "percentComplete", strNewVal);
        Map objectValues = new HashMap(1);
        objectValues.put("percentComplete", strNewVal);

        Map objectList = new HashMap(1);
        objectList.put(objId, objectValues);
        task.updateDates(context, objectList, false, false);

        if(100 != Task.parseToDouble(strNewVal)){
            //task.rollupAndSave(context);
        }


        /* DLK Removed; replaced by date rollup call.
        // get the states for the object
        StringList busSelects = new StringList(1);
        busSelects.add(task.SELECT_PERCENT_COMPLETE);
        Map taskMap           = task.getInfo(context, busSelects);
        String oldPercent     = (String) taskMap.get(task.SELECT_PERCENT_COMPLETE);
        if(strNewVal == null) {
            strNewVal = "0.0";
        } else if(strNewVal.indexOf("%")>-1) {
            strNewVal = strNewVal.substring(0,strNewVal.indexOf("%"));
        }
        if(oldPercent == null) {
            strNewVal = "0.0";
        } else if(oldPercent.indexOf("%")>-1) {
            oldPercent = oldPercent.substring(0,oldPercent.indexOf("%"));
        }
        double oldPercentValue     = Task.parseToDouble(oldPercent);
        double currentPercentValue = Task.parseToDouble(strNewVal);
        if(currentPercentValue != oldPercentValue) { //begining of if 1
                        // set the percent complete to value given by user
                        task.setAttributeValue(context, task.ATTRIBUTE_PERCENT_COMPLETE, strNewVal);
         } //end of if 1
         */
    }//end of the method
    /**
     * Get assignee Id list. copy by base 对于不是项目任务的情况下不需要找项目成员
     * @param context - The ENOVIA <code>Context</code> object.
     * @param args - The args hold information about object.
     * @return assignee Id list.
     * @throws MatrixException if Operation fails.
     */
    @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList includeMembersToAddAsAssignee(Context context, String[] args)throws MatrixException
    {
        StringList returnList = new StringList();
        try {
            Map programMap = (HashMap) JPO.unpackArgs(args);
            String objectId = (String)programMap.get("objectId");
            com.matrixone.apps.common.Task task = (com.matrixone.apps.common.Task)DomainObject.newInstance(context,DomainConstants.TYPE_TASK, DomainConstants.PROGRAM);
            task.setId(objectId);
            String type = task.getInfo(context,DomainConstants.SELECT_TYPE);

            StringList busSelect = new StringList(SELECT_ID);
            if(type.equalsIgnoreCase("Task")||type.equalsIgnoreCase("JF_ESOTask")) {
                Map projectInfo = task.getProject(context, busSelect);
                String strProjectId = (String) projectInfo.get(DomainConstants.SELECT_ID);

                DomainObject project = DomainObject.newInstance(context, strProjectId);
                returnList = project.getInfoList(context, ProgramCentralConstants.SELECT_MEMBER_ID);
            }else{//其他Task任务子类型类型
                returnList =JF_Util_mxJPO.mapList2StringList(findObjects(context,DomainConstants.TYPE_PERSON,"*","current==Active", busSelect),DomainConstants.SELECT_ID);
            }

        } catch(Exception ex){
            throw new MatrixException(ex);
        }

        return returnList;
    }
    /** copy by base add by caipan 20251128 目的是取消审批到完成状态的自动操作
     * Modify action for  Percentage complete attribute
     * Based on the %age set the trigger will promote or demote
     * the object to the required state
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     * @throws Exception if operation fails
     * @since AEF 9.5.1.3
     */
    public int triggerModifyPercentCompleteAction(Context context, String[] args) throws Exception {
        logger.info("triggerModifyPercentCompleteAction");
        StringList busSelects = new StringList();
        busSelects.add(SELECT_PERCENT_COMPLETE);
        busSelects.add(SELECT_CURRENT);
        busSelects.add(SELECT_STATES);
        busSelects.add(SELECT_PARENT_ID);
        busSelects.add(SELECT_TYPE);
        busSelects.add(SELECT_POLICY);
        busSelects.add(ProgramCentralConstants.SELECT_NEEDS_REVIEW);
        busSelects.add("from[Subtask]");
        busSelects.add("from[Object Route]");
        busSelects.add("from[Object Route].to.current");
        busSelects.add(ProgramCentralConstants.SELECT_IS_GATE);
        busSelects.add(ProgramCentralConstants.SELECT_IS_MILESTONE);

        try {
            // get values from args.
            String objectId = args[0];
            if(UIUtil.isNotNullAndNotEmpty(objectId)) {
                setId(objectId);

                Map taskMap = getInfo(context, busSelects);
                String type = (String) taskMap.get(SELECT_TYPE);
                String state = (String) taskMap.get(SELECT_CURRENT);
                StringList taskStateList = (StringList) taskMap.get(SELECT_STATES);
                String parentId = (String) taskMap.get(SELECT_PARENT_ID);
                String newPercent = (String) taskMap.get(SELECT_PERCENT_COMPLETE);
                double newPercentValue = Task.parseToDouble(newPercent);
                logger.info("newPercentValue:{}",newPercentValue);
                String taskPolicy = (String) taskMap.get(SELECT_POLICY);
                String taskReviewCheck = (String)taskMap.get(ProgramCentralConstants.SELECT_NEEDS_REVIEW);
                String isSummaryTask = (String)taskMap.get("from[Subtask]");
                String isTaskKindOfGate = (String)taskMap.get(ProgramCentralConstants.SELECT_IS_GATE);
                String isTaskKindOfMilestone = (String)taskMap.get(ProgramCentralConstants.SELECT_IS_MILESTONE);

                //Added to fix the cloud issue. this is only happen when route configured between review and complete state.
                String hasRoute = (String)taskMap.get("from[Object Route]");
                boolean blockPromoteEvent=false;
                if("true".equalsIgnoreCase(hasRoute)) {
                    String routeCurrentState    = (String)taskMap.get("from[Object Route].to.current");
                    if(!"Complete".equalsIgnoreCase(routeCurrentState)&&!"Archive".equalsIgnoreCase(routeCurrentState)) {
                        blockPromoteEvent=true;
                    }
                }
                //get the position of the task's current state wrt
                //to its state list
                int taskCurrentPosition = taskStateList.indexOf(state);
                //get the position of "Active" and "Complete" in the state list
                int taskActiveStatePosition = taskStateList.indexOf(STATE_PROJECT_TASK_ACTIVE);
                int taskCompleteStatePosition = taskStateList.indexOf(STATE_PROJECT_TASK_COMPLETE);

                if ((taskActiveStatePosition == -1 && !("true".equalsIgnoreCase(isTaskKindOfGate) || "true".equalsIgnoreCase(isTaskKindOfMilestone)))
                        || taskCompleteStatePosition == -1)   //Added::nr2:PRG:R210:14-06-2010:For Stage Gate Highlight
                {
                    //object may not be of type task or project
                    if(!ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(taskPolicy))
                        return 0;
                }

                //1. if newpercent is 0% and the task is above active state demote it
                //    to active state. Do nothing if it is below active state
                //2. if newpercent is between 1-99% irrespective of the current state
                //    set the state to Active
                //3. if newPercent is 100% and the task is already in Complete or
                //    beyond do nothing
                //4. if newPercent is 100% and the task is below Complete state setState
                //    to review or complete based on taskReviewCheck.
                //5. if taskReviewCheck = True...Set state to Review.
                //   taskReviewCheck = False...Set state to Compleate.

                //Since there is no Active state in Project Review Policy, taskActiveStatePosition will be -1 and control
                //will never go in conditions where <anything> > taskActiveStatePosition
                //Hence we make this 2 (as for Project Task Policy), to enable comparison to take place.

                if(taskActiveStatePosition == -1 && ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(taskPolicy)) {
                    taskActiveStatePosition = 2;
                    taskCompleteStatePosition = 4;
                }
                if((newPercentValue == 0) && taskCurrentPosition > taskActiveStatePosition) {
                    //Refer #1
                    if("true".equalsIgnoreCase(isTaskKindOfGate) || "true".equalsIgnoreCase(isTaskKindOfMilestone)){
                        changeStateTo(context,objectId, ProgramCentralConstants.STATE_PROJECT_REVIEW_CREATE);
                    } else {
                        //This condition is added if ProjectSpace is governed by Project Space Hold Cancel Policy
                        int activeStatePosition = taskStateList.indexOf(STATE_PROJECT_TASK_ACTIVE);
                        if(activeStatePosition != -1){
                            changeStateTo(context,objectId, STATE_PROJECT_TASK_CREATE);
                        }
                    }
                } else if(newPercentValue > 0 && newPercentValue < 100) {
                    //Refer #2
                    if (taskActiveStatePosition > taskCurrentPosition) {
                        //Do not set states if this is "Part Quality Plan" object
                        if(!TYPE_PART_QUALITY_PLAN.equals(type)) {
                            //This is added so that Gate will not be Moved to assign state.
                            if(!("true".equalsIgnoreCase(isTaskKindOfGate) || "true".equalsIgnoreCase(isTaskKindOfMilestone)) && !ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(taskPolicy)){
                                changeStateTo(context, objectId, STATE_PROJECT_TASK_ACTIVE);
                            }
                        }
                        //Coming in this condition if Gate/Milestone (with policy Project Review)
                        //is in Create state and %Complete is set between 1-99%.
                        if("true".equalsIgnoreCase(isTaskKindOfGate) || "true".equalsIgnoreCase(isTaskKindOfMilestone)){
                            changeStateTo(context, objectId, ProgramCentralConstants.STATE_PROJECT_REVIEW_REVIEW);
                        } else if(ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(taskPolicy)) {
                            HashMap programMap = new HashMap();
                            programMap.put(SELECT_ID, parentId);
                            programMap.put(SELECT_CURRENT, STATE_PROJECT_SPACE_REVIEW);
                            String[] arrJPOArgs = JPO.packArgs(programMap);
                            JPO.invoke(context, "emxProgramCentralUtilBase", null, "setPreviousState",arrJPOArgs,String.class);
                        }
                    } else if (taskActiveStatePosition < taskCurrentPosition) {
                        //control will come in this if condition when Gate/Milestone is in Review/Complete state.
                        //We do not want to move the Gate to Create state (As there is only this state below current state)
                        //of the Gate.Also Gate should not be demoted from Complete state.
                        if(!("true".equalsIgnoreCase(isTaskKindOfGate) || "true".equalsIgnoreCase(isTaskKindOfMilestone)) && !ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(taskPolicy)){ //End:nr2:PRG:R210:14-06-2010:For Stage Gate Highlight
                            changeStateTo(context, objectId, STATE_PROJECT_TASK_ACTIVE);
                        }
                    }
                } else if((newPercentValue == 100) && taskCurrentPosition < taskCompleteStatePosition) {
                    //At this Position we do not want any further comparison to be made for Project Review Policy
                    //So we will set taskActiveStatePosition as -1, which we had set to 2 earlier.
                    /*              if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(taskPolicy)){
                    taskActiveStatePosition = -1;
                }
                     */
                    //Refer #3 and #4
                    //had to break up the promotion to 3 stages
                    if (taskActiveStatePosition > taskCurrentPosition) {
                        if("true".equalsIgnoreCase(isTaskKindOfGate) || "true".equalsIgnoreCase(isTaskKindOfMilestone)){
                            changeStateTo(context, objectId, ProgramCentralConstants.STATE_PROJECT_REVIEW_REVIEW);
                        } else if(ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(taskPolicy)) {
                            HashMap programMap = new HashMap();
                            programMap.put(SELECT_ID, objectId);
                            programMap.put(SELECT_CURRENT, STATE_PROJECT_SPACE_ACTIVE);
                            String[] arrJPOArgs = JPO.packArgs(programMap);
                            JPO.invoke(context, "emxProgramCentralUtilBase", null, "setPreviousState",arrJPOArgs,String.class);
                            setId(objectId);
                        } else {
                            changeStateTo(context, objectId, STATE_PROJECT_TASK_ACTIVE);
                        }
                    }

                    //set to Complete if this is "Part Quality Plan" object
                    //because PQP has different number of states than Project and Task
                    if(!TYPE_PART_QUALITY_PLAN.equals(type)) {
                        if("true".equalsIgnoreCase(isTaskKindOfGate) || "true".equalsIgnoreCase(isTaskKindOfMilestone)){
                            changeStateTo(context, objectId, ProgramCentralConstants.STATE_PROJECT_REVIEW_REVIEW);
                        } else if(ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL.equals(taskPolicy)) {
                            HashMap programMap = new HashMap();
                            programMap.put(SELECT_ID, objectId);
                            programMap.put(SELECT_CURRENT, STATE_PROJECT_SPACE_REVIEW);
                            String[] arrJPOArgs = JPO.packArgs(programMap);
                            JPO.invoke(context, "emxProgramCentralUtilBase", null, "setPreviousState",arrJPOArgs,String.class);
                            setId(objectId);
                        }

                        else {
                            logger.info(" 434 ");
                            changeStateTo(context, objectId, ProgramCentralConstants.STATE_PROJECT_REVIEW_REVIEW);
                            String toState = PropertyUtil.getRPEValue(context, "State", true);

//IR-965099-3DEXPERIENCER2021x
                            boolean checkPassed = true;
                            if("true".equalsIgnoreCase(isSummaryTask)) {
                                logger.info(" 441 ");
                                setId(objectId);
                                checkPassed = checkChildrenStates(context, STATE_PROJECT_TASK_COMPLETE, null);
                            }

                            if(checkPassed && "true".equalsIgnoreCase(isSummaryTask))
                            { //IR-965099-3DEXPERIENCER2021x
                                if(ProgramCentralConstants.POLICY_PROJECT_TASK.equals(taskPolicy)
                                        &&!(STATE_PROJECT_TASK_REVIEW.equalsIgnoreCase(state))
                                        && "NO".equalsIgnoreCase(taskReviewCheck)
                                        && !(STATE_PROJECT_TASK_REVIEW.equalsIgnoreCase(toState))
                                        && !blockPromoteEvent && checkPassed) {
                                    logger.info(" 453 ");
                                    changeStateTo(context, objectId, ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE);
                                }
                            }
                            else if(ProgramCentralConstants.POLICY_PROJECT_TASK.equals(taskPolicy)
                                    && checkPassed
                                    && "NO".equalsIgnoreCase(taskReviewCheck)
                                    && !(STATE_PROJECT_TASK_REVIEW.equalsIgnoreCase(toState))
                                    && !blockPromoteEvent) { //IR-1042109-3DEXPERIENCER2022x
                                logger.info("设置到完成状态");
                                logger.info(" 463 ");
//                                changeStateTo(context, objectId, ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE);//caipan 可能是这个位置
                            }
                        }
                    }
                }
            }
            return 0;
        }
        catch (Exception exp) {
            throw exp;
        }
    }
    private void changeStateTo(Context context,String objectId , String state) throws FrameworkException {

        Task task = new Task();
        task.setId(objectId);

        task.setState(context, state);

    }

    /**
     * Get the list edit access settings for Name column in Prject WBS
     * @param context the ENOVIA <code>Context</code> object.
     * @param args request arguments
     * @return A list of edit access settings for Name column in Project WBS.
     * @throws MatrixException if operation fails.
     */
    public StringList isTaskNameCellEditable(Context context, String[] args) throws MatrixException{
        long start = System.currentTimeMillis();
        StringList isCellEditable = null;
        try{
            Map programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            int size = objectList.size();
            String[] objIds = new String[size];

            for(int i=0;i<size;i++){
                Map objectMap = (Map)objectList.get(i);
                String id = (String)objectMap.get(SELECT_ID);
                objIds[i] = id;
            }
            isCellEditable = new StringList(size);

            StringList busSelect = new StringList();
            busSelect.add(SELECT_CURRENT);
            busSelect.add(ProgramCentralConstants.IS_PASSIVE_TASK);

            BusinessObjectWithSelectList objectWithSelectList = null;
            invokeFromODTFile       = (String)programMap.get("invokeFrom"); //Added for ODT
            if(ODT_TEST_CASE.equalsIgnoreCase(invokeFromODTFile)){ //Added for ODT usage
                objectWithSelectList = ProgramCentralUtil.getObjectWithSelectList(context, objIds,new StringList(SELECT_CURRENT),false);
            }else{
                ContextUtil.pushContext(context);
                objectWithSelectList = ProgramCentralUtil.getObjectWithSelectList(context, objIds,busSelect);
                ContextUtil.popContext(context);
            }
            DomainObject ps = DomainObject.newInstance(context);
            for(int i=0;i<size;i++){
                Map taskInfoMap = (Map) objectList.get(i);
                String level = (String) taskInfoMap.get("level");
                String id = (String) taskInfoMap.get(SELECT_ID);
                ps.setId(id);
                String isInvokedInNewTab = (String) taskInfoMap.get("invokedFromNewTab");
                BusinessObjectWithSelect bws = objectWithSelectList.getElement(i);
                String currentState          = bws.getSelectData(SELECT_CURRENT);
                String isPassiveSubtask      = bws.getSelectData(ProgramCentralConstants.IS_PASSIVE_TASK);
                if(ps.isKindOf(context,"Project Space")){
                    isCellEditable.add("false");
                }
                if(ProgramCentralConstants.STATE_PROJECT_TASK_COMPLETE.equals(currentState) ||
                        ProgramCentralConstants.STATE_PROJECT_SPACE_ARCHIVE.equals(currentState)){
                    isCellEditable.add("false");
                }else if (isPassiveTaskSelected(context,args) && "TRUE".equalsIgnoreCase(isPassiveSubtask) && ("true".equalsIgnoreCase(isInvokedInNewTab) || "2".equalsIgnoreCase(level))) {
                    //1.Passive Task is always at level 2 in "Passive Tasks" View. 2.Prevent edit when placeholder added as passive Subtask.
                    //3.Placeholder when opened in New tab, Passive at level 1, Check flag "invokedFromNewTab".
                    isCellEditable.add("false");
                }else {
                    isCellEditable.add("true");
                }
            }

            DebugUtil.debug("Total time taken by isTaskNameCellEditable:"+(System.currentTimeMillis()-start));

        }catch(Exception e){
            //e.printStackTrace();
            DebugUtil.debug("isTaskNameCellEditable "+ e.getMessage());
        }
        return isCellEditable;

    }
}
