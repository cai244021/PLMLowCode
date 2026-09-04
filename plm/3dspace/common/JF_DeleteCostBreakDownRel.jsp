<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="java.util.Map" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    try {
        String objectId = emxGetParameter(request, "objectId");
        String mode = emxGetParameter(request, "mode");
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        if ("remove".equalsIgnoreCase(mode)) {
            for(String tableRowId : strSelectIds) {
                StringList tableIds = FrameworkUtil.split(tableRowId,"|");
                DomainRelationship.disconnect(context,tableIds.get(0));
            }
        }else if("delete".equals(mode)){
            StringList deleteList = new StringList();
            for(String tableRowId : strSelectIds) {
                StringList tableIds = FrameworkUtil.split(tableRowId,"|");
                String delId = "";
                if(tableIds.size()<4){
                    delId = tableIds.get(0);
                }else  if(tableIds.size()==4){
                    delId = tableIds.get(1);
                }
                deleteList.add(delId);
            }
            ContextUtil.pushContext(context);
            DomainObject.deleteObjects(context, deleteList.toStringArray());
            ContextUtil.popContext(context);
        }
    }catch (Exception e) {
        e.printStackTrace();
    }

%>
<html>
<script>
    var refreshURL = window.parent.location.href;
    window.parent.location.href = refreshURL;
</script>
</html>
