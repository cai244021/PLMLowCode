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
        String actionCode = request.getParameter("actionCode");
        String jpoName;
        String methodName;
        if ("CREATE_COMPETITIVE_BOM".equals(actionCode)) {
            jpoName = "JF_CompetitiveBOM";
            methodName = "createCompetitiveBOMLowCode";
        } else if ("QUERY_COMPETITIVE_BOM".equals(actionCode)) {
            jpoName = "JF_CompetitiveBOM";
            methodName = "getCompetitiveBOMListLowCode";
        } else if ("QUERY_CURRENT_USER_DA_LIST".equals(actionCode)) {
            jpoName = "JF_LowCode";
            methodName = "getCurrentUserDAListLowCode";
        } else if ("CREATE_DA".equals(actionCode)) {
            jpoName = "JF_LowCode";
            methodName = "createDALowCode";
        } else if ("DELETE_DA".equals(actionCode)) {
            jpoName = "JF_LowCode";
            methodName = "deleteDALowCode";
        } else if ("QUERY_ATTRIBUTE_RANGE".equals(actionCode)) {
            jpoName = "JF_LowCode";
            methodName = "getAttributeRangeOptionsLowCode";
        } else if ("QUERY_PAGE_FIELD_METADATA".equals(actionCode)) {
            jpoName = "JF_LowCode";
            methodName = "getPageFieldMetadataLowCode";
        } else {
            throw new IllegalArgumentException("不支持的actionCode");
        }

        StringBuilder body = new StringBuilder();
        String line;
        java.io.BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        Map params = body.length() == 0 ? new HashMap() : (Map) gson.fromJson(body.toString(), Map.class);
        Map data = (Map) JPO.invoke(context, jpoName, null, methodName, JPO.packArgs(params), Map.class);
        result.put("status", 0);
        result.put("msg", "DELETE_DA".equals(actionCode) ? "删除成功"
                : (("CREATE_COMPETITIVE_BOM".equals(actionCode) || "CREATE_DA".equals(actionCode))
                ? "创建成功" : ""));
        result.put("data", data);
    } catch (Exception e) {
        result.put("status", 1);
        result.put("msg", e.getMessage() == null ? "执行失败" : e.getMessage());
        result.put("data", new HashMap());
    }
    out.print(gson.toJson(result));
%>
