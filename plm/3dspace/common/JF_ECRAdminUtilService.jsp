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
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="matrix.db.Vault" %>
<%--登录检查--%>
<%@include file = "emxNavigatorInclude.inc"%>
<%!
    //日志
    private static final Logger _logger =  LoggerFactory.getLogger("JF_ECRAdminUtilService");
%>
<%

    String strObjectId = emxGetParameter(request, "objectId");
    String strParentId = emxGetParameter(request, "parentId");
    String strMode = emxGetParameter(request, "mode");
    _logger.info("strObjectId:{}",strObjectId);
    _logger.info("strParentId:{}",strParentId);
    _logger.info("strMode:{}",strMode);
    String emxTableRowIdActual[] = emxGetParameterValues(request, "emxTableRowIdActual");
    boolean isAlert = false ;
    StringList ecrNameList = new StringList();
    StringList selectIdList = new StringList();
    DomainObject ecr = DomainObject.newInstance(context);
    if (null != emxTableRowIdActual &&  emxTableRowIdActual.length > 0){
        for (int i = 0; i < emxTableRowIdActual.length; i++) {
            String selectedId = emxTableRowIdActual[i];
            Map rowMap = ProgramCentralUtil.parseTableRowId(context,selectedId);
            String strECRId = (String) rowMap.get("objectId");
            ecr.setId(strECRId);
            Map ecrInfo = ecr.getInfo(context, StringList.create(DomainConstants.SELECT_CURRENT,DomainConstants.SELECT_NAME));
            String strCurrent = (String)ecrInfo.get(DomainConstants.SELECT_CURRENT);
            if (!"Countersign".equalsIgnoreCase(strCurrent)){
                isAlert = true;
                String strName = (String)ecrInfo.get(DomainConstants.SELECT_NAME);
                ecrNameList.add(strName);
            }
            selectIdList.add(strECRId);
        }
    }
    if (isAlert){
        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ECRAdminUtilCheckFail");
        strMess = strMess.replace("$1",ecrNameList.join(","));
%>
<script>
    alert("<%=strMess%>")
</script>
<%
        return;
    }
    if ("disconnect".equals(strMode)){
        //校验ECR必须是会签状态
        Map argsMap = new HashMap<>();
        argsMap.put("isStartTran",Boolean.TRUE);
        argsMap.put("ECRIdList",selectIdList);
        String[] args = JPO.packArgs(argsMap);
        JPO.invoke(context, "JF_ECRService", null, "disConnectECRAssociationObjByECRId", args, void.class);
    }else if ("rebuild".equals(strMode)){
        Map argsMap = new HashMap<>();
        argsMap.put("ECRIdList",selectIdList);
        String[] args = JPO.packArgs(argsMap);
        Map res = JPO.invoke(context, "JF_ECRService", null, "checkECRAssociationObjByECRId", args, Map.class);
        Boolean isPass = (Boolean)res.get("code");
        if (isPass){
            String strMess = (String)res.get("mess");
%>
<script>
    alert("<%=strMess%>")
</script>
<%
            return;
        }else {
                for (int i = 0; i < selectIdList.size(); i++) {
                    String strECRId = selectIdList.get(i);
                    JPO.invoke(context, "JF_VPMReferenceEBOM", null, "findBuyPartOneLevel", new String[]{strECRId}, void.class);
                }
        }
    }
%>
<script>
    var drame1= parent;
    drame1.emxEditableTable.refreshSelectedRows();
</script>
<%--MQL通知--%>
<jsp:include  page="../components/emxMQLNotice.jsp" flush="true"/>
</html>