<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.apache.commons.fileupload.DiskFileUpload" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page import="com.google.gson.Gson" %>
<%@include file = "./emxNavigatorInclude.inc"%>
<%
    out.clear();
    response.resetBuffer();
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Pragma", "no-cache");
    response.setContentType("application/json; charset=UTF-8");

    Gson gson = new Gson();
    Map result = new HashMap();
    try {
        DiskFileUpload upload = new DiskFileUpload();
        List files = upload.parseRequest(request);
        String objectId = "";
        for (Object itemObj : files) {
            FileItem item = (FileItem) itemObj;
            if (item.isFormField() && "objectId".equals(item.getFieldName())) {
                objectId = item.getString("UTF-8");
            }
        }
        HashMap params = new HashMap();
        params.put("files", files);
        params.put("objectId", objectId);
        result = (Map) JPO.invoke(context, "JF_ProductConfig", null, "importTargetSalesSummaryExcel", JPO.packArgs(params), Map.class);
    } catch (Exception e) {
        result.put("code", "404");
        result.put("mess", e.getMessage());
    }
    out.print(gson.toJson(result));
%>
