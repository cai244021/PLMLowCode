<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.apache.commons.text.StringEscapeUtils" %>
<%@ page import="com.matrixone.apps.domain.util.PersonUtil" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %><%--  emxTreeRefresh.jsp
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
    System.out.println("strObjId---->"+strObjId);
    System.out.println("strInputId---->"+strInputId);
    System.out.println("selectedBuildIds---->"+selectedBuildIds[0]);
    StringBuffer sb = new StringBuffer();
    try {
        for(int i=0;i<selectedBuildIds.length;i++){
            String personid=selectedBuildIds[i];
            System.out.println("personid---->"+personid);
            String fullname="";
            String owner="";
            String owner2="";
            try {
                ContextUtil.pushContext(context);


                DomainObject personObj=DomainObject.newInstance(context,personid);
                owner=personObj.getName(context);
                owner2=personObj.getOwner(context).getName();

                System.out.println("owner-l-->"+owner);
                System.out.println("owner-l-->"+owner2);

                fullname=PersonUtil.getFullName(context,owner);
                System.out.println("fullname--->"+fullname);

            }catch (Exception e2){
                e2.printStackTrace();
            }finally {
                ContextUtil.popContext(context);
            }


            sb.append(fullname);
            DomainObject pcrobj=DomainObject.newInstance(context,strObjId);
            pcrobj.setAttributeValue(context,"JF_PCREvaluationInformResponsible",owner);
        }
    }catch (Exception e){
        e.printStackTrace();
    }

    strShowData = sb.toString();

  %>

<!DOCTYPE html>

<html>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<script language="javascript" type="text/javascript" src="../common/scripts/emxUICore.js"></script>
<script language="Javascript">
<%--alert(<%=strInputId%>);--%>
//var filter = parent.window.document.getElementById('DSFAttribute2FilterBox')
//     window.close();
    filter = window.parent.getTopWindow().getWindowOpener().document.getElementById('<%=strInputId%>');
<%--filter.value='<%=strShowData%>';--%>
    filter.innerHTML = '';
    const opt = document.createElement('option');
    opt.value = '<%=strShowData%>';
    opt.innerHTML = '<%=strShowData%>';
    filter.appendChild(opt);
    debugger;
    let href = window.parent.location.href;
    if(href.includes("SearchUI")){
        window.parent.close();
    }else {
        window.close();
    }
</script>

</body>
<%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
</html>
