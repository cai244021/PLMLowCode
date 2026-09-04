<%@include file = "./emxNavigatorInclude.inc"%>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.google.gson.Gson" %>
<%
    out.clear();
    response.resetBuffer();
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Pragma", "no-cache");
    response.setContentType("application/json; charset=UTF-8");

    Gson gson = new Gson();
    Map resultMap = new HashMap();
    StringBuilder body = new StringBuilder();
    String line = null;
    java.io.BufferedReader reader = request.getReader();
    while ((line = reader.readLine()) != null) {
        body.append(line);
    }

    try {
        Map params = body.length() == 0 ? new HashMap() : (Map) gson.fromJson(body.toString(), Map.class);
        String result = (String) JPO.invoke(context,
                "JF_ProductConfig",
                null,
                "updateProductConfigMatrixCells",
                JPO.packArgs(params),
                String.class);
        out.print(result);
    } catch (Exception e) {
        resultMap.put("code", "404");
        resultMap.put("mess", e.getMessage());
        out.print(gson.toJson(resultMap));
    }
%>
