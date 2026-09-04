<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.apache.batik.gvt.filter.BackgroundRable8Bit" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="com.matrixone.apps.domain.util.*" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralConstants" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_PublicRefreshCaller.jsp");
%>

<%
    String strMode = emxGetParameter(request, "mode");
    String objectId = emxGetParameter(request, "objectId");
    objectId = XSSUtil.encodeURLForServer(context, objectId);
    LOGGER.info("strMode:{}", strMode);
    LOGGER.info("objectId:{}", objectId);
    if("refreshWBS".equals(strMode)) {
        Map<String,String> projectScheduleMap = ProgramCentralUtil.getProjectSchedule(context, objectId);
        String projectSchedule = projectScheduleMap.get(objectId);
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        StringList infoList = domainObject.getInfoList(context, "from[Subtask].to.id");
        LOGGER.info("!!!!!!!infoList:{}", infoList.size());
        String flag = infoList.size() > 1 ? "false" : "true";
        LOGGER.info("flag:{}", flag);
        LOGGER.info("projectSchedule:{}", projectSchedule);
        if(projectSchedule == null || projectSchedule.isEmpty()){
            projectSchedule = ProgramCentralConstants.PROJECT_SCHEDULE_AUTO;
        }
        LOGGER.info("projectSchedule:{}", projectSchedule);
        String portalCommandName = (String)emxGetParameter(request, "portalCmdName");
        portalCommandName = XSSUtil.encodeURLForServer(context, portalCommandName);
        LOGGER.info("portalCommandName:{}", portalCommandName);
        String mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Notice.ESOCreateComplete", new String[]{});
        LOGGER.info("mess:{}", mess);
        MapList taskMapList = (MapList) CacheUtil.getCacheObject(context, "newTasksMapList");
        LOGGER.info("taskMapList:{}", taskMapList);
        String selectedId = null;
        String rowId = emxGetParameter(request, "rowId");
        LOGGER.info("rowId:{}", rowId);
        boolean isFromRMB = "true".equalsIgnoreCase(emxGetParameter(request, "isFromRMB"));
        boolean isAddTaskAbove = Boolean.TRUE;
        String mode = DomainConstants.EMPTY_STRING;
        StringBuilder sBuff = new StringBuilder();
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
        LOGGER.info("sBuff:{}", sBuff);

%>
<script language="javascript">
    // findFrame(getTopWindow(),"PMCWBS").refreshSBTable();
    alert("<%=mess%>");
    // getTopWindow().getWindowOpener().parent.document.location.href = getTopWindow().getWindowOpener().parent.document.location.href;
    var frame = "<%=portalCommandName%>";
    var schedule = "<%=projectSchedule%>";
    var selectedObjId = "<%=selectedId%>";
    var isAddTaskAbove = "<%=isAddTaskAbove%>";
    var isAddTaskAbove = "<%=isAddTaskAbove%>";
    var flag = "<%=flag%>";
    if ("" === frame || null === frame) {
        frame = "PMCTaskSchedule";
    }
    var topFrame = getTopWindow().findFrame(getTopWindow().getWindowOpener().getTopWindow(), frame);
    if(null == topFrame){
        topFrame = findFrame(getTopWindow(), "PMCWhatIfExperimentStructure");
        if(null == topFrame)
            topFrame = findFrame(getTopWindow(), "detailsDisplay");
    }
    //Added by DI7
    if("Manual" == schedule){
        if ("true" === flag) {
            //刷新页面  add by ljr 用于解决删除ESO任务为空的时候，不手动刷新再次创建任务显示没有子级的问题
            findFrame(getTopWindow().getWindowOpener().getTopWindow(), frame).refreshSBTable();
        } else {
            topFrame.emxEditableTable.addToSelected('<%=sBuff.toString()%>');
            top.jQuery('.fonticon-refresh').addClass('fonticon-refresh-info');
            toggleRollupIcon(topFrame,"iconActionUpdateDates","iconActionUpdateDatesActive");
        }
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
    } else {
    }
%>
