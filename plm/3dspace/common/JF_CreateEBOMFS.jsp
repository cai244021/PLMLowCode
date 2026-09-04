<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<%@ include file="../programcentral/emxProgramGlobals2.inc" %>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../common/enoviaCSRFTokenValidation.inc"%>
<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<%!
    private static final Logger NIO_LOG = LoggerFactory.getLogger("JF_CreateEBOMFS.jsp");
%>
<%
    NIO_LOG.info("EBOM FS");
    Enumeration em = request.getParameterNames();
    while(em.hasMoreElements()){
        String strName = (String)em.nextElement();
        String [] values = request.getParameterValues(strName);
        System.out.print(strName+" = ");
        for(String str : values){
            System.out.print(str+"  ");
        }
        System.out.println();
    }
    String parentOID = emxGetParameter(request, "parentOID");
    String[]	objectIds = emxGetParameterValues(request, "emxTableRowId");
    StringList pcList = new StringList();
    for(int i=0; i < objectIds.length; i++)
    {
        StringList sList = FrameworkUtil.split(objectIds[i],"|");
        if(sList.size() == 1 || sList.size() == 2)
            pcList.add((String)sList.get(0));
        else if(sList.size() == 3)
            pcList.add((String)sList.get(0));
        else if(sList.size() == 4)
            pcList.add((String)sList.get(1));
    }
   String pcStr =  pcList.join(",");
    NIO_LOG.info("parentOID:{} pcStr :{}",parentOID,pcStr);
    %>
<html>
<body>

<script>
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
    overlay.innerHTML = '<div style="color: white; font-size: 20px;"><%=XSSUtil.encodeForHTML(context, "\u6B63\u5728BOM\u521B\u5EFA\u53CA\u66F4\u65B0\uFF0C\u8BF7\u52FF\u91CD\u590D\u70B9\u51FB...")%></div>';
    document.body.appendChild(overlay);
    // 禁用当前按钮
    // button.disabled = true;
    //创建 FormData 对象
/*    var formData = new FormData();
    formData.append('parentOID', 'sss'); // 文本参数
    formData.append('emxTableRowId', 'V2.1'); // 另一个文本参数
    console.log(formData);*/
    const pcStr = '<%=pcStr%>';
    const parentOID = '<%=parentOID%>';
    var url = "JF_CreateEBOMProcess.jsp?parentOID="+parentOID+"&pcList="+pcStr;
    // 使用 fetch 发送数据到后端
   /* fetch(url)
        .then(response => response.json())
        .then(result => {
            debugger;
            const  code=result.code;
            const  message=result.message;
            if (code==="200"){
                alert("successfully");
            }else {
                alert(message);
            }
            // 刷新页面
            // getTopWindow().getWindowOpener().refreshSBTable(getTopWindow().getWindowOpener().configuredTableName);
            // getTopWindow().closeWindow();
            // window.top.getWindowOpener().top.refreshTablePage();
            // window.parent.opener.location.reload();
            // window.top.close();

        })
        .catch(err => {
            debugger;
            alert("fail");
            // 刷新页面
            console.log("error-->", err);
     /!*       getTopWindow().closeWindow();
            window.top.getWindowOpener().top.refreshTablePage();
            window.parent.opener.location.reload();
            window.top.close();*!/
        });*/
    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            // 先以文本形式读取响应
            return response.text();
        })
        .then(text => {
            console.log('原始响应:', text); // 在控制台查看完整响应内容
            // 尝试手动去除首尾可能的空白字符后解析
            const trimmedText = text.trim();
            console.log('原始响应:', trimmedText); // 在控制台查看完整响应内容
            try {
              if(trimmedText.includes("500")){
                alert('create fail,Please Contact administrator!!!');
              }else{
                  alert('Create/update Success');
                  getTopWindow().closeWindow();
                  window.top.getWindowOpener().top.refreshTablePage();
                  window.parent.opener.location.reload();
                  window.top.close();
              }
            } catch (e) {
                console.error('手动解析 trimmed 文本仍出错:', e);
            }
        })
        .catch(err => {
            console.error('请求失败:', err);
        });

</script>
</body>
</html>