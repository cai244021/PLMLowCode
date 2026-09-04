<%--<!DOCTYPE html>--%>
<%--<html>--%>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUtil" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="com.matrixone.apps.domain.util.*" %>
<%--登录检查--%>
<%@include file = "emxNavigatorInclude.inc"%>
<script src="./scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="./scripts/emxUICore.js"></script>
<script language="JavaScript" src="./scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="./scripts/emxUISearch.js"></script>
<script language="JavaScript" src="../webapps\ENOAEFCore\webroot\common\scripts/bpsTagNavSBInit.js"></script>

<%!
    //日志
    private static final Logger _logger =  LoggerFactory.getLogger("JF_NewECRAffectedItemsActionService");
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
        String strUrl = JPO.invoke(context, "JF_NewECRProcess", null, "getSearchUrl", new String[]{strObjectId,strParentId}, String.class);
%>
<script>
    var strUrl = "<%=strUrl%>";
    showWizard(strUrl);
</script>
<%
    }else if ("remove".equals(strMode)){
        _logger.info("strMode:{}",strMode);
        String emxTableRowIdActual[] = emxGetParameterValues(request, "emxTableRowIdActual");
        MapList mapList = new MapList();
        StringList notNumList = new StringList();
        DomainObject domainObject = DomainObject.newInstance(context);
        for (int i = 0; i < emxTableRowIdActual.length; i++) {
            String selectedId = emxTableRowIdActual[i];
            Map rowMap = ProgramCentralUtil.parseTableRowId(context, selectedId);
            mapList.add(rowMap);
            String oId = (String) rowMap.get("objectId");
            String relId = (String) rowMap.get("relId");
            String relName = MqlUtil.mqlCommand(context, false, "print connection '" + relId + "' select name dump",true);
            _logger.info("relName:{}",relName);
            if (!"JFECRRelateRoot".equalsIgnoreCase(relName)) {
                //如果不是一级结构  不允许删除
                domainObject.setId(oId);
                notNumList.add(domainObject.getAttributeValue(context, "EnterpriseExtension.V_PartNumber"));
            }
        }
        if (!notNumList.isEmpty()) {
            //有问题数据提示
            String msg = notNumList.join(",") + ComponentsUtil.i18nStringNow("emxComponents.NewECR.RemoveFiled", context.getLocale().getLanguage());
%>
<script>
    alert("<%=msg%>");
</script>
<%
        } else {
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("selectId", mapList);
            paramsMap.put("strObjectId", strObjectId);
            paramsMap.put("strParentId", strParentId);
            String result = JPO.invoke(context, "JF_NewECRProcess", null, "removeSelectPartAffectedItems", JPO.packArgs(paramsMap), String.class);
            try{
                Gson gson = new Gson();
                String strRowIds = gson.toJson(emxTableRowIdActual);
                _logger.info("strRowIds:{}",strRowIds);
                String removeMess = ComponentsUtil.i18nStringNow("emxComponents.Remove.Successful", context.getLocale().getLanguage());

%>
<script>
    alert("<%=removeMess%>");
    const selectRowArr = <%=strRowIds%>;
    const iFrame = findFrame(getTopWindow(),"JFECRNewAffectedItemsPartCmd")
    iFrame.refreshSBTable();
    // iFrame.emxEditableTable.removeRowsSelected(selectRowArr);
    // iFrame.rebuildView();
</script>
<%
            }catch (Exception e){
                _logger.info(e.getMessage());
            }finally {
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