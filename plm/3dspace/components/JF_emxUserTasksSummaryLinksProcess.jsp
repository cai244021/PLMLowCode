<%-- emxTasksSummaryLinksProcess.jsp -- for Opening the Window on clicking the Top links in Content Page.
  Copyright (c) 1992-2020 Dassault Systemes.
  All Rights Reserved.
  This program contains proprietary and trade secret information of MatrixOne, Inc.
  Copyright notice is precautionary only and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxUserTasksSummaryLinksProcess.jsp.rca 1.2.2.1.7.5 Wed Oct 22 16:17:47 2008 przemek Experimental przemek $
--%>
<%@page import="matrix.util.StringList"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@ page import = "com.matrixone.apps.domain.*,com.matrixone.apps.framework.ui.UINavigatorUtil, com.matrixone.apps.common.*,com.matrixone.apps.domain.util.*,com.matrixone.apps.common.UserTask" %>
<%@page import="jakarta.json.JsonArray"%>
<%@page import="jakarta.json.Json"%>
<%@page import="jakarta.json.JsonObject"%>
<%@page import="jakarta.json.JsonArrayBuilder"%>
<%@page import="jakarta.json.JsonObjectBuilder"%>
<%@page import="java.lang.reflect.Method"%>
<%@page import="com.matrixone.apps.domain.DomainConstants"%>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="java.util.Locale" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>

<script language="javascript" src="../common/scripts/emxUICore.js"></script>
<script language="javascript" src="../common/scripts/emxUIModal.js"></script>
<script language="javascript" src="../emxUIPageUtility.js"></script>
<script language="javascript" src="../common/scripts/emxUIConstants.js"></script>
<script type="text/javascript" src="../webapps/AmdLoader/AmdLoader.js"></script>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<script language="javascript" src="../common/scripts/emxUIToolbar.js"></script>
<script language="javascript" src="../common/scripts/emxUIEsignDialog.js"></script>
<script language="javascript" src="../common/scripts/emxUINavigator.js"></script>

<jsp:useBean id="formBean" scope="session" class="com.matrixone.apps.common.util.FormBean"/>
<%@include file = "emxRouteInclude.inc" %>
<%!
    private  static final Logger logger = LoggerFactory.getLogger("JF_PreProjectSpaceECRSelectCheck.jsp");
%>
<%
    String strLanguage = request.getHeader("Accept-Language");
    String i18NReadAndUnderstand =EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),
            "emxFramework.UserAuthentication.ReadAndUnderstand");

    String InProcess = (String)session.getAttribute("InProcess");
    String keyValue=emxGetParameter(request,"keyValue");
    String fromFDA = emxGetParameter(request,"fromFDA");
    String isCheck   = emxGetParameter(request,"isCheck");
    boolean bfromFDA = UIUtil.isNotNullAndNotEmpty(fromFDA)? ("True".equalsIgnoreCase(fromFDA) ? true: false): false;
    ArrayList<String> eSignList = new ArrayList<String>();
    String currTenantId = "OnPremise";
    String eSignId="";
    int eSignCount=0;
    String [] eSignRecords=null;
    String attr_RequiresEsign          = PropertyUtil.getSchemaProperty(context, "attribute_RequiresESign");
    String selectRequiresEsign = "from[" + DomainConstants.RELATIONSHIP_ROUTE_TASK + "].to.attribute[" + attr_RequiresEsign + "]";
    String alertMess = DomainConstants.EMPTY_STRING;
    if ("Y".equalsIgnoreCase(isCheck)) {
        Boolean isPush = Boolean.FALSE;
        try {
            ContextUtil.pushContext(context);
            isPush = true;
            logger.info("@@@@@@@@@@@@@@@@@");
            String titleZh = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("zh_CN"), "emxComponents.ECR.Review.ProjectManager");
            String titleen = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale("en"), "emxComponents.ECR.Review.ProjectManager");
            String[] tableRowIds = emxGetParameterValues(request, "emxTableRowId");
            StringList tableRowIdList = StringList.create(tableRowIds);
            logger.info("tableRowIdList:{}", tableRowIdList);
            DomainObject domainObject = DomainObject.newInstance(context);
            StringList selECRIdList = new StringList();
            StringList infoList = new StringList();
            String ecrId = DomainConstants.EMPTY_STRING;
            String type = DomainConstants.EMPTY_STRING;
            String taskId = DomainConstants.EMPTY_STRING;
            String title = DomainConstants.EMPTY_STRING;
            Map rowMap = new HashMap();
            StringList ecrList = StringList.create("JFECR", "JFNewECR", "JFFormalECR");
            for(int i=0; i < tableRowIdList.size(); i++){
                rowMap = ProgramCentralUtil.parseTableRowId(context,tableRowIdList.get(i));
                logger.info("rowMap:{}", rowMap);
                taskId = (String) rowMap.get("objectId");
                domainObject.setId(taskId);
                title = domainObject.getAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE);
                logger.info("title:{}", title);
                //拿取她的审核内容
                infoList = domainObject.getInfoList(context, "from[Route Task].to.to[Object Route].from.id");
                logger.info("infoList:{}", infoList);
                for (int i1 = 0; i1 < infoList.size(); i1++) {
                    ecrId = infoList.get(i1);
                    domainObject.setId(ecrId);
                    type = domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
                    if (ecrList.contains(type)) {
                        if (title.equalsIgnoreCase(titleZh) || title.equalsIgnoreCase(titleen)) {
                            selECRIdList.add(ecrId);
                        }
                    }
                }
            }
            logger.info("selECRIdList:{}", selECRIdList);
            //提示 ECR项目经理审核节点，不允许一次性审核多个ECR审核任务
            if (selECRIdList.size() > 1) {
                alertMess =  EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ECRReview.PMOneECR");
            }
        }catch (Exception e) {
            e.printStackTrace();
            alertMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.action.error");
        } finally {
            if (isPush) {
                ContextUtil.popContext(context);
            }
        }
    }
    if (UIUtil.isNotNullAndNotEmpty(alertMess)) {
%>
<script language="Javascript" >
    alert("<%=alertMess%>");
</script>
<%
    } else {
    if( UIUtil.isNotNullAndNotEmpty(InProcess)){
%>
<script language="Javascript" >
    alert(emxUIConstants.SUBMIT_URL_PREV_REQ_IN_PROCESS);
</script>
<%

}else {
    String sOutput = "";
    String errMsg = "";
    String objName = "";
    String sCompletedTasks = "";
    String curOutput = "";
    String taskAssigneeUserName = "";
    boolean isException = false;
    boolean isAuthRequired = false;

    String isFDAEnabled ="false";
    String esignConfigSetting ="None";
    try{
        esignConfigSetting = MqlUtil.mqlCommand(context, "list expression $1 select $2 dump","ENXESignRequiresESign", "value");
        if( UIUtil.isNullOrEmpty(esignConfigSetting) || ("").equals(esignConfigSetting))
            esignConfigSetting="None";
    }
    catch (Exception e){
        isFDAEnabled="false";
    }
    if("RouteSpecific".equalsIgnoreCase(esignConfigSetting)||"All".equalsIgnoreCase(esignConfigSetting))
    {
        isFDAEnabled = "true";
    }



    try{

        formBean.processForm(session,request);

        if(keyValue == null)
        {
            keyValue = formBean.newFormKey(session);
        }else{
            session.setAttribute("InProcess", "true");
        }

        formBean.processForm(session, request, "keyValue");
        //check all task to enable ESign start
        String objectId[] = ComponentsUIUtil.getSplitTableRowIds(formBean.getElementValues("emxTableRowId"));
        String[] onlyObjectId = new String[objectId.length];

        for(int arrIndex = 0; arrIndex < objectId.length; arrIndex++)
        {
            onlyObjectId[arrIndex]=(objectId[arrIndex].indexOf("|")!=-1)?(objectId[arrIndex].substring(objectId[arrIndex].lastIndexOf("|")+1)):objectId[arrIndex];
        }
        StringList masterSelects = new StringList();
        masterSelects.add(DomainObject.SELECT_TYPE);
        masterSelects.add(DomainObject.SELECT_NAME);
        masterSelects.add(selectRequiresEsign);
        masterSelects.add("attribute["+DomainConstants.ATTRIBUTE_ROUTE_ACTION+"]");
        masterSelects.add(DomainObject.SELECT_PHYSICAL_ID);
        masterSelects.add(DomainObject.SELECT_CURRENT);
        MapList mlist = DomainObject.getInfo(context, onlyObjectId, masterSelects);
        DomainObject genericDomainObject = new DomainObject();
        String taskCurrent = "";
        String taskObjRouteAction = "";
        String objRequiresESign = "";
        for(int i=0; i<mlist.size(); i++){
            Map map = (Map) mlist.get(i);
            objRequiresESign = (String) map.get(selectRequiresEsign);
            taskObjRouteAction = (String) map.get("attribute["+DomainConstants.ATTRIBUTE_ROUTE_ACTION+"]");
            taskCurrent = (String) map.get(DomainObject.SELECT_CURRENT);
            if("True".equalsIgnoreCase(objRequiresESign) && !"Review".equalsIgnoreCase(taskCurrent) && !"Comment".equalsIgnoreCase(taskObjRouteAction)){
                isFDAEnabled="true";
                break;
            }else{
                isFDAEnabled="false";
            }
        }
        //check all task to enable ESign end
        boolean blnAction=false;
        String jsTreeID   = emxGetParameter(request,"jsTreeID");
        String suiteKey   = emxGetParameter(request,"suiteKey");
        String isFDAEntered   = emxGetParameter(request,"isFDAEntered");
        String comments   = emxGetParameter(request,"txtComments");
        String sAction   = emxGetParameter(request,"fromPage");
        String eSignTaskId="";
        String sActionSuccessful = "";

        if( comments == null || comments.equals(""))
        {
            comments   = (String)formBean.getElementValue("txtComments");
        }
        if(UIUtil.isNotNullAndNotEmpty(isFDAEnabled) && isFDAEnabled.equals("true")){
            String eSignRecordsArry=emxGetParameter(request, "eSignRecordId");
            if(eSignRecordsArry!=null){
                eSignRecords=eSignRecordsArry.split(",");
            }
        }

        boolean isCommentsRequired = false;
        String returnFromPopup = emxGetParameter(request, "returnBack");
        if (returnFromPopup == null)
        {
            returnFromPopup = "false";
        }

        if ("true".equalsIgnoreCase(returnFromPopup))
        {
            DomainObject taskObject = new DomainObject();
            if(objectId.length > 1){
                taskAssigneeUserName = context.getUser();
            }
            else{
                String currentTaskid = onlyObjectId[0];
                taskObject.setId(currentTaskid);
                taskAssigneeUserName = taskObject.getInfo(context,"from[" + DomainConstants.RELATIONSHIP_PROJECT_TASK + "].to.name");
            }

            if(!FrameworkUtil.isOnPremise(context)){
                currTenantId = context.getTenant();
            }


        }

        UserTask userTask = new UserTask();
        String requireComment = "false";

        if ("false".equalsIgnoreCase(returnFromPopup))
        {
            isCommentsRequired = userTask.isCommentsRequired(context, onlyObjectId, sAction);
            if (!isCommentsRequired){
                returnFromPopup = "true";
            }
            String isApprovalCommentRequired = EnoviaResourceBundle.getProperty(context,"emxComponents.Routes.EnforceAssigneeApprovalComments");
            String isOwnerCommentRequired = EnoviaResourceBundle.getProperty(context,"emxComponents.Routes.EnforceOwnerReviewComments");
            StringList selectable = new StringList();
            selectable.add(DomainConstants.SELECT_CURRENT);
            selectable.add("attribute["+DomainConstants.ATTRIBUTE_ROUTE_ACTION+"]");
            MapList mapList      = DomainObject.getInfo(context, onlyObjectId, selectable);
            for(int i=0; i<mapList.size(); i++){
                Map map = (Map) mapList.get(i);
                String current = (String)map.get(DomainConstants.SELECT_CURRENT);
                String routeAction = (String)map.get("attribute["+DomainConstants.ATTRIBUTE_ROUTE_ACTION+"]");
                if(("true".equals(isApprovalCommentRequired) && "Assigned".equals(current)) ||("true".equals(isOwnerCommentRequired) && "Review".equals(current)) || "Comment".equals(routeAction)){
                    requireComment = "true";
                    isCommentsRequired = true;
                    returnFromPopup = "false";
                    break;
                }
            }
        }

        boolean isApproveAction = userTask.isListContainsApproveAction(context,  onlyObjectId, DomainObject.TYPE_INBOX_TASK + "~Approve");

        if ("true".equalsIgnoreCase(returnFromPopup))
        {
            if ( (isApproveAction && "true".equalsIgnoreCase(isFDAEnabled) && "true".equalsIgnoreCase(isFDAEntered)) || !isApproveAction || (isApproveAction && "false".equalsIgnoreCase(isFDAEnabled)) )
            {
                HashMap appMap = new HashMap();
                String sSubject = i18nNow.getI18nString("emxComponents.common.TaskDeletionNotice", "emxComponentsStringResource" ,sLanguage);
                String sMessage1 = i18nNow.getI18nString("emxComponents.common.TaskDeletionMessage3", "emxComponentsStringResource" ,sLanguage);
                String sMessage2 = i18nNow.getI18nString("emxComponents.common.TaskDeletionMessage2", "emxComponentsStringResource" ,sLanguage);
                String sRouteTaskUser = i18nNow.getI18nString("emxComponents.TaskSummary.TasksNotAccepted", "emxComponentsStringResource" ,sLanguage);
                String sCannotRejectReason = i18nNow.getI18nString("emxComponents.InboxTask.CannotRejectReason", "emxComponentsStringResource", sLanguage);
                String sRouteStopped = i18nNow.getI18nString("emxComponents.Task.RouteStopped", "emxComponentsStringResource", sLanguage);
                String sTasksCompleted = i18nNow.getI18nString("emxComponents.Task.TaskCompleted", "emxComponentsStringResource", sLanguage);


                appMap.put("eServiceComponents.treeMenu.Route", JSPUtil.getApplicationProperty(context,application,"eServiceComponents.treeMenu.Route"));
                appMap.put("eServiceComponents.treeMenu.InboxTask", JSPUtil.getApplicationProperty(context,application,"eServiceComponents.treeMenu.InboxTask"));
                appMap.put("emxComponents.common.TaskDeletionNotice", sSubject);
                appMap.put("emxComponents.common.TaskDeletionMessage3", sMessage1);
                appMap.put("emxComponents.common.TaskDeletionMessage2", sMessage2);
                appMap.put("emxComponents.TaskSummary.TasksNotAccepted", sRouteTaskUser);
                appMap.put("emxComponents.Task.RouteStopped", sRouteStopped);
                appMap.put("emxComponents.Task.TaskCompleted", sTasksCompleted);
                appMap.put("emxComponents.InboxTask.CannotRejectReason", sCannotRejectReason);


                double clientTZOffset = (new Double((String)session.getValue("timeZone"))).doubleValue();
                //sOutput = userTask.doProcess(context, onlyObjectId, appMap, comments, sAction, clientTZOffset);


                DomainObject genericObject = new DomainObject();

                String taskId = "";
                String objType = "";
                String requiresEsign = "";
                String taskRouteAction = "";
                try {
                    ContextUtil.startTransaction(context,true);
                    for (int index=0 ;index < onlyObjectId.length; index++)
                    {
                        curOutput = "";
                        try
                        {

                            taskId = onlyObjectId[index];
                            genericObject.setId(taskId);

                            StringList selectList = new StringList();
                            selectList.add(DomainObject.SELECT_TYPE);
                            selectList.add(DomainObject.SELECT_NAME);
                            selectList.add(selectRequiresEsign);
                            selectList.add("attribute["+DomainConstants.ATTRIBUTE_ROUTE_ACTION+"]");
                            selectList.add(DomainObject.SELECT_PHYSICAL_ID);
                            Map taskMap = genericObject.getInfo(context, selectList);

                            objType = (String) taskMap.get(DomainObject.SELECT_TYPE);
                            objName = (String) taskMap.get(DomainObject.SELECT_NAME);
                            requiresEsign = (String) taskMap.get(selectRequiresEsign);
                            taskRouteAction = (String) taskMap.get("attribute["+DomainConstants.ATTRIBUTE_ROUTE_ACTION+"]");
                            eSignTaskId=(String)taskMap.get(DomainObject.SELECT_PHYSICAL_ID);

                            if (DomainObject.TYPE_INBOX_TASK.equals(objType))
                            {
                                InboxTask taskObject  = (InboxTask)DomainObject.newInstance(context, taskId);
                                if(!taskObject.canCompleteTask(context)){
                                    throw new Exception(i18nNow.getI18nString("emxComponents.Common.CanNotPromoteTask" ,"emxComponentsStringResource", sLanguage));
                                }
                                curOutput = userTask.doInboxTaskAction(context, taskId, appMap, comments, sAction, clientTZOffset);
                                if(!"".equals(curOutput)){
                                    sOutput += curOutput;
                                }else{
                                    sCompletedTasks += objName + "\n";
                                }
                            }
                            else if (DomainObject.TYPE_TASK.equals(objType))
                            {
                                curOutput = userTask.doWBSTaskAction(context, taskId, sAction);
                                if(!"".equals(curOutput)){
                                    sOutput += curOutput;
                                }else{
                                    sCompletedTasks += objName + "\n";
                                }
                            }


                            if(UIUtil.isNotNullAndNotEmpty(requiresEsign) && requiresEsign.equalsIgnoreCase("true") && taskRouteAction.equals("Approve"))
                            {
                                MqlUtil.mqlCommand(context, "Modify bus $1 add history $2 comment $3",false, taskId,sAction,i18NReadAndUnderstand);
                                if(eSignRecords!= null){
                                    eSignId=eSignRecords[eSignCount];
                                    eSignCount++;
                                    eSignList.add(eSignId+"@"+eSignTaskId+"@"+sAction);

                                }
                            }

                        }
                        catch(Exception ex)
                        {
                            isException = true;
                            boolean clientTaskMessagesExists = false;
                            clientTaskMessagesExists = MqlNoticeUtil.checkIfClientTaskMessageExists(context);
                            if (clientTaskMessagesExists) {
                                //  already handled by check trigger.
                            }else {
                                errMsg += objName + " : " + ex.getMessage() + "\n";
                            }
                            ContextUtil.abortTransaction(context);
                            break;
                        }
                    }

                    if (errMsg.length() > 0)
                    {
                        session.putValue("error.message", errMsg);
                    }
                    if(!isException)
                    {
                        ContextUtil.commitTransaction(context);
                        if(UIUtil.isNotNullAndNotEmpty(isFDAEnabled) && isFDAEnabled.equals("true") && eSignList!= null){
                            //ESign record update
                            StringList taskIdList=new StringList();
                            String taskPhysicalId="";
                            String eSignRecordId=null;
                            String taskAction="";
                            try {
                                for(String taskDetails:eSignList) {
                                    String []esignRecordArry = taskDetails.split("@");
                                    eSignRecordId= esignRecordArry[0];
                                    taskPhysicalId = esignRecordArry[1];
                                    taskAction= esignRecordArry[2];
                                    if(UIUtil.isNotNullAndNotEmpty(taskAction) &&  taskAction!= "" ) {
                                        if("Reject".equals(taskAction)){
                                            taskAction="Disapprove";
                                        }else if("Complete".equals(taskAction)){
                                            taskAction="Approve";
                                        }
                                    }
                                    DomainObject task = new DomainObject();
                                    task.setId(taskPhysicalId);
                                    //check the db calls
                                    StringList selectStmt = new StringList();
                                    selectStmt.addElement(DomainConstants.SELECT_REVISION);
                                    selectStmt.addElement(DomainConstants.SELECT_CURRENT);
                                    Map taskInfoMap           = task.getInfo(context, selectStmt);
                                    String taskState = (String)taskInfoMap.get(DomainConstants.SELECT_CURRENT);
                                    String taskLatestRevision=(String)taskInfoMap.get(DomainConstants.SELECT_REVISION);
                                    if(DomainObject.STATE_INBOX_TASK_COMPLETE.equals(taskState) || DomainObject.STATE_INBOX_TASK_REVIEW.equals(taskState) ) {
                                        //API to update the eSign
                                        JsonObjectBuilder jsonMaturityObj = BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder());
                                        JsonObjectBuilder jsonObjRevision = BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder());
                                        JsonObjectBuilder jsonObj = BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder());
                                        jsonObj.add("before","To Do");
                                        jsonObj.add("after",taskState);
                                        jsonObjRevision.add("ObjectRevision",taskLatestRevision);
                                        jsonObjRevision.build();
                                        jsonMaturityObj.add("MaturityChange",jsonObj.build());
                                        jsonMaturityObj.build();
                                        JsonArrayBuilder jsonArry = Json.createArrayBuilder();
                                        jsonArry.add(jsonObjRevision);
                                        jsonArry.add(jsonMaturityObj);
                                        JsonObjectBuilder actionTakenObj = BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder());
                                        actionTakenObj.add("actionType", jsonArry.build());
                                        final Map<String, String> updates = new HashMap<String, String>();
                                        updates.put("ESignObjectRef",taskPhysicalId);
                                        updates.put("ESignObjectServiceID", "3DSpace");
                                        updates.put("ESignObjectURI", "/v1/resources/task/");
                                        updates.put("ESignActionTaken", actionTakenObj.build().toString().replaceAll("\\\\", ""));
                                        updates.put("eSignRecordId", eSignRecordId);
                                        updates.put("ESignObjectActionType", taskAction);
                                        System.out.println("updates"+updates.toString());
                                        Class objectTypeArray[] = new Class[2];
                                        objectTypeArray[0] =  context.getClass();
                                        objectTypeArray[1] = Map.class;
                                        Class<?> c = Class.forName("com.dassault_systemes.enovia.esign.ESignRecordUtil");
                                        Object eSignRecordUtil = c.newInstance();
                                        Method updateESignRecord = c.getMethod("UpdateESignRecord", objectTypeArray);
                                        updateESignRecord.invoke(eSignRecordUtil, context,updates);
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("Exception in updateEsignRecord"+e.getCause());
                            }
                        }
                    }
                }
                catch(Exception ex1) {
                    isException = true;
                    ContextUtil.abortTransaction(context);

                }

            }
        }
%>
<body>

<%
    if(isCommentsRequired && isApproveAction && ("true".equalsIgnoreCase(isFDAEnabled) && !"true".equalsIgnoreCase(isFDAEntered)))
    {
%>
<script language="Javascript" >
    emxShowModalDialog('emxRouteTaskAddCommentsFS.jsp?keyValue=<%=XSSUtil.encodeForURL(context, keyValue)%>&fromPage=<%=XSSUtil.encodeForURL(context, sAction)%>&isFDAEnabled=<%=isFDAEnabled%>',575, 575);
</script>
<%
}
else if( (isCommentsRequired && !isApproveAction) || (isCommentsRequired && isApproveAction && "false".equalsIgnoreCase(isFDAEnabled)) )
{
%>
<script language="Javascript" >
    emxShowModalDialog('emxRouteTaskAddCommentsFS.jsp?keyValue=<%=XSSUtil.encodeForURL(context, keyValue)%>&fromPage=<%=XSSUtil.encodeForURL(context, sAction)%>&requireComment=<%=XSSUtil.encodeForURL(context, requireComment)%>',575, 575);
</script>
<%
}
else if ("true".equalsIgnoreCase(isFDAEnabled) && !"true".equalsIgnoreCase(isFDAEntered) && isApproveAction)
{
%>
<script language="Javascript" >

    var action = "../components/emxUserTasksSummaryLinksProcess.jsp?keyValue=<%=XSSUtil.encodeForURL(context, keyValue)%>&fromPage=<%=XSSUtil.encodeForURL(context, sAction)%>&isFDAEntered=true&returnBack=true";
    var fromJSP = "taskSummaryCompleteRejectProcess";

    var jsonObj = {"taskAssigneeUserName":"<%=taskAssigneeUserName%>", "currTenantId":"<%=currTenantId%>"};
    if(getTopWindow().launchnew){
        getTopWindow().launchnew(action,fromJSP, jsonObj);
    }
</script>

<%
}
else
{
    session.removeAttribute("InProcess");
    if (!isException && (!"".equals(sOutput) || !"".equals(sCompletedTasks)))
    {
        String sCompleteComments = i18nNow.getI18nString("emxComponents.Task.CompleteComments", "emxComponentsStringResource" ,sLanguage);
        if ("Complete".equalsIgnoreCase(sAction))
        {
            sOutput = !"".equals(sOutput) ? (sCompleteComments + ":\n"+ sOutput) : "";
            sActionSuccessful = i18nNow.getI18nString("emxComponents.Task.TaskCompletionSuccessful", "emxComponentsStringResource" ,sLanguage);
        }
        else if ("Reject".equalsIgnoreCase(sAction))
        {
            sOutput = !"".equals(sOutput) ? (sCompleteComments + ":\n" + sOutput) : "";
            sActionSuccessful = i18nNow.getI18nString("emxComponents.Task.TaskRejectionSuccessful", "emxComponentsStringResource" ,sLanguage);
        }
        sOutput = sCompletedTasks.length() > 0 ? (sActionSuccessful + ": " + "\n" + sCompletedTasks + "\n" + sOutput) : sOutput;

%>
<script>
    var temp = "<%=XSSUtil.encodeForJavaScript(context, sOutput)%>";
    alert(temp);
</script>
<%
    }
%>
<script>
    if(<%=bfromFDA%>){
        var taskContentFrame  =  findFrame(getTopWindow(), "content");
        if(taskContentFrame){
            taskContentFrame.location.reload();
        }
    }
    else {

        if (parent.window.getWindowOpener() != null)
        {
            parent.window.getWindowOpener().parent.location.href = parent.window.getWindowOpener().parent.location.href.replace("persist=true", "");
            getTopWindow().close();
        }
        else
        {
            parent.window.location.href = parent.window.location.href;
        }
    }
</script>
<%
            }
        }
        finally{
            session.removeAttribute("InProcess");
        }
    }
  }
%>


</body>
