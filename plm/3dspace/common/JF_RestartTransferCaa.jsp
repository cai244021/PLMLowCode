<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<%@ include file="../programcentral/emxProgramGlobals2.inc" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%
    String objectId = (String) emxGetParameter(request, "objectId");
    DomainObject domainObject = DomainObject.newInstance(context, objectId);
    String strJSReason = domainObject.getAttributeValue(context, "JSReason");
    Map<String, String> map = new HashMap<>();
    //重新生成文件
    if (strJSReason.contains("文件版本") && strJSReason.contains("下发版本")) {
        //重新生成json
        JPO.invoke(context, "JF_DataOutSource", new String[]{}, "generateJSONFileAgain", new String[]{objectId}, void.class);
    }
    map.put("JSReason", "");
    map.put("JSdataProcessProgress", "PendingCAAProcess");
    domainObject.setAttributeValues(context, map);
    String mess = "";
    %>
<html>
<script>
    alert("\u5df2\u5c06\u7533\u8bf7\u5355\u52a0\u5165CAA\u8f6c\u6362\u961f\u5217\u4e2d...");
    parent.getTopWindow().findFrame(parent.getTopWindow(),"detailsDisplay").location.href = parent.getTopWindow().findFrame(parent.getTopWindow(),"detailsDisplay").location.href.replace("persist=true", "persist=false");
</script>
</html>