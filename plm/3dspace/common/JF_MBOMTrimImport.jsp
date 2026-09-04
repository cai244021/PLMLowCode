<%@ page pageEncoding="utf-8" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.commons.fileupload.DiskFileUpload" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@include file="./emxNavigatorInclude.inc"%>
<%
    out.clear();
    response.resetBuffer();
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Pragma", "no-cache");
    response.setContentType("application/json; charset=UTF-8");

    Map result = new HashMap();
    try {
        DiskFileUpload upload = new DiskFileUpload();
        List files = upload.parseRequest(request);
        String objectId = "";
        for (Object fileObject : files) {
            FileItem fileItem = (FileItem) fileObject;
            if (fileItem.isFormField() && "objectId".equals(fileItem.getFieldName())) {
                objectId = fileItem.getString("UTF-8");
                break;
            }
        }
        Map params = new HashMap();
        params.put("files", files);
        params.put("objectId", objectId);
        result = (Map) JPO.invoke(
                context,
                "JF_FormalExportImportMBOM",
                null,
                "formalImportTrimMBOMExcel",
                JPO.packArgs(params),
                Map.class);
    } catch (Exception e) {
        String message = e.getMessage() == null ? e.toString() : e.getMessage();
        result.put("flag", "N");
        result.put("message", message);
        result.put("errors", java.util.Collections.singletonList(message));
    }
    out.print(new Gson().toJson(result));
%>
