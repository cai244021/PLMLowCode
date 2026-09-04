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
    private static final Logger _logger =  LoggerFactory.getLogger("JF_PartListService");
%>
<%
    String strObjectId = (String) emxGetParameter(request, "objectId");
    String strMode = (String) emxGetParameter(request, "mode");
    _logger.info("strObjectId:{}",strObjectId);
    _logger.info("strMode:{}",strMode);
    String emxTableRowIdActual[] = emxGetParameterValues(request, "emxTableRowIdActual");
    String emxTableRowId[] = emxGetParameterValues(request, "emxTableRowId");
    if ("delete".equals(strMode)){
        boolean isAlert = false ;
        StringList alertPartList = new StringList();
        StringList partListIdList = new StringList();
        // emxComponents.Command.JFDeletePartListAlertMess
        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Command.JFDeletePartListAlertMess");
        if (null != emxTableRowIdActual &&  emxTableRowIdActual.length > 0){
            DomainObject partListBO = DomainObject.newInstance(context);
            for (int i = 0; i < emxTableRowIdActual.length; i++) {
                String selectedId = emxTableRowIdActual[i];
                Map rowMap = ProgramCentralUtil.parseTableRowId(context,selectedId);
               String strPartListId = (String) rowMap.get("objectId");
                partListBO.setId(strPartListId);
                partListIdList.add(strPartListId);
                Map infoMap = partListBO.getInfo(context,StringList.create(SELECT_CURRENT,SELECT_ATTRIBUTE_TITLE));
                String strCurrent = (String) infoMap.get(SELECT_CURRENT);
                String strTitleValue = (String) infoMap.get(SELECT_ATTRIBUTE_TITLE);
                if (!"InWork".equals(strCurrent)){
                    isAlert = true ;
                    alertPartList.add(strTitleValue);
                }
            }
        }
        if (isAlert){
            strMess = strMess.replace("{}",alertPartList.join(","));
%>
<script>
    alert("<%=strMess%>")
</script>
<%
        return;
        }else {
            try {
                ContextUtil.startTransaction(context,true);
                DomainObject.deleteObjects(context,partListIdList.toStringArray());
                ContextUtil.commitTransaction(context);
                Gson gson = new Gson();
                String strRowIds = gson.toJson(emxTableRowIdActual);
                _logger.info("strRowIds:{}",strRowIds);
%>
<script>
    const selectRowArr = <%=strRowIds%>;
    const iFrame = parent;
    debugger;
    iFrame.emxEditableTable.removeRowsSelected(selectRowArr);
    iFrame.rebuildView();
</script>
<%
            } catch (Exception e) {
                ContextUtil.abortTransaction(context);
%>
<script>
    alert("<%=e.getMessage()%>")
</script>
<%
                return;
            }
        }
    }else  if ("remove".equals(strMode)){
    //emxComponents.Remove.Successful
                    StringList relIdList = new StringList();
                    if (null != emxTableRowIdActual &&  emxTableRowIdActual.length > 0){
                        for (int i = 0; i < emxTableRowIdActual.length; i++) {
                            String selectedId = emxTableRowIdActual[i];
                            Map rowMap = ProgramCentralUtil.parseTableRowId(context,selectedId);
                            String strPartListRelId = (String) rowMap.get("relId");
                            relIdList.add(strPartListRelId);
                        }
                    }
                    try {
                        ContextUtil.startTransaction(context,true);
                        DomainRelationship.disconnect(context,relIdList.toStringArray());
                        ContextUtil.commitTransaction(context);
                        Gson gson = new Gson();
                        String strRowIds = gson.toJson(emxTableRowIdActual);
                        _logger.info("strRowIds:{}",strRowIds);
                        String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PartList.RemovePartSuccess");
%>
<script>
    alert("<%=strMess%>")
    const selectRowArr = <%=strRowIds%>;
    const iFrame = findFrame(getTopWindow(),"JFPartListPartsPMCmd");
    iFrame.emxEditableTable.removeRowsSelected(selectRowArr);
    iFrame.rebuildView();
</script>
<%
                    } catch (Exception e) {
                        ContextUtil.abortTransaction(context);
                        String strFailMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.PartList.RemovePartFail");
                        e.printStackTrace();

%>
<script>

    alert("<%=strFailMess%>")
</script>
<%

                        return;
                    }
    }else if ("add".equals(strMode)){
        _logger.info("-------------------------------- JF_PartListService add begin ---------------------------------------------");
        if (null != emxTableRowId &&  emxTableRowId.length > 0){
            Set partIdSet = new HashSet<String>();
            for (int i = 0; i < emxTableRowId.length; i++) {
                String selectedId = emxTableRowId[i];
                Map rowMap = ProgramCentralUtil.parseTableRowId(context,selectedId);
                String strPartId = (String) rowMap.get("objectId");
                partIdSet.add(strPartId);
            }
            Map argsMap = new HashMap<>();
            argsMap.put("selectPartId",partIdSet);
            argsMap.put("partListId",strObjectId);
            Map resMap = null;
            try {
                resMap = JPO.invoke(context, "JF_PartList", null, "partListAddPart",  JPO.packArgs(argsMap), Map.class);
            } catch (MatrixException e) {
                resMap.put("code","404");
                resMap.put("mess",e.getMessage());
            }
            _logger.info("resMap:{}",resMap);
            String strCode = (String) resMap.get("code");
            if ("200".equals(strCode)){
%>
<script>
    window.top.getWindowOpener().top.refreshTablePage();
    window.top.close();
</script>
<%
            }else {

                String strMess = (String) resMap.get("mess");
%>
<script>
    alert("<%=strMess%>");
    window.top.close();

</script>
<%
            }
        }
        _logger.info("-------------------------------- JF_PartListService add end ---------------------------------------------");
    }else if ("initialization".equals(strMode)){
        Map argsMap = new HashMap<>();
        argsMap.put("partListId",strObjectId);
        Map resMap = JPO.invoke(context, "JF_PartList", null, "partListInitializationPart",  JPO.packArgs(argsMap), Map.class);
        String strMess = (String) resMap.get("mess");
        String strCode = (String) resMap.get("code");
        if ("200".equals(strCode)){
%>
<script>
    const iFrame = findFrame(getTopWindow(),"JFPartListPartsPMCmd");
//    iFrame.emxEditableTable.removeRowsSelected(selectRowArr);
    if (iFrame){
        iFrame.refreshSBTable();
    }
</script>
<%
        }else {
%>
<script>
    alert("<%=strMess%>")
</script>
<%
        }
    }
%>