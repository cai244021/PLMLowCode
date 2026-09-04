<%--<!DOCTYPE html>--%>
<%--<html>--%>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUtil" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<%--登录检查--%>
<%@include file = "emxNavigatorInclude.inc"%>
<script src="./scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="./scripts/emxUICore.js"></script>
<script language="JavaScript" src="./scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="./scripts/emxUISearch.js"></script>
<%!
    //日志
    private static final Logger _logger =  LoggerFactory.getLogger("JF_NewECRPostProcessAddAffectedItems");
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
    String mess = DomainConstants.EMPTY_STRING;
    if (selectIdList.size() > 0){
        //校验： 选中添加的根节点，查询ECR已经关联的Root节点以及Root节点关联的子级、孙子级状态为冻结的(JFECRRelateRoot,JFECRRoot2Item)这两条关系，
        // 如果已经存在了就报错，提示已经在哪个根节点中添加了，不需要重复添加
        //需要考虑如果同时添加了，这种情况应该也是需要提示---提示 哪个零件，已经在哪个你选择的哪个根节点里面
        Map pack1Map = new HashMap<>();
        pack1Map.put("ecrId",strObjectId);
        pack1Map.put("selectList",selectIdList);
        String result = (String) JPO.invoke(context, "JF_NewECRProcess", null, "validateECRAddExist", JPO.packArgs(pack1Map), String.class);
        _logger.info("result:{}", result);
        if ("success".equalsIgnoreCase(result)) {
            mess = ComponentsUtil.i18nStringNow("emxComponents.NewECR.AddAffectedItemSuccess", context.getLocale().getLanguage());
            //搭建结构 变更对象
            Map pack2Map = new HashMap<>();
            pack2Map.put("objectId", strObjectId);
            pack2Map.put("partList", selectIdList);
            JPO.invoke(context, "JF_NewECRService", null, "AddAffectedItemsAndCompareStructures", JPO.packArgs(pack2Map), null);
            // 添加相关受影响父件  去掉受影响父件   update by ljr 20260317
//            Map packMap = new HashMap<>();
//            packMap.put("ecrId",strObjectId);
//            packMap.put("partList",selectIdList);
//            JPO.invoke(context, "JF_NewECRProcess", null, "processAffectedItemAdd", JPO.packArgs(packMap), null);
        } else {
            mess = result;
        }
    }
    _logger.info(" --------------------------------------- add end ---------------------------------------");
%>
<script>
    const mess = "<%=mess%>";

        //刷新table
        // const iFrame = findFrame(getTopWindow(),"JFECRAffectedItemsPartCmd")
        // const iFrame = findFrame(getTopWindow(),"JFECRNewAffectedItemsPartCmd");
        // iFrame.refreshSBTable();
        alert(mess);

    getTopWindow().closeWindow();
    window.top.getWindowOpener().top.refreshTablePage();
</script>
<%--MQL通知--%>
<jsp:include  page="../components/emxMQLNotice.jsp" flush="true"/>
</html>