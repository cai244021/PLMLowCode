<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.poi.ss.usermodel.Workbook" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="java.util.Base64" %>
<%@include file = "../common/emxNavigatorNoDocTypeInclude.inc"%>
<%
    Gson gson = new Gson();
    HashMap map = new HashMap<>();
    try {
        //返回参数
        Map res = JPO.invoke(context, "JF_ECRService", null, "exportAllECRAndSignTaskList", new String[]{}, Map.class);
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
    }catch (Exception e) {
        e.printStackTrace();
    }
%>
<%=gson.toJson(map)%>