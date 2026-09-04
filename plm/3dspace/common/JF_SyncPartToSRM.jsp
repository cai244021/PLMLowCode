<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page pageEncoding="utf-8" %>
<%@include file = "emxNavigatorInclude.inc"%>
<script src="./scripts/emxUIModal.js" type="text/javascript"></script>
<script language="JavaScript" src="./scripts/emxUICore.js"></script>
<script language="JavaScript" src="./scripts/emxUIConstants.js"></script>
<script language="JavaScript" src="./scripts/emxUISearch.js"></script>
<script language="JavaScript" src="../webapps/AmdLoader/AmdLoader.js"></script>
<script type="text/javascript" src="../common/scripts/jquery-latest.js"></script>


<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_SyncPartToSRM.jsp");
%>
<%
    String strObjectId = (String)emxGetParameter(request, "objectId");
%>
<html>
<script>

    customerConfirm("提示信息","是否确定将当前Partlist同步至SRM系统？");

    function syncMbomToMDM(){
        const args = { ID: '<%=strObjectId%>'};
        console.log("JSON.stringify(args)::", JSON.stringify(args));
        $.ajax({
            async :true ,
            url: '../TWXPublicRest/TWXTicketService?JPOName=JF_DataInterface&FuncName=syncPartListToSRM',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(args),
            success: function(res) {
                console.log("res::",res)
                if (res.code ==200){
                    alert("同步至SRM已完成。")
                }else {
                    alert("同步失败:"+res.msg);
                }

            },
            error: function(res) {
                alert("同步失败:"+res.msg);
            },
        });
    }
    function customerConfirm(title, message) {
        require(["DS/UIKIT/SuperModal"], function (SuperModal) {
            var env = window === window.top ? window : window.top,
                superModal;
            superModal = new SuperModal({ renderTo: env.document.body });
            superModal.confirm({
                title: title,
                message: message.split('. ').join('.\n'),
                callback: function (confirmed) {
                    if (confirmed) {
                        alert("同步需要5到10分钟 请稍等。。。")
                        syncMbomToMDM();
                    }else{
                        //do nothing
                        console.log("点击取消")
                    }
                }
            });
            var container = superModal.modals.current.elements.container;
            container.setStyle('white-space', 'pre-line'); // 保持换行
            container.setStyle('font-weight', 'bold'); // 设置字体加粗

        });
    }
</script>
</html>