<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="matrix.db.RelationshipType" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="com.matrixone.apps.domain.util.PropertyUtil" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    String relationshipName = emxGetParameter(request, "rel");
    String objectId = emxGetParameter(request, "objectId");
    //添加内容
    String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
    if (tableRowIdList != null) {
        //分解选择的对象id
        String[] allContent = new String[tableRowIdList.length];
        for (int i = 0 ; i < tableRowIdList.length; i++) {
            String[] split = tableRowIdList[i].split("\\|");
            String strSelectId = split[1].trim();
            allContent[i] = strSelectId;
        }
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        domainObject.addRelatedObjects(context, new RelationshipType(relationshipName), true, allContent);
        //后处理   根据类型 获取关系
        if ("JFRelPart2Raw".equalsIgnoreCase(relationshipName)) {
            domainObject.setAttributeValue(context, "PLMEntity.V_description", "");
            domainObject.setAttributeValue(context, "PLMEntity.V_Name", "");
        }
    }

%>
<html>
<script >
    getTopWindow().closeWindow();
    var refreshURL = getTopWindow().getWindowOpener().location.href;
    getTopWindow().getWindowOpener().location.href = refreshURL;
    // window.top.getWindowOpener().top.refreshTablePage();
</script>
</html>

