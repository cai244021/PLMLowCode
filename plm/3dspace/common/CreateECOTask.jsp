<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Arrays" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    try {
        ArrayList list = new ArrayList();
        String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
        for (int i = 0; i < strSelectIds.length; i++) {
            String id = strSelectIds[i].split("\\|")[1];
            list.add(id);
            if (i == strSelectIds.length-1) list.add(strSelectIds[i].split("\\|")[2]);
        }
        JPO.invoke(context,"JF_ChangeExecutionECOSource",null,"createECOTaskObject", JPO.packArgs(list));
    }catch (Exception e) {
        e.printStackTrace();
    }

%>
<html>

</html>
