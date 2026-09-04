<%@ page import="java.util.Map" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.SELECT_CURRENT" %>
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page import="static com.matrixone.apps.domain.DomainConstants.*" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkException" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.util.MatrixException" %>
<%@include file = "./emxNavigatorInclude.inc"%>
<script language="JavaScript" src="./scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="./scripts/emxUICore.js"></script>
<%!
    //日志
    private static final Logger _logger =  LoggerFactory.getLogger("JF_PartListDocService");
%>
<%
    String strObjectId = (String) emxGetParameter(request, "objectId");
    String strMode = (String) emxGetParameter(request, "mode");
    _logger.info("strObjectId:{}",strObjectId);
    _logger.info("strMode:{}",strMode);
//    String emxTableRowIdActual[] = emxGetParameterValues(request, "emxTableRowIdActual");
    String[] emxTableRowIdActual = emxGetParameterValues(request, "emxTableRowId");
    _logger.info("emxTableRowIdActual:{}",emxTableRowIdActual.length);
    if ("add".equals(strMode)){
        StringList relIdList = new StringList();
        if (null != emxTableRowIdActual &&  emxTableRowIdActual.length > 0){
            for (int i = 0; i < emxTableRowIdActual.length; i++) {
                String selectedId = emxTableRowIdActual[i];
                Map rowMap = ProgramCentralUtil.parseTableRowId(context,selectedId);
                String strRelId = (String) rowMap.get("relId");
                relIdList.add(strRelId);
            }
        }
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append("./emxIndentedTable.jsp?table=AEFGeneralSearchResults");
        sbUrl.append("&objectId=");
        sbUrl.append(strObjectId);
        sbUrl.append("&program=JF_PartList:getPartListDocByLoginUser&selection=multiple&insertNewRow=false&cancelButton=true&cancelLabel=emxFramework.Common.Cancel&submitURL=./JF_PartListDocService.jsp?mode=postAdd&relIdSet=" + relIdList.join(","));

%>
<script>
    getTopWindow().showWizard('<%=sbUrl.toString()%>')
</script>
<%
    }else if ("remove".equals(strMode)){
        StringList relIdList = new StringList();
        Boolean isAlert = Boolean.FALSE;
        String strMess = "";
        String strPartRelId = "";
        if (null != emxTableRowIdActual &&  emxTableRowIdActual.length > 0){
            Map paramsMap = new HashMap<>();
            for (int i = 0; i < emxTableRowIdActual.length; i++) {
                String selectedId = emxTableRowIdActual[i];
                Map rowMap = ProgramCentralUtil.parseTableRowId(context,selectedId);
                String strRelId = (String) rowMap.get("relId");
                // todo 检查是否关联文档 一个没有的话不需要弹出
                paramsMap.put("relId",strRelId);
                strPartRelId = strRelId;
                paramsMap.put("objectId",strObjectId);
                Map res = JPO.invoke(context, "JF_PartList", null, "checkDocHasConnectionByRelId", JPO.packArgs(paramsMap), Map.class);
                isAlert = (Boolean) res.get("isAlert");
                strMess = (String) res.get("mess");
                if (isAlert){
                    break;
                }
                paramsMap.clear();
            }
        }
        if (isAlert){
%>
<script>
    alert("<%=strMess%>")
</script>
<%
        }else {
            StringBuilder sbUrl = new StringBuilder();
            sbUrl.append("./emxIndentedTable.jsp?table=AEFGeneralSearchResults");
            sbUrl.append("&objectId=");
            sbUrl.append(strObjectId);
            sbUrl.append("&partRelId=");
            sbUrl.append(strPartRelId);
            sbUrl.append("&program=JF_PartList:getPartConnectDocByLoginUser&selection=multiple&insertNewRow=false&cancelButton=true&cancelLabel=emxFramework.Common.Cancel&submitURL=./JF_PartListDocService.jsp?mode=postRemove&relId=" +strPartRelId);

%>
<script>
    getTopWindow().showWizard('<%=sbUrl.toString()%>')
</script>
<%
        }

    }else if ("postAdd".equals(strMode)){
        boolean isAlert = false ;
        String strMess = "";
        StringList docIdList = new StringList();
        if (emxTableRowIdActual != null && emxTableRowIdActual.length > 0){
            String relIdSet = (String) emxGetParameter(request, "relIdSet");
            for (int i = 0; i < emxTableRowIdActual.length; i++) {
                String selectedId = emxTableRowIdActual[i];
                Map rowMap = ProgramCentralUtil.parseTableRowId(context,selectedId);
                String strDocId = (String) rowMap.get("objectId");
                docIdList.add(strDocId);
            }
            Map paramsMap = new HashMap<>();
            paramsMap.put("docIdList",docIdList);
            paramsMap.put("relIds",relIdSet);
            paramsMap.put("objectId",strObjectId);
            Map res = JPO.invoke(context, "JF_PartList", null, "checkAndAddDocByRelId", JPO.packArgs(paramsMap), Map.class);
            isAlert = (Boolean) res.get("isAlert");
            strMess = (String) res.get("mess");
        }else {
            isAlert = true;
            strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.PartList.NotSelectDocFail");

        }

        if (isAlert){
%>
<script>
    alert("<%=strMess%>")
    //设置请求为已完成
    parent.setSubmitURLRequestCompleted();
</script>
<%
        }else {

%>
<script>
    debugger;
    window.top.getWindowOpener().top.refreshTablePage();
    window.top.close();
</script>
<%

        }
    }else if ("postRemove".equals(strMode)){
            boolean isAlert = false ;
            String strMess = "";
            StringList docIdList = new StringList();
            if (emxTableRowIdActual != null && emxTableRowIdActual.length > 0){
                String strRelId = (String) emxGetParameter(request, "relId");
                MapList list  = new MapList();
                for (int i = 0; i < emxTableRowIdActual.length; i++) {
                    String selectedId = emxTableRowIdActual[i];
                    Map rowMap = ProgramCentralUtil.parseTableRowId(context,selectedId);
                    String strDocId = (String) rowMap.get("objectId");
                    docIdList.add(strDocId);
                    list.add(rowMap);
                }
                Map paramsMap = new HashMap<>();
                paramsMap.put("docIdList",docIdList);
                paramsMap.put("relId",strRelId);
                paramsMap.put("objectId",strObjectId);
                paramsMap.put("list",list);
                _logger.info("paramsMap:{}",paramsMap);
                Map res = JPO.invoke(context, "JF_PartList", null, "removeDocByRelId", JPO.packArgs(paramsMap), Map.class);
                isAlert = (Boolean) res.get("isAlert");
                strMess = (String) res.get("mess");
            }else {
                isAlert = true;
                strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.PartList.NotSelectDocFail");
            }
            if (isAlert){
%>
<script>
    alert("<%=strMess%>")
    //设置请求为已完成
    parent.setSubmitURLRequestCompleted();
</script>
<%
            }else {

%>
<script>
    window.top.refreshTablePage();
    // window.top.close();
</script>
<%

        }
    }
%>