<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.io.OutputStream" %>
<%@ page import="org.apache.poi.ss.usermodel.*" %>
<%@ page import="org.apache.poi.xssf.usermodel.*" %>
<%@ page import="org.apache.poi.ss.util.CellRangeAddress" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="com.matrixone.apps.common.util.ComponentsUIUtil" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page trimDirectiveWhitespaces="true" %>   <!--关键1：立 自动去除空白 -->
<%@include file = "../common/emxNavigatorNoDocTypeInclude.inc"%>
<%!
    private static final Logger JF_LOGGER = LoggerFactory.getLogger("JF_ESOExportCheckout.jsp");
%>
<%
    // 关键1：立即清空 JSP 输出缓冲区，防止隐式 getWriter()
    if (out != null) {
        out.clear();
        out = null;
    }
    String esoTaskId = (String) emxGetParameter(request, "esoTaskId");
    String projectId = (String) emxGetParameter(request, "objectId");
    String error = ComponentsUIUtil.getI18NString(context, context.getLocale().toString(), "emxComponents.ESO.Error", new String[]{});
    try {
        Map<String, Object> res = JPO.invoke(context, "JF_ESO", null, "exportESOTask", new String[]{esoTaskId, projectId}, Map.class);
        String flag = (String) res.get("flag");

        // ===== 关键2 设置状态 Cookie =====
        jakarta.servlet.http.Cookie statusCookie = new jakarta.servlet.http.Cookie("esoExportStatus", "");
        statusCookie.setPath("/");
        statusCookie.setMaxAge(30); // 30秒过期
        statusCookie.setHttpOnly(false); // 必须 false，JS 才能读

        if (!"Y".equalsIgnoreCase(flag)) {
            String strMess = (String) res.get("Mess");
            if (UIUtil.isNullOrEmpty(strMess)) {
                strMess = error;
            }
            // 转义特殊字符，防止 JS 注入或解析错误
            strMess = strMess.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            String cookieValue = "error:" + strMess;
            //关键：对整个值进行 URL 编码（使用 UTF-8）
            String encodedValue = URLEncoder.encode(cookieValue, StandardCharsets.UTF_8.toString());
            statusCookie.setValue(encodedValue);
            response.addCookie(statusCookie);

//            // 返回错误页面（可选，但 form submit 会显示）
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            response.setContentType("text/html;charset=UTF-8");
//            response.getWriter().write("<html><body><script>alert('导出失败：" + strMess + "');</script></body></html>");
            return;
        } else {
            Workbook workbook = (Workbook) res.get("file");
            String fileName = (String) res.get("fileName");
            if (fileName == null || fileName.isEmpty()) {
                fileName = "ESO_Export.xlsx";
            }

            // 成功：设置 success 状态
            statusCookie.setValue("success");
            response.addCookie(statusCookie);

            // 触发文件下载
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" +
                    URLEncoder.encode(fileName, "UTF-8").replace("+", "%20") + "\"");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);

            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
            } finally {
                workbook.close();
            }
            return;
        }
    } catch (Exception e) {
        e.printStackTrace();
        JF_LOGGER.error("导出 ESO 失败:{}", e);

        // 设置错误 Cookie
        jakarta.servlet.http.Cookie statusCookie = new jakarta.servlet.http.Cookie("esoExportStatus", "");
        statusCookie.setValue("error:" + error);
        statusCookie.setPath("/");
        statusCookie.setMaxAge(30);
        statusCookie.setHttpOnly(false);
        response.addCookie(statusCookie);
//
//        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//        response.setContentType("text/plain;charset=UTF-8");
//        response.getWriter().write("系统错误: " + e.getMessage());
        return;
    }
%>