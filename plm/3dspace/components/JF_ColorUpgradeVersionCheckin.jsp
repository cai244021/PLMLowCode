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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    String strMess = DomainConstants.EMPTY_STRING;
    StringList errorIds = new StringList();
    String objectId = emxGetParameter(request, "objectId");
    StringBuffer sbUrl = new StringBuffer( "JF_ColorUpgradeVersionFS.jsp");
    sbUrl.append("?objectId=");
    sbUrl.append(objectId);
%>
<html>
<script>
    window.open ("<%=sbUrl%>", '', 'width=500,height=250,left=600,top=350,scrollbars=no,location=no,fullscreen=no','false');
    // var refreshURL = window.parent.location.href;
    // window.parent.location.href = refreshURL;
</script>
</html>
