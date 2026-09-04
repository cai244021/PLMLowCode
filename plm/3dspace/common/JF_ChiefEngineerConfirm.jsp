
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="matrix.util.MatrixException" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.commons.collections4.CollectionUtils" %>

<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_ChiefEngineerConfirm.jsp");
%>
<%
    String strObjectId = emxGetParameter(request, "objectId");
    JF_LOGGER.info("JF_ChiefEngineerConfirm.jsp----strObjectId::{}",strObjectId);
    String mess = "";
    String[] splitTableRowIds = new String[1];
    StringBuffer sbUrl = new StringBuffer( "JF_ChiefEngineerConfirmDialogFS.jsp");
    String AMP  = "&";
    try {
        String[] tableRowId = emxGetParameterValues(request, "emxTableRowId");
        JF_LOGGER.info("JF_ChiefEngineerConfirm.jsp======tableRowId:{}", Arrays.asList(tableRowId));
        splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
        DomainObject domainObject = DomainObject.newInstance(context,splitTableRowIds[0]);
        StringList busel = new StringList();
        busel.add("attribute[JF_ReviewCounte]");
        busel.add("attribute[JF_EstimatedSignDate]");
        busel.add("attribute[JF_PhaseState]");
        busel.add("current");
        Map info = domainObject.getInfo(context,busel);
        String current  = UIUtil.getValue(info,"current");
        String JF_ReviewCounte  = UIUtil.getValue(info,"attribute[JF_ReviewCounte]");
        String JF_EstimatedSignDate  = UIUtil.getValue(info,"attribute[JF_EstimatedSignDate]");
        String JF_PhaseState  = UIUtil.getValue(info,"attribute[JF_PhaseState]");
        if (!"Create".equals(current)){
            mess = "\u53ea\u80fd\u9009\u62e9create\u72b6\u6001\u7684eso\u7b7e\u53d1\u8bb0\u5f55";
        }
        if (UIUtil.isNullOrEmpty(JF_ReviewCounte) || UIUtil.isNullOrEmpty(JF_EstimatedSignDate) || UIUtil.isNullOrEmpty(JF_PhaseState)){
            if (UIUtil.isNotNullAndNotEmpty(mess)) {
                mess += ",";
            }
            mess += "\u8bf7\u586b\u5199\u5fc5\u586b\u5c5e\u6027";
        }
        String esoTaskId = domainObject.getInfo(context, "to[JFESOTask2ESOReview].from.id");
        if (UIUtil.isNotNullAndNotEmpty(esoTaskId)) {
            domainObject.setId(esoTaskId);
            StringList busSelList = StringList.create("id", "name", "type", "attribute[JF_Function]","attribute[JF_Rows]", "attribute[JF_ESOType]", "attribute[JF_Grade]","attribute[JF_Modules]","attribute[JF_ColorIdentification]");
            JF_LOGGER.info("busSelList:{}", busSelList);
            MapList subTask = domainObject.getRelatedObjects(context,"Subtask","JF_ESOTask,Task",busSelList,new StringList(),false,true,(short)1,"","",0);
            subTask.add(domainObject.getInfo(context, busSelList));
            JF_LOGGER.info("subTask:{}", subTask);
            //类型分组
            Map groupTask = (Map) subTask.stream().collect(Collectors.groupingBy(m -> {
                Map map = (Map) m;
                return UIUtil.getValue(map, "type");
            }));
            List subEsoTaskList = (List) groupTask.get("JF_ESOTask");
            StringList sheetNameList = new StringList();
            if (CollectionUtils.isNotEmpty(subEsoTaskList)) {
                for (int i = 0; i < subEsoTaskList.size(); i++) {
                    Map map = (Map) subEsoTaskList.get(i);
                    String subEsoFunction = UIUtil.getValue(map, "attribute[JF_Function]");
                    String subEsoModules = UIUtil.getValue(map, "attribute[JF_Modules]");
                    String subEsoRows = UIUtil.getValue(map, "attribute[JF_Rows]");
                    String subEsoESOType = UIUtil.getValue(map, "attribute[JF_ESOType]");
                    // 组合名称
                    String sheetBaseName = subEsoModules + "_" + subEsoRows + "_" + subEsoESOType;
                    if (sheetBaseName.length()>31) {
                        sheetNameList.add(sheetBaseName);
                    }
                }
            }
            if (!sheetNameList.isEmpty()) {
                if (UIUtil.isNotNullAndNotEmpty(mess)) {
                    mess += ",";
                }
                String i18NString = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.Notice.ESOReviewError", new String[]{});
                mess += i18NString.replaceAll("sheetName",sheetNameList.join(","));
            }
        }
        sbUrl.append("?objectId=");
        sbUrl.append(splitTableRowIds[0]);
        sbUrl.append(AMP);
        sbUrl.append("ProgramSuiteKey=");
        sbUrl.append("ProgramCentral");
        sbUrl.append(AMP);
        sbUrl.append("SuiteKey=");
        sbUrl.append("Components");
    } catch (MatrixException e) {
        JF_LOGGER.info("JF_ChiefEngineerConfirm.jsp----error::",e);
    }
%>
<html>
<% if (mess.isEmpty()) { %>
<form name="application" action="<%= XSSUtil.encodeForHTML(context, sbUrl.toString()) %>">
    <input type="hidden" name="objectId" value="<%= splitTableRowIds[0] %>" />
</form>
<script>
    document.application.submit();
</script>
<% } else { %>
<script>
    alert("<%= mess %>");
    window.close();
</script>
<% } %>
</html>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>

