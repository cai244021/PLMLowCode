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
<%@include file = "../common/emxNavigatorNoDocTypeInclude.inc"%>
<%
    String filePath = DomainConstants.EMPTY_STRING;
    String fileName = DomainConstants.EMPTY_STRING;
    String base64File = DomainConstants.EMPTY_STRING;
    HashMap map = new HashMap<>();
    Gson gson = new Gson();
    try {
        String objectId = emxGetParameter(request, "objectId");
        Map resMap = (Map) JPO.invoke(context, "JF_ExportEBOM", new String[]{objectId}, "exportFormList", new String[]{objectId}, Map.class);
        if (!resMap.isEmpty()) {
            filePath = UIUtil.getValue(resMap, "filePath");
            fileName = UIUtil.getValue(resMap, "fileName");
            if (UIUtil.isNullOrEmpty(filePath)) {
                base64File = "";
            } else {
                InputStream inputStream = new FileInputStream(filePath);
                //2.设置输出流的编码方式
                response.setCharacterEncoding("UTF-8");
                //3.获取输出流
                byte[] bytes = inputStream.readAllBytes();
                base64File = Base64.getEncoder().encodeToString(bytes);
            }
            map.put("flag", "Y");
            map.put("filePath", filePath);
            map.put("fileName", fileName);
            map.put("base64File", base64File);
        } else {
            map.put("flag", "N");
        }
    }catch (Exception e) {
        e.printStackTrace();
    }
%>
<%=gson.toJson(map)%>