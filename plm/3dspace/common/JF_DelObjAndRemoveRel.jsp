<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    String mode = emxGetParameter(request, "mode");
    String flag = "T";
    String strMess = "";
    try {
        String objectId = emxGetParameter(request, "objectId");
        String owner = emxGetParameter(request, "owner");
        String delType = emxGetParameter(request, "delType");
        String parentOID = emxGetParameter(request, "parentOID");
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        String contextOwner = context.getUser();
        if("ECO".equalsIgnoreCase(delType)&& UIUtil.isNotNullAndNotEmpty(objectId)){
            DomainObject obj = DomainObject.newInstance(context,objectId);
            contextOwner =obj.getInfo(context, DomainConstants.SELECT_OWNER);
        }
        Boolean DAFlag = false;
        //获取DA关联项目的项目经理账号
        if("DA".equalsIgnoreCase(delType)&& UIUtil.isNotNullAndNotEmpty(objectId)){
            DomainObject obj = DomainObject.newInstance(context,objectId);
//            contextOwner =obj.getInfo(context, DomainConstants.SELECT_OWNER);
            Map requestMap = new HashMap();
            requestMap.put("objectId",objectId);
             DAFlag = (Boolean) JPO.invoke(context, "JF_DeviationApplicationSource", new String[0], "access2ExecutePlan", JPO.packArgs(requestMap), Boolean.class);

        }
        strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorPart.DeleteSuccess");
        if ("remove".equalsIgnoreCase(mode)) {
            for(String tableRowId : strSelectIds) {
                StringList tableIds = FrameworkUtil.split(tableRowId,"|");
                DomainRelationship.disconnect(context,tableIds.get(0));
            }
        }else if("delete".equals(mode)){
            StringList deleteIds = new StringList();
            StringList errorIds = new StringList();
            for(String tableRowId : strSelectIds) {
                StringList tableIds = FrameworkUtil.split(tableRowId,"|");
                String delId = "";
               if(tableIds.size()<4){
                delId = tableIds.get(0);
               }else  if(tableIds.size()==4){
                   delId = tableIds.get(1);
               }
                DomainObject domainObject = DomainObject.newInstance(context, delId);
                String delOwner = domainObject.getInfo(context, DomainConstants.SELECT_OWNER);
                if ("ECO".equalsIgnoreCase(delType)&&contextOwner.equalsIgnoreCase(context.getUser())) {
                    deleteIds.add(delId);
                } else  if ("DA".equalsIgnoreCase(delType)) {//项目经理有权限删除
                    if(DAFlag) {
                        deleteIds.add(delId);
                    }else{
                        String title = domainObject.getInfo(context, DomainConstants.SELECT_ATTRIBUTE_TITLE);
                        errorIds.add(title);
                    }
                }
                else if (contextOwner.equalsIgnoreCase(delOwner)) {
                    deleteIds.add(delId);
                } else {
                    String title = domainObject.getInfo(context, DomainConstants.SELECT_ATTRIBUTE_TITLE);
                    errorIds.add(title);
                }
            }
            if (errorIds.isEmpty()) {
                ContextUtil.pushContext(context);
                com.matrixone.apps.program.Task.deleteTasks(context, deleteIds, false);
                ContextUtil.popContext(context);
            } else {
                strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorPart.DeleteFailed");
                if(delType.equals("DA")){
                    strMess += EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Delete.ProjectManagerNotice");
                }else {
                    strMess += EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Delete.Owner");
                }
                flag = "F";
            }
        }
    }catch (Exception e) {
        e.printStackTrace();
    }

%>
<html>
<script>
    var mode = "<%=mode%>";
    var flag = "<%=flag%>";
    var strMess = "<%=strMess%>";
    if ("delete" === mode) {
        if ("T" === flag) {
            alert(strMess);
            // window.top.getWindowOpener().top.refreshTablePage();
            // parent.getTopWindow().getWindowOpener().refreshSBTable();

            var refreshURL = window.parent.location.href;
            window.parent.location.href = refreshURL;
        } else {
            alert(strMess);
        }
    } else {
        var refreshURL = window.parent.location.href;
        window.parent.location.href = refreshURL;
    }
</script>
</html>
