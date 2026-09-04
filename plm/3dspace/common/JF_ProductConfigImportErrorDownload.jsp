<%@ page import="java.io.File" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.io.OutputStream" %>
<%@ page import="java.net.URLEncoder" %>
<%@include file = "./emxNavigatorNoDocTypeInclude.inc"%>
<%
    if (out != null) {
        out.clear();
        out = null;
    }
    String token = emxGetParameter(request, "token");
    String path = token == null ? null : (String) session.getAttribute("JFProductConfigImportError." + token);
    if (path == null) {
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().write("Error Excel expired.");
        return;
    }
    File file = new File(path);
    if (!file.exists()) {
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().write("Error Excel expired.");
        return;
    }
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode("ProductConfigImportErrors.xlsx", "UTF-8").replace("+", "%20") + "\"");
    FileInputStream inputStream = new FileInputStream(file);
    OutputStream outputStream = response.getOutputStream();
    byte[] buffer = new byte[8192];
    int length;
    try {
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.flush();
    } finally {
        inputStream.close();
    }
%>
