<%@page import="com.matrixone.apps.domain.util.EnoviaResourceBundle"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    String strLanguage = request.getHeader("Accept-Language");   //PRG:RG6:R212:1-Jun-2011:IR-111810V6R2012x
    String strObjectId = emxGetParameter(request, "objectId");
    String submitUrl = "&submitURL=../common/JF_postProcessTwoDimensionalDrawingWithRel.jsp?" ;
    String user = context.getUser();
%>
<html>
<script>
    let user = '<%=user%>';
    var searchUrl = "../common/emxFullSearch.jsp?field=TYPES=type_Drawing,type_Document:owner="+user+"&showInitialResults=true&table=AEFGeneralSearchResults&selection=multiple";
    searchUrl += "&cancelLabel=emxFramework.Common.Close";
    searchUrl += "<%=submitUrl%>";
    searchUrl += "&objectId=" + "<%=strObjectId%>";
    searchUrl += "&submitAction=refreshCaller";
    searchUrl += "&HelpMarker=emxhelpfullsearch";
    showModalDialog(searchUrl);
</script>
</html>

