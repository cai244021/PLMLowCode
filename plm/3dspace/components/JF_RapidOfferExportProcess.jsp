<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.util.Base64" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.util.Map" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.poi.ss.usermodel.Workbook" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@include file = "../common/emxNavigatorNoDocTypeInclude.inc"%>
<%
    String fileName = DomainConstants.EMPTY_STRING;
    HashMap map = new HashMap<>();
    Gson gson = new Gson();
    try {
        String objectId = emxGetParameter(request, "objectId");
        Map resMap = (Map) JPO.invoke(context, "JF_Cost", new String[]{objectId}, "exportFormList", new String[]{objectId}, Map.class);
        String flag = (String) resMap.get("flag");
        if ("Y".equalsIgnoreCase(flag)) {
            Workbook workbook = (Workbook) resMap.get("file");
            fileName = UIUtil.getValue(resMap, "fileName");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);

            //转换为字节流数组
            byte[] bFileArr = bos.toByteArray();
            String strB64 = Base64.getEncoder().encodeToString(bFileArr);

            map.put("fileName", fileName);
            map.put("base64File", strB64);
        } else {
            map.put("fileName", "");
        }
    }catch (Exception e) {
        e.printStackTrace();
    }
%>
<%=gson.toJson(map)%>