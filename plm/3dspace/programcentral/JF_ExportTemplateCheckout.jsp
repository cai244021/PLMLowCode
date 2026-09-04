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
    try {
        String downloadMod = emxGetParameter(request, "download");
        Map<String, Object> res = new HashMap();
        if ("ECRPart".equalsIgnoreCase(downloadMod)){
            res = JPO.invoke(context, "JF_ProcessExcel", null, "generateTemplate", new String[]{"ECRPartTemplate.xlsx"}, Map.class);
        }
        // 返回参数
        String flag = (String) res.get("flag");
        if ("Y".equalsIgnoreCase(flag)) {
            Workbook workbook = (Workbook) res.get("file");
            String strFileName = (String) res.get("fileName");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            //转换为字节流数组
            byte[] bFileArr = bos.toByteArray();
            String strB64 = Base64.getEncoder().encodeToString(bFileArr);
            map.put("base64File", strB64);
            //清空workbook`
            map.put("fileName", strFileName);
        } else {
            map.put("fileName", "");
        }
    } catch (Exception e) {
        e.printStackTrace();
        map.put("error", "An error occurred: " + e.getMessage()); // 添加错误信息
    }
%>
<%=gson.toJson(map)%>