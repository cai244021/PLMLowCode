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
    String objectId = emxGetParameter(request, "objectId");
    String[] tableRowIds = emxGetParameterValues(request, "emxTableRowId");
    StringList tableRowIdList = StringList.create(tableRowIds);
    StringList selectPartList = new StringList();
    //判断如果选择的MBOM结构中包含了，相同零件和相同版本的数据弹出提示
    for (int i = 0; i < tableRowIdList.size(); i++) {
        String strId = (String) tableRowIdList.get(i);
        Map rowMap = ProgramCentralUtil.parseTableRowId(context, strId);
        strId = (String) rowMap.get("objectId");
        selectPartList.add(strId);
    }
    //判断 选择MBOM零件的Owner是不是当前登录人
    DomainObject vpmobj = DomainObject.newInstance(context);
    String loginUser=context.getUser();
    StringList errorOwnerList = new StringList();
    StringList errorSameList = new StringList();
    String errorMsg = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.common.CreateMbomPartAndConnectErrorMsg");
    String sameMsg = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.common.MbomPartSameErrorMsg");
    StringList numberRevList = new StringList();
    for(String vpmid:selectPartList){
        vpmobj =DomainObject.newInstance(context,vpmid);
        String owner=vpmobj.getOwner(context).getName();
        String strPartNumber = vpmobj.getAttributeValue(context, "JF_PartNumber");
        String rev = vpmobj.getInfo(context, DomainConstants.SELECT_REVISION);
        String key = strPartNumber + "_" + rev;
        if(!loginUser.equals(owner)){
            //当前所勾选数据无编辑权限，请重新选择！
            errorOwnerList.add(strPartNumber);
        }
        if (numberRevList.contains(key)) {
            errorSameList.add(strPartNumber);
        } else {
            numberRevList.add(key);
        }
    }
    if (!errorOwnerList.isEmpty()) {
        errorMsg = errorOwnerList.join(",") + errorMsg;
%>
<script language="Javascript">
    alert("<%=errorMsg%>");
</script>
<%
        return;
    }
    if (!errorSameList.isEmpty()) {
        sameMsg =  sameMsg.replaceAll("1", errorSameList.join(","));
%>
<script language="Javascript">
    alert("<%=sameMsg%>");
</script>
<%
        return;
    }
    StringBuffer sbUrl = new StringBuffer("JF_ProjectMBOMAddMakeVPMDialogFS.jsp");
    sbUrl.append("?objectId=");
    sbUrl.append(objectId);
    sbUrl.append("&selectParts=");
    sbUrl.append(selectPartList.join(","));
%>
<html>
<script>
    window.open ("<%=sbUrl%>", '', 'width=500,height=250,left=600,top=350,scrollbars=no,location=no,fullscreen=no','false');
</script>
</html>
