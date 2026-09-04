<%@ page import="matrix.db.JPO" %>
<%@ page import="matrix.db.Environment" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileOutputStream" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.UUID" %>
<%@ page import="org.apache.commons.fileupload.DiskFileUpload" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page import="org.apache.poi.ss.usermodel.Workbook" %>
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
    Workbook errorWorkbook = null;
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
        params.put("language", request.getHeader("Accept-Language"));
        result = (Map) JPO.invoke(context, "JF_ProductConfig", null, "importProductConfigExcel", JPO.packArgs(params), Map.class);
        errorWorkbook = (Workbook) result.remove("errorWorkbook");
        if (errorWorkbook != null) {
            String token = UUID.randomUUID().toString().replace("-", "");
            String tempDir = Environment.getValue(context, "TMPDIR");
            if (tempDir == null || tempDir.trim().length() == 0) {
                tempDir = System.getProperty("java.io.tmpdir");
            }
            File target = new File(tempDir, "JFProductConfigImportError_" + token + ".xlsx");
            FileOutputStream fileOutputStream = new FileOutputStream(target);
            try {
                errorWorkbook.write(fileOutputStream);
            } finally {
                fileOutputStream.close();
                errorWorkbook.close();
            }
            session.setAttribute("JFProductConfigImportError." + token, target.getAbsolutePath());
            result.put("errorToken", token);
        }
    } catch (Exception e) {
        result.put("code", "404");
        result.put("mess", e.getMessage());
    }
    out.print(gson.toJson(result));
%>
