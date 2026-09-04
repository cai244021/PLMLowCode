<%--<!DOCTYPE html>--%>
<%--<html>--%>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.apache.poi.ss.usermodel.Workbook" %>
<%@ page import="jakarta.servlet.ServletOutputStream" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkException" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.google.gson.Gson" %>
<%--登录检查--%>
<%@include file = "emxNavigatorInclude.inc"%>
<%--<script language="JavaScript" src="scripts/emxUIConstants.js" type="text/javascript"></script>--%>
<%--<script language="javascript" src="scripts/emxUICore.js"></script>--%>
<%--<script language="javascript" src="scripts/emxUIModal.js"></script>--%>
<%--<script language="javascript" src="scripts/emxUIFormUtil.js"></script>--%>
<%!
    //日志
    private static final Logger _logger =  LoggerFactory.getLogger("JF_ColorPartService");
%>
<%
    String strMode = emxGetParameter(request, "mode");
    String strType = emxGetParameter(request, "type");
    if ("del".equalsIgnoreCase(strMode)){
        String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
        Gson gson = new Gson();
        //转json
        String strSelectArr = gson.toJson(tableRowIdList);
        Set<String> selectIdSet = new HashSet<String>();
        for (int i = 0; i < tableRowIdList.length; i++) {
            String strChooseData = tableRowIdList[i];
            StringTokenizer strtk = new StringTokenizer(strChooseData, "|");
            int size = strtk.countTokens();
            String strOId = "";
            if (size== 4){
                strtk.nextToken();
                strOId = strtk.nextToken();
                strOId = XSSUtil.encodeForJavaScript(context, strOId);
                selectIdSet.add(strOId);
            }else{
                strOId = strtk.nextToken();
                strOId = XSSUtil.encodeForJavaScript(context, strOId);
                selectIdSet.add(strOId);
            }
        }
        if ("group".equals(strType)){
            String strMess = "";
            boolean isError = false ;
            if (selectIdSet.size() > 0){
                try {
                    ContextUtil.startTransaction(context,true);
                    DomainObject.deleteObjects(context, StringList.create(selectIdSet).toStringArray());
                    ContextUtil.commitTransaction(context);
                    strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ColorPartMess.DelColorGroupSuccess");

                } catch (FrameworkException e) {
                    ContextUtil.abortTransaction(context);
                    strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Mess.ColorPartMess.DelColorGroupError");
                    _logger.error(e.getMessage());
                    isError = true;
                }
            }


%>
<script>
    alert("<%=strMess%>")
    if (!<%=isError%>){
        const selectArr = JSON.parse(`<%=strSelectArr%>`);
        parent.emxEditableTable.removeRowsSelected(selectArr);
    }
</script>
<%
        }else if ("style".equals(strType)){
            if (selectIdSet.size() > 0){
                try {
                    ContextUtil.startTransaction(context,true);
                    DomainObject.deleteObjects(context, StringList.create(selectIdSet).toStringArray());
                    ContextUtil.commitTransaction(context);
                } catch (FrameworkException e) {
                    ContextUtil.abortTransaction(context);
                    _logger.error(e.getMessage());
                }
            }
%>
<script>
    debugger;
    const tableWin = parent.findFrame(parent,"detailsDisplay");
    console.log(tableWin);
    if (tableWin){
        tableWin.location.href = tableWin.location.href;
    }else if (tableWin.top){
        parent.location.href = parent.location.href;
    }
</script>
<%
        }
    }

%>
