<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<%@ include file="../programcentral/emxProgramGlobals2.inc" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<link rel="stylesheet" href="../common/styles/emxUIForm.css">
<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<script type="text/javascript" src="../common/scripts/emxUICore.js"></script>
<html>
<head>
<%--        <meta charset="utf-8"/>--%>
<%--        <title>VPM</title>--%>
</head>
<%!
    //日志
    private static final Logger _logger =  LoggerFactory.getLogger("JF_PreCreateFlexiblePart");
%>
<%
    String objectId = (String) emxGetParameter(request, "objectId");
    String strFieldNls = EnoviaResourceBundle.getAttributeI18NString(context,"JF_VPMReference.JF_TransformationPlan",context.getLocale().toString());
    String strRequire = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxFramework.Common.FieldRequire");
    String strFlushTableName = "JFVPMReferenceListTable";
    String language = context.getLocale().toString();
    String codeTitle = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponent.Common.FlexPartCode");
    String alertMessage = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponent.Common.ErrorAlertMessage");
    _logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@");

%>
<body>
<div id="divPageBody" style="top: 79px;">
    <form name="createDeformablePart" id="createDeformablePart" method="post" target="searchView">
        <table class="list">
            <tr><td class="labelRequired" width="100%"><%=strRequire%></td></tr>
            <tr id="calc_1">
                <td class="labelRequired" width="25%"><label for="DeformablePartCode"><%=codeTitle%></label></td>
                <td class="inputField" width="75%">
                    <input type="text" name="DeformablePartCode" id="DeformablePartCode"  rows="5" cols="50"  value="">
                </td>
            </tr>
            <tr id="calc_2">
                <td class="label" width="25%"><label for="DeformablePart"><%=strFieldNls%></label></td>
                <td class="inputField" width="75%">
                    <textarea name="DeformablePart" id="DeformablePart" rows="5" cols="50" wrap="" value=""></textarea>
                </td>
            </tr>

        </table>
    </form>
</div>
</body>
</html>

<script language="JavaScript">
    function createPart() {
        // alert("@@@@@@@@@@@@@@@@@@@@");
        let deformablePartCode = document.createDeformablePart.DeformablePartCode.value;
        console.log("code:", deformablePartCode);
        // 变形件代码输入不合法，请确保输入仅包含数字和字母。
        let rule = /^[a-zA-Z0-9]+$/;
        // let rule = "";
        if (!rule.test(deformablePartCode)){
            alert("<%=alertMessage%>");
        } else {
            let DeformablePart = document.createDeformablePart.DeformablePart.value;
            console.log("DeformablePart:", DeformablePart);
            let url = './JF_CreateFlexiblePart.jsp?objectId=<%=objectId%>&JF_VPMReference.JF_TransformationPlan=';
            url+=DeformablePart + "&code=" + deformablePartCode;
            console.log("url:", url);
            jQuery.ajax({
                url:url,
                type:"GET",
                async:false,
                success:function (res){
                    res = res.trim();
                    let words = res.split("{");
                    let data = JSON.parse("{" + words[1]);
                    let mess = data.mess;
                    alert(mess);
                    parent.getTopWindow().getWindowOpener().refreshSBTable();
                    parent.close();
                }
            });
        }
    }
</script>
