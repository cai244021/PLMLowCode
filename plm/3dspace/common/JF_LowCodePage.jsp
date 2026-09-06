<%@ page import="com.google.gson.Gson" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page pageEncoding="UTF-8" %>
<%@include file="./emxNavigatorInclude.inc"%>
<%
    out.clear();
    response.resetBuffer();
    response.setContentType("application/json; charset=UTF-8");
    response.setHeader("Cache-Control", "no-store");
    try {
        String pageCode = request.getParameter("pageCode");
        if (pageCode == null || !pageCode.matches("[A-Z0-9_]{1,100}")) {
            throw new IllegalArgumentException("页面编码不合法");
        }
        Map params = new HashMap();
        params.put("pageCode", pageCode);
        Map data = (Map) JPO.invoke(context, "JF_LowCodePage", null,
                "getPublishedPage", JPO.packArgs(params), Map.class);
        String contents = String.valueOf(data.get("contents"));
        new Gson().fromJson(contents, Map.class);
        out.print(contents);
    } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        Map error = new HashMap();
        error.put("status", 1);
        error.put("msg", "未找到已发布页面");
        out.print(new Gson().toJson(error));
    }
%>
