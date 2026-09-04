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

    Gson gson = new Gson();
    Map result = new HashMap();
    try {
        String mode = request.getParameter("mode");
        HashMap params = new HashMap();
        if ("preCheck".equalsIgnoreCase(mode)) {
            params.put("productConfigId", request.getParameter("productConfigId"));
            params.put("servicePartsListId", request.getParameter("servicePartsListId"));
            params.put("forceSubmit", request.getParameter("forceSubmit"));
            result = (Map) JPO.invoke(context, "JF_ProductConfig", null, "preCheckProductConfigApproval", JPO.packArgs(params), Map.class);
        } else {
            DiskFileUpload upload = new DiskFileUpload();
            List files = upload.parseRequest(request);
            params.put("files", files);
            for (Object itemObj : files) {
                FileItem item = (FileItem) itemObj;
                if (item.isFormField()) {
                    params.put(item.getFieldName(), item.getString("UTF-8"));
                }
            }
            if ("upload".equalsIgnoreCase(mode)) {
                result = (Map) JPO.invoke(context, "JF_ProductConfig", null, "uploadProductConfigApprovalAttachment", JPO.packArgs(params), Map.class);
            } else {
                result = (Map) JPO.invoke(context, "JF_ProductConfig", null, "createProductConfigApproval", JPO.packArgs(params), Map.class);
            }
        }
    } catch (Exception e) {
        result.put("code", "404");
        result.put("mess", e.getMessage());
    }
    out.print(gson.toJson(result));
%>
