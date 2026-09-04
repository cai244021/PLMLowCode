<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_PCRTasKPromote.jsp");
%>
<%
    String suiteKey = "emxComponentsStringResource";
    String RELATIONSHIP_REFERENCE_DOCUMENT = "Reference Document";
    StringBuffer stringBuffer = new StringBuffer();
    StringList bosel = new StringList();
    bosel.add(DomainObject.SELECT_ID);
    bosel.add(DomainObject.SELECT_NAME);
    bosel.add(DomainObject.SELECT_CURRENT);
    String alertResult = "";
    Boolean flag = true;
    try {
        String owner = context.getUser();
        ContextUtil.pushContext(context);
        String objectId = emxGetParameter(request, "objectId");//PCR
        String[] tableRowId = emxGetParameterValues(request, "emxTableRowIdActual");
        String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
        LOGGER.info("splitTableRowIds:{} objectId：{}",splitTableRowIds,objectId);
        DomainObject taskObj = DomainObject.newInstance(context);
        String isNullMessage = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.PCR.noticeNullMessage");
        String processTaskMessage = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.PCR.noTask");
        String onlyowner = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.PCR.onlyOwner");
        String PerformTaskFinishDateMessage = EnoviaResourceBundle.getProperty(context, suiteKey, context.getLocale(), "emxComponents.PCR.PerformTaskFinishDate");
        //查询当前PCR下面的执行任务，条件的owner是当前登录人并且Title可以匹配上
        DomainObject pcrObj = DomainObject.newInstance(context,objectId);
        StringList selList = new StringList();
        selList.add(DomainConstants.SELECT_ID);
        selList.add("attribute[JF_PCRFunction]");
        selList.add("attribute[Task Estimated Finish Date]");

        StringList relList = new StringList();
        relList.add(DomainConstants.SELECT_RELATIONSHIP_ID);
        String where = "owner=='"+owner+"' ";
       MapList taskList =  pcrObj.getRelatedObjects(context,
                "JF_PCR2ExecuteTask", //pattern to match relationships
                "JF_PCRExecuteTask", //pattern to match types
                selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                relList, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                false, //get To relationships
                true, //get From relationships
                (short) 1, //the number of levels to expand, 0 equals expand all.
                where, //where clause to apply to objects, can be empty ""
                null, //where clause to apply to relationship, can be empty ""
                (short) 0); //limit
/*       MapList executeList =(MapList) taskList.stream().map(m->{
            Map infoMap = (Map) m;
            return UIUtil.getValue(infoMap,"attribute[JF_PCRFunction]");
        }).collect(Collectors.toCollection(MapList::new));*/
//        String executeStr = executeList.join(",");

        LOGGER.info("taskList:{} JF_PCRFunction:{}",taskList );
        for(int i=0;i<splitTableRowIds.length;i++){//选中的评估任务
            taskObj.setId(splitTableRowIds[i]);
            String JF_Conclusion = taskObj.getAttributeValue(context, "JF_Conclusion");
            String Title = taskObj.getAttributeValue(context, "Title");
           /* if(Title.equals("\u4F9B\u5E94\u5546\u8D28\u91CF")||Title.equals("\u91C7\u8D2D")){//如果评估任务的标题为供应商质量或者采购就直接改成供应链
                Title ="\u4F9B\u5E94\u94FE";//因为需要的执行任务是对应的供应链
            }*/
            String copyTitle = Title;
            String taskowner  = taskObj.getInfo(context,DomainConstants.SELECT_OWNER);
            LOGGER.info("taskowner:{} owner:{}",taskowner,owner);
            if(owner.equalsIgnoreCase(taskowner)) {
                if (UIUtil.isNullOrEmpty(JF_Conclusion)) {
                    stringBuffer.append(isNullMessage);
                }
                if ("OK".equalsIgnoreCase(JF_Conclusion)) {//必须创建执行任务
                    if (taskList.size() == 0) {
                        if (stringBuffer.length() == 0) {
                            stringBuffer.append(processTaskMessage);
                        } else {
                            stringBuffer.append("\\n");
                            stringBuffer.append(processTaskMessage);
                        }
                    } else if (!taskList.stream().anyMatch(item -> {
                        Map mapInfo = (Map) item;
                        String itemTitle = UIUtil.getValue(mapInfo, "attribute[JF_PCRFunction]");
                        boolean mask = false;
                        if (copyTitle.contains(itemTitle)) {
                            mask = true;
                        }
                        //如果评估任务是工艺的情况下，执行任务是工艺或者设备都算已经创建了
                        else if (copyTitle.contains("\u5DE5\u827A") && (itemTitle.equals("\u8BBE\u5907") || itemTitle.equals("\u5DE5\u827A"))) {
                            mask = true;
                        }
                        return mask;
                    })) {
                        if (stringBuffer.length() == 0) {
                            stringBuffer.append(processTaskMessage);
                        } else {
                            stringBuffer.append("\\n");
                            stringBuffer.append(processTaskMessage);
                        }
                    }
                }
                //如果没有错误的情况下，在看所有的执行的人是否填写计划完成日期
                if (stringBuffer.length() == 0){
                    //校验所有的执行任务是否有计划完成日期没有填写日期
                    if (!taskList.stream().allMatch(item -> {
                        Map mapInfo = (Map) item;
                        String itemFinishDate = UIUtil.getValue(mapInfo, "attribute[Task Estimated Finish Date]");
                        boolean mask = false;
                        if (UIUtil.isNotNullAndNotEmpty(itemFinishDate)) {//如果计划完成日期为空的情况下，不允许提交完成
                            mask = true;
                        }
                        return mask;
                    })) {
                        if (stringBuffer.length() == 0) {
                            stringBuffer.append(PerformTaskFinishDateMessage);
                        } else {
                            stringBuffer.append("\\n");
                            stringBuffer.append(PerformTaskFinishDateMessage);
                        }
                    }
            }
            }else{
                stringBuffer = new StringBuffer();
                stringBuffer.append(onlyowner);
                break;
            }
        }
        LOGGER.info("stringBuffer:{}",stringBuffer);
        if(stringBuffer.length() == 0){
            flag = false;
//            taskObj.promote(context);
            //设置状态未审核中，如果是PCR评估类型
            for(int i=0;i<splitTableRowIds.length;i++) {
//                if ("JF_PCRTask".equalsIgnoreCase(type)) {
                    MqlUtil.mqlCommand(context, false, "mod bus " + splitTableRowIds[i] + " current Review", true);
                    //调用下方法判断下是否是最后一个评估任务,如果是最后一个评估任务，就生成评估流程
                 JPO.invoke(context, "JF_PCR", new String[0], "getActiveEvaluateTask", new String[]{objectId}, void.class);
//                }
            }
        }
        alertResult = stringBuffer.toString();
    }catch (Exception e) {
        e.printStackTrace();
    }finally {
        ContextUtil.popContext(context);
    }

%>
<html>
<script >
    if(<%=flag%>) {
        alert("<%=alertResult%>");
    }else {
        parent.window.parent.location.href=parent.window.parent.location.href
    }

</script>
</html>
