<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="java.util.*" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    String mess = "";
    try {
        StringList list = new StringList();
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        String strObjId = emxGetParameter(request, "objectId");
        StringList selectedBuildIds = new StringList();
        if (strSelectIds != null) {
            for (int i = 0; i < strSelectIds.length; i++) {
                StringTokenizer strTokenizer = new StringTokenizer(strSelectIds[i], "|");
                selectedBuildIds.add((String) strTokenizer.nextElement());
            }
        } else {
            selectedBuildIds.add(strObjId);
        }
        StringList errorList = new StringList();
        for (String strId : selectedBuildIds) {
            DomainObject domainObject = DomainObject.newInstance(context, strId);
            if (!"Release".equalsIgnoreCase(domainObject.getInfo(context, DomainConstants.SELECT_CURRENT))) {
                errorList.add(domainObject.getInfo(context, DomainConstants.SELECT_NAME));
            }
        }
        if (errorList.size() > 0) {
            mess = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.RapidOffer.StateError", new String[]{});
            mess += errorList.join(",");
        } else {
            HashMap hashMap = new HashMap();
            hashMap.put("objectIdList", selectedBuildIds);
            mess = (String) JPO.invoke(context, "JF_Cost", JPO.packArgs (hashMap), "setRapidOfferRevise", JPO.packArgs (hashMap), String.class);
        }
    }catch (Exception e) {
        e.printStackTrace();
    }

%>
<html>
<script>
    alert("<%=mess%>");
    var refreshURL = window.parent.location.href;
    window.parent.location.href = refreshURL;
</script>
</html>
