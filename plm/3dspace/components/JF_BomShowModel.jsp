
<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../common/emxNavigatorTopErrorInclude.inc" %>
<%
    try {
        String Mode = emxGetParameter(request, "Mode");
        System.out.println("Mode: " + Mode);
        String objectId = emxGetParameter(request, "objectId");
        String header = "";
        String allowEdit = "";
        String toolbar = "";
        if("Expand".equals(Mode)){
            header = "Usage View";
            allowEdit = "true";
            toolbar = "JFEBOMAction";
        }else{
            header = "Reference View";
            allowEdit = "false";
            toolbar = "JFEBOMAction";
        }
%>
<html>
<head>
</head>
<body>

<script language="JavaScript">
    var url = parent.document.location.href;
    url = resetParameter("Mode", "<xss:encodeForJavaScript><%=Mode%></xss:encodeForJavaScript>", url);
    url = resetParameter("objectId", "<xss:encodeForJavaScript><%=objectId%></xss:encodeForJavaScript>", url);
    url = resetParameter("header", "<xss:encodeForJavaScript><%=header%></xss:encodeForJavaScript>", url);
    url = resetParameter("editLink", "<xss:encodeForJavaScript><%=allowEdit%></xss:encodeForJavaScript>", url);
    url = resetParameter("toolbar", "<xss:encodeForJavaScript><%=toolbar%></xss:encodeForJavaScript>", url);
    parent.document.location.href = url;
    function resetParameter(parm, val, urlStr){
        if(urlStr.indexOf("amp;") >= 0){
            while(urlStr.indexOf("amp;") >= 0){
                urlStr = urlStr.replace("amp;", "");
            }
        }

        var arrURLparms = urlStr.split("&");
        var len = arrURLparms.length;
        var count = 0;
        for(var i = 0; i < len; i++){
            arrURLparms[i] = arrURLparms[i].split("=");
            //only change the first matching parm
            if(arrURLparms[i][0] == parm && count == 0){
                count++;
                //set new value
                arrURLparms[i][1] = val;
            }
            arrURLparms[i] = arrURLparms[i].join("=");
        }
        urlStr = arrURLparms.join("&");
        //if the count is still zero add param to end
        if(count == 0){
            urlStr += ("&" + parm + "=" + val);
        }
        return urlStr;
    }
</script>
<%
    } catch (Exception ex) {
        if (ex.toString() != null && ex.toString().length() > 0) {
            emxNavErrorObject.addMessage(ex.toString());
        }
        ex.printStackTrace();
    }
%>
</body>
</html>
<%@include file="../common/emxNavigatorBottomErrorInclude.inc" %>
