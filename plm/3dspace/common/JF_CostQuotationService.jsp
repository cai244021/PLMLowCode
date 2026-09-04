<%--<!DOCTYPE html>--%>
<%--<html>--%>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.poi.ss.usermodel.Workbook" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="matrix.util.MatrixException" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.SELECT_ID" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.SELECT_NAME" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.*" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="com.matrixone.apps.domain.util.PersonUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="java.util.*" %>
<%--登录检查--%>
<%@include file = "emxNavigatorInclude.inc"%>
<script src="./scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="./scripts/emxUICore.js"></script>
<script language="JavaScript" src="./scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="./scripts/emxUISearch.js"></script>
<script language="JavaScript" src="../webapps\ENOAEFCore\webroot\common\scripts/bpsTagNavSBInit.js"></script>
<%!
    //日志
    private static final Logger _logger =  LoggerFactory.getLogger("JF_CostQuotationService");
%>
<%
    try {
        String strMode = emxGetParameter(request, "mode");
        String strObjectId = emxGetParameter(request, "objectId");
        _logger.info("strMode:{}",strMode);
        HashSet<String> idSet = new HashSet<>();
        HashSet<String> relSet = new HashSet<>();
        StringBuffer sbMess  = new StringBuffer() ;
        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.CostQuotationSelectedPartTypeInconsistent");
        String strMess1 = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.CostQuotationAlreadyCreated");
        Boolean isContext = Boolean.TRUE;
        boolean isAlert = false ;
        if ("bomCreate".equals(strMode)){
            String strPartType = "";
            String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
            //update bu ljr  20250305  兼容EBOM页面的成本分析
            if (tableRowIdList == null || tableRowIdList.length == 0) {
                tableRowIdList = new String[]{"xxx|"+strObjectId};
                isContext = Boolean.FALSE;
            }
            StringList typeErrorList = new StringList();
            StringList alreadyErrorList = new StringList();
            //end
            for (String tableRowId : tableRowIdList) {
                Map rowMap = ProgramCentralUtil.parseTableRowId(context,tableRowId);
                String strPartId = (String) rowMap.get("objectId");
                DomainObject part = DomainObject.newInstance(context,strPartId);
                Map partInfo = part.getInfo(context,StringList.create("attribute[JF_VPMReference.JF_PartType]",SELECT_NAME, SELECT_REVISION,"attribute[EnterpriseExtension.V_PartNumber]","from[JFVPMReference2CostAnalysis]", "from[JFVPMReference2CostAnalysis].to.owner"));
                String strTemPartType = (String) partInfo.get("attribute[JF_VPMReference.JF_PartType]");
                _logger.info("strPartType:{}",strPartType);
                String strPartName = (String) partInfo.get("attribute[EnterpriseExtension.V_PartNumber]");
                String strHasConnCost = (String) partInfo.get("from[JFVPMReference2CostAnalysis]");
                String strCostOwner = (String) partInfo.get("from[JFVPMReference2CostAnalysis].to.owner");
                if (UIUtil.isNullOrEmpty(strPartType)){
                    strPartType = strTemPartType;
                }
                if (!strPartType.equals(strTemPartType)){
                    isAlert = true ;
                    strPartName = UIUtil.isNullOrEmpty(strPartName) ? (String) partInfo.get(SELECT_NAME) : strPartName;
                    typeErrorList.add(strPartName + "_" + UIUtil.getValue(partInfo, SELECT_REVISION));
                    continue;
                }else if ("TRUE".equalsIgnoreCase(strHasConnCost) && isContext){
                    //update bu ljr  20250305  判断是否是EBOM菜单的成本分析 还是EBOM Table中的新建成本分析
                    isAlert = true ;
                    strPartName = UIUtil.isNullOrEmpty(strPartName) ? (String) partInfo.get(SELECT_NAME) : strPartName;
                    alreadyErrorList.add(strPartName + "_" + UIUtil.getValue(partInfo, SELECT_REVISION) + "(" + PersonUtil.getFullName(context, strCostOwner) +")");
                    continue;
                }else {
                    idSet.add(strPartId);
                }
            }
            if (!typeErrorList.isEmpty()) {
                sbMess.append(typeErrorList.join(",") + strMess);
            }
            if (!alreadyErrorList.isEmpty()) {
                strMess1 = strMess1.replace("$1",alreadyErrorList.join(","));
                sbMess.append(strMess1);
            }

            if (isAlert){
%>
<script>

    alert("<%=sbMess.toString()%>")
</script>
<%
            }else {
                String strSelectIds = StringList.create(idSet).join(",");
                ///emxTable.jsp  hideRootSelection=true //隐藏根节点
                String strUrl = "./emxIndentedTable.jsp?table=AEFGeneralSearchResults" +
                        "&program=JF_CostAnalysis:getCostAnalysisLibList&direction=from&relationship=relationship_Subclass&expandByDefault=true&expandLevel=2" +
//                        "&type=*&selection=multiple&applyURL=javascript:checkSubmitLibTable&insertNewRow=false&StringResourceFileId=emxComponentsStringResource&submitURL=./JF_CostQuotationService.jsp";
                        "&type=*&selection=multiple&insertNewRow=false&callbackFunction=checkSubmitLibTable";
                StringBuffer sb = new StringBuffer(strUrl);
                sb.append("&selectIds=");
                sb.append(strSelectIds);
                sb.append("&");
                sb.append("mode=");
                sb.append("bomCreateProcess");
                sb.append("&");
                //add by ljr  ebom界面不需要刷新  菜单成本分析需要刷新
                if (isContext) {
                    sb.append("refresh=false");
                } else {
                    sb.append("refresh=true");
                }

%>
<script>
    getTopWindow().showWizard('<%=sb.toString()%>')
</script>
<%
            }
        }else if ("bomCreateProcess".equals(strMode)){

%>
<script>
alert("<%=strMess%>")
</script>
<%
        }else if ("edit".equals(strMode)){
            String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
            Map<String,StringList> idMap = new HashMap<>();
            for (String tableRowId : tableRowIdList) {
                Map rowMap = ProgramCentralUtil.parseTableRowId(context,tableRowId);
                String strPartId = (String) rowMap.get("objectId");
                String strPartRid = (String) rowMap.get("relId");
                DomainObject part = DomainObject.newInstance(context,strPartId);
                Map partInfo = part.getInfo(context,StringList.create("attribute[JF_VPMReference.JF_PartType]",SELECT_NAME,"attribute[EnterpriseExtension.V_PartNumber]","from[JFVPMReference2CostAnalysis]"));
                String strPartName = (String) partInfo.get("attribute[EnterpriseExtension.V_PartNumber]");
                String strHasConnCost = (String) partInfo.get("from[JFVPMReference2CostAnalysis]");
                //只有关联成本才可编辑
               if ("FALSE".equalsIgnoreCase(strHasConnCost)){
                    isAlert = true ;
                    strPartName = UIUtil.isNullOrEmpty(strPartName) ? (String) partInfo.get(SELECT_NAME) : strPartName;
                   String strEditMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.SelectPartNotCreateCostQuotation");
                    strMess1 = strEditMess.replace("$1",strPartName);
                    sbMess.append(strMess1);
                    break;
                }else {
                   if (idMap.containsKey(strPartId)){
                       StringList relIdList = idMap.get(strPartId);
                       if (!relIdList.contains(strPartRid)) {
                           relIdList.add(strPartRid);
                       }
                   }else {
                       StringList relIdList = new StringList();
                       relIdList.add(strPartRid);
                       idMap.put(strPartId,relIdList);
                   }
                }
            }
            if (isAlert){
%>
<script>

    alert("<%=sbMess.toString()%>")
</script>
<%
            }else {
//                String strSelectIds = StringList.create(idSet).join(",");
//                String strSelectRids = StringList.create(relSet).join(",");
                Gson gson = new Gson();
                String strIdMapJson = gson.toJson(idMap);
                ///emxTable.jsp  hideRootSelection=true //隐藏根节点
                String strUrl = "./emxIndentedTable.jsp?table=JFBOMCostAnalysisEditTable" +
                        "&program=JF_CostAnalysis:getCostAnalysisEditList" +
                       "&insertNewRow=false&freezePane=Name,PartNameCN,revision";
                StringBuffer sb = new StringBuffer(strUrl);
                sb.append("&selectIds=");
                String strEncodeUrl = URLEncoder.encode(strIdMapJson, StandardCharsets.UTF_8);
                sb.append(strEncodeUrl);
//                sb.append("&");
//                sb.append("&selectRids=");
//                sb.append(strSelectRids);
                sb.append("&");
                sb.append("editLink=true");
                sb.append("&");
                sb.append("callbackFunction=costQuotationSaveAndFlush");
                sb.append("&");
                sb.append("submitLabel=emxFramework.Command.JFCostQuotationSubmit");
                sb.append("&");
                sb.append("cancelButton=true");
                sb.append("&");
                sb.append("cancelLabel=emxFramework.Command.JFCostQuotationCancel");
                sb.append("&");
                sb.append("postProcessJPO=JF_CostAnalysis:updateCostAnalysisAttrAndFlush");
%>
<script>
    getTopWindow().showWizard('<%=sb.toString()%>');
</script>
<%
            }
        } else if ("transferOwner".equals(strMode)){
            String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
            Map<String,StringList> idMap = new HashMap<>();
            DomainObject part = DomainObject.newInstance(context);
            DomainObject cost = DomainObject.newInstance(context);
            String costAnalysisId = "";
            StringList notHasCostList = new StringList();
            StringList ownerNotCostList = new StringList();
            StringList partList = new StringList();
            String loginUser = context.getUser();
            String owner = context.getUser();
            Vector assignments = PersonUtil.getAssignments(context, loginUser);
            Boolean adminFlag = Boolean.FALSE;
            if (assignments.contains("JfITAdmin")) {
                adminFlag = Boolean.TRUE;
            }
            System.out.println("adminFlag:" + adminFlag);
            for (String tableRowId : tableRowIdList) {
                Map rowMap = ProgramCentralUtil.parseTableRowId(context, tableRowId);
                System.out.println("rowMap:" + rowMap);
                String strPartId = (String) rowMap.get("objectId");
                part.setId(strPartId);
                System.out.println("1111111111111111111111111111");
                costAnalysisId = part.getInfo(context, "from[JFVPMReference2CostAnalysis].to.id");
                String mess = part.getAttributeValue(context, "EnterpriseExtension.V_PartNumber") + "_" + part.getInfo(context, DomainConstants.SELECT_REVISION);
                System.out.println("costAnalysis:" + costAnalysisId);
                if (UIUtil.isNullOrEmpty(costAnalysisId)) {
                    notHasCostList.add(mess);
                    isAlert = Boolean.TRUE;
                    continue;
                }
                //判断owner是否匹配  或者是否为JfITAdmin
                cost.setId(costAnalysisId);
                owner = cost.getInfo(context, DomainConstants.SELECT_OWNER);
                if (owner.equalsIgnoreCase(loginUser) || adminFlag) {
                    partList.add(strPartId);
                } else {
                    isAlert = Boolean.TRUE;
                    ownerNotCostList.add(mess);
                }
            }
            System.out.println("partList:" + partList);
            System.out.println("isAlert:" + isAlert);
            if (isAlert){
                if (!notHasCostList.isEmpty()) {
                    String strEditMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.SelectPartNotCreateCostQuotation");
                    strMess1 = strEditMess.replace("$1",notHasCostList.join(","));
                    sbMess.append(strMess1);
                }
                if (!ownerNotCostList.isEmpty()) {
                    String strEditMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.SelectPartOwnerNotCost");
                    strMess1 = strEditMess.replace("$1",ownerNotCostList.join(","));
                    sbMess.append(strMess1);
                }

%>
<script>

    alert("<%=sbMess.toString()%>")
</script>
<%
            }else {
                String strUrl = "./emxFullSearch.jsp?table=PMCCommonPersonSearchTable&field=TYPES=type_Person:CURRENT=policy_Person.state_Active&searchMode=GeneralPeopleTypeMode&form=PMCCommonPersonSearchForm&selection=single&suiteKey=ProgramCentral&HelpMarker=emxhelpriskassign&submitURL=./JF_TransferPartCostOwner.jsp";
                StringBuffer sb = new StringBuffer(strUrl);
                sb.append("&selectIds=");
                sb.append(partList.join(","));
                System.out.println("sb:" + sb);
%>
<script>
//    getTopWindow().showWizard('<%=sb.toString()%>');
    showChooser('<%=sb.toString()%>', 700, 500);
</script>
<%
            }
        }
    } catch (MatrixException e) {
        _logger.error(e.getMessage());
        e.printStackTrace();
    }


%>
