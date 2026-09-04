<%@page import="com.matrixone.apps.domain.util.EnoviaResourceBundle"%>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="java.util.Enumeration" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%


    String strLanguage = request.getHeader("Accept-Language");   //PRG:RG6:R212:1-Jun-2011:IR-111810V6R2012x
    //objectId type=Type_JFDataOutSource&relationship=relationship_ReferenceDocument
    String strObjectId = emxGetParameter(request, "objectId");
    String strType = emxGetParameter(request, "type");
    String strRelationship = emxGetParameter(request, "relationship");
    String strProgram = emxGetParameter(request, "program");
    String strFunction = emxGetParameter(request, "function");
    String flag = emxGetParameter(request, "flag");
    String submitUrl = "&submitURL=../common/JF_CostProcess.jsp.jsp?flag=" + flag;

%>
<html>
<script>
    var searchUrl = "../common/emxFullSearch.jsp?field=TYPES=type_JFCost&showInitialResults=true&table=PMCGeneralSearchResults&selection=multiple";
    searchUrl += "&cancelLabel=emxFramework.Common.Close";
    searchUrl += "<%=submitUrl%>";
    searchUrl += "&rel=" + "<%=strRelationship%>";
    searchUrl += "&Type=" + "<%=strType%>";
    searchUrl += "&objectId=" + "<%=strObjectId%>";
    searchUrl += "&submitAction=refreshCaller";
    searchUrl += "&HelpMarker=emxhelpfullsearch";
    showModalDialog(searchUrl);
</script>
</html>

