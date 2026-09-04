
        <%@ page pageEncoding="utf-8" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
        <%@ page import="java.util.Arrays" %>
        <%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
        <%@ page import="com.matrixone.apps.domain.DomainObject" %>
        <%@ page import="matrix.util.StringList" %>
        <%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
        <%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
        <%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../components/emxComponentsNoCache.inc"%>
<%@include file = "../components/emxComponentsUtil.inc"%>
<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>
<jsp:useBean id="tableBean" class="com.matrixone.apps.framework.ui.UITable" scope="session"/>
        <jsp:useBean id="formBean" scope="page" class="com.matrixone.apps.common.util.FormBean" />
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<script language="JavaScript" src="../common/scripts/emxUIConstants.js" type="text/javascript"></script>
<script language="JavaScript" src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<html>
<head>
    <link rel="stylesheet" type="text/css" href="../common/styles/emxUIDefault.css">
    <link rel="stylesheet" type="text/css" href="../common/styles/emxUIList.css">
</head>
<body>
<%!
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger("JF_MBOMExportDialogFS.jsp");
%>
<%
    String objectId = (String) emxGetParameter(request, "objectId");
    StringBuffer sbUrl = new StringBuffer( "JF_MBOMExportCheckout.jsp?objectId="+objectId);
    String notesY = context.getLocale().toString().contains("zh") ? "MBOM导出完成!" : "MBOM Export Success";
    String notesN = context.getLocale().toString().contains("zh") ? "MBOM导出失败!" : "MBOM Export Failed";
    String header = context.getLocale().toString().contains("zh") ? "导出MBOM" : "Export MBOM";
    String content = context.getLocale().toString().contains("zh") ? "正在导出中......" : "Exporting......";

%>
</body>
<script language="JavaScript">
    function checkinCancel() {
        getTopWindow().closeWindow();
    }


    function checkin(button) {
        // 创建遮罩层
        var overlay = document.createElement('div');
        overlay.style.position = 'fixed';
        overlay.style.top = '0';
        overlay.style.left = '0';
        overlay.style.width = '100%';
        overlay.style.height = '100%';
        overlay.style.backgroundColor = 'rgba(0, 0, 0, 0.5)';
        overlay.style.zIndex = '9999';
        overlay.style.display = 'flex';
        overlay.style.justifyContent = 'center';
        overlay.style.alignItems = 'center';
        overlay.innerHTML = '<div style="color: white; font-size: 20px;"><%=content%></div>';
        document.body.appendChild(overlay);

        // 禁用当前按钮
        button.disabled = true;
        // 获取表单元素
        var form = document.getElementById('checkinForm');
        // 创建 FormData 对象
        var formData = new FormData(form);
        console.log('Form Data:', Array.from(formData.entries())); // 打印表单数据
        // 使用 fetch 发送数据到后端
        fetch("JF_MBOMExportCheckout.jsp", {
            method: "POST",
            body: formData
        })
            .then(response => response.json())
            .then(result => {
                console.log("result-->", result);
                let fileName = result.fileName;
                let base64File = result.base64File;
                console.log("fileName-->", fileName);
                console.log("base64File-->", base64File);
                if (fileName.length > 0) {
                    let bstr = atob(base64File),
                        n = bstr.length,
                        u8arr = new Uint8Array(n);
                    while (n--) {
                        u8arr[n] = bstr.charCodeAt(n);
                    }
                    let blob = new Blob([u8arr]);
                    console.log("blob:", blob);
                    let link = document.createElement('a');
                    link.href = window.URL.createObjectURL(blob);
                    link.download = fileName;
                    link.click();
                    window.URL.revokeObjectURL(link.href); // 修正这里，释放链接
                    mess = "<%=notesY%>";
                } else {
                    mess = "<%=notesN%>";
                }
                alert(mess);
                getTopWindow().closeWindow();
            })
            .catch(err => {
                alert("fail");
                console.log("error-->", err);
                getTopWindow().closeWindow();
            });
    }

</script>
<div id='divPageBody'>
    <form id="checkinForm" name="checkinForm" enctype="multipart/form-data" target="_parent" method="post"  action="" scrolling="auto">
        <table class="list">
            <tbody>
            <tr>
                <framework>
                    <th class="" width="100%"> <%=header%> </th>
                </framework>
            </tr>
            <tr class="even">
                <!-- //XSSOK -->
                <td width="100%" style="height: 50vh; display: flex; justify-content: center; align-items: center; flex-direction: column;">
                    <table style="margin: 0 auto; text-align: center;">
                        <tbody>
                        <tr style="top: 100px;">
                            <td class="label" width="100%">
                                <div class="radio-group" style="display:flex; flex-direction:row; justify-content: center; gap: 15px;">
                                    <span style="margin-right:10px;">
                                        <input type="radio" id="wholeChair" name="mbomType" value="GC" checked>
                                        <label for="wholeChair">整椅MBOM</label>
                                    </span>
                                    <span style="margin-right:10px;">
                                        <input type="radio" id="cover" name="mbomType" value="GT">
                                        <label for="cover">面套MBOM</label>
                                    </span>
                                    <span style="margin-right:10px;">
                                        <input type="radio" id="foam" name="mbomType" value="GU">
                                        <label for="foam">发泡MBOM</label>
                                    </span>
                                </div>

                            </td>
                        </tr>
                        <tr>
                            <td style="font-weight:bold;">
                                <input name="objectId" id="objectId" maxlength="" size="20" title="objectId" value="<%=objectId%>" type="text" style="display:none">
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
            </tbody>
        </table>
    </form>
</div>
<div id="divPageFoot">
    <table width="100%" border="0" align="center" cellspacing="2" cellpadding="3">
        <tbody>
        <tr>
            <td class="buttons" align="right">
                <table border="0" cellspacing="0">
                    <tbody>
                    <tr>
                        <td>
                            <a href="javascript:void(0)" class="button" onclick="checkin(this)"><button class="btn-primary" type="button">
                                <emxUtil:i18n localize="i18nId">emxComponents.Button.Done</emxUtil:i18n></button>
                            </a>
                            <a onclick="javascript:window.close()">
                                <button class="btn-default"><emxUtil:i18n localize="i18nId">emxComponents.Button.Cancel</emxUtil:i18n></button>
                            </a>

                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        </tbody>
    </table>
</div>
</html>
<%--        <script language="JavaScript">--%>
<%--            let mess = "<%=mess%>";--%>
<%--            // 检查mess是否为空--%>
<%--            if (mess) {--%>
<%--                alert(mess); // 弹出提示框--%>
<%--            } else {--%>
<%--                let url = "<%=sbUrl%>";--%>
<%--                var l = window.open("<%=srcLink%>", '', 'width=500,height=250,left=600,top=350,scrollbars=no,location=no,fullscreen=no', 'false');--%>
<%--                setTimeout(function () {--%>
<%--                    jQuery.ajax({--%>
<%--                        url: url,--%>
<%--                        type: "GET",--%>
<%--                        async: true,--%>
<%--                        timeout: 60000000,--%>
<%--                        success: function (res) {--%>
<%--                            res = res.trim();--%>
<%--                            let words = res.split("{");--%>
<%--                            let data = JSON.parse("{" + words[1]);--%>
<%--                            let fileName = data.fileName;--%>
<%--                            let base64File = data.base64File;--%>
<%--                            console.log("fileName：", fileName);--%>
<%--                            if (fileName.length > 0) {--%>
<%--                                let bstr = atob(base64File),--%>
<%--                                    n = bstr.length,--%>
<%--                                    u8arr = new Uint8Array(n);--%>
<%--                                while (n--) {--%>
<%--                                    u8arr[n] = bstr.charCodeAt(n);--%>
<%--                                }--%>
<%--                                let blob = new Blob([u8arr]);--%>
<%--                                console.log("blob:", blob);--%>
<%--                                let link = document.createElement('a');--%>
<%--                                link.href = window.URL.createObjectURL(blob);--%>
<%--                                link.download = fileName;--%>
<%--                                link.click();--%>
<%--                                window.URL.revokeObjectURL(link.href); // 修正这里，释放链接--%>
<%--                                mess = "<%=notesY%>";--%>
<%--                            } else {--%>
<%--                                mess = "<%=notesN%>";--%>
<%--                            }--%>
<%--                            l.close();--%>
<%--                            setTimeout(function () {--%>
<%--                                alert(mess);--%>
<%--                            }, 1000);--%>
<%--                        }--%>
<%--                    });--%>
<%--                }, 2000); // 等待 2000 毫秒后执行 Ajax 请求--%>
<%--            }--%>
<%--        </script>--%>
