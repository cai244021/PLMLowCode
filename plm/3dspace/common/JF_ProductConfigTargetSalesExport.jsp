<%@ page import="matrix.db.JPO" %>
<%@ page import="java.io.OutputStream" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.apache.poi.xssf.usermodel.XSSFWorkbook" %>
<%@include file = "./emxNavigatorNoDocTypeInclude.inc"%>
<%
    if (out != null) {
        out.clear();
        out = null;
    }
    String objectId = emxGetParameter(request, "objectId");
    HashMap params = new HashMap();
    params.put("objectId", objectId);
    XSSFWorkbook workbook = null;
    try {
        workbook = (XSSFWorkbook) JPO.invoke(context, "JF_ProductConfig", null, "exportTargetSalesSummaryExcel", JPO.packArgs(params), XSSFWorkbook.class);
        jakarta.servlet.http.Cookie statusCookie = new jakarta.servlet.http.Cookie("JFProductConfigTargetSalesExportStatus", "success");
        statusCookie.setPath("/");
        statusCookie.setMaxAge(120);
        statusCookie.setHttpOnly(false);
        response.addCookie(statusCookie);
        String fileName = "TargetSalesSummary_" + System.currentTimeMillis() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8").replace("+", "%20") + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        outputStream.flush();
    } catch (Exception e) {
        String message = e.getMessage() == null ? "Export failed." : e.getMessage();
        jakarta.servlet.http.Cookie statusCookie = new jakarta.servlet.http.Cookie("JFProductConfigTargetSalesExportStatus", URLEncoder.encode("error:" + message, "UTF-8"));
        statusCookie.setPath("/");
        statusCookie.setMaxAge(120);
        statusCookie.setHttpOnly(false);
        response.addCookie(statusCookie);
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().write(message);
    } finally {
        if (workbook != null) {
            workbook.close();
        }
    }
%>
