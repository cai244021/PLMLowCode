<%--<!DOCTYPE html>--%>
<%--<html>--%>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.apache.poi.ss.usermodel.Workbook" %>
<%@ page import="jakarta.servlet.ServletOutputStream" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="java.util.Base64" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.util.MatrixException" %>
<%--登录检查--%>
<%@include file = "emxNavigatorInclude.inc"%>
<%!
    private static final Logger _logger = LoggerFactory.getLogger("JF_CostHistoryDownLoad");
%>
<%
    Gson gson = new Gson();
//    out.clear();
    //获取请求路径
    String strPath = request.getRealPath("");
    Map res = new HashMap();
//    String[] tableRowIdList = emxGetParameterValues(request, "emxTableRowId");
//    System.out.println("tableRowIdList:"+ tableRowIdList);
    String strProjectIds = (String)emxGetParameter(request, "projectIds");
    _logger.info("strProjectIds:{}",strProjectIds);
    String[] projectIdArr = strProjectIds.split(",");

    // 设置响应头
//    response.setContentType("application/octet-stream");
    try {
        for (String strProjectId : projectIdArr) {
//            StringList tableIds = FrameworkUtil.split(tableRowId,"|");
//            String strProjectId =  tableIds.get(0);
            res = JPO.invoke(context, "JF_NewECRService", null, "projectCostingDownLoad", new String[]{strProjectId,strPath}, Map.class);
            Workbook workbook =  (Workbook)res.get("file");
            //add by chenyan 修改为ajax下载
            // 处理文件名编码
    //        strFileName = URLEncoder.encode(strFileName, "UTF-8").replaceAll("\\+", "%20");
    //        // 设置 Content-Disposition 头
    //        StringBuffer sb = new StringBuffer();
    //        sb.append( "attachment; filename=\"");
    //        sb.append(strFileName);
    //        sb.append("\"; filename*=UTF-8''");
    //        sb.append(strFileName);
    //        response.setHeader("Content-Disposition",sb.toString());
            // 创建一个字节输出流
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            //转换为字节流数组
            byte[] bFileArr = bos.toByteArray();
            String strB64 = Base64.getEncoder().encodeToString(bFileArr);
            res.put("base64File",strB64);
            //清空workbook`
            res.put("file",null);
            res.put("flag", "Y");
        }
    } catch (MatrixException e) {
        res.put("flag", "N");
        throw new RuntimeException(e);
    }
    System.out.println("#####################################################################");
    System.out.println("#####################################################################");
    System.out.println("#####################################################################");
    System.out.println("#####################################################################");
    System.out.println("#####################################################################");

%>
<%=gson.toJson(res)%>

