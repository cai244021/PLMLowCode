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
        //20260917 update by caipan 双端页面加载委托同一个响应入口。
        String result = (String) JPO.invoke(context, "JF_LowCodePage", null,
                "getPublishedPageContents", JPO.packArgs(params), String.class);
        out.print(result);
    } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        Map error = new HashMap();
        error.put("status", 1);
        error.put("msg", "已发布页面加载失败");
        error.put("data", new HashMap());
        out.print(new Gson().toJson(error));
    }
%>
