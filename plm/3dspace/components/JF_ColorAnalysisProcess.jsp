<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.apache.commons.text.StringEscapeUtils" %><%--  emxTreeRefresh.jsp
   Copyright (c) 1992-2020 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,Inc.
   Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

      static const char RCSID[] = $Id: emxTreeRefresh.jsp.rca 1.5 Wed Oct 22 15:47:53 2008 przemek Experimental przemek $
--%>

<%@include file = "../common/emxNavigatorNoDocTypeInclude.inc"%>
<%
 String strObjId = emxGetParameter(request, "objectId");
 String strInputId = emxGetParameter(request, "inputId");
    //获取search页面选中的数据
    String strContextObjectId[] = emxGetParameterValues(request,"emxTableRowId");
    //选中数据oid
    String[]  selectedBuildIds = new String[strContextObjectId.length];

    String strShowData = "";
    for(int i=0; i<strContextObjectId.length; i++){
        StringTokenizer strTokenizer = new StringTokenizer(strContextObjectId[i] ,"|");
        selectedBuildIds[i] = (String) strTokenizer.nextElement();
    }
    //根据传入参数不同，返回不同的查询结果
    StringBuffer sb = new StringBuffer();
    for(int i=0;i<selectedBuildIds.length;i++){
        DomainObject obj = DomainObject.newInstance(context,selectedBuildIds[i]);
        String strAppendData = "";
        //颜色风格   数据回显
        strAppendData = obj.getInfo(context, "attribute[Title]");
        sb.append(StringEscapeUtils.escapeHtml4(strAppendData));
        //处理多选以逗号分割
    }
    strShowData = sb.toString();
  %>

<!DOCTYPE html>

<html>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<script language="javascript" type="text/javascript" src="scripts/emxUICore.js"></script>
<script language="Javascript">
<%--alert(<%=strInputId%>);--%>
//var filter = parent.window.document.getElementById('DSFAttribute2FilterBox')
//     getTopWindow().close();
window.parent.close();
// window.style.display = 'none'; // 隐藏自己
    // window.close();
    filter = window.parent.getTopWindow().getWindowOpener().document.getElementById('<%=strInputId%>');
    filter.value='<%=strShowData%>';
</script>

</body>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
</html>
