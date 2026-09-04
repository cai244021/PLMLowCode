<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<!--
CE添加ECR
-->
<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<%!
    private static final Logger _logger = LoggerFactory.getLogger("JF_PreCreateEsoTaskCheck.jsp");
%>
<%
    Boolean isPush = Boolean.FALSE;
    String mess = DomainConstants.EMPTY_STRING;
    try {
        String task = emxGetParameter(request, "task");
        String[] split2 = task.split(",");
        StringList taskIdList = StringList.create(split2);
        String strObjectId = emxGetParameter(request, "objectId");
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        Map rowMap = ProgramCentralUtil.parseTableRowId(context,tableRowIdList[0]);
        String objId = (String) rowMap.get("objectId");
        _logger.info("objId:{}", objId);
        DomainObject domainObject = DomainObject.newInstance(context, objId);
        String personName = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
        _logger.info("personName:{}", personName);

        //当人员都没有部门和业务单位的时候 获取他的property
        String mql = "print Person $1 select $2 dump $3;";
        String result = MqlUtil.mqlCommand(context, true, false, mql, true, personName, "assignment", "@");
        String[] split = result.split("@");
        String organization = DomainConstants.EMPTY_STRING;
        for (int j = 0; j < split.length; j++) {
            String assignment = split[j];
            if (assignment.contains("ctx::VPLMCreator") || assignment.contains("ctx::VPLMProjectLeader")) {
                String[] split1 = assignment.split("\\.");
                organization = split1[1];
                break;
            }
        }
        _logger.info("organization:{}", organization);
        _logger.info("taskIdList:{}", taskIdList);

        ContextUtil.startTransaction(context, true);
        ContextUtil.pushContext(context);
        //转移责任人
        isPush = true;
        for (String oid: taskIdList) {
            DomainObject object = DomainObject.newInstance(context, oid);
            // 设置owner
            object.setOwner(context, personName);
            // 设置ECO的协作区和组织
            if (UIUtil.isNotNullAndNotEmpty(organization)) {
                object.setPrimaryOwnership(context, "JFSeat", organization);
            }
        }
        mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Event.Object_Owner_Changed", new String[]{});
        ContextUtil.commitTransaction(context);
    }catch (Exception e){
        ContextUtil.abortTransaction(context);
        if (isPush) {
            ContextUtil.popContext(context);
        }
        e.printStackTrace();
    }
%>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
<script>
    // var refreshURL = getTopWindow().getWindowOpener().location.href;
    // getTopWindow().getWindowOpener().location.href = refreshURL;
    var mess = "<%=mess%>";
    if ("" != mess) {
        alert(mess);
    }
    getTopWindow().getWindowOpener().parent.document.location.href = getTopWindow().getWindowOpener().parent.document.location.href;
    getTopWindow().closeWindow();
</script>