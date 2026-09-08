<%@ page import="com.google.gson.Gson" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page pageEncoding="UTF-8" %>
<%@include file="./emxNavigatorInclude.inc"%>
<%@include file="./enoviaCSRFTokenValidation.inc"%>
<%
    out.clear();
    response.resetBuffer();
    response.setContentType("application/json; charset=UTF-8");
    response.setHeader("Cache-Control", "no-store");

    Gson gson = new Gson();
    Map result = new HashMap();
    try {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throw new IllegalArgumentException("仅支持POST请求");
        }
        String pageCode = request.getParameter("pageCode");
        String actionCode = request.getParameter("actionCode");
        if (pageCode == null || !pageCode.matches("[A-Z0-9_]{1,100}")
                || actionCode == null || !actionCode.matches("[A-Z0-9_]{1,100}")) {
            throw new IllegalArgumentException("页面编码或动作编码不合法");
        }

        StringBuilder body = new StringBuilder();
        String line;
        java.io.BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        Map params = body.length() == 0 ? new HashMap() : (Map) gson.fromJson(body.toString(), Map.class);
        Map invocationParams = new HashMap();
        invocationParams.put("pageCode", pageCode);
        invocationParams.put("actionCode", actionCode);
        invocationParams.put("params", params);
        Map invocation = (Map) JPO.invoke(context, "JF_LowCodePage", null,
                "prepareActionInvocation", JPO.packArgs(invocationParams), Map.class);
        String jpoName = String.valueOf(invocation.get("jpoName"));
        String methodName = String.valueOf(invocation.get("methodName"));
        Map mappedParams = invocation.get("params") instanceof Map
                ? (Map) invocation.get("params") : new HashMap();
        Object data = JPO.invoke(context, jpoName, null, methodName,
                JPO.packArgs(mappedParams), Object.class);
        String actionKind = String.valueOf(invocation.get("actionKind"));
        String actionName = String.valueOf(invocation.get("actionName"));
        result.put("status", 0);
        result.put("msg", "QUERY".equals(actionKind) ? "" : actionName + "成功");
        result.put("data", data == null ? new HashMap() : data);
    } catch (Exception e) {
        result.put("status", 1);
        result.put("msg", e.getMessage() == null ? "执行失败" : e.getMessage());
        result.put("data", new HashMap());
    }
    out.print(gson.toJson(result));
%>
