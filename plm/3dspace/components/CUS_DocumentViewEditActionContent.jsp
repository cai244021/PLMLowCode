<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="java.util.logging.Logger" %>
<%--  .jsp   -  Dialog page for Manual-Stop functionality

   Copyright (c) 1992-2018 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne, Inc.
   Copyright notice is precautionary only and does not evidence any actual or intended
   publication of such program.

--%>
<html>
<%@page pageEncoding="utf-8" %>

<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../common/emxUIConstantsInclude.inc"%>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>
<script type="text/javascript" src="../common/scripts/emxUICore.js"></script>
<%
    Logger _logger = Logger.getLogger("cus_DocumentViewEditActionContent.jsp");
    String strDocId = emxGetParameter(request, "objectId");
    //编辑or显示
    String modeAction = emxGetParameter(request, "modeAction");
    if(UIUtil.isNullOrEmpty(modeAction)){
        modeAction="";
    }

    boolean anShowEditButton=false;
    String currentUser=context.getUser();

    DomainObject docObj=DomainObject.newInstance(context,strDocId);
    String docCurrent=  docObj.getInfo(context,DomainObject.SELECT_CURRENT);
    String docOwner=  docObj.getInfo(context,DomainObject.SELECT_OWNER);
           /* policy[Document Release].state = PRIVATE
            policy[Document Release].state = IN_WORK*/

    if("PRIVATE".equalsIgnoreCase(docCurrent)||"IN_WORK".equalsIgnoreCase(docCurrent)){
        // action=mode_edit;
        if(currentUser.contains("admin_")||currentUser.equalsIgnoreCase(docOwner)){
            modeAction="edit";
            anShowEditButton=true;
        }
    }

    String routeId = emxGetParameter(request, "routeId");
    _logger.info("cus_DocumentViewEditActionContent.jsp routeId>>>>>>>>>>>"+routeId);
    _logger.info("cus_DocumentViewEditActionContent.jsp modeAction>>>>>>>>>>>"+modeAction);
    //DomainObject doDocObj=DomainObject.newInstance(context,strDocId);
	//有下载动作
   MapList valML= (MapList) JPO.invoke(context, "CUS_OnlyOfficeOnlineAction", new String[]{strDocId,modeAction}, "getDocOnLineServicePath", new String[]{strDocId,modeAction}, MapList.class);
    //无下载动作 只查找批注文件 给显示权限
	//MapList valML= (MapList) JPO.invoke(context, "Aer_OnlyOfficeOnlineAction", new String[]{strDocId,modeAction}, "getDocObjViewServicesPath", new String[]{strDocId,routeId,modeAction}, MapList.class);
//    valML.addAll(valML);
//    valML.addAll(valML);
   
	String onlineDocLink= "";
    //容器服务文件名称
    String onlineDocFileName="";
    //容器务文件名称对应3de名称；
    String file3deName="";

    if(valML!=null&&valML.size()>0){
        onlineDocLink= (String) (((Map)valML.get(0)).get("onlineLink"));
        file3deName= (String) (((Map)valML.get(0)).get("file3deName"));
        onlineDocFileName= (String)(((Map)valML.get(0)).get("onlineDocFileName"));


    %>
<body>
<style type="text/css">
    html,
    body {
        width: 100%;
        height: 100%;
        margin: 0;
        overflow: hidden;
        font-family: Arial, "Microsoft YaHei", sans-serif;
        background: #f4f6f8;
    }

    #content {
        position: fixed;
        top: 0;
        right: 300px;
        bottom: 0;
        left: 0;
        background: #f4f6f8;
        transition: right 0.2s ease;
    }

    body.sidebar-collapsed #content {
        right: 40px;
    }

    #InlineFrameDoc {
        display: block;
        width: 100%;
        height: 100%;
        border: 0;
    }

    #div1 {
        position: fixed;
        top: 0;
        right: 0;
        width: 300px;
        height: 100%;
        box-sizing: border-box;
        background: #ffffff;
        border-left: 1px solid #d8dde6;
        box-shadow: -8px 0 24px rgba(15, 23, 42, 0.12);
        z-index: 20;
        display: flex;
        flex-direction: column;
        cursor: default;
        transition: width 0.2s ease;
    }

    .drawer-header {
        height: 48px;
        padding: 0 12px 0 16px;
        border-bottom: 1px solid #e6e9ef;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 8px;
        background: #f8fafc;
    }

    .drawer-title {
        color: #1f2937;
        font-size: 15px;
        font-weight: 600;
        white-space: nowrap;
    }

    #ButtonID {
        min-width: 52px;
        height: 28px;
        border: 1px solid #c8d2df;
        border-radius: 4px;
        background: #ffffff;
        color: #2563a7;
        font-size: 13px;
        line-height: 26px;
        text-align: center;
        text-decoration: none;
        cursor: pointer;
        box-sizing: border-box;
    }

    #ButtonID:hover {
        background: #eef6ff;
        border-color: #8cb8df;
    }

    #Show {
        flex: 1;
        padding: 14px 14px 16px;
        overflow-y: auto;
        display: flex;
        flex-direction: column;
        gap: 10px;
    }

    .file-action-row {
        display: flex;
        align-items: center;
        gap: 8px;
        min-height: 36px;
    }

    .file-switch-button {
        flex: 1;
        min-width: 0;
        height: 34px;
        padding: 0 10px;
        border: 1px solid #cfd8e3;
        border-radius: 4px;
        background: #f8fbff;
        color: #1f5f99;
        font-size: 13px;
        font-weight: 600;
        text-align: left;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
        cursor: pointer;
    }

    .file-switch-button:hover {
        background: #eaf4ff;
        border-color: #83b5df;
    }

    .new-window-link {
        width: 32px;
        height: 32px;
        border: 1px solid #cfd8e3;
        border-radius: 4px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        background: #ffffff;
        cursor: pointer;
        flex: 0 0 auto;
    }

    .new-window-link:hover {
        background: #eef6ff;
        border-color: #83b5df;
    }

    .new-window-link img {
        width: 16px;
        height: 16px;
    }

    .save-area {
        margin-top: auto;
        padding-top: 12px;
        border-top: 1px solid #e6e9ef;
    }

    .save-button {
        width: 100%;
        height: 36px;
        border: 1px solid #1c6ea4;
        border-radius: 4px;
        background: #2378ad;
        color: #ffffff;
        font-size: 14px;
        font-weight: 600;
        cursor: pointer;
    }

    .save-button:hover {
        background: #1c6593;
    }

    body.sidebar-collapsed #div1 {
        width: 40px;
    }

    body.sidebar-collapsed .drawer-header {
        height: 100%;
        padding: 8px 5px;
        border-bottom: 0;
        justify-content: center;
        align-items: flex-start;
    }

    body.sidebar-collapsed .drawer-title,
    body.sidebar-collapsed #Show {
        display: none;
    }

    body.sidebar-collapsed #ButtonID {
        min-width: 28px;
        width: 28px;
        height: auto;
        padding: 8px 0;
        line-height: 16px;
        writing-mode: vertical-rl;
    }
</style>
<script>

//创建iframe
    function createIframe(src) {
        closeIframe();
        var iframe = document.createElement("iframe");
        //传入iframe的src
        iframe.src = "";
        iframe.id = "InlineFrameDoc";
        iframe.width = "100%";
        iframe.height = "100%";
        document.getElementById("content").appendChild(iframe);
        console.log("创建iframe完成");
    }
    //点击事件
    function headerClick(id) {
    closeIframe();
    createIframe(id);
    }
    //删除iframe
    function closeIframe(){

        let iframe = document.getElementById("InlineFrameDoc");
        if(iframe!=null){

        iframe.src = "about:blank";
        iframe.contentWindow.document.write('')
        iframe.contentWindow.document.clear();
        iframe.contentWindow.close();
        iframe.parentNode.removeChild(iframe);
        iframe = null;
        }
    }
     function ChangeIframe(src,onlineDoc,file3deDoc){

        let iframe = document.getElementById("InlineFrameDoc");
        if(iframe!=null){
<%--      // var Isrc = "https://oos.v6.com/we/wordeditorframe.aspx?WOPISrc=http%3A%2F%2FAD.v6.com%3A80%2Fwopi%2Ffiles%2Ftest3DPlayDOCX2.docx&access_token=&access_token_ttl=0&wdPid=77133374&wdModeSwitchTime=1696741465915&wdPreviousSession=d1165f9d-b79c-4116-95ec-33e5ff4b5e81";--%>
        iframe.src = src;



<%--        iframe.contentWindow.document.write('')--%>
<%--        iframe.contentWindow.document.clear();--%>
<%--        iframe.contentWindow.close();--%>
<%--        iframe.parentNode.removeChild(iframe);--%>
<%--        iframe = null;--%>
        }

          var onlineDocFileName = document.getElementById("onlineDocFileName");
         // alert("onlineDocFileName"+onlineDocFileName.value);
          if(onlineDocFileName!=null){
          onlineDocFileName.value=onlineDoc;
          }

          var file3deNameVar = document.getElementById("file3deName");
         //  alert("file3deNameVar"+file3deNameVar.value);
          if(file3deNameVar!=null){
          file3deNameVar.value=file3deDoc;
          }




    }

    function openUrlShowPage(srcLink,onlineDoc,file3deDoc){
     //设置新窗口的特性
    var features = "height=500, width=800, top=100, left=100, toolbar=no, menubar=no,scrollbars=no,resizable=no, location=no, status=no";
   // window.open (URL, name, features, replace)

      window.open (srcLink, '_blank', 'width=1000,height=1000','false');

    }


    //点击按钮事件
    function headerClick(){
        var Isrc = "";
        console.log("Isrc"+Isrc);
         createIframe(Isrc);
    }

//保存回3de
function saveClick(){
    var oid="<%=strDocId%>";
   // var fileName="<%=onlineDocFileName%>";

    var file3deName = document.getElementById("file3deName").value;

    var onlineDocFileName = document.getElementById("onlineDocFileName").value;

    if(confirm("请确保先Ctrl+S对文件进行保存\n对文件'"+file3deName+"' 是否确定进行保存到PLM?")){
        file3deName=encodeURIComponent(file3deName);
        onlineDocFileName=encodeURIComponent(onlineDocFileName);
	$.ajax({
						url: '../components/CUS_onlineDocSaveProcess.jsp?objectId='+oid+'&file3deName='+file3deName+'&onlineDocFileName='+onlineDocFileName,
						type: 'POST',
						cache: false,
						contentType: false,
						processData: false,
						async:false,
						success:function(data){

							if(data!=null&&data.indexOf("success")>0){
							alert("保存成功！");
							}else{
							  alert("保存失败，请联系管理员！");
							}
							//var newIdData=data.substring(data.indexOf("flags@")+6,data.indexOf("@newId"));
							//window.open("../common/emxNavigator.jsp?objectId="+newIdData)
				        },
				         error:function(i,e,m){

					     alert("保存失败，请联系管理员！");

				        }
		});
   }
   }

      //点击事件
    function nextAlert(id) {
     alert("点击文件名称切换内容");
    }


function Display(id,name) {
    var isCollapsed = document.body.classList.toggle("sidebar-collapsed");
    var button = document.getElementById(name);
    if (button != null) {
        button.innerText = isCollapsed ? "展开" : "隐藏";
        button.title = isCollapsed ? "展开文件操作栏" : "隐藏文件操作栏";
    }
}

// js代码 拖拽
window.onload=function() {
    // Right drawer is fixed; keep legacy drag function unused.
};
    function  drag(divId){

            var disX = disY = 0;                         // 鼠标距离div的左距离和上距离
            var div1 = document.getElementById(divId);  // 得到div1对象

            // 鼠标按下div1时
            div1.onmousedown = function(e) {
                var evnt = e || event;                   // 得到鼠标事件
                disX = evnt.clientX - div1.offsetLeft;   // 鼠标横坐标 - div1的left
                disY = evnt.clientY - div1.offsetTop;    // 鼠标纵坐标 - div1的top

                // 鼠标移动时
                document.onmousemove = function(e) {
                    var evnt = e || event;
                    var x = evnt.clientX - disX;
                    var y = evnt.clientY - disY;
                    var window_width  = document.documentElement.clientWidth  - div1.offsetWidth;
                    var window_height = document.documentElement.clientHeight - div1.offsetHeight;

                    x = ( x < 0 ) ? 0 : x;                          // 当div1到窗口最左边时
                    x = ( x > window_width ) ? window_width : x;    // 当div1到窗口最右边时
                    y = ( y < 0 ) ? 0 : y;                          // 当div1到窗口最上边时
                    y = ( y > window_height ) ? window_height : y;  // 当div1到窗口最下边时

                    div1.style.left = x + "px";
                    div1.style.top  = y + "px";
                };

                // 鼠标抬起时
                document.onmouseup = function() {
                    document.onmousemove =null;
                    document.onmouup = null;
                };

                return false;
            };

    }

</script>
</br>

<div class="form-group"></div>


<div id="div1">
    <div class="drawer-header">
        <span class="drawer-title">文件操作</span>
        <a onclick="Display('Show','ButtonID')" id="ButtonID" title="隐藏文件操作栏">隐藏</a>
    </div>

    <div id="Show" class="panel table-responsive">
        <%--保存按钮暂时 隐藏 --%>
   <%--  <a onclick="saveClick();"><img src="../components/images/cus_save.png" title="保存文件"/></a>--%>
        <%
            for (int i = 0; i <valML.size() ; i++) {
                Map valMap= (Map) valML.get(i);
                String onlineDocLinkStr= (String) valMap.get("onlineLink");
                String  file3deNameStr= (String) valMap.get("file3deName");
                String  file3deNameTitle= XSSUtil.encodeForHTMLAttribute(context, file3deNameStr);
                String onlineDocFileNameStr= (String)valMap.get("onlineDocFileName");
 /*           if(i!=0&&i%2==0){

        }*/
        %>

        <div class="file-action-row">
            <button class="file-switch-button" title="<%=file3deNameTitle%>" onclick="ChangeIframe('<%=onlineDocLinkStr%>','<%=onlineDocFileNameStr%>','<%=file3deNameStr%>');"><%=file3deNameStr%></button>
          <%--
            <button style="font-size:18px; background-color: #96c0d9; color: snow;  height: 30px;" onclick="openUrlShowPage('<%=onlineDocLinkStr%>','<%=onlineDocFileNameStr%>','<%=file3deNameStr%>');">New Windows</button>
--%>
            <a class="new-window-link" title="新窗口打开" onclick="openUrlShowPage('<%=onlineDocLinkStr%>','<%=onlineDocFileNameStr%>','<%=file3deNameStr%>');"><img src="../common/images/iconActionNewWindow.png" alt="New Windows"></a>
        </div>
<%--请注意：保存office后,上传文件至平台!!!
        &nbsp;&nbsp;<button style="font-size:18px; background-color: #96c0d9; color: snow;  height: 30px;" onclick="ChangeIframe('<%=onlineDocLinkStr%>','<%=onlineDocFileNameStr%>','<%=file3deNameStr%>');"><%=file3deNameStr%></button>
--%>

          <%
            }
        %>

            <%
                if(anShowEditButton){
            %>

            <div class="save-area">
                <%--<button style="font-size:18px; background-color:#96c0d9; color: #e54f4f;  height: 32px;" onclick="saveClick()" >请注意：保存office后,上传文件至平台!!!</button>--%>
                <button class="save-button" onclick="saveClick()" >保存到PLM</button>
            </div>
            <%

                }
            %>


    </div>
</div>


<div id="content" >
    <iframe
            id="InlineFrameDoc"
            title="word online"
            width="100%"
            height="100%"
            src="<%=onlineDocLink%> ">
        <%--        src="https://oos.v6.com/we/wordeditorframe.aspx?WOPISrc=http%3A%2F%2FAD.v6.com%3A80%2Fwopi%2Ffiles%2Ftest3DPlayDOCX2.docx&access_token=&access_token_ttl=0&wdPid=77133374&wdModeSwitchTime=1696741465915&wdPreviousSession=d1165f9d-b79c-4116-95ec-33e5ff4b5e81">--%>

    </iframe>
</div>


<input type="hidden" name="file3deName"  id="file3deName"  value="<%=file3deName%>" />
<input type="hidden" name="onlineDocFileName"  id="onlineDocFileName"  value="<%=onlineDocFileName%>" />

</br>
</body>
<%
}else{
    %>
<script>
    alert("不支持此文件格式!!!");
    window.close();
</script>
    <%
}


%>


</html>
