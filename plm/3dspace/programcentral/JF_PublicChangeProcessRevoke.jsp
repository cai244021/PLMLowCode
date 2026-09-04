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
<%@ page import="org.apache.commons.collections4.CollectionUtils" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_PublicChangeProcessRevoke.jsp");
%>

<%
    String strMess = DomainConstants.EMPTY_STRING;
    String partialXML = "";
    String xmlMessage = "<mxRoot>";
    String[] tableRowId = emxGetParameterValues(request, "emxTableRowId");
    //20260811 update by ljr 文档详情页没有表格选中行，撤回时使用当前详情对象ID。
    String detailObjectId = emxGetParameter(request, "objectId");
    if ((tableRowId == null || tableRowId.length == 0) && UIUtil.isNotNullAndNotEmpty(detailObjectId)) {
        tableRowId = new String[]{detailObjectId};
    }
    if (tableRowId == null) {
        tableRowId = new String[0];
    }
    LOGGER.info("JF_PublicChangeProcessRevoke.jsp======tableRowId:{}", Arrays.asList(tableRowId));
    String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
    LOGGER.info("JF_PublicChangeProcessRevoke.jsp======splitTableRowIds:{}",Arrays.asList(splitTableRowIds));
    StringList strings = new StringList();
    StringList objList = new StringList();
    String user = context.getUser();
    StringList busList = StringList.create("type", "owner", "current");
    try {
        ContextUtil.pushContext(context);
        if (splitTableRowIds.length == 0) {
            strMess="请选择需要撤回的对象";
        }
        for (String s : splitTableRowIds) {
            DomainObject domainObject = DomainObject.newInstance(context, s);
            Map busMap = domainObject.getInfo(context,busList);
            String owner = UIUtil.getValue(busMap, "owner");
            String type = UIUtil.getValue(busMap, "type");
            String current = UIUtil.getValue(busMap, "current");
            if (!StringUtils.equals(user,owner)){
                strMess="只能撤回所有者是自己的流程";
                break;
            }
            //20260811 update by ljr 批量审批单仅允许在审核中状态撤回，避免删除非审批中对象的流程。
            if ("JFBatchDocumentReview".equalsIgnoreCase(type) && !"Review".equalsIgnoreCase(current)) {
                strMess="只能撤回审核中的批量审批单";
                break;
            }
            //20260811 update by ljr 单文档仅允许所有者撤回冻结状态的审核流程。
            if (type.equalsIgnoreCase(DomainConstants.TYPE_DOCUMENT)
                    && !"FROZEN".equalsIgnoreCase(current)) {
                strMess="只能撤回审核中的文档";
                break;
            }
            StringList inboxTaskCurrentList = domainObject.getInfoList(context,"from[Object Route].to.to[Route Task].from.current");
            boolean isComplete = inboxTaskCurrentList.stream().anyMatch("Complete"::equals);
            if (isComplete){
                strMess="流程中有审批通过的任务，不允许撤回";
                break;
            }
            StringList routeList = domainObject.getInfoList(context,"from[Object Route].to.id");
            if (CollectionUtils.isEmpty(routeList)){
                strMess="没有审批流程，无需撤回";
                break;
            }

        }
        if (UIUtil.isNullOrEmpty(strMess)) {
            for (String s : splitTableRowIds) {
                DomainObject domainObject = DomainObject.newInstance(context, s);
                StringList routeList = domainObject.getInfoList(context,"from[Object Route].to.id");
                strings.addAll(routeList);
                objList.add(s);
            }
        }

            if (strings.size() > 0){
                DomainObject.deleteObjects(context,strings.toStringArray());
                LOGGER.info("JF_PublicChangeProcessRevoke.jsp======删除route成功");
                for (String s : objList) {
                    DomainObject domainObject = DomainObject.newInstance(context, s);
                    String type = domainObject.getInfo(context,"type");
                    if ("JFDA".equals(type)) {
                       MqlUtil.mqlCommand(context,false,"mod bus "+s+" current 'In_Work'",true);
                    }else if ("JFECR".equals(type) || "JFNewECR".equals(type)|| "JFFormalECR".equals(type)){
                        MqlUtil.mqlCommand(context,false,"mod bus "+s+" current 'Create'",true);
                    }else if ("JF_DRW".equals(type)){
                        MqlUtil.mqlCommand(context,false,"mod bus "+s+" current 'In Work'",true);
                    }else if ("JFDR".equals(type)){
                        //去除interface
                        JPO.invoke(context, "JF_DR", null, "drRefuseRemoveChangeControl", new String[]{s}, void.class);
                        MqlUtil.mqlCommand(context,false,"mod bus "+s+" current 'In_Work'",true);
                    }else if ("JFESOReview".equalsIgnoreCase(type)) {
                        //撤回签发表总工审批后，清除本次选择的总工人员并退回创建状态。
                        StringList chiefEngineerRelIds = domainObject.getInfoList(
                                context,
                                "from[JFESOReview2Person].id");
                        for (Object relIdObj : chiefEngineerRelIds) {
                            String relId = String.valueOf(relIdObj);
                            if (UIUtil.isNotNullAndNotEmpty(relId)) {
                                DomainRelationship.disconnect(context, relId);
                            }
                        }
                        MqlUtil.mqlCommand(context,false,"mod bus "+s+" current 'Create'",true);
                    }else if ("JFSPartsApplication".equalsIgnoreCase(type)) {
                        //去除interface
                        JPO.invoke(context, "JF_FasteningPiece", null, "removeChangeInterface", new String[]{s}, void.class);
                        MqlUtil.mqlCommand(context,false,"mod bus "+s+" current 'Draft'",true);
                    }else if ("JFBatchDocumentReview".equalsIgnoreCase(type)) {
                        //撤回签发表总工审批后，清除本次选择的总工人员并退回创建状态。
                        StringList docIds = domainObject.getInfoList(
                                context,
                                "from[Reference Document].to.id");
                        for (String docId : docIds) {
                            MqlUtil.mqlCommand(context, false,"mod bus "+ docId +" current 'IN_WORK'", true);
                        }
                        //20260827 update by caipan 批量审批撤回后统一释放全部关联文档的Change Control。
                        JPO.invoke(context, "JF_DocumentTrigger", null,
                                "removeDocumentChangeControl", docIds.toStringArray(), void.class);
                        MqlUtil.mqlCommand(context,false,"mod bus "+s+" current 'InWork'",true);
                    }else if (type.equalsIgnoreCase(DomainConstants.TYPE_DOCUMENT)) {
                        //20260811 update by ljr 单文档撤回公共流程后恢复为工作中状态。
                        MqlUtil.mqlCommand(context, "mod bus $1 current $2", s, "IN_WORK");
                        //20260827 update by caipan 单文档审批撤回后释放Change Control。
                        JPO.invoke(context, "JF_DocumentTrigger", null,
                                "removeDocumentChangeControl", new String[]{s}, void.class);
                    }
                }
                LOGGER.info("JF_PublicChangeProcessRevoke.jsp======撤回成功");
            }

    }catch (Exception e) {
        strMess = e.getMessage();
        LOGGER.info("JF_PublicChangeProcessRevoke.jsp======error",e);
    }finally {
        try {
            ContextUtil.popContext(context);
        } catch (FrameworkException e) {
            LOGGER.info("JF_PublicChangeProcessRevoke.jsp======error",e);
        }
    }

%>
<html>
<script>
    var strMess = "<%=strMess%>";
    if (strMess !== "" && strMess != null){
        alert("<%=strMess%>");
    }else {
        // var refreshURL = window.parent.location.href;
        // window.parent.location.href = refreshURL;
        alert("流程撤回成功");
        if (window.parent.emxEditableTable
                && typeof window.parent.emxEditableTable.refreshSelectedRows === "function") {
            window.parent.emxEditableTable.refreshSelectedRows();
        } else {
            //20260812 update by ljr Form撤回只刷新当前对象详情Frame，避免重载Navigator后返回主页面。
            var detailsFrame = findFrame(getTopWindow(), "detailsDisplay");
            if (detailsFrame && detailsFrame !== window) {
                detailsFrame.location.reload();
            }
        }
        if (window.parent.getTopWindow && window.parent.getTopWindow().RefreshHeader) {
            window.parent.getTopWindow().RefreshHeader();
        }
    }
</script>
</html>
