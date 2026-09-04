
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="matrix.util.MatrixException" %>

<%@include file = "../common/emxNavigatorInclude.inc"%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_AddEsoReviewRecord.jsp");
%>
<%
    String strObjectId = emxGetParameter(request, "objectId");
    String flag = emxGetParameter(request, "flag");
    JF_LOGGER.info("JF_AddEsoReviewRecord.jsp----strObjectId::{}",strObjectId);
    JF_LOGGER.info("JF_AddEsoReviewRecord.jsp----flag::{}",flag);
    try {
        JPO.invoke(context, "JF_ESO", null, "createESOReviewRecord", new String[]{strObjectId}, Void.class);
    } catch (MatrixException e) {
        JF_LOGGER.info("JF_AddEsoReviewRecord.jsp----error::",e);
    }
%>
<html>
<script>
    // findFrame(getTopWindow(),"JFRapidOfferModuleListCmd").emxEditableTable.refreshStructure()
    // window.location.reload()
    // parent.window.onload = function() {
    //     // 假设你要触发的元素的 id 是 "editButttonId"
    //     var element = parent.document.getElementById("editButttonId");
    //     console.log("element::",element)
    //
    //     if (element) {
    //         // 创建一个点击事件
    //         var clickEvent = new Event('click');
    //         // 触发点击事件
    //         element.dispatchEvent(clickEvent);
    //     }
    // };
    if ("JFAddReviewRecordCmd"==="<%=flag%>"){
        // window.top.getWindowOpener().top.refreshTablePage();
        parent.findFrame(parent.getTopWindow(),"JFESORecordCmd").refreshSBTable()
        // parent.findFrame(parent.getTopWindow(),"JFESORecordCmd").emxEditableTable.addToSelected()

    }else {
        parent.window.location.reload();
    }

</script>
</html>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>

