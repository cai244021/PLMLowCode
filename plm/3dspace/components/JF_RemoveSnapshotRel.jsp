<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    String strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorPart.DeleteSuccess");
    try {
        String objectId = emxGetParameter(request, "objectId");
        String mode = emxGetParameter(request, "mode");
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        String contextUser = context.getUser().toString();
        if("delete".equals(mode)){
            StringList deleteIds = new StringList();
            StringList errorIds = new StringList();
            for(String tableRowId : strSelectIds) {
                StringList tableIds = FrameworkUtil.split(tableRowId,"|");
                String oid = DomainConstants.EMPTY_STRING;
                if (tableIds.size() == 3) {
                    oid = tableIds.get(0);
                } else if (tableIds.size() == 4) {
                    oid = tableIds.get(1);
                }
                DomainObject domainObject = DomainObject.newInstance(context, oid);
                String owner = domainObject.getInfo(context, DomainConstants.SELECT_OWNER);
                String current = domainObject.getInfo(context, DomainConstants.SELECT_CURRENT);
                String name = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
                // “DRAFT”状态的数据快照允许owner进行删除，其他情况不允许删除；
                if (contextUser.equalsIgnoreCase(owner) && "DRAFT".equalsIgnoreCase(current)) {
                    deleteIds.add(oid);
                } else {
                    errorIds.add(name);
                }
            }
            if (!deleteIds.isEmpty()) {
                String[] stringArray = deleteIds.toStringArray();
                ContextUtil.pushContext(context);
                DomainObject.deleteObjects(context, stringArray);
                ContextUtil.popContext(context);
            }
            if (errorIds.size() > 0){
                strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorPart.DeleteFailed");
                strMess += EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.SnapshotDelete.OwnerAndCurrent");
                strMess += errorIds.join(",");
            }
        }
    }catch (Exception e) {
        strMess = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.ColorPart.DeleteFailed");
        e.printStackTrace();
    }

%>
<html>
<script>
    alert("<%=strMess%>");
    var refreshURL = window.parent.location.href;
    window.parent.location.href = refreshURL;
</script>
</html>
