<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="matrix.db.AttributeList" %>
<%@ page import="matrix.db.Attribute" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.SELECT_NAME" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralConstants" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.context1" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<%@include file="../common/emxNavigatorInclude.inc" %>
<%!
    private static final Logger _logger = LoggerFactory.getLogger("JF_PreCreateEsoTaskCheck.jsp");
%>


<%
    String strMess = DomainConstants.EMPTY_STRING;
    String strURL = DomainConstants.EMPTY_STRING;
    try {
//        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
//        StringList strIdList = FrameworkUtil.split(strSelectIds[0], "|");
//        _logger.info("strIdList:{}", strIdList);
//        String objectId = DomainConstants.EMPTY_STRING;
//        if (strIdList.size() == 4) {
//            objectId = strIdList.get(1);
//        } else {
//            objectId = strIdList.get(0);
//        }
        String portalCommandName = emxGetParameter(request, "portalCmdName");
        portalCommandName = XSSUtil.encodeURLForServer(context, portalCommandName);
        String selectedNodeId = emxGetParameter(request, "emxTableRowId");
        String parentId = (String)(ProgramCentralUtil.parseTableRowId(context,selectedNodeId)).get("parentOId");
        parentId = XSSUtil.encodeURLForServer(context, parentId);

        String rowId = (String)(ProgramCentralUtil.parseTableRowId(context,selectedNodeId)).get("rowId");

        String objectId = (String)(ProgramCentralUtil.parseTableRowId(context,selectedNodeId)).get("objectId");
        objectId = XSSUtil.encodeURLForServer(context, objectId);

        _logger.info("portalCommandName:{}", portalCommandName);
        _logger.info("objectId:{}", objectId);
        //判断该任务的类型
        DomainObject domainObject = DomainObject.newInstance(context);
        domainObject.setId(objectId);
        String type = domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
        String strName = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
        _logger.info("type:{}", type);
        _logger.info("strName:{}", strName);
        //当任务名称是ESO和ESO签发的时候，获取ESO任务为CS的时候
        if (!((DomainConstants.TYPE_TASK.equalsIgnoreCase(type) && (strName.equalsIgnoreCase("ESO") || strName.equalsIgnoreCase("ESO\u7b7e\u53d1"))) || ("JF_ESOTask".equalsIgnoreCase(type) && strName.equalsIgnoreCase("CS")))) {
            strMess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.Mess.TypeError");
            _logger.info("strMess:{}", strMess);
 %>
<script language="Javascript">
    alert("<%=strMess%>");
</script>
<%
            return;
        }
    StringList busSelectList = new StringList();
    busSelectList.add(DomainConstants.SELECT_ID);
    busSelectList.add(DomainConstants.SELECT_TYPE);
    busSelectList.add(DomainConstants.SELECT_NAME);
    busSelectList.add(DomainConstants.SELECT_LEVEL);
    busSelectList.add(DomainConstants.SELECT_ORIGINATED);
    //判断当前任务的阶段是否在ESO项目模板中匹配
    MapList mapList = domainObject.getRelatedObjects(
            context,
            DomainRelationship.RELATIONSHIP_SUBTASK,
            DomainConstants.TYPE_TASK + ",Phase",
            busSelectList,
            new StringList(),
            true,
            false,
            (short) 0,
            "",
            "",
            0
    );
    _logger.info("mapList:{}", mapList);
    mapList = (MapList) mapList.stream().filter(m -> {
        Map map = (Map) m;
        String strType = UIUtil.getValue(map, DomainConstants.SELECT_TYPE);
        if ("Phase".equalsIgnoreCase(strType)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }).collect(Collectors.toCollection(MapList::new));
    Boolean flag = Boolean.TRUE;
    String tempId = JPO.invoke(context, "JF_PublicMethodClass", null, "getBasicUrl", new String[]{"ESOTask.Template.Id"}, String.class);
    if (mapList.isEmpty()) {
        flag = Boolean.FALSE;
    } else {
        mapList.addSortKey(DomainConstants.SELECT_ORIGINATED, ProgramCentralConstants.ASCENDING_SORT, ProgramCentralConstants.SORTTYPE_STRING);
        mapList.sort();
        Map map = (Map) mapList.get(0);
        String phaseName = (String) map.get(DomainConstants.SELECT_NAME);
        _logger.info("phaseName:{}", phaseName);
        //获取ESO任务模板
        StringList tempPhaseList  = JPO.invoke(context, "JF_ESO", null, "getTemplateESOTaskFirstPhases", new String[]{DomainConstants.SELECT_NAME}, StringList.class);
        //获取ESO项目中的所有的phase,匹配
        _logger.info("tempPhaseList:{}", tempPhaseList);
        if (!tempPhaseList.contains(phaseName)) {
            flag = Boolean.FALSE;
        }
    }
    if (!flag) {
        strMess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.Mess.PhaseError");
        _logger.info("strMess:{}", strMess);
%>
<script language="Javascript">
    alert("<%=strMess%>");
</script>
<%
        return;
    }
    //  1. ESO相关的项目属性是否填写完成 项目代号、客户、3R Grade、项目状态  必填
    domainObject.setId(objectId);
    String projectId = domainObject.getInfo(context, "to[Project Access Key].from.from[Project Access List].to.id");
     _logger.info("projectId:{}", projectId);
    domainObject.setId(projectId);
    StringList selAttrList = new StringList();
    selAttrList.add("JF_ProjectECI");
    selAttrList.add("JF_ProjectGrade");
    selAttrList.add("JF_ProjectStatus");
    StringList errorList = new StringList();
    AttributeList attributes = domainObject.getAttributeValues(context, selAttrList);
    _logger.info("projectId:{}", projectId);
    Boolean r3Flag = Boolean.FALSE;
    for (int i = 0; i < attributes.size(); i++) {
        Attribute attribute = attributes.get(i);
        String value = attribute.getValue();
        String name = attribute.getName();
        if (UIUtil.isNullOrEmpty(value)) {
            if ("JF_ProjectGrade".equalsIgnoreCase(name)) {
                r3Flag = Boolean.TRUE;
            } else {
                errorList.add(EnoviaResourceBundle.getAttributeI18NString(context, name, context.getLocale().toString()));
            }
        }
    }
    if (!errorList.isEmpty() || r3Flag) {
        strMess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.Mess.AttributeError");
        strMess += errorList.join(",");
        if (r3Flag) {
            strMess += "," + EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.NoticeMess.AttributeError");
        }
        _logger.info("strMess:{}", strMess);
%>
<script language="Javascript">
    alert("<%=strMess%>");
</script>
<%
        return;
    }

//            add by ljr 20250613 打开标准件发布流程 form 填写流程信息  并发起  紧固件
//    strURL = "../common/emxCreate.jsp?nameField=both&autoNameChecked=true&showApply=true&type=type_JF_ESOTask&policy=policy_Project_Task&form=JFESOTaskCreateForm&preProcessJavaScript=reloadESOFunctionModules&ProcessJavaScript=&mode=create&postProcessURL=../programcentral/JF_PublicRefreshCaller.jsp?mode=refreshWBS&submitAction=nothing&createJPO=JF_ESO:createESOTask&header=emxFramework.JFESOTask.CreateNew&postProcessJPO=JF_ESO:createPostProcess";
//        strURL = "../common/emxCreate.jsp?nameField=both&autoNameChecked=true&showApply=true&type=type_JF_ESOTask&policy=policy_Project_Task&HelpMarker=emxhelpwbsadddialog&form=JFESOTaskCreateForm&preProcessJavaScript=reloadESOFunctionModules&ProcessJavaScript=&mode=create&postProcessURL=../programcentral/emxProgramCentralUtil.jsp?mode=addSubTaskBelow&submitAction=doNothing&createJPO=JF_ESO:createESOTask&header=emxFramework.JFESOTask.CreateNew&postProcessJPO=JF_ESO:createPostProcess";
        strURL = "../common/emxCreate.jsp?nameField=both&autoNameChecked=true&showApply=true&type=type_JF_ESOTask&policy=policy_Project_Task&HelpMarker=emxhelpwbsadddialog&form=JFESOTaskCreateForm&preProcessJavaScript=reloadESOFunctionModules&ProcessJavaScript=&mode=create&postProcessURL=../programcentral/JF_PublicRefreshCaller.jsp?mode=refreshWBS&submitAction=doNothing&createJPO=JF_ESO:createESOTask&header=emxFramework.JFESOTask.CreateNew&postProcessJPO=JF_ESO:createPostProcess";
    strURL += "&tempId=" + tempId + "&objectId="  + objectId + "&projectId=" + projectId + "&rowId=" + rowId + "&portalCmdName=" + portalCommandName;
//            end
    }catch (Exception e) {
        e.printStackTrace();
    }

%>
<html>
<body>
<script language="Javascript">
    showModalDialog("<%=strURL%>");
    <%--showWizard("<%=strURL%>");--%>
    <%--window.open("<%=strURL%>", '', 'width=700,height=600,left=400,top=150,scrollbars=no,location=no,fullscreen=no', 'false');--%>
    // findFrame(getTopWindow(),"PMCWBS").emxEditableTable.refreshSelectedRows();
</script>
</body>
</html>
