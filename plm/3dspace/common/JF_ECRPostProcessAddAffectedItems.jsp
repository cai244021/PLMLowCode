<%--<!DOCTYPE html>--%>
<%--<html>--%>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="java.util.HashMap" %>
<%--登录检查--%>
<%@include file = "emxNavigatorInclude.inc"%>
<script src="./scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="./scripts/emxUICore.js"></script>
<script language="JavaScript" src="./scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="./scripts/emxUISearch.js"></script>
<%!
    //日志
    private static final Logger _logger =  LoggerFactory.getLogger("JF_ECRPostProcessAddAffectedItems");
%>
<%
    String strObjectId = emxGetParameter(request, "objectId");
    String strParentId = emxGetParameter(request, "parentId");
    String strMode = emxGetParameter(request, "mode");
    _logger.info("strObjectId:{}",strObjectId);
    _logger.info("strParentId:{}",strParentId);
    _logger.info("strMode:{}",strMode);
    String emxTableRowId[] = emxGetParameterValues(request, "emxTableRowId");
    StringList selectIdList = new StringList();
    if (null != emxTableRowId &&  emxTableRowId.length > 0){
        for (int i = 0; i < emxTableRowId.length; i++) {
            String selectedId = emxTableRowId[i];
            Map rowMap = ProgramCentralUtil.parseTableRowId(context,selectedId);
            selectIdList.add((String) rowMap.get("objectId"));
        }
    }
    _logger.info("selectIdList:{}",selectIdList);
    if (selectIdList.size() > 0){
        DomainObject ecr = DomainObject.newInstance(context, strObjectId);
        String strJFChangeSource = ecr.getInfo(context,"attribute[JFChangeSource]");
        DomainObject to = DomainObject.newInstance(context);
        for (int i = 0; i < selectIdList.size(); i++) {
            String strToId = selectIdList.get(i);
            to.setId(strToId);
            DomainRelationship rel = DomainRelationship.connect(context, ecr, "JFRelateItem", to);
            rel.setAttributeValue(context,"JFChangeSource",strJFChangeSource);
        }
        // 添加相关受影响父件
        Map packMap = new HashMap<>();
        packMap.put("ecrId",strObjectId);
        packMap.put("partList",selectIdList);
        JPO.invoke(context, "JF_ECRProcess", null, "processAffectedItemAdd", JPO.packArgs(packMap), null);
    }
    _logger.info(" --------------------------------------- add end ---------------------------------------");
%>
<script>
    //刷新table
    const iFrame = findFrame(getTopWindow(),"JFECRAffectedItemsPartCmd")
    iFrame.refreshSBTable();
</script>
<%--MQL通知--%>
<jsp:include  page="../components/emxMQLNotice.jsp" flush="true"/>
</html>