//
// $Id: emxProjectSpace.java.rca 1.6 Wed Oct 22 16:21:26 2008 przemek Experimental przemek $ 
//
// emxProjectSpace.java
//
// Copyright (c) 2002-2020 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//
import com.dassault_systemes.enovia.e6wv2.foundation.*;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxb.Datacollection;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxb.Dataobject;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxb.ServiceParameters;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxb.Servicedata;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxbext.ArgMap;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxbext.DataelementMapAdapter;
import com.dassault_systemes.enovia.e6wv2.foundation.service.ServiceResource;
import com.dassault_systemes.enovia.e6wv2.foundation.util.LicenseUtil;
import com.matrixone.apps.common.ContentReplicateOptions;
import com.matrixone.apps.common.Issue;
import com.matrixone.apps.common.TaskHolder;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.PostProcessCallable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import jakarta.servlet.http.HttpServletRequest;
import matrix.db.*;
import matrix.util.StringList;
import com.matrixone.apps.program.*;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.dassault_systemes.enovia.e6wv2.foundation.jaxbext.*;
import com.dassault_systemes.enovia.e6wv2.foundation.jaxb.*;
import org.slf4j.LoggerFactory;

/**
 * The <code>emxProjectSpace</code> class represents the Project Space JPO
 * functionality for the AEF type.
 *
 * @version AEF 10.0.SP4 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxProjectSpace_mxJPO extends emxProjectSpaceBase_mxJPO
{
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(emxProjectSpace_mxJPO.class);
    private static List<String> EMPTY_LIST = Collections.unmodifiableList(new ArrayList(0));

    private String Attr_ProjectRole = "Project Role";
    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     * @grade 0
     */
    public emxProjectSpace_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }

    /**
     * Constructs a new emxProjectSpace JPO object.
     *
     * @param
     * @param
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     */
    public emxProjectSpace_mxJPO (String id)
        throws Exception
    {
        // Call the super constructor
        super(id);
    }
    /*
     * @description: 量产年份 Range 值
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return: 
     **/
    public Map getDateOfMassProduction(Context context , String[] args)throws Exception{
        Map policyMap = new HashMap();
        policyMap.put("field_choices",  getLastFiveYears());
        policyMap.put("field_display_choices",  getLastFiveYears());
        return policyMap;
    }
    /*
     * @description: 获取近5年的年份
     * @author: caipan
     * @date:
     * @param:
     * @return:
     **/
    public static StringList getLastFiveYears() {
        StringList years = new StringList();
        int currentYear = Year.now().getValue();
        int lastYear = currentYear-1;
        for (int i = 0; i < 10; i++) {
            years.add(String.valueOf(currentYear + i).substring(2));
        }
        years.add(String.valueOf(String.valueOf(lastYear).substring(2)));
        return years;
    }

    /**
     * Create new blank project space object.
     * @param context - The eMatrix <code>Context</code> object.
     * @param args holds information about object.
     * @return newly created obejct id;
     * @throws Exception if operation fails.
     */
    @com.matrixone.apps.framework.ui.CreateProcessCallable
    public Map createNewProject(Context context,String[]args)throws Exception
    {
        ProjectSpace project =(ProjectSpace) DomainObject.newInstance(context,
                ProgramCentralConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);

        ProjectSpace newProject =(ProjectSpace)DomainObject.newInstance(context,
                ProgramCentralConstants.TYPE_PROJECT_SPACE,DomainConstants.PROGRAM);

        Map <String,String>returnMap = new HashMap();
        try{
            ContextUtil.startTransaction(context, true);

            String SCHEDULE_FROM = PropertyUtil.getSchemaProperty(context,"attribute_ScheduleFrom");
            Map <String,String>attributeMap = new HashMap();
            Map <String,String>basicProjectInfo = new HashMap();
            Map <String,String>relatedProjectInfo = new HashMap();

            Map programMap 					= (HashMap) JPO.unpackArgs(args);
            String objectId 				= (String)programMap.get("objectId");
            String createProject 			= (String)programMap.get("createProject");
            String projectName 				= (String)programMap.get("Name");
            String projectAutoName 			= (String)programMap.get("autoNameCheck");
            String projectDescrption 		= (String)programMap.get("Description");
            String businessUnitId 			= (String)programMap.get("BusinessUnitOID");
            String programId 				= (String)programMap.get("ProgramOID");
            String businessGoalId 			= (String)programMap.get("BusinessGoalOID");
            String baseCurrency 			= (String)programMap.get("BaseCurrency");
            String projectVault 			=  project.DEFAULT_VAULTS;
            String projectVisibility 		= (String)programMap.get("ProjectVisibility");
            String projectPolicy 			= (String)programMap.get("Policy");
            String projectScheduleFrom 		= (String)programMap.get("ScheduleFrom");
            String projectDate 				= (String)programMap.get("ProjectDate");
            String defaultConstraintType 	= (String)programMap.get("DefaultConstraintType");
            String projectSpaceType 		= (String)programMap.get("TypeActual");
            String selectedProjectId 		= (String)programMap.get("SeachProjectOID");
            String connectRelatedProjects 	= (String)programMap.get("connectRelatedProject");
            String connectPassiveTasks 		= (String)programMap.get("connectPassiveTasks");


            String copyFinancialData 		= (String)programMap.get("financialData");
            String copyFolderData 			= (String)programMap.get("folders");
            String sKeepSourceConstraints 	= (String)programMap.get("keepSourceConstraints");
            String sKeepSourceColors 		= (String)programMap.get("keepSourceColors");

            String refernceDocument 		= (String)programMap.get("ReferenceDocument");
            String deliverabletId 			= (String)programMap.get("DeliverableOID");
            String calendarId 				= (String)programMap.get("CalendarOID");
            String JF_ProjType 				= (String)programMap.get("JF_ProjType");
            String JF_ProducingArea 				= (String)programMap.get("JF_ProducingArea");
            String JF_ProductType 				= (String)programMap.get("JF_ProductType");
            String JF_DateOfMassProduction 				= (String)programMap.get("JF_DateOfMassProduction");
            String copyName 				= (String)programMap.get("copyName");

            String newProjectId 			= DomainConstants.EMPTY_STRING;
logger.info("objectId:{} selectedProjectId:{}",objectId,selectedProjectId);
            if(ProgramCentralUtil.isNullString(objectId) && ProgramCentralUtil.isNotNullString(selectedProjectId)){
                objectId = selectedProjectId;
            }

            StringList calendarIds = FrameworkUtil.split(calendarId, "|");

            Locale locale	=	(Locale)programMap.get("localeObj");
            if(locale == null) {
                locale	=	context.getLocale();
            }

            String strTimeZone 				= (String)programMap.get("timeZone");
            double dClientTimeZoneOffset 	= (Double.valueOf(strTimeZone)).doubleValue();
            //  IR-528127-3DEXPERIENCER2018x
            boolean isECHInstalled =  FrameworkUtil.isSuiteRegistered(context,
                    "appVersionEnterpriseChange",false,null,null);
            if(isECHInstalled){
                if(mxType.isOfParentType(context, projectSpaceType, DomainObject.TYPE_CHANGE_PROJECT))
                    programId = (String)programMap.get("ECHMandProgramOID");
            }
            //end  IR-528127-3DEXPERIENCER2018x

            if(ProgramCentralUtil.isNotNullString(projectDate)){
  				/*projectDate = projectDate.trim();
  				projectDate = eMatrixDateFormat.getFormattedInputDate(context,projectDate,dClientTimeZoneOffset,locale);*/
                TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
                double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
                dClientTimeZoneOffset = (Double.valueOf(dbMilisecondsOffset/(1000*60*60))).doubleValue();
                projectDate = projectDate.trim();
                int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
                String strInputTime = eMatrixDateFormat.adjustTimeStringForInputFormat("");
                projectDate = eMatrixDateFormat.getFormattedInputDateTime(projectDate, strInputTime, iDateFormat, dClientTimeZoneOffset, locale);
            }

            //For program,Businessgoal and related project
            if(ProgramCentralUtil.isNotNullString(objectId)){
                StringList selectable = new StringList();
                selectable.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
                selectable.addElement(ProgramCentralConstants.SELECT_IS_PROGRAM);
                selectable.addElement(ProgramCentralConstants.SELECT_IS_BUSINESS_GOAL);

                DomainObject parentObject = DomainObject.newInstance(context,objectId);
                Map <String,String>parentObjectInfo = parentObject.getInfo(context, selectable);
                String isProjectSpace 				= parentObjectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
                String isProgram 					= parentObjectInfo.get(ProgramCentralConstants.SELECT_IS_PROGRAM);
                String isBusinessGoal 				= parentObjectInfo.get(ProgramCentralConstants.SELECT_IS_BUSINESS_GOAL);

                if("true".equalsIgnoreCase(isProgram)){
                    programId = objectId;
                }else if("true".equalsIgnoreCase(isBusinessGoal)){
                    businessGoalId = objectId;
                }else if("true".equalsIgnoreCase(isProjectSpace)){
                    relatedProjectInfo.put("AddAsChild", "true");
                    relatedProjectInfo.put("RelatedProjectId", objectId);
                }
            }

            //Project space attribute map values
            attributeMap.put(DomainObject.ATTRIBUTE_TASK_ESTIMATED_START_DATE, projectDate);
            attributeMap.put(DomainObject.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE, projectDate);
            attributeMap.put(DomainObject.ATTRIBUTE_TASK_ESTIMATED_DURATION, "0.0");
            attributeMap.put(DomainObject.ATTRIBUTE_PROJECT_VISIBILITY, projectVisibility);
            attributeMap.put(DomainObject.ATTRIBUTE_CURRENCY, baseCurrency);
            attributeMap.put(SCHEDULE_FROM, projectScheduleFrom);
            attributeMap.put(DomainObject.ATTRIBUTE_DEFAULT_CONSTRAINT_TYPE, defaultConstraintType);

            //Baseline attributes should not have any values while project creation.
            attributeMap.put(DomainObject.ATTRIBUTE_BASELINE_INITIAL_START_DATE, ProgramCentralConstants.EMPTY_STRING);
            attributeMap.put(DomainObject.ATTRIBUTE_BASELINE_INITIAL_END_DATE, ProgramCentralConstants.EMPTY_STRING);
            attributeMap.put(DomainObject.ATTRIBUTE_BASELINE_CURRENT_START_DATE, ProgramCentralConstants.EMPTY_STRING);
            attributeMap.put(DomainObject.ATTRIBUTE_BASELINE_CURRENT_END_DATE, ProgramCentralConstants.EMPTY_STRING);
            //add by caipan 生成项目编码
            boolean iscopy = false;
            if(UIUtil.isNullOrEmpty((copyName))) {
                String code = generateCode(context, JF_DateOfMassProduction);
                StringBuffer projectNameStr = new StringBuffer();
                projectNameStr.append(JF_ProjType);
                projectNameStr.append(JF_ProducingArea);
                projectNameStr.append(JF_ProductType);
                projectNameStr.append(JF_DateOfMassProduction);
                projectNameStr.append("-");
                projectNameStr.append(code);
                projectName = projectNameStr.toString();
                logger.info("ProjectName:{}", projectName);
            }else{
                projectName = copyName;
                iscopy = true;
            }
            //end
            //get auto name
            if(ProgramCentralUtil.isNullString(projectName) && projectAutoName.equalsIgnoreCase("true")){
                String symbolicTypeName = PropertyUtil.getAliasForAdmin(context, "Type", projectSpaceType, true);
                String symbolicPolicyName = PropertyUtil.getAliasForAdmin(context, "Policy", projectPolicy, true);

                projectName = FrameworkUtil.autoName(context,
                        symbolicTypeName,
                        null,
                        symbolicPolicyName,
                        null,
                        null,
                        true,
                        true);
            }

            if("Clone".equalsIgnoreCase(createProject) || "Template".equalsIgnoreCase(createProject)){

                SimpleDateFormat dateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),Locale.US);
                Calendar constraintDate = Calendar.getInstance();
                Date newDate = null;
                newDate = dateFormat.parse(projectDate);
                constraintDate.setTime(newDate);
                if(defaultConstraintType.equalsIgnoreCase(ATTRIBUTE_TASK_CONSTRAINT_TYPE_RANGE_ASAP)){
                    constraintDate.set(Calendar.HOUR_OF_DAY, 8);
                } else {
                    constraintDate.set(Calendar.HOUR_OF_DAY, 17);
                }
                constraintDate.set(Calendar.MINUTE, 0);
                constraintDate.set(Calendar.SECOND, 0);
                projectDate = dateFormat.format((constraintDate.getTime()));
                attributeMap.put(DomainObject.ATTRIBUTE_TASK_CONSTRAINT_DATE, projectDate);


                String copyAssignees = (String)programMap.get("assignees");
                String copyDeliverables = (String)programMap.get("deliverables");
                String copyProjectMembers = (String)programMap.get("projectMembers");
                Dataobject inputProject = new Dataobject();

                if(ProgramCentralUtil.isNullString(copyAssignees)){
                    copyAssignees = "false";
                }
                if(ProgramCentralUtil.isNullString(copyDeliverables)){
                    copyDeliverables = "false";
                }
                if(ProgramCentralUtil.isNullString(copyProjectMembers)){
                    copyProjectMembers = "false";
                }

                if(calendarIds.size() > 0){
                    Datacollection calendars = new Datacollection();
                    for(String locaCalendarId : calendarIds){
                        calendars.getDataobjects().add(new Dataobject() {
                            {setId(locaCalendarId);}
                        });
                    }
                    RelateddataMapAdapter.addRelatedData(inputProject, "calendar", calendars);
                }

                ContentReplicateOptions selectedOptionForReferenceDocument = ContentReplicateOptions.COPY;
                if(ProgramCentralUtil.isNullString(refernceDocument) || "Reference".equalsIgnoreCase(refernceDocument)){
                    selectedOptionForReferenceDocument = ContentReplicateOptions.CONNECT_EXISTING;
                }
                // projectDate date format required for both if-else condition
                projectDate = com.dassault_systemes.enovia.e6wv2.foundation.util.FormatUtil.getFormattedISODate(context, constraintDate.getTime(), null /*SimpleDateFormat*/);

                if("Clone".equalsIgnoreCase(createProject)) {
                    //create the data.
                    //inputProject.setId(objectId);

                    Datacollection createFromProject = new Datacollection();
                    Dataobject sourceObject = new Dataobject();
                    sourceObject.setId(objectId);
                    createFromProject.getDataobjects().add(sourceObject);
                    RelateddataMapAdapter.addRelatedData(inputProject, "createFromProject", createFromProject);

                    DataelementMapAdapter.setDataelementValue(inputProject, "title",projectName);
                    DataelementMapAdapter.setDataelementValue(inputProject, "constraintDate",projectDate);
                    DataelementMapAdapter.setDataelementValue(inputProject, "Constraint Date",projectDate);
                    DataelementMapAdapter.setDataelementValue(inputProject, "currency", baseCurrency);
                    DataelementMapAdapter.setDataelementValue(inputProject, "Description",projectDescrption );
                    DataelementMapAdapter.setDataelementValue(inputProject, "Default Constraint Type", defaultConstraintType);
                    DataelementMapAdapter.setDataelementValue(inputProject, "Schedule from", projectScheduleFrom);

                    final Servicedata inputService = new Servicedata();
                    inputService.getData().add(inputProject);

                    ServiceParameters lParameters = new ServiceParameters();
                    final String serviceName = "dpm.projects";
                    lParameters.setServiceName(serviceName + "/copy");

                    ArgMap args1 = new ArgMap();
                    lParameters.setServiceArgs(args1);
                    //check with create
                    lParameters.setDefaultUpdateAction(UpdateActions.MODIFY);

                    args1.put("copyTaskConstraint", sKeepSourceConstraints);
                    args1.put("copyBookmarks", copyFolderData);
                    args1.put("copyFinancialData", copyFinancialData);
                    args1.put("copyRelatedProjects", connectRelatedProjects);
                    args1.put("copyPassiveTasks", connectPassiveTasks);
                    args1.put("copyTaskDeliverables", copyDeliverables);
                    args1.put("copyTaskAssignees", copyAssignees);
                    args1.put("copyProjectMembers", copyProjectMembers);
                    args1.put("copyTaskReferences", "true"); // Attachments are true for every case

                    Servicedata  response = ServiceBase.saveData(context, inputService, lParameters, 0);
                    newProjectId = (String)response.getData().get(0).getId();
                    newProject.setId(newProjectId);
                    //vur1 : FUN122007
                }else{
                    Map requestMap = (Map) programMap.get("RequestValuesMap");
                    String resourceTemplateId = (String)programMap.get("ResourceTemplate");
                    String type = (String)programMap.get("TypeActual");
                    inputProject.setType(type);
                    //add by caipan
                    DomainObject program = DomainObject.newInstance(context);
                    program.setId(objectId);
                    Datacollection createFromProjectTemplate = new Datacollection();
                    Dataobject sourceObject = new Dataobject();
                    if(program.isKindOf(context, DomainConstants.TYPE_PROGRAM)){
                        sourceObject.setId(selectedProjectId);
                        sourceObject.setType(DomainConstants.TYPE_PROJECT_TEMPLATE);
                    }else {
                        sourceObject.setId(objectId);
                    sourceObject.setType(DomainConstants.TYPE_PROJECT_TEMPLATE);
                    }
                    //end
    logger.info("376 objectId:{}",objectId);
                    //creating question-response list
                    String loggedInUser = context.getUser();
                    String questionResponseValue 	= (String) CacheUtil.getCacheObject(context, loggedInUser+"_QuestionsResponse");
                    if(ProgramCentralUtil.isNotNullString(questionResponseValue)){
                        Datacollection questionResponseList = new Datacollection();
                        StringList questionResponseValueList = FrameworkUtil.split(questionResponseValue, "|");
                        for(int i=0;i<questionResponseValueList.size();i++){
                            String questionRValue = (String)questionResponseValueList.get(i);
                            StringList questionActualRList = FrameworkUtil.split(questionRValue, "=");
                            Dataobject question = new Dataobject();
                            question.setId(new DomainObject(questionActualRList.get(0)).getPhysicalId(context));
                            DataelementMapAdapter.setDataelementValue(question, "questionResponse", questionActualRList.get(1));
                            DataelementMapAdapter.setDataelementValue(question, "id", questionActualRList.get(0));
                            DataelementMapAdapter.setDataelementValue(question, "type", ProgramCentralConstants.TYPE_QUESTION);
                            questionResponseList.getDataobjects().add(question);
                        }
                        System.out.println("in from Template questionResponseValueList : "+questionResponseValueList);

                        RelateddataMapAdapter.addRelatedData(sourceObject, "questions", questionResponseList);
                    }
                    createFromProjectTemplate.getDataobjects().add(sourceObject);
                    RelateddataMapAdapter.addRelatedData(inputProject, "createFromProjectTemplate", createFromProjectTemplate);
                    DataelementMapAdapter.setDataelementValue(inputProject, "title",projectName);
                    DataelementMapAdapter.setDataelementValue(inputProject, "constraintDate",projectDate);
                    if(ProgramCentralUtil.isNotNullString(resourceTemplateId)){
                        DataelementMapAdapter.setDataelementValue(inputProject,"resourcePlanTemplateId",ProgramCentralUtil.getConnectionPhysicalId(context,resourceTemplateId));
                    }

                    final Servicedata inputService = new Servicedata();
                    inputService.getData().add(inputProject);

                    ServiceParameters lParameters = new ServiceParameters();
                    final String serviceName = "dpm.projects";
                    lParameters.setServiceName(serviceName + "/copy");

                    ArgMap args1 = new ArgMap();
                    lParameters.setServiceArgs(args1);
                    lParameters.setDefaultUpdateAction(UpdateActions.MODIFY);

                    args1.put("copyTaskConstraint", sKeepSourceConstraints);
                    args1.put("copyFinancialData", copyFinancialData);
                    args1.put("copyBookmarks", copyFolderData);
                    args1.put("copyTaskDeliverables", copyDeliverables);
                    args1.put("copyTaskReferences", "true"); // Attachments are true for every case
                    args1.put("referencesReplicateOption", selectedOptionForReferenceDocument.toString());
                    args1.put("is3DSpaceCall", "true");

                    Servicedata  response = ServiceBase.saveData(context, inputService, lParameters, 0);
                    newProjectId = (String)response.getData().get(0).getId();
                    newProject.setId(newProjectId);
                }
            }

            //builds basic project info map
            basicProjectInfo.put("name", projectName);
            basicProjectInfo.put("type", projectSpaceType);
            basicProjectInfo.put("policy", projectPolicy);
            basicProjectInfo.put("vault", projectVault);
            basicProjectInfo.put("description", projectDescrption);

            //Builds related project info map
            relatedProjectInfo.put("programId", programId);
            relatedProjectInfo.put("businessUnitId", businessUnitId);
            relatedProjectInfo.put("businessGoalId", businessGoalId);
            relatedProjectInfo.put("deliverableId", deliverabletId);

            boolean isCopyFolderData	= true;
            boolean isCopyFinancialData = true;
            boolean keepSourceConstraints = true;
            boolean keepSourceColors = true;

            if(ProgramCentralUtil.isNullString(copyFolderData) || "false".equalsIgnoreCase(copyFolderData)){
                isCopyFolderData = false;
            }

            if(ProgramCentralUtil.isNullString(copyFinancialData) || "false".equalsIgnoreCase(copyFinancialData)){
                isCopyFinancialData = false;
            }

            if(ProgramCentralUtil.isNullString(sKeepSourceConstraints) || "false".equalsIgnoreCase(sKeepSourceConstraints)){
                keepSourceConstraints = false;
            }
            //Can Comment iF as both statement in If and Else are same
            if(ProgramCentralUtil.isNullString(sKeepSourceColors) || "false".equalsIgnoreCase(sKeepSourceColors)){
                PropertyUtil.setGlobalRPEValue(context, "CopyColorAttribute", "true");
            }else{
                PropertyUtil.setGlobalRPEValue(context, "CopyColorAttribute", "true");
            }


            //Create new project object.
            if("Blank".equalsIgnoreCase(createProject) ||
                    "Import".equalsIgnoreCase(createProject)){

                newProject = project.createBlankProject(context,
                        basicProjectInfo,
                        attributeMap,
                        relatedProjectInfo);

            }else if("Clone".equalsIgnoreCase(createProject) || "Template".equalsIgnoreCase(createProject)){
				/* vur1 : FUN122007
  				boolean isConnectRelatedProject = false;
  				if(ProgramCentralUtil.isNotNullString(connectRelatedProjects) &&
  						connectRelatedProjects.equalsIgnoreCase("True")){
  					isConnectRelatedProject = true;
  				}

  				//create new project from existing object.
  				newProject = project.clone(context,
  						selectedProjectId,
  						basicProjectInfo,
  						relatedProjectInfo,
  						attributeMap,
  						isConnectRelatedProject,
  						isCopyFolderData,
  						isCopyFinancialData,
						keepSourceConstraints);
				*/
                PropertyUtil.setGlobalRPEValue(context, "IGNORE_CREATE_TRIGGER", "true");
                relatedProjectInfo.put("objectId", newProjectId);
                newProject.callUpdateProjectRelatedInfo(context,relatedProjectInfo);
                PropertyUtil.setGlobalRPEValue(context, "IGNORE_CREATE_TRIGGER", "false");
            }
			/*
			else if("Template".equalsIgnoreCase(createProject)){

  				boolean  isTemplateTaskAutoName = false;
  				String questionResponseValue 	= (String) CacheUtil.getCacheObject(context, "QuestionsResponse");
  				String resourceTemplateId 	    = (String)programMap.get("ResourceTemplate");

  				Map <String,String>questionResponseMap = new HashMap<String,String>();
  				if(ProgramCentralUtil.isNotNullString(questionResponseValue)){
  					StringList questionResponseValueList = FrameworkUtil.split(questionResponseValue, "|");
  					for(int i=0;i<questionResponseValueList.size();i++){
  						String questionRValue = (String)questionResponseValueList.get(i);
  						StringList questionActualRList = FrameworkUtil.split(questionRValue, "=");
  						questionResponseMap.put((String)questionActualRList.get(0), (String)questionActualRList.get(1));
  					}
  				}


  				//update related info
  				relatedProjectInfo.put("resourceTemplateId", resourceTemplateId);

  				newProject = project.cloneTemplateToCreateProject(context,
  						selectedProjectId,
  						basicProjectInfo,
  						relatedProjectInfo,
  						attributeMap,
  						questionResponseMap,
  						isTemplateTaskAutoName,
  						isCopyFolderData,
						isCopyFinancialData,
						keepSourceConstraints,
                        selectedOptionForReferenceDocument, calendarIds);

  			}*/
            //Get new project ID
            newProjectId = newProject.getObjectId();

            returnMap.put("id", newProjectId);
            if(!("Template".equalsIgnoreCase(createProject) || "Clone".equalsIgnoreCase(createProject))) {
                // If only one calendar is selected then that will be connected as Default Calendar
                if(calendarIds.size()==1){
                    String defaultCalendarId = calendarIds.get(0) +"|DefaultCalendar";
                    calendarIds.remove(0);
                    calendarIds.add(defaultCalendarId);
                }
                newProject.addCalendars(context, calendarIds);

                ContextUtil.commitTransaction(context);
                //required for Calendars
                Task rollup = new Task(newProjectId);
                rollup.rollupAndSave(context);
            }
            //add by caipan for project Role
            DomainObject newProjectObj= DomainObject.newInstance(context);
            newProjectObj.setId(newProjectId);
            if(iscopy){
                newProjectObj.setAttributeValue(context, "JF_isCopy", "Y");
            }

           StringList selList = new JF_Util_mxJPO().basicBolistSel();
                   selList.add("attribute[Source Id]");
           MapList subTask = newProjectObj.getRelatedObjects(context,DomainConstants.RELATIONSHIP_SUBTASK,DomainConstants.TYPE_TASK_MANAGEMENT,selList,
                    null,false,true,(short)0,null,null,0);
           Map map = null;
           DomainObject subObj = DomainObject.newInstance(context);
           DomainObject SourceObj = DomainObject.newInstance(context);
           for(int i=0;i<subTask.size();i++){
               map = (Map)subTask.get(i);
               String subId = UIUtil.getValue(map, DomainConstants.SELECT_ID);
               String SourceId = UIUtil.getValue(map, "attribute[Source Id]");
               if(UIUtil.isNotNullAndNotEmpty(SourceId)){
               subObj.setId(subId);
               SourceObj.setId(SourceId);
               subObj.setAttributeValue(context,Attr_ProjectRole,SourceObj.getAttributeValue(context, Attr_ProjectRole));
               }



           }
            //end
        }catch(Exception ex){
            ContextUtil.abortTransaction(context);
            ex.printStackTrace();
            if(ex.getMessage().contains("No create access")){
                throw new Exception(EnoviaResourceBundle.getProperty(context, ProgramCentralConstants.PROGRAMCENTRAL,
                        "emxProgramCentral.Project.NoCreateAccess", context.getSession().getLanguage()));
            }
            else{
                throw  ex;
            }
        }finally{
            PropertyUtil.setGlobalRPEValue(context, "CopyColorAttribute", "true");
            PropertyUtil.setGlobalRPEValue(context, "IGNORE_CREATE_TRIGGER", "false");
        }

        return returnMap;
    }
    /*
     * @description: 生成项目编码 流水号每年重置
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
 public static synchronized String generateCode(Context context,String Year) throws Exception{
        String code = "";
        try {
            ContextUtil.pushContext(context);
            String mql = "attribute[JF_DateOfMassProduction]=='" + Year + "' && attribute[JF_isCopy]==N";
            StringList list = new StringList();
            list.add(DomainConstants.SELECT_ID);
            list.add(DomainConstants.SELECT_ORIGINATED);
            list.add(DomainConstants.SELECT_NAME);
            MapList projectList = DomainObject.findObjects(context, DomainConstants.TYPE_PROJECT_SPACE, null, mql, list);
            projectList.sort(DomainConstants.SELECT_ORIGINATED, "descending", "date");
            if (projectList.size() > 0) {
                code = (String) ((Map) projectList.get(0)).get(DomainConstants.SELECT_NAME);
                StringList codeList = FrameworkUtil.split(code, "-");
                code = codeList.get(codeList.size() - 1);
                int num = Integer.parseInt(code) + 1;
                code = String.format("%03d", num);
            } else {
                code = "001";
            }
        }catch (Exception e){
            logger.info(e.getMessage());
        }finally {
            ContextUtil.popContext(context);
        }
     return code;
 }
    /*
     * @description:关键任务，必须要上传交付物
     * 如果有交付物的情况下,交付物状态必须为发布状态
     * @author: caipan
     * @date:
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public int checkProjectTaskDeliverable(Context context,String[] args) throws Exception{
        String objectId = args[0];
        DomainObject taskObj = DomainObject.newInstance(context);
        taskObj.setId(objectId);
        //判断是项目任务
        if(!JF_Util_mxJPO.isProjectTask(context, taskObj)){//不是项目任务直接返回
            return 0;
        }
        Locale sLanguage = context.getLocale();
        //获取属性关键任务
        String isKeyTask = taskObj.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_JF_isKeyTask);
        MapList deliverableList = taskObj.getRelatedObjects(context,DomainConstants.RELATIONSHIP_TASK_DELIVERABLE,DomainConstants.TYPE_DOCUMENT,JF_Util_mxJPO.basicBolistSel(),
                null,false,true,(short)1,null,null,0);
        String checkkeyError =EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource",sLanguage, "emxProgramCentral.task.checkkeyError");
        String checkDeliverableCurrentError =EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource",sLanguage, "emxProgramCentral.task.checkDeliverableCurrentError");
        checkDeliverableCurrentError=taskObj.getInfo(context, DomainConstants.SELECT_NAME)+":"+checkDeliverableCurrentError;
        if("Y".equalsIgnoreCase(isKeyTask)&&deliverableList.size()==0){//必须有交付物，并且交付物是发布状态
            emxContextUtil_mxJPO.mqlNotice(context,checkkeyError);
            return 1;
        }
        Map temp = null;
        String current;
        for(int i=0;i<deliverableList.size();i++){//如果有交付物必须发布
            temp = (Map)deliverableList.get(i);
            current = UIUtil.getValue(temp, DomainConstants.SELECT_CURRENT);
            if(!"RELEASED".equalsIgnoreCase(current)){
                emxContextUtil_mxJPO.mqlNotice(context,checkDeliverableCurrentError);
                return 1;
            }
        }
        return 0;
    }
    /*
     * @description:删除项目的时候，校验有关联变更单、数据外发、数模的时候不让删除
     * @author: caipan
     * @date: 2025/3/26 14:59:02
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public int checkDelProject(Context context,String[] args) throws Exception{
        logger.info("checkDelProject check start");
        String relList = "JFProject2RootPart,JFProject2JFColorMatrix,JFProject2Snapshot,JFChange2Project,JFECR2AffectedProject";//from端
        //数据外发是通过属性来记录的
        String projectId = args[0];
        StringList selList = JF_Util_mxJPO.basicBolistSel();
        DomainObject projectObj = DomainObject.newInstance(context,projectId);
        MapList list = projectObj.getRelatedObjects(context,relList,"*",selList,
                null,true,true,(short)1,null,null,1);
        String error = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Notice.delProjectcheck");
        if(list.size()>0){
            emxContextUtil_mxJPO.mqlNotice(context, error);
            return 1;
        }
        String where ="attribute[JSConnectProject]=='"+projectId+"'";
        MapList projectList = DomainObject.findObjects(context, "JFDataOutSource", "*", where, selList);
        if(projectList.size()>0){
            emxContextUtil_mxJPO.mqlNotice(context, error);
            return 1;
        }
        logger.info("checkDelProject check end");
        return 0;
    }

    @PostProcessCallable
    public static void copyPartialScheduleProcessTask(Context paramContext, String[] paramArrayOfString) throws Exception {
        try {
            Map map1 = (Map)JPO.unpackArgs(paramArrayOfString);
            CacheUtil.removeCacheObject(paramContext, "taskIdList");
            String str1 = paramContext.getUser();
            String str2 = (String)CacheUtil.getCacheObject(paramContext, str1 + "_QuestionsResponse");
            CacheUtil.removeCacheObject(paramContext, str1 + "_QuestionsResponse");
            PropertyUtil.setGlobalRPEValue(paramContext, "CopyColorAttribute", "true");
            String str3 = (String)map1.get("objectId");
            String str4 = (String)map1.get("SeachProjectOID");
            String str5 = (String)map1.get("UseStartAndEndDatesAsEntered");
            String str6 = (String)map1.get("copyDeliverables");
            String str7 = (String)map1.get("copyAssignees");
            String str8 = (String)map1.get("connectPassiveTasks");
            StringList stringList1 = FrameworkUtil.split(str4, "|");
            String[] arrayOfString = new String[stringList1.size()];
            boolean bool1 = false;
            boolean bool2 = false;
            stringList1.toArray((Object[])arrayOfString);
            StringList stringList2 = new StringList();
            stringList2.add("id");
            stringList2.add("type");
            stringList2.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
            stringList2.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE);
            MapList mapList = DomainObject.getInfo(paramContext, arrayOfString, stringList2);
            StringList stringList3 = new StringList();
            DomainObject domainObject = new DomainObject(str3);
            StringList stringList4 = new StringList();
            stringList4.add(ProgramCentralConstants.SELECT_PROJECT_ID);
            stringList4.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
            Map map2 = domainObject.getInfo(paramContext, stringList4);
            String str9 = (String)map2.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);
            String str10 = str3;
            if ("true".equalsIgnoreCase(str9))
                str10 = (String)map2.get(ProgramCentralConstants.SELECT_PROJECT_ID);
            String str11 = (new DomainObject(str10)).getAttributeValue(paramContext, ProgramCentralConstants.ATTRIBUTE_SCHEDULED_FROM);
            if (ProgramCentralUtil.isNullString(str7))
                str7 = "false";
            if (ProgramCentralUtil.isNullString(str6))
                str6 = "false";
            ArgMap argMap = new ArgMap();
            argMap.put("copyTaskDeliverables", str6);
            argMap.put("copyTaskAssignees", str7);
            argMap.put("copyTaskReferences", "true");
            argMap.put("copyChecklists", "true");
            argMap.put("UseStartAndEndDatesAsEntered", str5);
            argMap.put("copyPartialSchedule", "true");
            argMap.put("scheduleFrom", str11);
            argMap.put("copyPassiveTasks", str8);
            if ("true".equalsIgnoreCase(str9))
                argMap.put("copyPartialTargetTask", ProgramCentralUtil.getPhysicalId(paramContext, str3));
            for (Object object : mapList) {
                Map map = (Map) object;
                String str12 = (String)map.get("id");
                String str13 = (String)map.get("type");
                String str14 = (String)map.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
                String str15 = (String)map.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE);
                if ("true".equalsIgnoreCase(str14) || "true"
                        .equalsIgnoreCase(str15)) {
                    Dataobject dataobject = new Dataobject();
                    dataobject.setId(str12);
                    Servicedata servicedata1 = new Servicedata();
                    servicedata1.getData().add(dataobject);
                    ServiceParameters serviceParameters = new ServiceParameters();
                    serviceParameters.setServiceName("dpm.projects/copy");
                    serviceParameters.setServiceArgs(argMap);
                    serviceParameters.setDefaultUpdateAction(UpdateActions.MODIFY);
                    HashMap<Object, Object> hashMap = new HashMap<>();
                    if (ProgramCentralUtil.isNotNullString(str2)) {
                        Map map3 = Question.getQuestionResponce(str2);
                        ArrayList<Dataobject> arrayList = new ArrayList();
                        for (Object entryObject : map3.entrySet()) {
                            Map.Entry entry = (Map.Entry)entryObject;
                            Dataobject dataobject1 = new Dataobject();
                            dataobject1.setId((new DomainObject((String)entry.getKey())).getPhysicalId(paramContext));
                            DataelementMapAdapter.setDataelementValue(dataobject1, "questionResponse", (String)entry.getValue());
                            DataelementMapAdapter.setDataelementValue(dataobject1, "id", (String)entry.getKey());
                            DataelementMapAdapter.setDataelementValue(dataobject1, "type", ProgramCentralConstants.TYPE_QUESTION);
                            arrayList.add(dataobject1);
                        }
                        RelateddataMapAdapter.addRelatedData(dataobject, "questions", arrayList);
                    }
                    argMap.put("copyPartialDestinationProject", str10);
                    Servicedata servicedata2 = ServiceBase.saveData(paramContext, servicedata1, serviceParameters, 0L);
                    continue;
                }
                stringList3.add(str12);
            }
            if (stringList3.size() > 0) {
                Servicedata servicedata1 = new Servicedata();
                Dataobject dataobject = new Dataobject();
                dataobject.setId(str10);
                servicedata1.getData().add(dataobject);
                ServiceParameters serviceParameters = new ServiceParameters();
                serviceParameters.setServiceName("dpm.projects/copy");
                serviceParameters.setServiceArgs(argMap);
                serviceParameters.setDefaultUpdateAction(UpdateActions.MODIFY);
                ArrayList<Dataobject> arrayList = new ArrayList();
                for (String str12 : stringList3) {
                    Dataobject dataobject1 = new Dataobject();
                    dataobject1.setId(str12);
                    arrayList.add(dataobject1);
                }
                RelateddataMapAdapter.addRelatedData(dataobject, "cloneTasks", arrayList);
                argMap.put("copyPartialScheduleTasks", "true");
                Servicedata servicedata2 = saveData(paramContext, servicedata1, serviceParameters, 0L);
                String str = ((Dataobject)servicedata2.getData().get(0)).getId();
            }
        } catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            PropertyUtil.setGlobalRPEValue(paramContext, "CopyColorAttribute", "true");
            PropertyUtil.setGlobalRPEValue(paramContext, "UseStartAndEndDatesAsEntered", "false");
        }
    }

    public static Servicedata saveData(Context var0, Servicedata var1, ServiceParameters var2, long var3) throws FoundationException {
        FoundationUtil.debug("starting transaction...", var3);
        com.dassault_systemes.enovia.e6wv2.foundation.db.ContextUtil.startTransaction(var0, true);
        FoundationUtil.debug("starting transaction... done", var3);

        Servicedata var7;
        try {
            Servicedata var5 = var1;
            if (var1 == null) {
                var5 = new Servicedata();
            }

            Servicedata var13 = processServiceRequest(var0, var5, var2, false, var3);
            FoundationUtil.debug("committing transaction...", var3);
            com.dassault_systemes.enovia.e6wv2.foundation.db.ContextUtil.commitTransaction(var0);
            FoundationUtil.debug("committing transaction... done", var3);
            var7 = var13;
        } catch (Exception var11) {
            com.dassault_systemes.enovia.e6wv2.foundation.db.ContextUtil.abortTransaction(var0);
            FoundationException var6 = FoundationException.processException(var0, var11);
            throw var6;
        } finally {
            if (!com.dassault_systemes.enovia.e6wv2.foundation.db.ContextUtil.isTransactionAborting(var0)) {
                com.dassault_systemes.enovia.e6wv2.foundation.db.ContextUtil.clearClientMessages(var0);
            }
        }
        return var7;
    }

    private static Servicedata processServiceRequest(Context var0, Servicedata var1, ServiceParameters var2, boolean var3, long var4) throws FoundationException {
        try {
            String var6 = var2 != null ? var2.getServiceName() : null;
            if (var6 == null) {
                throw new FoundationException("ServiceBase: Service Name is required.");
            } else {
                if (var2.getServiceArgs() == null) {
                    var2.setServiceArgs(new ArgMap());
                }
                //使用反射来调用私有方法
                Class<?> clazz = Class.forName("com.dassault_systemes.enovia.e6wv2.foundation.ServiceBase");
                Object instance = clazz.newInstance(); // 创建实例
                Method method = clazz.getDeclaredMethod("processServicePath", Context.class, String.class, long.class, HttpServletRequest.class, String.class, boolean.class);
                Method method1 = clazz.getDeclaredMethod("filterServicesAndFields", DataobjectDefinition.class, String.class, String.class);
                Method method2 = clazz.getDeclaredMethod("getServiceData", Context.class, DataobjectDefinition.class, Datacollection.class, ServiceParameters.class, boolean.class, long.class);
                Method method3 = clazz.getDeclaredMethod("processDefinitionItems", Context.class, Service.class, ServiceParameters.class);
                method.setAccessible(true); // 设置为可访问
                method1.setAccessible(true); // 设置为可访问
                method2.setAccessible(true); // 设置为可访问
                method3.setAccessible(true); // 设置为可访问
                FoundationUtil.debug("checking foundation license...", var4);
                LicenseUtil.checkLicenseFoundation(var0, var2.getHttpRequest());
                FoundationUtil.debug("checking foundation license... done.", var4);
                String var7 = (String)var2.getServiceArgs().get("$ids");
                Service var8 = (Service) method.invoke(instance,var0, var6, var4, var2.getHttpRequest(), var7, var3);
                DataobjectDefinition var9 = var8.getDataobjectDefinition();
                Servicedata var10 = new Servicedata();
                var10.getSingleCardinality().addAll(var8.getSingleCardinality());
                var10.getStructured().addAll(var8.getStructured());
                Datacollections var11 = null;
                if (var9 != null) {
                    var2.setServiceName(var9.getName());
                    if (var3) {
                        if (!"nodata".equalsIgnoreCase((String)var2.getServiceArgs().get("$definition"))) {
                            String var12 = (String)var2.getServiceArgs().get("$include");
                            String var13 = (String)var2.getServiceArgs().get("$fields");
                            method1.invoke(instance, var9, var12, var13);
                            Datacollection var14 = new Datacollection();
                            if (var1 != null) {
                                var14.getDataobjects().addAll(var1.getData());
                            }

                            var11 = (Datacollections) method2.invoke(instance, var0, var9, var14, var2, true, var4);
                            if (!var11.getDatacollections().isEmpty()) {
                                Iterator var15 = var11.getDatacollections().iterator();

                                while(var15.hasNext()) {
                                    Datacollection var16 = (Datacollection)var15.next();
                                    var10.getData().addAll(var16.getDataobjects());
                                }
                            }

                            var10.setInfo(var11.getInfo());
                            var10.setSummary(var11.getSummary());
                            var10.setDataCollectionName(var11.getDataCollectionName());
                            var10.setSummaryObjectName(var11.getSummaryObjectName());
                            var11.setInfo((String)null);
                            var11.setSummary((Dataobject)null);
                            if (var10.getStructured().contains(var11.getName())) {
                                var10.getStructured().add("Root_Service");
                            }
                        }
                    } else {
                        UpdateActions var17 = var2.getDefaultUpdateAction();
                        if (var17 == null) {
                            var17 = ServiceResource.getDefaultUpdateAction(var2.getHttpRequest());
                        }

                        ArrayList var19 = new ArrayList();
                        List var20 = updateInputData(var0, var9, var1, var2, var17, var19);
                        var10.setDataCollectionName(var9.getDataCollectionName());
                        var10.getData().clear();
                        var10.getData().addAll(var20);
                        if (!var19.isEmpty()) {
                            var10.setInfo(com.dassault_systemes.enovia.e6wv2.foundation.util.StringUtil.join(var19, ";"));
                            var10.setStatusCode(var20.isEmpty() ? 400 : 207);
                        }
                    }
                } else if (var1 != null) {
                    throw new FoundationException("Service data is not modifiable.");
                }

                if (var2.getServiceArgs().get("$definition") != null && !"FALSE".equalsIgnoreCase((String)var2.getServiceArgs().get("$definition"))) {
                    FoundationUtil.debug("processing service definition...", var4);
                    method3.invoke(instance, var0, var8, var2);
                    var10.getDefinition().addAll(var8.getItems());
                    var10.setLabel(var8.getLabel());
                    FoundationUtil.debug("processing service definition... done.", var4);
                }

                CSRFToken var18 = com.dassault_systemes.enovia.e6wv2.foundation.db.ContextUtil.getCSRFKey(var0, var2.getHttpRequest());
                var10.setCsrf(var18);
                FoundationUtil.debug("service data processing completed.", var4);
                return var10;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public static List<Dataobject> updateInputData(Context var0, DataobjectDefinition var1, Servicedata var2, ServiceParameters var3, UpdateActions var4, List<String> var5) throws Exception {
        try {
            Class<?> clazz = Class.forName("com.dassault_systemes.enovia.e6wv2.foundation.ServiceSave");
            Object instance = clazz.newInstance(); // 创建实例
            Method method = clazz.getDeclaredMethod("setDefaultAction",List.class, UpdateActions.class);
            method.setAccessible(true); // 设置为可访问
            DataobjectDefinition var6 = var1;
            DataobjectDefinition var7 = var1;
            Dataobject var8 = null;
            ArrayList var9 = new ArrayList();
            var9.add(var1.getName());

            List var11;
            while(var7.getId() != null || var7.isTarget() != null && var7.isTarget()) {
                Dataobject var10 = new Dataobject();
                var10.setId(var7.getId());
                if (var8 != null) {
                    var10.setParent(var8);
                    if (var8.getId() != null) {
                        var9.add("/");
                        var9.add(var8.getId());
                    }

                    var9.add("/");
                    var9.add(var7.getName());
                }

                var8 = var10;
                var6 = var7;
                var11 = var7.getRelatedDataobjectDefinition();
                if (var11.size() != 1) {
                    break;
                }

                var7 = (DataobjectDefinition)var11.get(0);
            }

            var1.setDataCollectionName(var6.getDataCollectionName());
            if (var6.getDatafunctions().isCsrf()) {
                CSRFToken var16 = var2.getCsrf();
                com.dassault_systemes.enovia.e6wv2.foundation.db.ContextUtil.validateCSRFKey(var0, var3.getHttpRequest(), var16);
            }

            List var17 = var2.getData();
            var11 = null;
            String var18;
            if (var8 != null) {
                Dataobject var12 = var8;
                var8 = var8.getParent();
                Iterator var13;
                if (var17.isEmpty()) {
                    var17.add(var12);
                    var12.setParent((Dataobject)null);
                } else if (var6.getId() != null) {
                    var13 = var17.iterator();

                    while(var13.hasNext()) {
                        Dataobject var14 = (Dataobject)var13.next();
                        var14.setId(var6.getId());
                    }
                }

                var18 = "";

                String var21;
                for(var13 = var9.iterator(); var13.hasNext(); var18 = var18 + var21) {
                    var21 = (String)var13.next();
                }
            } else {
                var18 = var6.getName();
            }

            if (var4 != null) {
                if (var17.isEmpty()) {
                    var17.add(new Dataobject());
                }
                method.invoke(instance, var17, var4);
            }

            Object var19 = (Map) com.dassault_systemes.enovia.e6wv2.foundation.db.ContextUtil.getAttribute(var0, "TEMP_ID_CACHEMAP");
            if (var19 == null) {
                var19 = new ConcurrentHashMap(2);
                com.dassault_systemes.enovia.e6wv2.foundation.db.ContextUtil.setAttribute(var0, "TEMP_ID_CACHEMAP", var19);
            }
            ArrayList var20 = new ArrayList();
            ArrayList var22 = new ArrayList();
            var20.add((Object)null);
            var22.add(var8);
//            List var15 = ServiceSave.UpdateRecursiveTask.performUpdate(var0, var6, var17, var3, var20, var22, (Map)var19, var18, var5);
            return new ArrayList<>();
        } catch (Exception e) {
            throw e;
        }
    }


    /**
     * Updated project information
     * @param context - The eMatrix <code>Context</code> object.
     * @param args holds information about object.
     * @throws Exception if operation fails.
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void createAndConnectProject(Context context,String[] args)throws Exception
    {
        Map request = JPO.unpackArgs(args);
        Map requestMap = (Map)request.get("paramMap");
        String newObjectId = (String)requestMap.get("newObjectId");
        //TODO
        logger.info("createAndConnectProject:{}",requestMap);
        DomainObject newProjectObj = DomainObject.newInstance(context,newObjectId);
        newProjectObj.setAttributeValue(context, "JF_ProjectCode", newProjectObj.getDescription(context));//设置项目代号
    }
    /*
     * @description:任务提升的时候需要校验是否有未完成的问题
     * @author: caipan
     * @date: 2025/8/8 11:25:39
     * @param: * @param[1] context
     * @param[2] args
     * @return:
     **/
    public int checkIssue(Context context,String[] args) throws Exception{
        String objectId = args[0];
        DomainObject taskObj = DomainObject.newInstance(context);
        taskObj.setId(objectId);
        //判断是项目任务
        if(!JF_Util_mxJPO.isProjectTask(context, taskObj)){//不是项目任务直接返回
            return 0;
        }
        StringList taskSelectList = StringList.create(
                DomainConstants.SELECT_TYPE,
                "to[Subtask].from.type");
        Map taskInfo = taskObj.getInfo(context, taskSelectList);
        String taskType = UIUtil.getValue(taskInfo, DomainConstants.SELECT_TYPE);
        String parentTaskType = UIUtil.getValue(taskInfo, "to[Subtask].from.type");
        //20260729 update by ljr ESO任务由工作中提交到审批中时不校验问题，问题关闭校验延后到审批中提交已完成时执行；
        if (JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(taskType)
                || JF_PLMConstants_mxJPO.TYPE_JF_ESOTask.equalsIgnoreCase(parentTaskType)) {
            return 0;
        }
        Locale sLanguage = context.getLocale();
        String physicalId = taskObj.getInfo(context, DomainConstants.SELECT_PHYSICAL_ID);
        //未完成的问题
        MapList relIssueList= new Issue().getAllIssues(context, physicalId, true, false);
        logger.info("relIssueList:{}",relIssueList);
        if(relIssueList.size()>0){
            String checkkeyError =EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource",sLanguage, "emxProgramCentral.task.checkIssueError");
            emxContextUtil_mxJPO.mqlNotice(context,checkkeyError);
            return 1;
        }
        return 0;
    }

    /**
    * WBS中从项目复制  不卷积
    * @param context
	* @param paramArrayOfString
    * @author LIUJR
    * @throws
    * @return void
    * @date 2026/5/12 15:05
    * @description
    */
    @PostProcessCallable
    public static void copyProjectPhaseTaskProcess(Context context, String[] paramArrayOfString) throws Exception {
        try {
            Map map1 = (Map) JPO.unpackArgs(paramArrayOfString);
            String objectId = (String) map1.get("objectId");
            String seachProjectOID = (String) map1.get("SeachProjectOID");
            logger.info("str3!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.info("objectId:{}", objectId);
            logger.info("seachProjectOID:{}", seachProjectOID);
            //查询选择复制的对象信息
            DomainObject selectObject = DomainObject.newInstance(context, seachProjectOID);
            StringList stringList2 = JF_Util_mxJPO.basicBolistSel();
            stringList2.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE);
            stringList2.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE);
            Map selectObjectMap = selectObject.getInfo(context, stringList2);
            //拿取挂载对象的估计开始时间/估计结束时间/天数等属性
            DomainObject object = DomainObject.newInstance(context, objectId);
            String duration = object.getAttributeValue(context, "Task Estimated Duration");
            String finishDate = object.getAttributeValue(context, "Task Estimated Finish Date");
            String startDate = object.getAttributeValue(context, "Task Estimated Start Date");
            String constraintDate = object.getAttributeValue(context, "Task Constraint Date");
            String taskConstraintType = object.getAttributeValue(context, "Task Constraint Type");
            //判断选择复制的对象是否是项目或者项目模板
            JF_ESO_mxJPO jfEsoMxJPO = new JF_ESO_mxJPO();
            MapList palList = new MapList();
            StringList copyTaskIdList = new StringList();
            //如果是项目/项目模板需要拿取所有的子级，不能包含本身；如果是任务包含本身及子级
            if ("true".equalsIgnoreCase(UIUtil.getValue(selectObjectMap, ProgramCentralConstants.SELECT_KINDOF_PROJECT_SPACE)) || "true"
                    .equalsIgnoreCase(UIUtil.getValue(selectObjectMap, ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE))) {
                palList = jfEsoMxJPO.getObjectListFromPAL(context,seachProjectOID);
                copyTaskIdList = selectObject.getInfoList(context, "from[Subtask].to.id");
                //是项目模板/项目空间 需要将任务进行排序复制
                String taskOrder = jfEsoMxJPO.getTaskOrder(context, palList, copyTaskIdList);
                String[] split = taskOrder.split("\\|");
                copyTaskIdList = StringList.create(split);
            } else {
                copyTaskIdList.add(seachProjectOID);
            }
            logger.info("@@@@@@@@@@@@@@copyTaskIdList:{}", copyTaskIdList);
            //构造线程需要的参数
            String mql = "mod bus '%s' 'Task Estimated Start Date' '"+startDate+"' 'Task Estimated Finish Date' '"+finishDate+"' 'Task Estimated Duration' '"+duration+"';";
//            String mql = "mod bus '%s' 'Task Estimated Start Date' '"+startDate+"' 'Task Estimated Finish Date' '"+finishDate+"' 'Task Estimated Duration' '"+duration+"' 'Task Constraint Date' '"+constraintDate+"' 'Task Constraint Type' '"+ taskConstraintType +"';";
            MapList mapList = new MapList();
            StringList taskAllIdList = new StringList();
            for (int i = 0; i < copyTaskIdList.size(); i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("temTaskId", copyTaskIdList.get(i));
                map.put("parentId", objectId);
                map.put("mql", mql);
                mapList.clear();
                mapList.add(map);
                List<StringList> resultList = JF_Util_mxJPO.executeBatch(context, mapList, "emxProjectSpace", "preCreateProjectTaskWithSelectTemplate");
                logger.info("@@@@@@@@@@@@@@resultList:{}", resultList);
                for (StringList list : resultList) {
                if (list != null) {
                    taskAllIdList.addAll(list);
                }
            }
            }
            logger.info("@@@@@@@@@@@@@@taskAllIdList:{}", taskAllIdList);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
    * 复制的项目任务的前置方法  开启事务
    * @param context
	* @param args
    * @author LIUJR
    * @throws
    * @return matrix.util.StringList
    * @date 2026/5/13 10:15
    * @description
    */
    public static StringList preCreateProjectTaskWithSelectTemplate(Context context, String[] args) throws Exception{
        StringList stringList = new StringList();
        try {
            //开始事务，调用方法为递归方法，需要在这个方法中开启
            ContextUtil.startTransaction(context, true);
            stringList = createProjectTaskWithSelectTemplate(context, args);
            ContextUtil.commitTransaction(context);
        }catch (Exception e) {
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
            throw e;
        }
        return stringList;
    }


    /**
    * 复制的项目任务的方法   并且修改估计时间
    * @param context
    * @param args
    * @author LIUJR
    * @throws
    * @return void
    * @date 2026/5/12 16:01
    * @description
     * taskAllIdList  保存新创建的任务集合
     * temTaskId  根据哪一个任务去复制
     * parentId   挂载的任务
    */
    public static StringList createProjectTaskWithSelectTemplate(Context context, String[] args) throws Exception{
        StringList stringList = new StringList();
        try {
            //拿取参数
            Map paramsMap= (Map) JPO.unpackArgs(args);
            String temTaskId = (String) paramsMap.get("temTaskId");
            String parentId = (String) paramsMap.get("parentId");
            String mql = (String) paramsMap.get("mql");
            logger.info("temTaskId:{}", temTaskId);
            //被复制对象
            DomainObject domainObject = DomainObject.newInstance(context, temTaskId);
            //复制出来的新对象
            DomainObject newDomainObject = DomainObject.newInstance(context);
            //构造复制任务的参数
            Map createTemp = new HashMap();
            domainObject.setId(temTaskId);
            createTemp.put("taskName", domainObject.getInfo(context, SELECT_NAME));
            createTemp.put("description", domainObject.getDescription(context));  //说明
            createTemp.put(DomainConstants.SELECT_ID, parentId);  //挂载的id
            //执行复制
            String newTaskId = JF_ProjectTaskUtils_mxJPO.createTask(context,parentId,createTemp,domainObject.getInfo(context, DomainConstants.SELECT_TYPE),"addTaskBelow");
            //设置新对象属性
            newDomainObject.setId(newTaskId);
            AttributeList attributeValues = domainObject.getAttributeValues(context);
            attributeValues.add(new Attribute(JF_PLMConstants_mxJPO.ATTR_ProjectRole, domainObject.getAttributeValue(context, JF_PLMConstants_mxJPO.ATTR_ProjectRole)));
            newDomainObject.setAttributeValues(context, attributeValues);
            stringList.add(newTaskId);
            //执行修改时间的mql  将复制新对象的时间改为选择挂载对象的估计时间
            String newMql = String.format(mql, newTaskId);
            /*
             *  Task Actual Start Date 7/17/2025 8:00:00 AM
                Task Estimated Duration 1.0
                Task Estimated Finish Date 3/6/2024 5:00:00 PM
                Task Estimated Start Date 3/6/2024 8:00:00 AM
                Task Constraint Date 6/26/2025 5:00:00 PM
             * */
            MqlUtil.mqlCommand(context,false, newMql,true);
            //继续去找子级对象
            JF_ESO_mxJPO jfEsoMxJPO = new JF_ESO_mxJPO();
            MapList palList = new MapList();
            palList = jfEsoMxJPO.getObjectListFromPAL(context,temTaskId);
            logger.info("palList:{}", palList);
            StringList subTaskIdList = domainObject.getInfoList(context, "from[Subtask].to.id");
            if (!subTaskIdList.isEmpty()) {
                //是项目模板/项目空间 需要将任务进行排序复制
                String taskOrder = jfEsoMxJPO.getTaskOrder(context, palList, subTaskIdList);
                logger.info("subTaskIdList:{}", subTaskIdList);
                String[] split = taskOrder.split("\\|");
                subTaskIdList = StringList.create(split);
                logger.info("subTaskIdList:{}", subTaskIdList);
                logger.info("subTaskIdList:{}", subTaskIdList.size());
            }
            //遍历后复制
            if (!subTaskIdList.isEmpty()) {
                for (int i = 0; i < subTaskIdList.size(); i++) {
                    Map<String, Object> mapCopy = new HashMap<>();
                    mapCopy.put("temTaskId", subTaskIdList.get(i));
                    mapCopy.put("parentId", newTaskId);
                    mapCopy.put("mql", mql);
                    StringList list = createProjectTaskWithSelectTemplate(context, JPO.packArgs(mapCopy));
                    stringList.addAll(list);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return stringList;
    }

    /**
     * 获取项目当前日期所在的第一层阶段或任务。
     **
     * @param context
     * @param args 表格参数
     * @return List 当前阶段或任务的显示值
     * @throws Exception
     * @author caipan
     * @date 2026/8/3 16:13
     */
    public java.util.List getCurrentPhase(Context context, String[] args) throws Exception {

        HashMap projectMap = (HashMap) JPO.unpackArgs(args);
        Map paramList = (Map) projectMap.get("paramList");
        String suiteDirectory = (String)paramList.get("SuiteDirectory");
        String strPrinterFriendly = (String)paramList.get("reportFormat");
        boolean isPrinterFriendly = false;
        if(strPrinterFriendly != null){
            isPrinterFriendly = true;
        }

        List currentPhase = new StringList();
        MapList projectDetails = getProjectDetails(context,args);
        Iterator prjIterator = projectDetails.iterator();
        while (prjIterator.hasNext()){
            String displayPhase = "";
            Map projectDetailsMap = (HashMap) prjIterator.next();
            String phase = (String) projectDetailsMap.get("currentPhase");
            String phaseURL = (String) projectDetailsMap.get("currentPhaseURL");
            String sType = (String) projectDetailsMap.get("type");
            String phaseIcon = ProgramCentralConstants.EMPTY_STRING;


            if(TYPE_PHASE.equalsIgnoreCase(sType)){
                phaseIcon = "iconSmallPhase.png";
            }else if(TYPE_GATE.equalsIgnoreCase(sType)){
                phaseIcon = "iconSmallGate.png";
            }else if(DomainConstants.TYPE_TASK.equalsIgnoreCase(sType)){
                phaseIcon = "iconSmallTask16.png";
            }

            if(TYPE_PROJECT_SPACE.equalsIgnoreCase(sType)){
                phaseIcon ="iconSmallProject.png";
            }
            if(TYPE_PROJECT_CONCEPT.equalsIgnoreCase(sType)){
                phaseIcon ="iconSmallProjectConcept.gif";
            }
            phaseURL =  XSSUtil.encodeForHTML(context, phaseURL);

            if(!"".equals(phase)){
                StringBuffer sbDisplayPhase = new StringBuffer(100);
                sbDisplayPhase.append("<a title='").append(XSSUtil.encodeForXML(context, phase));
                sbDisplayPhase.append("' href ='").append(phaseURL);
                sbDisplayPhase.append("'>");
                sbDisplayPhase.append("<img src=\"../common/images/"+phaseIcon+"\" name=\"imgTask\" border=\"0\"/>");
                sbDisplayPhase.append(XSSUtil.encodeForXML(context,phase));
                sbDisplayPhase.append("</a>");

                if(!isPrinterFriendly) {
                    displayPhase = sbDisplayPhase.toString();
                } else {
                    displayPhase = phase;
                }
            }
            currentPhase.add(displayPhase);
        }
        return currentPhase;
    }

    public MapList getProjectDetails(Context context, String[] args) throws Exception
    {
        double totalTasks = 0.0;
        double completedTasks = 0.0;
        long slipDay   = 0;
        long slipDayab = 0;
        String col     = "";
        boolean slipFlag=true;
        int slipValue1 = 0;
        int slipValue2 = 0;
        String slipColor1 = ProgramCentralConstants.EMPTY_STRING;
        String slipColor2 = ProgramCentralConstants.EMPTY_STRING;
        String slipColor3 = ProgramCentralConstants.EMPTY_STRING;
        java.util.Date date1 =  new java.util.Date();
        java.util.Date today = new java.util.Date();
        today.setHours(0);
        today.setMinutes(0);
        today.setSeconds(0);
        String strCurrentPhase = "";
        ArrayList phaseList = new ArrayList();
        ArrayList dateList = new ArrayList();
        String phaseEstEndDate = ProgramCentralConstants.EMPTY_STRING;
        String phaseActEndDate = ProgramCentralConstants.EMPTY_STRING;
        String CurrentPhaseDate="";
        String percentComplete = "0";
        java.util.HashMap incompleteTask  = new java.util.HashMap();
        java.util.HashMap completeTask    = new java.util.HashMap();
        String phaseId = "";

        //Read SlipThresholdValues from property file
        if (slipFlag)
        {
            int thresholdValues[]    = {5,10,5,10,5,10};
            String colorCodeValues[] = {"008000","FFCC00","FF0000","008000","FFCC00","FF0000","008000","FFCC00","FF0000"};
            try
            {
                thresholdValues = getThresholdValues(context);
                colorCodeValues = getColorCodeValues(context);
            }
            catch(Exception e)
            {
                //if threshold values are not there in the property file
            }

            slipValue1 = thresholdValues[0];
            slipValue2 = thresholdValues[1];

            slipColor1 = colorCodeValues[0];
            slipColor2 = colorCodeValues[1];
            slipColor3 = colorCodeValues[2];

            slipFlag=false;
        }

        // Maplist to hold the projects' details
        MapList projectDetails = new MapList();

        try{
            ProjectSpace projectSpace = (ProjectSpace) DomainObject.newInstance(context,
                    DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
            //com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,TYPE_TASK,PROGRAM);

            //Formatting Date to Ematrix Date Format
            boolean bDisplayTime = PersonUtil.getPreferenceDisplayTimeValue(context);
            int iDateFormat = PersonUtil.getPreferenceDateFormatValue(context);

            // get Task information
            StringList task_busSelects =  new StringList(6);
            task_busSelects.add(Task.SELECT_ID);
            task_busSelects.add(Task.SELECT_TYPE);
            task_busSelects.add(Task.SELECT_NAME);
            task_busSelects.add(Task.SELECT_TASK_ESTIMATED_START_DATE);
            task_busSelects.add(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
            task_busSelects.add(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
            task_busSelects.add(Task.SELECT_CURRENT);
            task_busSelects.add(Task.SELECT_BASELINE_CURRENT_END_DATE);
            task_busSelects.add(Task.SELECT_PERCENT_COMPLETE);

            StringList relSelects = new StringList(1);
            //relSelects.add(SubtaskRelationship.SELECT_TASK_WBS);
            //relSelects.add(SubtaskRelationship.SELECT_SEQUENCE_ORDER);

            Map programMap = (Map) JPO.unpackArgs(args);
            Map paramList = (Map) programMap.get("paramList");
            String suiteDirectory = (String)paramList.get("SuiteDirectory");
            String stringClientTimeOffset = (String) paramList.get("timeZone");
            Locale locale = (Locale) paramList.get("localeObj");
            double iClientTimeOffset = Task.parseToDouble(stringClientTimeOffset);

            MapList objectList = (MapList) programMap.get("objectList");
            int size = objectList.size();
            String []projectIdArr = new String[size];
            for(int i=0;i<size;i++){
                Map projectMap = (Map)objectList.get(i);
                String id = (String)projectMap.get(SELECT_ID);
                projectIdArr[i] = id;
            }

            MapList projectInfoList = ProgramCentralUtil.getObjectDetails(context, projectIdArr, new StringList(SELECT_CURRENT));
            Map<String,Object> projectInfoMap = new HashMap();


            for(int i=0;i<size;i++){
                Map projectMap = (Map)objectList.get(i);
                Map projectMap1 = (Map)projectInfoList.get(i);
                Map projectTempMap = new HashMap();

                String id = (String)projectMap.get(SELECT_ID);
                String state = (String)projectMap1.get(SELECT_CURRENT);
                MapList projectTasks = null;
                //Current phase should get computed for Active and Review projects.
                if(ProgramCentralConstants.STATE_PROJECT_SPACE_ACTIVE.equalsIgnoreCase(state)
                        || ProgramCentralConstants.STATE_PROJECT_SPACE_REVIEW.equalsIgnoreCase(state)) {

                    projectSpace.setId(id);
                    projectTasks = Task.getTasks(context, (TaskHolder) projectSpace, 1, task_busSelects, relSelects);
                } else {
                    projectTasks = new MapList();
                }
                projectTempMap.put(SELECT_CURRENT, state);
                projectTempMap.put("TaskList", projectTasks);

                projectInfoMap.put(id, projectTempMap);
            }

            Iterator objectListIterator = objectList.iterator();
            StringBuffer sbCurrentPhaseURL = new StringBuffer(64);
            for(int i=0;i<size;i++){
                Map projectMap = new HashMap();
                slipDay         = 0;
                slipDayab       = 0;
                col             = "";
                totalTasks      = 0.0;
                completedTasks  = 0.0;

                strCurrentPhase = "";

                Map objectMap = (Map) objectList.get(i);
                String objectId = (String) objectMap.get(projectSpace.SELECT_ID);
                Map projectDetailsMap 	= (Map) projectInfoMap.get(objectId);
                String current 			= (String) projectDetailsMap.get(SELECT_CURRENT);
			  /* projectSpace.setId(objectId);

			   // Build busSelects and busWhere for retrieving owner information
			   String current = projectSpace.getInfo(context,projectSpace.SELECT_CURRENT);*/
                //No need to compute current phase for project which are in following states.
                if(ProgramCentralConstants.STATE_PROJECT_SPACE_CREATE.equals(current)
                        || ProgramCentralConstants.STATE_PROJECT_SPACE_ASSIGN.equals(current)
                        || ProgramCentralConstants.STATE_PROJECT_SPACE_COMPLETE.equals(current)
                        || ProgramCentralConstants.STATE_PROJECT_SPACE_ARCHIVE.equals(current)){
                    projectMap.put("col", DomainConstants.EMPTY_STRING);
                    projectMap.put("currentPhase", DomainConstants.EMPTY_STRING);
                    projectMap.put("currentPhaseURL", DomainConstants.EMPTY_STRING);
                    projectDetails.add(projectMap);
                    continue;
                }

                //MapList projectTasks = projectSpace.getTasks(context,1,task_busSelects, relSelects,false);
                //commented for 478380
                //MapList projectTasks = Task.getTasks(context, (TaskHolder)projectSpace, 1, task_busSelects, relSelects);
                MapList projectTasks = (MapList) projectDetailsMap.get("TaskList");
                //clear the lists to make sure they are empty
                incompleteTask.clear();
                completeTask.clear();
                if (! projectTasks.isEmpty())
                {
                    Iterator taskItr = projectTasks.iterator();
                    while(taskItr.hasNext())
                    {
                        Map taskMap = (Map) taskItr.next();
                        String currstate = (String) taskMap.get(Task.SELECT_CURRENT);
                        // task.setId((String) taskMap.get(Task.SELECT_ID));
                        if ("Complete".equals(currstate))
                        {
                            //Complete State Block
                            String finishDate = "";
                            String baselineCurrentFinishDateStr = (String) taskMap.get(Task.SELECT_BASELINE_CURRENT_END_DATE);
                            if (null == baselineCurrentFinishDateStr || "".equals(baselineCurrentFinishDateStr))
                            {
                                finishDate = (String) taskMap.get(Task.SELECT_TASK_ACTUAL_FINISH_DATE);
                            }
                            else
                            {
                                finishDate = baselineCurrentFinishDateStr;
                            }
                            completeTask.put(finishDate + taskMap.get(Task.SELECT_ID),taskMap);
                        }
                        else
                        {
                            // Incomplete State Block
                            String finishDate = "";
                            String baselineCurrentFinishDateStr = (String) taskMap.get(Task.SELECT_BASELINE_CURRENT_END_DATE);
                            if (null == baselineCurrentFinishDateStr || "".equals(baselineCurrentFinishDateStr)){
                                finishDate = (String) taskMap.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                            }else{
                                finishDate = baselineCurrentFinishDateStr;
                            }

                            incompleteTask.put(finishDate+taskMap.get(Task.SELECT_ID),taskMap);
                        }
                    } // end while
                } //end if task list is not empty

                Map minTask      = null;

                Map disply         = null;
                String  currentPhaseURL   = ProgramCentralConstants.EMPTY_STRING;
                String minStr = ProgramCentralConstants.EMPTY_STRING;
                String sType  = EMPTY_STRING;

                //if there are tasks see which one we are going to set as the current phase
                if ( !incompleteTask.isEmpty() || !completeTask.isEmpty() )
                {
                    //all tasks are complete, get the task with the oldest end date
                    if(incompleteTask.isEmpty())
                    {
                        Map map = getCurrentPhase(context, projectTasks,current);
                        if(map!=null && !map.isEmpty())
                        {
                            disply = map;
                            date1  = sdf.parse((String)disply.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE));
                        }
                       /*
					   // COMPLETE
					   if(! completeTask.isEmpty())
					   {
						   Object keys[] = (completeTask.keySet()).toArray();
						   //put the keys in ascending order
						   java.util.Arrays.sort(keys);
						   minStr = (String)keys[0];
						   Date testDate;
						   int testOrder;
						   Date maxDate;
						   int maxOrder;
						   for (int i =0; i < keys.length; i++){
							   Map mTask = (Map)completeTask.get((String)keys[i]);
							   String strTaskActualFinishDate = (String)mTask.get(task.SELECT_TASK_ACTUAL_FINISH_DATE);
							   if(ProgramCentralUtil.isNotNullString(strTaskActualFinishDate)){
								   testDate = sdf.parse(strTaskActualFinishDate);
								   maxDate = sdf.parse(((String)((Map)completeTask.get(minStr)).get(task.SELECT_TASK_ACTUAL_FINISH_DATE)));
								   maxOrder = Integer.parseInt((String)((Map)completeTask.get(minStr)).get(SubtaskRelationship.SELECT_SEQUENCE_ORDER));
								   //If dates are same, take sequence order, to get the latest completed task.
								   if(testDate.equals(maxDate)){
									   testOrder = Integer.parseInt((String)mTask.get(SubtaskRelationship.SELECT_SEQUENCE_ORDER));
									   if(testOrder > maxOrder){
										   minStr = (String)keys[i];
									   }
								   }
								   else if(testDate.after(maxDate)){
									   minStr = (String)keys[i];
								   }
							   }
						   }//end for keys
					   }//end if completed list is not empty

					   disply = (Map) completeTask.get(minStr);
					   //if the task is complete use the Actual Finish date for slip days calculations
					   String actualFinishDate = (String)disply.get(task.SELECT_TASK_ACTUAL_FINISH_DATE);
					   if(ProgramCentralUtil.isNotNullString(actualFinishDate))
					   {
					   date1  = sdf.parse((String)disply.get(task.SELECT_TASK_ACTUAL_FINISH_DATE));
					   }
					   //if the task is complete use the estimated finish date
					   min    = sdf.parse((String)disply.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE));

				   */}
                    //incomplete task list is not empty get the first task with the lowest estimated end date
                    //Or with min wbs Number comparing texicographically
                    //So 1.1.1 < 1.1.2
                    else
                    {
                        Map map = getCurrentPhase(context, projectTasks,current);
                        if(map!=null && !map.isEmpty())
                        {
                            disply = map;
                            //if the task is incomplete use the Estimated Finish date for slip days calculations
                            date1  = sdf.parse((String)disply.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE));
                        }/*else{
					   if(! incompleteTask.isEmpty())
					   {
						   Object keys[] = (incompleteTask.keySet()).toArray();
						   //put the keys in ascending order
						   java.util.Arrays.sort(keys);
						   minStr = (String)keys[0];

						   java.util.Date minDate;
						   java.util.Date test;
						   String wbsNumber = DomainConstants.EMPTY_STRING;
						   String minNumber = DomainConstants.EMPTY_STRING;
						   for (int i =0; i <keys.length; i++)
						   {
							   minDate   = sdf.parse(((String)((Map)incompleteTask.get(minStr)).get(task.SELECT_TASK_ESTIMATED_FINISH_DATE)));
							   test      = sdf.parse(((String)((Map)incompleteTask.get((String)keys[i])).get(task.SELECT_TASK_ESTIMATED_FINISH_DATE)));

							   minNumber = ((String)((Map)incompleteTask.get(minStr)).get(com.matrixone.apps.common.SubtaskRelationship.SELECT_TASK_WBS));
							   wbsNumber = ((String)((Map)incompleteTask.get((String)keys[i])).get(com.matrixone.apps.common.SubtaskRelationship.SELECT_TASK_WBS));
							   //Added:10-Feb-09:nzf:R207:PRG Bug:363823
							   boolean flag = false;
							   Task tskForCheking =  new Task();
							   if(tskForCheking.checkTaskSeniority(wbsNumber,minNumber)>0){
								   flag = true;
							   }
							   if(test.before(minDate) || (test.equals(minDate) && flag)){
								   //End:R207:PRG Bug:363823
								   minStr = (String)keys[i];
							   }
						   }
					   }//end if
					   disply = (Map) incompleteTask.get(minStr);
					   //if the task is incomplete use the Estimated Finish date for slip days calculations
					   date1  = sdf.parse((String)disply.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE));
				   }*/
                    }
                }

                date1.setHours(0);
                date1.setMinutes(0);
                date1.setSeconds(0);

                if(disply!=null) {
                    // format the date as required
                    phaseEstEndDate = (String)disply.get(Task.SELECT_TASK_ESTIMATED_FINISH_DATE);
                    phaseActEndDate = (String)disply.get(Task.SELECT_TASK_ACTUAL_FINISH_DATE);

                    phaseEstEndDate = eMatrixDateFormat.getFormattedDisplayDateTime(context,phaseEstEndDate, bDisplayTime, iDateFormat, iClientTimeOffset,locale);
                    phaseActEndDate = eMatrixDateFormat.getFormattedDisplayDateTime(context,phaseActEndDate, bDisplayTime, iDateFormat, iClientTimeOffset,locale);

                    sType  = (String)disply.get(SELECT_TYPE);
                    projectMap.put("type", sType);
                    //Added:22-July-11:MS9:R211:PRG IR-116187V6R2012x
                    String strFinishDate = phaseEstEndDate;
                    if(incompleteTask.isEmpty() && !completeTask.isEmpty())
                    {
                        strFinishDate = phaseActEndDate;
                    }

                    //CurrentPhaseDate= eMatrixDateFormat.getFormattedDisplayDateTime(context,strFinishDate, bDisplayTime, iDateFormat, iClientTimeOffset,locale);
                    CurrentPhaseDate= strFinishDate;
                    //End:22-July-11:MS9:R211:PRG IR-116187V6R2012x

                    if(CurrentPhaseDate != null) {
                        projectMap.put("CurrentPhaseDate", CurrentPhaseDate);
                    }
                    else {
                        projectMap.put("CurrentPhaseDate", "");
                    }


                    projectMap.put("CurrentPhaseEstimatedEndDate", phaseEstEndDate);
                    projectMap.put("CurrentPhaseActualEndDate", phaseActEndDate);

                    percentComplete = (String)disply.get(Task.SELECT_PERCENT_COMPLETE);
                    projectMap.put("percentComplete", percentComplete);

                    //determine slip days and color of slip days
                    if ( date1.after(today) ) {
                        //if the (Estimated/Actual) date is after the current date (min) then
                        //the milestone (task) is currently on time, so show the slip days as
                        //the amount of time until (Estimated/Actual) Finish date ( always green)
                        int dayOfWeek = today.getDay();
                        if (dayOfWeek  == 0 || dayOfWeek == 6) {
                            //don't remove day since start day is not a week day
                            slipDay = Task.computeDuration(today,date1);
                        } else {
                            //week day, so take out the starting day
                            slipDay = Task.computeDuration(today,date1) - 1;
                        }
                        //slip day color should always be green when it is before the (Estimated/Actual) Finish date
                        slipDayab = java.lang.Math.abs(slipDay);
                        col = "#"+slipColor1;
                    } else {
                        //calculate the slip days and change color according to the amount of days
                        //the milestone (task) has slipped
                        slipDay = Task.computeDuration(date1,today) - 1;//take out the starting day
                        //determine color of slip days
                        if ( slipDay >= slipValue2 ) {
                            slipDayab = java.lang.Math.abs(slipDay);
                            col       = "#" + slipColor3;
                        } else if ( slipDay > slipValue1 && slipDay < slipValue2 ) {
                            slipDayab = java.lang.Math.abs(slipDay);
                            col       = "#" + slipColor2;
                        } else {
                            slipDayab = java.lang.Math.abs(slipDay);
                            col = "#"+slipColor1;
                        }//ends else

                        // set the color to green if project is complete
                        if(incompleteTask.isEmpty())
                        {
                            col = "#"+slipColor1;
                        }
                    }//ends else
                    sbCurrentPhaseURL = new StringBuffer(64);
                    sbCurrentPhaseURL.append("../common/emxTree.jsp?objectId=");
                    sbCurrentPhaseURL.append(disply.get(Task.SELECT_ID));
                    //sbCurrentPhaseURL.append("&amp;AppendParameters=false&amp;emxSuiteDirectory=");
                    //sbCurrentPhaseURL.append(suiteDirectory);

                    strCurrentPhase = (String) disply.get(Task.SELECT_NAME);
                    projectMap.put("slipDays", String.valueOf(slipDayab));
                }
                else {
                    projectMap.put("CurrentPhaseEstimatedEndDate", "");
                    projectMap.put("CurrentPhaseActualEndDate", "");
                    projectMap.put("type", sType);
                    // added for Dashboard table
                    projectMap.put("CurrentPhaseDate", "");
                    // till here
                    projectMap.put("slipDays", "");
                }

                projectMap.put("col", col);
                projectMap.put("currentPhase", strCurrentPhase);
                projectMap.put("currentPhaseURL", sbCurrentPhaseURL.toString());

                projectDetails.add(projectMap);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally
        {
            return projectDetails;
        }
    }
    /**
     * 按当前日期获取项目第一层的任务、阶段或Gate；多个日期区间重叠时取预计完成时间距离当前时间最近的对象。
     **
     * @param context
     * @param list 项目第一层任务列表
     * @param projectState 项目状态
     * @return Map 当前任务、阶段或Gate
     * @author caipan
     * @date 2026/8/3 16:13
     */
    private Map getCurrentPhase(Context context, MapList list, String projectState) {
        Map currentMap = java.util.Collections.EMPTY_MAP;
        Date currentDate = new Date();
        long minimumFinishDateDistance = Long.MAX_VALUE;
        String estimatedStartSelect = Task.SELECT_TASK_ESTIMATED_START_DATE;
        String estimatedFinishSelect = Task.SELECT_TASK_ESTIMATED_FINISH_DATE;
        for (int i = 0; i < list.size(); i++) {
            Map taskMap = (Map) list.get(i);
            String type = (String) taskMap.get(DomainConstants.SELECT_TYPE);
            if (!DomainConstants.TYPE_TASK.equalsIgnoreCase(type)
                    && !TYPE_PHASE.equalsIgnoreCase(type)
                    && !TYPE_GATE.equalsIgnoreCase(type)) {
                continue;
            }
            String estimatedStartValue = (String) taskMap.get(estimatedStartSelect);
            String estimatedFinishValue = (String) taskMap.get(estimatedFinishSelect);
            if (UIUtil.isNullOrEmpty(estimatedStartValue) || UIUtil.isNullOrEmpty(estimatedFinishValue)) {
                continue;
            }
            try {
                Date estimatedStartDate = eMatrixDateFormat.getJavaDate(estimatedStartValue);
                Date estimatedFinishDate = eMatrixDateFormat.getJavaDate(estimatedFinishValue);
                boolean hasStarted = estimatedStartDate.before(currentDate) || areDatesEqual(estimatedStartDate, currentDate);
                boolean hasNotFinished = estimatedFinishDate.after(currentDate) || areDatesEqual(estimatedFinishDate, currentDate);
                if (!hasStarted || !hasNotFinished) {
                    continue;
                }
                long finishDateDistance = Math.abs(estimatedFinishDate.getTime() - currentDate.getTime());
                if (finishDateDistance < minimumFinishDateDistance) {
                    currentMap = taskMap;
                    minimumFinishDateDistance = finishDateDistance;
                }
            } catch (Exception e) {
                logger.warn("getCurrentPhase skip invalid task date, taskId:{}",
                        taskMap.get(DomainConstants.SELECT_ID), e);
            }
        }
        return currentMap;
    }
    /**
     * It returns true if two dates are equal
     *
     * @param  firstDate
     * @param  secondDate
     * @return true if firstDate and secondDate are equal
     */
    private boolean areDatesEqual(Date firstDate,Date secondDate){
        boolean returnVal = false;
        try{
            SimpleDateFormat sfd = new SimpleDateFormat("yyyy.MM.dd");
            String firstDateStr =  sfd.format(firstDate);
            String secondDateStr =  sfd.format(secondDate);

            returnVal = firstDateStr.equals(secondDateStr);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return returnVal;
    }
    private String getLastDecision(Context context,String gateId){
        String decisionName = "";
        try{
            GateReport grBean = new GateReport();
            decisionName = grBean.getLastDecision(context,gateId);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return decisionName;
    }
}
