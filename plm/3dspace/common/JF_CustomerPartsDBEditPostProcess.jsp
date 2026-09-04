<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" %>
<%@include file="../emxUICommonAppInclude.inc"%>
<script src="../common/scripts/emxUICore.js" type="text/javascript"></script>
<script>
    /* 重新加载客户零件号/DB表格，确保项目名称、客户等冻结列同步刷新 */
    var tableWin = findFrame(getTopWindow(), "detailsDisplay");
    if (tableWin) {
        var refreshUrl = tableWin.location.href;
        if (refreshUrl.indexOf("persist=true") > -1) {
            refreshUrl = refreshUrl.replace("persist=true", "persist=false");
        }
        tableWin.location.href = refreshUrl;
    }
    /* 关闭右侧编辑表单 */
    getTopWindow().closeSlideInDialog();
</script>
