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
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="java.util.HashMap" %>
<%--登录检查--%>
<%@include file = "emxNavigatorInclude.inc"%>
<script src="./scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="./scripts/emxUICore.js"></script>
<script language="JavaScript" src="./scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="./scripts/emxUISearch.js"></script>
<script language="JavaScript" src="../webapps\ENOAEFCore\webroot\common\scripts/bpsTagNavSBInit.js"></script>

<%!
    //日志
    private static final Logger _logger =  LoggerFactory.getLogger("JF_ECRAffectedItemsActionService");
%>
<%
    //车型 oid
    String strObjectId = emxGetParameter(request, "objectId");
    String strParentId = emxGetParameter(request, "parentId");
    String strMode = emxGetParameter(request, "mode");
    _logger.info("strObjectId:{}",strObjectId);
    _logger.info("strParentId:{}",strParentId);
    _logger.info("strMode:{}",strMode);
    if ("add".equals(strMode)){
        String strUrl = JPO.invoke(context, "JF_ECRService", null, "getSearchUrl", new String[]{strObjectId,strParentId}, String.class);
%>
<script>
    var strUrl = "<%=strUrl%>";
    showWizard(strUrl);
</script>
<%
    }else if ("remove".equals(strMode)){
        String emxTableRowIdActual[] = emxGetParameterValues(request, "emxTableRowIdActual");
        StringList selectRIdList = new StringList();
        StringList selectIdList = new StringList();
        if (null != emxTableRowIdActual &&  emxTableRowIdActual.length > 0){
            for (int i = 0; i < emxTableRowIdActual.length; i++) {
                String selectedId = emxTableRowIdActual[i];
                Map rowMap = ProgramCentralUtil.parseTableRowId(context,selectedId);
                selectRIdList.add((String) rowMap.get("relId"));
                selectIdList.add((String) rowMap.get("objectId"));
            }
            if (selectRIdList.size() > 0){
                try{
                    ContextUtil.pushContext(context);
                    ContextUtil.startTransaction(context,true);
                    DomainRelationship.disconnect(context,selectRIdList.toStringArray());
                    ContextUtil.commitTransaction(context);
                    Gson gson = new Gson();
                    String strRowIds = gson.toJson(emxTableRowIdActual);
                    _logger.info("strRowIds:{}",strRowIds);
%>
<script>
    const selectRowArr = <%=strRowIds%>;
    const iFrame = findFrame(getTopWindow(),"JFECRAffectedItemsPartCmd")
    iFrame.emxEditableTable.removeRowsSelected(selectRowArr);
    iFrame.rebuildView();
</script>
<%
                Map packMap = new HashMap<>();
                packMap.put("ecrId",strObjectId);
                packMap.put("partList",selectIdList);
                // 移除相关受影响父件
                JPO.invoke(context, "JF_ECRProcess", null, "processAffectedItemRemove", JPO.packArgs(packMap), null);
            }catch (Exception e){
                    ContextUtil.abortTransaction(context);
                    _logger.info(e.getMessage());
                }finally {
                    ContextUtil.popContext(context);
                }
            }
        }
    }else if ("create_parent".equals(strMode)){
    try {
        String strProcessFlag = (String) session.getAttribute("create_parent");
        if (!"1".equals(strProcessFlag)){
            session.setAttribute("create_parent","1");

        }
    }finally {
        //移除seesion 属性
        session.removeAttribute("create_parent");
    }
%>
<%
    }

%>
<%--MQL通知--%>
<jsp:include  page="../components/emxMQLNotice.jsp" flush="true"/>
</html>