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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_PublicChangeProcessDelete.jsp");
%>

<%
    String strMess = DomainConstants.EMPTY_STRING;
    String partialXML = "";
    String xmlMessage = "<mxRoot>";
    String[] tableRowId = emxGetParameterValues(request, "emxTableRowId");
    //20260812 update by ljr 拥有的文档删除按钮删除成功后重新加载当前Table，确保列表与数据库保持一致。
    boolean refreshTable = "true".equalsIgnoreCase(request.getParameter("refreshTable"));
    LOGGER.info("JF_PublicChangeProcessDelete.jsp======tableRowId:{}", Arrays.asList(tableRowId));
    String[] splitTableRowIds = ComponentsUIUtil.getSplitTableRowIds(tableRowId);
    LOGGER.info("JF_PublicChangeProcessDelete.jsp======splitTableRowIds:{}",Arrays.asList(splitTableRowIds));
    StringList strings = new StringList();
    String user = context.getUser();
    StringList busList = StringList.create("type", "owner", "current");
    try {
        ContextUtil.pushContext(context);
        for (String s : splitTableRowIds) {
            DomainObject domainObject = DomainObject.newInstance(context, s);
            Map busMap = domainObject.getInfo(context,busList);
            String owner = UIUtil.getValue(busMap, "owner");
            String type = UIUtil.getValue(busMap, "type");
            String current = UIUtil.getValue(busMap, "current");
            if ("JFDA".equals(type)){
                if (!StringUtils.equals(user,owner) || !"In_Work".equals(current)){
                    strMess="只能删除草稿状态并且所有者是自己的DA单据";
                    break;
                }
            }else if ("JFECR".equals(type) || "JFNewECR".equals(type)|| "JFFormalECR".equals(type)){
                if (!StringUtils.equals(user,owner) || !"Create".equals(current)){
                    strMess="只能删除创建状态并且所有者是自己的ECR单据";
                    break;
                }
            }else if ("JF_DRW".equals(type)){
                if (!StringUtils.equals(user,owner) || !("Prepare".equals(current) || "In Work".equals(current))){
                    strMess="只能删除草稿或工作中状态并且所有者是自己的DRW单据";
                    break;
                }
            }else if ("JFDR".equals(type)){
                if (!StringUtils.equals(user,owner) || !"In_Work".equals(current)){
                    strMess="只能删除工作中状态并且所有者是自己的DR单据";
                    break;
                }
            }else if ("JF_PCR".equals(type)){
                if (!StringUtils.equals(user,owner) || !"Draft".equals(current)){
                    strMess="只能删除草稿状态并且所有者是自己的PCR";
                    break;
                }
            }else if ("JFSPartsApplication".equals(type)) {
                if (!StringUtils.equals(user,owner) || !"Draft".equals(current)){
                    strMess="只能删除草稿状态并且所有者是自己的标准件发布申请单";
                    break;
                }
            }else if (type.equalsIgnoreCase(DomainConstants.TYPE_DOCUMENT)) {
                //20260811 update by ljr 拥有的文档列表仅允许删除本人所有且处于工作中的文档。
                if (!StringUtils.equals(user, owner) || !"IN_WORK".equals(current)) {
                    strMess="只能删除工作中状态并且所有者是自己的文档";
                    break;
                }
            }else if ("JF_PCRExecuteTask".equals(type)){
                //如果评估任务为Review，执行任务不可以删除 评估任务和执行任务的部门匹配 owner匹配
                String pcrId =domainObject.getInfo(context,"to[JF_PCR2ExecuteTask].from.id");
               String JF_PCRFunction =  domainObject.getAttributeValue(context,"JF_PCRFunction");
                DomainObject obj = DomainObject.newInstance(context,pcrId);
                StringList selList = new StringList();
                //当部门是制造工艺时，评估结论为OK，评估任务提交了，属于设备的执行任务仍允许被删除，应该不允许。
                // 当部门是采购时，评估结论为OK， 没有创建执行计划，直接提交评估任务，没有报错提示（但是没有成功提交），
                // 且后续评估任务提交了，属于供应链的执行任务仍允许被删除，应该不允许。（部门是供应商质量也存在此问题）
//                 else if (copyTitle.contains("\u5DE5\u827A") && (itemTitle.equals("\u8BBE\u5907") || itemTitle.equals("\u5DE5\u827A"))) {
                String where="current==Review && owner=='" + user + "' && ";
                String titleWhere= "";
                if("\u8BBE\u5907".equals(JF_PCRFunction)){
                    JF_PCRFunction="\u5DE5\u827A";
                    titleWhere =" attribute[Title] ~~ '*" + JF_PCRFunction + "*'";
                }else if("\u4F9B\u5E94\u94FE".equals(JF_PCRFunction)){
//                    评估任务新增SQD角色，对应供应商质量部门，对应任务是供应链；
//                    评估任务新增采购代表角色，对应采购部门，对应任务是供应链；
                    titleWhere=" (attribute[Title] ~~ '*\u4F9B\u5E94\u5546\u8D28\u91CF*' || attribute[Title] ~~ '*\u91C7\u8D2D*')";
                }else{
                    titleWhere =" attribute[Title] ~~ '*" + JF_PCRFunction + "*'";
                }
                where = where +titleWhere;
                LOGGER.info("where:{}",where);
                selList.add("attribute[Title]");
                  MapList list = obj.getRelatedObjects(context,//评估任务
                    "JF_PCR2Task", //pattern to match relationships
                    "Task", //pattern to match types
                    selList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    new StringList(), //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    false, //get To relationships
                    true, //get From relationships
                    (short) 1, //the number of levels to expand, 0 equals expand all.
                      where, //where clause to apply to objects, can be empty ""
                    null, //where clause to apply to relationship, can be empty ""
                    (short) 0); //limit
                LOGGER.info("list:{} where {}",list,where);
                if (!StringUtils.equals(user,owner) || !"Assign".equals(current) || list.size()>0){
                    strMess="只能删除Todo状态、所有者是自己的并且对应的评估任务还是待提交状态的执行任务";
                    break;
                }
            } else if ("JFExpertsGroup".equalsIgnoreCase(type)) {
                String hasConnPs = domainObject.getInfo(context, "to[JFProject2ExpertsGroup]");
                if ("TRUE".equalsIgnoreCase(hasConnPs)) {
                    strMess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ExpertsGroup.DeleteMess", new String[]{});
                    break;
                }
            } else if ("JFBatchDocumentReview".equalsIgnoreCase(type)) {
                if (!StringUtils.equals(user,owner) || !"InWork".equals(current)){
                    strMess="只能删除工作中状态并且所有者是自己的文档批量审批单据";
                    break;
                }
            }
        }
        if (UIUtil.isNullOrEmpty(strMess)) {
            for (String s : tableRowId) {
                Map mParsedRowId = ProgramCentralUtil.parseTableRowId(context, s);
                String sTempObjectId = null;
                String sTempParentId = null;
                String sTempRowId = null;
                String rowId = null;
                if (null != mParsedRowId) {
                    sTempObjectId = (String) mParsedRowId.get("objectId");
                    sTempParentId = (String) mParsedRowId.get("parentOId");
                    sTempRowId = (String) mParsedRowId.get("rowId");
                    rowId = sTempRowId;
                }
                if (ProgramCentralUtil.isNotNullString(rowId)) {
                    partialXML += "<item id=\"" + rowId + "\" />";
                }
                LOGGER.info("JF_PublicChangeProcessDelete.jsp======sTempObjectId:{}", sTempObjectId);
                LOGGER.info("JF_PublicChangeProcessDelete.jsp======rowId:{}", rowId);
                DomainObject domainObject = DomainObject.newInstance(context, sTempObjectId);
                Map busMap = domainObject.getInfo(context,busList);
                String type =UIUtil.getValue(busMap,"type");
                if ("JFDA".equals(type) || "JFECR".equals(type)  || "JFNewECR".equals(type) || "JFFormalECR".equals(type)) {
                    strings.add(sTempObjectId);
                } else if ("JFDR".equals(type)){
                    //去除interface
                    JPO.invoke(context, "JF_DR", null, "drRefuseRemoveChangeControl", new String[]{sTempObjectId}, void.class);
                    strings.add(sTempObjectId);
                }else if ("JF_DRW".equals(type)) {
                    if ("In Work".equals(UIUtil.getValue(busMap,"current"))) {
                        domainObject.demote(context);
                    }
                    strings.add(sTempObjectId);
                }else{
                    strings.add(sTempObjectId);
                }
            }
        }

            if (strings.size() > 0){
                DomainObject.deleteObjects(context,strings.toStringArray());
                LOGGER.info("JF_PublicChangeProcessDelete.jsp======删除成功");
            }

        String message = "";
        xmlMessage += "<action refresh=\"true\" fromRMB=\"\"><![CDATA[remove]]></action>";
        xmlMessage += partialXML;
        xmlMessage += "<message><![CDATA[" + message + "]]></message>";
        xmlMessage += "</mxRoot>";
    }catch (Exception e) {
        strMess = e.getMessage();
        LOGGER.info("JF_PublicChangeProcessDelete.jsp======error",e);
    }finally {
        try {
            ContextUtil.popContext(context);
        } catch (FrameworkException e) {
            LOGGER.info("JF_PublicChangeProcessDelete.jsp======error",e);
        }
    }

%>
<html>
<script>
    var strMess = "<%=strMess%>";
    if (strMess !== "" && strMess != null){
        alert("<%=strMess%>");
    }else {
        <%--20260812 update by ljr 仅拥有的文档删除按钮强制刷新当前Table，其他复用入口保留原有局部刷新方式。--%>
        if (<%=refreshTable%>) {
            window.parent.location.reload();
        } else {
            window.parent.removedeletedRows('<%=xmlMessage%>');
            window.parent.emxEditableTable.refreshStructureWithOutSort();
            window.parent.getTopWindow().RefreshHeader();
        }
    }
</script>
</html>
