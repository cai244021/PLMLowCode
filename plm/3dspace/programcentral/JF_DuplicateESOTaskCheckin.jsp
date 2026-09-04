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
<%@ page import="com.matrixone.apps.program.ProgramCentralUtil" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<script src="../common/scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="../common/scripts/emxUISearch.js"></script>
<%@include file="../emxUIFramesetUtil.inc"%>
<%
    String parentId = emxGetParameter(request, "objectId");
    String[] strSelectIds = emxGetParameterValues(request, "emxTableRowId");
    Map rowMap = ProgramCentralUtil.parseTableRowId(context,strSelectIds[0]);
    String objectId = (String) rowMap.get("objectId");
//    StringList strIdList = FrameworkUtil.split(strSelectIds[0], "|");
//    String objectId = strIdList.get(1);
    DomainObject domainObject = DomainObject.newInstance(context, objectId);
    String type  = domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
    //只能选择ESO任务进行复制
    if (!"JF_ESOTask".equalsIgnoreCase(type)) {
        String strMess = EnoviaResourceBundle.getProperty(context, "emxProgramCentralStringResource", context.getLocale(), "emxProgramCentral.Mess.CopyTypeError");
%>
<script language="Javascript">
    alert("<%=strMess%>");
</script>
<%
        return;
    }
    StringBuffer sbUrl = new StringBuffer("./JF_DuplicateESOTaskFS.jsp");
    sbUrl.append("?parentId=");
    sbUrl.append(parentId);
    sbUrl.append("&objectId=");
    sbUrl.append(objectId);
%>
<html>
<script>
    window.open ("<%=sbUrl%>", '', 'width=500,height=300,left=600,top=350,scrollbars=no,location=no,fullscreen=no','false');
    // var refreshURL = window.parent.location.href;
    // window.parent.location.href = refreshURL;
</script>
</html>
