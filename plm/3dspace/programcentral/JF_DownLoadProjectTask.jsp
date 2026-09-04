<%@ page import="matrix.db.JPO"%>
<%@ page import="org.apache.poi.xssf.usermodel.XSSFWorkbook" %>
<%@ page import="java.io.OutputStream" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkUtil" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@include file="emxProgramGlobals2.inc"%>
<%@include file="../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file="../emxUICommonAppInclude.inc"%>
<%@ include file = "../emxUICommonHeaderBeginInclude.inc" %>
<%@include file = "../emxUICommonHeaderEndInclude.inc" %>
<%@include file = "../common/emxUIConstantsInclude.inc"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Export All BlankMaterial</title>
</head>
<body>
<%
    boolean flag = true;
    try {
        String objectId = emxGetParameter(request, "objectId");
        String emxTableRowId = emxGetParameter(request, "emxTableRowId");
        if(UIUtil.isNullOrEmpty(objectId)){
          StringList list =   FrameworkUtil.split( emxTableRowId,"|");
            System.out.println(list.size()+"size");
          if(list.size()==4) {
              objectId =list.get(1);
          }else{
              objectId =list.get(0);
          }
        }

       /* Enumeration em = request.getParameterNames();
        while(em.hasMoreElements()){
            String strName = (String)em.nextElement();
            String [] values = request.getParameterValues(strName);
            System.out.print(strName+" = ");
            for(String str : values){
                System.out.print(str+"  ");
            }
            System.out.println();
        }*/

        String treeLabel = emxGetParameter(request, "treeLabel");
        String downloadMod = emxGetParameter(request, "download");
        System.out.println("objectId="+objectId+"=emxTableRowId"+emxTableRowId+" downloadMod="+downloadMod);
        HashMap map = new HashMap();
        map.put("id", objectId);
        XSSFWorkbook xwb = null;
        OutputStream fout = null;
        String fileName = "";

        if ("project".equalsIgnoreCase(downloadMod)) {//导出项目
            xwb = JPO.invoke(context, "JF_ProcessExcel", null, "generateProjectTask", JPO.packArgs(map), XSSFWorkbook.class);
            fout = response.getOutputStream();
            fileName =treeLabel+"-" + String.valueOf(System.currentTimeMillis()).substring(4, 13) + ".xlsx";
        }else  if ("Trim".equalsIgnoreCase(downloadMod)) {//面套模版
            xwb = JPO.invoke(context, "JF_ProcessExcel", null, "generateTrimTemplate", JPO.packArgs(map), XSSFWorkbook.class);
            fout = response.getOutputStream();
            fileName =treeLabel+"-" + String.valueOf(System.currentTimeMillis()).substring(4, 13) + ".xlsx";
        }else if ("Competitive".equalsIgnoreCase(downloadMod)) {//导出竞品BOM
                xwb = JPO.invoke(context, "JF_ProcessExcel", null, "generateCompetitiveBOM", JPO.packArgs(map), XSSFWorkbook.class);
                fout = response.getOutputStream();
                fileName = downloadMod + "-" + String.valueOf(System.currentTimeMillis()).substring(4, 13) + ".xlsx";
        }else if ("ChairManager".equalsIgnoreCase(downloadMod)) {//导出ESO任务
            xwb = JPO.invoke(context, "JF_ProcessExcel", null, "generateProjectESOTask", JPO.packArgs(map), XSSFWorkbook.class);
            fout = response.getOutputStream();
            downloadMod="ESOTask";
            fileName = downloadMod + "-" + String.valueOf(System.currentTimeMillis()).substring(4, 13) + ".xlsx";
        } else if ("PartList".equalsIgnoreCase(downloadMod)) {
            xwb = JPO.invoke(context, "JF_ProcessExcel", null, "generatePartLists", JPO.packArgs(map), XSSFWorkbook.class);
            fout = response.getOutputStream();
            downloadMod="PartList";
            DomainObject domainObject = DomainObject.newInstance(context, objectId);
            String name = domainObject.getInfo(context, DomainConstants.SELECT_NAME);
            fileName = downloadMod + "_" + name + "-" + String.valueOf(System.currentTimeMillis()).substring(4, 13) + ".xlsx";
        } else {//后面按需增加标识
            xwb = JPO.invoke(context, "JF_ProcessExcel", null, "generateCompetitiveBOM", JPO.packArgs(map), XSSFWorkbook.class);
            fout = response.getOutputStream();
            fileName =treeLabel+"-" + String.valueOf(System.currentTimeMillis()).substring(4, 13) + ".xlsx";
        }
            String headStr = "attachment; filename=\"" + fileName + "\"";
            response.setContentType("APPLICATION/OCTET-STREAM");
            response.setHeader("Content-Disposition", headStr);
            xwb.write(fout);
            fout.flush();
            fout.close();
    }catch (IOException e) {
        e.printStackTrace();
     String message = e.getMessage();
%>
<script>
    alert("Export error"+"<%=message%>");
</script>
<%
    }finally {
        out.clear();
        out = pageContext.pushBody();
    }

    System.out.println("flag="+flag);
if(flag){
%>
<script>
    alert("Export Success");
</script>
<%
    }else{
%>
<script>
    alert("Export faile");
</script>
<%
    }
%>
</body>
</html>