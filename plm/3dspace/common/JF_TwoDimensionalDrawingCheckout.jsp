<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.poi.ss.usermodel.Workbook" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="java.util.Base64" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileInputStream" %>
<%@include file = "../common/emxNavigatorNoDocTypeInclude.inc"%>
<%
    Gson gson = new Gson();
    HashMap<String, Object> map = new HashMap<>();
    String objectIds = (String) emxGetParameter(request, "objectIds");
    String objectId = (String) emxGetParameter(request, "objectId");
    try {
        // 返回参数
        Map<String, Object> res = JPO.invoke(context, "JF_PublicProjectQuery", null, "downloadTwoDimensionalDrawing", new String[]{objectIds,objectId}, Map.class);
        String flag = (String) res.get("flag");
        if ("Y".equalsIgnoreCase(flag)) {
            File file = (File) res.get("file"); // 确保 res.get("file") 返回的是 File 类型
            String strFileName = (String) res.get("fileName");
            // 使用 FileInputStream 读取文件内容
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                // 转换为字节流数组
                byte[] bFileArr = bos.toByteArray();
                String strB64 = Base64.getEncoder().encodeToString(bFileArr);
                map.put("base64File", strB64);
                map.put("fileName", strFileName);
            }
        } else {
            map.put("fileName", "");
        }
    } catch (Exception e) {
        e.printStackTrace();
        map.put("error", "An error occurred: " + e.getMessage()); // 添加错误信息
    }
%>
<%=gson.toJson(map)%>